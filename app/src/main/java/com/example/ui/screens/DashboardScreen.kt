package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocaleManager
import com.example.domain.model.TransactionType
import com.example.domain.model.TransactionWithCategory
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.GlassGradientCard
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToQuickAdd: () -> Unit,
    onEditTransaction: (TransactionWithCategory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()
    val isTransactionsVisible by viewModel.isTransactionsVisible.collectAsState()

    val totalIncome = remember(transactions) { transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } }
    val totalExpense = remember(transactions) { transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } }
    val balance = totalIncome - totalExpense

    val categorySpending = remember(transactions) {
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val total = expenseTransactions.sumOf { it.amount }
        val spending = mutableListOf<ExpenseViewModel.CategorySpending>()
        val categories = viewModel.categories.value
        for (cat in categories) {
            val catTotal = expenseTransactions.filter { it.categoryId == cat.id }.sumOf { it.amount }
            if (catTotal > 0) {
                val percentage = if (total > 0) (catTotal / total * 100).toFloat() else 0f
                val localizedName = LocaleManager.getDefaultCategoryName(cat.iconRes, currentLanguage)
                spending.add(ExpenseViewModel.CategorySpending(localizedName, catTotal, cat.colorHex, percentage))
            }
        }
        if (spending.sumOf { it.amount } < total) {
            val otherAmount = total - spending.sumOf { it.amount }
            if (otherAmount > 0) {
                val otherName = LocaleManager.getDefaultCategoryName("other", currentLanguage)
                spending.add(ExpenseViewModel.CategorySpending(otherName, otherAmount, "#9E9E9E", (otherAmount / total * 100).toFloat()))
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.dialog_delete_transaction_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_transaction_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteTransaction(it) }
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedGeometricBackground()

        LazyColumn(
            modifier = modifier.fillMaxSize().testTag("dashboard_screen").padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_welcome),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.dashboard_idari),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        item {
            DonutBalanceCard(
                balance = balance,
                income = totalIncome,
                expense = totalExpense,
                currency = currency,
                categorySpending = categorySpending,
                isVisible = isBalanceVisible,
                onToggleVisibility = { viewModel.toggleBalanceVisibility() }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(300)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dashboard_weekly_overview), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(stringResource(R.string.dashboard_last_7_days), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    val weekData = remember(transactions) { calculateLast7DaysSpending(transactions) }

                    AnimatedVisibility(visible = weekData.all { it.second == 0.0 }, enter = fadeIn(), exit = fadeOut()) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.dashboard_no_expenses_week), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    }

                    AnimatedVisibility(visible = weekData.any { it.second > 0.0 }, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        SpendingBarChart(spendingData = weekData, currency = currency, modifier = Modifier.fillMaxWidth().height(120.dp))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.dashboard_recent_transactions), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = { viewModel.toggleTransactionsVisibility() }) {
                    Icon(
                        imageVector = if (isTransactionsVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        val recentList = transactions.take(20)
        if (recentList.isEmpty()) {
            item {
                AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.dashboard_empty_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        } else {
            items(recentList, key = { it.id }) { item ->
                TransactionListItem(
                    transaction = item,
                    currency = currency,
                    currentLanguage = currentLanguage,
                    isVisible = isTransactionsVisible,
                    onEdit = { onEditTransaction(item) },
                    onDelete = { showDeleteDialog = item.id }
                )
            }
        }
    }

    }
}

@Composable
fun DonutBalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    currency: String,
    categorySpending: List<ExpenseViewModel.CategorySpending>,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {}
) {
    val totalExpense = categorySpending.sumOf { it.amount }

    GlassGradientCard(
        gradientColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(blue = MaterialTheme.colorScheme.primary.blue * 0.85f, alpha = 0.95f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.dashboard_total_balance),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isVisible) "%.2f %s".format(Locale.US, balance, currency) else "•••••",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (totalExpense > 0 && categorySpending.isNotEmpty()) {
                    DonutChart(
                        data = categorySpending,
                        size = 100.dp,
                        strokeWidth = 20.dp,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.dashboard_income), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        Text(
                            text = if (isVisible) "%.1f %s".format(Locale.US, income, currency) else "•••••",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.dashboard_expenses), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        Text(
                            text = if (isVisible) "%.1f %s".format(Locale.US, expense, currency) else "•••••",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                }
            }

            if (totalExpense > 0 && categorySpending.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_spending_breakdown),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SpendingLegend(data = categorySpending, currency = currency)
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<ExpenseViewModel.CategorySpending>,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val totalPercent = data.sumOf { it.percentage.toDouble() }
    val sweepAngles = remember(data) {
        data.map { (it.percentage.toDouble() / totalPercent * 360.0).toFloat() }
    }

    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Butt)
        val pxSize = size.toPx()
        val arcDiameter = pxSize - strokeWidth.toPx()
        val arcTopLeft = Offset(
            (pxSize - arcDiameter) / 2f,
            (pxSize - arcDiameter) / 2f
        )
        val arcSize = Size(arcDiameter, arcDiameter)

        var startAngle = -90f
        sweepAngles.forEachIndexed { index, sweep ->
            val color = try {
                Color(android.graphics.Color.parseColor(data[index].colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = stroke
            )
            startAngle += sweep
        }
    }
}

@Composable
fun SpendingLegend(
    data: List<ExpenseViewModel.CategorySpending>,
    currency: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        data.take(5).forEach { item ->
            val color = try {
                Color(android.graphics.Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "%.1f%% · %.1f %s".format(Locale.US, item.percentage, item.amount, currency),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionWithCategory,
    currency: String,
    currentLanguage: String = "en",
    isVisible: Boolean = true,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val categoryColor = remember(transaction.categoryColorHex) {
        try { Color(android.graphics.Color.parseColor(transaction.categoryColorHex)) } catch (e: Exception) { Color(0xFF95A5A6) }
    }
    val dateStr = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        sdf.format(Date(transaction.timestamp))
    }

    val localizedCatName = remember(transaction.categoryIcon, currentLanguage) {
        LocaleManager.getDefaultCategoryName(transaction.categoryIcon, currentLanguage)
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = getCategoryIcon(transaction.categoryIcon), contentDescription = localizedCatName, tint = categoryColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(localizedCatName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        if (!transaction.note.isNullOrBlank()) {
                            Text("\u2022  ${transaction.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val isIncome = transaction.type == TransactionType.INCOME
                Text(
                    text = if (isVisible) "%s%.1f %s".format(Locale.US, if (isIncome) "+" else "-", transaction.amount, currency) else "•••••",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.dashboard_edit), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.dashboard_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SpendingBarChart(spendingData: List<Pair<String, Double>>, currency: String, modifier: Modifier = Modifier) {
    val maxAmount = remember(spendingData) { spendingData.maxOfOrNull { it.second }?.coerceAtLeast(10.0) ?: 10.0 }
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        spendingData.forEachIndexed { idx, pair ->
            val fraction = (pair.second / maxAmount).toFloat()
            val animFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(500, delayMillis = idx * 80))

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier.weight(1f).width(18.dp).clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(animFraction).clip(RoundedCornerShape(9.dp)).background(primary)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(pair.first, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

private fun calculateLast7DaysSpending(transactions: List<TransactionWithCategory>): List<Pair<String, Double>> {
    val list = mutableListOf<Pair<String, Double>>()
    val calendar = Calendar.getInstance()
    val daysSdf = SimpleDateFormat("EEE", Locale.US)
    val datesStr = mutableListOf<String>()
    val timestamps = mutableListOf<Long>()

    for (i in 6 downTo 0) {
        val testCal = Calendar.getInstance()
        testCal.add(Calendar.DAY_OF_YEAR, -i)
        datesStr.add(daysSdf.format(testCal.time))
        testCal.set(Calendar.HOUR_OF_DAY, 0); testCal.set(Calendar.MINUTE, 0); testCal.set(Calendar.SECOND, 0); testCal.set(Calendar.MILLISECOND, 0)
        timestamps.add(testCal.timeInMillis)
    }

    for (i in 0 until 7) {
        val startOfToday = timestamps[i]
        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L - 1
        val todayExpenses = transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp in startOfToday..endOfToday }.sumOf { it.amount }
        list.add(Pair(datesStr[i], todayExpenses))
    }
    return list
}

@Composable
fun AnimatedGeometricBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "geo")
    val shapeCount = 6
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val shapeData = remember {
        List(shapeCount) { i ->
            GeometricShape(
                centerX = Random.nextFloat(),
                centerY = Random.nextFloat(),
                size = Random.nextFloat() * 60f + 20f,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                rotation = Random.nextFloat() * 360f,
                alpha = Random.nextFloat() * 0.08f + 0.03f,
                type = i % 3,
                colorSeed = i
            )
        }
    }

    val animProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        shapeData.forEach { shape ->
            val phase = animProgress.value * shape.speed
            val x = ((shape.centerX + phase * 0.3f) % 1.2f - 0.1f) * w
            val y = ((shape.centerY + phase * 0.2f + sin(phase * 2f) * 0.05f) % 1.2f - 0.1f) * h
            val rot = shape.rotation + phase * 30f
            val s = shape.size * density

            val color = when (shape.colorSeed % 3) {
                0 -> primary.copy(alpha = shape.alpha)
                1 -> secondary.copy(alpha = shape.alpha)
                else -> tertiary.copy(alpha = shape.alpha)
            }

            translate(x, y) {
                rotate(rot, Offset(s / 2f, s / 2f)) {
                    when (shape.type) {
                        0 -> {
                            drawCircle(color = color, radius = s / 2f)
                        }
                        1 -> {
                            val path = Path().apply {
                                moveTo(s / 2f, 0f)
                                lineTo(s, s * 0.67f)
                                lineTo(s * 0.67f, s)
                                lineTo(s * 0.33f, s)
                                lineTo(0f, s * 0.67f)
                                close()
                            }
                            drawPath(path, color = color)
                        }
                        else -> {
                            val path = Path().apply {
                                val cx = s / 2f
                                val cy = s / 2f
                                val r = s / 2f
                                for (i in 0 until 6) {
                                    val angle = Math.toRadians((i * 60f).toDouble())
                                    val px = cx + r * cos(angle).toFloat()
                                    val py = cy + r * sin(angle).toFloat()
                                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                                }
                                close()
                            }
                            drawPath(path, color = color)
                        }
                    }
                }
            }
        }
    }
}

private data class GeometricShape(
    val centerX: Float,
    val centerY: Float,
    val size: Float,
    val speed: Float,
    val rotation: Float,
    val alpha: Float,
    val type: Int,
    val colorSeed: Int
)
