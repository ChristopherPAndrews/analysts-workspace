package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.EntityListPanel;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class provides our basic entity browser. Most of the work is actually performed by the
 * {@code EntityListPanel}. This just attaches a {@code MouseAdapter} to the list and uses
 * it to open entities on double click events.
 * 
 * @author cpa
 */
public class AWEntityBrowser extends AWInternalFrame implements AWSavable{
private EntityListPanel _panel;


    /**
     * Instantiate the main panel, which does most of the hard work managing the actual entity list.
     * This also adds a mouse listener to the list to set the behavior of double clicking an entry of the list.
     */
    public AWEntityBrowser() {
        super("Entity Browser");
        _panel = new EntityListPanel();
        _panel.entityList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    AWEntity entity = _panel.getSelectedEntity();
                    AWController.getInstance().displayEntity(entity);
                }
            }

        });
        add(_panel);
        pack();
    }

    /**
     * This passes the sort type down to the panel. This is necessary for the save functionality.
     * @param mode this is a {@code String} that corresponds to one of the accepted sort modes of the panel
     */
    public void setSortBy(String mode){
        _panel.setSortBy(mode);
    }
    
    /**
     * Get the sort type of this view.
     * @return the sort type
     */
    public String getSortBy(){
        return _panel.getSortBy();
    }

    /**
     * This passes the current type down to the panel. Again, this is for the save function.
     * @param type this is a {@code String} object that should correspond to one of the entity types in the system
     */
    public void setCurrentType(String type){
        _panel.setCurrentType(type);
    }

    
    /**
     * Get the current entity type
     * @return the current entity type
     */
    public String getCurrentType(){
        return _panel.getCurrentType();
    }

    /**
     * Implementation of the write handler for saving. The only things that the {@code AWEntityBrowser}
     * cares about other than the default frame components are the sort type and the current entity type.
     * @param writer
     */
    @Override
    public void writeData(AWWriter writer) {
         super.writeData(writer);
         writer.write("sortBy", _panel.getSortBy());
         writer.write("currentType", _panel.getCurrentType());
    }
}
