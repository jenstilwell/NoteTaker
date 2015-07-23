package com.jas.controller;


import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;

import com.jas.cache.NoteCache;
import com.jas.form.NoteTakerForm;
import com.jas.resource.NoteResource;

import java.util.Date;

import junit.framework.Assert;

 

public class NoteTakerControllerTest {
 
    //private MockMvc mockMvc;
 

    
    @Mock
    private NoteCache noteCacheMock;
    
    @Mock
    private BindingResult bindingResultMock;
    
    @Spy
    private ModelMap model;
    
    @InjectMocks
    NoteTakerController controller;
 
 
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeClass
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void test_not_logged_in() throws Exception {

        String result = controller.getNotes(model);
        Assert.assertEquals(result, "noteTaker");

    }
    

    @Test
    public void test_logged_in() throws Exception {
        
        NoteResource rsc = new NoteResource("bob", "123");
        Date d = new Date();
        rsc.addNote(d , "note1");
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername("bob");
        model.put("form", form);
  
        when(noteCacheMock.getNote("bob")).thenReturn(rsc);
        
        String result = controller.getNotes(model);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, atLeastOnce()).getNote("bob");
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 1);

    }
    
    @Test
    public void test_save_note() throws Exception {
        
        NoteResource rsc = new NoteResource("bob", "123");
        Date d = new Date();
        rsc.addNote(d , "note1");

        NoteTakerForm form = new NoteTakerForm();
        form.setUsername("bob");
        form.setNote("new note");
        model.put("form", form);
  
        when(noteCacheMock.getNote("bob")).thenReturn(rsc);
        
        String result = controller.saveNote(model, form, bindingResultMock);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, atLeastOnce()).getNote("bob");
        verify(noteCacheMock, atLeastOnce()).addNote(any(NoteResource.class));
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 2);

    }
   

    @Test
    public void test_save_note_error() throws Exception {
        
        reset(noteCacheMock);
        
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername(null);
        form.setNote("new note");
        model.put("form", form);
        
        BindingResult testBindingResult = new BindException(form, "form");
        ObjectError error = new ObjectError("username", "Username null");
        testBindingResult.addError(error);
 
        
        String result = controller.saveNote(model, form, testBindingResult);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, never()).getNote("bob");
        verify(noteCacheMock, never()).addNote(any(NoteResource.class));
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 0);

    }
    
    @Test
    public void test_login_error() throws Exception {
        
        reset(noteCacheMock);
        
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername(null);
        model.put("form", form);
        
        BindingResult testBindingResult = new BindException(form, "form");
        ObjectError error = new ObjectError("username", "Username null");
        testBindingResult.addError(error);
 
        
        String result = controller.login(model, form, testBindingResult);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, never()).getNote("bob");
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 0);

    }
    
    
    @Test
    public void test_login_exists() throws Exception {
        
        
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername("bob");
        form.setPassword("123");
        model.put("form", form);
        
        NoteResource rsc = new NoteResource("bob", "123");
        Date d = new Date();
        rsc.addNote(d , "note1");
        
        when(noteCacheMock.getNote("bob")).thenReturn(rsc);
        
        String result = controller.login(model, form, bindingResultMock);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, atLeastOnce()).getNote("bob");
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 1);

    }
    
    @Test
    public void test_login_bad_pass() throws Exception {
        
        
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername("bob");
        form.setPassword("456");
        model.put("form", form);
        
        NoteResource rsc = new NoteResource("bob", "123");
        Date d = new Date();
        rsc.addNote(d , "note1");
        
        when(noteCacheMock.getNote("bob")).thenReturn(rsc);
        
        String result = controller.login(model, form, bindingResultMock);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, atLeastOnce()).getNote("bob");
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 0);
        Assert.assertNull(form.getUsername());
        Assert.assertNull(form.getPassword());

    }
    
    @Test
    public void test_login_new_user() throws Exception {
        
        
        NoteTakerForm form = new NoteTakerForm();
        form.setUsername("john");
        form.setPassword("456");
        model.put("form", form);
        
        when(noteCacheMock.getNote("joun")).thenReturn(null);
        
        String result = controller.login(model, form, bindingResultMock);
        Assert.assertEquals(result, "noteTaker");
        verify(noteCacheMock, atLeastOnce()).getNote("john");
        Assert.assertTrue(model.containsAttribute("form"));
        form = (NoteTakerForm)model.get("form");
        Assert.assertNotNull(form);
        Assert.assertTrue(form.getNotes().size() == 0);
        Assert.assertEquals(form.getUsername(), "john");
        Assert.assertEquals(form.getPassword(), "456");

    }
    
}