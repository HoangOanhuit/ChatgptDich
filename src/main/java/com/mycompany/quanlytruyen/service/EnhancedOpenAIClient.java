package com.mycompany.quanlytruyen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.quanlytruyen.config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.Map;

public class EnhancedOpenAIClient {
    private final OkHttpClient http;
    private final String apiKey;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Constants for API
    private static final String API_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";
    
    // Model limits (approximate)
    private static final int GPT_3_5_TURBO_MAX_TOKENS = 4096;
    private static final int GPT_4_MAX_TOKENS = 8192;
    private static final int GPT_4_TURBO_MAX_TOKENS = 128000;

    public EnhancedOpenAIClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(15))  // Tăng timeout
            .connectTimeout(Duration.ofMinutes(2))
            .readTimeout(Duration.ofMinutes(10))
            .retryOnConnectionFailure(true)       // Thêm retry
            .build();
    }

    /**
     * Translate chapter with enhanced error handling and content splitting
     */
    public String translateChapter(String model, String guidelines, String nameTable, String sourceText) throws IOException {
        // Validate inputs
        if (sourceText == null || sourceText.trim().isEmpty()) {
            throw new IOException("Source text is empty");
        }
        
        // Check content length and split if necessary
        int maxContentLength = getMaxContentLength(model);
        if (estimateTokens(sourceText) > maxContentLength * 0.7) { // Use 70% of limit for safety
            return translateLongContent(model, guidelines, nameTable, sourceText, maxContentLength);
        }
        
        return translateSingleRequest(model, guidelines, nameTable, sourceText);
    }
    
    /**
     * Translate single request with better error handling
     */
    private String translateSingleRequest(String model, String guidelines, String nameTable, String sourceText) throws IOException {
        String systemPrompt = buildSystemPrompt(guidelines, nameTable);
        
        // Create request body with optimized settings
        ObjectNode requestBody = mapper.createObjectNode();
        //requestBody.put("model", model != null ? model : "gpt-3.5-turbo");
        requestBody.put("model", model != null ? model : AppConfig.getInstance().getDefaultModel());
        requestBody.put("temperature", 0.1);
        
        // Dynamic max_tokens based on model
        int maxTokens = getOptimalMaxTokens(model, sourceText);
        requestBody.put("max_tokens", maxTokens);
        
        // Add timeout and retry settings
        requestBody.put("timeout", 300); // 5 minutes
        
        // Create messages array
        ArrayNode messages = mapper.createArrayNode();
        
        // System message
        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        // User message
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", sourceText);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        // Make API request with retry logic
        return makeRequestWithRetry(requestBody, 3);
    }
    
    /**
     * Handle long content by splitting into chunks
     */
    private String translateLongContent(String model, String guidelines, String nameTable, String sourceText, int maxLength) throws IOException {
        // Split content into paragraphs
        String[] paragraphs = sourceText.split("\n\n");
        StringBuilder result = new StringBuilder();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            // If adding this paragraph would exceed limit, translate current chunk
            if (estimateTokens(currentChunk.toString() + paragraph) > maxLength * 0.6) {
                if (currentChunk.length() > 0) {
                    String translated = translateSingleRequest(model, guidelines, nameTable, currentChunk.toString());
                    result.append(translated).append("\n\n");
                    currentChunk.setLength(0);
                    
                    // Add delay between chunks to avoid rate limiting
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Translation interrupted", e);
                    }
                }
            }
            
            currentChunk.append(paragraph).append("\n\n");
        }
        
        // Translate remaining content
        if (currentChunk.length() > 0) {
            String translated = translateSingleRequest(model, guidelines, nameTable, currentChunk.toString());
            result.append(translated);
        }
        
        return result.toString().trim();
    }
    
    /**
     * Make API request with retry logic and better error handling
     */
    private String makeRequestWithRetry(ObjectNode requestBody, int maxRetries) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Request request = new Request.Builder()
                    .url(API_BASE_URL + CHAT_COMPLETIONS_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "Novel-Translator/1.0")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

                try (Response response = http.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    
                    if (response.isSuccessful()) {
                        return parseSuccessResponse(responseBody);
                    } else {
                        throw createDetailedError(response.code(), responseBody, attempt, maxRetries);
                    }
                }
                
            } catch (IOException e) {
                lastException = e;
                
                if (attempt < maxRetries) {
                    long delay = calculateBackoffDelay(attempt);
                    System.err.println("Attempt " + attempt + " failed: " + e.getMessage() + ". Retrying in " + delay + "ms...");
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Translation interrupted", ie);
                    }
                } else {
                    System.err.println("All " + maxRetries + " attempts failed");
                }
            }
        }
        
        throw new IOException("Translation failed after " + maxRetries + " attempts. Last error: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }
    
    /**
     * Parse successful API response
     */
    private String parseSuccessResponse(String responseBody) throws IOException {
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                String extracted = extractContentFromMessage(message);
                if (extracted != null && !extracted.trim().isEmpty()) {
                    return extracted.trim();
                }
            }
            
            throw new IOException("Invalid response format: missing content in choices");
        } catch (Exception e) {
            throw new IOException("Failed to parse API response: " + e.getMessage() + "\nResponse: " + responseBody, e);
        }
    }
    
    private String extractContentFromMessage(JsonNode message) {
        if (message == null) {
            return null;
        }

        String contentText = extractTextFromContentNode(message.get("content"), true);
        if (contentText != null && !contentText.trim().isEmpty()) {
            return contentText;
        }

        String reasoningContent = extractTextFromContentNode(message.get("reasoning_content"), false);
        if (reasoningContent != null && !reasoningContent.trim().isEmpty()) {
            return reasoningContent;
        }

        String reasoning = extractTextFromContentNode(message.get("reasoning"), false);
        if (reasoning != null && !reasoning.trim().isEmpty()) {
            return reasoning;
        }

        String outputText = extractTextFromContentNode(message.get("output_text"), true);
        if (outputText != null && !outputText.trim().isEmpty()) {
            return outputText;
        }

        return null;
    }

    private String extractTextFromContentNode(JsonNode node, boolean preferOutputText) {
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isObject()) {
            String type = node.has("type") && node.get("type").isTextual()
                ? node.get("type").asText() : null;

            if (preferOutputText && type != null) {
                String normalized = type.toLowerCase();
                if (normalized.equals("reasoning") || normalized.equals("thought") || normalized.equals("thinking")) {
                    return null;
                }
            }

            if (node.has("text") && node.get("text").isTextual()) {
                return node.get("text").asText();
            }

            if (node.has("content")) {
                return extractTextFromContentNode(node.get("content"), preferOutputText);
            }

            if (node.has("value") && node.get("value").isTextual()) {
                return node.get("value").asText();
            }
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode element : node) {
                if (element == null || element.isNull()) {
                    continue;
                }

                String part = extractTextFromContentNode(element, preferOutputText);
                if (part != null && !part.isEmpty()) {
                    if (builder.length() > 0 && !part.startsWith("\n")) {
                        builder.append("\n");
                    }
                    builder.append(part);
                }
            }

            return builder.length() > 0 ? builder.toString() : null;
        }

        return node.asText();
    }    
    
    /**
     * Create detailed error with specific handling for different error codes
     */
    private IOException createDetailedError(int code, String responseBody, int attempt, int maxRetries) {
        String errorMessage = "HTTP " + code;
        String suggestion = "";
        
        try {
            JsonNode errorJson = mapper.readTree(responseBody);
            JsonNode error = errorJson.get("error");
            if (error != null) {
                String type = error.has("type") ? error.get("type").asText() : "";
                String message = error.has("message") ? error.get("message").asText() : "";
                errorMessage = type + ": " + message;
                
                // Specific error handling
                switch (code) {
                    case 401:
                        suggestion = "Check your API key in app.properties";
                        break;
                    case 429:
                        suggestion = "Rate limit exceeded. Increase delay between requests";
                        break;
                    case 400:
                        if (message.contains("maximum context length")) {
                            suggestion = "Content too long. Try splitting into smaller chunks";
                        }
                        break;
                    case 500:
                    case 502:
                    case 503:
                        suggestion = "OpenAI server error. Will retry automatically";
                        break;
                }
            }
        } catch (Exception e) {
            errorMessage += " (Could not parse error response)";
        }
        
        String fullMessage = String.format("API Error [Attempt %d/%d]: %s", attempt, maxRetries, errorMessage);
        if (!suggestion.isEmpty()) {
            fullMessage += "\nSuggestion: " + suggestion;
        }
        fullMessage += "\nResponse: " + responseBody;
        
        return new IOException(fullMessage);
    }
    
    /**
     * Calculate exponential backoff delay
     */
    private long calculateBackoffDelay(int attempt) {
        // Exponential backoff: 2^attempt * 1000ms, max 30 seconds
        long delay = (long) (Math.pow(2, attempt) * 1000);
        return Math.min(delay, 30000);
    }
    
    /**
     * Estimate token count (rough approximation)
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // Rough estimation: 1 token ≈ 4 characters for mixed Chinese/English
        return text.length() / 3;
    }
    
    /**
     * Get maximum content length for model
     */
    private int getMaxContentLength(String model) {
        if (model == null) return GPT_3_5_TURBO_MAX_TOKENS;
        
        model = model.toLowerCase();
        if (model.contains("gpt-4-turbo") || model.contains("gpt-4o")) {
            return GPT_4_TURBO_MAX_TOKENS;
        } else if (model.contains("gpt-4")) {
            return GPT_4_MAX_TOKENS;
        } else {
            return GPT_3_5_TURBO_MAX_TOKENS;
        }
    }
    
    /**
     * Get optimal max_tokens for response
     */
    private int getOptimalMaxTokens(String model, String sourceText) {
        int maxModelTokens = getMaxContentLength(model);
        int estimatedInputTokens = estimateTokens(sourceText) + 500; // +500 for system prompt
        
        // Reserve tokens for response (typically 1.5x source length for translation)
        int reserveForResponse = Math.min(estimatedInputTokens * 2, maxModelTokens / 2);
        
        return Math.max(1000, reserveForResponse); // Minimum 1000 tokens
    }
    
    /**
     * Build enhanced system prompt
     */
    private String buildSystemPrompt(String guidelines, String nameTable) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional translator specializing in Chinese to Vietnamese translation. ");
        prompt.append("Translate accurately, naturally, and culturally appropriately for Vietnamese readers.\n\n");
        
        if (guidelines != null && !guidelines.trim().isEmpty()) {
            prompt.append("TRANSLATION GUIDELINES:\n");
            prompt.append(guidelines.trim());
            prompt.append("\n\n");
        }
        
        if (nameTable != null && !nameTable.trim().isEmpty()) {
            prompt.append("CHARACTER NAMES AND FORMS OF ADDRESS:\n");
            prompt.append(nameTable.trim());
            prompt.append("\n\n");
        }
        
        prompt.append("INSTRUCTIONS:\n");
        prompt.append("- Return ONLY the translated content, no explanations or notes\n");
        prompt.append("- Preserve paragraph structure and punctuation\n");
        prompt.append("- Translate accurately but naturally in Vietnamese\n");
        prompt.append("- Use correct character names and forms of address from the provided table\n");
        prompt.append("- Maintain the original tone and style of the text");
        
        return prompt.toString();
    }
    
    /**
     * Test API connection with detailed diagnostics
     */
    public ConnectionTestResult testConnection() {
        try {
            ObjectNode testBody = mapper.createObjectNode();
            //testBody.put("model", "gpt-3.5-turbo");
            testBody.put("model", AppConfig.getInstance().getDefaultModel());
            testBody.put("max_tokens", 10);
            
            ArrayNode messages = mapper.createArrayNode();
            ObjectNode testMessage = mapper.createObjectNode();
            testMessage.put("role", "user");
            testMessage.put("content", "Test");
            messages.add(testMessage);
            testBody.set("messages", messages);
            
            Request request = new Request.Builder()
                .url(API_BASE_URL + CHAT_COMPLETIONS_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(testBody.toString(), JSON))
                .build();
            
            try (Response response = http.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return new ConnectionTestResult(true, "Connection successful", null);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    return new ConnectionTestResult(false, "HTTP " + response.code(), errorBody);
                }
            }
        } catch (Exception e) {
            return new ConnectionTestResult(false, e.getMessage(), null);
        }
    }
    
    /**
     * Connection test result
     */
    public static class ConnectionTestResult {
        public final boolean success;
        public final String message;
        public final String details;
        
        public ConnectionTestResult(boolean success, String message, String details) {
            this.success = success;
            this.message = message;
            this.details = details;
        }
        
        @Override
        public String toString() {
            return success ? "✓ " + message : "✗ " + message + (details != null ? "\nDetails: " + details : "");
        }
    }
    
    /**
     * Calculate estimated cost for translation
     */
    public double calculateEstimatedCost(int textLength, String model) {
        double costPer1kTokens;
        switch (model.toLowerCase()) {
            case "gpt-4":
            case "gpt-4-0314":
                costPer1kTokens = 0.06;
                break;
            case "gpt-4-turbo":
            case "gpt-4-turbo-preview":
                costPer1kTokens = 0.03;
                break;
            case "gpt-4o":
                costPer1kTokens = 0.005;
                break;
            case "gpt-3.5-turbo":
            default:
                costPer1kTokens = 0.002;
                break;
        }
        
        int estimatedTokens = textLength / 3;
        return (estimatedTokens / 1000.0) * costPer1kTokens;
    }
}