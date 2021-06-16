package com.example.screenrecorder;

public class ConferenceRoom {
    private int id;
    private String room_name;
    private int participants;

    public ConferenceRoom(int id, String room_name, int participants) {
        this.id = id;
        this.room_name = room_name;
        this.participants = participants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }
}
