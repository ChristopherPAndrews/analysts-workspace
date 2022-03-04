package edu.vt.workspace.components.utilities;

import edu.vt.workspace.components.SimpleLink;
import java.util.EventListener;

/**
 * Describe class SimpleLinkListener here.
 *
 *
 * Created: Tue Mar  3 12:52:19 2009
 *
 * @author <a href="mailto:cpa@gambit.cs.vt.edu">Christopher Andrews</a>
 * @version 1.0
 */
public interface SimpleLinkListener extends EventListener{

    public void linkClosing(SimpleLink link);
    public void linkSelected(SimpleLink link);
    public void linkDeselected(SimpleLink link);
    public void linkChanging(SimpleLink link);

}
