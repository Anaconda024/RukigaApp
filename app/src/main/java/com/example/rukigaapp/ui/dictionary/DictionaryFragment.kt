package com.example.rukigaapp.ui.dictionary

import android.R
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rukigaapp.data.DictionState
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.databinding.DictionDialogBinding
import com.example.rukigaapp.databinding.FragmentDictionaryBinding
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.events.DictionEvent
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
                // Convert Diction to DictionState
                val dictionState = DictionState(
                    isAddingDiction = false,
                    id = diction.id,
                    english = diction.english,
                    rukiga = diction.rukiga,
                    categoryId = diction.categoryId
                )

                viewModel.onEvent(DictionEvent.SelectDiction(diction))
                showDictionDialog(dictionState)
            }
        )

        // Setup RecyclerView
        binding.dictionRecyclerView.apply {
            adapter = dictionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            // You can also add ItemDecorations for spacing if needed
            // addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Submit the list of dictions to the adapter
                    dictionAdapter.submitList(state.dictions)

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
            showDictionDialog(DictionState())
        }

        // Trigger ShowDialog event on FAB click
        binding.filterPopUpButton.setOnClickListener {
            showFilterDialog(DictionState())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showFilterDialog(dictionState: DictionState) {

    }

    private fun showDictionDialog(state: DictionState) {
        val dialogBinding = DictionDialogBinding.inflate(layoutInflater)

        // Pre-populate the fields with state data
        dialogBinding.editEnglish.setText(state.english)
        dialogBinding.editRukiga.setText(state.rukiga)
        if(state.categoryId != 0){
            dialogBinding.textInputLayoutCategory.hint = ""
            // Set appropriate title based on whether we're adding or editing
            val title = dialogBinding.title
            title.text = getString(com.example.rukigaapp.R.string.edit_title)
        }

        // Set the category if it exists
        val category = Categories.fromId(state.categoryId)
        dialogBinding.autoCompleteTextView.setText(category?.displayName ?: "")

        // Correctly reference the AutoCompleteTextView inside the dialog
        val autoCompleteTextView = dialogBinding.autoCompleteTextView

        // Disable Keyboard input
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

        // Set up AutoComplete dropdown
        val categories = Categories.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        autoCompleteTextView.setAdapter(adapter)

        // Hide keyboard when clicked
        autoCompleteTextView.setOnClickListener { view ->
            hideKeyboard(view)
            autoCompleteTextView.hint = ""
            autoCompleteTextView.showDropDown() // optional: ensures dropdown opens
        }

        // Clear hint when an item is selected
        autoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
            // the user selected an item → hide the floating hint
            dialogBinding.textInputLayoutCategory.hint = ""
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.submitButton.setOnClickListener {
            val english = dialogBinding.editEnglish.text.toString()
            val rukiga = dialogBinding.editRukiga.text.toString()
            val selectedName = dialogBinding.autoCompleteTextView.text.toString()
            val selectedCategory = Categories.fromDisplayName(selectedName)
            val categoryId = selectedCategory?.id ?: 0

            // Update ViewModel
            viewModel.onEvent(DictionEvent.SetDictionEnglish(english))
            viewModel.onEvent(DictionEvent.SetDictionRukiga(rukiga))
            viewModel.onEvent(DictionEvent.SetDictionCategoryId(categoryId))
            viewModel.onEvent(DictionEvent.SaveDiction)

            // Dismiss the dialog immediately
            dialog.dismiss()
            // Ensure the state reflects the dialog is no longer needed/active
            viewModel.onEvent(DictionEvent.HideDialog)
        }

        dialogBinding.cancelButton.setOnClickListener {
            viewModel.onEvent(DictionEvent.HideDialog)
            dialog.dismiss()
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