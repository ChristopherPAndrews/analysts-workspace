package edu.vt.workspace.components;

import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author  <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
public class AWNote extends AWInternalFrame implements AWSavable {

    static final long serialVersionUID = -3694475937612341393L;
    private static final Color DEFAULT_COLOR = new Color(255, 255, 204);
    private static int _noteCount = 1;
    private Color _color = DEFAULT_COLOR;
    private int _noteID;
    private AWTextPane _text;
    private JPopupMenu _menu;
    private boolean _dirty = false;

    public AWNote(Queriable controller) {
        super(controller);
        setupContents();
    }

    public AWNote() {
        super();
        setupContents();
    }

    @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);
        writer.write("title", title);
        writer.write("text", _text.getText());
        writer.write("rgb", _color.getRGB());
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    public void setText(String data) {
        _text.setText(data);
    }

    public String getText(){
        return _text.getText();
    }

    public void setRgb(Integer rgb) {
        _color = new Color(rgb);
        setColor(_color);
    }

    public void setNoteID(int id) {
        _noteID = id;
        setTitle("Note " + _noteID);
    }
    
    
    public int getNoteID(){
        return _noteID;
    }
    
    
    
    

    public void setColor() {
        final JColorChooser chooser = new JColorChooser(_color);
        JDialog dialog = JColorChooser.createDialog(this,
                "Choose a new background color",
                true,
                chooser,
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        setColor(chooser.getColor());
                    }
                },
                null);
        AWController.getInstance().place(dialog);
        dialog.setVisible(true);
    }

    public void setColor(Color c) {
        _color = c;
        if (_text != null) {
            _text.setBackground(_color);
        }
        setBorder(new MatteBorder(1, 1, 1, 1, _color));

    }

    /**
     * Return the current color of the note.
     * @return the current color of the note
     */
    public Color getColor() {
        return _color;
    }

    private void setupContents() {
        JScrollPane scrollArea;
        setTitle("Note " + _noteCount++);


        setMaximizable(false);
        _text = new AWTextPane();
        scrollArea = new JScrollPane(_text);
        _text.setDragEnabled(true); // allows the user to drag _text

        // set a flag if the text of the note changes
        _text.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                _dirty = true;
            }

            public void removeUpdate(DocumentEvent e) {
                _dirty = true;
            }

            public void changedUpdate(DocumentEvent e) {
                _dirty = true;
            }
        });

        

        scrollArea.getViewport().setSize(new Dimension(200, 200));
        getContentPane().add(scrollArea, BorderLayout.CENTER);
        setSize(new Dimension(200, 200));
        addComponentListener(new PositionHelper());

        setColor(_color);

        _menu = new JPopupMenu();

        JMenuItem menuItem;
        menuItem = new JMenuItem("Set Color");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setColor();
            }
        });
        _menu.add(menuItem);

        _text.addMouseListener(new MouseInputAdapterImpl());
    }

    public void appendText(String data) {
        int len = _text.getText().length();
        _text.setCaretPosition(len);
        _text.replaceSelection(data);
    }

    
    private class PositionHelper extends ComponentAdapter {

        private Component tracked;
        private int xOffset, yOffset;
        private boolean dirty = false;

        @Override
        public void componentMoved(ComponentEvent ce) {
            if (ce.getComponent() == AWNote.this) {
                Rectangle bounds = AWNote.this.getBounds();
                Point posTR = new Point(bounds.x + bounds.width, bounds.y);
                if (dirty) {
                    dirty = false;
                    return;
                }
                if (tracked != null) {
                    tracked.removeComponentListener(this);
                    tracked = null;
                }
                JInternalFrame[] frames = ((WorkspacePane) getParent()).getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
                JInternalFrame secondary; // right top corner
                for (JInternalFrame f : frames) {
                    Rectangle targetBounds = f.getBounds();
                    if (targetBounds.contains(bounds.x, bounds.y)) { // top left corner - accept it
                        tracked = f;
                        break;
                    } else if (targetBounds.contains(posTR)) { // top right corner - maybe, but keep iterating
                        tracked = f;
                    }

                }
                if (tracked != null) {
                    tracked.addComponentListener(this);
                    xOffset = bounds.x - tracked.getLocation().x;
                    yOffset = bounds.y - tracked.getLocation().y;
                }

            } else if (ce.getComponent() == tracked) {
                // the component this is attached to is moving - follow it
                Point p = tracked.getLocation();
                setLocation(p.x + xOffset, p.y + yOffset);
                dirty = true;
            }
        }

        @Override
        public void componentResized(ComponentEvent ce) {
        }
    }

    private class MouseInputAdapterImpl extends MouseInputAdapter {

        public MouseInputAdapterImpl() {
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                if (_menu != null) {
                    _menu.show(me.getComponent(), me.getX(), me.getY());
                }
                return;
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (me.isPopupTrigger()) {
                if (_menu != null) {
                    _menu.show(me.getComponent(), me.getX(), me.getY());
                }
                return;
            }
        }
    }
}
