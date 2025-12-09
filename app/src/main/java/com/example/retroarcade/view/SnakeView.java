package com.example.retroarcade.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import com.example.retroarcade.model.SnakeGame;

public class SnakeView extends View {

    private SnakeGame game;
    private Paint paintSnake;
    private Paint paintApple;
    private Paint paintBomb;
    private int blockSize;

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintSnake = new Paint();
        paintSnake.setColor(Color.parseColor("#4CAF50")); // Green
        paintSnake.setStyle(Paint.Style.FILL);

        paintApple = new Paint();
        paintApple.setColor(Color.parseColor("#F44336")); // Red
        paintApple.setStyle(Paint.Style.FILL);

        paintBomb = new Paint();
        paintBomb.setColor(Color.parseColor("#212121")); // Dark Grey/Black
        paintBomb.setStyle(Paint.Style.FILL);
    }

    public void setGame(SnakeGame game) {
        this.game = game;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (game != null) {
            int targetWidthBlocks = 20;

            int newBlockSize = w / targetWidthBlocks;

            if (newBlockSize < 1) newBlockSize = 1;

            int newHeightBlocks = h / newBlockSize;

            game.setBoardSize(targetWidthBlocks, newHeightBlocks);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) return;

        blockSize = getWidth() / game.getWidth();

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#333333"));
        gridPaint.setStrokeWidth(2);

        for (int x = 0; x <= game.getWidth(); x++) {
            canvas.drawLine(x * blockSize, 0, x * blockSize, getHeight(), gridPaint);
        }
        for (int y = 0; y <= game.getHeight(); y++) {
            canvas.drawLine(0, y * blockSize, getWidth(), y * blockSize, gridPaint);
        }

        Point apple = game.getApplePosition();
        drawCircle(canvas, apple.x, apple.y, Color.parseColor("#F44336"));

        for (Point p : game.getBombs()) {
            drawCircle(canvas, p.x, p.y, Color.parseColor("#757575"));

            Paint redDot = new Paint();
            redDot.setColor(Color.RED);
            redDot.setAntiAlias(true);

            float cx = (p.x * blockSize) + (blockSize / 2f);
            float cy = (p.y * blockSize) + (blockSize / 2f);

            canvas.drawCircle(cx, cy, blockSize / 4f, redDot);
        }

        if (!game.getSnakeBody().isEmpty()) {
            for (Point p : game.getSnakeBody()) {
                drawCircle(canvas, p.x, p.y, Color.parseColor("#4CAF50"));
            }

            Point head = game.getSnakeBody().getFirst();
            drawCircle(canvas, head.x, head.y, Color.parseColor("#2E7D32")); // Dark Green

            drawEyes(canvas, head);
        }
    }

    private void drawCircle(Canvas canvas, int x, int y, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        float cx = (x * blockSize) + (blockSize / 2f);
        float cy = (y * blockSize) + (blockSize / 2f);
        float radius = (blockSize / 2f) - 2;

        canvas.drawCircle(cx, cy, radius, paint);
    }

    private void drawEyes(Canvas canvas, Point head) {
        Paint eyePaint = new Paint();
        eyePaint.setColor(Color.WHITE);

        float cx = (head.x * blockSize) + (blockSize / 2f);
        float cy = (head.y * blockSize) + (blockSize / 2f);
        float radius = blockSize / 8f;

        canvas.drawCircle(cx - radius * 2, cy - radius, radius, eyePaint);
        canvas.drawCircle(cx + radius * 2, cy - radius, radius, eyePaint);
    }

    private void drawBlock(Canvas canvas, int x, int y, Paint paint) {
        int padding = 2;
        canvas.drawRect(
                x * blockSize + padding,
                y * blockSize + padding,
                (x + 1) * blockSize - padding,
                (y + 1) * blockSize - padding,
                paint
        );
    }
}