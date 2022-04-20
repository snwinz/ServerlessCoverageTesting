package gui.controller;

import gui.model.Graph;
import gui.view.TestCaseExecutionView;
import gui.view.wrapper.TestcaseWrapper;
import javafx.scene.control.Spinner;
import javafx.stage.FileChooser;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.Executor;
import logic.testcaseexecution.TestcaseExecutor;
import shared.model.Testcase;

import java.io.File;
import java.util.List;

public class TestCaseExecutionController {
    private final Graph model;
    private TestCaseExecutionView view;

    public TestCaseExecutionController(Graph model) {

        this.model = model;
    }

    public void setup(List<Testcase> testcases, File tcFile) {
        this.view = new TestCaseExecutionView(this, testcases, tcFile, model);
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
        var thread = new Thread(() -> tcExecutor.calibrate(testcase,resetFunction));
        thread.start();
    }
}
