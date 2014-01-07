import java.io.IOException;

	public class QryopNear extends Qryop {

	  /**
	   * It is convenient for the constructor to accept a variable number of arguments. Thus new
	   * qryopAnd (arg1, arg2, arg3, ...).
	   */
		int near;
	  public QryopNear(int n, Qryop... q) {
	    for (int i = 0; i < q.length; i++)
	      {
	    	this.near=n;
	    	this.args.add(q[i]);
	      }
	  }
	    public QryopNear(int n) {
		    	this.near=n;
	  }

	  /**
	   * Evaluate the query operator.
	   */
	  public QryResult evaluate() throws IOException {
	    //Qryop impliedQryOp = new QryopScore(args.get(0));
	    QryResult result = args.get(0).evaluate();

	    // Each pass of the loop evaluates one query argument.
	    for (int i = 1; i < args.size(); i++) {

	      //impliedQryOp = new QryopScore(args.get(i));
	      QryResult iResult = args.get(i).evaluate();

	      // Use the results of the i'th argument to incrementally compute the query operator.
	      // Intersection-style query operators iterate over the incremental results, not the results of
	      // the i'th query argument.
	      int rDoc = 0; /* Index of a document in result. */
	      int iDoc = 0; /* Index of a document in iResult. */
	      //int flag=0;

	      while (rDoc < result.invertedList.postings.size()) {

	        // Check for documents matched by both at a distance <=n
	        while ((iDoc < iResult.invertedList.postings.size())&&(result.invertedList.postings.get(rDoc).docid > iResult.invertedList.postings.get(iDoc).docid))
	        {
	           iDoc++;
	        }
	        int x=0,y=0;   //Index for the document in each posting - In rDoc's while loop
	        //System.out.println(iDoc);
	           if((iDoc<iResult.invertedList.postings.size())&&(result.invertedList.postings.get(rDoc).docid==iResult.invertedList.postings.get(iDoc).docid))  //DocIDs match
	           { 
	        	   //System.out.println(iDoc);
	        		while(x<result.invertedList.postings.get(rDoc).positions.size())//;x++)
	        	        {
	        			 while ((y < iResult.invertedList.postings.get(iDoc).positions.size()) &&(result.invertedList.postings.get(rDoc).positions.get(x) > iResult.invertedList.postings.get(iDoc).positions.get(y)))
	        		        {
	        		           y++;
	        		        }
	        			if((y < iResult.invertedList.postings.get(iDoc).positions.size())&&(iResult.invertedList.postings.get(iDoc).positions.get(y)-result.invertedList.postings.get(rDoc).positions.get(x)<=near))  //Match
	        			  {
	        				result.invertedList.postings.get(rDoc).positions.set(x,iResult.invertedList.postings.get(iDoc).positions.get(y));
	        				x++;  //Location in rDoc
	        				y++;   //Location in iDoc
	        				//flag=1;
	        			  }
	        			//else if (result.invertedList.postings.get(p).positions.get(x)<iResult.invertedList.postings.get(q).positions.get(y))
	        				//x++;
	        			else
	        				{    //Remove if there is no match
	        				  result.invertedList.postings.get(rDoc).positions.remove(x);
	        				  result.invertedList.postings.get(rDoc).tf--;
	        				  result.invertedList.ctf--;
	        				}
	        	        }
	        	if(result.invertedList.postings.get(rDoc).positions.size()<=0)
	           {
	        	   result.invertedList.postings.remove(rDoc);
	        	   result.invertedList.df--;
	        	}
	           else
	           {
	        	   rDoc++;
	           }
	      iDoc++;
	      }	
	           else// If the rDoc document appears in both lists, keep it, otherwise discard it.
	           {
	        	   result.invertedList.postings.remove(rDoc);
	        	   result.invertedList.df--;
	           }
	      }
	    }

	    return result;
	  }
	}


