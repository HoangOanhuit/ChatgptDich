package com.mycompany.quanlytruyen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.quanlytruyen.config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

public class OpenAIClient implements AIClient {
    private final OkHttpClient http;
    private final String apiKey;
    private final String apiBaseUrl;
    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Constants for API
    protected static final String DEFAULT_API_BASE_URL = "https://api.openai.com/v1";
    protected static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    public OpenAIClient(String apiKey) {
        this(apiKey, DEFAULT_API_BASE_URL);
    }

    protected OpenAIClient(String apiKey, String apiBaseUrl) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
        this.http = new OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(10))  // Increased timeout for long translations
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofMinutes(5))
            .build();
    }

    /**
     * Translate chapter using ChatGPT API
     * @param model Model to use (e.g., "gpt-3.5-turbo", "gpt-4")
     * @param guidelines Translation guidelines
     * @param nameTable Character name mapping table
     * @param sourceText Source text to translate
     * @return Translated text
     * @throws IOException If API call fails
     */
    
    @Override
    public String translateChapter(String model, String guidelines, String nameTable, String sourceText) throws IOException {

        String systemPrompt = buildSystemPrompt(guidelines, nameTable);

        int maxModelTokens = getMaxModelTokens(model);

        // If content is too long for a single request, split into smaller chunks
        if (estimateTokens(sourceText) > maxModelTokens * 0.7) {
            return translateLongContent(model, systemPrompt, sourceText, maxModelTokens);
        }

        return translateSingleRequest(model, systemPrompt, sourceText);
    }

    private String translateSingleRequest(String model, String systemPrompt, String userPrompt) throws IOException {
        ObjectNode requestBody = mapper.createObjectNode();
        //requestBody.put("model", model != null ? model : "gpt-3.5-turbo");
        requestBody.put("model", model != null ? model : AppConfig.getInstance().getDefaultModel());

        requestBody.put("temperature", 0.1);
        int maxTokens = calculateMaxTokens(model, userPrompt);
        requestBody.put("max_tokens", maxTokens);

        ArrayNode messages = mapper.createArrayNode();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            ObjectNode systemMessage = mapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }

        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);

        Request request = new Request.Builder()
            .url(apiBaseUrl + CHAT_COMPLETIONS_ENDPOINT)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody.toString(), JSON))
            .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("OpenAI API Error " + response.code() + ": " + errorBody);
            }
            

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);
            
            // Extract translated content

            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                String extracted = extractContentFromMessage(message);
                if (extracted != null && !extracted.trim().isEmpty()) {
                    return extracted.trim();
                }
            }
            

            throw new IOException("Invalid response format from OpenAI API: " + responseBody);
        }
    }

    private String translateLongContent(String model, String systemPrompt, String sourceText, int maxModelTokens) throws IOException {
        String[] paragraphs = sourceText.split("\n\n");
        StringBuilder result = new StringBuilder();
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (estimateTokens(currentChunk.toString() + paragraph) > maxModelTokens * 0.6) {
                if (currentChunk.length() > 0) {
                    result.append(translateSingleRequest(model, systemPrompt, currentChunk.toString())).append("\n\n");
                    currentChunk.setLength(0);
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

        if (currentChunk.length() > 0) {
            result.append(translateSingleRequest(model, systemPrompt, currentChunk.toString()));
        }

        return result.toString().trim();
    }
    
    /**
     * Legacy method for compatibility with TranslationRunner
     * @param model Model to use
     * @param prompt Complete prompt (system + user combined)
     * @return Translated text
     * @throws IOException If API call fails
     */
    @Override
    public String translate(String model, String prompt) throws IOException {
        // For legacy compatibility, treat entire prompt as user message
        return translateWithSimplePrompt(model, prompt);
    }
    
    private String translateWithSimplePrompt(String model, String prompt) throws IOException {
        // Create request body
        ObjectNode requestBody = mapper.createObjectNode();
        //requestBody.put("model", model != null ? model : "gpt-3.5-turbo");
        requestBody.put("model", model != null ? model : AppConfig.getInstance().getDefaultModel());
        requestBody.put("temperature", 0.1);
        //requestBody.put("max_tokens", 4000);
        int maxTokens = calculateMaxTokens(model, prompt);
        requestBody.put("max_tokens", maxTokens);        
        
        // Create messages array with single user message
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        // Make API request
        Request request = new Request.Builder()
            .url(apiBaseUrl + CHAT_COMPLETIONS_ENDPOINT)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(requestBody.toString(), JSON))
            .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("OpenAI API Error " + response.code() + ": " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);
            
            // Extract translated content
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                String extracted = extractContentFromMessage(message);
                if (extracted != null && !extracted.trim().isEmpty()) {
                    return extracted.trim();
                }    
            }
            
            throw new IOException("Invalid response format from OpenAI API: " + responseBody);
        }
    }
        
   private String extractContentFromMessage(JsonNode message) {
        if (message == null) {
            return null;
        }

        // Primary content field
        String contentText = extractTextFromContentNode(message.get("content"), true);
        if (contentText != null && !contentText.trim().isEmpty()) {
            return contentText;
        }

        // Some providers (e.g. DeepSeek) may return final answer in other fields
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
    
    private int calculateMaxTokens(String model, String sourceText) {
        int maxModelTokens = getMaxModelTokens(model);
        int estimatedInputTokens = estimateTokens(sourceText) + 500; // reserve for system prompt
        int available = maxModelTokens - estimatedInputTokens;
        if (available <= 0) {
            // If the chunk is still too large, fall back to a conservative value instead of 1 token
            return Math.max(256, maxModelTokens / 4);
        }
        int outputLimit = determineMaxOutputTokens(model, maxModelTokens);
        return Math.min(available, outputLimit);
    }

    private int getMaxModelTokens(String model) {
        if (model == null) return 4096;
        model = model.toLowerCase();
        if (model.contains("deepseek")) {
            // DeepSeek-chat currently supports a 64k token context window
            return 64000;
        } else if (model.contains("gpt-4-turbo") || model.contains("gpt-4o")) {
            return 128000;
        } else if (model.contains("gpt-4")) {
            return 8192;
        } else {
            return 4096;
        }
    }
    
    private int determineMaxOutputTokens(String model, int maxModelTokens) {
        int defaultCap = Math.min(3000, Math.max(512, maxModelTokens / 3));

        if (model == null) {
            return defaultCap;
        }

        String normalized = model.toLowerCase();
        if (normalized.contains("deepseek")) {
            // Allow DeepSeek to return much longer continuations while keeping a safety margin
            return Math.min(8000, maxModelTokens - 1000);
        }

        if (normalized.contains("gpt-4o") || normalized.contains("gpt-4-turbo")) {
            return Math.min(6000, maxModelTokens - 2000);
        }

        return defaultCap;
    }    

    private int estimateTokens(String text) {
        if (text == null) return 0;
        return text.length();
    }   
    /**
     * Test API connection and key validity
     * @return true if API key is valid
     */
    @Override
    public boolean testConnection() {
        try {
            // Make a simple test request
            ObjectNode testBody = mapper.createObjectNode();
            String testModel = getTestModel();
            if (testModel == null || testModel.trim().isEmpty()) {
                testModel = AppConfig.getInstance().getDefaultModel();
            }
            testBody.put("model", testModel);
            testBody.put("max_tokens", 1);
            
            ArrayNode messages = mapper.createArrayNode();
            ObjectNode testMessage = mapper.createObjectNode();
            testMessage.put("role", "user");
            testMessage.put("content", "Hi");
            messages.add(testMessage);
            testBody.set("messages", messages);
            
            Request request = new Request.Builder()
                .url(apiBaseUrl + CHAT_COMPLETIONS_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(testBody.toString(), JSON))
                .build();
            
            try (Response response = http.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Build system prompt for translation
     */
    private String buildSystemPrompt(String guidelines, String nameTable) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một dịch giả chuyên nghiệp từ tiếng Trung sang tiếng Việt. ");
        prompt.append("Hãy dịch văn bản một cách chính xác, tự nhiên và phù hợp với văn hóa Việt Nam.\n\n");
        
        if (guidelines != null && !guidelines.trim().isEmpty()) {
            prompt.append("QUY TẮC DỊCH:\n");
            prompt.append(guidelines.trim());
            prompt.append("\n\n");
        }
        
        if (nameTable != null && !nameTable.trim().isEmpty()) {
            prompt.append("BẢNG TÊN NHÂN VẬT VÀ CÁCH XƯNG HÔ:\n");
            prompt.append(nameTable.trim());
            prompt.append("\n\n");
        }
        
        prompt.append("HƯỚNG DẪN:\n");
        prompt.append("- Chỉ trả về nội dung đã dịch, không thêm chú thích hay giải thích\n");
        prompt.append("- Giữ nguyên format đoạn văn và dấu câu\n");
        prompt.append("- Dịch sát nghĩa nhưng tự nhiên trong tiếng Việt\n");
        prompt.append("- Sử dụng đúng tên nhân vật và cách xưng hô theo bảng tên đã cho");
        
        return prompt.toString();
    }
    
    /**
     * Calculate estimated cost for translation
     * @param textLength Total length of text to translate
     * @param model Model being used
     * @return Estimated cost in USD
     */
    @Override
    public double calculateEstimatedCost(int textLength, String model) {
        // Rough estimation based on OpenAI pricing (as of 2024)
        double costPer1kTokens;
        switch (model.toLowerCase()) {
            case "gpt-4":
            case "gpt-4-0314":
                costPer1kTokens = 0.06; // $0.06 per 1K tokens
                break;
            case "gpt-4-turbo":
            case "gpt-4-turbo-preview":
                costPer1kTokens = 0.03; // $0.03 per 1K tokens
                break;
            case "gpt-4o":
                costPer1kTokens = 0.005;  // $0.03 per 1K tokens  
                break;
            case "gpt-3.5-turbo":
            default:
                costPer1kTokens = 0.002; // $0.002 per 1K tokens
                break;
        }
        
        // Rough estimation: 1 token ≈ 4 characters for mixed Chinese/English text
        int estimatedTokens = textLength / 3; // Conservative estimate
        return (estimatedTokens / 1000.0) * costPer1kTokens;
    }
    
    protected String getTestModel() {
        return AppConfig.getInstance().getDefaultModel();
    }
}