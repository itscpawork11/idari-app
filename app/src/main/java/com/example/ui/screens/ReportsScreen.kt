package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.LocaleManager
import com.example.domain.model.TransactionType
import com.example.R
import com.example.ui.components.getCategoryIcon
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()

    var selectedRangeIndex by remember { mutableStateOf(1) }
    val ranges = listOf(stringResource(R.string.reports_weekly), stringResource(R.string.reports_monthly), stringResource(R.string.reports_all))

    val filteredTransactions = remember(transactions, selectedRangeIndex) {
        val now = System.currentTimeMillis()
        when (selectedRangeIndex) {
            0 -> transactions.filter { it.timestamp >= now - 7 * 24 * 60 * 60 * 1000L }
            1 -> transactions.filter { it.timestamp >= now - 30 * 24 * 60 * 60 * 1000L }
            else -> transactions
        }
    }

    val categoryExpenses = remember(filteredTransactions, currentLanguage) {
        val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        val grouped = expenses.groupBy { it.categoryId }
        grouped.map { (catId, list) ->
            val sum = list.sumOf { it.amount }
            val first = list.first()
            val localizedName = LocaleManager.getDefaultCategoryName(first.categoryIcon, currentLanguage)
            CategoryExpense(catId, localizedName, first.categoryIcon, first.categoryColorHex, sum, if (total > 0) (sum / total).toFloat() else 0f)
        }.sortedByDescending { it.totalAmount }
    }

    val totalExpensesSum = remember(categoryExpenses) { categoryExpenses.sumOf { it.totalAmount } }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("reports_screen").padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(stringResource(R.string.reports_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(stringResource(R.string.reports_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ranges.forEachIndexed { index, title ->
                    val selected = selectedRangeIndex == index
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedRangeIndex = index }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (totalExpensesSum == 0.0) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.reports_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                    }
                }
            }
        } else {
            item {
                AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.reports_category_spending), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                                DonutChart(categoryExpenses = categoryExpenses, modifier = Modifier.fillMaxSize())
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.reports_total), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("%.0f".format(Locale.US, totalExpensesSum), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(currency, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.reports_category_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            items(categoryExpenses, key = { it.categoryId }) { catExpense ->
                AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically(animationSpec = tween(300))) {
                    CategoryBreakdownItem(categoryExpense = catExpense, currency = currency)
                }
            }
        }
    }
}

data class CategoryExpense(
    val categoryId: Int,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val totalAmount: Double,
    val percentage: Float
)

@Composable
fun DonutChart(categoryExpenses: List<CategoryExpense>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 18.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeftOffset = Offset(x = (size.width - diameter) / 2f, y = (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        categoryExpenses.forEach { cat ->
            val sweepAngle = cat.percentage * 360f
            val color = try { Color(android.graphics.Color.parseColor(cat.categoryColorHex)) } catch (e: Exception) { Color.Gray }
            drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, topLeft = topLeftOffset, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryBreakdownItem(categoryExpense: CategoryExpense, currency: String) {
    val categoryColor = remember(categoryExpense.categoryColorHex) {
        try { Color(android.graphics.Color.parseColor(categoryExpense.categoryColorHex)) } catch (e: Exception) { Color(0xFF95A5A6) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(getCategoryIcon(categoryExpense.categoryIcon), contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(categoryExpense.categoryName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("%.1f %s".format(Locale.US, categoryExpense.totalAmount, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("%.0f%%".format(Locale.US, categoryExpense.percentage * 100), style = MaterialTheme.typography.labelSmall, color = categoryColor, fontWeight = FontWeight.Bold)
                }
            }
            val animProgress by animateFloatAsState(targetValue = categoryExpense.percentage, animationSpec = tween(600))
            LinearProgressIndicator(
                progress = animProgress,
                color = categoryColor,
                trackColor = categoryColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )
        }
    }
}
