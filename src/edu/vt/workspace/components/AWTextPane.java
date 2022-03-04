package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.ColoredHighlightPainter;
import edu.vt.workspace.components.utilities.EntityHighlightPainter;
import edu.vt.workspace.components.utilities.HighlightEvent;
import edu.vt.workspace.components.utilities.HighlightListener;
import edu.vt.workspace.components.utilities.UserHighlightPainter;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.EntityManager;
import edu.vt.workspace.data.Range;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a special purpose extension of JTextPane with a couple of features.
 * The primary purpose of this is to provide highlighting of serveral different types.
 * There are search highlights, entity highlights and user generated highlights.
 *
 *
 * @author cpa@cs.vt.edu
 */
public class AWTextPane extends JTextPane implements HighlightListener {
// this creates a hashmap of all of the search terms and the associated
    // highlight painters. This way we can selectively turn on an off the
    // highlights for individual search terms.

    public enum HighlightMode {

        EMPHASIZE,
        DEEMPHASIZE,
        REMOVE
    };
    private static Color INACTIVE_TERM_HIGHLIGHT = new Color(200, 255, 200);
    private static Color ACTIVE_TERM_HIGHLIGHT = Color.green;
    //private static Color INACTIVE_TERM_HIGHLIGHT = new Color(150, 150, 150);
    //private static Color ACTIVE_TERM_HIGHLIGHT = Color.black;
    private static Color SELECTION_HIGHLIGHT = Color.yellow;
    private Color NORMAL_HIGHLIGHT = getSelectionColor();
    private HashMap<String, ColoredHighlightPainter> searchPainters = new HashMap<String, ColoredHighlightPainter>();
    private HashMap<AWEntity, EntityHighlightPainter> entityPainters = new HashMap<AWEntity, EntityHighlightPainter>();
    private ColoredHighlightPainter highlightPainter = new ColoredHighlightPainter(SELECTION_HIGHLIGHT);
    private boolean searchHighlightsDirty = true;
    private boolean highlightMode = false;
    private Highlighter.Highlight _selectedHighlight = null;

//    public AWTextPane() {
//        setToolTipText("");
//        addKeyListener(new KeyListener(){
//
//            public void keyTyped(KeyEvent ke) {
//
//            }
//
//            public void keyPressed(KeyEvent ke) {
//
//            }
//
//            public void keyReleased(KeyEvent ke) {
//                 if (ke.getKeyCode() == KeyEvent.VK_DELETE || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE){
//                    if (_selectedHighlight != null && _document != null){
//                        _document.removeHighlight(_selectedHighlight.getStartOffset(), _selectedHighlight.getEndOffset());
//                       }
//                     }
//            }
//        });
//    }
//
//
    /**
     * This method handles the pop-up tooltip that provides information about entities in the document.
     * It works by looking at the location of the mouse, checking if the mouse is within one of the highlights
     * (and thus, possibly on top of an entity). If it is, it looks for the entity and prints out information
     * (e.g., hit count and aliases). Note that we leverage the fact that tooltips understand html to get multi-line
     * tips.
     *
     * @param me the mouse event that contains the position of the cursor
     * @return
     */
    @Override
    public String getToolTipText(MouseEvent me) {
        int position = viewToModel(me.getPoint());
        int start, end;
        Highlighter h = getHighlighter();
        Highlighter.Highlight[] currentHighlights = h.getHighlights();
        for (Highlighter.Highlight highlight : currentHighlights) {
            start = highlight.getStartOffset();
            end = highlight.getEndOffset();
            if (position >= start && position <= end) {
                try {
                    AWEntity entity = EntityManager.getInstance().getEntity(getText(start, end - start));
                    if (entity != null) {
                        StringBuilder text = new StringBuilder();
                        Set<String> aliases = entity.getAliases();
                        text.append("<html><body>");
                        text.append("Hits: ");
                        text.append(entity.numDocs());
                        if (aliases.size() > 0) {
                            text.append("<br />Aliases:");
                            text.append("<br />");
                            text.append(entity.getValue());

                            for (String alias : aliases) {
                                text.append("<br />");
                                text.append(alias);
                            }
                        }

                        // we need to climb the Component heirarchy to get the position
                        // with respect to the top level window
                        Component parent = getParent();
                        while (!(parent instanceof AnalystsWorkspace)) {
                            parent = parent.getParent();
                        }

                        Point displayPoint = parent.getMousePosition();
                        displayPoint.y -= 20;

                        AWTooltip.getInstance().showTip(text.toString(), displayPoint);

                        return ""; // tell the system there is no tooltip for this location since we are supplying our own
                    }
                } catch (BadLocationException ex) {
                    System.err.println(ex.getMessage());
                    return null;
                }

            }
        }

        return null;
    }

    public boolean isHighlightMode() {

        return highlightMode;
    }

    public void setHighlightMode(boolean highlightMode) {
        this.highlightMode = highlightMode;
        if (highlightMode) {
            setSelectionColor(SELECTION_HIGHLIGHT);
        } else {
            setSelectionColor(NORMAL_HIGHLIGHT);
        }
    }

    /**
     * Create highlights for this given query in the displayed text. This method
     * is slightly long because it takes a fairly complex approach to handling
     * search terms highlights. Search terms are not simple strings that need to matched,
     * they can be complex, involving phrases, wildcards, special words, and negations. This
     * currently tries to parse the query and do something reasonable with it. However, this is quite
     * primitive and doesn't completely work. At some point this should be supplemented with
     * Lucene's sense of what should be highlighted so the search results and the highlights actually line up.
     *
     * @param query the query that is to be highlighted
     */
    public void changeTermHighlight(String query) {
        Highlighter h = getHighlighter();
        Highlighter.Highlight[] currentHighlights = h.getHighlights();
        ColoredHighlightPainter searchPainter;


        int length = getDocument().getLength();
        int termLength = query.length();
        int start, next;
        String data = "";
        try {
            data = getDocument().getText(0, length).toLowerCase();
        } catch (BadLocationException ble) {
            System.out.println(ble.getMessage());
        }

        searchPainter = searchPainters.get(query);
        if (!searchHighlightsDirty && searchPainter != null) {
            searchPainter.setColor(ACTIVE_TERM_HIGHLIGHT);
        } else {
            if (searchPainter == null) {
                searchPainter = new ColoredHighlightPainter(ACTIVE_TERM_HIGHLIGHT);
                searchPainters.put(query, searchPainter);
            }

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
                        highlightTerm(match, data, h, searchPainter);

                    }

                } else {

                    highlightTerm(term, data, h, searchPainter);

                }


            }
        }
        searchHighlightsDirty = false;

        repaint();
    }

    private void highlightTerm(String term, String data, Highlighter h, ColoredHighlightPainter highlightPainter) {
        int start = 0;
        int next = data.indexOf(term, start);
        int termLength = term.length();

        while (next != -1) {
            try {
                h.addHighlight(next, next + termLength, highlightPainter);
            } catch (BadLocationException e) {
            }
            next = data.indexOf(term, start);
            start = next + 1;
        }

    }

    public void deEmphasizeTerm(String term) {
        ColoredHighlightPainter searchPainter;

        searchPainter =
                searchPainters.get(term);
        if (searchPainter != null) {
            searchPainter.setColor(INACTIVE_TERM_HIGHLIGHT);
        }

        repaint();
    }

    public void removeTerm(String term) {
        Highlighter h = getHighlighter();
        Highlighter.Highlight[] currentHighlights = h.getHighlights();
        ColoredHighlightPainter searchPainter;

        searchPainter =
                searchPainters.get(term);
        if (searchPainter != null) {
            searchPainters.remove(term);
            // remove all of the old search highlights
            for (Highlighter.Highlight highlight : currentHighlights) {
                if (highlight.getPainter() == searchPainter) {
                    h.removeHighlight(highlight);
                }

            }
        }

    }

    public void clearHighlights() {
        Highlighter h = getHighlighter();

        Highlighter.Highlight[] currentHighlights = h.getHighlights();
        for (Highlighter.Highlight highlight : currentHighlights) {
            h.removeHighlight(highlight);
        }

        searchHighlightsDirty = true;
    }

    public Range getClickedTerm(Point p) {
        Range range = null;

        int position;

        if (p == null) {
            position = getCaret().getDot();
        } else {
            position = viewToModel(p);
        }
        int start, end;

        Highlighter h = getHighlighter();
        Highlighter.Highlight[] currentHighlights = h.getHighlights();

        if (_selectedHighlight != null) {
            ((UserHighlightPainter) _selectedHighlight.getPainter()).setSelected(false);
            _selectedHighlight = null;
            repaint();
        }


        for (Highlighter.Highlight highlight : currentHighlights) {
            start = highlight.getStartOffset();
            end = highlight.getEndOffset();
            if (position >= start && position <= end) {
                if (highlight.getPainter() instanceof UserHighlightPainter) {
                    _selectedHighlight = highlight;
                    ((UserHighlightPainter) highlight.getPainter()).setSelected(true);
                    repaint();
                }
                if (range == null) {
                    range = new Range(start, end);
                } else if (highlight.getPainter() instanceof EntityHighlightPainter) {
                    range.setRange(start, end);
                }
            }
        }

        return range;
    }

    public Range getSelectedHighlight() {
        if (_selectedHighlight != null) {
            return new Range(_selectedHighlight.getStartOffset(), _selectedHighlight.getEndOffset());
        } else {
            return null;
        }
    }

    void highlightRange(Range highlight) {
        try {
            getHighlighter().addHighlight(highlight.getStart(), highlight.getEnd(), new UserHighlightPainter());
        } catch (BadLocationException ex) {
            Logger.getLogger(AWTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void highlightRange(Range highlight, AWEntity entity) {
        EntityHighlightPainter entityPainter;

        entityPainter = entityPainters.get(entity);
        if (entityPainter == null) {
            entityPainter = new EntityHighlightPainter(EntityManager.getInstance().getColor(entity));
            entityPainters.put(entity, entityPainter);
        }
        try {
            getHighlighter().addHighlight(highlight.getStart(), highlight.getEnd(), entityPainter);
        } catch (BadLocationException ex) {
            Logger.getLogger(AWTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addHighlight(HighlightEvent he) {
        AWEntity entity = he.getEntity();
        ColoredHighlightPainter painter = null;
        if (entity != null) {
            painter = entityPainters.get(entity);
            if (painter == null) {
                painter = new EntityHighlightPainter(EntityManager.getInstance().getColor(entity));
                entityPainters.put(entity, (EntityHighlightPainter) painter);
            }
        } else {
            painter = new UserHighlightPainter();
        }
        try {
            getHighlighter().addHighlight(he.getStart(), he.getEnd(), painter);
        } catch (BadLocationException ex) {
            Logger.getLogger(AWTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        getCaret().setDot(getCaret().getMark());
        repaint();

    }

    public void updateHighlight(HighlightEvent he) {
        AWEntity entity = he.getEntity();
        if (entity == null) {
            return; // don't know what to do with updates not associated with entities
        }
        // change the color of the painter for this entity if we need to
        ColoredHighlightPainter painter = entityPainters.get(entity);
        if (painter != null) {
            painter.setColor(EntityManager.getInstance().getColor(entity));
        }

        removeHighlight(he);
        addHighlight(he);

    }

    public void removeHighlight(HighlightEvent he) {
        int start1, end1;
        Highlighter h = getHighlighter();
        Highlighter.Highlight[] currentHighlights = h.getHighlights();

        if (_selectedHighlight != null) {
            start1 = _selectedHighlight.getStartOffset();
            end1 = _selectedHighlight.getEndOffset();
            if (he.getStart() == start1 && he.getEnd() == end1) {
                h.removeHighlight(_selectedHighlight);
                _selectedHighlight = null;
                repaint();
                return;
            }
        }
        for (Highlighter.Highlight highlight : currentHighlights) {
            start1 = highlight.getStartOffset();
            end1 = highlight.getEndOffset();

            if (he.getStart() == start1 && he.getEnd() == end1
                    && ((he.getEntity() != null && highlight.getPainter() instanceof EntityHighlightPainter)
                    || (he.getEntity() == null && !(highlight.getPainter() instanceof EntityHighlightPainter)))) {
                h.removeHighlight(highlight);
                break;
            }
        }
        repaint();
    }
}
