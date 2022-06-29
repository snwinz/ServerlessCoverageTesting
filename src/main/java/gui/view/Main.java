package gui.view;

import gui.view.console.controller.ConsoleController;
import logic.mutation.MutationExecutor;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            MutationExecutor executor = new MutationExecutor();
            ConsoleController controller = new ConsoleController(executor);
            controller.setup(args);
        } else {
            MainApp.main(args);
        }
    }
}
