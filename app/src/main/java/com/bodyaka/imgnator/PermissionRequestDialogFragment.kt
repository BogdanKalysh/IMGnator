package com.bodyaka.imgnator

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment

class PermissionRequestDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.permission_request_dialog_fragment, null)
            builder.setView(dialogView)

            val dialog = builder.create()
            // applying dialog background to be transparent to get rid of corners
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // setting buttons on click actions
            dialogView.findViewById<AppCompatButton>(R.id.settings_button).setOnClickListener {
                launchSettings()
                dismiss()
            }
            dialogView.findViewById<AppCompatButton>(R.id.not_now_button).setOnClickListener {
                dismiss()
            }

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun launchSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity?.packageName, null)
        startActivity(intent)
    }
}