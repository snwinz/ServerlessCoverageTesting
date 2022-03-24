package gui.view.wrapper;

import gui.model.SourceCodeLine;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;

public class SourceEntryWrapper {
    private final SourceCodeLine sourceEntry;
    private final RelationsWrapper relationsDefs;
    private final RelationsWrapper relationsUses;
    private final NodeWrapper nodesCallableForAllNodes;
    private final RelationsWrapper relationsCallableForAllRelations;


    public SourceEntryWrapper(SourceCodeLine sourceEntry, List<Long> neighbours, List<Long> nodes, List<Long> arrows) {
        List<Long> neighboursWithAllIncluded = new ArrayList<>(neighbours);
        neighboursWithAllIncluded.add(0, logic.model.SourceCodeLine.INFLUENCING_ALL_RELATIONS_CONSTANT);
        this.relationsDefs = new RelationsWrapper(neighboursWithAllIncluded);
        this.relationsUses = new RelationsWrapper(neighboursWithAllIncluded);
        this.relationsCallableForAllRelations = new RelationsWrapper(arrows);
        this.nodesCallableForAllNodes = new NodeWrapper(nodes);
        this.sourceEntry = sourceEntry;
        setupComboboxes();
        activateCheckboxesForData(sourceEntry);

    }

    private void setupComboboxes() {
        relationsDefs.setupCombobox();
        relationsUses.setupCombobox();
        relationsCallableForAllRelations.setupCombobox();
        nodesCallableForAllNodes.setupCombobox();
    }

    private void activateCheckboxesForData(SourceCodeLine sourceEntry) {
        relationsDefs.activateNeighbours(sourceEntry.getRelationsInfluencedByDef());
        relationsUses.activateNeighbours(sourceEntry.getRelationsInfluencingUse());
        relationsCallableForAllRelations.activateNeighbours(sourceEntry.getRelationsCoveredByStatement());
        nodesCallableForAllNodes.activateNodes(sourceEntry.getNodesCoveredByStatement());
    }

    public SourceEntryWrapper(SourceCodeLine sourceEntry) {
        this(sourceEntry, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public String getSourceLine() {
        return sourceEntry.getSourceLine();
    }


    public String getDefContainer() {
        return sourceEntry.getDefContainer();
    }

    public String getUse() {
        return sourceEntry.getUse();
    }

    public void setDefContainer(String jsonKey) {
        sourceEntry.setDefContainer(jsonKey);
    }

    public String getReplaceLine() {
        return sourceEntry.getReplaceLine();
    }

    public void setReplaceLine(String replaceLine){
        sourceEntry.setReplaceLine(replaceLine);
    }

    public void setUse(String use) {
        sourceEntry.setUse(use);
    }


    public SourceCodeLine getSourceEntry() {
        List<Long> relationsInfluencedByDef = relationsDefs.getSelectedRelations();
        List<Long> relationsInfluencedByUse = relationsUses.getSelectedRelations();
        List<Long> nodesInfluencedByStatement = nodesCallableForAllNodes.getSelectedNodes();
        List<Long> relationsCalledByStatement = relationsCallableForAllRelations.getSelectedRelations();
        sourceEntry.setRelationsInfluencedByDef(relationsInfluencedByDef);
        sourceEntry.setRelationsInfluencingUse(relationsInfluencedByUse);
        sourceEntry.setNodesCoveredByStatement(nodesInfluencedByStatement);
        sourceEntry.setRelationsCoveredByStatement(relationsCalledByStatement);
        return sourceEntry;
    }

    public ComboBox<ComboBoxItemWrap<String>> getRelationsDefs() {
        return relationsDefs.getCombobox();
    }

    public ComboBox<ComboBoxItemWrap<String>> getRelationsUses() {
        return relationsUses.getCombobox();
    }

    public ComboBox<ComboBoxItemWrap<String>> getNodesCallableForAllNodes() {
        return nodesCallableForAllNodes.getCombobox();
    }

    public ComboBox<ComboBoxItemWrap<String>> getRelationsCallableForAllRelations() {
        return relationsCallableForAllRelations.getCombobox();
    }

    public RelationsWrapper getDefWrapper() {
        return relationsDefs;
    }

    public RelationsWrapper getUseWrapper() {
        return relationsUses;
    }


}
