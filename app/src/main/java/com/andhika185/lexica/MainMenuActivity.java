/*
 *  Copyright (C) 2008-2009 Rev. Johnny Healey <rev.null@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.andhika185.lexica;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.andhika185.lexica.activities.HighScoresActivity;
import com.andhika185.lexica.databinding.SplashBinding;
import com.andhika185.lexica.db.Database;
import com.andhika185.lexica.db.GameMode;
import com.andhika185.lexica.db.GameModeRepository;
import com.andhika185.lexica.db.Result;
import com.andhika185.lexica.db.ResultRepository;
import com.andhika185.lexica.db.migration.MigrateHighScoresFromPreferences;
import com.andhika185.lexica.lang.Language;

import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    protected static final String TAG = "Lexica";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.getInstance().applyTheme(this);
        load();
    }

    private void splashScreen(GameMode gameMode, Result highScore) {

        SplashBinding binding = SplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.newGame.setOnClickListener(v -> {
            Intent intent = new Intent("com.andhika185.lexica.action.NEW_GAME");
            intent.putExtra("gameMode", gameMode);
            startActivity(intent);
        });

        binding.gameModeButton.setOnClickListener(v -> startActivity(new Intent(this, ChooseGameModeActivity.class)));
        //binding.gameModeButton.setText(gameMode.label(this));
        binding.gameModeButton.setText(getResources().getString(R.string.game_mode));

        Language language = new Util().getSelectedLanguageOrDefault(this);
        //binding.languageButton.setText(LanguageLabel.getLabel(this, language));
        binding.languageButton.setText(getResources().getString(R.string.pref_dict));
        binding.languageButton.setOnClickListener(v -> startActivity(new Intent(this, ChooseLexiconActivity.class)));

        if (savedGame()) {
            binding.restoreGame.setOnClickListener(v -> startActivity(new Intent("com.andhika185.lexica.action.RESTORE_GAME")));
            binding.restoreGame.setEnabled(true);
        }

        binding.help.setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));

        binding.preferences.setOnClickListener(v -> startActivity(new Intent("com.andhika185.lexica.action.CONFIGURE")));

        long score = highScore == null ? 0 : highScore.getScore();
        binding.highScore.setText(String.format(Locale.getDefault(), "%d", score));

        // Think we can get away without yet-another button. It will lower the discoverability
        // of the feature, but simplify the home screen a bit more.
        binding.highScoreLabel.setOnClickListener(v -> startActivity(new Intent(this, HighScoresActivity.class)));
        binding.highScore.setOnClickListener(v -> startActivity(new Intent(this, HighScoresActivity.class)));

        Changelog.show(this);
    }

    public void onResume() {
        super.onResume();
        load();
    }

    public boolean savedGame() {
        return new GameSaverPersistent(this).hasSavedGame();
    }

    private void load() {
        AsyncTask.execute(() -> {

            // Force migrations to run prior to querying the database.
            // This is required because we populate default game modes, for which we need at least one to be present.
            // https://stackoverflow.com/a/55067991
            final Database db = Database.get(this);
            db.getOpenHelper().getReadableDatabase();

            Language language = new Util().getSelectedLanguageOrDefault(this);

            final GameModeRepository gameModeRepository = new GameModeRepository(getApplicationContext());
            final ResultRepository resultRepository = new ResultRepository(this);

            if (!gameModeRepository.hasGameModes()) {
                new MigrateHighScoresFromPreferences(this).initialiseDb(db.gameModeDao(), db.resultDao());
            }

            final GameMode gameMode = gameModeRepository.loadCurrentGameMode();
            final Result highScore = resultRepository.findHighScore(gameMode, language);

            runOnUiThread(() -> splashScreen(gameMode, highScore));

        });
    }

}
