package com.github.nicolasholanda;

import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Analyzes dependencies of a Java class using ASM's {@link ClassVisitor}.
 * <p>
 * This class collects all class names referenced as superclasses, interfaces,
 * field types, method argument types, and return types.
 * </p>
 */
public class ClassDependencyAnalyzer extends ClassVisitor {

    /**
     * Set of class names (in internal JVM format, e.g., java/lang/String) that this class depends on.
     */
    private final Set<String> referencedClasses = new HashSet<>();

    private String className;

    /**
     * Constructs a new {@code ClassDependencyAnalyzer} using ASM9 API version.
     */
    public ClassDependencyAnalyzer() {
        super(Opcodes.ASM9);
    }

    /**
     * Analyzes the dependencies of a given .class file.
     *
     * @param classFile the .class file to be analyzed
     * @throws java.io.IOException if a read error occurs
     */
    public void analyze(java.io.File classFile) throws java.io.IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(classFile)) {
            org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(fis);
            reader.accept(this, 0);
        }
    }

    /**
     * Visits the header of the class.
     *
     * @param version    the class version
     * @param access     the class's access flags (see {@link Opcodes})
     * @param name       the internal name of the class
     * @param signature  the signature of this class
     * @param superName  the internal name of the super class (may be {@code null} for {@code Object})
     * @param interfaces the internal names of the class's interfaces (may be {@code null})
     */
    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        className = name;

        if (superName != null) {
            referencedClasses.add(superName);
        }
        if (interfaces != null) {
            referencedClasses.addAll(Arrays.asList(interfaces));
        }
    }

    /**
     * Visits a field of the class.
     *
     * @param access     the field's access flags (see {@link Opcodes})
     * @param name       the field's name
     * @param descriptor the field's descriptor (see {@link Type})
     * @param signature  the field's signature
     * @param value      the field's value (if any)
     * @return a visitor to visit field annotations and attributes, or {@code null}
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String type = Type.getType(descriptor).getClassName().replace('.', '/');
        referencedClasses.add(type);
        return super.visitField(access, name, descriptor, signature, value);
    }

    /**
     * Visits a method of the class.
     *
     * @param access     the method's access flags (see {@link Opcodes})
     * @param name       the method's name
     * @param descriptor the method's descriptor (see {@link Type})
     * @param signature  the method's signature
     * @param exceptions the internal names of the method's exception classes (if any)
     * @return a visitor to visit the method code, or {@code null}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Type methodType = Type.getMethodType(descriptor);

        for (Type argType : methodType.getArgumentTypes()) {
            referencedClasses.add(argType.getClassName().replace('.', '/'));
        }

        referencedClasses.add(methodType.getReturnType().getClassName().replace('.', '/'));
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    /**
     * Returns the set of class dependencies collected during the visit.
     *
     * @return a set of internal class names this class depends on
     */
    public Set<String> getReferencedClasses() {
        return referencedClasses;
    }

    /**
     * Returns the fully qualified name of the class being analyzed.
     *
     * @return the class name in dot notation (e.g., java.lang.String)
     */
    public String getClassName() {
        return className;
    }
}
