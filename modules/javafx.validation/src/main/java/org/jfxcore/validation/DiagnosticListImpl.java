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

package org.jfxcore.validation;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.validation.DiagnosticList;
import java.util.ArrayList;
import java.util.List;

public class DiagnosticListImpl<D> extends ObservableListBase<D> implements DiagnosticList<D> {

    private final List<Diagnostic<D>> backingList;
    private SubListBase<D> validList;
    private SubListBase<D> invalidList;
    private boolean quiescent;

    public DiagnosticListImpl(int maxCapacity) {
        this.backingList = new ArrayList<>(Math.min(2, maxCapacity));
    }

    @Override
    public D get(int index) {
        return backingList.get(index).value;
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isValid(int index) {
        return backingList.get(index).valid;
    }

    @Override
    public ObservableList<D> validSubList() {
        if (validList == null) {
            validList = new SubListBase<>(backingList, true);
        }

        return validList;
    }

    @Override
    public ObservableList<D> invalidSubList() {
        if (invalidList == null) {
            invalidList = new SubListBase<>(backingList, false);
        }

        return invalidList;
    }

    public void beginQuiescence() {
        if (quiescent) {
            throw new IllegalStateException();
        }

        quiescent = true;
        beginChange();

        if (validList != null) {
            validList.doBeginChange();
        }

        if (invalidList != null) {
            invalidList.doBeginChange();
        }
    }

    public void endQuiescence() {
        if (!quiescent) {
            throw new IllegalStateException();
        }

        quiescent = false;
        endChange();

        if (validList != null) {
            validList.doEndChange();
        }

        if (invalidList != null) {
            invalidList.doEndChange();
        }
    }

    public void setDiagnostic(int diagnosticIndex, D diagnostic, boolean valid) {
        beginChangeImpl();
        var newDiagnostic = new Diagnostic<>(diagnostic, diagnosticIndex, valid);

        for (int i = 0, max = backingList.size(); i <= max; ++i) {
            if (i == max) {
                backingList.add(newDiagnostic);
                nextAdd(i, i + 1);
            } else {
                Diagnostic<D> d = backingList.get(i);

                if (diagnosticIndex < d.index) {
                    backingList.add(i, newDiagnostic);
                    nextAdd(i, i + 1);
                    break;
                }

                if (diagnosticIndex == d.index) {
                    D oldValue = backingList.get(i).value;
                    backingList.set(i, newDiagnostic);
                    nextSet(i, oldValue);
                    break;
                }
            }
        }

        SubListBase<D> setSubList = valid ? validList : invalidList;
        if (setSubList != null) {
            setSubList.setDiagnostic(newDiagnostic);
        }

        SubListBase<D> clearSubList = valid ? invalidList : validList;
        if (clearSubList != null) {
            clearSubList.clearDiagnostic(diagnosticIndex);
        }

        endChangeImpl();
    }

    public void clearDiagnostic(int diagnosticIndex) {
        beginChangeImpl();
        boolean valid = false;

        for (int i = 0, max = backingList.size(); i < max; ++i) {
            if (diagnosticIndex == backingList.get(i).index) {
                Diagnostic<D> removed = backingList.remove(i);
                valid = removed.valid;
                nextRemove(i, removed.value);
                break;
            }
        }

        SubListBase<D> subList = valid ? validList : invalidList;
        if (subList != null) {
            subList.clearDiagnostic(diagnosticIndex);
        }

        endChangeImpl();
    }

    private void beginChangeImpl() {
        if (!quiescent) {
            beginChange();

            if (validList != null) {
                validList.doBeginChange();
            }

            if (invalidList != null) {
                invalidList.doBeginChange();
            }
        }
    }

    private void endChangeImpl() {
        if (!quiescent) {
            endChange();

            if (validList != null) {
                validList.doEndChange();
            }

            if (invalidList != null) {
                invalidList.doEndChange();
            }
        }
    }

    private record Diagnostic<D>(D value, int index, boolean valid) {}

    private static class SubListBase<D> extends ObservableListBase<D> {
        final List<Diagnostic<D>> backingList;

        SubListBase(List<Diagnostic<D>> backingList, boolean valid) {
            List<Diagnostic<D>> newList = new ArrayList<>(backingList.size());

            for (Diagnostic<D> diagnostic : backingList) {
                if (diagnostic.valid() == valid) {
                    newList.add(diagnostic);
                }
            }

            this.backingList = newList;
        }

        @Override
        public D get(int index) {
            return backingList.get(index).value();
        }

        @Override
        public int size() {
            return backingList.size();
        }

        void setDiagnostic(Diagnostic<D> diagnostic) {
            for (int i = 0, max = backingList.size(); i <= max; ++i) {
                if (i == max) {
                    backingList.add(diagnostic);
                    nextAdd(i, i + 1);
                } else {
                    Diagnostic<D> d = backingList.get(i);

                    if (diagnostic.index < d.index) {
                        backingList.add(i, diagnostic);
                        nextAdd(i, i + 1);
                        break;
                    }

                    if (diagnostic.index == d.index) {
                        D oldValue = backingList.get(i).value;
                        backingList.set(i, diagnostic);
                        nextSet(i, oldValue);
                        break;
                    }
                }
            }
        }

        public void clearDiagnostic(int diagnosticIndex) {
            for (int i = 0, max = backingList.size(); i < max; ++i) {
                if (diagnosticIndex == backingList.get(i).index) {
                    Diagnostic<D> removed = backingList.remove(i);
                    nextRemove(i, removed.value);
                    break;
                }
            }
        }

        void doBeginChange() {
            beginChange();
        }

        void doEndChange() {
            endChange();
        }
    }

}
