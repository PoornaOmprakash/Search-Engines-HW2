import java.io.IOException;
import java.util.ArrayList;

public class QryopWeight extends Qryop {

	public ArrayList<Double> weight = new ArrayList<Double>();  //Stores weights of query terms
	
	 public double default_score(QryResult result) throws IOException {  //To calculate the default score
		 
		    int ctf = result.invertedList.ctf;
		    long length_c = QryEval.READER.getSumTotalTermFreq(result.invertedList.field);
		    //long length_d=QryEval.READER.getSumTotalTermFreq(result.invertedList.field)/QryEval.READER.getDocCount(result.invertedList.field);
		    //double def_score=(float)Math.log((QryEval.lambda*((tf_d+(QryEval.mu*(ctf_qt/length_c)))/(length_d+QryEval.mu))) + (1-QryEval.lambda)*(ctf_qt/length_c));  //Confusing. Split up into individual parts and compute.
		    double ctfc = (double)ctf/(double)length_c;
		    //System.out.println(length_c);
		    double avglen=(double)QryEval.READER.getSumTotalTermFreq(result.invertedList.field)/(double)QryEval.READER.getDocCount(result.invertedList.field);
		    double def_score=(QryEval.lambda*QryEval.mu*ctfc);
		    def_score= def_score/(avglen + QryEval.mu)+(1-(QryEval.lambda)*ctfc);
		    def_score=Math.log(def_score);
		    return def_score;
		  }

  public QryResult evaluate() throws IOException {        //To evaluate Indri queries and handle their weights
	  
    Qryop impliedQryOp = new QryopScore(args.get(0));
    QryResult result = impliedQryOp.evaluate();
    //double score=0; 
    
    double default_sum = weight.get(0)*default_score(result);   //Default score for the result terms
    //System.out.println(default_sum);
    for (int i = 0; i < result.docScores.scores.size(); i++) {
      //System.out.println(i);
    	result.docScores.setDocidScore(i,(float) (result.docScores.getDocidScore(i)*weight.get(0)));   //Score for the query term is modified based on the weight
    }

    // Each pass of the loop evaluates one query argument.
    for (int i = 1; i < args.size(); i++) {

      impliedQryOp = new QryopScore(args.get(i));
      QryResult iResult = impliedQryOp.evaluate();

      double iRes_default_score = default_score(iResult);  //Default score for the iResult terms

      // Use the results of the i'th argument to incrementally compute the query operator.
      // Intersection-style query operators iterate over the incremental results, not the results of
      // the i'th query argument.
      int rDoc = 0; /* Index of a document in result. */
      int iDoc = 0; /* Index of a document in iResult. */

      while (rDoc < result.docScores.scores.size()) {

        // Function for Indri retrieval

        // Add documents matched only by the i'th query term into result, with default score
        while ((iDoc < iResult.docScores.scores.size())
                && (result.docScores.getDocid(rDoc) > iResult.docScores.getDocid(iDoc))) {
          double score=(weight.get(i)*iResult.docScores.getDocidScore(iDoc))+default_sum;
          result.docScores.add(rDoc,iResult.docScores.getDocid(iDoc),(float)score);
          rDoc++;
          iDoc++;
        }

        // If the rDoc term appears in both lists, compute score for the terms and update.
        if ((iDoc < iResult.docScores.scores.size())
                && ((result.docScores.getDocid(rDoc))==(iResult.docScores.getDocid(iDoc)))) {
          float score = (float)((result.docScores.getDocidScore(rDoc))+(weight.get(i)*iResult.docScores.getDocidScore(iDoc)));
          result.docScores.setDocidScore(rDoc, score);
          rDoc++;
          iDoc++;
        } else {   //Add iDoc terms to result, with default scores
          result.docScores.setDocidScore(rDoc,(float)((result.docScores.getDocidScore(rDoc))+(weight.get(i)*iRes_default_score)));
          rDoc++;
        }
      }
      
      while (iDoc < iResult.docScores.scores.size()) {  //Rest of the terms
        double score = weight.get(i)*iResult.docScores.getDocidScore(iDoc) + default_sum;
        result.docScores.add(iResult.docScores.getDocid(iDoc),(float)score);
        iDoc++;
      }
      default_sum = default_sum+(weight.get(i)*iRes_default_score);
    }
    return result;
  }

}