package android.com.displayhubcompanion.boardsScreen.modulesConfiguration

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.com.displayhubcompanion.utils.EscapeSequenceUtils
import android.com.displayhubcompanion.utils.LoadingDialog
import android.view.*
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A simple [Fragment] subclass.
 */
class WifiConfigurationFragment : Fragment() {
    private lateinit var wifiName: TextInputEditText
    private lateinit var wifiPassword: TextInputEditText

    private var btManager: BluetoothManager? = null
    private lateinit var navController: NavController
    private lateinit var dialog: LoadingDialog

    private var connecting: AtomicBoolean = AtomicBoolean(false)

    private lateinit var wifiStatusObserver: androidx.lifecycle.Observer<String?>

    private val viewModel: BoardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_configuration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiName = view.findViewById(R.id.wifi_host_name__input)
        wifiPassword = view.findViewById(R.id.wifi_host_password__input)
        navController = Navigation.findNavController(view)
        btManager = viewModel.btManager!!
        dialog = LoadingDialog(requireActivity())

        wifiStatusObserver = androidx.lifecycle.Observer<String?> {
            val seq = EscapeSequenceUtils.splitEscapeSequence(it!!)
            if (seq[0] != "WifiStatus") {
                Toast.makeText(requireContext(), "${seq[0]} Received as status", Toast.LENGTH_SHORT).show()
                dialog.hideDialog()
            }
            else {
                when (seq[1]) {
                    "Success" -> {
                        connecting.set(false)
                        Toast.makeText(requireContext(), "Successfully connected", Toast.LENGTH_SHORT).show()
                        dialog.hideDialog()
                        navController.popBackStack()
                    }
                    "Error" -> {
                        connecting.set(false)
                        Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
                        dialog.hideDialog()
                    }
                }

            }
        }
        btManager?.messageReceived?.observe(viewLifecycleOwner, wifiStatusObserver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_action ->{
                dialog.showDialog()
                btManager?.sendMessage(EscapeSequenceUtils.getEscapeSequence("WifiConnect",wifiName.text.toString(), wifiPassword.text.toString()))
                connecting.set(true)
                doAsync {
                    var counter = 11
                    while (counter > 0) {
                        Thread.sleep(1000)
                        counter--
                    }
                    uiThread {
                        if (connecting.get()) {
                            dialog.hideDialog()
                            try {
                                //Context may expire
                                Toast.makeText(requireContext(), "Connection timeout", Toast.LENGTH_SHORT).show()
                            } catch (_ : Exception) {}

                        }
                    }
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_menu,menu)
    }

    //Resubscribe for observers/Attach btManager handlers here
    override fun onStart() {
        super.onStart()
        activity?.title = "WIFI configuration"
        btManager?.onInternalErrorCallback = {
            dialog.hideDialog()
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_wifiConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = btManager?.onInternalErrorCallback
        btManager?.onConnectionSuccessCallback = {}
    }

    //Unsubscribe observers/Detach btManager handlers here
    override fun onDestroy() {
        super.onDestroy()
        btManager?.messageReceived?.removeObserver(wifiStatusObserver)
        btManager?.onInternalErrorCallback = null
        btManager?.onConnectionFailureCallback = null
        btManager?.onConnectionSuccessCallback = null
    }
}
