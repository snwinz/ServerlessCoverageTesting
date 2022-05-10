package logic.testcasegenerator;

import logic.model.Graph;
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

    public List<CoverageTargetAllResources> getAllTargetsToBeCoveredByAllResources(Graph graph) {
        List<CoverageTargetAllResources> result = new ArrayList<>();
        graph.getNodes().forEach(node -> result.add(new CoverageTargetAllResources(node)));
        return result;
    }


    public List<CoverageTargetAllRelations> getAllTargetsToBeCoveredByAllRelations(Graph graph) {
        List<CoverageTargetAllRelations> result = new ArrayList<>();
        graph.getArrows().forEach(arrow -> result.add(new CoverageTargetAllRelations(arrow)));
        return result;
    }

    public List<CoverageTargetAllDefs> getAllTargetsToBeCoveredByAllDefs(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
        List<CoverageTargetAllDefs> lambdaDefs = new ArrayList<>();
        allDefsOfGraph.forEach(def -> lambdaDefs.add(new CoverageTargetAllDefs(def)));
        return lambdaDefs;
    }

    public List<FunctionWithDefSourceLine> getAllDefsOfGraph(Graph graph) {
        List<FunctionWithDefSourceLine> functionsWithDefSourceLine = new ArrayList<>();
        for (var node : graph.getNodes()) {
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

    public List<CoverageTargetAllDefUse> getAllTargetsToBeCoveredByAllDefUse(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
        List<CoverageTargetAllDefUse> result = new ArrayList<>();
        for (var def : allDefsOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(def);
            result.add(target);
        }
        List<FunctionWithUseSourceLine> allUsesOfGraph = graphHelper.getAllUsesOfGraph(graph);
        for (var use : allUsesOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(use);
            result.add(target);
        }
        return result;
    }

    public List<CoverageTargetAllUses> getAllTargetsToBeCoveredByAllUses(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
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
        List<FunctionWithUseSourceLine> allUsesOfGraph = graphHelper.getAllUsesOfGraph(graph);
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
