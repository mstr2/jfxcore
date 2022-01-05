/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jfxcore.beans.property.validation;

import com.sun.javafx.binding.Logging;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.validation.Constraint;
import javafx.beans.property.validation.ReadOnlyConstrainedProperty;
import javafx.beans.property.validation.ValidationResult;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableListBase;
import javafx.scene.Node;
import javafx.scene.input.NodeState;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public class ValidationHelper<T, D> implements InvalidationListener {

    private static final List<Pair<Class<?>, Accessor>> accessors = new ArrayList<>();

    public static void setAccessor(Class<?> clazz, Accessor accessor) {
        ValidationHelper.accessors.add(new Pair<>(clazz, accessor));
    }

    public static ValidationHelper<?, ?> getValidationHelper(ReadOnlyConstrainedProperty<?, ?> property) {
        for (Pair<Class<?>, Accessor> pair : accessors) {
            if (pair.getKey().isInstance(property)) {
                return pair.getValue().getValidationHelper(property);
            }
        }

        throw new IllegalArgumentException();
    }

    public interface Accessor {
        ValidationHelper<?, ?> getValidationHelper(ReadOnlyConstrainedProperty<?, ?> property);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(this);
    private final ConstraintValidatorImpl[] constraintValidators;
    private final ObservableValue<T> observable;
    private final WritableProperty<T> constrainedValue;
    private final BooleanPropertyImpl valid;
    private final BooleanPropertyImpl invalid;
    private final BooleanPropertyImpl userValid;
    private final BooleanPropertyImpl userInvalid;
    private final BooleanPropertyImpl validating;

    private DiagnosticList errorList;
    private DiagnosticList warningsList;
    private ReadOnlyListWrapper<D> errors;
    private ReadOnlyListWrapper<D> warnings;
    private ReadOnlyBooleanProperty userModified;
    private int currentlyValidatingCount;

    @SuppressWarnings("unchecked")
    public ValidationHelper(
            ObservableValue<T> observable,
            WritableProperty<T> constrainedValue,
            Constraint<? super T, D>[] constraints) {
        this.observable = observable;
        this.constrainedValue = constrainedValue;

        int length = constraints != null ? constraints.length : 0;
        this.constraintValidators = (ConstraintValidatorImpl[])Array.newInstance(ConstraintValidatorImpl.class, length);

        valid = new BooleanPropertyImpl(constraints == null || constraints.length == 0) {
            @Override public String getName() { return "valid"; }
            @Override public Object getBean() { return ValidationHelper.this; }
        };

        invalid = new BooleanPropertyImpl(false) {
            @Override public String getName() { return "invalid"; }
            @Override public Object getBean() { return ValidationHelper.this; }
        };

        userValid = new BooleanPropertyImpl(false) {
            @Override public String getName() { return "userValid"; }
            @Override public Object getBean() { return ValidationHelper.this; }
        };

        userInvalid = new BooleanPropertyImpl(false) {
            @Override public String getName() { return "userInvalid"; }
            @Override public Object getBean() { return ValidationHelper.this; }
        };

        validating = new BooleanPropertyImpl(false) {
            @Override public String getName() { return "validating"; }
            @Override public Object getBean() { return ValidationHelper.this; }
        };

        if (constraints != null && constraints.length > 0) {
            for (int i = 0; i < constraints.length; ++i) {
                for (Observable dependency : constraints[i].getDependencies()) {
                    if (!isRecurringDependency(dependency)) {
                        dependency.addListener(weakInvalidationListener);
                    }
                }

                this.constraintValidators[i] = new ConstraintValidatorImpl(constraints[i]);
            }

            invalidated(observable);
        }

        observable.addListener(this);
    }

    public BooleanPropertyImpl validProperty() {
        return valid;
    }

    public BooleanPropertyImpl invalidProperty() {
        return invalid;
    }

    public BooleanPropertyImpl userValidProperty() {
        return userValid;
    }

    public BooleanPropertyImpl userInvalidProperty() {
        return userInvalid;
    }

    public BooleanPropertyImpl validatingProperty() {
        return validating;
    }

    public ReadOnlyListProperty<D> errorsProperty() {
        if (errors == null) {
            errorList = new DiagnosticList(false);
            errors = new ReadOnlyListWrapper<>(
                observable, "errors", FXCollections.unmodifiableObservableList(errorList));
        }

        return errors.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<D> warningsProperty() {
        if (warnings == null) {
            warningsList = new DiagnosticList(true);
            warnings = new ReadOnlyListWrapper<>(
                observable, "warnings", FXCollections.unmodifiableObservableList(warningsList));
        }

        return warnings.getReadOnlyProperty();
    }

    public void connect(Node node) {
        if (userModified != null) {
            throw new IllegalStateException(
                observable + " can only provide validation state for a single node; " +
                "currently targeting " + userModified.getBean());
        }

        userModified = NodeState.userModifiedProperty(node);
        userModified.addListener(weakInvalidationListener);
        invalidated(userModified);
    }

    public void disconnect() {
        userModified.removeListener(weakInvalidationListener);
        userModified = null;
    }

    @Override
    public void invalidated(Observable dependency) {
        if (dependency == userModified) {
            onUserModifiedInvalidated(userModified.get());
        } else {
            onDependencyInvalidated(dependency);
        }
    }

    private void onUserModifiedInvalidated(boolean isUserModified) {
        if (invalid.get() && userInvalid.set(isUserModified)) {
            userInvalid.fireValueChangedEvent();
        }

        if (valid.get() && userValid.set(isUserModified)) {
            userValid.fireValueChangedEvent();
        }
    }

    private void onDependencyInvalidated(Observable dependency) {
        T value = observable.getValue();

        if (constraintValidators.length == 0) {
            boolean constrainedValueChanged = constrainedValue.setValue(value);
            if (constrainedValueChanged) {
                constrainedValue.fireValueChangedEvent();
            }

            return;
        }

        boolean validChanged = valid.set(false);
        boolean userValidChanged = userValid.set(false);

        if (validChanged) {
            valid.fireValueChangedEvent();
        }

        if (userValidChanged) {
            userValid.fireValueChangedEvent();
        }

        for (var constraintValidator : constraintValidators) {
            if (dependency == observable || constraintValidator.isDependency(dependency)) {
                constraintValidator.validate(value);
            }
        }
    }

    private boolean isRecurringDependency(Observable dependency) {
        for (var constraintValidator : constraintValidators) {
            if (constraintValidator == null) {
                return false;
            }

            if (constraintValidator.isDependency(dependency)) {
                return true;
            }
        }

        return false;
    }

    private class DiagnosticList extends ObservableListBase<D> {
        final List<D> backingList = new ArrayList<>(constraintValidators.length);
        final boolean isWarningList;

        DiagnosticList(boolean isWarningList) {
            this.isWarningList = isWarningList;

            beginChange();

            for (var constraintValidator : constraintValidators) {
                ValidationResult<D> lastResult = constraintValidator.lastResult;

                if (lastResult != null
                        && lastResult.getDiagnostic() != null
                        && lastResult.isValid() == isWarningList) {
                    int size = backingList.size();
                    backingList.add(lastResult.getDiagnostic());
                    constraintValidator.lastDiagnosticIndex = size;
                    constraintValidator.lastDiagnosticIsWarning = isWarningList;
                    nextAdd(size, size + 1);
                }
            }

            endChange();
        }

        public void add(ConstraintValidatorImpl validator, D diagnostic) {
            beginChange();

            int index = validator.lastDiagnosticIndex;
            if (index >= 0) {
                if (!Objects.equals(backingList.get(index), diagnostic)) {
                    nextSet(index, backingList.set(index, diagnostic));
                }
            } else {
                int size = backingList.size();
                backingList.add(diagnostic);
                validator.lastDiagnosticIndex = size;
                validator.lastDiagnosticIsWarning = isWarningList;

                for (var constraintValidator : constraintValidators) {
                    if (constraintValidator != validator
                            && constraintValidator.lastDiagnosticIsWarning == isWarningList
                            && constraintValidator.lastDiagnosticIndex > size) {
                        ++constraintValidator.lastDiagnosticIndex;
                    }
                }

                nextAdd(size, size + 1);
            }

            endChange();
        }

        public void remove(ConstraintValidatorImpl validator) {
            if (validator.lastDiagnosticIsWarning != isWarningList) {
                return;
            }

            int index = validator.lastDiagnosticIndex;
            if (index >= 0) {
                validator.lastDiagnosticIndex = -1;

                for (var constraintValidator : constraintValidators) {
                    if (constraintValidator != validator
                            && constraintValidator.lastDiagnosticIsWarning == isWarningList
                            && constraintValidator.lastDiagnosticIndex > index) {
                        --constraintValidator.lastDiagnosticIndex;
                    }
                }

                beginChange();
                nextRemove(index, backingList.remove(index));
                endChange();
            }
        }

        @Override
        public D get(int index) {
            return backingList.get(index);
        }

        @Override
        public int size() {
            return backingList.size();
        }
    }

    private class ConstraintValidatorImpl extends SerializedConstraintValidator<T, D> {
        /**
         * If the last validation produced a diagnostic, this is the index of the diagnostic
         * in the errors or warnings list.
         */
        int lastDiagnosticIndex = -1;

        /**
         * If the last validation produced a diagnostic, this flag specifies whether the
         * diagnostic is in the errors or warnings list.
         */
        boolean lastDiagnosticIsWarning;

        ValidationResult<D> lastResult;

        ConstraintValidatorImpl(Constraint<? super T, D> constraint) {
            super(constraint);
        }

        @Override
        protected void onAsyncValidationStarted() {
            currentlyValidatingCount++;

            if (validating.set(true)) {
                validating.fireValueChangedEvent();
            }
        }

        @Override
        protected void onAsyncValidationEnded() {
            currentlyValidatingCount--;

            if (currentlyValidatingCount == 0 && validating.set(false)) {
                validating.fireValueChangedEvent();
            }
        }

        @Override
        protected void onValidationCompleted(T value, ValidationResult<D> result, Throwable exception) {
            if (exception instanceof CancellationException) {
                return;
            }

            if (result == null) {
                result = new ValidationResult<>(false);
            }

            lastResult = result;

            if (exception != null) {
                Logging.getLogger().severe(
                    "Exception in constraint validator",
                    exception instanceof CompletionException ? exception.getCause() : exception);
            } else if (result.isValid()) {
                onSuccessfulValidation(value, result);
            } else {
                onFailedValidation(result);
            }
        }

        private void onSuccessfulValidation(T value, ValidationResult<D> result) {
            boolean validChanged = false;
            boolean userValidChanged = false;
            boolean invalidChanged = false;
            boolean userInvalidChanged = false;
            boolean constrainedValueChanged = false;

            if (errorList != null) {
                errorList.remove(this);
            }

            if (warningsList != null) {
                D diagnostic = result.getDiagnostic();
                if (diagnostic != null) {
                    warningsList.add(this, diagnostic);
                } else {
                    warningsList.remove(this);
                }
            }

            if (!validating.get() && checkValid()) {
                validChanged = valid.set(true);
                userValidChanged = userModified != null && userValid.set(userModified.get());
                invalidChanged = invalid.set(false);
                userInvalidChanged = userInvalid.set(false);
                constrainedValueChanged = constrainedValue.setValue(value);
            }

            if (validChanged) {
                valid.fireValueChangedEvent();
            }

            if (invalidChanged) {
                invalid.fireValueChangedEvent();
            }

            if (userValidChanged) {
                userValid.fireValueChangedEvent();
            }

            if (userInvalidChanged) {
                userInvalid.fireValueChangedEvent();
            }

            if (constrainedValueChanged) {
                constrainedValue.fireValueChangedEvent();
            }
        }

        private void onFailedValidation(ValidationResult<D> result) {
            boolean invalidChanged = invalid.set(true);
            boolean userInvalidChanged = userModified != null && userInvalid.set(userModified.get());

            if (warningsList != null) {
                warningsList.remove(this);
            }

            if (errorList != null) {
                D diagnostic = result.getDiagnostic();
                if (diagnostic != null) {
                    errorList.add(this, diagnostic);
                } else {
                    errorList.remove(this);
                }
            }

            if (invalidChanged) {
                invalid.fireValueChangedEvent();
            }

            if (userInvalidChanged) {
                userInvalid.fireValueChangedEvent();
            }
        }

        private boolean checkValid() {
            for (var constraintValidator : constraintValidators) {
                ValidationResult<D> lastResult = constraintValidator.lastResult;
                if (lastResult != null && !lastResult.isValid()) {
                    return false;
                }
            }

            return true;
        }
    }

}
