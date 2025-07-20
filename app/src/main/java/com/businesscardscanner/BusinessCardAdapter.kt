package com.businesscardscanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.businesscardscanner.databinding.ItemBusinessCardBinding

class BusinessCardAdapter(
    private val cards: List<BusinessCard>,
    private val onItemClick: (BusinessCard) -> Unit
) : RecyclerView.Adapter<BusinessCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(
        private val binding: ItemBusinessCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: BusinessCard) {
            binding.apply {
                tvName.text = card.name
                tvCompany.text = card.company
                tvPhone.text = card.phone
                tvEmail.text = card.email
                
                root.setOnClickListener {
                    onItemClick(card)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemBusinessCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size
}
