package logic.testcasegenerator.coveragetargets.coverageelements;

public record DefUsePair(FunctionWithDefSourceLine def,
                         FunctionWithUseSourceLine use) {

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


    public String getCoverageTargetDescription() {
        return String.format("Coverage of definition %s by use %s",
                def.getSourceCodeLine().getDefTracker("", def.getFunction().getIdentifier()),
                use.getSourceCodeLine().getUseTracker("", use.getFunction().getIdentifier()));
    }
}
