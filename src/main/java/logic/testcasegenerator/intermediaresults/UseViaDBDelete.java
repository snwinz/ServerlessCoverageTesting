package logic.testcasegenerator.intermediaresults;

import logic.model.NodeModel;
import logic.model.ServerlessFunction;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;
import shared.model.AccessMode;
import shared.model.NodeType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record UseViaDBDelete(FunctionWithUseSourceLine use,
                             NodeModel db) implements UseViaDB {

    @Override
    public FunctionWithUseSourceLine getUse() {
        return this.use;
    }

    @Override
    public List<Testcase> getTestcases(FunctionWithDefSourceLine def) {
        List<Testcase> testcases = new LinkedList<>();
        var logStatements = List.of(def.getLogMessage() + use.getLogMessage());
        var function1 = new ServerlessFunction(def.getFunction());
        var function2 = new ServerlessFunction(use.getFunction());
        String target = String.format("def %s should be covered by %s ", def, use);
        List<NodeModel> writeNodes = getWriteNodesOfDB(db);
        for (var writeNode : writeNodes) {
            var writeFunction = new ServerlessFunction(writeNode);
            String targetWithWrite = target + " while data are written by " + writeFunction.getName() + " before data are updated";
            logStatements = List.of(writeFunction.getLogMessage() + def.getLogMessage() + use.getLogMessage());
            Testcase testcase = new Testcase(List.of(writeFunction, function1, function2), targetWithWrite, logStatements);
            testcases.add(testcase);
        }
        if (writeNodes.isEmpty()) {
            String targetWithoutWrite = target + " but no function found to write data before data are deleted/updated";
            Testcase testcase = new Testcase(List.of(function1, function2), targetWithoutWrite, logStatements);
            testcases.add(testcase);
        }
        return testcases;
    }

    private List<NodeModel> getWriteNodesOfDB(NodeModel dbNode) {
        List<NodeModel> result = new ArrayList<>();
        var arrows = dbNode.getIncomingArrows();
        for (var arrow : arrows) {
            var predecessor = arrow.getPredecessorNode();
            if (arrow.hasAccessMode(AccessMode.CREATE) && NodeType.FUNCTION.equals(predecessor.getType())) {
                result.add(predecessor);
            }
        }
        return result;
    }

}
