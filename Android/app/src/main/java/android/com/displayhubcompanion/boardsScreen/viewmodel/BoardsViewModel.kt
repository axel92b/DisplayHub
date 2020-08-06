package android.com.displayhubcompanion.boardsScreen.viewmodel

import android.com.displayhubcompanion.utils.BluetoothManager
import androidx.lifecycle.ViewModel

class BoardsViewModel: ViewModel() {
    var btManager: BluetoothManager? = null
        set(value) {
            field?.onConnectionFailureCallback = null
            field?.onConnectionSuccessCallback = null
            field?.onInternalErrorCallback = null
            field?.disconnect()
            field = value
        }

}