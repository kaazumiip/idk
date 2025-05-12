package com.example.schedule_application.utils;

import okhttp3.*;
import com.google.gson.Gson;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekApiService {
    // New OpenRouter API key
    private static final String API_KEY = "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";

    // OpenRouter API URLs
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";
    private static final String TAG = "DeepSeekApiService";

    // DeepSeek Prover V2 model from OpenRouter
    private static final String MODEL = "deepseek/deepseek-prover-v2";

    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    public void getRecommendations(String userQuery, Callback callback) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", MODEL);
        jsonMap.put("temperature", 0.7);
        jsonMap.put("max_tokens", 150);

        // Simplify the query to use fewer tokens
        String conciseQuery = "Based on these tasks, provide a brief productivity tip (max 100 words):\n" + userQuery;

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", conciseQuery);
        messages.add(message);

        jsonMap.put("messages", messages);

        // Add OpenRouter specific headers in the JSON body
        Map<String, String> httpReferer = new HashMap<>();
        httpReferer.put("HTTP-Referer", "https://example.com");
        jsonMap.put("headers", httpReferer);

        String jsonBody = gson.toJson(jsonMap);
        Log.d(TAG, "Request body: " + jsonBody);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                // OpenRouter specific headers
                .addHeader("HTTP-Referer", "https://example.com")
                .addHeader("X-Title", "Schedule Application")
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Method to discover available models on OpenRouter
    public void listAvailableModels(Callback callback) {
        Request request = new Request.Builder()
                .url(MODELS_URL)
                .get()
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(callback);
    }
}