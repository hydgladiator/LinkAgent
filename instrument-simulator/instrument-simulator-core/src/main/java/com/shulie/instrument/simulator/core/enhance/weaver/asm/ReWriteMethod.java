/**
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shulie.instrument.simulator.core.enhance.weaver.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 方法重写
 * ReWriteJavaMethod
 */
public class ReWriteMethod extends AdviceAdapter implements Opcodes, AsmTypes, AsmMethods {

    private final Type[] argumentTypeArray;

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api    the ASM API version implemented by this visitor. Must be one
     *               of {@link Opcodes#ASM4} or {@link Opcodes#ASM7}.
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    protected ReWriteMethod(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.argumentTypeArray = Type.getArgumentTypes(desc);
    }

    /**
     * 将NULL压入栈
     */
    final protected void pushNull() {
        push((Type) null);
    }

    /**
     * 是否静态方法
     *
     * @return true:静态方法 / false:非静态方法
     */
    final protected boolean isStaticMethod() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    /**
     * 加载this/className
     */
    final protected void loadThisOrPushNullIsStatic() {
        if (isStaticMethod()) {
            pushNull();
        } else {
            loadThis();
        }
    }

    final protected void checkCastReturn(Type returnType) {
        final int sort = returnType.getSort();
        switch (sort) {
            case Type.VOID: {
                pop();
                mv.visitInsn(Opcodes.RETURN);
                break;
            }
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT: {
                unbox(returnType);
                returnValue();
                break;
            }
            case Type.FLOAT: {
                unbox(returnType);
                mv.visitInsn(Opcodes.FRETURN);
                break;
            }
            case Type.LONG: {
                unbox(returnType);
                mv.visitInsn(Opcodes.LRETURN);
                break;
            }
            case Type.DOUBLE: {
                unbox(returnType);
                mv.visitInsn(Opcodes.DRETURN);
                break;
            }
            case Type.ARRAY:
            case Type.OBJECT:
            case Type.METHOD:
            default: {
                // checkCast(returnType);
                unbox(returnType);
                mv.visitInsn(ARETURN);
                break;
            }

        }
    }

    final protected void visitFieldInsn(int opcode, Type owner, String name, Type type) {
        super.visitFieldInsn(opcode, owner.getInternalName(), name, type.getDescriptor());
    }

    /**
     * 保存参数数组
     */
    final protected void storeArgArray() {
        for (int i = 0; i < argumentTypeArray.length; i++) {
            dup();
            push(i);
            arrayLoad(ASM_TYPE_OBJECT);
            unbox(argumentTypeArray[i]);
            storeArg(i);
        }
    }

}
