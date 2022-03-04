package edu.vt.workspace.plaf;


import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.utilities.WorkspacePaneDM;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.JInternalFrame;
import javax.swing.event.MouseInputAdapter;
import javax.swing.SwingConstants;
import java.awt.event.MouseEvent;
import javax.swing.plaf.metal.MetalInternalFrameUI;
import java.awt.Point;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Dimension;


/**
 * This is class overrides the Metal L&F to allow for
 * multiple window dragging.
 *
 *
 * Created: Mon Feb 16 13:22:01 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class AWInternalFrameUI extends MetalInternalFrameUI{

    private boolean _multiDragging = false;

    /**
     * Creates a new <code>AWInternalFrameUI</code> instance.
     *
     * @param f
     */
    public AWInternalFrameUI(JInternalFrame f) {
	super(f);
	
    }

    /**
     * Describe <code>createUI</code> method here.
     *
     * @param c
     * @return a <code>ComponentUI</code> value
     */
    public static ComponentUI createUI(JComponent c) {
	return new AWInternalFrameUI((JInternalFrame)c);
    }

    /**
     * Describe <code>createBorderListener</code> method here.
     *
     * @param JInternalFrame a <code>JInternalFrame</code> value
     * @return a <code>MouseInputAdapter</code> value
     */
   @Override
   public final MouseInputAdapter createBorderListener(final JInternalFrame JInternalFrame) {
       return new BorderListener1();
    }
 

    private class BorderListener1 extends BorderListener implements SwingConstants{
	// since the x,y variables are hidden in the parent class, I need to make my own set
	Point ep = new Point(); // event location
	Point ap = new Point(); // absolute location (within app, not display)
	Rectangle initialBounds;

	@Override
	public void mouseClicked(MouseEvent me){
	    if (me.isControlDown()) {
		// add the current object to the list of selected frames or subtract it
		// if it is already present
		
		((WorkspacePaneDM)getDesktopManager()).toggleMultiSelect(((AWInternalFrame)frame).getDragProxy());
	    }else {
		super.mouseClicked(me);
	    }
	}

	@Override
	public void mousePressed(MouseEvent me){
	    // if this is one of the selected frames and in border, send event to DM
	    // DM should respond if it wants to handle this (more than one frame)
	    // or if it should go to default
	   
	    if (me.isControlDown() || ((AWInternalFrame)frame).getDragProxy().isMultiSelectMember()) {
		if (me.isControlDown()
		    ||! ((WorkspacePaneDM)getDesktopManager()).isMultipleDrag() 
		    ||! (me.getSource() == getNorthPane())) {
		    super.mousePressed(me);
		    return;
		}
		
		// do multi drag stuff stuff
		ap = SwingUtilities.convertPoint((Component)me.getSource(),
						 me.getX(), me.getY(), null);
		try { frame.setSelected(true); }
		catch (PropertyVetoException e1) { }
					
		ep.setLocation(me.getX(), me.getY());
		((WorkspacePaneDM)getDesktopManager()).beginDraggingFrames(((AWInternalFrame)frame).getDragProxy());
		_multiDragging = true;
	
	
	   }else {
	       // this isn't in a multi-selection and it isn't being added
		// reset the selected frames to a single item
	       ((WorkspacePaneDM)getDesktopManager()).clearMultiSelect(((AWInternalFrame)frame).getDragProxy());
	       super.mousePressed(me);
	    }
	}

	@Override
	public void mouseReleased(MouseEvent me){
	    ep.setLocation(0,0);
	    ap.setLocation(0,0);
	    initialBounds = null;
	    _multiDragging = false;
	    super.mouseReleased(me);
            frame.getParent().repaint();
	}

	@Override
	public void mouseDragged(MouseEvent me){
	    if (_multiDragging) {
		// Handle a MOVE
		Point p = SwingUtilities.convertPoint((Component)me.getSource(),
						      me.getX(), me.getY(), null);
		int deltaX = p.x - ap.x;
		int deltaY = p.y - ap.y;
		Insets i = frame.getInsets();
		Dimension size = frame.getParent().getSize();
		int newX, newY;
		ap.setLocation(p);
		// make sure that we don't drag the current frame off of the display

		newX = frame.getX() + deltaX;
		newY = frame.getY() + deltaY;

		if(newX + i.left <= -ep.x)
		    newX = -ep.x - i.left + 1;
		if(newY + i.top <= -ep.y)
		    newY = -ep.y - i.top + 1;
		if(newX + ep.x + i.right >= size.width)
		    newX = size.width - ep.x - i.right - 1;
		if(newY + ep.y + i.bottom >= size.height)
		    newY =  size.height - ep.y - i.bottom - 1;
		
		deltaX = newX - frame.getX();
		deltaY = newY - frame.getY();


		((WorkspacePaneDM)getDesktopManager()).dragFrames(((AWInternalFrame)frame).getDragProxy(), deltaX, deltaY);

		return;
	    }


	    // this is an ugly hack, but it kind of works at the moment
	    ((AWInternalFrame)frame).disableMaximum();
	    
	    super.mouseDragged(me);
	    ((AWInternalFrame)frame).restoreMaximum();
	 
	}

	
    } // end of BorderListener1


} // end of AWInternalFrameUI
