package com.example.android_labs.data.local

import com.example.android_labs.domain.model.Expense

// Android Framework - Основные компоненты
import android.content.Context
import android.util.Log
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.database.Cursor
import android.os.Build

// Apache POI
import org.apache.poi.xssf.usermodel.XSSFWorkbook

// Java I/O
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// Для PDF
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment

class StorageHelper(private val context: Context) {

    // ========== App-specific Storage (XLS) ==========
    fun saveToXLSInAppStorage(expenses: List<Expense>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Expenses")

        // Заголовки столбцов
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Category")
        headerRow.createCell(2).setCellValue("Price")

        // Заполнение данными
        expenses.forEachIndexed { index, expense ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(expense.id.toDouble())
            row.createCell(1).setCellValue(expense.category)
            row.createCell(2).setCellValue(expense.price)
        }

        // Сохранение файла
        val file = File(context.filesDir, "expenses.xlsx")
        // Открываем FileOutputStream, который перезапишет файл, если он существует
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
        Log.d("StorageHelper", "Saved to app storage: ${file.absolutePath}")
    }

    fun loadFromXLSInAppStorage(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val file = File(context.filesDir, "expenses.xlsx")

        if (file.exists()) {
            FileInputStream(file).use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)
                Log.d("StorageHelper", "Файл expenses.xlsx найден")

                // Чтение данных из файла
                for (row in 1..sheet.lastRowNum) { // Начинаем с 1, чтобы пропустить заголовки
                    val id = sheet.getRow(row).getCell(0).numericCellValue.toLong()
                    val category = sheet.getRow(row).getCell(1).stringCellValue
                    val price = sheet.getRow(row).getCell(2).numericCellValue
                    expenses.add(Expense(id, category, price))
                }
                workbook.close()
                Log.d("StorageHelper", "Данные загружены из XLS файла: ${file.absolutePath}")
            }
        } else {
            Log.d("StorageHelper", "Файл не найден: ${file.absolutePath}")
        }
        return expenses
    }

    // ========== Scoped Storage (XLS) ==========
    fun saveToXLSInScopedStorage(expenses: List<Expense>, uri: Uri? = null) {
        // Создаем рабочую книгу
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Expenses")

        // Заголовки столбцов
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Категория")
        headerRow.createCell(2).setCellValue("Цена")

        // Заполнение данными
        for ((index, expense) in expenses.withIndex()) {
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(expense.id.toDouble())
            row.createCell(1).setCellValue(expense.category)
            row.createCell(2).setCellValue(expense.price)
        }

        try {
            when {
                uri != null -> {
                    // Save to specific Uri (Android 10+)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        workbook.write(outputStream)
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    // Save to Documents directory (Android 10+)
                    saveXlsToMediaStore(workbook)
                }
                else -> {
                    // Legacy storage (pre-Android 10)
                    saveXlsToLegacyStorage(workbook)
                }
            }
            Log.d("StorageHelper", "XLS saved successfully")
        } catch (e: Exception) {
            Log.e("StorageHelper", "Error saving XLS", e)
            throw e
        } finally {
            workbook.close()
        }
    }

    private fun saveXlsToMediaStore(workbook: XSSFWorkbook) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "expenses_${System.currentTimeMillis()}.xlsx")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Expenses")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                workbook.write(outputStream)
            }
        }
    }

    private fun saveXlsToLegacyStorage(workbook: XSSFWorkbook) {
        val downloadsDir = File(
            context.getExternalFilesDir(null),
            "Expenses"
        ).apply { mkdirs() }

        val file = File(downloadsDir, "expenses_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
    }

    fun readXlsFromUri(uri: Uri): List<Expense> {
        val expenses = mutableListOf<Expense>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (row in 1..sheet.lastRowNum) {
                try {
                    val id = sheet.getRow(row).getCell(0).numericCellValue.toLong()
                    val category = sheet.getRow(row).getCell(1).stringCellValue
                    val price = sheet.getRow(row).getCell(2).numericCellValue
                    expenses.add(Expense(id, category, price))
                } catch (e: Exception) {
                    Log.e("StorageHelper", "Error reading row $row", e)
                }
            }

            workbook.close()
        }

        return expenses
    }

    fun loadFromXLSInStorageScoped(): List<Expense> {
        val expenses = mutableListOf<Expense>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(MediaStore.Files.FileColumns._ID)
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Files.getContentUri("external"),
                        id.toString()
                    )
                    expenses.addAll(readXlsFromUri(uri))
                }
            }
        } else {
            // For legacy versions, use direct file access
            val downloadsDir = File(context.getExternalFilesDir(null), "Expenses")
            if (downloadsDir.exists()) {
                downloadsDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".xlsx")) {
                        expenses.addAll(readXlsFromUri(Uri.fromFile(file)))
                    }
                }
            }
        }

        return expenses
    }

    // ========== PDF Export ==========
    fun savePdfToUri(expenses: List<Expense>, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Add title
            document.add(Paragraph("Expense Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f))

            // Create table with 3 columns
            val table = Table(3)
            table.setWidth(500f)

            // Add table headers
            table.addHeaderCell("ID")
            table.addHeaderCell("Category")
            table.addHeaderCell("Price")

            // Add data rows
            expenses.forEach { expense ->
                table.addCell(expense.id.toString())
                table.addCell(expense.category)
                table.addCell(expense.price.toString())
            }

            document.add(table)
            document.close()
        }
    }

    fun saveToPDFInStorageScoped(expenses: List<Expense>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfToMediaStore(expenses)
        } else {
            savePdfToLegacyStorage(expenses)
        }
    }

    private fun savePdfToMediaStore(expenses: List<Expense>) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "expenses_${System.currentTimeMillis()}.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Expenses")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )

        uri?.let {
            savePdfToUri(expenses, it)
        }
    }

    private fun savePdfToLegacyStorage(expenses: List<Expense>) {
        val downloadsDir = File(
            context.getExternalFilesDir(null),
            "Expenses"
        ).apply { mkdirs() }

        val file = File(downloadsDir, "expenses_${System.currentTimeMillis()}.pdf")
        savePdfToUri(expenses, Uri.fromFile(file))
    }

    fun readPdfFromUri(uri: Uri): List<Expense> {
        val expenses = mutableListOf<Expense>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = PdfReader(inputStream)
            val pdfDocument = PdfDocument(reader)

            for (pageNum in 1..pdfDocument.numberOfPages) {
                val text = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(pageNum))
                val lines = text.split("\n")

                // Пропускаем заголовки и пустые строки
                val dataLines = lines.dropWhile { it.contains("Expense Report") || it.trim().isEmpty() }

                // Ищем начало таблицы (после заголовков столбцов)
                val tableStartIndex = dataLines.indexOfFirst {
                    it.contains("ID") && it.contains("Category") && it.contains("Price")
                }

                if (tableStartIndex >= 0) {
                    // Берем строки после заголовков таблицы
                    val tableLines = dataLines.drop(tableStartIndex + 1)

                    // Обрабатываем каждую строку таблицы
                    tableLines.forEach { line ->
                        // Разбиваем строку по пробелам (таблица использует выравнивание пробелами)
                        val cells = line.split("\\s+".toRegex()) // Разделяем по пробелам
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        if (cells.size >= 3) {
                            try {
                                val id = cells[0].toLong()
                                val category = cells[1]
                                val price = cells[2].toDouble()
                                expenses.add(Expense(id, category, price))
                            } catch (e: Exception) {
                                Log.e("StorageHelper", "Error parsing table row: $line", e)
                            }
                        }
                    }
                }
            }

            pdfDocument.close()
        }

        return expenses
    }

    fun loadFromPDFInStorageScoped(): List<Expense> {
        val expenses = mutableListOf<Expense>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(MediaStore.Files.FileColumns._ID)
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("application/pdf")

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Files.getContentUri("external"),
                        id.toString()
                    )
                    expenses.addAll(readPdfFromUri(uri))
                }
            }
        } else {
            // For legacy versions, use direct file access
            val downloadsDir = File(context.getExternalFilesDir(null), "Expenses")
            if (downloadsDir.exists()) {
                downloadsDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".pdf")) {
                        expenses.addAll(readPdfFromUri(Uri.fromFile(file)))
                    }
                }
            }
        }

        return expenses
    }
}