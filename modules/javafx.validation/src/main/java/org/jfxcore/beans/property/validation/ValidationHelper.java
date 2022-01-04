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
import javafx.scene.Node;
import javafx.scene.input.NodeState;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public class ValidationHelper<T, E> implements InvalidationListener {

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

    @SuppressWarnings("rawtypes")
    private static final SerializedConstraintValidator[] EMPTY_CONSTRAINT_VALIDATORS =
        new SerializedConstraintValidator[0];

    @SuppressWarnings("FieldCanBeLocal")
    private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(this);
    private final SerializedConstraintValidator<? super T, E>[] constraintValidators;
    private final ObservableValue<T> observable;
    private final WritableProperty<T> constrainedValue;
    private final BooleanPropertyImpl valid;
    private final BooleanPropertyImpl invalid;
    private final BooleanPropertyImpl userValid;
    private final BooleanPropertyImpl userInvalid;
    private final BooleanPropertyImpl validating;
    private final ReadOnlyListWrapper<E> errors;

    private ReadOnlyBooleanProperty userModified;
    private Map<Constraint<? super T, E>, E> errorMap;
    private int currentlyValidatingCount;

    @SuppressWarnings("unchecked")
    public ValidationHelper(
            ObservableValue<T> observable,
            WritableProperty<T> constrainedValue,
            Constraint<? super T, E>[] constraints) {
        this.observable = observable;
        this.constrainedValue = constrainedValue;

        if (constraints != null && constraints.length > 0) {
            this.constraintValidators = new SerializedConstraintValidator[constraints.length];
        } else {
            this.constraintValidators = EMPTY_CONSTRAINT_VALIDATORS;
        }

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

        errors = new ReadOnlyListWrapper<>(
            observable, "errors", FXCollections.observableList(new ArrayList<>(1)));

        if (constraints != null && constraints.length > 0) {
            for (int i = 0; i < constraints.length; ++i) {
                for (Observable dependency : constraints[i].getDependencies()) {
                    if (!isRecurringDependency(dependency)) {
                        dependency.addListener(weakInvalidationListener);
                    }
                }

                this.constraintValidators[i] = new SerializedConstraintValidator<>(constraints[i]) {
                    @Override
                    protected void onAsyncValidationStarted() {
                        ValidationHelper.this.incrementValidatingCount();
                    }

                    @Override
                    protected void onAsyncValidationEnded() {
                        ValidationHelper.this.decrementValidatingCount();
                    }

                    @Override
                    protected void onValidationCompleted(
                            Constraint<? super T, E> constraint, T value, ValidationResult<E> result, Throwable ex) {
                        ValidationHelper.this.onValidationCompleted(constraint, value, result, ex);
                    }
                };
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

    public ReadOnlyListProperty<E> errorsProperty() {
        return errors.getReadOnlyProperty();
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

        for (SerializedConstraintValidator<? super T, E> constraintValidator : constraintValidators) {
            if (dependency == observable || constraintValidator.isDependency(dependency)) {
                constraintValidator.validate(value);
            }
        }
    }

    private void onValidationCompleted(Constraint<? super T, E> constraint, T value, ValidationResult<E> result, Throwable exception) {
        if (exception instanceof CancellationException) {
            return;
        }

        if (exception != null) {
            Logging.getLogger().severe(
                "Exception in constraint validator",
                exception instanceof CompletionException ? exception.getCause() : exception);
        } else if (result != null && result.isValid()) {
            onSuccessfulValidation(constraint, value);
        } else {
            onFailedValidation(constraint, result);
        }
    }

    private void onSuccessfulValidation(Constraint<? super T, E> constraint, T value) {
        boolean validChanged = false;
        boolean userValidChanged = false;
        boolean invalidChanged = false;
        boolean userInvalidChanged = false;
        boolean constrainedValueChanged = false;

        if (errorMap != null) {
            errorMap.remove(constraint);
        }

        if (!validating.get() && (errorMap == null || errorMap.isEmpty())) {
            validChanged = valid.set(true);
            userValidChanged = userModified != null && userValid.set(userModified.get());
            invalidChanged = invalid.set(false);
            userInvalidChanged = userInvalid.set(false);
            constrainedValueChanged = constrainedValue.setValue(value);
        }

        if (errorMap != null) {
            updateErrors();
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

    private void onFailedValidation(Constraint<? super T, E> constraint, ValidationResult<E> result) {
        if (errorMap == null) {
            errorMap = new HashMap<>(2);
        }

        E current = result.getErrorInfo();
        errorMap.put(constraint, current);
        boolean invalidChanged = invalid.set(true);
        boolean userInvalidChanged = userModified != null && userInvalid.set(userModified.get());

        if (current != null && !errors.contains(current)) {
            errors.add(current);
        }

        if (invalidChanged) {
            invalid.fireValueChangedEvent();
        }

        if (userInvalidChanged) {
            userInvalid.fireValueChangedEvent();
        }
    }

    private void updateErrors() {
        List<E> errors = this.errors.get();
        List<E> distinctErrors = new ArrayList<>(errorMap.size());

        for (E error : errorMap.values()) {
            if (error != null && !distinctErrors.contains(error)) {
                distinctErrors.add(error);
            }
        }

        errors.retainAll(distinctErrors);
    }

    private void incrementValidatingCount() {
        currentlyValidatingCount++;

        if (validating.set(true)) {
            validating.fireValueChangedEvent();
        }
    }

    private void decrementValidatingCount() {
        currentlyValidatingCount--;

        if (currentlyValidatingCount == 0 && validating.set(false)) {
            validating.fireValueChangedEvent();
        }
    }

    private boolean isRecurringDependency(Observable dependency) {
        for (SerializedConstraintValidator<?, ?> constraintValidator : constraintValidators) {
            if (constraintValidator == null) {
                return false;
            }

            if (constraintValidator.isDependency(dependency)) {
                return true;
            }
        }

        return false;
    }

}
