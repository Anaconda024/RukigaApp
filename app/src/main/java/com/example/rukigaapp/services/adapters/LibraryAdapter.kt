package com.example.rukigaapp.services.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rukigaapp.R
import com.example.rukigaapp.data.KigaWord
import com.example.rukigaapp.databinding.LibraryItemBinding

/**
 * RecyclerView adapter for displaying KigaWord dictionary entries.
 *
 * Features:
 * - Displays word, part of speech, definition, and related words
 * - Handles bookmark toggling with visual feedback
 * - Click handling for word navigation
 * - Efficient updates with DiffUtil
 *
 * IMPORTANT: For this adapter to work properly with all features,
 * update library_item.xml to add the following IDs:
 * - android:id="@+id/partOfSpeech" to the "noun" TextView (line 46-52)
 * - android:id="@+id/definition" to the definition TextView (line 56-63)
 * - android:id="@+id/seeContainer" to the "See:" LinearLayout (line 65-88)
 * - android:id="@+id/seeReference" to the "Humanity" TextView (line 80-86)
 */
class LibraryAdapter(
    private val onWordClicked: (KigaWord) -> Unit,
    private val onBookmarkClicked: (KigaWord) -> Unit
) : ListAdapter<KigaWord, LibraryAdapter.LibraryViewHolder>(LibraryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = LibraryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word, onWordClicked, onBookmarkClicked)
    }

    class LibraryViewHolder(
        private val binding: LibraryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            word: KigaWord,
            onWordClicked: (KigaWord) -> Unit,
            onBookmarkClicked: (KigaWord) -> Unit
        ) {
            // Bind the main word (this ID exists in the XML)
            binding.word1.text = word.word

            // Access views by ID (you need to add these IDs to library_item.xml)
            // If IDs don't exist yet, the app will compile but may crash at runtime
            try {
                // Part of speech
                val partOfSpeechView = binding.root.findViewById<TextView>(R.id.partOfSpeech)
                if (!word.partOfSpeech.isNullOrBlank()) {
                    partOfSpeechView?.apply {
                        text = word.partOfSpeech
                        visibility = View.VISIBLE
                    }
                } else {
                    partOfSpeechView?.visibility = View.GONE
                }

                // Definition
                val definitionView = binding.root.findViewById<TextView>(R.id.definition)
                if (!word.definition.isNullOrBlank()) {
                    definitionView?.apply {
                        text = word.definition
                        visibility = View.VISIBLE
                    }
                } else {
                    definitionView?.visibility = View.GONE
                }

                // See reference section
                val seeContainer = binding.root.findViewById<ViewGroup>(R.id.seeContainer)
                val seeReferenceView = binding.root.findViewById<TextView>(R.id.seeReference)

                if (!word.see.isNullOrBlank()) {
                    seeContainer?.visibility = View.VISIBLE
                    seeReferenceView?.text = word.see
                } else {
                    seeContainer?.visibility = View.GONE
                }

            } catch (e: Exception) {
                // Handle case where IDs don't exist in XML yet
                // At minimum, the word will still display
                e.printStackTrace()
            }

            // Set bookmark icon based on bookmark status
            updateBookmarkIcon(word.isBookmarked)

            // Handle bookmark click
            binding.bookmark1.setOnClickListener {
                onBookmarkClicked(word)
            }

            // Handle word click for navigation/details
            binding.root.setOnClickListener {
                onWordClicked(word)
            }
        }

        /**
         * Updates the bookmark icon and color based on bookmark status
         */
        private fun updateBookmarkIcon(isBookmarked: Boolean) {
            val context = binding.root.context

            if (isBookmarked) {
                // Bookmarked state - filled icon with accent color
                binding.bookmark1.setIconResource(R.drawable.ic_bookmark)
                binding.bookmark1.iconTint = ContextCompat.getColorStateList(
                    context,
                    R.color.colorPrimary // Adjust this color as needed
                )
            } else {
                // Not bookmarked - outline icon with muted color
                binding.bookmark1.setIconResource(R.drawable.ic_bookmark)
                binding.bookmark1.iconTint = ContextCompat.getColorStateList(
                    context,
                    R.color.white // The #8C7E73 color from XML
                )
            }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class LibraryDiffCallback : DiffUtil.ItemCallback<KigaWord>() {
    override fun areItemsTheSame(oldItem: KigaWord, newItem: KigaWord): Boolean {
        // Items are the same if they have the same ID
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: KigaWord, newItem: KigaWord): Boolean {
        // Contents are the same if all fields match
        // This is efficient since KigaWord is a data class
        return oldItem == newItem
    }
}