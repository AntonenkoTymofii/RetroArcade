package com.example.retroarcade;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.retroarcade.model.SnakeGame;
import com.example.retroarcade.view.SnakeView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private SnakeGame game;
    private SnakeView snakeView;
    private TextView scoreText;

    private Button btnPause;
    private TextView txtPausedOverlay;
    private boolean isPaused = false;

    private LinearLayout controlsLayout;
    private TextView txtSwipeHint;
    private boolean useButtons;

    private SnakeGame.Difficulty currentDifficulty;
    private Handler handler;
    private Runnable gameLoop;

    private final long START_SPEED = 200;
    private final long MIN_SPEED_DELAY = 60;
    private final long SPEED_DECREASE_PER_POINT = 5;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snakeView = findViewById(R.id.snakeView);
        scoreText = findViewById(R.id.scoreText);
        controlsLayout = findViewById(R.id.controlsLayout);
        txtSwipeHint = findViewById(R.id.txtSwipeHint);

        btnPause = findViewById(R.id.btnPause);
        txtPausedOverlay = findViewById(R.id.txtPausedOverlay);

        btnPause.setOnClickListener(v -> togglePause());

        String diffString = getIntent().getStringExtra("KEY_DIFFICULTY");
        if (diffString != null) {
            currentDifficulty = SnakeGame.Difficulty.valueOf(diffString);
        } else {
            currentDifficulty = SnakeGame.Difficulty.EASY;
        }

        useButtons = getIntent().getBooleanExtra("KEY_USE_BUTTONS", false);

        if (useButtons) {
            controlsLayout.setVisibility(View.VISIBLE);
            txtSwipeHint.setVisibility(View.GONE);
            setupButtonListeners();
        } else {
            controlsLayout.setVisibility(View.GONE);
            txtSwipeHint.setVisibility(View.VISIBLE);
        }

        game = new SnakeGame();
        game.startNewGame(currentDifficulty);
        snakeView.setGame(game);

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        snakeView.setOnTouchListener(this);

        setupGameLoop();
    }

    private void togglePause() {
        if (!game.isPlaying()) return;

        if (isPaused) {
            isPaused = false;
            startGame();
            btnPause.setText("⏸");
            txtPausedOverlay.setVisibility(View.GONE);
        } else {
            isPaused = true;
            stopGame();
            btnPause.setText("▶");
            txtPausedOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonListeners() {
        findViewById(R.id.btnUp).setOnClickListener(v -> handleInput(SnakeGame.Direction.UP));
        findViewById(R.id.btnDown).setOnClickListener(v -> handleInput(SnakeGame.Direction.DOWN));
        findViewById(R.id.btnLeft).setOnClickListener(v -> handleInput(SnakeGame.Direction.LEFT));
        findViewById(R.id.btnRight).setOnClickListener(v -> handleInput(SnakeGame.Direction.RIGHT));
    }

    private void handleInput(SnakeGame.Direction dir) {
        if (game.isPlaying() && !isPaused) {
            game.setDirection(dir);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game.isPlaying() && !isPaused) {
            togglePause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGame();
        if (game.isPlaying()) {
            isPaused = true;
            btnPause.setText("▶");
            txtPausedOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void setupGameLoop() {
        handler = new Handler(Looper.getMainLooper());
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (game.isPlaying()) {
                    game.update();
                    snakeView.invalidate();
                    updateScoreUI();
                    long currentSpeed = calculateCurrentSpeed();
                    handler.postDelayed(this, currentSpeed);
                } else {
                    saveScore();
                    stopGame();

                    isPaused = false;
                    btnPause.setText("⏸");
                    txtPausedOverlay.setVisibility(View.GONE);

                    showGameOverDialog();
                }
            }
        };
    }

    private void showGameOverDialog() {
        int currentScore = game.getScore();
        int bestScore = getBestScore();

        GameOverDialog dialog = GameOverDialog.newInstance(currentScore, bestScore);

        dialog.setListener(new GameOverDialog.GameOverListener() {
            @Override
            public void onRestart() {
                game.startNewGame(currentDifficulty);
                startGame();
            }

            @Override
            public void onMenu() {
                finish();
            }
        });

        dialog.show(getSupportFragmentManager(), "GameOverDialog");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!useButtons) {
            if (gestureDetector.onTouchEvent(event)) return true;
        }

        return true;
    }

    private void updateScoreUI() {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < game.getLives(); i++) hearts.append("❤️");
        int best = getBestScore();
        if (game.getScore() > best) best = game.getScore();
        scoreText.setText("Score: " + game.getScore() + " (Best: " + best + ")\n" + "Lives: " + hearts.toString());
    }

    private long calculateCurrentSpeed() {
        long currentDelay = START_SPEED - (game.getScore() * SPEED_DECREASE_PER_POINT);
        if (currentDelay < MIN_SPEED_DELAY) return MIN_SPEED_DELAY;
        return currentDelay;
    }

    private void saveScore() {
        SharedPreferences prefs = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        String key = "HIGH_SCORE_" + currentDifficulty.name();
        if (game.getScore() > prefs.getInt(key, 0)) {
            prefs.edit().putInt(key, game.getScore()).apply();
        }
    }

    private int getBestScore() {
        SharedPreferences prefs = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        return prefs.getInt("HIGH_SCORE_" + currentDifficulty.name(), 0);
    }

    private void startGame() {
        stopGame();
        handler.post(gameLoop);
    }

    private void stopGame() {
        handler.removeCallbacks(gameLoop);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (useButtons || isPaused) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) game.setDirection(SnakeGame.Direction.RIGHT);
                    else game.setDirection(SnakeGame.Direction.LEFT);
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) game.setDirection(SnakeGame.Direction.DOWN);
                    else game.setDirection(SnakeGame.Direction.UP);
                    return true;
                }
            }
            return false;
        }
    }
}