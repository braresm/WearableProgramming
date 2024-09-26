package com.example.android.wearable.datalayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp


@Composable
fun MainApp(
    tasks: List<String>,
    checkedTasks: List<Int>,
    apiAvailable: Boolean,
    onAddTask: (String) -> Unit,
    onToggleTask: (Int) -> Unit
) {
    var newTask by remember { mutableStateOf("") }

    // Box layout to manage the positions of the task list and input field/button
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable task list
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp)
        ) {
            if (!apiAvailable) {
                item {
                    Text(
                        text = stringResource(R.string.wearable_api_unavailable),
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(tasks.size) { index ->
                val task = tasks[index]
                val isChecked = checkedTasks.contains(index)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        task,
                        style = if (isChecked) {
                            TextStyle(textDecoration = TextDecoration.LineThrough)
                        } else {
                            MaterialTheme.typography.body2
                        },
                        modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                    )
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { _ ->
                            onToggleTask(index)
                        }
                    )
                }
                Divider()
            }
        }

        // Input field and button fixed at the bottom of the screen
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newTask,
                onValueChange = { newTask = it },
                label = { Text("New Task") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (newTask.isNotEmpty()) {
                        onAddTask(newTask)
                        newTask = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Add Task")
            }
        }
    }
}


