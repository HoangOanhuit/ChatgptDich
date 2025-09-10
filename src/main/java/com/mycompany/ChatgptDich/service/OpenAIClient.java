/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ChatgptDich.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

public class OpenAIClient {
  private final OkHttpClient http;
  private final String apiKey;
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private final ObjectMapper mapper = new ObjectMapper();

  public OpenAIClient(String apiKey) {
    this.apiKey = apiKey;
    this.http = new OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(120))
        .build();
  }

  /**
   * Gọi Responses API: trả về text dịch (string).
   * Ưu tiên đọc `output_text`; nếu không có thì ghép từ `output`/`content`.
   * Docs: Responses API + output_text, Text generation guide. 
   */
  public String translate(String model, String prompt) throws IOException {
    String body = "{\"model\":\"" + escape(model) + "\",\"input\":" + mapper.writeValueAsString(prompt) + "}";
    Request req = new Request.Builder()
        .url("https://api.openai.com/v1/responses")
        .addHeader("Authorization", "Bearer " + apiKey)
        .addHeader("Content-Type", "application/json")
        .post(RequestBody.create(body, JSON))
        .build();

    try (Response resp = http.newCall(req).execute()) {
      if (!resp.isSuccessful()) {
        throw new IOException("OpenAI HTTP " + resp.code() + ": " + (resp.body()!=null?resp.body().string():""));
      }
      JsonNode root = mapper.readTree(resp.body().string());

      // 1) thử lấy output_text (tiện lợi, docs/cookbook)
      JsonNode outText = root.get("output_text");
      if (outText != null && !outText.isNull() && !outText.asText().isEmpty()) return outText.asText();

      // 2) fallback: duyệt output -> content -> text.value
      JsonNode output = root.get("output");
      if (output != null && output.isArray()) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode item : output) {
          JsonNode content = item.get("content");
          if (content != null && content.isArray()) {
            for (JsonNode part : content) {
              JsonNode text = part.get("text");
              if (text != null) {
                JsonNode val = text.get("value");
                if (val != null) sb.append(val.asText());
              }
            }
          }
        }
        if (sb.length() > 0) return sb.toString();
      }

      // 3) cuối cùng: trả toàn bộ JSON (để debug)
      return root.toString();
    }
  }

  private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
}