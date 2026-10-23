package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.AppScreen

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        AppScreen.DASHBOARD to Icons.Default.Dashboard,
        AppScreen.TRANSACTIONS to Icons.Default.ReceiptLong,
        AppScreen.BUDGET to Icons.Default.BarChart,
        AppScreen.BILLS to Icons.Default.CalendarToday,
        AppScreen.SAVINGS to Icons.Default.Savings
    )

    NavigationBar(
        modifier = modifier.testTag("app_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { (screen, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.titleFr
                    )
                },
                label = { Text(screen.titleFr) },
                modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onAddTransactionClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = currentScreen.titleFr,
                style = MaterialTheme.typography.titleLarge
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            // Quick access to other sections
            IconButton(
                onClick = { onNavigate(AppScreen.DEBTS) },
                modifier = Modifier.testTag("top_action_debts")
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Dettes",
                    tint = if (currentScreen == AppScreen.DEBTS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onNavigate(AppScreen.ADVISOR) },
                modifier = Modifier.testTag("top_action_advisor")
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Conseiller",
                    tint = if (currentScreen == AppScreen.ADVISOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("top_action_more_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Dettes familiales") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    onClick = {
                        menuExpanded = false
                        onNavigate(AppScreen.DEBTS)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rapports & Graphiques") },
                    leadingIcon = { Icon(Icons.Default.BarChart, null) },
                    onClick = {
                        menuExpanded = false
                        onNavigate(AppScreen.REPORTS)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Conseiller intelligent") },
                    leadingIcon = { Icon(Icons.Default.Psychology, null) },
                    onClick = {
                        menuExpanded = false
                        onNavigate(AppScreen.ADVISOR)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Paramètres & Devises") },
                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                    onClick = {
                        menuExpanded = false
                        onNavigate(AppScreen.SETTINGS)
                    }
                )
            }
        },
        modifier = modifier
    )
}
