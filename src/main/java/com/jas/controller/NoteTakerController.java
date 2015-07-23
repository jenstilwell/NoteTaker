package com.jas.controller;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jas.cache.NoteCache;
import com.jas.form.NoteTakerForm;
import com.jas.resource.NoteResource;
import com.jas.util.NoteUtil;

@Controller
@RequestMapping("/note")
public class NoteTakerController {

    private static final Log logger = LogFactory.getLog(NoteTakerController.class);
    
    @Autowired
    private NoteCache noteCache;
    
    
    // http://localhost:8080/NoteTaker/note/
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getNotes(ModelMap model) {
        
        logger.info("getNotes...");      
        NoteTakerForm form = (NoteTakerForm)model.get("form");
        
        if (form == null || NoteUtil.isNullOrEmpty(form.getUsername())) {
            form = new NoteTakerForm();
        } else {
            NoteResource rsc = noteCache.getNote(form.getUsername());
            
            if (rsc != null) {
                form.setUsername(rsc.getUsername());
                form.setNotes(rsc.getNotes());
            } else { 
                form = new NoteTakerForm();              
            }
        }
        model.addAttribute("form", form);
        return "noteTaker";
    }
    @RequestMapping(method = RequestMethod.GET)
    public String getGetNotes(ModelMap model) {
        return getNotes(model);
    }
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLogin(ModelMap model) {
        return getNotes(model);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String saveNote(ModelMap model, @Validated NoteTakerForm form, BindingResult result) {
        
        if (!result.hasErrors()) {
            
            logger.info("Got new note: " + form.getNote() + " for " + form.getUsername());
            
            NoteResource rsc = noteCache.getNote(form.getUsername());
            logger.debug("rsc has before: " + rsc.getNotes());
            rsc.addNote(new Date(), form.getNote());
            try {
                noteCache.addNote(rsc);
            } catch (Exception e) {
                logger.error("Unable to save new note", e);
                result.addError(new ObjectError("note", e.getMessage()));
            }
            logger.debug("rsc has after: " + rsc.getNotes());
            
            form.setNote(null);
            form.setNotes(rsc.getNotes());
            model.addAttribute("form", form);
        }
        return "noteTaker";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(ModelMap model, @Validated NoteTakerForm form, BindingResult result) {
        
        if (!result.hasErrors()) {
            
            logger.info("Login: " + form.getUsername() + " - " + form.getPassword());
            
            NoteResource rsc = noteCache.getNote(form.getUsername());
            if (rsc != null) {
                logger.info("Found User: " + rsc.getUsername() + " - " + rsc.getPassword());
                if (!rsc.getPassword().equals(form.getPassword())) {
                    logger.info("Passwords don't match!");
                    form.setUsername(null);
                    form.setPassword(null);
                    model.put("username", "Bad bad");
                    form.setMessage("Bad credentials");
                    
                } else {
                    form.setNotes(rsc.getNotes());
                }
                
            } else {
                logger.info("User Not Found - creating new...");
                rsc = new NoteResource(form.getUsername(), form.getPassword());
                try {
                    noteCache.addNote(rsc);
                } catch (Exception e) {
                    logger.error("Unable to create new user", e);
                    result.addError(new ObjectError("username", e.getMessage()));
                }
            }
            model.addAttribute("form", form);
        }
        return "noteTaker";
    }
    
    
    
    public void setNoteCache(NoteCache noteCache) {
        this.noteCache = noteCache;
    }
    
   

}
