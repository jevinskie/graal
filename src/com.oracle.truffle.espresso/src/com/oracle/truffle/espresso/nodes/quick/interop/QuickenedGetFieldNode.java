/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.espresso.nodes.quick.interop;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Field;
import com.oracle.truffle.espresso.nodes.BytecodeNode;
import com.oracle.truffle.espresso.nodes.helper.AbstractGetFieldNode;
import com.oracle.truffle.espresso.nodes.quick.QuickNode;
import com.oracle.truffle.espresso.runtime.StaticObject;

public final class QuickenedGetFieldNode extends QuickNode {
    private final Field field;

    @Child AbstractGetFieldNode getFieldNode;

    public QuickenedGetFieldNode(int top, int callerBCI, Field field) {
        super(top, callerBCI);
        assert !field.isStatic();
        this.getFieldNode = AbstractGetFieldNode.create(field);
        this.field = field;
    }

    @Override
    public int execute(final VirtualFrame frame) {
        // TODO: instrumentation
        BytecodeNode root = getBytecodesNode();
        StaticObject receiver = nullCheck(root.peekAndReleaseObject(frame, top - 1));
        return getFieldNode.getField(frame, root, receiver, top - 1);
    }

    @Override
    public boolean producedForeignObject(VirtualFrame frame) {
        return field.getKind().isObject() && getBytecodesNode().peekObject(frame, top).isForeignObject();
    }
}
