package android.com.displayhubcompanion.adapters

import android.app.AlertDialog
import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.boardsScreen.BoardConfigFragment
import android.com.displayhubcompanion.models.ModuleFireStoreModel
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.com.displayhubcompanion.repository.IconRepository
import android.content.Context
import android.content.DialogInterface
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.log

class ModulesFirestoreAdapter (options: FirestoreRecyclerOptions<ModuleFireStoreModel>, val fragment: BoardConfigFragment, val mContext: Context) :
    FirestoreRecyclerAdapter<ModuleFireStoreModel, ModulesFirestoreAdapter.ViewHolder>(options) {

    var mOnItemClickListener: View.OnClickListener? = null
    var ignoreChanges = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: ModuleFireStoreModel
    ) {
        holder.container.animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in)
        holder.name.text = item.name
        holder.docId = snapshots.getSnapshot(position).id
        holder.icon.setImageResource(IconRepository.getIcon(item.name!!))
}

    override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {
        if(!ignoreChanges) {
            super.onChildChanged(type, snapshot, newIndex, oldIndex)
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
        var container: MaterialCardView = view.findViewById(R.id.module_card)
        var name: TextView = view.findViewById(R.id.module_name)
        var icon: ImageView = view.findViewById(R.id.imageView2)
        var macAddress: String? = null
        var docId: String? = null
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }

    }
}