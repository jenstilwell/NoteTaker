package com.jas.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class NoteResource implements Serializable {


    private static final long serialVersionUID = 724273464084224970L;
    
    
    private String username;
    private String password;
    private List<ImmutablePair<Date, String>> notes;
    
    public NoteResource(String username, String password) {
        this.username = username;
        this.password = password;
        this.notes = new ArrayList<ImmutablePair<Date, String>>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ImmutablePair<Date, String>> getNotes() {
        return notes;
    }

    public void setNotes(List<ImmutablePair<Date, String>> notes) {
        this.notes = notes;
    }
    
    public void addNote(Date date, String note) {
        ImmutablePair<Date, String> p = new ImmutablePair<Date, String>(date, note);
        this.notes.add(p);
    }
    
    
}
