package logic.model;

public class LogLine {
    private final String commentForTarget;
    private String logMessage;
    private String beforeLogStatement;
    private String afterLogStatement;

    public LogLine(String commentForTarget) {
        this.commentForTarget = commentForTarget;
    }


    @Override
    public String toString() {
        String executionCodeBeforeLog = beforeLogStatement == null ? "" : "\n" + beforeLogStatement;
        String executionCodeAfterLog = afterLogStatement == null ? "" : "\n" + afterLogStatement;
        String logMessageToBePrinted = logMessage == null ? "" : "\n" + String.format("console.log('%s');", logMessage);

        return "/*#" + commentForTarget +  "*/" +
                executionCodeBeforeLog +
                logMessageToBePrinted +
                executionCodeAfterLog +
                "\n//End of instrumentation insertion";
    }

    public void setBeforeLogStatement(String beforeLogStatement) {
        this.beforeLogStatement = beforeLogStatement;

    }

    public void setAfterLogStatement(String afterLogStatement) {
        this.afterLogStatement = afterLogStatement;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
