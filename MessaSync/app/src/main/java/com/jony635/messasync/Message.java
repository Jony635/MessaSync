package com.jony635.messasync;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Message
{
    public User owner = null;
    public String content = "";

    Timestamp date = new Timestamp(new Date());

    public Message(){}
    public Message(User owner, String content)
    {
        this.owner = owner; this.content = content;
    }
}
