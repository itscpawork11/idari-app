@file:OptIn(ExperimentalMaterial3Api::class)
package com.example

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.*
import com.example.R
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private var pendingBiometricAction: String? = null

    private val restoreBackupLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.restoreBackup(this, it) }
    }

    override fun attachBaseContext(newBase: Context?) {
        val prefs = newBase?.getSharedPreferences("idari_prefs", Context.MODE_PRIVATE)
        val lang = prefs?.getString("language", "en") ?: "en"
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = Configuration(newBase?.resources?.configuration ?: Configuration())
        config.setLocale(locale)
        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }

    private val viewModel: ExpenseViewModel by viewModels {
        val appObj = application as MyApplication
        ExpenseViewModel.Factory(
            appObj.categoryRepository,
            appObj.transactionRepository,
            appObj.budgetRepository,
            appObj.addTransactionUseCase,
            appObj.getMonthlyExpensesUseCase,
            appObj.getBudgetStatusUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.init(this)

        setContent {
            val categories by viewModel.categories.collectAsState()
            val isUnlocked by viewModel.isUnlocked.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
            val currentLanguage by viewModel.language.collectAsState()

            val currentThemeIndex by viewModel.themeIndex.collectAsState()
            val isRtl = currentLanguage == "ar"

            MyApplicationTheme(
                themeIndex = currentThemeIndex,
                isRtl = isRtl
            ) {
                val layoutDir = if (currentLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    val context = LocalContext.current
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()

                    var currentTab by remember { mutableStateOf(0) }
                    var showQuickAddSheet by remember { mutableStateOf(false) }
                    var showAboutScreen by remember { mutableStateOf(false) }
                    var editingTransaction by remember { mutableStateOf<com.example.domain.model.TransactionWithCategory?>(null) }

                    LaunchedEffect(isBiometricEnabled) {
                        if (isBiometricEnabled) {
                            viewModel.setLockState(true)
                            showBiometricPrompt()
                        } else {
                            viewModel.setLockState(false)
                        }
                    }

                    LaunchedEffect(Unit) {
                        viewModel.eventFlow.collectLatest { event ->
                            when (event) {
                                is ExpenseViewModel.ExpenseEvent.TransactionAddedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_transaction_saved), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.TransactionUpdatedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_transaction_updated), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.TransactionDeletedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_transaction_deleted), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.BudgetCreatedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_budget_created), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.BudgetDeletedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_budget_deleted), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.CategoryCreatedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_category_created), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.DataClearedSuccessfully -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_data_cleared), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.BudgetExceededWarning -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_budget_exceeded, event.categoryName), duration = SnackbarDuration.Long) }
                                }
                                is ExpenseViewModel.ExpenseEvent.BackupSuccess -> {
                                    try {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", event.file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_chooser_title)))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.export_share_failed, e.localizedMessage), Toast.LENGTH_SHORT).show()
                                    }
                                }
                                is ExpenseViewModel.ExpenseEvent.RestoreSuccess -> {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_restore_success), duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.ExportSuccess -> {
                                    try {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", event.file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_chooser_title)))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.export_share_failed, e.localizedMessage), Toast.LENGTH_SHORT).show()
                                    }
                                }
                                is ExpenseViewModel.ExpenseEvent.Error -> {
                                    val msg = if (event.formatArg != null) {
                                        context.getString(event.messageResId, event.formatArg)
                                    } else {
                                        context.getString(event.messageResId)
                                    }
                                    scope.launch { snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short) }
                                }
                                is ExpenseViewModel.ExpenseEvent.RequestBiometricForExport -> {
                                    this@MainActivity.pendingBiometricAction = "export"
                                    showBiometricPrompt()
                                }
                                is ExpenseViewModel.ExpenseEvent.RequestBiometricForClear -> {
                                    this@MainActivity.pendingBiometricAction = "clear"
                                    showBiometricPrompt()
                                }
                                else -> {}
                            }
                        }
                    }

                    if (showAboutScreen) {
                        AboutScreen(onBackClick = { showAboutScreen = false })
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = isUnlocked,
                                    enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)),
                                    exit = slideOutVertically(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(300))
                                ) {
                                    AppBottomNavigation(
                                        currentTab = currentTab,
                                        onTabSelected = { currentTab = it },
                                        onFabClicked = { showQuickAddSheet = true }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                AnimatedContent(
                                    targetState = isUnlocked,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                                    },
                                    label = "lockContent"
                                ) { unlocked ->
                                    if (!unlocked) {
                                        BiometricLockedOverlay(onAuthenticateClick = { showBiometricPrompt() })
                                    } else {
                                        AnimatedContent(
                                            targetState = currentTab,
                                            transitionSpec = {
                                                val direction = if (targetState > initialState) 1 else -1
                                                slideInHorizontally(animationSpec = tween(350)) { fullWidth -> fullWidth * direction } + fadeIn(animationSpec = tween(350)) togetherWith
                                                slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth * direction } + fadeOut(animationSpec = tween(250))
                                            },
                                            label = "tabContent"
                                        ) { tab ->
                                            when (tab) {
                                                 0 -> DashboardScreen(viewModel = viewModel, onNavigateToQuickAdd = { showQuickAddSheet = true }, onEditTransaction = { t -> editingTransaction = t }, modifier = Modifier.padding(innerPadding))
                                                 1 -> ReportsScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                                                 2 -> BudgetsScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                                                 3 -> SettingsScreen(
                                                     viewModel = viewModel,
                                                     onBackClick = { currentTab = 0 },
                                                     onRestart = { recreate() },
                                                     onNavigateToAbout = { showAboutScreen = true },
                                                     onRestoreBackup = { restoreBackupLauncher.launch(arrayOf("application/json")) },
                                                     onExportBackup = {
                                                         viewModel.exportBackup(this@MainActivity)
                                                     },
                                                     modifier = Modifier.padding(innerPadding)
                                                 )
                                            }
                                        }
                                    }
                                }

                                if (showQuickAddSheet) {
                                    ModalBottomSheet(
                                        onDismissRequest = { showQuickAddSheet = false },
                                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                                    ) {
                                        QuickAddBottomSheet(
                                            categories = categories,
                                            currentLanguage = currentLanguage,
                                            onAddTransaction = { amount, type, catId, note ->
                                                viewModel.addTransaction(amount, type, catId, note)
                                            },
                                            onAddCategory = { name, iconRes, colorHex ->
                                                viewModel.addCategory(name, iconRes, colorHex)
                                            },
                                            onDismiss = { showQuickAddSheet = false }
                                        )
                                    }
                                }

                                editingTransaction?.let { transaction ->
                                    ModalBottomSheet(
                                        onDismissRequest = { editingTransaction = null },
                                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                                    ) {
                                        QuickAddBottomSheet(
                                            categories = categories,
                                            currentLanguage = currentLanguage,
                                            editTransaction = transaction,
                                            onUpdateTransaction = { id, amount, type, catId, timestamp, note ->
                                                viewModel.updateTransaction(id, amount, type, catId, timestamp, note)
                                                editingTransaction = null
                                            },
                                            onDeleteTransaction = { id ->
                                                viewModel.deleteTransaction(id)
                                                editingTransaction = null
                                            },
                                            onAddCategory = { name, iconRes, colorHex ->
                                                viewModel.addCategory(name, iconRes, colorHex)
                                            },
                                            onDismiss = { editingTransaction = null }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    runOnUiThread {
                        val action = this@MainActivity.pendingBiometricAction
                        this@MainActivity.pendingBiometricAction = null
                        when (action) {
                            "export" -> viewModel.exportToCsv(this@MainActivity)
                            "clear" -> viewModel.clearAllData()
                            else -> viewModel.setLockState(false)
                        }
                    }
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED) {
                        showBiometricPrompt()
                    }
                }
            }
        )
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_cancel))
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun AppBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onFabClicked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(title = stringResource(R.string.nav_home), filledIcon = Icons.Default.Home, outlinedIcon = Icons.Outlined.Home, selected = currentTab == 0, onClick = { onTabSelected(0) })
            BottomNavItem(title = stringResource(R.string.nav_reports), filledIcon = Icons.Default.PieChart, outlinedIcon = Icons.Outlined.PieChart, selected = currentTab == 1, onClick = { onTabSelected(1) })

            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress); onFabClicked()
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.nav_quick_add), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }

            BottomNavItem(title = stringResource(R.string.nav_budgets), filledIcon = Icons.Default.AccountBalanceWallet, outlinedIcon = Icons.Outlined.AccountBalanceWallet, selected = currentTab == 2, onClick = { onTabSelected(2) })
            BottomNavItem(title = stringResource(R.string.nav_settings), filledIcon = Icons.Default.Settings, outlinedIcon = Icons.Outlined.Settings, selected = currentTab == 3, onClick = { onTabSelected(3) })
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    title: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = if (selected) filledIcon else outlinedIcon
    val tintColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Column(
        modifier = Modifier.weight(1f).fillMaxHeight().clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tintColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = tintColor, fontSize = 11.sp)
    }
}

@Composable
fun BiometricLockedOverlay(onAuthenticateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp).clickable { onAuthenticateClick() }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.lock_app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.lock_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAuthenticateClick, shape = RoundedCornerShape(24.dp)) {
            Text(stringResource(R.string.biometric_tap_auth), fontWeight = FontWeight.Bold)
        }
    }
}
