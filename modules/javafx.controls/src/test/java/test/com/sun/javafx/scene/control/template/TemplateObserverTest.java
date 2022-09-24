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
import com.sun.javafx.scene.control.template.TemplateObserverShim;
import org.junit.jupiter.api.Test;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Template;
import javafx.scene.control.TemplateShim;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateObserverTest {

    private static int getUseCount(Node node) {
        TemplateObserver observer = (TemplateObserver)node.getProperties().get(TemplateObserver.class);
        if (observer == null) {
            fail("TemplateObserver is null");
        }

        return TemplateObserverShim.getUseCount(observer);
    }

    private static TemplateObserver getTemplateObserver(Node node) {
        return (TemplateObserver)node.getProperties().get(TemplateObserver.class);
    }

    private static class TemplateManagerMock extends TemplateManager {
        TemplateManagerMock(Node node) { super(node, null); }
        @Override protected void onApplyTemplate() {}
    }

    private static class NGroup extends Group {
        NGroup(String name, Node... children) {
            super(children);
            setId(name);
        }
    }

    @Test
    public void testTemplateObserverIsInstalledOnAllParents() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group(
                    c = new Group())));

        new TemplateManagerMock(c);

        assertNotNull(getTemplateObserver(root));
        assertNotNull(getTemplateObserver(a));
        assertNotNull(getTemplateObserver(b));
        assertNotNull(getTemplateObserver(c));
    }

    @Test
    public void testUseCountReflectsChildrenAndTemplateManagers() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group(
                    c = new Group())));

        new TemplateManagerMock(b);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertEquals(1, getUseCount(b));
        assertNull(getTemplateObserver(c));

        new TemplateManagerMock(c);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertEquals(2, getUseCount(b));
        assertEquals(1, getUseCount(c));

        new TemplateManagerMock(a);
        assertEquals(1, getUseCount(root));
        assertEquals(2, getUseCount(a));
        assertEquals(2, getUseCount(b));
        assertEquals(1, getUseCount(c));
    }

    @Test
    public void testUseCountIsDecreasedWhenTemplateManagerIsDisposed() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group(
                    c = new Group())));

        new TemplateManagerMock(c);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertEquals(1, getUseCount(b));
        assertEquals(1, getUseCount(c));

        var b_manager = new TemplateManagerMock(b);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertEquals(2, getUseCount(b));
        assertEquals(1, getUseCount(c));

        b_manager.dispose();
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertEquals(1, getUseCount(b));
        assertEquals(1, getUseCount(c));
    }

    @Test
    public void testTemplateObserverIsRemovedWhenUseCountReachesZero() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group(
                    c = new Group())));

        var c_manager = new TemplateManagerMock(c);
        new TemplateManagerMock(a);
        assertNotNull(getTemplateObserver(root));
        assertNotNull(getTemplateObserver(a));
        assertNotNull(getTemplateObserver(b));
        assertNotNull(getTemplateObserver(c));

        c_manager.dispose();
        assertNotNull(getTemplateObserver(root));
        assertNotNull(getTemplateObserver(a));
        assertNull(getTemplateObserver(b));
        assertNull(getTemplateObserver(c));
    }

    @Test
    public void testUseCountIsIncreasedWhenChildWithTemplateManagerIsAdded() {
        Group root, a, b, c;
        root = new NGroup("root",
            a = new NGroup("a",
                b = new NGroup("b")));
        c = new NGroup("c");

        new TemplateManagerMock(a);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertNull(getTemplateObserver(b));

        // Adding the unrelated 'c' node as a child of 'b' should increase the use count of b's TemplateObserver.
        new TemplateManagerMock(c);
        b.getChildren().add(c);
        assertEquals(1, getUseCount(root));
        assertEquals(2, getUseCount(a));
        assertEquals(1, getUseCount(b));
        assertEquals(1, getUseCount(c));

        // When 'c' is removed, b's TemplateObserver is no longer used and will be disposed.
        b.getChildren().remove(c);
        assertEquals(1, getUseCount(root));
        assertEquals(1, getUseCount(a));
        assertNull(getTemplateObserver(b));
    }

    @Test
    public void testTemplateListenerIsAddedForExistingTemplates() {
        Group root, a, b;
        root = new Group(
            a = new Group(
                b = new Group()));

        // The template is added to 'root' before a TemplateObserver is installed.
        var t1 = new Template<>(String.class);
        root.getProperties().put("t1", t1);
        assertEquals(0, TemplateShim.getListeners(t1).size());

        // When a TemplateObserver is installed (by TemplateManager), it adds a listener to the existing template.
        var manager = new TemplateManagerMock(b);
        assertEquals(1, TemplateShim.getListeners(t1).size());

        // When the TemplateObserver is disposed, the listener is removed from the template.
        manager.dispose();
        assertEquals(0, TemplateShim.getListeners(t1).size());
    }

    @Test
    public void testReapplyEventIsNotFiredWhenNoAmbientTemplateIsPresent() {
        Group root, a, b, c;
        root = new Group(
            a = new Group(
                b = new Group()));
        c = new Group();

        // No template is present, so no event is fired.
        new TemplateManagerMock(c);
        boolean[] flag = new boolean[1];
        getTemplateObserver(c).addListener(() -> flag[0] = true);
        b.getChildren().add(c);
        assertFalse(flag[0]);

        // Add a non-ambient template at the root, which fires no event.
        root.getProperties().put("test", new Template<>(String.class));
        assertFalse(flag[0]);
    }

    @Test
    public void testReapplyEventIsFiredWhenAmbientTemplateIsPresent() {
        Group root, a, b, c;
        root = new Group(
                a = new Group(
                        b = new Group()));
        c = new Group();

        // Add an ambient template at the root, without which no event would be fired.
        root.getProperties().put("test", new Template<>(String.class) {{ setAmbient(true); }});

        new TemplateManagerMock(c);
        boolean[] flag = new boolean[1];
        getTemplateObserver(c).addListener(() -> flag[0] = true);
        b.getChildren().add(c);

        assertTrue(flag[0]);
    }

    @Test
    public void testReapplyEventIsFiredWhenAmbientFlagIsToggled() {
        Group root, a, b;
        root = new Group(
            a = new Group(
                b = new Group()));

        new TemplateManagerMock(b);
        int[] count = new int[1];
        getTemplateObserver(b).addListener(() -> count[0]++);

        // Add a non-ambient template at the root, which fires no event.
        Template<?> template = new Template<>(String.class);
        root.getProperties().put("test", template);
        assertEquals(0, count[0]);

        // Set the template to ambient, which fires an event.
        template.setAmbient(true);
        assertEquals(1, count[0]);

        // Setting it back to non-ambient fires the event again.
        template.setAmbient(false);
        assertEquals(2, count[0]);
    }

}
