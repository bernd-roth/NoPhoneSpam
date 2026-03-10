package at.bitfire.nophonespam.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.nophonespam.CountryCode
import at.bitfire.nophonespam.R
import at.bitfire.nophonespam.model.Number
import at.bitfire.nophonespam.viewmodel.EditNumberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumberScreen(
    existingPattern: String?,
    onBack: () -> Unit
) {
    val vm: EditNumberViewModel = viewModel()
    val loadedNumber by vm.number.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var selectedCountryIndex by remember { mutableIntStateOf(0) }
    var localNumber by remember { mutableStateOf("") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    // Load existing number when editing
    if (existingPattern != null) {
        LaunchedEffect(existingPattern) {
            vm.load(existingPattern)
        }
    }

    // Populate fields when the number is loaded
    LaunchedEffect(loadedNumber) {
        val n = loadedNumber ?: return@LaunchedEffect
        name = n.name ?: ""
        val viewNumber = Number.wildcardsDbToView(n.number)
        val countryIndex = CountryCode.findByDialCode(viewNumber)
        selectedCountryIndex = countryIndex
        localNumber = CountryCode.stripDialCode(viewNumber, countryIndex)
    }

    fun buildCombinedNumber(): String {
        return if (selectedCountryIndex > 0) {
            "+${CountryCode.COUNTRIES[selectedCountryIndex].dialCode}$localNumber"
        } else {
            localNumber
        }
    }

    fun doSave() {
        val combinedNumber = buildCombinedNumber()
        val numberDb = Number.wildcardsViewToDb(combinedNumber)
        vm.save(existingPattern, name, numberDb, onBack)
    }

    fun doCancel() {
        onBack()
    }

    // Back press handling: save if editing or has data, cancel if adding and empty
    BackHandler {
        val isEmpty = existingPattern == null &&
                name.isBlank() &&
                localNumber.isBlank() &&
                selectedCountryIndex == 0
        if (isEmpty) {
            doCancel()
        } else {
            doSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (existingPattern != null) R.string.edit_edit_number
                            else R.string.edit_add_number
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { doSave() }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.edit_save))
                    }
                    IconButton(onClick = { overflowMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = overflowMenuExpanded,
                        onDismissRequest = { overflowMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_cancel)) },
                            onClick = {
                                overflowMenuExpanded = false
                                doCancel()
                            }
                        )
                        if (existingPattern != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_delete)) },
                                onClick = {
                                    overflowMenuExpanded = false
                                    vm.delete(existingPattern, onBack)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.edit_name)) },
                placeholder = { Text(stringResource(R.string.edit_name_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = countryDropdownExpanded,
                onExpandedChange = { countryDropdownExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = CountryCode.COUNTRIES[selectedCountryIndex].toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.edit_country_code)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = countryDropdownExpanded,
                    onDismissRequest = { countryDropdownExpanded = false }
                ) {
                    CountryCode.COUNTRIES.forEachIndexed { index, country ->
                        DropdownMenuItem(
                            text = { Text(country.toString()) },
                            onClick = {
                                selectedCountryIndex = index
                                countryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = localNumber,
                onValueChange = { localNumber = it },
                label = { Text(stringResource(R.string.edit_number)) },
                placeholder = { Text(stringResource(R.string.edit_number_hint)) },
                singleLine = true,
                supportingText = { Text(stringResource(R.string.edit_number_hints)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}
