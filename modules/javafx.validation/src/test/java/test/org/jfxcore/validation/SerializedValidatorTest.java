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

import com.sun.javafx.tk.Toolkit;
import org.jfxcore.validation.SerializedValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javafx.validation.Constraint;
import javafx.validation.Constraints;
import javafx.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SerializedValidatorTest extends ConcurrentTestBase {

    @BeforeAll
    static void setupAll() {
        Toolkit.getToolkit();
    }

    @Test
    public void testIntermediateValidationRequestsAreElided() { retry(() -> {
        AtomicInteger validatorCalls = new AtomicInteger();
        AtomicInteger startedCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        List<Number> validatedNumbers = new ArrayList<>();

        Constraint<Number, Object> constraint = Constraints.validateAsync(value -> {
            validatorCalls.getAndIncrement();
            sleep(50);
            return ValidationResult.valid();
        }, getThreadPool());

        var validator = new SerializedValidator<Number, Object>(constraint) {
            @Override
            protected CompletableFuture<ValidationResult<Object>> newValidationRun(Number value) {
                return constraint.validate(value);
            }

            @Override
            public ValidationResult<Object> getValidationResult() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void onValidationStarted() {
                startedCount.getAndIncrement();
            }

            @Override
            public void onValidationCompleted(
                    Number value, ValidationResult<Object> result, boolean intermediateCompletion) {
                completedCount.getAndIncrement();
                if (result != null) {
                    validatedNumbers.add(value);
                }
            }
        };

        runNow(() -> {
            // The validator will be invoked for the first value, but will be cancelled
            // by subsequent validation requests.
            validator.validate(1);

            // Validation requests 2-4 will be elided (the validator will not be invoked).
            validator.validate(2);
            validator.validate(3);
            validator.validate(4);

            // The validator will be invoked again for the last value, and it will
            // complete validation successfully.
            validator.validate(5);
        });

        sleep(200);

        runNow(() -> {
            assertEquals(2, startedCount.get());
            assertEquals(2, completedCount.get());
            assertEquals(2, validatorCalls.get());
            assertEquals(List.of(5), validatedNumbers);
        });
    }); }

}
