package com.example.rukigaapp.ui.dictionary

import android.R
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rukigaapp.data.Categories
import com.example.rukigaapp.databinding.DictionDialogBinding
import com.example.rukigaapp.databinding.FragmentDictionaryBinding
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.ui.dictionary.adapters.DictionAdapter
import kotlinx.coroutines.launch

class DictionaryFragment : Fragment() {
    private var _binding: FragmentDictionaryBinding? = null
    private lateinit var viewModel: DictionaryViewModel
    private lateinit var dictionAdapter: DictionAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)

        val dictionDao = LearnKigaDatabase.getDatabase(requireContext()).dictionDao

        val repository = DictionRepository(dictionDao) // <-- get this from somewhere
        val factory = DictionaryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DictionaryViewModel::class.java]

        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter
        dictionAdapter = DictionAdapter(
            onDictionClicked = { diction ->
                // Handle item click, e.g., show details, edit, delete
                // For now, let's say you want to trigger an edit or view event in ViewModel
                // viewModel.onEvent(DictionEvent.SelectDiction(diction))
                // Or maybe show another dialog
                // showEditDictionDialog(diction)
                android.widget.Toast.makeText(requireContext(), "Clicked: ${diction.english}", android.widget.Toast.LENGTH_SHORT).show()
            }
        )

        // Setup RecyclerView
        binding.dictionRecyclerView.apply {
            adapter = dictionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            // You can also add ItemDecorations for spacing if needed
            // addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }


        // Observe state
//        lifecycleScope.launchWhenStarted {
//            viewModel.state.collect { state ->
//                if (state.isAddingDiction) {
//                    showDictionDialog()
//                }
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Submit the list of dictions to the adapter
                    dictionAdapter.submitList(state.dictions)

                    if (state.isAddingDiction) {
                        showDictionDialog()
                    }
                    // Handle other state changes like error messages, loading indicators, etc.
                    state.errorMessage?.let {
                        android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


        // Trigger ShowDialog event on FAB click
        binding.floatingActionButton2.setOnClickListener {
            viewModel.onEvent(DictionEvent.ShowDialog)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDictionDialog() {
        val dialogBinding = DictionDialogBinding.inflate(layoutInflater)

        //  Correctly reference the AutoCompleteTextView inside the dialog
        val autoCompleteTextView = dialogBinding.autoCompleteTextView

        //Disable Keboard input
        autoCompleteTextView.inputType = InputType.TYPE_NULL
        autoCompleteTextView.keyListener = null

        autoCompleteTextView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                hideKeyboard(view)
            }
        }
        autoCompleteTextView.setOnClickListener { view ->
            hideKeyboard(view)
            // If the dropdown isn't showing automatically, you might need to call:
            // autoCompleteTextView.showDropDown()
        }

        // Set up AutoComplete dropdown (example)
        val categories = Categories.entries.map { it.displayName }
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.autoCompleteTextView.setAdapter(adapter)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialogBinding.saveButton.setOnClickListener {
            // Dismiss the dialog immediately
            dialog.dismiss()
            // Ensure the state reflects the dialog is no longer needed/active
            viewModel.onEvent(DictionEvent.HideDialog)

            val english = dialogBinding.editEnglish.text.toString()
            val rukiga = dialogBinding.editRukiga.text.toString()
            val selectedName = dialogBinding.autoCompleteTextView.text.toString()
            val selectedCategory = Categories.fromDisplayName(selectedName)
            val categoryId = selectedCategory?.id ?: 1

            // Update ViewModel
            viewModel.onEvent(DictionEvent.SetDictionEnglish(english))
            viewModel.onEvent(DictionEvent.SetDictionRukiga(rukiga))
            viewModel.onEvent(DictionEvent.SetDictionCategoryId(categoryId))
            viewModel.onEvent(DictionEvent.SaveDiction)



        }
        dialogBinding.cancelButton.setOnClickListener {
            viewModel.onEvent(DictionEvent.HideDialog)
        }
        dialog.setOnDismissListener {
            // This will be called when the dialog is dismissed for any reason,
            // including outside touch (if setCancelable(true)) or back press.
            viewModel.onEvent(DictionEvent.HideDialog)
        }
        dialog.show()
    }
    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun populateRecycleView() {
        val recyclerView = binding.dictionRecyclerView

    }

}