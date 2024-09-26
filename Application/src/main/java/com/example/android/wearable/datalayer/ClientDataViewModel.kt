package com.example.android.wearable.datalayer

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent

class ClientDataViewModel :
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private val _tasks = mutableStateListOf<String>()
    private val _checkedTasks = mutableStateListOf<Int>()

    val tasks: List<String> = _tasks
    val checkedTasks: List<Int> = _checkedTasks


    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem

                if (dataItem.uri.path == "/todo_list") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val currentTasks = dataMap.getStringArrayList("tasks")
                    if (currentTasks != null) {
                        _tasks.clear()
                        _tasks.addAll(currentTasks)
                    }
                }

                if (dataItem.uri.path == "/todo_checked") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val checkedTasks = dataMap.getIntegerArrayList("tasks_checked")
                    if (checkedTasks != null) {
                        _checkedTasks.clear()
                        _checkedTasks.addAll(checkedTasks)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
    }
}
