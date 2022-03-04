package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.SimpleLine;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.LinkTarget;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.Area;
import java.util.ArrayList;

/**
 * This is a link that can connect to documents internally.
 * 
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
public class DocumentListLink extends SimpleLink {

    private Object _payload;
    private AWDocument _document;
    private ArrayList<Rectangle>[] _locations = new ArrayList[2];
    private ArrayList<Rectangle>[] _rectangles;
    private static final Color INTERNAL_COLOR = new Color(0, 185, 30, 80);
    private static final BasicStroke INTERNAL_STROKE = new BasicStroke(3);

    public DocumentListLink(AWInternalFrame f0, AWInternalFrame f1, Object payload) {
        super(f0, f1);
        _payload = payload;

        AdjustmentListener listener = new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                recalculate(true);
            }
        };

        ((LinkTarget) getFrames()[0]).addAdjustmentListener(listener);
        ((LinkTarget) getFrames()[1]).addAdjustmentListener(listener);

        if (getFrames()[0] instanceof AWDocumentView) {
            _document = ((AWDocumentView) getFrames()[0]).getDocument();
        } else if (getFrames()[1] instanceof AWDocumentView) {
            _document = ((AWDocumentView) getFrames()[1]).getDocument();
        }

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        recalculate(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (_selected) {
           
            Shape clip = g2.getClip();
            
            Area mask = new Area(clip);
            mask.subtract(new Area(getFrames()[0].getBounds()));
            mask.subtract(new Area(getFrames()[1].getBounds()));
            g2.setClip(mask);
            
            g2.setColor(SELECTED_COLOR);
            g2.setStroke(SELECTED_STROKE);
            
            paintLine(g2, _currentLine);
            
            g2.setColor(INTERNAL_COLOR);
            g2.setStroke(INTERNAL_STROKE);

            for (int i = 0; i < 2; i++) {
                if (!getFrames()[i].isIcon()) {
                    g2.setClip(getFrames()[i].getBounds());
                    if (_locations[i].size() > 1) {

                        for (Rectangle rect : _locations[i]) {
                            if (i == 0) {
                                g2.drawLine(_currentLine.x0, _currentLine.y0, rect.x, rect.y);
                            } else {
                                g2.drawLine(_currentLine.x1, _currentLine.y1, rect.x, rect.y);
                            }
                            g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                        }
                    } else if (_locations[i].size() > 0){
                        paintLine(g2, _currentLine);
                        Rectangle rect = _locations[i].get(0);
                        if (rect.height == -1){
                            Point origin = rect.getLocation();
                            g2.drawLine(origin.x, origin.y, origin.x + 15, origin.y + 30);
                            g2.drawLine(origin.x + 15, origin.y + 30, origin.x - 15, origin.y + 30);
                            g2.drawLine(origin.x, origin.y, origin.x - 15, origin.y + 30);
                        }else if (rect.height == -2){
                            Point origin = rect.getLocation();
                            g2.drawLine(origin.x, origin.y, origin.x + 15, origin.y - 30);
                            g2.drawLine(origin.x + 15, origin.y - 30, origin.x - 15, origin.y - 30);
                            g2.drawLine(origin.x, origin.y, origin.x - 15, origin.y - 30);
                        }else{
                            g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                        }
                    }

                }
            }

            g2.setClip(clip);
        } else {
            super.paint(g);
        }

    }

    @Override
    protected void recalculate(boolean dragging) {

        if (_selected) {
            Rectangle[] rects = new Rectangle[2];
            for (int i = 0; i < 2; i++) {
                AWInternalFrame frame = getFrames()[i];
                if (!frame.isIcon()) {
                    if (frame instanceof AWDocumentView) {
                        if (_payload instanceof AWEntity) {
                            _locations[i] =  ((AWDocumentView) frame).getEntityLocations((AWEntity) _payload);
                        } else if (_payload instanceof String) {
                            _locations[i] =  ((AWDocumentView) frame).getTermLocations((String) _payload);
                        }
                    } else if (frame instanceof AWFileList) {
                        _locations[i] =  ((AWFileList) frame).getDocumentLocation(_document);
                    }

                    if (_locations[i].size() == 1) { // only one target, go directly to it
                        rects[i] = _locations[i].get(0);
                        if (rects[i].height < 0){ // offscreen
                            rects[i] = frame.getBounds();
                        }
                    } else {
                        rects[i] = frame.getBounds();
                    }

                } else {
                    rects[i] = frame.getDesktopIcon().getBounds();
                }
            }

            SimpleLine newLine = new SimpleLine(rects[0], rects[1], false);
            if (newLine.equals(_currentLine)) {
                return;
            }
            

            if (dragging) {
                Rectangle clip;
                Graphics2D g = (Graphics2D) _canvas.getGraphics();
                if (g != null) {
                    // we are using a fast a dirty way to instantly draw links after
                    // a change. The first drawline erases the old line. The next
                    // two erase any artifacts that might have appeared inside of
                    // the _frames due to this step. Finally, we draw the  new line
                    
                    
                    clip = g.getClipBounds();
               
                    g.setXORMode(Color.black);
                    
                    g.setColor(SELECTED_COLOR);
                    g.setStroke(SELECTED_STROKE);
                   
                    
                    paintLine(g, _lastLine);
                    g.setClip(rects[0]);
                    paintLine(g, _lastLine);
                    g.setClip(rects[0]);
                    paintLine(g, _lastLine);
                    g.setClip(clip);
                    
                    paintLine(g, newLine);
                    g.dispose();
                    
                    
                }
            
            getFrames()[0].repaint();
                getFrames()[1].repaint();
            }
            
            _currentLine = newLine;
        } else {
            super.recalculate(dragging);
        }
    }
}
