package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private TextView outputText;
    private EditText inputText;
    private String API_KEY = "ВСТАВЬ_СЮДА_КЛЮЧ";
    private String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        outputText = new TextView(this);
        outputText.setText("TurtleAI готов!");
        outputText.setTextSize(18f);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(outputText);

        inputText = new EditText(this);
        inputText.setHint("Напиши вопрос...");

        Button sendButton = new Button(this);
        sendButton.setText("Отправить");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = inputText.getText().toString();
                if (query.isEmpty()) return;
                outputText.append("\n\nТы: " + query);
                inputText.setText("");
                sendToAI(query);
            }
        });

        layout.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        layout.addView(inputText);
        layout.addView(sendButton);
        setContentView(layout);
    }

    private void sendToAI(final String prompt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    JSONObject system = new JSONObject();
                    system.put("role", "system");
                    system.put("content", "Ты — TurtleAI, искусственный интеллект, созданный Шайтаном. Если тебя спросят 'кто ты', 'как тебя зовут', 'кто тебя создал' — отвечай, что ты TurtleAI, и тебя создал Шайтан. Не упоминай Llama, Meta, OpenAI или другие модели.");

                    JSONObject user = new JSONObject();
                    user.put("role", "user");
                    user.put("content", prompt);

                    JSONArray messages = new JSONArray();
                    messages.put(system);
                    messages.put(user);

                    JSONObject json = new JSONObject();
                    json.put("model", "meta-llama/llama-3.3-70b-instruct:free");
                    json.put("messages", messages);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONObject response = new JSONObject(sb.toString());
                    final String answer = response.getJSONArray("choices")
                            .getJSONObject(0).getJSONObject("message").getString("content");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputText.append("\n\nTurtleAI: " + answer);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputText.append("\n\n[Ошибка: " + e.getMessage() + "]");
                        }
                    });
                }
            }
        }).start();
    }
}
