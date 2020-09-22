package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.QuizGameApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentQuizOverviewBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QuizOverviewFragment : Fragment() {
    private lateinit var viewModel : QuizGameOverviewViewModel
    private lateinit var binding: FragmentQuizOverviewBinding
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz_overview, container, false)
        viewModel = ViewModelProvider(this).get(QuizGameOverviewViewModel::class.java)
        db = AppDatabase.getDatabase(requireContext())

        val activeGamesAdapter = QuizGameAdapter(requireContext())
        binding.activeGames.adapter = activeGamesAdapter
        viewModel.activeGames.observe(viewLifecycleOwner, Observer { activeGamesAdapter.submitList(it) })

        val finishedGamesAdapter = QuizGameAdapter(requireContext())
        binding.finishedGames.adapter = finishedGamesAdapter
        viewModel.finishedGames.observe(viewLifecycleOwner, Observer { finishedGamesAdapter.submitList(it) })

        val gameId = arguments?.getString(ARG_GAME_ID)
        if (gameId != null) {
            val gameFinished = arguments?.getBoolean(ARG_GAME_FINISHED, false)
            GlobalScope.launch {
                val quizDb = db.quizGameDao()
                if (quizDb.getGame(gameId) == null) {
                    quizDb.insert(QuizGame(gameId, -1))
                }
                if (gameFinished == true) {
                    quizDb.update(QuizGame(gameId, System.currentTimeMillis()))
                }
                fetchGames()
            }
        } else {
            fetchGames()
        }

        binding.startNewGame.setOnClickListener { startNewGame() }
        binding.refreshLayout.setOnRefreshListener { fetchGames() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (binding.finishedGamesLoading.visibility != View.VISIBLE)
            fetchGames()
    }

    private fun fetchGames() {
        binding.activeGamesLoading.visibility = View.VISIBLE
        binding.finishedGamesLoading.visibility = View.VISIBLE
        GlobalScope.launch {
            fetchActiveGamesInfos()
            fetchFinishedGamesInfos() // important to run after each other because fetchActiveGames might produce new finished games
            activity?.runOnUiThread { binding.refreshLayout.isRefreshing = false }
        }
    }

    private fun fetchActiveGamesInfos() {
        val activeGameIds = db.quizGameDao().getActive().map { it.gameId }
        try {
            val (finishedGames, activeGames) = QuizGameApiClient.getMultipleGames(requireActivity(), activeGameIds).partition { it.answers.size == 8 }
            if (finishedGames.isNotEmpty()) {
                val gameIds = finishedGames.map { it.gameId }
                val quizGameDao = db.quizGameDao()
                gameIds.forEach { quizGameDao.update(QuizGame(it, System.currentTimeMillis())) }
            }
            activity?.let { it.runOnUiThread {
                binding.noGamesActive.visibility = if (activeGames.isEmpty()) View.VISIBLE else View.GONE
                binding.activeGamesLoading.visibility = View.GONE
                viewModel.activeGames.value = activeGames.map { game -> QuizGameViewModel(game) }
            }}
        } catch (e: Exception) {
            Log.d(tag, "Error fetching multiple Games: ", e)
            activity?.let{ it.runOnUiThread {
                Toast.makeText(it, R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
            }}
        }
    }

    private fun fetchFinishedGamesInfos() {
        val finishedGameIds = db.quizGameDao().getFinished().take(5).map { it.gameId }
        try {
            val finishedGames = QuizGameApiClient.getMultipleGames(requireActivity(), finishedGameIds)
            activity?.let { it.runOnUiThread {
                binding.noGamesFinished.visibility = if (finishedGames.isEmpty()) View.VISIBLE else View.GONE
                binding.finishedGamesLoading.visibility = View.GONE
                viewModel.finishedGames.value = finishedGames.map { game -> QuizGameViewModel(game) }
            }}
        } catch (e: Exception) {
            Log.d(tag, "Error fetching multiple Games: ", e)
            activity?.let{ it.runOnUiThread {
                Toast.makeText(it, R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
            }}
        }
    }

    private fun startNewGame() {
        QuizActivity.start(requireContext(), null)
    }
}