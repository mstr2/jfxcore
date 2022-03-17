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
import org.jfxcore.validation.MapValidationHelper;
import org.jfxcore.validation.PropertyHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.validation.Constraint;
import javafx.validation.ConstraintBase;
import javafx.validation.Constraints;
import javafx.validation.MapConstraint;
import javafx.validation.ValidationResult;
import javafx.validation.property.ConstrainedMapProperty;
import javafx.validation.property.SimpleConstrainedMapProperty;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

public class MapValidationHelperTest {

    private MapValidationHelper<Integer, String, String> helper;
    private ConstrainedMapProperty<Integer, String, String> value;
    private ReadOnlyMapProperty<Integer, String> constrainedValue;

    private void assertValidationState(
            MapValidationHelper<?, ?, ?> helper,
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
    final void initialize(ObservableMap<Integer, String> initialValue, ConstraintBase<? super String, String>... constraints) {
        value = new SimpleConstrainedMapProperty<>(initialValue, constraints);
        constrainedValue = value.constrainedValueProperty();
        helper = (MapValidationHelper<Integer, String, String>)PropertyHelper.getValidationHelper(value);
    }

    @Test
    public void testConstrainedValueIsNullWhenInitialValueIsNull() {
        initialize(null, Constraints.forMap(Constraints.notNull()));
        assertValidationState(helper, false, false, true);
        assertNull(constrainedValue.get());
    }

    @Test
    public void testConstrainedValueIsNullWhenValueIsNull() {
        initialize(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableMap(Map.of(0, "foo", 1, "bar", 2, "baz")));
        assertValidationState(helper, false, true, false);
        assertEquals(Map.of(0, "foo", 1, "bar", 2, "baz"), constrainedValue);

        value.set(null);
        assertValidationState(helper, false, true, false);
        assertNull(constrainedValue.get());

        value.set(FXCollections.observableMap(Map.of(0, "foo", 1, "bar", 2, "baz")));
        assertValidationState(helper, false, true, false);
        assertEquals(Map.of(0, "foo", 1, "bar", 2, "baz"), constrainedValue);
    }

    @Test
    public void testMapConstraintForEmptyMapIsEvaluatedOnce() {
        int[] validationCount = new int[1];

        initialize(
            null,
            new MapConstraint<Integer, String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(
                        Map<? super Integer, ? super String> value) {
                    validationCount[0]++;
                    return CompletableFuture.completedFuture(ValidationResult.valid());
                }

                @Override public Executor getCompletionExecutor() { return null; }
                @Override public Observable[] getDependencies() { return null; }
            });

        assertEquals(1, validationCount[0]);
        assertEquals(Map.of(), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testElementConstraintIsEvaluatedOnceForEachInitialElement() {
        int[] validationCount = new int[1];

        initialize(
            FXCollections.observableMap(Map.of(0, "foo", 1, "bar", 2, "baz")),
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
        assertEquals(Map.of(0, "foo", 1, "bar", 2, "baz"), constrainedValue);
        assertValidationState(helper, false, true, false);
    }

    @Test
    public void testConstrainedValueFiresCorrectMapChangeNotifications() {
        initialize(FXCollections.observableHashMap(), Constraints.notNullOrBlank());
        ObservableMap<Integer, String> boundList = FXCollections.observableHashMap();
        ContentBinding.bind(boundList, constrainedValue);
        assertEquals(boundList, constrainedValue);

        value.put(0, "foo");
        value.putAll(Map.of(1, "   ", 2, "bar", 3, "baz", 4, "qux"));
        value.keySet().removeAll(Set.of(2, 3));
        value.remove(1);
        assertEquals(boundList, constrainedValue);
    }

    @Test
    public void testSetConstraintIsEvaluatedWhenElementIsAddedOrRemoved() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableHashMap(),
            Constraints.forMap(
                Constraints.validate(map -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(map.values().stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.put(0, "foo");
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.put(1, "   ");
        value.putAll(Map.of(2, "bar", 3, "baz"));
        assertEquals(5, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.put(4, "qux");
        assertEquals(6, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.keySet().removeAll(Set.of(1, 2, 3));
        assertEquals(9, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Map.of(0, "foo", 4, "qux"), constrainedValue);
    }

    @Test
    public void testSetConstraintIsEvaluatedWhenObservableSetIsReplaced() {
        int[] validatorInvocations = new int[1];

        initialize(
            FXCollections.observableHashMap(),
            Constraints.forMap(
                Constraints.validate(map -> {
                    validatorInvocations[0]++;
                    return new ValidationResult<>(map.values().stream().noneMatch(String::isBlank), null);
                })));

        assertEquals(1, validatorInvocations[0]);

        value.set(FXCollections.observableMap(Map.of(0, "foo")));
        assertEquals(2, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.set(FXCollections.observableMap(Map.of(0, "foo", 1, "    ")));
        assertEquals(3, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.set(FXCollections.observableMap(Map.of(0, "foo", 1, "    ", 2, "qux")));
        assertEquals(4, validatorInvocations[0]);
        assertFalse(value.isValid());
        assertEquals(Map.of(0, "foo"), constrainedValue);

        value.set(FXCollections.observableMap(Map.of(0, "foo", 1, "qux")));
        assertEquals(5, validatorInvocations[0]);
        assertTrue(value.isValid());
        assertEquals(Map.of(0, "foo", 1, "qux"), constrainedValue);
    }

}
