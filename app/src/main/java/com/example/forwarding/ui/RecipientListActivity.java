/*
 * This file is part of Android SMS Auto Forwarding Service.
 *
 * Original project licensed under GNU GPL v2
 *
 * Modifications:
 * - Automated background execution
 * - Keyword and sender-based filtering
 * - Blacklist support
 *
 * Modified by < CoderShivamGusain >, 2026
 */

package com.example.forwarding.ui;

import com.example.forwarding.R;
import com.example.forwarding.data_model.Preferences;
import com.example.forwarding.data_model.RecipientListItem;
import com.example.forwarding.security_model.RuntimePermissions;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecipientListActivity extends Activity {

    private CheckBox inputEnable;
    private ListView listView;

    private ArrayList<RecipientListItem> listItems;
    private ArrayAdapter<RecipientListItem> listAdapter;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔑 Request runtime permissions FIRST
        if (!RuntimePermissions.isEnabled(this)) {
            // Wait for user response, activity will be recreated
            return;
        }

        setContentView(R.layout.activity_recipient_list);

        inputEnable = findViewById(R.id.input_enable);
        listView = findViewById(R.id.listview);

        listItems = Preferences.getRecipientListItems(this);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(
                (AdapterView<?> parent, View view, int position, long id) ->
                        showEditDialog(position)
        );

        // Forwarding is ALWAYS enabled
        inputEnable.setChecked(true);
        inputEnable.setEnabled(false);
        inputEnable.setClickable(false);
    }

    // ---------------------------------------------------------------------------------------------
    // Runtime permissions callback
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        RuntimePermissions.onRequestPermissionsResult(
                this,
                requestCode,
                permissions,
                grantResults
        );
    }

    // ---------------------------------------------------------------------------------------------
    // ActionBar
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }
        getMenuInflater().inflate(R.menu.activity_recipient_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_add) {
            showEditDialog(-1);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    // ---------------------------------------------------------------------------------------------
    // Add / Edit Dialog
    // ---------------------------------------------------------------------------------------------

    private void showEditDialog(final int position) {

        final boolean isAdd = (position < 0);
        final RecipientListItem listItem =
                isAdd ? new RecipientListItem() : listItems.get(position);

        final Dialog dialog = new Dialog(this, R.style.app_theme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_recipient_listitem);

        final EditText inputRecipient = dialog.findViewById(R.id.input_recipient);
        final EditText inputSender = dialog.findViewById(R.id.input_sender);
        final EditText inputKeywords = dialog.findViewById(R.id.input_keywords);
        final EditText inputBlacklist = dialog.findViewById(R.id.input_blacklist);

        final Button buttonDelete = dialog.findViewById(R.id.button_delete);
        final Button buttonSave = dialog.findViewById(R.id.button_save);

        // Populate existing values
        inputRecipient.setText(listItem.recipient);
        inputSender.setText(
                listItem.sender == null || listItem.sender.isEmpty() ? "*" : listItem.sender
        );
        inputKeywords.setText(listItem.keywords == null ? "" : listItem.keywords);
        inputBlacklist.setText(listItem.blacklist == null ? "" : listItem.blacklist);

        if (isAdd) {
            buttonDelete.setText(R.string.label_button_cancel);
        }

        // Delete / Cancel
        buttonDelete.setOnClickListener(v -> {
            if (!isAdd) {
                listItems.remove(position);
                listAdapter.notifyDataSetChanged();
                Preferences.setRecipientListItems(this, listItems);
            }
            dialog.dismiss();
        });

        // Save
        buttonSave.setOnClickListener(v -> {

            String newRecipient = inputRecipient.getText().toString().trim();
            String newSender = inputSender.getText().toString().trim();
            String newKeywords = inputKeywords.getText().toString().trim();
            String newBlacklist = inputBlacklist.getText().toString().trim();

            if (newRecipient.isEmpty()) {
                Toast.makeText(
                        this,
                        getResources().getString(R.string.error_missing_required_value),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (newSender.isEmpty()) {
                newSender = "*";
            }

            listItem.recipient = newRecipient;
            listItem.sender = newSender;
            listItem.keywords = newKeywords;
            listItem.blacklist = newBlacklist;

            if (isAdd) {
                listItems.add(listItem);
            }

            listAdapter.notifyDataSetChanged();
            Preferences.setRecipientListItems(this, listItems);
            dialog.dismiss();
        });

        dialog.show();
    }
}
