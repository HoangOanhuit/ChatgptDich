package com.mycompany.quanlytruyen.service;

/**
 * Client for interacting with the DeepSeek API using an OpenAI compatible schema.
 */
public class DeepSeekClient extends OpenAIClient {
    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1";
    private static final String DEFAULT_MODEL = "deepseek-reasoner";

    public DeepSeekClient(String apiKey) {
        super(apiKey, DEEPSEEK_BASE_URL);
    }

    @Override
    protected String getTestModel() {
        return DEFAULT_MODEL;
    }

    @Override
    public double calculateEstimatedCost(int textLength, String model) {
        // Pricing data for DeepSeek is subject to change; using a conservative default cost.
        double costPer1kTokens = 0.001; // Approximate estimation.
        if (model != null && model.toLowerCase().contains("coder")) {
            costPer1kTokens = 0.0009;
        }
        int estimatedTokens = textLength / 3;
        return (estimatedTokens / 1000.0) * costPer1kTokens;
    }
}