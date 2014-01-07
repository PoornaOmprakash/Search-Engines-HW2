/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreList {
 public float default_score=0;
  /**
   * A little utilty class to create a <docid, score> object.
   */
  protected class ScoreListEntry {
    private int docid;
    public float score;
    //public float default_score;

    private ScoreListEntry(int docid, float score) {  
      this.docid = docid;       ////Sort this to display rank in unranked
      this.score = score;      //Sort this to display rank in ranked
      //default_score=0;
    }
  }

  public class ScoreCompare implements Comparator<ScoreListEntry> {  //Comparator lets you sort by multiple values.

	    public int compare(ScoreListEntry arg0, ScoreListEntry arg1) {
	    	
	      if (arg0.score - arg1.score > 0) return 1;
	      else if (arg0.score - arg1.score < 0) return -1;
	      else
	      {
	        return arg1.docid-arg0.docid;
	      }
	    }
	  }
  
  public ScoreCompare sc = new ScoreCompare();
  List<ScoreListEntry> scores = new ArrayList<ScoreListEntry>();

  /**
   * Append a document score to a score list.
   */
  
  public void swap(int i,int j)
  {
	  ScoreListEntry temp=scores.get(i);
	  scores.set(i, scores.get(j));
	  scores.set(j,temp);
  }
  
  public void add(int docid, float score) {
    scores.add(new ScoreListEntry(docid, score));
  }

  public void add(int n, int docid, float score)
  {
    scores.add(n, new ScoreListEntry(docid, score));
  }

  public int getDocid(int n) {
    return this.scores.get(n).docid;
  }

  public float getDocidScore(int n) {          //Return the score for the document ID n
    return this.scores.get(n).score;
  }
 
  public void setDocidScore(int n, float score)          //Set the score for a document that is already in the score list.
  {
	  int docid = getDocid(n);
	  scores.set(n, new ScoreListEntry(docid, score));
  }
  
}
