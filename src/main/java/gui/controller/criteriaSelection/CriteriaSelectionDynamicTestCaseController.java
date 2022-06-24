package gui.controller.criteriaSelection;

import gui.controller.DynamicTCSelectionController;
import gui.model.Graph;
import gui.view.criteriaSelection.CriteriaSelectionView;
import logic.model.TestSuiteOfTargets;
import logic.testcasegenerator.TestCaseGenerator;
import logic.testcasegenerator.TestCaseGeneratorImpl;


public class CriteriaSelectionDynamicTestCaseController implements CriteriaSelectionStrategyController {

    private final Graph model;
    private CriteriaSelectionView view;

    public CriteriaSelectionDynamicTestCaseController(Graph model) {
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
        TestSuiteOfTargets testSuiteOfTargets = new TestSuiteOfTargets();

        if (isAllResources) {
            TestSuiteOfTargets testSuiteOfTargetsAllResources = tcGenerator.getResourceCoverage(modelAsJson);
            testSuiteOfTargets.add(testSuiteOfTargetsAllResources.getTestTargets());
        }

        if (isAllRelations) {
            TestSuiteOfTargets testSuiteOfTargetsAllRelations = tcGenerator.getRelationCoverage(modelAsJson);
            testSuiteOfTargets.add(testSuiteOfTargetsAllRelations.getTestTargets());
        }
        if (isAllDefs) {
            TestSuiteOfTargets testSuiteOfTargetsAllDefs = tcGenerator.getAllDefsCoverage(modelAsJson);
            testSuiteOfTargets.add(testSuiteOfTargetsAllDefs.getTestTargets());
        }

        if (isAllDefUse) {
            TestSuiteOfTargets testSuiteOfTargetsAllDefUse = tcGenerator.getDefUseCoverage(modelAsJson);
            testSuiteOfTargets.add(testSuiteOfTargetsAllDefUse.getTestTargets());
        }

        if (isAllUses) {
            TestSuiteOfTargets testSuiteOfTargetsAllUse = tcGenerator.getAllUsesCoverage(modelAsJson);
            testSuiteOfTargets.add(testSuiteOfTargetsAllUse.getTestTargets());
        }
        DynamicTCSelectionController controller = new DynamicTCSelectionController();
        controller.setup(testSuiteOfTargets);
        view.close();
    }
}