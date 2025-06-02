package com.example.android_labs.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_labs.R
import com.example.android_labs.databinding.FragmentExpenseListBinding
import com.example.android_labs.ui.viewmodel.ExpenseListViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_expenseListFragment_to_addExpenseFragment)
        }
        binding.btnGenerate.setOnClickListener {
            findNavController().navigate(R.id.action_expenseListFragment_to_generateExpensesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
