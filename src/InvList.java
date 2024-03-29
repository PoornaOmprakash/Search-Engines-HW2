/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.util.*;
import java.io.*;

import org.apache.lucene.index.*;
import org.apache.lucene.util.*;
import org.apache.lucene.search.*;

public class InvList {
  public String field="body";
  /**
   * Utility class that makes it easier to deal with postings.
   */
  public class DocPosting {

    public int docid = 0;
    public int tf = 0;
    public Vector<Integer> positions = new Vector<Integer>();

    /**
     * Constructor.
     */
    public DocPosting(int d, int... locations) {
      this.docid = d;
      this.tf = locations.length;
      for (int i = 0; i < locations.length; i++)
        this.positions.add(locations[i]);
    }

    /**
     * Inserts the postings from dp2 into this (dp1) docPosting. Maintains a sorted order.
     */
    public void insert(DocPosting dp2) {

      int dp1Pos = 0; /* Offset to the current position in dp1 */
      int dp2Pos = 0; /* Offset to the current position in dp2 */

      // Each pass of the loop inserts one position from dp2 into dp1.
      while (dp2Pos < dp2.tf) {

        // Skip positions in dp1 that are less than the current position in dp2.
        while ((dp1Pos < this.tf)
            && (this.positions.elementAt(dp1Pos) < dp2.positions.elementAt(dp2Pos))) {
          dp1Pos++;
        }

        // dp1Pos points to where a dp2 position can be inserted (possibly at the end of the list).
        // If it is not a duplicate, insert it.
        if ((dp1Pos == this.tf)
            || (this.positions.elementAt(dp1Pos) > dp2.positions.elementAt(dp2Pos))) {

          this.positions.add(dp1Pos, dp2.positions.elementAt(dp2Pos));
          this.tf++;
        }

        dp1Pos++;
        dp2Pos++;
      }
    }
  }

  /**
   * Class variables.
   */
  public int ctf = 0;
  public int df = 0;
  public Vector<DocPosting> postings = new Vector<DocPosting>();

  /**
   * An empty inverted list. Useful for some query operators.
   */
  public InvList() {
  }

  
  public void addPosting(int doc, int pos)
  {
    this.postings.add(new DocPosting(doc, pos));
    this.df++;
    this.ctf++;
  }
  
  public void insertInPosting(int pos)
  {
    this.postings.lastElement().positions.add(pos);
    this.postings.lastElement().tf++;
    this.ctf++;
  }
  /**
   * Fetch the inverted list from the index.
   */
  public InvList(String termString, String fieldString) throws IOException {

    // Prepare to access the index.
    BytesRef termBytes = new BytesRef(termString);
    Term term = new Term(fieldString, termBytes);
    field=fieldString;
    if (QryEval.READER.docFreq(term) < 1)
      return;

    // Lookup the inverted list.
    DocsAndPositionsEnum iList = MultiFields.getTermPositionsEnum(QryEval.READER,
        MultiFields.getLiveDocs(QryEval.READER), fieldString, termBytes);

    // Copy from Lucene inverted list format to our inverted list format. This is a little
    // inefficient, but allows query operators such as #syn, #od/n, and #uw/n to be insulated from
    // the details of Lucene inverted list implementations.
    while (iList.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {

      int tf = iList.freq();
      int[] pos = new int[tf];

      for (int j = 0; j < tf; j++)
        pos[j] = iList.nextPosition();

      this.postings.add(new DocPosting(iList.docID(), pos));
      this.df++;
      this.ctf += tf;
    }
  }

  /**
   * Inserts inverted list i2 into this inverted list (called "i1"). Maintains a sorted order.
   */
  public void insert(InvList i2) {

    int i1Doc = 0; /* Offset to the current document in i1 */
    int i2Doc = 0; /* Offset to the current document in i2 */

    // Each pass of the loop inserts one docPosting from i2 into i1.
    while (i2Doc < i2.df) {

      // Skip documents in i1 that are less than the current document in i2.
      while ((i1Doc < this.df)
          && (this.postings.elementAt(i1Doc).docid < i2.postings.elementAt(i2Doc).docid)) {
        i1Doc++;
      }

      // i1Doc points to where an i2 document can be merged or inserted (possibly at the end of the
      // list).
      if ((i1Doc < this.df)
          && (this.postings.elementAt(i1Doc).docid == i2.postings.elementAt(i2Doc).docid)) {

        /* Merge the i1 and i2 postings for this document */
        this.postings.elementAt(i1Doc).insert(i2.postings.elementAt(i2Doc));
        this.ctf += i2.postings.elementAt(i2Doc).tf;
      } else {

        /* Insert the i2 document into i1 */
        this.postings.add(i1Doc, i2.postings.elementAt(i2Doc));
        this.ctf += i2.postings.elementAt(i2Doc).tf;
        this.df++;
      }

      i1Doc++;
      i2Doc++;
    }
  }

  /**
   * Handy for debugging.
   */
  public void print() {

    System.out.println("df:  " + this.df + ", ctf: " + this.ctf);

    for (int i = 0; i < this.df; i++) {
      System.out.print("docid:  " + this.postings.elementAt(i).docid + ", tf: "
          + this.postings.elementAt(i).tf + ", locs: ");

      for (int j = 0; j < this.postings.elementAt(i).tf; j++) {
        System.out.print(this.postings.elementAt(i).positions.elementAt(j) + " ");
      }

      System.out.println();
    }
  }
}
