package edu.vt.workspace.components.utilities;

import edu.vt.workspace.components.AWTextFrame;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.EntityManager;
import edu.vt.workspace.data.Range;
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
public class TextFrameMenuHelper implements ActionListener {
    private static final String NEW_TYPE_MSG = "New type...";
    private static final String TYPE_MENU_LABEL = "Change type of %";
    private static final String[] CORE_MENU_OPTIONS = {
        "Clone",
        "Show Neighbors",
        "Find connection to...",
        "Link to..."
    };
    private static final String[] ENTITY_MENU_OPTIONS = {
        "Use % as alias for ...",
        "Remove entity %",};
    private static final String[] TEXT_MENU_OPTIONS = {
        "Mark % as entity"
    };
    
    private static final TextFrameMenuHelper _instance = new TextFrameMenuHelper();

    private JMenuItem[] _coreMenuItems;
    private JMenuItem[] _entityMenuItems;
    private JMenuItem[] _textMenuItems;
    
    private JPopupMenu _menu = new JPopupMenu();
    private JMenu _typeMenu = new JMenu();
    private AWEntity _currentEntity = null;
    private String _currentText = null;
    private Range _currentRange = null;
    private AWTextFrame _currentFrame = null;
    private ActionListener _typeMenuListener;

     /**
     * This is a singleton class so all access should be through this method.
     * @return the singleton instance of this class
     */
    public static TextFrameMenuHelper getInstance() {
        return _instance;
    }


    /**
     * In the contrustor, we setup the three lists of menu items. We then store the
     * resulting {@code JMenuItem} objects in seperate lists so we can construct the
     * contents of the popupmenu on the fly.
     *
     * Note that the {@code ActionCommand} is set the same as the text of the menu item.
     * These are used below to determine which menu item was chosen.
     */
    private TextFrameMenuHelper() {
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

        _entityMenuItems = new JMenuItem[ENTITY_MENU_OPTIONS.length];
        index = 0;
        for (String name : ENTITY_MENU_OPTIONS) {
            menuItem = new JMenuItem(name);
            menuItem.setActionCommand(name);
            menuItem.addActionListener(this);
            _entityMenuItems[index++] = menuItem;
        }

        _textMenuItems = new JMenuItem[TEXT_MENU_OPTIONS.length];
        index = 0;
        for (String name : TEXT_MENU_OPTIONS) {
            menuItem = new JMenuItem(name);
            menuItem.setActionCommand(name);
            menuItem.addActionListener(this);
            _textMenuItems[index++] = menuItem;
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
     * This method loads the core menu items and returns the resulting menu.
     * @param frame the {@code AWSInternalFrame} that is requesting the context menu
     * @return a {@code JPopupMenu} to use
     */
    public JPopupMenu loadMenu(AWTextFrame frame) {
        _currentFrame = frame;
        _menu.removeAll();
        for (JMenuItem menuItem : _coreMenuItems) {
            _menu.add(menuItem);
        }
        return _menu;
    }

    /**
     * This method loads the core menu items as well as the menu items associated
     * with a particular entity (this is used to change the text of the menu item
     * and to set a field so we can operate on the entity when the callback is called).
     *
     * @param frame the {@code AWSInternalFrame} that is requesting the context menu
     * @param entity the {@code AWEntity} the this menu is being called on
     * @param range the {@code Range} in the text in which the entity appears
     * @return a {@code JPopupMenu} to use
     */
    public JPopupMenu loadMenu(AWTextFrame frame, AWEntity entity, Range range) {
        _currentFrame = frame;
        _menu.removeAll();
        _currentEntity = entity;
        _currentRange = range;

        for (JMenuItem menuItem : _entityMenuItems) {
            menuItem.setText(menuItem.getActionCommand().replace("%", entity.getValue()));
            _menu.add(menuItem);
        }

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

        _menu.addSeparator();
        for (JMenuItem menuItem : _coreMenuItems) {
            _menu.add(menuItem);
        }


        return _menu;
    }


    /**
     * This method loads the core menu items as well as the menu items associated
     * with a raw text selection.
     *
     * @param frame the {@code AWSInternalFrame} that is requesting the context menu
     * @param text the selected text that this menu might operate on
     * @param range the {@code Range} in the text in which the text appears
     * @return a {@code JPopupMenu} to use
     */
    public JPopupMenu loadMenu(AWTextFrame frame, String text, Range range) {
        _currentFrame = frame;
        
        _menu.removeAll();
        _currentText = text;
        _currentRange = range;

        for (JMenuItem menuItem : _textMenuItems) {
            menuItem.setText(menuItem.getActionCommand().replace("%", text));
            _menu.add(menuItem);
        }
        _menu.addSeparator();
        for (JMenuItem menuItem : _coreMenuItems) {
            _menu.add(menuItem);
        }
        return _menu;
    }

   
    /**
     * The action callback that handles the actual behavior of all of the menu items.
     * @param e the {@code ActionEvent} associated with making a menu selection
     */
    public void actionPerformed(ActionEvent e) {
        if ("Clone".equals(e.getActionCommand())) {
            AWController.getInstance().displayFile(_currentFrame.getDocument(), true);
        } else if ("Show Neighbors".equals(e.getActionCommand())) {
            AWController.getInstance().findNeighbors(_currentFrame);
        } else if ("Find connection to...".equals(e.getActionCommand())) {
            AWController.getInstance().findConnection(_currentFrame);
        } else if ("Link to...".equals(e.getActionCommand())) {
            AWController.getInstance().linkTo(_currentFrame);
        } else if ("Remove entity %".equals(e.getActionCommand())) {
            AWController.getInstance().removeEntity(_currentEntity, _currentFrame, _currentRange);
        }else if ("Mark % as entity".equals(e.getActionCommand())) {
            AWController.getInstance().markEntity(_currentText, _currentFrame, _currentRange);
        } else if ("Use % as alias for ...".equals(e.getActionCommand())) {
            AWController.getInstance().makeAlias(_currentEntity);
        }
    }
}
