import java.io.IOException;
import java.util.Vector;

public class QryopUW extends Qryop {

  public int n;

  public class TermEntry {
    InvList result = null;// the invList of each term

    InvList.DocPosting rPos = null;// the index of docID in invList of each term

    int docIndex = 0;// the current posting list in current document of each term

    int posIndex = 0;// the index of pos entry in current document of each term
    // **the rPos and docIndex should be changed together
    // the No.docIndex entry of resulft.invList.postings should always equal to
    // rPos
  }

  Vector<TermEntry> result = null;

  public QryopUW(int n) {
    this.n = n;
  }

  @Override
  public QryResult evaluate() throws IOException {
    // TODO Auto-generated method stub
    if (args.size() < 1) {
      throw new IOException("#UW operator must have at least 1 terms.");
    }

    QryResult ret = new QryResult();// for returning
    ret.invertedList = new InvList();
    result = new Vector<TermEntry>();
    for (int i = 0; i < args.size(); i++) {
      TermEntry te = new TermEntry();
      te.result = args.get(i).evaluate().invertedList;// initial the result
      if (i == 0) ret.invertedList.field = te.result.field;
      te.docIndex = 0;// initial the index of current document entry
      te.rPos = te.result.postings.get(te.docIndex);// initial the current posting list
      te.posIndex = 0;// initial the index of current pos entry
      result.add(te);
    }

    int minDocTerm = getMinDocidTermIndex();// the index of the term which has the minimum docid;
    int maxDocTerm = getMaxDocidTermIndex();// the index of the term which has the maximum docid;
    while (result.get(maxDocTerm).docIndex < result.get(maxDocTerm).result.postings.size()) {
      if (getDocid(minDocTerm) == getDocid(maxDocTerm)) {// same doc
        int minPosTerm = getMinPosTermIndex();
        int maxPosTerm = getMaxPosTermIndex();
        boolean firstMatch = true;
        while (result.get(maxPosTerm).posIndex < result.get(maxPosTerm).rPos.positions.size()) {
          if (getPos(maxPosTerm) - getPos(minPosTerm) + 1 <= n)// match
          {
            // add to position list of posting
            if (firstMatch) {
              ret.invertedList.addPosting(getDocid(minPosTerm), getPos(minPosTerm));
              firstMatch = false;
            } else {
              ret.invertedList.insertInPosting(getPos(minPosTerm));
            }
            // update posIndex
            int j = 0;
            for (j = 0; j < result.size(); j++) {
              result.get(j).posIndex++;
              if (result.get(j).posIndex >= result.get(j).rPos.positions.size()) {
                break;// out of bound
              }
            }
            if (j < result.size()) {
              break;// means this doc is over.
            }
            minPosTerm = getMinPosTermIndex();
            maxPosTerm = getMaxPosTermIndex();
          } else {
            // update the smallest posIndex
            result.get(minPosTerm).posIndex++;
            if (result.get(minPosTerm).posIndex >= result.get(minPosTerm).rPos.positions.size()) {
              break;
            }
            // update maxPosTerm
            if (getPos(minPosTerm) > getPos(maxPosTerm)) {
              maxPosTerm = minPosTerm;
            }
            // update minPosTerm
            minPosTerm = getMinPosTermIndex();
          }
        }
        // update docIndex
        int j = 0;
        for (j = 0; j < result.size(); j++) {
          result.get(j).docIndex++;
          result.get(j).rPos = getCurrentPostings(j);// rPos should be sync with docIndex
          if (result.get(j).rPos == null)
            break;// out of bound
        }
        if (j < result.size())
          break;
        this.refreshPosIndex();
        // update minDocTerm and maxDocTerm
        minDocTerm = getMinDocidTermIndex();
        maxDocTerm = getMaxDocidTermIndex();
      } else {
        // update smallest posting Index
        result.get(minDocTerm).docIndex++;
        result.get(minDocTerm).rPos = getCurrentPostings(minDocTerm);
        if (result.get(minDocTerm).rPos == null)
          break;
        result.get(minDocTerm).posIndex = 0;
        // update maxDocTerm
        if (getDocid(minDocTerm) > getDocid(maxDocTerm)) {
          maxDocTerm = minDocTerm;
        }
        // update minDocTerm
        minDocTerm = getMinDocidTermIndex();
      }
    }
    return ret;
  }

  /**
   * Get the document id for No.termIndex term's current invList entry.
   * 
   * @param termIndex
   *          the index of the term.
   * @return
   */
  public int getDocid(int termIndex) {
    return result.get(termIndex).rPos.docid;
  }

  /**
   * Get current posting entry according to the invList and current docIndex. This funcation should
   * be called whenever docIndex is changed to update rPos
   * 
   * @param termIndex
   * @return
   */
  public InvList.DocPosting getCurrentPostings(int termIndex) {
    TermEntry te = result.get(termIndex);
    if (te.docIndex >= te.result.postings.size())
      return null;
    return te.result.postings.get(te.docIndex);
  }

  public int getPos(int termIndex) {
    TermEntry te = result.get(termIndex);
    return te.rPos.positions.get(te.posIndex);
  }

  /**
   * Get the index of the term with the max doc
   * 
   * @return
   */
  public int getMinDocidTermIndex() {
    int minIndex = -1;
    for (int i = 0; i < result.size(); i++) {
      if (minIndex == -1 || getDocid(i) < getDocid(minIndex)) {
        minIndex = i;
      }
    }
    return minIndex;
  }

  public int getMinPosTermIndex() {
    int minIndex = -1;
    for (int i = 0; i < result.size(); i++) {
      if (minIndex == -1 || getPos(i) < getPos(minIndex)) {
        minIndex = i;
      }
    }
    return minIndex;
  }

  public int getMaxDocidTermIndex() {
    int maxIndex = -1;
    for (int i = 0; i < result.size(); i++) {
      if (maxIndex == -1 || getDocid(i) > getDocid(maxIndex)) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  public int getMaxPosTermIndex() {
    int maxIndex = -1;
    for (int i = 0; i < result.size(); i++) {
      if (maxIndex == -1 || getPos(i) > getPos(maxIndex)) {
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  public void refreshPosIndex() {
    for (int i = 0; i < result.size(); i++) {
      result.get(i).posIndex = 0;
    }
  }
}



/*import java.io.IOException;

import java.util.ArrayList;
import java.util.Vector;


public class QryopUW extends Qryop{
	  public int near;
	  
	  public int getMinIndex()    //For DocID
	  {
	    int index = 0;  //Index needs to be something initially
	    //System.out.println(tracker_list.size());
	    for (int i = 0; i < tracker_list.size(); i++) 
	    {
	      if (tracker_list.get(i).dp.docid < tracker_list.get(index).dp.docid || i == 0)
	      {
	        index = i;
	      }
	    }
	    return index;
	  }
	  
	  public int getMaxIndex()
	  {   //Also for DocID
		    int index = 0;
		    for (int i = 0; i < tracker_list.size(); i++) {
		      if (tracker_list.get(i).dp.docid > tracker_list.get(index).dp.docid ||i==0)
		      {
		        index = i;
		      }
		    }
		    //System.out.println(index);
		    return index;
	  }


	  public int getMinPosition()
	  {    //For position within a posting
	    int index = 0;
	    for (int i = 0; i < tracker_list.size(); i++) {
	      if (getPosition(i) < getPosition(index)||i == 0) {
	        index = i;
	      }
	    }
	    return index;
	  }

	  public int getMaxPosition()
	  {           //For position within a posting
	    int index = 0;
	    for (int i = 0; i < tracker_list.size(); i++) 
	    {
	      if (getPosition(i) > getPosition(index)||(i==0))
	      {
	        index = i;
	      }
	    }
	    //System.out.println(index);
	    return index;
	  }

	  
	  public class Tracker    //To keep track of inverted lists of all query terms. This is where the checking happens.
	  {   
		  
	    InvList tracker_invlist;
	    InvList.DocPosting dp;
	    int docIndex;
	    int positionIndex;
	    
	    Tracker()
	    {
	    	tracker_invlist=null;
	    	dp=null;
	    	docIndex=0;
	    	positionIndex=0;
	    }
	  }

	  ArrayList<Tracker> tracker_list = null;   //To keep track of inverted lists

	  public QryopUW(int near)
	  {
	    this.near = near;
	  }

	 
	  public QryResult evaluate() throws IOException {

		boolean check;
		
	    QryResult result = new QryResult();
	    result.invertedList = new InvList();  //This is what we want - return a QryResult type.
	    tracker_list = new ArrayList<Tracker>();
	    //System.out.println(args.size());
	    for (int i=0;i<args.size(); i++)    //Initialization
	    {
	      //System.out.println(i);
	      Tracker t = new Tracker();
	      t.tracker_invlist = args.get(i).evaluate().invertedList;
	      if (i==0) 
	    	  result.invertedList.field=t.tracker_invlist.field;   //#UW terms must be in the same field.
	      t.positionIndex=0;
	      t.docIndex=0;
	      t.dp=t.tracker_invlist.postings.get(t.docIndex);
	      tracker_list.add(t);
	      //System.out.println(tracker_list.size());
	    }
	  
	    int i;
	    
	    
	    //Mess-up was here. These terms cannot come earlier, since the size of tracker_list is not yet known. Meh!
	    
	    int minIndex = getMinIndex();  //For DocID
		int maxIndex = getMaxIndex();  //Also for DocID
	  
	    while (tracker_list.get(maxIndex).docIndex < tracker_list.get(maxIndex).tracker_invlist.postings.size())
	    {
	      if (tracker_list.get(minIndex).dp.docid==tracker_list.get(maxIndex).dp.docid)   //Same document in both lists 
	      {
	    	check=true;
	    	  
	        int minPosition=getMinPosition();
	        int maxPosition=getMaxPosition();
	        
	       
	        
	        while (tracker_list.get(maxPosition).positionIndex < tracker_list.get(maxPosition).dp.positions.size())
	        {
	          if (getPosition(maxPosition)-getPosition(minPosition)+1 <= near)   //There is a match within the window size.
	          {
	            if (check)
	            {
	              result.invertedList.addPosting(tracker_list.get(minPosition).dp.docid, getPosition(minPosition));  //Add a posting into result's InvList.
	              check = false;
	            }
	            else 
	            {
	            //	System.out.println(getPosition(minPosition));
	              result.invertedList.insertInPosting(getPosition(minPosition));
	            }
	           
	            //System.out.println(tracker_list.size());
	            for (i = 0; i < tracker_list.size(); i++)
	            {
	              //System.out.println(i);
	              tracker_list.get(i).positionIndex++;
	              if (tracker_list.get(i).positionIndex >= tracker_list.get(i).dp.positions.size()) {
	                   break;
	              }
	            }
	            
	            if (i < tracker_list.size()) {
	              break;
	            }
	            
	            minPosition = getMinPosition();
	            maxPosition = getMaxPosition();
	          } 
	          
	          else
	          {
	            tracker_list.get(minPosition).positionIndex++;
	            if (tracker_list.get(minPosition).positionIndex >= tracker_list.get(minPosition).dp.positions.size()) {
	                break;
	            }
	            
	            if (getPosition(minPosition) > getPosition(maxPosition))
	            {
	              maxPosition = minPosition;
	            }
	            minPosition = getMinPosition();
	            //System.out.println(minPosition);
	          }
	        }
	        

	        for (i = 0; i < tracker_list.size(); i++)    //Index of the document needs to be updated here.
	        {
	      //  System.out.println(tracker_list.docIndex);
	          tracker_list.get(i).docIndex++;
	          tracker_list.get(i).dp = getPosting(i);
	          if (tracker_list.get(i).dp == null)     //Necessary to check for this condition.
	                break;
	        }
	        
	        if (i < tracker_list.size())
	        {   break;  }
	        
	        this.reset();
	       
	        minIndex = getMinIndex();
	        maxIndex = getMaxIndex();
	   
	       
	      } 
	      else
	      {
	    	  
	    	 // System.out.println(minIndex);
	    	tracker_list.get(minIndex).dp = getPosting(minIndex);
	        tracker_list.get(minIndex).docIndex++;
	        
	        if (tracker_list.get(minIndex).dp == null)
	              break;
	        tracker_list.get(minIndex).positionIndex = 0;
	        if (tracker_list.get(minIndex).dp.docid> tracker_list.get(maxIndex).dp.docid) 
	        {
	          maxIndex=minIndex;
	        }
	        minIndex = getMinIndex();
	        //System.out.println(minIndex);
	      }
	    }
	    return result;
	  }

	  
	  public void reset()            //This is what was needed!
	  {
		    for (int i = 0; i < tracker_list.size(); i++)
		    {
		      tracker_list.get(i).positionIndex=0;
		    }
	}

	  
	  public InvList.DocPosting getPosting(int i)   
	  {
	    Tracker t = tracker_list.get(i);
	    if (t.docIndex >= t.tracker_invlist.postings.size())
	        return null;
	    return t.tracker_invlist.postings.get(t.docIndex);
	  }

	  public int getPosition(int i) {    //Added this function because it is too cumbersome to calculate the value in every place
	    Tracker t = tracker_list.get(i);
	    int pos = t.dp.positions.get(t.positionIndex);
	    return pos;
	    //tracker_list.gett(i).dp.positions.get(tracker_list.get(i).positionIndex)
	  }*/
