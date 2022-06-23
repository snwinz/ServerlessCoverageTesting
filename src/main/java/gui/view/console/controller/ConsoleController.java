package gui.view.console.controller;

import gui.view.console.Console;
import logic.mutation.MutationExecutor;

import java.nio.file.Path;
import java.util.List;

public class ConsoleController {


    private final MutationExecutor executor;

    public ConsoleController(MutationExecutor executor) {
        this.executor = executor;
    }

    public void setup(String[] args) {
        Console console = new Console(executor, this);
        console.handleInput(args);
    }


    public void setMutants(Path path) {
        executor.setMutants(path);
    }

    public void setTestSuits(Path path) {
        executor.setTestSuits(path);
    }

    public void startMutations(List<String> allFunctions, int mutantStartNumber, int mutantEndNumber, String region, String resetFunction, String outputPath) {
        executor.startMutations(allFunctions, mutantStartNumber, mutantEndNumber, region, resetFunction, outputPath);
    }
}
