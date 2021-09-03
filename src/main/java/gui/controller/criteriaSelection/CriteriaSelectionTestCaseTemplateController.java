package gui.controller.criteriaSelection;

import gui.model.Graph;
import gui.view.StandardPresentationView;
import gui.view.criteriaSelection.CriteriaSelectionView;
import logic.model.TestSuite;
import logic.testcasegenerator.TestCaseGenerator;
import logic.testcasegenerator.TestCaseGeneratorImpl;

public class CriteriaSelectionTestCaseTemplateController implements CriteriaSelectionStrategyController {

    private final Graph model;
    private CriteriaSelectionView view;

    public CriteriaSelectionTestCaseTemplateController(Graph model) {
        this.model = model;
    }

    @Override
    public void setup() {
        this.view = new CriteriaSelectionView("Criteria selection", this);
        view.showAndWait();
    }

    @Override
    public void cancel() {
        view.close();
    }

    @Override
    public void handleInput(boolean isAllResources, boolean isAllRelations, boolean isAllDefs, boolean isAllDefUse, boolean isAllUses) {
        TestCaseGenerator tcGenerator = new TestCaseGeneratorImpl();
        String modelAsJson = model.getJSON();
        if (isAllResources) {

            TestSuite testSuite = tcGenerator.getResourceCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All resources", testSuite.toString());
            tcView.show();
        }

        if (isAllRelations) {
            TestSuite testSuite = tcGenerator.getRelationCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All relations", testSuite.toString());
            tcView.show();
        }

        if (isAllDefs) {
            TestSuite testSuite = tcGenerator.getAllDefsCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All defs", testSuite.toString());
            tcView.show();
        }

        if (isAllDefUse) {
            TestSuite testSuite = tcGenerator.getDefUseCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All defuse", testSuite.toString());
            tcView.show();
        }

        if (isAllUses) {
            TestSuite testSuite = tcGenerator.getAllUsesCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All uses", testSuite.toString());
            tcView.show();
        }
        view.close();
    }
}