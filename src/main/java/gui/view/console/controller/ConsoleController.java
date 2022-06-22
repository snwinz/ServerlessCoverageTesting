package gui.view.console.controller;

import gui.view.console.Console;
import logic.mutation.MutationExecutor;

public class ConsoleController {


    private final MutationExecutor executor;

    public ConsoleController(MutationExecutor executor) {
        this.executor = executor;
    }

    public void setup(String[] args) {
        Console console = new Console(executor,this);
        console.handleInput(args);
    }




}
