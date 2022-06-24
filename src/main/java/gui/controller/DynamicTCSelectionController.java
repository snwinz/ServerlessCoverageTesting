package gui.controller;

import gui.view.DynamicTCSelectionView;
import gui.view.StandardPresentationView;
import gui.view.wrapper.ExecutionSettings;
import javafx.stage.FileChooser;
import logic.dynamicdatageneration.DynamicTestCaseGenerator;
import logic.dynamicdatageneration.TestcaseSimulator;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.Executor;
import logic.model.TestSuiteOfTargets;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.CoverageTarget;

import java.io.File;
import java.util.List;

public class DynamicTCSelectionController {

    private DynamicTCSelectionView view;

    public DynamicTCSelectionController() {


    }

    public void setup(TestSuiteOfTargets testSuiteOfTargets) {
        this.view = new DynamicTCSelectionView(testSuiteOfTargets, this);
        view.setMaximized(true);
        view.show();
    }

    public void closeView() {
        if (view != null) {
            view.close();
        }
    }

    public void startDynamicTCCalculation(List<Testcase> testcasesToBeCreated, ExecutionSettings executionSettings) {
        DynamicTestCaseGenerator dynamicTestCaseGenerator = new DynamicTestCaseGenerator();
        Thread thread = new Thread(() -> dynamicTestCaseGenerator.generateTestcases(testcasesToBeCreated, executionSettings));
        thread.start();


    }


    public void reexecuteTestcase(Testcase testcase, String region) {
        TestcaseSimulator simulator = new TestcaseSimulator(1, region);
        Thread thread = new Thread(() -> simulator.executeSingleTestCase(testcase));
        thread.start();
    }

    public void executeReset(String resetFunction, String region) {
        Executor executor = new AWSInvoker(region);
        var thread = new Thread(() -> executor.resetApplication(resetFunction));
        thread.start();
    }

    public void getTestCaseData(Testcase testcase) {
        String infoOfTestcase = testcase.getInfos();
        StandardPresentationView tcView = new StandardPresentationView("TC info", infoOfTestcase);
        tcView.show();
    }

    public void showTestSuitData(TestSuiteOfTargets testSuiteOfTargets) {
        String data = testSuiteOfTargets.getCoverageData();
        StandardPresentationView tcView = new StandardPresentationView("Test suite info", data);
        tcView.show();
    }


    public void showTestSuiteForExecution(TestSuiteOfTargets testSuiteOfTargets) {
        String data = testSuiteOfTargets.getTCsWithInput();
        StandardPresentationView tcView = new StandardPresentationView("Test suite info", data);
        tcView.show();
    }

    public void exportTestSuitForExecution(List<shared.model.Testcase> testSuiteExecution) {
        var fileChooser = new FileChooser();
        var extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var fileToSave = fileChooser.showSaveDialog(view);
        if (fileToSave != null) {
            PersistenceUtilities.saveTestSuite(testSuiteExecution, fileToSave.toPath());
        }
    }

    public void coverAllTargets(List<CoverageTarget> testTargets, ExecutionSettings executionSettings) {
        DynamicTestCaseGenerator dynamicTestCaseGenerator = new DynamicTestCaseGenerator();
        Thread thread = new Thread(() -> dynamicTestCaseGenerator.generateTestcasesForTarget(testTargets, executionSettings));
        thread.start();
    }
}
