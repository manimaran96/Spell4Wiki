package com.manimarank.spell4wiki.ui.compose.spell4wiktionary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLanguageSelectionBottomSheet
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showCategorySelectionBottomSheet
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.ui.listerners.OnCategorySelectionListener
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupStatusBarHandling
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.Urls

/**
 * Compose implementation of Spell4Wiktionary Activity
 * Replaces the traditional View-based Spell4Wiktionary with modern Jetpack Compose UI
 */
class Spell4WiktionaryComposeActivity : ComponentActivity() {

    private val viewModel: Spell4WiktionaryViewModel by viewModels()
    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup proper status bar handling
        setupStatusBarHandling(window.decorView)

        pref = PrefManager(this)
        
        // Initialize ViewModel with current language
        viewModel.initialize(this, pref.languageCodeSpell4WikiAll)

        setContent {
            Spell4WikiTheme {
                Spell4WiktionaryScreen(
                    viewModel = viewModel,
                    onBackPressed = { handleBackPress() },
                    onLanguageSelection = { showLanguageSelection() },
                    onCategorySelection = { showCategorySelection() },
                    onWiktionaryInfo = { word -> openWiktionaryPage(word) },
                    onRecord = { word -> handleRecord(word) }
                )
            }
        }
    }

    private fun handleBackPress() {
        if (viewModel.hasActiveFilter()) {
            // Show confirmation dialog if filter is active
            showConfirmBackDialog { finish() }
        } else {
            finish()
        }
    }

    private fun showLanguageSelection() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                viewModel.updateLanguage(this@Spell4WiktionaryComposeActivity, langCode)
            }
        }
        showLanguageSelectionBottomSheet(callback, ListMode.SPELL_4_WIKI_ALL)
    }

    private fun showCategorySelection() {
        val callback = object : OnCategorySelectionListener {
            override fun onCallBackListener(category: String?) {
                viewModel.updateCategory(category)
            }
        }
        showCategorySelectionBottomSheet(
            callback,
            ListMode.SPELL_4_WIKI_ALL,
            pref.languageCodeSpell4WikiAll,
            selectedCategory = viewModel.uiState.value.selectedCategory
        )
    }

    private fun openWiktionaryPage(word: String) {
        val intent = Intent(this, com.manimarank.spell4wiki.ui.webui.CommonWebActivity::class.java).apply {
            putExtra(AppConstants.TITLE, word)
            putExtra(AppConstants.URL, String.format(Urls.WIKTIONARY_WEB, pref.languageCodeSpell4WikiAll, word))
            putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
            putExtra(AppConstants.LANGUAGE_CODE, pref.languageCodeSpell4WikiAll)
        }
        startActivity(intent)
    }

    private fun handleRecord(word: String) {
        if (NetworkUtils.isConnected(this)) {
            GeneralUtils.showRecordDialog(this, word, pref.languageCodeSpell4WikiAll)
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Spell4WiktionaryScreen(
    viewModel: Spell4WiktionaryViewModel,
    onBackPressed: () -> Unit,
    onLanguageSelection: () -> Unit,
    onCategorySelection: () -> Unit,
    onWiktionaryInfo: (String) -> Unit,
    onRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInPreview = LocalInspectionMode.current

    val uiState by viewModel.uiState.collectAsState()
    val words by viewModel.words.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showBackDialog by viewModel.showBackConfirmationDialog.collectAsState()
    val showFilterDialog by viewModel.showFilterDialog.collectAsState()

    // Android 16 lifecycle management for privacy and performance
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Pause data loading when app goes to background for privacy
                    if (!isInPreview) {
                        viewModel.pauseDataLoading()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Resume data loading when app comes to foreground
                    if (!isInPreview) {
                        viewModel.resumeDataLoading()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val listState = rememberLazyListState()
    
    // Handle infinite scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= words.size - 3 && 
                    !isLoading && 
                    viewModel.hasMoreData()) {
                    viewModel.loadMoreWords()
                }
            }
    }
    
    Scaffold(
        topBar = {
            Spell4WiktionaryTopBar(
                languageInfo = uiState.languageInfo,
                onBackClick = onBackPressed,
                onLanguageClick = onLanguageSelection,
                onCategoryClick = onCategorySelection,
                onRefreshClick = { viewModel.refreshWords() }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Selection Section
            CategorySelectionSection(
                selectedCategory = uiState.selectedCategory,
                categoryInfo = uiState.categoryInfo,
                onCategoryClick = onCategorySelection,
                modifier = Modifier.padding(16.dp)
            )
            
            // Filter Actions Section
            FilterActionsSection(
                onRunFilter = { viewModel.showFilterDialog() },
                onFilterInfo = { viewModel.showFilterInfo(context) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Words List
            if (words.isEmpty() && !isLoading) {
                EmptyStateContent(
                    onRefresh = { viewModel.refreshWords() }
                )
            } else {
                WordsList(
                    words = words,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshWords() },
                    onWordClick = onRecord,
                    onWiktionaryClick = onWiktionaryInfo,
                    listState = listState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Show back confirmation dialog if needed
    if (showBackDialog) {
        BackConfirmationDialog(
            onConfirm = {
                viewModel.hideBackConfirmationDialog()
                onBackPressed()
            },
            onCancel = {
                viewModel.hideBackConfirmationDialog()
            }
        )
    }
    
    // Show filter dialog if needed
    if (showFilterDialog) {
        FilterProgressDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideFilterDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Spell4WiktionaryTopBar(
    languageInfo: String,
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.spell_4_wiki_all),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
                if (languageInfo.isNotEmpty()) {
                    Text(
                        text = languageInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onCategoryClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_category),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = onLanguageClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_language),
                    contentDescription = stringResource(R.string.select_language),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
    )
}

@Composable
fun CategorySelectionSection(
    selectedCategory: String,
    categoryInfo: String,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.words_category),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedCard(
            onClick = onCategoryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedCategory.ifEmpty { stringResource(R.string.select_category) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedCategory.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (categoryInfo.isNotEmpty()) {
                        Text(
                            text = categoryInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.select_category),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FilterActionsSection(
    onRunFilter: () -> Unit,
    onFilterInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onRunFilter,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.run_filter))
        }

        IconButton(onClick = onFilterInfo) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.run_filter_info)
            )
        }
    }
}

@Composable
fun WordsList(
    words: List<String>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onWordClick: (String) -> Unit,
    onWiktionaryClick: (String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words) { word ->
            WordListItem(
                word = word,
                onWordClick = { onWordClick(word) },
                onWiktionaryClick = { onWiktionaryClick(word) }
            )
        }

        if (isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.loading_words),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordListItem(
    word: String,
    onWordClick: () -> Unit,
    onWiktionaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWordClick() } // Make the entire row clickable for recording
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Dictionary icon for Wiktionary
            IconButton(
                onClick = onWiktionaryClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = stringResource(R.string.wiktionary),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_words_found),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRefresh) {
            Text(stringResource(R.string.refresh))
        }
    }
}

@Composable
fun BackConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(R.string.confirmation),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.confirm_to_back),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun FilterProgressDialog(
    viewModel: Spell4WiktionaryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.filterProgress.collectAsState()
    val total by viewModel.filterTotal.collectAsState()

    AlertDialog(
        onDismissRequest = { /* Don't allow dismiss during filter */ },
        title = {
            Text(
                text = stringResource(R.string.run_filter),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.filter_progress_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LinearProgressIndicator(
                    progress = if (total > 0) progress.toFloat() / total.toFloat() else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "$progress / $total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = modifier
    )
}
