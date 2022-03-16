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

package test.org.jfxcore.validation;

import org.jfxcore.validation.DiagnosticListImpl;
import org.junit.jupiter.api.Test;
import javafx.collections.ListChangeListener;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiagnosticListImplTest {

    @Test
    public void testSetDiagnostic() {
        var list = new DiagnosticListImpl<String>(2);
        list.setDiagnostic(3, "three", true);
        list.setDiagnostic(1, "one", false);
        list.setDiagnostic(5, "five", false);
        assertEquals(List.of("one", "three", "five"), list);

        list.setDiagnostic(5, "five-new", true);
        assertEquals(List.of("one", "three", "five-new"), list);
    }

    @Test
    public void testClearDiagnostic() {
        var list = new DiagnosticListImpl<String>(2);
        list.setDiagnostic(3, "three", true);
        list.setDiagnostic(1, "one", false);
        list.setDiagnostic(5, "five", false);

        list.clearDiagnostic(999);
        assertEquals(3, list.size());

        list.clearDiagnostic(3);
        assertEquals(List.of("one", "five"), list);

        list.clearDiagnostic(5);
        assertEquals(List.of("one"), list);

        list.clearDiagnostic(1);
        assertEquals(0, list.size());
    }

    @Test
    public void testDiagnosticIsNotPresentInBothSubLists() {
        var list = new DiagnosticListImpl<String>(2);
        list.setDiagnostic(3, "three", true);
        assertEquals(1, list.validSubList().size());
        assertEquals(0, list.invalidSubList().size());

        list.setDiagnostic(3, "three", false);
        assertEquals(0, list.validSubList().size());
        assertEquals(1, list.invalidSubList().size());
    }

    @Test
    public void testGetSubListsAfterAddingDiagnostics() {
        var list = new DiagnosticListImpl<String>(2);
        list.setDiagnostic(3, "three", true);
        list.setDiagnostic(1, "one", false);
        list.setDiagnostic(5, "five", false);

        var validSubList = list.validSubList();
        assertEquals(List.of("three"), validSubList);

        var invalidSubList = list.invalidSubList();
        assertEquals(List.of("one", "five"), invalidSubList);
    }

    @Test
    public void testSubListIsUnmodifiable() {
        var list = new DiagnosticListImpl<String>(2);
        list.setDiagnostic(1, "one", true);
        list.setDiagnostic(2, "two", false);
        assertThrows(RuntimeException.class, () -> list.validSubList().clear());
        assertThrows(RuntimeException.class, () -> list.invalidSubList().clear());
    }

    @Test
    public void testSubListChangeListener() {
        var list = new DiagnosticListImpl<String>(2);
        var valid = new TestListener();
        var invalid = new TestListener();
        list.validSubList().addListener(valid);
        list.invalidSubList().addListener(invalid);

        list.setDiagnostic(1, "one", true);
        list.setDiagnostic(2, "two", false);
        list.setDiagnostic(3, "three", true);

        assertEquals(2, valid.changes.size());
        valid.changes.get(0).assertChange(List.of("one"), List.of());
        valid.changes.get(1).assertChange(List.of("three"), List.of());

        assertEquals(1, invalid.changes.size());
        invalid.changes.get(0).assertChange(List.of("two"), List.of());

        list.setDiagnostic(1, "one-new", true);

        assertEquals(3, valid.changes.size());
        valid.changes.get(2).assertChange(List.of("one-new"), List.of("one"));
    }

    @Test
    public void testQuiescentSubListChangeListener() {
        var list = new DiagnosticListImpl<String>(2);
        var valid = new TestListener();
        var invalid = new TestListener();
        list.validSubList().addListener(valid);
        list.invalidSubList().addListener(invalid);

        list.beginQuiescence();
        list.setDiagnostic(1, "one", true);
        list.setDiagnostic(2, "two", false);
        list.setDiagnostic(3, "three", true);
        list.setDiagnostic(4, "four", false);
        list.endQuiescence();

        assertEquals(1, valid.changes.size());
        valid.changes.get(0).assertChange(List.of("one", "three"), List.of());

        assertEquals(1, invalid.changes.size());
        invalid.changes.get(0).assertChange(List.of("two", "four"), List.of());
    }

    private record SubChange(List<String> added, List<String> removed) {
        void assertChange(List<String> added, List<String> removed) {
            assertEquals(added, this.added);
            assertEquals(removed, this.removed);
        }
    }

    private static class TestListener implements ListChangeListener<String> {
        final List<SubChange> changes = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public void onChanged(Change<? extends String> change) {
            while (change.next()) {
                changes.add(new SubChange((List<String>)change.getAddedSubList(), (List<String>)change.getRemoved()));
            }
        }
    }

}
