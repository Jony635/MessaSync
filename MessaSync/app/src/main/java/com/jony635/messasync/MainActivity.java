package com.jony635.messasync;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    public static final String ROOM_NAME_MESSAGE = "com.jony635.messasync.ROOMNAME";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPrefs;

    private User loggedUser = null;

    private CollectionReference roomsCol;

    private RecyclerView roomsRV;
    private RoomsAdapter roomsAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Room> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomsCol = db.collection("rooms");

        roomsRV = findViewById(R.id.roomsRV);
        //roomsRV.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        roomsRV.setLayoutManager(layoutManager);

        roomsAdapter = new RoomsAdapter();
        roomsRV.setAdapter(roomsAdapter);

        roomsCol.orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot roomsContainer, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    //Error handling
                    return;
                }

                if(roomsContainer != null)
                {
                    rooms.clear();
                    for(QueryDocumentSnapshot room : roomsContainer)
                    {
                        rooms.add(room.toObject(Room.class));
                    }

                    roomsAdapter.notifyDataSetChanged();
                }
            }
        });

        sharedPrefs = getSharedPreferences("data", MODE_PRIVATE);

        if(!sharedPrefs.contains("user"))
        {
            //Register the user
            RegisterUser("");
        }
        else
        {
            loggedUser = new User(sharedPrefs.getString("user", ""), sharedPrefs.getString("password", ""));
            if(loggedUser.user.compareTo("Jony635") == 0)
                findViewById(R.id.addRoomButton).setVisibility(View.VISIBLE);
        }
    }

    public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder>
    {
        public class RoomsViewHolder extends RecyclerView.ViewHolder
        {
            //View references
            public TextView roomName;

            public RoomsViewHolder(View view)
            {
                super(view);

                //Initialize view references
                roomName = view.findViewById(R.id.roomLayout);

                if(loggedUser != null && loggedUser.user.compareTo("Jony635") == 0)
                {
                    roomName.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view)
                        {
                            //AlertDialog with a menu for deleting, renaming, whitelist and blacklist.
                            return true;
                        }
                    });
                }

                roomName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        int position = getLayoutPosition();

                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                        intent.putExtra(ROOM_NAME_MESSAGE, rooms.get(position).name);
                        startActivity(intent);
                    }
                });
            }
        }

        @NonNull
        @Override
        public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            //Create a RoomsViewHolder with an inflated view and return it.

            //Can be a custom layout view
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.roomlayout, parent, false);

            return new RoomsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoomsViewHolder holder, int position)
        {
            //Set the holder views content to the stored position one.
            holder.roomName.setText(rooms.get(position).name);
        }

        @Override
        public int getItemCount()
        {
            return rooms.size();
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

        final EditText userEditText = viewInflated.findViewById(R.id.userEditText);
        final EditText passwordEditText = viewInflated.findViewById(R.id.passwordEditText);

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
        //Enable the FloatingActionButton for adding new rooms if the user is admin.
        if(loggedUser.user.compareTo("Jony635") == 0)
            findViewById(R.id.addRoomButton).setVisibility(View.VISIBLE);

        //This is used to reset OnLongClick listeners, which are only available if the user is an admin one.
        roomsRV.setAdapter(roomsAdapter);

        //Save locally the registered user data.
        sharedPrefs.edit().putString("user", loggedUser.user).apply();
        sharedPrefs.edit().putString("password", loggedUser.password).apply();
    }

    private void LogOut()
    {
        findViewById(R.id.addRoomButton).setVisibility(View.INVISIBLE);
        loggedUser = null;

        sharedPrefs.edit().clear().apply();

        RegisterUser("");
    }

    public void AddRoom(View view)
    {
        LayoutInflater inflater = getLayoutInflater();

        final View viewInflated = inflater.inflate(R.layout.newroomdialog, null, false);

        final EditText roomNameEditText = viewInflated.findViewById(R.id.roomNameEditText);
        final EditText roomPasswordEditText = viewInflated.findViewById(R.id.roomPasswordEditText);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflated);
        builder.setTitle("Setup the new room");
        builder.setCancelable(true);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(roomNameEditText.getText().length() > 0)
                {
                    //Search for this room in the database, and create it

                    String roomName = roomNameEditText.getText().toString();
                    String roomPassword = roomPasswordEditText.getText().toString();

                    Room room = new Room(roomName, roomPassword, new Timestamp(new Date()));
                    roomsCol.document(roomName).set(room);
                }
            }
        });
        builder.create().show();
    }
}