package com.manimarank.spell4wiki.ui.compose.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme

/**
 * BottomSheetDialogFragment wrapper for Compose bottom sheets
 * Allows showing Compose bottom sheets in traditional Fragment-based activities
 */
class ComposeBottomSheetFragment(
    private val content: @Composable (onDismiss: () -> Unit) -> Unit
) : BottomSheetDialogFragment() {

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
}
