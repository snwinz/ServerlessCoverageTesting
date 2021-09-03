package gui.model;

public enum AccessMode {
    READ("read"), WRITE("write"), DELETE("delete"), RETURN("return"), FUNCTIONCALL("function call");

    private final String mode;

    AccessMode(final String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
}
