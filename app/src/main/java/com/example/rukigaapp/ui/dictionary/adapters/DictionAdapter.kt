package com.example.rukigaapp.ui.dictionary.adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.data.enums.Categories // Your enum
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

            val categoryEnumEntry = Categories.fromId(diction.categoryId)
            val categoryDot = binding.categoryDot


            try {
                val color = categoryEnumEntry?.color?.toColorInt() ?: Categories.Other.color.toColorInt()
                binding.categoryDot.setBackgroundColor(color)
            } catch (e: Exception) {
                binding.categoryDot.setBackgroundColor(Color.GRAY)
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