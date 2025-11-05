package com.sankalp.womensafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "TrustedContacts";
    private static final String CONTACTS_KEY = "contacts";

    ListView contactsListView;
    TextView emptyListView;
    Button addContactButton;
    ArrayList<String> contactsList;
    ArrayAdapter<String> arrayAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsListView = findViewById(R.id.list_contacts);
        emptyListView = findViewById(R.id.empty_list_view);
        addContactButton = findViewById(R.id.button_add_contact);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadContacts();

        addContactButton.setOnClickListener(v -> showAddContactDialog());

        // Add long-press to delete
        contactsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmationDialog(position);
            return true;
        });
    }

    private void loadContacts() {
        Set<String> contacts = sharedPreferences.getStringSet(CONTACTS_KEY, new HashSet<>());
        contactsList = new ArrayList<>(contacts);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(arrayAdapter);
        checkIfListIsEmpty();
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> contactSet = new HashSet<>(contactsList);
        editor.putStringSet(CONTACTS_KEY, contactSet);
        editor.apply();
    }

    private void checkIfListIsEmpty() {
        if (contactsList.isEmpty()) {
            contactsListView.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
        } else {
            contactsListView.setVisibility(View.VISIBLE);
            emptyListView.setVisibility(View.GONE);
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Trusted Contact");

        final EditText input = new EditText(this);
        input.setHint("Enter Phone Number");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newContact = input.getText().toString().trim();
            if (!newContact.isEmpty()) {
                contactsList.add(newContact);
                arrayAdapter.notifyDataSetChanged();
                saveContacts();
                checkIfListIsEmpty();
                Toast.makeText(ContactsActivity.this, "Contact Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ContactsActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    contactsList.remove(position);
                    arrayAdapter.notifyDataSetChanged();
                    saveContacts();
                    checkIfListIsEmpty();
                    Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
