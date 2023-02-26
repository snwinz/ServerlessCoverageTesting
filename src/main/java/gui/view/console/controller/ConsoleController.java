package gui.view.console.controller;

import gui.controller.PersistenceUtilities;
import gui.view.console.Console;
import gui.view.wrapper.ExecutionSettings;
import logic.dynamicdatageneration.DynamicTestCaseGenerator;
import logic.model.TestSuiteOfTargets;
import logic.mutation.MutationExecutor;
import logic.testcasegenerator.TestCaseGenerator;
import logic.testcasegenerator.TestCaseGeneratorImpl;
import logic.testcasegenerator.coveragetargets.CoverageTarget;
import logic.testcasegenerator.testcaseexecution.TestcaseExecutor;
import shared.model.Mutant;
import shared.model.Testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsoleController {


    private final MutationExecutor mutationExecutor;

    public ConsoleController(MutationExecutor mutationExecutor) {
        this.mutationExecutor = mutationExecutor;
    }

    public void setup(String[] args) {
        Console console = new Console(this);
        console.handleInput(args);
    }


    public void setMutants(Path path) {
        mutationExecutor.setMutants(path);
    }

    public void setOldMutationResults(Path path) {
        mutationExecutor.setOldMutationResults(path);
    }

    public void setTestSuits(Path path) {
        mutationExecutor.setTestSuits(path);
    }

    public void startMutations(List<String> allFunctions, int mutantStartNumber, int mutantEndNumber, String region, String resetFunction, String outputPath) {

        String[] regions = getRegions(region);


        List<Mutant> mutantList = mutationExecutor.getMutants();
        BlockingQueue<Mutant> mutants = new LinkedBlockingQueue<>(mutationExecutor.getMutants());

        for (var regionForExecutor : regions) {
            Runnable runnable = () -> {
                while (!mutants.isEmpty()) {
                    var mutant = mutants.poll();
                    if (mutant != null) {
                        int mutantNumber = mutantList.indexOf(mutant);
                        if (mutantNumber >= mutantStartNumber && mutantNumber <= mutantEndNumber && mutantNumber < mutantList.size()) {
                            mutationExecutor.startMutations(allFunctions, mutantNumber, mutantNumber, regionForExecutor, resetFunction, outputPath);
                        }
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }

    }

    public void createDynamicTestcases(String graphPath, String resetFunction, Set<String> authKeys, String regionsAsParameter, int startNumberIncluding, int endNumberExcluding, String outputPath, String metric) {
        TestCaseGenerator testCaseGenerator = new TestCaseGeneratorImpl();
        String graphJson = null;
        try {
            graphJson = Files.readString(Path.of(graphPath));
        } catch (IOException e) {
            System.out.printf("File of graph with path %s could not be loaded", graphPath);
            return;
        }
        TestSuiteOfTargets testSuiteOfTargets = new TestSuiteOfTargets();
        if (metric == null) {
            testSuiteOfTargets.add(testCaseGenerator.getResourceCoverage(graphJson).getTestTargets());
            testSuiteOfTargets.add(testCaseGenerator.getRelationCoverage(graphJson).getTestTargets());
            testSuiteOfTargets.add(testCaseGenerator.getAllDefsCoverage(graphJson).getTestTargets());
            testSuiteOfTargets.add(testCaseGenerator.getDefUseCoverage(graphJson).getTestTargets());
            testSuiteOfTargets.add(testCaseGenerator.getAllUsesCoverage(graphJson).getTestTargets());
        }
        switch (metric) {
            case "allResources" ->
                    testSuiteOfTargets.add(testCaseGenerator.getResourceCoverage(graphJson).getTestTargets());
            case "allRelations" ->
                    testSuiteOfTargets.add(testCaseGenerator.getRelationCoverage(graphJson).getTestTargets());
            case "allDefs" -> testSuiteOfTargets.add(testCaseGenerator.getAllDefsCoverage(graphJson).getTestTargets());
            case "allDefUse" -> testSuiteOfTargets.add(testCaseGenerator.getDefUseCoverage(graphJson).getTestTargets());
            case "allUses" -> testSuiteOfTargets.add(testCaseGenerator.getAllUsesCoverage(graphJson).getTestTargets());
            default -> System.out.println("Parameter not correct, choose on of the following: allResources, allRelations, allDefs, allDefUse, allUses");
        }

        DynamicTestCaseGenerator dynamicTestCaseGenerator = new DynamicTestCaseGenerator();


        var regions = getRegions(regionsAsParameter);
        var targetQueue = new LinkedBlockingQueue<CoverageTarget>();
        var testTargets = testSuiteOfTargets.getTestTargets();
        if (startNumberIncluding > endNumberExcluding || testTargets == null || testTargets.size() <= endNumberExcluding) {
            return;
        }
        for (int i = startNumberIncluding; i < endNumberExcluding; i++) {
            var target = testTargets.get(i);
            try {
                targetQueue.put(target);
            } catch (InterruptedException e) {
                System.out.printf("Target %s could not be added", target);
            }
        }
        for (var region : regions) {
            var settings = new ExecutionSettings(region, resetFunction, authKeys);
            settings.setProbSimilarOutputAsValue(1.0);
            settings.setNumberOfTries(1);
            Runnable runnable = () -> {
                while (!targetQueue.isEmpty()) {
                    var testTarget = targetQueue.poll();
                    if (testTarget != null) {

                        var testcases = testTarget.getTestcases();
                        for (var testcase : testcases) {
                            var logsToCover = testcase.getLogsOfTarget();
                            String fileName = String.join("", logsToCover);
                            try {

                                var result = dynamicTestCaseGenerator.generateTestcase(testcase, settings);
                                if (result.isPresent()) {

                                    saveTestCase(outputPath, fileName, testTarget, testcase, settings.getAuthKeys());
                                    break;
                                }
                            } catch (Exception e) {
                                System.out.println("retry of : " + testTarget);
                                var result = dynamicTestCaseGenerator.generateTestcase(testcase, settings);
                                if (result.isPresent()) {

                                    saveTestCase(outputPath, fileName, testTarget, testcase, settings.getAuthKeys());
                                    break;
                                }
                            }

                        }
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }


    }

    private static void saveTestCase(String outputPath, String fileName, CoverageTarget testTarget, logic.model.Testcase testcase, Set<String> authKeys) {
        var testcaseForExecution = testcase.getSharedTestcaseCopy(testTarget.getCoverageTargetDescription(), authKeys);
        testcaseForExecution.setManualCreated(false);
        PersistenceUtilities.saveTestSuite(List.of(testcaseForExecution), Path.of(outputPath, fileName));
    }

    record TestSuiteInfo(List<Testcase> testcase, Path path) {
    }

    public void calibrateFolder(Path pathOfTestSuites, String region, String resetFunction) {
        BlockingQueue<TestSuiteInfo> testSuitesToExecute = getTestSuites(pathOfTestSuites);
        String[] regions = getRegions(region);
        for (var regionForExecutor : regions) {
            Runnable runnable = () -> {
                TestcaseExecutor tcExecutor = new TestcaseExecutor(regionForExecutor);
                while (!testSuitesToExecute.isEmpty()) {
                    var testSuiteInfo = testSuitesToExecute.poll();
                    if (testSuiteInfo != null) {
                        var testcases = testSuiteInfo.testcase;
                        for (var testcase : testcases) {
                            try {
                                tcExecutor.calibrate(testcase, resetFunction);
                            } catch (Exception e) {
                                //brutal retry
                                tcExecutor.calibrate(testcase, resetFunction);
                            }
                            PersistenceUtilities.saveTestSuite(testcases, testSuiteInfo.path);
                        }
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }

    }

    private BlockingQueue<TestSuiteInfo> getTestSuites(Path pathOfTestSuites) {
        if (!Files.isDirectory(pathOfTestSuites) || Files.notExists(pathOfTestSuites)) {
            throw new IllegalArgumentException("Invalid path to folder");
        }
        var testSuitesToExecute = new LinkedBlockingQueue<TestSuiteInfo>();
        try (var walk = Files.walk(pathOfTestSuites)) {
            var files = walk
                    .filter(Files::isRegularFile)   // is a file
                    .filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            for (var path : files) {
                var testcases = PersistenceUtilities.loadTCs(path);
                testSuitesToExecute.put(new TestSuiteInfo(testcases, path));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Could not read : " + pathOfTestSuites);
        }
        return testSuitesToExecute;
    }

    private String[] getRegions(String region) {
        return region.split(",");
    }


    public void reCalibrateFolder(Path pathOfTestSuites, String region, String resetFunction) {
        BlockingQueue<TestSuiteInfo> testSuitesToExecute = getTestSuites(pathOfTestSuites);
        String[] regions = getRegions(region);
        for (var regionForExecutor : regions) {
            Runnable runnable = () -> {
                TestcaseExecutor tcExecutor = new TestcaseExecutor(regionForExecutor);
                while (!testSuitesToExecute.isEmpty()) {
                    var testSuiteInfo = testSuitesToExecute.poll();
                    if (testSuiteInfo != null) {
                        var testcases = testSuiteInfo.testcase;
                        for (var testcase : testcases) {
                            String potentialAuthentication = tcExecutor.resetApplication(resetFunction);
                            var res = tcExecutor.executeTC(testcase, potentialAuthentication);
                            if (res.isPresent()) {
                                res.ifPresent((t) -> System.out.printf("TestSuite of %s is not correct%nFailure: %s%nTestcase:%n %s%n%n",
                                        testSuiteInfo.path.toString(), t, testcase));
                                tcExecutor.recalibrate(testcase, resetFunction);
                                PersistenceUtilities.saveTestSuite(testcases, testSuiteInfo.path);
                            }
                        }
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }

    }


    public void executeTestcases(String pathOfTestSuites, String region, String resetFunction) {
        record TestcaseInfo(Testcase testcase, Path path) {
        }
        Path folderContainingTestSuites = Path.of(pathOfTestSuites);
        if (!Files.isDirectory(folderContainingTestSuites) || Files.notExists(folderContainingTestSuites)) {
            throw new IllegalArgumentException("Invalid path to folder");
        }
        var testcasesToExecute = new LinkedBlockingQueue<TestcaseInfo>();
        try (var walk = Files.walk(folderContainingTestSuites)) {
            var files = walk
                    .filter(Files::isRegularFile)   // is a file
                    .filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            for (var path : files) {
                var testcases = PersistenceUtilities.loadTCs(path);
                for (var testcase : testcases) {
                    testcasesToExecute.put(new TestcaseInfo(testcase, path));
                }
                PersistenceUtilities.saveTestSuite(testcases, path);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Could not read : " + pathOfTestSuites);
        }
        String[] regions = getRegions(region);
        for (var regionForExecutor : regions) {
            Runnable runnable = () -> {
                TestcaseExecutor tcExecutor = new TestcaseExecutor(regionForExecutor);
                while (!testcasesToExecute.isEmpty()) {
                    String potentialAuthentication = tcExecutor.resetApplication(resetFunction);
                    var testcaseInfo = testcasesToExecute.poll();
                    if (testcaseInfo != null) {
                        var testcase = Objects.requireNonNull(testcaseInfo).testcase();
                        var res = tcExecutor.executeTC(testcase, potentialAuthentication);
                        res.ifPresent((t) -> System.out.printf("TestSuite of %s is not correct%nFailure: %s%nTestcase:%n %s%n%n",
                                testcaseInfo.path.toString(), t, testcaseInfo.testcase));
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }
    }

}
