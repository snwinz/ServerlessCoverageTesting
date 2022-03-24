package shared.model;

public enum AccessMode {
    READ("read","r"), CREATE("create","c"), DELETE("delete", "d"),
    RETURN("return", "r"), FUNCTIONCALL("function call", "fc"), UPDATE("update", "u" );

    private final String mode;
    private final String abbreviation;

    AccessMode(final String mode, final String abbreviation) {
        this.mode = mode;
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return mode;
    }

    public String toShortString() {
        return abbreviation;
    }
}
