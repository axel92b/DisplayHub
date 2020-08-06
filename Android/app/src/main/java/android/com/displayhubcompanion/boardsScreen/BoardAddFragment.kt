package android.com.displayhubcompanion.boardsScreen

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.adapters.DiscoveredDevicesAdapter
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.BoardFirebaseModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.com.displayhubcompanion.utils.LoadingDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception


class BoardAddFragment : Fragment() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val btDevices = ArrayList<BluetoothDevice>()
    private val REQUEST_ENABLE_BT = 1000
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1001
    private var connectionInProgress: Boolean = false
    private lateinit var devicesRecView: RecyclerView
    private lateinit var navController: NavController
    lateinit var loadingDialog: LoadingDialog
    private var btManager: BluetoothManager? = null

    private val viewModel: BoardsViewModel by activityViewModels()

    private val broadcastReceiver = object : BroadcastReceiver() {

        var btDevice: BluetoothDevice? = null

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (btDevices.all { it.address != device.address}) {
                        btDevices.add(device)
                        devicesRecView.adapter?.notifyDataSetChanged()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!

                    if (device.bondState == BluetoothDevice.BOND_BONDED) {
                        Toast.makeText(context,"Paired to ${device.name}",Toast.LENGTH_SHORT).show()
                        DatabaseRepository.addNewBoardToLoggedInUser(
                            BoardFirebaseModel(
                                device.name,
                                device.address
                            )
                        )
                        btDevice = device
                        bluetoothDiscoveryCancellation()
                        btManager = BluetoothManager(device.address)
                        btManager?.onInternalErrorCallback = {
                            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
                            loadingDialog.hideDialog()
                        }
                        btManager?.onConnectionFailureCallback = {
                            loadingDialog.hideDialog()
                            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
                        }
                        btManager?.onConnectionSuccessCallback = {
                            loadingDialog.hideDialog()
                            Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
                            viewModel.btManager = btManager
                            navController.navigate(R.id.action_boardAddFragment_to_boardConfigFragment, bundleOf( "MacAddress" to btDevice!!.address ))
                        }
                    }
                    else if (device.bondState == BluetoothDevice.BOND_NONE) {
                        Toast.makeText(context,"User canceled pairing",Toast.LENGTH_SHORT).show()
                        connectionInProgress = false
                        bluetoothAdapter?.startDiscovery()
                        loadingDialog.hideDialog()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        loadingDialog = LoadingDialog(requireActivity())
        devicesRecView = view.findViewById(R.id.devices_rec_view)
        devicesRecView.layoutManager = LinearLayoutManager(context)
        devicesRecView.adapter = DiscoveredDevicesAdapter(btDevices, this::connectToDevice)

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth isn't supported, can't proceed", Toast.LENGTH_SHORT).show()
        }
        when {
            bluetoothAdapter?.isEnabled == false -> {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
            }
            else -> {
                registerBtDiscovering()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
                    registerBtDiscovering()
                } else {
                    Toast.makeText(context, "Location permission is necessary for bluetooth functionality", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun registerBtDiscovering() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        activity?.registerReceiver(broadcastReceiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                registerBtDiscovering()
            }
            else {
                Toast.makeText(context, "Please enable bluetooth manually inorder to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //used in recycler view as on item click handler
    private fun connectToDevice(view: View?) {
        if (connectionInProgress) {
            return
        }
        loadingDialog.showDialog()
        bluetoothAdapter?.cancelDiscovery()
        connectionInProgress = true
        val viewHolder = view?.tag as DiscoveredDevicesAdapter.ViewHolder
        val position = viewHolder.adapterPosition
        if (!btDevices[position].createBond()) {
            loadingDialog.hideDialog()
            connectionInProgress = false
            bluetoothAdapter?.startDiscovery()
            Toast.makeText(context, "Can't initiate pairing process, internal error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = getString(R.string.add_board)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothDiscoveryCancellation()
    }

    private fun bluetoothDiscoveryCancellation() {
        bluetoothAdapter?.cancelDiscovery()
        try {
            activity?.unregisterReceiver(broadcastReceiver)
        }
        catch (_: Exception) {
            //don't do anything
        }
    }
}
