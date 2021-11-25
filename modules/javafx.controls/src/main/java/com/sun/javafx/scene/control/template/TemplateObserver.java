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

package com.sun.javafx.scene.control.template;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.template.Template;
import java.util.ArrayList;
import java.util.List;

public final class TemplateObserver implements MapChangeListener<Object, Object>, ChangeListener<Parent> {

    private static final String KEY = TemplateObserver.class.getName();

    private final Node host;
    private final List<TemplateObserver> children = new ArrayList<>(2);

    public static void install(Node host) {
        host.getProperties().put(KEY, new TemplateObserver(host));
    }

    public static TemplateObserver get(Node host) {
        return (TemplateObserver)host.getProperties().get(KEY);
    }

    private TemplateObserver(Node host) {
        this.host = host;
        host.parentProperty().addListener(this);
        host.getProperties().addListener(this);

        Parent parent = host.getParent();
        if (parent != null) {
            if (parent.hasProperties() && parent.getProperties().get(KEY) instanceof TemplateObserver observer) {
                observer.childAdded(this);
            } else {
                var observer = new TemplateObserver(parent);
                parent.getProperties().put(KEY, observer);
                observer.childAdded(this);
            }
        }
    }

    @Override
    public void changed(ObservableValue<? extends Parent> observable, Parent oldParent, Parent newParent) {
        if (oldParent != null) {
            TemplateObserver observer = (TemplateObserver)oldParent.getProperties().get(KEY);
            if (observer != null) {
                observer.childRemoved(this);
            }
        }

        if (newParent != null) {
            TemplateObserver observer = (TemplateObserver)newParent.getProperties().get(KEY);
            if (observer == null) {
                observer = new TemplateObserver(newParent);
                newParent.getProperties().put(KEY, observer);
            }

            observer.childAdded(this);
        }
    }

    @Override
    public void onChanged(Change<?, ?> change) {
        boolean templatesChanged =
            change.wasAdded() && change.getValueAdded() instanceof Template ||
            change.wasRemoved() && change.getValueRemoved() instanceof Template;

        if (templatesChanged) {
            notifyTemplatesChanged();
        }
    }

    private void childRemoved(TemplateObserver observer) {
        children.remove(observer);

        if (children.isEmpty()) {
            host.getProperties().remove(KEY);
            host.getProperties().removeListener(this);

            Parent parent = host.getParent();
            if (parent != null
                    && parent.hasProperties()
                    && parent.getProperties().get(KEY) instanceof TemplateObserver parentObserver) {
                parentObserver.childRemoved(this);
            }
        }
    }

    private void childAdded(TemplateObserver observer) {
        children.add(observer);
    }

    private void notifyTemplatesChanged() {
        if (host instanceof ListView<?> listView) {
            listView.refresh();
        } else if (host instanceof TableView<?> tableView) {
            tableView.refresh();
        } else if (host instanceof TreeView<?> treeView) {
            treeView.refresh();
        } else if (host instanceof TreeTableView<?> treeTableView) {
            treeTableView.refresh();
        }

        for (TemplateObserver child : children) {
            child.notifyTemplatesChanged();
        }
    }

}
