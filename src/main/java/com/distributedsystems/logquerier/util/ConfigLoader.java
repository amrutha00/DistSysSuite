package com.distributedsystems.logquerier.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private Properties configProperties;

    public ConfigLoader() {
        this.configProperties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new FileNotFoundException("application.properties not found in the classpath");
            }
            configProperties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return configProperties.getProperty(key);
    }
}
