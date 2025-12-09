package com.example.retroarcade.model;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SnakeGame {
    private int NUM_BLOCKS_WIDE = 20;
    private int NUM_BLOCKS_HIGH = 30;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    private Difficulty currentDifficulty;

    private int score;
    private int lives;
    private boolean isPlaying;

    private final LinkedList<Point> snakeBody = new LinkedList<>();
    private Point applePosition;

    private final List<Point> bombs = new ArrayList<>();

    public enum Direction { UP, RIGHT, DOWN, LEFT }
    private Direction currentDirection = Direction.RIGHT;

    public SnakeGame() {
        startNewGame(Difficulty.EASY);
    }

    public void setBoardSize(int widthInBlocks, int heightInBlocks) {
        this.NUM_BLOCKS_WIDE = widthInBlocks;
        this.NUM_BLOCKS_HIGH = heightInBlocks;
    }

    public void startNewGame(Difficulty difficulty) {
        this.currentDifficulty = difficulty;

        snakeBody.clear();
        snakeBody.add(new Point(10, 10));
        snakeBody.add(new Point(9, 10));
        snakeBody.add(new Point(8, 10));

        score = 0;
        currentDirection = Direction.RIGHT;

        setupDifficultyRules();

        spawnApple();
        isPlaying = true;
    }

    private void setupDifficultyRules() {
        bombs.clear();
        switch (currentDifficulty) {
            case EASY:
                lives = 1;
                break;
            case MEDIUM:
                lives = 3;
                spawnBombs(5);
                break;
            case HARD:
                lives = 1;
                spawnBombs(10);
                break;
        }
    }

    private void spawnBombs(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(NUM_BLOCKS_WIDE);
                y = random.nextInt(NUM_BLOCKS_HIGH);
            } while (isSnakeBody(x, y) || (x == 10 && y == 10));
            bombs.add(new Point(x, y));
        }
    }

    private void spawnApple() {
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(NUM_BLOCKS_WIDE);
            y = random.nextInt(NUM_BLOCKS_HIGH);
        } while (isSnakeBody(x, y) || isBomb(x, y));

        applePosition = new Point(x, y);
    }

    public void update() {
        if (!isPlaying) return;

        Point head = snakeBody.getFirst();
        Point newHead = new Point(head.x, head.y);

        switch (currentDirection) {
            case UP:    newHead.y--; break;
            case RIGHT: newHead.x++; break;
            case DOWN:  newHead.y++; break;
            case LEFT:  newHead.x--; break;
        }

        if (newHead.x < 0 || newHead.x >= NUM_BLOCKS_WIDE ||
                newHead.y < 0 || newHead.y >= NUM_BLOCKS_HIGH) {
            isPlaying = false;
            return;
        }

        if (isSnakeBody(newHead.x, newHead.y)) {
            isPlaying = false;
            return;
        }

        if (isBomb(newHead.x, newHead.y)) {
            lives--;
            bombs.remove(new Point(newHead.x, newHead.y));

            if (lives <= 0) {
                isPlaying = false;
                return;
            }
        }

        snakeBody.addFirst(newHead);

        if (newHead.equals(applePosition)) {
            score++;
            spawnApple();
            if (currentDifficulty == Difficulty.HARD) {
                spawnBombs(1);
            }
        } else {
            snakeBody.removeLast();
        }
    }

    public void setDirection(Direction newDir) {
        if (currentDifficulty == Difficulty.HARD) {
            switch (newDir) {
                case UP:    newDir = Direction.DOWN; break;
                case DOWN:  newDir = Direction.UP; break;
                case LEFT:  newDir = Direction.RIGHT; break;
                case RIGHT: newDir = Direction.LEFT; break;
            }
        }

        if ((currentDirection == Direction.UP && newDir == Direction.DOWN) ||
                (currentDirection == Direction.DOWN && newDir == Direction.UP) ||
                (currentDirection == Direction.LEFT && newDir == Direction.RIGHT) ||
                (currentDirection == Direction.RIGHT && newDir == Direction.LEFT)) {
            return;
        }
        currentDirection = newDir;
    }

    private boolean isSnakeBody(int x, int y) {
        for (Point p : snakeBody) {
            if (p.x == x && p.y == y) return true;
        }
        return false;
    }

    private boolean isBomb(int x, int y) {
        for (Point p : bombs) {
            if (p.x == x && p.y == y) return true;
        }
        return false;
    }

    public LinkedList<Point> getSnakeBody() { return snakeBody; }
    public List<Point> getBombs() { return bombs; }
    public Point getApplePosition() { return applePosition; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isPlaying() { return isPlaying; }
    public int getWidth() { return NUM_BLOCKS_WIDE; }
    public int getHeight() { return NUM_BLOCKS_HIGH; }
}