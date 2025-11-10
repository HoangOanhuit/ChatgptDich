package com.mycompany.quanlytruyen.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static AppConfig instance;
    private Properties properties;
    
    // Configuration keys
    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static final String OPENAI_MODEL = "OPENAI_MODEL";
    public static final String OPENAI_MAX_TOKENS = "OPENAI_MAX_TOKENS";
    public static final String OPENAI_TEMPERATURE = "OPENAI_TEMPERATURE";
    
    // Supported OpenAI models (newest first)
    private static final String[] AVAILABLE_MODELS = {
        "gpt-4o",
        "gpt-4-turbo",
        "gpt-4",
        "gpt-3.5-turbo"
    };    
    
    public static final String DB_URL = "DB_URL";
    public static final String DB_USER = "DB_USER";
    public static final String DB_PASS = "DB_PASS";
    
    public static final String UI_THEME = "UI_THEME";
    public static final String LAST_DIRECTORY = "LAST_DIRECTORY";
    public static final String AUTO_SAVE_INTERVAL = "AUTO_SAVE_INTERVAL";
    
    private AppConfig() {
        loadProperties();
    }
    
    public static synchronized AppConfig getInstance() {
        
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        
        // Load from classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("assets/app.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Could not load app.properties: " + e.getMessage());
        }
        
        // Load system properties (override file properties)
        properties.putAll(System.getProperties());
    }
    
    public String getString(String key) {
        return properties.getProperty(key);
    }
    
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    // Convenience methods for common properties
    public String getOpenAIApiKey() {
        return getString(OPENAI_API_KEY, "");
    }
    
    public String[] getAvailableModels() {
        return AVAILABLE_MODELS.clone();
    }

    public String getDefaultModel() {
        return AVAILABLE_MODELS[0];
    }    
    public String getOpenAIModel() {
        return getString(OPENAI_MODEL, getDefaultModel());
    }
    
    public int getOpenAIMaxTokens() {
        return getInt(OPENAI_MAX_TOKENS, 4000);
    }
    
    public double getOpenAITemperature() {
        return getDouble(OPENAI_TEMPERATURE, 0.1);
    }
    
    public String getDatabaseUrl() {
        return getString(DB_URL, "jdbc:mysql://localhost:3306/truyen?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh");
    }
    
    public String getDatabaseUser() {
        return getString(DB_USER, "root");
    }
    
    public String getDatabasePassword() {
        return getString(DB_PASS, "");
    }
    
    public String getLastDirectory() {
        return getString(LAST_DIRECTORY, System.getProperty("user.home"));
    }
    
    public void setLastDirectory(String directory) {
        setProperty(LAST_DIRECTORY, directory);
    }
    
    /**
     * Validate configuration
     */
    public ConfigValidationResult validate() {
        ConfigValidationResult result = new ConfigValidationResult();
        
       
        String apiKey = getOpenAIApiKey();
        if (!apiKey.isEmpty()) {
            if (!apiKey.startsWith("sk-")) {
                result.addError("OpenAI API Key should start with 'sk-'");
            } else if (apiKey.length() < 20) {
                result.addError("OpenAI API Key seems too short");
            }
        }
        
        // Validate model
        String model = getOpenAIModel();
        if (!isValidModel(model)) {
            result.addWarning("Unknown OpenAI model: " + model);
        }
        
        // Validate database URL
        String dbUrl = getDatabaseUrl();
        if (dbUrl.isEmpty()) {
            result.addError("Database URL is required");
        } else if (!dbUrl.startsWith("jdbc:")) {
            result.addError("Database URL should start with 'jdbc:'");
        }
        
        // Validate numeric values
        int maxTokens = getOpenAIMaxTokens();
        if (maxTokens <= 0 || maxTokens > 32000) {
            result.addWarning("Max tokens should be between 1 and 32000, got: " + maxTokens);
        }
        
        double temperature = getOpenAITemperature();
        if (temperature < 0.0 || temperature > 2.0) {
            result.addWarning("Temperature should be between 0.0 and 2.0, got: " + temperature);
        }
        
        return result;
    }
    
    private boolean isValidModel(String model) {
        return model != null && model.startsWith("gpt-");
    }
    
    public static class ConfigValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public java.util.List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }
        
        public java.util.List<String> getWarnings() {
            return new java.util.ArrayList<>(warnings);
        }
        
        public String getErrorsAsString() {
            return String.join("\n", errors);
        }
        
        public String getWarningsAsString() {
            return String.join("\n", warnings);
        }
    }
}