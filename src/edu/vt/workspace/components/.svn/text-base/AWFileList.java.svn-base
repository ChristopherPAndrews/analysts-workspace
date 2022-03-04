package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.AWDocumentComparator;
import edu.vt.workspace.components.utilities.FileListCellRenderer;
import edu.vt.workspace.components.utilities.LayoutManager;
import edu.vt.workspace.components.utilities.SortedListModel;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.AWLinkManager;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.FileManager;
import edu.vt.workspace.data.LinkTarget;
import java.awt.event.KeyEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;

/**
 * This class is a actually a general purpose listing of files with a preview window,
 * _links to the documents, and coloring based on the file state. It is currently used
 * to represent search results and entity file lists.
 *
 *
 * Created: Sun Mar  1 15:27:20 2009
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public abstract class AWFileList extends AWInternalFrame implements AWSavable, LinkTarget {

    protected JList _list;
    protected Collection<AWDocument> _documents;
    private JComponent _source;
    private String _highlightTerm = null;
    private JLabel _queryLabel;
    private boolean _listInitialized = false;
    private boolean _hiddenUpdate = false;
    private ArrayList<AWDocument> _selectedItems = new ArrayList<AWDocument>();

    /**
     * An empty constructor for restoring from save files.
     *
     * This constructor is intended to be used by the save mechanism to recreate
     * an old frame. This must be followed by a call to reinitialize() before the
     * frame is fully valid.
     */
    public AWFileList() {
        super("", null);
        buildInterface();
    }

    public AWFileList(String title, JComponent source, Queriable controller) {
        super(title, controller);
        _source = source;
        buildInterface();
    }

    /**
     * This method writes out the data that is saved by this frame.
     *
     * @param writer the AWSwriter to be used to write the data to the save file.
     */
    @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);

    }

    public Collection<AWDocument> getTargetDocuments() {
        return _documents;
    }

    public Collection<AWEntity> getTargetEntities() {
        return null;
    }

    public LinkTarget getLinkSource() {
        return (LinkTarget) _source;
    }

    /**
     * This is the method that sets up the graphical look of the frame.
     */
    protected void buildInterface() {
        _list = new JList();
        AWDocumentComparator comparator = new AWDocumentComparator(AWDocumentComparator.SortType.DATE);
        ListModel model = _list.getModel();
        _list.setModel(new SortedListModel(model, comparator));

        JScrollPane scrollPane = new JScrollPane(_list);

        _queryLabel = new JLabel();
        _queryLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        _queryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _queryLabel.setOpaque(true);
        _queryLabel.setBackground(Color.WHITE);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().add(_queryLabel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // turn this off for the time being until I work out what to do with the preview pane
        setMaximizable(false);
        Dimension minimumSize = new Dimension(0, 0);
        scrollPane.setMinimumSize(minimumSize);


        _list.setCellRenderer(new FileListCellRenderer(_list));

        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                if (_highlightTerm != null) {
                    List<AWInternalFrame> frames;
                    AWDocument doc;
                    for (int i = 0; i < _list.getModel().getSize(); i++) {
                        doc = (AWDocument) _list.getModel().getElementAt(i);
                        frames = FileManager.getInstance().getOpenDocs(doc);
                        for (AWInternalFrame frame : frames) {
                            ((AWTextFrame) frame).highlightTerm(_highlightTerm);
                        }
                    }
                }
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                if (_highlightTerm != null) {
                    List<AWInternalFrame> frames;
                    AWDocument doc;
                    for (int i = 0; i < _list.getModel().getSize(); i++) {
                        doc = (AWDocument) _list.getModel().getElementAt(i);
                        frames = FileManager.getInstance().getOpenDocs(doc);
                        for (AWInternalFrame frame : frames) {
                            ((AWTextFrame) frame).deHighlightTerm(_highlightTerm, false);
                        }
                    }
                }
            }
        });

    }

    /**
     * For use by the sub classes to set the content of the label
     * @param text the text of the label
     * @param icon an icon (if one is desired)
     */
    protected void setLabel(String text, Icon icon) {
        _queryLabel.setText(text);
        if (icon != null) {
            _queryLabel.setIcon(icon);
        }
    }

    protected JLabel getLabel() {
        return _queryLabel;
    }

    /**
     * This sets the highlight term - the term that will be highlighted in documents linked to this file
     * _list. Currently, this is only relevant for search results.
     * @param term the term to highlight in documents
     */
    protected void setHighlightTerm(String term) {
        _highlightTerm = term;
    }

    /**
     * This method is called automatically when the frame has been placed in the workspace.
     *
     * We use this to perform any setup for which we want to make sure the rest of the GUI
     * context is available.
     */
    @Override
    protected void postOpenInit() {
        // we are waiting to initialize the _list to make sure all docs are present
        // before we do the population
        if (!_listInitialized) {
            initializeList();
        }

        super.postOpenInit();
        // set up all of the various listeners now that the frame has been placed

        // monitor selection state for preview and _links
        _list.addListSelectionListener(new SelectionHelper());

        // allow documents to be opened
        _list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int index = _list.locationToIndex(me.getPoint());
                    AWDocument doc = (AWDocument) (_list.getModel().getElementAt(index));
                    AWInternalFrame frame = AWController.getInstance().displayFile(doc, true);
                }
            }
        });


        _list.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int[] selected = _list.getSelectedIndices();
                    if (selected.length > 0) {
                        LayoutManager.getInstance().clearSelected();
                    }
                    for (int i = 0; i < selected.length; i++) {
                        AWDocument doc = (AWDocument) (_list.getModel().getElementAt(selected[i]));
                        AWInternalFrame frame = AWController.getInstance().displayFile(doc, false);
                        LayoutManager.getInstance().addFrameToSelection(frame);
                    }
                }
            }
        });

    }

    /**
     * This method is a placeholder that should be overridden by subclasses.
     * This is the opportunity to load content into the _list.
     * @return boolean to indicate if _list is actually loaded
     */
    protected abstract boolean loadList();

    protected void initializeList() {

        _listInitialized = loadList();
        if (_listInitialized) {
            ListModel model = _list.getModel();
            if (model.getSize() > 0) {
                _list.setPrototypeCellValue(model.getElementAt(0));
            }

            if (model instanceof SortedListModel) {
                ((SortedListModel) model).sort();
            }
            pack();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(450, size.width);
        size.height = 70 + _list.getFixedCellHeight() * _list.getModel().getSize();

        if (getParent() != null) {
            Rectangle parentBounds = getParent().getBounds();
            size.height = size.height > parentBounds.height + 25 ? parentBounds.height - 25 : size.height;
            size.height = size.height > 1000 ? 1000 : size.height;
        }
        return size;
    }

    /**
     * This method returns the desired maximum size of the frame.
     *
     * This is calculated based on the length of the _list.
     * @return a Rectangle describing the desired maximum rect for the frame
     */
    @Override
    public Rectangle getMaximizedBounds() {
        Rectangle bounds = getBounds();
        Dimension currentList = _list.getParent().getSize();
        Rectangle parentBounds = getParent().getBounds();

        // tell the _list we want to see everything
        _list.setVisibleRowCount(_list.getModel().getSize());

        // convert parent to parent's own coordinate system
        parentBounds.x = 0;
        parentBounds.y = 0;
        bounds.height = _list.getPreferredScrollableViewportSize().height + bounds.height - currentList.height;
        // too big to fit in the parent at max _size
        if (bounds.height > parentBounds.height) {
            // just fill height of parent
            bounds.y = parentBounds.y;
            bounds.height = parentBounds.height;
        } else if (bounds.y + bounds.height
                > parentBounds.y + parentBounds.height) {
            // did the bottom extend past the bottom of the enclosing window?
            // then move up to fit
            bounds.y = parentBounds.y + parentBounds.height - bounds.height;
        }

        return bounds;
    }

    public void updateSelectedDocuments() {
        _selectedItems.clear();
        int[] selectedIndicies = _list.getSelectedIndices();

        for (int i : selectedIndicies) {
            _selectedItems.add((AWDocument) _list.getModel().getElementAt(i));
        }

        AWLinkManager.getInstance().highlightLinks(_selectedItems);
    }

    public void setSelection(Collection<AWDocument> documents) {
        _list.clearSelection();
        ListModel model = _list.getModel();
        int[] indices = new int[documents.size()];
        int index = 0;

        for (int i = 0; i < model.getSize(); i++) {
            if (documents.contains((AWDocument) model.getElementAt(i))) {
                indices[index++] = i;
            }
        }
        _hiddenUpdate = true;
        _list.setSelectedIndices(indices);

    }

    public ArrayList<Rectangle> getDocumentLocation(AWDocument document) {
        for (int i = 0; i < _list.getModel().getSize(); i++) {
            if (document == (AWDocument) _list.getModel().getElementAt(i)) {
                Rectangle rect = _list.getCellBounds(i, i);

  
                if (SwingUtilities.convertPoint(_list, rect.getLocation(), _list.getParent()).y < 0) { // off the top
                    rect.height = -1;
                    rect.x = getX() + getWidth() / 2;

                    rect.y = SwingUtilities.convertPoint(_list.getParent(), _list.getParent().getLocation(), getParent()).y;
                } else if (SwingUtilities.convertPoint(_list, rect.getLocation(), this).y+ rect.height > getHeight()) { // off the bottom
                    rect.height = -2;
                    rect.x = getX() + getWidth() / 2;
                    rect.y = getY() + getHeight() - 5;
                } else {
                    rect.setLocation(SwingUtilities.convertPoint(_list, rect.getLocation(), getParent()));
                }

                ArrayList<Rectangle> locations = new ArrayList<Rectangle>(1);
                locations.add(rect);
                return locations;

            }
        }


        return null;
    }

    /**
     * This allows outside classes to tap in and receive scroll events.
     * @param listener 
     */
    public void addAdjustmentListener(AdjustmentListener listener) {
        if (_list.getParent().getParent() instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) _list.getParent().getParent();

            scroll.getVerticalScrollBar().addAdjustmentListener(listener);

        }
    }

    /**
     * This class provides support for selections in the AWFileList lists.
     *
     * The main purpose of this helper is to maintain several of the GUI states
     * that are affected by changes in the selection. These currently include:
     * - setting the link color to the currently selected _doc
     * - displaying the popup preview window
     */
    private class SelectionHelper implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent lse) {
            AWDocument doc;

            JList list = (JList) lse.getSource();

            updateSelectedDocuments();

            // preview text if we've only selected a single item
            if (_selectedItems.size() == 1 && !_hiddenUpdate) {
                // only one frame selected, show it
                doc = (AWDocument) list.getSelectedValue();
                //doc = (AWDocument) (list.getModel().getElementAt(selectedIndex));
                if (_highlightTerm != null) {
                    AWPopupTextWindow.getInstance().showDocument(doc, _highlightTerm, AWFileList.this);
                } else {
                    AWPopupTextWindow.getInstance().showDocument(doc, null, AWFileList.this);
                }
            } else {
                AWPopupTextWindow.getInstance().showDocument(null, null, AWFileList.this);
            }
            _hiddenUpdate = false;
        }
    }
}
