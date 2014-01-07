
import java.io.IOException;

public class QryopOr extends Qryop {
	/*
	 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
	 */
	  /**
	   * It is convenient for the constructor to accept a variable number of arguments. Thus new
	   * qryopAnd (arg1, arg2, arg3, ...).
	   */
	  public QryopOr(Qryop... q) {                    // Constructor that accepts variable number of arguments
	    for (int i = 0; i < q.length; i++)
	      this.args.add(q[i]);
	  }

	  /**
	   * Evaluate the query operator.
	   */
	  public QryResult evaluate() throws IOException {       // Comes from QryopScore

	    // Seed the result list by evaluating the first query argument. The result could be docScores or
	    // invList, depending on the query operator. Wrap a SCORE query operator around it to force it
	    // to be a docScores list. There are more efficient ways to do this. This approach is just easy
	    // to see and understand.
	     Qryop impliedQryOp = new QryopScore(args.get(0));       // Seed the result list with the first query term
	     QryResult result = impliedQryOp.evaluate();             // Contains the docScores for the first query term

	    //QryResult result = null;
		  // Each pass of the loop evaluates one query argument.
	    for (int i = 0; i < args.size(); i++) {
         //System.out.println(i);
	      impliedQryOp = new QryopScore(args.get(i));          // Gets next query term
	      QryResult iResult = impliedQryOp.evaluate();         // Contains the docScores for the i'th query term (Doc ID's that contain the required query trem)

	      // Use the results of the i'th argument to incrementally compute the query operator.
	      // Intersection-style query operators iterate over the incremental results, not the results of
	      // the i'th query argument.
	      int rDoc = 0; /* Index of a document in result. */
	      int iDoc = 0; /* Index of a document in iResult. */       // docScores from the i'th query term

	      while (rDoc < result.docScores.scores.size()) {
            //System.out.println(rDoc);
	        // DIFFERENT RETRIEVAL MODELS IMPLEMENT THIS DIFFERENTLY.
	        // Unranked Boolean AND. Remove from the incremental result any documents that weren't 
	        // returned by the i'th query argument.

	        // Ignore documents matched only by the i'th query arg.
	        while ((iDoc < iResult.docScores.scores.size())
	            && (result.docScores.getDocid(rDoc) > iResult.docScores.getDocid(iDoc))) {
	            result.docScores.scores.add(rDoc,iResult.docScores.scores.get(iDoc));
	        	rDoc++;
	            iDoc++;
	        }

	        // If the rDoc document appears in one of the lists, keep it, otherwise discard it.
	        if ((iDoc < iResult.docScores.scores.size()) && (result.docScores.getDocid(rDoc) == iResult.docScores.getDocid(iDoc)))
	        	{
	        	if(QryEval.retrievalAlgortihm.equals("RankedBoolean"))
	        	   result.docScores.setDocidScore (rDoc, Math.max(result.docScores.getDocidScore(rDoc),iResult.docScores.getDocidScore(iDoc)));  //Ranked Bookean
	        	rDoc++;
	          iDoc++;
	       }
	        else
	        	rDoc++;
	      }
	    }
	     
	    return result;
	  }
	}


