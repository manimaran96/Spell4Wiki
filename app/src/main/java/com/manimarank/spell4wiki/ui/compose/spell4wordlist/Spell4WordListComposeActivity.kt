package com.manimarank.spell4wiki.ui.compose.spell4wordlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.compose.theme.Spell4WikiTheme
import com.manimarank.spell4wiki.ui.compose.dialogs.BackConfirmationDialog
import com.manimarank.spell4wiki.ui.compose.dialogs.DialogMigrationUtils.showLanguageSelectionBottomSheet
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Compose version of Spell4WordListActivity
 * Provides word list input, file selection, and batch recording functionality using Jetpack Compose
 */
class Spell4WordListComposeActivity : AppCompatActivity() {

    private lateinit var viewModel: Spell4WordListViewModel
    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        pref = PrefManager(this)
        viewModel = ViewModelProvider(this)[Spell4WordListViewModel::class.java]
        
        // Initialize ViewModel with current language
        viewModel.initialize(this, pref.languageCodeSpell4WikiAll)
        
        setContent {
            Spell4WikiTheme {
                Spell4WordListScreen(
                    viewModel = viewModel,
                    onBackPressed = { handleBackPress() },
                    onLanguageSelection = { showLanguageSelection() },
                    onWiktionaryInfo = { word -> openWiktionaryPage(word) },
                    onRecord = { word -> handleRecord(word) },
                    onFileSelected = { uri -> handleFileSelection(uri) }
                )
            }
        }
    }

    private fun handleBackPress() {
        if (viewModel.hasContent()) {
            // Show confirmation dialog if there's content
            viewModel.showBackConfirmationDialog()
        } else {
            finish()
        }
    }

    private fun showLanguageSelection() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                viewModel.updateLanguage(this@Spell4WordListComposeActivity, langCode)
            }
        }
        showLanguageSelectionBottomSheet(callback, ListMode.SPELL_4_WIKI_ALL)
    }

    private fun openWiktionaryPage(word: String) {
        if (word.isNotEmpty()) {
            val intent = Intent(this, com.manimarank.spell4wiki.ui.webui.CommonWebActivity::class.java)
            intent.putExtra(AppConstants.URL, "https://${viewModel.languageCode.value}.wiktionary.org/wiki/$word")
            intent.putExtra(AppConstants.TITLE, word)
            intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
            intent.putExtra(AppConstants.LANGUAGE_CODE, viewModel.languageCode.value)
            startActivity(intent)
        }
    }

    private fun handleRecord(word: String) {
        if (word.isNotEmpty()) {
            if (isConnected(applicationContext)) {
                val trimmedWord = word.trim()
                if (viewModel.isAllowRecord(trimmedWord)) {
                    showRecordDialog(this, trimmedWord, viewModel.languageCode.value)
                } else {
                    showLong(
                        findViewById(android.R.id.content),
                        String.format(getString(R.string.audio_file_already_exist), trimmedWord)
                    )
                }
            } else {
                showLong(findViewById(android.R.id.content), getString(R.string.check_internet))
            }
        }
    }

    private fun handleFileSelection(uri: Uri) {
        try {
            contentResolver.openInputStream(uri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val fileContent = StringBuilder()
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        fileContent.append(line).append("\n")
                    }
                    viewModel.setFileContent(fileContent.toString())
                    viewModel.switchToEditMode()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showLong(findViewById(android.R.id.content), "Error reading file: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Spell4WordListScreen(
    viewModel: Spell4WordListViewModel,
    onBackPressed: () -> Unit,
    onLanguageSelection: () -> Unit,
    onWiktionaryInfo: (String) -> Unit,
    onRecord: (String) -> Unit,
    onFileSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val currentMode by viewModel.currentMode.collectAsState()
    val languageInfo by viewModel.languageInfo.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val wordList by viewModel.wordList.collectAsState()
    val showBackDialog by viewModel.showBackConfirmationDialog.collectAsState()
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.spell4wordlist),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (languageInfo.isNotEmpty()) {
                            Text(
                                text = languageInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLanguageSelection) {
                        Icon(
                            painter = painterResource(R.drawable.ic_language),
                            contentDescription = stringResource(R.string.select_language),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentMode) {
                Spell4WordListMode.SELECT -> {
                    SelectModeContent(
                        onSelectFile = {
                            filePickerLauncher.launch(arrayOf("text/plain"))
                        },
                        onDirectContent = {
                            viewModel.switchToEditMode()
                        }
                    )
                }
                Spell4WordListMode.EDIT -> {
                    EditModeContent(
                        fileContent = fileContent,
                        onContentChange = { viewModel.updateFileContent(it) },
                        onDone = {
                            keyboardController?.hide()
                            viewModel.processWordList()
                        },
                        onBack = {
                            viewModel.switchToSelectMode()
                        }
                    )
                }
                Spell4WordListMode.LIST -> {
                    ListModeContent(
                        wordList = wordList,
                        onWordClick = { word -> onRecord(word) },
                        onWiktionaryClick = { word -> onWiktionaryInfo(word) },
                        onBack = {
                            viewModel.switchToEditMode()
                        }
                    )
                }
                Spell4WordListMode.EMPTY -> {
                    EmptyStateContent(
                        onBack = {
                            viewModel.switchToSelectMode()
                        }
                    )
                }
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
}

@Composable
fun SelectModeContent(
    onSelectFile: () -> Unit,
    onDirectContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon
        Image(
            painter = painterResource(R.drawable.ic_spell4word_list),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp)
        )
        
        // Select File Button
        Button(
            onClick = onSelectFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.select_file),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Direct Content Button
        OutlinedButton(
            onClick = onDirectContent,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.direct_content),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Helper Text
        Text(
            text = stringResource(R.string.hint_select_file),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
fun EditModeContent(
    fileContent: String,
    onContentChange: (String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.back))
            }

            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.next),
                    color = Color.White
                )
            }
        }

        // Content Input Field
        OutlinedTextField(
            value = fileContent,
            onValueChange = onContentChange,
            label = {
                Text(stringResource(R.string.type_word))
            },
            placeholder = {
                Text(stringResource(R.string.hint_direct_copy_next))
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            maxLines = Int.MAX_VALUE,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ListModeContent(
    wordList: List<String>,
    onWordClick: (String) -> Unit,
    onWiktionaryClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack
            ) {
                Text(stringResource(R.string.back))
            }
        }

        // Word List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(wordList) { word ->
                WordListItem(
                    word = word,
                    onWordClick = { onWordClick(word) },
                    onWiktionaryClick = { onWiktionaryClick(word) }
                )
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
        modifier = modifier
            .fillMaxWidth()
            .clickable { onWordClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onWiktionaryClick
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.info),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.provide_valid_content),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.back),
                color = Color.White
            )
        }
    }
}

enum class Spell4WordListMode {
    SELECT, EDIT, LIST, EMPTY
}
