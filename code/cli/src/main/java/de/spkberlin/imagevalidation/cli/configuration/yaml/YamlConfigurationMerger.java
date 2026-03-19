package de.spkberlin.imagevalidation.cli.configuration.yaml;

import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class YamlConfigurationMerger {

    public static void mergeInput(
        YamlConfiguration config,
        Path cwd,
        Path configPath,
        Path baseDirectory,
        List<String> includes,
        List<String> excludes
    ) {
        if (Objects.nonNull(baseDirectory)) {
            // cli option overwrites everything
            config.getInput().setBaseDirectory(baseDirectory);
        } else if (Objects.isNull(config.getInput().getBaseDirectory())) {
            // default value for base directory
            if (Objects.nonNull(configPath)) {
                // use parent of config filepath
                config.getInput().setBaseDirectory(configPath.getParent());
            } else {
                // fallback to current working directory
                config.getInput().setBaseDirectory(cwd);
            }
        }
        if (Objects.nonNull(includes)) {
            config.getInput().getIncludes().addAll(includes);
        }
        if (Objects.nonNull(excludes)) {
            config.getInput().getExcludes().addAll(excludes);
        }
    }

    public static void mergeExtraction(YamlConfiguration config, List<ExtractorType> modules) {
        if (Objects.nonNull(modules) && !modules.isEmpty()) {
            config.getExtraction().setModules(modules);
        }
    }

    public static void mergeOutput(YamlConfiguration config, String csvPath) {
        if (Objects.nonNull(csvPath) && !csvPath.isEmpty()) {
            config.getOutput().getCsv().setPath(csvPath);
        }
    }
}
