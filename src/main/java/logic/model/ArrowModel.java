package logic.model;

import com.google.gson.annotations.Expose;
import shared.model.AccessMode;

import java.util.Set;

public class ArrowModel {
    @Expose
    private long identifier;

    @Expose
    private long successor;
    @Expose
    private long predecessor;
    @Expose
    private Set<AccessMode> accessMode;

    private NodeModel successorNode;
    private NodeModel predecessorNode;

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public long getSuccessor() {
        return successor;
    }

    public void setSuccessor(long successor) {
        this.successor = successor;
    }

    public long getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(long predecessor) {
        this.predecessor = predecessor;
    }

    public void setAccessMode(Set<AccessMode> accessMode) {
        this.accessMode = accessMode;
    }

    public NodeModel getSuccessorNode() {
        return successorNode;
    }

    public void setSuccessorNode(NodeModel successorNode) {
        this.successorNode = successorNode;
    }

    public NodeModel getPredecessorNode() {
        return predecessorNode;
    }

    public void setPredecessorNode(NodeModel predecessorNode) {
        this.predecessorNode = predecessorNode;
    }

    public boolean hasAccessMode(AccessMode mode) {
        if (accessMode != null) {
            return accessMode.contains(mode);
        } else {
            return false;
        }
    }

    public Set<AccessMode> getAccessMode() {
        return accessMode;
    }

    @Override
    public String toString() {
        return "Arrow{" +
                "identifier=" + identifier +
                '}';
    }
}
