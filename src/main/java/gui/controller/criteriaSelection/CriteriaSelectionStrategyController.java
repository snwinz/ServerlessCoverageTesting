package gui.controller.criteriaSelection;

public interface CriteriaSelectionStrategyController {
    void handleInput(boolean isAllResources, boolean isAllRelations, boolean isAllDefs, boolean isAllDefUse, boolean isAllUses);
    void cancel();
    void setup();
}
