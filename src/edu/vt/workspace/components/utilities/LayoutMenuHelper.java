package edu.vt.workspace.components.utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class is a helper class that creates and manages a menu for doing layout
 * of selected objects.
 *
 * @author cpa
 */
public class LayoutMenuHelper implements ActionListener {
    private static final String[] ALIGN_VERT_OPTIONS = {
        "Left",
        "Vertical Center",
        "Right"
    };
    private static final String[] ALIGN_HORIZ_OPTIONS = {
        "Top",
        "Horizontal Center",
        "Bottom"
    };
    private static final String[] DISTRIBUTE_OPTIONS = {
        "Distribute horizontally",
        "Distribute vertically"
    };
    private static final String[] LAYOUT_OPTIONS = {
        "Timeline",
        "Circular Layout",
        "Force Directed Layout"
        
    };

    private static final LayoutMenuHelper _instance = new LayoutMenuHelper();


    private JPopupMenu _menu = new JPopupMenu();
    private int _coreLength;

    public static LayoutMenuHelper getInstance() {
        return _instance;
    }

    private LayoutMenuHelper() {
        buildMenu();
        _coreLength = _menu.getSubElements().length;
    }


    private void loadMenu(JComponent menu, String[] items){
        JMenuItem menuItem;
        for (String name: items){
            menuItem = new JMenuItem(name);
            menuItem.setActionCommand(name);
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
    }

    private void buildMenu(){
        JMenu subMenu;

        subMenu = new JMenu("Align Vertically");
        loadMenu(subMenu, ALIGN_VERT_OPTIONS);
        _menu.add(subMenu);

        subMenu = new JMenu("Align Horizontally");
        loadMenu(subMenu, ALIGN_HORIZ_OPTIONS);
        _menu.add(subMenu);

        _menu.addSeparator();

         loadMenu(_menu, DISTRIBUTE_OPTIONS);
        _menu.addSeparator();
        loadMenu(_menu, LAYOUT_OPTIONS);
    }



    public JPopupMenu getMenu(){
        if (_menu.getSubElements().length != _coreLength){
            _menu.removeAll();
            buildMenu();
        }
        return _menu;
    }



    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Left")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.WEST);
        } else if (e.getActionCommand().equals("Vertical Center")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.NORTH_SOUTH);
        } else if (e.getActionCommand().equals("Right")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.EAST);
        } else if (e.getActionCommand().equals("Top")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.NORTH);
        } else if (e.getActionCommand().equals("Horizontal Center")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.EAST_WEST);
        } else if (e.getActionCommand().equals("Bottom")){
            LayoutManager.getInstance().alignSelected(LayoutManager.AlignmentEdge.SOUTH);
        } else if (e.getActionCommand().equals("Distribute horizontally")){
            LayoutManager.getInstance().distributeSelected(LayoutManager.DistibutionDirection.HORIZONTAL);
        } else if (e.getActionCommand().equals("Distribute vertically")){
            LayoutManager.getInstance().distributeSelected(LayoutManager.DistibutionDirection.VERTICAL);
        } else if (e.getActionCommand().equals("Timeline")){
            LayoutManager.getInstance().organizeSelectedTemporally();
        }else if (e.getActionCommand().equals("Circular Layout")){
            LayoutManager.getInstance().layoutGraph(LayoutManager.GraphLayoutType.CIRCULAR);
        }else if (e.getActionCommand().equals("Force Directed Layout")){
            LayoutManager.getInstance().layoutGraph(LayoutManager.GraphLayoutType.FORCE_DIRECTED);
        }
    }



}
