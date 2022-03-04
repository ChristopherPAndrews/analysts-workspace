/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.vt.workspace.components.utilities;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author cpa
 */
public interface MultiDraggableComponent {
    public void setMultiSelectMember(boolean select);
    public boolean isMultiSelectMember();
    public void hilite(boolean on);
    public void drag(int deltaX,int deltaY);
    public Rectangle getBounds();
    public Point getLocation();
    public Point getLocation(Point p);
    public void setLocation(Point p);
    public int getWidth();
    public int getHeight();
    public int getX();
    public int getY();
}
