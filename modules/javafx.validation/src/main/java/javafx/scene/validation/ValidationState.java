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

package javafx.scene.validation;

import org.jfxcore.beans.property.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.validation.ReadOnlyConstrainedProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * Adds data validation pseudo-class support to a scene graph node.
 * <p>
 * An easy way for scene graph nodes to visualize the result of data validation is by providing
 * CSS styles for the different data validation states. The data validation framework supports
 * five validation states:
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
 * Data validation pseudo-class support can be enabled for a {@link Node} by specifying the data
 * validation source using the {@link ValidationState#setSource(Node, ReadOnlyConstrainedProperty) source} attached
 * property. The data validation source must be an implementation of {@link ReadOnlyConstrainedProperty}:
 *
 * <blockquote><pre>
 * var textField = new TextField();
 * var firstName = new SimpleConstrainedStringProperty&lt;String&gt;(
 *     Constraints.notNullOrBlank(() -> "Value cannot be empty"),
 *     Constraints.matchesPattern("[^\\d\\W]*", v -> "Invalid value"));
 *
 * textField.textProperty().bindBidirectional(firstName);
 * ValidationState.setSource(textField, firstName);
 * </pre></blockquote>
 *
 * In an FXML document, the {@code ::} operator must be used to select the data validation source:
 * <blockquote><pre>
 * &lt;TextField text="{fx:sync firstName}"
 *            ValidationState.source="{fx:once ::firstName}"/&gt;
 * </pre></blockquote>
 *
 * @since JFXcore 18
 */
public final class ValidationState {

    private static final PseudoClass VALIDATING_PSEUDOCLASS = PseudoClass.getPseudoClass("validating");
    private static final PseudoClass INVALID_PSEUDOCLASS = PseudoClass.getPseudoClass("invalid");
    private static final PseudoClass VALID_PSEUDOCLASS = PseudoClass.getPseudoClass("valid");
    private static final PseudoClass USER_INVALID_PSEUDOCLASS = PseudoClass.getPseudoClass("user-invalid");
    private static final PseudoClass USER_VALID_PSEUDOCLASS = PseudoClass.getPseudoClass("user-valid");

    private ValidationState() {}

    /**
     * Returns the current {@code source} attached property on the specified {@link Node}.
     */
    public static ReadOnlyConstrainedProperty<?, ?> getSource(Node node) {
        if (!node.hasProperties()) {
            return null;
        }

        ValidationInfo sourceInfo = (ValidationInfo)node.getProperties().get(ValidationInfo.class);
        return sourceInfo != null ? sourceInfo.source : null;
    }

    /**
     * Sets the {@code source} attached property on the specified {@link Node}.
     */
    public static void setSource(Node node, ReadOnlyConstrainedProperty<?, ?> source) {
        ValidationInfo sourceInfo = (ValidationInfo)node.getProperties().get(ValidationInfo.class);

        if (sourceInfo != null) {
            if (sourceInfo.source == source) {
                return;
            }

            sourceInfo.dispose(source == null);
        }

        if (source != null) {
            node.getProperties().put(ValidationState.class, new ValidationInfo(node, source));
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private static final class ValidationInfo implements InvalidationListener {
        private final Node node;
        private final ReadOnlyConstrainedProperty<?, ?> source;
        private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(this);

        ValidationInfo(Node node, ReadOnlyConstrainedProperty<?, ?> source) {
            this.node = node;
            this.source = source;

            source.validatingProperty().addListener(weakInvalidationListener);
            source.invalidProperty().addListener(weakInvalidationListener);
            source.validProperty().addListener(weakInvalidationListener);
            source.userInvalidProperty().addListener(weakInvalidationListener);
            source.userValidProperty().addListener(weakInvalidationListener);

            ValidationHelper.getValidationHelper(source).connect(node);

            invalidated(null);
        }

        @Override
        public void invalidated(Observable observable) {
            node.pseudoClassStateChanged(VALIDATING_PSEUDOCLASS, source.isValidating());
            node.pseudoClassStateChanged(INVALID_PSEUDOCLASS, source.isInvalid());
            node.pseudoClassStateChanged(VALID_PSEUDOCLASS, source.isValid());
            node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, source.isUserInvalid());
            node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, source.isUserValid());
        }

        public void dispose(boolean removePseudoClassesToo) {
            ValidationHelper.getValidationHelper(source).disconnect();

            source.validatingProperty().removeListener(weakInvalidationListener);
            source.invalidProperty().removeListener(weakInvalidationListener);
            source.validProperty().removeListener(weakInvalidationListener);
            source.userInvalidProperty().removeListener(weakInvalidationListener);
            source.userValidProperty().removeListener(weakInvalidationListener);

            if (removePseudoClassesToo) {
                node.pseudoClassStateChanged(VALIDATING_PSEUDOCLASS, false);
                node.pseudoClassStateChanged(INVALID_PSEUDOCLASS, false);
                node.pseudoClassStateChanged(VALID_PSEUDOCLASS, false);
                node.pseudoClassStateChanged(USER_INVALID_PSEUDOCLASS, false);
                node.pseudoClassStateChanged(USER_VALID_PSEUDOCLASS, false);
            }
        }
    }

}
