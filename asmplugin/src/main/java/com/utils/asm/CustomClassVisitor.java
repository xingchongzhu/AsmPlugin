package com.utils.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

/**
 * @author zhanglulu on 2020/5/27.
 * for
 */
public class CustomClassVisitor extends ClassVisitor {

    private String mClassName;
    private String[] mInterfaces;

    public CustomClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mClassName = name;
        mInterfaces = interfaces;
        System.out.println("mInterfaces: " + Arrays.toString(mInterfaces));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        System.out.println("visitMethod methodName " + name);
        if (isMatchOnClickListener(name)) {
            System.out.println("implements the interface " + name);
            methodVisitor = new CustomAdviceAdapter(methodVisitor, access, name, descriptor) {
                @Override
                protected void onMethodEnter() {
                    super.onMethodEnter();
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn("click before implements the interface " + name+" descriptor "+descriptor);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }

                @Override
                protected void onMethodExit(int opcode) {
                    super.onMethodExit(opcode);
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn("click finish");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
            };
        }
        return methodVisitor;
    }

    private boolean isMatchOnClickListener(String methodName) {
        if (mInterfaces == null) {
            return false;
        }
        for (String mInterface : mInterfaces) {
            if (mInterface.equals("android/view/View$OnClickListener") && "onClick".equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}
