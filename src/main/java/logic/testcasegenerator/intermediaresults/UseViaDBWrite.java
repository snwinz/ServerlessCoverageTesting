package logic.testcasegenerator.intermediaresults;

import logic.model.ServerlessFunction;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;

import java.util.List;

public record UseViaDBWrite(FunctionWithUseSourceLine use) implements UseViaDB {

    @Override
    public List<Testcase> getTestcases(FunctionWithDefSourceLine def) {
        var logStatements = List.of(def.getLogMessage() + use.getLogMessage());
        var function1 = new ServerlessFunction(def.getFunction());
        var function2 = new ServerlessFunction(use.getFunction());
        String target = String.format("def %s should be covered by %s ", def, use);
        Testcase testcase = new Testcase(List.of(function1, function2), target, logStatements);
        return List.of(testcase);
    }
}
