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

package test.org.jfxcore.validation;

import com.sun.javafx.binding.Logging;
import com.sun.javafx.tk.Toolkit;
import org.jfxcore.validation.DeferredStringProperty;
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.ValidateTask;
import org.jfxcore.validation.ValidationHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.javafx.beans.InvalidationListenerMock;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.NodeState;
import javafx.scene.layout.Region;
import javafx.validation.Constraint;
import javafx.validation.ValidationResult;
import javafx.validation.ValidationState;
import javafx.validation.Validator;
import javafx.validation.property.ConstrainedStringProperty;
import javafx.validation.property.SimpleConstrainedStringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"FieldCanBeLocal", "unused", "Convert2Lambda", "CodeBlock2Expr"})
public class ValidationHelperTest {

    private ValidationHelper<String, String> helper;
    private ConstrainedStringProperty<String> value;
    private DeferredStringProperty constrainedValue;

    private void assertValidationState(
            ValidationHelper<?, ?> helper,
            boolean validating,
            boolean valid,
            boolean invalid) {
        assertEquals(validating, helper.validatingProperty().get(), "validating");
        assertEquals(valid, helper.validProperty().get(), "valid");
        assertEquals(invalid, helper.invalidProperty().get(), "invalid");
    }

    @BeforeAll
    static void setupAll() {
        Logging.getLogger().disableLogging();
        Toolkit.getToolkit();
    }

    @SafeVarargs
    final void initialize(Constraint<? super String, String>... constraints) {
        value = new SimpleConstrainedStringProperty<>(constraints);
        constrainedValue = (DeferredStringProperty)value.constrainedValueProperty();
        helper = PropertyHelper.getValidationHelper(value);
    }

    @Test
    public void testConstrainedValueEqualsValueWhenNoConstraintsAreSpecified() {
        initialize();
        value.set("foo");
        assertEquals("foo", constrainedValue.get());
    }

    @Test
    public void testAlwaysValidValidator() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, null));

        value.set("foo");
        assertEquals("foo", constrainedValue.get());
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testAlwaysInvalidValidator() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.completedFuture(ValidationResult.invalid());
                }
            }, null, null));

        value.set("foo");
        assertNull(constrainedValue.get());
        assertValidationState(helper, false, false, true);
    }

    @Test
    public void testUserValidWithAlwaysValidValidator() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, null));

        var control = new Region();
        var userValid = ValidationState.userValidProperty(control);
        ValidationState.setSource(control, value);

        value.set("foo");
        assertEquals("foo", constrainedValue.get());
        assertFalse(userValid.get());
        assertValidationState(helper, false, true, false);

        NodeState.setUserModified(control, true);
        assertTrue(userValid.get());
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testUserInvalidWithAlwaysInvalidValidator() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.completedFuture(ValidationResult.invalid());
                }
            }, null, null));

        var control = new Region();
        var userInvalid = ValidationState.userInvalidProperty(control);
        ValidationState.setSource(control, value);

        value.set("foo");
        assertNull(constrainedValue.get());
        assertFalse(userInvalid.get());
        assertValidationState(helper, false, false, true);

        NodeState.setUserModified(control, true);
        assertTrue(userInvalid.get());
        assertValidationState(helper, false, false, true);
    }

    @Test
    public void testExceptionInValidatorStopsValidation() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    throw new RuntimeException();
                }
            }, null, null));

        assertValidationState(helper, false, false, false);
    }

    @Test
    public void testValidatorYieldsDiagnosticOrError() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value != null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(true, value.isBlank() ? "<blank>" : null));
                    }
                    return CompletableFuture.completedFuture(new ValidationResult<>(false, "<null>"));
                }
            }, null, null));

        var invalidSubList = helper.diagnosticsProperty().invalidSubList();
        var validSubList = helper.diagnosticsProperty().validSubList();

        assertEquals(List.of("<null>"), invalidSubList);
        assertEquals(List.of(), validSubList);
        assertValidationState(helper, false, false, true);

        value.set("    ");
        assertEquals(List.of(), invalidSubList);
        assertEquals(List.of("<blank>"), validSubList);
        assertValidationState(helper, false, true, false);

        value.set("foo");
        assertEquals(List.of(), invalidSubList);
        assertEquals(List.of(), validSubList);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testMultipleDiagnosticsAndErrors() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value == null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(false, "<blank-null>"));
                    }
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(true, value.isBlank() ? "<blank>" : null));
                }
            }, null, null),
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value == null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(false, "<empty-null>"));
                    }
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(true, value.isEmpty() ? "<empty>" : null));
                }
            }, null, null),
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    boolean valid = value != null && value.length() > 4;
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(valid, !valid ? "<short>" : null));
                }
            }, null, null));

        var invalidSubList = helper.diagnosticsProperty().invalidSubList();
        var validSubList = helper.diagnosticsProperty().validSubList();

        assertEquals(List.of(), validSubList);
        assertEquals(List.of("<blank-null>", "<empty-null>", "<short>"), invalidSubList);
        assertValidationState(helper, false, false, true);

        value.set("    ");
        assertEquals(List.of("<blank>"), validSubList);
        assertEquals(List.of("<short>"), invalidSubList);
        assertValidationState(helper, false, false, true);

        value.set("");
        assertEquals(List.of("<blank>", "<empty>"), validSubList);
        assertEquals(List.of("<short>"), invalidSubList);
        assertValidationState(helper, false, false, true);

        value.set("foo");
        assertEquals(List.of(), validSubList);
        assertEquals(List.of("<short>"), invalidSubList);
        assertValidationState(helper, false, false, true);

        value.set("foobar");
        assertEquals(List.of(), validSubList);
        assertEquals(List.of(), invalidSubList);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testConstraintIsEvaluatedWhenDependencyIsInvalidated() {
        var dep1 = new SimpleDoubleProperty();
        var dep2 = new SimpleDoubleProperty();
        var validator1Count = new int[1];
        var validator2Count = new int[1];

        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    validator1Count[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, new Observable[] {dep1}),
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    validator2Count[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, new Observable[] {dep2}));

        assertEquals(1, validator1Count[0]);
        assertEquals(1, validator2Count[0]);

        value.set("foo");
        assertEquals(2, validator1Count[0]);
        assertEquals(2, validator2Count[0]);

        dep1.set(1);
        assertEquals(3, validator1Count[0]);
        assertEquals(2, validator2Count[0]);

        dep2.set(1);
        assertEquals(3, validator1Count[0]);
        assertEquals(3, validator2Count[0]);
    }

    @Test
    public void testValidatorDoesNotValidateObservable() {
        initialize(
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, null));

        var listener = new InvalidationListenerMock();
        value.addListener(listener);
        value.get(); // validates the observable
        listener.check(null, 0);

        value.set("foo"); // invalidates the observable, InvalidationListener is invoked
        listener.check(value, 1);

        value.set("bar"); // observable is already invalid, InvalidationListener not invoked
        listener.check(null, 0);
    }

    @Nested
    class AsynchronousTests extends ConcurrentTestBase {
        @Test
        public void testValidatingIsTrueWhileValidatorIsRunning() {
            runNow(() -> {
                initialize(
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                return ValidationResult.valid();
                            });
                        }
                    }, this::runLater, null));

                assertValidationState(helper, true, false, false);
            });

            sleep(100);

            runNow(() -> {
                assertValidationState(helper, false, true, false);

                value.set("foo");
                assertValidationState(helper, true, false, false);
                assertNull(constrainedValue.get());
            });

            sleep(100);

            runNow(() -> {
                assertValidationState(helper, false, true, false);
                assertEquals("foo", constrainedValue.get());
            });
        }

        @Test
        public void testExceptionInValidatorStopsValidation() {
            runNow(() -> {
                initialize(
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            sleep(50);
                            throw new RuntimeException();
                        }
                    }, this::runLater, null));
            });

            sleep(100);

            runNow(() -> {
                assertValidationState(helper, false, false, false);
            });
        }

        @Test
        public void testExceptionInValidatorFutureStopsValidation() {
            runNow(() -> {
                initialize(
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                throw new RuntimeException();
                            });
                        }
                    }, this::runLater, null));

                assertValidationState(helper, true, false, false);
            });

            sleep(100);

            runNow(() -> {
                assertValidationState(helper, false, false, false);
            });
        }

        @Test
        public void testValidatorYieldsDiagnosticOrError() {
            runNow(() -> {
                initialize(
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                if (value != null) {
                                    return new ValidationResult<>(true, value.isBlank() ? "<blank>" : null);
                                }
                                return new ValidationResult<>(false, "<null>");
                            });
                        }
                    }, this::runLater, null));

                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of("<null>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, false, false, true);

                value.set("    ");
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of("<blank>"), helper.diagnosticsProperty().validSubList());
                assertEquals("    ", constrainedValue.get());
                assertValidationState(helper, false, true, false);

                value.set("foo");
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertEquals("foo", constrainedValue.get());
                assertValidationState(helper, false, true, false);
            });
        }

        @Test
        public void testMultipleDiagnosticsAndErrors() {
            runNow(() -> {
                initialize(
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                if (value == null) {
                                    return new ValidationResult<>(false, "<blank-null>");
                                }
                                return new ValidationResult<>(true, value.isBlank() ? "<blank>" : null);
                            });
                        }
                    }, this::runLater, null),
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                if (value == null) {
                                    return new ValidationResult<>(false, "<empty-null>");
                                }
                                return new ValidationResult<>(true, value.isEmpty() ? "<empty>" : null);
                            });
                        }
                    }, this::runLater, null),
                    new Constraint<>(new Validator<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(100);
                                boolean valid = value != null && value.length() > 4;
                                return new ValidationResult<>(valid, !valid ? "<short>" : null);
                            });
                        }
                    }, this::runLater, null));

                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of("<blank-null>", "<empty-null>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, true, false, true);
            });

            sleep(50);

            runNow(() -> {
                assertEquals(List.of("<blank-null>", "<empty-null>", "<short>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, false, false, true);

                value.set("    ");
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of("<blank>"), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, true, false, false);
            });

            sleep(50);

            runNow(() -> {
                assertEquals(List.of("<short>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of("<blank>"), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, false, false, true);

                value.set("");
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of("<blank>", "<empty>"), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, true, false, false);
            });

            sleep(50);

            runNow(() -> {
                assertEquals(List.of("<short>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of("<blank>", "<empty>"), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, false, false, true);

                value.set("foo");
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, true, false, false);
            });

            sleep(50);

            runNow(() -> {
                assertEquals(List.of("<short>"), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertNull(constrainedValue.get());
                assertValidationState(helper, false, false, true);

                value.set("foobar");
                assertValidationState(helper, true, false, false);
            });

            sleep(125);

            runNow(() -> {
                assertEquals(List.of(), helper.diagnosticsProperty().invalidSubList());
                assertEquals(List.of(), helper.diagnosticsProperty().validSubList());
                assertEquals("foobar", constrainedValue.get());
                assertValidationState(helper, false, true, false);
            });
        }

        private record TestValidator(Supplier<ValidationResult<String>> action) implements Validator<String, String> {
            @Override
            public CompletableFuture<ValidationResult<String>> validate(String value) {
                var task = new ValidateTask<String, String>(value) {
                    @Override
                    protected ValidationResult<String> apply(String value) {
                        return action.get();
                    }
                };
                ForkJoinPool.commonPool().execute(task);
                return task;
            }
        }

        @Test
        public void testConstraintIsEvaluatedWhenDependencyIsInvalidated() {
            var dep1 = new SimpleDoubleProperty();
            var dep2 = new SimpleDoubleProperty();
            var validator1Count = new AtomicInteger();
            var validator2Count = new AtomicInteger();

            runNow(() -> {
                initialize(
                    new Constraint<>(new TestValidator(() -> {
                        sleep(50);
                        validator1Count.getAndIncrement();
                        return ValidationResult.valid();
                    }), this::runLater, new Observable[] {dep1}),
                    new Constraint<>(new TestValidator(() -> {
                        sleep(50);
                        validator2Count.getAndIncrement();
                        return ValidationResult.valid();
                    }), this::runLater, new Observable[] {dep2}));

                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(1, validator1Count.get());
                assertEquals(1, validator2Count.get());
                assertValidationState(helper, false, true, false);

                dep1.set(1);
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(2, validator1Count.get());
                assertEquals(1, validator2Count.get());
                assertValidationState(helper, false, true, false);

                value.set("foo"); // Starts both validators
                dep2.set(2); // Cancels and re-starts the second validator
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(3, validator1Count.get());
                assertEquals(2, validator2Count.get());
                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(3, validator1Count.get());
                assertEquals(3, validator2Count.get());
                assertValidationState(helper, false, true, false);
            });
        }

        @Test
        public void testChangeNotificationIsElidedForIntermediateCompletion() {
            var validValues = new ArrayList<Boolean>();
            var invalidValues = new ArrayList<Boolean>();
            var validatingValues = new ArrayList<Boolean>();
            var constrainedValues = new ArrayList<String>();

            runNow(() -> {
                initialize(
                    new Constraint<>(new TestValidator(() -> {
                        sleep(50);
                        return ValidationResult.valid();
                    }), this::runLater, null));
            });

            sleep(75);

            runNow(() -> {
                assertValidationState(helper, false, true, false);

                value.validProperty().addListener((obs, o, n) -> validValues.add(n));
                value.invalidProperty().addListener((obs, o, n) -> invalidValues.add(n));
                value.validatingProperty().addListener((obs, o, n) -> validatingValues.add(n));
                value.constrainedValueProperty().addListener((obs, o, n) -> constrainedValues.add(n));

                // Starts the validator for 'foo' and schedules a second validation run for 'bar'.
                // The first validation run results in an intermediate completion.
                value.set("foo");
                value.set("bar");

                assertValidationState(helper, true, false, false);
            });

            sleep(125);

            runNow(() -> {
                assertValidationState(helper, false, true, false);
                assertEquals(List.of(false, true), validValues);
                assertEquals(List.of(), invalidValues);
                assertEquals(List.of(true, false), validatingValues);
                assertEquals(1, constrainedValues.size());
                assertEquals("bar", constrainedValues.get(0));
            });
        }
    }

}
