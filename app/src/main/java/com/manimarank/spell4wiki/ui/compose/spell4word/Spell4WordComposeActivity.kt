package com.manimarank.spell4wiki.ui.compose.spell4word

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
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

/**
 * Compose version of Spell4WordActivity
 * Provides word input and recording functionality using Jetpack Compose
 */
class Spell4WordComposeActivity : AppCompatActivity() {

    private lateinit var viewModel: Spell4WordViewModel
    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        pref = PrefManager(this)
        viewModel = ViewModelProvider(this)[Spell4WordViewModel::class.java]
        
        // Initialize ViewModel with current language
        viewModel.initialize(this, pref.languageCodeSpell4WikiAll)
        
        setContent {
            Spell4WikiTheme {
                Spell4WordScreen(
                    viewModel = viewModel,
                    onBackPressed = { handleBackPress() },
                    onLanguageSelection = { showLanguageSelection() },
                    onWiktionaryInfo = { word -> openWiktionaryPage(word) },
                    onRecord = { word -> handleRecord(word) }
                )
            }
        }
    }

    private fun handleBackPress() {
        if (!TextUtils.isEmpty(viewModel.wordInput.value)) {
            // Show confirmation dialog if there's text input
            viewModel.showBackConfirmationDialog()
        } else {
            finish()
        }
    }

    private fun showLanguageSelection() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                viewModel.updateLanguage(this@Spell4WordComposeActivity, langCode)
            }
        }
        showLanguageSelectionBottomSheet(callback, ListMode.SPELL_4_WIKI_ALL)
    }

    private fun openWiktionaryPage(word: String) {
        if (word.isNotEmpty() && word.length < 30) {
            val intent = Intent(this, com.manimarank.spell4wiki.ui.webui.CommonWebActivity::class.java)
            intent.putExtra(AppConstants.URL, "https://${viewModel.languageCode.value}.wiktionary.org/wiki/$word")
            intent.putExtra(AppConstants.TITLE, word)
            intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
            intent.putExtra(AppConstants.LANGUAGE_CODE, viewModel.languageCode.value)
            startActivity(intent)
        } else {
            showLong(findViewById(android.R.id.content), getString(R.string.enter_valid_word))
        }
    }

    private fun handleRecord(word: String) {
        if (word.isNotEmpty() && word.length < 30) {
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
        } else {
            showLong(findViewById(android.R.id.content), getString(R.string.enter_valid_word))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Spell4WordScreen(
    viewModel: Spell4WordViewModel,
    onBackPressed: () -> Unit,
    onLanguageSelection: () -> Unit,
    onWiktionaryInfo: (String) -> Unit,
    onRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val wordInput by viewModel.wordInput.collectAsState()
    val languageInfo by viewModel.languageInfo.collectAsState()
    val showBackDialog by viewModel.showBackConfirmationDialog.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.spell4word),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Icon
                Image(
                    painter = painterResource(R.drawable.ic_spell4word),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 32.dp)
                )
                
                // Word Input Field
                OutlinedTextField(
                    value = wordInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 30) {
                            viewModel.updateWordInput(newValue)
                        }
                    },
                    label = {
                        Text(stringResource(R.string.enter_word))
                    },
                    placeholder = {
                        Text(stringResource(R.string.enter_word))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onWiktionaryInfo(wordInput)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.info),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            onRecord(wordInput)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Record Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onRecord(wordInput)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(48.dp)
                        .widthIn(min = 120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.record),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Helper Text
                Text(
                    text = stringResource(R.string.enter_word_to_record),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
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
}
