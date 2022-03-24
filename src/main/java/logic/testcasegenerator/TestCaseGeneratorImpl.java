package logic.testcasegenerator;

import logic.model.*;
import logic.testcasegenerator.coveragetargets.*;
import logic.testcasegenerator.coveragetargets.coverageelements.DefUsePair;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithDefSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithSourceLine;
import logic.testcasegenerator.coveragetargets.coverageelements.FunctionWithUseSourceLine;
import logic.testcasegenerator.intermediaresults.*;
import shared.model.AccessMode;
import shared.model.NodeType;

import java.util.*;
import java.util.stream.Collectors;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.*;

public class TestCaseGeneratorImpl implements TestCaseGenerator {
    @Override
    public TestSuite getResourceCoverage(String graphJSON) {
        Graph graph = new Graph(graphJSON);
        graph.addRelationsToElements();

        List<CoverageTargetAllResources> coverageTargets = getAllTargetsToBeCoveredByAllResources(graph);

        for (var target : coverageTargets) {
            var node = target.getCoverageElement();
            List<Testcase> testcases = getTestcasesCoveringNode(node, graph);
            var targetCoverageStatement = List.of(String.format("%s%d%s", RESOURCE_MARKER, node.getIdentifier(), LOGDELIMITER));
            List<Testcase> additionalTestcases = new ArrayList<>();
            for (var testcase : testcases) {
                additionalTestcases.addAll(getTestcasesInfluencingFunction(node, testcase));
            }
            testcases.addAll(additionalTestcases);
            testcases.forEach(tc -> tc.setLogsOfTarget(targetCoverageStatement));
            target.addTestcases(testcases);
        }

        TestSuite testsuite = new TestSuite();
        testsuite.add(coverageTargets);
        testsuite.calculateOracleNodes(graph);
        testsuite.calculateStateNodes(graph);
        return testsuite;
    }

    private List<Testcase> getTestcasesCoveringNode(NodeModel node, Graph graph) {
        List<Testcase> testcases = new ArrayList<>();
        var logStatements = List.of(String.format("%s%d%s", RESOURCE_MARKER, node.getIdentifier(), LOGDELIMITER));

        if (NodeType.FUNCTION.equals(node.getType())) {
            String target = String.format("Coverage of %s", node);
            var serverlessFunction = new ServerlessFunction(node);
            var functions = List.of(serverlessFunction);
            Testcase testcase = new Testcase(functions, target, logStatements);
            testcases.add(testcase);
        } else {
            var allArrows = graph.getArrows();
            List<NodeModel> functionsToBeCalled = getFunctionCallingResource(node, allArrows);
            for (var function : functionsToBeCalled) {
                String target = String.format("Coverage of %s by calling %s", node, function);
                var serverlessFunction = new ServerlessFunction(function);
                var functions = List.of(serverlessFunction);
                Testcase testcase = new Testcase(functions, target, logStatements);
                testcases.add(testcase);
            }
            if (functionsToBeCalled.isEmpty()) {
                String target = String.format("Coverage of resource %s is not possible", node);
                List<ServerlessFunction> functions = List.of();
                Testcase testcase = new Testcase(functions, target, logStatements);
                testcases.add(testcase);
            }
        }
        return testcases;
    }

    private List<CoverageTargetAllResources> getAllTargetsToBeCoveredByAllResources(Graph graph) {
        List<CoverageTargetAllResources> result = new ArrayList<>();
        graph.getNodes().forEach(node -> result.add(new CoverageTargetAllResources(node)));
        return result;
    }

    private List<NodeModel> getFunctionCallingResource(NodeModel resource, List<ArrowModel> arrows) {
        List<ArrowModel> arrowsNotConsidered = new ArrayList<>(arrows);
        Queue<ArrowModel> relationsToBeInvestigated = new LinkedList<>(resource.getIncomingArrows());
        arrowsNotConsidered.removeAll(relationsToBeInvestigated);
        List<NodeModel> result = new ArrayList<>();

        while (!relationsToBeInvestigated.isEmpty()) {
            var incomingArrow = relationsToBeInvestigated.remove();
            var predecessorNode = incomingArrow.getPredecessorNode();
            if (NodeType.FUNCTION.equals(predecessorNode.getType())) {
                result.add(predecessorNode);
            } else {
                for (var arrow : predecessorNode.getIncomingArrows()) {
                    if (arrowsNotConsidered.remove(arrow)) {
                        if (arrow.hasAccessMode(AccessMode.CREATE) || arrow.hasAccessMode(AccessMode.DELETE) || arrow.hasAccessMode(AccessMode.UPDATE)) {
                            relationsToBeInvestigated.add(arrow);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public TestSuite getRelationCoverage(String graphJSON) {
        Graph graph = new Graph(graphJSON);
        graph.addRelationsToElements();

        List<CoverageTargetAllRelations> coverageTargets = getAllTargetsToBeCoveredByAllRelations(graph);

        for (var target : coverageTargets) {
            var arrow = target.getCoverageElement();
            List<Testcase> testcases = getTestCasesCoveringArrow(arrow, graph);

            List<Testcase> additionalTestcases = new ArrayList<>();
            for (var testcase : testcases) {
                additionalTestcases.addAll(getTestcasesInfluencingFunction(arrow.getPredecessorNode(), testcase));
            }
            testcases.addAll(additionalTestcases);

            var targetCoverageStatement = List.of(String.format("%s%d%s", RELATION_MARKER, arrow.getIdentifier(), LOGDELIMITER));
            testcases.forEach(tc -> tc.setLogsOfTarget(targetCoverageStatement));
            target.addTestcases(testcases);
        }

        TestSuite testsuite = new TestSuite();
        testsuite.add(coverageTargets);
        testsuite.calculateOracleNodes(graph);
        testsuite.calculateStateNodes(graph);
        return testsuite;

    }

    private List<Testcase> getTestCasesCoveringArrow(ArrowModel arrow, Graph graph) {
        List<Testcase> testcases = new ArrayList<>();
        List<NodeModel> functions = getFunctionsCallingBinding(arrow, graph);
        var predecessorNode = arrow.getPredecessorNode();
        var successorNode = arrow.getSuccessorNode();
        var logStatements = List.of(String.format("%s%d%s", RELATION_MARKER, arrow.getIdentifier(), LOGDELIMITER));
        if (!functions.isEmpty()) {
            for (var functionNode : functions) {
                String target = String.format("Coverage of relation from %s " + "to %s by calling %s", predecessorNode, successorNode, functionNode);
                var function = new ServerlessFunction(functionNode);
                List<ServerlessFunction> functionsToBeCalled = List.of(function);
                Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
                testcases.add(testcase);
            }
        } else {
            String target = String.format("Coverage of relation from %s " + "to %s is not possible", predecessorNode, successorNode);
            List<ServerlessFunction> functionsToBeCalled = List.of();
            Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
            testcases.add(testcase);
        }
        return testcases;
    }

    private List<CoverageTargetAllRelations> getAllTargetsToBeCoveredByAllRelations(Graph graph) {
        List<CoverageTargetAllRelations> result = new ArrayList<>();
        graph.getArrows().forEach(arrow -> result.add(new CoverageTargetAllRelations(arrow)));
        return result;
    }

    private List<NodeModel> getFunctionsCallingBinding(ArrowModel arrow, Graph graph) {
        List<NodeModel> resultFunctions = new ArrayList<>();
        Queue<ArrowModel> relationsToIterate = new LinkedList<>();
        List<ArrowModel> relationsNotInvestigatedYet = new ArrayList<>(graph.getArrows());
        relationsToIterate.add(arrow);
        relationsNotInvestigatedYet.remove(arrow);
        while (!relationsToIterate.isEmpty()) {
            ArrowModel currentArrow = relationsToIterate.remove();
            NodeModel predecessor = currentArrow.getPredecessorNode();
            if (NodeType.FUNCTION.equals(predecessor.getType()) && !currentArrow.hasAccessMode(AccessMode.RETURN)) {
                resultFunctions.add(predecessor);
            } else {
                var predecessorArrows = predecessor.getIncomingArrows();
                for (var preArrow : predecessorArrows) {
                    if (relationsNotInvestigatedYet.remove(preArrow)) {
                        if (!preArrow.hasAccessMode(AccessMode.READ)) {
                            relationsToIterate.add(preArrow);
                        }
                    }
                }
            }

        }
        return resultFunctions;
    }

    @Override
    public TestSuite getAllDefsCoverage(String graphJSON) {
        Graph graph = new Graph(graphJSON);
        graph.addRelationsToElements();

        List<CoverageTargetAllDefs> coverageTargets = getAllTargetsToBeCoveredByAllDefs(graph);

        for (var target : coverageTargets) {
            var def = target.getCoverageElement();
            List<Testcase> testcases = getTestcasesCoveringUseOfDefinition(def);
            var logStatements = List.of(def.getLogMessage() + USELOG_MARKER);
            testcases.forEach(tc -> tc.setLogsOfTarget(logStatements));
            target.addTestcases(testcases);
        }

        TestSuite testsuite = new TestSuite();
        testsuite.add(coverageTargets);
        testsuite.calculateOracleNodes(graph);
        testsuite.calculateStateNodes(graph);
        return testsuite;
    }


    private List<CoverageTargetAllDefs> getAllTargetsToBeCoveredByAllDefs(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
        List<CoverageTargetAllDefs> lambdaDefs = new ArrayList<>();
        allDefsOfGraph.forEach(def -> lambdaDefs.add(new CoverageTargetAllDefs(def)));
        return lambdaDefs;
    }

    private List<FunctionWithDefSourceLine> getAllDefsOfGraph(Graph graph) {
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


    private List<FunctionWithUseSourceLine> findAllUsesOfADefOnItsSuccessors(FunctionWithSourceLine def) {
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


    private List<FunctionWithUseSourceLine> findAllUsesOfFunctionLinesOfADefCoupledByADataStorage(FunctionWithSourceLine def) {
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

    @Override
    public TestSuite getDefUseCoverage(String graphJSON) {
        Graph graph = new Graph(graphJSON);
        graph.addRelationsToElements();

        List<CoverageTargetAllDefUse> coverageTargets = getAllTargetsToBeCoveredByAllDefUse(graph);
        for (var target : coverageTargets) {
            List<Testcase> testcases = getTestcaseForTarget(target);
            target.addTestcases(testcases);
        }
        TestSuite testsuite = new TestSuite();
        testsuite.add(coverageTargets);
        testsuite.calculateOracleNodes(graph);
        testsuite.calculateStateNodes(graph);
        return testsuite;
    }

    private List<Testcase> getTestcaseForTarget(CoverageTargetAllDefUse targetAllDefUse) {
        List<Testcase> potentialTestcases = new ArrayList<>();

        var coverageTestTarget = targetAllDefUse.getCoverageElement();
        if (coverageTestTarget instanceof FunctionWithDefSourceLine) {
            var testcases = getTestcasesCoveringUseOfDefinition((FunctionWithDefSourceLine) coverageTestTarget);
            var logStatements = List.of(coverageTestTarget.getLogMessage() + USELOG_MARKER);
            testcases.forEach(tc -> tc.setLogsOfTarget(logStatements));
            potentialTestcases.addAll(testcases);
        }
        if (coverageTestTarget instanceof FunctionWithUseSourceLine) {
            var testcases = getTestcasesCoveringDefinitionOfUse((FunctionWithUseSourceLine) coverageTestTarget);
            var logStatements = List.of(DEFLOG_MARKER + coverageTestTarget.getLogMessage());
            testcases.forEach(tc -> tc.setLogsOfTarget(logStatements));
            potentialTestcases.addAll(testcases);
        }
        return potentialTestcases;

    }

    private List<CoverageTargetAllDefUse> getAllTargetsToBeCoveredByAllDefUse(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
        List<CoverageTargetAllDefUse> result = new ArrayList<>();
        for (var def : allDefsOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(def);
            result.add(target);
        }
        List<FunctionWithUseSourceLine> allUsesOfGraph = getAllUsesOfGraph(graph);
        for (var use : allUsesOfGraph) {
            CoverageTargetAllDefUse target = new CoverageTargetAllDefUse(use);
            result.add(target);
        }
        return result;
    }


    private List<Testcase> getTestcasesCoveringDefinitionOfUse(FunctionWithUseSourceLine use) {
        List<FunctionWithDefSourceLine> defsForUseOnPath = findAllDefsOfAUseOnItsPredecessors(use);
        boolean isDefFoundForUseOnPath = defsForUseOnPath.size() > 0;
        List<DefViaDB> defsForUseViaDB = findAllDefsOfAUseCoupledByADataStorage(use);
        boolean isDefFoundForCoupled = defsForUseViaDB.size() > 0;
        var potentialTestcasesForUse = new ArrayList<Testcase>();
        if (isDefFoundForUseOnPath) {
            for (var defOnPath : defsForUseOnPath) {
                var logStatements = List.of(defOnPath.getLogMessage() + use.getLogMessage());
                String defDescription = String.format("def %s", defOnPath);
                String target = String.format("use %s should be covered by %s", use, defDescription);
                ServerlessFunction function = getServerlessFunctionForCoverageOfDef(defOnPath, use);
                List<ServerlessFunction> functionsToBeCalled = List.of(function);
                Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
                potentialTestcasesForUse.add(testcase);
                potentialTestcasesForUse.addAll(getTestcasesInfluencingFunction(defOnPath.getFunction(), testcase));
            }
        }
        if (isDefFoundForCoupled) {
            for (var defCoupledViaDbEntry : defsForUseViaDB) {
                List<Testcase> testcases = defCoupledViaDbEntry.getTestcases(use);
                potentialTestcasesForUse.addAll(testcases);
                List<Testcase> additionalTestcases = new ArrayList<>();
                for (var testcase : testcases) {
                    additionalTestcases.addAll(getTestcasesInfluencingFunction(defCoupledViaDbEntry.getDef().getFunction(), testcase));
                }
                potentialTestcasesForUse.addAll(additionalTestcases);
            }
        }
        if (!isDefFoundForUseOnPath && !isDefFoundForCoupled) {
            var logStatements = List.of(use.getLogMessage());
            String target = String.format("use %s should be covered but no def could be found ", use);
            var function = new ServerlessFunction(use.getFunction());
            List<ServerlessFunction> functionsToBeCalled = List.of(function);
            Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
            potentialTestcasesForUse.add(testcase);
        }


        return potentialTestcasesForUse;
    }


    private List<Testcase> getTestcasesCoveringUseOfDefinition(FunctionWithDefSourceLine def) {

        List<FunctionWithUseSourceLine> usesForDefOnPath = findAllUsesOfADefOnItsSuccessors(def);
        boolean isUseFoundForDef = usesForDefOnPath.size() > 0;
        var usesForDefViaDB = findAllUsesOfADefCoupledByADataStorage(def);
        boolean isUseFoundForCoupled = usesForDefViaDB.size() > 0;

        var potentialTestcasesForDefinition = new ArrayList<Testcase>();
        if (isUseFoundForDef) {
            for (var useOnPath : usesForDefOnPath) {
                var logStatements = List.of(def.getLogMessage() + useOnPath.getLogMessage());
                String useDescription = String.format("use %s ", useOnPath);
                String target = String.format("def %s should be covered by %s", def, useDescription);


                ServerlessFunction function = getServerlessFunctionForCoverageOfDef(def, useOnPath);

                Testcase testcase = new Testcase(List.of(function), target, logStatements);
                potentialTestcasesForDefinition.add(testcase);
            }
        }
        if (isUseFoundForCoupled) {
            for (var useCoupledViaDbEntry : usesForDefViaDB) {
                var testcases = useCoupledViaDbEntry.getTestcases(def);
                potentialTestcasesForDefinition.addAll(testcases);

            }
        }

        if (!isUseFoundForDef && !isUseFoundForCoupled) {
            String target = String.format("def %s  should be covered but no use could be found ", def);
            var logStatements = List.of(def.getLogMessage());
            var function = new ServerlessFunction(def.getFunction());
            List<ServerlessFunction> functionsToBeCalled = List.of(function);
            Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
            potentialTestcasesForDefinition.add(testcase);
        }

        List<Testcase> additionalTestcases = new ArrayList<>();
        for (var testcase : potentialTestcasesForDefinition) {
            additionalTestcases.addAll(getTestcasesInfluencingFunction(def.getFunction(), testcase));
        }
        potentialTestcasesForDefinition.addAll(additionalTestcases);
        return potentialTestcasesForDefinition;
    }


    private List<DefViaDB> findAllDefsOfAUseCoupledByADataStorage(FunctionWithUseSourceLine use) {
        List<ArrowModel> arrows = getSuccessorArrowsToBeConsidered(use.getFunction(), use.getSourceCodeLine().getRelationsInfluencingUse());
        List<DefViaDB> results = new LinkedList<>();
        for (var arrow : arrows) {
            if (arrow.hasAccessMode(AccessMode.READ)) {
                var potentialDB = arrow.getSuccessorNode();
                if (NodeType.DATA_STORAGE.equals(potentialDB.getType())) {
                    List<DefViaDB> defsOfDBviaWrite = DefViaDBViaWrite.parse(potentialDB);
                    List<DefViaDB> defsOfDBviaDelete = DefViaDBViaDelete.parse(potentialDB);
                    List<DefViaDB> defsOfDBviaUpdate = DefViaDBViaUpdate.parse(potentialDB);
                    results.addAll(defsOfDBviaWrite);
                    results.addAll(defsOfDBviaDelete);
                    results.addAll(defsOfDBviaUpdate);
                }
            }
        }
        return results;
    }

    private List<UseViaDB> findAllUsesOfADefCoupledByADataStorage(FunctionWithDefSourceLine def) {
        List<ArrowModel> arrows = getSuccessorArrowsToBeConsidered(def.getFunction(), def.getSourceCodeLine().getRelationsInfluencedByDef());
        List<UseViaDB> results = new LinkedList<>();
        for (var arrow : arrows) {
            if (arrow.hasAccessMode(AccessMode.DELETE) || arrow.hasAccessMode(AccessMode.UPDATE) || arrow.hasAccessMode(AccessMode.CREATE)) {
                var potentialDB = arrow.getSuccessorNode();
                if (NodeType.DATA_STORAGE.equals(potentialDB.getType())) {
                    var readUsagesOfDb = getReadUsageOfDB(potentialDB);
                    if (arrow.hasAccessMode(AccessMode.DELETE)) {
                        for (var readUse : readUsagesOfDb) {
                            results.add(new UseViaDBDelete(readUse, potentialDB));
                        }
                    }
                    if (arrow.hasAccessMode(AccessMode.UPDATE) || (arrow.hasAccessMode(AccessMode.READ) && arrow.hasAccessMode(AccessMode.CREATE))) {
                        for (var readUse : readUsagesOfDb) {
                            results.add(new UseViaDBUpdate(readUse, potentialDB));
                        }
                    } else if (arrow.hasAccessMode(AccessMode.CREATE)) {
                        for (var readUse : readUsagesOfDb) {
                            results.add(new UseViaDBWrite(readUse));
                        }
                    }
                }
            }
        }

        return results;
    }

    private ServerlessFunction getServerlessFunctionForCoverageOfDef(FunctionWithDefSourceLine def, FunctionWithUseSourceLine useOnPath) {
        boolean isSuccessorAlsoPredecessor = false;
        var sourceCodeLinesOfUseNode = useOnPath.getFunction().getSourceList();
        boolean callToDefNodeFromUse = false;
        var outgoingArrows = useOnPath.getFunction().getOutgoingArrows();
        for (var line : sourceCodeLinesOfUseNode) {
            if (!callToDefNodeFromUse && !(line.getRelationsInfluencedByDef() == null)) {
                var idsOfInfluencedArrows = line.getRelationsInfluencedByDef();
                if (idsOfInfluencedArrows == null) {
                    continue;
                }
                var nodesInfluencedByDefOfTheUseNode = outgoingArrows.stream().filter(arrow -> idsOfInfluencedArrows.contains(arrow.getIdentifier())).map(ArrowModel::getSuccessorNode).filter(Objects::nonNull).collect(Collectors.toList());
                if (nodesInfluencedByDefOfTheUseNode.contains(def.getFunction())) {
                    callToDefNodeFromUse = true;
                }
            }
            if (callToDefNodeFromUse) {
                if (line.getRelationsInfluencingUse() != null) {
                    var idsOfInfluencingArrows = line.getRelationsInfluencingUse();
                    if (idsOfInfluencingArrows == null) {
                        continue;
                    }
                    var influencingNodes = useOnPath.getFunction().getIncomingArrows().stream().filter(arrow -> idsOfInfluencingArrows.contains(arrow.getIdentifier())).map(ArrowModel::getPredecessorNode).filter(Objects::nonNull).collect(Collectors.toList());
                    if (influencingNodes.contains(def.getFunction())) {
                        isSuccessorAlsoPredecessor = true;
                        break;
                    }

                }
            }
        }
        return isSuccessorAlsoPredecessor ? new ServerlessFunction(useOnPath.getFunction()) : new ServerlessFunction(def.getFunction());
    }


    private List<FunctionWithDefSourceLine> findAllDefsOfAUseOnItsPredecessors(FunctionWithSourceLine use) {
        List<ArrowModel> arrows = getPreArrowsToBeConsidered(use.getFunction(), use.getSourceCodeLine().getRelationsInfluencingUse());
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        for (var arrow : arrows) {
            var predecessor = arrow.getPredecessorNode();
            if (NodeType.FUNCTION.equals(predecessor.getType())) {
                List<FunctionWithDefSourceLine> usagesForArrow = getDefsInAFunction(predecessor, arrow.getIdentifier());
                result.addAll(usagesForArrow);
            } else if (NodeType.DATA_STORAGE.equals(predecessor.getType())) {
                result.addAll(getDefsBeforeNode(predecessor));

            } else {
                result.addAll(getDefsBeforeNode(predecessor));
            }
        }
        return result;
    }


    private List<FunctionWithDefSourceLine> getDefsBeforeNode(NodeModel node) {
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        var arrows = node.getIncomingArrows();
        Set<Long> visitedNodes = new HashSet<>();
        Queue<ArrowModel> arrowsToBeVisited = new LinkedList<>(arrows);
        while (!arrowsToBeVisited.isEmpty()) {
            var arrow = arrowsToBeVisited.remove();
            if (!arrow.hasAccessMode(AccessMode.READ)) {
                var predecessorNode = arrow.getPredecessorNode();
                if (visitedNodes.add(predecessorNode.getIdentifier())) {
                    if (NodeType.FUNCTION.equals(predecessorNode.getType())) {
                        result.addAll(getDefsInAFunction(predecessorNode, arrow.getIdentifier()));
                    } else {
                        arrowsToBeVisited.addAll(predecessorNode.getIncomingArrows());
                    }
                }
            }
        }
        return result;
    }

    private List<FunctionWithDefSourceLine> getDefsInAFunction(NodeModel node, long idOfArrow) {
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        var defs = node.getSourceList().stream().filter(sourceCodeLine -> (sourceCodeLine.getDefContainer() != null && !sourceCodeLine.getDefContainer().isBlank())).filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencedByDef() != null).filter(sourceCodeLine -> sourceCodeLine.getRelationsInfluencedByDef().contains(idOfArrow) || sourceCodeLine.getRelationsInfluencedByDef().contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)).collect(Collectors.toList());
        defs.forEach(def -> result.add(new FunctionWithDefSourceLine(node, def)));
        return result;
    }

    private List<ArrowModel> getPreArrowsToBeConsidered(NodeModel node, List<Long> relationsInfluencingUse) {
        List<ArrowModel> result;
        if (relationsInfluencingUse.contains(SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT)) {
            result = node.getIncomingArrows();
        } else {
            result = new ArrayList<>();
            for (var relation : node.getIncomingArrows()) {
                if (relationsInfluencingUse.contains(relation.getIdentifier())) {
                    result.add(relation);
                }
            }
        }
        return result;
    }


    @Override
    public TestSuite getAllUsesCoverage(String graphJSON) {
        Graph graph = new Graph(graphJSON);
        graph.addRelationsToElements();

        List<CoverageTargetAllUses> coverageTargets = getAllTargetsToBeCoveredByAllUses(graph);
        for (var target : coverageTargets) {
            List<Testcase> testcases = getTestcaseForTarget(target);
            target.addTestcases(testcases);
        }
        TestSuite testsuite = new TestSuite();
        testsuite.add(coverageTargets);
        testsuite.calculateOracleNodes(graph);
        testsuite.calculateStateNodes(graph);

        return testsuite;
    }

    private List<Testcase> getTestcaseForTarget(CoverageTargetAllUses coverageTargetAllUses) {
        List<Testcase> potentialTestcases = new ArrayList<>();
        var defuse = coverageTargetAllUses.getCoverageElement();
        var defOfTestTarget = defuse.getDef();
        var useOfTestTarget = defuse.getUse();
        if (useOfTestTarget == null) {
            var logStatements = List.of(defOfTestTarget.getLogMessage());
            String target = String.format("def %s should be covered but no use was found", defOfTestTarget);
            var function = new ServerlessFunction(defOfTestTarget.getFunction());
            List<ServerlessFunction> functionsToBeCalled = List.of(function);
            Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
            testcase.setLogsOfTarget(logStatements);
            potentialTestcases.add(testcase);
        } else if (defOfTestTarget == null) {
            var logStatements = List.of(useOfTestTarget.getLogMessage());
            String target = String.format("use %s should be covered but no def was found", useOfTestTarget);
            var function = new ServerlessFunction(useOfTestTarget.getFunction());
            List<ServerlessFunction> functionsToBeCalled = List.of(function);
            Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
            testcase.setLogsOfTarget(logStatements);
            potentialTestcases.add(testcase);
        } else {
            String target = String.format("def %s should be covered by %s ", defOfTestTarget, useOfTestTarget);
            List<FunctionWithUseSourceLine> usesForDefOnPath = findAllUsesOfADefOnItsSuccessors(defOfTestTarget);
            for (var use : usesForDefOnPath) {
                if (useOfTestTarget.equals(use)) {
                    var logStatements = List.of(defOfTestTarget.getLogMessage() + useOfTestTarget.getLogMessage());
                    var function = getServerlessFunctionForCoverageOfDef(defOfTestTarget, useOfTestTarget);
                    List<ServerlessFunction> functionsToBeCalled = List.of(function);
                    Testcase testcase = new Testcase(functionsToBeCalled, target, logStatements);
                    testcase.setLogsOfTarget(logStatements);
                    potentialTestcases.add(testcase);
                }
            }
            var usesForDefViaDB = findAllUsesOfADefCoupledByADataStorage(defOfTestTarget);
            for (var useCoupledViaDbEntry : usesForDefViaDB) {
                if (useOfTestTarget.equals(useCoupledViaDbEntry.getUse())) {
                    var testcases = useCoupledViaDbEntry.getTestcases(defOfTestTarget);
                    var logStatementsOfTarget = List.of(defOfTestTarget.getLogMessage() + useOfTestTarget.getLogMessage());
                    testcases.forEach(tc -> tc.setLogsOfTarget(logStatementsOfTarget));
                    potentialTestcases.addAll(testcases);
                }
            }
        }

        List<Testcase> additionalTestcases = new ArrayList<>();
        for (var testcase : potentialTestcases) {
            var functionWithDefSourceLine = defuse.getDef();
            if (functionWithDefSourceLine != null) {
                additionalTestcases.addAll(getTestcasesInfluencingFunction(functionWithDefSourceLine.getFunction(), testcase));
            }
        }
        potentialTestcases.addAll(additionalTestcases);

        return potentialTestcases;
    }

    private List<CoverageTargetAllUses> getAllTargetsToBeCoveredByAllUses(Graph graph) {
        List<FunctionWithDefSourceLine> allDefsOfGraph = getAllDefsOfGraph(graph);
        List<CoverageTargetAllUses> result = new ArrayList<>();
        for (var def : allDefsOfGraph) {
            List<FunctionWithUseSourceLine> usesOnPath = findAllUsesOfADefOnItsSuccessors(def);
            for (var use : usesOnPath) {
                var defUsePair = new DefUsePair(def, use);
                var coverageTarget = new CoverageTargetAllUses(defUsePair);
                result.add(coverageTarget);
            }
            List<FunctionWithUseSourceLine> usesViaDB = findAllUsesOfFunctionLinesOfADefCoupledByADataStorage(def);
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
        List<FunctionWithUseSourceLine> allUsesOfGraph = getAllUsesOfGraph(graph);
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

    private List<FunctionWithUseSourceLine> getAllUsesOfGraph(Graph graph) {
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

    private List<FunctionWithDefSourceLine> getWriteOnlyNodesOfDB(ArrowModel dbArrow) {
        var db = dbArrow.getSuccessorNode();
        var writeNodeArrows = db.getIncomingArrows().stream().filter(arrow -> arrow.getAccessMode() != null && arrow.getAccessMode().size() == 1
                && arrow.hasAccessMode(AccessMode.CREATE)).collect(Collectors.toList());
        List<FunctionWithDefSourceLine> result = new ArrayList<>();
        for (var writeNodeArrow : writeNodeArrows) {
            var writeNode = writeNodeArrow.getPredecessorNode();
            var writeLines = writeNode.getSourceList().stream()
                    .filter(line -> line.getDefContainer() != null)
                    .filter(line -> line.getRelationsInfluencedByDef().contains(writeNodeArrow.getIdentifier()))
                    .collect(Collectors.toList());
            var writeFunctionsWithLine = writeLines.stream().map(line -> new FunctionWithDefSourceLine(writeNode, line)).collect(Collectors.toList());
            result.addAll(writeFunctionsWithLine);

        }
        return result;
    }

    private List<FunctionWithUseSourceLine> getUsesOfFunctionOnDB(NodeModel function, ArrowModel dbArrow) {
        var usesInDef = function.getSourceList().stream().
                filter(line -> line.getUse() != null)
                .filter(line -> line.getRelationsInfluencingUse().contains(dbArrow.getIdentifier())).collect(Collectors.toList());
        return usesInDef.stream().map(line -> new FunctionWithUseSourceLine(function, line)).collect(Collectors.toList());
    }


    private List<Testcase> getTestcasesInfluencingFunction(NodeModel function, Testcase testcase) {
        if (function.getType() != NodeType.FUNCTION) {
            var firstFunction = testcase.getFunctions().stream().findFirst();
            if (firstFunction.isPresent()) {
                function = firstFunction.get().getNodeModel();
            }
        }
        List<Testcase> additionalTestcases = new ArrayList<>();
        var readArrowsOfDB = function.getOutgoingArrows().stream()
                .filter(arrow -> arrow.hasAccessMode(AccessMode.READ))
                .collect(Collectors.toList());
        for (var dbArrow : readArrowsOfDB) {
            var writeNodes = getWriteOnlyNodesOfDB(dbArrow);
            var useNodesOfDef = getUsesOfFunctionOnDB(function, dbArrow);
            for (var writeNode : writeNodes) {
                for (var useNode : useNodesOfDef) {
                    String additionalTarget = String.format(" ;def %s should be covered by use %s before execution", writeNode, useNode);
                    String testTargetNew = testcase.getTarget() + additionalTarget;
                    var logStatementAdditional = new ArrayList<String>();
                    logStatementAdditional.add(writeNode.getLogMessage() + useNode.getLogMessage());
                    logStatementAdditional.addAll(testcase.getLogsToCover());
                    List<ServerlessFunction> functions = new ArrayList<>();
                    functions.add(new ServerlessFunction(writeNode.getFunction()));
                    functions.addAll(testcase.getFunctions());
                    Testcase additionalTestCase = new Testcase(functions, testTargetNew, logStatementAdditional);
                    additionalTestCase.setLogsOfTarget(testcase.getLogsOfTarget());
                    additionalTestcases.add(additionalTestCase);
                }

            }
        }
        return additionalTestcases;
    }


}
