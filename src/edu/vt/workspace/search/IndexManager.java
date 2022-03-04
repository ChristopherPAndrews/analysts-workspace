package edu.vt.workspace.search;

/**
 * This class manaages the creation of the Lucene index. Currently it just operates on a
 * directory of files. The next step will be to allow this to read in .jig files and
 * add those as well.
 *
 * @author <a href="mailto:cpa@cs.vt.edu">Christopher Andrews</a>
 */
import edu.vt.workspace.data.AWDocument;
import java.util.Vector;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;

/** Index all text files under a directory. */
public class IndexManager {

    private static File indexDir = null;
    private static boolean shutdownHookInstalled = false;

    private IndexManager() {
    }

    /**
     * This makes a new directory in the system's temp location for an index.
     *
     * @return a File objects pointing to the new directory
     * @throws IOException
     */
    private static File makeTmpDir() throws IOException {
        final File tmp;
        tmp = File.createTempFile("aw_index", Long.toString(System.nanoTime()));

        if (!tmp.delete()) {
            throw new IOException("Could not delete index placeholder " + tmp.getAbsolutePath());
        }
        if (!tmp.mkdir()) {
            throw new IOException("Could not create temporary index directory " + tmp.getAbsolutePath());
        }

        return tmp;
    }

    /**
     * This is a utility function to help clean up old index files. It is just a recursive directory deletion method.
     * 
     * @param dir the directory (or file) to delete
     * @return indicates if the operation was successful
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            boolean success;
            for (String child : children) {
                success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private static void setupIndex() throws IOException {
        // if there already is an index, we want to overwrite it
        // at some point we may want to add data to an index, but for right now,
        //
        if (indexDir == null) {
            indexDir = makeTmpDir();
        } else {
            if (!deleteDir(indexDir)) {
                System.out.println("failed to cleanup old log file: " + indexDir.getAbsolutePath());
                indexDir = makeTmpDir();
            } else {
                if (!indexDir.mkdir()) {
                    throw new IOException("Could not create temporary index directory " + indexDir.getAbsolutePath());
                }
            }
        }

        // make arrangements for the tmp index to be destroyed at termination time
        if (!shutdownHookInstalled) {
            shutdownHookInstalled = true;
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    deleteDir(indexDir);
                }
            });
        }
    }

    public static void buildIndex(Vector<AWDocument> docs) throws IOException {
        setupIndex();

        IndexWriter writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        for (AWDocument doc : docs) {
            writer.addDocument(FileDocument.Document(doc));
        }

        writer.optimize();
        writer.close();

    }

    /**
     * Primary function for building the index. This currently will create a
     * temporary index that should not be used beyond the close of the application.
     * At some point this may be something the user can save. Also, this method
     * will erase the contents of the old index, so the addition of new documents
     * is not currently possible within the tool. At some point it might be nice to
     * Allow for incremental of data (with some helpful way for the user to be aware
     * of new material, of course).
     * 
     * @todo this need to be rewritten or refactored to allow a main File index to be built up
     * [currently when a collection of docs is imported, it create doc object opportunistically and
     * the FileBrowser doesn't work properly]
     *
     * @param docDir the directory to be indexed
     * @throws IOException
     */
    public static void buildIndex(File docDir) throws IOException {

        /**
         * @todo change these printlines and exits to exceptions
         */
        setupIndex();


        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            IndexWriter writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
            System.out.println("Indexing to directory '" + indexDir + "'...");
            indexDocs(writer, docDir);
            System.out.println("Optimizing...");
            writer.optimize();
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass()
                    + "\n with message: " + e.getMessage());
        }
    }

    /**
     * This is the main workhorse method that sets up the index. That said, it doesn't have that much to do.
     * This takes in a File object and adds it to the index. Most of the actual work here is
     * determining if the File object is a text document or not. If it is a directory,
     * this just recurses on the directory. If it is an image, it just skips it. If it is a
     * text file, it builds a FileDocument object and passes it to the index.
     *
     * @param writer the IndexWriter that actually writes the index
     * @param file a file or directory to be added to the index
     * @throws IOException
     */
    public static void indexDocs(IndexWriter writer, File file)
            throws IOException {
        String[] suffixes = ImageIO.getReaderFormatNames();
        String extension;
        boolean isImage = false;
        // do not try to index files that cannot be read
        if (file.canRead() && !file.isHidden()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {
                try {
                    extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    isImage = false;
                    for (String suffix : suffixes) {
                        if (suffix.equalsIgnoreCase(extension)) {
                            isImage = true;
                            break;
                        }
                    }
                    if (!isImage) {
                        writer.addDocument(FileDocument.Document(file));
                    }
                } // at least on windows, some temporary files raise this exception with an "access denied" message
                // checking if the file can be read doesn't help
                catch (FileNotFoundException fnfe) {
                }
            }
        }
    }

    /**
     * Return the absolute path to the current location of the index.
     * @return the path to the index
     */
    public static String getIndexPath() {
        if (indexDir != null) {
            return indexDir.getAbsolutePath();
        } else {
            return "";
        }
    }
}


