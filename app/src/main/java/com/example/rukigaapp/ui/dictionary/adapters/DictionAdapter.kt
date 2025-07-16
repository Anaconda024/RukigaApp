package com.example.rukigaapp.ui.dictionary.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.data.Category // Assuming you have this
import com.example.rukigaapp.data.Categories // Your enum
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.databinding.DictionItemBinding
import androidx.core.graphics.toColorInt

class DictionAdapter(
    private val onDictionClicked: (Diction) -> Unit,
    //private val getCategoryById: suspend (Int) -> Unit // Function to fetch category details
) : ListAdapter<Diction, DictionAdapter.DictionViewHolder>(DictionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionViewHolder {
        val binding = DictionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DictionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DictionViewHolder, position: Int) {
        val diction = getItem(position)
        holder.bind(diction, onDictionClicked)
    }

    class DictionViewHolder(
        private val binding: DictionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            diction: Diction,
            onDictionClicked: (Diction) -> Unit,
            //getCategoryById: suspend (Int) -> Category?
            // getCategoryById is likely not needed for this simple color mapping anymore
        ) {
            binding.textRukiga.text = diction.rukiga
            binding.textEnglish.text = diction.english

            val categoryColorButton = binding.categoryColor

            // Find the category from the enum using the diction's categoryId
            val categoryEnumEntry = Categories.fromId(diction.categoryId)

            if (categoryEnumEntry != null) {
                // Parse the color string from the enum and set it
                try {
                    categoryColorButton.setBackgroundColor(categoryEnumEntry.color.toColorInt())
                } catch (e: IllegalArgumentException) {
                    // Handle cases where the color string might be invalid
                    // Set a default color in case of error
                    categoryColorButton.setBackgroundColor(Color.GRAY) // Or some other default
                    // Log the error if needed: Log.e("DictionAdapter", "Invalid color string: ${categoryEnumEntry.color}", e)
                }
            } else {
                // Handle cases where diction.categoryId doesn't match any known category
                // This could be your "Other" category or a default color
                try {
                    categoryColorButton.setBackgroundColor(Categories.Other.color.toColorInt())
                } catch (e: IllegalArgumentException) {
                    categoryColorButton.setBackgroundColor(Color.LTGRAY) // Fallback default
                }
            }

            binding.root.setOnClickListener {
                onDictionClicked(diction)
            }
        }
    }
}

class DictionDiffCallback : DiffUtil.ItemCallback<Diction>() {
    override fun areItemsTheSame(oldItem: Diction, newItem: Diction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Diction, newItem: Diction): Boolean {
        return oldItem == newItem
    }
}