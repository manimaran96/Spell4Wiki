package com.manimarank.spell4wiki.ui.compose.main

import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.compose.common.OptionCard
import com.manimarank.spell4wiki.ui.compose.common.CommonSpacing
import com.manimarank.spell4wiki.ui.compose.common.CommonTextStyles

@Composable
fun HeaderSection(
    userName: String,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.ic_spell4wiki_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        // App Name
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        // Welcome User Text
        if (!isAnonymous && userName.isNotEmpty()) {
            Text(
                text = stringResource(R.string.welcome_user, userName),
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.wiktionary_search)) },
        placeholder = { Text(stringResource(R.string.wiktionary_search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchSubmit(query) }
        ),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun MainOptionsSection(
    onNavigateToSpell4Wiki: () -> Unit,
    onNavigateToSpell4WordList: () -> Unit,
    onNavigateToSpell4Word: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Spell4Wiki Card
        OptionCard(
            title = stringResource(R.string.spell4wiktionary),
            iconRes = R.drawable.ic_spell4wiktionary,
            onClick = onNavigateToSpell4Wiki,
            modifier = Modifier.weight(1f)
        )

        // Spell4WordList Card
        OptionCard(
            title = stringResource(R.string.spell4wordlist),
            iconRes = R.drawable.ic_spell4word_list,
            onClick = onNavigateToSpell4WordList,
            modifier = Modifier.weight(1f)
        )

        // Spell4Word Card
        OptionCard(
            title = stringResource(R.string.spell4word),
            iconRes = R.drawable.ic_spell4word,
            onClick = onNavigateToSpell4Word,
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
fun UserActionsSection(
    isAnonymous: Boolean,
    onViewContribution: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAnonymous) {
            // Login Section for Anonymous Users
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.contribute_to_wiki_commons),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedButton(
                    onClick = onLogin,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_to_contribute),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // View Contribution Button for Logged-in Users
            OutlinedButton(
                onClick = onViewContribution,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.view_my_contribution),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomNavigationSection(
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // About Button
        NavigationButton(
            text = stringResource(R.string.about),
            icon = Icons.Default.Info,
            onClick = onNavigateToAbout
        )

        // Settings Button
        NavigationButton(
            text = stringResource(R.string.settings),
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )

        // Logout Button (only for logged-in users)
        if (!isAnonymous) {
            NavigationButton(
                text = stringResource(R.string.logout),
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = onLogout
            )
        }
    }
}

@Composable
fun NavigationButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun JoinTelegramSection(
    onJoinTelegram: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onJoinTelegram,
            shape = RoundedCornerShape(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_telegram),
                contentDescription = "Telegram",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            
            Text(
                text = stringResource(R.string.join_spell4wiki_support),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
