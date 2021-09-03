package logic.testcasegenerator;


import logic.model.TestSuite;

/**
 * Interface expects graph as JSON since a separate web gui is planned
 */
public interface TestCaseGenerator {
    TestSuite getResourceCoverage(String graphJSON);

    TestSuite getRelationCoverage(String graphJSON);

    TestSuite getAllDefsCoverage(String graphJSON);

    TestSuite getDefUseCoverage(String graphJSON);

    TestSuite getAllUsesCoverage(String graphJSON);
}
