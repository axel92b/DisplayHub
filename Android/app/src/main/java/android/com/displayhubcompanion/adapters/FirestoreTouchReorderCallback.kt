package android.com.displayhubcompanion.adapters

import android.app.AlertDialog
import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.repository.DatabaseRepository
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks


class FirestoreTouchReorderCallback(private val adapter: ModulesFirestoreAdapter, private val board_mac: String): ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

    private var dragFrom = -1
    private var dragTo = -1
    private var icon: Drawable? = null
    private var background: GradientDrawable? = null



    init {
        icon = adapter.mContext.getDrawable(R.drawable.ic_delete_white_36dp)
        background = GradientDrawable()
        background?.shape = GradientDrawable.RECTANGLE
        background?.setColor(Color.RED)
        background?.cornerRadius = 25F
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView


        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
        val iconBottom = iconTop + icon!!.intrinsicHeight

        when {
            dX > 0 -> { // Swiping to the right
                val iconLeft = itemView.left + iconMargin + icon!!.intrinsicWidth
                val iconRight = itemView.left + iconMargin
                icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background!!.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to the left
                val iconLeft = itemView.right - iconMargin - icon!!.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background!!.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom
                )
            }
            else -> { // view is unSwiped
                background!!.setBounds(0, 0, 0, 0)
            }
        }
        background!!.draw(c)
        icon!!.draw(c)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (dragFrom == -1) {
            dragFrom = source.adapterPosition
        }
        dragTo = target.adapterPosition
        adapter.notifyItemMoved(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val alertDialog: AlertDialog? = adapter.fragment.let { fragment ->
            val builder = AlertDialog.Builder(fragment.context)
                builder.apply {
                    setPositiveButton(R.string.remove) { _, _ ->
                        adapter.notifyItemRemoved(position)
                        DatabaseRepository.getListOfBoardModulesSorted(board_mac).get().addOnCompleteListener {
                            val tasks: ArrayList<Task<Void>> = ArrayList()
                            if (it.isComplete) {
                                val docs = it.result?.documents
                                if (docs != null) {
                                    for (doc in docs) {
                                        val module = doc.data
                                        var index = doc.getLong("index")
                                        if (index!! == position.toLong()) {
                                            tasks.add(DatabaseRepository.removeModule(board_mac, index))
                                            continue
                                        }
                                        if (index > position) {
                                            index -= 1
                                            module?.set("index", index)
                                            tasks.add(DatabaseRepository.setModuleToBoard(board_mac, module!!, doc.id))
                                        }

                                    }
                                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                                    }
                                }
                            }
                        }
                    }
                    setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    setCancelable(false)
                    setMessage(R.string.dialog_remove_module_message)
                    setTitle(R.string.dialog_remove_module_title)
                }
                // Create the AlertDialog
                builder.create()
            }
            alertDialog?.show()

    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        adapter.ignoreChanges = true

        DatabaseRepository.getListOfBoardModulesSorted(board_mac).get().addOnCompleteListener {
            val tasks: ArrayList<Task<Void>> = ArrayList()
            if (it.isComplete) {
                val docs = it.result?.documents
                if (docs != null) {
                    for (doc in docs) {
                        val module = doc.data
                        var index = doc.getLong("index")
                        if (index == dragFrom.toLong()) {
                            index = dragTo.toLong()
                            module?.set("index", index)
                            tasks.add(DatabaseRepository.setModuleToBoard(board_mac, module!!, doc.id))
                            continue
                        }
                        if (dragTo > dragFrom) {
                            if (index!! > dragFrom && index <= dragTo) {
                                index -= 1
                                module?.set("index", index)
                                tasks.add(DatabaseRepository.setModuleToBoard(board_mac,module!!, doc.id))
                            }
                        }
                        else {
                            if (index!! < dragFrom && index >= dragTo) {
                                index += 1
                                module?.set("index", index)
                                tasks.add(DatabaseRepository.setModuleToBoard(board_mac,module!!, doc.id))
                            }
                        }
                    }
                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        adapter.ignoreChanges = false
                        dragFrom = -1
                        dragTo = -1
                    }
                }
            }
        }
    }
}