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

package javafx.scene.control.template;

import javafx.beans.DefaultProperty;
import javafx.scene.Node;
import java.util.Map;
import java.util.function.Predicate;

@DefaultProperty("content")
@TemplateContentProperty("content")
public class Template<T> {

    private TemplateContent<T> content;
    private TemplateContent<T> editingContent;
    private Predicate<Object> selector;

    public TemplateContent<T> getContent() {
        return content;
    }

    public void setContent(TemplateContent<T> content) {
        this.content = content;
    }

    public TemplateContent<T> getEditingContent() {
        return editingContent;
    }

    public void setEditingContent(TemplateContent<T> editingContent) {
        this.editingContent = editingContent;
    }

    public Predicate<Object> getSelector() {
        return selector;
    }

    public void setSelector(Predicate<Object> selector) {
        this.selector = selector;
    }

    /**
     * Tries to find a template in the scene graph that matches the specified item.
     */
    @SuppressWarnings("unchecked")
    public static <T> Template<T> find(Node node, Object item) {
        do {
            if (node.hasProperties()) {
                for (Map.Entry<Object, Object> entry : node.getProperties().entrySet()) {
                    if (entry.getValue() instanceof Template) {
                        Predicate<Object> selector = ((Template<?>) entry.getValue()).getSelector();
                        if (selector != null) {
                            if (selector.test(item)) {
                                return (Template<T>) entry.getValue();
                            }
                        } else if (entry.getKey() instanceof Class && ((Class<?>) entry.getKey()).isInstance(item)) {
                            return (Template<T>) entry.getValue();
                        }
                    }
                }
            }

            node = node.getParent();
        } while (node != null);

        return null;
    }

    /**
     * Determines whether the template matches the specified item.
     */
    public static <T> boolean match(Template<T> template, Object item) {
        Predicate<Object> selector = template.getSelector();
        return selector == null || selector.test(item);
    }

    public static <T> TemplateContent<T> getContent(Template<T> template, boolean editing) {
        if (editing) {
            TemplateContent<T> content = template.getEditingContent();
            if (content != null) {
                return content;
            }
        }

        return template.getContent();
    }

}
