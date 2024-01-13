package com.bodyaka.imgnator

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
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
            setPermissionNameSemiBold(dialogView)

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun launchSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity?.packageName, null)
        startActivity(intent)
    }

    private fun setPermissionNameSemiBold(dialogView: View) {
        val textView = dialogView.findViewById<TextView>(R.id.explanation_text)
        val ss = SpannableString(textView.text)

        val textToBald = resources.getString(R.string.permission_name)

        val startIndex = ss.indexOf(textToBald)
        val endIndex = startIndex + textToBald.length

        val boldSpan = StyleSpan(Typeface.BOLD)

        ss.setSpan(boldSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = ss
    }
}