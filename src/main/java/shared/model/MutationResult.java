package shared.model;

public class MutationResult {
    private final boolean killed;
    private final int tcNumber;
    private  int mutantNumber;
    private final Mutant mutant;
    private final Testcase testcase;
    private final String testSuiteName;
    private final String notCovered;

    public MutationResult(boolean killed, int tcNumber, Mutant mutant, Testcase testcase, String testSuiteName, String notCovered) {
        this.killed = killed;
        this.tcNumber = tcNumber;
        this.mutant = mutant;
        this.testcase = testcase;
        this.testSuiteName = testSuiteName;
        this.notCovered = notCovered;
    }

    public boolean isKilled() {
        return killed;
    }

    public int getTcNumber() {
        return tcNumber;
    }

    public Mutant getMutant() {
        return mutant;
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public int getMutantNumber() {
        return mutantNumber;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setMutantNumber(int mutantNumber) {
        this.mutantNumber = mutantNumber;
    }
}
