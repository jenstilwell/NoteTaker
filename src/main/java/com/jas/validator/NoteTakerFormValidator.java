package com.jas.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.jas.form.NoteTakerForm;
import com.jas.util.NoteUtil;

public class NoteTakerFormValidator implements Validator {

    public boolean supports(Class<?> clazz) {
        return NoteTakerForm.class.equals(clazz);
    }

    public void validate(Object obj, Errors errors) {
        NoteTakerForm form = (NoteTakerForm) obj;
        
        if (NoteUtil.isNullOrEmpty(form.getUsername())) {
            errors.rejectValue("username","user.null");
        } 
        
    }
    
    
}
