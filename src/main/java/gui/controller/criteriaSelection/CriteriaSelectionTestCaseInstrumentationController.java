package gui.controller.criteriaSelection;

import gui.model.SourceCode;
import gui.view.StandardPresentationView;
import gui.view.criteriaSelection.CriteriaSelectionView;
import logic.instrumentation.SourceInstrumentator;
import logic.instrumentation.SourceInstrumentatorImpl;

public class CriteriaSelectionTestCaseInstrumentationController implements CriteriaSelectionStrategyController {

    private final SourceCode model;
    private CriteriaSelectionView view;

    public CriteriaSelectionTestCaseInstrumentationController(SourceCode model) {
        this.model = model;
    }


    @Override
    public void setup() {
        this.view = new CriteriaSelectionView("Criteria selection", this);
        view.showAndWait();
    }

    @Override
    public void cancel() {
        view.close();
    }

    @Override
    public void handleInput(boolean isAllResources, boolean isAllRelations, boolean isAllDefs, boolean isAllDefUse, boolean isAllUses) {

        SourceInstrumentator instrumentator = new SourceInstrumentatorImpl();

        String sourceAsJSON = model.getJSON();
        boolean defUse = isAllDefs || isAllDefUse || isAllUses;
        String instrumentedSourceCode = instrumentator.instrumentSourceCode(sourceAsJSON, isAllResources, isAllRelations, defUse);
        StandardPresentationView tcView = new StandardPresentationView("Instrumentation", instrumentedSourceCode);
        tcView.show();
        view.close();
    }
}