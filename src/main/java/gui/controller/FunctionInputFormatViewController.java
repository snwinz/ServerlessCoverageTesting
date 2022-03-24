package gui.controller;

import gui.model.FunctionInputFormat;
import gui.view.FunctionInputFormatView;
import gui.view.StandardPresentationView;

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

    public void showPotentialInput(FunctionInputFormat functionInputFormat) {
        String jsonText = functionInputFormat.getJSON();
        StandardPresentationView view = new StandardPresentationView("Potential input");
        view.setText(jsonText);
        view.show();
    }

    public void showPotentialInputWithContent(FunctionInputFormat functionInputFormat) {
        String jsonText = functionInputFormat.getJSONWithContent();
        StandardPresentationView view = new StandardPresentationView("Potential input");
        view.setText(jsonText);
        view.show();
    }
}
