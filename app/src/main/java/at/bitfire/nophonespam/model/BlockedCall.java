/*
 * Copyright © Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.nophonespam.model;

public class BlockedCall {

    public static final String
            _TABLE = "blocked_calls",
            ID = "id",
            MATCHED_PATTERN = "matched_pattern",
            INCOMING_NUMBER = "incoming_number",
            BLOCKED_AT = "blocked_at";

    public long id;
    public String matchedPattern;
    public String incomingNumber;
    public long blockedAt;

}
