package com.example.rukigaapp.ui.quiz

import android.R
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rukigaapp.data.QuizQuestion
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.data.enums.QuizCategories
import com.example.rukigaapp.databinding.DictionDialogBinding
import com.example.rukigaapp.databinding.FragmentQuizBinding
import com.example.rukigaapp.databinding.QuizSetupDialogBinding
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.QuizResultRepository
import com.example.rukigaapp.services.dao.QuizResultDao
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.events.QuizResultEvent
import com.example.rukigaapp.ui.dictionary.DictionaryViewModel
import com.example.rukigaapp.ui.dictionary.DictionaryViewModelFactory
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: QuizViewModel
    private lateinit var quizDao: QuizResultDao
    private lateinit var repository: QuizResultRepository
    private lateinit var factory: QuizViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val quizDao = LearnKigaDatabase.getDatabase(requireContext()).quizResultDao()
        val dictionDao = LearnKigaDatabase.getDatabase(requireContext()).dictionDao
        val dictionRepository = DictionRepository(dictionDao)
        val repository = QuizResultRepository(quizDao)
        val factory = QuizViewModelFactory(repository, dictionRepository)

        viewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]

        showQuizDialog()

        var cancelButton = binding.closeButton
        cancelButton.setOnClickListener {
            //Close fragment and end viewmodel and got to home fragment
            //but first show confirmation dialog to close quiz fragment
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showQuizDialog() {
        val dialogBinding = QuizSetupDialogBinding.inflate(layoutInflater)


        //  Correctly reference the AutoCompleteTextView inside the dialog
        val selectCategoryTextView = dialogBinding.selectCategoryTextView

        //Disable Keboard input
        selectCategoryTextView.inputType = InputType.TYPE_NULL
        selectCategoryTextView.keyListener = null

        selectCategoryTextView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                hideKeyboard(view)
            }
        }
        selectCategoryTextView.setOnClickListener { view ->
            hideKeyboard(view)
            // If the dropdown isn't showing automatically, you might need to call:
            // selectCategoryTextView.showDropDown()
        }

        // Set up AutoComplete dropdown (example)
        val categories = QuizCategories.entries.map { it.displayName }
        val adapter1 =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.selectCategoryTextView.setAdapter(adapter1)


        // Second dropdown (Question Numbers)
        val selectQuestionNumberTextView = dialogBinding.selectQuestionNumberTextView
        selectQuestionNumberTextView.inputType = InputType.TYPE_NULL
        selectQuestionNumberTextView.keyListener = null
        //Set up select number of question
        val questionNumbers = listOf("10", "20", "30") // Simple list instead of lambda
        val adapter2 = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, questionNumbers)
        selectQuestionNumberTextView.setAdapter(adapter2)


        // Second dropdown (select if typed)
        val selectWrittenTextView = dialogBinding.typedSwitch


        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        dialogBinding.submitButton.setOnClickListener {
            // Dismiss the dialog immediately
            val selectedCategory = dialogBinding.selectCategoryTextView.text.toString()
            val selectedCategoryInt = QuizCategories.entries.find { it.displayName == selectedCategory }?.id ?: 3
            val selectedQuestionNumber = dialogBinding.selectQuestionNumberTextView.text.toString().toInt()
            val selectedWritten = selectWrittenTextView.isChecked


            viewModel.setQuizConfig(selectedCategoryInt, selectedQuestionNumber, selectedWritten)
            //SetQuestion()
            //Submit
            dialog.dismiss()
            // Ensure the state reflects the dialog is no longer needed/active


            val currentMoment = Clock.System.now()
            val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())


        }
        dialogBinding.cancelButton.setOnClickListener {
            viewModel.onEvent(QuizResultEvent.HideAddQuizResultDialog)
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            // This will be called when the dialog is dismissed for any reason,
            // including outside touch (if setCancelable(true)) or back press.
            viewModel.onEvent(QuizResultEvent.HideAddQuizResultDialog)
        }
        dialog.show()
    }
    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }



    public fun saveQuizResult() {
        val quizResult = QuizResult(
            // Get todays date and turn it into a string
            dateTaken = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            //minus the answerd wrong from the total questions in quiz config to get the score
            score = 0,
            //getfrom selected quiz category, probably from viewmodel quiz config
            quizCategoryId = viewModel.quizConfig?.quizCategoryId ?: 1,
            userId = "Ankunda",
            //get from qiz config
            questionCount = 0,
            answeredCorrect = viewModel.answeredCorrect.toString(),
            answeredWrong = viewModel.answeredWrong.toString(),
        )

        viewModel.saveQuizResultToDb(quizResult)
    }

    public fun updateUI(quizQuestion: QuizQuestion){
        binding.questionText.text = quizQuestion.Question
    }

    public fun revealAnswer(quizQuestion: QuizQuestion){
        //binding.answerInput.text.setText(quizQuestion.CorrectAnswer)
    }

    public fun nextQuestion(){
        viewModel.currentQuestionIndex += 1
        //update the ui with the new question
        var currentQuestion = viewModel.currentQuestion
    }
}