package gui.model;

import com.google.gson.annotations.Expose;
import shared.model.AccessMode;

import java.util.Set;

public class Arrow {

    @Expose
    private long identifier;
    @Expose
    private double originalStartOffsetPositionX = 0;
    @Expose
    private double originalStartOffsetPositionY = 0;
    @Expose
    private double originalEndOffsetPositionX = 0;
    @Expose
    private double originalEndOffsetPositionY = 0;

    @Expose
    private long successor;
    @Expose
    private long predecessor;

    private static long idCounter = 0;

    @Expose
    private Set<AccessMode> accessMode;

    public Arrow() {
        identifier = idCounter;
        idCounter++;
    }


    public double getOriginalStartOffsetPositionX() {
        return originalStartOffsetPositionX;
    }

    public void setOriginalStartOffsetPositionX(double originalStartOffsetPositionX) {
        this.originalStartOffsetPositionX = originalStartOffsetPositionX;
    }

    public double getOriginalStartOffsetPositionY() {
        return originalStartOffsetPositionY;
    }

    public void setOriginalStartOffsetPositionY(double originalStartOffsetPositionY) {
        this.originalStartOffsetPositionY = originalStartOffsetPositionY;
    }

    public double getOriginalEndOffsetPositionX() {
        return originalEndOffsetPositionX;
    }

    public void setOriginalEndOffsetPositionX(double originalEndOffsetPositionX) {
        this.originalEndOffsetPositionX = originalEndOffsetPositionX;
    }

    public double getOriginalEndOffsetPositionY() {
        return originalEndOffsetPositionY;
    }

    public void setOriginalEndOffsetPositionY(double originalEndOffsetPositionY) {
        this.originalEndOffsetPositionY = originalEndOffsetPositionY;
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

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        if (identifier + 1 >= idCounter) {
            idCounter = identifier + 1;
        } else {
            idCounter--;
        }
        this.identifier = identifier;
    }

    public static void resetCounter() {
        idCounter = 0;
    }


    public void setAccessMode(Set<AccessMode> accessMode) {
        this.accessMode = accessMode;
    }

    public Set<AccessMode> getAccessMode() {
        return accessMode;
    }


}
