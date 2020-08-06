package android.com.displayhubcompanion.adapters

import android.bluetooth.BluetoothDevice
import android.com.displayhubcompanion.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DiscoveredDevicesAdapter(private val devices: ArrayList<BluetoothDevice>,
                               private val connectDevice: ((View?) -> Unit)?) : RecyclerView.Adapter<DiscoveredDevicesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.discovered_device_card, parent, false)
        v.setOnClickListener(connectDevice)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.name.text = devices[position].name
        holder.macAddress.text = devices[position].address
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.device_name)
        val macAddress: TextView = view.findViewById(R.id.device_mac)
        init {
            view.tag = this
        }
    }
}