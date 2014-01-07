/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;
import java.util.Collections;

public class QryopAnd extends Qryop {
int qrylen;
  /**
   * It is convenient for the constructor to accept a variable number of arguments. Thus new
   * qryopAnd (arg1, arg2, arg3, ...).
   */
  public QryopAnd(Qryop... q) {
    for (int i = 0; i < q.length; i++)
       this.args.add(q[i]);
      qrylen=args.size();
       //System.out.println(qrylen);
  }

  /**
   * Evaluate the query operator.
   */
  public QryResult evaluate() throws IOException {

    Qryop impliedQryOp = new QryopScore(args.get(0));
    QryResult result = impliedQryOp.evaluate();

    
    if(QryEval.retrievalAlgortihm.equals("Indri")) 
    {
    	QryopWeight qw = new QryopWeight();     
        qw.args = args;
        for (int i = 0; i<args.size(); i++){
          qw.weight.add(1.0/args.size());  //for 1/|q|
        }
        return qw.evaluate();
    }
    // Each pass of the loop evaluates one query argument.
    for (int i = 1; i < args.size(); i++) {
      //System.out.println(i);
      impliedQryOp = new QryopScore(args.get(i));
      QryResult iResult = impliedQryOp.evaluate();

      // Use the results of the i'th argument to incrementally compute the query operator.
      // Intersection-style query operators iterate over the incremental results, not the results of
      // the i'th query argument.
      int rDoc = 0; /* Index of a document in result. */
      int iDoc = 0; /* Index of a document in iResult. */

      while (rDoc < result.docScores.scores.size()) {
       // if(QryEval.retrievalAlgortihm.equals("RankedBoolean")|(QryEval.retrievalAlgortihm.equals("UnrankedBoolean")))
        //{
          while ((iDoc < iResult.docScores.scores.size())      //Number of documents containing the query term
            && (result.docScores.getDocid(rDoc) > iResult.docScores.getDocid(iDoc))) {
                  iDoc++;
        }   
      //}
       /*else if(QryEval.retrievalAlgortihm.equals("Indri"))        - Seems like it's better to not do this here. It gets confusing.
        {
        	while ((iDoc < iResult.docScores.scores.size())      //Number of documents containing the query term
                    && (result.docScores.getDocid(rDoc) > iResult.docScores.getDocid(iDoc))) { //Add second query term to the result, with default score for the first query term
        		  //System.out.println(iDoc);
        		  float score_temp;
	    		  float score1;
	    		  float score2;
	    		  float score;
		      		long length_c=0;
		      		int tf_d=0;  //Term frequency of the term in the posting i (in document i)
		      		int ctf_qt=result.invertedList.ctf;     //Corpus term frequency for the particular term
		      		//System.out.println(result.invertedList.field);
		      		long length_d=QryEval.READER.getSumTotalTermFreq("body")/QryEval.READER.getDocCount("body"); //Average document length by default
		      		//System.out.println(length_d);
		      		//System.out.println(length_d);
		      		length_c=QryEval.READER.getSumTotalTermFreq("body");
		      		//System.out.println(length_c);
		      		score_temp=(float)Math.log((QryEval.lambda*((tf_d+(QryEval.mu*(ctf_qt/length_c)))/(length_d+QryEval.mu))) + (1-QryEval.lambda)*(ctf_qt/length_c));  //Default score for the first term in rDoc
		      		//System.out.println(qlen);
		      		//score1=(float)Math.pow(score_temp,(1/qlen));  
		      		//score2=(float)Math.pow(iResult.docScores.getDocidScore(iDoc), (1/qlen));
		      		score=score_temp+iResult.docScores.getDocidScore(iDoc);
		      		iResult.docScores.setDocidScore(iDoc,score);
		      		result.docScores.scores.add(rDoc,iResult.docScores.scores.get(iDoc));
		      		iDoc++;          
         }
       }*/
        	
 

        // If the rDoc document appears in both lists
       // If the rDoc document appears in both lists, keep it, otherwise discard it.
          if ((iDoc < iResult.docScores.scores.size())
                  && (result.docScores.getDocid(rDoc) == iResult.docScores.getDocid(iDoc))) {
            if (result.docScores.getDocidScore(rDoc) > iResult.docScores.getDocidScore(iDoc)) {
              result.docScores.setDocidScore(rDoc, iResult.docScores.getDocidScore(iDoc));
            }
            rDoc++;
            iDoc++;
          } else {
            result.docScores.scores.remove(rDoc);
          }
        }
      }
      return result;
    }
}