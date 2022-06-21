package logic.testcasegenerator;


import logic.model.TestSuiteOfTargets;

/**
 * Interface expects graph as JSON since a separate web gui is planned
 */
public interface TestCaseGenerator {
    TestSuiteOfTargets getResourceCoverage(String graphJSON);

    TestSuiteOfTargets getRelationCoverage(String graphJSON);

    TestSuiteOfTargets getAllDefsCoverage(String graphJSON);

    TestSuiteOfTargets getDefUseCoverage(String graphJSON);

    TestSuiteOfTargets getAllUsesCoverage(String graphJSON);
}
