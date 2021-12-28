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

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.validation.AsyncValidator;
import javafx.beans.property.validation.Constraints;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintsTest {

    @Test
    public void testNotNull() throws ExecutionException, InterruptedException {
        AsyncValidator<String, Object> validator = Constraints.<String, Object>notNull().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertTrue(validator.validate("").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrBlank() throws ExecutionException, InterruptedException {
        AsyncValidator<String, Object> validator = Constraints.notNullOrBlank().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertFalse(validator.validate("    ").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testNotNullOrEmpty() throws ExecutionException, InterruptedException {
        AsyncValidator<String, Object> validator = Constraints.notNullOrEmpty().getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertTrue(validator.validate("    ").get().isValid());
        assertTrue(validator.validate("test").get().isValid());
    }

    @Test
    public void testMatchesPattern() throws ExecutionException, InterruptedException {
        AsyncValidator<String, Object> validator = Constraints.matchesPattern("[abc]+").getValidator();
        assertFalse(validator.validate(null).get().isValid());
        assertFalse(validator.validate("").get().isValid());
        assertFalse(validator.validate("def").get().isValid());
        assertTrue(validator.validate("abc").get().isValid());
        assertTrue(validator.validate("aaa").get().isValid());
    }

    @Test
    public void testMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        AsyncValidator<String, Object> validator = Constraints.matchesPattern(pattern).getValidator();
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
        AsyncValidator<String, Object> validator = Constraints.notMatchesPattern("[abc]+").getValidator();
        assertTrue(validator.validate(null).get().isValid());
        assertTrue(validator.validate("").get().isValid());
        assertTrue(validator.validate("def").get().isValid());
        assertFalse(validator.validate("abc").get().isValid());
        assertFalse(validator.validate("aaa").get().isValid());
    }

    @Test
    public void testNotMatchesObservablePattern() throws ExecutionException, InterruptedException {
        var pattern = new SimpleStringProperty("[abc]+");
        AsyncValidator<String, Object> validator = Constraints.notMatchesPattern(pattern).getValidator();
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
        AsyncValidator<Number, Object> validator = Constraints.between(5, 10).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenLong() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.between(5L, 10L).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenFloat() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.between(5F, 10F).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenDouble() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.between(5D, 10D).getValidator();
        assertFalse(validator.validate(4).get().isValid());
        assertTrue(validator.validate(5).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
        assertFalse(validator.validate(10).get().isValid());
    }

    @Test
    public void testBetweenObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        var max = new SimpleIntegerProperty(10);
        AsyncValidator<Number, Object> validator = Constraints.between(min, max).getValidator();
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
        AsyncValidator<Number, Object> validator = Constraints.between(min, max).getValidator();
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
        AsyncValidator<Number, Object> validator = Constraints.between(min, max).getValidator();
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
        AsyncValidator<Number, Object> validator = Constraints.between(min, max).getValidator();
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
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(5).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanLong() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(5L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanFloat() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(5F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanDouble() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(5D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.greaterThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToInt() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToLong() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(6D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testGreaterThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.greaterThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanInt() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThan(5).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanLong() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThan(5L).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanFloat() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThan(5F).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanDouble() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThan(5D).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
    }

    @Test
    public void testLessThanObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(5);
        AsyncValidator<Number, Object> validator = Constraints.lessThan(min).getValidator();
        assertFalse(validator.validate(5).get().isValid());
        assertTrue(validator.validate(4).get().isValid());
        min.set(10);
        assertFalse(validator.validate(10).get().isValid());
        assertTrue(validator.validate(9).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToInt() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(6).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToLong() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(6L).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToFloat() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(6F).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToDouble() throws ExecutionException, InterruptedException {
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(6D).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableInt() throws ExecutionException, InterruptedException {
        var min = new SimpleIntegerProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableLong() throws ExecutionException, InterruptedException {
        var min = new SimpleLongProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableFloat() throws ExecutionException, InterruptedException {
        var min = new SimpleFloatProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

    @Test
    public void testLessThanOrEqualToObservableDouble() throws ExecutionException, InterruptedException {
        var min = new SimpleDoubleProperty(6);
        AsyncValidator<Number, Object> validator = Constraints.lessThanOrEqualTo(min).getValidator();
        assertFalse(validator.validate(7).get().isValid());
        assertTrue(validator.validate(6).get().isValid());
        min.set(11);
        assertFalse(validator.validate(12).get().isValid());
        assertTrue(validator.validate(11).get().isValid());
    }

}
