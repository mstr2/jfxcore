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
import org.junit.jupiter.api.Test;
import javafx.scene.Group;
import javafx.scene.control.Template;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateManagerTest {

    @Test
    public void testTemplatesAreAppliedWhenBranchIsAddedToNode() {
        var leaf = new Group();
        var branch = new Group(new Group(leaf));
        var root = new Group();
        var flag = new int[1];
        var manager = new TemplateManager(leaf) {
            @Override
            protected void onApplyTemplate() {
                flag[0]++;
            }
        };

        root.getProperties().put("test_root", new Template<>(String.class) {{ setAmbient(true); }});
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
        var manager = new TemplateManager(a) {
            @Override
            protected void onApplyTemplate() {
                flag[0]++;
            }
        };

        c.getProperties().put("test_c", new Template<>(String.class) {{ setAmbient(true); }});
        assertEquals(0, flag[0]);

        b.getProperties().put("test_c", new Template<>(String.class) {{ setAmbient(true); }});
        assertEquals(0, flag[0]);

        a.getProperties().put("test_a", new Template<>(String.class) {{ setAmbient(true); }});
        assertEquals(1, flag[0]);

        root.getProperties().put("test_root", new Template<>(String.class) {{ setAmbient(true); }});
        assertEquals(2, flag[0]);
    }

}
