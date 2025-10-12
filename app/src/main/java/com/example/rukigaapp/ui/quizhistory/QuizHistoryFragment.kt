package com.example.rukigaapp.ui.quizhistory

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rukigaapp.R

class QuizHistoryFragment : Fragment() {

    companion object {
        fun newInstance() = QuizHistoryFragment()
    }

    private val viewModel: QuizHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_quiz_history, container, false)
    }
}