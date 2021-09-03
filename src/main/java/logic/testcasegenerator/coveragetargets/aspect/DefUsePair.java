package logic.testcasegenerator.coveragetargets.aspect;

public class DefUsePair {
    private final FunctionWithDefSourceLine def;
    private final FunctionWithUseSourceLine use;

    public DefUsePair(FunctionWithDefSourceLine def, FunctionWithUseSourceLine use) {
        this.def = def;
        this.use = use;
    }

    public FunctionWithDefSourceLine getDef() {
        return def;
    }

    public FunctionWithUseSourceLine getUse() {
        return use;
    }


    @Override
    public String toString() {
        String definition = def == null ? "def could not be found for the use" : def.toString();
        String usage = use == null ? "a use could not be found for the definition" : use.toString();

        return "DefUsePair{" +
                "def=" + definition +
                ", use=" + usage +
                '}';
    }

    public String getLogMessage() {
        return String.format("#%s%s",
                def.getSourceCodeLine().getDefTracker("", def.getFunction().getIdentifier()),
                use.getSourceCodeLine().getUseTracker("", use.getFunction().getIdentifier()
                ));
    }

    public String getAspectTarget() {
        return String.format("Coverage of definition %s by use %s",
                def.getSourceCodeLine().getDefTracker("", def.getFunction().getIdentifier()),
                use.getSourceCodeLine().getUseTracker("", use.getFunction().getIdentifier()));
    }
}
