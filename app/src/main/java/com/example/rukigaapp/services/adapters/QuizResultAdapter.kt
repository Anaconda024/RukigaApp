package com.example.rukigaapp.services.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.data.enums.QuizCategories
import com.example.rukigaapp.databinding.QuizResultItemBinding
import com.example.rukigaapp.ui.home.QuizResultItem
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class QuizResultAdapter(
    private val onQuizResultClicked: (QuizResult) -> Unit
) : ListAdapter<QuizResult, QuizResultAdapter.QuizResultViewHolder>(QuizResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizResultViewHolder {
        val binding = QuizResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizResultViewHolder, position: Int) {
        val quizResult = getItem(position)
        holder.bind(quizResult, onQuizResultClicked)
    }

    class QuizResultViewHolder(
        private val binding: QuizResultItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            quizResult: QuizResult,
            onQuizResultClicked: (QuizResult) -> Unit
        ) {
            // Use the quizCategoryId to determine color
            val quizCategory = QuizCategories.fromId(quizResult.quizCategoryId)

            // Map QuizResultItem properties to the existing binding views
            binding.scorePercentage.text = "${((quizResult.score.toDouble() / quizResult.questionCount) * 100).toInt()}%"

            val formattedDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(quizResult.dateTaken)
                date?.let { outputFormat.format(it) } ?: quizResult.dateTaken
            } catch (e: Exception) {
                quizResult.dateTaken
            }
            binding.quizDate.text = formattedDate

            binding.quizTitle.text = "${quizCategory?.displayName} ( ${quizResult.questionCount} )"



            try {
                val color = quizCategory?.color?.toColorInt() ?: "#c4a57c".toColorInt()
                binding.categoryDot.setBackgroundColor(color)
            } catch (e: Exception) {
                binding.categoryDot.setBackgroundColor(Color.GRAY)
            }

            binding.root.setOnClickListener {
                onQuizResultClicked(quizResult)
            }
        }
    }


}

class QuizResultDiffCallback : DiffUtil.ItemCallback<QuizResult>() {
    override fun areItemsTheSame(oldItem: QuizResult, newItem: QuizResult): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: QuizResult, newItem: QuizResult): Boolean {
        return oldItem == newItem
    }
}