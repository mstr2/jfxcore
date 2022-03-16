/*
 * Copyright (c) 2022, JFXcore. All rights reserved.
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

package org.jfxcore.validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.validation.Constraint;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationResult;
import javafx.validation.ValidationState;
import javafx.validation.Validator;
import javafx.validation.property.ReadOnlyConstrainedProperty;
import javafx.validation.property.ReadOnlyDiagnosticListProperty;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages the validation state of a {@link ReadOnlyConstrainedProperty} and invokes
 * constraint validators when the property value was changed.
 *
 * @param <T> data type
 * @param <D> diagnostic type
 */
public class ValidationHelper<T, D> implements InvalidationListener {

    @SuppressWarnings("rawtypes")
    private static final ValidatorImpl[] NO_VALIDATORS = new ValidatorImpl[0];

    private static final int VALID_FLAG = 1;
    private static final int INVALID_FLAG = 1 << 2;
    private static final int VALIDATING_FLAG = 1 << 4;

    @SuppressWarnings("FieldCanBeLocal")
    private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(this);
    private final ReadOnlyConstrainedProperty<T, D> observable;
    private final DeferredProperty<T> constrainedValue;
    private final ValidatorImpl<T, D>[] validators;

    private List<ValidationListener<? super T, D>> validationListeners;
    private DiagnosticListImpl<D> diagnosticsList;
    private Properties<D> properties;
    private boolean quiescent;
    private int currentlyValidatingCount;
    private int flags;

    private static class Properties<D> {
        private BooleanPropertyImpl valid;
        private BooleanPropertyImpl invalid;
        private BooleanPropertyImpl validating;
        private DiagnosticListPropertyImpl<D> diagnostics;
    }

    public ValidationHelper(
            ReadOnlyConstrainedProperty<T, D> observable,
            DeferredProperty<T> constrainedValue,
            ValidationState initialValidationState,
            Constraint<?, D>[] constraints) {
        this(observable, constrainedValue, initialValidationState, constraints, ConstraintType.SCALAR);
    }

    @SuppressWarnings("unchecked")
    ValidationHelper(
            ReadOnlyConstrainedProperty<T, D> observable,
            DeferredProperty<T> constrainedValue,
            ValidationState initialValidationState,
            Constraint<?, D>[] constraints,
            ConstraintType constraintType) {
        Objects.requireNonNull(initialValidationState, "initialValidationState");

        this.observable = observable;
        this.constrainedValue = constrainedValue;
        int length = 0;

        if (constraints != null) {
            for (Constraint<?, D> constraint : constraints) {
                if (constraintType.checkType(constraint)) {
                    ++length;
                }
            }
        }

        validators = length > 0 ? (ValidatorImpl<T, D>[])Array.newInstance(ValidatorImpl.class, length) : NO_VALIDATORS;

        boolean valid = length == 0 || initialValidationState == ValidationState.VALID;
        setFlag(VALID_FLAG, valid);
        setLastFlag(VALID_FLAG, valid);

        boolean invalid = initialValidationState == ValidationState.INVALID;
        setFlag(INVALID_FLAG, invalid);
        setLastFlag(INVALID_FLAG, invalid);

        if (length > 0) {
            for (int i = 0, j = 0; i < constraints.length && j < length; ++i) {
                if (!constraintType.checkType(constraints[i])) {
                    continue;
                }

                for (Observable dependency : constraints[i].getDependencies()) {
                    if (!isRecurringDependency(dependency)) {
                        dependency.addListener(weakInvalidationListener);
                    }
                }

                validators[j] = new ValidatorImpl<>(this, constraints[i], j);
                j++;
            }
        }
    }

    private boolean isRecurringDependency(Observable dependency) {
        for (var constraintValidator : validators) {
            if (constraintValidator == null) {
                return false;
            }

            if (constraintValidator.isDependency(dependency)) {
                return true;
            }
        }

        return false;
    }

    ReadOnlyConstrainedProperty<T, D> getObservable() {
        return observable;
    }

    public ReadOnlyBooleanProperty validProperty() {
        Properties<D> properties = properties();
        if (properties.valid == null) {
            properties.valid = new BooleanPropertyImpl() {
                @Override public String getName() { return "valid"; }
                @Override public Object getBean() { return observable; }
                @Override public boolean get() { return isFlag(VALID_FLAG); }
            };
        }

        return properties.valid;
    }

    public boolean isValid() {
        return isFlag(VALID_FLAG);
    }

    public ReadOnlyBooleanProperty invalidProperty() {
        Properties<D> properties = properties();
        if (properties.invalid == null) {
            properties.invalid = new BooleanPropertyImpl() {
                @Override public String getName() { return "invalid"; }
                @Override public Object getBean() { return observable; }
                @Override public boolean get() { return isFlag(INVALID_FLAG); }
            };
        }

        return properties.invalid;
    }

    public boolean isInvalid() {
        return isFlag(INVALID_FLAG);
    }

    public ReadOnlyBooleanProperty validatingProperty() {
        Properties<D> properties = properties();
        if (properties.validating == null) {
            properties.validating = new BooleanPropertyImpl() {
                @Override public String getName() { return "validating"; }
                @Override public Object getBean() { return observable; }
                @Override public boolean get() { return isFlag(VALIDATING_FLAG); }
            };
        }

        return properties.validating;
    }

    public boolean isValidating() {
        return isFlag(VALIDATING_FLAG);
    }

    public ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
        Properties<D> properties = properties();
        if (properties.diagnostics == null) {
            properties.diagnostics = new DiagnosticListPropertyImpl<>(observable, getDiagnostics());
        }

        return properties.diagnostics;
    }

    public DiagnosticList<D> getDiagnostics() {
        if (diagnosticsList == null) {
            diagnosticsList = new DiagnosticListImpl<>(validators.length);

            if (quiescent) {
                diagnosticsList.beginQuiescence();
            }
        }

        return diagnosticsList;
    }

    public void addListener(ValidationListener<? super T, D> listener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }

        if (validationListeners == null) {
            validationListeners = new ArrayList<>(1);
        }

        validationListeners.add(listener);
    }

    public void removeListener(ValidationListener<? super T, D> listener) {
        if (validationListeners != null) {
            validationListeners.remove(listener);
        }
    }

    /**
     * Called when a dependency or the observable itself has changed.
     */
    @Override
    public void invalidated(Observable dependency) {
        beginQuiescence();
        onStartValidation(dependency, PropertyHelper.readValue(observable));
        endQuiescence();
    }

    /**
     * Notifies the validation helper that the state of a validator has changed.
     * A well-behaved validator must signal the STARTED state before doing any computations,
     * and signal either SUCCEEDED, FAILED or CANCELLED at the end of its computation.
     *
     * @param state the new state of the validator
     * @param intermediate indicates whether the validator has scheduled a follow-up validation run
     */
    public void notifyValidatorStateChanged(ValidatorState state, boolean intermediate) {
        switch (state) {
            case STARTED -> {
                if (currentlyValidatingCount++ == 0) {
                    setFlag(VALIDATING_FLAG, true);
                    setFlag(VALID_FLAG, false);
                    setFlag(INVALID_FLAG, false);
                }

                if (!quiescent) {
                    fireValidationStateChanged();
                }
            }

            case SUCCEEDED -> {
                if (currentlyValidatingCount <= 0) {
                    throw new IllegalStateException();
                }

                if (--currentlyValidatingCount == 0) {
                    ValidationState validationState = getValidationState();

                    if (!intermediate) {
                        updateValidatingProperties(validationState);
                    }

                    if (validationState == ValidationState.VALID) {
                        constrainedValue.applyValue();
                    }

                    if (!quiescent) {
                        fireValidationStateChanged();
                    }
                }
            }

            case FAILED -> {
                if (currentlyValidatingCount <= 0) {
                    throw new IllegalStateException();
                }

                setFlag(VALID_FLAG, false);
                setFlag(INVALID_FLAG, true);
                setFlag(VALIDATING_FLAG, --currentlyValidatingCount > 0);

                if (!intermediate && !quiescent) {
                    fireValidationStateChanged();
                }
            }

            case CANCELLED -> {
                if (currentlyValidatingCount <= 0) {
                    throw new IllegalStateException();
                }

                if (--currentlyValidatingCount == 0) {
                    setFlag(VALIDATING_FLAG, false);

                    if (!intermediate && !quiescent) {
                        fireValidationStateChanged(VALIDATING_FLAG, ValidationListener.ChangeType.VALIDATING);
                    }
                }
            }
        }
    }

    /**
     * Configures the validation helper to not fire change notifications for the following properties:
     * {@link #validProperty()}, {@link #invalidProperty()}, {@link #validatingProperty()}, {@link #constrainedValue}.
     *
     * Calling this method before starting validators prevents repeatedly toggling the aforementioned
     * properties if the validators complete immediately and synchronously.
     */
    protected void beginQuiescence() {
        if (quiescent) {
            throw new IllegalStateException();
        }

        quiescent = true;

        if (diagnosticsList != null) {
            diagnosticsList.beginQuiescence();
        }
    }

    /**
     * Re-enables change notifications for the properties affected by {@link #beginQuiescence()},
     * and fires change notifications if necessary.
     */
    protected void endQuiescence() {
        if (!quiescent) {
            throw new IllegalStateException();
        }

        quiescent = false;

        if (currentlyValidatingCount == 0) {
            ValidationState validationState = getValidationState();
            updateValidatingProperties(validationState);

            if (validationState == ValidationState.VALID) {
                constrainedValue.applyValue();
            }
        }

        if (diagnosticsList != null) {
            diagnosticsList.endQuiescence();
        }

        fireValidationStateChanged();
    }

    /**
     * Occurs when a dependency (or the observable value itself) was invalidated, and a new validation
     * run begins. Extending classes can override this method to start additional validators.
     */
    protected void onStartValidation(Observable dependency, T newValue) {
        if (validators.length > 0) {
            for (var validator : validators) {
                if (dependency == observable || validator.isDependency(dependency)) {
                    validator.validate(newValue);
                }
            }
        } else {
            constrainedValue.storeValue(newValue);
        }
    }

    /**
     * Determines the current {@link ValidationState} of the validation, which is
     * <ol>
     *     <li>{@link ValidationState#VALID} if all validators have successfully completed,
     *     <li>{@link ValidationState#INVALID} if at least one validator has failed,
     *     <li>{@link ValidationState#UNKNOWN} otherwise.
     * </ol>
     */
    protected ValidationState getValidationState() {
        boolean unknown = false;

        for (var validator : validators) {
            ValidationResult<D> lastResult = validator.getValidationResult();
            if (lastResult == null) {
                unknown = true;
            } else if (!lastResult.isValid()) {
                return ValidationState.INVALID;
            }
        }

        return unknown ? ValidationState.UNKNOWN : ValidationState.VALID;
    }

    private void updateValidatingProperties(ValidationState validState) {
        setFlag(VALIDATING_FLAG, false);
        setFlag(VALID_FLAG, validState == ValidationState.VALID);
        setFlag(INVALID_FLAG, validState == ValidationState.INVALID);
    }

    private void fireValidationStateChanged() {
        fireValidationStateChanged(VALID_FLAG, ValidationListener.ChangeType.VALID);
        fireValidationStateChanged(INVALID_FLAG, ValidationListener.ChangeType.INVALID);
        fireValidationStateChanged(VALIDATING_FLAG, ValidationListener.ChangeType.VALIDATING);
    }

    private void fireValidationStateChanged(int flag, ValidationListener.ChangeType changeType) {
        boolean oldValue = isLastFlag(flag), newValue = isFlag(flag);
        if (oldValue != newValue) {
            setLastFlag(flag, newValue);
            fireValidationStateChanged(changeType, oldValue, newValue);
        }
    }

    private void fireValidationStateChanged(
            ValidationListener.ChangeType changeType, boolean oldValue, boolean newValue) {
        if (validationListeners != null) {
            for (ValidationListener<? super T, D> validationListener : validationListeners) {
                validationListener.changed(observable, changeType, oldValue, newValue);
            }
        }

        if (properties != null) {
            switch (changeType) {
                case VALID -> fireValueChangedEvent(properties.valid);
                case INVALID -> fireValueChangedEvent(properties.invalid);
                case VALIDATING -> fireValueChangedEvent(properties.validating);
            }
        }
    }

    private Properties<D> properties() {
        if (properties == null) {
            properties = new Properties<>();
        }
        return properties;
    }

    private void fireValueChangedEvent(BooleanPropertyImpl property) {
        if (property != null) {
            property.fireValueChangedEvent();
        }
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    private boolean isFlag(int flag) {
        return (flags & flag) != 0;
    }

    private boolean isLastFlag(int flag) {
        return isFlag(flag << 1);
    }

    private void setLastFlag(int flag, boolean value) {
        setFlag(flag << 1, value);
    }

    private static class ValidatorImpl<T, D> extends SerializedValidator<T, D> {
        private final ValidationHelper<T, D> helper;
        private final Constraint<?, D> constraint;
        private final int index;
        private ValidationResult<D> validationResult;

        @SuppressWarnings("unchecked")
        ValidatorImpl(ValidationHelper<T, D> helper, Constraint<?, D> constraint, int index) {
            super((Validator<? super T, D>)constraint.getValidator(), constraint.getCompletionExecutor());
            this.helper = helper;
            this.constraint = constraint;
            this.index = index;
        }

        public boolean isDependency(Observable observable) {
            return constraint.isDependency(observable);
        }

        @Override
        public ValidationResult<D> getValidationResult() {
            return validationResult;
        }

        @Override
        protected void onValidationStarted() {
            if (helper.diagnosticsList != null) {
                helper.diagnosticsList.clearDiagnostic(index);
            }

            helper.notifyValidatorStateChanged(ValidatorState.STARTED, false);
        }

        @Override
        protected void onValidationCompleted(T value, ValidationResult<D> result, boolean intermediateCompletion) {
            validationResult = result;

            if (result == null) {
                helper.notifyValidatorStateChanged(ValidatorState.CANCELLED, intermediateCompletion);
            } else if (result.isValid()) {
                updateDiagnostic(result, true);
                helper.constrainedValue.storeValue(value);
                helper.notifyValidatorStateChanged(ValidatorState.SUCCEEDED, intermediateCompletion);
            } else {
                updateDiagnostic(result, false);
                helper.notifyValidatorStateChanged(ValidatorState.FAILED, intermediateCompletion);
            }
        }

        private void updateDiagnostic(ValidationResult<D> result, boolean valid) {
            D diagnostic = result.getDiagnostic();
            if (diagnostic != null) {
                ((DiagnosticListImpl<D>)helper.getDiagnostics()).setDiagnostic(index, diagnostic, valid);
            } else if (helper.diagnosticsList != null) {
                helper.diagnosticsList.clearDiagnostic(index);
            }
        }
    }

    private static abstract class BooleanPropertyImpl extends ReadOnlyBooleanPropertyBase {
        @Override
        protected final void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

}
