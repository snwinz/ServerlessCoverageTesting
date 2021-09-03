package logic.model;

public class ServerlessFunction {
    private final NodeModel nodeModel;
    private final String name;
    private final long id;


    public ServerlessFunction(NodeModel nodeModel) {
        this.name = nodeModel.getNameOfNode();
        this.id = nodeModel.getIdentifier();
        this.nodeModel = nodeModel;
    }

    public FunctionInputFormat getFunctionInputFormat(){
            return nodeModel.getInputFormats();
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getInputFormatString(){
        if(nodeModel==null){
            return "";
        }
        return nodeModel.getInputFormats().toString();
    }



}
