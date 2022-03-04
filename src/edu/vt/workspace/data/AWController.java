package edu.vt.workspace.data;

import edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI.ADocument;
import edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI.ConceptMapLoader;
import edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI.EdgeWithInfo;
import edu.vt.cs.shahriar.VAST.Preprocess.NeighborSearch;
import edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI_NonXML.EdgeWithDocs;

import edu.vt.entityextractor.Entity;
import edu.vt.entityextractor.EntityExtractor;




import edu.vt.workspace.components.AWArbFileList;
import edu.vt.workspace.components.AWDocumentView;
import edu.vt.workspace.components.AWEntityView;
import edu.vt.workspace.components.AWFileBrowser;
import edu.vt.workspace.components.AWImageFrame;
import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.AWTextFrame;
import edu.vt.workspace.components.AWNote;
import edu.vt.workspace.components.AWSearchResults;
import edu.vt.workspace.components.AnalystsWorkspace;
import edu.vt.workspace.components.EntityEditor;
import edu.vt.workspace.components.EntityLink;
import edu.vt.workspace.components.EntitySelectionDialog;
import edu.vt.workspace.components.LinkLabel;
import edu.vt.workspace.components.Monitor;
import edu.vt.workspace.components.Queriable;
import edu.vt.workspace.components.SearchTool;
import edu.vt.workspace.components.SimpleLink;
import edu.vt.workspace.components.WorkerProgressDialog;
import edu.vt.workspace.components.WorkspacePane;
import edu.vt.workspace.components.utilities.SelectedLinkGraphicsPane;
import edu.vt.workspace.components.utilities.SimpleLinkListener;
import edu.vt.workspace.search.IndexManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * This class provides basic data management services. Eventually I'll be porting most of the
 * various Queriable functionality over here, but for now I'm just starting with the new functions
 * rather than adding to the interface I already want to fix.
 *
 * @author cpa
 */
public class AWController implements SimpleLinkListener {
    // hack for data mining stuff

    private static final boolean ENABLE_MINING = true; // a convience to allow me to turn off mining while I'm testing
    private static final boolean ENABLE_ENTITY_MINING = true;
    private static final boolean ENABLE_FULL_ENTITY_MINING = true;
    private static final String ENTITY_MINING_FILE = "ConceptGraph/nernerGraph.xml";
    private static final String ENTITY_MINING_FILE_FULL1 = "ConceptGraph/docLevelEntityvsDocs.txt";
    private static final String ENTITY_MINING_FILE_FULL2 = "ConceptGraph/docLevelConceptMap.txt";
    private static final String WEIGHTED_TERM_FILE = "weightedTermFile.txt";
    private static final String[] STORYTELLING_FILES = {
        "weightedTermFile.txt",
        "cpplattice.txt",
        "docIDFile.txt",
        "termIDFile.txt"
    };
    private static AWController _instance = new AWController();
    private WorkspacePane _workspace;
    //private final HandleJigSawFile _miner;
    private edu.vt.cs.shahriar.CoverTree.AllNew _surfer;
    private NeighborSearch _neighborFinder;
    private ConceptMapLoader _entityConnector;
    private edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI_NonXML.ConceptMapLoader _entityConnectorFull;
    private String _entityMiningPath;
    private Queriable _controller;
    private final Preferences _prefs;
    private LinkLabel _linkLabel;
    private Hashtable _weightedTerms;
    private Vector<String> _currentTerms = null;
    private boolean _miningEnabled = false;
    private boolean _entityConnections = false;
    private boolean _entityFullConnections = false;

    private AWController() {
        _prefs = Preferences.userNodeForPackage(AnalystsWorkspace.class);
    }

    /**
     * Get the singleton instance of the controller
     * @return the main controller
     */
    public static AWController getInstance() {
        return _instance;
    }

    /**
     * Set the workspace for the application. This is required so that controller can place objects into it.
     * 
     * @param workspace
     */
    public void setWorkspace(WorkspacePane workspace) {
        _workspace = workspace;

    }

    /**
     * This is a legacy method to pass the old controller in so the new controller can handle old code.
     * This method should go away once the transition is complete.
     * @param controller
     */
    public void setController(Queriable controller) {
        _controller = controller;
    }

    /**
     * Another legacy method to allow remote access to the old controller -- again, this should go away
     * @return 
     */
    public Queriable getController() {
        return _controller;
    }

    /**
     * This method loads a Jigsaw file into the system. It takes in a file object and
     * the creates a worker to do the real loading into lucene and the data miner.
     * @param file a path to a jigsaw file
     */
    public void loadJigFile(File file) {
        clearWorkspace();
        _prefs.put(AnalystsWorkspace.PREFS_DATA_PATH_KEY, file.getAbsolutePath());
        LoadJigsawWorker worker = new LoadJigsawWorker(file);
        List<SwingWorker> workers = new ArrayList<SwingWorker>(1);
        workers.add(worker);
        WorkerProgressDialog progress = new WorkerProgressDialog("Loading Jigsaw file", workers, (JFrame) _workspace.getTopLevelAncestor(), false);
        progress.setVisible(true);
    }

    /**
     * This uses the {@code DataManager} to load in the contents of a save database.
     * @param the database file to load
     */
    public void loadDatabase(File database) {
        // clear out the old stuff
        clearWorkspace();
        _prefs.put(AnalystsWorkspace.PREFS_DATA_PATH_KEY, database.getAbsolutePath());
        LoadDatabaseWorker worker = new LoadDatabaseWorker(database);
        List<SwingWorker> workers = new ArrayList<SwingWorker>(1);
        workers.add(worker);
        WorkerProgressDialog progress = new WorkerProgressDialog("Loading data file", workers, (JFrame) _workspace.getTopLevelAncestor(), false);
        progress.setVisible(true);

    }

    /**
     * This removes everything from the workspace so we can load in new data and start fresh. 
     */
    protected void clearWorkspace() {
        // remove all of the regular frames
        JInternalFrame[] frames = _workspace.getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
        for (JInternalFrame f : frames) {
            if (!(f instanceof SearchTool)) {
                _workspace.remove(f);
                f.dispose();
            }
        }
        // remove the notes
        frames = _workspace.getAllFramesInLayer(WorkspacePane.NOTE_LAYER);
        for (JInternalFrame f : frames) {
            if (!(f instanceof SearchTool)) {
                _workspace.remove(f);
                f.dispose();
            }
        }


        FileManager.reset();
        EntityManager.reset();
        _workspace.setDirty(false);
    }

    /**
     * This is a method for placing components on the display. This implements the 
     * nearby placement of dialogs and the like.
     * 
     * @param c the {@code Component} to be placed
     */
    public void place(Component c) {
        Point p = _workspace.getMousePosition();
        Rectangle bounds = c.getBounds();
        if (p == null) {
            // no mouse pointer, so try around the current note
            Rectangle tmpBounds;
            JInternalFrame currentFrame = _workspace.getSelectedFrame();
            if (currentFrame != null) {
                tmpBounds = currentFrame.getBounds();

            } else {
                // no current note either - so just put it dead center of the workspace
                tmpBounds = _workspace.getBounds();
            }
            p = new Point(tmpBounds.x + tmpBounds.width / 2, tmpBounds.y + tmpBounds.height / 2);
        }

        p.x = Math.max(0, p.x - bounds.width / 2);
        p.y = Math.max(0, p.y - bounds.height / 2);

        if (!(c instanceof JInternalFrame)) {
            SwingUtilities.convertPointToScreen(p, _workspace);
        }


        c.setLocation(p);
    }

    /**
     * This method displays a document in the space, optionally setting the focus when it is added.
     * 
     * @param doc the AWDocument to be opened
     * @param setFocus should the new frame get focus
     * @return the frame that has been added to the space
     */
    public AWInternalFrame displayFile(AWDocument doc, boolean setFocus) {
        AWInternalFrame frame = null;

        // check for other open copies and rename to be unique
        List<AWInternalFrame> frames = FileManager.getInstance().getOpenDocs(doc);
        String title = null;
        if (frames.size() > 0) {
            int index = 0;
            boolean done;
            do {
                done = true;
                if (index == 0) {
                    title = doc.getName();
                } else {
                    title = doc.getName() + " " + index;
                }
                for (AWInternalFrame f : frames) {
                    if (f.getTitle().equals(title)) {
                        done = false;
                        index++;
                        break;
                    }
                }
            } while (!done);
        }


        if (doc.getType().equals("image")) {
            frame = new AWImageFrame(doc, _controller);
        } else {
            try {
                frame = new AWTextFrame(doc, _controller);
                ((AWTextFrame) frame).setEditable(false);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                //e.printStackTrace();
            }
        }


        if (frame != null) {
            FileManager.getInstance().addFrame(frame);
            Monitor.getInstance().monitor(frame);
            if (title != null) {
                frame.setTitle(title);
            }
            _workspace.add(frame);

            // link to all other open copies
//            for (AWInternalFrame f : frames) {
//                SimpleLink link = AWController.getInstance().createLink(frame, f);
//                link.setColor(Color.CYAN);
//            }

            if (setFocus) {
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException e) {
                }
            }
        }

        doc.setSeen(true);
        return frame;
    }

    /**
     * This method loads a frame directly into the workspace and then manually sets the bounds. This is
     * intended to be used primarily for restoring the workspace when reloading from saved data.
     * @param frame
     * @param x
     * @param y
     * @param width
     * @param height
     * @param iconfied 
     */
    public void loadFrame(AWInternalFrame frame, int x, int y, int width, int height, boolean iconified) {

        if (frame instanceof AWNote) {
            _workspace.setLayer(frame, WorkspacePane.NOTE_LAYER);
        }

        _workspace.add(frame);

        frame.setBounds(x, y, width, height);
        try {
            frame.setIcon(iconified);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
        }
        frame.setController(_controller);
    }

    /**
     * Perform an "entity search" this is less of a search and more of a view of
     * all of the documents containing a particular entity. This may get changed
     * dramatically later if I alter the way entities behave.
     *
     * This now checks to see if the entity is already open and just selects it if it is.
     *
     * @param entity an {@code AWEntity} that we want to browse
     * @return the {@code AWEntity} that has been found or created
     */
    public AWEntityView displayEntity(AWEntity entity) {
        JInternalFrame[] frames = _workspace.getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
        for (JInternalFrame frame : frames) {
            if (frame instanceof AWEntityView
                    && ((AWEntityView) frame).getEntity() == entity) {
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException e) {
                }
                return (AWEntityView) frame;
            }
        }

        AWEntityView resultsPane = new AWEntityView(entity, _controller);
        _workspace.add(resultsPane);

        try {
            resultsPane.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }

        return resultsPane;
    }

    public void linkClosing(SimpleLink link) {
    }

    public void linkChanging(SimpleLink link) {
    }

    public void linkDeselected(SimpleLink link) {
        JInternalFrame[] frames = link.getFrames();
        if (_currentTerms != null
                && frames[0] instanceof AWTextFrame
                && frames[1] instanceof AWTextFrame) {
            for (String term : _currentTerms) {
                ((AWTextFrame) frames[0]).deHighlightTerm(term, true);
                ((AWTextFrame) frames[1]).deHighlightTerm(term, true);
            }
            _currentTerms = null;
        }
    }

    /**
     * When a link is selected, we want to put a label on it if appropriate
     * @param link
     */
    public void linkSelected(SimpleLink link) {
        if (_linkLabel == null) {
            _linkLabel = new LinkLabel();
            _workspace.setLayer(_linkLabel, WorkspacePane.DIALOG_LAYER);
            _workspace.add(_linkLabel);

        }


        JInternalFrame[] frames = link.getFrames();
        // if the link is between two text documents, show the connecting terms
        if (_miningEnabled && frames[0] instanceof AWTextFrame && frames[1] instanceof AWTextFrame) {
            String name0 = frames[0].getTitle().toLowerCase();
            String name1 = frames[1].getTitle().toLowerCase();
            System.out.println("finding term overlap");
            _currentTerms = edu.vt.cs.shahriar.SimilarityHandler.SimilarityFinder.getImportantOverlap(name0, name1, _weightedTerms);

            _linkLabel.showLabel(_currentTerms.toString(), link);

            for (String term : _currentTerms) {
                ((AWTextFrame) frames[0]).highlightTerm(term);
                ((AWTextFrame) frames[1]).highlightTerm(term);
            }

        } else if (link instanceof EntityLink) {
            _linkLabel.showLabel(((EntityLink) link).getLabel(), link);
        }
    }

    /**
     * This is a method to request textual input from the user. If the user returns
     * an empty response or cancels, this will return null
     * @param title {@code String} to be used as the title for the dialog box
     * @param message {@code String} to be used as the message for the dialog box
     * @return the text entered by the user or null
     */
    public String askForInput(String title, String message) {
        String response = (String) JOptionPane.showInputDialog(
                (JFrame) _workspace.getTopLevelAncestor(),
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        if (response.length() == 0) {
            response = null;
        }
        return response;
    }

    /**
     * This is a wrapper to ask the user a simple question requiring a response.The default option is the final one.
     *
     * @param message the message to display
     * @param title the title to put on the dialog
     * @param messageType the JOptionPane message type
     * @param optionType the JOPtionPane option type
     * @param options an array of options
     * @return the option chosen by the user
     */
    public Object askQuestion(String message, String title, int messageType, int optionType, Object[] options) {
        JOptionPane optionPane = new JOptionPane(message,
                messageType,
                optionType,
                null,
                options,
                options[options.length - 1]);

        JDialog dialog = optionPane.createDialog(_workspace, title);
        AWController.getInstance().place(dialog);
        dialog.setVisible(true);
        return optionPane.getValue();
    }


       /**
     * This is a wrapper method for showing informative or error messages in which no response beyond
     * an acknowledgment is required.
     *
     * @param message the message to display
     * @param title the title to put on the dialog
     * @param error a boolean indicating if this is an error (true) or just informative (false)
     */
    public void showMessage(String message, String title, boolean error) {
        JOptionPane optionPane;
        JDialog dialog;
        if (error) {
            optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        } else {
            optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        }
        dialog = optionPane.createDialog(_workspace, title);
        place(dialog);
        dialog.setVisible(true);
    }





    /**
     * Find the nearest neighbors of the selected document and display a list of the results.
     * This is slightly ugly because the data miner is using lowercase ids and I am using uppercase ones,
     * so I end up having to convert back and forth.
     * 
     * @param source
     */
    public void findNeighbors(AWInternalFrame source) {
        if (!_miningEnabled) {
            return;
        }
        AWDocument doc = source.getDocument();
        Vector neighborIDs = _neighborFinder.getkNN_V(10, doc.getName().toLowerCase());
        Vector<AWDocument> neighbors = new Vector<AWDocument>(neighborIDs.size());
        for (Object id : neighborIDs) {
            if (id instanceof String) {
                AWDocument document = FileManager.getInstance().getDocument((String) id);
                if (document != null) {
                    neighbors.add(document);
                } else { //handle Shariar's occassional case issues
                    document = FileManager.getInstance().getDocument(((String) id).toUpperCase());
                    if (document != null) {
                        neighbors.add(document);
                    } else {
                        showMessage("Unable to find file " + (String)id, "Unable to match neighbor", true);
                        System.out.println("unable to match " + id);
                    }
                }
            }
        }

        AWArbFileList frame = new AWArbFileList(neighbors, "Neighborhood", "Neighbors of " + doc.getName(), source, _controller);
        _workspace.add(frame);
    }

    /**
     * This method looks at all of the currently open documents, finds the local neighborhoods for all of them,
     * removes the already open documents and lists alternatives.
     */
    public void makeSuggestions() {
        JInternalFrame[] frames = _workspace.getAllFrames();
        Set<AWDocument> documents = new HashSet<AWDocument>(40);
        for (JInternalFrame frame : frames) {
            if (frame instanceof AWTextFrame) {
                AWDocument doc = ((AWTextFrame) frame).getDocument();
                Vector neighborIDs = _neighborFinder.getkNN_V(10, doc.getName().toLowerCase());
                for (Object id : neighborIDs) {
                    if (id instanceof String) {
                        AWDocument document = FileManager.getInstance().getDocument(((String) id).toUpperCase());
                        if (document != null) {
                            documents.add(document);
                        }
                    }

                }
            }
        }

        Vector<AWDocument> suggestions = new Vector<AWDocument>(documents.size());
        for (AWDocument document : documents) {
            if (FileManager.getInstance().getOpenDocs(document).isEmpty()) {
                suggestions.add(document);
            }
        }


        AWArbFileList frame = new AWArbFileList(suggestions, "Suggsetions", "Suggestions", null, _controller);
        _workspace.add(frame);
    }

    /**
     * Create a search results frame, populate it, add it to the workspace and return
     * the frame to the caller.
     *
     * @param query the search term that is being used
     * @param source the frame that originated the search (if there is one)
     * @return a {@code AWSearchResults} instance with the results
     */
    public AWSearchResults displaySearchResults(String query, AWInternalFrame source) {
        AWSearchResults resultsPane;

        if (source instanceof AWInternalFrame) {
            resultsPane = new AWSearchResults(query, (AWInternalFrame) source, _controller);
        } else {
            resultsPane = new AWSearchResults(query, null, _controller);
        }
        try {
            resultsPane.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
        }

        _workspace.add(resultsPane);

        return resultsPane;

    }

    public AWArbFileList displayFileList(Vector<AWDocument> documents, String title, String description, AWInternalFrame source) {
        AWArbFileList frame = new AWArbFileList(documents, title, description, source, _controller);
        _workspace.add(frame);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return frame;
    }

    /**
     * This method returns a frame matching the given frameID. If one cannot be found, null is returned.
     * @param frameID the id of the frame to be returned
     * @return 
     */
    public AWInternalFrame getFrame(int frameID) {
        if (frameID <= 0) {
            return null;
        }
        for (JInternalFrame frame : _workspace.getAllFrames()) {
            if (frame instanceof AWInternalFrame && ((AWInternalFrame) frame).getID() == frameID) {
                return (AWInternalFrame) frame;
            }
        }

        return null;
    }

    /**
     * Create a new file browser and add it to the workspace
     */
    private void createFileBrowser() {
        AWFileBrowser browser = new AWFileBrowser();
        browser.setController(_controller);
        _workspace.add(browser);
        place(browser);
        browser.setVisible(true);
        try {
            browser.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void linkTo(final AWDocumentView frame) {
        Point p = frame.getLocationOnScreen();
        p.x += frame.getWidth() / 2;
        p.y += frame.getHeight() / 2;

        _workspace.getSelectedLinkLayer().trackCursor(p,
                new SelectedLinkGraphicsPane.CompletionCall() {

                    public void done(Component sink) {
                        if (sink instanceof AWDocumentView) {
                            SimpleLink link = AWLinkManager.getInstance().createUserLink(frame, (AWInternalFrame) sink, Color.red);

                        }
                    }
                });
    }

    /**
     * This is the main method to invoke the storytelling methods. This allows the user to draw a link
     * between two components on the display. This then looks at the components to see what they are,
     * and if they match (two documents or two entities), it passes control to the appropriate mining method.
     * @param frame the frame that initiated the connection
     */
    public void findConnection(final AWInternalFrame frame) {
        if (!_miningEnabled) {
            return;
        }

        Point p;
        if (frame.isIcon()) {
            JDesktopIcon icon = frame.getDesktopIcon();
            p = icon.getLocationOnScreen();
            p.x += icon.getWidth() / 2;
            p.y += icon.getHeight() / 2;
        } else {
            p = frame.getLocationOnScreen();
            p.x += frame.getWidth() / 2;
            p.y += frame.getHeight() / 2;
        }

        _workspace.getSelectedLinkLayer().trackCursor(p,
                new SelectedLinkGraphicsPane.CompletionCall() {

                    public void done(Component sink) {
                        if (sink == frame) {
                            return; // can't find a path to the same object
                        }

                        if (sink instanceof JDesktopIcon) {
                            sink = ((JDesktopIcon) sink).getInternalFrame();
                        }

                        if (frame instanceof AWDocumentView && sink instanceof AWDocumentView) {
                            finishDocumentConnection((AWDocumentView) frame, (AWDocumentView) sink);
                        } else if (_entityConnections && frame instanceof AWEntityView && sink instanceof AWEntityView) {
                            finishEntityConnection((AWEntityView) frame, (AWEntityView) sink);
                        }
                    }
                });
    }

    /**
     * This method handles the document connections. While the mining results provides neighborhoods for additional exploration,
     * right now we just take the connecting documents. The resulting documents are added to the 
     * space along the line connecting the two documents.
     * 
     * @param source the document frame that initiated the connection
     * @param sink the destination document
     */
    private void finishDocumentConnection(AWDocumentView source, AWDocumentView sink) {

        _surfer.threshold = 0.98f;
        _surfer.clique_k = 3;

        // this returns a Vector of HashSets (in theory)
        Vector cliqueChain = _surfer.FindStoryOfAGivenPair(sink.getDocument().getName().toLowerCase(),
                source.getDocument().getName().toLowerCase());

        if (cliqueChain.isEmpty()) {
            showMessage("Unable to find a story", "Unable to find story", false);
            System.out.println("Unable to find story.");
            return;
        }
        // now we find the connecting documents from the cliques through set intersection
        ArrayList<AWDocument> story = new ArrayList<AWDocument>(cliqueChain.size());
        story.add(source.getDocument());
        for (int i = 0; i < cliqueChain.size() - 1; i++) {
            Set current = (Set) cliqueChain.get(i);
            Set next = (Set) cliqueChain.get(i + 1);
            current.retainAll(next); // intersection of the two sets should yeild up the "bridge" doc
            if (current.size() != 1) {
                System.out.println("clique overlap of :" + current.size());
            }
            AWDocument document;
            for (Object id : current) {
                try {
                    document = FileManager.getInstance().getDocument((String) id);
                    if (document != null) {
                        story.add(document);
                    } else {

                        document = FileManager.getInstance().getDocument(((String) id).toUpperCase());
                    if (document != null) {
                        story.add(document);
                    } else {
                        showMessage("Unable to find file " + (String)id, "Unable to complete story", true);
                        System.out.println("unable to match " + id);
                        }
                    }
                } catch (ClassCastException ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        story.add(sink.getDocument());


        // now we need to lay these documents out in the space along the line connecting the source and the sink
        AWInternalFrame last = null;
        AWInternalFrame current = null;
        SimpleLink link = null;
        int index = 1;
        int span = story.size() - 1;
        Point newLocation = new Point(0, 0);

        // find the middle of the source and sink documents
        Point sourceMid = source.getLocation();
        Point sinkMid = sink.getLocation();
        sourceMid.x += source.getWidth() / 2;
        sourceMid.y += source.getHeight() / 2;
        sinkMid.x += sink.getWidth() / 2;
        sinkMid.y += sink.getHeight() / 2;


        _workspace.setAutoPlace(false);
        for (AWDocument doc : story) {
            if (doc == source.getDocument()) {
                current = source;
            } else if (doc == sink.getDocument()) {
                current = sink;
            } else {
                // create new frame and add it to the workspace
                current = this.displayFile(doc, true);

                // calculate where it should be placed along the line using interpolation
                float factor = (float) index / (float) span;

                newLocation.x = Math.round(factor * sinkMid.x
                        + (1 - factor) * sourceMid.x
                        - (current.getWidth() / 2));

                newLocation.y = Math.round(factor * sinkMid.y
                        + (1 - factor) * sourceMid.y
                        - (current.getHeight() / 2));


                current.setLocation(newLocation);
                index += 1;
            }
            // link the current frame to the last one that we placed
            if (last != null) {
                link = AWLinkManager.getInstance().createUserLink(last, current, Color.red);
            }
            last = current;
        }
        _workspace.setAutoPlace(true);
    }

    /**
     * This method handles the display of entity-level storytelling.
     * 
     * @param source
     * @param sink 
     */
    private void finishEntityConnection(AWEntityView source, AWEntityView sink) {
        boolean simple = false;
        ArrayList<AWEntity> entities;
        HashMap<AWEntity, String> sentences = null;
        Vector path = _entityConnector.getStoryBetweenTwoEntities(source.getEntity().getValue(), sink.getEntity().getValue());


        if (path != null) {
            // first extract the path through the entities
            entities = new ArrayList<AWEntity>(path.size());
            sentences = new HashMap<AWEntity, String>(path.size());
            AWEntity currentEntity = source.getEntity();
            entities.add(currentEntity);
            for (EdgeWithInfo edge : (Vector<EdgeWithInfo>) path) {
                if (edge.node1.equalsIgnoreCase(currentEntity.getValue())) {
                    currentEntity = EntityManager.getInstance().getEntity(edge.node2);
                } else {
                    currentEntity = EntityManager.getInstance().getEntity(edge.node1);
                }
                //System.out.println(currentEntity);
                entities.add(currentEntity);

                if (!simple) {
                    StringBuilder buffer = new StringBuilder();
                    for (ADocument doc : (Vector<ADocument>) edge.documents) {
                        for (String sentence : (Vector<String>) doc.sentencesV) {
                            buffer.append(sentence);
                            buffer.append("<br />");
                        }
                    }
                    sentences.put(currentEntity, buffer.toString());
                }


            }
        } else if (_entityFullConnections) {
            System.out.println("Falling back to document level connections");
            path = _entityConnectorFull.getStoryBetweenTwoEntities(source.getEntity().getValue(), sink.getEntity().getValue());
            if (path == null) {
                showMessage("Unable to connect entities", "Unable to connect entities", false);
                System.out.println("unable to make entity connection");
                return;
            }
            entities = new ArrayList<AWEntity>(path.size());
            AWEntity currentEntity = source.getEntity();
            for (EdgeWithDocs edge : (Vector<EdgeWithDocs>) path) {
                if (edge.node1.equalsIgnoreCase(currentEntity.getValue())) {
                    currentEntity = EntityManager.getInstance().getEntity(edge.node2);
                } else {
                    currentEntity = EntityManager.getInstance().getEntity(edge.node1);
                }
                //System.out.println(currentEntity);
                entities.add(currentEntity);


            }

        } else {
            showMessage("Unable to connect entities", "Unable to connect entities", false);
                System.out.println("unable to make entity connection");
            return;
        }

        // now we lay out the entities along a path between the source and the sink
        // unlike the documents, we will leave existing entities in place
        // find the middle of the source and sink documents

        AWEntityView last = null;
        AWEntityView current = null;
        EntityLink link = null;
        int index = 0;
        int span = path.size();

        Point newLocation = new Point(0, 0);

        Point sourceMid = source.getLocation();
        Point sinkMid = sink.getLocation();
        sourceMid.x += source.getWidth() / 2;
        sourceMid.y += source.getHeight() / 2;
        sinkMid.x += sink.getWidth() / 2;
        sinkMid.y += sink.getHeight() / 2;


        _workspace.setAutoPlace(false);
        for (AWEntity entity : entities) {
            if (entity == source.getEntity()) {
                current = source;
            } else if (entity == sink.getEntity()) {
                current = sink;
            } else {
                // create new frame and add it to the workspace

                // check to see if it is already out there
                JInternalFrame[] frames = _workspace.getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
                current = null;
                for (JInternalFrame frame : frames) {
                    if (frame instanceof AWEntityView
                            && ((AWEntityView) frame).getEntity() == entity) {
                        current = (AWEntityView) frame;
                    }
                }
                if (current == null) {
                    current = new AWEntityView(entity, _controller);
                    _workspace.add(current);
                }

                // iconify since we will initially just care about the entity not the documents
                try {
                    current.setIcon(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                }

                // calculate where it should be placed along the line using interpolation
                // note that since we iconify the entities, we need to use the icon bounds
                float factor = (float) index / (float) span;

                newLocation.x = Math.round(factor * sinkMid.x
                        + (1 - factor) * sourceMid.x
                        - (current.getDesktopIcon().getWidth() / 2));

                newLocation.y = Math.round(factor * sinkMid.y
                        + (1 - factor) * sourceMid.y
                        - (current.getDesktopIcon().getHeight() / 2));


                current.setLocation(newLocation);
                current.getDesktopIcon().setLocation(newLocation);

            }
            index += 1;

            // the links are created automatically, we just want to update it with a little more information
            if (last != null) {
                link = last.getEntityLink(current);
                if (link != null) {
                    if (sentences != null) {
                        link.setLabel(sentences.get(current.getEntity()));
                    }
                    link.setKeepVisible(true);
                }
            }
            last = current;
        }
        _workspace.setAutoPlace(true);
        _workspace.repaint();


    }

    /**
     * This method is used when the user need to select an entity for some purpose.
     * This provides the user with a modal dialog box asking for selection of an
     * entity. The user can select one or select cancel. This method will return
     * the selected entity or null, if the user canceled.
     * @param query Text to provide to the use in the dialog
     * @param initialType the entity type to start with (null if no default)
     *
     * @return the selected entity
     */
    private AWEntity selectEntity(String query, String initialType) {
        AWEntity entity = null;
        EntitySelectionDialog dialog = new EntitySelectionDialog((JFrame) _workspace.getTopLevelAncestor(), "Select an entity", true, query, initialType);
        place(dialog);
        dialog.setVisible(true);
        entity = dialog.getValue();
        return entity;
    }

    public AWEntity createEntity(String text) {
        AWEntity entity = new AWEntity(null, text);
        EntityEditor editor = new EntityEditor((JFrame) _workspace.getTopLevelAncestor(), entity, true);
        place(editor);
        editor.setVisible(true);
        return entity;
    }

    /**
     * This method is called when the user has selected an entity to be removed. We need to ask if 
     * the entity should be removed universally, or just from this document. Optionally, if this
     * is called with null values for the frame and range, the entity will be removed universally.
     * 
     * @param entity the {@code AWEntity} to be removed
     * @param frame the source frame
     * @param range the {@code Range} in the source text in case we want to remove just a single instance
     */
    public void removeEntity(AWEntity entity, AWTextFrame frame, Range range) {
        Object[] options = {"Cancel", "Remove Current", "Remove All"};
        Object response;
        if (frame == null || range == null) {
            // in this case the request did not come from a document, so we just want to delete it
            response = options[2];
        } else {
            response = askQuestion("Do you wish to remove ALL instances of  " + entity.getValue() + ",\n"
                    + "or would you like to remove just the CURRENT instance?",
                    "Removing entity",
                    JOptionPane.WARNING_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    options);
        }
        if (response == options[1]) {
            frame.getDocument().removeEntityFromRange(range);
        } else if (response == options[2]) {
            EntityManager.getInstance().removeEntity(entity);
            if (_miningEnabled) {
                _entityConnector.deleteEntityFromConceptGraph(entity.getValue());
                _entityConnector.closeSession();
                _entityConnector = new ConceptMapLoader(_entityMiningPath);

            }
        } else {
            // do nothing - canceled
        }
    }

    public void markEntity(String text, AWTextFrame frame, Range range) {
        AWEntity entity = null;
        Object[] options = {"Cancel", "Use Existing", "Make New"};
        Object response = askQuestion("Do you wish to make <" + text + "> a new entity,\n"
                + "or associate it with an existing entity?",
                "Marking entity",
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_CANCEL_OPTION,
                options);

        if (response == options[1]) {
            // show list of entities to select from
            entity = selectEntity("Select an entity to associate with <" + text + ">", null);
            if (entity != null) {
                frame.getDocument().addEntity(entity, range);
            }
        } else if (response == options[2]) {
            // show entity creation dialog
            entity = createEntity(text);
            if (entity.getType() == null) { // user canceled - bail out
                return;
            }

            // add entity to the system
            EntityManager.getInstance().addEntity(entity);

            // notify documents about the new entity
            Vector<AWDocument> docs = _controller.performSearch("\"" + entity.getValue() + "\"");
            for (AWDocument doc : docs) {
                doc.addEntity(entity);
            }

            // if the name is different from the text used to create it - update the document as well
            if (!entity.getValue().equals(text)) {
                frame.getDocument().addEntity(entity, range);
            }
        } else {
            // do nothing - canceled
        }
    }

    public void makeAlias(AWEntity entity) {
        // request an entity to associate this with
        AWEntity masterEntity = selectEntity("Use <" + entity.getValue() + "> as alias for:", entity.getType());
        if (masterEntity != null) {
            masterEntity.addAlias(entity);
        }
        if (_miningEnabled) {
            _entityConnector.mergeEntities(masterEntity.getValue(), entity.getValue());
            _entityConnector.closeSession();
            _entityConnector = new ConceptMapLoader(_entityMiningPath);

        }
    }

    public boolean selectEntity(AWEntity entity) {
        JInternalFrame[] frames = _workspace.getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
        for (JInternalFrame frame : frames) {
            if (frame instanceof AWEntityView
                    && ((AWEntityView) frame).getEntity() == entity) {
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException e) {
                }
                return true;
            }
        }
        return false;
    }

    public void detectEntities() {
        EntityManager.reset();
        List<AWDocument> documents = FileManager.getInstance().getDocuments();
        List<SwingWorker> workers;
        EntityFinder finder;

        if (documents.size() < 30) {
            workers = new ArrayList<SwingWorker>(1);
            finder = new EntityFinder(documents, 0);
            workers.add(finder);
        } else {


            int division = documents.size() / 10; // number of docs for each thread -- experimenting with this value
            int start = 0;
            int end;

            workers = new ArrayList<SwingWorker>(documents.size() / division + 1);

            int count = 0;
            while (start < documents.size()) {
                end = start + division;
                if (end > documents.size()) {
                    end = documents.size();
                }
                finder = new EntityFinder(documents.subList(start, end), count);
                workers.add(finder);
                start = end;
                count++;
            }
        }
        //EntityFinder finder = new EntityFinder(documents, 2);
        // workers.add(finder);

        WorkerProgressDialog progress = new WorkerProgressDialog("Finding entities", workers, (JFrame) _workspace.getTopLevelAncestor(), true);
        place(progress);
        progress.setVisible(true);
    }

    private class EntityFinder extends SwingWorker<Boolean, Object[]> {

        private List<AWDocument> _docs;
        int _id;

        public EntityFinder(List<AWDocument> documents, int id) {
            _docs = documents;
            _id = id;
            //System.out.println("Creating " + _id);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            EntityExtractor extractor = new EntityExtractor();
            extractor.loadKnownAltRecognizers();


            // System.out.println(_id + " starting");
            int count = 0;
            for (AWDocument doc : _docs) {

                List<Entity> entities = extractor.extractEntities(doc.getText());
                Object[] data = {doc, entities};
                publish(data);

                count++;
                setProgress((100 * count) / _docs.size());

            }
            //System.out.println(_id + " done");
            setProgress(100);
            return Boolean.TRUE;
        }

        @Override
        protected void process(List<Object[]> data) {
            AWEntity newEntity;
            for (Object[] entry : data) {
                AWDocument doc = (AWDocument) entry[0];
                List<Entity> entities = (List<Entity>) entry[1];
                for (Entity entity : entities) {
                    newEntity = EntityManager.getInstance().getEntity(entity.getType().toLowerCase(), entity.getText());
                    for (Entity.EntityRange range : entity.getOccurrances()) {
                        doc.addEntity(newEntity, new Range(range.getStart(), range.getEnd()));
                    }
                }
            }
        }
    }

    /**
     * This class performs the main loading task of reading in the documents from a jigsaw file, inserting them into lucene and
     * loading them into the data miner. It uses the SwingWorker to keep everything running in the GUI since the
     * data miner can take a little bit to run.
     */
    private class LoadJigsawWorker extends SwingWorker<Boolean, Boolean> {

        private final File _file;

        public LoadJigsawWorker(File file) {
            _file = file;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            Vector<AWDocument> docs = JigParser.parseJigFile(_file);
            FileManager.getInstance().addDocuments(docs);
            try {
                IndexManager.buildIndex(docs);
            } catch (IOException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
                return Boolean.FALSE;
            }
            setProgress(40);
            publish(Boolean.TRUE);
            String directory = _file.getParent();


            _miningEnabled = ENABLE_MINING && (new File(directory)).exists();

            for (String file : STORYTELLING_FILES) {
                if (!(new File(directory + File.separator + file)).exists()) {
                    _miningEnabled = false;
                    System.out.println("Cannot find " + file + " disabling storytelling");
                }
            }
            if (_miningEnabled) {
                _surfer = new edu.vt.cs.shahriar.CoverTree.AllNew(true);

                _surfer.dir = directory;
                _neighborFinder = new NeighborSearch();
                _neighborFinder.loadNeighbourhoodInfo(directory + File.separator + WEIGHTED_TERM_FILE);

                try {
                    _surfer.doAllinits();
                } catch (IOException ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                    _miningEnabled = false;
                } catch (Exception ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                    _miningEnabled = false;
                }

                _weightedTerms = (Hashtable) edu.vt.cs.shahriar.Modeller.AbstractHelper.getDocTermTFIDFHT(directory + File.separator + WEIGHTED_TERM_FILE);

            }

            setProgress(60);
            System.out.println();

            //setup the entity connections
            _entityConnections = _miningEnabled && ENABLE_ENTITY_MINING;
            if (_entityConnections) {
                _entityMiningPath = directory + File.separator + ENTITY_MINING_FILE;
                _entityConnections &= (new File(_entityMiningPath).exists());
                if (!_entityConnections) {
                    System.out.println("Cannot find " + ENTITY_MINING_FILE + ", disabling entity connections");
                } else {
                    _entityConnector = new ConceptMapLoader(_entityMiningPath);

                    setProgress(75);

                    // check for the files for the full entity connections
                    _entityFullConnections = ENABLE_FULL_ENTITY_MINING & _entityConnections;
                    if (_entityFullConnections && !(new File(directory + File.separator + ENTITY_MINING_FILE_FULL1)).exists()) {
                        System.out.println("Cannot find + " + ENTITY_MINING_FILE_FULL1 + ", disabling full entity connections");
                        _entityFullConnections = false;
                    }

                    _entityFullConnections = ENABLE_FULL_ENTITY_MINING & _entityConnections;
                    if (_entityFullConnections && !(new File(directory + File.separator + ENTITY_MINING_FILE_FULL2)).exists()) {
                        System.out.println("Cannot find + " + ENTITY_MINING_FILE_FULL2 + ", disabling full entity connections");
                        _entityFullConnections = false;
                    }

                    if (_entityFullConnections) {
                        _entityConnectorFull = new edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI_NonXML.ConceptMapLoader(directory + File.separator + ENTITY_MINING_FILE_FULL1, directory + File.separator + ENTITY_MINING_FILE_FULL2);
                    }



                }

            }



            System.out.println(); // clear the console
            setProgress(100);
            return Boolean.TRUE;
        }

        @Override
        protected void process(List<Boolean> valid) {
            if (valid.get(0)) {
                createFileBrowser();
            }
        }

        @Override
        protected void done() {
        }
    }

    /**
     * This class handles all of the data import for database files.
     */
    private class LoadDatabaseWorker extends SwingWorker<Boolean, Boolean> {

        private final File _file;

        public LoadDatabaseWorker(File file) {
            _file = file;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            setProgress(5);
            AWDataManager.getInstance().loadData(_file);
            setProgress(25);
            try {
                IndexManager.buildIndex(FileManager.getInstance().getDocuments());
            } catch (IOException ex) {
                Logger.getLogger(AnalystsWorkspace.class.getName()).log(Level.SEVERE, null, ex);
            }
            setProgress(25);
            publish(Boolean.TRUE);

            String directory = _file.getParent();

            // check if we have all of the file for mining and setup the miner
            _miningEnabled = ENABLE_MINING && (new File(directory)).exists();

            for (String file : STORYTELLING_FILES) {
                if (!(new File(directory + File.separator + file)).exists()) {
                    _miningEnabled = false;
                    System.out.println("Cannot find " + file + " disabling storytelling");
                }
            }
            if (_miningEnabled) {
                _surfer = new edu.vt.cs.shahriar.CoverTree.AllNew(true);

                _surfer.dir = directory;
                _neighborFinder = new NeighborSearch();
                _neighborFinder.loadNeighbourhoodInfo(directory + File.separator + WEIGHTED_TERM_FILE);

                try {
                    _surfer.doAllinits();
                } catch (IOException ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                    _miningEnabled = false;
                } catch (Exception ex) {
                    Logger.getLogger(AWController.class.getName()).log(Level.SEVERE, null, ex);
                    _miningEnabled = false;
                }

                _weightedTerms = (Hashtable) edu.vt.cs.shahriar.Modeller.AbstractHelper.getDocTermTFIDFHT(directory + File.separator + WEIGHTED_TERM_FILE);

            }

            setProgress(60);
            System.out.println();

            //setup the entity connections
            _entityConnections = _miningEnabled && ENABLE_ENTITY_MINING;
            if (_entityConnections) {
                _entityMiningPath = directory + File.separator + ENTITY_MINING_FILE;
                _entityConnections &= (new File(_entityMiningPath).exists());
                if (!_entityConnections) {
                    System.out.println("Cannot find " + ENTITY_MINING_FILE + ", disabling entity connections");
                } else {
                    _entityConnector = new ConceptMapLoader(_entityMiningPath);

                    setProgress(75);

                    // check for the files for the full entity connections
                    _entityFullConnections = ENABLE_FULL_ENTITY_MINING & _entityConnections;
                    if (_entityFullConnections && !(new File(directory + File.separator + ENTITY_MINING_FILE_FULL1)).exists()) {
                        System.out.println("Cannot find + " + ENTITY_MINING_FILE_FULL1 + ", disabling full entity connections");
                        _entityFullConnections = false;
                    }

                    _entityFullConnections = ENABLE_FULL_ENTITY_MINING & _entityConnections;
                    if (_entityFullConnections && !(new File(directory + File.separator + ENTITY_MINING_FILE_FULL2)).exists()) {
                        System.out.println("Cannot find + " + ENTITY_MINING_FILE_FULL2 + ", disabling full entity connections");
                        _entityFullConnections = false;
                    }

                    if (_entityFullConnections) {
                        _entityConnectorFull = new edu.vt.cs.shahriar.VAST11.Processor.ConceptMapAPI_NonXML.ConceptMapLoader(directory + File.separator + ENTITY_MINING_FILE_FULL1, directory + File.separator + ENTITY_MINING_FILE_FULL2);
                    }



                }

            }


            System.out.println(); // clear the console
            setProgress(100);
            return Boolean.TRUE;
        }

        @Override
        protected void process(List<Boolean> valid) {
            if (valid.get(0)) {
                int count = AWDataManager.getInstance().loadFrames();
                if (count == 0) { // i.e., there are no frames
                    createFileBrowser();
                }
            }
        }

        @Override
        protected void done() {
        }
    }
}
