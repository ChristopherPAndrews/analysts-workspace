package edu.vt.workspace.components;



import apple.compatability.osxadapter.OSXAdapter;
import edu.vt.workspace.components.utilities.LayoutManager;
import edu.vt.workspace.components.utilities.SourceAwareTextTransferable;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDataManager;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWLinkManager;
import edu.vt.workspace.data.AWReader;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.EntityManager;
import edu.vt.workspace.data.FileManager;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import javax.swing.JPopupMenu;
import java.util.Vector;
import java.awt.Point;
import javax.swing.JInternalFrame;
import javax.swing.JDialog;
import java.awt.Component;
import java.awt.HeadlessException;

import edu.vt.workspace.search.SearchEngine;
import edu.vt.workspace.search.IndexManager;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;


/**
 * Describe class <code>AnalystsWorkspace</code> here.
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class AnalystsWorkspace extends JFrame
        implements ActionListener, Queriable {
    static final long serialVersionUID = -7969848029284769138L;

    public static final String DEFAULT_DATA_PATH = "data";
    public static final String PREFS_DATA_PATH_KEY = "DATA_PATH";
    public static final String PREFS_WORKSPACE_PATH_KEY = "WORKSPACE_PATH";



    private Preferences _prefs;
    private WorkspacePane _desktop;
    private SearchTool _searchTool;
    private String _indexPath;
    private File _currentWorkspaceFile;
    private File _tmpSaveFile;
    private SearchEngine _searchEngine;
    private Timer _saveTimer;

    private Object[][] importMenuItems = {
        {"Import files","import_files", null, null},
        {"Import Workspace file", "import_db", null, null},
        {"Import Jigsaw file", "import_jig", null, null},
        {"Import AWX file", "import_awx", null, null}
    };
    private Object[][] mainMenuItems = {
        {"Find", "find", KeyEvent.VK_F, null},
        {"separator"},
        {"Entity Browser", "entity_browser", KeyEvent.VK_E, null},
        {"File Browser", "file_browser", null, null},
        {"Category Browser", "category_browser", null, null},
       // {"Suggestions", "suggest", null, null},
        {"New Note", "new_note", KeyEvent.VK_N, null},
        {"New Workspace", "new_workspace", null,null},
        {"separator"},
        {"Open Workspace", "open_workspace", KeyEvent.VK_O, null},
        {"Save Workspace", "save_workspace", KeyEvent.VK_S,null},
        {"Save Workspace As...", "save_workspace_as", null, null},
        {"Export Data As...", "save_data", null, null},
        {"separator"},
        {"Import", "import", null, importMenuItems},
        {"Detect Entities", "detect_entities",null, null},
        {"separator"},
        {"Quit", "quit", KeyEvent.VK_Q, null}
    };
     

    public AnalystsWorkspace() {
        super("Analyst's Workspace");

        //Make the big window be indented 40 pixels from each edge
        //of the screen.
        int inset = 20;
        _prefs = Preferences.userNodeForPackage(getClass());
        String path = _prefs.get(PREFS_WORKSPACE_PATH_KEY, "");
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc =
                    gd.getConfigurations();
            for (int i = 0; i < gc.length; i++) {
                virtualBounds = virtualBounds.union(gc[i].getBounds());
            }
        }
        
        setBounds(virtualBounds.x ,
                virtualBounds.y ,
                virtualBounds.width ,
                virtualBounds.height - inset);
       // setBounds(0,0,1930,1130); // HD screencast bounds + frame

        //setBounds(0, 1600, 2560, 1600);  //gambit test bounds

      //  setBounds(0, 0, 2560, 1600);  //screencast bounds
      //  setBounds(-5120,1600,2560,1600); // magneto test bounds
        //setBounds(0, 10, 1300, 1000); // Thor test bounds
        //Set up the GUI.
        _desktop = new WorkspacePane(createPopupMenu()); //a specialized layered pane
        _desktop.add(AWPopupTextWindow.getInstance(), new Integer(WorkspacePane.PREVIEW_LAYER));
        _desktop.add(AWTooltip.getInstance(), new Integer(WorkspacePane.PREVIEW_LAYER));
        
        AWController.getInstance().setWorkspace(_desktop);
        AWController.getInstance().setController(this);
        LayoutManager.getInstance().setWorkspace(_desktop);
        AWLinkManager.getInstance().setWorkspace(_desktop);
        AWPopupTextWindow.getInstance().setController(this);
        setContentPane(_desktop);
        setJMenuBar(createMenuBar());
        Monitor.getInstance();

        
        // add drop support to the _desktop
        _desktop.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                Transferable t = support.getTransferable();
                try {
                    String s = (String) t.getTransferData(DataFlavor.stringFlavor);
                    AWNote note = createNote();
                    if (note != null) {
                        note.appendText(s);
                    }
                    System.out.println(t.getClass());
                    if (t instanceof SourceAwareTextTransferable){
                        System.out.println(((SourceAwareTextTransferable)t).getSource());
                    }
                } catch (UnsupportedFlavorException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }
                return true;
            }
        });

     
        // start the save timer -- this runs every 2 minutes and saves out to 
        // the workspace file if it exists or a temporary location if it doesn't
        
        ActionListener saveAction = new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                if (_desktop.isDirty()){
                    if (_currentWorkspaceFile == null){
                        if (_tmpSaveFile == null){
                            try {
                                _tmpSaveFile = File.createTempFile("aw_workspace", ".db");
                                saveWorkspace(_tmpSaveFile, false);
                            } catch (IOException ex) {
                                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                    }else{
                        File backupFile = new File(_currentWorkspaceFile.getPath() + "~");
                        try {
                            backupFile.createNewFile();
                            saveWorkspace(backupFile, false);
                        } catch (IOException ex) {
                            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
                        }
                       
                    }                    
                }
            }
            
        };
        
        _saveTimer = new Timer(120000, saveAction);
        _saveTimer.start();
        
    }

    private boolean initializeSearchEngine(boolean ignoreFailure) {
        // at the moment, this just loops the one time
        // later, when the manager is a little more complete, perhaps the user will cancel
       
        _indexPath = IndexManager.getIndexPath();
        try {
            _searchEngine = new SearchEngine(_indexPath);
        } catch (FileNotFoundException fnfe) {
            if (ignoreFailure) {
                return false;
            }
            Object[] options = {"Cancel", "Import raw files", "Import Jig File"};
            Object response = AWController.getInstance().askQuestion("There are currently no searchable documents in the workspace.\n" +
                    "Would you like to import documents, or work with search disabled?\n " +
                    "(data can also be imported through the 'Import' option in the menu)",
                    "No documents found",
                    JOptionPane.WARNING_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    options);

            if (response == options[1]) {
                //showIndexManager();
                importFiles();
                return initializeSearchEngine(false);
            } else if (response == options[2]){
                importJigFile();
                return initializeSearchEngine(false);
            }else {
                return false;
            }
        }
        return true;
    }

    private void resetTitle() {
        if (_currentWorkspaceFile != null) {
            setTitle("Analyst's Workspace : " + _currentWorkspaceFile.getName());
        } else {
            setTitle("Analyst's Workspace");
        }
    }



 
    private JFileChooser getFileChooser(String path, int mode) {
        String dirPath = new File(path).getParent();
        if (dirPath == null) {
            dirPath = ".";
        }

        JFileChooser jfc = new JFileChooser(dirPath) {

            static final long serialVersionUID = 998866510L;

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                Point p = parent.getMousePosition();
                if (p != null) {
                    Rectangle bounds = getBounds();
                    p.x -= bounds.width / 2;
                    p.y -= bounds.height / 2;
                    SwingUtilities.convertPointToScreen(p, parent);

                    dialog.setLocation(p);
                }
                return dialog;
            }
        };

        jfc.setFileSelectionMode(mode);
        return jfc;
    }

    
    /**
     * This method populates menus based on a 2D array containing a description of the 
     * values to be found in the array. The entries in the array are of the form:
     * name, action string, accelerator, submenu
     * Also valid are single entries containing the String "separator", which will
     * create a separator in the menu.
     * 
     * @param menu the menu object to populate
     * @param menuDesc The array containing the menu description
     * @return
     */
    private void populateMenu(JMenu menu, Object[][] menuDesc){
        JMenuItem menuItem;

         for (Object[] item : menuDesc) {
            if (item[0].equals("separator")) {
                // this is just a seperator
                menu.addSeparator();
            } else if (item[3] != null){
                // this is a submenu
                JMenu subMenu = new JMenu((String)item[0]);
                if (item[2] != null) {
                    subMenu.setMnemonic(((Integer) item[2]).intValue());
                    subMenu.setAccelerator(KeyStroke.getKeyStroke(
                            ((Integer) item[2]).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
                populateMenu(subMenu, (Object[][]) item[3]);
                menu.add(subMenu);

            }else {
                // this is a normal menu item
                menuItem = new JMenuItem((String) item[0]);
                menuItem.setActionCommand((String) item[1]);
                if (item[2] != null) {
                    menuItem.setMnemonic(((Integer) item[2]).intValue());
                    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                            ((Integer) item[2]).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }

                menuItem.addActionListener(this);
                menu.add(menuItem);
            }
        }
         
         
    }

     /**
     * This method is the same as the other populateMenu() method, with the exception that
     * this one operates on JPopoupMenu objects. This is an unfortunate duplication caused by the
     * fact that JMenu and JPopupMenu share many methods (e.g., addSeparator()), but
     * there is not a common parent that defines these.
     * >> Be warned about making changes in one or the other <<
     *
     * @param menu the popup menu object to populate
     * @param menuDesc The array containing the menu description
     * @return
     */
    private void populateMenu(JPopupMenu menu, Object[][] menuDesc){
        JMenuItem menuItem;

         for (Object[] item : menuDesc) {
            if (item[0].equals("separator")) {
                // this is just a seperator
                menu.addSeparator();
            } else if (item[3] != null){
                // this is a submenu
                JMenu subMenu = new JMenu((String)item[0]);
                if (item[2] != null) {
                    subMenu.setMnemonic(((Integer) item[2]).intValue());
                    subMenu.setAccelerator(KeyStroke.getKeyStroke(
                            ((Integer) item[2]).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
                populateMenu(subMenu, (Object[][]) item[3]);
                menu.add(subMenu);

            }else {
                // this is a normal menu item
                menuItem = new JMenuItem((String) item[0]);
                menuItem.setActionCommand((String) item[1]);
                if (item[2] != null) {
                    menuItem.setMnemonic(((Integer) item[2]).intValue());
                    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                            ((Integer) item[2]).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }

                menuItem.addActionListener(this);
                menu.add(menuItem);
            }
        }
    }





    /**
     * Create the main menubar. This will be infrequently used since it is duplicated
     * in a pop-up menu. It is primarily there for hot key support and as a backup.
     * @return the JMenuBar object to install
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        //Set up the main menu.
        JMenu menu = new JMenu("Document");
        menu.setMnemonic(KeyEvent.VK_D);
        
        populateMenu(menu, mainMenuItems);
        

        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);
        // create the edit menu
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        JMenuItem menuItem;
        
        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
         menuItem.setAccelerator(KeyStroke.getKeyStroke(((Integer) KeyEvent.VK_X).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(((Integer) KeyEvent.VK_C).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(((Integer) KeyEvent.VK_V).intValue(), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(menuItem);
        
//        menuItem = new JMenuItem("Cut");
//        menuItem.setAccelerator(KeyStroke.getKeyStroke('x', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//        menuItem.setAction(new DefaultEditorKit.CutAction());
//        menu.add(menuItem);
//        
//        menuItem.setAccelerator(KeyStroke.getKeyStroke('c', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//        menuItem.setAction(new DefaultEditorKit.CopyAction());
//        menu.add(menuItem);
//        
//        menuItem.setAccelerator(KeyStroke.getKeyStroke('v', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//        menuItem.setAction(new DefaultEditorKit.PasteAction());
//        menu.add(menuItem);
        
        menuBar.add(menu);
        return menuBar;
    }

    /**
     * Create the main contextual menu accessible by right-clicking anywhere in the background.
     * This provides all of the same functionality of the main menu.
     * 
     * @return the JPopupMenu object to install
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        populateMenu(menu, mainMenuItems);
        
        return menu;
    }

    //React to menu selections.
    public void actionPerformed(ActionEvent e) {
        if ("new_note".equals(e.getActionCommand())) { // new note
            createNote();
        }else if ("open_workspace".equals(e.getActionCommand())) {
            if (doOptionalSave()) {
                openWorkspace(true);
            }
        }else if ("save_workspace".equals(e.getActionCommand())) {
            if (_currentWorkspaceFile != null && _currentWorkspaceFile.isFile()) {
                saveWorkspace(_currentWorkspaceFile, true);
               _desktop.setDirty(false);
            } else {
                saveWorkspace();
            }
        }else if ("save_workspace_as".equals(e.getActionCommand())) {
            saveWorkspace();

        }else if ("save_data".equals(e.getActionCommand())){
            exportData();
        }else if ("find".equals(e.getActionCommand())) { // find
            showFind();
        }else if ("entity_browser".equals(e.getActionCommand())) { // show entity browser
            createEntityBrowser();
        }else if ("file_browser".equals(e.getActionCommand())) { // show file browser
            createFileBrowser();
        } else if ("category_browser".equals(e.getActionCommand())) { // show file browser
            createCategoryBrowser();
        } else if ("suggest".equals(e.getActionCommand())) { // show file browser
            AWController.getInstance().makeSuggestions();
        }else if ("import_files".equals(e.getActionCommand())){ // import
            importFiles();
        } else if ("import_db".equals(e.getActionCommand())){ // import
           openWorkspace(false);
        }else if ("import_jig".equals(e.getActionCommand())){ // import
           importJigFile();
        } else if ("import_awx".equals(e.getActionCommand())){ // import
           importWorkspace();
        } else if ("detect_entities".equals(e.getActionCommand())){ // import
           AWController.getInstance().detectEntities();
        } else if ("new_workspace".equals(e.getActionCommand())) {
            if (doOptionalSave()) {
                clearWorkspace();
                FileManager.reset();
                _currentWorkspaceFile = null;
                _prefs.put(PREFS_WORKSPACE_PATH_KEY, "");
            }
        } else { //quit
            quit();
        }
        resetTitle();
    }

    /**
     * Open a directory of text files and index them.
     */
    private void importFiles(){
       JFileChooser chooser = getFileChooser(_prefs.get(PREFS_DATA_PATH_KEY, ""),JFileChooser.DIRECTORIES_ONLY);
       int returnValue = chooser.showDialog(this, "Select Directory");
       if (returnValue == JFileChooser.APPROVE_OPTION){
           File dataDir = chooser.getSelectedFile();
           _prefs.put(PREFS_DATA_PATH_KEY,dataDir.getAbsolutePath());
            try {
                IndexManager.buildIndex(dataDir);
                // create internal documents and add them to the FileManager
                FileManager.getInstance().addDocuments(dataDir);
                System.out.println("Done");

            } catch (IOException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
            }
           
       }
    }


    /**
     * This method invokes the file browser to find a jigsaw file and then passes the file off
     * to the {@code AWController} for the actual loading.
     */
    private void importJigFile() {
        JFileChooser chooser = getFileChooser(_prefs.get(PREFS_DATA_PATH_KEY, ""), JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Jig files", "jig");
        chooser.setFileFilter(filter);
        if (chooser.showDialog(this, "Select File") == JFileChooser.APPROVE_OPTION) {
            AWController.getInstance().loadJigFile(chooser.getSelectedFile());
        }
    }


    /**
     * This method asks the user if they wish to save the workspace if it has
     * unsaved changes. It is intended to be called before loss events like
     * quitting or opening a new workspace.
     *
     * @return a boolean value indicating if the user canceled (thus canceling the loss event)
     */
    private boolean doOptionalSave() {
        if (_desktop.isDirty()) { // check to see if the user wants to save the current version first
            Object[] options = {"Don't Save", "Cancel", "Save"};
            Object response = AWController.getInstance().askQuestion("There are unsaved changes in the current workspace, do you wish to save them?",
                    "Unsaved changes",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    options);
            if (response == options[1]) {
                // user canceled - bail out
                return false;
            }

            if (response == options[2]) {
                if (_currentWorkspaceFile != null && _currentWorkspaceFile.isFile()) {
                    saveWorkspace(_currentWorkspaceFile, true);
                } else {
                    saveWorkspace();
                }
            }
        }
        return true;
    }

    protected void clearWorkspace() {
        // remove all of the regular frames
        JInternalFrame[] frames = _desktop.getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
        for (JInternalFrame f : frames) {
            if (!(f instanceof SearchTool)) {
                _desktop.remove(f);
                f.dispose();
            }
        }
        // remove the notes
        frames = _desktop.getAllFramesInLayer(WorkspacePane.NOTE_LAYER);
        for (JInternalFrame f : frames) {
            if (!(f instanceof SearchTool)) {
                _desktop.remove(f);
                f.dispose();
            }
        }


        FileManager.reset();
        EntityManager.reset();
        _desktop.setDirty(false);
    }

  
   

    /**
     * Loads an existing workspace file at the user's request.
     *
     * @todo the data miner is not hooked up for when we load an old workspace - this will actually be complicated since it is written to accept only Jigsaw files
     */
    protected void importWorkspace() {
        JFileChooser chooser = getFileChooser(_prefs.get(PREFS_DATA_PATH_KEY, ""), JFileChooser.FILES_ONLY);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File workspaceDoc = chooser.getSelectedFile();

            // clear out the old stuff
            clearWorkspace();
            try {
                System.out.println("Reading");
                AWReader reader = new AWReader(workspaceDoc);
                System.out.println("Indexing");
                // at this point the file should be parsed
                try {
                    IndexManager.buildIndex(FileManager.getInstance().getDocuments());
                } catch (IOException ex) {
                    Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Laying out");
                _desktop.setAutoPlace(false);
                for (AWInternalFrame frame: reader.getFrames()){
                    frame.setController(this);
                    if (frame instanceof AWNote){
                        _desktop.setLayer(frame, WorkspacePane.NOTE_LAYER);
                    }
                    _desktop.add(frame);
                    if (frame.getDocument()!= null)
                        FileManager.getInstance().addFrame(frame);
                }
                _desktop.setAutoPlace(true);

                // now that all of the frames are back in place - tell them to perform any post placement tasks
                for (AWInternalFrame frame: reader.getFrames()){
                    frame.reinitialize(this);
                }


                _currentWorkspaceFile = workspaceDoc;
                _desktop.setDirty(false);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
    }

    
    protected void saveWorkspace(File workspaceDoc, boolean clean) {
        for (JInternalFrame frame: _desktop.getAllFrames()){
            if (frame instanceof AWInternalFrame){
                AWDataManager.getInstance().updateFrame((AWInternalFrame)frame);
            }
        }
        
        for (AWDocument document: FileManager.getInstance().getDocuments()){
            AWDataManager.getInstance().updateDocumentState(document);
        }
        
        AWDataManager.getInstance().saveData(workspaceDoc);

        _desktop.setDirty(_desktop.isDirty() && !clean);
    }


    protected void saveWorkspace() {
        JFileChooser chooser = getFileChooser(_prefs.get(PREFS_DATA_PATH_KEY, ""), JFileChooser.FILES_ONLY);

        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File workspaceDoc = chooser.getSelectedFile();
            _currentWorkspaceFile = workspaceDoc;
            saveWorkspace(workspaceDoc, true);
        }
    }

    protected void exportData(){
        JFileChooser chooser = getFileChooser(".", JFileChooser.FILES_ONLY);

        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File dataDoc = chooser.getSelectedFile();
            AWWriter writer = new AWWriter(dataDoc);
            writer.setDataOnly(true);
            writer.write("entities", EntityManager.getInstance());
            writer.write("documents", FileManager.getInstance());
            writer.close();
        }
    }


    
    /**
     * This method opens a workspace file into the workspace. This really serves two purposes,
     * the workspace file can be a working file, so future saves should go back into it, or
     * it can be a load only, in which case future saves will prompt the user for a new file.
     * 
     * @param working if true, use the loaded file as the default destination for saves.
     */
    protected void openWorkspace(boolean working){
        JFileChooser chooser = getFileChooser(_prefs.get(PREFS_DATA_PATH_KEY, ""), JFileChooser.FILES_ONLY);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File database = chooser.getSelectedFile();
            AWController.getInstance().loadDatabase(database);
            if (working)
                _currentWorkspaceFile = database;
        }
    }
 

    protected AWNote createNote() {
        AWNote note = new AWNote(this);
        Monitor.getInstance().monitor(note);
        // notes don't worry about overlap - they should go where the mouse is
        _desktop.setAutoPlace(false);
        _desktop.setLayer(note, WorkspacePane.NOTE_LAYER);
        _desktop.add(note);
        AWController.getInstance().place(note);
        _desktop.setAutoPlace(true);
        try {
            note.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }

        return note;

    }


    
    /**
     * This is an interface back to the search engine.
     *
     * Given a search term, this uses the search engine to perform the search, and then the
     * results are transformed into AWDocument objects that are returned as a Vector.
     * @param query
     * @return an ArrayList of AWDocument objects
     */
    public Vector<AWDocument> performSearch(String query){
        ArrayList<String> results;
        Vector<AWDocument> resultDocs = new Vector<AWDocument>(10);
        if (_searchEngine == null) {
            if (!initializeSearchEngine(false)) {
                System.out.println("failed to initialize search engine");
                return null;
            }
        }
        // results is a list of Strings
        // This needs to check if the FileManager has a record of each result,
        // and if not, it should see if there is a File that corresponds to it and create a
        // new AWDocument to hold it


        results = _searchEngine.search(query);
        for (String result: results){
            AWDocument doc = FileManager.getInstance().getDocument(result);
            resultDocs.add(doc);
        }
        return resultDocs;
    }
    
  




    protected void showFind() {
        if (_searchEngine == null) {
            if (!initializeSearchEngine(false)) {
                return;
            }
        }

        if (_searchTool == null) {
            _searchTool = SearchTool.getInstance();
            _searchTool.setController(this);
            _desktop.setLayer(_searchTool, WorkspacePane.DIALOG_LAYER);
            _desktop.add(_searchTool);
        }

        AWController.getInstance().place(_searchTool);

        _searchTool.setVisible(true);

        try {
            _searchTool.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }

    }




    /**
     * Create a new Entity Browser and add it to the workspace.
     */
    private void createEntityBrowser() {
        AWEntityBrowser browser = new AWEntityBrowser();
        browser.setController(this);
        _desktop.add(browser);
        AWController.getInstance().place(browser);
        browser.setVisible(true);
        try {
            browser.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create a new file browser and add it to the workspace
     */
    private void createFileBrowser() {
        AWFileBrowser browser = new AWFileBrowser();
        browser.setController(this);
        _desktop.add(browser);
        AWController.getInstance().place(browser);
        browser.setVisible(true);
        try {
            browser.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
     /**
     * Create a new category browser and add it to the workspace
     */
    private void createCategoryBrowser() {
        AWCategoryBrowser browser = new AWCategoryBrowser();
        browser.setController(this);
        _desktop.add(browser);
        AWController.getInstance().place(browser);
        browser.setVisible(true);
        try {
            browser.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    



    //Quit the application.
    public void quit() {
        System.out.println("quitting");
        if (doOptionalSave()) {
            System.exit(0);
        }

    }
    
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel("edu.vt.workspace.plaf.AWLookAndFeel");
        } catch (Exception e) {
            // if it doesn't work, I'll accept the default
        }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        final AnalystsWorkspace frame = new AnalystsWorkspace();
        // note.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                frame.quit();
            }
        });
        
        // extra voodoo to tap into the OSX quit handler so I can ask for
        // saves when CMD-Q is activated
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")){
            try {
                OSXAdapter.setQuitHandler(frame, frame.getClass().getDeclaredMethod("quit", (Class[])null));
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        
        


        //Display the window.
        frame.setVisible(true);

        frame.initializeSearchEngine(true);

    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });

    }

    
}
