package com.manimarank.spell4wiki.ui.compose.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme

/**
 * DialogFragment wrapper for Compose dialogs
 * Allows showing Compose dialogs in traditional Fragment-based activities
 */
class ComposeDialogFragment(
    private val content: @Composable (onDismiss: () -> Unit) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Spell4WikiTheme {
                    content { dismiss() }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the dialog background transparent so Compose dialog handles styling
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar)
    }
}
