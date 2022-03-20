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
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.validation.ConstrainedValue;
import javafx.validation.Constraint;
import javafx.validation.Constraints;
import javafx.validation.ListConstraint;
import javafx.validation.MapConstraint;
import javafx.validation.SetConstraint;
import javafx.validation.ValidationResult;
import javafx.validation.ValidationState;
import javafx.validation.function.CancellableValidationFunction1;
import javafx.validation.function.ValidationFunction0;
import javafx.validation.function.ValidationFunction1;
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
 *     <li><a href="#Diagnostics">Diagnostics</a>
 *     <li><a href="#Constraints">Constraints</a>
 *     <li><a href="#Dependencies">Constraint dependencies</a>
 *     <li><a href="#Collections">Collection constraints</a>
 *     <li><a href="#AsynchronousValidation">Asynchronous data validation</a>
 *     <li><a href="#Visualization">Visualization</a>
 * </ol>
 *
 * <a id="Overview"></a>
 * <h2>Overview</h2>
 * The data validation framework enables applications to validate user input, and to visualize the
 * validation state via CSS.
 * Data validation ensures correctness and consistency of data in a JavaFX application by specifying
 * data {@link Constraint constraints}: a value that satisfies all constraints is <em>valid</em> and
 * can be used for further processing; a value that violates a constraint is <em>invalid</em>.
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
 * <p>
 * Any value that is entered by users of an application can be considered <em>tainted</em>, and should
 * be validated before it is used in business logic.
 * Constrained properties make it easier to work with potentially tainted values by introducing
 * {@link ReadOnlyConstrainedProperty#constrainedValueProperty()}.
 * The value of this property always corresponds to the last value that was successfully validated,
 * and can therefore be considered to be <em>untainted</em>.
 *
 * <a id="Diagnostics"></a>
 * <h2>Diagnostics</h2>
 * All {@link ConstrainedValue} implementations are parameterized with a diagnostic type.
 * When diagnostics are not used, {@code Void} can be used as a generic type placeholder.
 * Diagnostics are application-specified objects that can be attached to the {@link ValidationResult}
 * that is produced by {@link Constraint} validators, and can be retrieved after a validation
 * run by calling {@link ConstrainedValue#getDiagnostics()}.
 * {@code ConstrainedProperty} also adds {@link ConstrainedProperty#diagnosticsProperty() diagnosticsProperty}.
 * <p>
 * In simple cases, the diagnostic type could be a {@code String} value that contains a
 * message when a value fails validation.
 * <p>
 * The data validation framework only differentiates between <em>valid</em> and <em>invalid</em>
 * values; it does not include higher-level concepts like <em>errors</em> and <em>warnings</em>,
 * nor classifications like <em>severity</em> or <em>priority</em>.
 * <p>
 * Applications are free to choose their own diagnostic semantics by defining custom diagnostic types.
 * Consider the following example:
 * <pre>{@code
 *    enum Severity { ERROR, WARNING }
 *
 *    record DiagnosticInfo(Severity severity, String message) {}
 *
 *    class PersonController {
 *        private final ConstrainedStringProperty<DiagnosticInfo> name =
 *                new SimpleConstrainedStringProperty<>(
 *                    Constraints.notNullOrEmpty(
 *                        () -> new DiagnosticInfo(Severity.ERROR, "Name cannot be empty")));
 *
 *        public ConstrainedStringProperty<DiagnosticInfo> nameProperty() {
 *            return name;
 *        }
 *
 *        private final ConstrainedIntegerProperty<DiagnosticInfo> age =
 *                new SimpleConstrainedIntegerProperty<>(
 *                    Constraints.validate(number -> {
 *                        if (number.intValue() < 0)
 *                            return ValidationResult.invalid(
 *                                new DiagnosticInfo(Severity.ERROR, number + " out of range"));
 *
 *                        if (number.intValue() > 130)
 *                            return ValidationResult.valid(
 *                                new DiagnosticInfo(Severity.WARNING, number + " is suspicious, check again"));
 *
 *                        return ValidationResult.valid();
 *                    }));
 *
 *        public ConstrainedIntegerProperty<DiagnosticInfo> ageProperty() {
 *            return age;
 *        }
 *    }
 * }</pre>
 *
 * <a id="Constraints"></a>
 * <h2>Constraints</h2>
 * The value range of a property can be constrained by one or more {@link Constraint}s.
 * Constraints are automatically evaluated by the data validation system whenever the property value or
 * one of its dependencies has changed.
 * <p>
 * Implementations of the {@link Constraint} interface must provide three methods:
 * <table>
 *     <caption></caption>
 *     <tr>
 *         <td>{@link Constraint#validate validate(T)}</td>
 *         <td>Contains the validation logic and returns a {@link ValidationResult} for each validated value.</td>
 *     </tr>
 *     <tr>
 *        <td>{@link Constraint#getDependencies() getDependencies()}</td>
 *        <td>Returns the dependencies of the constraint
 *            (see <a href="#ConstraintDependencies">Constraint dependencies</a>).</td>
 *    </tr>
 *    <tr>
 *         <td>{@link Constraint#getCompletionExecutor() getCompletionExecutor()}</td>
 *         <td>For asynchronous constraint implementations, returns the {@link Executor} that is used to yield the
 *             result to the validation system (see <a href="#AsynchronousValidation">Asynchronous data validation</a>);
 *             for synchronous constraint implementations, returns {@code null}.</td>
 *     </tr>
 * </table>
 * <p>
 * For ease of use, the {@link Constraints} class contains several predefined constraint factories that
 * cover a wide variety of use cases.
 * In the following example, the general-purpose {@link Constraints#validate(ValidationFunction0)}
 * factory is used to create a EAN barcode number constraint:
 * <pre>{@code
 *    class ProductController {
 *        private final ConstrainedStringProperty<String> ean =
 *                new SimpleConstrainedStringProperty<>(
 *                    Constraints.validate(value -> {
 *                        ValidationResult<String> result;
 *
 *                        boolean checksumValid = IntStream.range(0, value.length())
 *                            .map(i -> Character.digit(value.charAt(value.length() - i - 1), 10) * (i % 2 == 0 ? 3 : 1))
 *                            .sum() % 10 == 0;
 *
 *                        if (value.length() != 8 && value.length() != 13) {
 *                            result = ValidationResult.invalid("Value must contain 8 or 13 digits");
 *                        } else if (!checksumValid) {
 *                            result = ValidationResult.invalid("Value is not a valid EAN number");
 *                        } else {
 *                            result = ValidationResult.valid();
 *                        }
 *
 *                        return result;
 *                    }));
 *
 *        public ConstrainedStringProperty<String> eanProperty() {
 *            return ean;
 *        }
 *    }
 * }</pre>
 *
 * <a id="Dependencies"></a>
 * <h2>Constraint dependencies</h2>
 * If a {@link Constraint} implementation uses other fields and property values as inputs to its
 * validation logic, it is often useful to register these values as constraint dependencies by
 * returning them from {@link Constraint#getDependencies()}.
 * The data validation system will automatically re-evaluate a constraint when any of its registered
 * dependencies has changed.
 * <p>
 * Many of the constraint factories in the {@link Constraints} class that take an
 * {@link ObservableValue} as an argument also register the argument as a constraint dependency.
 * Some examples include:
 * <ul>
 *     <li>{@link Constraints#greaterThan(ObservableIntegerValue)}
 *     <li>{@link Constraints#between(ObservableIntegerValue, ObservableIntegerValue)}
 *     <li>{@link Constraints#matchesPattern(ObservableStringValue)}
 *     <li>...
 * </ul>
 * <p>
 * The four general-purpose constraint factories are overloaded to accept up to eight dependencies
 * (shown here are the overloads for a single dependency):
 * <ul>
 *     <li>{@link Constraints#validate(ValidationFunction1, ObservableValue)}
 *     <li>{@link Constraints#validateAsync(ValidationFunction1, ObservableValue, Executor)}
 *     <li>{@link Constraints#validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor)}
 *     <li>{@link Constraints#validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor)}
 * </ul>
 * The current values of the dependencies are passed into each invocation of the validation function:
 * <pre>{@code
 *    var dependency1 = new SimpleIntegerProperty();
 *    var dependency2 = new SimpleStringProperty();
 *
 *    Constraint<String, Void> constraint = Constraints.validateAsync(
 *        (String value, Number dep1, String dep2) -> {
 *            // Validate the value and return a ValidationResult
 *            return new ValidationResult(...);
 *        },
 *        dependency1,
 *        dependency2,
 *        ForkJoinPool.commonPool());
 * }</pre>
 *
 * <a id="Collections"></a>
 * <h2>Collection constraints</h2>
 * When a {@link Constraint} is applied to {@link ConstrainedListProperty}, {@link ConstrainedSetProperty} or
 * {@link ConstrainedMapProperty}, the constraint is not evaluated for the collection instance itself, but
 * for each of its containing elements.
 * <p>
 * If a constraint should be evaluated for the collection instance instead of its elements, it must
 * be an implementation of {@link ListConstraint}, {@link SetConstraint} or {@link MapConstraint}.
 * <p>
 * A {@link Constraint} instance can be converted into a collection constraint instance by calling
 * {@link Constraints#forList(Constraint)}, {@link Constraints#forSet(Constraint)}, or
 * {@link Constraints#forMap(Constraint)}.
 * <p>
 * In the following example, the {@link Constraints#notNull() notNull} constraint is applied to all
 * list elements, as well as to the list instance:
 * <pre>{@code
 *    ConstrainedListProperty<String, Void> list = new SimpleConstrainedListProperty<>(
 *            Constraints.notNull(),                      // applies to list elements
 *            Constraints.forList(Constraints.notNull())  // applies to list instance
 *        );
 * }</pre>
 *
 * <a id="AsynchronousValidation"></a>
 * <h2>Asynchronous data validation</h2>
 * The {@link Constraints} class comes with three types of factory methods to create asynchronous constraints.
 * The difference between the three factory types is how cancellation is implemented:
 * <ul>
 *     <li>{@link Constraints#validateAsync(ValidationFunction0, Executor)}<br>
 *         This is the simplest asynchronous constraint, since it does not support cancellation.
 *         When the data validation system cancels a future that was produced by this constraint,
 *         the future does not transition into the {@link CompletableFuture#isCancelled() cancelled}
 *         state before the validation function has run to completion or throws an exception.<p>
 *     <li>{@link Constraints#validateCancellableAsync(CancellableValidationFunction1, ObservableValue, Executor)}<br>
 *         This constraint implements a cooperative cancellation strategy.
 *         The validation function receives a token that it can use to periodically check whether
 *         cancellation was requested, and if that is the case, stop validating and return from the
 *         validation function as soon as possible.<br>
 *         Cooperative cancellation is particularly useful for computationally intensive validation
 *         functions that run in a loop and can therefore check the token repeatedly.<p>
 *     <li>{@link Constraints#validateInterruptibleAsync(ValidationFunction1, ObservableValue, Executor)}<br>
 *         This constraint implements cancellation by thread interruption, and is useful for
 *         IO-bound validation functions that wait on interruptible APIs.<br>
 *         When the data validation system cancels a future that was produced by this factory,
 *         the thread that is executing the validation function is interrupted.
 * </ul>
 * It is recommended to use the built-in factories instead of implementing the {@link Constraint}
 * interface directly for asynchronous constraints.
 * <p>
 * <h3>Thread safety considerations</h3>
 * The data validation system is not inherently thread-safe and can therefore not generally be
 * accessed from multiple concurrent threads.
 * The asynchronous constraint factories in the {@link Constraints} class assume that the
 * {@link ConstrainedProperty} instances to which they apply are only accessed on the JavaFX
 * application thread.
 * <p>
 * The values of dependencies are read <em>before</em> the validation function is executed by
 * the user-specified {@link Executor}.
 * This ensures that no concurrent reads or writes can happen when dependency values are
 * accessed on a background thread within the validation function.
 * <p>
 * However, this guarantee does not cover the <em>internal state</em> of dependency values.
 * If a dependency value is a mutable object, then the application must manually synchronize
 * access to its shared state to prevent concurrent modifications or memory ordering effects.
 * <p>
 * In general, it is recommended to use deeply immutable objects to prevent race conditions.
 *
 * <a id="Visualization"></a>
 * <h2>Visualization</h2>
 * Applications often need to visualize the validation state of data in the user interface.
 * This can be easily achieved by binding the {@link ValidationState} of a {@link ConstrainedValue}
 * to a UI control that serves as the representation of that value:
 * <pre>{@code
 *    class ViewModel {
 *        private final ConstrainedStringProperty<String> name =
 *                new SimpleConstrainedStringProperty<>(Constraints.notNullOrEmpty());
 *
 *        public ConstrainedStringProperty<String> nameProperty() {
 *            return name;
 *        }
 *    }
 *
 *    class View extends Pane {
 *        View(ViewModel viewModel) {
 *            var textField = new TextField();
 *            getChildren().add(textField);
 *            textField.textProperty().bindBidirectional(viewModel.nameProperty());
 *
 *            // The 'name' property will provide validation states for the 'textField' node:
 *            ValidationState.setSource(textField, viewModel.nameProperty());
 *        }
 *    }
 * }</pre>
 * In this example, the validation state of the {@code name} property is applied to the
 * {@code TextField} to which the property is bound.
 *
 * <h3>CSS validation pseudo-classes</h3>
 * The data validation framework adds five CSS pseudo-classes that can be used to style UI controls:
 * <p>
 * <table border="1">
 *     <caption></caption>
 *     <tr><th></th><th>Description</th><th>Corresponding property</th></tr>
 *     <tr>
 *         <td><b><span style="white-space: nowrap;">:validating</span></b></td>
 *         <td>Selects an element that is currently validating</td>
 *         <td>{@link ConstrainedProperty#validatingProperty()}</td>
 *     </tr>
 *     <tr>
 *         <td><b><span style="white-space: nowrap;">:invalid</span></b></td>
 *         <td>Selects an element that failed data validation</td>
 *         <td>{@link ConstrainedProperty#invalidProperty()}</td>
 *     </tr>
 *     <tr>
 *         <td><b><span style="white-space: nowrap;">:valid</span></b></td>
 *         <td>Selects an element that successfully completed data validation</td>
 *         <td>{@link ConstrainedProperty#validProperty()}</td>
 *     </tr>
 *     <tr>
 *         <td><b><span style="white-space: nowrap;">:user-invalid</span></b></td>
 *         <td>Selects an element that failed data validation after the user has interacted with it,
 *             for example by typing or clicking</td>
 *         <td>{@link ValidationState#userInvalidProperty(Node)}</td>
 *     </tr>
 *     <tr>
 *         <td><b><span style="white-space: nowrap;">:user-valid</span></b></td>
 *         <td>Selects an element that successfully completed data validation after the user has
 *             interacted with it, for example by typing or clicking</td>
 *         <td>{@link ValidationState#userValidProperty(Node)}</td>
 *     </tr>
 * </table>
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