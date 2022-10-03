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

import com.sun.javafx.scene.control.template.TemplateContainerHelper;
import org.junit.jupiter.api.Test;
import javafx.scene.control.Template;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateContainerHelperTest {

    @Test
    public void testFindMostDerivedTemplate() {
        var t1 = new Template<>(Comparable.class);
        var t2 = new Template<>(String.class);
        var t3 = new Template<>(Object.class);
        var container = List.of(t1, t2, t3);
        var helper = new TemplateContainerHelper();

        assertSame(t2, helper.selectTemplate(container, "foo"));
    }

    @Test
    public void testFindMostDerivedTemplateInLongInheritanceChain() {
        class C1 {}
        class C2 extends C1 {}
        class C3 extends C2 {}
        class C4 extends C3 {}
        class C5 extends C4 {}
        class C6 extends C5 {}
        class C7 extends C6 {}

        var c1 = new Template<>(C1.class);
        var c2 = new Template<>(C2.class);
        var c3 = new Template<>(C3.class);
        var c4 = new Template<>(C4.class);
        var c5 = new Template<>(C5.class);
        var c6 = new Template<>(C6.class);
        var c7 = new Template<>(C7.class);

        var container = List.<Template<?>>of(c2, c6, c1, c7, c4, c3, c5);
        var helper = new TemplateContainerHelper();

        assertSame(c1, helper.selectTemplate(container, new C1()));
        assertSame(c2, helper.selectTemplate(container, new C2()));
        assertSame(c3, helper.selectTemplate(container, new C3()));
        assertSame(c4, helper.selectTemplate(container, new C4()));
        assertSame(c5, helper.selectTemplate(container, new C5()));
        assertSame(c6, helper.selectTemplate(container, new C6()));
        assertSame(c7, helper.selectTemplate(container, new C7()));
    }

    @Test
    public void testFindMostDerivedTemplateWithSelectors() {
        class C1 {}
        class C2 extends C1 {}
        class C3 extends C2 {}
        class C4 extends C3 {}
        class C5 extends C4 {}
        class C6 extends C5 {}

        var c1 = new Template<>(C1.class);
        var c2 = new Template<>(C2.class);
        var c3 = new Template<>(C3.class);
        var c4 = new Template<>(C4.class);
        var c5 = new Template<>(C5.class);
        var c6 = new Template<>(C6.class);

        Predicate<Object> selector = x -> !x.toString().equals("not");
        var container = List.<Template<?>>of(c2, c6, c1, c4, c3, c5);
        var helper = new TemplateContainerHelper();

        c2.setSelector(selector);
        assertSame(c1, helper.selectTemplate(container, new C2() { @Override public String toString() { return "not"; } }));
        c2.setSelector(null);

        c3.setSelector(selector);
        assertSame(c2, helper.selectTemplate(container, new C3() { @Override public String toString() { return "not"; } }));
        c3.setSelector(null);

        c4.setSelector(selector);
        assertSame(c3, helper.selectTemplate(container, new C4() { @Override public String toString() { return "not"; } }));
        c4.setSelector(null);

        c5.setSelector(selector);
        assertSame(c4, helper.selectTemplate(container, new C5() { @Override public String toString() { return "not"; } }));
        c5.setSelector(null);

        c6.setSelector(selector);
        assertSame(c5, helper.selectTemplate(container, new C6() { @Override public String toString() { return "not"; } }));
        c6.setSelector(null);
    }

}
