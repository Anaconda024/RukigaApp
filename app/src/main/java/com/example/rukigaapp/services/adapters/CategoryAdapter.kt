package com.example.rukigaapp.services.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.data.CategoryItem
import com.example.rukigaapp.databinding.LessonItemBinding

class CategoryAdapter(
    private val onCategoryClicked: (CategoryItem) -> Unit
) : ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = LessonItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onCategoryClicked)
    }

    class CategoryViewHolder(
        private val binding: LessonItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            category: CategoryItem,
            onCategoryClicked: (CategoryItem) -> Unit
        ) {
            // Set category name
            binding.lessonTitle.text = category.displayName

            // Set word count
            binding.lessonDescription.text = "${category.wordCount} words"

            // Set card background color
            try {
                val color = category.color.toColorInt()
                binding.lessonCard.setCardBackgroundColor(color)
            } catch (e: Exception) {
                binding.lessonCard.setCardBackgroundColor(Color.GRAY)
            }

            // Set click listener
            binding.root.setOnClickListener {
                onCategoryClicked(category)
            }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
    override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
        return oldItem == newItem
    }
}