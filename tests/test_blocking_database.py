"""Exercise the SQL used by the Android app with Python's built-in SQLite.

Run with: py -m unittest discover -s tests -v
No Android device or additional Python packages are required.
"""

from pathlib import Path
import re
import sqlite3
import unittest


ROOT = Path(__file__).resolve().parents[1] / "app/src/main/java/at/bitfire/nophonespam"
MODEL = (ROOT / "model/Number.kt").read_text(encoding="utf-8")
HELPER = (ROOT / "model/DbHelper.kt").read_text(encoding="utf-8")
HANDLER = (ROOT / "BlockedCallHandler.kt").read_text(encoding="utf-8")
CONSTANTS = dict(re.findall(r'const val (\w+) = "([^"]*)"', MODEL))


def expand(text):
    text = re.sub(r'\$\{Number\.(\w+)\}', lambda m: CONSTANTS[m[1]], text)
    return text.replace("$now", "1000")


def sql_expression(text):
    return expand("".join(re.findall(r'"([^"]*)"', text)))


class BlockingDatabaseTest(unittest.TestCase):
    def test_country_history_never_matches_in_either_query_path(self):
        # Extract the actual schema and predicates rather than maintaining copies.
        schema = sql_expression(
            HELPER.split("db.execSQL(", 1)[1].split("\n        )", 1)[0]
        )
        active_filter = sql_expression(
            HANDLER.split("private fun activeTimeRangeFilter", 1)[1]
            .split("fun queryAndUpdateDb", 1)[0]
        )
        templates = re.findall(
            r'"(\? LIKE .*?activeTimeRangeFilter\(now\).*?)"', HANDLER
        )
        self.assertEqual(len(templates), 2)
        insert_block = HANDLER.split(
            "val insertValues = ContentValues().apply {", 1
        )[1].split("\n                }", 1)[0]
        history_flag = int(re.search(
            r"put\(Number.IS_BLOCKING_RULE, (\d+)\)", insert_block
        )[1])
        self.assertEqual(history_flag, 0)

        for template in templates:
            with self.subTest(query=template), sqlite3.connect(":memory:") as db:
                selection = expand(template.replace(
                    "${activeTimeRangeFilter(now)}", active_filter
                ))
                db.execute(schema)

                def matches():
                    return bool(db.execute(
                        "SELECT * FROM numbers WHERE " + selection,
                        ("+436641234567",),
                    ).fetchall())

                db.execute(
                    "INSERT INTO numbers(number,is_blocking_rule) VALUES (?,?)",
                    ("+43%", history_flag),
                )
                self.assertFalse(matches(), "Country history must not block")
                db.execute("UPDATE numbers SET timesCalled=timesCalled+1,lastCall=1000")
                self.assertFalse(matches(), "Logging must not enable blocking")
                db.execute("UPDATE numbers SET is_blocking_rule=1")
                self.assertTrue(matches(), "An explicit country rule must block")
                db.execute("UPDATE numbers SET block_from=1100")
                self.assertFalse(matches(), "A future rule must not block")
                db.execute("UPDATE numbers SET block_from=900,block_until=1100")
                self.assertTrue(matches(), "An active temporary rule must block")
                db.execute("UPDATE numbers SET block_until=999")
                self.assertFalse(matches(), "An expired rule must not block")
                db.execute("UPDATE numbers SET block_until=1100,is_blocking_rule=0")
                self.assertFalse(matches(), "History ignores the active time period")

    def test_upgrade_preserves_existing_rules_and_names(self):
        migration = expand(re.search(
            r'if \(from < 4\).*?db.execSQL\("([^"]*)"\)', HELPER, re.S
        )[1])
        with sqlite3.connect(":memory:") as db:
            db.execute("CREATE TABLE numbers(number TEXT PRIMARY KEY, name TEXT)")
            db.execute("INSERT INTO numbers VALUES (?,?)", ("+43%", "My rule"))
            db.execute(migration)
            self.assertEqual(
                db.execute("SELECT name,is_blocking_rule FROM numbers").fetchone(),
                ("My rule", 1),
            )


if __name__ == "__main__":
    unittest.main()
