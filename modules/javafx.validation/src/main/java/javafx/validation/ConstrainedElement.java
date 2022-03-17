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

package javafx.validation;

import org.jfxcore.validation.ConstrainedElementHelper;
import org.jfxcore.validation.DiagnosticListImpl;
import org.jfxcore.validation.DiagnosticListPropertyImpl;
import org.jfxcore.validation.ElementValidationHelper;
import org.jfxcore.validation.SerializedValidator;
import org.jfxcore.validation.ValidatorState;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.util.Incubating;
import javafx.validation.property.ReadOnlyDiagnosticListProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Defines methods and properties of constrained collection elements.
 *
 * @param <T> data type
 * @param <D> diagnostic type
 */
@Incubating
public final class ConstrainedElement<T, D> implements ConstrainedValue<T, D> {

    private static final SerializedValidator<?, ?> VALID_RESULT = new SerializedValidator<>(null) {
        @Override public ValidationResult<Object> getValidationResult() { return ValidationResult.valid(); }
        @Override protected CompletableFuture<ValidationResult<Object>> newValidationRun(Object value) { return null; }
        @Override protected void onValidationStarted() {}
        @Override protected void onValidationCompleted(Object value, ValidationResult<Object> result, boolean intermediate) {}
        @Override public String toString() { return "valid"; }
    };

    private static final SerializedValidator<?, ?> INVALID_RESULT = new SerializedValidator<>(null) {
        @Override public ValidationResult<Object> getValidationResult() { return ValidationResult.invalid(); }
        @Override protected CompletableFuture<ValidationResult<Object>> newValidationRun(Object value) { return null; }
        @Override protected void onValidationStarted() {}
        @Override protected void onValidationCompleted(Object value, ValidationResult<Object> result, boolean intermediate) {}
        @Override public String toString() { return "invalid"; }
    };

    private static final SerializedValidator<?, ?> NO_RESULT = new SerializedValidator<>(null) {
        @Override protected CompletableFuture<ValidationResult<Object>> newValidationRun(Object value) { return null; }
        @Override public ValidationResult<Object> getValidationResult() { return ValidationResult.none(); }
        @Override protected void onValidationStarted() {}
        @Override protected void onValidationCompleted(Object value, ValidationResult<Object> result, boolean intermediate) {}
        @Override public String toString() { return "none"; }
    };

    private static final int VALID_FLAG = 1;
    private static final int INVALID_FLAG = 1 << 2;
    private static final int VALIDATING_FLAG = 1 << 3;
    private static final int SUPPRESS_CHANGE_EVENT_FLAG = 1 << 4;

    static {
        ConstrainedElementHelper.setAccessor(new ConstrainedElementHelper.Accessor() {
            @Override
            public <T0, D0> ConstrainedElement<T0, D0> newInstance(
                    T0 value, ElementValidationHelper<T0, D0> validationHelper) {
                return new ConstrainedElement<>(value, validationHelper);
            }

            @Override
            public <T0, D0> void dispose(ConstrainedElement<T0, D0> element) {
                element.dispose();
            }

            @Override
            public <T0, D0> void validate(ConstrainedElement<T0, D0> element) {
                element.validate();
            }
        });
    }

    private final T value;
    private int flags;
    private List<ValidationListener<? super T, D>> validationListeners;
    private ElementValidationHelper<T, D> validationHelper;
    private SerializedValidator<T, D>[] validators;
    private DiagnosticListImpl<D> diagnostics;
    private Observables observables;
    private int currentlyValidatingCount;

    /**
     * Properties that might not be used by user code will be allocated on demand.
     */
    private class Observables {
        private BooleanPropertyImpl invalidProperty;
        private BooleanPropertyImpl validProperty;
        private BooleanPropertyImpl validatingProperty;
        private DiagnosticListPropertyImpl<D> diagnosticsProperty;
    }

    private ConstrainedElement(T value, ElementValidationHelper<T, D> validationHelper) {
        this.value = value;
        this.validationHelper = validationHelper;
        setFlag(VALID_FLAG, validationHelper.getElementConstraints().length == 0);
    }

    private void dispose() {
        for (int i = 0; i < currentlyValidatingCount; ++i) {
            validationHelper.notifyValidatorStateChanged(ValidatorState.CANCELLED, false);
        }

        // We don't wait for the validators to terminate, but instead null out the validationHelper
        // field to prevent running validators from modifying the validation helper at a later time.
        validationHelper = null;

        if (validators != null) {
            for (var validator : validators) {
                validator.dispose();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validate() {
        Constraint<? super T, D>[] constraints = validationHelper.getElementConstraints();
        if (constraints.length == 0) {
            return;
        }

        if (validators == null) {
            validators = new SerializedValidator[constraints.length];
        }

        boolean oldValid = isFlag(VALID_FLAG);
        boolean oldInvalid = isFlag(INVALID_FLAG);
        setFlag(VALID_FLAG, false);

        // Validators either complete immediately (synchronously), or at a later time (asynchronously).
        // Since synchronous validators run one at a time, they will repeatedly toggle properties like
        // 'validating' or 'valid'. To prevent that, we suppress change notifications while the validators
        // are invoked, and fire the final change notification later.
        setFlag(SUPPRESS_CHANGE_EVENT_FLAG, true);

        for (int i = 0; i < constraints.length; ++i) {
            SerializedValidator<T, D> validator = validators[i];
            if (validator == null
                    || validator == VALID_RESULT
                    || validator == INVALID_RESULT
                    || validator == NO_RESULT) {
                validators[i] = new ValidatorImpl(i, constraints[i]);
            }

            validators[i].validate(value);
        }

        setFlag(SUPPRESS_CHANGE_EVENT_FLAG, false);

        // We only set the 'validating' flag if the validators didn't immediately complete and are
        // still validating. Since a synchronous validator always completes immediately, it will
        // never toggle the 'validating' property.
        if (currentlyValidatingCount > 0 && setFlag(VALIDATING_FLAG, true)) {
            fireValidationListener(ValidationListener.ChangeType.VALIDATING);

            if (observables != null && observables.validatingProperty != null) {
                observables.validatingProperty.fireValueChangedEvent();
            }
        }

        if (oldValid != isFlag(VALID_FLAG)) {
            fireValidationListener(ValidationListener.ChangeType.VALID);

            if (observables != null && observables.validProperty != null) {
                observables.validProperty.fireValueChangedEvent();
            }
        }

        if (oldInvalid != isFlag(INVALID_FLAG)) {
            fireValidationListener(ValidationListener.ChangeType.INVALID);

            if (observables != null && observables.invalidProperty != null) {
                observables.invalidProperty.fireValueChangedEvent();
            }
        }
    }

    private void notifyValidatorStateChanged(ValidatorState state, boolean intermediate) {
        if (validationHelper != null) {
            validationHelper.notifyValidatorStateChanged(state, intermediate);
        }
    }

    @Override
    public void addListener(ValidationListener<? super T, D> listener) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }

        if (validationListeners == null) {
            validationListeners = new ArrayList<>(1);
        }

        validationListeners.add(listener);
    }

    @Override
    public void removeListener(ValidationListener<? super T, D> listener) {
        if (validationListeners != null) {
            validationListeners.remove(listener);
        }
    }

    /**
     * Gets the value of this element.
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Indicates whether the element value is currently known to be valid.
     * <p>
     * The element value is valid if all constraint validators have successfully completed.
     */
    public ReadOnlyBooleanProperty validProperty() {
        Observables observables = getObservables();
        if (observables.validProperty == null) {
            observables.validProperty = new BooleanPropertyImpl() {
                @Override
                public boolean get() {
                    return isFlag(VALID_FLAG);
                }

                @Override
                public Object getBean() {
                    return ConstrainedElement.this;
                }

                @Override
                public String getName() {
                    return "valid";
                }
            };
        }

        return observables.validProperty;
    }

    /**
     * Gets the value of the {@link #validProperty() valid} property.
     */
    @Override
    public boolean isValid() {
        return isFlag(VALID_FLAG);
    }

    /**
     * Indicates whether the element value is currently known to be invalid.
     * <p>
     * The element value is invalid if at least one constraint has been violated, independently of
     * whether other constraint validators have already completed validation.
     */
    public ReadOnlyBooleanProperty invalidProperty() {
        Observables observables = getObservables();
        if (observables.invalidProperty == null) {
            observables.invalidProperty = new BooleanPropertyImpl() {
                @Override
                public boolean get() {
                    return isFlag(INVALID_FLAG);
                }

                @Override
                public Object getBean() {
                    return ConstrainedElement.this;
                }

                @Override
                public String getName() {
                    return "invalid";
                }
            };
        }

        return observables.invalidProperty;
    }

    /**
     * Gets the value of the {@link #invalidProperty() invalid} property.
     */
    @Override
    public boolean isInvalid() {
        return isFlag(INVALID_FLAG);
    }

    /**
     * Indicates whether the element value is currently being validated.
     */
    public ReadOnlyBooleanProperty validatingProperty() {
        Observables observables = getObservables();
        if (observables.validatingProperty == null) {
            observables.validatingProperty = new BooleanPropertyImpl() {
                @Override
                public boolean get() {
                    return isFlag(VALIDATING_FLAG);
                }

                @Override
                public Object getBean() {
                    return ConstrainedElement.this;
                }

                @Override
                public String getName() {
                    return "validating";
                }
            };
        }

        return observables.validatingProperty;
    }

    /**
     * Gets the value of the {@link #validatingProperty() validating} property.
     */
    @Override
    public boolean isValidating() {
        return isFlag(VALIDATING_FLAG);
    }

    /**
     * Contains a list of validation diagnostics.
     * <p>
     * A {@link Constraint} validator may generate a diagnostic as part of the returned {@link ValidationResult}.
     * Diagnostics are application-specified data objects that can be used to provide information about
     * the validated value. For example, if a value fails to validate, the validator may want to provide
     * an error message for the invalid value.
     * <p>
     * All diagnostics that were generated by constraint validators during a validation run are surfaced
     * in this list. Since diagnostics are optional and can be generated regardless of whether the value
     * is valid or invalid, the presence or absence of diagnostics does not necessarily imply that the
     * validated value is either valid or invalid.
     * <p>
     * Diagnostics in this list are not retained across subsequent validation runs: when a constraint
     * is re-evaluated, the diagnostic that was generated in the previous validation run is removed.
     * This means that the diagnostic list will never contain multiple diagnostics from a single
     * constraint validator.
     * <p>
     * For ease of use, the returned diagnostics list provides two sublist views:
     * <ul>
     *     <li>{@link DiagnosticList#validSubList()}, which only includes diagnostics of constraint
     *         validators that successfully validated the value
     *     <li>{@link DiagnosticList#invalidSubList()}, which only includes diagnostics of constraint
     *         validators that failed to validate the value
     * </ul>
     */
    public ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
        Observables observables = getObservables();
        if (observables.diagnosticsProperty == null) {
            if (diagnostics == null) {
                diagnostics = new DiagnosticListImpl<>(validators.length);
            }

            observables.diagnosticsProperty = new DiagnosticListPropertyImpl<>(this, diagnostics);
        }

        return observables.diagnosticsProperty;
    }

    /**
     * Gets the value of the {@link #diagnosticsProperty() diagnostics} property.
     */
    @Override
    public DiagnosticList<D> getDiagnostics() {
        if (diagnostics == null) {
            diagnostics = new DiagnosticListImpl<>(validators.length);
        }

        return diagnostics;
    }

    private boolean setFlag(int flag, boolean value) {
        boolean changed = ((flags & flag) != 0) ^ value;

        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }

        return changed;
    }

    private boolean isFlag(int flag) {
        return (flags & flag) != 0;
    }

    private Observables getObservables() {
        if (observables != null) {
            return observables;
        }

        return observables = new Observables();
    }

    private ValidationState getValidationState() {
        boolean unknown = false;

        if (validators != null) {
            for (var validator : validators) {
                ValidationResult<D> lastResult = validator.getValidationResult();
                if (lastResult == null || lastResult == ValidationResult.none()) {
                    unknown = true;
                } else if (!lastResult.isValid()) {
                    return ValidationState.INVALID;
                }
            }
        }

        return unknown ? ValidationState.UNKNOWN : ValidationState.VALID;
    }

    private void fireValidationListener(ValidationListener.ChangeType changeType) {
        if (validationListeners != null) {
            boolean value = isFlag(switch (changeType) {
                case VALID -> VALID_FLAG;
                case INVALID -> INVALID_FLAG;
                case VALIDATING -> VALIDATING_FLAG;
            });

            for (ValidationListener<? super T, D> validationListener : validationListeners) {
                validationListener.changed(ConstrainedElement.this, changeType, !value, value);
            }
        }
    }

    private class ValidatorImpl extends SerializedValidator<T, D> {
        final int index;
        final Function<T, CompletableFuture<ValidationResult<D>>> validateFunc;
        ValidationResult<D> validationResult;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public ValidatorImpl(int index, ConstraintBase<?, D> constraint) {
            super(constraint);
            this.index = index;

            if (constraint instanceof Constraint c) {
                validateFunc = value -> c.validate(value);
            } else if (constraint instanceof ListConstraint c) {
                validateFunc = value -> c.validate((List)value);
            } else if (constraint instanceof SetConstraint c) {
                validateFunc = value -> c.validate((Set)value);
            } else if (constraint instanceof MapConstraint c) {
                validateFunc = value -> c.validate((Map)value);
            } else {
                throw new IllegalArgumentException("constraint");
            }
        }

        @Override
        public ValidationResult<D> getValidationResult() {
            return validationResult;
        }

        @Override
        protected CompletableFuture<ValidationResult<D>> newValidationRun(T value) {
            return validateFunc.apply(value);
        }

        @Override
        protected void onValidationStarted() {
            notifyValidatorStateChanged(ValidatorState.STARTED, false);
            ++currentlyValidatingCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void onValidationCompleted(T value, ValidationResult<D> result, boolean intermediateCompletion) {
            boolean validatingChanged = --currentlyValidatingCount == 0 && setFlag(VALIDATING_FLAG, false);
            validationResult = result;

            if (result == null) {
                onCancelledValidation(validatingChanged, intermediateCompletion);
                validators[index] = (SerializedValidator<T, D>)NO_RESULT;
            } else if (result.isValid()) {
                onSuccessfulValidation(result, validatingChanged, intermediateCompletion);
                validators[index] = (SerializedValidator<T, D>)VALID_RESULT;
            } else {
                onFailedValidation(result, validatingChanged, intermediateCompletion);
                validators[index] = (SerializedValidator<T, D>)INVALID_RESULT;
            }
        }

        private void onCancelledValidation(boolean validatingChanged, boolean intermediateCompletion) {
            fireValueChangedEvent(false, false, validatingChanged);
            notifyValidatorStateChanged(ValidatorState.CANCELLED, intermediateCompletion);
        }

        private void onSuccessfulValidation(
                ValidationResult<D> result, boolean validatingChanged, boolean intermediateCompletion) {
            boolean validChanged = false;
            boolean invalidChanged = false;

            if (!isFlag(VALIDATING_FLAG)) {
                ValidationState validationState = getValidationState();
                validChanged = setFlag(VALID_FLAG, validationState == ValidationState.VALID);
                invalidChanged = setFlag(INVALID_FLAG, validationState == ValidationState.INVALID);
            }

            handleDiagnostic(result, true);
            fireValueChangedEvent(validChanged, invalidChanged, validatingChanged);
            notifyValidatorStateChanged(ValidatorState.SUCCEEDED, intermediateCompletion);
        }

        private void onFailedValidation(
                ValidationResult<D> result, boolean validatingChanged, boolean intermediateCompletion) {
            boolean validChanged = setFlag(VALID_FLAG, false);
            boolean invalidChanged = setFlag(INVALID_FLAG, true);

            handleDiagnostic(result, false);
            notifyValidatorStateChanged(ValidatorState.FAILED, intermediateCompletion);
            fireValueChangedEvent(validChanged, invalidChanged, validatingChanged);
        }

        private void handleDiagnostic(ValidationResult<D> result, boolean valid) {
            D diagnostic = result.getDiagnostic();
            if (diagnostic != null) {
                if (diagnostics == null) {
                    diagnostics = new DiagnosticListImpl<>(validators.length);
                }

                diagnostics.setDiagnostic(index, diagnostic, valid);
            } else if (diagnostics != null) {
                diagnostics.clearDiagnostic(index);
            }
        }

        private void fireValueChangedEvent(boolean validChanged, boolean invalidChanged, boolean validatingChanged) {
            if (isFlag(SUPPRESS_CHANGE_EVENT_FLAG)) {
                return;
            }

            if (validChanged) {
                fireValidationListener(ValidationListener.ChangeType.VALID);
                if (observables != null && observables.validProperty != null) {
                    observables.validProperty.fireValueChangedEvent();
                }
            }

            if (invalidChanged) {
                fireValidationListener(ValidationListener.ChangeType.INVALID);
                if (observables != null && observables.invalidProperty != null) {
                    observables.invalidProperty.fireValueChangedEvent();
                }
            }

            if (validatingChanged) {
                fireValidationListener(ValidationListener.ChangeType.VALIDATING);
                if (observables != null && observables.validatingProperty != null) {
                    observables.validatingProperty.fireValueChangedEvent();
                }
            }
        }
    }

    private abstract static class BooleanPropertyImpl extends ReadOnlyBooleanPropertyBase {
        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

}
