package com.jas.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class NoteTakerForm {

        private String message;
        
        private String username = null;
        private String password;
        
        private String note;
        private List<ImmutablePair<Date, String>> notes;
        
        public NoteTakerForm() {
            notes = new ArrayList<ImmutablePair<Date, String>>();
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

        public boolean getIsLoggedIn() {
            return this.username != null;
        }
        
        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<ImmutablePair<Date, String>> getNotes() {
            return notes;
        }

        public void setNotes(List<ImmutablePair<Date, String>> notes) {
            this.notes = notes;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        
        
}
