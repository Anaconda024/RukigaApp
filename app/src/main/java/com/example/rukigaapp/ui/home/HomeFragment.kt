package com.example.rukigaapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rukigaapp.R
import com.example.rukigaapp.databinding.FragmentHomeBinding
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.QuizResultRepository
import com.example.rukigaapp.services.events.QuizResultEvent
import com.example.rukigaapp.ui.dictionary.adapters.QuizResultAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var quizResultAdapter: QuizResultAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Crucial for Fragment's options menu
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val quizResultDao = LearnKigaDatabase.getDatabase(requireContext()).quizResultDao()

        val repository = QuizResultRepository(quizResultDao )
        val factory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider( this,  factory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupBottomNavigation()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quizResultAdapter = QuizResultAdapter(
            onQuizResultClicked = { quizResult ->
                viewModel.onEvent(QuizResultEvent.LoadQuizResult(quizResult))
            }
        )
        /* Setup RecyclerView */
        binding.quizHistoryRecyclerView.apply {
            adapter = quizResultAdapter
            layoutManager = LinearLayoutManager(requireContext())
            // You can also add ItemDecorations for spacing if needed
            // addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }

// Setup empty state view click listener
        binding.emptyStateText.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_quiz)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Submit the list of quiz results to the adapter
                    val quizResults = state.quizResults // Assuming your state has a quizResults property
                    quizResultAdapter.submitList(quizResults)

                    // Handle empty state
                    if (quizResults.isEmpty()) {
                        binding.quizHistoryRecyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No quiz results. Start new quiz"
                    } else {
                        binding.quizHistoryRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateText.visibility = View.GONE
                    }

                    // Handle other state changes like error messages, loading indicators, etc.
                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = binding.bottomNavigation // or find by ID

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_quiz -> {
                    Log.d("BottomNav", "Navigating to quiz...")
                    try {
                        findNavController().navigate(R.id.action_nav_home_to_nav_quiz)
                    } catch (e: Exception) {
                        Log.e("BottomNav", "Error navigating to quiz", e)
                    }
                    true
                }
                R.id.bottom_nav_learn -> {
                    findNavController().navigate(R.id.action_nav_home_to_nav_library)
                    true
                }
                R.id.bottom_nav_profile -> {
                    // Handle profile navigation
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

