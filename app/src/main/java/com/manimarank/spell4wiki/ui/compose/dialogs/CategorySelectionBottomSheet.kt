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
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.CategoryItem
import com.manimarank.spell4wiki.data.model.WikiCategoryListItemResponse
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Compose implementation of Category Selection Bottom Sheet
 * Replaces CategorySelectionFragment with Material Design 3 ModalBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    listMode: Int,
    preSelectedLanguageCode: String? = null,
    subTitleInfo: String? = null,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pref = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Function to fetch categories
    fun fetchCategories(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            errorMessage = "Not valid search term"
            return
        }
        
        if (!isConnected(context)) {
            errorMessage = context.getString(R.string.check_internet)
            return
        }
        
        isLoading = true
        errorMessage = null
        
        val api = ApiClient.getWiktionaryApi(
            context, 
            pref.languageCodeSpell4WikiAll ?: AppConstants.DEFAULT_LANGUAGE_CODE
        ).create(ApiInterface::class.java)
        
        val call = api.fetchCategoryList(searchTerm, 100, null)
        
        call.enqueue(object : Callback<WikiCategoryListItemResponse?> {
            override fun onResponse(call: Call<WikiCategoryListItemResponse?>, response: Response<WikiCategoryListItemResponse?>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    val resList = response.body()?.query?.allpages ?: emptyList()
                    categoryList = resList
                    if (categoryList.isEmpty()) {
                        errorMessage = context.getString(R.string.no_categories_found)
                    }
                } else {
                    errorMessage = "Failed to load categories"
                }
            }

            override fun onFailure(call: Call<WikiCategoryListItemResponse?>, t: Throwable) {
                isLoading = false
                errorMessage = t.message ?: "Network error"
            }
        })
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
                text = stringResource(R.string.select_category),
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
                onValueChange = { 
                    searchQuery = it
                    if (it.length >= 3) {
                        scope.launch {
                            fetchCategories(it)
                        }
                    }
                },
                label = { Text(stringResource(R.string.search_categories)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.type_to_search_categories)) }
            )
            
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.loading_categories),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                    
                    categoryList.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(categoryList) { category ->
                                CategoryItem(
                                    category = category,
                                    onClick = {
                                        onCategorySelected(category.title)
                                    }
                                )
                            }
                        }
                    }
                    
                    searchQuery.length < 3 -> {
                        Text(
                            text = stringResource(R.string.type_at_least_3_characters),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                    
                    else -> {
                        Text(
                            text = stringResource(R.string.no_categories_found),
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
private fun CategoryItem(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = false,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = category.title ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
