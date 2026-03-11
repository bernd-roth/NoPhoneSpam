package at.bitfire.nophonespam.ui

import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.nophonespam.CountryCode
import at.bitfire.nophonespam.R
import at.bitfire.nophonespam.model.Number
import at.bitfire.nophonespam.viewmodel.BlacklistViewModel
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(
    onShortPress: (Number) -> Unit,
    onLongPress: (Number) -> Unit,
    onAddClick: () -> Unit
) {
    val vm: BlacklistViewModel = viewModel()
    val numbers by vm.numbers.collectAsStateWithLifecycle()
    val blockHiddenNumbers by vm.blockHiddenNumbers.collectAsStateWithLifecycle()
    val showNotifications by vm.showNotifications.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val blockNonContacts by vm.blockNonContacts.collectAsStateWithLifecycle()
    val blockHiddenLabel = stringResource(R.string.blacklist_block_hidden_numbers)
    val showNotificationsLabel = stringResource(R.string.blacklist_show_notifications)
    val blockNonContactsLabel = stringResource(R.string.blacklist_block_non_contacts)

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (blockHiddenNumbers) "\u2713 $blockHiddenLabel" else blockHiddenLabel)
                            },
                            onClick = {
                                vm.toggleBlockHiddenNumbers()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (showNotifications) "\u2713 $showNotificationsLabel" else showNotificationsLabel)
                            },
                            onClick = {
                                vm.toggleShowNotifications()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (blockNonContacts) "\u2713 $blockNonContactsLabel" else blockNonContactsLabel)
                            },
                            onClick = {
                                vm.toggleBlockNonContacts()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.blacklist_about)) },
                            onClick = {
                                menuExpanded = false
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, "https://gitlab.com/bitfireAT/NoPhoneSpam/".toUri())
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.edit_add_number))
            }
        }
    ) { innerPadding ->
        if (numbers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.blacklist_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(numbers, key = { it.number }) { number ->
                    NumberItem(
                        number = number,
                        onShortPress = { onShortPress(number) },
                        onLongPress = { onLongPress(number) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberItem(
    number: Number,
    onShortPress: () -> Unit,
    onLongPress: () -> Unit
) {
    val viewNumber = Number.wildcardsDbToView(number.number)
    val countryIndex = CountryCode.findByDialCode(viewNumber)
    val displayNumber = if (countryIndex > 0) {
        "${CountryCode.COUNTRIES[countryIndex].flag} $viewNumber"
    } else {
        viewNumber
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .combinedClickable(
                onClick = onShortPress,
                onLongClick = onLongPress
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!number.name.isNullOrBlank()) {
                Text(
                    text = number.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = displayNumber,
                style = MaterialTheme.typography.bodyMedium
            )
            if (number.lastCall != null) {
                val formattedDate = SimpleDateFormat.getDateTimeInstance().format(Date(number.lastCall))
                val statsText = pluralStringResource(
                    R.plurals.blacklist_call_details,
                    number.timesCalled,
                    number.timesCalled,
                    formattedDate
                )
                Text(
                    text = statsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
