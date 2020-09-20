package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.QuizClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
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

        val activeGamesAdapter = QuizGameAdapter()
        binding.activeGames.adapter = activeGamesAdapter
        viewModel.activeGames.observe(viewLifecycleOwner, Observer { activeGamesAdapter.submitList(it) })

        val finishedGamesAdapter = QuizGameAdapter()
        binding.finishedGames.adapter = finishedGamesAdapter
        viewModel.finishedGames.observe(viewLifecycleOwner, Observer { finishedGamesAdapter.submitList(it) })

        GlobalScope.launch { fetchGames() }

        binding.startNewGame.setOnClickListener { startNewGame() }
        return binding.root
    }

    private fun fetchGames() {
        val activeGameIds = db.quizGameDao().getActive().map { it.gameId }
        val finishedGameIds = db.quizGameDao().getFinished().take(5).map { it.gameId }

        GlobalScope.launch {
            val activeGames = QuizClient.getGames(activeGameIds)
            activity?.let { it.runOnUiThread {
              viewModel.activeGames.value = activeGames
            }}
        }

        GlobalScope.launch {
            val finishedGames = QuizClient.getFinishedGamesDummy(finishedGameIds)
            activity?.let { it.runOnUiThread {
                viewModel.finishedGames.value = finishedGames
            }}
        }

    }

    private fun startNewGame() {
        TODO("Start quiz game activity")
    }
}