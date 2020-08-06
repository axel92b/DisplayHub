package android.com.displayhubcompanion.boardsScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.adapters.ModulesAdapter
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.Module
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.tasks.Task

/**
 * A simple [Fragment] subclass.
 */
class BoardModulesList : Fragment() {
    private lateinit var modulesRecView: RecyclerView
    private lateinit var navController: NavController
    private lateinit var shimmer: ShimmerFrameLayout
    private var modules = ArrayList<String>()
    private var btManager: BluetoothManager? = null

    private val viewModel: BoardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board_modules_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shimmer = view.findViewById(R.id.mShimmerViewContainer)
        shimmer.startShimmer()
        btManager = viewModel.btManager
        DatabaseRepository.getListOfModules().addOnCompleteListener { task ->
            task.addOnSuccessListener {
                modules.addAll(it)
                modulesRecView.adapter?.notifyDataSetChanged()
                shimmer.stopShimmer()
                shimmer.hideShimmer()
            }.addOnFailureListener{
                Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
            }
        }
        val board_mac = arguments?.getString("board_mac_address")
        navController = Navigation.findNavController(view)
        modulesRecView = view.findViewById(R.id.modules_rec_view)
        modulesRecView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = ModulesAdapter(modules).apply { mOnItemClickListener =  View.OnClickListener {
                    v ->
                val rvh = v.tag as ModulesAdapter.ViewHolder
                DatabaseRepository.getSizeOfListOfBoardModules(board_mac!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val nextIndex = task.result
                        DatabaseRepository.addModuleToBoard(board_mac, Module().apply {
                            name = rvh.name.text.toString()
                            index = nextIndex })
                        navController.popBackStack()
                    }
                }
            } }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = "Available Modules"
        btManager?.onInternalErrorCallback = {
            Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_boardModulesList_to_boardsListFragment)
        }
        btManager?.onConnectionFailureCallback = {
            Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.action_boardModulesList_to_boardsListFragment)
        }
        btManager?.onConnectionSuccessCallback = null
    }
}
