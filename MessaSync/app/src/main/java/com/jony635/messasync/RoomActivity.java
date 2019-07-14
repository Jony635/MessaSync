package com.jony635.messasync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RoomActivity extends AppCompatActivity {

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
    {
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            //TODO: HERE WE HAVE TO GUESS WHAT KIND OF LAYOUT WE INFLATE,
            //TODO: DEPENDING OF THE USER AND THE PREVIOUS MESSAGE.
            //TODO: PROB USING THE viewType.

            //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
        {

        }

        @Override
        public int getItemCount()
        {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder
        {
            TextView userTextView;
            TextView contentTextView;

            public MessageViewHolder(@NonNull View view)
            {
                super(view);

                userTextView = view.findViewById(R.id.userName);
                contentTextView = view.findViewById(R.id.userMessage);
            }
        }
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPrefs;

    private DocumentReference roomRef;
    private Room room;

    private RecyclerView messagesRV;
    private MessageAdapter adapter;

    private EditText inputText;

    private List<Message> messages = new ArrayList<>();

    private User loggedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        sharedPrefs = getSharedPreferences("data", MODE_PRIVATE);
        loggedUser = new User(sharedPrefs.getString("user", ""), sharedPrefs.getString("password", ""));

        messagesRV = findViewById(R.id.messagesRV);
        adapter = new MessageAdapter();
        messagesRV.setAdapter(adapter);
        messagesRV.setLayoutManager(new LinearLayoutManager(this));

        Init();

        inputText = findViewById(R.id.InputText);
    }

    private void Init()
    {
        Intent intent = getIntent();

        if(intent != null)
        {
            String roomName = intent.getStringExtra(MainActivity.ROOM_NAME_MESSAGE);
            if(roomName != null)
            {
                roomRef = db.collection("rooms").document(roomName);
                roomRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        room = task.getResult().toObject(Room.class);
                    }
                });

                roomRef.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
                    {
                        if (e != null)
                        {
                            //TODO: Handle exceptions
                            return;
                        }

                        messages.clear();
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots)
                        {
                            messages.add(document.toObject(Message.class));
                        }

                        //TODO: NOTIFY THE ADAPTER
                    }
                });
            }
        }
    }

    public void SendMessage(View view)
    {
        Message newMessage = new Message(loggedUser, inputText.getText().toString());
        roomRef.collection("messages").document().set(newMessage);

        inputText.setText(null);
    }
}
