package com.example.retroarcade;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class GameOverDialog extends DialogFragment {

    private int score;
    private int bestScore;
    private GameOverListener listener;

    public interface GameOverListener {
        void onRestart();
        void onMenu();
    }

    public void setListener(GameOverListener listener) {
        this.listener = listener;
    }

    public static GameOverDialog newInstance(int score, int bestScore) {
        GameOverDialog dialog = new GameOverDialog();
        Bundle args = new Bundle();
        args.putInt("SCORE", score);
        args.putInt("BEST", bestScore);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            score = getArguments().getInt("SCORE");
            bestScore = getArguments().getInt("BEST");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_game_over, null);

        TextView tvCurrentScore = view.findViewById(R.id.tvCurrentScore);
        TextView tvBestScore = view.findViewById(R.id.tvBestScore);
        Button btnRestart = view.findViewById(R.id.btnRestart);
        Button btnMenu = view.findViewById(R.id.btnMenu);

        tvCurrentScore.setText("Score: " + score);
        tvBestScore.setText("Best: " + bestScore);

        btnRestart.setOnClickListener(v -> {
            if (listener != null) listener.onRestart();
            dismiss();
        });

        btnMenu.setOnClickListener(v -> {
            if (listener != null) listener.onMenu();
            dismiss();
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}