package gui.view.wrapper;

public class Commands {


    private String functionInvocation = "";
    private String logRetrieval ="";
    private String dataStorageClearing ="";
    private String projectDirectory ="";
    private boolean localExecution;

    public String getFunctionInvocation() {
        return functionInvocation;
    }

    public void setFunctionInvocation(String functionInvocation) {
        this.functionInvocation = functionInvocation;
    }

    public String getLogRetrieval() {
        return logRetrieval;
    }

    public void setLogRetrieval(String logRetrieval) {
        this.logRetrieval = logRetrieval;
    }

    public String getDataStorageClearing() {
        return dataStorageClearing;
    }

    public void setDataStorageClearing(String dataStorageClearing) {
        this.dataStorageClearing = dataStorageClearing;
    }

    public String getProjectDirectory() {
        return  this.projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void setLocalExeuction(boolean selected) {
        this.localExecution = true;
    }

    public boolean isLocalExecution() {
        return localExecution;
    }
}
