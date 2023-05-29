package com.example.objectboxpractice.util

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.example.objectboxpractice.R

object Alerts {

    fun deleteUsersDialog(context: Context,title : String,message : String, listener: () -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val dialog: AlertDialog =
            builder.setTitle(title).setMessage(message)
                .setPositiveButton(R.string.btn_yes) { _, _ ->
                    listener()
                }.setNegativeButton(R.string.btn_no) { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(context.getColor(R.color.green))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }
}