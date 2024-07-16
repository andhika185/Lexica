package com.serwylo.lexica.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.serwylo.lexica.R
import com.serwylo.lexica.ThemeManager
import com.serwylo.lexica.Util
import com.serwylo.lexica.databinding.HighScoreListItemBinding
import com.serwylo.lexica.databinding.HighScoresBinding
import com.serwylo.lexica.db.*
import com.serwylo.lexica.lang.Language
import com.serwylo.lexica.lang.LanguageLabel
import com.serwylo.lexica.view.CustomTextArrayAdapter

class HighScoresActivity : AppCompatActivity() {

    private lateinit var binding: HighScoresBinding

    private var selectedGameMode: GameMode? = null
    private lateinit var selectedLanguage: Language

    private val languages = Language.allLanguages.values.toList()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        ThemeManager.getInstance().applyTheme(this)
        selectedLanguage = Util().getSelectedLanguageOrDefault(this)

        binding = HighScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.highScoreList.layoutManager = LinearLayoutManager(this)
        binding.highScoreList.setHasFixedSize(true)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.language.adapter = CustomTextArrayAdapter(this, languages) { LanguageLabel.getLabel(this, it) }
        binding.language.setSelection(languages.indexOf(selectedLanguage))

        binding.language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(av: AdapterView<*>?, view: View?, index: Int, id: Long) {
                selectedLanguage = languages[index]
                loadResults()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        loadCurrentGameMode()
    }

    /**
     * Fetches the current game mode from the database asynchronously. Once loaded, will invoke
     * [loadGameModes] on the UI thread again.
     */
    private fun loadCurrentGameMode() {
        val repo = GameModeRepository(applicationContext)

        AsyncTask.execute {
            val current = repo.loadCurrentGameMode()
                    ?: error("No game mode present, should have run database migrations prior to navigating to choose game mode activity.")

            selectedGameMode = current
            runOnUiThread { loadGameModes(current) }
        }
    }

    /**
     * Populates the list of available game modes with those available in the database.
     */
    private fun loadGameModes(selected: GameMode) {
        val repo = GameModeRepository(applicationContext)

        repo.all().observe(this) { modes ->
            binding.gameMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
            binding.gameMode.adapter = CustomTextArrayAdapter(this, modes) { it.label(this@HighScoresActivity) }
            binding.gameMode.setSelection(modes.indexOfFirst { it.gameModeId == selected.gameModeId })
            binding.gameMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(av: AdapterView<*>?, view: View?, index: Int, id: Long) {
                    selectedGameMode = modes[index]
                    loadResults()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun loadResults() {

        val gameMode = selectedGameMode ?: return

        val repo = ResultRepository(applicationContext)
        repo.top10(gameMode, selectedLanguage).observe(this, { results ->
            binding.highScoreList.adapter = ResultsAdapter(results)
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.high_scores_menu, binding.toolbar.menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.view_found_words) {
            viewFoundWords()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun viewFoundWords() {
        startActivity(Intent(this, FoundWordsActivity::class.java))
    }

    inner class ResultsAdapter(val results: List<Result>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = HighScoreListItemBinding.inflate(this@HighScoresActivity.layoutInflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(results[position])
        }

        override fun getItemCount(): Int {
            return results.size
        }

    }

    inner class ViewHolder(private val binding: HighScoreListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(highScore: Result) {

            val context = this@HighScoresActivity

            binding.numPoints.text = highScore.score.toString()
            binding.maxPointsSuffix.text = context.resources.getQuantityString(R.plurals.max_score_points_suffix, highScore.maxScore.toInt(), highScore.maxScore.toInt())
            binding.maxWords.text = context.resources.getQuantityString(R.plurals.num_found_words_out_of_total, highScore.maxNumWords, highScore.numWords, highScore.maxNumWords)

        }

    }

}