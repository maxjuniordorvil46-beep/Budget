package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.BudgetRepository
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.budget.BudgetScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.debts.DebtsScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.savings.SavingsScreen
import com.example.ui.screens.settings.AdvisorScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.BudgetViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BudgetViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = BudgetRepository(database)
        BudgetViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: BudgetViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    var showAddTxDialog by remember { mutableStateOf(false) }

    // Android back button handler: if on subscreen, navigate back to Dashboard
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                onAddTransactionClicked = { showAddTxDialog = true }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onAddTransactionClicked = { showAddTxDialog = true },
                modifier = screenModifier
            )
            AppScreen.TRANSACTIONS -> TransactionsScreen(
                viewModel = viewModel,
                onAddTransactionClicked = { showAddTxDialog = true },
                modifier = screenModifier
            )
            AppScreen.BUDGET -> BudgetScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.BILLS -> BillsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.SAVINGS -> SavingsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.DEBTS -> DebtsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.REPORTS -> ReportsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.ADVISOR -> AdvisorScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
            AppScreen.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                modifier = screenModifier
            )
        }
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            categories = categories,
            accounts = accounts,
            members = members,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { tx ->
                viewModel.addTransaction(tx)
                showAddTxDialog = false
            }
        )
    }
}
