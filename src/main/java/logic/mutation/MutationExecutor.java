package logic.mutation;

import gui.controller.PersistenceUtilities;
import logic.executionplatforms.Executor;
import logic.testcasegenerator.testcaseexecution.TestcaseExecutor;
import shared.model.Mutant;
import shared.model.MutationResult;
import shared.model.TestSuite;
import shared.model.Testcase;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MutationExecutor {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Mutant> mutants = new ArrayList<>();
    private final List<TestSuite> testSuites = new ArrayList<>();

    public void setMutants(Path mutantFolder) {
        this.mutants.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(mutantFolder, "*.json")) {
            for (Path entry : stream) {
                var mutantsOfFile = PersistenceUtilities.loadMutants(entry);
                mutants.addAll(mutantsOfFile);
            }
        } catch (IOException e) {
            System.err.println("Could not read : " + mutantFolder.toAbsolutePath());
        }
        pcs.firePropertyChange("mutationsUpdated", null, mutants);
    }

    public void setTestSuits(Path folderOfTestSuits) {
        this.testSuites.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderOfTestSuits, "*.json")) {
            for (Path entry : stream) {
                var testcases = PersistenceUtilities.loadTCs(entry.toAbsolutePath().toString());
                String nameOfTestSuite = getNameOfTestSuite(entry);
                var testSuite = new TestSuite(nameOfTestSuite, testcases);
                testSuites.add(testSuite);
            }
        } catch (IOException e) {
            System.err.println("Could not read : " + folderOfTestSuits.toAbsolutePath());
        }
        pcs.firePropertyChange("testSuitesUpdated", null, testSuites);
    }

    private String getNameOfTestSuite(Path entry) {
        String fileName = entry.getFileName().toString();
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.indexOf("."));
        }
        return fileName;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public List<Mutant> getMutants() {
        return mutants;
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public void startMutations(List<String> allFunctions, int minValue, int maxValue, String region, String resetFunction, String targetDirectory) {
        TestcaseExecutor testcaseExecutor = new TestcaseExecutor(region);
        for (int i = minValue; i <= maxValue; i++) {
            var mutation = mutants.get(i);
            for (var testSuite : testSuites) {
                System.out.println("Start Mutation " + i + " with test suite " + testSuite.getName());
                var mutationResult = checkMutationForTestSuite(mutation, testSuite, allFunctions, testcaseExecutor, resetFunction);
                mutationResult.setMutantNumber(i);
                PersistenceUtilities.saveMutationResult(mutationResult, Path.of(targetDirectory));
            }
        }
    }

    private MutationResult checkMutationForTestSuite(Mutant mutant, TestSuite testSuite, List<String> allFunctions, TestcaseExecutor tcExecutor, String resetFunction) {
        setEnvironmentVariables(mutant, allFunctions, tcExecutor.getExecutor());
        Optional<Testcase> killingTestcase = Optional.empty();
        String missingPart = "";
        var executor = tcExecutor.getExecutor();
        var testcases = testSuite.getTestcases();
        for (Testcase testcase : testcases) {
            executor.resetApplication(resetFunction);
            if (tcExecutor.executeTC(testcase).isPresent()) {
                executor.resetApplication(resetFunction);
                var partNotCoveredInformation = tcExecutor.executeTC(testcase);
                if (partNotCoveredInformation.isPresent()) {
                    missingPart = partNotCoveredInformation.get();
                    killingTestcase = Optional.of(testcase);
                    break;
                }
            }
        }
        var tcNumber = -1;
        if (killingTestcase.isPresent()) {
            tcNumber = testSuite.getTestcases().indexOf(killingTestcase.get());
        }
        return new MutationResult(killingTestcase.isPresent(), tcNumber, mutant, killingTestcase.orElse(null), testSuite.getName(), missingPart);
    }


    private void setEnvironmentVariables(Mutant mutant, List<String> allFunctions, Executor executor) {
        Map<String, String> envVariables = new HashMap<>();

        if (mutant.getMutationType() != null) {
            envVariables.put("processType", mutant.getMutationType().toString());
        }
        if (mutant.getVariable() != null) {
            envVariables.put("variableName", mutant.getVariable());
        }
        if (mutant.getValue() != null) {
            envVariables.put("variableValue", mutant.getValue());
        }
        if (mutant.getLocation() != null) {
            envVariables.put("locationIdentifier", mutant.getLocation());
        }

        executor.setEnvironmentVariables(allFunctions, envVariables);
    }
}
