package de.spkberlin.imagevalidation.cli.path;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileUtils {

    public static Map<Path, List<Path>> mapFilesByDirectory(List<Path> files) {
        return files
            .stream()
            .collect(Collectors.groupingBy(Path::getParent, Collectors.mapping(p -> p.getFileName(), Collectors.toList())));
    }
}
