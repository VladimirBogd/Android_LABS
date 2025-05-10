package com.example.android_labs.ui.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.android_labs.data.local.StorageHelper
import com.example.android_labs.databinding.FragmentStorageBinding
import com.example.android_labs.ui.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<StorageViewModel>()
    private lateinit var storageHelper: StorageHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storageHelper = StorageHelper(requireContext())

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnExportXlsAppStorage.setOnClickListener { exportToXlsAppStorage() }
            btnImportXlsAppStorage.setOnClickListener { importFromXlsAppStorage() }
            btnExportXlsSharedStorage.setOnClickListener { exportToXlsSharedStorage() }
            btnImportXlsSharedStorage.setOnClickListener { importFromXlsSharedStorage() }
            btnExportPdfSharedStorage.setOnClickListener { exportToPdfSharedStorage() }
            btnImportPdfSharedStorage.setOnClickListener { importFromPdfSharedStorage() }
        }
    }

    private fun exportToXlsAppStorage() {
        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = viewModel.currentExpenses.value
            if (expenses.isNotEmpty()) {
                try {
                    storageHelper.saveToXLSInAppStorage(expenses)
                    showToast("Экспорт в XLS (App Storage) завершен")
                } catch (e: Exception) {
                    showToast("Ошибка экспорта: ${e.localizedMessage}")
                }
            } else {
                showToast("Нет данных для экспорта")
            }
        }
    }

    private fun importFromXlsAppStorage() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = storageHelper.loadFromXLSInAppStorage()
                if (expenses.isNotEmpty()) {
                    viewModel.importExpenses(expenses)
                } else {
                    showToast("Файл не найден или пуст")
                }
            } catch (e: Exception) {
                showToast("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    private fun exportToXlsSharedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Для Android 10+ используем системный пикер
            createXlsFile()
        } else {
            if (hasStoragePermission()) {
                performXlsExport()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun createXlsFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, "expenses_${System.currentTimeMillis()}.xlsx")
        }
        startActivityForResult(intent, XLS_EXPORT_REQUEST_CODE)
    }

    private fun performXlsExport() {
        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = viewModel.currentExpenses.value
            if (expenses.isNotEmpty()) {
                try {
                    storageHelper.saveToXLSInScopedStorage(expenses)
                    showToast("Экспорт в XLS (Shared Storage) завершен")
                } catch (e: Exception) {
                    showToast("Ошибка экспорта: ${e.localizedMessage}")
                }
            } else {
                showToast("Нет данных для экспорта")
            }
        }
    }

    private fun importFromXlsSharedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Для Android 10+ используем системный пикер
            openXlsFile()
        } else {
            if (hasStoragePermission()) {
                performXlsImport()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun openXlsFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        startActivityForResult(intent, XLS_IMPORT_REQUEST_CODE)
    }

    private fun performXlsImport() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = storageHelper.loadFromXLSInStorageScoped()
                if (expenses.isNotEmpty()) {
                    viewModel.importExpenses(expenses)
                } else {
                    showToast("Файл не найден или пуст")
                }
            } catch (e: Exception) {
                showToast("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    private fun exportToPdfSharedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createPdfFile()
        } else {
            if (hasStoragePermission()) {
                performPdfExport()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun createPdfFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "expenses_${System.currentTimeMillis()}.pdf")
        }
        startActivityForResult(intent, PDF_EXPORT_REQUEST_CODE)
    }

    private fun performPdfExport() {
        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = viewModel.currentExpenses.value
            if (expenses.isNotEmpty()) {
                try {
                    storageHelper.saveToPDFInStorageScoped(expenses)
                    showToast("Экспорт в PDF завершен")
                } catch (e: Exception) {
                    showToast("Ошибка экспорта: ${e.localizedMessage}")
                }
            } else {
                showToast("Нет данных для экспорта")
            }
        }
    }

    private fun importFromPdfSharedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openPdfFile()
        } else {
            if (hasStoragePermission()) {
                performPdfImport()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun openPdfFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PDF_IMPORT_REQUEST_CODE)
    }

    private fun performPdfImport() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = storageHelper.loadFromPDFInStorageScoped()
                if (expenses.isNotEmpty()) {
                    viewModel.importExpenses(expenses)
                } else {
                    showToast("Файл не найден или пуст")
                }
            } catch (e: Exception) {
                showToast("Ошибка импорта: ${e.localizedMessage}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            XLS_EXPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val expenses = viewModel.currentExpenses.value
                        if (expenses.isNotEmpty()) {
                            try {
                                storageHelper.saveToXLSInScopedStorage(expenses, uri)
                                showToast("Экспорт в XLS завершен")
                            } catch (e: Exception) {
                                showToast("Ошибка экспорта: ${e.localizedMessage}")
                            }
                        }
                    }
                }
            }
            XLS_IMPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val expenses = storageHelper.readXlsFromUri(uri)
                            if (expenses.isNotEmpty()) {
                                viewModel.importExpenses(expenses)
                            } else {
                                showToast("Файл пуст или не содержит данных")
                            }
                        } catch (e: Exception) {
                            showToast("Ошибка чтения файла: ${e.localizedMessage}")
                        }
                    }
                }
            }
            PDF_EXPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val expenses = viewModel.currentExpenses.value
                        if (expenses.isNotEmpty()) {
                            try {
                                storageHelper.savePdfToUri(expenses, uri)
                                showToast("Экспорт в PDF завершен")
                            } catch (e: Exception) {
                                showToast("Ошибка экспорта: ${e.localizedMessage}")
                            }
                        }
                    }
                }
            }
            PDF_IMPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val expenses = storageHelper.readPdfFromUri(uri)
                            if (expenses.isNotEmpty()) {
                                viewModel.importExpenses(expenses)
                            } else {
                                showToast("Файл пуст или не содержит данных")
                            }
                        } catch (e: Exception) {
                            showToast("Ошибка чтения файла: ${e.localizedMessage}")
                        }
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is StorageViewModel.StorageUiState.Loading -> {
                        // Показать ProgressBar
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is StorageViewModel.StorageUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        showToast(state.message)
                    }
                    is StorageViewModel.StorageUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showToast(state.message)
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Для Android 10+ разрешения не нужны при использовании системного пикера
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showToast("Разрешения получены")
            } else {
                showToast("Разрешения не получены, некоторые функции недоступны")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
        private const val XLS_EXPORT_REQUEST_CODE = 1002
        private const val XLS_IMPORT_REQUEST_CODE = 1003
        private const val PDF_EXPORT_REQUEST_CODE = 1004
        private const val PDF_IMPORT_REQUEST_CODE = 1005
    }
}