package android.com.displayhubcompanion.adapters


import android.com.displayhubcompanion.R
import android.com.displayhubcompanion.repository.IconRepository
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModulesAdapter(private val modules: ArrayList<String>) : RecyclerView.Adapter<ModulesAdapter.ViewHolder>() {
    var mOnItemClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_module, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return modules.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.name.text = modules[position]
        holder.icon.setImageResource(IconRepository.getIcon(modules[position]))
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.module_name)
        var icon: ImageView = view.findViewById(R.id.imageView2)
        init {
            view.tag = this
            view.setOnClickListener(mOnItemClickListener)
        }
    }
}
