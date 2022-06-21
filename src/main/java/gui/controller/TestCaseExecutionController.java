package gui.controller;

import gui.model.Graph;
import gui.model.TestcasesContainer;
import gui.view.StandardPresentationView;
import gui.view.ExecutionView;
import gui.view.wrapper.TestcaseWrapper;
import javafx.stage.FileChooser;
import logic.evaluation.TestcaseEvaluator;
import logic.executionplatforms.AWSInvoker;
import logic.executionplatforms.Executor;
import logic.model.LogicGraph;
import logic.testcasegenerator.RandomTestSuiteGenerator;
import logic.testcasegenerator.testcaseexecution.TestcaseExecutor;
import shared.model.Testcase;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class TestCaseExecutionController {
    private final ExecutionView view;
    private final TestcasesContainer model;


    public TestCaseExecutionController(List<Testcase> testcases, Graph graph) {
        this.model = new TestcasesContainer(testcases);
        this.view = new ExecutionView(this, testcases, graph);

    }

    public void setup() {
        model.addPropertyChangeListener(view);
        view.setMaximized(true);
        view.show();
    }

    public void executeReset(String resetFunction, String region) {
        Executor executor = new AWSInvoker(region);
        var thread = new Thread(() -> executor.callResetFunction(resetFunction));
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

    public void recalibrateOutput(TestcaseWrapper testcase, String region, String resetFunction) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.recalibrate(testcase, resetFunction));
        thread.start();
    }

    public void recalibrateTestcases(List<TestcaseWrapper> testcases, String region, String resetFunction) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.recalibrateTCs(testcases, resetFunction));
        thread.start();
    }

    public void executeTC(TestcaseWrapper testcase, String region) {
        TestcaseExecutor tcExecutor = new TestcaseExecutor(region);
        var thread = new Thread(() -> tcExecutor.executeTC(testcase));
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

    public void resetApplication(String resetFunction, String region) {
        Executor executor = new AWSInvoker(region);
        executor.resetApplication(resetFunction);
    }

    public void getLogsOnPlatform(String region) {
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

    public void addFunctionToTestcase(TestcaseWrapper testcase) {
        this.model.addFunctionToTestcase(testcase.getTestcase());
    }


    public void getLogsOfTestcases(List<TestcaseWrapper> testcases) {
        var allLogLists = testcases.stream().map(TestcaseWrapper::getLogsMeasured)
                .filter(Objects::nonNull).flatMap(entry -> entry.stream()).toList();
        var text = String.join("\n", allLogLists);
        var view = new StandardPresentationView("logs of testcases", text);
        view.show();
    }

    public void evaluateLogs(List<TestcaseWrapper> testcases, Graph graph) {
        var allLogs = testcases.stream().map(TestcaseWrapper::getLogsMeasured)
                .filter(Objects::nonNull).flatMap(entry -> entry.stream()).toList();
        LogEvaluationController controller = new LogEvaluationController(graph);
        controller.setup(allLogs);

    }


    public void createTestSuite(Graph graph, List<Testcase> testSuite) {
        if (graph != null) {

            var fileChooser = new FileChooser();
            var extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            var fileToSave = fileChooser.showSaveDialog(view);

            var randomTestSuiteGenerator = new RandomTestSuiteGenerator( new LogicGraph(graph.getJSON()));
            List<Testcase> randomTestcases = randomTestSuiteGenerator.generateTestcases(testSuite);
            PersistenceUtilities.saveTestSuite(randomTestcases, fileToSave.getAbsolutePath());
        }
    }
}
