package logic.testcasegenerator;

import logic.model.LogicGraph;
import logic.model.SourceCodeLine;
import logic.testcasegenerator.coveragetargets.*;
import logic.testcasegenerator.coveragetargets.coverageelements.DefUsePair;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;
import shared.model.NodeType;

import java.util.ArrayList;
import java.util.List;

public class TargetGenerator {
    private final GraphHelper graphHelper = new GraphHelper();

    public List<CoverageTargetAllResources> getAllTargetsToBeCoveredByAllResources(LogicGraph logicGraph) {
        List<CoverageTargetAllResources> result = new ArrayList<>();
        logicGraph.getNodes().forEach(node -> result.add(new CoverageTargetAllResources(node)));
        return result;
    }


    public List<CoverageTargetAllRelations> getAllTargetsToBeCoveredByAllRelations(LogicGraph logicGraph) {
        List<CoverageTargetAllRelations> result = new ArrayList<>();
        logicGraph.getArrows().forEach(arrow -> result.add(new CoverageTargetAllRelations(arrow)));
        return result;
    }

    public List<CoverageTargetAllDefs> getAllTargetsToBeCoveredByAllDefs(LogicGraph logicGraph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(logicGraph);
        List<CoverageTargetAllDefs> lambdaDefs = new ArrayList<>();
        allDefsOfGraph.forEach(def -> lambdaDefs.add(new CoverageTargetAllDefs(def)));
        return lambdaDefs;
    }

    public List<FunctionWithDefSourceLine> getAllDefsOfGraph(LogicGraph logicGraph) {
        List<FunctionWithDefSourceLine> functionsWithDefSourceLine = new ArrayList<>();
        for (var node : logicGraph.getNodes()) {
            if (NodeType.FUNCTION.equals(node.getType())) {
                List<SourceCodeLine> entries = node.getSourceList();
                for (var entry : entries) {
                    if ((entry.getDefContainer() != null && !entry.getDefContainer().isBlank())) {
                        FunctionWithDefSourceLine functionWithDef = new FunctionWithDefSourceLine(node, entry);
                        functionsWithDefSourceLine.add(functionWithDef);
                    }
                }
            }
        }
        return functionsWithDefSourceLine;
    }

    public List<CoverageTargetAllDefUse> getAllTargetsToBeCoveredByAllDefUse(LogicGraph logicGraph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(logicGraph);
        List<CoverageTargetAllDefUse> result = new ArrayList<>();
        for (var def : allDefsOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(def);
            result.add(target);
        }
        List<FunctionWithUseSourceLine> allUsesOfGraph = graphHelper.getAllUsesOfGraph(logicGraph);
        for (var use : allUsesOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(use);
            result.add(target);
        }
        return result;
    }

    public List<CoverageTargetAllUses> getAllTargetsToBeCoveredByAllUses(LogicGraph logicGraph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(logicGraph);
        List<CoverageTargetAllUses> result = new ArrayList<>();
        for (var def : allDefsOfGraph) {
            List<FunctionWithUseSourceLine> usesOnPath = graphHelper.findAllUsesOfADefOnItsSuccessors(def);
            for (var use : usesOnPath) {
                var defUsePair = new DefUsePair(def, use);
                var coverageTarget = new CoverageTargetAllUses(defUsePair);
                result.add(coverageTarget);
            }
            List<FunctionWithUseSourceLine> usesViaDB = graphHelper.findAllUsesOfFunctionLinesOfADefCoupledByADataStorage(def);
            for (var use : usesViaDB) {
                var defUsePair = new DefUsePair(def, use);
                var coverageTarget = new CoverageTargetAllUses(defUsePair);
                result.add(coverageTarget);
            }
            if (usesOnPath.isEmpty() && usesViaDB.isEmpty()) {
                var defUsePair = new DefUsePair(def, null);
                var coverageTarget = new CoverageTargetAllUses(defUsePair);
                result.add(coverageTarget);
            }
        }
        List<FunctionWithUseSourceLine> allUsesOfGraph = graphHelper.getAllUsesOfGraph(logicGraph);
        for (var use : allUsesOfGraph) {
            var isAlreadyUsed = result.stream().filter(target -> use.equals(target.getCoverageElement().getUse())).findAny();
            if (isAlreadyUsed.isEmpty()) {
                var defUsePair = new DefUsePair(null, use);
                var coverageTarget = new CoverageTargetAllUses(defUsePair);
                result.add(coverageTarget);
            }
        }
        return result;
    }


}
