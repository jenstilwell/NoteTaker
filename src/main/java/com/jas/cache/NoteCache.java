package com.jas.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jas.resource.NoteResource;
import com.jas.util.NoteTakerProperties;

import java.sql.SQLException;



@Component
@Scope("singleton")
public class NoteCache implements CacheShutdownListener {

    public static final Log logger = LogFactory.getLog(NoteCache.class);

    
    CacheManager<String, NoteResource> cacheManager;
    
    public static final MetaData<String, NoteResource> metaData = new MetaData<String, NoteResource>() {
        public String getKey(NoteResource value) {
            return value.getUsername();
        }

        public Object getAlternateKey(NoteResource value, Object index) {           
            return value.getUsername();
        }
    };

    public NoteCache() {
        try {
            logger.info("\n Cache Dir: " + NoteTakerProperties.getCacheDir() + "\n");           
            
            Cache<String, NoteResource> cache = new HSQLDBCacheImpl<String, NoteResource>(NoteTakerProperties.getCacheDir(), "Note", false);
            
            this.cacheManager = new CacheManager<String, NoteResource>(CacheEnum.NOTE, metaData, cache);
            
        } catch (SQLException e) {
            logger.error("UNABLE TO CREATE CACHE: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    
    
    public void addNote(NoteResource resource) throws Exception {
                
        cacheManager.add(resource);
        
    }
    
    public NoteResource getNote(String username) {
        try {
            return cacheManager.get(username);
        } catch (Exception e) {
            logger.error("Unable to get note for user: " + username, e);
            return null;
        }
    }

    @Override
    public void shutdown() {
        logger.info("In shutdown");
        cacheManager.shutdown();
    }
    

}
