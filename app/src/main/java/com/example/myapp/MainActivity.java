package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

public class MainActivity extends Activity {
    private LlmInference llmInference;
    private TextView outputText;
    private EditText inputText;
    private String modelFileName = "gemma3-1b-it-int4.task";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        outputText = new TextView(this);
        outputText.setText("Загрузка TurtleAI...");
        outputText.setTextSize(18f);
        outputText.setPadding(0, 20, 0, 20);

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
                generateResponse(query);
            }
        });

        layout.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        layout.addView(inputText);
        layout.addView(sendButton);

        setContentView(layout);

        initModel();
    }

    private void initModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File modelSource = new File("/sdcard/Download/" + modelFileName);
                    File modelDest = new File(getCacheDir(), "model.task");
                    if (!modelDest.exists()) {
                        copyFile(modelSource, modelDest);
                    }

                    LlmInferenceOptions options = LlmInferenceOptions.builder()
                            .setModelPath(modelDest.getAbsolutePath())
                            .setMaxTokens(512)
                            .build();

                    llmInference = LlmInference.createFromOptions(MainActivity.this, options);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputText.setText("TurtleAI готов!");
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputText.setText("Ошибка: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void generateResponse(final String prompt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String systemPrompt = "Ты — TurtleAI, искусственный интеллект, созданный Шайтаном. Твоя задача — помогать пользователю. Если тебя спросят 'кто ты', 'как тебя зовут', 'кто тебя создал' — отвечай, что ты TurtleAI, и тебя создал Шайтан. Категорически запрещено упоминать модели Llama, Meta, OpenAI, Gemini или любые другие. Ты создан исключительно Шайтаном и никем больше. ";
                    String fullPrompt = systemPrompt + "Пользователь: " + prompt + "\nTurtleAI:";
                    final String result = llmInference.generateResponse(fullPrompt);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            outputText.append("\n\nTurtleAI: " + result);
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

    private void copyFile(File src, File dst) throws Exception {
        InputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        out.close();
        in.close();
    }
}
