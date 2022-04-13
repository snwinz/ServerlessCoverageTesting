package shared.model;

import javafx.beans.property.BooleanProperty;

import java.util.List;

public record Testcase(List<Function> functions, List<String> coverageLogs, String target) {


}