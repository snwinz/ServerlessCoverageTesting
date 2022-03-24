package logic.testcasegenerator.intermediaresults;

import logic.model.NodeModel;
import logic.model.ServerlessFunction;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;
import shared.model.AccessMode;
import shared.model.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefViaDBViaWrite implements DefViaDB {
    private FunctionWithDefSourceLine def;

    public DefViaDBViaWrite(FunctionWithDefSourceLine def) {
        this.def = def;
    }

    public static List<DefViaDB> parse(NodeModel dbNode) {
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        var arrows = dbNode.getIncomingArrows();
        for (var arrow : arrows) {
            var predecessor = arrow.getPredecessorNode();
            if (arrow.hasAccessMode(AccessMode.CREATE) && NodeType.FUNCTION.equals(predecessor.getType())) {
                result.addAll(DefViaDB.getDefsInAFunction(predecessor, arrow.getIdentifier()));
            }
        }
        return result.stream().map(DefViaDBViaWrite::new).collect(Collectors.toList());
    }

    @Override
    public FunctionWithDefSourceLine getDef() {
        return this.def;
    }

    @Override
    public List<Testcase>  getTestcases(FunctionWithUseSourceLine use) {
        var logStatements = List.of(def.getLogMessage() + use.getLogMessage());
        String defDescription = String.format("def %s", def);
        String target = String.format("use %s should be covered by %s", use, defDescription);
        var functionDef = new ServerlessFunction(def.getFunction());
        var functionUse = new ServerlessFunction(use.getFunction());
        Testcase testcase = new Testcase(List.of(functionDef, functionUse), target, logStatements);
        return List.of(testcase);
    }
}
