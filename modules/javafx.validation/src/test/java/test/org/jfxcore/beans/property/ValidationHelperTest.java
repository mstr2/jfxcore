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

import com.sun.javafx.binding.Logging;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.validation.Constraint;
import javafx.beans.property.validation.ValidationResult;
import javafx.beans.property.validation.Validator;
import org.jfxcore.beans.property.validation.StringPropertyImpl;
import org.jfxcore.beans.property.validation.ValidationHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unchecked", "FieldCanBeLocal", "unused", "Convert2Lambda"})
public class ValidationHelperTest {

    private static class StringPropertyTestImpl extends StringPropertyImpl {
        public StringPropertyTestImpl(String initialValue) { super(initialValue); }
        @Override public Object getBean() { return null; }
        @Override public String getName() { return null; }
    }

    private StringProperty value;
    private StringPropertyImpl constrainedValue;

    private void assertValidationState(
            ValidationHelper<?, ?> helper,
            boolean validating,
            boolean valid,
            boolean userValid,
            boolean invalid,
            boolean userInvalid) {
        assertEquals(validating, helper.validatingProperty().get());
        assertEquals(valid, helper.validProperty().get());
        assertEquals(userValid, helper.userValidProperty().get());
        assertEquals(invalid, helper.invalidProperty().get());
        assertEquals(userInvalid, helper.userInvalidProperty().get());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    static void setupAll() {
        Logging.getLogger().disableLogging();
        Toolkit.getToolkit();
    }

    @BeforeEach
    public void setupEach() {
        value = new SimpleStringProperty();
        constrainedValue = new StringPropertyTestImpl(value.get());
    }

    @Test
    public void testConstrainedValueEqualsValueWhenNoConstraintsAreSpecified() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[0]);
        value.set("foo");
        assertEquals("foo", constrainedValue.get());
    }

    @Test
    public void testAlwaysValidValidator() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>((Validator<String, String>)value ->
                CompletableFuture.completedFuture(ValidationResult.valid()), null, null)
        });

        value.set("foo");
        assertEquals("foo", constrainedValue.get());
        assertValidationState(helper, false, true, false, false, false);
    }

    @Test
    public void testAlwaysInvalidValidator() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>((Validator<String, String>)value ->
                CompletableFuture.completedFuture(ValidationResult.invalid()), null, null)
        });

        value.set("foo");
        assertNull(constrainedValue.get());
        assertValidationState(helper, false, false, false, true, false);
    }

    @Test
    public void testValidatingIsTrueWhileValidatorIsRunning() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.supplyAsync(() -> {
                        sleep(100);
                        return ValidationResult.valid();
                    });
                }
            }, Platform::runLater, null)
        });

        assertValidationState(helper, true, false, false, false, false);
        sleep(200);
        assertValidationState(helper, false, true, false, false, false);

        value.set("foo");
        assertValidationState(helper, true, false, false, false, false);
        assertNull(constrainedValue.get());

        sleep(200);
        assertValidationState(helper, false, true, false, false, false);
        assertEquals("foo", constrainedValue.get());
    }

    @Test
    public void testExceptionInValidatorStopsValidation() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    throw new RuntimeException();
                }
            }, Platform::runLater, null)
        });

        assertValidationState(helper, false, false, false, false, false);
    }

    @Test
    public void testExceptionInValidatorFutureStopsValidation() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    return CompletableFuture.supplyAsync(() -> {
                        sleep(50);
                        throw new RuntimeException();
                    });
                }
            }, Platform::runLater, null)
        });

        assertValidationState(helper, true, false, false, false, false);
        sleep(100);
        assertValidationState(helper, false, false, false, false, false);
    }

    @Test
    public void testValidatorYieldsWarningOrError() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value != null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(true, value.isBlank() ? "<blank>" : null));
                    }
                    return CompletableFuture.completedFuture(new ValidationResult<>(false, "<null>"));
                }
            }, Platform::runLater, null)
        });

        assertEquals(1, helper.errorsProperty().size());
        assertEquals(0, helper.warningsProperty().size());
        assertEquals("<null>", helper.errorsProperty().get(0));
        assertValidationState(helper, false, false, false, true, false);

        value.set("    ");
        assertEquals(0, helper.errorsProperty().size());
        assertEquals(1, helper.warningsProperty().size());
        assertEquals("<blank>", helper.warningsProperty().get(0));
        assertValidationState(helper, false, true, false, false, false);

        value.set("foo");
        assertEquals(0, helper.errorsProperty().size());
        assertEquals(0, helper.warningsProperty().size());
        assertValidationState(helper, false, true, false, false, false);
    }

    @Test
    public void testMultipleWarningsAndErrors() {
        var helper = new ValidationHelper<String, String>(value, constrainedValue, new Constraint[] {
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value == null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(false, "<blank-null>"));
                    }
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(true, value.isBlank() ? "<blank>" : null));
                }
            }, Platform::runLater, null),
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    if (value == null) {
                        return CompletableFuture.completedFuture(
                            new ValidationResult<>(false, "<empty-null>"));
                    }
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(true, value.isEmpty() ? "<empty>" : null));
                }
            }, Platform::runLater, null),
            new Constraint<>(new Validator<String, String>() {
                @Override
                public CompletableFuture<ValidationResult<String>> validate(String value) {
                    boolean valid = value != null && value.length() > 4;
                    return CompletableFuture.completedFuture(
                        new ValidationResult<>(valid, !valid ? "<short>" : null));
                }
            }, Platform::runLater, null)
        });

        assertEquals(3, helper.errorsProperty().size());
        assertEquals(0, helper.warningsProperty().size());
        assertEquals("<blank-null>", helper.errorsProperty().get(0));
        assertEquals("<empty-null>", helper.errorsProperty().get(1));
        assertEquals("<short>", helper.errorsProperty().get(2));
        assertValidationState(helper, false, false, false, true, false);

        value.set("    ");
        assertEquals(1, helper.errorsProperty().size());
        assertEquals(1, helper.warningsProperty().size());
        assertEquals("<blank>", helper.warningsProperty().get(0));
        assertEquals("<short>", helper.errorsProperty().get(0));
        assertValidationState(helper, false, false, false, true, false);

        value.set("");
        assertEquals(1, helper.errorsProperty().size());
        assertEquals(2, helper.warningsProperty().size());
        assertEquals("<blank>", helper.warningsProperty().get(0));
        assertEquals("<empty>", helper.warningsProperty().get(1));
        assertEquals("<short>", helper.errorsProperty().get(0));
        assertValidationState(helper, false, false, false, true, false);

        value.set("foo");
        assertEquals(1, helper.errorsProperty().size());
        assertEquals(0, helper.warningsProperty().size());
        assertEquals("<short>", helper.errorsProperty().get(0));
        assertValidationState(helper, false, false, false, true, false);

        value.set("foobar");
        assertEquals(0, helper.errorsProperty().size());
        assertEquals(0, helper.warningsProperty().size());
        assertValidationState(helper, false, true, false, false, false);
    }

}
