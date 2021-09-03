package gui.controller;

import gui.controller.criteriaSelection.CriteriaSelectionStrategyController;
import gui.controller.criteriaSelection.CriteriaSelectionTestCaseInstrumentationController;
import gui.controller.dto.NodeInputData;
import gui.model.Graph;
import gui.model.SourceCode;
import gui.model.SourceCodeLine;
import gui.view.NodeEditorView;
import gui.view.UtilityConverter;
import gui.view.graphcomponents.DraggableNode;
import logic.sourcecodeanalyzer.Analyzer;

import java.io.File;
import java.util.List;

public class NodeEditorController {
    private final Graph model;
    private NodeEditorView view = null;

    public NodeEditorController(Graph model) {
        this.model = model;
    }

    public void updateNodeToGraph(NodeInputData infos) {
        model.updateNode(infos);
        if (view != null) {
            view.close();
        }
    }

    public void setup(DraggableNode draggableNode) {
        view = new NodeEditorView(this, draggableNode, model);
        view.setup();
    }

    public void setSourceFile(File file) {
        var tableItems = UtilityConverter.getTableItems(file);
        view.setDataForTable(tableItems);
    }

    public void instrumentSourceCode(List<SourceCodeLine> sourceListUnwrapped, Long idOfNode) {

        CriteriaSelectionStrategyController controller = new CriteriaSelectionTestCaseInstrumentationController(new SourceCode(sourceListUnwrapped, idOfNode));
        controller.setup();
    }

    public void analyzeSource(List<SourceCodeLine> sourceListUnwrapped, Long idOfNode, NodeEditorView nodeEditorView, boolean adaptForDeletes) {
        var sourceCode = new SourceCode(sourceListUnwrapped, idOfNode);
        String sourceAsJSON = sourceCode.getJSON();

        Analyzer analyzer = new Analyzer(adaptForDeletes);
        String sourceAnalyzedAsJson = analyzer.getSuggestionForInstrumentation(sourceAsJSON, model.getJSON());
        SourceCode resultSourceCode = SourceCode.getSourceCodeObject(sourceAnalyzedAsJson);
        nodeEditorView.setDataForTable(resultSourceCode.getSourceCode());
    }
}
