package edu.vt.workspace.components.utilities;


import edu.vt.workspace.components.AWInternalFrame;
import javax.swing.DefaultDesktopManager;
import java.awt.Rectangle;
import javax.swing.JInternalFrame;
import java.util.Vector;
import java.beans.PropertyVetoException;
import javax.swing.JDesktopPane;

/**
 * Describe class WorkspacePaneDM here.
 *
 *
 * Created: Tue Feb 10 12:47:54 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class WorkspacePaneDM extends DefaultDesktopManager {

    private Vector<MultiDraggableComponent> _selectedObjects = new Vector<MultiDraggableComponent>(5);

    /**
     * Creates a new <code>WorkspacePaneDM</code> instance.
     *
     */
    public WorkspacePaneDM() {
        super();
    }

    /**
     * This method overrides the desktop filling maximize code from the DefaultDesktopManager.
     * This code instead asks the component for a reasonable maximized value. This allows the component
     * to "smart maximize" and only grow enough to show all of its contents.
     *
     * @param f the <code>JInternalFrame</code> to be maximized
     */
    @Override
    public void maximizeFrame(JInternalFrame f) {
        if (f.isIcon()) {
            try {
                f.setIcon(false);
            } catch (PropertyVetoException pve) {
            }
        } else {
            f.setNormalBounds(f.getBounds());
            try {
                AWInternalFrame frame = (AWInternalFrame) f;
                Rectangle maxBounds = frame.getMaximizedBounds();
                setBoundsForFrame(f, maxBounds.x, maxBounds.y,
                        maxBounds.width, maxBounds.height);

                //new Throwable().printStackTrace();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                Rectangle desktopBounds = f.getParent().getBounds();
                setBoundsForFrame(f, 0, 0,
                        desktopBounds.width, desktopBounds.height);
            }
        }

        try {
            f.setSelected(true);
        } catch (PropertyVetoException pve) {
        }
    }


    /**
     * This method is called when a frame needs to be iconified. For reasons that I do not understand,
     * using my own desktopIcon subclass means that the icon is not visible when it is added. This
     * is a little mysterious as I have not yet found any code that actually makes the normal icon visible...
     * @param f
     */
    @Override
    public void iconifyFrame(JInternalFrame f){
        super.iconifyFrame(f);
        f.getDesktopIcon().setVisible(true);
        f.getDesktopIcon().getParent().repaint();

    }



    /**
     * Removes the desktopIcon from the desktop and restores the component. This overrides the
     * default behavior to restore the component to the current location of the icon.
     * @param f the <code>JInternalFrame</code> to deiconify
     */
    @Override
    public void deiconifyFrame(JInternalFrame f) {
        JInternalFrame.JDesktopIcon desktopIcon = f.getDesktopIcon();
        super.deiconifyFrame(f);
        f.setLocation(desktopIcon.getLocation());
        f.getParent().repaint();
    }

    /**
     * This method returns the bounds for the desktopIcon that is going to be placed
     * on the desktop when the component is iconified. This overrides the default behavior
     * so that the icon gets placed in the same location as the original component rather
     * than down at the bottom of the screen.
     *
     * @param f the <code>JInternalFrame</code> to be iconified
     * @return the new bounds for the icon
     */
    @Override
    protected Rectangle getBoundsForIconOf(JInternalFrame f) {
        // Get the icon for this internal component and its preferred size

        JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
        Rectangle rect = new Rectangle(f.getLocation(), icon.getPreferredSize());

        // now we need to make sure the new icon is actually on the desktop
        JDesktopPane d = f.getDesktopPane();

        if (d == null) {
            // the component has not yet been added to the parent; how about (0,0) ?
            rect.setLocation(0, 0);
            return rect;
        }

        Rectangle parentBounds = d.getBounds();

        rect.x = (rect.x < 0) ? 0 : rect.x;
        rect.x = (rect.x > parentBounds.width - rect.width) ? parentBounds.width - rect.width : rect.x;
        rect.y = (rect.y < 0) ? 0 : rect.y;
        rect.y = (rect.y > parentBounds.height - rect.height) ? parentBounds.height - rect.height : rect.y;


        return rect;
    }

    /**
     * This normal behavior for this code is to set a property on the component to
     * indicate that the icon has been setup properly and doesn't need to be recomputed.
     * I want the position to be recomputed every time, and while removing the functionality
     * of this is not necessary, I am doing it for completeness.
     * @param f the <code>JInternalFrame</code> of interest
     * @param value the value to be set (ignored)
     */
    @Override
    protected void setWasIcon(JInternalFrame f, Boolean value) {
    }

    /**
     * The original use for this method was to tell us if the component had been iconified
     * before. If it had, then the bounds for the icon are valid and can be reused.
     * This overrides this behavior so that the position of the icon will be recomputed
     * for every iconification.
     *
     * @param f the <code>JInternalFrame</code> of interest
     * @return <code>false</code>
     */
    @Override
    protected boolean wasIcon(JInternalFrame f) {
        return false;
    }


    /**
     * This method is the main one for handling selection sets. This is typically
     * in response to one of the multi-select actions (like control or shift click).
     * This adds or removes the passed in component depending on its current state.
     *
     * We also need to update the highlighting appropriately. If we only have a single item at the end,
     * it should not be highlighted, if we have more than one, we need to make sure that they are highlighted.
     * @param component the <code>JComponent</code> that is entering or leaving the selection set
     */
    public void toggleMultiSelect(MultiDraggableComponent component) {
        boolean present = _selectedObjects.remove(component);
        if (!present) {
            // it wasn't in there so add it
            _selectedObjects.add(component);
            component.setMultiSelectMember(true);
            if (_selectedObjects.size() >= 2) {
                component.hilite(true);
                _selectedObjects.firstElement().hilite(true);
            }
        } else {

            component.setMultiSelectMember(false);
            component.hilite(false);
            if (_selectedObjects.size() == 1) {
                _selectedObjects.firstElement().hilite(false);
            }
        }

    }

    public Vector<MultiDraggableComponent> getSelectedObjects() {
        return _selectedObjects;
    }

    public void clearMultiSelect(MultiDraggableComponent component) {
        for (MultiDraggableComponent current : _selectedObjects) {
            current.setMultiSelectMember(false);
            current.hilite(false);
        }

        _selectedObjects.clear();
        if (component != null) {
            _selectedObjects.add(component);
            component.setMultiSelectMember(true);
        }
    }

    public void clearMultiSelect() {
        clearMultiSelect(null);
    }

    public boolean isMultipleDrag() {
        // technically, if we have one we could treat it as a multidrag, but
        // why not let the underlying support for a single component handle this?
        return (_selectedObjects.size() > 1);
    }

    /**
     * Perform any of the necessary setup that should occur before starting a drag.
     * [This may not be necessary.]
     *
     * @param f
     */
    public void beginDraggingFrames(MultiDraggableComponent f) {
    }

    public void dragFrames(MultiDraggableComponent c, int deltaX, int deltaY) {
        for (MultiDraggableComponent component : _selectedObjects) {
            component.drag(deltaX, deltaY);

        }
    }
}



