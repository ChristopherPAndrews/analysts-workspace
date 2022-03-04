package edu.vt.workspace.data;

import edu.vt.workspace.components.AWDocumentView;
import edu.vt.workspace.components.AWEntityView;
import edu.vt.workspace.components.AWFileList;
import edu.vt.workspace.components.AWInternalFrame;
import edu.vt.workspace.components.AWSearchResults;
import edu.vt.workspace.components.EntityLink;
import edu.vt.workspace.components.DocumentListLink;
import edu.vt.workspace.components.SimpleLink;
import edu.vt.workspace.components.WorkspacePane;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * This class manages all of the links in the system.
 * 
 * @author Christopher Andrews [cpa@cs.vt.edu]
 */
public class AWLinkManager {

    private static final AWLinkManager _instance = new AWLinkManager();
    private WorkspacePane _workspace;
    private FrameListener _listener;
    private ArrayList<AWDocumentView> _documentViewTargets;
    private ArrayList<AWEntityView> _entityViewTargets;
    private ArrayList<AWFileList> _fileListTargets;
    private ArrayList<SimpleLink> _links;
    private ArrayList<SimpleLink> _userLinks;
    private ArrayList<SimpleLink> _selectedLinks;
    public static final Color HIGHLIGHTED_COLOR = Color.CYAN;
    public static final Color NORMAL_COLOR = Color.GRAY;
    public static final Color CLONE_COLOR = Color.CYAN;
    public static final Color ENTITY_LINK_COLOR = Color.YELLOW;
    private LinkTarget _currentSource = null;


    private AWLinkManager() {
        _listener = new FrameListener();


        _documentViewTargets = new ArrayList<AWDocumentView>(50);
        _entityViewTargets = new ArrayList<AWEntityView>(50);
        _fileListTargets = new ArrayList<AWFileList>(50);

        _links = new ArrayList<SimpleLink>(50);
        _userLinks = new ArrayList<SimpleLink>(25);
        _selectedLinks = new ArrayList<SimpleLink>(15);
    }

    public static AWLinkManager getInstance() {
        return _instance;
    }

    public void setWorkspace(WorkspacePane workspace) {
        _workspace = workspace;
        _workspace.addContainerListener(new ContainerListener() {

            public void componentAdded(ContainerEvent e) {
                if (e.getChild() instanceof LinkTarget) {
                    register(((LinkTarget) e.getChild()));
                } else if (e.getChild() instanceof JInternalFrame.JDesktopIcon) {
                    if (((JInternalFrame.JDesktopIcon) e.getChild()).getInternalFrame() instanceof LinkTarget) {
                        register((LinkTarget) ((JInternalFrame.JDesktopIcon) e.getChild()).getInternalFrame());
                    }
                }
            }

            public void componentRemoved(ContainerEvent e) {
                if (e.getChild() instanceof LinkTarget) {
                    unregister(((LinkTarget) e.getChild()));
                } else if (e.getChild() instanceof JInternalFrame.JDesktopIcon) {
                    if (((JInternalFrame.JDesktopIcon) e.getChild()).getInternalFrame() instanceof LinkTarget) {
                        unregister((LinkTarget) ((JInternalFrame.JDesktopIcon) e.getChild()).getInternalFrame());
                    }
                }
            }
        });

    }

    public void paintLinks(Graphics2D g2) {
        for (SimpleLink link : _links) {
            link.paint(g2);
        }

        for (SimpleLink link : _userLinks) {
            link.paint(g2);
        }
    }

    public void paintSelectedLinks(Graphics2D g2) {
        for (SimpleLink link : _selectedLinks) {
            link.paint(g2);
        }
    }

    public SimpleLink createUserLink(AWInternalFrame t1, AWInternalFrame t2, Color color) {
        SimpleLink link = new SimpleLink(t1, t2);
        link.setKeepVisible(true);
        link.setMutable(true);
        link.setColor(color);
        link.setCanvas(_workspace);
        _userLinks.add(link);

        AWDataManager.getInstance().addLink(link);
        return link;

    }

    public void addLink(SimpleLink link) {
        link.setCanvas(_workspace);
        _userLinks.add(link);
    }

    public void highlightLinks(Collection<AWDocument> documents) {
        //_selectedLinks.clear();
        for (SimpleLink link : _links) {
            AWInternalFrame frame = link.getFrames()[1];
            if (frame instanceof AWDocumentView) {
                if (documents.contains(((AWDocumentView) frame).getDocument())) {
                    link.setSelected(true);
                    _selectedLinks.add(link);
                } else {
                    link.setSelected(false);
                    _selectedLinks.remove(link);
                }
            }
        }
    }

    public void tryLinkSelection(Point point, int clickCount) {
        for (SimpleLink link : _selectedLinks) {
            link.setSelected(false);
        }
        _selectedLinks.clear();


        for (SimpleLink link : _links) {
            if (link.containsPoint(point)) {
                _selectedLinks.add(link);
                link.select(clickCount);
                return;
            }
        }

        for (SimpleLink link : _userLinks) {
            if (link.containsPoint(point)) {
                _selectedLinks.add(link);
                link.select(clickCount);
                return;
            }
        }
    }

    public void deleteSelectedLink() {
        for (SimpleLink link : _selectedLinks) {
            if (link.isMutable()) {
                link.setSelected(false);
                link.severLink();
                _userLinks.remove(link); // _userLinks is the only place we may have mutable links
                AWDataManager.getInstance().removeLink(link);

            }
        }
        _selectedLinks.clear();
    }

    /**
     * Query the target for all of the entities and documents that it is associated with and register
     * this in the collection of targets.
     * @param target 
     */
    private void register(final LinkTarget target) {
        if (target instanceof AWDocumentView) {
            _documentViewTargets.add((AWDocumentView) target);
        } else if (target instanceof AWEntityView) {
            _entityViewTargets.add((AWEntityView) target);
        } else if (target instanceof AWFileList) {
            _fileListTargets.add((AWFileList) target);
        } else {
            System.out.println("Unknown type attempting to register: " + target.getClass().getName());
            return;
        }

        if (target instanceof AWInternalFrame) {
            ((AWInternalFrame) target).addInternalFrameListener(_listener);
        }

    }

    /**
     * Remove the target from all of the target lists.
     * @param target 
     */
    private void unregister(final LinkTarget target) {
        if (target instanceof AWDocumentView) {
            _documentViewTargets.remove((AWDocumentView) target);
        } else if (target instanceof AWEntityView) {
            _entityViewTargets.remove((AWEntityView) target);
        } else if (target instanceof AWFileList) {
            _fileListTargets.remove((AWFileList) target);
        } else {
            System.out.println("Unknown type attempting to unregister: " + target.getClass().getName());
            return;
        }

        if (target instanceof AWInternalFrame) {
            ((AWInternalFrame) target).removeInternalFrameListener(_listener);
        }
    }

    private void createCloneLink(AWDocumentView v1, AWDocumentView v2) {
        SimpleLink link = new SimpleLink(v1, v2);
        link.setColor(CLONE_COLOR);
        registerLink(link);
    }

    private void createDocumentLink(AWDocumentView v1, AWFileList v2) {
        SimpleLink link;
        if (v2 instanceof AWEntityView) {
            link = new DocumentListLink(v1, v2, ((AWEntityView) v2).getEntity());
        } else if (v2 instanceof AWSearchResults) {
            link = new DocumentListLink(v1, v2, ((AWSearchResults) v2).getQuery());
        } else {
            link = new SimpleLink(v1, v2);
        }
        
        
        link.setColor(NORMAL_COLOR);
        if (v1 == v2.getLinkSource()) {
            link.setDirected(true);
        }

        registerLink(link);
    }

    private void createListLink(AWFileList v1, AWDocumentView v2) {
        SimpleLink link;
        if (v1 instanceof AWEntityView) {
            link = new DocumentListLink(v1, v2, ((AWEntityView) v1).getEntity());
        } else if (v1 instanceof AWSearchResults) {
            link = new DocumentListLink(v1, v2, ((AWSearchResults) v1).getQuery());
        } else {
            link = new SimpleLink(v1, v2);
        }



        link.setColor(NORMAL_COLOR);
        if (v2 == v1.getLinkSource()) {
            link.setDirected(true);
            link.setReverse(true);
        }
        registerLink(link);
    }

    private void createEntityLink(AWEntityView v1, AWEntityView v2) {
        SimpleLink link = new EntityLink(v1, v2);
        link.setColor(ENTITY_LINK_COLOR);
        registerLink(link);
    }

    private void registerLink(SimpleLink link) {
        link.setCanvas(_workspace);
        _links.add(link);
        link.addSimpleLinkListener(AWController.getInstance());
    }

    private void findLinksFrom(LinkTarget source) {
        // a hack to cope with the quick swapping that happens when an {@code EntityLink} is selected
        if (!_selectedLinks.isEmpty()
                && _selectedLinks.get(0) instanceof EntityLink
                && _selectedLinks.get(0).isAnchor((AWInternalFrame) source)) {
            return;
        }


        // don't swap just because we had link selection
        if (_currentSource == source) {
            return;
        }


        for (SimpleLink link : _links) {
            link.setVisible(false);
        }
        for (SimpleLink link : _selectedLinks) {
            link.setSelected(false);
        }
        _selectedLinks.clear();
        _links.clear();
        _workspace.repaint();


        _currentSource = source;

        if (source instanceof AWDocumentView) {
            // starting from  document, we want all lists that contain this document
            AWDocument document = ((AWDocumentView) source).getDocument();
            for (AWEntityView view : _entityViewTargets) {
                if (view.getTargetDocuments().contains(document)) {
                    createDocumentLink((AWDocumentView) source, view);
                }
            }

            for (AWFileList view : _fileListTargets) {
                if (view.getTargetDocuments().contains(document)) {
                    createDocumentLink((AWDocumentView) source, view);
                }
            }

            // now look at the documents for clone links
            for (AWDocumentView view : _documentViewTargets) {
                if (view != source) {
                    if (view.getDocument() == document) {
                        createCloneLink((AWDocumentView) source, view);
                    }
                }
            }


        } else if (source instanceof AWFileList) {
            // starting from a file list -- we want all of the documents we are holding
            Collection<AWDocument> documents = source.getTargetDocuments();

            for (AWDocumentView view : _documentViewTargets) {
                if (documents.contains(view.getDocument())) {
                    createListLink((AWFileList) source, view);
                }
            }

            if (source instanceof AWEntityView) {
                // the file list is also an entity view -- connect to other entities
                for (AWEntityView view : _entityViewTargets) {
                    if (view != source) {
                        for (AWDocument document : documents) {
                            if (view.getTargetDocuments().contains(document)) {
                                createEntityLink((AWEntityView) source, view);
                                break;
                            }
                        }
                    }
                }
            }
            ((AWFileList) source).updateSelectedDocuments();
        }



    }

    private class FrameListener extends InternalFrameAdapter {

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            JInternalFrame frame = e.getInternalFrame();
            findLinksFrom((LinkTarget) e.getInternalFrame());
        }
    }
}
