package com.example.quiznetic.quiz

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aunix.quiznetic.databinding.ItemQuizBinding
import com.example.quiznetic.data.QuizCategory

class QuizAdapter(
    private val categories: List<QuizCategory>,
    private val onCategoryClick: (QuizCategory) -> Unit
) : RecyclerView.Adapter<QuizAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(val binding: ItemQuizBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(categories[position])
                }
            }
        }

        fun bind(category: QuizCategory) {
            binding.titleText.text = category.name

            // Setup Lottie animation
            binding.lottieAnimation.apply {
                setAnimation(category.lottieAnimationResId)
                playAnimation()
                loop(true)
            }

            // Adjust card height
            val layoutParams = binding.root.layoutParams
            layoutParams.height = dpToPx(binding.root.context, 220)
            binding.root.layoutParams = layoutParams
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
