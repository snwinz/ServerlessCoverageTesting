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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MutationExecutor {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Mutant> mutants = new ArrayList<>();
    private final List<TestSuite> testSuites = new ArrayList<>();
    private final List<MutationResult> oldMutationResults = new ArrayList<>();

    public void setMutants(Path mutantFolder) {
        this.mutants.clear();
        try (var walk = Files.walk(mutantFolder)) {
            var files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            for (Path entry : files) {
                var mutantsOfFile = PersistenceUtilities.loadMutants(entry);
                mutants.addAll(mutantsOfFile);
            }
        } catch (IOException e) {
            System.err.println("Could not read : " + mutantFolder.toAbsolutePath());
        }
        pcs.firePropertyChange("mutationsUpdated", null, mutants);
    }

    public void setOldMutationResults(Path oldMutationResultsFolder) {
        this.oldMutationResults.clear();
        try (var walk = Files.walk(oldMutationResultsFolder)) {
            var files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".txt")).toList();
            for (Path entry : files) {
                var mutationResult = PersistenceUtilities.loadMutationResults(entry);
                oldMutationResults.add(mutationResult);
            }
        } catch (IOException e) {
            System.err.println("Could not read : " + oldMutationResultsFolder.toAbsolutePath());
        }
    }

    public void setTestSuits(Path folderOfTestSuits) {
        this.testSuites.clear();

        try (var walk = Files.walk(folderOfTestSuits)) {
            var files = walk
                    .filter(Files::isRegularFile)   // is a file
                    .filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            for (var entry : files) {
                var testcases = PersistenceUtilities.loadTCs(entry);
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
                var cachedResult = findCachedResult(mutation, testSuite.getName());
                Path targetPath = Path.of(targetDirectory);
                MutationResult mutationResult;
                mutationResult = cachedResult.orElseGet(() -> checkMutationForTestSuite(mutation, testSuite, allFunctions, testcaseExecutor, resetFunction));
                mutationResult.setMutantNumber(i);
                PersistenceUtilities.saveMutationResult(mutationResult, targetPath);
            }
        }
    }

    private Optional<MutationResult> findCachedResult(Mutant mutation, String testSuiteName) {
        if (mutation == null || testSuiteName == null) {
            return Optional.empty();
        }
        return oldMutationResults.stream().filter(entry -> entry.getMutant().equals(mutation) && testSuiteName.equals(entry.getTestSuiteName())).findAny();
    }

    private MutationResult checkMutationForTestSuite(Mutant mutant, TestSuite testSuite, List<String> allFunctions, TestcaseExecutor tcExecutor, String resetFunction) {
        setEnvironmentVariables(mutant, allFunctions, tcExecutor.getExecutor());
        StringBuilder missingParts = new StringBuilder();
        int killCounter = 0;
        List<Integer> killingTestcases = new ArrayList<>();
        var executor = tcExecutor.getExecutor();
        var testcases = testSuite.getTestcases();
        for (Testcase testcase : testcases) {
            String potentialAuthentication = executor.resetApplication(resetFunction);
            if (tcExecutor.executeTC(testcase, potentialAuthentication).isPresent()) {
                //repeat if testcase does not cover
                potentialAuthentication = tcExecutor.resetApplication(resetFunction);
                var partNotCoveredInformation = tcExecutor.executeTC(testcase, potentialAuthentication);
                if (partNotCoveredInformation.isPresent()) {
                    missingParts.append(killCounter++).append(":\n").append(partNotCoveredInformation.get()).append("\n");
                    var tcNumber = testSuite.getTestcases().indexOf(testcase);
                    killingTestcases.add(tcNumber);
                }
            }
        }

        return new
                MutationResult(killingTestcases.size() > 0, killingTestcases, mutant, testSuite.getName(), missingParts.toString());
    }


    private void setEnvironmentVariables(Mutant mutant, List<String> allFunctions, Executor executor) {
        Map<String, String> envVariables = new HashMap<>();

        if (mutant.getMutationType() != null) {
            envVariables.put("mutationType", mutant.getMutationType().toString());
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
