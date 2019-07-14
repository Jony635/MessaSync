package com.jony635.messasync;

import com.google.firebase.Timestamp;

public class Room
{
    public String name = "";
    public String password = "";
    public Timestamp date;

    public Room(){}
    public Room(String name, String password, Timestamp date)
    {
        this.name = name; this.password = password; this.date = date;
    }
}
