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

package test.com.sun.javafx.scene.control.template;

import com.sun.javafx.scene.control.template.TemplateManager;
import com.sun.javafx.scene.control.template.TemplateObserver;
import org.junit.jupiter.api.Test;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Template;
import javafx.scene.control.TemplateShim;
import javafx.scene.control.cell.TemplatedCellFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateManagerTest {

    @Test
    public void testTemplatesAreAppliedWhenBranchIsAddedToNode() {
        var leaf = new Group();
        var branch = new Group(new Group(leaf));
        var root = new Group();
        var flag = new int[1];
        var manager = new TemplateManager(leaf, null, null, () -> flag[0]++);

        root.getProperties().put("test_root", new Template<>(String.class));
        assertEquals(0, flag[0]);
        root.getChildren().add(branch);
        assertEquals(1, flag[0]);
        root.getChildren().set(0, leaf);
        assertEquals(2, flag[0]);
    }

    @Test
    public void testTemplatesAreNotAppliedWhenEventSourceIsBelowTemplateManager() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group(
                    c = new Group())));

        var flag = new int[1];
        var manager = new TemplateManager(a, null, null, () -> flag[0]++);

        c.getProperties().put("test_c", new Template<>(String.class));
        assertEquals(0, flag[0]);

        b.getProperties().put("test_c", new Template<>(String.class));
        assertEquals(0, flag[0]);

        a.getProperties().put("test_a", new Template<>(String.class));
        assertEquals(1, flag[0]);

        root.getProperties().put("test_root", new Template<>(String.class));
        assertEquals(2, flag[0]);
    }

    @Test
    public void testTemplatesAreAppliedWhenTemplateIsAddedToNodeProperties() {
        int[] count = new int[1];
        var factory = new TemplatedCellFactoryImpl();
        var listView = new ListViewImpl(factory, List.of("foo", "bar", "baz"));
        var manager = new TemplateManager(listView, null, null, () -> count[0]++);

        var scene = new Scene(listView);
        listView.applyCss();
        listView.layout();
        assertEquals(0, count[0]);

        listView.getProperties().put("t1", new Template<>(String.class));
        assertEquals(1, count[0]);

        listView.getProperties().put("t2", new Template<>(String.class));
        assertEquals(2, count[0]);
    }

    @Test
    public void testTemplatesAreAppliedForMultipleControlsWhenTemplateIsAddedToNodeProperties() {
        List<String> trace = new ArrayList<>();
        var factory = new TemplatedCellFactoryImpl();
        var listView1 = new ListViewImpl(factory, List.of("foo"));
        var manager1 = new TemplateManager(listView1, null, null, () -> trace.add("listView1"));
        var listView2 = new ListViewImpl(factory, List.of("bar"));
        var manager2 = new TemplateManager(listView2, null, null, () -> trace.add("listView2"));

        var root = new Group(listView1, listView2);
        var scene = new Scene(root);
        listView1.applyCss();
        listView1.layout();
        listView2.applyCss();
        listView2.layout();

        root.getProperties().put("t", new Template<>(String.class));
        assertEquals(2, trace.size());
        assertTrue(trace.contains("listView1"));
        assertTrue(trace.contains("listView2"));
    }

    @Test
    public void testTemplatesAreAppliedWhenTemplatePropertyIsChanged() {
        int[] count = new int[1];
        var factory = new TemplatedCellFactoryImpl();
        var listView = new ListViewImpl(factory, List.of("foo", "bar", "baz"));
        var template = new Template<>(String.class);
        listView.getProperties().put("t1", template);
        var manager = new TemplateManager(listView, null, null, () -> count[0]++);

        var scene = new Scene(listView);
        listView.applyCss();
        listView.layout();
        assertEquals(0, count[0]);

        template.setSelector(x -> true);
        assertEquals(1, count[0]);

        template.setContent(x -> null);
        assertEquals(2, count[0]);
    }

    @Test
    public void testTemplatesAreAppliedWhenTemplateDependencyIsInvalidated() {
        int[] count = new int[1];
        var templateProp = new SimpleObjectProperty<Template<?>>();
        var manager = new TemplateManager(null, null, new ObservableValue[] { templateProp }, () -> count[0]++);

        var template = new Template<>(String.class);
        assertEquals(0, count[0]);

        templateProp.set(template);
        assertEquals(1, count[0]);

        template.setContent(x -> null);
        assertEquals(2, count[0]);
    }

    @Test
    public void testExplicitTemplateListenerIsRemovedWhenTemplateDependencyIsChanged() {
        var template = new Template<>(String.class);
        var templateProp = new SimpleObjectProperty<Template<?>>(template);
        var manager = new TemplateManager(null, null, new ObservableValue[] { templateProp }, null);

        assertEquals(1, TemplateShim.getListeners(template).size());
        templateProp.set(new Template<>(String.class));
        assertEquals(0, TemplateShim.getListeners(template).size());
    }

    @Test
    public void testExplicitTemplateListenerIsRemovedWhenTemplateManagerIsDisposed() {
        var template = new Template<>(String.class);
        var templateProp = new SimpleObjectProperty<Template<?>>(template);
        var manager = new TemplateManager(null, null, new ObservableValue[] { templateProp }, null);

        assertEquals(1, TemplateShim.getListeners(template).size());
        manager.dispose();
        assertEquals(0, TemplateShim.getListeners(template).size());
    }

    @Test
    public void testInactiveTemplateManagerDoesNotObserveTemplatesInSceneGraph() {
        var factory = new TemplatedCellFactoryImpl();
        var listView = new ListViewImpl(factory, List.of("foo", "bar", "baz"));
        var active = new SimpleBooleanProperty(false);
        var manager = new TemplateManager(listView, active, null, null);

        var scene = new Scene(listView);
        listView.applyCss();
        listView.layout();
        assertNull(listView.getProperties().get(TemplateObserver.class));

        active.set(true);
        assertNotNull(listView.getProperties().get(TemplateObserver.class));

        active.set(false);
        assertNull(listView.getProperties().get(TemplateObserver.class));
    }

    private static class ListViewImpl extends ListView<String> {
        ListViewImpl(TemplatedCellFactoryImpl factory, List<String> items) {
            super(FXCollections.observableList(items));
            setCellFactory(factory);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SkinBase<>(this) {};
        }
    }

    private static class TemplatedCellFactoryImpl
            extends TemplatedCellFactory<String, ListView<String>, ListCell<String>> {
        @Override
        protected ListCell<String> createCell(ListView<String> item) {
            return new ListCell<>() {
                final CellWrapper cellWrapper = new CellWrapper(this) {
                    @Override
                    protected Node getControl() {
                        return getListView();
                    }
                };

                @Override
                public void startEdit() {
                    super.startEdit();
                    cellWrapper.startEdit();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    cellWrapper.cancelEdit();
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    cellWrapper.commitEdit();
                }

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    cellWrapper.updateItem(item, empty);
                }
            };
        }
    }

}
