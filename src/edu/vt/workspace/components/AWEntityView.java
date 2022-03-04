package edu.vt.workspace.components;

import edu.vt.workspace.components.utilities.ColoredIcon;
import edu.vt.workspace.components.utilities.EntityChangeEvent;
import edu.vt.workspace.components.utilities.EntityChangeListener;
import edu.vt.workspace.components.utilities.EntityMenuHelper;
import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWEntity;
import edu.vt.workspace.data.AWSavable;
import edu.vt.workspace.data.AWWriter;
import edu.vt.workspace.data.EntityManager;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;

/**
 *
 * @author cpa
 */
public class AWEntityView extends AWFileList implements EntityChangeListener, AWSavable{
    private AWEntity _entity;
    private HashMap<AWEntityView, EntityLink> _entityLinks = new HashMap<AWEntityView, EntityLink>(5);
    private ArrayList<AWEntity> _entityList = new ArrayList<AWEntity>(1);

    /**
     * This is an empty constructor to be used by the save mechanism.
     */
    public AWEntityView(){
        super("",null, null);
        setIconifiable(true);
        

    }


    /**
     * Creates a new <code>AWEntityView</code> instance based on an entity.
     *
     * @param entity the entity being used
     * @param controller the master class that this can make requests to
     */
    public AWEntityView(AWEntity entity, Queriable controller) {
        super(entity.toString(),null, controller);
        
        _entity = entity;
        if (_entity != null)
            setLabel(_entity.toString(), new ColoredIcon(EntityManager.getInstance().getColor(_entity), 15));
        _entityList.add(entity);
        setIconifiable(true);
        updateUI();

    }
    

    @Override
    protected void postOpenInit(){
        super.postOpenInit();
        Container parent;
        if (this.isIcon){
            parent = desktopIcon.getParent();
        }else{
            parent = getParent();
        }

        EntityManager.getInstance().addEntityChangeListener(this);

        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    JPopupMenu menu = EntityMenuHelper.getInstance().loadMenu(AWEntityView.this.getEntity(), AWEntityView.this);
                     menu.show(AWEntityView.this,
                            me.getX(),
                            me.getY());
                }
            }
        });
         
        
    }


    
    /**
     * Set the Entity to display.
     *
     * @param entityID the name of the entity
     */
    public void setEntityID(String entityID){
        _entity = EntityManager.getInstance().getEntity(entityID);
         _entityList.set(0, _entity);
        setTitle(_entity.toString());
        setLabel(_entity.toString(), new ColoredIcon(EntityManager.getInstance().getColor(_entity), 15));
        updateUI();
    }

    public AWEntity getEntity() {
        return _entity;
    }

    /**
     * Get the entity link linking this view to some other {@code EntityView}, if it exists. 
     * If it doesn't, return null.
     * @param view the {@code EntityView} that we want to know about
     * @return an {@code EntityLink} connecting to the far {@code EntityView} or null if such a link doesn't exist.
     */
    public EntityLink getEntityLink(AWEntityView view){
        return _entityLinks.get(view);
    }
    
    
        @Override
    public void writeData(AWWriter writer) {
        super.writeData(writer);
        writer.write("entityID", _entity.getValue());
    }

    @Override
    protected boolean loadList() {
        if (_entity != null){
         _list.setListData(_entity.getDocs().toArray(new AWDocument[0]));
         return true;
        }else
            return false;
    }

    public void addEntityLink(AWEntityView view, EntityLink link) {
       _entityLinks.put(view, link);
    }

    public void addEntity(EntityChangeEvent ece) {
        // ignore this, we don't care
    }

    public void removeEntity(EntityChangeEvent ece) {
 
        if (ece.getEntity() == _entity)
            dispose();
    }

    public void entityChanged(EntityChangeEvent ece) {
       if (ece.getEntity() == _entity && ece.getType() == EntityChangeEvent.ChangeType.TYPE_CHANGE){
            // update the label and the title
            JLabel label = getLabel();
            Icon icon = label.getIcon();
            label.setText(_entity.toString());
            if (icon != null && icon instanceof ColoredIcon) {
                ((ColoredIcon) icon).setColor(EntityManager.getInstance().getColor(_entity));
            }
            setTitle(_entity.toString());

            // update the desktop icon
            if (isIcon()) {
                // we need to move the frame if we are iconified because the
                setLocation(this.desktopIcon.getLocation());
            }
            updateUI();
        }else if (ece.getType() == EntityChangeEvent.ChangeType.ALIAS_CHANGE){
            if (ece.getEntity() == _entity){
                // a new alias was added to this entity, need to update the file and link lists
                initializeList();
            }else if (ece.getEntity().getAliases().contains(_entity.getValue())){
                // this entity is actually the new alias for the changed entity, so go away
                if (AWController.getInstance().selectEntity(ece.getEntity())){
                    // the entity is already in the workspace
                    dispose();
                } else{
                    // entity is not yet open so morph into it
                    _entity = ece.getEntity();
                    setTitle(_entity.toString());
                    setLabel(_entity.toString(), new ColoredIcon(EntityManager.getInstance().getColor(_entity), 15));
                    initializeList();
                    updateUI();
                }
            }
        }
    }
    
    
   
    
    
    
    @Override
    public Collection<AWEntity> getTargetEntities(){
        return _entityList;
    }
    
    @Override
    public Collection<AWDocument> getTargetDocuments(){
        return _entity.getDocs();
    }

    
        
}
