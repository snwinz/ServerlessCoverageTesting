package logic.model;

import static logic.testcasegenerator.coveragetargets.LogNameConfiguration.*;

public class ServerlessFunction {
    private final NodeModel nodeModel;
    private final String name;
    private final long id;


    public ServerlessFunction(NodeModel nodeModel) {
        this.name = nodeModel.getNameOfNode();
        this.id = nodeModel.getIdentifier();
        this.nodeModel = nodeModel;
    }

    public FunctionInputFormat getFunctionInputFormat() {
        return nodeModel.getInputFormats();
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getInputFormatString() {
        if (nodeModel == null) {
            return "";
        }
        return nodeModel.getInputFormats().toString();
    }

    public String getLogMessage() {
        return String.format("%s%d_%s%s", DEFLOG_MARKER, id, FUNCTION_MARKER, LOGDELIMITER);
    }

    public NodeModel getNodeModel() {
        return nodeModel;
    }
}
