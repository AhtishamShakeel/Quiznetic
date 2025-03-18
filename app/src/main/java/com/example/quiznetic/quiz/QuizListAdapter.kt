package com.example.quiznetic.quiz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.quiznetic.R
import com.example.quiznetic.data.QuizCategory

class QuizListAdapter(
    private val context: Context,
    private val categories: List<QuizCategory>,
    private val onCategoryClick: (QuizCategory) -> Unit
) : RecyclerView.Adapter<QuizListAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_quiz, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.lottieAnimation)

        fun bind(category: QuizCategory) {
            titleText.text = category.name

            // Set Lottie animation (ensure category object has animation reference)
            lottieAnimation.setAnimation(category.lottieAnimationResId)
            lottieAnimation.playAnimation()
            lottieAnimation.loop(true)

            itemView.setOnClickListener { onCategoryClick(category) }
        }
    }
}
