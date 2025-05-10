package com.example.android_labs.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_labs.R
import com.example.android_labs.databinding.FragmentExpenseListBinding
import com.example.android_labs.ui.viewmodel.ExpenseListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ExpenseListViewModel>()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { expense ->
            viewModel.deleteExpense(expense)
        }

        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExpenseListFragment.adapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.expenses.collectLatest { expenses ->
                adapter.submitList(expenses)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.totalExpense.collectLatest { total ->
                binding.tvTotal.text = getString(R.string.total_expense, total ?: 0.0)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_expenseListFragment_to_addExpenseFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}