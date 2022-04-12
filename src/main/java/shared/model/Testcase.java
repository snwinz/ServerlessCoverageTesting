package shared.model;

import java.util.List;

public record Testcase(List<Function> functions, List<String> coverageLogs, String target) {


}
