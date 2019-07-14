package com.jony635.messasync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPrefs;

    private User loggedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences("data", MODE_PRIVATE);

        if(!sharedPrefs.contains("user"))
        {
            //Register the user
            RegisterUser("");
        }
        else
        {
            loggedUser = new User(sharedPrefs.getString("user", ""), sharedPrefs.getString("password", ""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.LogOutmenuItem:
            {
                LogOut();
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void RegisterUser(final String userPredefined)
    {
        LayoutInflater inflater = getLayoutInflater();

        final View viewInflated = inflater.inflate(R.layout.edittextdialog, null, false);

        final EditText userEditText = viewInflated.findViewById(R.id.userEditTextDialog);
        final EditText passwordEditText = viewInflated.findViewById(R.id.passwordEditTextDialog);

        if(!userPredefined.isEmpty())
            userEditText.setText(userPredefined);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflated);
        builder.setTitle("Please, register before using this App");
        builder.setCancelable(false);
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(userEditText.getText().length() <= 0)
                {
                    Toast.makeText(MainActivity.this, "Error: User field is empty", Toast.LENGTH_SHORT).show();
                    RegisterUser("");
                }

                else if(passwordEditText.getText().length() <= 0)
                {
                    Toast.makeText(MainActivity.this, "Error: Password field is empty", Toast.LENGTH_SHORT).show();
                    RegisterUser(userEditText.getText().toString());
                }

                else
                {
                    //Search for the user in the database

                    final String user = userEditText.getText().toString();
                    final String password = passwordEditText.getText().toString();

                    Task<DocumentSnapshot> documentFinder = db.collection("users").document(user).get();
                    documentFinder.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists())
                            {
                                //Check if it has the same password stored

                                if(((String)document.get("password")).compareTo(password) == 0)
                                {
                                    loggedUser = document.toObject(User.class);

                                    UpdateSharedPreferences();
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, "Error: User exists and the password is different", Toast.LENGTH_SHORT).show();
                                    RegisterUser(user);
                                }
                            }
                            else
                            {
                                //This document does not exist, create it
                                loggedUser = new User(user, password);

                                db.collection("users").document(user).set(loggedUser);

                                UpdateSharedPreferences();
                            }
                        }
                    });
                }
            }
        });

        builder.create().show();
    }

    private void UpdateSharedPreferences()
    {
        sharedPrefs.edit().putString("user", loggedUser.user).apply();
        sharedPrefs.edit().putString("password", loggedUser.password).apply();
    }

    private void LogOut()
    {

    }
}
