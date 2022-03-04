
package edu.vt.workspace.components.utilities;

import edu.vt.workspace.data.AWDocument;
import java.util.Comparator;

/**
 * This class provides a number of ways to compare AWDocument objects.
 *
 * The idea is to be able to switch this sort based on various properties of the documents.
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
public class AWDocumentComparator implements Comparator<AWDocument>{
    private SortType _sortType;

    public AWDocumentComparator() {
        this(SortType.TITLE);
    }
   
    public AWDocumentComparator(SortType sortType){
        _sortType = sortType;
    }


    public void setSortType(SortType sortType){
        _sortType = sortType;
    }

    public int compare(AWDocument t, AWDocument t1) {
        int result = 0;

        if (_sortType == SortType.TITLE){
            result = t.getTitle().compareToIgnoreCase(t1.getTitle());
        }else if (_sortType == SortType.NAME){
            result = t.getName().compareToIgnoreCase(t1.getName());
        }else if (_sortType == SortType.DATE){
            if (t.getDate() != null || t1.getDate() != null){ // make sure we can actually compare by date
           result = t.getDate().compareTo(t1.getDate());
           if (result == 0){
               result = t.getTitle().compareToIgnoreCase(t1.getTitle());
           }
            }
        }else if (_sortType == SortType.RANK){
            int r, r1;
            r = Integer.decode(t.getProperty("rank"));
            r1 = Integer.decode(t1.getProperty("rank"));
            result = r1 - r;
          //  System.out.println("compare " + t.getName() + "[" + r + "]" + " to " + t1.getName() + "[" + r + "]" + " -> " + result);
            
        }
        return result;
    }


  public enum SortType{
      TITLE,
      NAME,
      DATE,
      RANK;
  }
}
