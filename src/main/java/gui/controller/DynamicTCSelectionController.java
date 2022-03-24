package gui.controller;

import gui.model.Graph;
import gui.view.DynamicTCSelectionView;
import gui.view.StandardPresentationView;
import gui.view.wrapper.Commands;
import logic.dynamicdatageneration.DynamicTestCaseGenerator;
import logic.dynamicdatageneration.TestcaseSimulator;
import logic.model.TestSuite;
import logic.model.Testcase;

import java.util.List;

public class DynamicTCSelectionController {

    private DynamicTCSelectionView view;

    public DynamicTCSelectionController() {


    }

    public void setup(TestSuite testSuite, Graph model) {
        this.view = new DynamicTCSelectionView(testSuite, model, this);
        view.setMaximized(true);
        view.show();
    }

    public void closeView() {
        if (view != null) {
            view.close();
        }
    }

    public void startDynamicTCCalculation(List<Testcase> testcasesToBeCreated, Graph model, Commands commands) {
        DynamicTestCaseGenerator dynamicTestCaseGenerator = new DynamicTestCaseGenerator();
        Thread thread = new Thread(() -> dynamicTestCaseGenerator.generateTestcases(testcasesToBeCreated, commands));
        thread.start();


    }


    public void reexecuteTestcase(Testcase testcase, String region) {
        TestcaseSimulator simulator = new TestcaseSimulator(1, region);
        Thread thread = new Thread(() -> simulator.executeSingleTestCase(testcase));
        thread.start();
    }

    public void executeReset(String resetFunction, String region) {

        TestcaseSimulator simulator = new TestcaseSimulator(1, region);
        simulator.setResetFunction(resetFunction);
        Thread thread = new Thread(simulator::resetApplication);
        thread.start();
    }

    public void getTestCaseData(Testcase testcase) {
        String infoOfTestcase = testcase.getInfos();
        StandardPresentationView tcView = new StandardPresentationView("TC info", infoOfTestcase);
        tcView.show();
    }

    public void showTestSuitData(TestSuite testSuite) {
        String data = testSuite.getCoverageData();
        StandardPresentationView tcView = new StandardPresentationView("Test suite info", data);
        tcView.show();
    }


}
