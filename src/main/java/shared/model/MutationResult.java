package shared.model;

import java.util.List;

public class MutationResult {
    private final boolean killed;
    private final List<Integer> tcNumber;
    private  int mutantNumber;
    private final Mutant mutant;
    private final String testSuiteName;
    private final String notCovered;

    public MutationResult(boolean killed, List<Integer> testCases, Mutant mutant, String testSuiteName, String notCovered) {
        this.killed = killed;
        this.tcNumber = testCases;
        this.mutant = mutant;
        this.testSuiteName = testSuiteName;
        this.notCovered = notCovered;
    }

    public boolean isKilled() {
        return killed;
    }

    public List<Integer> getTcNumber() {
        return tcNumber;
    }

    public Mutant getMutant() {
        return mutant;
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

    public String getNotCovered() {
        return notCovered;
    }


}
