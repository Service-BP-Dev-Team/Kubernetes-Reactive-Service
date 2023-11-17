package com.reactive.service.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ENV_Manager {
    public static void main(String[] args) {
        String yamlFilePath = "path/to/your/file.yml";
        String envFilePath = "path/to/your/.env";

        try {
            String replacedContent = replaceEnvVariables(yamlFilePath, envFilePath);
            System.out.println(replacedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String replaceEnvVariables(String yamlFilePath, String envFilePath) throws IOException {
        // Read the .env file and store the key-value pairs in a map
        Map<String, String> envVariables = readEnvFile(envFilePath);

        // Read the YAML file content
        String yamlContent = readFileToString(yamlFilePath);

        // Replace the environment variables in the YAML content
        String replacedContent = replaceEnvVariablesInYaml(yamlContent, envVariables);

        return replacedContent;
    }

    private static Map<String, String> readEnvFile(String envFilePath) throws IOException {
        Map<String, String> envVariables = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        envVariables.put(key, value);
                    }
                }
            }
        }

        return envVariables;
    }

    private static String readFileToString(String filePath) throws IOException {
        Path path = Path.of(filePath);
        return Files.readString(path);
    }

    private static String replaceEnvVariablesInYaml(String yamlContent, Map<String, String> envVariables) {
        StringBuilder replacedContent = new StringBuilder(yamlContent);

        Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");

        Matcher matcher = pattern.matcher(replacedContent);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = envVariables.get(variableName);
            if (variableValue != null) {
                String variablePlaceholder = "\\$\\{" + variableName + "\\}";
                replacedContent = new StringBuilder(replacedContent.toString().replaceAll(variablePlaceholder, variableValue));
            }
        }

        return replacedContent.toString();
    }
}