package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.LayoutMenuHelper;
import javax.swing.JComponent;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Describe class AWInternalFrameGlassPane here.
 *
 *
 * Created: Thu Feb 12 18:35:02 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public class AWInternalFrameGlassPane extends JComponent {

    public AWInternalFrameGlassPane() {
        MouseHelper helper = new MouseHelper();
        // pass all of the events in the text box up to the frame
        addMouseListener(helper);
        addMouseMotionListener(helper);

    }

    /**
     * Describe <code>paintComponent</code> method here.
     *
     * @param g a <code>Graphics</code> value
     */
    @Override
    public final void paintComponent(final Graphics g) {
        Rectangle clip = g.getClipBounds();
        Color alphaBlue = new Color(0.7f, 0.8f, 01.0f, 0.5f);
        g.setColor(alphaBlue);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
    }

    private class MouseHelper extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent me) {

            getRootPane().getParent().dispatchEvent(me);
        }

        @Override
        public void mouseDragged(MouseEvent me) {

            getRootPane().getParent().dispatchEvent(me);
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            getRootPane().getParent().dispatchEvent(me);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            getRootPane().getParent().dispatchEvent(me);
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            getRootPane().getParent().dispatchEvent(me);
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                LayoutMenuHelper.getInstance().getMenu().show(me.getComponent(),
                        me.getX(),
                        me.getY());
            } else {
                getRootPane().getParent().dispatchEvent(me);
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {

            getRootPane().getParent().dispatchEvent(me);
        }
    }
}



