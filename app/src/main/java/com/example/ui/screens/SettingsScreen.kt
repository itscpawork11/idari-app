package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onBackClick: () -> Unit = {},
    onRestart: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency by viewModel.currency.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()
    val currentThemeIndex by viewModel.themeIndex.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val langLabel = if (currentLanguage == "en") stringResource(R.string.settings_english) else stringResource(R.string.settings_arabic)
    val themeNames = listOf(stringResource(R.string.settings_theme_ocean), stringResource(R.string.settings_theme_emerald), stringResource(R.string.settings_theme_royal))
    val themeLabel = themeNames.getOrElse(currentThemeIndex) { stringResource(R.string.settings_theme_ocean) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Payments,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.settings_currency),
                        subtitle = stringResource(R.string.settings_currency_subtitle, currency),
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_subtitle, langLabel),
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.settings_theme),
                        subtitle = stringResource(R.string.settings_theme_subtitle, themeLabel),
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRowItem(
                        icon = Icons.Default.Fingerprint,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = stringResource(R.string.settings_biometric),
                        hint = stringResource(R.string.settings_biometric_hint),
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric() }
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.settings_data_management),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Share,
                        iconTint = Color(0xFF2ECC71),
                        title = stringResource(R.string.settings_export),
                        subtitle = stringResource(R.string.settings_export_subtitle),
                        onClick = {
                            if (isBiometricEnabled) {
                                scope.launch { viewModel.requestBiometricForExport() }
                            } else {
                                viewModel.exportToCsv(context)
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.DeleteSweep,
                        iconTint = Color(0xFFE53935),
                        title = stringResource(R.string.settings_clear),
                        subtitle = stringResource(R.string.settings_clear_subtitle),
                        onClick = { showClearDataDialog = true }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.settings_about_subtitle),
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.dialog_choose_currency), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(stringResource(R.string.currency_egp), stringResource(R.string.currency_usd)).forEach { option ->
                        val key = if (option.contains("USD")) "USD" else "ج.م"
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                                viewModel.setCurrency(key)
                                showCurrencyDialog = false
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currency == key, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(option, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.dialog_choose_theme), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                                viewModel.setTheme(index)
                                showThemeDialog = false
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentThemeIndex == index, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.dialog_app_language), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(stringResource(R.string.settings_english), stringResource(R.string.settings_arabic)).forEach { option ->
                        val langKey = if (option == "English" || option == "en") "en" else "ar"
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                                viewModel.setLanguage(langKey)
                                showLanguageDialog = false
                                onRestart()
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentLanguage == langKey, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(option, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(stringResource(R.string.dialog_clear_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text(stringResource(R.string.dialog_clear_body)) },
            confirmButton = {
                Button(
                    onClick = { showClearDataDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    if (isBiometricEnabled) {
                        scope.launch { viewModel.requestBiometricForClear() }
                    } else {
                        viewModel.clearAllData()
                    }
                }) { Text(stringResource(R.string.dialog_delete_everything), color = MaterialTheme.colorScheme.error) }
            }
        )
    }


}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
    }
}

@Composable
fun SettingsToggleRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    hint: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (hint != null) {
                    Text(text = hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
