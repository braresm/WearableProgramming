package com.example.android.wearable.datalayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.util.ArrayList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("VisibleForTests")
class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            MaterialTheme {
                var apiAvailable by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    apiAvailable = isAvailable(capabilityClient)
                }
                MainApp(
                    tasks = clientDataViewModel.tasks,
                    checkedTasks = clientDataViewModel.checkedTasks,
                    apiAvailable = apiAvailable,
                    onAddTask = { task ->
                        coroutineScope.launch {
                            sendTask(task)
                        }
                    },
                    onToggleTask = { taskId ->
                        coroutineScope.launch {
                            toggleTask(taskId)
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    private suspend fun syncTasks(tasks: List<String>, selected: List<Int>) {
        Log.e("syncTasks", "syncTasks")

        try {
            Log.e("syncTasks", "currentTasks List: ${tasks.joinToString(", ")}")
            Log.e("syncTasks", "currentChecked List: ${selected.joinToString(", ")}")

            val requestToDo = PutDataMapRequest.create(TODO_PATH).apply {
                dataMap.putStringArrayList(TASKS_KEY, ArrayList(tasks))
            }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(requestToDo).await()

            val requestChecked = PutDataMapRequest.create(TODO_CHECKED_PATH).apply {
                dataMap.putIntegerArrayList(TASKS_CHECKED_KEY, ArrayList(selected))
            }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(requestChecked).await()

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    private suspend fun toggleTask(taskId: Int) {
        try {
            val currentChecked = clientDataViewModel.checkedTasks.toMutableList()
            val isChecked = currentChecked.contains(taskId)

            if(isChecked) {
                currentChecked.remove(taskId)
            } else {
                currentChecked.add(taskId)
            }

            syncTasks(clientDataViewModel.tasks, currentChecked)

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    private suspend fun sendTask(task: String) {
        try {
            val currentTasks = clientDataViewModel.tasks.toMutableList()
            currentTasks.add(task)

            syncTasks(currentTasks, clientDataViewModel.checkedTasks)

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    private suspend fun isAvailable(api: GoogleApi<*>): Boolean {
        return try {
            GoogleApiAvailability.getInstance()
                .checkApiAvailability(api)
                .await()

            true
        } catch (e: AvailabilityException) {
            Log.d(
                TAG,
                "${api.javaClass.simpleName} API is not available in this device."
            )
            false
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val TODO_PATH = "/todo_list"
        private const val TODO_CHECKED_PATH = "/todo_checked"
        private const val TASKS_KEY = "tasks"
        private const val TASKS_CHECKED_KEY = "tasks_checked"

    }
}
