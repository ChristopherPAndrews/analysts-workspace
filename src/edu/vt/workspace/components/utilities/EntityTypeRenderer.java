package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.EntityManager;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * This class provides a basic renderer for entity type information in lists and
 * combo boxes.
 * @author cpa
 */
public class EntityTypeRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            setOpaque(true);
            setText((String)value);
            Color color = EntityManager.getInstance().getColor((String)value);
            if (color != null)
                setIcon(new ColoredIcon(color, 10));
            else
                setIcon(null);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {

                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }
