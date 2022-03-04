package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.AWLinkManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author cpa
 */
public class SelectedLinkGraphicsPane extends JPanel {

    private Point _start, _end;
    private final BasicStroke _stroke;
    private final MouseTracker _tracker;
    private Timer _lineTimer;
    private boolean _tracking = false;

    public SelectedLinkGraphicsPane() {
        super();
        _tracker = new MouseTracker();
        _stroke = new BasicStroke(5);

        setOpaque(false);


// this is something of a bizarre hack to get mouse tracking working because for some reason mouseMoved events were not being captured
        ActionListener lineAction = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (! _tracking){
                    _lineTimer.stop();
                    return;
                }
                Graphics2D g = (Graphics2D) getGraphics();
                if (g != null) {
                    g.setXORMode(Color.black);
                    g.setStroke(_stroke);
                    g.setColor(Color.GREEN);
                    if (_end != null) {

                        g.drawLine(_start.x, _start.y, _end.x, _end.y);
                    }
                    _end = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(_end, SelectedLinkGraphicsPane.this);
                    g.drawLine(_start.x, _start.y, _end.x, _end.y);
                    g.dispose();
                }
            }
        };

        _lineTimer = new Timer(50, lineAction);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        AWLinkManager.getInstance().paintSelectedLinks((Graphics2D) g);
    }

    public void trackCursor(Point p, CompletionCall complete) {
        SwingUtilities.convertPointFromScreen(p, this);
         _start = p;
         _tracking = true;
        _tracker.setComplete(complete);
       
        _lineTimer.start();
        addMouseListener(_tracker);


    }

    private class MouseTracker extends MouseInputAdapter {

        private CompletionCall _complete;

        public void setComplete(CompletionCall _complete) {
            this._complete = _complete;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (_tracking){
                _tracking = false;
                SelectedLinkGraphicsPane.this.removeMouseListener(this);
                _end = null;
                Point p = e.getPoint();
                Rectangle currentBounds = getBounds();
                setBounds(0, 0, 0, 0);
                Component component = getParent().getComponentAt(p);
                setBounds(currentBounds);
                _complete.done(component);
            }
        }
    }

    public interface CompletionCall {

        public void done(Component component);
    }
}
