package com.example.schedule_application.utils;

import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekApiService {
    private static final String API_KEY = "sk-b04824034b074545bb15b84cd1400168";
    private static final String BASE_URL = "https://api.deepseek.com/v1/chat/completions";

    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    public void getRecommendations(String userQuery, Callback callback) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", "deepseek-chat");
        jsonMap.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userQuery);
        messages.add(message);

        jsonMap.put("messages", messages);

        String jsonBody = gson.toJson(jsonMap);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(callback);
    }

}
