package gui.controller.criteriaSelection;

import gui.model.Graph;
import gui.view.StandardPresentationView;
import gui.view.criteriaSelection.CriteriaSelectionView;
import logic.model.TestSuiteOfTargets;
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

            TestSuiteOfTargets testSuiteOfTargets = tcGenerator.getResourceCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All resources", testSuiteOfTargets.toString());
            tcView.show();
        }

        if (isAllRelations) {
            TestSuiteOfTargets testSuiteOfTargets = tcGenerator.getRelationCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All relations", testSuiteOfTargets.toString());
            tcView.show();
        }

        if (isAllDefs) {
            TestSuiteOfTargets testSuiteOfTargets = tcGenerator.getAllDefsCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All defs", testSuiteOfTargets.toString());
            tcView.show();
        }

        if (isAllDefUse) {
            TestSuiteOfTargets testSuiteOfTargets = tcGenerator.getDefUseCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All defuse", testSuiteOfTargets.toString());
            tcView.show();
        }

        if (isAllUses) {
            TestSuiteOfTargets testSuiteOfTargets = tcGenerator.getAllUsesCoverage(modelAsJson);
            StandardPresentationView tcView = new StandardPresentationView("All uses", testSuiteOfTargets.toString());
            tcView.show();
        }
        view.close();
    }
}