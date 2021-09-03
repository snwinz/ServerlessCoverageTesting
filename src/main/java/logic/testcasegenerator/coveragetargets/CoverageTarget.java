package logic.testcasegenerator.coveragetargets;

import logic.model.Testcase;

import java.util.List;

public interface CoverageTarget {
    List<Testcase> getTestcases();


    String getAspectLogMessage();

    String getAspectTarget();
}
