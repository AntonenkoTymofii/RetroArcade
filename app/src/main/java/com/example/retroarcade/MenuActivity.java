package com.example.retroarcade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.retroarcade.model.SnakeGame;

public class MenuActivity extends AppCompatActivity {

    private RadioButton radioButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnEasy = findViewById(R.id.btnEasy);
        Button btnMedium = findViewById(R.id.btnMedium);
        Button btnHard = findViewById(R.id.btnHard);

        radioButtons = findViewById(R.id.radioButtons);

        btnEasy.setOnClickListener(v -> startGame(SnakeGame.Difficulty.EASY));
        btnMedium.setOnClickListener(v -> startGame(SnakeGame.Difficulty.MEDIUM));
        btnHard.setOnClickListener(v -> startGame(SnakeGame.Difficulty.HARD));
    }

    private void startGame(SnakeGame.Difficulty difficulty) {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra("KEY_DIFFICULTY", difficulty.name());

        boolean useButtons = radioButtons.isChecked();
        intent.putExtra("KEY_USE_BUTTONS", useButtons);

        startActivity(intent);
    }
}