package edu.vt.workspace.data;

import edu.vt.workspace.components.AWArbFileList;
import edu.vt.workspace.components.AWEntityBrowser;
import edu.vt.workspace.components.AWEntityView;
import edu.vt.workspace.components.AWFileBrowser;
import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.AWImageFrame;
import edu.vt.workspace.components.AWTextFrame;
import edu.vt.workspace.components.AWNote;
import edu.vt.workspace.components.AWSearchResults;
import edu.vt.workspace.components.SimpleLink;
import edu.vt.workspace.components.utilities.HighlightEvent;
import edu.vt.workspace.components.utilities.HighlightListener;
import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.sqlite.SQLiteConfig;

/**
 * This class serves as the main interface to the SQLite database.
 * 
 * @todo this should be more robust in  the face of changing schemas, either aborting when a bad one is detected or attempting to rectify any discrepancies.
 * 
 * @author Christopher Andrews [cpa@cs.vt.edu]
 */
public class AWDataManager implements HighlightListener {

    private static final String[] SCHEMA = {
        "CREATE TABLE entityType (typeID INTEGER PRIMARY KEY, type TEXT UNIQUE, color INTEGER );",
        "CREATE TABLE entity (entityID INTEGER PRIMARY KEY, name TEXT, typeID INTEGER, FOREIGN KEY(typeID) REFERENCES entityType(typeID));",
        "CREATE TABLE alias (aliasID INTEGER PRIMARY KEY, name TEXT, entityID INTEGER, FOREIGN KEY(entityID) REFERENCES entity(entityID) ON DELETE CASCADE);",
        "CREATE TABLE entityProperty (entityID INTEGER, key TEXT, value TEXT, PRIMARY KEY(entityID, key), FOREIGN KEY(entityID) REFERENCES entity(entityID) ON DELETE CASCADE);",
        "CREATE TABLE document (documentID INTEGER PRIMARY KEY, name TEXT UNIQUE, title TEXT, text TEXT, type TEXT, date DATE, source TEXT, seen BOOLEAN);",
        "CREATE TABLE documentEntity (start INTEGER, end INTEGER,documentID INTEGER,  entityID INTEGER, FOREIGN KEY(documentID) REFERENCES document(documentID) , FOREIGN KEY(entityID) REFERENCES entity(entityID) ON DELETE CASCADE);",
        "CREATE TABLE documentHighlight (start INTEGER, end INTEGER,documentID INTEGER, FOREIGN KEY(documentID) REFERENCES document(documentID));",
        "CREATE TABLE documentProperty (documentID INTEGER, key TEXT, value TEXT, PRIMARY KEY(documentID, key), FOREIGN KEY(documentID) REFERENCES document(documentID));",
        "CREATE TABLE note (noteID INTEGER PRIMARY KEY, title TEXT, text TEXT, color INTEGER);",
        "CREATE TABLE frame (frameID INTEGER PRIMARY KEY, frameTitle TEXT, x INTEGER, y INTEGER, width INTEGER, height INTEGER, iconified BOOLEAN);",
        "CREATE TABLE documentFrame (frameID INTEGER PRIMARY KEY, documentID, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE, FOREIGN KEY(documentID) REFERENCES document(documentID))",
        "CREATE TABLE entityFrame (frameID INTEGER PRIMARY KEY, entityID, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE, FOREIGN KEY(entityID) REFERENCES entity(entityID) ON DELETE CASCADE)",
        "CREATE TABLE noteFrame (frameID INTEGER PRIMARY KEY, noteID, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE, FOREIGN KEY(noteID) REFERENCES note(noteID) ON DELETE CASCADE)",
        "CREATE TABLE fileBrowser (frameID INTEGER PRIMARY KEY, mode TEXT, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE)",
        "CREATE TABLE entityBrowser (frameID INTEGER PRIMARY KEY, currentType TEXT, sortBy TEXT, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE)",
        "CREATE TABLE searchResults (query Text, source INTEGER, frameID INTEGER, FOREIGN KEY(source) REFERENCES frame(frameID) ON DELETE SET NULL, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE SET NULL);",
        "CREATE TABLE fileList (listID INTEGER PRIMARY KEY, description TEXT, frameID INTEGER, FOREIGN KEY(frameID) REFERENCES frame(frameID) ON DELETE CASCADE);",
        "CREATE TABLE fileListLink (listID, documentID, FOREIGN KEY(listID) REFERENCES fileList(listID) ON DELETE CASCADE, FOREIGN KEY(documentID) REFERENCES document(documentID) ON DELETE CASCADE);",
        "CREATE TABLE link (sourceID INTEGER, sinkID INTEGER, color INTEGER, keepVisible BOOLEAN, mutable BOOLEAN, FOREIGN KEY(sourceID) REFERENCES frame(frameID) ON DELETE CASCADE, FOREIGN KEY(sinkID) REFERENCES frame(frameID) ON DELETE CASCADE);",
        "CREATE INDEX documentEntityLookup ON documentEntity(documentID);",
        "CREATE INDEX entityDocumentLookup ON documentEntity(entityID);",
        "CREATE INDEX documentPropertyLookup ON documentProperty(documentID);",
        "CREATE INDEX entityPropertyLookup ON entityProperty(entityID);",
        "CREATE INDEX aliasNameLookup ON alias(name);",
        "CREATE INDEX aliasIDLookup ON alias(entityID);",
        "CREATE INDEX documentHighlightLookup ON documentHighlight(documentID);"
    };
    private static AWDataManager _instance = new AWDataManager();
    private Connection _connection = null;
    private Statement _statement;
    private ChangeListener _listener;
    private boolean _loading = false;
    private PreparedStatement _addEntityStmt;
    private PreparedStatement _changeEntityStmt;
    private PreparedStatement _addAliasStmt;
    private PreparedStatement _findAliasStmt;
    private PreparedStatement _findEntityStmt;
    private PreparedStatement _addEntityViewStmt;
    private PreparedStatement _getTypeCountStmt;
    private PreparedStatement _findEntityByTypeStmt;
    private PreparedStatement _addDocumentStmt;
    private PreparedStatement _addHighlightStmt;
    private PreparedStatement _rmHighlightStmt;
    private PreparedStatement _addDocumentEntityStmt;
    private PreparedStatement _rmDocumentEntityStmt;
    private PreparedStatement _setDocumentStateStmt;
    private PreparedStatement _setDocumentPropertyStmt;
    private PreparedStatement _clearDocumentPropertiesStmt;
    private PreparedStatement _setEntityPropertyStmt;
    private PreparedStatement _clearEntityPropertiesStmt;
    private PreparedStatement _addNoteStmt;
    private PreparedStatement _updateNoteStmt;
    private PreparedStatement _addSearchResultsStmt;
    private PreparedStatement _addFileBrowserStmt;
    private PreparedStatement _updateFileBrowserStmt;
    private PreparedStatement _addEntityBrowserStmt;
    private PreparedStatement _updateEntityBrowserStmt;
    private PreparedStatement _addFileListStmt;
    private PreparedStatement _regFileListStmt;
    private PreparedStatement _addFrameStmt;
    private PreparedStatement _rmFrameStmt;
    private PreparedStatement _positionStmt;
    private PreparedStatement _regDocFrameStmt;
    private PreparedStatement _regNoteFrameStmt;
    private PreparedStatement _addLinkStmt;
    private PreparedStatement _rmLinkStmt;

    private AWDataManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {

            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);

            _connection = DriverManager.getConnection("jdbc:sqlite::memory:", config.toProperties());
            _statement = _connection.createStatement();

            for (String cmd : SCHEMA) {
                _statement.addBatch(cmd);
            }
            _statement.executeBatch();

            initializePreparedStatements();

        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        _listener = new ChangeListener();

    }

    public static AWDataManager getInstance() {
        return _instance;
    }

    private void initializePreparedStatements() {
        try {
            _addEntityStmt = _connection.prepareStatement("INSERT INTO entity (name, typeID) VALUES (?, (SELECT typeID FROM entityType WHERE type=?));");
            _changeEntityStmt = _connection.prepareStatement("UPDATE entity set typeID = (SELECT typeID FROM entityType WHERE type=?) WHERE entityID=?;");
            _addAliasStmt = _connection.prepareStatement("INSERT INTO alias (name, entityID) VALUES (?, ?);");
            _findAliasStmt = _connection.prepareStatement("SELECT entityID FROM alias WHERE name = ?;");
            _findEntityStmt = _connection.prepareStatement("SELECT entityID FROM entity WHERE name = ?;");
            _getTypeCountStmt = _connection.prepareStatement("SELECT count(entityID) FROM entity WHERE typeID = (SELECT typeID FROM entityType WHERE type LIKE ?);");
            _findEntityByTypeStmt = _connection.prepareStatement("SELECT entityID FROM entity WHERE typeID = (SELECT typeID FROM entityType WHERE type LIKE ?);");
            _addEntityViewStmt = _connection.prepareStatement("INSERT INTO entityFrame (entityID, frameID) VALUES (?, ?);");

            _addDocumentStmt = _connection.prepareStatement("INSERT INTO document (name, title, text, type, date, source, seen) VALUES (?,?,?,?,?,?,?)");
            _addHighlightStmt = _connection.prepareStatement("INSERT INTO documentHighlight (start, end, documentID) VALUES (?,?,?);");
            _rmHighlightStmt = _connection.prepareStatement("DELETE FROM documentHighlight WHERE documentID = ? AND start = ? AND end = ?;");
            _addDocumentEntityStmt = _connection.prepareStatement("INSERT INTO documentEntity (start, end, documentID, entityID) VALUES (?,?,?,?);");
            _rmDocumentEntityStmt = _connection.prepareStatement("DELETE FROM documentEntity WHERE documentID =? AND start = ? AND end = ?;");
            _setDocumentStateStmt = _connection.prepareStatement("UPDATE document SET seen=? WHERE documentID = ?;");

            _setDocumentPropertyStmt = _connection.prepareStatement("INSERT INTO documentProperty (documentID, key, value) VALUES (?,?,?);");
            _setEntityPropertyStmt = _connection.prepareStatement("INSERT INTO entityProperty (entityID, key, value) VALUES (?,?,?);");
            _clearDocumentPropertiesStmt = _connection.prepareStatement("DELETE FROM documentProperty WHERE documentID=?;");
            _clearEntityPropertiesStmt = _connection.prepareStatement("DELETE FROM entityProperty WHERE entityID=?;");

            _addNoteStmt = _connection.prepareStatement("INSERT INTO note (title, text, color) VALUES (?,?,?);");
            _updateNoteStmt = _connection.prepareStatement("UPDATE note SET text = ?, color = ? WHERE noteID = ?;");


            _addSearchResultsStmt = _connection.prepareStatement("INSERT INTO searchResults (query, source, frameID) VALUES (?, ?, ?);");

            _addFileBrowserStmt = _connection.prepareStatement("INSERT INTO fileBrowser (frameID, mode) VALUES (?, ?);");
            _updateFileBrowserStmt = _connection.prepareStatement("UPDATE fileBrowser SET mode = ? WHERE frameID = ?;");
            _addEntityBrowserStmt = _connection.prepareStatement("INSERT INTO entityBrowser (frameID, currentType, sortBy) VALUES (?, ?, ?);");
            _updateEntityBrowserStmt = _connection.prepareStatement("UPDATE entityBrowser SET currentType = ?, sortBy = ? WHERE frameID = ?;");

            _addFileListStmt = _connection.prepareStatement("INSERT INTO fileList (description, frameID) VALUES (?,?);");
            _regFileListStmt = _connection.prepareStatement("INSERT INTO fileListLink (listID, documentID) VALUES (?,?);");

            _addFrameStmt = _connection.prepareStatement("INSERT INTO frame (frameTitle,x,y,width,height,iconified) VALUES (?,?,?,?,?,?);");
            _rmFrameStmt = _connection.prepareStatement("DELETE FROM frame WHERE frameID = ?;");
            _positionStmt = _connection.prepareStatement("UPDATE frame SET x = ?, y = ?, width = ?, height = ?, iconified= ? WHERE frameID = ?");
            _regDocFrameStmt = _connection.prepareStatement("INSERT INTO documentFrame (frameID, documentID) VALUES (?,?);");
            _regNoteFrameStmt = _connection.prepareStatement("INSERT INTO noteFrame (frameID, noteID) VALUES (?,?);");

            _addLinkStmt = _connection.prepareStatement("INSERT INTO link (sourceID, sinkID, color, keepVisible, mutable) VALUES (?,?,?,?,?);");
            _rmLinkStmt = _connection.prepareStatement("DELETE FROM link WHERE sourceID = ? AND sinkID = ?");
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void loadData(File file) {
        Statement statement;
        _loading = true;
        try {
            statement = _connection.createStatement();
            statement.executeUpdate("restore from '" + file.getAbsolutePath() +  "'");


            System.out.println("loading entities");
            // load entities into the system
            PreparedStatement getProperties = _connection.prepareStatement("SELECT key, value FROM entityProperty WHERE entityID = ?;");
            PreparedStatement aliasStmt = _connection.prepareStatement("SELECT name from alias WHERE entityID = ?;");
            ResultSet rs = statement.executeQuery("SELECT entityID, name, type FROM entity NATURAL JOIN entityType;");
            while (rs.next()) {
                AWEntity entity = new AWEntity(rs.getString("type"), rs.getString("name"));
                entity.setID(rs.getInt("entityID"));
                EntityManager.getInstance().addEntity(entity);

                // collect aliases
                aliasStmt.setInt(1, entity.getID());
                ResultSet aliases = aliasStmt.executeQuery();
                while (aliases.next()) {
                    entity.addAlias(aliases.getString(1));
                }

                getProperties.setInt(1, entity.getID());
                ResultSet propertySet = getProperties.executeQuery();
                while (propertySet.next()) {
                    entity.setProperty(propertySet.getString("key"), propertySet.getString("value"));
                }
            }

            System.out.println("loading documents");
            DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
            // load documents into the system
            PreparedStatement docEntityStmt = _connection.prepareStatement("SELECT entityID, start, end FROM documentEntity WHERE documentID = ?;");
            PreparedStatement highlightStmt = _connection.prepareStatement("SELECT start, end FROM documentHighlight WHERE documentID = ?;");
            getProperties = _connection.prepareStatement("SELECT key, value FROM documentProperty WHERE documentID = ?;");
            rs = statement.executeQuery("SELECT documentID, name, title, text, type, date, source, seen FROM document;");
            while (rs.next()) {
                AWDocument document = new AWDocument();
                document.setId(rs.getInt("documentID"));
                document.setName(rs.getString("name"));
                document.setTitle(rs.getString("title"));
                document.setText(rs.getString("text"));
                document.setType(rs.getString("type"));
                try {
                    // we have to play some games with the date since rs.getDate() gives us an sql.Date object, which isn't very useful
                    document.setDate(dateFormater.parse(rs.getString("date")));
                } catch (ParseException ex) {
                    Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                document.setSource(rs.getString("source"));
                document.setSeen(rs.getBoolean("seen"));
                
                FileManager.getInstance().addDocument(document);

                // connect documents and entities
                docEntityStmt.setInt(1, document.getId());
                ResultSet entitySet = docEntityStmt.executeQuery();
                while (entitySet.next()) {
                    AWEntity entity = EntityManager.getInstance().getEntity(entitySet.getInt("entityID"));
                    entity.addDoc(document);
                    document.addEntity(entity, new Range(entitySet.getInt("start"), entitySet.getInt("end")));
                }

                // load the highlights
                highlightStmt.setInt(1, document.getId());

                ResultSet highlightSet = highlightStmt.executeQuery();
                while (highlightSet.next()) {
                    document.setHighlight(highlightSet.getInt("start"), highlightSet.getInt("end"));
                }

                getProperties.setInt(1, document.getId());
                ResultSet propertySet = getProperties.executeQuery();
                while (propertySet.next()) {
                    document.setProperty(propertySet.getString("key"), propertySet.getString("value"));
                }
                
                 document.addHighlightListener(this);


            }
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        initializePreparedStatements();
        _loading = false;
    }

    public int loadFrames() {
        Statement statement;
        ResultSet rs;
        _loading = true;
        int count = 0;
        System.out.println("loading frames");
        try {
            statement = _connection.createStatement();
            // Now load the visible components
            AWInternalFrame frame;
            // load the open documents into the space

            rs = statement.executeQuery("SELECT name, type, frameID, frameTitle, x, y, width, height, iconified FROM documentFrame NATURAL JOIN frame NATURAL JOIN document;");
            while (rs.next()) {
                AWDocument document = FileManager.getInstance().getDocument(rs.getString("name"));
                if (document != null) {
                    if (rs.getString("type").equalsIgnoreCase("image")) {
                        frame = new AWImageFrame(document, AWController.getInstance().getController());
                    } else {
                        frame = new AWTextFrame(document, AWController.getInstance().getController());
                        ((AWTextFrame) frame).setEditable(false);
                    }

                    if (frame != null) {
                        frame.setID(rs.getInt("frameID"));
                        frame.setTitle(rs.getString("frameTitle"));
                        AWController.getInstance().loadFrame(frame, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"), rs.getBoolean("iconified"));
                        FileManager.getInstance().addFrame(frame);
                    }
                }
                count++;
            }



            // load the entities into the space
            rs = statement.executeQuery("SELECT entityID, frameID, frameTitle, x, y, width, height, iconified FROM entityFrame NATURAL JOIN frame;");
            while (rs.next()) {
                AWEntity entity = EntityManager.getInstance().getEntity(rs.getInt("entityID"));
                try {
                    AWEntityView view = AWController.getInstance().displayEntity(entity);
                    view.setBounds(rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"));
                    view.setID(rs.getInt("frameID"));
                    try {
                        view.setIcon(rs.getBoolean("iconified"));
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (NullPointerException ex) {
                    Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                count++;
            }

            // load the file browsers into the space
            rs = statement.executeQuery("SELECT mode, frameID, frameTitle, x, y, width, height FROM fileBrowser NATURAL JOIN frame;");
            while (rs.next()) {
                AWFileBrowser fileBrowser = new AWFileBrowser();
                fileBrowser.setDisplayMode(rs.getString("mode"));
                fileBrowser.setTitle(rs.getString("frameTitle"));
                fileBrowser.setID(rs.getInt("frameID"));
                AWController.getInstance().loadFrame(fileBrowser, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"), false);
                count++;
            }

            // load the entity browsers into the space
            rs = statement.executeQuery("SELECT currentType, sortBy, frameID, frameTitle, x, y, width, height FROM entityBrowser NATURAL JOIN frame;");
            while (rs.next()) {
                AWEntityBrowser entityBrowser = new AWEntityBrowser();
                entityBrowser.setCurrentType(rs.getString("currentType"));
                entityBrowser.setSortBy(rs.getString("sortBy"));
                entityBrowser.setTitle(rs.getString("frameTitle"));
                entityBrowser.setID(rs.getInt("frameID"));
                AWController.getInstance().loadFrame(entityBrowser, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"), false);
                count++;
            }

            // load the notes into the space
            rs = statement.executeQuery("SELECT note.noteID, title, text, color, frameID, frameTitle, x, y, width, height FROM noteFrame NATURAL JOIN frame NATURAL JOIN note;");
            while (rs.next()) {
                AWNote note = new AWNote(AWController.getInstance().getController());
                note.setNoteID(rs.getInt("noteID"));
                note.setTitle(rs.getString("title"));
                note.setText(rs.getString("text"));
                note.setColor(new Color(rs.getInt("color")));

                note.setTitle(rs.getString("frameTitle"));
                note.setID(rs.getInt("frameID"));
                AWController.getInstance().loadFrame(note, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"), false);

                count++;
            }

            //load the search results into the space
            rs = statement.executeQuery("SELECT query, source, frameID, frameTitle, x, y, width, height FROM searchResults NATURAL JOIN frame;");
            while (rs.next()) {
                AWInternalFrame sourceFrame = AWController.getInstance().getFrame(rs.getInt("source"));

                AWSearchResults results = AWController.getInstance().displaySearchResults(rs.getString("query"), sourceFrame);
                results.setTitle(rs.getString("frameTitle"));
                results.setID(rs.getInt("frameID"));
                results.setBounds(rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"));
                count++;
            }


           


            // load arbitrary file list views
            PreparedStatement docListStmt = _connection.prepareStatement("SELECT name FROM fileListLink NATURAL JOIN document WHERE listID = ?");

            rs = statement.executeQuery("SELECT listID, description, frame.frameID, frameTitle, x, y, width, height FROM fileList NATURAL JOIN frame;");
            while (rs.next()) {
                AWArbFileList list = new AWArbFileList();
                list.setListID(rs.getInt("listID"));
                list.setID(rs.getInt("frameID"));
                list.setTitle(rs.getString("frameTitle"));
                list.setDescription(rs.getString("description"));

                docListStmt.setInt(1, list.getListID());
                ResultSet docList = docListStmt.executeQuery();
                while (docList.next()) {
                    list.addDocument(FileManager.getInstance().getDocument(docList.getString("name")));
                }

                AWController.getInstance().loadFrame(list, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height"), false);
                count++;
            }
            
            
             //load the links into the space [needs to be last, so the links have frames to connect to
            rs = statement.executeQuery("SELECT sourceID, sinkID, color, keepVisible, mutable FROM link;");
            while (rs.next()) {

                AWInternalFrame sourceFrame = AWController.getInstance().getFrame(rs.getInt("sourceID"));
                AWInternalFrame sinkFrame = AWController.getInstance().getFrame(rs.getInt("sinkID"));
                if (sourceFrame != null && sinkFrame != null) {
                    SimpleLink link = new SimpleLink(sourceFrame, sinkFrame);
                    link.setColor(new Color(rs.getInt("color")));
                    link.setKeepVisible(rs.getBoolean("keepVisible"));
                    link.setMutable(rs.getBoolean("mutable"));
                    AWLinkManager.getInstance().addLink(link);
                }
                count++;
            }

        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("done loading");
        _loading = false;
        return count;
    }

    public void saveData(File file) {
        try {

            Statement statement = _connection.createStatement();
            statement.executeUpdate("backup to '" + file.getAbsolutePath() + "'");


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*********************** Entity Management methods *************************/
    /**
     * Add a new entity type to the system
     * @param type the new type to add
     * @param color the color associated with the type
     */
    public void addEntityType(String type, Color color) {
        try {
            Statement statement = _connection.createStatement();
            String stmt = "INSERT INTO entityType (type, color) values ('" + type + "', " + color.getRGB() + ");";
            statement.executeUpdate(stmt);


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove a type from the system
     * @param type the type to remove
     */
    public void removeEntityType(String type) {
        try {
            Statement statement = _connection.createStatement();
            String stmt = "DELETE FROM entityType WHERE type LIKE '" + type + "';";
            statement.executeUpdate(stmt);


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add a new entity and set the id appropriately. If this fails, it returns the ID 0
     * 
     * @param name the name of the new entity
     * @param type the type of the new entity
     * @return the unique id number to use for the entity
     */
    public int addEntity(String name, String type) {
        try {
            _addEntityStmt.setString(1, name);
            _addEntityStmt.setString(2, type);
            _addEntityStmt.execute();
            ResultSet rs = _statement.executeQuery("SELECT last_insert_rowid();");
            return rs.getInt(1);


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Remove an entity from the system.
     * 
     * @param entity the entity being removed
     */
    public void removeEntity(AWEntity entity) {
        try {
            _statement.addBatch("DELETE FROM entity WHERE entityID='" + entity.getID() + "';");
            _statement.executeBatch();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Changes an entity's type to a new type.
     * @todo currently, this will fail if there isn't an instance of the new type in the DB already -- currently this is handled by the old code, but this is something to be wary of as I rip out the old stuff
     * 
     * @param entity the entity that is being changed
     * @param newType the new type to be applied to the entity
     */
    public void changeEntityType(AWEntity entity, String newType) {
        try {
            _changeEntityStmt.setString(1, newType);
            _changeEntityStmt.setInt(2, entity.getID());
            _changeEntityStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Register an alias with an entity.
     *
     * @param entity the entity that is gaining the alias
     * @param alias the alias to be added
     */
    public void registerAlias(AWEntity entity, String alias) {
        try {
            _addAliasStmt.setString(1, alias);
            _addAliasStmt.setInt(2, entity.getID());
            _addAliasStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method looks up the id for a particular entity. If no entity is found, this returns 0
     * @param name the name of the entity that is being searched for
     * @return the id of the entity or 0 if none is found
     */
    public int getEntity(String name) {

        int result = 0;
        try {
            _findEntityStmt.setString(1, name);
            ResultSet rs = _findEntityStmt.executeQuery();

            result = rs.getInt(1);
        } catch (SQLException ex) {
        }
        return result;
    }

    public Set<String> getEntityTypes() {
        try {
            Statement statement = _connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT count(type) FROM entityType;");
            int count = rs.getInt(1);

            Set<String> entityTypes = new HashSet<String>(count);

            rs = statement.executeQuery("SELECT type FROM entityType;");
            while (rs.next()) {
                entityTypes.add(rs.getString("type"));
            }
            return entityTypes;



        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public Color getEntityTypeColor(String type) {
        try {
            Statement statement = _connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT color from entityType WHERE type = '" + type + "';");
            if (rs.next()) {
                int c = rs.getInt("color");
                return new Color(c);


            }
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void updateEntityTypeColor(String type, Color color){
        try {
            PreparedStatement statement = _connection.prepareStatement("UPDATE entityType SET color=? WHERE type=?");
            statement.setInt(1, color.getRGB());
            statement.setString(2, type);
            statement.execute();
        
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method returns a list of all entity ids corresponding to a given type. If the list is
     * empty, the return set is empty.
     * @param type the type of the entities
     * @return an array of the entity ids
     */
    public int[] getEntitiesForType(String type) {
        try {
            int count = getEntityTypeCount(type);

            if (count > 0) {
                int[] result = new int[count];

                _findEntityByTypeStmt.setString(1, type);
                ResultSet rs = _findEntityByTypeStmt.executeQuery();
                int i = 0;
                while (rs.next() && i < count) {
                    result[i++] = rs.getInt(1);
                }
                return result;
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return new int[0];
    }

    /**
     * This method looks for a match for a particular alias and returns the matching entityID.
     * If no alias entity exists, then this returns 0
     * @param alias the alias that we are checking
     * @return
     */
    public int getEntityForAlias(String alias) {

        int result = 0;
        try {
            _findAliasStmt.setString(1, alias);
            ResultSet rs = _findAliasStmt.executeQuery();

            result = rs.getInt(1);
        } catch (SQLException ex) {
        }
        return result;
    }

    /**
     * Returns the number of entities for a particular type.
     * @param type
     * @return
     */
    public int getEntityTypeCount(String type) {
        try {
            _getTypeCountStmt.setString(1, type);
            ResultSet rs = _getTypeCountStmt.executeQuery();
            return rs.getInt(1);


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    private void addEntityView(AWEntityView view) {
        try {
            _addEntityViewStmt.setInt(1, view.getEntity().getID());
            _addEntityViewStmt.setInt(2, view.getID());
            _addEntityViewStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*********************** Document Management methods *************************/
    /**
     * This method loads a document from an {@code AWDocument} object into the database.
     * The method will also set the id of the document to correspond with the key used in the database.
     *
     * @param doc the document we are loading into the DB
     */
    public void addDocument(AWDocument doc) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        doc.addHighlightListener(this);
        try {
            _addDocumentStmt.setString(1, doc.getName());
            _addDocumentStmt.setString(2, doc.getTitle());
            _addDocumentStmt.setString(3, doc.getText());
            _addDocumentStmt.setString(4, doc.getType());
            if (doc.getDate() != null) {
                _addDocumentStmt.setString(5, format.format(doc.getDate()));
            } else {
                _addDocumentStmt.setString(5, "");
            }
            _addDocumentStmt.setString(6, doc.getSource());
            _addDocumentStmt.setBoolean(7, doc.getSeen());
            _addDocumentStmt.execute();
            ResultSet rs = _statement.executeQuery("SELECT last_insert_rowid();");
            doc.setId(rs.getInt(1));

            Map<Range, AWEntity> entityRanges = doc.getEntityRanges();
            for (Range range : entityRanges.keySet()) {
                addDocumentEntityConnection(doc, entityRanges.get(range), range.getStart(), range.getEnd());


            }

        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addHighlight(HighlightEvent he) {
        if (he.getEntity() == null) { // this is a user highlight
            try {
                _addHighlightStmt.setInt(1, he.getStart());
                _addHighlightStmt.setInt(2, he.getEnd());
                _addHighlightStmt.setInt(3, he.getDocument().getId());
                _addHighlightStmt.execute();


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addDocumentEntityConnection(he.getDocument(), he.getEntity(), he.getStart(), he.getEnd());
        }
    }

    public void updateHighlight(HighlightEvent he) {
        // ignore -- this is just about changing entity highlighting
    }

    public void removeHighlight(HighlightEvent he) {
        if (he.getEntity() == null) { // this is a user highlight
            try {
                _rmHighlightStmt.setInt(1, he.getDocument().getId());
                _rmHighlightStmt.setInt(2, he.getStart());
                _rmHighlightStmt.setInt(3, he.getEnd());
                _rmHighlightStmt.execute();


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            rmDocumentEntityConnection(he.getDocument(), he.getStart(), he.getEnd());
        }
    }

    public void addDocumentEntityConnection(AWDocument document, AWEntity entity, int start, int end) {
        try {
            _addDocumentEntityStmt.setInt(1, start);
            _addDocumentEntityStmt.setInt(2, end);
            _addDocumentEntityStmt.setInt(3, document.getId());
            _addDocumentEntityStmt.setInt(4, entity.getID());
            _addDocumentEntityStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void rmDocumentEntityConnection(AWDocument document, int start, int end) {
        try {
            _rmDocumentEntityStmt.setInt(1, start);
            _rmDocumentEntityStmt.setInt(2, end);
            _rmDocumentEntityStmt.setInt(3, document.getId());
            _rmDocumentEntityStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateDocumentState(AWDocument document) {
        try {
            _setDocumentStateStmt.setBoolean(1, document.getSeen());
            _setDocumentStateStmt.setInt(2, document.getId());
            _setDocumentStateStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*********************** Note Management methods ************************/
    /**
     * This method handles the creation of a new note and sets up a collection of
     * listeners to handle updates to the database.
     * title TEXT, text TEXT, color INTEGER, visible BOOLEAN, x INTEGER, y INTEGER, width INTEGER, height INTEGER
     * 
     * @param note an {@code AWNote} object to be added to the database
     */
    private void createNote(AWNote note) {
        // create the note in the database
        try {
            _addNoteStmt.setString(1, note.getTitle());
            _addNoteStmt.setString(2, note.getText());
            _addNoteStmt.setInt(3, note.getColor().getRGB());
            _addNoteStmt.execute();
            ResultSet rs = _statement.executeQuery("SELECT last_insert_rowid();");
            note.setNoteID(rs.getInt(1));



        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // update the title
        Statement statement;
        try {
            statement = _connection.createStatement();

            String stmt = "UPDATE note SET title = '" + note.getTitle() + "' WHERE noteID = " + note.getNoteID() + ";";
            statement.executeUpdate(stmt);


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            _regNoteFrameStmt.setInt(1, note.getID());
            _regNoteFrameStmt.setInt(2, note.getNoteID());
            _regNoteFrameStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void updateNote(AWNote note) {
        try {
            _updateNoteStmt.setString(1, note.getText());
            _updateNoteStmt.setInt(2, note.getColor().getRGB());
            _updateNoteStmt.setInt(3, note.getNoteID());
            _updateNoteStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*********************** Search Tool methods ************************/
    private void addSearchResults(AWSearchResults results) {
        if (!_loading) {
            try {
                _addSearchResultsStmt.setString(1, results.getQuery());
                if (results.getSource() != null) {
                    _addSearchResultsStmt.setInt(2, results.getSource().getID());
                } else {
                    _addSearchResultsStmt.setNull(2, java.sql.Types.INTEGER);
                }
                _addSearchResultsStmt.setInt(3, results.getID());
                _addSearchResultsStmt.execute();


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /*********************** Browser Management methods ************************/
    private void addFileBrowser(AWFileBrowser browser) {
        if (!_loading) {
            try {
                _addFileBrowserStmt.setInt(1, browser.getID());
                _addFileBrowserStmt.setString(2, browser.getDisplayMode());
                _addFileBrowserStmt.execute();


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateFileBrowser(AWFileBrowser browser) {
        try {
            _updateFileBrowserStmt.setString(1, browser.getDisplayMode());
            _updateFileBrowserStmt.setInt(2, browser.getID());
            _updateFileBrowserStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addEntityBrowser(AWEntityBrowser browser) {
        if (!_loading) {
            try {
                _addEntityBrowserStmt.setInt(1, browser.getID());
                _addEntityBrowserStmt.setString(2, browser.getCurrentType());
                _addEntityBrowserStmt.setString(3, browser.getSortBy());
                _addEntityBrowserStmt.execute();


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateEntityBrowser(AWEntityBrowser browser) {
        try {
            _updateEntityBrowserStmt.setString(1, browser.getCurrentType());
            _updateEntityBrowserStmt.setString(2, browser.getSortBy());
            _updateEntityBrowserStmt.setInt(3, browser.getID());
            _updateEntityBrowserStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*********************** Link Management methods ************************/
    public void addLink(SimpleLink link) {
        try {
            AWInternalFrame[] frames = link.getFrames();
            _addLinkStmt.setInt(1, frames[0].getID());
            _addLinkStmt.setInt(2, frames[1].getID());
            _addLinkStmt.setInt(3, link.getColor().getRGB());
            _addLinkStmt.setBoolean(4, link.getKeepVisible());
            _addLinkStmt.setBoolean(5, link.isMutable());
            _addLinkStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeLink(SimpleLink link){
        try {
            AWInternalFrame[] frames = link.getFrames();
                _rmLinkStmt.setInt(1, frames[0].getID());
                _rmLinkStmt.setInt(2, frames[1].getID());
                _rmLinkStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    /*********************** File List Management methods ************************/
    private void addFileList(AWArbFileList frame) {
        try {
            _addFileListStmt.setString(1, frame.getDescription());
            _addFileListStmt.setInt(2, frame.getID());
            _addFileListStmt.execute();
            ResultSet rs = _statement.executeQuery("SELECT last_insert_rowid();");
            int id = rs.getInt(1);

            frame.setListID(id);

            _regFileListStmt.setInt(1, id);
            for (AWDocument document : frame.getDocuments()) {
                _regFileListStmt.setInt(2, document.getId());
                _regFileListStmt.addBatch();
            }
            _regFileListStmt.executeBatch();



        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /*********************** Frame Management methods ************************/
    public void addFrame(AWInternalFrame frame) {

        if (!_loading && frame.getID() <= 0) {
            try {
                _addFrameStmt.setString(1, frame.getTitle());
                _addFrameStmt.setInt(2, frame.getX());
                _addFrameStmt.setInt(3, frame.getY());
                _addFrameStmt.setInt(4, frame.getWidth());
                _addFrameStmt.setInt(5, frame.getHeight());
                _addFrameStmt.setBoolean(6, frame.isIcon());
                _addFrameStmt.execute();
                ResultSet rs = _statement.executeQuery("SELECT last_insert_rowid();");
                frame.setID(rs.getInt(1));


            } catch (SQLException ex) {
                Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
            }


            if (frame.getDocument() != null) {
                try {
                    _regDocFrameStmt.setInt(1, frame.getID());
                    _regDocFrameStmt.setInt(2, frame.getDocument().getId());
                    _regDocFrameStmt.execute();


                } catch (SQLException ex) {
                    Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (frame instanceof AWNote) {
                createNote((AWNote) frame);
            } else if (frame instanceof AWFileBrowser) {
                addFileBrowser((AWFileBrowser) frame);
            } else if (frame instanceof AWEntityBrowser) {
                addEntityBrowser((AWEntityBrowser) frame);
            } else if (frame instanceof AWEntityView) {
                addEntityView((AWEntityView) frame);
            } else if (frame instanceof AWSearchResults) {
                addSearchResults((AWSearchResults) frame);
            } else if (frame instanceof AWArbFileList) {
                addFileList((AWArbFileList) frame);
            }
        }


        frame.addInternalFrameListener(_listener);

    }

    private void updateFrameState(AWInternalFrame frame) {
        try {
            _positionStmt.setInt(1, frame.getX());
            _positionStmt.setInt(2, frame.getY());
            _positionStmt.setInt(3, frame.getWidth());
            _positionStmt.setInt(4, frame.getHeight());
            _positionStmt.setBoolean(5, frame.isIcon());
            _positionStmt.setInt(6, frame.getID());
            _positionStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeFrame(AWInternalFrame frame) {
        try {
            _rmFrameStmt.setInt(1, frame.getID());
            _rmFrameStmt.execute();


        } catch (SQLException ex) {
            Logger.getLogger(AWDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void updateFrame(AWInternalFrame frame) {
        updateFrameState(frame);

        if (frame instanceof AWFileBrowser) {
            AWDataManager.getInstance().updateFileBrowser((AWFileBrowser) frame);
        } else if (frame instanceof AWEntityBrowser) {
            AWDataManager.getInstance().updateEntityBrowser((AWEntityBrowser) frame);
        } else if (frame instanceof AWNote) {
            AWDataManager.getInstance().updateNote((AWNote) frame);


        }
    }

    private class ChangeListener implements InternalFrameListener {

        public void internalFrameOpened(InternalFrameEvent e) {
        }

        public void internalFrameClosing(InternalFrameEvent e) {
        }

        public void internalFrameClosed(InternalFrameEvent e) {
            if (e.getInternalFrame() instanceof AWInternalFrame) {
                AWDataManager.getInstance().removeFrame((AWInternalFrame) e.getInternalFrame());
            }
        }

        public void internalFrameIconified(InternalFrameEvent e) {
            if (e.getInternalFrame() instanceof AWInternalFrame) {
                updateFrame((AWInternalFrame) e.getInternalFrame());
            }
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
            if (e.getInternalFrame() instanceof AWInternalFrame) {
                updateFrame((AWInternalFrame) e.getInternalFrame());
            }
        }

        public void internalFrameActivated(InternalFrameEvent e) {
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
            if (e.getInternalFrame() instanceof AWInternalFrame) {
                updateFrame((AWInternalFrame) e.getInternalFrame());
            }
        }
    }
}
