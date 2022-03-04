
package edu.vt.workspace.search;

/**
 *
 * @author Christopher Andrews
 */
import edu.vt.workspace.data.AWDocument;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/** A utility for making Lucene Documents from a File. */
public class FileDocument {

 /**
  * Fetch the entire contents of a text file, and return it in a String.
  * This style of implementation does not throw Exceptions to the caller.
  *
  * @param aFile is a file which already exists and can be read.
  */
  static public String getContents(File aFile) {
    //...checks on aFile are elided
    StringBuilder contents = new StringBuilder();

    try {
      //use buffering, reading one line at a time
      //FileReader always assumes default encoding is OK!
      BufferedReader input =  new BufferedReader(new FileReader(aFile));
      try {
        String line = null; //not declared within while loop
        /*
        * readLine is a bit quirky :
        * it returns the content of a line MINUS the newline.
        * it returns null only for the END of the stream.
        * it returns an empty String if two newlines appear in a row.
        */
        while (( line = input.readLine()) != null){
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }
      }
      finally {
        input.close();
      }
    }
    catch (IOException ex){
      ex.printStackTrace();
    }

    return contents.toString();
  }







    /** Makes a document for a File.
    <p>
    The document has five fields:
    <ul>
    <li><code>path</code>--containing the pathname of the file, as a stored,
    untokenized field;
     * <li><code>title</code>--containing the title of the file;
    <li><code>modified</code>--containing the last modified date of the file as
    a field as created by <a
    href="lucene.document.DateTools.html">DateTools</a>; 
     * <li><code>type</code>--containing the file type of the document and
    <li><code>contents</code>--containing the full contents of the file, as a
    Reader field;
     * @param f 
     * @return 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Document Document(File f)
            throws FileNotFoundException, IOException {

        // make a new, empty document
        Document doc = new Document();

        // Add in the first real title of the file as the title, stored, but not tokenized
       
        BufferedReader buffer = new BufferedReader(new FileReader(f));
        doc.add(new Field("path", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED));

//        String title;
//        do {
//            title = buffer.readLine();
//        } while(title.length() == 0);
//
//        if (title.startsWith("IMAGEREF:")){
//            // this is a placeholder for an image we want to use the text of this file and put point to the image
//            File imageFile = new File(f.getParent(),title.substring(10));
//            if (!imageFile.exists()){ throw new FileNotFoundException();}
//            doc.add(new Field("path", imageFile.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
//            doc.add(new Field("type", "image", Field.Store.YES, Field.Index.NOT_ANALYZED));
//            // now find the real title
//            do {
//                title = buffer.readLine();
//            } while (title.length() == 0);
//
//        }else{
//            // Add the path of the file as a field named "path".  Use a field that is
//            // indexed (i.e. searchable), but don't tokenize the field into words.
//            doc.add(new Field("path", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
//            doc.add(new Field("type", "text", Field.Store.YES, Field.Index.NOT_ANALYZED));
//
//        }
//
//        doc.add(new Field("title", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
//

        // Add the contents of the file to a field named "contents".
        // This should store the entire contents in the index
        doc.add(new Field("contents", FileDocument.getContents(f), Field.Store.YES, Field.Index.ANALYZED));

        // Add the last modified date of the file a field named "modified".  Use
        // a field that is indexed (i.e. searchable), but don't tokenize the field
        // into words.
        doc.add(new Field("modified",
                DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        // return the document
        return doc;
    }



    public static Document Document(AWDocument awsDoc){
        // make a new, empty document
        Document doc = new Document();
        doc.add(new Field("path", awsDoc.getName(), Field.Store.YES, Field.Index.ANALYZED));

        // Add the contents of the file to a field named "contents".
        // This should store the entire contents in the index
        doc.add(new Field("contents", awsDoc.getText(), Field.Store.YES, Field.Index.ANALYZED));
        return doc;
    }

    private FileDocument() {
    }
}
    

