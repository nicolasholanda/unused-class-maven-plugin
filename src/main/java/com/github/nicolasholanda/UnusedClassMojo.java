package com.github.nicolasholanda;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.util.*;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY)
public class UnusedClassMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File outputDirectory;

    private Map<String, Set<String>> referenceGraph = new HashMap<>();
    private Set<String> allClasses = new HashSet<>();

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Running Unused Class Detector on: " + outputDirectory);

        if (!outputDirectory.exists()) {
            throw new MojoExecutionException("Output directory does not exist: " + outputDirectory);
        }


        try {
            List<File> classFiles = new ArrayList<>();
            collectClassFiles(outputDirectory, classFiles);

            getLog().info("Found " + classFiles.size() + " .class files");
            for (File file : classFiles) {
                getLog().debug("Class: " + file.getAbsolutePath());

                ClassDependencyAnalyzer analyzer = new ClassDependencyAnalyzer();
                analyzer.analyze(file);

                String className = analyzer.getClassName();
                allClasses.add(className);
                referenceGraph.put(className, analyzer.getReferencedClasses());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed during unused class detection", e);
        }

        Set<String> usedClasses = new HashSet<>();
        for (Set<String> refs : referenceGraph.values()) {
            usedClasses.addAll(refs);
        }

        Set<String> unusedClasses = new HashSet<>(allClasses);
        unusedClasses.removeAll(usedClasses);

        if (unusedClasses.isEmpty()) {
            getLog().info("No unused classes found.");
        } else {
            getLog().info("Unused classes:");
            for (String unusedClass : unusedClasses) {
                getLog().info("  " + unusedClass);
            }
        }
    }

    private void collectClassFiles(File dir, List<File> classFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                collectClassFiles(f, classFiles);
            } else if (f.getName().endsWith(".class")) {
                classFiles.add(f);
            }
        }
    }
}
