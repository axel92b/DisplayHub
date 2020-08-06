package android.com.displayhubcompanion.boardsScreen.modulesConfiguration

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.ClockSettingsModel
import android.com.displayhubcompanion.models.StockSettingsModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Source

/**
 * A simple [Fragment] subclass.
 */
class StocksConfigurationFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var symbolEditText: AutoCompleteTextView
    private lateinit var boardMac: String
    private lateinit var docId: String
    private var btManager: BluetoothManager? = null

    private var index: Int? = null
    private var moduleName: String? = null

    private val viewModel: BoardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stocks_configuration, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btManager = viewModel.btManager
        navController = Navigation.findNavController(view)
        symbolEditText = view.findViewById(R.id.symbol_name)
        docId = arguments?.getString("DocId")!!
        boardMac = arguments?.getString("MacAddress")!!
        DatabaseRepository.getStockSettings(Source.CACHE, boardMac, docId).addOnCompleteListener {
            initView(it)
            DatabaseRepository.getStockSettings(Source.SERVER, boardMac, docId).addOnCompleteListener { settings ->
                initView(settings)
            }
        }
    }

    private fun initView(it: Task<StockSettingsModel>) {
        if (it.isSuccessful) {
            val settings = it.result
            if (!settings?.stock_symbol.isNullOrEmpty() && settings?.stock_symbol != symbolEditText.text.toString()) {
                symbolEditText.setText(settings?.stock_symbol)
            }
            if (!settings?.name.isNullOrEmpty()) {
                moduleName = settings?.name
            }
            if (settings?.index != null) {
                index = settings.index
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_action ->{
                if (index != null) {
                    val settingsModel = StockSettingsModel(moduleName,index,symbolEditText.text.toString())
                    DatabaseRepository.setStockSettings(boardMac, docId,settingsModel).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(requireContext(), "Settings have been saved", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
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

    override fun onStart() {
        super.onStart()
        activity?.title = "Stocks configuration"
        btManager?.onInternalErrorCallback = {
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_stocksConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = {
            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_stocksConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionSuccessCallback = null
    }
}
