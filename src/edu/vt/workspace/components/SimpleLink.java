package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.SimpleLine;
import edu.vt.workspace.components.utilities.SimpleLinkListener;
import edu.vt.workspace.data.AWController;
import java.awt.BasicStroke;
import javax.swing.JInternalFrame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.geom.AffineTransform;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;

/**
 * The basic {@code SimpleLink} class which provides the visual links.
 *
 *
 * Created: Wed Feb 18 17:56:41 2009
 *
 * @author Christopher Andrews
 * @version 1.0
 */
public class SimpleLink {

    protected static final BasicStroke DEFAULT_STROKE = new BasicStroke(2);
    protected static final BasicStroke SELECTED_STROKE = new BasicStroke(4);
    protected static final Color SELECTED_COLOR = Color.GREEN;
    private static final int ARROW_WIDTH = 16;
    private static final int ARROW_HEIGHT = 16;
    private static Polygon ARROW_POLY = null;
    private AWInternalFrame[] _frames = new AWInternalFrame[2];
    protected SimpleLine _currentLine;
    protected SimpleLine _lastLine;
    protected WorkspacePane _canvas;
    private boolean _visible = false;
    private boolean _keepVisible = false;
    private boolean _stale = false;
    private boolean _directed = false;
    protected boolean _selected = false;
    private boolean _mutable = false;
    private boolean _reversed = false;
    private Color _color = Color.gray;
    private Vector<SimpleLinkListener> _listeners = new Vector<SimpleLinkListener>(2);
    private FrameListener _listener;

    /**
     * Creates a new <code>SimpleLink</code> instance.
     *
     * @param f0
     * @param f1
     */
    public SimpleLink(AWInternalFrame f0, AWInternalFrame f1) {
        _listener = new FrameListener();
        _frames[0] = f0;
        _frames[1] = f1;

        f0.addInternalFrameListener(_listener);
        f0.getDesktopIcon().addComponentListener(_listener);
        f0.addComponentListener(_listener);
        f1.addInternalFrameListener(_listener);
        f1.getDesktopIcon().addComponentListener(_listener);
        f1.addComponentListener(_listener);
        addSimpleLinkListener(AWController.getInstance());
        setVisible(true);

    }

    public AWInternalFrame[] getFrames() {
        return _frames;
    }

    public SimpleLine getLine() {
        return _currentLine;
    }

    public void severLink() {
        for (SimpleLinkListener listener : _listeners) {
            listener.linkClosing(this);
        }

        for (JInternalFrame frame : _frames) {
            frame.removeComponentListener(_listener);
            frame.removeInternalFrameListener(_listener);
        }

        setVisible(false);
    }

    public void setCanvas(WorkspacePane canvas) {
        this._canvas = canvas;
        recalculate(false);
    }

    public boolean isVisible() {
        return _visible;
    }

    public void setVisible(boolean visible) {

        for (int i = 0; visible && i < 2; i++) {
            visible = visible && !(_frames[i].isClosed());
            // visible = visible && !(_frames[i].isIcon());
        }

        if (visible != this._visible) {
            this._visible = visible;
            if (_canvas != null) {
                _canvas.repaint();
            }
        }
    }
    
   
    public void setReverse(boolean reverse){
        _reversed = reverse;
        recalculate(false);
    }

    /**
     * Get the value of _mutable
     *
     * @return the value of _mutable
     */
    public boolean isMutable() {
        return _mutable;
    }

    /**
     * Set the value of _mutable
     *
     * @param _mutable new value of _mutable
     */
    public void setMutable(boolean mutable) {
        _mutable = mutable;
    }


    public void setKeepVisible(boolean keepVisible) {
        _keepVisible = keepVisible;
        setVisible(_keepVisible || _visible);

    }

    public boolean getKeepVisible() {
        return _keepVisible;
    }

    public void setColor(Color c) {
        _color = c;
        if (_canvas != null) {
            _canvas.repaint();
        }

    }

    public Color getColor() {
        return _color;
    }

    public void setDirected(boolean directed) {
        this._directed = directed;
        if (ARROW_POLY == null) {
            buildArrowImage();
        }
    }

    public void setSelected(boolean selected) {
        _selected = selected;
        if (_canvas != null) {
            _canvas.repaint();
        }

       
        for (SimpleLinkListener listener : _listeners) {
            if (selected) {
                listener.linkSelected(this);
            } else {
                listener.linkDeselected(this);
            }
        }

    }
    
    /**
     * This method is a variant of the {@code setSelected()} method that also 
     * accepts the number of clicks that selected the link. This allows the link to
     * capture double clicks. Note that this always selects the link.
     * 
     * @param clickCount 
     */
    public void select(int clickCount){
        setSelected(true);
        
    }

    public void addSimpleLinkListener(final SimpleLinkListener listener) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                _listeners.add(listener);
            }
        });

    }

    public void removeSimpleLinkListener(final SimpleLinkListener listener) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                _listeners.remove(listener);
            }
        });
    }

    public boolean isAnchor(JInternalFrame frame) {
        return ((frame == _frames[0]) || frame == _frames[1]);
    }

    public boolean containsPoint(Point p) {
        return _currentLine.containsPoint(p, 3);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (_visible) {
            if (_stale) {
                recalculate(false);
            }
            if (_selected) {
                g2.setColor(SELECTED_COLOR);
                g2.setStroke(SELECTED_STROKE);
            } else {
                g2.setColor(_color);
                g2.setStroke(DEFAULT_STROKE);
            }
            paintLine(g2, _currentLine);
        }
    }

    protected void paintLine(Graphics2D g2, SimpleLine line) {
        g2.drawLine(line.x0, line.y0, line.x1, line.y1);
        if (_directed && line.getTransform() != null) {
            AffineTransform currentTransform = g2.getTransform();
            AffineTransform oldTransform = (AffineTransform) currentTransform.clone();
            currentTransform.concatenate(line.getTransform());
            g2.setTransform(currentTransform);
            g2.fill(ARROW_POLY);
            g2.setTransform(oldTransform);
        }
        _lastLine = line;
    }

    private static void buildArrowImage() {
        int[] xpoints = {-1, -ARROW_WIDTH, -ARROW_WIDTH * 2 / 3, -ARROW_WIDTH};
        int[] ypoints = {0, -ARROW_HEIGHT / 2, 0, ARROW_HEIGHT / 2};
        ARROW_POLY = new Polygon(xpoints, ypoints, 4);
    }

    protected void recalculate(boolean dragging) {
        Rectangle r0, r1, clip;
        SimpleLine newLine;

        if (!_visible || _canvas == null) {
            _stale = true;
            return;
        }


        if (_frames[0].isIcon()) {
            r0 = _frames[0].getDesktopIcon().getBounds();
        } else {
            r0 = _frames[0].getBounds();
        }

        if (_frames[1].isIcon()) {
            r1 = _frames[1].getDesktopIcon().getBounds();
        } else {
            r1 = _frames[1].getBounds();
        }
        if (_reversed){
            newLine = new SimpleLine(r1, r0, _directed);
        }else{
            newLine = new SimpleLine(r0, r1, _directed);
        }
        
        if (newLine.equals(_currentLine)) {
            return;
        }
        if (dragging && _lastLine != null) {
            Graphics2D g = (Graphics2D) _canvas.getGraphics();
            if (g != null) {
                // we are using a fast a dirty way to instantly draw links after
                // a change. The first drawline erases the old line. The next
                // two erase any artifacts that might have appeared inside of
                // the _frames due to this step. Finally, we draw the  new line

 
                   clip = g.getClipBounds();
               
                g.setXORMode(Color.black);

                if (_selected) {
                    g.setColor(SELECTED_COLOR);
                    g.setStroke(SELECTED_STROKE);
                } else {
                    g.setColor(_color);
                    g.setStroke(DEFAULT_STROKE);
                }

                paintLine(g, _lastLine);
                g.setClip(r0);
                paintLine(g, _lastLine);
                g.setClip(r1);
                paintLine(g, _lastLine);
                g.setClip(clip);

//
//                g.setPaintMode();
//
//                if (_selected) {
//                    g.setColor(SELECTED_COLOR);
//                    g.setStroke(SELECTED_STROKE);
//                } else {
//                    g.setColor(_color);
//                    g.setStroke(DEFAULT_STROKE);
//                }

                _currentLine = newLine;
                paintLine(g, _currentLine);
                g.dispose();
            }
        } else {
            _currentLine = newLine;
        }
        _stale = false;

    }

    private class FrameListener extends ComponentAdapter implements InternalFrameListener {

        @Override
        public void componentMoved(ComponentEvent ce) {
            recalculate(true);
        }

        @Override
        public void componentResized(ComponentEvent ce) {
            recalculate(true);
        }

        public void internalFrameClosing(InternalFrameEvent e) {
        }

        public void internalFrameClosed(InternalFrameEvent e) {
            // check to see if the closing frame is one of the end points and abort if it isn't
            boolean abort = true;
            for (JInternalFrame frame : _frames) {
                if (frame == e.getInternalFrame()) {
                    abort = false;
                }
            }
            if (abort) {
                e.getInternalFrame().removeInternalFrameListener(this);
                return;
            }

            severLink();

        }

        public void internalFrameOpened(InternalFrameEvent e) {
        }

        public void internalFrameIconified(InternalFrameEvent e) {
            recalculate(false);
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
            recalculate(false);
        }

        public void internalFrameActivated(InternalFrameEvent e) {

        }

        public void internalFrameDeactivated(InternalFrameEvent e) {

        }
    } // end of FrameListener
} // end of SimpleLink
