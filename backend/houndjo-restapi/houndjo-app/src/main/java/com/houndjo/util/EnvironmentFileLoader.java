package com.houndjo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class EnvironmentFileLoader {

    private static final String COMMENT_LINE_START_SYMBOL = "#";
    private static final String VARIABLE_AFFECTATION_OPERATOR = "=";

    public static void load() {
        try {
            Optional<Path> optionalPath = findEnvFile(3);
            if (optionalPath.isEmpty()) {
                log.warn("No environment file found. If you are on dev, create your own .env file");
                return;
            }

            Map<String, String> envProperties = buildEnvironmentVariables(optionalPath.get());
            addVariableToSystemEnvironmentVariable(envProperties);
        } catch (IOException e) {
            log.error("Unable to load any .env file. It should only be possible in production.");
        }
    }

    public static Optional<Path> findEnvFile(int maxDepth) throws IOException {
        Path startDir = Paths.get(System.getProperty("user.dir"));

        try (Stream<Path> paths = Files.walk(startDir, maxDepth)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> Strings.CS.contains(path.toString(), "backend")
                            && Strings.CS.startsWith(path.getFileName().toString(), ".env")
                            && !Strings.CS.equals(path.getFileName().toString(), ".env.example"))
                    .findFirst();
        }
    }

    private static void addVariableToSystemEnvironmentVariable(Map<String, String> envProperties) {
        for (Map.Entry<String, String> entry : envProperties.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue()); // Sets as system properties
        }
    }

    private static Map<String, String> buildEnvironmentVariables(Path path) throws IOException {
        Map<String, String> envProperties = new HashMap<>();
        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            if (!shouldSkipLine(line)) {
                String[] parts = line.split(VARIABLE_AFFECTATION_OPERATOR, 2);
                String key = parts[0].trim();
                String valuePart = parts[1].trim();
                String value;

                if (valuePart.startsWith("'")) {
                    int endIndex = valuePart.indexOf('\'', 1);
                    value = (endIndex != -1) ? valuePart.substring(1, endIndex) : valuePart;
                } else if (valuePart.startsWith("\"")) {
                    int endIndex = valuePart.indexOf('"', 1);
                    value = (endIndex != -1) ? valuePart.substring(1, endIndex) : valuePart;
                } else {
                    // Remove the comment part and unquote the value
                    value = StringUtils.unwrap(valuePart.split(COMMENT_LINE_START_SYMBOL)[0].trim(), '\'');
                }
                envProperties.put(key, value);
            }
        }
        return envProperties;
    }

    private static boolean shouldSkipLine(String line) {
        // Skip comments and empty lines
        return StringUtils.startsWith(line, COMMENT_LINE_START_SYMBOL)
                || !StringUtils.contains(line, VARIABLE_AFFECTATION_OPERATOR);
    }
}
