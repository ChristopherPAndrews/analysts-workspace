package edu.vt.workspace.components;

import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.Range;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author cpa
 */
public class AWPopupTextWindow extends AWInternalFrame {

    private static final AWPopupTextWindow INSTANCE = new AWPopupTextWindow();
    private AWInternalFrame _caller = null;
    private AWTextPane _text;
    private static final int SPACER = 5;
    private ComponentAdapter _moveHelper;
    private InternalFrameAdapter _viewHelper;

    private AWPopupTextWindow() {
        setupContents();
    }

    public static AWPopupTextWindow getInstance() {
        return INSTANCE;
    }

    private void setupContents() {
        _text = new AWTextPane();

        JScrollPane scrollArea = new JScrollPane(_text);

        scrollArea.getViewport().setSize(new Dimension(400, 200));
        getContentPane().add(scrollArea, BorderLayout.CENTER);
        setSize(new Dimension(400, 200));
        setBorder(null);
        ((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).setNorthPane(null);
        setMaximizable(false);
        setResizable(false);
        setClosable(false);
        _text.setBackground(new Color(245, 245, 250));
        setVisible(false);

        _moveHelper = new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent ce) {
                positionFrame();
            }
        };
        
        _viewHelper = new InternalFrameAdapter() {

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {
                    if (AWPopupTextWindow.this.isSelected()) {
                        // the focus change was caused by the user clicking this pane
                        AWInternalFrame frame = AWController.getInstance().displayFile(_doc, true);
                        frame.setLocation(getLocation());
                    }

                    closeContact((AWInternalFrame)e.getInternalFrame());
                }
            };
        

    }

    public void showDocument(AWDocument doc, String term, AWInternalFrame caller) {

        
        if (doc == null){
            closeContact(caller);
            return;
        }
        
        // Load the document and do highlighting
        _doc = doc;

        if (_doc.getType().equals("text")) {
            _text.clearHighlights();
            _text.setText(_doc.getText());

            if (term != null) {
                _text.changeTermHighlight(term);
            }

            Map<Range, AWEntity> entities = _doc.getEntityRanges();

            for (Range range : entities.keySet()) {
                AWEntity entity = entities.get(range);
                _text.highlightRange(range, entity);
            }

            _doc.setSeen(true);


        } else {
            _text.setText("Document preview does not current support documents of type " + doc.getType() + ".");
        }
        _text.setCaretPosition(0);


        // the caller is a new one, so we want to get a new location and register to listen for changes from it
        if (_caller == null) {
            _caller = caller;
            // place the window and make it visible
            positionFrame();
            setVisible(true);
            try {
                _caller.setSelected(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(AWPopupTextWindow.class.getName()).log(Level.SEVERE, null, ex);
            }


            caller.addComponentListener(_moveHelper);
            caller.addInternalFrameListener(_viewHelper);
        }
    }
    
    private void closeContact(AWInternalFrame frame) {
        setVisible(false);
        frame.removeInternalFrameListener(_viewHelper);
        frame.removeComponentListener(_moveHelper);

        _doc = null;
        _caller = null;
    }


    private void positionFrame() {
        Rectangle callerBounds = _caller.getBounds();
        Rectangle parentBounds = getParent().getBounds();
        Rectangle bounds = getBounds();

        // try to be top aligned and just to the right of the caller
        // if we are offscreen - opt for the left
        bounds.y = callerBounds.y;
        bounds.x = callerBounds.x + callerBounds.width + SPACER;

        if (bounds.x + bounds.width > parentBounds.width) {
            bounds.x = callerBounds.x - bounds.width - SPACER;
        }
        setBounds(bounds);
    }
}
