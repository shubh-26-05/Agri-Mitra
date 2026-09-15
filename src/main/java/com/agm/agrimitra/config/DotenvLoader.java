package com.agm.agrimitra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight utility to load environment variables from a local .env file
 * into System properties and JVM environment before Spring Boot starts.
 */
public final class DotenvLoader {

    private static final Logger log = LoggerFactory.getLogger(DotenvLoader.class);

    private DotenvLoader() {}

    public static Map<String, String> load() {
        Map<String, String> loaded = new HashMap<>();
        File[] candidateFiles = new File[] {
                new File(".env"),
                new File("..", ".env"),
                new File(System.getProperty("user.dir"), ".env")
        };

        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                loadFile(file, loaded);
                break;
            }
        }
        return loaded;
    }

    private static void loadFile(File file, Map<String, String> loaded) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();

                    // Strip wrapping double or single quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        if (value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                    }

                    loaded.put(key, value);
                    // System.setProperty takes effect in Spring Boot environment if not in OS env
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, value);
                    }
                }
            }
            log.info("Loaded {} environment variables from {}", loaded.size(), file.getName());
        } catch (Exception e) {
            log.warn("Could not read .env file {}: {}", file.getName(), e.getMessage());
        }
    }
}
