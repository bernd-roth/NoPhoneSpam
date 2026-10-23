# NoPhoneSpam

NoPhoneSpam is an Android app for blocking unwanted incoming calls. Block individual numbers, number prefixes, entire countries, hidden numbers, or callers who are not in your contacts. Set temporary blocking periods and review blocked-call history.

This README describes the version in this repository: [bernd-roth/NoPhoneSpam](https://github.com/bernd-roth/NoPhoneSpam).

## Features

- Block complete phone numbers or groups of numbers using wildcards.
- Select international dialing codes from a country list.
- Display country flags and country names for entries without custom names.
- Keep automatically grouped country history separate from blocking rules.
- Block hidden/private numbers.
- Block callers who are not in your contacts, while allowing saved contacts in that mode.
- Pause all blocking without deleting rules.
- Limit individual rules to a start and end date/time.
- Automatically remove expired temporary rules and restore expiry scheduling after reboot.
- Show optional notifications for rejected calls.
- View call counts, the last blocked-call time, and individual calls for each rule.
- Edit or delete rules and clear their detailed history.

The interface includes English, German, Spanish, and Japanese translations. Country names currently come from the app's English country list.

## Installation and first launch

Install an APK built from this repository, or build it yourself using the instructions below. If Android asks, allow installation from the app you use to open the APK.

1. Open **NoPhoneSpam**.
2. Allow notifications if you want alerts about rejected calls.
3. Allow contacts access if you want to use **Block numbers not in contacts**.
4. Accept Android's request to make NoPhoneSpam your call-screening app.
5. Open the main screen's three-dot menu and check that **Blocking enabled** is selected.
6. Add a rule with **+**, or enable one of the general blocking options.

On Android 10 and later, the app requests the system call-screening role. If you need to change this later, look in Android's default-app settings for the caller ID/spam or call-screening app. The wording depends on the device.

The project allows installation on Android 7.0 and later, but its current setup flow requests the call-screening role only on Android 10 and later. Blocking on older versions is not guaranteed by this implementation.

## Main screen and controls

The main screen lists your blocking rules and country history entries, ordered by number/pattern. An entry shows its custom name or detected country name, its number/pattern and country flag when recognized, and blocking statistics when available. Entries marked **History only · does not block calls** group past calls without blocking future calls.

| Control | Action |
| --- | --- |
| **+** | Add a blocking rule. |
| Tap an entry | Open its blocked-call history. |
| Press and hold an entry | Edit the rule. |
| Three-dot menu | Change blocking/notification settings or open the project page with **About NoPhoneSpam**. |

Country labels are inferred from dialing prefixes. They do not verify a caller's physical location. Some countries share dialing codes, so a country label can be ambiguous.

## Add a blocking rule

1. Tap **+**.
2. Optionally enter a **Name**, such as `Unwanted sales calls`.
3. Choose a **Country code**, or choose **None** to enter the full number yourself.
4. Enter the number or wildcard pattern in **Number**.
5. Leave **Block calls matching this entry** on to enable the rule. Optionally enable **Block only during time period** and choose the dates and times.
6. Tap the checkmark to save.

When selecting a country, enter only the part after its dialing code. For example, select **Austria (+43)** and enter `6641234567` to create `+436641234567`. If **None** is selected, enter the full international number, including `+43`.

Use the international format delivered with incoming calls for reliable matching. Spaces, hyphens, and parentheses entered in the editor are removed when saving. The editor does not automatically convert a national leading `0` or an international `00` prefix into `+` format.

If **Name** is empty and the prefix is recognized, the country name becomes the display label. Custom names take priority. The same label is used for the history title and notifications.

### Wildcards and examples

| Character | Meaning |
| --- | --- |
| `*` | Matches zero or more digits. |
| `#` | Matches exactly one digit. |

These examples show the complete pattern after combining the country code and number fields:

| Pattern | Blocks |
| --- | --- |
| `+436641234567` | That exact number. |
| `+43664*` | Numbers starting with `+43664`. |
| `+43*` | Numbers starting with Austria's `+43` dialing code. |
| `+49691234##` | Numbers starting with `+49691234` followed by exactly two digits. |
| `*` | Every non-hidden number passed to the app for screening, subject to the contacts exception below. |

To create a country-wide rule, select the country and enter `*` in **Number**. Selecting a country alone does not add the wildcard.

## General blocking options

Open the three-dot menu on the main screen. A checkmark indicates an enabled option.

| Option | Default | Effect |
| --- | --- | --- |
| **Blocking enabled** | On | Enables the app's blocking decisions. Turn it off to pause blocking without deleting rules or history. |
| **Block hidden numbers** | Off | Rejects calls without a visible number. |
| **Block numbers not in contacts** | Off | Rejects visible numbers that are not in your contacts. Saved contacts bypass number and country rules while this option is on. |
| **Show notifications** | On | Shows notifications for rejected calls, provided Android also allows them. |

### How the options work together

1. When **Blocking enabled** is off, the app allows calls through.
2. Hidden calls follow **Block hidden numbers**. The non-contacts option does not control hidden calls.
3. With **Block numbers not in contacts** on, saved contacts are allowed even if their number matches a rule.
4. Other visible calls are rejected if they match an active rule or if the non-contacts option is on.

When the non-contacts option is off, saved contacts can also be blocked by matching rules.

### Country entries created by non-contact calls

If a rejected non-contact call does not match an active rule and its country prefix is recognized, the app creates or updates a country entry, such as `+43*`, and records the call under it. New automatic entries are marked **History only · does not block calls**. Logging calls does not enable blocking or change an existing rule's settings.

Turning off **Block numbers not in contacts** keeps that history visible without blocking those countries. To deliberately block a country, press and hold its entry, enable **Block calls matching this entry**, and save.

**Upgrading from an older version:** earlier releases created active country rules when logging non-contact calls. Those versions did not store whether a rule was created automatically or manually, so existing rules are preserved during upgrade. Review unwanted country entries, turn **Block calls matching this entry** off, and save to keep their history without blocking calls. You can also delete an entry and its history.

## Temporary blocking

1. Add a rule or press and hold an existing entry.
2. Enable **Block only during time period**.
3. Set the **Start** date and time.
4. Set the **End** date and time later than the start.
5. Save with the checkmark.

The rule applies only during that interval. Dates and times use the device's local time zone. This is a single interval, not a recurring daily or weekly schedule.

After the end time, the app schedules removal of the rule from the main list. If Android delays the cleanup alarm, the rule's time check still prevents it from matching after expiry. After reboot, the app removes expired rules and reschedules pending expiry alarms.

Turn the time-period switch off and save to make a rule permanent again.

Other blocking options still apply outside a rule's time period. In particular, **Block numbers not in contacts** can continue rejecting a caller and recording country history independently of the temporary rule.

## Edit, cancel, or delete a rule

Press and hold an entry to open its editor.

- Change the name, country, number/pattern, or time period, then tap the checkmark to save.
- Turn **Block calls matching this entry** off to keep an entry for history without using it as a blocking rule. Saving with this switch off also clears its temporary blocking dates and cancels its expiry alarm.
- **Android Back also saves** an existing rule or a new entry containing data. Back from an untouched, empty new entry cancels it.
- To discard edits, explicitly choose **Cancel** from the editor's three-dot menu.
- To remove an existing rule, choose **Delete** from that menu. Deletion also removes the detailed history associated with that pattern.

Each number/pattern must be unique. Edit an existing entry instead of adding the same pattern again.

## Blocked-call history and statistics

Tap an entry to see individual calls recorded against its pattern. The history shows the incoming number and rejection date/time, with the newest calls first.

The main entry shows its cumulative blocked-call count and last recorded call time. If rules overlap, a call is recorded against the matched rule selected by the app; it is not counted under every matching rule.

In the history screen, choose **Clear history** from the three-dot menu to delete that rule's detailed records. This keeps the rule and its cumulative count/last-call statistics.

History is associated with the number/pattern. Changing a rule's pattern does not move its earlier detailed records to the new pattern. Automatic expiry removes the rule from the list; it does not perform the same history deletion as the editor's **Delete** action.

Hidden calls and calls whose country prefix cannot be recognized do not get a country entry. The current interface has no separate all-calls history screen for those calls.

## Notifications

With **Show notifications** enabled, a rejected call can produce a **Call rejected** notification containing the rule's display name, its pattern, or a private-number label when appropriate. Tapping a notification opens the app.

The app uses Android's **Blocked calls** notification channel. Android's notification permission and channel settings control whether alerts appear and how they sound. Notifications for the same rule can replace an earlier notification; use the rule's history to review individual calls.

## Permissions and data

| Permission or system access | Purpose |
| --- | --- |
| Call-screening role | Lets Android deliver calls to the app for an allow/reject decision. |
| Contacts | Checks whether a caller is saved when non-contact blocking is enabled. |
| Notifications | Displays optional blocked-call alerts. |
| Alarms | Schedules removal of temporary rules; falls back to an inexact alarm when exact alarms are unavailable. |
| Receive boot completed | Cleans up expired rules and restores expiry scheduling after restarting the device. |

Rules and history are stored in a local SQLite database; settings are stored locally as well. The app does not declare Internet permission or provide a cloud spam lookup. **About NoPhoneSpam** opens the project page in your browser.

Android backup is enabled in the manifest, so app data may be included in the device's system backup according to its settings. There is currently no in-app import, export, or backup control.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Calls are not blocked | Check **Blocking enabled**, the selected system call-screening app, the full international pattern, and any start/end dates. |
| A saved contact gets through a country rule | This is expected while **Block numbers not in contacts** is enabled. Saved contacts bypass number rules in that mode. |
| Hidden calls still get through | Enable **Block hidden numbers** separately. |
| Calls are still blocked after disabling non-contact blocking | Check for explicit rules and country rules retained from older versions. Turn **Block calls matching this entry** off for unwanted rules, or delete them. |
| A rejected call has no notification | Enable **Show notifications** and check Android's notification permission and **Blocked calls** channel. |
| Non-contact blocking behaves unexpectedly | Check the app's contacts permission in Android settings. |
| An expired rule is still visible | Android may delay its cleanup alarm. Its date restriction still prevents that rule from matching after expiry. |
| Edits were saved when leaving the editor | Back saves changes. Choose **Cancel** from the editor menu to discard them. |

Call screening depends on the calls Android delivers to the app. NoPhoneSpam handles telephone calls delivered through Android's call-screening service; it does not provide blocking for messaging-app calls or SMS. Test your rules on your device, including Wi-Fi calling if you use it.

## Build and development

The project uses Kotlin, Jetpack Compose, Material 3, and Android's call-screening service. Its application ID is `at.bitfire.nophonespam`.

Requirements are an Android SDK with platform 36 and a Java setup compatible with the Gradle wrapper and Android Gradle Plugin configured in this repository. Kotlin compilation uses a Java 17 toolchain. Set your SDK location through Android Studio or an untracked `local.properties` file.

From the project root on Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Database regression checks can also run with Python 3 and its built-in SQLite module:

```powershell
py -m unittest discover -s tests -v
```

On Linux or macOS, use `./gradlew` in place of `.\gradlew.bat`.

The debug APK is written to:

```text
app/build/outputs/apk/debug/nophonespam.apk
```

To install on a connected device with USB debugging enabled:

```powershell
.\gradlew.bat :app:installDebug
```

After installation, open the app and complete the permission and call-screening setup. For changes to blocking behavior, verify relevant rules on a device in addition to running unit tests.

## Credits and license

Based on the original [NoPhoneSpam project by bitfire](https://gitlab.com/bitfireAT/NoPhoneSpam/).

Original copyright (C) bitfire web engineering (Ricki Hirner, Bernhard Stockmann).

Licensed under the [GNU General Public License, version 3](LICENSE). This program comes with absolutely no warranty; see the license for details.

