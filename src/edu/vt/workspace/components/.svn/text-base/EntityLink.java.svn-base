package edu.vt.workspace.components;

import edu.vt.workspace.data.AWController;
import edu.vt.workspace.data.AWDocument;
import edu.vt.workspace.data.AWLinkManager;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.SwingUtilities;

/**
 * This is a specialized link for linking two entities based on shared documents.
 * At the moment, the only real difference is that it holds a collection of shared
 * documents.
 * @author cpa
 */
public class EntityLink extends SimpleLink {

    private Collection<AWDocument> _sharedDocs;
    private String _label = null;

    public EntityLink(AWEntityView view1, AWEntityView view2) {
        super(view1, view2);
        setColor(Color.yellow);
        setKeepVisible(false);
        _sharedDocs = new HashSet(view1.getEntity().getDocs());
        _sharedDocs.retainAll(view2.getEntity().getDocs());
    }

    public Collection<AWDocument> getSharedDocuments() {
        return _sharedDocs;
    }

    public void setLabel(String label) {
        _label = label;
    }

    public String getLabel() {
        if (_label != null) {
            return _label;
        } else if (_sharedDocs.size() == 1) {
            return "Connected by 1 document";
        } else {
            return "Connected by " + _sharedDocs.size() + " documents";
        }
    }

    @Override
    public void select(int clickCount) {
        super.select(clickCount);

        if (clickCount >= 2) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Vector<AWDocument> documents = new Vector<AWDocument>(_sharedDocs);
                    AWInternalFrame[] frames = getFrames();
                    String description = "Connecting " + ((AWEntityView) frames[0]).getEntity().getValue() + " and " + ((AWEntityView) frames[1]).getEntity().getValue();
                    AWInternalFrame frame = AWController.getInstance().displayFileList(documents, "Entity Connection", description, null);
                    SimpleLink link = AWLinkManager.getInstance().createUserLink(frame, frames[0], AWLinkManager.ENTITY_LINK_COLOR);
                    link = AWLinkManager.getInstance().createUserLink(frame, frames[1], AWLinkManager.ENTITY_LINK_COLOR);
                }
            });


        }

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            AWInternalFrame[] frames = getFrames();
            ((AWEntityView) frames[0]).setSelection(_sharedDocs);
            ((AWEntityView) frames[1]).setSelection(_sharedDocs);
        }

        // highlight documents in the space?
    }
}
