package com.github.nicolasholanda;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ClassDependencyAnalyzerTest {

    @Test
    void testAnalyzeSimpleClass() throws IOException {
        String className = "com/github/nicolasholanda/SimpleClass";
        String superName = "java/lang/Object";
        File classFile = generateSimpleClass(className, superName);

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(classFile);

        assertEquals(className, analyzer.getClassName());
        Set<String> refs = analyzer.getReferencedClasses();
        assertTrue(refs.contains(superName));
    }

    @Test
    void testAnalyzeClassWithInterface() throws IOException {
        String className = "com/github/nicolasholanda/WithInterface";
        String superName = "java/lang/Object";
        String[] interfaces = {"java/io/Serializable"};
        File classFile = generateSimpleClass(className, superName, interfaces);

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(classFile);

        Set<String> refs = analyzer.getReferencedClasses();
        assertTrue(refs.contains(superName));
        assertTrue(refs.contains(interfaces[0]));
    }

    @Test
    void testAnalyzeClassWithFieldAndMethod() throws IOException {
        String className = "com/github/nicolasholanda/FieldMethodClass";
        String superName = "java/lang/Object";
        File file = File.createTempFile("FieldMethodClass", ".class");
        file.deleteOnExit();

        ClassWriter classWriter = new ClassWriter(0);

        classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, superName, null);
        classWriter.visitField(Opcodes.ACC_PRIVATE, "list", "Ljava/util/List;", null, null).visitEnd();
        classWriter.visitMethod(Opcodes.ACC_PUBLIC, "foo", "(Ljava/util/Map;)Ljava/util/List;", null, null).visitEnd();

        classWriter.visitEnd();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(classWriter.toByteArray());
        }

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(file);

        Set<String> refs = analyzer.getReferencedClasses();
        assertTrue(refs.contains(superName));
        assertTrue(refs.contains("java/util/List"));
        assertTrue(refs.contains("java/util/Map"));
    }

    @Test
    void testAnalyzeClassWithSpringAnnotation() throws IOException {
        String className = "com/github/nicolasholanda/AnnotatedController";
        String superName = "java/lang/Object";
        File file = File.createTempFile("AnnotatedController", ".class");
        file.deleteOnExit();

        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, superName, null);
        // Add @Controller annotation
        classWriter.visitAnnotation("Lorg/springframework/stereotype/Controller;", true).visitEnd();
        classWriter.visitEnd();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(classWriter.toByteArray());
        }

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(file);

        assertTrue(analyzer.isUsedByFramework());
    }

    @Test
    void testAnalyzeClassWithJaxRsAnnotation() throws IOException {
        String className = "com/github/nicolasholanda/JaxRsResource";
        String superName = "java/lang/Object";
        File file = File.createTempFile("JaxRsResource", ".class");
        file.deleteOnExit();

        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, superName, null);
        // Add @Path annotation
        classWriter.visitAnnotation("Ljavax/ws/rs/Path;", true).visitEnd();
        classWriter.visitEnd();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(classWriter.toByteArray());
        }

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(file);

        assertTrue(analyzer.isUsedByFramework());
    }

    @Test
    void testAnalyzeClassWithoutFrameworkAnnotation() throws IOException {
        String className = "com/github/nicolasholanda/PlainClass";
        String superName = "java/lang/Object";
        File file = generateSimpleClass(className, superName);

        ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
        analyzer.analyze(file);

        assertFalse(analyzer.isUsedByFramework());
    }

    private File generateSimpleClass(String className, String superName, String... interfaces) throws IOException {
        File file = File.createTempFile(className, ".class");
        file.deleteOnExit();

        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, superName, interfaces);
        classWriter.visitEnd();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(classWriter.toByteArray());
        }
        return file;
    }
}
