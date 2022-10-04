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

package test.javafx.scene.control;

import com.sun.javafx.scene.control.template.TemplateHelper;
import com.sun.javafx.scene.control.template.TemplateManager;
import org.junit.jupiter.api.Test;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Template;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateTest {

    private static void assertTemplateManager(Node node) {
        for (var entry : node.getProperties().entrySet()) {
            if (entry.getValue() instanceof TemplateManager) {
                return;
            }
        }

        fail("Node should have a TemplateManager");
    }

    private static void assertNoTemplateManager(Node node) {
        for (var entry : node.getProperties().entrySet()) {
            if (entry.getValue() instanceof TemplateManager) {
                fail("Node should not have a TemplateManager");
            }
        }
    }

    @Test
    public void testContentDefaultValueIsNull() {
        assertNull(new Template<>(String.class).getContent());
    }

    @Test
    public void testSelectorDefaultValueIsNull() {
        assertNull(new Template<>(String.class).getSelector());
    }

    @Test
    public void testOnApplyRunnableIsInvokedWhenTemplateIsAdded() {
        Group root, a, b;
        root = new Group(
            a = new Group(
                b = new Group()));

        int[] count = new int[1];
        Template.setOnApply(b, () -> count[0]++);
        assertTemplateManager(b);

        var scene = new Scene(root);
        root.applyCss();
        root.layout();
        assertEquals(0, count[0]);

        root.getProperties().put("test", new Template<>(String.class));
        assertEquals(1, count[0]);
    }

    @Test
    public void testOnApplyRunnableIsNotInvokedAfterRemoved() {
        Group root, a, b;
        root = new Group(
            a = new Group(
                b = new Group()));

        int[] count = new int[1];
        Template.setOnApply(b, () -> count[0]++);
        assertTemplateManager(b);

        var scene = new Scene(root);
        root.applyCss();
        root.layout();
        assertEquals(0, count[0]);

        Template.setOnApply(b, null);
        assertNoTemplateManager(b);

        root.getProperties().put("test", new Template<>(String.class));
        assertEquals(0, count[0]);
    }

    @Test
    public void testTemplateListenerIsInvoked() {
        class TemplateImpl extends Template<String> {
            public TemplateImpl() {
                super(String.class);
            }

            void notifyTemplateChanged() {
                notifyTemplateChanged(null);
            }
        }

        var template = new TemplateImpl();
        int[] count = new int[1];
        TemplateHelper.addListener(template, (t, observable) -> count[0]++);
        assertEquals(0, count[0]);

        template.notifyTemplateChanged();
        assertEquals(1, count[0]);
    }

}
