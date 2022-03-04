package edu.vt.workspace.components.utilities;

import java.util.EventListener;

/**
 *
 * @author cpa
 */
public interface HighlightListener extends EventListener{

    public void addHighlight(HighlightEvent he);
    public void updateHighlight(HighlightEvent he);
    public void removeHighlight(HighlightEvent he);
}
