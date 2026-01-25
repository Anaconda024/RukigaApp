package com.example.rukigaapp.ui.quiz

//noinspection SuspiciousImport
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rukigaapp.data.QuizQuestion
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.data.enums.QuizCategories
import com.example.rukigaapp.databinding.DictionDialogBinding
import com.example.rukigaapp.databinding.FragmentQuizBinding
import com.example.rukigaapp.databinding.QuizSetupDialogBinding
import com.example.rukigaapp.services.repositories.DictionRepository
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.repositories.QuizResultRepository
import com.example.rukigaapp.services.dao.QuizResultDao
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.events.QuizResultEvent
import com.example.rukigaapp.ui.dictionary.DictionaryViewModel
import com.example.rukigaapp.ui.dictionary.DictionaryViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import android.R
import com.example.rukigaapp.databinding.DialogQuizCompletionBinding
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var auth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.closeButton?.setOnClickListener{
            findNavController().navigate(com.example.rukigaapp.R.id.action_nav_quiz_to_nav_home)
        }

        binding.nextButton.setOnClickListener{
            nextQuestion()
        }

        binding.skipButton.setOnClickListener{
            nextQuestion(false)
        }

        // In your QuizFragment.kt inside onViewCreated
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update your other UI components based on the state...
                    // e.g., binding.questionText.text = state.currentQuestion?.Question

                    // Show or hide the loading overlay
                    if (state.isLoading) {
                        showSpinner()
                    } else {
                        SetQuestion()
                        hideSpinner()
                    }
                }
            }
        }
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
        }

        selectCategoryTextView.setOnItemClickListener { _, _, _, _ ->
            dialogBinding.quizTypeLayout.hint = ""
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

        selectQuestionNumberTextView.setOnItemClickListener{_, _, _, _ ->
            dialogBinding.numQuestionsLayout.hint = ""
        }

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

            showSpinner()


            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.setQuizConfig(selectedCategoryInt, selectedQuestionNumber, selectedWritten)
                dialog.dismiss()
            }


            val currentMoment = Clock.System.now()
            val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())


        }
        dialogBinding.cancelButton.setOnClickListener {
            viewModel.onEvent(QuizResultEvent.HideAddQuizResultDialog)
            hideSpinner()
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

    private fun SetQuestion() {
        val currentQuestion = viewModel.currentQuestion ?: run {
            // Show error message
            showError("No questions available")
            return
        }
        binding.answerInputLayout.visibility = if(viewModel.quizConfig?.isWritten == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
        updateUI(currentQuestion)

        binding.nextButton.setOnClickListener() {
            val answer = binding.answerInput.text
            val mark = viewModel.markInput(answer.toString(), currentQuestion)
            nextQuestion()
        }
    }

    private fun showError(message: String) {
        // Show toast, snackbar, or update UI with error message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        // Optionally navigate back or disable UI
    }

    public fun saveQuizResult() {
        val score = viewModel.quizConfig?.numberOfQuestions!! - viewModel.answeredWrong.size
        val dateTaken = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        // Get the actual logged-in user's name
        val userId = getCurrentUserId()

        val quizResult = QuizResult(
            dateTaken = dateTaken.toString(),
            score = score,
            quizCategoryId = viewModel.quizConfig?.quizCategoryId ?: 1,
            userId = userId,
            questionCount = viewModel.quizConfig?.numberOfQuestions!!,
            answeredCorrect = viewModel.answeredCorrect.toString(),
            answeredWrong = viewModel.answeredWrong.toString(),
        )

        viewModel.saveQuizResultToDb(quizResult)

        // Show the summary dialog
        showCompletionDialog(score, userId, dateTaken.toString())
    }

    private fun showCompletionDialog(score: Int, userId: String, date: String) {
        // Inflate dialog binding
        val dialogBinding = DialogQuizCompletionBinding.inflate(layoutInflater)

        // Set data
        dialogBinding.tvUserId.text = "Congratulations, $userId!"
        dialogBinding.tvScore.text = "$score / ${viewModel.quizConfig?.numberOfQuestions}"
        dialogBinding.tvDate.text = "Date: ${date.substringBefore('T')}"

        // Create dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Make dialog background transparent (so custom background shows)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set button click listener
        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(com.example.rukigaapp.R.id.action_nav_quiz_to_nav_home)
        }

        dialog.show()
    }

     fun updateUI(quizQuestion: QuizQuestion, questionNumber: Int = 1, totalQuestions: Int = 10){
        binding.questionText.text = quizQuestion.Question
         binding.questionProgress.text = "Question $questionNumber/$totalQuestions"
    }

    public fun revealAnswer(quizQuestion: QuizQuestion){
        //binding.answerInput.text.setText(quizQuestion.CorrectAnswer)
    }

    fun nextQuestion(notSkipped: Boolean = true){
        val currentQuestion = viewModel.currentQuestion ?: return
        val answer = binding.answerInput.text.toString()
        if(notSkipped){
            viewModel.markInput(answer, currentQuestion)
        }
        else{
            //If question is skipped it is marked as wrong
            viewModel.markInput(answer, currentQuestion,notSkipped= notSkipped)
        }


        viewModel.currentQuestionIndex += 1
        lastQuestionCheck()

        if (viewModel.currentQuestionIndex < viewModel.quizQuestionsState.value.size) {
            val nextQuestion = viewModel.currentQuestion
            if (nextQuestion != null) {
                updateUI(nextQuestion, viewModel.currentQuestionIndex + 1, viewModel.quizQuestionsState.value.size)
            }
        } else {
            // This is the end of the quiz
            saveQuizResult()
        }
    }

    fun showSpinner() {
        binding.loadingSpinnerOverlay.visibility = View.VISIBLE
    }

    fun hideSpinner() {
        binding.loadingSpinnerOverlay.visibility = View.GONE
    }

    fun lastQuestionCheck(){
        if(viewModel.currentQuestionIndex == viewModel.quizQuestionsState.value.size - 1) {
        binding.nextButton.text = getString(com.example.rukigaapp.R.string.finish)
        }
    }

    private fun getCurrentUserId(): String {
        val currentUser = auth.currentUser
        return when {
            // First priority: display name
            !currentUser?.displayName.isNullOrBlank() -> currentUser?.displayName!!
            // Second priority: email (take part before @)
            !currentUser?.email.isNullOrBlank() -> {
                currentUser?.email?.substringBefore('@') ?: "Guest"
            }
            // Fallback: Guest
            else -> "Guest"
        }
    }
}