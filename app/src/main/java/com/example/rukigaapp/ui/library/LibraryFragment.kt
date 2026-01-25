package com.example.rukigaapp.ui.library

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.databinding.FragmentLibraryBinding
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.adapters.LibraryAdapter
import com.example.rukigaapp.services.events.KigaWordEvent

import com.example.rukigaapp.services.repositories.LibraryRepository
import kotlinx.coroutines.launch

/**
 * Fragment for browsing the Rukiga-English dictionary
 *
 * Features:
 * - Paginated word list (100 items per page)
 * - Real-time search (word + definition)
 * - Bookmark management
 * - Infinite scroll pagination
 */
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        val kigaWordDao = LearnKigaDatabase.getDatabase(requireContext()).kigaWordDao()
        val repository = LibraryRepository(kigaWordDao)
        val factory = LibraryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LibraryViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Setup RecyclerView with adapter and pagination
     */
    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(
            onWordClicked = { word ->
                viewModel.onEvent(KigaWordEvent.OnWordClicked(word))
                // Optionally navigate to detail screen
                // findNavController().navigate(
                //     LibraryFragmentDirections.actionLibraryToWordDetail(word.id)
                // )
            },
            onBookmarkClicked = { word ->
                viewModel.onEvent(KigaWordEvent.ToggleBookmark(word))
            }
        )

        layoutManager = LinearLayoutManager(requireContext())

        binding.libraryRecyclerView.apply {
            adapter = libraryAdapter
            layoutManager = this@LibraryFragment.layoutManager



        }
        // Add scroll listener for pagination
        binding.libraryRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // You must get the layoutManager and cast it
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Logic for infinite scroll / pagination
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                    // Call your ViewModel to load more words
                    viewModel.onEvent(KigaWordEvent.LoadMoreWords())
                }
            }
        })
    }


    /**
     * Setup search bar with real-time text watching
     */
    private fun setupSearchBar() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""

                // Update cancel icon visibility
                binding.cancelIcon.isVisible = query.isNotEmpty()

                // Trigger search
                viewModel.onEvent(KigaWordEvent.SearchWords(query))
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle IME search action
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    /**
     * Setup click listeners for UI elements
     */
    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Cancel/Clear search
        binding.cancelIcon.setOnClickListener {
            binding.searchInput.text.clear()
            viewModel.onEvent(KigaWordEvent.ClearSearch)
            hideKeyboard()
        }

        // Optional: Make search icon clickable to focus search input
        binding.searchIcon.setOnClickListener {
            binding.searchInput.requestFocus()
            showKeyboard(binding.searchInput)
        }
    }

    /**
     * Observe ViewModel state and update UI
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Update adapter with new word list
                    libraryAdapter.submitList(state.words)

                    // Update loading state
                    isLoading = state.isInitialLoading || state.isLoadingMore

                    // Show loading indicator (you can add a ProgressBar to XML if needed)
                    // binding.progressBar?.isVisible = state.isInitialLoading

                    // Handle error messages
                    state.errorMessage?.let { error ->
                        //Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }

                    // Update search result count (optional - add TextView to XML)
                    // if (state.isSearchActive) {
                    //     binding.searchResultCount?.text = "${state.words.size} results"
                    // }

                    // Handle empty state (optional - add empty view to XML)
                    // binding.emptyView?.isVisible = state.words.isEmpty() && !state.isInitialLoading
                }
            }
        }
    }

    /**
     * Hide soft keyboard
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    /**
     * Show soft keyboard for a view
     */
    private fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}