package android.com.displayhubcompanion.boardsScreen

import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.adapters.BoardsFireStoreAdapter
import android.com.displayhubcompanion.boardsScreen.viewmodel.BoardsViewModel
import android.com.displayhubcompanion.models.BoardFireStoreModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.utils.BluetoothManager
import android.com.displayhubcompanion.utils.LoadingDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * A simple [Fragment] subclass.
 */
class BoardsListFragment : Fragment() {
    private lateinit var addBoardButton: ExtendedFloatingActionButton
    lateinit var placeholder: TextView
    lateinit var boardsAdapter: FirestoreRecyclerAdapter<BoardFireStoreModel, BoardsFireStoreAdapter.ViewHolder>
    lateinit var boardsRecycleView: RecyclerView
    private lateinit var navController: NavController
    private lateinit var loadingDialog: LoadingDialog
    private var btManager: BluetoothManager? = null
    private var macAddress: String? = null

    private val viewModel: BoardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_boards_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        placeholder = view.findViewById(R.id.no_boards_placeholder)
        addBoardButton = view.findViewById(R.id.fab_add_board)
        navController = Navigation.findNavController(view)
        setButtonOnClickListeners()
        val query = DatabaseRepository.getMyBoardsQuery()
        val options = FirestoreRecyclerOptions.Builder<BoardFireStoreModel>()
            .setQuery(query, BoardFireStoreModel::class.java)
            .build()
        boardsAdapter = BoardsFireStoreAdapter(options,this,requireContext()).apply {
            mOnItemClickListener = View.OnClickListener {
                v ->
                val rvh = v.tag as BoardsFireStoreAdapter.ViewHolder
                macAddress = rvh.macAddress!!
                loadingDialog.showDialog()
                btManager = BluetoothManager(rvh.macAddress!!)
                btManager?.onInternalErrorCallback = {
                    Toast.makeText(context, "Device disconnected or internal bluetooth error", Toast.LENGTH_SHORT).show()
                }
                btManager?.onConnectionFailureCallback = {
                    loadingDialog.hideDialog()
                    Toast.makeText(context, "Can't connect to device, possibly not in range or other kind of error", Toast.LENGTH_SHORT).show()
                }
                btManager?.onConnectionSuccessCallback = {
                    loadingDialog.hideDialog()
                    Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
                    viewModel.btManager = btManager
                    navController.navigate(R.id.action_boardsListFragment_to_boardConfigFragment, bundleOf( "MacAddress" to macAddress!!))
                }
            }
        }
        boardsRecycleView = view.findViewById<RecyclerView>(R.id.boards_rec_view).apply{
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (addBoardButton.isShown) {
                            addBoardButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_down))
                            addBoardButton.visibility = View.GONE
                        }
                    } else if (dy < 0) {
                        if (!addBoardButton.isShown) {
                            addBoardButton.visibility = View.VISIBLE
                            addBoardButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_up))
                        }
                    }
                }
            })
            adapter = boardsAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.title = (DatabaseRepository.getFirebaseAuth().currentUser?.displayName?.split(" ")
            ?.get(0) ?: "My") + " " + getString(R.string.my_boards)
        boardsAdapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        boardsAdapter.stopListening()
    }

    //Put all setters for button listeners here
    private fun setButtonOnClickListeners() {
        addBoardButton.setOnClickListener {
            navController.navigate(R.id.action_boardsListFragment_to_boardAddFragment)
        }
    }
}
