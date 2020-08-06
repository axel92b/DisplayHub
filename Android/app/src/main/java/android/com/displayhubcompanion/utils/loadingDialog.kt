package android.com.displayhubcompanion.utils

import android.app.Dialog
import android.com.displayhubcompanion.R
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide

class LoadingDialog(val activity: FragmentActivity) {
    private var dialog: Dialog? = null

    fun showDialog() {
        dialog = Dialog(activity)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.loading_dialog)
        val gifImageView = dialog?.findViewById<ImageView>(R.id.custom_loading_imageView)

        Glide.with(activity)
            .load(R.drawable.loading)
            .placeholder(R.drawable.loading)
            .centerCrop()
            .into(gifImageView!!)

        dialog?.show();
    }

    fun hideDialog() {
        dialog?.hide()
        dialog?.dismiss()
        dialog = null
    }
}