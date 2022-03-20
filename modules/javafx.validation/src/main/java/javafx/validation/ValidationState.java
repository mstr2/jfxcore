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

package javafx.validation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.InputNode;
import javafx.util.Incubating;

/**
 * Represents the validation state of a {@link ConstrainedValue} and facilitates the
 * visualization of validation states in the scene graph using CSS.
 * <p>
 * An easy way for scene graph nodes to visualize the result of data validation is by providing
 * CSS styles for the different data validation states. The data validation framework supports
 * five validation pseudo-classes:
 *
 * <ol>
 *     <li><b>:validating</b> - Selects an element that is currently validating.
 *     <li><b>:invalid</b> - Selects an element that failed data validation.
 *     <li><b>:valid</b> - Selects an element that successfully completed data validation.
 *     <li><b>:user-invalid</b> - Selects an element that failed data validation after the user has interacted with it.
 *     <li><b>:user-valid</b> - Selects an element that successfully completed data validation after the user has
 *                              interacted with it.
 * </ol>
 *
 * Validation pseudo-classes are enabled by connecting a scene graph {@link Node} to a {@code ConstrainedValue}
 * by setting the {@link ValidationState#sourceProperty(Node) source} attached property:
 *
 * <pre>{@code
 * var textField = new TextField();
 * var firstName = new SimpleConstrainedStringProperty<String>(
 *     Constraints.notNullOrBlank(() -> "Value cannot be empty"),
 *     Constraints.matchesPattern("[^\\d\\W]*", v -> "Invalid value"));
 *
 * textField.textProperty().bindBidirectional(firstName);
 *
 * // The 'firstName' property will provide validation states for the 'textField' node:
 * ValidationState.setSource(textField, firstName);
 * }</pre>
 *
 * @since JFXcore 18
 */
@Incubating
public enum ValidationState {

    /**
     * The value is currently validating, or the validation run was cancelled.
     */
    UNKNOWN,

    /**
     * The value is known to be valid.
     */
    VALID,

    /**
     * The value is known to be invalid.
     */
    INVALID;

    /**
     * Returns an attached property for the specified {@link Node} that indicates whether the value
     * is currently known to be valid after the user has significantly interacted with the node.
     * This information is only available when {@link #sourceProperty source} was set for the node.
     */
    public static ReadOnlyBooleanProperty userValidProperty(Node node) {
        ValidationInfo info = (ValidationInfo)node.getProperties().get(ValidationInfo.class);
        if (info == null) {
            node.getProperties().put(ValidationInfo.class, info = new ValidationInfo(node));
        }

        return info.userValidProperty();
    }

    /**
     * Gets the value of the {@link #userValidProperty(Node) userValid} attached property.
     */
    public static boolean isUserValid(Node node) {
        return userValidProperty(node).get();
    }

    /**
     * Returns an attached property for the specified {@link Node} that indicates whether the value
     * is currently known to be invalid after the user has significantly interacted with the node.
     * This information is only available when {@link #sourceProperty source} was set for the node.
     */
    public static ReadOnlyBooleanProperty userInvalidProperty(Node node) {
        ValidationInfo info = (ValidationInfo)node.getProperties().get(ValidationInfo.class);
        if (info == null) {
            node.getProperties().put(ValidationInfo.class, info = new ValidationInfo(node));
        }

        return info.userInvalidProperty();
    }

    /**
     * Gets the value of the {@link #userInvalidProperty(Node) userInvalid} attached property.
     */
    public static boolean isUserInvalid(Node node) {
        return userInvalidProperty(node).get();
    }

    /**
     * Returns an attached property for the specified {@link Node} that indicates the
     * {@link ConstrainedValue} that provides validation states for the specified node.
     */
    public static ObjectProperty<ConstrainedValue<?, ?>> sourceProperty(Node node) {
        ValidationInfo info = (ValidationInfo)node.getProperties().get(ValidationInfo.class);
        if (info == null) {
            node.getProperties().put(ValidationInfo.class, info = new ValidationInfo(node));
        }

        return info.source;
    }

    /**
     * Gets the value of the {@link #sourceProperty(Node) source} attached property.
     */
    public static ConstrainedValue<?, ?> getSource(Node node) {
        if (!node.hasProperties()) {
            return null;
        }

        return sourceProperty(node).get();
    }

    /**
     * Sets the value of the {@link #sourceProperty(Node) source} attached property.
     */
    public static void setSource(Node node, ConstrainedValue<?, ?> source) {
        sourceProperty(node).set(source);
    }

    @SuppressWarnings({"FieldCanBeLocal", "rawtypes", "unchecked"})
    private static final class ValidationInfo implements ValidationListener, InvalidationListener {
        private static final PseudoClass VALIDATING_PSEUDOCLASS = PseudoClass.getPseudoClass("validating");
        private static final PseudoClass INVALID_PSEUDOCLASS = PseudoClass.getPseudoClass("invalid");
        private static final PseudoClass VALID_PSEUDOCLASS = PseudoClass.getPseudoClass("valid");
        private static final PseudoClass USER_INVALID_PSEUDOCLASS = PseudoClass.getPseudoClass("user-invalid");
        private static final PseudoClass USER_VALID_PSEUDOCLASS = PseudoClass.getPseudoClass("user-valid");

        private final WeakValidationListener weakValidationListener = new WeakValidationListener(this);
        private final ObjectProperty<ConstrainedValue<?, ?>> source;
        private final ReadOnlyBooleanProperty userModified;
        private final Node node;

        private BooleanPropertyImpl userValid;
        private BooleanPropertyImpl userInvalid;

        ValidationInfo(Node node) {
            this.node = node;
            this.userModified = node instanceof InputNode inputNode ? inputNode.userModifiedProperty() : null;
            if (this.userModified != null) {
                this.userModified.addListener(this);
            }

            this.source = new ObjectPropertyBase<>() {
                ConstrainedValue<?, ?> oldValue;

                @Override
                public Object getBean() {
                    return ValidationState.class;
                }

                @Override
                public String getName() {
                    return "source";
                }

                @Override
                protected void invalidated() {
                    boolean userModified = isUserModified();
                    ConstrainedValue<?, ?> newValue = get();

                    if (oldValue != null) {
                        oldValue.removeListener(weakValidationListener);
                    }

                    if (newValue != null) {
                        newValue.addListener(weakValidationListener);
                    }

                    if (userValid != null) {
                        userValid.set(newValue != null && newValue.isValid() && userModified);
                    }

                    if (userInvalid != null) {
                        userInvalid.set(newValue != null && newValue.isInvalid() && userModified);
                    }

                    node.pseudoClassStateChanged(VALID_PSEUDOCLASS, newValue != null && newValue.isValid());
                    node.pseudoClassStateChanged(INVALID_PSEUDOCLASS, newValue != null && newValue.isInvalid());
                    node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, userModified && newValue != null && newValue.isValid());
                    node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, userModified && newValue != null && newValue.isInvalid());
                    node.pseudoClassStateChanged(VALIDATING_PSEUDOCLASS, newValue != null && newValue.isValidating());

                    oldValue = newValue;
                }
            };

            node.pseudoClassStateChanged(VALIDATING_PSEUDOCLASS, false);
            node.pseudoClassStateChanged(VALID_PSEUDOCLASS, false);
            node.pseudoClassStateChanged(INVALID_PSEUDOCLASS, false);
            node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, false);
            node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, false);
        }

        public ReadOnlyBooleanProperty userValidProperty() {
            if (userValid == null) {
                boolean valid = source.get() != null && source.get().isValid();
                userValid = new BooleanPropertyImpl(valid && isUserModified()) {
                    @Override public Object getBean() { return ValidationState.class; }
                    @Override public String getName() { return "userValid"; }
                };
            }

            return userValid;
        }

        public ReadOnlyBooleanProperty userInvalidProperty() {
            if (userInvalid == null) {
                boolean invalid = source.get() != null && source.get().isInvalid();
                userInvalid = new BooleanPropertyImpl(invalid && isUserModified()) {
                    @Override public Object getBean() { return ValidationState.class; }
                    @Override public String getName() { return "userInvalid"; }
                };
            }

            return userInvalid;
        }

        /**
         * Called when the 'userModified' property was changed.
         */
        @Override
        public void invalidated(Observable observable) {
            ConstrainedValue<?, ?> source = this.source.get();
            boolean userModified = isUserModified();
            boolean userValid = source != null && source.isValid() && userModified;
            boolean userInvalid = source != null && source.isInvalid() && userModified;

            if (this.userValid != null) {
                this.userValid.set(userValid);
            }

            if (this.userInvalid != null) {
                this.userInvalid.set(userInvalid);
            }

            node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, userValid);
            node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, userInvalid);
        }

        /**
         * Called when the validation state was changed.
         */
        @Override
        public void changed(ConstrainedValue value, ChangeType changeType, boolean oldValue, boolean newValue) {
            switch (changeType) {
                case VALID -> {
                    if (userValid != null) {
                        userValid.set(newValue && isUserModified());
                    }

                    node.pseudoClassStateChanged(VALID_PSEUDOCLASS, newValue);
                    node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, newValue && isUserModified());
                }

                case INVALID -> {
                    if (userInvalid != null) {
                        userInvalid.set(newValue && isUserModified());
                    }

                    node.pseudoClassStateChanged(INVALID_PSEUDOCLASS, newValue);
                    node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, newValue && isUserModified());
                }

                case VALIDATING -> node.pseudoClassStateChanged(VALIDATING_PSEUDOCLASS, newValue);
            }
        }

        private boolean isUserModified() {
            return userModified != null && userModified.get();
        }
    }

    private abstract static class BooleanPropertyImpl extends ReadOnlyBooleanPropertyBase {
        private boolean value;

        BooleanPropertyImpl(boolean initialValue) {
            this.value = initialValue;
        }

        @Override
        public boolean get() {
            return value;
        }

        void set(boolean value) {
            if (this.value != value) {
                this.value = value;
                fireValueChangedEvent();
            }
        }
    }

}
