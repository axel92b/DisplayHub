package android.com.displayhubcompanion.boardsScreen

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.adapters.FirestoreTouchReorderCallback
import android.com.displayhubcompanion.adapters.ModulesFirestoreAdapter
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.ModuleFireStoreModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.com.displayhubcompanion.utils.EscapeSequenceUtils
import android.com.displayhubcompanion.utils.LoadingDialog
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class BoardConfigFragment : Fragment() {
    var board_mac_address: String? = null
    private var board_name: String? = null
    lateinit var placeholder: TextView
    lateinit var modulesAdapter: ModulesFirestoreAdapter
    lateinit var modulesRecycleView: RecyclerView
    private lateinit var addModuleFAB: ExtendedFloatingActionButton
    private lateinit var navController: NavController
    private lateinit var loadingDialog: LoadingDialog
    private var btManager: BluetoothManager? = null

    private val viewModel: BoardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        placeholder = view.findViewById(R.id.no_modules_placeholder)
        addModuleFAB = view.findViewById(R.id.fab_add_module)
        navController = Navigation.findNavController(view)
        board_mac_address = arguments?.getString("MacAddress")
        btManager = viewModel.btManager
//        btManager.messageReceived.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            Toast.makeText(context, it,Toast.LENGTH_SHORT).show()
//        })

        if (board_mac_address != null) {
            val query = DatabaseRepository.getListOfBoardModulesSorted(board_mac_address!!)
            val options = FirestoreRecyclerOptions.Builder<ModuleFireStoreModel>()
                .setQuery(query, ModuleFireStoreModel::class.java).build()
            modulesAdapter = ModulesFirestoreAdapter(options,this,requireContext()).apply {
                mOnItemClickListener = View.OnClickListener {
                        v ->
                    val rvh = v.tag as ModulesFirestoreAdapter.ViewHolder
                    var navigationDestionation: Int? = null
                    when(rvh.name.text){
                        "News Feed" -> {
                            navigationDestionation = R.id.action_boardConfigFragment_to_newsConfigurationFragment
                        }
                        "Clock" -> {
                            navigationDestionation = R.id.action_boardConfigFragment_to_clockConfigurationFragment
                        }
                        "Weather" -> {
                            navigationDestionation = R.id.action_boardConfigFragment_to_weatherConfigurationFragment
                        }
                        "Stocks" ->{
                            navigationDestionation = R.id.action_boardConfigFragment_to_stocksConfigurationFragment
                        }
                    }

                    navigationDestionation?.let { navController.navigate(it, bundleOf( "MacAddress" to board_mac_address, "DocId" to rvh.docId)) }
                }
            }
            modulesRecycleView = view.findViewById<RecyclerView>(R.id.loaded_modules_rec_view).apply{
                layoutManager = LinearLayoutManager(activity)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            if (addModuleFAB.isShown) {
                                addModuleFAB.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_down))
                                addModuleFAB.visibility = View.GONE
                            }
                        } else if (dy < 0) {
                            if (!addModuleFAB.isShown) {
                                addModuleFAB.visibility = View.VISIBLE
                                addModuleFAB.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_up))
                            }
                        }
                    }
                })
                adapter = modulesAdapter
            }
            val touchHelper = ItemTouchHelper(FirestoreTouchReorderCallback(modulesAdapter, board_mac_address!!))
            touchHelper.attachToRecyclerView(modulesRecycleView)
            DatabaseRepository.getBoardInfo(board_mac_address!!).addOnSuccessListener { board ->
                board_name = board["name"].toString()
                activity?.title = "$board_name Config"
            }.addOnCanceledListener {
                invalidMacAddressHandle()
            }
        } else {
            invalidMacAddressHandle()
        }
        addModuleFAB.setOnClickListener { v ->
            navController.navigate(
                R.id.action_boardConfigFragment_to_boardModulesList,
                bundleOf("board_mac_address" to board_mac_address)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.wifi_config -> {
                navController.navigate(R.id.action_boardConfigFragment_to_wifiConfigurationFragment, bundleOf( "MacAddress" to board_mac_address))
            }
            R.id.save_config -> {
                DatabaseRepository.getBoardModules(board_mac_address!!).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val modules = it.result
                        val settings = StringBuilder()
                        for (module in modules!!) {
                            val dict = module.data
                            if(dict["name"] == "Clock"){
                                //TODO handle 12/24 hour
                                val rightNow = Calendar.getInstance()
                                dict["time"] = "${rightNow.get(Calendar.HOUR_OF_DAY)}:${rightNow.get(Calendar.MINUTE)}"
                            }
                            val jsonObject = JSONObject(dict).toString()
                            settings.append("${module.data["name"]}|$jsonObject|")
                        }
                        if (settings.isEmpty()) {
                            return@addOnCompleteListener
                        }
                        settings.deleteCharAt(settings.length - 1)
                        btManager?.sendMessage(EscapeSequenceUtils.getEscapeSequence("Settings", settings.toString()))
                        Toast.makeText(requireContext(),"Completed",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.board_conf_menu,menu)
    }

    //Resubscribe for observers/Attach btManager handlers here
    override fun onStart() {
        super.onStart()
        modulesAdapter.startListening()
        btManager?.onInternalErrorCallback = {
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_boardConfigFragment_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = {
            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_boardConfigFragment_to_boardsListFragment)
        }
        btManager?.onConnectionSuccessCallback = {
            loadingDialog.hideDialog()
            Toast.makeText(context, "Connected!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun invalidMacAddressHandle() {
        Toast.makeText(requireContext(), "There is no mac-address provided!", Toast.LENGTH_LONG)
            .show()
        navController.popBackStack()
    }

    //Unsubscribe observers/Detach btManager handlers here
    override fun onPause() {
        super.onPause()
        modulesAdapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        btManager?.onInternalErrorCallback = null
        btManager?.onConnectionFailureCallback = null
        btManager?.onConnectionSuccessCallback = null
        btManager?.disconnect()
    }
}
