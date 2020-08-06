package android.com.displayhubcompanion.repository

import android.com.displayhubcompanion.R

class IconRepository {
    companion object {
        fun getIcon(name: String) : Int {
            return when(name) {
                "Clock" -> R.drawable.ic_clock
                "Stocks" -> R.drawable.ic_money
                "Weather" -> R.drawable.ic_cloud
                "News Feed" -> R.drawable.ic_menu
                else -> R.drawable.ic_plugin
            }
        }
    }
}