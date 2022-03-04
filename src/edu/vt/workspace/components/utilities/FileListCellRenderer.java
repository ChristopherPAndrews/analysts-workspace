package edu.vt.workspace.components.utilities;

import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.FileManager;
import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author cpa
 */
public class FileListCellRenderer extends JLabel implements ListCellRenderer {
    public static enum DisplayMode {BY_TITLE, BY_NAME, BY_DATE, BY_RANK};
    private static final Color openDocBackground = new Color(191,255,236);//new Color(190, 255, 190);
    private static final DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
    

    private DisplayMode displayMode = DisplayMode.BY_TITLE;

    public FileListCellRenderer(JList list){
        super();
    }

    public void setDisplayMode(DisplayMode mode){
        displayMode = mode;
    }

    public DisplayMode getDisplayMode(){
        return displayMode;
    }

    public Component getListCellRendererComponent(JList list,
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus
    {
        AWDocument doc = (AWDocument) value;
        
        boolean seen = ((AWDocument) value).getSeen();
        List<AWInternalFrame> docs = FileManager.getInstance().getOpenDocs((AWDocument) value);

        if (displayMode == DisplayMode.BY_TITLE)
            setText(doc.getTitle());
        else if (displayMode == DisplayMode.BY_NAME)
            setText(doc.getName());
        else if (displayMode == DisplayMode.BY_DATE)
            setText(dateFormater.format(doc.getDate()) + " " + doc.getTitle());
        else if (displayMode == DisplayMode.BY_RANK)
            setText(doc.getTitle() + " [" +doc.getProperty("rank") +"]");

        if (isSelected) {
             setForeground(list.getSelectionForeground());
             setBackground(list.getSelectionBackground());

        } else {
            if (seen) {
                if (docs.size() > 0) {
                    setForeground(list.getForeground());
                    setBackground(openDocBackground);
                } else {
                    setForeground(new Color(180,180,180));
                    setBackground(list.getBackground());
                }
            } else {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}
