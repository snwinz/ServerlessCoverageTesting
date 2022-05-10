package logic.testcasegenerator;

import logic.model.ArrowModel;
import logic.model.Graph;
import logic.model.NodeModel;
import logic.model.SourceCodeLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;
import shared.model.AccessMode;
import shared.model.NodeType;

import java.util.*;
import java.util.stream.Collectors;

public class GraphHelper {

    List<FunctionWithUseSourceLine> getAllUsesOfGraph(Graph graph) {
        List<FunctionWithUseSourceLine> functionsWithUseSourceLine = new ArrayList<>();
        for (var node : graph.getNodes()) {
            if (NodeType.FUNCTION.equals(node.getType())) {
                List<SourceCodeLine> entries = node.getSourceList();
                for (var entry : entries) {
                    if (entry.getUse() != null && !entry.getUse().isBlank()) {
                        FunctionWithUseSourceLine functionWithUse = new FunctionWithUseSourceLine(node, entry);
                        functionsWithUseSourceLine.add(functionWithUse);
                    }
                }
            }
        }
        return functionsWithUseSourceLine;
    }

    List<FunctionWithUseSourceLine> findAllUsesOfADefOnItsSuccessors(FunctionWithSourceLine def) {
        List<ArrowModel> arrows = getSuccessorArrowsToBeConsidered(def.getFunction(), def.getSourceCodeLine().getRelationsInfluencedByDef());
        List<FunctionWithUseSourceLine> result = new ArrayList<>();
        for (var arrow : arrows) {
            var successor = arrow.getSuccessorNode();
            if (NodeType.FUNCTION.equals(successor.getType())) {
                List<FunctionWithUseSourceLine> usagesForArrow = getUsesInAFunction(successor, arrow.getIdentifier());
                result.addAll(usagesForArrow);
            } else if (NodeType.DATA_STORAGE.equals(successor.getType())) {
                result.addAll(getUsagesAfterNode(successor));

            } else {
                result.addAll(getUsagesAfterNode(successor));
            }
        }
        return result;
    }

    List<FunctionWithUseSourceLine> findAllUsesOfFunctionLinesOfADefCoupledByADataStorage(FunctionWithSourceLine def) {
        List<ArrowModel> arrows = getSuccessorArrowsToBeConsidered(def.getFunction(), def.getSourceCodeLine().getRelationsInfluencedByDef());
        List<FunctionWithUseSourceLine> result = new ArrayList<>();
        for (var arrow : arrows) {
            if (arrow.hasAccessMode(AccessMode.DELETE) || arrow.hasAccessMode(AccessMode.UPDATE) || arrow.hasAccessMode(AccessMode.CREATE)) {
                var successor = arrow.getSuccessorNode();
                if (NodeType.DATA_STORAGE.equals(successor.getType())) {
                    result.addAll(getReadUsageOfDB(successor));
                }
            }
        }
        return result;
    }

    private List<ArrowModel> getSuccessorArrowsToBeConsidered(NodeModel node, List<Long> relationsInfluenced) {
        List<ArrowModel> result;
        if (relationsInfluenced.contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)) {
            result = node.getOutgoingArrows();
        } else {
            result = new ArrayList<>();
            for (var relation : node.getOutgoingArrows()) {
                if (relationsInfluenced.contains(relation.getIdentifier())) {
                    result.add(relation);
                }
            }
        }
        return result;
    }

    private List<FunctionWithUseSourceLine> getReadUsageOfDB(NodeModel dbNode) {
        List<FunctionWithUseSourceLine> result = new ArrayList<>();
        var arrows = dbNode.getIncomingArrows();
        for (var arrow : arrows) {
            var predecessor = arrow.getPredecessorNode();
            if (arrow.hasAccessMode(AccessMode.READ) && NodeType.FUNCTION.equals(predecessor.getType())) {
                result.addAll(getUsesInAFunction(predecessor, arrow.getIdentifier()));
            }
        }
        return result;
    }

    private List<FunctionWithUseSourceLine> getUsesInAFunction(NodeModel node, long idOfArrow) {
        List<FunctionWithUseSourceLine> result = new ArrayList<>();
        var uses = node.getSourceList().stream().filter(sourceCodeLine -> sourceCodeLine.getUse() != null && !sourceCodeLine.getUse().isBlank()).filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencingUse() != null).filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencingUse().contains(idOfArrow) || sourceCodeLine.getRelationsInfluencingUse().contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)).collect(Collectors.toList());
        uses.forEach(use -> result.add(new FunctionWithUseSourceLine(node, use)));
        return result;
    }

    private List<FunctionWithUseSourceLine> getUsagesAfterNode(NodeModel node) {
        List<FunctionWithUseSourceLine> result = new ArrayList<>();
        var arrows = node.getOutgoingArrows();
        Set<Long> visitedNodes = new HashSet<>();
        Queue<ArrowModel> arrowsToBeVisited = new LinkedList<>(arrows);
        while (!arrowsToBeVisited.isEmpty()) {
            var arrow = arrowsToBeVisited.remove();
            var successorNode = arrow.getSuccessorNode();
            if (visitedNodes.add(successorNode.getIdentifier())) {
                if (NodeType.FUNCTION.equals(successorNode.getType())) {
                    result.addAll(getUsesInAFunction(successorNode, arrow.getIdentifier()));
                } else {
                    arrowsToBeVisited.addAll(successorNode.getOutgoingArrows());
                }

            }
        }
        return result;
    }
}
