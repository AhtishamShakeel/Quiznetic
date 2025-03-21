package com.example.quiznetic.quiz

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.aunix.quiznetic.R
import com.aunix.quiznetic.databinding.DialogQuizResultsBinding

class QuizResultsDialog : DialogFragment() {
    private var _binding: DialogQuizResultsBinding? = null
    private val binding get() = _binding!!
    private var onDismissCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            _binding = DialogQuizResultsBinding.inflate(layoutInflater)

            binding.apply {
                val correctAnswers = arguments?.getInt(ARG_CORRECT_ANSWERS) ?: 0
                val totalQuestions = arguments?.getInt(ARG_TOTAL_QUESTIONS) ?: 0
                
                quizCompletedText.text = getString(R.string.quiz_completed)
                pointsEarnedText.text = "You Scored $correctAnswers/$totalQuestions"

                doneButton.setOnClickListener {
                    onDismissCallback?.invoke()
                    dismiss()
                }
            }

            builder.setView(binding.root)
            builder.create().apply {
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        private const val ARG_CORRECT_ANSWERS = "correct_answers"
        private const val ARG_TOTAL_QUESTIONS = "total_questions"

        fun show(
            fragmentManager: FragmentManager,
            correctAnswers: Int,
            totalQuestions: Int,
            onDismiss: () -> Unit
        ) {
            val dialog = QuizResultsDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CORRECT_ANSWERS, correctAnswers)
                    putInt(ARG_TOTAL_QUESTIONS, totalQuestions)
                }
                onDismissCallback = onDismiss
            }
            dialog.show(fragmentManager, "quiz_results")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 