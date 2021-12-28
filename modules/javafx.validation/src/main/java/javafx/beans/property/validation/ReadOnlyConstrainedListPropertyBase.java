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

package javafx.beans.property.validation;

import com.sun.javafx.binding.ListExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Provides a base implementation for a {@link ReadOnlyConstrainedListProperty}.
 *
 * @param <T> element type
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ReadOnlyConstrainedListPropertyBase<T, E> extends ReadOnlyConstrainedListProperty<T, E> {

    private ListExpressionHelper<T> helper;

    /**
     * Creates a default {@code ReadOnlyConstrainedListPropertyBase}.
     */
    protected ReadOnlyConstrainedListPropertyBase() {
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ListExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ListExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableList<T>> listener) {
        helper = ListExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableList<T>> listener) {
        helper = ListExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void addListener(ListChangeListener<? super T> listener) {
        helper = ListExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super T> listener) {
        helper = ListExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Invokes {@link InvalidationListener InvalidationListeners}, {@link ChangeListener ChangeListeners},
     * and {@link ListChangeListener ListChangeListeners} when the property value has changed.
     */
    protected void fireValueChangedEvent() {
        ListExpressionHelper.fireValueChangedEvent(helper);
    }

    /**
     * Invokes {@link InvalidationListener InvalidationListeners}, {@link ChangeListener ChangeListeners},
     * and {@link ListChangeListener ListChangeListeners} when the list content has changed.
     */
    protected void fireValueChangedEvent(ListChangeListener.Change<? extends T> change) {
        ListExpressionHelper.fireValueChangedEvent(helper, change);
    }

}
