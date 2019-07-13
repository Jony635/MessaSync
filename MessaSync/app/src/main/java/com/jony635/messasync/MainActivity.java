package com.jony635.messasync;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPrefs;

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
            //Login the user
        }


    }

    void RegisterUser(String user)
    {
        LayoutInflater inflater = getLayoutInflater();

        final View viewInflated = inflater.inflate(R.layout.edittextdialog, null, false);
        final EditText userEditText = viewInflated.findViewById(R.id.userEditTextDialog);
        if(!user.isEmpty())
            userEditText.setText(user);
        final EditText passwordEditText = viewInflated.findViewById(R.id.passwordEditTextDialog);

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
                    //Store this pair in the database

                }
            }
        });

        builder.create().show();
    }

}
