package com.example.android.wearable.datalayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.util.ArrayList
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            WearApp(
                mainViewModel=mainViewModel,
                onToggleTask = { taskId ->
                    coroutineScope.launch {
                        toggleTask(taskId)
                    }
                }
            )
        }
    }

    private suspend fun syncTasks(tasks: List<String>, selected: List<Int>) {
        try {
            val requestChecked = PutDataMapRequest.create(TODO_CHECKED_PATH).apply {
                dataMap.putIntegerArrayList(TASKS_CHECKED_KEY, ArrayList(selected))
            }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(requestChecked).await()

            val requestToDo = PutDataMapRequest.create(TODO_PATH).apply {
                dataMap.putStringArrayList(TASKS_KEY, ArrayList(tasks))
            }
                .asPutDataRequest()
                .setUrgent()

            dataClient.putDataItem(requestToDo).await()



        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    private suspend fun toggleTask(taskId: Int) {
        try {
            val currentChecked = mainViewModel.checkedTasks.toMutableList()
            val isChecked = currentChecked.contains(taskId)

            if(isChecked) {
                currentChecked.remove(taskId)
            } else {
                currentChecked.add(taskId)
            }

            syncTasks(mainViewModel.tasks, currentChecked)

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(mainViewModel)
        messageClient.addListener(mainViewModel)
        capabilityClient.addListener(
            mainViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(mainViewModel)
        messageClient.removeListener(mainViewModel)
        capabilityClient.removeListener(mainViewModel)
    }

    companion object {
        private const val TAG = "MainActivity"

        const val CAMERA_CAPABILITY = "camera"
        const val WEAR_CAPABILITY = "wear"
        const val MOBILE_CAPABILITY = "mobile"

        private const val TODO_PATH = "/todo_list"
        private const val TODO_CHECKED_PATH = "/todo_checked"
        private const val TASKS_KEY = "tasks"
        private const val TASKS_CHECKED_KEY = "tasks_checked"

    }
}
