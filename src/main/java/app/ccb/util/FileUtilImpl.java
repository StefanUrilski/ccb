package app.ccb.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtilImpl implements FileUtil {

    @Override
    public String readFile(String filePath) {
        List<String> allLines = new ArrayList<>();
        try {
            allLines = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            allLines.add("Oops, something goes wrong... ");
            e.printStackTrace();
        }

        return String.join(System.lineSeparator(), allLines);
    }
}
