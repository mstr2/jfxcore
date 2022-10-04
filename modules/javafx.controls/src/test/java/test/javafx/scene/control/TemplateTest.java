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
import org.junit.jupiter.api.Test;
import javafx.scene.control.Template;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateTest {

    @Test
    public void testContentDefaultValueIsNull() {
        assertNull(new Template<>(String.class).getContent());
    }

    @Test
    public void testSelectorDefaultValueIsNull() {
        assertNull(new Template<>(String.class).getSelector());
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
