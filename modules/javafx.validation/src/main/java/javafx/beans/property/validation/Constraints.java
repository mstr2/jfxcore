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

package javafx.beans.property.validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableStringValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Contains a number of predefined constraints.
 *
 * @since JFXcore 18
 */
@SuppressWarnings("unused")
public final class Constraints {
    
    private Constraints() {}

    /**
     * Creates a constraint that synchronously validates a value, and specifies its dependencies.
     * <p>
     * A constrained property that validates its value can be defined as follows:
     * <blockquote><pre>
     * var minLength = new SimpleIntegerProperty(5);
     *
     * var text = new SimpleConstrainedStringProperty&lt;String>(
     *     Constraints.validate(
     *         value -> {
     *             if (value != null &amp;&amp; value.length() >= minLength.get()) {
     *                 return ValidationResult.valid();
     *             }
     *
     *             return new ValidationResult&lt;>(false, "Value too short");
     *         },
     *         minLength));
     * </pre></blockquote>
     *
     * @param validationFunc the function that validates the value
     * @param dependencies the dependencies of the constraint
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validate(Function<T, ValidationResult<E>> validationFunc, Observable... dependencies) {
        return new Constraint<>(validationFunc::apply, dependencies);
    }

    /**
     * Creates a constraint that asynchronously validates a value.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value);
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor) {
        return validateAsync(validationFunc, completionExecutor, (Observable[])null);
    }

    /**
     * Creates a constraint that asynchronously validates a value and specifies its dependencies.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the values of
     * the constraint dependencies change.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var dependency1 = new SimpleDoubleProperty();
     * var dependency2 = new SimpleDoubleProperty();
     *
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value, dependency1.get(), dependency2.get());
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater,
     *         dependency1, dependency2));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param dependencies the dependencies of the constraint
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor, Observable... dependencies) {
        return new Constraint<>(
            value -> CompletableFuture.supplyAsync(() -> validationFunc.apply(value)),
            null,
            completionExecutor,
            dependencies);
    }

    /**
     * Creates a delayed constraint that asynchronously validates a value.
     * <p>
     * When the underlying property value changes, constraint validation will be delayed by the specified duration.
     * If the property value changes again within the delay period, the existing validation request is cancelled
     * and a new delayed validation request is scheduled.
     * This allows a rapidly changing value to settle before a computationally expensive validation function is invoked.
     * In practice, a small delay should be used if the source of the value change is a UI control that allows rapid
     * data input, like a value spinner.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value);
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater,
     *         300));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function. The validation function will be invoked
     * 300 milliseconds after the most recent change of the property value.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param delayMillis the delay that needs to elapse before the validator is invoked
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor, long delayMillis) {
        return validateAsync(validationFunc, completionExecutor, delayMillis, TimeUnit.MILLISECONDS, (Observable[])null);
    }

    /**
     * Creates a delayed constraint that asynchronously validates a value.
     * <p>
     * When the underlying property value changes, constraint validation will be delayed by the specified duration.
     * If the property value changes again within the delay period, the existing validation request is cancelled
     * and a new delayed validation request is scheduled.
     * This allows a rapidly changing value to settle before a computationally expensive validation function is invoked.
     * In practice, a small delay should be used if the source of the value change is a UI control that allows rapid
     * data input, like a value spinner.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value);
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater,
     *         300, TimeUnit.MILLISECONDS));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function. The validation function will be invoked
     * 300 milliseconds after the most recent change of the property value.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param delay the delay that needs to elapse before the validator is invoked
     * @param unit the delay unit
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor, long delay, TimeUnit unit) {
        return validateAsync(validationFunc, completionExecutor, delay, unit, (Observable[])null);
    }

    /**
     * Creates a delayed constraint that asynchronously validates a value  and specifies its dependencies.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the values of
     * the constraint dependencies change.
     * <p>
     * When the underlying property value changes, constraint validation will be delayed by the specified duration.
     * If the property value changes again within the delay period, the existing validation request is cancelled
     * and a new delayed validation request is scheduled.
     * This allows a rapidly changing value to settle before a computationally expensive validation function is invoked.
     * In practice, a small delay should be used if the source of the value change is a UI control that allows rapid
     * data input, like a value spinner.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var dependency1 = new SimpleDoubleProperty();
     * var dependency2 = new SimpleDoubleProperty();
     *
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value, dependency1.get(), dependency2.get());
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater,
     *         300,
     *         dependency1, dependency2));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function. The validation function will be invoked
     * 300 milliseconds after the most recent change of the property value.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param delayMillis the delay that needs to elapse before the validator is invoked
     * @param dependencies the dependencies of the constraint
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor, long delayMillis, Observable... dependencies) {
        return validateAsync(validationFunc, completionExecutor, delayMillis, TimeUnit.MILLISECONDS, dependencies);
    }

    /**
     * Creates a delayed constraint that asynchronously validates a value  and specifies its dependencies.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the values of
     * the constraint dependencies change.
     * <p>
     * When the underlying property value changes, constraint validation will be delayed by the specified duration.
     * If the property value changes again within the delay period, the existing validation request is cancelled
     * and a new delayed validation request is scheduled.
     * This allows a rapidly changing value to settle before a computationally expensive validation function is invoked.
     * In practice, a small delay should be used if the source of the value change is a UI control that allows rapid
     * data input, like a value spinner.
     * <p>
     * The validation function will be invoked by a task running in {@link ForkJoinPool#commonPool()}.
     * After the validation function returns, the {@link ValidationResult} will be yielded by {@code completionExecutor}.
     * It is important that {@code completionExecutor} can safely access the constrained property to prevent data races.
     * <p>
     * A constrained property that is safe to use on the JavaFX application thread can be defined as follows:
     * <blockquote><pre>
     * var dependency1 = new SimpleDoubleProperty();
     * var dependency2 = new SimpleDoubleProperty();
     *
     * var property = new SimpleConstrainedDoubleProperty&lt;String>(
     *     Constraints.validateAsync(
     *         value -> {
     *             try {
     *                 // takes a long time, throws if invalid
     *                 complexValidation(value, dependency1.get(), dependency2.get());
     *                 return ValidationResult.valid();
     *             } catch (RuntimeException e) {
     *                 return new ValidationResult&lt;>(false, e.getMessage());
     *             }
     *         },
     *         Platform::runLater,
     *         300, TimeUnit.MILLISECONDS,
     *         dependency1, dependency2));
     * </pre></blockquote>
     * In this example, {@code Platform::runLater} is used to yield the {@code ValidationResult} back to the JavaFX
     * application thread after being returned from the validation function. The validation function will be invoked
     * 300 milliseconds after the most recent change of the property value.
     *
     * @param validationFunc the function that validates the value
     * @param completionExecutor the executor that yields the validation result
     * @param delay the delay that needs to elapse before the validator is invoked
     * @param unit the delay unit
     * @param dependencies the dependencies of the constraint
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> validateAsync(
            Function<T, ValidationResult<E>> validationFunc, Executor completionExecutor, long delay, TimeUnit unit, Observable... dependencies) {
        return new Constraint<>(
            value -> CompletableFuture.supplyAsync(() -> validationFunc.apply(value)),
            CompletableFuture.delayedExecutor(delay, unit),
            completionExecutor,
            dependencies);
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(int minInclusive, int maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(int minInclusive, int maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v >= minInclusive && v < maxExclusive;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(long minInclusive, long maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(long minInclusive, long maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v >= minInclusive && v < maxExclusive;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(float minInclusive, float maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(float minInclusive, float maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v >= minInclusive && v < maxExclusive;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(double minInclusive, double maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(double minInclusive, double maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v >= minInclusive && v < maxExclusive;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableIntegerValue minInclusive, ObservableIntegerValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableIntegerValue minInclusive, ObservableIntegerValue maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minInclusive, maxExclusive});
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableLongValue minInclusive, ObservableLongValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableLongValue minInclusive, ObservableLongValue maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minInclusive, maxExclusive});
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableFloatValue minInclusive, ObservableFloatValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableFloatValue minInclusive, ObservableFloatValue maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minInclusive, maxExclusive});
    }

    /**
     * Creates a constraint that validates that a number is within a range.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableDoubleValue minInclusive, ObservableDoubleValue maxExclusive) {
        return between(minInclusive, maxExclusive, null);
    }

    /**
     * Creates a constraint that validates that a number is within a range,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or any of
     * the constraint dependencies are invalidated.
     *
     * @param minInclusive the lower limit, inclusive
     * @param maxExclusive the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> between(ObservableDoubleValue minInclusive, ObservableDoubleValue maxExclusive, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v >= minInclusive.get() && v < maxExclusive.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minInclusive, maxExclusive});
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(int minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(int minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v > minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(long minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(long minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v > minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(float minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(float minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v > minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(double minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(double minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v > minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableIntegerValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableIntegerValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v > minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableLongValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableLongValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v > minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableFloatValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableFloatValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v > minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableDoubleValue minimum) {
        return greaterThan(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThan(ObservableDoubleValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v > minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(int minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(int minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v >= minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(long minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(long minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v >= minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(float minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(float minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v >= minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(double minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(double minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v >= minimum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     * 
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableIntegerValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableIntegerValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v >= minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableLongValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableLongValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v >= minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableFloatValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableFloatValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v >= minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableDoubleValue minimum) {
        return greaterThanOrEqualTo(minimum, null);
    }

    /**
     * Creates a constraint that validates that a number is greater than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * minimum value dependency changes.
     *
     * @param minimum the lower limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> greaterThanOrEqualTo(ObservableDoubleValue minimum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v >= minimum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {minimum});
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(int maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(int maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v < maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(long maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(long maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v < maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(float maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(float maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v < maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(double maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(double maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v < maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableIntegerValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableIntegerValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v < maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableLongValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableLongValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v < maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableFloatValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableFloatValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v < maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableDoubleValue maximum) {
        return lessThan(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, exclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThan(ObservableDoubleValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v < maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(int maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(int maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v <= maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(long maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(long maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v <= maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(float maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(float maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v <= maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(double maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(double maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v <= maximum;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableIntegerValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableIntegerValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            int v = value != null ? value.intValue() : 0;
            boolean valid = v <= maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableLongValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableLongValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            long v = value != null ? value.longValue() : 0;
            boolean valid = v <= maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableFloatValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableFloatValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            float v = value != null ? value.floatValue() : 0;
            boolean valid = v <= maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableDoubleValue maximum) {
        return lessThanOrEqualTo(maximum, null);
    }

    /**
     * Creates a constraint that validates that a number is less than or equal to the specified value,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * maximum value dependency changes.
     *
     * @param maximum the upper limit, inclusive
     * @param errorInfo the handler that returns an error information object when validation fails
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<Number, E> lessThanOrEqualTo(ObservableDoubleValue maximum, Function<Number, E> errorInfo) {
        return new Constraint<>(value -> {
            double v = value != null ? value.doubleValue() : 0;
            boolean valid = v <= maximum.get();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }, new Observable[] {maximum});
    }

    /**
     * Creates a constraint that validates that a valus is not {@code null}.
     *
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> notNull() {
        return notNull(null);
    }

    /**
     * Creates a constraint that validates that a valus is not {@code null}, and specifies a function
     * that creates an error information object when validation fails.
     *
     * @param <T> value type
     * @param <E> error information type
     * @return the new constraint
     */
    public static <T, E> Constraint<T, E> notNull(Supplier<E> errorInfo) {
        return new Constraint<>(value -> {
            boolean valid = value != null;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.get() : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or empty.
     *
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notNullOrEmpty() {
        return notNullOrEmpty(null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or empty,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notNullOrEmpty(Supplier<E> errorInfo) {
        return new Constraint<>(value -> {
            boolean valid = value != null && !value.isEmpty();
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.get() : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or blank.
     *
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notNullOrBlank() {
        return notNullOrBlank(null);
    }

    /**
     * Creates a constraint that validates that a {@link String} is not {@code null} or blank,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notNullOrBlank(Supplier<E> errorInfo) {
        return new Constraint<>(value -> {
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
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.get() : null);
        }, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> matchesPattern(String regex) {
        return matchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> matchesPattern(String regex, Function<String, E> errorInfo) {
        return new Constraint<>(new Validator<>() {
            final Pattern pattern = Pattern.compile(regex);

            @Override
            public ValidationResult<E> validate(String value) {
                boolean valid = pattern.matcher(value != null ? value : "").matches();
                return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
            }
        }, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> matchesPattern(ObservableStringValue regex) {
        return matchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} matches a regular expression pattern,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> matchesPattern(ObservableStringValue regex, Function<String, E> errorInfo) {
        return new Constraint<>(new ObservablePatternValidator<>(regex, errorInfo, false), new Observable[] {regex});
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notMatchesPattern(String regex) {
        return notMatchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern,
     * and specifies a function that creates an error information object when validation fails.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notMatchesPattern(String regex, Function<String, E> errorInfo) {
        return new Constraint<>(new Validator<>() {
            final Pattern pattern = Pattern.compile(regex);

            @Override
            public ValidationResult<E> validate(String value) {
                boolean valid = !pattern.matcher(value != null ? value : "").matches();
                return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
            }
        }, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notMatchesPattern(ObservableStringValue regex) {
        return notMatchesPattern(regex, null);
    }

    /**
     * Creates a constraint that validates that a {@link String} does not match a regular expression pattern,
     * and specifies a function that creates an error information object when validation fails.
     * <p>
     * The constraint will be re-evaluated whenever the underlying property value or the
     * regular expression pattern changes.
     *
     * @param regex the regular expression pattern
     * @param <E> error information type
     * @return the new constraint
     */
    public static <E> Constraint<String, E> notMatchesPattern(ObservableStringValue regex, Function<String, E> errorInfo) {
        return new Constraint<>(new ObservablePatternValidator<>(regex, errorInfo, true), new Observable[] {regex});
    }

    private static final class ObservablePatternValidator<E> implements Validator<String, E>, InvalidationListener {
        private final boolean flip;
        private final ObservableStringValue regex;
        private final Function<String, E> errorInfo;
        private Pattern pattern;

        ObservablePatternValidator(ObservableStringValue regex, Function<String, E> errorInfo, boolean flip) {
            this.flip = flip;
            this.regex = regex;
            this.errorInfo = errorInfo;
            regex.addListener(new WeakInvalidationListener(this));
            invalidated(null);
        }

        @Override
        public void invalidated(Observable observable) {
            String regex = this.regex.get();
            pattern = regex != null ? Pattern.compile(regex) : null;
        }

        @Override
        public ValidationResult<E> validate(String value) {
            boolean valid = (pattern != null && pattern.matcher(value != null ? value : "").matches()) ^ flip;
            return new ValidationResult<>(valid, !valid && errorInfo != null ? errorInfo.apply(value) : null);
        }
    }

}
