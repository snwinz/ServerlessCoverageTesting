package logic.testcasegenerator.intermediaresults;

import logic.model.NodeModel;
import logic.model.SourceCodeLine;
import logic.model.Testcase;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface DefViaDB {
    FunctionWithDefSourceLine getDef();

    static List<FunctionWithDefSourceLine> getDefsInAFunction(NodeModel node, long idOfArrow) {
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        var defs = node.getSourceList().stream().
                filter(sourceCodeLine -> (sourceCodeLine.getDefContainer() != null
                        && !sourceCodeLine.getDefContainer().isBlank())).filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencedByDef() != null).
                filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencedByDef().contains(idOfArrow) ||
                        sourceCodeLine.getRelationsInfluencedByDef().contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)).
                collect(Collectors.toList());
        defs.forEach(def -> result.add(new FunctionWithDefSourceLine(node, def)));
        return result;
    }



    List<Testcase> getTestcases(FunctionWithUseSourceLine use);
}
