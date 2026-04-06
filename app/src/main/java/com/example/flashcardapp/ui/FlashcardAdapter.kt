package com.example.flashcardapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard

class FlashcardAdapter(private val cards: List<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtWord: TextView = view.findViewById(R.id.txtItemWord)
        val txtDef: TextView = view.findViewById(R.id.txtItemDef)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cards[position]
        holder.txtWord.text = card.word
        holder.txtDef.text = card.definition
    }

    override fun getItemCount() = cards.size
}