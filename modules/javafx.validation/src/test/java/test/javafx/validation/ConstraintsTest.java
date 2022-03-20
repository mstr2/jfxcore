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

package test.javafx.validation;

import org.jfxcore.validation.DeferredDoubleProperty;
import org.jfxcore.validation.PropertyHelper;
import org.junit.jupiter.api.Test;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.validation.Constraint;
import javafx.validation.Constraints;
import javafx.validation.ValidationResult;
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
import javafx.validation.property.SimpleConstrainedDoubleProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class ConstraintsTest {

    private static final int MAX_DEPS = 8;

    private List<DoubleProperty> doublePropertiesList(int num) {
        List<DoubleProperty> properties = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            properties.add(new SimpleDoubleProperty());
        }

        return properties;
    }

    private Class<?>[] paramTypes(Object validationFunction, int dependencies, boolean withExecutor) {
        var params = new ArrayList<Class<?>>();
        params.add(validationFunction.getClass().getInterfaces()[0]);
        for (int i = 0; i < dependencies; ++i) {
            params.add(ObservableValue.class);
        }
        if (withExecutor) {
            params.add(Executor.class);
        }
        return params.toArray(Class[]::new);
    }

    private Object[] arguments(Object validator, List<DoubleProperty> dependencies, Executor executor) {
        var args = new ArrayList<>();
        args.add(validator);
        args.addAll(dependencies);
        if (executor != null) {
            args.add(executor);
        }
        return args.toArray();
    }

    private Object[] validationFunctions(Supplier<ValidationResult<Object>> supplier) {
        ValidationFunction0<Number, Object> func0 = v -> supplier.get();
        ValidationFunction1<Number, Number, Object> func1 = (v, d1) -> supplier.get();
        ValidationFunction2<Number, Number, Number, Object> func2 = (v, d1, d2) -> supplier.get();
        ValidationFunction3<Number, Number, Number, Number, Object> func3 = (v, d1, d2, d3) -> supplier.get();
        ValidationFunction4<Number, Number, Number, Number, Number, Object> func4 = (v, d1, d2, d3, d4) -> supplier.get();
        ValidationFunction5<Number, Number, Number, Number, Number, Number, Object> func5 = (v, d1, d2, d3, d4, d5) -> supplier.get();
        ValidationFunction6<Number, Number, Number, Number, Number, Number, Number, Object> func6 = (v, d1, d2, d3, d4, d5, d6) -> supplier.get();
        ValidationFunction7<Number, Number, Number, Number, Number, Number, Number, Number, Object> func7 = (v, d1, d2, d3, d4, d5, d6, d7) -> supplier.get();
        ValidationFunction8<Number, Number, Number, Number, Number, Number, Number, Number, Number, Object> func8 = (v, d1, d2, d3, d4, d5, d6, d7, d8) -> supplier.get();
        return new Object[] {func0, func1, func2, func3, func4, func5, func6, func7, func8};
    }

    private Object[] cancellableValidationFunctions(Supplier<ValidationResult<Object>> supplier) {
        CancellableValidationFunction0<Number, Object> func0 = (v, c) -> supplier.get();
        CancellableValidationFunction1<Number, Number, Object> func1 = (v, d1, c) -> supplier.get();
        CancellableValidationFunction2<Number, Number, Number, Object> func2 = (v, d1, d2, c) -> supplier.get();
        CancellableValidationFunction3<Number, Number, Number, Number, Object> func3 = (v, d1, d2, d3, c) -> supplier.get();
        CancellableValidationFunction4<Number, Number, Number, Number, Number, Object> func4 = (v, d1, d2, d3, d4, c) -> supplier.get();
        CancellableValidationFunction5<Number, Number, Number, Number, Number, Number, Object> func5 = (v, d1, d2, d3, d4, d5, c) -> supplier.get();
        CancellableValidationFunction6<Number, Number, Number, Number, Number, Number, Number, Object> func6 = (v, d1, d2, d3, d4, d5, d6, c) -> supplier.get();
        CancellableValidationFunction7<Number, Number, Number, Number, Number, Number, Number, Number, Object> func7 = (v, d1, d2, d3, d4, d5, d6, d7, c) -> supplier.get();
        CancellableValidationFunction8<Number, Number, Number, Number, Number, Number, Number, Number, Number, Object> func8 = (v, d1, d2, d3, d4, d5, d6, d7, d8, c) -> supplier.get();
        return new Object[] {func0, func1, func2, func3, func4, func5, func6, func7, func8};
    }

    @SuppressWarnings({"unchecked", "unused"})
    private void testValidateImpl(String constraintName, Object[] functions, int[] calls, boolean withExecutor)
            throws ReflectiveOperationException {
        for (int i = 0; i < MAX_DEPS; ++i) {
            calls[0] = 0;

            var dependencies = doublePropertiesList(i);
            var method = Constraints.class.getMethod(constraintName, paramTypes(functions[i], i, withExecutor));
            var constraint = (Constraint<? super Number, Object>)method.invoke(
                null, arguments(functions[i], dependencies, withExecutor ? Runnable::run : null));
            var value = new SimpleConstrainedDoubleProperty<>(constraint);
            var constrainedValue = (DeferredDoubleProperty)value.constrainedValueProperty();
            var validationHelper = PropertyHelper.getValidationHelper(value);

            assertEquals(1, calls[0]);
            value.set(999);
            assertEquals(2, calls[0]);

            for (int j = 0; j < i; ++j) {
                dependencies.get(j).set(j);
                assertEquals(j + 2, calls[0]);
            }
        }
    }

    @Test
    public void testValidate() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testValidateImpl("validate", validationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls, false);
    }

    @Test
    public void testValidateAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testValidateImpl("validateAsync", validationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls, true);
    }

    @Test
    public void testValidateInterruptibleAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testValidateImpl("validateInterruptibleAsync", validationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls, true);
    }

    @Test
    public void testValidateCancellableAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testValidateImpl("validateCancellableAsync", cancellableValidationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls, true);
    }

    @Test
    public void testNotNull() throws ExecutionException, InterruptedException {
        var constraint = Constraints.<String, Object>notNull();
        assertFalse(constraint.validate(null).get().isValid());
        assertTrue(constraint.validate("").get().isValid());
        assertTrue(constraint.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrBlank() throws ExecutionException, InterruptedException {
        var constraint = Constraints.notNullOrBlank();
        assertFalse(constraint.validate(null).get().isValid());
        assertFalse(constraint.validate("").get().isValid());
        assertFalse(constraint.validate("    ").get().isValid());
        assertTrue(constraint.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrEmpty() throws ExecutionException, InterruptedException {
        var constraint = Constraints.notNullOrEmpty();
        assertFalse(constraint.validate(null).get().isValid());
        assertFalse(constraint.validate("").get().isValid());
        assertTrue(constraint.validate("    ").get().isValid());
        assertTrue(constraint.validate("test").get().isValid());
    }

    @Test
    public void testMatchesPattern() throws ExecutionException, InterruptedException {
        var constraint = Constraints.matchesPattern("[abc]+");
        assertFalse(constraint.validate(null).get().isValid());
        assertFalse(constraint.validate("").get().isValid());
        assertFalse(constraint.validate("def").get().isValid());
        assertTrue(constraint.validate("abc").get().isValid());
        assertTrue(constraint.validate("aaa").get().isValid());
    }

    @Test
    public void testMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        var constraint = Constraints.matchesPattern(pattern);
        assertFalse(constraint.validate(null).get().isValid());
        assertFalse(constraint.validate("").get().isValid());
        assertFalse(constraint.validate("def").get().isValid());
        assertTrue(constraint.validate("abc").get().isValid());
        assertTrue(constraint.validate("aaa").get().isValid());

        pattern.set("[def]+");
        assertFalse(constraint.validate("abc").get().isValid());
        assertFalse(constraint.validate("aaa").get().isValid());
        assertTrue(constraint.validate("ddd").get().isValid());
        assertTrue(constraint.validate("defdefdef").get().isValid());
    }

    @Test
    public void testNotMatchesPattern() throws ExecutionException, InterruptedException {
        var constraint = Constraints.notMatchesPattern("[abc]+");
        assertTrue(constraint.validate(null).get().isValid());
        assertTrue(constraint.validate("").get().isValid());
        assertTrue(constraint.validate("def").get().isValid());
        assertFalse(constraint.validate("abc").get().isValid());
        assertFalse(constraint.validate("aaa").get().isValid());
    }

    @Test
    public void testNotMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        var constraint = Constraints.notMatchesPattern(pattern);
        assertTrue(constraint.validate(null).get().isValid());
        assertTrue(constraint.validate("").get().isValid());
        assertTrue(constraint.validate("def").get().isValid());
        assertFalse(constraint.validate("abc").get().isValid());
        assertFalse(constraint.validate("aaa").get().isValid());

        pattern.set("[def]+");
        assertTrue(constraint.validate("abc").get().isValid());
        assertTrue(constraint.validate("aaa").get().isValid());
        assertFalse(constraint.validate("ddd").get().isValid());
        assertFalse(constraint.validate("defdefdef").get().isValid());
    }

    @Test
    public void testBetweenInt() throws ExecutionException, InterruptedException {
        var constraint = Constraints.between(5, 10);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
    }

    @Test
    public void testBetweenLong() throws ExecutionException, InterruptedException {
        var constraint = Constraints.between(5L, 10L);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
    }

    @Test
    public void testBetweenFloat() throws ExecutionException, InterruptedException {
        var constraint = Constraints.between(5F, 10F);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
    }

    @Test
    public void testBetweenDouble() throws ExecutionException, InterruptedException {
        var constraint = Constraints.between(5D, 10D);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
    }

    @Test
    public void testBetweenObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        var max = new SimpleIntegerProperty(10);
        var constraint = Constraints.between(min, max);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
        min.set(8);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
        max.set(9);
        assertFalse(constraint.validate(9).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        var max = new SimpleLongProperty(10);
        var constraint = Constraints.between(min, max);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
        min.set(8);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
        max.set(9);
        assertFalse(constraint.validate(9).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        var max = new SimpleFloatProperty(10);
        var constraint = Constraints.between(min, max);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
        min.set(8);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
        max.set(9);
        assertFalse(constraint.validate(9).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        var max = new SimpleDoubleProperty(10);
        var constraint = Constraints.between(min, max);
        assertFalse(constraint.validate(4).get().isValid());
        assertTrue(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
        assertFalse(constraint.validate(10).get().isValid());
        min.set(8);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
        max.set(9);
        assertFalse(constraint.validate(9).get().isValid());
        assertTrue(constraint.validate(8).get().isValid());
    }

    @Test
    public void testGreaterThanInt() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThan(5);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanLong() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThan(5L);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanFloat() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThan(5F);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanDouble() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThan(5D);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        var constraint = Constraints.greaterThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        var constraint = Constraints.greaterThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        var constraint = Constraints.greaterThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        var constraint = Constraints.greaterThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToInt() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThanOrEqualTo(6);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToLong() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThanOrEqualTo(6L);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThanOrEqualTo(6F);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        var constraint = Constraints.greaterThanOrEqualTo(6D);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        var constraint = Constraints.greaterThanOrEqualTo(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        var constraint = Constraints.greaterThanOrEqualTo(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        var constraint = Constraints.greaterThanOrEqualTo(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        var constraint = Constraints.greaterThanOrEqualTo(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testLessThanInt() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThan(5);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
    }

    @Test
    public void testLessThanLong() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThan(5L);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
    }

    @Test
    public void testLessThanFloat() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThan(5F);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
    }

    @Test
    public void testLessThanDouble() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThan(5D);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
    }

    @Test
    public void testLessThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        var constraint = Constraints.lessThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        var constraint = Constraints.lessThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        var constraint = Constraints.lessThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        var constraint = Constraints.lessThan(min);
        assertFalse(constraint.validate(5).get().isValid());
        assertTrue(constraint.validate(4).get().isValid());
        min.set(10);
        assertFalse(constraint.validate(10).get().isValid());
        assertTrue(constraint.validate(9).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToInt() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThanOrEqualTo(6);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToLong() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThanOrEqualTo(6L);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThanOrEqualTo(6F);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        var constraint = Constraints.lessThanOrEqualTo(6D);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        var constraint = Constraints.lessThanOrEqualTo(min);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(12).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        var constraint = Constraints.lessThanOrEqualTo(min);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(12).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        var constraint = Constraints.lessThanOrEqualTo(min);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(12).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        var constraint = Constraints.lessThanOrEqualTo(min);
        assertFalse(constraint.validate(7).get().isValid());
        assertTrue(constraint.validate(6).get().isValid());
        min.set(11);
        assertFalse(constraint.validate(12).get().isValid());
        assertTrue(constraint.validate(11).get().isValid());
    }

}
