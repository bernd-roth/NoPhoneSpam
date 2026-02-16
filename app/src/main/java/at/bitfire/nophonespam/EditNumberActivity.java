/*
 * Copyright © Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.nophonespam;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import at.bitfire.nophonespam.model.DbHelper;
import at.bitfire.nophonespam.model.Number;

public class EditNumberActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Number> {

    static final String EXTRA_NUMBER = "number";

    TextView tvName, tvNumber;
    Spinner spinnerCountryCode;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String intentNumber = getIntentNumber();
        setTitle(intentNumber == null ? R.string.edit_add_number : R.string.edit_edit_number);
        setContentView(R.layout.activity_edit_number);

        tvName = (TextView)findViewById(R.id.name);
        tvNumber = (TextView)findViewById(R.id.number);
        spinnerCountryCode = (Spinner)findViewById(R.id.country_code);

        spinnerCountryCode.setAdapter(new CountryCode.CountryCodeAdapter(this));

        if (intentNumber != null)
            getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_number, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private String getCombinedNumber() {
        int selectedIndex = spinnerCountryCode.getSelectedItemPosition();
        String localNumber = tvNumber.getText().toString();
        if (selectedIndex > 0) {
            String dialCode = CountryCode.COUNTRIES[selectedIndex].dialCode;
            return "+" + dialCode + localNumber;
        }
        return localNumber;
    }

    public void onSave(MenuItem item) {
        if (validate()) {
            String combinedNumber = getCombinedNumber();

            ContentValues values = new ContentValues(2);
            values.put(Number.NAME, tvName.getText().toString());
            values.put(Number.NUMBER, Number.wildcardsViewToDb(combinedNumber));

            DbHelper dbHelper = new DbHelper(this);
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (getIntentNumber() != null)
                    db.update(Number._TABLE, values, Number.NUMBER + "=?", new String[] { getIntentNumber() });
                else
                    db.insert(Number._TABLE, null, values);

                Toast.makeText(this, R.string.edit_changes_saved, Toast.LENGTH_SHORT).show();

                finish();
            } finally {
                dbHelper.close();
            }
        }
    }

    public void onCancel(MenuItem item) {
        finish();
    }

    @Override
    public void onBackPressed() {
        boolean hasCountryCode = spinnerCountryCode.getSelectedItemPosition() > 0;
        if (getIntentNumber() == null && TextUtils.isEmpty(tvName.getText()) && TextUtils.isEmpty(tvNumber.getText()) && !hasCountryCode)
            onCancel(null);
        else
            onSave(null);
    }


    protected String getIntentNumber() {
        return getIntent().getStringExtra(EXTRA_NUMBER);
    }

    protected boolean validate() {
        boolean ok = true;
        if (TextUtils.isEmpty(tvName.getText())) {
            tvName.setError(getString(R.string.edit_must_not_be_empty));
            ok = false;
        }
        boolean hasCountryCode = spinnerCountryCode.getSelectedItemPosition() > 0;
        if (TextUtils.isEmpty(tvNumber.getText()) && !hasCountryCode) {
            tvNumber.setError(getString(R.string.edit_must_not_be_empty));
            ok = false;
        }
        return ok;
    }


    @Override
    public Loader<Number> onCreateLoader(int i, Bundle bundle) {
        return new NumberLoader(this, getIntentNumber());
    }

    @Override
    public void onLoadFinished(Loader<Number> loader, Number number) {
        if (number != null) {
            tvName.setText(number.name);
            String viewNumber = Number.wildcardsDbToView(number.number);
            int countryIndex = CountryCode.findByDialCode(viewNumber);
            spinnerCountryCode.setSelection(countryIndex);
            String localPart = CountryCode.stripDialCode(viewNumber, countryIndex);
            tvNumber.setText(localPart);
        }
    }

    @Override
    public void onLoaderReset(Loader<Number> loader) {
    }


    protected static class NumberLoader extends AsyncTaskLoader<Number> {

        final String number;

        public NumberLoader(Context context, String number) {
            super(context);
            this.number = number;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Number loadInBackground() {
            DbHelper dbHelper = new DbHelper(getContext());
            Cursor c = null;
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                c = db.query(Number._TABLE, null, Number.NUMBER + "=?", new String[] { number }, null, null, null);
                if (c.moveToNext()) {
                    ContentValues values = new ContentValues(c.getColumnCount());
                    DatabaseUtils.cursorRowToContentValues(c, values);
                    return Number.fromValues(values);
                }
            } finally {
                if (c != null)
                    c.close();
                dbHelper.close();
            }
            return null;
        }

    }

}
