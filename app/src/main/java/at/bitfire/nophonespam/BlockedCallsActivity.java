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
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.bitfire.nophonespam.model.BlockedCall;
import at.bitfire.nophonespam.model.DbHelper;
import at.bitfire.nophonespam.model.Number;

public class BlockedCallsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<BlockedCall>> {

    static final String EXTRA_NUMBER = "number";
    static final String EXTRA_NAME = "name";

    ListView list;
    ArrayAdapter<BlockedCall> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_calls);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String number = getIntent().getStringExtra(EXTRA_NUMBER);
        setTitle(!TextUtils.isEmpty(name) ? name : Number.wildcardsDbToView(number));

        list = (ListView) findViewById(R.id.calls);
        TextView emptyView = (TextView) findViewById(R.id.empty);
        list.setEmptyView(emptyView);
        list.setAdapter(adapter = new BlockedCallAdapter(this));

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_blocked_calls, menu);
        return true;
    }

    public void onClearHistory(MenuItem item) {
        final String number = getIntent().getStringExtra(EXTRA_NUMBER);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbHelper dbHelper = new DbHelper(BlockedCallsActivity.this);
                try {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(BlockedCall._TABLE, BlockedCall.MATCHED_PATTERN + "=?", new String[]{number});
                } finally {
                    dbHelper.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                getLoaderManager().restartLoader(0, null, BlockedCallsActivity.this);
            }
        }.execute();
    }

    @Override
    public Loader<List<BlockedCall>> onCreateLoader(int i, Bundle bundle) {
        return new BlockedCallLoader(this, getIntent().getStringExtra(EXTRA_NUMBER));
    }

    @Override
    public void onLoadFinished(Loader<List<BlockedCall>> loader, List<BlockedCall> calls) {
        adapter.clear();
        adapter.addAll(calls);
    }

    @Override
    public void onLoaderReset(Loader<List<BlockedCall>> loader) {
        adapter.clear();
    }


    private static class BlockedCallAdapter extends ArrayAdapter<BlockedCall> {

        public BlockedCallAdapter(Context context) {
            super(context, R.layout.blocked_call_item);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = View.inflate(getContext(), R.layout.blocked_call_item, null);

            BlockedCall call = getItem(position);

            TextView tvNumber = (TextView) view.findViewById(R.id.incoming_number);
            tvNumber.setText(!TextUtils.isEmpty(call.incomingNumber)
                    ? call.incomingNumber
                    : getContext().getString(R.string.blocked_calls_private_number));

            TextView tvTime = (TextView) view.findViewById(R.id.blocked_at);
            tvTime.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(call.blockedAt)));

            return view;
        }
    }


    private static class BlockedCallLoader extends AsyncTaskLoader<List<BlockedCall>> {

        final String matchedPattern;

        public BlockedCallLoader(Context context, String matchedPattern) {
            super(context);
            this.matchedPattern = matchedPattern;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public List<BlockedCall> loadInBackground() {
            DbHelper dbHelper = new DbHelper(getContext());
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                List<BlockedCall> calls = new ArrayList<>();
                Cursor c = db.query(BlockedCall._TABLE, null,
                        BlockedCall.MATCHED_PATTERN + "=?",
                        new String[]{matchedPattern},
                        null, null, BlockedCall.BLOCKED_AT + " DESC");
                while (c.moveToNext()) {
                    BlockedCall call = new BlockedCall();
                    call.id = c.getLong(c.getColumnIndex(BlockedCall.ID));
                    call.matchedPattern = c.getString(c.getColumnIndex(BlockedCall.MATCHED_PATTERN));
                    call.incomingNumber = c.getString(c.getColumnIndex(BlockedCall.INCOMING_NUMBER));
                    call.blockedAt = c.getLong(c.getColumnIndex(BlockedCall.BLOCKED_AT));
                    calls.add(call);
                }
                c.close();
                return calls;
            } finally {
                dbHelper.close();
            }
        }
    }

}
