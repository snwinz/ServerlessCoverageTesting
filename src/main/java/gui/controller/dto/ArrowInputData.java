package gui.controller.dto;

import gui.model.AccessMode;

public class ArrowInputData {
    private long successor;
    private long predecessor;

    private long id = -1;
    private double originalStartOffsetPositionX = 0;
    private double originalStartOffsetPositionY = 0;
    private double originalEndOffsetPositionX = 0;
    private double originalEndOffsetPositionY = 0;
    private AccessMode accessMode;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSuccessor() {
        return successor;
    }

    public long getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(long predecessor) {
        this.predecessor = predecessor;
    }
    public ArrowInputData() {
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

    public void setSuccessor(long successor) {
        this.successor = successor;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }
}
