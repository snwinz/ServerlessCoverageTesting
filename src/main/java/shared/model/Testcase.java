package shared.model;


import java.util.List;
import java.util.Objects;

public final class Testcase {
    private final List<Function> functions;
    private final List<String> coverageLogs;
    private final String target;

    public Testcase(List<Function> functions, List<String> coverageLogs, String target) {
        this.functions = functions;
        this.coverageLogs = coverageLogs;
        this.target = target;
    }

    public List<Function> functions() {
        return functions;
    }

    public String target() {
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Testcase) obj;
        return Objects.equals(this.functions, that.functions) &&
                Objects.equals(this.coverageLogs, that.coverageLogs) &&
                Objects.equals(this.target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functions, coverageLogs, target);
    }

    @Override
    public String toString() {
        return "Testcase[" +
                "functions=" + functions + ", " +
                "coverageLogs=" + coverageLogs + ", " +
                "target=" + target + ']';
    }


}
