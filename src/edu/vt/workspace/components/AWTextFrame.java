package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.TextFrameMenuHelper;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.Range;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.View;

/**
 * This is the main document view class.
 *
 *
 * Created: Thu Feb 19 15:09:47 2009
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class AWTextFrame extends AWDocumentView implements AWSavable {
// this is the loose target width and height for _text documents

    private static final int DEFAULT_DIMENSION = 500;
    private AWTextPane _text;
    private JScrollPane _scrollArea;
    private Helper _helper;


    /**
     *
     * @param doc
     * @param controller
     */
    public AWTextFrame(AWDocument doc, Queriable controller) {
        super(doc, controller);
        this._doc = doc;
        setupContents();
        loadDocument();

        setTitle(_doc.getName());

        // now that the _text has been added, pack this up
        pack();

    }


    private void loadDocument() {
        desktopIcon.updateUI();
        _text.setText(_doc.getText());

        Map<Range, AWEntity> entities = _doc.getEntityRanges();

        for (Range range: entities.keySet()){
            AWEntity entity = entities.get(range);
            _text.highlightRange(range, entity);
        }


        _doc.addHighlightListener(_text);
        for (Range highlight : _doc.getHighlights()) {
            _text.highlightRange(highlight);
        }
       
    }

    @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);
        if (_doc != null) {
            writer.write("docID", _doc.getId());
        } else {
            writer.write("text", _text.getText());
        }
        writer.write("editable", _text.isEditable());
    }

   

    protected void setupContents() {
        _helper = new Helper();
        setIconifiable(true);
        //create the _text area
        _text = new AWTextPane();

        _scrollArea = new JScrollPane(_text);
        _text.setDragEnabled(true); // allows the user to drag _text
        _text.setToolTipText("");
        _scrollArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        _scrollArea.getViewport().setSize(new Dimension(500, 500));
        getContentPane().add(_scrollArea, BorderLayout.CENTER);


        _text.addMouseListener(_helper);
        _text.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent ke) {
            }

            public void keyPressed(KeyEvent ke) {
            }

            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_DELETE || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    Range highlight = _text.getSelectedHighlight();
                    if (highlight != null && _doc != null) {
                        _doc.removeHighlight(highlight);
                    }
                }
            }
        });

        //  _text.setTransferHandler(_transferHandler);

//       
    }

    @Override
    public void reinitialize(Queriable controller) {
        super.reinitialize(controller);
        _text.addMouseListener(_helper);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        Dimension currentTextSize = _text.getSize();

        if (currentTextSize.width == 0 && currentTextSize.height == 0) {
            // this hasn't been laid out yet, so we need to calculate a reasonable height for the _text and substitute it in
            Insets i = _text.getInsets();
            Dimension preferredTextSize = _text.getPreferredSize();
            int newHeight;
            int wPadding = size.width - preferredTextSize.width + i.right + i.left; // scrollbar, border, insets, etc.
            View view = _text.getUI().getRootView(_text);

            // if the _text fits comfortably in a slightly wider frame, let it.
            // otherwise use the default dimension
            size.width = (size.width < DEFAULT_DIMENSION * 1.5) ? size.width : DEFAULT_DIMENSION;

            // calculate out what the new height should be based on this width
            view.setSize(size.width - wPadding, Integer.MAX_VALUE);
            newHeight = (int) Math.min((long) view.getPreferredSpan(View.Y_AXIS) + (long) i.top + (long) i.bottom,
                    Integer.MAX_VALUE);
            size.height += newHeight - preferredTextSize.height;
            // shrink the height if it is over long
            size.height = (size.height < DEFAULT_DIMENSION * 2) ? size.height : (int) (DEFAULT_DIMENSION * 1.5);
        }
        return size;
    }

    @Override
    public Rectangle getMaximizedBounds() {
        Rectangle bounds = getBounds();
        Dimension currentText = _text.getParent().getSize();
        Rectangle parentBounds = getParent().getBounds();

        // convert parent to parent's own coordinate system
        parentBounds.x = 0;
        parentBounds.y = 0;
        bounds.height = _text.getPreferredScrollableViewportSize().height + bounds.height - currentText.height;

        // too big to fit in the parent at max size
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

    public void setEditable(boolean canEdit) {
        _text.setEditable(canEdit);
    }

    public void highlightTerm(String term) {
        _text.changeTermHighlight(term);
    }

    public void deHighlightTerm(String term, boolean completely) {
        if (completely){
            _text.removeTerm(term);
        }else{
            _text.deEmphasizeTerm(term);
        }
    }
    
    private Rectangle calcLocation(int start, int end){
        try {
            Rectangle rect = _text.modelToView(start);
            Rectangle rectEnd = _text.modelToView(end);
            
            while (rect.y != rectEnd.y){
                end -= 1;
                rectEnd = _text.modelToView(end);
            }
            
            rect.add(rectEnd);
            
            // check if the location is offscreen
            if (SwingUtilities.convertPoint(_text, rect.getLocation(), _text.getParent()).y < 0){ // off the top
                rect.height = -1;
                rect.x = getX() + getWidth() / 2;
                rect.y = SwingUtilities.convertPoint(_text.getParent(), _text.getParent().getLocation(), getParent()).y;
            }else if (SwingUtilities.convertPoint(_text, rect.getLocation(), this).y + rect.height > getHeight()){ // off the bottom
                rect.height = -2;
                rect.x = getX() + getWidth() / 2;
                rect.y = getY() + getHeight() - 5;
            }else{
                rect.setLocation(SwingUtilities.convertPoint(_text, rect.getLocation(), getParent()));
            }
            
            return rect;
        } catch (BadLocationException ex) {
            Logger.getLogger(AWTextFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    @Override
     public ArrayList<Rectangle> getEntityLocations(AWEntity entity){
        Map<Range, AWEntity> entityRanges = _doc.getEntityRanges();
        ArrayList<Rectangle> locations = new ArrayList<Rectangle>(5);
        for (Range range: entityRanges.keySet()){
            if (entity == entityRanges.get(range)){
               locations.add(calcLocation(range.getStart(), range.getEnd()));
            }
        }
        
        
        return locations;
    }
    
    
    @Override
     public ArrayList<Rectangle> getTermLocations(String query){
          ArrayList<Rectangle> locations = new ArrayList<Rectangle>(5);

        String data = _doc.getText().toLowerCase();
        query = query.toLowerCase();

        ArrayList<String> terms = new ArrayList<String>(5);
        int quote = query.indexOf("\"");
        int index = 0;
        while (index < query.length()) {
            if (quote == index) { // first character is a quote
                quote = query.indexOf("\"", quote + 1);
                if (quote != -1) {
                    terms.add(query.substring(index + 1, quote)); // add the phrase, minus the quotes
                    index = quote + 1;
                    continue;
                }
            }
            if (quote == -1) {
                quote = query.length();
            }
            terms.addAll(Arrays.asList(query.substring(index, quote).split(" ")));
            
            index = quote;
            
        }



        // not the most intelligent way to do this, but it should work for now
        // note that this only support s limited number of lucene query formats
        boolean skip = false;
        
        for (String term : terms) {
            if (term.startsWith("+")) { // and trim if needed, remove if isolated
                if (term.length() == 1) {
                    continue;
                }
                term = term.substring(1);
                } else if (term.startsWith("-")) { // not - ignore
                    if (term.length() == 1) {
                        skip = true;
                    }
                    continue;
                } else if (term.equals("and") || term.equals("or")) { // remove and and ors
                    continue;
                } else if (term.equals("not")) { // skip the next term
                    skip = true;
                    continue;
                }
            
            if (skip) { // last term was a not, just move on
                    skip = false;
                    continue;
                }

                if (term.indexOf('*') != -1) { //there is a multi char wildcard
                    Pattern p = Pattern.compile("(" + term.replace("*", "\\w*") + ")");
                    Set<String> matches = new HashSet<String>();
                    Matcher m = p.matcher(data);
                    while (m.find()) {
                        matches.add(m.group());
                    }

                    for (String match : matches) {
                        loadLocations2(match, data, locations);
                    }

                } else {
                   loadLocations2(term, data, locations);
                }


            }
        return locations;
     }
     private void loadLocations2(String term, String data, ArrayList<Rectangle> locations){
        int start = 0;
        int next = data.indexOf(term, start);
        int termLength = term.length();

        while (next != -1) {
            locations.add(calcLocation(next, next+termLength));
            next = data.indexOf(term, start);
            start = next + 1;
        }

    }
    
    
    
    
    @Override
    public void addAdjustmentListener(AdjustmentListener listener){
        if (_text.getParent().getParent() instanceof JScrollPane){
            JScrollPane scroll = (JScrollPane)_text.getParent().getParent();
            
            scroll.getVerticalScrollBar().addAdjustmentListener(listener);
            
        }
    }

    private class Helper implements MouseListener {

        private void showContextMenu(MouseEvent me) {
            JPopupMenu menu = null;
            Range range = _text.getClickedTerm(me.getPoint());
            if (range != null && !range.isEmpty()) {
                AWEntity entity = _doc.getEntity(range);
                if (entity != null) {
                    menu = TextFrameMenuHelper.getInstance().loadMenu(AWTextFrame.this, entity, range);
                } else {
                    String selectedText;
                    try {
                        selectedText = _text.getText(range.getStart(), range.getEnd() - range.getStart());
                        menu = TextFrameMenuHelper.getInstance().loadMenu(AWTextFrame.this, selectedText, range);
                    } catch (BadLocationException ex) {
                        Logger.getLogger(AWTextFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else {
                menu = TextFrameMenuHelper.getInstance().loadMenu(AWTextFrame.this);
            }
            if (menu != null) {
                menu.show(me.getComponent(), me.getX(), me.getY());
            }
        }

        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                showContextMenu(me);
            } else if (me.isAltDown()) {
                _text.setHighlightMode(true);
            }
        }

        public void mouseReleased(MouseEvent me) {
            // handle popupmenu first
            if (me.isPopupTrigger()) {
                showContextMenu(me);
                return;
            }

            // wasn't a right click, handle selection
            String selectedText = _text.getSelectedText();
            if (selectedText != null) {
                SearchTool.getInstance().setQuery("\"" + selectedText.trim() + "\"", AWTextFrame.this);
            }

            if (_text.isHighlightMode()) {
                _text.setHighlightMode(false);
                _doc.setHighlight(_text.getSelectionStart(), _text.getSelectionEnd());
                _text.setSelectionStart(_text.getSelectionEnd());
            }

            // this is just a static window, so transfer control back to search field
            if (!_text.isEditable()) {
                transferFocus();
            }

        }

        public void mouseClicked(MouseEvent me) {
            Range range = _text.getClickedTerm(null);
            if (range != null && !range.isEmpty()) {
                String selectedText;
                try {
                    selectedText = _text.getText(range.getStart(), range.getEnd() - range.getStart());
                    SearchTool.getInstance().setQuery("\"" + selectedText.trim() + "\"", AWTextFrame.this);
                    if (me.getClickCount() == 2) {
                        AWEntity entity = _doc.getEntity(range);
                        if (entity != null) {
                            AWController.getInstance().displayEntity(entity);
                            _text.setSelectionStart(_text.getSelectionEnd());
                        }
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(AWTextFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void mouseEntered(MouseEvent me) {
        }

        public void mouseExited(MouseEvent me) {
        }
    } // end of Helper
} // end of AWTextFrame

