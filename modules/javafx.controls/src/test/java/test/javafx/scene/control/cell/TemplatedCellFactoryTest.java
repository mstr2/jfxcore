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

package test.javafx.scene.control.cell;

import org.junit.jupiter.api.Test;
import test.util.memory.JMemoryBuddy;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Template;
import javafx.scene.control.cell.TemplatedCellFactory;
import java.util.List;

public class TemplatedCellFactoryTest {

    @Test
    public void testControlIsWeaklyReferencedByFactory() {
        JMemoryBuddy.memoryTest(test -> {
            var factory = new TemplatedCellFactoryImpl();
            var listView = new ListViewImpl(factory, List.of("foo"));
            var scene = new Scene(listView);
            factory.setCellTemplate(new Template<>(String.class));
            listView.applyCss();
            listView.layout();

            test.setAsReferenced(factory);
            test.assertCollectable(scene);
            test.assertCollectable(listView);
        });
    }

    @Test
    public void testMultipleControlsAreWeaklyReferencedByFactory() {
        JMemoryBuddy.memoryTest(test -> {
            var factory = new TemplatedCellFactoryImpl();
            var listView1 = new ListViewImpl(factory, List.of("foo")) {{ setId("listView1"); }};
            var listView2 = new ListViewImpl(factory, List.of("bar")) {{ setId("listView2"); }};
            var scene = new Scene(new Group(listView1, listView2));
            factory.setCellTemplate(new Template<>(String.class));
            listView1.applyCss();
            listView1.layout();
            listView2.applyCss();
            listView2.layout();

            test.setAsReferenced(factory);
            test.assertCollectable(scene);
            test.assertCollectable(listView1);
            test.assertCollectable(listView2);
        });
    }

    private static class ListViewImpl extends ListView<String> {
        ListViewImpl(TemplatedCellFactoryImpl factory, List<String> items) {
            super(FXCollections.observableList(items));
            setCellFactory(factory);
        }
    }

    private static class TemplatedCellFactoryImpl
            extends TemplatedCellFactory<String, ListView<String>, ListCell<String>> {
        TemplatedCellFactoryImpl() {}

        TemplatedCellFactoryImpl(Template<String> template) {
            super(template);
        }

        @Override
        protected ListCell<String> createCell(ListView<String> item) {
            return new ListCell<>() {
                final CellWrapper<String> cellWrapper = new CellWrapper<>(this) {
                    @Override
                    protected Node getControl() {
                        return getListView();
                    }

                    @Override
                    protected Template<? super String> getCellTemplate() {
                        return TemplatedCellFactoryImpl.this.getCellTemplate();
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
