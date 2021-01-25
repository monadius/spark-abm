package org.sparklogo.project;

import org.sparklogo.gui.Configuration;

import java.io.File;

public class MainCLI {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: spark-logo path_to_project_file");
            return;
        }
        Project project = new Project();
        Configuration config = new Configuration();

        System.out.println("Reading: " + args[0]);
        ProjectFile.readProjectFile(new File(args[0]), project);

        System.out.println("Translating");
        project.translate(config.getLibPath());

        System.out.println("Compiling");
        project.compile(config.getLibPath());
    }
}
