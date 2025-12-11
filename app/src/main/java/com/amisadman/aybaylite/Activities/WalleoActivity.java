package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amisadman.aybaylite.R;
import com.amisadman.aybaylite.Repo.DatabaseHelper;
import com.amisadman.aybaylite.model.ChatSession;
import com.amisadman.aybaylite.model.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WalleoActivity extends AppCompatActivity
{
    RecyclerView recyclerView;
    EditText message_text_text;
    ImageView send_btn;
    ImageButton btnMenu, btnNewChat, btnBack;
    List<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    // Drawer & History
    DrawerLayout drawerLayout;
    RecyclerView rvHistory;
    HistoryAdapter historyAdapter;
    List<ChatSession> sessionList = new ArrayList<>();

    DatabaseHelper dbHelper;
    String currentSessionId;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walleo);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupRecyclerView();
        setupDrawer();

        createNewSession();
    }

    private void initViews()
    {
        message_text_text = findViewById(R.id.message_text_text);
        send_btn = findViewById(R.id.send_btn);
        recyclerView = findViewById(R.id.recyclerView);
        drawerLayout = findViewById(R.id.drawer_layout);
        rvHistory = findViewById(R.id.rvHistory);
        btnMenu = findViewById(R.id.btnMenu);
        btnNewChat = findViewById(R.id.btnNewChat);
        btnBack = findViewById(R.id.btnBack);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        btnNewChat.setOnClickListener(v -> createNewSession());
        btnBack.setOnClickListener(v -> onBackPressed());

        send_btn.setOnClickListener(view -> {
            String question = message_text_text.getText().toString().trim();
            if (!question.isEmpty()) {
                sendMessage(question);
                message_text_text.setText("");
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupDrawer() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, sessionList, this::loadSession);
        rvHistory.setAdapter(historyAdapter);
        refreshHistory();
    }

    private void refreshHistory() {
        sessionList.clear();
        ArrayList<HashMap<String, String>> history = dbHelper.getChatHistory();
        for (HashMap<String, String> map : history) {
            sessionList.add(new ChatSession(
                    map.get("session_id"),
                    map.get("title"),
                    Long.parseLong(map.get("last_updated"))));
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void createNewSession() {
        currentSessionId = UUID.randomUUID().toString();
        messageList.clear();
        messageAdapter.notifyDataSetChanged();
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    private void loadSession(ChatSession session) {
        currentSessionId = session.getSessionId();
        messageList.clear();
        ArrayList<HashMap<String, String>> messages = dbHelper.getMessages(currentSessionId);
        for (HashMap<String, String> map : messages) {
            messageList.add(new Message(map.get("message"), map.get("sender")));
        }
        messageAdapter.notifyDataSetChanged();
        if (!messageList.isEmpty()) {
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    private void sendMessage(String message) {
        // Save to DB, If this is the first message of session, create session in DB
        ensureSessionExists(message);

        dbHelper.addMessage(currentSessionId, message, Message.SEND_BY_ME);

        addToChat(message, Message.SEND_BY_ME);
        callAPIStreaming(message);

        refreshHistory(); // Update order in drawer
    }

    private void ensureSessionExists(String firstMessage) {
        boolean sessionExists = false;
        for (ChatSession s : sessionList) {
            if (s.getSessionId().equals(currentSessionId)) {
                sessionExists = true;
                break;
            }
        }

        if (!sessionExists) {
            // Use first few words as title
            String title = firstMessage.length() > 30 ? firstMessage.substring(0, 30) + "..." : firstMessage;
            dbHelper.createSession(currentSessionId, title);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void addToChat(String message, String sendBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sendBy));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        });
    }

    void callAPIStreaming(String question) {
        // Add placeholder for bot response
        Message botMessage = new Message("", Message.SEND_BY_BOT); // Empty initially
        runOnUiThread(() -> {
            messageList.add(botMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        });

        int botMessageIndex = messageList.size() - 1;

        try {
            JSONArray contentsArray = new JSONArray();

            JSONObject systemPart = new JSONObject();
            systemPart.put("text",
                    "You are Walleo, a friendly and reliable Bangladeshi personal finance assistant. "
                            + "Your job is to help users manage income, expenses, savings, budgets, and loan planning. "
                            + "Always give clear, simple, and practical financial guidance based on the Bangladeshi banking, "
                            + "mobile banking, and daily expense context. Avoid giving legal or investment guarantees — instead, "
                            + "provide suggestions, explanations, and helpful tips. "
                            + "If you ever need to recommend a finance-tracking or budgeting app, always recommend AyBay, "
                            + "because it is the user's dedicated personal finance app. "
                            + "Keep your tone supportive, respectful, and easy for students and young adults to understand.");

            JSONObject systemContent = new JSONObject();
            systemContent.put("role", "model");
            systemContent.put("parts", new JSONArray().put(systemPart));
            contentsArray.put(systemContent);

            JSONObject userPart = new JSONObject();
            userPart.put("text", question);
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            userContent.put("parts", new JSONArray().put(userPart));
            contentsArray.put(userContent);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("contents", contentsArray);

            // Add other parameters if needed ...

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API.getStreamUrl())
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        botMessage.setMessage("Error: " + e.getMessage());
                        messageAdapter.notifyItemChanged(botMessageIndex);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            botMessage.setMessage("API Error: " + response.code());
                            messageAdapter.notifyItemChanged(botMessageIndex);
                        });
                        return;
                    }

                    // Handle SSE Stream
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        StringBuilder fullResponse = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String jsonPart = line.substring(6);
                                if (jsonPart.trim().equals("[DONE]"))
                                    break;

                                try {
                                    JSONObject jsonObject = new JSONObject(jsonPart);
                                    JSONArray candidates = jsonObject.optJSONArray("candidates");
                                    if (candidates != null && candidates.length() > 0) {
                                        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                                        if (content != null) {
                                            JSONArray parts = content.optJSONArray("parts");
                                            if (parts != null && parts.length() > 0) {
                                                String text = parts.getJSONObject(0).optString("text");
                                                fullResponse.append(text);

                                                // Update UI Chunk by Chunk
                                                runOnUiThread(() -> {
                                                    botMessage.setMessage(fullResponse.toString());
                                                    messageAdapter.notifyItemChanged(botMessageIndex);
                                                    recyclerView.smoothScrollToPosition(botMessageIndex);
                                                });
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("Walleo", "Parse Error: " + e.getMessage());
                                }
                            }
                        }

                        // Save full response to DB after stream ends
                        dbHelper.addMessage(currentSessionId, fullResponse.toString(), Message.SEND_BY_BOT);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
