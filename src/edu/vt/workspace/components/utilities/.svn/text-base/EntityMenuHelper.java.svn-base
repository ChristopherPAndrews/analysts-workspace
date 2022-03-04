package edu.vt.workspace.components.utilities;

import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.EntityManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This class provides the context menu for the text frames. It sets up the menu
 * items and handles all of the actions.
 *
 * @author cpa
 */
public class EntityMenuHelper implements ActionListener {
    private static final String NEW_TYPE_MSG = "New type...";
    private static final String TYPE_MENU_LABEL = "Change type of %";
   
    private static final String[] CORE_MENU_OPTIONS = {
        "Use % as alias for ...",
        "Remove entity",
        "Find connection to..."
    };
    
    
    private static final EntityMenuHelper _instance = new EntityMenuHelper();

    private JMenuItem[] _coreMenuItems;
    private JMenuItem[] _textMenuItems;
    
    private JPopupMenu _menu = new JPopupMenu();
    private JMenu _typeMenu = new JMenu();
    private AWEntity _currentEntity = null;
    private AWInternalFrame _currentFrame = null;
    private ActionListener _typeMenuListener;

     /**
     * This is a singleton class so all access should be through this method.
     * @return the singleton instance of this class
     */
    public static EntityMenuHelper getInstance() {
        return _instance;
    }


    /**
     * In the constructor, we setup the three lists of menu items. We then store the
     * resulting {@code JMenuItem} objects in separate lists so we can construct the
     * contents of the popupmenu on the fly.
     *
     * Note that the {@code ActionCommand} is set the same as the text of the menu item.
     * These are used below to determine which menu item was chosen.
     */
    private EntityMenuHelper() {
        JMenuItem menuItem;
        int index;

        _coreMenuItems = new JMenuItem[CORE_MENU_OPTIONS.length];
        index = 0;
        for (String name : CORE_MENU_OPTIONS) {
            menuItem = new JMenuItem(name);
            menuItem.setActionCommand(name);
            menuItem.addActionListener(this);
            _coreMenuItems[index++] = menuItem;
        }


        _typeMenuListener = new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                String type;
                if (NEW_TYPE_MSG.equals(e.getActionCommand())){
                    type = AWController.getInstance().askForInput("Create new type", "Please enter a label for the new type \nto use for the entity <" + _currentEntity.getValue() + ">");
                    if (type == null) // user canceled
                        return;
                }else{
                    type = e.getActionCommand();
                   
                } 
                EntityManager.getInstance().changeEntityType(_currentEntity, type);
            }

        };
    }

   

    /**
     * This loads the menu for the specific entity
     *
     * @param entity the {@code AWEntity} the this menu is being called on
     * @return a {@code JPopupMenu} to use
     */
    public JPopupMenu loadMenu(AWEntity entity, AWInternalFrame frame) {

        _menu.removeAll();
        _currentEntity = entity;
        _currentFrame = frame;

        _typeMenu.removeAll();
        _typeMenu.setText(TYPE_MENU_LABEL.replace("%", entity.getValue()));
        Set<String> types = EntityManager.getInstance().getEntityTypes();
        JMenuItem item;
        for (String type: types){
            item = new JMenuItem(type);
            item.setActionCommand(type);
            item.addActionListener(_typeMenuListener);
            _typeMenu.add(item);
        }

        item = new JMenuItem(NEW_TYPE_MSG);
        item.setActionCommand(NEW_TYPE_MSG);
        item.addActionListener(_typeMenuListener);
        _typeMenu.add(item);
        _menu.add(_typeMenu);

        for (JMenuItem menuItem : _coreMenuItems) {
             menuItem.setText(menuItem.getActionCommand().replace("%", entity.getValue()));
            _menu.add(menuItem);
        }


        return _menu;
    }



   
    /**
     * The action callback that handles the actual behavior of all of the menu items.
     * @param e the {@code ActionEvent} associated with making a menu selection
     */
    public void actionPerformed(ActionEvent e) {
        if ("Use % as alias for ...".equals(e.getActionCommand())) {
            AWController.getInstance().makeAlias(_currentEntity);
        } else if ("Remove entity".equals(e.getActionCommand())){
            AWController.getInstance().removeEntity(_currentEntity, null, null);
        }else if ("Find connection to...".equals(e.getActionCommand())){
            AWController.getInstance().findConnection(_currentFrame);
        }
    }
}
