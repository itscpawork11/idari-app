package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.LocaleManager
import com.example.domain.model.Category
import com.example.domain.model.TransactionType
import com.example.domain.model.TransactionWithCategory
import com.example.ui.components.getCategoryIcon

private val iconOptions = listOf(
    "income" to "TrendingUp",
    "food" to "Restaurant",
    "transport" to "Car",
    "home" to "Home",
    "shopping" to "Cart",
    "education" to "School",
    "entertainment" to "Celebration",
    "other" to "Category"
)

private val colorOptions = listOf(
    "#E74C3C", "#E67E22", "#F1C40F", "#2ECC71",
    "#1ABC9C", "#3498DB", "#9B59B6", "#607D8B",
    "#795548", "#FF5722", "#00BCD4", "#8BC34A"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    categories: List<Category>,
    currentLanguage: String = "en",
    onAddTransaction: (Double, TransactionType, Int, String?) -> Unit = { _, _, _, _ -> },
    onUpdateTransaction: (Long, Double, TransactionType, Int, Long, String?) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteTransaction: (Long) -> Unit = {},
    onAddCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit,
    editTransaction: TransactionWithCategory? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val isEditing = editTransaction != null

    var selectedType by remember { mutableStateOf(editTransaction?.type ?: TransactionType.EXPENSE) }
    var amountString by remember {
        mutableStateOf(
            if (editTransaction != null) {
                val amt = editTransaction.amount
                if (amt == amt.toLong().toDouble()) amt.toLong().toString() else amt.toString()
            } else ""
        )
    }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var noteText by remember { mutableStateOf(editTransaction?.note ?: "") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(categories, editTransaction) {
        if (categories.isNotEmpty()) {
            selectedCategory = if (editTransaction != null) {
                categories.find { it.id == editTransaction.categoryId } ?: categories.first()
            } else {
                categories.first()
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_transaction_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_transaction_body)) },
            confirmButton = {
                TextButton(onClick = {
                    editTransaction?.let { onDeleteTransaction(it.id) }
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_add_sheet")
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        )

        Text(
            text = if (isEditing) stringResource(R.string.quick_add_edit_title) else stringResource(R.string.quick_add_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedType == TransactionType.EXPENSE) Color(0xFFE74C3C)
                        else Color.Transparent
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedType = TransactionType.EXPENSE
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.quick_add_expense),
                    color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedType == TransactionType.INCOME) Color(0xFF2ECC71)
                        else Color.Transparent
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedType = TransactionType.INCOME
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.quick_add_income),
                    color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val amountToDisplay = if (amountString.isEmpty()) "0.0" else amountString
            Text(
                text = amountToDisplay,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (selectedType == TransactionType.INCOME) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.quick_add_select_category),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                IconButton(
                    onClick = { showCreateCategoryDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_category),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = selectedCategory?.id == category.id
                    val catColor = remember(category.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            Color(0xFF95A5A6)
                        }
                    }

                    val displayName = remember(category.iconRes, currentLanguage) {
                        val localized = LocaleManager.getDefaultCategoryName(category.iconRes, currentLanguage)
                        if (localized == category.iconRes) category.name else localized
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCategory = category
                        },
                        label = {
                            Text(text = displayName, fontWeight = FontWeight.Bold)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(category.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text(stringResource(R.string.quick_add_note_hint)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        NumericKeyboard(
            onKeyPress = { key ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                when (key) {
                    KeyboardKey.Backspace -> {
                        if (amountString.isNotEmpty()) {
                            amountString = amountString.dropLast(1)
                        }
                    }
                    KeyboardKey.Dot -> {
                        if (!amountString.contains(".")) {
                            amountString = if (amountString.isEmpty()) "0." else "$amountString."
                        }
                    }
                    else -> {
                        if (amountString.length < 9) {
                            amountString += key.value
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.quick_add_delete),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    val amountVal = amountString.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0.0 && selectedCategory != null) {
                        if (isEditing && editTransaction != null) {
                            onUpdateTransaction(
                                editTransaction.id,
                                amountVal,
                                selectedType,
                                selectedCategory!!.id,
                                editTransaction.timestamp,
                                noteText.takeIf { it.isNotBlank() }
                            )
                        } else {
                            onAddTransaction(
                                amountVal,
                                selectedType,
                                selectedCategory!!.id,
                                noteText.takeIf { it.isNotBlank() }
                            )
                        }
                        onDismiss()
                    }
                },
                enabled = (amountString.toDoubleOrNull() ?: 0.0) > 0.0 && selectedCategory != null,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) stringResource(R.string.quick_add_update) else stringResource(R.string.quick_add_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { name, iconRes, colorHex ->
                onAddCategory(name, iconRes, colorHex)
                showCreateCategoryDialog = false
            }
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("other") }
    var selectedColor by remember { mutableStateOf("#607D8B") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.create_category),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = stringResource(R.string.category_icon),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { (key, _) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(key),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.category_color),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions) { hex ->
                        val isSelected = selectedColor == hex
                        val color = remember(hex) {
                            try { Color(android.graphics.Color.parseColor(hex)) }
                            catch (e: Exception) { Color.Gray }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, selectedIcon, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

enum class KeyboardKey(val value: String) {
    Num1("1"), Num2("2"), Num3("3"),
    Num4("4"), Num5("5"), Num6("6"),
    Num7("7"), Num8("8"), Num9("9"),
    Num0("0"), Dot("."), Backspace("\u232B")
}

@Composable
fun NumericKeyboard(
    onKeyPress: (KeyboardKey) -> Unit
) {
    val keys = listOf(
        listOf(KeyboardKey.Num7, KeyboardKey.Num8, KeyboardKey.Num9),
        listOf(KeyboardKey.Num4, KeyboardKey.Num5, KeyboardKey.Num6),
        listOf(KeyboardKey.Num1, KeyboardKey.Num2, KeyboardKey.Num3),
        listOf(KeyboardKey.Dot, KeyboardKey.Num0, KeyboardKey.Backspace)
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { onKeyPress(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == KeyboardKey.Backspace) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = stringResource(R.string.quick_add_numpad_backspace),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = key.value,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
