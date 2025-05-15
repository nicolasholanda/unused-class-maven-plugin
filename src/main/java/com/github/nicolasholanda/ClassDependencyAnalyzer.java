package com.github.nicolasholanda;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassDependencyAnalyzer extends ClassVisitor {

    public ClassDependencyAnalyzer() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        // TODO: Implement logic here to detect dependencies in a class
    }
}
