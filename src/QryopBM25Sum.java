import java.io.IOException;


public class QryopBM25Sum extends Qryop {
	/*
	 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
	 */
	  /**
	   * It is convenient for the constructor to accept a variable number of arguments. Thus new
	   * qryopAnd (arg1, arg2, arg3, ...).
	   */
	  public QryopBM25Sum(Qryop... q) {                    // Constructor that accepts variable number of arguments
	    for (int i = 0; i < q.length; i++)
	      this.args.add(q[i]);
	  }

	  /**
	   * Evaluate the query operator.
	   */
	  public QryResult evaluate() throws IOException {       // Comes from QryopScore

	     Qryop impliedQryOp = new QryopScore(args.get(0));       // Seed the result list with the first query term
	     QryResult result = impliedQryOp.evaluate();             // Contains the docScores for the first query term

	    //QryResult result = null;
		  // Each pass of the loop evaluates one query argument.
	    for (int i = 1; i < args.size(); i++) {
         //System.out.println(i);
	      impliedQryOp = new QryopScore(args.get(i));          // Gets next query term
	      QryResult iResult = impliedQryOp.evaluate();         // Contains the docScores for the i'th query term (Doc ID's that contain the required query trem)

	      // Use the results of the i'th argument to incrementally compute the query operator.
	      // Intersection-style query operators iterate over the incremental results, not the results of
	      // the i'th query argument.
	      int rDoc = 0; /* Index of a document in result. */
	      int iDoc = 0; /* Index of a document in iResult. */       // docScores from the i'th query term

	      while (rDoc < result.docScores.scores.size()) {

	        while ((iDoc < iResult.docScores.scores.size())
	            && (result.docScores.getDocid(rDoc) > iResult.docScores.getDocid(iDoc))) {
	            result.docScores.scores.add(rDoc,iResult.docScores.scores.get(iDoc));
	        	rDoc++;
	            iDoc++;
	        }

	        // If the rDoc document appears in one of the lists, keep it, otherwise discard it.
	        if ((iDoc < iResult.docScores.scores.size()) && (result.docScores.getDocid(rDoc) == iResult.docScores.getDocid(iDoc)))
	        	{
	        	 float score=result.docScores.getDocidScore(rDoc)+iResult.docScores.getDocidScore(iDoc);
	        	   result.docScores.setDocidScore (rDoc,score);  //Ranked Boolean
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
