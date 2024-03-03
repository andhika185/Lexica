package com.serwylo.lexica;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.serwylo.lexica.db.GameMode;
import com.serwylo.lexica.game.Board;
import com.serwylo.lexica.game.Game;

import java.util.Date;

public abstract class GameSaver {

    protected static final int DEFAULT_BOARD_SIZE = 16;
    protected static final int DEFAULT_TIME_REMAINING = 0;
    protected static final int DEFAULT_WORD_COUNT = 0;

    protected static final String ACTIVE_GAME = "activeGame";
    protected static final String WORD_COUNT = "wordCount";
    protected static final String WORDS = "words";
    protected static final String GAME_MODE = "gameMode";
    protected static final String TIME_REMAINING = "timeRemaining";
    protected static final String GAME_BOARD = "gameBoard";
    protected static final String BOARD_SIZE = "boardSize";
    protected static final String STATUS = "status";
    protected static final String START = "startTime";

    public abstract boolean hasSavedGame();

    public abstract int readWordCount();

    public abstract String[] readWords();

    public abstract GameMode readGameMode();

    public abstract int readTimeRemaining();

    public abstract String[] readGameBoard();

    public abstract int readBoardSize();

    public abstract Game.GameStatus readStatus();

    public abstract Date readStart();

    protected static String[] safeSplit(@Nullable String string) {
        return TextUtils.isEmpty(string) ? new String[]{} : string.split(",");
    }

    public abstract void save(Board board, int timeRemaining, GameMode gameMode, String wordListToString, int wordCount, Date start, Game.GameStatus status);
}
