package com.manimarank.spell4wiki.ui.compose.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Common Compose extensions and utility functions
 * Provides reusable patterns and helper functions for Compose UI
 */

/**
 * Extension function to handle infinite scrolling in LazyColumn/LazyRow
 */
@Composable
fun LazyListState.OnBottomReached(
    loadMore: () -> Unit,
    buffer: Int = 3
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - buffer
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad) {
                    loadMore()
                }
            }
    }
}

/**
 * Extension function to detect when user reaches the top (for pull-to-refresh)
 */
@Composable
fun LazyListState.OnTopReached(
    onTopReached: () -> Unit
) {
    LaunchedEffect(this) {
        snapshotFlow { firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index == 0) {
                    onTopReached()
                }
            }
    }
}

/**
 * Common modifier for standard screen padding
 */
fun Modifier.screenPadding() = this.padding(16.dp)

/**
 * Common modifier for card content padding
 */
fun Modifier.cardContentPadding() = this.padding(12.dp)

/**
 * Common modifier for section spacing
 */
fun Modifier.sectionSpacing() = this.padding(vertical = 8.dp)

/**
 * Extension function to apply conditional modifiers
 */
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

/**
 * Common text styles for consistent typography
 */
object CommonTextStyles {
    
    @Composable
    fun SectionTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier.fillMaxWidth()
        )
    }
    
    @Composable
    fun SectionSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier.fillMaxWidth()
        )
    }
    
    @Composable
    fun BodyText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = textAlign,
            modifier = modifier
        )
    }
    
    @Composable
    fun CaptionText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign: TextAlign = TextAlign.Start
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            textAlign = textAlign,
            modifier = modifier
        )
    }
    
    @Composable
    fun ErrorText(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = textAlign,
            modifier = modifier
        )
    }
    
    @Composable
    fun SuccessText(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = textAlign,
            modifier = modifier
        )
    }
}

/**
 * Common spacing values for consistent layout
 */
object CommonSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    
    val cardPadding = 12.dp
    val screenPadding = 16.dp
    val sectionSpacing = 24.dp
    val itemSpacing = 8.dp
}

/**
 * Common elevation values for consistent Material Design
 */
object CommonElevation {
    val none = 0.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val extraLarge = 16.dp
}

/**
 * Common corner radius values for consistent shapes
 */
object CommonCornerRadius {
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val extraLarge = 16.dp
}

/**
 * Extension function to safely get string from nullable string resource
 */
@Composable
fun String?.orEmpty(): String = this ?: ""

/**
 * Extension function to format text with proper capitalization
 */
fun String.toTitleCase(): String {
    return this.lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

/**
 * Extension function to truncate text with ellipsis
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        "${this.take(maxLength - 3)}..."
    }
}

/**
 * Extension function to check if string is valid (not null, not empty, not blank)
 */
fun String?.isValid(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * Extension function to get initials from a name
 */
fun String.getInitials(maxChars: Int = 2): String {
    return this.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(maxChars)
        .joinToString("")
        .uppercase()
}

/**
 * Common validation functions
 */
object CommonValidation {
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String, minLength: Int = 6): Boolean {
        return password.length >= minLength
    }
    
    fun isValidUsername(username: String, minLength: Int = 3): Boolean {
        return username.length >= minLength && username.all { it.isLetterOrDigit() || it == '_' }
    }
    
    fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }
}

/**
 * Common animation durations for consistent motion
 */
object CommonAnimationDuration {
    const val fast = 150
    const val normal = 300
    const val slow = 500
    const val extraSlow = 1000
}

/**
 * Extension function to apply alpha based on enabled state
 */
fun Modifier.enabledAlpha(enabled: Boolean): Modifier {
    return this.then(
        if (enabled) Modifier else Modifier.alpha(0.6f)
    )
}
