package com.jony635.messasync;

import com.google.firebase.Timestamp;

import java.sql.Time;
import java.util.Date;

public class Message
{
    public User owner = null;
    public String content = "";
    public Timestamp date;

    public Message(){}
    public Message(User owner, String content)
    {
        this.owner = owner; this.content = content;
        date = Timestamp.now();
    }
}
