package android.com.displayhubcompanion.boardsScreen.modulesConfigurationnavigation_destionation

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.ClockSettingsModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.view.*
import android.widget.ArrayAdapter
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
class ClockConfigurationFragment : Fragment() {

    private lateinit var formatEditText: AutoCompleteTextView
    private lateinit var regionEditText: AutoCompleteTextView
    private lateinit var timeDisplayFormatEditText: AutoCompleteTextView
    private lateinit var navController: NavController
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
        return inflater.inflate(R.layout.fragment_clock_configuration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btManager = viewModel.btManager
        navController = Navigation.findNavController(view)
        val format = arrayOf<String?>("12H", "24H")
        val formatAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            format
        )
        formatEditText = view.findViewById(R.id.clock_format_input)
        formatEditText.apply {
            setAdapter(formatAdapter)
            setText(format[0],false)
        }

        val region = arrayOf<String?>("Israel","Russia","USA")
        val regionAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            region
        )
        regionEditText = view.findViewById(R.id.clock_region_input)
        regionEditText.apply {
            setAdapter(regionAdapter)
            setText(region[0],false)
        }

        val timeDisplayFormat = arrayOf<String?>("HH:mm","M/D HH::MM")
        val timeDisplayFormatAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            timeDisplayFormat
        )
        timeDisplayFormatEditText = view.findViewById(R.id.clock_time_display_format_input)
        timeDisplayFormatEditText.apply {
            setAdapter(timeDisplayFormatAdapter)
            setText(timeDisplayFormat[0],false)
        }
        docId = arguments?.getString("DocId")!!
        boardMac = arguments?.getString("MacAddress")!!
        DatabaseRepository.getClockSettings(Source.CACHE, boardMac, docId).addOnCompleteListener {
            initView(it)
            DatabaseRepository.getClockSettings(Source.SERVER, boardMac, docId).addOnCompleteListener { settings ->
                initView(settings)
            }
        }

    }

    private fun initView(it: Task<ClockSettingsModel>) {
        if (it.isSuccessful) {
            val settings = it.result
            if (!settings?.format.isNullOrEmpty() && settings?.format != formatEditText.text.toString()) {
                formatEditText.setText(settings?.format, false)
            }
            if (!settings?.region.isNullOrEmpty() && settings?.region != regionEditText.text.toString()) {
                regionEditText.setText(settings?.region, false)
            }
            if (!settings?.display_format.isNullOrEmpty() && settings?.display_format != timeDisplayFormatEditText.text.toString()) {
                timeDisplayFormatEditText.setText(settings?.display_format, false)
            }
            if (!settings?.name.isNullOrEmpty()) {
                moduleName = settings?.name
            }
            if (settings?.index != null) {
                index = settings.index
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_action ->{
                if (index != null) {
                    val settingsModel = ClockSettingsModel(moduleName,index,formatEditText.text.toString(),
                        regionEditText.text.toString(),timeDisplayFormatEditText.text.toString())
                    DatabaseRepository.setClockSettings(boardMac, docId,settingsModel).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(requireContext(), "Settings have been saved",Toast.LENGTH_SHORT).show()
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
        activity?.title = "Clock configuration"
        btManager?.onInternalErrorCallback = {
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_clockConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = {
            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_clockConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionSuccessCallback = null
    }

}
