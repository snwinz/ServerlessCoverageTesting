package gui.controller;

import gui.view.FunctionInputFormatView;
import gui.view.StandardPresentationView;
import logic.model.FunctionInputFormat;

public class FunctionInputFormatViewController {
    private FunctionInputFormatView view;

    public FunctionInputFormatViewController() {


    }

    public void setup(  FunctionInputFormat inputFormats) {
        this.view = new FunctionInputFormatView(inputFormats,  this);
        view.setMaximized(true);
        view.show();
    }


    public void closeView() {
        if (view != null) {
            view.close();
        }
    }

    public void showPotentialInputWithContent(FunctionInputFormat functionInputFormat) {
        String jsonText = functionInputFormat.getJSONWithNewContent();
        StandardPresentationView view = new StandardPresentationView("Potential input");
        view.setText(jsonText);
        view.show();
    }
}
