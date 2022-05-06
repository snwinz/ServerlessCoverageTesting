package gui.model;

import shared.model.Function;
import shared.model.Testcase;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class TestcasesContainer {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Testcase> testcases;

    public TestcasesContainer(List<Testcase> testcases) {
        this.testcases = testcases;
    }

    public void addFunctionToTestcase(Testcase testcase) {
        for(var tc : testcases){
            if(tc == testcase ){
                tc.addFunction(new Function("myFunction", "myParameter"));
            }
        }
        pcs.firePropertyChange("functionAdded", null, testcase);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public List<Testcase> getTestcases() {
        return testcases;
    }
}
