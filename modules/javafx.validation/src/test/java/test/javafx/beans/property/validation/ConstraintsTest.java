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

package test.javafx.beans.property.validation;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.validation.Constraint;
import javafx.beans.property.validation.ValidationResult;
import javafx.beans.property.validation.Validator;
import javafx.beans.property.validation.Constraints;
import javafx.beans.property.validation.function.*;
import javafx.beans.value.ObservableValue;
import org.jfxcore.beans.property.validation.DoublePropertyImpl;
import org.jfxcore.beans.property.validation.ValidationHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintsTest {

    private static final int MAX_DEPS = 8;

    private static class DoublePropertyTestImpl extends DoublePropertyImpl {
        public DoublePropertyTestImpl(double initialValue) { super(initialValue); }
        @Override public Object getBean() { return null; }
        @Override public String getName() { return null; }
    }

    private List<DoubleProperty> doublePropertiesList(int num) {
        List<DoubleProperty> properties = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            properties.add(new SimpleDoubleProperty());
        }

        return properties;
    }

    private Class<?>[] paramTypes(Object validationFunction, int dependencies) {
        var params = new ArrayList<Class<?>>();
        params.add(validationFunction.getClass().getInterfaces()[0]);
        for (int i = 0; i < dependencies; ++i) {
            params.add(ObservableValue.class);
        }
        params.add(Executor.class);
        return params.toArray(Class[]::new);
    }

    private Object[] arguments(Object validator, List<DoubleProperty> dependencies, Executor executor) {
        var args = new ArrayList<>();
        args.add(validator);
        args.addAll(dependencies);
        args.add(executor);
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
    private void testApplyAsyncImpl(String constraintName, Object[] functions, int[] calls) throws ReflectiveOperationException {
        for (int i = 0; i < MAX_DEPS; ++i) {
            calls[0] = 0;

            var dependencies = doublePropertiesList(i);
            var method = Constraints.class.getMethod(constraintName, paramTypes(functions[i], i));
            var constraint = (Constraint<? super Number, Object>)method.invoke(null, arguments(functions[i], dependencies, Runnable::run));
            var value = new SimpleDoubleProperty();
            var constrainedValue = new DoublePropertyTestImpl(value.get());
            var validationHelper = new ValidationHelper<Number, Object>(value, constrainedValue, new Constraint[] {constraint});

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
    public void testApplyAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testApplyAsyncImpl("applyAsync", validationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls);
    }

    @Test
    public void testApplyInterruptibleAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testApplyAsyncImpl("applyInterruptibleAsync", validationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls);
    }

    @Test
    public void testApplyCancellableAsyncWithDirectExecutor() throws ReflectiveOperationException {
        int[] calls = new int[1];
        testApplyAsyncImpl("applyCancellableAsync", cancellableValidationFunctions(() -> {
            calls[0]++;
            return ValidationResult.valid();
        }), calls);
    }

    @Test
    public void testNotNull() throws ExecutionException, InterruptedException {
        Validator<String, Object> validator = Constraints.<String, Object>notNull().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertTrue(validator.validate("").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrBlank() throws ExecutionException, InterruptedException {
        Validator<String, Object> validator = Constraints.notNullOrBlank().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertFalse(validator.validate("    ").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrEmpty() throws ExecutionException, InterruptedException {
        Validator<String, Object> validator = Constraints.notNullOrEmpty().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertTrue(validator.validate("    ").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testMatchesPattern() throws ExecutionException, InterruptedException {
        Validator<String, Object> validator = Constraints.matchesPattern("[abc]+").getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertFalse(validator.validate("def").get().isValid());
        assertTrue(validator.validate("abc").get().isValid());
        assertTrue(validator.validate("aaa").get().isValid());
    }

    @Test
    public void testMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        Validator<String, Object> validator = Constraints.matchesPattern(pattern).getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertFalse(validator.validate("def").get().isValid());
        assertTrue(validator.validate("abc").get().isValid());
        assertTrue(validator.validate("aaa").get().isValid());

        pattern.set("[def]+");
        assertFalse(validator.validate("abc").get().isValid());
        assertFalse(validator.validate("aaa").get().isValid());
        assertTrue(validator.validate("ddd").get().isValid());
        assertTrue(validator.validate("defdefdef").get().isValid());
    }

    @Test
    public void testNotMatchesPattern() throws ExecutionException, InterruptedException {
        Validator<String, Object> validator = Constraints.notMatchesPattern("[abc]+").getValidator();
        assertTrue(validator.validate(null).get().isValid());
        assertTrue(validator.validate("").get().isValid());
        assertTrue(validator.validate("def").get().isValid());
        assertFalse(validator.validate("abc").get().isValid());
        assertFalse(validator.validate("aaa").get().isValid());
    }

    @Test
    public void testNotMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        Validator<String, Object> validator = Constraints.notMatchesPattern(pattern).getValidator();
        assertTrue(validator.validate(null).get().isValid());
        assertTrue(validator.validate("").get().isValid());
        assertTrue(validator.validate("def").get().isValid());
        assertFalse(validator.validate("abc").get().isValid());
        assertFalse(validator.validate("aaa").get().isValid());

        pattern.set("[def]+");
        assertTrue(validator.validate("abc").get().isValid());
        assertTrue(validator.validate("aaa").get().isValid());
        assertFalse(validator.validate("ddd").get().isValid());
        assertFalse(validator.validate("defdefdef").get().isValid());
    }

    @Test
    public void testBetweenInt() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.between(5, 10).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenLong() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.between(5L, 10L).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenFloat() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.between(5F, 10F).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenDouble() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.between(5D, 10D).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        var max = new SimpleIntegerProperty(10);
        Validator<Number, Object> validator = Constraints.between(min, max).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
        min.set(8);
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
        max.set(9);
        assertFalse(validator.validate(9).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        var max = new SimpleLongProperty(10);
        Validator<Number, Object> validator = Constraints.between(min, max).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
        min.set(8);
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
        max.set(9);
        assertFalse(validator.validate(9).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        var max = new SimpleFloatProperty(10);
        Validator<Number, Object> validator = Constraints.between(min, max).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
        min.set(8);
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
        max.set(9);
        assertFalse(validator.validate(9).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
    }

    @Test
    public void testBetweenObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        var max = new SimpleDoubleProperty(10);
        Validator<Number, Object> validator = Constraints.between(min, max).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
        min.set(8);
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
        max.set(9);
        assertFalse(validator.validate(9).get().isValid());
        assertTrue(validator.validate(8).get().isValid());
    }

    @Test
    public void testGreaterThanInt() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThan(5).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanLong() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThan(5L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanFloat() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThan(5F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanDouble() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThan(5D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        Validator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        Validator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        Validator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        Validator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToInt() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToLong() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        Validator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanInt() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThan(5).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanLong() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThan(5L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanFloat() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThan(5F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanDouble() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThan(5D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        Validator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        Validator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        Validator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        Validator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToInt() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(6).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToLong() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(6L).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(6F).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(6D).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        Validator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

}
