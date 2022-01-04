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

package test.org.jfxcore.beans.property;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.validation.Constraint;
import javafx.beans.property.validation.Constraints;
import javafx.beans.property.validation.ValidationResult;
import org.jfxcore.beans.property.validation.SerializedConstraintValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SerializedConstraintValidatorTest {

    @BeforeAll
    static void setupAll() {
        Toolkit.getToolkit();
    }

    @Test
    public void testIntermediateValidationRequestsAreElided() throws Exception {
        AtomicInteger validatorCalls = new AtomicInteger();
        List<Number> validatedNumbers = new ArrayList<>();

        var constraint = Constraints.<Number, Object>applyAsync(value -> {
            try {
                validatorCalls.getAndIncrement();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ValidationResult.valid();
        }, ForkJoinPool.commonPool());

        var validator = new SerializedConstraintValidator<>(constraint) {
            @Override protected void onAsyncValidationStarted() {}
            @Override protected void onAsyncValidationEnded() {}
            @Override protected void onValidationCompleted(
                    Constraint<? super Number, Object> constraint,
                    Number value,
                    ValidationResult<Object> result,
                    Throwable exception) {
                if (exception == null) {
                    validatedNumbers.add(value);
                }
            }
        };

        validator.validate(1);
        validator.validate(2);
        validator.validate(3);
        validator.validate(4);
        validator.validate(5);

        Thread.sleep(200);

        assertEquals(2, validatorCalls.get());
        assertEquals(List.of(1, 5), validatedNumbers);
    }

}
