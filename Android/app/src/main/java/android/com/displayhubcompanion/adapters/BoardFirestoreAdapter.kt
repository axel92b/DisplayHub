package android.com.displayhubcompanion.adapters

import android.app.AlertDialog
import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.BoardsListFragment
import android.com.displayhubcompanion.models.BoardFireStoreModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class BoardsFireStoreAdapter(options: FirestoreRecyclerOptions<BoardFireStoreModel>, val fragment: BoardsListFragment, val mContext: Context) :
    FirestoreRecyclerAdapter<BoardFireStoreModel, BoardsFireStoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_board, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: BoardFireStoreModel
    ) {
        holder.container.animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in)
        holder.name.text = item.name
        holder.macAddress = item.mac_address
        holder.itemView.setOnLongClickListener {
            val alertDialog: AlertDialog? = fragment.let {
                val builder = AlertDialog.Builder(it.context)
                builder.apply {
                    setPositiveButton(R.string.remove, DialogInterface.OnClickListener { dialog, id ->
                        DatabaseRepository.removeBoard(holder.macAddress!!)
                    })
                    setCancelable(true)
                    setMessage(R.string.dialog_remove_board_message)
                    setTitle(R.string.dialog_remove_board_title)
                }
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()
            true
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        fragment.apply {
            if (itemCount <= 0) {
                placeholder.visibility = View.VISIBLE
            } else {
                placeholder.visibility = View.GONE
            }
        }
    }


    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var container: MaterialCardView = view.findViewById(R.id.board_card_container)
        var name: TextView = view.findViewById(R.id.board_name)
        var macAddress: String? = null
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }

    }
}