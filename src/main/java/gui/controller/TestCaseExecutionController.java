package gui.controller;

import gui.view.StandardPresentationView;
import gui.view.TestCaseExecutionView;
import gui.view.wrapper.TestcaseWrapper;
import javafx.stage.FileChooser;
import logic.evaluation.TestcaseEvaluator;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.Executor;
import logic.testcaseexecution.TestcaseExecutor;
import shared.model.Testcase;

import java.io.File;
import java.util.List;

public class TestCaseExecutionController {
    private TestCaseExecutionView view;

    public TestCaseExecutionController() {
    }

    public void setup(List<Testcase> testcases) {
        this.view = new TestCaseExecutionView(this, testcases);
        view.showAndWait();
    }

    public void executeReset(String resetFunction, String region) {
        Executor executor = new AWSInvoker(region);
        var thread = new Thread(() -> executor.callResetFunction(resetFunction));
        thread.start();
    }

    public void executeTC(TestcaseWrapper testcase, String region) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.executeTC(testcase));
        thread.start();

    }

    public void saveTestcases(List<Testcase> testcasesOriginal) {
        var fileChooser = new FileChooser();
        var extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var fileToSave = fileChooser.showSaveDialog(view);
        PersistenceUtilities.saveTestSuite(testcasesOriginal, fileToSave.getAbsolutePath());
    }

    public void calibrateOutput(TestcaseWrapper testcase, String region, String resetFunction) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.calibrate(testcase, resetFunction));
        thread.start();
    }

    public void executeTestcases(List<TestcaseWrapper> testcases, String region, String resetFunction) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.executeTCs(testcases, resetFunction));
        thread.start();
    }
    public void calibrateTestcases(List<TestcaseWrapper> testcases, String region, String resetFunction) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.calibrate(testcases, resetFunction));
        thread.start();
    }

    public void deleteLogs(String region) {
        Executor executor = new AWSInvoker(region);
        executor.deleteOldLogs();
    }

    public void getLogs(String region) {
        Executor executor = new AWSInvoker(region);

        var logs = executor.getAllNewLogs(0L);
        var view = new StandardPresentationView("All logs", String.join("", logs));
        view.show();

    }

    public void showPassedTCs(List<TestcaseWrapper> testcases) {
        TestcaseEvaluator evaluator = new TestcaseEvaluator(testcases);
        String text = evaluator.getPassedData();
        var view = new StandardPresentationView("Testcase data", text);
        view.show();

    }


}
