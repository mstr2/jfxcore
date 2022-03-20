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

import org.jfxcore.validation.ValidateCancellableTask;
import org.jfxcore.validation.ValidateInterruptibleTask;
import org.jfxcore.validation.ValidateTask;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;
import javafx.validation.function.CancellableValidationFunction0;
import javafx.validation.function.CancellableValidationFunction1;
import javafx.validation.function.CancellableValidationFunction2;
import javafx.validation.function.CancellableValidationFunction3;
import javafx.validation.function.CancellableValidationFunction4;
import javafx.validation.function.CancellableValidationFunction5;
import javafx.validation.function.CancellableValidationFunction6;
import javafx.validation.function.CancellableValidationFunction7;
import javafx.validation.function.CancellableValidationFunction8;
import javafx.validation.function.ValidationFunction0;
import javafx.validation.function.ValidationFunction1;
import javafx.validation.function.ValidationFunction2;
import javafx.validation.function.ValidationFunction3;
import javafx.validation.function.ValidationFunction4;
import javafx.validation.function.ValidationFunction5;
import javafx.validation.function.ValidationFunction6;
import javafx.validation.function.ValidationFunction7;
import javafx.validation.function.ValidationFunction8;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Contains a number of predefined constraints.
 *
 * @since JFXcore 18
 */
@Incubating
@SuppressWarnings("unused")
public final class Constraints {

    private Constraints() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static abstract class ConstraintImpl<T, D> implements Constraint<T, D> {
        final ObservableValue[] dependencies;

        ConstraintImpl() {
            this.dependencies = null;
        }

        ConstraintImpl(ObservableValue... dependencies) {
            this.dependencies = dependencies;
        }

        @SuppressWarnings("ConstantConditions")
        final <P> ObservableValue<P> dependency(int index) {
            return (ObservableValue<P>)dependencies[index];
        }

        @Override
        public Executor getCompletionExecutor() {
            return null;
        }

        @Override
        public Observable[] getDependencies() {
            return dependencies;
        }
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value changes.
     * If the validation function takes a significant amount of execution time, consider
     * {@link #validateAsync(ValidationFunction0, Executor) validateAsync} to improve the
     * responsiveness of the application.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var text = new SimpleConstrainedStringProperty<String>(
     *     Constraints.apply(
     *         (String value) -> {
     *             if (value != null && value.length() >= 5) {
     *                 return ValidationResult.valid();
     *             }
     *
     *             return ValidationResult.invalid("Value too short");
     *         }));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction0, Executor) 
     * @see #validateCancellableAsync(CancellableValidationFunction0, Executor) 
     * @see #validateInterruptibleAsync(ValidationFunction0, Executor) 
     * @param validationFunc the function that validates the value
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> validate(ValidationFunction0<T, D> validationFunc) {
        Objects.requireNonNull(validationFunc, "validationFunc");

        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies a dependency on another {@link ObservableValue}.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the value of
     * the constraint dependency changes.
     * If the validation function takes a significant amount of execution time, consider
     * {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * to improve the responsiveness of the application.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var minLength = new SimpleIntegerProperty(5);
     *
     * var text = new SimpleConstrainedStringProperty<String>(
     *     Constraints.apply(
     *         (String value, Number minLength) -> {
     *             if (value != null && value.length() >= minLength.intValue()) {
     *                 return ValidationResult.valid();
     *             }
     *
     *             return ValidationResult.invalid("Value too short");
     *         },
     *         minLength));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction1, ObservableValue, Executor) 
     * @see #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) 
     * @see #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) 
     * @param validationFunc the function that validates the value
     * @param dependency the constraint dependency
     * @param <T> value type
     * @param <P1> type of the dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, D> Constraint<T, D> validate(
            ValidationFunction1<T, P1, D> validationFunc, ObservableValue<P1> dependency) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency, "dependency");

        return new ConstraintImpl<>(dependency) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(
                        value, this.<P1>dependency(0).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on two {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, D> Constraint<T, D> validate(
            ValidationFunction2<T, P1, P2, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");

        return new ConstraintImpl<>(dependency1, dependency2) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(
                        value, this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on three {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, D> Constraint<T, D> validate(
            ValidationFunction3<T, P1, P2, P3, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on four {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, D> Constraint<T, D> validate(
            ValidationFunction4<T, P1, P2, P3, P4, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue(), this.<P4>dependency(3).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on five {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, D> Constraint<T, D> validate(
            ValidationFunction5<T, P1, P2, P3, P4, P5, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue(), this.<P4>dependency(3).getValue(),
                        this.<P5>dependency(4).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on six {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, D> Constraint<T, D> validate(
            ValidationFunction6<T, P1, P2, P3, P4, P5, P6, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue(), this.<P4>dependency(3).getValue(),
                        this.<P5>dependency(4).getValue(), this.<P6>dependency(5).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on seven {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, D> Constraint<T, D> validate(
            ValidationFunction7<T, P1, P2, P3, P4, P5, P6, P7, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue(), this.<P4>dependency(3).getValue(),
                        this.<P5>dependency(4).getValue(), this.<P6>dependency(5).getValue(),
                        this.<P7>dependency(6).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that synchronously validates a value by applying a validation function,
     * and specifies dependencies on eight {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validate(ValidationFunction1, ObservableValue) validate} for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param dependency8 the eighth constraint dependency
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <P8> type of the eighth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, P8, D> Constraint<T, D> validate(
            ValidationFunction8<T, P1, P2, P3, P4, P5, P6, P7, P8, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            ObservableValue<P8> dependency8) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(dependency8, "dependency8");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7, dependency8) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                try {
                    return CompletableFuture.completedFuture(validationFunc.apply(value,
                        this.<P1>dependency(0).getValue(), this.<P2>dependency(1).getValue(),
                        this.<P3>dependency(2).getValue(), this.<P4>dependency(3).getValue(),
                        this.<P5>dependency(4).getValue(), this.<P6>dependency(5).getValue(),
                        this.<P7>dependency(6).getValue(), this.<P8>dependency(7).getValue()));
                } catch (Throwable ex) {
                    return CompletableFuture.failedFuture(ex);
                }
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value changes.
     * Importantly, the property value must only be changed on the JavaFX application thread; doing so
     * on any other thread may introduce subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value);
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return ValidationResult.invalid(e.getMessage());
     *             }
     *         },
     *         threadPool));
     * }</pre>
     * Note that long-running validation operations may have significant performance implications.
     * When the constraint is re-evaluated while the validation function has not yet completed,
     * the next invocation of the validation function will not be scheduled until the current
     * invocation completes. This might happen if the property value is modified in short succession
     * and the validation function takes a considerable amount of time to complete.
     * Allowing the validation system to cancel an invocation of the validation function helps
     * to reduce the response time in these cases.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction0, Executor) validateCancellableAsync}
     * and {@link #validateInterruptibleAsync(ValidationFunction0, Executor) validateInterruptibleAsync}
     * for more information.
     *
     * @see #validateCancellableAsync
     * @see #validateInterruptibleAsync
     * @param validationFunc the function that validates the value
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> validateAsync(ValidationFunction0<T, D> validationFunc, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies a dependency on another {@link ObservableValue}.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the value of
     * the constraint dependency changes. Importantly, the property value or dependency value must
     * only be changed on the JavaFX application thread; doing so on any other thread may introduce
     * subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * The validation function accepts the value to be validated, as well as the value of the constraint
     * dependency. It is generally safe to use these values within the validation function, as long as
     * the values are immutable objects. If any of the objects contain mutable state, appropriate data
     * synchronization mechanisms should be implemented.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     *
     * var maxValue = new SimpleDoubleProperty(10);
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyAsync(
     *         (value, maxValue) -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value);
     *
     *                 // use the dependency value for additional validation
     *                 if (value.doubleValue() > maxValue.doubleValue()) {
     *                     return ValidationResult.invalid("Value too large");
     *                 } else {
     *                     return ValidationResult.valid();
     *                 }
     *             } catch (RuntimeException e) {
     *                 return ValidationResult.invalid(e.getMessage());
     *             }
     *         },
     *         maxValue,
     *         threadPool));
     * }</pre>
     * Note that long-running validation operations may have significant performance implications.
     * When the constraint is re-evaluated while the validation function has not yet completed,
     * the next invocation of the validation function will not be scheduled until the current
     * invocation completes. This might happen if the property value is modified in short succession
     * and the validation function takes a considerable amount of time to complete.
     * Allowing the validation system to cancel an invocation of the validation function helps
     * to reduce the response time in these cases.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * and {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for more information.
     *
     * @see #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) 
     * @see #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) 
     * @param validationFunc the function that validates the value
     * @param dependency the constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> dependency type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, D> Constraint<T, D> validateAsync(
            ValidationFunction1<T, P1, D> validationFunc, ObservableValue<P1> dependency, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency, "dependency");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep = this.<P1>dependency(0).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on two {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, D> Constraint<T, D> validateAsync(
            ValidationFunction2<T, P1, P2, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on three {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, D> Constraint<T, D> validateAsync(
            ValidationFunction3<T, P1, P2, P3, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on four {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, D> Constraint<T, D> validateAsync(
            ValidationFunction4<T, P1, P2, P3, P4, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on five {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, D> Constraint<T, D> validateAsync(
            ValidationFunction5<T, P1, P2, P3, P4, P5, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on six {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, D> Constraint<T, D> validateAsync(
            ValidationFunction6<T, P1, P2, P3, P4, P5, P6, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5, dependency6) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on seven {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, D> Constraint<T, D> validateAsync(
            ValidationFunction7<T, P1, P2, P3, P4, P5, P6, P7, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6, dep7);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a validation function,
     * and specifies dependencies on eight {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateAsync(ValidationFunction1, ObservableValue, Executor) validateAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param dependency8 the eighth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <P8> type of the eighth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, P8, D> Constraint<T, D> validateAsync(
            ValidationFunction8<T, P1, P2, P3, P4, P5, P6, P7, P8, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            ObservableValue<P8> dependency8,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(dependency8, "dependency8");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7, dependency8) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                final var dep8 = this.<P8>dependency(7).getValue();
                var task = new ValidateTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6, dep7, dep8);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively
     * cancellable validation function.
     * <p>
     * Cooperative cancellation works by repeatedly checking the value of a cancellation flag within
     * the implementation of a long-running algorithm, and is thus best suited for computationally
     * intensive validation functions. For I/O-bound validation functions that use thread interruption
     * to cancel a blocking operation, consider using
     * {@link #validateInterruptibleAsync(ValidationFunction0, Executor) validateInterruptibleAsync} instead.
     * <p>
     * When cancellation is requested, the implementation should return from the validation function
     * as soon as possible; in this case, the {@link ValidationResult} returned from the validation
     * function will be ignored.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value changes.
     * Importantly, the property value must only be changed on the JavaFX application thread; doing so
     * on any other thread may introduce subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyCancellableAsync(
     *         (Number value, AtomicBoolean cancellationRequested) -> {
     *             // CHECKLIST is assumed to contain a large number of individual checks.
     *             // After each check, we give the algorithm a chance to return early.
     *             for (int i = 0; i < CHECKLIST.length && !cancellationRequested.get(); ++i) {
     *                 if (!CHECKLIST[i].check(value)) {
     *                     return ValidationResult.invalid();
     *                 }
     *             }
     *             return ValidationResult.valid();
     *         },
     *         threadPool));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction0, Executor) 
     * @see #validateInterruptibleAsync(ValidationFunction0, Executor) 
     * @param validationFunc the function that validates the value
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction0<T, D> validationFunc, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively
     * cancellable validation function, and specifies a dependency on another {@link ObservableValue}.
     * <p>
     * Cooperative cancellation works by repeatedly checking the value of a cancellation flag within
     * the implementation of a long-running algorithm, and is thus best suited for computationally
     * intensive validation functions. For I/O-bound validation functions that use thread interruption
     * to cancel a blocking operation, consider using
     * {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * instead.
     * <p>
     * When cancellation is requested, the implementation should return from the validation function
     * as soon as possible; in this case, the {@link ValidationResult} returned from the validation
     * function will be ignored.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the value of
     * the constraint dependency changes. Importantly, the property value or dependency value must
     * only be changed on the JavaFX application thread; doing so on any other thread may introduce
     * subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * The validation function accepts the value to be validated, as well as the value of the constraint
     * dependency. It is generally safe to use these values within the validation function, as long as
     * the values are immutable objects. If any of the objects contain mutable state, appropriate data
     * synchronization mechanisms should be implemented.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     *
     * var maxValue = new SimpleDoubleProperty(10);
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyCancellableAsync(
     *         (Number value, Number maxValue, AtomicBoolean cancellationRequested) -> {
     *             try {
     *                 // CHECKLIST is assumed to contain a large number of individual checks.
     *                 // After each check, we give the algorithm a chance to return early.
     *                 for (int i = 0; i < CHECKLIST.length && !cancellationRequested.get(); ++i) {
     *                     if (!CHECKLIST[i].check(value)) {
     *                         return ValidationResult.invalid();
     *                     }
     *                 }
     *
     *                 // Use the dependency value for additional validation
     *                 if (value.doubleValue() > maxValue.doubleValue()) {
     *                     return ValidationResult.invalid("Value too large");
     *                 } else {
     *                     return ValidationResult.valid();
     *                 }
     *             } catch (RuntimeException e) {
     *                 return ValidationResult.invalid(e.getMessage());
     *             }
     *         },
     *         maxValue,
     *         threadPool));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction1, ObservableValue, Executor) 
     * @see #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) 
     * @param validationFunc the function that validates the value
     * @param dependency the constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> dependency type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction1<T, P1, D> validationFunc, ObservableValue<P1> dependency, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency, "dependency");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep = this.<P1>dependency(0).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on two {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction2<T, P1, P2, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep1, dep2, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on three {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction3<T, P1, P2, P3, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep1, dep2, dep3, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on four {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction4<T, P1, P2, P3, P4, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on five {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction5<T, P1, P2, P3, P4, P5, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on six {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction6<T, P1, P2, P3, P4, P5, P6, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5, dependency6) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on seven {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction7<T, P1, P2, P3, P4, P5, P6, P7, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(
                            value, dep1, dep2, dep3, dep4, dep5, dep6, dep7, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying a cooperatively cancellable
     * validation function, and specifies dependencies on eight {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param dependency8 the eighth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <P8> type of the eighth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, P8, D> Constraint<T, D> validateCancellableAsync(
            CancellableValidationFunction8<T, P1, P2, P3, P4, P5, P6, P7, P8, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            ObservableValue<P8> dependency8,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(dependency8, "dependency8");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7, dependency8) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                final var dep8 = this.<P8>dependency(7).getValue();
                var task = new ValidateCancellableTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested) {
                        return validationFunc.apply(
                            value, dep1, dep2, dep3, dep4, dep5, dep6, dep7, dep8, cancellationRequested);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible
     * validation function.
     * <p>
     * Interruptible validation functions are useful for blocking I/O operations, since they support
     * cancellation by thread interruption. For computationally intensive validation functions, consider using
     * {@link #validateCancellableAsync(CancellableValidationFunction0, Executor) validateCancellableAsync} instead.
     * <p>
     * When the thread is interrupted, the implementation should return from the validation function
     * as soon as possible; in this case, the {@link ValidationResult} returned from the validation
     * function will be ignored.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value changes.
     * Importantly, the property value must only be changed on the JavaFX application thread; doing so
     * on any other thread may introduce subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyInterruptibleAsync(
     *         value -> {
     *             try {
     *                 // Simulate a blocking operation
     *                 Thread.sleep(5000);
     *                 return ValidationResult.valid();
     *             } catch (InterruptedException ex) {
     *                 return ValidationResult.invalid();
     *             }
     *         },
     *         threadPool));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction0, Executor) 
     * @see #validateCancellableAsync(CancellableValidationFunction0, Executor) 
     * @param validationFunc the function that validates the value
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction0<T, D> validationFunc, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible
     * validation function, and specifies a dependency on another {@link ObservableValue}.
     * <p>
     * Interruptible validation functions are useful for blocking I/O operations, since they support
     * cancellation by thread interruption. For computationally intensive validation functions, consider using
     * {@link #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor) validateCancellableAsync}
     * instead.
     * <p>
     * When the thread is interrupted, the implementation should return from the validation function
     * as soon as possible; in this case, the {@link ValidationResult} returned from the validation
     * function will be ignored.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the value of
     * the constraint dependency changes. Importantly, the property value or dependency value must
     * only be changed on the JavaFX application thread; doing so on any other thread may introduce
     * subtle bugs and inconsistencies due to data races.
     * <p>
     * The validation function will be invoked with the specified {@link Executor executor}.
     * If the validation function runs concurrently with the JavaFX application thread, it is generally
     * not safe to access any other properties or shared state within this function without implementing
     * appropriate synchronization mechanisms.
     * <p>
     * The validation function accepts the value to be validated, as well as the value of the constraint
     * dependency. It is generally safe to use these values within the validation function, as long as
     * the values are immutable objects. If any of the objects contain mutable state, appropriate data
     * synchronization mechanisms should be implemented.
     * <p>
     * A constrained property that uses this type of constraint can be defined as follows:
     * <pre>{@code
     * var threadPool = Executors.newCachedThreadPool();
     * var maxValue = new SimpleDoubleProperty(10);
     *
     * var property = new SimpleConstrainedDoubleProperty<String>(
     *     Constraints.applyInterruptibleAsync(
     *         (Number value, Number maxValue) -> {
     *             try {
     *                 // Simulate a blocking operation
     *                 Thread.sleep(5000);
     *
     *                 return value.doubleValue() < maxValue.doubleValue() ?
     *                     ValidationResult.valid() : ValidationResult.invalid();
     *             } catch (InterruptedException ex) {
     *                 return ValidationResult.invalid();
     *             }
     *         },
     *         maxValue,
     *         threadPool));
     * }</pre>
     *
     * @see #validateAsync(ValidationFunction1, ObservableValue, Executor) 
     * @see #validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor)  
     * @param validationFunc the function that validates the value
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction1<T, P1, D> validationFunc, ObservableValue<P1> dependency, Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency, "dependency");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep = this.<P1>dependency(0).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on two {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction2<T, P1, P2, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on three {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction3<T, P1, P2, P3, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on four {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction4<T, P1, P2, P3, P4, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on five {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction5<T, P1, P2, P3, P4, P5, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on six {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction6<T, P1, P2, P3, P4, P5, P6, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(dependency1, dependency2, dependency3, dependency4, dependency5, dependency6) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on seven {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction7<T, P1, P2, P3, P4, P5, P6, P7, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6, dep7);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that asynchronously validates a value by applying an interruptible validation
     * function, and specifies dependencies on eight {@link ObservableValue ObservableValues}.
     * <p>
     * See {@link #validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor) validateInterruptibleAsync}
     * for additional information.
     *
     * @param validationFunc the function that validates the value
     * @param dependency1 the first constraint dependency
     * @param dependency2 the second constraint dependency
     * @param dependency3 the third constraint dependency
     * @param dependency4 the fourth constraint dependency
     * @param dependency5 the fifth constraint dependency
     * @param dependency6 the sixth constraint dependency
     * @param dependency7 the seventh constraint dependency
     * @param dependency8 the eighth constraint dependency
     * @param executor the executor that invokes the validation function
     * @param <T> value type
     * @param <P1> type of the first dependency
     * @param <P2> type of the second dependency
     * @param <P3> type of the third dependency
     * @param <P4> type of the fourth dependency
     * @param <P5> type of the fifth dependency
     * @param <P6> type of the sixth dependency
     * @param <P7> type of the seventh dependency
     * @param <P8> type of the eighth dependency
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, P1, P2, P3, P4, P5, P6, P7, P8, D> Constraint<T, D> validateInterruptibleAsync(
            ValidationFunction8<T, P1, P2, P3, P4, P5, P6, P7, P8, D> validationFunc,
            ObservableValue<P1> dependency1,
            ObservableValue<P2> dependency2,
            ObservableValue<P3> dependency3,
            ObservableValue<P4> dependency4,
            ObservableValue<P5> dependency5,
            ObservableValue<P6> dependency6,
            ObservableValue<P7> dependency7,
            ObservableValue<P8> dependency8,
            Executor executor) {
        Objects.requireNonNull(validationFunc, "validationFunc");
        Objects.requireNonNull(dependency1, "dependency1");
        Objects.requireNonNull(dependency2, "dependency2");
        Objects.requireNonNull(dependency3, "dependency3");
        Objects.requireNonNull(dependency4, "dependency4");
        Objects.requireNonNull(dependency5, "dependency5");
        Objects.requireNonNull(dependency6, "dependency6");
        Objects.requireNonNull(dependency7, "dependency7");
        Objects.requireNonNull(dependency8, "dependency8");
        Objects.requireNonNull(executor, "executor");

        return new ConstraintImpl<>(
                dependency1, dependency2, dependency3, dependency4, dependency5, dependency6, dependency7, dependency8) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                final var dep1 = this.<P1>dependency(0).getValue();
                final var dep2 = this.<P2>dependency(1).getValue();
                final var dep3 = this.<P3>dependency(2).getValue();
                final var dep4 = this.<P4>dependency(3).getValue();
                final var dep5 = this.<P5>dependency(4).getValue();
                final var dep6 = this.<P6>dependency(5).getValue();
                final var dep7 = this.<P7>dependency(6).getValue();
                final var dep8 = this.<P8>dependency(7).getValue();
                var task = new ValidateInterruptibleTask<T, D>(value) {
                    @Override
                    protected ValidationResult<D> apply(T value) {
                        return validationFunc.apply(value, dep1, dep2, dep3, dep4, dep5, dep6, dep7, dep8);
                    }
                };
                executor.execute(task);
                return task;
            }

            @Override
            public Executor getCompletionExecutor() {
                return Platform::runLater;
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(int minInclusive, int maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(int minInclusive, int maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v >= minInclusive && v < maxExclusive;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(long minInclusive, long maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(long minInclusive, long maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v >= minInclusive && v < maxExclusive;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(float minInclusive, float maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(float minInclusive, float maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v >= minInclusive && v < maxExclusive;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(double minInclusive, double maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(double minInclusive, double maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v >= minInclusive && v < maxExclusive;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableIntegerValue minInclusive, ObservableIntegerValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableIntegerValue minInclusive, ObservableIntegerValue maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>(minInclusive, maxExclusive) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableLongValue minInclusive, ObservableLongValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableLongValue minInclusive, ObservableLongValue maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>(minInclusive, maxExclusive) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableFloatValue minInclusive, ObservableFloatValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableFloatValue minInclusive, ObservableFloatValue maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>(minInclusive, maxExclusive) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableDoubleValue minInclusive, ObservableDoubleValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> between(ObservableDoubleValue minInclusive, ObservableDoubleValue maxExclusive, Function<Number, D> error) {
        return new ConstraintImpl<>(minInclusive, maxExclusive) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(int minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(int minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v > minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(long minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(long minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v > minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(float minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(float minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v > minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(double minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(double minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v > minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableIntegerValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableIntegerValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v > minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableLongValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableLongValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v > minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableFloatValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableFloatValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v > minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableDoubleValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThan(ObservableDoubleValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v > minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(int minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(int minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v >= minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(long minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(long minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v >= minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(float minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(float minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v >= minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(double minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(double minimum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v >= minimum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     * 
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableIntegerValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableIntegerValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v >= minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableLongValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableLongValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v >= minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableFloatValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableFloatValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v >= minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableDoubleValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> greaterThanOrEqualTo(ObservableDoubleValue minimum, Function<Number, D> error) {
        return new ConstraintImpl<>(minimum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v >= minimum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(int maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(int maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v < maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(long maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(long maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v < maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(float maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(float maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v < maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(double maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(double maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v < maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableIntegerValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableIntegerValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v < maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableLongValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableLongValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v < maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableFloatValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableFloatValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v < maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableDoubleValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThan(ObservableDoubleValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v < maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(int maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(int maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v <= maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(long maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(long maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v <= maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(float maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(float maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v <= maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(double maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(double maximum, Function<Number, D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v <= maximum;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableIntegerValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableIntegerValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                int v = value != null ? value.intValue() : 0;
                boolean valid = v <= maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableLongValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableLongValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                long v = value != null ? value.longValue() : 0;
                boolean valid = v <= maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableFloatValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableFloatValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                float v = value != null ? value.floatValue() : 0;
                boolean valid = v <= maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableDoubleValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param error the function that returns a diagnostic when validation fails
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<Number, D> lessThanOrEqualTo(ObservableDoubleValue maximum, Function<Number, D> error) {
        return new ConstraintImpl<>(maximum) {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(Number value) {
                double v = value != null ? value.doubleValue() : 0;
                boolean valid = v <= maximum.get();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a value is not {@code null}.
     *
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> notNull() {
        return notNull(null);
    }

    /**
     * Creates a constraint that validates that a value is not {@code null}, and specifies a function
     * that creates a diagnostic when validation fails.
     *
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> Constraint<T, D> notNull(Supplier<D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(T value) {
                boolean valid = value != null;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.get() : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or empty.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notNullOrEmpty() {
        return notNullOrEmpty(null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or empty,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notNullOrEmpty(Supplier<D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(String value) {
                boolean valid = value != null && !value.isEmpty();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.get() : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or blank.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notNullOrBlank() {
        return notNullOrBlank(null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or blank,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notNullOrBlank(Supplier<D> error) {
        return new ConstraintImpl<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(String value) {
                boolean blank = true;

                if (value != null) {
                    for (int i = 0; i < value.length(); ++i) {
                        if (!Character.isWhitespace(value.charAt(i))) {
                            blank = false;
                            break;
                        }
                    }
                }

                boolean valid = !blank;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.get() : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> matchesPattern(String regex) {
        return matchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> matchesPattern(String regex, Function<String, D> error) {
        return new ConstraintImpl<>() {
            final Pattern pattern = Pattern.compile(regex);

            @Override
            public CompletableFuture<ValidationResult<D>> validate(String value) {
                boolean valid = pattern.matcher(value != null ? value : "").matches();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> matchesPattern(ObservableStringValue regex) {
        return matchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> matchesPattern(ObservableStringValue regex, Function<String, D> error) {
        return new ObservablePatternConstraint<>(regex, error, false);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notMatchesPattern(String regex) {
        return notMatchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notMatchesPattern(String regex, Function<String, D> error) {
        return new ConstraintImpl<>() {
            final Pattern pattern = Pattern.compile(regex);

            @Override
            public CompletableFuture<ValidationResult<D>> validate(String value) {
                boolean valid = !pattern.matcher(value != null ? value : "").matches();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
            }
        };
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notMatchesPattern(ObservableStringValue regex) {
        return notMatchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern,
     * and specifies a function that creates a diagnostic when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> Constraint<String, D> notMatchesPattern(ObservableStringValue regex, Function<String, D> error) {
        return new ObservablePatternConstraint<>(regex, error, true);
    }

    private static final class ObservablePatternConstraint<D> implements Constraint<String, D>, InvalidationListener {
        private final boolean flip;
        private final ObservableStringValue regex;
        private final Function<String, D> error;
        private final Observable[] dependencies;
        private Pattern pattern;

        ObservablePatternConstraint(ObservableStringValue regex, Function<String, D> error, boolean flip) {
            this.flip = flip;
            this.regex = regex;
            this.error = error;
            this.dependencies = new Observable[] {regex};
            regex.addListener(new WeakInvalidationListener(this));
            invalidated(null);
        }

        @Override
        public void invalidated(Observable observable) {
            String regex = this.regex.get();
            pattern = regex != null ? Pattern.compile(regex) : null;
        }

        @Override
        public CompletableFuture<ValidationResult<D>> validate(String value) {
            boolean valid = (pattern != null && pattern.matcher(value != null ? value : "").matches()) ^ flip;
            return CompletableFuture.completedFuture(
                new ValidationResult<>(valid, !valid && error != null ? error.apply(value) : null));
        }

        @Override
        public Executor getCompletionExecutor() {
            return null;
        }

        @Override
        public Observable[] getDependencies() {
            return dependencies;
        }
    }

    /**
     * Creates a constraint that validates that a list is not {@code null}.
     *
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> ListConstraint<T, D> listNotNull() {
        return listNotNull(null);
    }

    /**
     * Creates a constraint that validates that a list is not {@code null}, and specifies a function
     * that creates a diagnostic when validation fails.
     *
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <T, D> ListConstraint<T, D> listNotNull(Supplier<D> error) {
        return new ListConstraint<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(List<? super T> value) {
                boolean valid = value != null;
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.get() : null));
            }

            @Override
            public Executor getCompletionExecutor() {
                return null;
            }

            @Override
            public Observable[] getDependencies() {
                return null;
            }
        };
    }

    /**
     * Creates a constraint that validates that a list is not {@code null} or empty.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> ListConstraint<String, D> listNotNullOrEmpty() {
        return listNotNullOrEmpty(null);
    }

    /**
     * Creates a constraint that validates that a list is not {@code null} or empty,
     * and specifies a function that creates a diagnostic when validation fails.
     *
     * @param <D> diagnostic type
     * @return the new constraint
     */
    public static <D> ListConstraint<String, D> listNotNullOrEmpty(Supplier<D> error) {
        return new ListConstraint<>() {
            @Override
            public CompletableFuture<ValidationResult<D>> validate(List<? super String> value) {
                boolean valid = value != null && !value.isEmpty();
                return CompletableFuture.completedFuture(
                    new ValidationResult<>(valid, !valid && error != null ? error.get() : null));
            }

            @Override
            public Executor getCompletionExecutor() {
                return null;
            }

            @Override
            public Observable[] getDependencies() {
                return null;
            }
        };
    }

    /**
     * Converts a constraint that applies to each element of a list to a {@link ListConstraint}
     * that applies to the list instance as a whole.
     *
     * @param constraint the constraint
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new {@code ListConstraint}
     */
    public static <T, D> ListConstraint<T, D> forList(Constraint<? super List<T>, D> constraint) {
        return new ListConstraint<>() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public CompletableFuture<ValidationResult<D>> validate(List value) {
                return ((Constraint)constraint).validate(value);
            }

            @Override
            public Executor getCompletionExecutor() {
                return constraint.getCompletionExecutor();
            }

            @Override
            public Observable[] getDependencies() {
                return constraint.getDependencies();
            }
        };
    }

    /**
     * Converts a constraint that applies to each element of a set to a {@link SetConstraint}
     * that applies to the set instance as a whole.
     *
     * @param constraint the constraint
     * @param <T> value type
     * @param <D> diagnostic type
     * @return the new {@code SetConstraint}
     */
    public static <T, D> SetConstraint<T, D> forSet(Constraint<? super Set<T>, D> constraint) {
        return new SetConstraint<>() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public CompletableFuture<ValidationResult<D>> validate(Set value) {
                return ((Constraint)constraint).validate(value);
            }

            @Override
            public Executor getCompletionExecutor() {
                return constraint.getCompletionExecutor();
            }

            @Override
            public Observable[] getDependencies() {
                return constraint.getDependencies();
            }
        };
    }

    /**
     * Converts a constraint that applies to each element of a map to a {@link MapConstraint}
     * that applies to the map instance as a whole.
     *
     * @param constraint the constraint
     * @param <K> key type
     * @param <V> value type
     * @param <D> diagnostic type
     * @return the new {@code MapConstraint}
     */
    public static <K, V, D> MapConstraint<K, V, D> forMap(Constraint<? super Map<K, V>, D> constraint) {
        return new MapConstraint<>() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public CompletableFuture<ValidationResult<D>> validate(Map value) {
                return ((Constraint)constraint).validate(value);
            }

            @Override
            public Executor getCompletionExecutor() {
                return constraint.getCompletionExecutor();
            }

            @Override
            public Observable[] getDependencies() {
                return constraint.getDependencies();
            }
        };
    }

}
