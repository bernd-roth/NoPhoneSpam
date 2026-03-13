package at.bitfire.nophonespam.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.bitfire.nophonespam.CountryCode
import at.bitfire.nophonespam.R
import at.bitfire.nophonespam.model.Number
import at.bitfire.nophonespam.viewmodel.EditNumberViewModel
import java.text.DateFormat
import java.util.Calendar
import java.util.TimeZone

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

    // Time-period blocking state
    var blockEnabled by remember { mutableStateOf(false) }
    var blockFromDateMs by remember { mutableStateOf<Long?>(null) }
    var blockFromHour by remember { mutableIntStateOf(8) }
    var blockFromMinute by remember { mutableIntStateOf(0) }
    var blockUntilDateMs by remember { mutableStateOf<Long?>(null) }
    var blockUntilHour by remember { mutableIntStateOf(18) }
    var blockUntilMinute by remember { mutableIntStateOf(0) }

    // Picker visibility flags
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

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

        if (n.blockFrom != null || n.blockUntil != null) {
            blockEnabled = true
            n.blockFrom?.let { ms ->
                val cal = Calendar.getInstance().apply { timeInMillis = ms }
                blockFromHour = cal.get(Calendar.HOUR_OF_DAY)
                blockFromMinute = cal.get(Calendar.MINUTE)
                blockFromDateMs = toUtcMidnight(cal)
            }
            n.blockUntil?.let { ms ->
                val cal = Calendar.getInstance().apply { timeInMillis = ms }
                blockUntilHour = cal.get(Calendar.HOUR_OF_DAY)
                blockUntilMinute = cal.get(Calendar.MINUTE)
                blockUntilDateMs = toUtcMidnight(cal)
            }
        }
    }

    fun buildCombinedNumber(): String {
        return if (selectedCountryIndex > 0) {
            "+${CountryCode.COUNTRIES[selectedCountryIndex].dialCode}$localNumber"
        } else {
            localNumber
        }
    }

    fun computeBlockFrom(): Long? {
        if (!blockEnabled) return null
        return combineDateAndTime(blockFromDateMs, blockFromHour, blockFromMinute)
    }

    fun computeBlockUntil(): Long? {
        if (!blockEnabled) return null
        return combineDateAndTime(blockUntilDateMs, blockUntilHour, blockUntilMinute)
    }

    fun doSave() {
        val combinedNumber = buildCombinedNumber()
        val numberDb = Number.wildcardsViewToDb(combinedNumber)
        vm.save(existingPattern, name, numberDb, computeBlockFrom(), computeBlockUntil(), onBack)
    }

    fun doCancel() {
        onBack()
    }

    // Back press handling: save if editing or has data, cancel if adding and empty
    BackHandler {
        val isEmpty = existingPattern == null &&
                name.isBlank() &&
                localNumber.isBlank() &&
                selectedCountryIndex == 0 &&
                !blockEnabled
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Time-period blocking section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.edit_block_time_period),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = blockEnabled,
                    onCheckedChange = { enabled ->
                        blockEnabled = enabled
                        if (enabled) {
                            // Initialize with today/tomorrow defaults if not already set
                            if (blockFromDateMs == null) {
                                blockFromDateMs = todayUtcMidnight()
                            }
                            if (blockUntilDateMs == null) {
                                blockUntilDateMs = tomorrowUtcMidnight()
                            }
                        }
                    }
                )
            }

            if (blockEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_block_start),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { showStartDatePicker = true }) {
                        Text(formatUtcDateMs(blockFromDateMs) ?: stringResource(R.string.edit_select_date))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { showStartTimePicker = true }) {
                        Text(formatTime(blockFromHour, blockFromMinute))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_block_end),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { showEndDatePicker = true }) {
                        Text(formatUtcDateMs(blockUntilDateMs) ?: stringResource(R.string.edit_select_date))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { showEndTimePicker = true }) {
                        Text(formatTime(blockUntilHour, blockUntilMinute))
                    }
                }
            }
        }
    }

    // Date/time picker dialogs
    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = blockFromDateMs ?: todayUtcMidnight())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    blockFromDateMs = state.selectedDateMillis
                    showStartDatePicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) { DatePicker(state = state) }
    }

    if (showStartTimePicker) {
        val state = rememberTimePickerState(initialHour = blockFromHour, initialMinute = blockFromMinute)
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                blockFromHour = state.hour
                blockFromMinute = state.minute
                showStartTimePicker = false
            }
        ) { TimePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = blockUntilDateMs ?: tomorrowUtcMidnight())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    blockUntilDateMs = state.selectedDateMillis
                    showEndDatePicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) { DatePicker(state = state) }
    }

    if (showEndTimePicker) {
        val state = rememberTimePickerState(initialHour = blockUntilHour, initialMinute = blockUntilMinute)
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                blockUntilHour = state.hour
                blockUntilMinute = state.minute
                showEndTimePicker = false
            }
        ) { TimePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(android.R.string.ok)) }
        },
        text = { content() }
    )
}

/** Returns UTC midnight millis for the local calendar date in [cal]. */
private fun toUtcMidnight(cal: Calendar): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return utc.timeInMillis
}

private fun todayUtcMidnight(): Long = toUtcMidnight(Calendar.getInstance())

private fun tomorrowUtcMidnight(): Long {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
    return toUtcMidnight(cal)
}

/**
 * Combines a UTC-midnight date (as returned by DatePicker) with a local hour/minute
 * to produce a single epoch-millis timestamp in the device's local timezone.
 */
private fun combineDateAndTime(utcMidnightMs: Long?, hour: Int, minute: Int): Long? {
    utcMidnightMs ?: return null
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMs }
    return Calendar.getInstance().apply {
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH),
            hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/** Formats a UTC-midnight millis value as a local short date string, or null if ms is null. */
private fun formatUtcDateMs(utcMidnightMs: Long?): String? {
    utcMidnightMs ?: return null
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMs }
    val local = Calendar.getInstance().apply {
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
    }
    return DateFormat.getDateInstance(DateFormat.SHORT).format(local.time)
}

private fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time)
}
