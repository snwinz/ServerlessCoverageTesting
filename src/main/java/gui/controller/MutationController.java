package gui.controller;

import gui.view.MutationView;
import logic.model.LogicGraph;
import logic.mutation.MutationExecutor;
import shared.model.Mutant;
import shared.model.TestSuite;

import java.nio.file.Path;
import java.util.List;

public class MutationController {


    private final MutationExecutor mutationExecutor;
    private MutationView mutationView;

    public MutationController() {
        this.mutationExecutor = new MutationExecutor();
    }

    public void setup(LogicGraph graph) {
        this.mutationView = new MutationView(mutationExecutor, this, graph);
        mutationView.show();
        mutationExecutor.addPropertyChangeListener(mutationView);
    }

    public void openMutants(Path path) {
        mutationExecutor.setMutants(path);
    }


    public void openTestSuites(Path path) {
        mutationExecutor.setTestSuits(path);
    }


    public void startMutations(List<Mutant> mutants, List<TestSuite> testSuites, List<String> allFunctions, int minValue, int maxValue, String region, String resetFunction, String targetDirectory) {
        mutationExecutor.startMutations(mutants, testSuites,allFunctions, minValue, maxValue, region, resetFunction, targetDirectory);
    }
}
