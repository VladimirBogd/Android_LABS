package com.example.android_labs.ui.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_labs.domain.model.Expense
import com.example.android_labs.domain.usecase.AddExpenseUseCase
import com.example.android_labs.domain.usecase.GenerateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class GenerateExpensesViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val generateExpenseUseCase: GenerateExpenseUseCase
) : ViewModel() {

    // State Flows для UI
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _maxProgress = MutableStateFlow(100)
    val maxProgress: StateFlow<Int> = _maxProgress

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _currentMethod = MutableStateFlow<String?>(null)
    val currentMethod: StateFlow<String?> = _currentMethod

    // Для управления задачами
    private var coroutineJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private var executorService = Executors.newFixedThreadPool(2)

    private val progressCounter = AtomicInteger(0)

    fun generateWithThreads(count: Int, baseCategory: String, basePrice: Double, onComplete: () -> Unit) {
        if (_isRunning.value) return

        // Инициализация
        progressCounter.set(0)
        _isRunning.value = true
        _currentMethod.value = "Threads"
        _progress.value = 0
        _maxProgress.value = count

        executorService.submit {
            try {
                val firstHalfCount = count / 2
                val secondHalfCount = count - firstHalfCount

                // Первая половина
                val firstHalf = (0 until firstHalfCount).map { index ->
                    Thread.sleep(100)
                    updateProgressOnUi(index + 1)
                    "$baseCategory ${index + 1}" to basePrice * (index + 1)
                }

                // Вторая половина
                val secondHalf = (0 until secondHalfCount).map { index ->
                    Thread.sleep(100)
                    updateProgressOnUi(firstHalfCount + index + 1)
                    val base = firstHalf.lastOrNull()?.second ?: basePrice
                    "${baseCategory}-${firstHalfCount + index + 1}" to base + (index + 1) * 10
                }

                updateProgressOnUi(count)
                (firstHalf + secondHalf).forEach { (category, price) ->
                    generateExpenseUseCase(Expense(category = category, price = price))
                }
                completeOnUi(onComplete)
            } catch (e: Exception) {
                handleErrorOnUi(e)
            }
        }
    }

    private fun updateProgressOnUi(value: Int) {
        val newValue = value.coerceAtMost(_maxProgress.value)
        if (progressCounter.getAndSet(newValue) != newValue) {
            handler.post {
                _progress.value = newValue
            }
        }
    }

    private fun completeOnUi(action: () -> Unit) {
        handler.post {
            action()
            _isRunning.value = false
        }
    }

    private fun handleErrorOnUi(e: Exception) {
        handler.post {
            _isRunning.value = false
        }
    }

    fun cancelThreadGeneration() {
        executorService.shutdownNow()
        executorService = Executors.newFixedThreadPool(2)
        _isRunning.value = false
    }

    fun generateWithCoroutines(count: Int, baseCategory: String, basePrice: Double, onComplete: () -> Unit) {
        if (_isRunning.value) return

        _isRunning.value = true
        _currentMethod.value = "Coroutines"
        _progress.value = 0
        _maxProgress.value = count

        coroutineJob = viewModelScope.launch {
            try {
                // Общий атомарный счетчик прогресса
                val progressCounter = AtomicInteger(0)

                // Первая часть в IO dispatcher
                val firstHalf = withContext(Dispatchers.IO) {
                    (0 until count / 2).map { index ->
                        delay(100)

                        // Обновляем прогресс через атомарный счетчик
                        val newProgress = progressCounter.incrementAndGet()
                        withContext(Dispatchers.Main) {
                            _progress.value = newProgress.coerceAtMost(count)
                        }

                        "$baseCategory ${index + 1}" to basePrice * (index + 1)
                    }
                }

                // Вторая часть в Default dispatcher
                val secondHalf = withContext(Dispatchers.Default) {
                    (count / 2 until count).map { index ->
                        delay(100)

                        // Обновляем через тот же счетчик
                        val newProgress = progressCounter.incrementAndGet()
                        withContext(Dispatchers.Main) {
                            _progress.value = newProgress.coerceAtMost(count)
                        }

                        val base = firstHalf.lastOrNull()?.second ?: basePrice
                        "${baseCategory}-${index + 1}" to base + (index + 1) * 10
                    }
                }

                withContext(Dispatchers.Main) {
                    _progress.value = count
                }

                (firstHalf + secondHalf).forEach { (category, price) ->
                    addExpenseUseCase(Expense(category = category, price = price))
                }
            } finally {
                onComplete()
                resetState()
            }
        }
    }

    fun cancelCoroutineGeneration() {
        coroutineJob?.cancel()
        resetState()
    }

    private fun resetState() {
        _isRunning.value = false
        _currentMethod.value = null
    }

    override fun onCleared() {
        super.onCleared()
        coroutineJob?.cancel()
        executorService.shutdownNow()
        handler.removeCallbacksAndMessages(null)
    }
}