package logic.testcasegenerator.intermediaresults;

import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;

import java.util.List;

public interface UseViaDB {
    FunctionWithUseSourceLine use();




    List<Testcase> getTestcases(FunctionWithDefSourceLine def);
}
