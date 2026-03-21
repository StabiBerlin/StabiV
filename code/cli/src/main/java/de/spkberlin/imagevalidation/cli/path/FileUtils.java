package de.spkberlin.imagevalidation.cli.path;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileUtils {

    public static Map<Path, List<Path>> mapFilesByDirectory(List<Path> files) {
        Map<Path, List<Path>> filesByDirectory = new TreeMap<>();
        for (Path path : files) {
            if (Files.isDirectory(path)) {
                // path is directory
                if (!filesByDirectory.containsKey(path)) {
                    filesByDirectory.put(path, new ArrayList<>());
                }
            } else {
                // path is file
                Path parent = path.getParent();
                if (!filesByDirectory.containsKey(parent)) {
                    filesByDirectory.put(parent, new ArrayList<>());
                }
                filesByDirectory.get(parent).add(path);
            }
        }
        return filesByDirectory;
    }
}
