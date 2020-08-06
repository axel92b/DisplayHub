package android.com.displayhubcompanion.utils

import android.annotation.SuppressLint
import android.com.displayhubcompanion.bluetooth.BluetoothManager
import android.com.displayhubcompanion.bluetooth.BluetoothSerialDevice
import android.com.displayhubcompanion.bluetooth.SimpleBluetoothDeviceInterface
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("CheckResult")
class BluetoothManager(private val macAddress: String) {
    private val bluetoothManager: BluetoothManager = BluetoothManager.instance!!
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface
    private var connected: Boolean = false

    val messageReceived: SingleLiveEvent<String> = SingleLiveEvent()


    //There is possibility to replace those fields with array lists(events simulation)
    var onInternalErrorCallback: ((Throwable) -> Unit)? = null
    var onConnectionSuccessCallback: (() -> Unit)? = null
    var onConnectionFailureCallback: ((Throwable) -> Unit)? = null

    init {
        bluetoothManager.closeDevice(macAddress)
        bluetoothManager.openSerialDevice(macAddress)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnected, this::onError)
    }

    fun disconnect() {
        if (connected) {
            bluetoothManager.closeDevice(macAddress)
            connected = false
        }
    }

    fun sendMessage(message: String) {
        deviceInterface.sendMessage(message)
    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface()

        // Listen to bluetooth events
        deviceInterface.setListeners( object: SimpleBluetoothDeviceInterface.OnMessageReceivedListener {
            override fun onMessageReceived(message: String) {
                messageReceived.value = message
            }
        }, object: SimpleBluetoothDeviceInterface.OnMessageSentListener {
            override fun onMessageSent(message: String) {
            }
        }, object: SimpleBluetoothDeviceInterface.OnErrorListener {
            override fun onError(error: Throwable) {
                onInternalErrorCallback?.invoke(error)
            }
        })
        connected = true
        onConnectionSuccessCallback?.invoke()
    }

    private fun onError(error: Throwable) {
        //Can't connect, go back and chose another device
        onConnectionFailureCallback?.invoke(error)
    }
}