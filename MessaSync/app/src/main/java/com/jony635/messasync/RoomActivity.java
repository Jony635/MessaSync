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

    public static final int OTHER_MESSAGE = 0;
    public static final int OTHER_MESSAGE_USER = 1;
    public static final int MESSAGE = 2;
    public static final int MESSAGE_USER = 3;

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
    {
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = null;

            switch(viewType)
            {
                case MESSAGE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
                    break;
                case MESSAGE_USER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messageuser, parent, false);
                    break;
                case OTHER_MESSAGE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.othermessage, parent, false);
                    break;
                case OTHER_MESSAGE_USER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.othermessageuser, parent, false);
                    break;
            }

            assert view != null;

            return new MessageViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
        {
            Message message = messages.get(position);
            holder.contentTextView.setText(message.content);

            if(holder.userTextView != null)
                holder.userTextView.setText(message.owner.user);
        }

        @Override
        public int getItemCount()
        {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            super.getItemViewType(position);

            Message message = messages.get(position);

            if (position == 0)
            {
                if (message.owner.user.compareTo(loggedUser.user) == 0)
                    return MESSAGE_USER;
                else
                    return OTHER_MESSAGE_USER;
            }
            else
            {
                if (message.owner.user.compareTo(messages.get(position - 1).owner.user) != 0)
                {
                    //Different owners, use the user version
                    if (message.owner.user.compareTo(loggedUser.user) != 0)
                        return OTHER_MESSAGE_USER;
                    else
                        return MESSAGE_USER;
                }
                else
                {
                    //Same owner, use the message version
                    if (message.owner.user.compareTo(loggedUser.user) != 0)
                        return OTHER_MESSAGE;
                    else
                        return MESSAGE;
                }
            }
        }

        class MessageViewHolder extends RecyclerView.ViewHolder
        {
            int viewType;

            TextView userTextView;
            TextView contentTextView;

            public MessageViewHolder(@NonNull View view, int viewType)
            {
                super(view);

                this.viewType = viewType;

                userTextView = view.findViewById(R.id.user);
                contentTextView = view.findViewById(R.id.message);
            }
        }
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPrefs;

    private DocumentReference roomRef;
    private Room room;

    private RecyclerView messagesRV;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;

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

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRV.setLayoutManager(layoutManager);

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

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public void SendMessage(View view)
    {
        if(inputText.getText().length() <= 0)
            return;

        Message newMessage = new Message(loggedUser, inputText.getText().toString());
        roomRef.collection("messages").document().set(newMessage);

        inputText.setText(null);

        messages.add(newMessage);

        messagesRV.smoothScrollToPosition(messages.size()-1);
    }
}
