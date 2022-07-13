package gui.view.console.controller;

import gui.controller.PersistenceUtilities;
import gui.view.console.Console;
import logic.mutation.MutationExecutor;
import logic.testcasegenerator.testcaseexecution.TestcaseExecutor;
import shared.model.Mutant;
import shared.model.Testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
                        if (mutantNumber >= mutantStartNumber && mutantNumber <= mutantEndNumber) {
                            mutationExecutor.startMutations(allFunctions, mutantNumber, mutantNumber, regionForExecutor, resetFunction, outputPath);
                        }
                    }
                }
            };
            var thread = new Thread(runnable);
            thread.start();
        }

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

                            var res = tcExecutor.executeTC(testcase);
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
                    tcExecutor.resetApplication(resetFunction);
                    var testcaseInfo = testcasesToExecute.poll();
                    if (testcaseInfo != null) {
                        var testcase = Objects.requireNonNull(testcaseInfo).testcase();
                        var res = tcExecutor.executeTC(testcase);
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
