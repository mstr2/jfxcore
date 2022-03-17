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

import com.sun.javafx.binding.ContentBinding;
import com.sun.javafx.binding.Logging;
import com.sun.javafx.tk.Toolkit;
import org.jfxcore.validation.ListValidationHelper;
import org.jfxcore.validation.PropertyHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.ConstraintBase;
import javafx.validation.Constraints;
import javafx.validation.ListConstraint;
import javafx.validation.ValidationResult;
import javafx.validation.property.ConstrainedListProperty;
import javafx.validation.property.SimpleConstrainedListProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ListValidationHelperTest {

    private ListValidationHelper<String, String> helper;
    private ConstrainedListProperty<String, String> value;
    private ReadOnlyListProperty<String> constrainedValue;

    private void assertValidationState(
            ListValidationHelper<?, ?> helper,
            boolean validating,
            boolean valid,
            boolean invalid) {
        assertEquals(validating, helper.validatingProperty().get(), "validating");
        assertEquals(valid, helper.validProperty().get(), "valid");
        assertEquals(invalid, helper.invalidProperty().get(), "invalid");
    }

    private void assertValidationState(
            ConstrainedElement<?, ?> element,
            boolean validating,
            boolean valid,
            boolean invalid) {
        assertEquals(validating, element.validatingProperty().get(), "validating");
        assertEquals(valid, element.validProperty().get(), "valid");
        assertEquals(invalid, element.invalidProperty().get(), "invalid");
    }

    @BeforeAll
    static void setupAll() {
        Logging.getLogger().disableLogging();
        Toolkit.getToolkit();
    }

    @SafeVarargs
    final void initialize(ObservableList<String> initialValue, ConstraintBase<? super String, String>... constraints) {
        value = new SimpleConstrainedListProperty<>(initialValue, constraints);
        constrainedValue = value.constrainedValueProperty();
        helper = (ListValidationHelper<String, String>)PropertyHelper.getValidationHelper(value);
    }

    @Test
    public void testConstrainedValueIsNullWhenInitialValueIsNull() {
        initialize(null, Constraints.forList(Constraints.notNull()));
        assertValidationState(helper, false, false, true);
        assertNull(constrainedValue.get());
    }

    @Test
    public void testConstrainedValueIsNullWhenValueIsNull() {
        initialize(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableArrayList("foo", "bar", "baz"));
        assertValidationState(helper, false, true, false);
        assertEquals(List.of("foo", "bar", "baz"), constrainedValue);

        value.set(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableArrayList("foo", "bar", "baz"));
        assertValidationState(helper, false, true, false);
        assertEquals(List.of("foo", "bar", "baz"), constrainedValue);
    }

    @Test
    public void testListConstraintForEmptyListIsEvaluatedOnce() {
        int[] validationCount = new int[1];

        initialize(
            null,
            new ListConstraint<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(List<? super String> value) {
                    validationCount[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }

                @Override public Executor getCompletionExecutor() { return null; }
                @Override public Observable[] getDependencies() { return null; }
            });

        assertEquals(1, validationCount[0]);
        assertEquals(List.of(), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testElementConstraintIsEvaluatedOnceForEachInitialElement() {
        int[] validationCount = new int[1];

        initialize(
            FXCollections.observableArrayList("foo", "bar", "baz"),
            new Constraint<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    validationCount[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }

                @Override public Executor getCompletionExecutor() { return null; }
                @Override public Observable[] getDependencies() { return null; }
            });

        assertEquals(3, validationCount[0]);
        assertEquals(List.of("foo", "bar", "baz"), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testConstrainedValueIsUpdatedWhenPropertyIsValid() {
        initialize(FXCollections.observableArrayList("   "), Constraints.notNullOrBlank());
        assertValidationState(helper, false, false, true);
        assertEquals(List.of(), constrainedValue);

        value.set(0, "foo");
        assertEquals(List.of("foo"), constrainedValue);
        assertValidationState(helper, false, true, false);

        value.addAll("   ", "bar", "baz", "qux");
        assertEquals(List.of("foo"), constrainedValue);
        assertValidationState(helper, false, false, true);

        value.remove(2, 4);
        assertEquals(List.of("foo"), constrainedValue);
        assertValidationState(helper, false, false, true);

        value.remove(1);
        assertEquals(List.of("foo", "qux"), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testConstrainedValueFiresCorrectListChangeNotifications() {
        initialize(FXCollections.observableArrayList(), Constraints.notNullOrBlank());
        ObservableList<String> boundList = FXCollections.observableArrayList();
        ContentBinding.bind(boundList, constrainedValue);
        assertEquals(boundList, constrainedValue);

        value.add("foo");
        value.addAll("   ", "bar", "baz", "qux");
        value.remove(2, 4);
        value.remove(1);
        assertEquals(boundList, constrainedValue);
    }

    @Test
    public void testListConstraintIsEvaluatedWhenElementIsAddedOrRemoved() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableArrayList(),
            Constraints.forList(
                Constraints.validate(list -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(list.stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.add("foo");
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.add("   ");
        assertEquals(3, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.add("qux");
        assertEquals(4, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.remove(1);
        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(List.of("foo", "qux"), constrainedValue);
    }

    @Test
    public void testListConstraintIsEvaluatedWhenObservableListIsReplaced() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableArrayList(),
            Constraints.forList(
                Constraints.validate(list -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(list.stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.set(FXCollections.observableArrayList("foo"));
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.set(FXCollections.observableArrayList("foo", "    "));
        assertEquals(3, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.set(FXCollections.observableArrayList("foo", "    ", "qux"));
        assertEquals(4, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(List.of("foo"), constrainedValue);

        value.set(FXCollections.observableArrayList("foo", "qux"));
        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(List.of("foo", "qux"), constrainedValue);
    }

    @Test
    public void testElementConstraintIsEvaluated() {
        initialize(
            FXCollections.observableArrayList(),
            Constraints.notNullOrBlank());

        assertTrue(value.isValid());

        value.add("foo");
        assertTrue(value.isValid());

        value.add("   ");
        assertFalse(value.isValid());

        value.remove(1);
        assertTrue(value.isValid());
    }

    @Test
    public void testListConstraintIsEvaluatedWhenListIsPermuted() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableArrayList("4", "5", "1", "3", "2"),
            Constraints.forList(
                Constraints.validate(value -> {
                    validatorInvocations[0]++;
                    return ValidationResult.valid();
                })));

        assertEquals(1, validatorInvocations[0]);
        assertTrue(value.isValid());

        FXCollections.sort(value.get());
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
    }

    @Test
    public void testElementConstraintIsNotEvaluatedWhenListIsPermuted() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableArrayList("4", "5", "1", "3", "2"),
            Constraints.validate(value -> {
                validatorInvocations[0]++;
                return ValidationResult.valid();
            }));

        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());

        FXCollections.sort(value.get());
        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());
    }

    @Nested
    class AsynchronousTests extends ConcurrentTestBase {
        @Test
        public void testListConstraintForEmptyListIsEvaluatedOnce() {
            var validationCount = new AtomicInteger();

            runNow(() -> {
                initialize(
                    null,
                    new ListConstraint<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(List<? super String> value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                validationCount.getAndIncrement();
                                return ValidationResult.valid();
                            });
                        }

                        @Override public Executor getCompletionExecutor() { return AsynchronousTests.this::runLater; }
                        @Override public Observable[] getDependencies() { return null; }
                    });

                assertValidationState(helper, true, false, false);
            });

            sleep(75);

            runNow(() -> {
                assertEquals(1, validationCount.get());
                assertEquals(List.of(), constrainedValue);
                assertValidationState(helper, false, true, false);
            });
        }

        @Test
        public void testElementConstraintIsEvaluatedOnceForEachInitialElement() {
            var validationCount = new AtomicInteger();

            runNow(() -> {
                initialize(
                    FXCollections.observableArrayList("foo", "   ", "baz"),
                    new Constraint<>() {
                        @Override
                        public CompletableFuture<ValidationResult<String>> validate(String value) {
                            return CompletableFuture.supplyAsync(() -> {
                                sleep(50);
                                validationCount.getAndIncrement();
                                return new ValidationResult<>(!value.isBlank(), value.isBlank() ? "<blank>" : null);
                            });
                        }

                        @Override public Executor getCompletionExecutor() { return AsynchronousTests.this::runLater; }
                        @Override public Observable[] getDependencies() { return null; }
                    });

                assertValidationState(helper, true, false, false);
                assertEquals(List.of(), constrainedValue);
                var elements = value.getConstrainedElements();
                assertEquals(3, elements.size());
                assertValidationState(elements.get(0), true, false, false);
                assertValidationState(elements.get(1), true, false, false);
                assertValidationState(elements.get(2), true, false, false);
            });

            sleep(100);

            runNow(() -> {
                assertEquals(List.of(), constrainedValue);
                assertEquals(3, validationCount.get());
                assertValidationState(helper, false, false, true);
                var elements = value.getConstrainedElements();
                assertValidationState(elements.get(0), false, true, false);
                assertValidationState(elements.get(1), false, false, true);
                assertValidationState(elements.get(2), false, true, false);
                assertEquals(List.of(), elements.get(0).getDiagnostics());
                assertEquals(List.of("<blank>"), elements.get(1).getDiagnostics());
                assertEquals(List.of(), elements.get(2).getDiagnostics());
            });
        }

        @Test
        public void testConcurrentValidationWithManyInitialValues() {
            List<String> testStrings = new ArrayList<>();
            for (int i = 0; i < 1000; ++i) {
                testStrings.add(i % 2 == 0 ? "abc" : "   ");
            }

            var validationCount = new AtomicInteger();

            runNow(() -> initialize(
                FXCollections.observableArrayList(testStrings),
                new Constraint<>() {
                    @Override
                    public CompletableFuture<ValidationResult<String>> validate(String value) {
                        return CompletableFuture.supplyAsync(() -> {
                            sleep(10);
                            validationCount.getAndIncrement();
                            return new ValidationResult<>(!value.isBlank(), value.isBlank() ? "<blank>" : null);
                        });
                    }

                    @Override public Executor getCompletionExecutor() { return AsynchronousTests.this::runLater; }
                    @Override public Observable[] getDependencies() { return null; }
                }));

            while (runNow(() -> value.isValidating())) {
                sleep(50);
            }

            runNow(() -> {
                assertEquals(testStrings.size(), validationCount.get());
                assertValidationState(helper, false, false, true);

                var elements = value.getConstrainedElements();
                for (int i = 0; i < elements.size(); ++i) {
                    if (i % 2 == 0) {
                        assertValidationState(elements.get(i), false, true, false);
                        assertEquals(List.of(), elements.get(i).getDiagnostics());
                    } else {
                        assertValidationState(elements.get(i), false, false, true);
                        assertEquals(List.of("<blank>"), elements.get(i).getDiagnostics());
                    }
                }
            });
        }

        @Test
        public void testConcurrentValidationWithManySubsequentlyAddedValues() {
            List<String> testStrings = new ArrayList<>();
            for (int i = 0; i < 250; ++i) {
                testStrings.add(i % 2 == 0 ? "abc" : "   ");
            }

            var validationCount = new AtomicInteger();

            runNow(() -> initialize(
                FXCollections.observableArrayList(),
                new Constraint<>() {
                    @Override
                    public CompletableFuture<ValidationResult<String>> validate(String value) {
                        return CompletableFuture.supplyAsync(() -> {
                            sleep(10);
                            validationCount.getAndIncrement();
                            return new ValidationResult<>(!value.isBlank(), value.isBlank() ? "<blank>" : null);
                        });
                    }

                    @Override public Executor getCompletionExecutor() { return AsynchronousTests.this::runLater; }
                    @Override public Observable[] getDependencies() { return null; }
                }));

            for (String s : testStrings) {
                runNow(() -> value.add(s));
                sleep(5);
            }

            while (runNow(() -> value.isValidating())) {
                sleep(50);
            }

            runNow(() -> {
                assertEquals(testStrings.size(), validationCount.get());
                assertValidationState(helper, false, false, true);

                var elements = value.getConstrainedElements();
                for (int i = 0; i < elements.size(); ++i) {
                    if (i % 2 == 0) {
                        assertValidationState(elements.get(i), false, true, false);
                        assertEquals(List.of(), elements.get(i).getDiagnostics());
                    } else {
                        assertValidationState(elements.get(i), false, false, true);
                        assertEquals(List.of("<blank>"), elements.get(i).getDiagnostics());
                    }
                }
            });
        }
    }

}
