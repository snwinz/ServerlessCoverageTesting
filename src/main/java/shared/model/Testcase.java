package shared.model;


import java.util.*;

public final class Testcase {
    private final List<Function> functions;
    private final List<String> coverageLogs = new ArrayList<>();
    private final String target;
    private final List<String> expectedLogs = new ArrayList<>();
    private final Set<String> authKeys = new HashSet<>();
    private boolean manualCreated = true;

    public Testcase(List<Function> functions, List<String> coverageLogs, String target) {
        this.functions = functions;
        this.coverageLogs.clear();
        this.coverageLogs.addAll(coverageLogs);
        this.target = target;
    }

    public List<String> getCoverageLogs() {
        return coverageLogs;
    }

    public void setCoverageLogs(List<String> coverageLogs) {
        this.coverageLogs.clear();
        this.coverageLogs.addAll(coverageLogs);
    }


    public List<Function> getFunctions() {
        return functions;
    }

    public List<String> getExpectedLogs() {
        return List.copyOf(expectedLogs);
    }

    public Set<String> getAuthKeys() {
        return authKeys;
    }

    public void addAuthKeys(Set<String> authKeys) {
        this.authKeys.addAll(authKeys);
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
        return "Testcase{" +
                "functions=" + functions +
                ", coverageLogs=" + coverageLogs +
                ", target='" + target + '\'' +
                ", expectedLogs=" + expectedLogs +
                ", authKeys=" + authKeys +
                ", manualCreated=" + manualCreated +
                '}';
    }

    public void addFunction(Function function) {
        this.functions.add(function);
    }

    public List<String> getLogsToBeCovered() {
        return List.copyOf(expectedLogs);
    }


    public void setExpectedLogOutput(String newValue) {
        var parts = newValue.split("\\*");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (part.endsWith("\\")) {
                parts[i + 1] = part.substring(0, part.length() - 1) + "*" + parts[i + 1];
                parts[i] = null;
            }
        }
        expectedLogs.clear();
        expectedLogs.addAll(Arrays.stream(parts).filter(Objects::nonNull).filter(entry -> !"".equals(entry.trim())).toList());
    }

    public void setExpectedLogOutput(List<String> calibratedLogs) {
        expectedLogs.clear();
        expectedLogs.addAll(calibratedLogs);
    }

    public void setManualCreated(Boolean manualCreated) {
        this.manualCreated = manualCreated;
    }

    public boolean isManualCreated() {
        return manualCreated;
    }


}
