package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.MultiDraggableComponent;
import edu.vt.workspace.components.utilities.WorkspacePaneDM;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.plaf.AWDesktopIconUI;
import java.awt.Dimension;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyVetoException;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.DesktopIconUI;


/**
 * This is the basic underlying class for all internal frames in AW.
 * @author cpa
 */
public class AWInternalFrame extends JInternalFrame implements  AWSavable {
    protected static int _currentCount = 0;
    protected int _id;
    protected AWDocument _doc = null;
    private boolean _normalMaximum;
    protected transient Queriable _controller;
    private MultiDragProxy _dragProxy;
    private boolean _iconify;

    public AWInternalFrame(String title, Queriable controller) {
        super(title,
                true, //resizable
                true, //closable
                true, //maximizable
                false);//iconifiable
        
        _controller = controller;
        //_id = _currentCount++;
        _id = 0;
        initialize();
       _dragProxy = new MultiDragProxy(this);
       _iconify = false;
    }

    public AWInternalFrame(String title) {
        this(title, null);
    }

    public AWInternalFrame(Queriable controller) {
        this("Untitled" , controller);
    }

    public AWInternalFrame(){
        this("Untitled", null);
    }
    

    public void setController(Queriable controller){
        _controller = controller;
    }


    public MultiDraggableComponent getDragProxy(){
        return _dragProxy;
    }


    private void initialize(){
        
        setGlassPane(new AWInternalFrameGlassPane());
        setMaximizable(true);

        // if this is maximized and resized to some non-maximized form
        // turn off the maximized flag
        addComponentListener(new ComponentAdapter() {
            // if this is maximized and resized to some non-maximized form
            // turn off the maximized flag

            @Override
            public void componentResized(ComponentEvent ce) {
                if (isMaximum() &&
                        !getBounds().equals(getMaximizedBounds())) {
                    try {
                        setNormalBounds(getBounds());
                        setMaximum(false);
                    } catch (PropertyVetoException pve) {
                    }
                }
            }

            // make sure the "normal" location updates when this is moved
            @Override
            public void componentMoved(ComponentEvent ce) {
                Rectangle r = getNormalBounds();
                if (r != null) {
                    Rectangle current = getBounds();
                    r.x = current.x;
                    r.y = current.y;
                    setNormalBounds(r);
                }
            }
        });

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent ae) {
                postOpenInit();
                ae.getComponent().removeAncestorListener(this); // make this a one shot deal
            }

            @Override
            public void ancestorRemoved(AncestorEvent ae) {
            }

            @Override
            public void ancestorMoved(AncestorEvent ae) {
            }
        });

        setVisible(true);
    }

    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
    }
    
    public Rectangle getMaximizedBounds() {
        return getParent().getBounds();
    }

    @Override
    public boolean isResizable() {
        return resizable;
    }

    public void disableMaximum() {
        _normalMaximum = isMaximum;
        isMaximum = false;
    }

    public void restoreMaximum() {
        isMaximum = _normalMaximum;
    }

    public void reinitialize(Queriable controller) {
        _controller = controller;
    }

    /**
     * This is a hook to permit initialization of components that require the frame to be placed in a container
     * to be meaningful.
     */
    protected void postOpenInit() {
        if (_iconify){
            try {
                setIcon(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(AWInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This returns the AWDocument object associated with this window, if there is one. if there isn't, this should return null
     * 
     * @return the _doc, if any, associated with this frame.
     */
    public AWDocument getDocument() {
        return _doc;
    }


    public int getID(){
        return _id;
    }

    public void setID(int id){
        _id = id;
    }


    public void setIconify(boolean set) {
        _iconify = set;
    }



    public void writeData(AWWriter writer) {
        // this is to make sure that we reord the correct position for icons that
        // may have moved from the original position of the frame
        if (isIcon){
            setLocation(desktopIcon.getLocation());
        }
        writer.write("bounds", getBounds());
        writer.write("iconify", isIcon);
    }

  

    /**
     * This class serves as a draggable proxy for the internal frames. It hides the 
     * fact that the frame may be iconified from the desktop manager.
     */
    public static class MultiDragProxy implements MultiDraggableComponent, InternalFrameListener{
        private AWInternalFrame _frame;
        private JComponent _currentComponent;
        private boolean _multiSelectMember;

        public MultiDragProxy(AWInternalFrame frame){
            _frame = frame;
            _currentComponent = _frame;
            _frame.addInternalFrameListener(this);
            _multiSelectMember = false;
        }


        public AWInternalFrame getFrame(){
            return _frame;
        }

        public void setMultiSelectMember(boolean select){
            _multiSelectMember = select;
        }
        
        public boolean isMultiSelectMember(){
            return _multiSelectMember;
        }

        public void hilite(boolean on){
            if (_currentComponent == _frame){
                _frame.getGlassPane().setVisible(on);
            }else{
                DesktopIconUI ui = _frame.getDesktopIcon().getUI();
                if (ui instanceof AWDesktopIconUI){
                    ((AWDesktopIconUI)ui).hilight(on);
                }
            }
            
        }


        public void drag(int deltaX, int deltaY) {
           Dimension parentSize = _currentComponent.getParent().getSize();
            Dimension size = _currentComponent.getSize();
            int newX, newY;

            newX = getX() + deltaX;
            newY = getY() + deltaY;
            newX = (newX < 0) ? 0 : newX;
            newX = (newX > parentSize.width - size.width) ? parentSize.width - size.width : newX;
            newY = (newY < 0) ? 0 : newY;
            newY = (newY > parentSize.height - size.height) ? parentSize.height - size.height : newY;

            _currentComponent.setLocation(newX, newY);


        }

        public Rectangle getBounds() {
            return _currentComponent.getBounds();
        }

        public Point getLocation() {
            return _currentComponent.getLocation();
        }

        public Point getLocation(Point p) {
            return _currentComponent.getLocation(p);
        }

        public void setLocation(Point p) {
            _currentComponent.setLocation(p);
        }

        public int getWidth() {
            return _currentComponent.getWidth();
        }

        public int getHeight() {
             return _currentComponent.getHeight();
        }

        public int getX() {
             return _currentComponent.getX();
        }

        public int getY() {
             return _currentComponent.getY();
        }

        public void internalFrameOpened(InternalFrameEvent e) {
            
        }

        public void internalFrameClosing(InternalFrameEvent e) {
            // clear out the multiselect list if this is closing
            if(_multiSelectMember
                    && _frame.getDesktopPane() != null
                    && _frame.getDesktopPane().getDesktopManager() != null
                    && _frame.getDesktopPane().getDesktopManager() instanceof WorkspacePaneDM){
                WorkspacePaneDM dm = ((WorkspacePaneDM) _frame.getDesktopPane().getDesktopManager());
                dm.toggleMultiSelect(this);
               
            }
        }

        public void internalFrameClosed(InternalFrameEvent e) {
        }

        public void internalFrameIconified(InternalFrameEvent e) {
            _currentComponent = _frame.getDesktopIcon();   
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {
            _currentComponent = _frame;
        }

        public void internalFrameActivated(InternalFrameEvent e) {
            
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
            
        }

    }





}
