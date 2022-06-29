package gui.view.console.controller;

import gui.controller.PersistenceUtilities;
import gui.view.console.Console;
import logic.mutation.MutationExecutor;
import logic.testcasegenerator.testcaseexecution.TestcaseExecutor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConsoleController {


    private final MutationExecutor mutationExecutor;

    public ConsoleController(MutationExecutor mutationExecutor) {
        this.mutationExecutor = mutationExecutor;
    }

    public void setup(String[] args) {
        Console console = new Console(this);
        console.handleInput(args);
    }


    public void setMutants(Path path) {
        mutationExecutor.setMutants(path);
    }

    public void setTestSuits(Path path) {
        mutationExecutor.setTestSuits(path);
    }

    public void startMutations(List<String> allFunctions, int mutantStartNumber, int mutantEndNumber, String region, String resetFunction, String outputPath) {
        mutationExecutor.startMutations(allFunctions, mutantStartNumber, mutantEndNumber, region, resetFunction, outputPath);
    }


    public void calibrateFolder(Path pathOfTestSuite, String region, String resetFunction) {
        TestcaseExecutor testcaseExecutor = new TestcaseExecutor(region);
        if (Files.isDirectory(pathOfTestSuite)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathOfTestSuite, "*.json")) {
                for (var path : stream) {
                    var testcases = PersistenceUtilities.loadTCs(path);
                    for (var testcase : testcases) {
                        testcaseExecutor.calibrate(testcase, resetFunction);
                    }
                    PersistenceUtilities.saveTestSuite(testcases, path);
                }
            } catch (IOException e) {
                System.err.println("Could not read : " + pathOfTestSuite.toAbsolutePath());
            }
        }


    }
}
