package android.com.displayhubcompanion.boardsScreen.modulesConfiguration

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.ClockSettingsModel
import android.com.displayhubcompanion.models.NewsSettingsModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Source


/**
 * A simple [Fragment] subclass.
 */
class NewsConfigurationFragment : Fragment() {
    private lateinit var feedEditText: AutoCompleteTextView
    private lateinit var boardMac: String
    private lateinit var navController: NavController
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
        return inflater.inflate(R.layout.fragment_news_configuration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btManager = viewModel.btManager
        navController = Navigation.findNavController(view)
        val newsFeeds = arrayOf<String?>("Ynet", "JPost")

        val adapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            newsFeeds
        )

        feedEditText = view.findViewById(R.id.news_feed_source_input)
        feedEditText.apply {
            setAdapter(adapter)
            setText(newsFeeds[0],false)
        }
        docId = arguments?.getString("DocId")!!
        boardMac = arguments?.getString("MacAddress")!!
        DatabaseRepository.getNewsSettings(Source.CACHE, boardMac, docId).addOnCompleteListener {
            initView(it)
            DatabaseRepository.getNewsSettings(Source.SERVER, boardMac, docId).addOnCompleteListener { settings ->
                initView(settings)
            }
        }

    }

    private fun initView(it: Task<NewsSettingsModel>) {
        if (it.isSuccessful) {
            val settings = it.result
            if (!settings?.feed.isNullOrEmpty() && settings?.feed != feedEditText.text.toString()) {
                feedEditText.setText(settings?.feed, false)
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
                    Log.d("GAVNO", feedEditText.text.toString());
                    val settingsModel = NewsSettingsModel(moduleName,index,feedEditText.text.toString())
                    DatabaseRepository.setNewsSettings(boardMac, docId,settingsModel).addOnCompleteListener {
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
        activity?.title = "News configuration"
        btManager?.onInternalErrorCallback = {
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_newsConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = {
            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_newsConfigurationFragment_to_boardsListFragment)
        }
        btManager?.onConnectionSuccessCallback = null
    }
}
