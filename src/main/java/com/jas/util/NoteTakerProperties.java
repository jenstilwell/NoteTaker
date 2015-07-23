package com.jas.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;


public class NoteTakerProperties {

    private static Properties properties = null;
    
    static {
        Resource resource = new ClassPathResource("noteTaker.properties"); 
    
        try {
            properties = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            properties = new Properties();
        } 
    }
    
    public static String getCacheDir() {
        String cacheDir = properties.getProperty("cache.dir");
        if (NoteUtil.isNullOrEmpty(cacheDir)) {
            cacheDir = "/tmp/NoteTaker/note";
        } else {
            if (!cacheDir.endsWith("/")) {
                cacheDir = cacheDir + "/";
            }
            cacheDir = cacheDir + "note";
        }
        return cacheDir;
    }
}
