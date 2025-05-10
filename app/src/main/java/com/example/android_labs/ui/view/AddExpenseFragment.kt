package com.example.android_labs.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android_labs.databinding.FragmentAddExpenseBinding
import com.example.android_labs.ui.viewmodel.AddExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddExpenseViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            val category = binding.etCategory.text.toString()
            val priceText = binding.etPrice.text.toString()

            if (category.isNotBlank() && priceText.isNotBlank()) {
                try {
                    val price = priceText.toDouble()
                    viewModel.addExpense(category, price)
                    findNavController().navigateUp()
                } catch (e: NumberFormatException) {
                    binding.etPrice.error = "Invalid number"
                }
            } else {
                if (category.isBlank()) binding.etCategory.error = "Required"
                if (priceText.isBlank()) binding.etPrice.error = "Required"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}