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

import java.util.List;

@SuppressWarnings("unused")
public class ListChange<T> {

    private final int from;
    private final int to;

    ListChange(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public static class AddedRange<T> extends ListChange<T> {
        private final List<T> elements;

        public AddedRange(int from, T element) {
            super(from, from + 1);
            this.elements = List.of(element);
        }

        public AddedRange(int from, List<T> elements) {
            super(from, from + elements.size());
            this.elements = List.copyOf(elements);
        }

        public List<T> getElements() {
            return elements;
        }
    }

    public static class RemovedRange<T> extends ListChange<T> {
        private final int removedSize;

        public RemovedRange(int from, int removedSize) {
            super(from, from + removedSize);
            this.removedSize = removedSize;
        }

        public int getRemovedSize() {
            return removedSize;
        }
    }

    public static class ReplacedRange<T> extends ListChange<T> {
        private final List<T> elements;
        private final int removedSize;

        public ReplacedRange(int from, int removedSize, T element) {
            super(from, from + 1);
            this.removedSize = removedSize;
            this.elements = List.of(element);
        }

        public ReplacedRange(int from, int removedSize, List<T> elements) {
            super(from, from + elements.size());
            this.removedSize = removedSize;
            this.elements = List.copyOf(elements);
        }

        public List<T> getElements() {
            return elements;
        }

        public int getRemovedSize() {
            return removedSize;
        }
    }

}
