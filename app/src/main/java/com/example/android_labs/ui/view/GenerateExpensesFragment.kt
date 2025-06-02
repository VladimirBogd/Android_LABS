package com.example.android_labs.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android_labs.databinding.FragmentGenerateExpensesBinding
import com.example.android_labs.ui.viewmodel.GenerateExpensesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenerateExpensesFragment : Fragment() {

    private var _binding: FragmentGenerateExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<GenerateExpensesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerateExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.progressBar.max = 100
        binding.progressBar.progress = 0
        binding.progressBar.visibility = View.GONE
        binding.tvProgress.text = "0/0"
    }

    private fun setupClickListeners() {
        binding.btnGenerateThreads.setOnClickListener {
            generateExpenses(useThreads = true)
        }

        binding.btnGenerateCoroutines.setOnClickListener {
            generateExpenses(useThreads = false)
        }

        binding.btnCancelThreads.setOnClickListener {
            viewModel.cancelThreadGeneration()
        }

        binding.btnCancelCoroutines.setOnClickListener {
            viewModel.cancelCoroutineGeneration()
        }
    }

    private fun generateExpenses(useThreads: Boolean) {
        val count = binding.etCount.text.toString().toIntOrNull() ?: 10
        val category = binding.etBaseCategory.text.toString().takeIf { it.isNotBlank() } ?: "Generated"
        val price = binding.etBasePrice.text.toString().toDoubleOrNull() ?: 10.0

        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.max = count

        if (useThreads) {
            viewModel.generateWithThreads(count, category, price) {
                binding.progressBar.visibility = View.GONE
            }
        } else {
            viewModel.generateWithCoroutines(count, category, price) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.progress.collect { progress ->
                        val max = viewModel.maxProgress.value
                        binding.progressBar.progress = progress
                        binding.tvProgress.text = "$progress/$max"
                    }
                }

                launch {
                    viewModel.isRunning.collect { isRunning ->
                        binding.btnGenerateThreads.isEnabled = !isRunning
                        binding.btnGenerateCoroutines.isEnabled = !isRunning
                        binding.btnCancelThreads.isEnabled = isRunning &&
                                (viewModel.currentMethod.value == "Threads")
                        binding.btnCancelCoroutines.isEnabled = isRunning &&
                                (viewModel.currentMethod.value == "Coroutines")

                        if (!isRunning) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.currentMethod.collect { method ->
                        // Обновляем UI в зависимости от текущего метода
                        binding.btnCancelThreads.isEnabled =
                            viewModel.isRunning.value && method == "Threads"
                        binding.btnCancelCoroutines.isEnabled =
                            viewModel.isRunning.value && method == "Coroutines"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}