package shared.model;

import java.util.List;

public class TestSuite {
    private final String name;
    private final List<Testcase> testcases;

    public TestSuite(String name, List<Testcase> testcases) {
        this.name = name;
        this.testcases = testcases;
    }

    public String getName() {
        return name;
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }
}
