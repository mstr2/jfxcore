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
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.SetValidationHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.validation.Constraint;
import javafx.validation.Constraints;
import javafx.validation.SetConstraint;
import javafx.validation.ValidationResult;
import javafx.validation.Validator;
import javafx.validation.property.ConstrainedSetProperty;
import javafx.validation.property.SimpleConstrainedSetProperty;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("Convert2Lambda")
public class SetValidationHelperTest {

    private SetValidationHelper<String, String> helper;
    private ConstrainedSetProperty<String, String> value;
    private ReadOnlySetProperty<String> constrainedValue;

    private void assertValidationState(
            SetValidationHelper<?, ?> helper,
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
    final void initialize(ObservableSet<String> initialValue, Constraint<? super String, String>... constraints) {
        value = new SimpleConstrainedSetProperty<>(initialValue, constraints);
        constrainedValue = value.constrainedValueProperty();
        helper = (SetValidationHelper<String, String>)PropertyHelper.getValidationHelper(value);
    }

    @Test
    public void testConstrainedValueIsNullWhenInitialValueIsNull() {
        initialize(null, Constraints.forSet(Constraints.notNull()));
        assertValidationState(helper, false, false, true);
        assertNull(constrainedValue.get());
    }

    @Test
    public void testConstrainedValueIsNullWhenValueIsNull() {
        initialize(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableSet("foo", "bar", "baz"));
        assertValidationState(helper, false, true, false);
        assertEquals(Set.of("foo", "bar", "baz"), constrainedValue);

        value.set(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableSet("foo", "bar", "baz"));
        assertValidationState(helper, false, true, false);
        assertEquals(Set.of("foo", "bar", "baz"), constrainedValue);
    }

    @Test
    public void testSetConstraintForEmptySetIsEvaluatedOnce() {
        int[] validationCount = new int[1];

        initialize(
            null,
            new SetConstraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(ObservableSet<? super String> value) {
                    validationCount[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, null));

        assertEquals(1, validationCount[0]);
        assertEquals(Set.of(), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testElementConstraintIsEvaluatedOnceForEachInitialElement() {
        int[] validationCount = new int[1];

        initialize(
            FXCollections.observableSet("foo", "bar", "baz"),
            new Constraint<>(new Validator<>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    validationCount[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }
            }, null, null));

        assertEquals(3, validationCount[0]);
        assertEquals(Set.of("foo", "bar", "baz"), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testConstrainedValueFiresCorrectSetChangeNotifications() {
        initialize(FXCollections.observableSet(), Constraints.notNullOrBlank());
        ObservableSet<String> boundList = FXCollections.observableSet();
        ContentBinding.bind(boundList, constrainedValue);
        assertEquals(boundList, constrainedValue);

        value.add("foo");
        value.addAll(Set.of("   ", "bar", "baz", "qux"));
        value.remove("baz");
        value.add("quux");
        value.removeAll(Set.of("   ", "foo"));

        assertEquals(boundList, constrainedValue);
    }

    @Test
    public void testSetConstraintIsEvaluatedWhenElementIsAddedOrRemoved() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableSet(),
            Constraints.forSet(
                Constraints.validate(set -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(set.stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.add("foo");
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.add("   ");
        value.addAll(Set.of("bar", "baz"));
        assertEquals(5, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.add("qux");
        assertEquals(6, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.removeAll(Set.of("   ", "bar", "baz"));
        assertEquals(9, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Set.of("foo", "qux"), constrainedValue);
    }

    @Test
    public void testSetConstraintIsEvaluatedWhenObservableSetIsReplaced() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableSet(),
            Constraints.forSet(
                Constraints.validate(set -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(set.stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.set(FXCollections.observableSet("foo"));
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.set(FXCollections.observableSet("foo", "    "));
        assertEquals(3, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.set(FXCollections.observableSet("foo", "    ", "qux"));
        assertEquals(4, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Set.of("foo"), constrainedValue);

        value.set(FXCollections.observableSet("foo", "qux"));
        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Set.of("foo", "qux"), constrainedValue);
    }

}
