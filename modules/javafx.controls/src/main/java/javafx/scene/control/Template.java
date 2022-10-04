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

package javafx.scene.control;

import com.sun.javafx.scene.control.template.TemplateHelper;
import com.sun.javafx.scene.control.template.TemplateListener;
import com.sun.javafx.scene.control.template.TemplateManager;
import com.sun.javafx.scene.control.template.TemplateObserver;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.cell.TemplatedCellFactory;
import javafx.util.Incubating;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * A {@code Template} describes the visual representation of a data object.
 *
 * @see TemplatedCellFactory
 * @param <T> the type of objects that can be visualized by this template
 * @since JFXcore 19
 */
@Incubating
@DefaultProperty("content")
public class Template<T> {

    static {
        TemplateHelper.setAccessor(new TemplateHelper.Accessor() {
            @Override
            public void addListener(Template<?> template, TemplateListener listener) {
                template.listeners.add(listener);
            }

            @Override
            public void removeListener(Template<?> template, TemplateListener listener) {
                template.listeners.remove(listener);
            }
        });
    }

    private static final class TemplateManagerImpl extends TemplateManager {
        final Runnable runnable;

        public TemplateManagerImpl(Node control, Runnable runnable) {
            super(control);
            this.runnable = Objects.requireNonNull(runnable, "runnable cannot be null");
        }

        @Override
        protected void onApplyTemplate() {
            runnable.run();
        }
    }

    /**
     * Sets the {@code Runnable} that is invoked when templates need to be re-applied on the specified node.
     *
     * @param node the templated {@code Node}
     * @param runnable the {@code Runnable} that is invoked when templates need to be re-applied
     */
    public static void setOnApply(Node node, Runnable runnable) {
        if (node.getProperties().get(TemplateManagerImpl.class) instanceof TemplateManagerImpl templateManager) {
            templateManager.dispose();
            node.getProperties().remove(TemplateManagerImpl.class);
        }

        if (runnable != null) {
            node.getProperties().put(TemplateManagerImpl.class, new TemplateManagerImpl(node, runnable));
        }
    }

    /**
     * Gets the {@code Runnable} that is invoked when templates need to be re-applied on the specified node.
     *
     * @param control the templated {@code Node}
     * @return the {@code Runnable} that is invoked when templates need to be re-applied
     */
    public static Runnable getOnApply(Node control) {
        if (control.getProperties().get(TemplateManagerImpl.class) instanceof TemplateManagerImpl templateManager) {
            return templateManager.runnable;
        }

        return null;
    }

    /**
     * Tries to find a template in the scene graph above the specified node that matches the data object.
     * <p>
     * This method will inspect the {@link Node#getProperties()} map of the specified node and potentially
     * all of its parents to find a template that matches the data object.
     *
     * @param node the {@code Node} that will be inspected
     */
    @SuppressWarnings("unchecked")
    public static <T> Template<? super T> find(Node node, T data) {
        return (Template<? super T>)TemplateObserver.findTemplate(node, data);
    }

    private final Class<T> dataType;
    private final List<TemplateListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@code Template} instance.
     *
     * @param dataType the type of objects that can be visualized by this template
     */
    public Template(@NamedArg("dataType") Class<T> dataType) {
        if (dataType == null) {
            throw new NullPointerException("dataType cannot be null");
        }

        this.dataType = dataType;
    }

    // package-private for testing
    List<TemplateListener> getListeners() {
        return listeners;
    }

    /**
     * Returns the data type for which this template can create a visual representation.
     * <p>
     * A data object is only applicable for this template if its runtime type is a
     * subtype of the data type.
     *
     * @return the data type
     */
    public final Class<T> getDataType() {
        return dataType;
    }

    /**
     * Represents the content of the template as a function that creates the visual
     * representation for a given data object.
     */
    private final ObjectProperty<TemplateContent<T>> content = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return Template.this;
        }

        @Override
        public String getName() {
            return "content";
        }

        @Override
        protected void invalidated() {
            get(); // validate the property
            notifyTemplateChanged(this);
        }
    };

    public final ObjectProperty<TemplateContent<T>> contentProperty() {
        return content;
    }

    public final TemplateContent<T> getContent() {
        return content.get();
    }

    public final void setContent(TemplateContent<T> content) {
        this.content.set(content);
    }

    /**
     * The selector predicate is used to determine whether this template should be selected
     * to create the visual representation for a given data object.
     * The selector is only considered when a data object could potentially be represented by
     * this template, which requires the data object type be a subtype of this template's
     * {@link #getDataType() data type}.
     */
    private final ObjectProperty<Predicate<? super T>> selector = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return Template.this;
        }

        @Override
        public String getName() {
            return "selector";
        }

        @Override
        protected void invalidated() {
            get(); // validate the property
            notifyTemplateChanged(this);
        }
    };

    public final ObjectProperty<Predicate<? super T>> selectorProperty() {
        return selector;
    }

    public final Predicate<? super T> getSelector() {
        return selector.get();
    }

    public final void setSelector(Predicate<? super T> selector) {
        this.selector.set(selector);
    }

    /**
     * Signals that the template has changed in a way that may require it to be re-applied.
     */
    protected final void notifyTemplateChanged(ReadOnlyProperty<?> observable) {
        for (TemplateListener listener : listeners) {
            listener.onTemplateChanged(this, observable);
        }
    }

}
