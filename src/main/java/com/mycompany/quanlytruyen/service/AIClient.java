package com.mycompany.quanlytruyen.service;

import java.io.IOException;

/**
 * Generic interface for AI translation providers.
 */
public interface AIClient {
    String translateChapter(String model, String guidelines, String nameTable, String sourceText) throws IOException;

    String translate(String model, String prompt) throws IOException;

    double calculateEstimatedCost(int textLength, String model);

    boolean testConnection();
}