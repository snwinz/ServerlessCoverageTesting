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
import java.util.stream.Collectors;

import static logic.testcasegenerator.intermediaresults.DefViaDB.getDefsInAFunction;

public record DefViaDBViaDelete(FunctionWithDefSourceLine def, NodeModel db) implements DefViaDB {

    public static List<DefViaDB> parse(NodeModel dbNode) {

        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        var arrows = dbNode.getIncomingArrows();
        for (var arrow : arrows) {
            var predecessor = arrow.getPredecessorNode();
            if (arrow.hasAccessMode(AccessMode.DELETE) && NodeType.FUNCTION.equals(predecessor.getType())) {
                result.addAll(getDefsInAFunction(predecessor, arrow.getIdentifier()));
            }
        }
        return result.stream().map(function -> new DefViaDBViaDelete(function, dbNode)).collect(Collectors.toList());
    }

    @Override
    public List<Testcase> getTestcases(FunctionWithUseSourceLine use) {
        List<Testcase> testcases = new LinkedList<>();
        var logStatements = List.of(def.getLogMessage() + use.getLogMessage());
        String defDescription = String.format("def %s", def);
        String target = String.format("use %s should be covered by %s", use, defDescription);
        var functionDef = new ServerlessFunction(def.getFunction());
        var functionUse = new ServerlessFunction(use.getFunction());
        List<NodeModel> writeNodes = getWriteNodesOfDB(db);
        for (var writeNode : writeNodes) {
            var writeFunction = new ServerlessFunction(writeNode);
            String targetWithWrite = target + " while data are written by " + writeFunction.getName() + " before data are deleted";
            logStatements = List.of(writeFunction.getLogMessage() + def.getLogMessage() + use.getLogMessage());
            Testcase testcase = new Testcase(List.of(writeFunction, functionDef, functionUse), targetWithWrite, logStatements);
            testcases.add(testcase);
        }
        if (writeNodes.isEmpty()) {
            String targetWithoutWrite = target + " but no function found to write data before data are deleted";
            Testcase testcase = new Testcase(List.of(functionDef, functionUse), targetWithoutWrite, logStatements);
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
