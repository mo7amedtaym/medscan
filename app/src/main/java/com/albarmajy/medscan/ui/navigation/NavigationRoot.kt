package com.albarmajy.medscan.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack

import androidx.navigation3.ui.NavDisplay
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.albarmajy.medscan.ui.screens.CalendarScreen
import com.albarmajy.medscan.ui.screens.CameraScannerScreen
import com.albarmajy.medscan.ui.screens.DashboardScreen
import com.albarmajy.medscan.ui.screens.MedicationPlanScreen
import com.albarmajy.medscan.ui.screens.MedicinesScreen
import com.albarmajy.medscan.ui.screens.SettingsScreen
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import org.koin.androidx.compose.koinViewModel


//@RequiresApi(Build.VERSION_CODES.S)
//@Composable
//fun NavigationRoot(modifier: Modifier = Modifier, viewModel: DashboardViewModel = koinViewModel()) {
//    val backStack = rememberNavBackStack(Routes.Dashboard)
//
//    NavDisplay(
//        backStack = backStack,
//        entryProvider = { key->
//            when(key){
//                is Routes.Dashboard -> {
//                    NavEntry(key) {
//                        DashboardScreen(
//                            onScanClick = { backStack.add(Routes.Scanner) }
//                        )
//                    }
//                }
//                is Routes.Scanner -> {
//                    NavEntry(key) {
//                        CameraScannerScreen(
//                            onTextScanned = { result ->
//                                viewModel.onTextScanned(result) { matchedMedicine ->
//                                    if (backStack.isNotEmpty()) {
//                                        backStack.removeAt(backStack.size - 1)
//                                    }
//                                    backStack.add(Routes.MedicationPlan(matchedMedicine.id.toLong()))
//
//                                }
//                            }
//                        )
//                    }
//                }
//                is Routes.Calendar -> {
//                    NavEntry(key) {
//                        CalendarScreen()
//                    }
//                }
//                is Routes.Medicines -> {
//                    NavEntry(key) {
//                        MedicinesScreen()
//                    }
//                }
//                is Routes.Settings -> {
//                    NavEntry(key) {
//                        SettingsScreen()
//                    }
//                }
//                is Routes.MedicationPlan -> {
//                    NavEntry(key) {
//                        MedicationPlanScreen(key.medId, onBack = { backStack.removeAt(backStack.size - 1)}){
//                            backStack.add(Routes.Dashboard)
//                        }
//                    }
//                }
//                else -> error("Unknown route")
//            }
//        },
//        modifier = modifier,
//    )
//
//}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun NavigationRoot(viewModel: DashboardViewModel = koinViewModel()) {
    val backStack = rememberNavBackStack(Routes.Dashboard)
    val currentRoute = backStack.lastOrNull()
    val mainRoutes = listOf(Routes.Dashboard, Routes.Calendar, Routes.Medicines, Routes.Settings)
    val shouldShowBottomBar = currentRoute in mainRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute!!,
                    onNavigate = { targetRoute ->
                        backStack.clear()
                        backStack.add(targetRoute)
                    }
                )
            }
        },
        floatingActionButton = {
            if (shouldShowBottomBar) {
                FloatingActionButton(
                    onClick = { backStack.add(Routes.Scanner) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .offset(y = 85.dp)
                        .size(64.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Scan")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            entryProvider = { key ->
                when (key) {
                    is Routes.Dashboard -> NavEntry(key) { DashboardScreen() }
                    is Routes.Calendar -> NavEntry(key) { CalendarScreen() }
                    is Routes.Medicines -> NavEntry(key) { MedicinesScreen() }
                    is Routes.Settings -> NavEntry(key) { SettingsScreen() }

                    is Routes.Scanner -> NavEntry(key) {
                        CameraScannerScreen(onTextScanned = { result ->
                            viewModel.onTextScanned(result) { matchedMedicine ->
                                viewModel.saveMedication(matchedMedicine)
                                backStack.removeAt(backStack.size - 1)
                                backStack.add(Routes.MedicationPlan(matchedMedicine.id.toLong()))
                            }
                        })
                    }

                    is Routes.MedicationPlan -> NavEntry(key) {
                        MedicationPlanScreen(
                            medId = key.medId,
                            onBack = { backStack.removeAt(backStack.lastIndex) },
                            onConfirm = { it, name ->
                                viewModel.saveMedicationPlan(it, name)
                                backStack.clear()
                                backStack.add(Routes.Dashboard)
                            }
                        )
                    }
                    else -> error("unknown NavKey: $key")
                }
            },
            modifier = Modifier.padding(innerPadding).background(BackgroundLight) // هام جداً لكي لا تظهر الشاشات خلف الـ Nav Bar
        )
    }
}

@Composable
fun BottomNavigationBar(currentRoute: NavKey, onNavigate: (NavKey) -> Unit) {
    NavigationBar(containerColor = Color.White, contentColor = PrimaryBlue) {
        NavigationBarItem(
            selected = currentRoute is Routes.Dashboard,
            onClick = { onNavigate(Routes.Dashboard) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute is Routes.Calendar,
            onClick = { onNavigate(Routes.Calendar) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            label = { Text("Calendar") }
        )

        Spacer(Modifier.weight(1f))

        NavigationBarItem(
            selected = currentRoute is Routes.Medicines,
            onClick = { onNavigate(Routes.Medicines) },
            icon = { Icon(Icons.Default.Medication, contentDescription = null) },
            label = { Text("Meds") }
        )
        NavigationBarItem(
            selected = currentRoute is Routes.Settings,
            onClick = { onNavigate(Routes.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") }
        )
    }
}