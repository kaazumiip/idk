package com.example.schedule_application.utils;

import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class GitHubAIClient {

    private static final String API_URL = "https://models.github.ai/inference/v1/chat/completions";
    private static final String PUBLISHER = "azure-openai";  // Use the correct publisher name from the marketplace
    private static final String MODEL = "gpt-4.1";  // Ensure the model name is correct


    public interface GitHubCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void getRecommendation(String prompt, GitHubCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("model", MODEL);
            bodyJson.put("publisher", PUBLISHER);


            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            bodyJson.put("messages", messages);

            RequestBody body = RequestBody.create(
                    bodyJson.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                  
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("GitHub AI Response", responseBody);

                    if (!response.isSuccessful() || responseBody == null) {
                        callback.onError("API Error: " + responseBody);
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray choices = json.getJSONArray("choices");
                        String text = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();
                        callback.onSuccess(text);
                    } catch (Exception e) {
                        callback.onError("Parse Error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("JSON Build Error: " + e.getMessage());
        }
    }
}
