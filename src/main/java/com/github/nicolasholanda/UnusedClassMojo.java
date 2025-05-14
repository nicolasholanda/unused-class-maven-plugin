package com.github.nicolasholanda;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY)
public class UnusedClassMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File outputDirectory;

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
            for (File f : classFiles) {
                getLog().debug("Class: " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed during unused class detection", e);
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
