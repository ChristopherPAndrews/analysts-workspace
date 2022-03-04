package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.SelectedLinkGraphicsPane;
import edu.vt.workspace.components.utilities.MultiDraggableComponent;
import edu.vt.workspace.components.utilities.WorkspacePaneDM;
import edu.vt.workspace.data.AWDataManager;
import edu.vt.workspace.data.AWLinkManager;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import javax.swing.JDesktopPane;
import java.awt.Component;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JInternalFrame;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.MouseInfo;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameListener;

public class WorkspacePane extends JDesktopPane implements AWSavable {

    static final long serialVersionUID = 6546970211073609034L;
    public static final int GRAPHICS_LAYER = -10;
    public static final int SELECTED_LINK_LAYER = 9;
    public static final int NOTE_LAYER = 10;
    public static final int PREVIEW_LAYER = 15;
    public static final int DIALOG_LAYER = 20;
    public static final int GLASS_LAYER = 18;
    private static final int PLACEMENT_ITERATIONS = 36;
    private static final int PLACEMENT_SPACING = 5;
    private final Random _rand = new Random();
    private WorkspaceGraphicsPane _canvas;
    private transient SelectionListener _selectionListener;
    private transient ChangeListener _changeListener;
    private transient BackgroundListener _backgroundListener;
    private boolean _showGlass;
    private boolean _autoplace;
    private boolean _dirty;
    
    private SelectedLinkGraphicsPane _selectedLinkLayer;
    private JInternalFrame[] _selectedFrames;

    public WorkspacePane(JPopupMenu contextMenu) {
        super();

        setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        setBackground(Color.black);

        _canvas = new WorkspaceGraphicsPane(this);
        add(_canvas,new Integer(GRAPHICS_LAYER));
        _selectedLinkLayer = new SelectedLinkGraphicsPane();
        add(_selectedLinkLayer, new Integer(SELECTED_LINK_LAYER));
       
        setDesktopManager(new WorkspacePaneDM());
        _selectionListener = new SelectionListener();
        _changeListener = new ChangeListener();
        _backgroundListener = new BackgroundListener();
        _backgroundListener.setContextMenu(contextMenu);
        _canvas.addMouseListener(_backgroundListener);
        _canvas.addMouseMotionListener(_backgroundListener);
        _canvas.addKeyListener(_backgroundListener);
        _showGlass = false;
        _autoplace = true;
        _dirty = false;

    }

    @Override
    public void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);

        comp.addComponentListener(_changeListener);
        if (comp instanceof AWInternalFrame) {
            AWInternalFrame frame = (AWInternalFrame) comp;
            frame.addInternalFrameListener(_changeListener);
            if (_autoplace) {
                placeFrame(frame);
                frame.getGlassPane().setVisible(_showGlass);
                frame.getGlassPane().addMouseListener(_selectionListener);
            }
            AWDataManager.getInstance().addFrame(frame);
        }

    }

    public void setAutoPlace(boolean auto) {
        _autoplace = auto;
    }

    public void setContextMenu(JPopupMenu contextMenu) {
        _backgroundListener.setContextMenu(contextMenu);
    }

    public boolean isDirty() {
        return _dirty;
    }

    public void setDirty(boolean dirty) {
        this._dirty = dirty;
    }
    
    public void showSelectedLinkLayer(boolean show){
        _selectedLinkLayer.setVisible(show);
    }
    
    public SelectedLinkGraphicsPane getSelectedLinkLayer(){
        return  _selectedLinkLayer;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        _canvas.setBounds(0, 0, width, height);
        _selectedLinkLayer.setBounds(0,0,width,height);
    }

    public WorkspaceGraphicsPane getCanvas() {
        return _canvas;
    }

    private void placeFrame(JInternalFrame frame) {
        JInternalFrame currentFrame = getSelectedFrame();

        Point p = null;
        Rectangle visibleBounds = getVisibleRect();

        if (currentFrame == null || !currentFrame.isShowing()) {
            // no current frame - use the mouse or absolute position

            p = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(p, this);
            if (p == null || !visibleBounds.contains(p)) {
                p = new Point(visibleBounds.getLocation());
            }

            // move the frame to make sure it is maximally visible
            p.x = Math.max(visibleBounds.x, p.x);
            p.x = Math.min(visibleBounds.x + visibleBounds.width - frame.getWidth(), p.x);

            p.y = Math.max(visibleBounds.y, p.y);
            p.y = Math.min(visibleBounds.y + visibleBounds.height - frame.getHeight(), p.y);

            frame.setLocation(p);
            return;
        }

        // there is a current frame, so we are going to be more careful placing the new frame

        // start by trying to put it to the side of the currently selected frame, adjusting the height for the viewport
        Rectangle oldBounds = currentFrame.getBounds();
        Rectangle bounds = frame.getBounds();
        boolean moveRight = true;
        p = new Point(oldBounds.x + oldBounds.width + PLACEMENT_SPACING, Math.max(visibleBounds.y, oldBounds.y));

        // make sure this is somewhat visible and pick the other side if it isn't
        if (p.x + (bounds.width / 3) > visibleBounds.x + visibleBounds.width) {
            // note that we accept overlap to avoid going out of bounds
            p.x = Math.max(0, oldBounds.x - bounds.width - PLACEMENT_SPACING);
            moveRight = false;

        }
        Point originalP = p;
        int count = 0;

        while (count < PLACEMENT_ITERATIONS && p.x > 0) {
            bounds.setLocation(p);
            count++;
            Point n = seekVLocation(bounds);
            if (n != null) {

                frame.setLocation(n);
                return;
            }

            if (moveRight) {
                p.x += bounds.width + PLACEMENT_SPACING;
                if (p.x + (bounds.width / 3) > visibleBounds.x + visibleBounds.width) {
                    moveRight = false;
                    p = originalP;
                    p.x -= bounds.width + PLACEMENT_SPACING;
                }
            } else {
                p.x -= bounds.width + PLACEMENT_SPACING;
            }

        }


        // okay, couldn't find a place yet - I'm going to cop out and just pick one at random for the time being

        p = oldBounds.getLocation();
        p.x += _rand.nextInt(oldBounds.width * 4 - oldBounds.width * 2);
        p.y += _rand.nextInt(oldBounds.height * 4 - oldBounds.height * 2);

        p.x = Math.max(visibleBounds.x, p.x);
        p.x = Math.min(visibleBounds.x + visibleBounds.width - frame.getWidth(), p.x);

        p.y = Math.max(visibleBounds.y, p.y);
        p.y = Math.min(visibleBounds.y + visibleBounds.height - frame.getHeight(), p.y);

        // part of the problem is that the frames don't have any size yet


        frame.setLocation(p);
    }

    /**
     * The role of this method is take a frame a try to find a location for it vertically.
     * It will try a location, check if it overlaps anything and if it does, move down until it doesn't any more.
     * The stop point will be when it finds a placement (which it will return), when it runs out of room, or
     * it has traveled more than ten times its own height. If the later two conditions are met, this will return null.
     * @param bounds the bounds of the frame we are trying to place.
     * @return the final position or null if a point can't be found
     */
    private Point seekVLocation(Rectangle bounds) {
        Rectangle visibleBounds = getVisibleRect();
        JInternalFrame overlap;
        Rectangle overlapBounds;
        int end = Math.min(visibleBounds.y + visibleBounds.height - bounds.height / 2,
                bounds.y + ((bounds.height + PLACEMENT_SPACING) * 10));
        while (bounds.y < end) {
            overlap = findOverlap(bounds);

            if (overlap == null) {
                return bounds.getLocation();
            }

            overlapBounds = overlap.getBounds();
            bounds.y = overlapBounds.y + overlapBounds.height + PLACEMENT_SPACING;
        }
        return null;
    }

    /**
     * This function finds any frames that this may overlap by checking the four corners. If the window is
     * much smaller and completely contained, it will be ignored, but that is the current cost of efficency.
     * In future, more test probse could be added (such as in the center) that would reduce the possibility
     * of missing smaller windows.
     * @param bounds the candidate window frame being tested
     * @return
     */
    private JInternalFrame findOverlap(Rectangle bounds) {
        Component c;

        c = getComponentAt(bounds.x, bounds.y);
        if (c instanceof JInternalFrame) {
            return (JInternalFrame) c;
        }

        c = getComponentAt(bounds.x + bounds.width - 1, bounds.y + 1);
        if (c instanceof JInternalFrame) {
            return (JInternalFrame) c;
        }

        c = getComponentAt(bounds.x + 1, bounds.y + bounds.height - 1);
        if (c instanceof JInternalFrame) {
            return (JInternalFrame) c;
        }

        c = getComponentAt(bounds.x + bounds.width - 1, bounds.y + bounds.height - 1);
        if (c instanceof JInternalFrame) {
            return (JInternalFrame) c;
        }

        return null;
    }

//    @Override
//    public final void paint(final Graphics g) {
//        super.paint(g);
//        AWLinkManager.getInstance().paintSelectedLinks((Graphics2D) g);
//
//    }

    /**
     * This method is in charge of saving the actual contents of the workspace. It
     * iterates through the open frames and saves all of the contents and positions.
     *
     * @param writer an instance of AWWriter that will actually write the data to the save file
     */
    public void writeData(AWWriter writer) {
        JInternalFrame[] frames = getAllFramesInLayer(WorkspacePane.DEFAULT_LAYER);
        for (JInternalFrame frame : frames) {
            if (frame instanceof AWSavable) {
                AWSavable item = (AWSavable) frame;
                writer.write("frame", item);
            }
        }

        frames = getAllFramesInLayer(WorkspacePane.NOTE_LAYER);
        for (JInternalFrame frame : frames) {
            if (frame instanceof AWSavable) {
                AWSavable item = (AWSavable) frame;
                writer.write("frame", item);
            }
        }
    }

    private class ChangeListener implements InternalFrameListener, ComponentListener {

        public void internalFrameOpened(InternalFrameEvent e) {
            setDirty(true);
        }

        public void internalFrameClosing(InternalFrameEvent e) {
            setDirty(true);
            e.getInternalFrame().removeInternalFrameListener(this);
        }

        public void internalFrameClosed(InternalFrameEvent e) {
            setDirty(true);
            e.getInternalFrame().removeInternalFrameListener(this);
        }

        public void internalFrameIconified(InternalFrameEvent e) {
            setDirty(true);
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
            setDirty(true);
        }

        public void internalFrameActivated(InternalFrameEvent e) {
            setDirty(true);
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
            setDirty(true);
        }

        public void componentResized(ComponentEvent e) {
            setDirty(true);
        }

        public void componentMoved(ComponentEvent e) {
            setDirty(true);
        }

        public void componentShown(ComponentEvent e) {
            setDirty(true);
        }

        public void componentHidden(ComponentEvent e) {
            setDirty(true);
        }
    }

    protected class BackgroundListener extends MouseInputAdapter implements KeyListener {

        Rectangle selectRegion = new Rectangle();
        Point origin = new Point();
        transient JPopupMenu contextMenu = null;
        boolean dragging = false;

        @Override
        public void mouseClicked(MouseEvent me) {
            for (JInternalFrame frame : WorkspacePane.this.getAllFrames()) {
                try {
                    frame.setSelected(false);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(WorkspacePane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            AWLinkManager.getInstance().tryLinkSelection(me.getPoint(), me.getClickCount());
            _canvas.requestFocusInWindow();

        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                if (contextMenu != null) {
                    contextMenu.show(me.getComponent(), me.getX(), me.getY());
                }
                return;
            }

            if (me.getButton() == MouseEvent.BUTTON1) {
                if (!me.isControlDown()) {
                    ((WorkspacePaneDM) getDesktopManager()).clearMultiSelect();
                }



                dragging = true;
                origin.setLocation(me.getX(), me.getY());
                selectRegion.setLocation(origin);
                selectRegion.setSize(0, 0);

            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            Component[] components = getComponents();
            if (me.isPopupTrigger()) {
                if (contextMenu != null) {
                    contextMenu.show(me.getComponent(), me.getX(), me.getY());
                }
                return;
            }
            dragging = false;
            if (me.getButton() == MouseEvent.BUTTON1) {
                MultiDraggableComponent dragItem;
                for (Component component : components) {
                    if (component.isVisible()
                            && selectRegion.intersects(component.getBounds())) {
                        dragItem = null;
                        if (component instanceof AWInternalFrame) {
                            dragItem = ((AWInternalFrame) component).getDragProxy();
                        } else if (component instanceof JDesktopIcon
                                && ((JDesktopIcon) component).getInternalFrame() instanceof AWInternalFrame) {
                            dragItem = ((AWInternalFrame) ((JDesktopIcon) component).getInternalFrame()).getDragProxy();
                        }
                        if (dragItem != null
                                && !dragItem.isMultiSelectMember()) {
                            ((WorkspacePaneDM) getDesktopManager()).toggleMultiSelect(dragItem);

                        }
                    }


                }

                repaint();
                selectRegion.setBounds(0, 0, 0, 0);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (dragging) {
                Graphics2D g2 = (Graphics2D) getGraphics();
                Point p = new Point(me.getX(), me.getY());
                final float[] dash = {5.0f};
                final BasicStroke dashed = new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        5.0f, dash, 0.0f);
                g2.setXORMode(Color.white);
                g2.setColor(Color.gray);
                g2.setStroke(dashed);
                g2.drawRect(selectRegion.x, selectRegion.y, selectRegion.width, selectRegion.height);
                selectRegion.width = Math.abs(origin.x - p.x);
                selectRegion.height = Math.abs(origin.y - p.y);
                selectRegion.x = (origin.x < p.x) ? origin.x : p.x;
                selectRegion.y = (origin.y < p.y) ? origin.y : p.y;
                g2.drawRect(selectRegion.x, selectRegion.y, selectRegion.width, selectRegion.height);
                g2.dispose();
            }
        }

        private void setContextMenu(JPopupMenu contextMenu) {
            this.contextMenu = contextMenu;
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                AWLinkManager.getInstance().deleteSelectedLink();
            }
        }
    }

    protected class SelectionListener extends MouseInputAdapter {

        /**
         * Describe <code>mousePressed</code> method here.
         *
         * @param mouseEvent a <code>MouseEvent</code> value
         */
        @Override
        public final void mousePressed(final MouseEvent mouseEvent) {
            //System.out.println("press");
        }
    }
}
