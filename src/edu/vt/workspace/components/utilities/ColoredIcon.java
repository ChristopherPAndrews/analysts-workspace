/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.vt.workspace.components.utilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

/**
 * A simple square, colored Icon.
 *
 * This creates a simple colored square that can be used in JLabels as the Icon.
 * It is currently used to provide the color coding for the entity objects.
 */
public class ColoredIcon implements Icon {
    private int _size;
    private Color _color;

    public ColoredIcon(Color c, int size) {
        _color = c;
        _size = size;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(_color);
        g2.fillRect(x, y, _size, _size);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, _size, _size);
    }

    public int getIconWidth() {
        return _size;
    }

    public int getIconHeight() {
        return _size;
    }

    public Color getColor() {
        return _color;
    }

    public void setColor(Color color) {
        _color = color;
    }


}
