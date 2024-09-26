
package com.example.android.wearable.datalayer

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip

@Composable
fun WearApp(mainViewModel: MainViewModel, onToggleTask: (Int) -> Unit) {
    AppScaffold {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    onShowNodesList = { navController.navigate("nodeslist") },
                    onToggleTask = onToggleTask,
                    mainViewModel = mainViewModel
                )
            }
            composable("nodeslist") {
                ConnectedNodesScreen()
            }

        }
    }
}

@Composable
fun MainScreen(
    onShowNodesList: () -> Unit,
    onToggleTask: (Int) -> Unit,
    mainViewModel: MainViewModel
) {
    MainScreen(
        mainViewModel.tasks,
        mainViewModel.checkedTasks,
        onShowNodesList,
        onToggleTask,
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainScreen(
    tasks: List<String>,
    checkedTasks: List<Int>,
    onShowNodesList: () -> Unit,
    onToggleTask: (Int) -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Chip,
            last = ItemType.Text
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
        ) {
            item {
                Chip(
                    label = stringResource(id = R.string.query_other_devices),
                    onClick = onShowNodesList
                )
            }


            if (tasks.isEmpty()) {
                item {
                    Text(
                        stringResource(id = R.string.waiting),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(tasks.size) { index ->
                    val task = tasks[index]
                    val isChecked = checkedTasks.contains(index)

                    Card(
                        onClick = {
                            Log.e("WearApp", "Click on task $index")
                            onToggleTask(index)
                        },
                        enabled = true
                    ) {
                        Text(
                            task,
                            style = if (isChecked) {
                                TextStyle(textDecoration = TextDecoration.LineThrough)
                            } else {
                                MaterialTheme.typography.body2
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
