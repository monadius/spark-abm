package org.sparklogo.project;

import org.sparklogo.gui.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "spark-pl", version = "1.4.0", description = "A SPARK-PL command line tool")
public class MainCLI implements Callable<Integer>  {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    @SuppressWarnings("unused")
    boolean usageHelpRequested;

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "display version info")
    @SuppressWarnings("unused")
    boolean versionInfoRequested;

    @Option(names = {"-c", "--compile"}, description = "compile output Java files")
    boolean compileFlag;

    @Option(names = {"-o", "--output"}, description = "output directory")
    File outputDirectory;

    @Option(names = "--name", description = "custom project name")
    String projectName;

    @Option(names = "--lib", description = "path to SPARK/lib")
    File customLibPath;

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive groups;

    static class Exclusive {
        @ArgGroup(exclusive = false, multiplicity = "1")
        FileOptions fileOptions;

        @ArgGroup(exclusive = false, multiplicity = "1")
        ProjectOptions projectOptions;
    }

    static class ProjectOptions {
        @Option(names = {"-p", "--project"}, required = true, description = "compile the SPARK-PL project file")
        File projectFile;
    }

    static class FileOptions {
        @Parameters(arity = "1..*", description = "SPARK-PL files to compile")
        File[] inputFiles;
    }

    @Override
    public Integer call() throws Exception {
        Project project = getProject();
        File libPath = getLibPath();
        project.translate(libPath);
        if (compileFlag) {
            project.compile(libPath);
        }
        return 0;
    }

    private Project getProject() throws Exception {
        final Project project = new Project();
        if (groups.projectOptions != null) {
            ProjectFile.readProjectFile(groups.projectOptions.projectFile, project);
        }
        else {
            for (File file : groups.fileOptions.inputFiles) {
                project.addFile(file);
            }
        }
        if (outputDirectory != null){
            project.setOutputDirectory(outputDirectory.getAbsoluteFile());
        }
        if (projectName != null) {
            project.setName(projectName);
        }

        return project;
    }

    private File getLibPath() {
        return customLibPath != null ?
                customLibPath :
                new Configuration().getLibPath();
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new MainCLI()).execute(args));
    }
}
