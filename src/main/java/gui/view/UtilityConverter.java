package gui.view;

import gui.model.SourceCodeLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class UtilityConverter {
    public static List<SourceCodeLine> getTableItems(File file) {
        List<SourceCodeLine> tableItems = new LinkedList<>();
        if (file != null) {
            try {
                try (var reader = Files.newBufferedReader(file.toPath())) {
                    String line = reader.readLine();
                    long lineNumber = 0;
                    while (line != null) {
                        SourceCodeLine sourceEntry = new SourceCodeLine();
                        sourceEntry.setSourceLine(line);
                        sourceEntry.setLineNumber(lineNumber++);
                        tableItems.add(sourceEntry);
                        line = reader.readLine();
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not read file: " + e.getMessage());
            }
        }
        return  tableItems;
    }
}
