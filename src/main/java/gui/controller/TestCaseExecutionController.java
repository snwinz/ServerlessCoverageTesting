package gui.controller;

import gui.model.Graph;
import gui.view.TestCaseExecutionView;
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
        this.view = new TestCaseExecutionView( this, testcases,tcFile, model);
        view.showAndWait();
    }

    public void executeReset(String resetFunction, String region) {
        Executor executor = new AWSInvoker(region);
        var thread = new Thread(() -> executor.callResetFunction(resetFunction));
        thread.start();
    }

    public void executeTC(Testcase testcase, String region) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        tcExecutor.executeTC(testcase);
        var thread = new Thread(() -> tcExecutor.executeTC(testcase));
        thread.start();

    }
}
