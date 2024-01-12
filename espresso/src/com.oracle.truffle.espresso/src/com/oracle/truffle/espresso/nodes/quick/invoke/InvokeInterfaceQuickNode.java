/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeInterface;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeInterfaceNodeGen;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.vm.ContinuationSupport;

public final class InvokeInterfaceQuickNode extends InvokeQuickNode {

    @Child InvokeInterface.WithoutNullCheck invokeInterface;

    public InvokeInterfaceQuickNode(Method method, int top, int curBCI) {
        super(method, top, curBCI);
        assert !method.isStatic();
        this.invokeInterface = insert(InvokeInterfaceNodeGen.WithoutNullCheckNodeGen.create(method));
    }

    @Override
    public int execute(VirtualFrame frame) {
        Object[] args = getArguments(frame);
        nullCheck((StaticObject) args[0]);
        return pushResult(frame, invokeInterface.execute(args));
    }

    @Override
    public int resumeContinuation(VirtualFrame frame, ContinuationSupport.HostFrameRecord hfr) {
        // The frame doesn't hold the arguments anymore, they were cleared during the invoke that
        // happened before the user suspended. So we get the receiver from the frame we're about to
        // wind in the first reference slot (which is 1, because slot 0 is the bci and thus a long
        // slot).
        StaticObject receiver = hfr.pointers[1];
        nullCheck(receiver);
        return pushResult(frame, invokeInterface.execute(new Object[]{receiver, hfr}));
    }
}
