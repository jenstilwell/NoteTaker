package com.jas.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.hsqldb.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HSQLDBCacheImpl<K, V> implements Cache<K, V> {
    public static final Log logger = LogFactory.getLog(HSQLDBCacheImpl.class);
    public static List<String> initializedCacheTables = new ArrayList<String>();

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            throw new RuntimeException("failed to load HSQLDB JDBC driver (org.hsqldb.jdbcDriver)");
        }
    }

    String cacheName;
    boolean cachedTable;
    private DB db;

    /**
     * @param cacheLocation the location of the cache file
     * @param cacheName     the name of the cache
     * @param cachedTable   whether or not to use HSQLDB cached table
     * @throws java.sql.SQLException
     */
    public HSQLDBCacheImpl(String cacheLocation, String cacheName, boolean cachedTable) throws SQLException {
        this.cacheName = cacheName;
        this.cachedTable = cachedTable;
        this.db = getDB(cacheLocation);
        this.db.ensureCacheTable(cacheName, cachedTable);
    }
    
    public static void remoteShutdown() {
        try {
            Connection connection = DriverManager.getConnection(DB.url, "sa", "");
            connection.createStatement().execute("SHUTDOWN");
            connection.close();
        } catch (SQLException e) {
            logger.warn("unable to shutdown db", e);
        }
    }
    
    static class DB {
        String location;
        public static String url;
        Connection connection;

        public DB(String location) {
            this.location = location;    
            DB.url = "jdbc:hsqldb:file:" + location;
            
            logger.info("Using db location: " + this.location);

        }

        synchronized Connection getConnection() throws SQLException {
            if (null == connection || !validConnection()) {
                connection = DriverManager.getConnection(url, "sa", "");
            }
            return connection;
        }

        private boolean validConnection() {
            try {
                if (connection.isClosed()) return false;
                connection.createStatement().executeQuery("call curdate()");
                return true;
            } catch (SQLException e) {
                try {
                    connection.close();
                } catch (Exception e2) {
                }
                
                return false;
            }
        }

        synchronized void close() {
            System.out.println("DB: In close: " + connection);
            if (null == connection) return;
            try {
                connection.createStatement().execute("SHUTDOWN");
            } catch (SQLException e) {
                logger.warn("unable to execute shutdown statement", e);
            }
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("unable to close connection");
            }

            try {
                DatabaseManager.closeDatabases(0);
            } catch (Exception e) {
                logger.debug("exception closing hsqldb for cache: " + location, e);
            }
            
        }
        
        

        // From HSQLDB Docs:
        //
        // Memory tables are the default type when the CREATE TABLE command is used. Their data is held
        // entirely in memory but any change to their structure or contents is written to the <dbname>.script
        // file. The script file is read the next time the database is opened, and the MEMORY tables are
        // recreated with all their contents. So unlike TEMP table, the default, MEMORY tables are persistent.
        //
        // CACHED tables are created with the CREATE CACHED TABLE command. Only part of their data or indexes
        // is held in memory, allowing large tables that would otherwise take up to several hundred megabytes
        // of memory. Another advantage of cached tables is that the database engine takes less time to start
        // up when a cached table is used for large amounts of data. The disadvantage of cached tables is a
        // reduction in speed. Do not use cached tables if your data set is relatively small. In an application
        // with some small tables and some large ones, it is better to use the default, MEMORY mode for the
        // small tables.
        //
        public void ensureCacheTable(String name, boolean cached) throws SQLException {
            if (!initializedCacheTables.contains(name.toUpperCase())) {
                Connection conn = getConnection();
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getTables(null, "PUBLIC", name.toUpperCase(), null);
                if (!rs.next()) {
                    String create = "CREATE " + (cached ? "CACHED " : "MEMORY ") + "TABLE " + name
                            + " (key BIGINT PRIMARY KEY, "
                            + " keyvalue OTHER, "
                            + " value OTHER, json OTHER)";
                    conn.createStatement().execute(create);
                    conn.commit();
                    initializedCacheTables.add(name.toUpperCase());
                }
            }
        }

        PreparedStatement prepareStatement(String statement) throws SQLException {
            return getConnection().prepareStatement(statement);
        }

        <T> T executeUpdate(PreparedStatement statement, UpdateCallBack<T> callback) throws SQLException {
            try {
                int i = statement.executeUpdate();
                return callback.call(i);
            } finally {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }

        <T> T executeQuery(PreparedStatement statement, QueryCallBack<T> callback) throws SQLException {
            ResultSet rs = null;
            try {
                rs = statement.executeQuery();
                return callback.call(rs);
            } finally {
                try {
                    if(rs != null) rs.close();
                } catch (SQLException e) {
                }
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    static interface UpdateCallBack<T> {
        public T call(int i) throws SQLException;
    }

    static interface QueryCallBack<T> {
        public T call(ResultSet rs) throws SQLException;
    }

    private static Map<String, DB> dbMap = new HashMap<String, DB>();

    public synchronized static DB getDB(String location) {
        DB db = dbMap.get(location);
        if (null == db) {
            db = new DB(location);
            dbMap.put(location, db);
        }
        return db;
    }

    PreparedStatement createInsertStatement() throws SQLException {
        return db.prepareStatement("INSERT INTO " + cacheName + " (key, keyvalue, value, json) VALUES (?, ?, ?, ?)");
    }

    PreparedStatement createUpdateStatement() throws SQLException {
        return db.prepareStatement("UPDATE " + cacheName + " SET value = ?, json = ? WHERE key = ?");
    }

    PreparedStatement createDeleteStatement() throws SQLException {
        return db.prepareStatement("DELETE FROM " + cacheName + " WHERE key = ?");
    }

    PreparedStatement createDeleteAllStatement() throws SQLException {
        return db.prepareStatement("DELETE FROM " + cacheName);
    }

    PreparedStatement createSelectStatement() throws SQLException {
        return db.prepareStatement("SELECT value, json FROM " + cacheName + " WHERE key = ?");
    }

    PreparedStatement createSelectCountStatement() throws SQLException {
        return db.prepareStatement("SELECT COUNT(1) FROM " + cacheName);
    }

    PreparedStatement createSelectKeysStatement() throws SQLException {
        return db.prepareStatement("SELECT keyvalue FROM " + cacheName);
    }


    public V get(final K key) {
        logger.debug("get() called on " + cacheName);
        try {
            PreparedStatement st = createSelectStatement();
            st.setLong(1, key.hashCode());
            return db.executeQuery(st, new QueryCallBack<V>() {
                @SuppressWarnings("unchecked")
				public V call(ResultSet rs) throws SQLException {
                    if (!rs.next()) return null;

                    V obj = null;
                    try {
                        
                        obj = (V) rs.getObject("value");

                    } catch (Throwable t) {
                        
                        logger.error("Error getting object from cache, try from json", t);

                        //String xml = rs.getString("xml");
                        //obj = (V)XMLSerializationUtility.fromXml(xml);
                        
                        String json = rs.getString("json");
                        ObjectMapper mapper = new ObjectMapper();
                        try {
							obj = (V)mapper.readValue(json.getBytes(), Object.class);
						} catch (Exception e) {
							logger.error("Unable to get object from json: " + json);
							e.printStackTrace();
						}

                        logger.info("Successfully created object from json, update cache with proper object");
                        // update the object in the cache
                        put(key, obj);
                    }

                    return obj;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("get from cache failed", e);
        }
    }

    public List<K> getKeys() {
        logger.debug("getKeys() called on " + cacheName);
        try {
            PreparedStatement st = createSelectKeysStatement();
            return db.executeQuery(st, new QueryCallBack<ArrayList<K>>() {
                @SuppressWarnings("unchecked")
				public ArrayList<K> call(ResultSet rs) throws SQLException {
                    ArrayList<K> l = new ArrayList<K>();
                    while (rs.next()) {
                        l.add((K) rs.getObject("keyvalue"));
                    }
                    return l;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("getKeys from cache failed", e);
        }
    }

    public String getName() {
        return cacheName;
    }

    public void put(final K key, final V val) {
        logger.debug("put() called on " + cacheName);

        final String json = objectToJson(val);
        
        try {

            PreparedStatement st = createUpdateStatement();
            st.setObject(1, val);
            st.setString(2, json);
            st.setLong(3, key.hashCode());
            db.executeUpdate(st, new UpdateCallBack<Object>() {
                public Object call(int i) throws SQLException {
                    if (0 == i) {
                        PreparedStatement st2 = createInsertStatement();
                        st2.setLong(1, key.hashCode());
                        st2.setObject(2, key);
                        st2.setObject(3, val);
                        st2.setString(4, json);
                        db.executeUpdate(st2, new UpdateCallBack<Object>() {
                            public Object call(int i) throws SQLException {
                                if (1 != i) {
                                    throw new RuntimeException("failed to insert into cache (returned " + i + ")");
                                }
                                return null;
                            }
                        });
                    } else if (1 != i) {
                        throw new RuntimeException("failed to update cache (returned " + i + ")");
                    }
                    return null;
                }
            });


        } catch (SQLException e) {
            throw new RuntimeException("put to cache failed", e);
        }
    }


    public void remove(K key) {
        logger.debug("remove() called on " + cacheName);
        try {
            PreparedStatement st = createDeleteStatement();
            st.setLong(1, key.hashCode());
            db.executeUpdate(st, new UpdateCallBack<Object>() {
                public Object call(int i) throws SQLException {
                    if (1 != i) logger.debug("delete key from cache returned " + i + ": ");
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("delete from cache failed", e);
        }
    }

    public void removeAll() {
        logger.debug("removeAll() called on " + cacheName);
        try {
            PreparedStatement st = createDeleteAllStatement();
            db.executeUpdate(st, new UpdateCallBack<Object>() {
                public Object call(int i) throws SQLException {
                    logger.debug("deleteAll returned " + i);
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("delete from cache failed", e);
        }
    }

    public int size() {
        logger.debug("size() called on " + cacheName);
        ResultSet rs = null;
        try {
            PreparedStatement st = createSelectCountStatement();
            return db.executeQuery(st, new QueryCallBack<Integer>() {
                public Integer call(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getInt(1);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("get size from cache failed", e);
        } finally {
            try {
                if (null != rs) rs.close();
            } catch (SQLException e) {
            }
        }
    }

    public void shutdown() {
        System.out.println("HSQLDBCacheImple: In shutdown");
        db.close();
    }

    
    private String objectToJson(Object o) {
    	ObjectMapper mapper = new ObjectMapper();
        String json = null;
		try {
			json = mapper.writeValueAsString(o);
		} catch (Exception e1) {
			logger.error("Unable to convert object to json: " + o);
			e1.printStackTrace();
		}
		return json;
    }
}
