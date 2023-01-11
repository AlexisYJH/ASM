package com.example.asminject;

import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 1. 引入ASM依赖
 * testImplementation 'org.ow2.asm:asm:7.1'
 * testImplementation 'org.ow2.asm:asm-commons:7.1'
 *
 * 2. 准备待插桩Class
 * 新建InjectTest.java; 编译为Class文件：在test/java目录右键open in terminal：javac com/example/asminject/InjectTest.java
 *
 * 3. 编码实现
 * 新建ASMUnitTest.java
 * 字节码指令
 *     命令：javap -c xxx.class
 *     插件：ASM ByteCode Viewer
 *
 * 4. 只在特定方法中插桩：注解
 * 1）新建ASMTest注解
 * 2）指定方法上添加注解@ASMTest
 * 3）判断方法上是否有注解
 */
public class ASMUnitTest {
    @Test
    public void test() throws IOException {
        FileInputStream fis = new FileInputStream("src/test/java/com/example/asminject/InjectTest.class");
        //class分析器
        ClassReader reader = new ClassReader(fis);
        //栈帧！class自动计算栈帧和局部变量表大小
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        //执行分析，修改
        reader.accept(new MyClassVisitor(Opcodes.ASM9, writer), ClassReader.EXPAND_FRAMES);
        //执行了插桩后得字节码数据
        byte[] bytes = writer.toByteArray();
        FileOutputStream fos = new FileOutputStream("src/test/java2/com/example/asminject/InjectTest.class");
        fos.write(bytes);
        fos.close();
    }

    static class MyClassVisitor extends ClassVisitor {

        public MyClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            System.out.println("visitMethod: " + name + " " + descriptor);
            return new MyMethodVisitor(api, methodVisitor, access, name, descriptor);
        }
    }

    //MethodVisitor子类AdviceAdapter(onMethodEnter, onMethodExit)，位于asm-commons库
    static class MyMethodVisitor extends AdviceAdapter {
        protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        boolean inject = false;
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            System.out.println("visitAnnotation: " + getName() + " " +  descriptor);
            if ("Lcom/example/asminject/ASMTest;".equals(descriptor)) {
                inject = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        int start;
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            if (!inject) {
                return;
            }
            //long start = System.currentTimeMillis();
            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            //索引
            start = newLocal(Type.LONG_TYPE);
            //用一个本地变量接收上一步的结果
            storeLocal(start);
        }

        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            if (!inject) {
                return;
            }
            //long end = System.currentTimeMillis();
            //System.out.println("execute:" + (end-start)+" ms.");
            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            //索引
            int end = newLocal(Type.LONG_TYPE);
            //用一个本地变量接收上一步的结果
            storeLocal(end);
            getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));
            newInstance(Type.getType("Ljava/lang/StringBuilder;"));
            dup();
            invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), new Method("<init>", "()V"));
            visitLdcInsn("execute:");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            loadLocal(end);
            loadLocal(start);
            math(SUB, Type.LONG_TYPE);
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(J)Ljava/lang/StringBuilder;"));
            visitLdcInsn(" ms.");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("toString", "()Ljava/lang/String;"));
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"));
        }
    }
}