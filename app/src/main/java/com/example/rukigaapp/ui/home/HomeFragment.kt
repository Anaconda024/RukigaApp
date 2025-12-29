package com.example.rukigaapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rukigaapp.R
import com.example.rukigaapp.databinding.FragmentHomeBinding
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.QuizResultRepository
import com.example.rukigaapp.services.adapters.CategoryAdapter
import com.example.rukigaapp.services.adapters.QuizResultAdapter
import com.example.rukigaapp.services.events.QuizResultEvent
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var quizResultAdapter: QuizResultAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val database = LearnKigaDatabase.getDatabase(requireContext())
        val quizResultDao = database.quizResultDao()
        val dictionDao = database.dictionDao

        val quizResultRepository = QuizResultRepository(quizResultDao)
        val dictionRepository = DictionRepository(dictionDao)

        val factory = HomeViewModelFactory(quizResultRepository, dictionRepository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupBottomNavigation()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Quiz Results Adapter
        quizResultAdapter = QuizResultAdapter(
            onQuizResultClicked = { quizResult ->
                viewModel.onEvent(QuizResultEvent.LoadQuizResult(quizResult))
            }
        )

        // Setup Category Adapter
        categoryAdapter = CategoryAdapter(
            onCategoryClicked = { category ->
                // Navigate to DictionaryFragment with category filter
                val bundle = bundleOf(
                    "categoryId" to category.id,
                    "categoryName" to category.displayName
                )
                findNavController().navigate(
                    R.id.action_nav_home_to_nav_dictionary,
                    bundle
                )
            }
        )

        // Setup Quiz Results RecyclerView
        binding.quizHistoryRecyclerView.apply {
            adapter = quizResultAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Setup Lessons RecyclerView
        binding.lessonRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Setup empty state view click listener
        binding.emptyStateText.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_quiz)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Submit quiz results
                    quizResultAdapter.submitList(state.quizResults)

                    // Handle empty state for quiz results
                    if (state.quizResults.isEmpty()) {
                        binding.quizHistoryRecyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No quiz results. Start new quiz"
                    } else {
                        binding.quizHistoryRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateText.visibility = View.GONE
                    }

                    // Submit categories
                    categoryAdapter.submitList(state.categories)

                    // Handle error messages
                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = binding.bottomNavigation

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
                    findNavController().navigate(R.id.action_nav_home_to_nav_dictionary)
                    true
                }
                R.id.bottom_nav_profile -> {
                    findNavController().navigate(R.id.action_nav_home_to_profileFragment)
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