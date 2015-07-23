package com.jas.cache;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class CacheServletListener implements ServletContextListener {

    public static final Log logger = LogFactory.getLog(CacheServletListener.class);
        
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
       try {
           logger.info("Shutting down caches...");
          
           
           HSQLDBCacheImpl.remoteShutdown();

           Thread.sleep(1000);
           logger.info("After Shutdown");
       } catch (Exception e) {
           logger.error("Unable to shutdown caches", e);
       }
       
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        
    }


}
