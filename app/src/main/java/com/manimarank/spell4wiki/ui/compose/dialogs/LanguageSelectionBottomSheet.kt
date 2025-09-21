package com.manimarank.spell4wiki.ui.compose.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.constants.ListMode

/**
 * Compose implementation of Language Selection Bottom Sheet
 * Replaces LanguageSelectionFragment with Material Design 3 ModalBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    listMode: Int,
    preSelectedLanguageCode: String? = null,
    onLanguageSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pref = remember { PrefManager(context) }
    val dbHelper = remember { DBHelper.getInstance(context) }
    
    var searchQuery by remember { mutableStateOf("") }
    var languageList by remember { mutableStateOf<List<WikiLang>>(emptyList()) }
    
    // Load languages on first composition
    LaunchedEffect(Unit) {
        val allLanguages = dbHelper.appDatabase.wikiLangDao?.wikiLanguageList?.filterNotNull() ?: emptyList()
        languageList = allLanguages
    }
    
    // Filter languages based on search query
    val filteredLanguages = remember(languageList, searchQuery) {
        if (searchQuery.isEmpty()) {
            languageList
        } else {
            languageList.filter { lang ->
                lang.name?.contains(searchQuery, ignoreCase = true) == true ||
                lang.localName?.contains(searchQuery, ignoreCase = true) == true ||
                lang.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val existingLanguageCode = when (listMode) {
        ListMode.SPELL_4_WIKI_ALL -> pref.languageCodeSpell4WikiAll
        ListMode.TEMP -> null
        else -> null
    }
    
    val subTitleInfo = when (listMode) {
        ListMode.SPELL_4_WIKI_ALL -> stringResource(R.string.spell_4_wiki_all)
        ListMode.TEMP -> stringResource(R.string.temporary)
        else -> null
    }?.let { info ->
        stringResource(R.string.language_for_note, info)
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtitle if available
            subTitleInfo?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            
            // Language list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLanguages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language.code == (preSelectedLanguageCode ?: existingLanguageCode),
                        onClick = {
                            when (listMode) {
                                ListMode.TEMP -> {}
                                else -> pref.languageCodeSpell4WikiAll = language.code
                            }
                            onLanguageSelected(language.code)
                        }
                    )
                }
                
                if (filteredLanguages.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_languages_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
            }
            
            // Bottom padding for gesture navigation
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageItem(
    language: WikiLang,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = language.name ?: language.code,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                language.localName?.let { localName ->
                    if (localName != language.name) {
                        Text(
                            text = localName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                Text(
                    text = language.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Search, // You might want to use a checkmark icon
                    contentDescription = stringResource(R.string.selected),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
