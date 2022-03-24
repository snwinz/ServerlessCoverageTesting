package gui.controller.criteriaSelection;

import gui.controller.DynamicTCSelectionController;
import gui.model.Graph;
import gui.view.criteriaSelection.CriteriaSelectionView;
import logic.model.TestSuite;
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
        TestSuite testSuite = new TestSuite();

        if (isAllResources) {
            TestSuite testSuiteAllResources = tcGenerator.getResourceCoverage(modelAsJson);
            testSuite.add(testSuiteAllResources.getTestTargets());
        }

        if (isAllRelations) {
            TestSuite testSuiteAllRelations = tcGenerator.getRelationCoverage(modelAsJson);
            testSuite.add(testSuiteAllRelations.getTestTargets());
        }
        if (isAllDefs) {
            TestSuite testSuiteAllDefs = tcGenerator.getAllDefsCoverage(modelAsJson);
            testSuite.add(testSuiteAllDefs.getTestTargets());
        }

        if (isAllDefUse) {
            TestSuite testSuiteAllDefUse = tcGenerator.getDefUseCoverage(modelAsJson);
            testSuite.add(testSuiteAllDefUse.getTestTargets());
        }

        if (isAllUses) {
            TestSuite testSuiteAllUse = tcGenerator.getAllUsesCoverage(modelAsJson);
            testSuite.add(testSuiteAllUse.getTestTargets());
        }
        DynamicTCSelectionController controller = new DynamicTCSelectionController();
        controller.setup(testSuite,model);
        view.close();
    }
}