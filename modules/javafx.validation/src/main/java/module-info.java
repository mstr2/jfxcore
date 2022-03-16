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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.StringProperty;
import javafx.validation.ConstrainedValue;
import javafx.validation.Constraint;
import javafx.validation.Constraints;
import javafx.validation.ValidationResult;
import javafx.validation.Validator;
import javafx.validation.property.ConstrainedBooleanProperty;
import javafx.validation.property.ConstrainedDoubleProperty;
import javafx.validation.property.ConstrainedFloatProperty;
import javafx.validation.property.ConstrainedIntegerProperty;
import javafx.validation.property.ConstrainedListProperty;
import javafx.validation.property.ConstrainedLongProperty;
import javafx.validation.property.ConstrainedMapProperty;
import javafx.validation.property.ConstrainedObjectProperty;
import javafx.validation.property.ConstrainedProperty;
import javafx.validation.property.ConstrainedSetProperty;
import javafx.validation.property.ConstrainedStringProperty;
import javafx.validation.property.ReadOnlyConstrainedProperty;
import javafx.validation.property.SimpleConstrainedBooleanProperty;
import javafx.validation.property.SimpleConstrainedDoubleProperty;
import javafx.validation.property.SimpleConstrainedFloatProperty;
import javafx.validation.property.SimpleConstrainedIntegerProperty;
import javafx.validation.property.SimpleConstrainedListProperty;
import javafx.validation.property.SimpleConstrainedLongProperty;
import javafx.validation.property.SimpleConstrainedMapProperty;
import javafx.validation.property.SimpleConstrainedObjectProperty;
import javafx.validation.property.SimpleConstrainedSetProperty;
import javafx.validation.property.SimpleConstrainedStringProperty;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Defines APIs that support data validation for the JavaFX UI toolkit.
 *
 * <h2>Contents</h2>
 * <ol>
 *     <li><a href="#Overview">Overview</a>
 *     <li><a href="#ConstrainedProperties">Constrained properties</a>
 *     <li><a href="#DataValidation">Data validation</a>
 * </ol>
 *
 * <a id="Overview"></a>
 * <h2>Overview</h2>
 * The data validation framework enables applications to validate user input, and to visualize the
 * validation state of a datum via CSS.
 * Data validation ensures correctness and consistency of data in a JavaFX application by specifying
 * data {@link Constraints constraints}: a datum that satisfies all constraints is <em>valid</em> and
 * can be used for further processing; a datum that violates a constraint is <em>invalid</em>.
 * <p>
 * Many applications decouple the validation and visualization aspects:
 * <ol>
 *     <li><em>Validation</em> is implemented in a controller, view model, or business logic class
 *     <li><em>Visualization</em> is implemented in the JavaFX scene graph
 * </ol>
 * The data validation framework also supports two modes of validation:
 * <ol>
 *     <li><em>Blocking</em> (synchronous) data validation that runs on the JavaFX application thread
 *         and is useful for simple validation logic
 *     <li><em>Non-blocking</em> (asynchronous) data validation that runs on an application-specified
 *         background thread, which allows applications to maintain a responsive user interface for
 *         long-running validation operations
 * </ol>
 *
 * <a id="ConstrainedProperties"></a>
 * <h2>Constrained properties</h2>
 * The basic primitive of data validation is the {@link ConstrainedValue} interface, which represents
 * a value with a constrained range of validity. This concept is extended to JavaFX properties with the
 * {@link ConstrainedProperty} interface. The data validation framework comes with a full set of
 * property implementations that extend the standard JavaFX property specification:
 * <p>
 * <table border="1">
 *     <caption>{@link ConstrainedProperty} implementations</caption>
 *     <tr><th>Standard property</th><th>Constrained property</th><th>Default implementation</th></tr>
 *     <tr><td>{@link BooleanProperty}</td><td>{@link ConstrainedBooleanProperty}</td><td>{@link SimpleConstrainedBooleanProperty}</td></tr>
 *     <tr><td>{@link IntegerProperty}</td><td>{@link ConstrainedIntegerProperty}</td><td>{@link SimpleConstrainedIntegerProperty}</td></tr>
 *     <tr><td>{@link LongProperty}</td><td>{@link ConstrainedLongProperty}</td><td>{@link SimpleConstrainedLongProperty}</td></tr>
 *     <tr><td>{@link FloatProperty}</td><td>{@link ConstrainedFloatProperty}</td><td>{@link SimpleConstrainedFloatProperty}</td></tr>
 *     <tr><td>{@link DoubleProperty}</td><td>{@link ConstrainedDoubleProperty}</td><td>{@link SimpleConstrainedDoubleProperty}</td></tr>
 *     <tr><td>{@link StringProperty}</td><td>{@link ConstrainedStringProperty}</td><td>{@link SimpleConstrainedStringProperty}</td></tr>
 *     <tr><td>{@link ObjectProperty}</td><td>{@link ConstrainedObjectProperty}</td><td>{@link SimpleConstrainedObjectProperty}</td></tr>
 *     <tr><td>{@link ListProperty}</td><td>{@link ConstrainedListProperty}</td><td>{@link SimpleConstrainedListProperty}</td></tr>
 *     <tr><td>{@link SetProperty}</td><td>{@link ConstrainedSetProperty}</td><td>{@link SimpleConstrainedSetProperty}</td></tr>
 *     <tr><td>{@link MapProperty}</td><td>{@link ConstrainedMapProperty}</td><td>{@link SimpleConstrainedMapProperty}</td></tr>
 * </table>
 *
 * <h3>Tainted and untainted values</h3>
 * Any value that is entered by users of an application can be considered <em>tainted</em>, and should
 * be validated before it is used in business logic.
 * Constrained properties make it easier to work with potentially tainted values by introducing
 * {@link ReadOnlyConstrainedProperty#constrainedValueProperty()}.
 * The value of this property always corresponds to the last value that was successfully validated,
 * and can therefore be considered to be <em>untainted</em>.
 *
 * <h3>Constraints</h3>
 * The value range of a property is constrained by one or more {@link Constraint} instances.
 * Constraints are automatically evaluated by the data validation framework whenever the property value or
 * one of its dependencies has changed.
 * Fundamentally, a constraint is simply a tuple that ties together a {@link Validator}, its dependencies,
 * and in case of asynchronous validation, an {@link Executor} to yield the {@link ValidationResult} back
 * to the data validation framework (in most cases, this is just a reference to {@link Platform#runLater}).
 * <p>
 * For ease of use, the {@link Constraints} class contains several predefined constraints that cover a wide
 * variety of use cases.
 *
 * <h3>Using constrained properties</h3>
 * Consider the following example, which is a class that declares and exposes constrained properties.
 * Note that the properties are parameterized with the type of the diagnostics produced by the property.
 * Since this example doesn't use diagnostics, {@code Void} is used as a generic placeholder.
 *
 * <pre>{@code
 *    class Person {
 *        private final ConstrainedStringProperty<Void> name =
 *                new SimpleConstrainedStringProperty<>(Constraints.notNullOrEmpty());
 *
 *        public ConstrainedStringProperty<Void> nameProperty() {
 *            return name;
 *        }
 *
 *        private final ConstrainedIntegerProperty<Void> age =
 *                new SimpleConstrainedIntegerProperty<>(Constraints.between(0, 120));
 *
 *        public ConstrainedIntegerProperty<Void> ageProperty() {
 *            return age;
 *        }
 *    }
 * }</pre>
 *
 * <a id="DataValidation"></a>
 * <h2>Data validation</h2>
 * Implementations of the {@link Validator} interface encapsulate the validation logic that determines
 * whether a value is valid.
 * For every value that is passed into a validator, it must return a {@link CompletableFuture} that
 * yields a {@link ValidationResult} for the validated value.
 * A validator can choose to attach an application-specified diagnostic object to the {@code ValidationResult},
 * which is surfaced in {@link ConstrainedValue#getDiagnostics()} after validation is complete.
 * <p>
 * Applications can use the diagnostics that were generated during constraint validation to show a
 * list of error messages, warnings, or other contextual information.
 *
 * <h3>Synchronous vs. asynchronous data validation</h3>
 * The difference between synchronous and asynchronous data validation is which type of
 * {@link CompletableFuture} is returned from {@link Validator#validate(Object)}:
 * <ul>
 *     <li>Synchronous validators always return a completed future, which can be obtained by
 *         calling {@link CompletableFuture#completedFuture(Object)}.
 *     <li>Asynchronous validators return a future that is not yet completed.
 *         In this case, when the future completes, its {@code ValidationResult} will be yielded to
 *         the data validation framework by invoking the constraint's {@code completionExecutor}.
 * </ul>
 *
 * <h3>Functional data validation</h3>
 * Implementing the {@code Validator} interface can be tricky, especially when asynchronous validation.
 *
 * <h3>Validator implementation example</h3>
 * In the following example, the validator checks whether a {@code String} value is a valid EAN-8
 * or EAN-13 number, and yields an error message if this is not the case:
 *
 * <pre>{@code
 *    class EANValidator implements Validator<String, String> {
 *        @Override
 *        public CompletableFuture<ValidationResult<String>> validate(String s) {
 *            ValidationResult<String> result;
 *
 *            boolean checksumValid = IntStream.range(0, s.length())
 *                .map(i -> Character.digit(s.charAt(s.length() - i - 1), 10) * (i % 2 == 0 ? 3 : 1))
 *                .sum() % 10 == 0;
 *
 *            if (s.length() != 8 && s.length() != 13) {
 *                result = ValidationResult.invalid("Value must contain 8 or 13 digits");
 *            } else if (!checksumValid) {
 *                result = ValidationResult.invalid("Value is not a valid EAN number");
 *            } else {
 *                result = ValidationResult.valid();
 *            }
 *
 *            return CompletableFuture.completedFuture(result);
 *        }
 *    }
 *
 *    ...
 *
 *    ConstrainedStringProperty<String> ean = new SimpleConstrainedStringProperty<>(
 *            new Constraint<>(
 *                new EANValidator(),
 *                null, // completionExecutor
 *                null // dependencies
 *            ));
 * }</pre>
 *
 * There are a few things to note in this example:
 * <ul>
 *     <li>{@code completionExecutor} is {@code null} because {@code EANValidator} always returns
 *         completed futures and therefore is a synchronous validator
 *     <li>There are no dependencies; if there were, changes of dependent {@code Observables} would
 *         trigger a re-evaluation of the constraint
 * </ul>
 *
 * <h3>Functional data validation</h3>
 * Implementing custom validators and keeping them in sync with the
 *
 * @moduleGraph
 * @since JFXcore 18
 */
module javafx.validation {
    requires transitive javafx.base;
    requires transitive javafx.graphics;

    exports javafx.validation;
    exports javafx.validation.function;
    exports javafx.validation.property;
}