/*
 *  This software illustrates the architecture for the portion of a
 *  search engine that evaluates queries.  It is a template for class
 *  homework assignments, so it emphasizes simplicity over efficiency.
 *  It implements an unranked Boolean retrieval model, however it is
 *  easily extended to other retrieval models.  For more information,
 *  see the ReadMe.txt file.
 *
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.BufferedInputStream;

import java.util.Comparator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.lang.*;
import java.lang.*;
import java.util.Comparator;
import java.util.Collections;

public class QryEval {
	
  static String retrievalAlgortihm;
  public static DocLengthStore dls;
  public static double lambda;
  public static double mu;
  public static String smoothing;
  public static float k1;
  public static float b;
  public static float k3;
  public static int N;
  public static int qlen;
  public static PrintWriter writer;
  static String usage = "Usage:  java " + System.getProperty("sun.java.command")
      + " paramFile\n\n";

  /**
   * The index file reader is accessible via a global variable. This isn't great programming style,
   * but the alternative is for every query operator to store or pass this value, which creates its
   * own headaches.
   */
  public static IndexReader READER;

  public static EnglishAnalyzerConfigurable analyzer =  new EnglishAnalyzerConfigurable (Version.LUCENE_43);
  static {
    analyzer.setLowercase(true);
    analyzer.setStopwordRemoval(true);
    analyzer.setStemmer(EnglishAnalyzerConfigurable.StemmerType.KSTEM);
  }

  /**
   * 
   * @param args The only argument should be one file name, which indicates the parameter file.
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    	  
	  
    // must supply parameter file
    if (args.length < 1) {
      System.err.println(usage);
      System.exit(1);
    }

    // read in the parameter file; one parameter per line in format of key=value
    Map<String, String> params = new HashMap<String, String>();
    Scanner scan = new Scanner(new File(args[0]));
    String line = null;
    
    /*line = scan.nextLine();
    String[] pair = line.split("=");
    String p = pair[1].trim();
    //System.out.println(p);*/
    
    
    
	//String qry="#AND(barack #OR(obama president))";
	//br.close();
    //fs.close();
    
   do {
      line = scan.nextLine();
      String [] pair = line.split("=");
      params.put(pair[0].trim(), pair[1].trim());
    } while (scan.hasNext());
    
   FileInputStream fr = new FileInputStream(params.get("queryFilePath"));
   BufferedReader br = new BufferedReader(new InputStreamReader(fr)); 
   Scanner s=new Scanner(new File(params.get("queryFilePath")));
   //fflush(br);
   //fr.flush();
   retrievalAlgortihm=params.get("retrievalAlgorithm");
   
    // parameters required for this example to run
    if (!params.containsKey("indexPath")) {
      System.err.println("Error: Parameters were missing.");
      System.exit(1);
    }
   
  
	  
	  scan.close();
	// open the index
	    READER = DirectoryReader.open(FSDirectory.open(new File(params.get("indexPath"))));

	    if (READER == null) {
	      System.err.println(usage);
	      System.exit(1);
	    }
	  
	    dls = new DocLengthStore(READER);
	    if(retrievalAlgortihm.equals("Indri"))
	    		{
	    	      lambda=Float.parseFloat(params.get("Indri:lambda"));
	              mu=Float.parseFloat(params.get("Indri:mu"));
	              smoothing=params.get("Indri:smoothing");
	    		}
	    if(retrievalAlgortihm.equals("BM25"))
	    {
	       k1=Float.parseFloat(params.get("BM25:k_1"));
	       b=Float.parseFloat(params.get("BM25:b"));
	       k3=Float.parseFloat(params.get("BM25:k_3"));
	    }
	    N=QryEval.READER.numDocs();         //Number of documents in the corpus
	  //String temp;//=br.readLine();
	  long StartTime=System.nanoTime();
	  String temp;//=br.readLine();//=s.nextLine();
	  //System.out.println(temp);
	  while((temp=br.readLine())!=null)//.hasNextLine())//!=null)
	  {
		//System.out.println(i);
	    //String temp = br.readLine();
	    int index=temp.indexOf(":");
	    //System.out.println(index);
	    String qryID=temp.substring(0,index);
	    String qry=temp.substring(index+1,temp.length());
	    //System.out.println(temp);
	    List <String> m=manager.get_tokens(qry);      //Pass the query to the parser
	    //System.out.println(m);
	    if((QryEval.retrievalAlgortihm.equals("RankedBoolean"))|(QryEval.retrievalAlgortihm.equals("RankedBoolean")))
	    	manager.root=new QryopOr();                      //Use #OR as a default query operator
	    if(QryEval.retrievalAlgortihm.equals("BM25"))
	    	manager.root=new QryopBM25Sum();
	    if(QryEval.retrievalAlgortihm.equals("Indri"))
	    	manager.root=new QryopAnd();
	    //manager.root=new QryopOr();                      //Use #OR as a default query operator
	    manager.parse_tree(m);
	    printResults(qryID, manager.root.evaluate());
	    //temp=br.readLine();
	  }
	  
	  FileOutputStream stream = new FileOutputStream("/Users/poornaomprakash/Desktop/output.txt");
      OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
      writer = new PrintWriter(sw);
      
	  long EndTime = System.nanoTime();
	  long time=EndTime-StartTime;
	  //System.out.println(time);
	  
	  br.close();
	  fr.close();
	  s.close();
  }

  /**
   *  Get the external document id for a document specified by an
   *  internal document id.  Ordinarily this would be a simple call to
   *  the Lucene index reader, but when the index was built, the
   *  indexer added "_0" to the end of each external document id.  The
   *  correct solution would be to fix the index, but it's too late
   *  for that now, so it is fixed here before the id is returned.
   * 
   * @param iid The internal document id of the document.
   * @throws IOException 
   */
  static String getExternalDocid (int iid) throws IOException {
    Document d = QryEval.READER.document (iid);
    String eid = d.get ("externalId");

    if ((eid != null) && eid.endsWith ("_0"))
      eid = eid.substring (0, eid.length()-2);

    return (eid);
  }

  /**
   * Prints the query results. 
   * 
   * THIS IS NOT THE CORRECT OUTPUT FORMAT.
   * YOU MUST CHANGE THIS METHOD SO THAT IT OUTPUTS IN THE FORMAT SPECIFIED IN THE HOMEWORK PAGE, 
   * WHICH IS: 
   * 
   * QueryID Q0 DocID Rank Score RunID
   * 
   * @param queryName Original query.
   * @param result Result object generated by {@link Qryop#evaluate()}.
   * @throws IOException 
   */
  static void printResults(String QryID, QryResult result) throws IOException {

    //System.out.println(queryName + ":  ");
    if (result.docScores.scores.size() < 1) {
      System.out.println(QryID+" Q0 "+" dummy 1 0 run-1");
    } else {
    	Collections.sort(result.docScores.scores, result.docScores.sc);
        //System.out.println(result.docScores.scores.size());
        //System.out.println(result.docScores.scores.get(result.docScores.scores.size()-1).score);
        for (int i = result.docScores.scores.size() - 1; i >= 0 && i >= result.docScores.scores.size() - 100; i--) {
          System.out.println(QryID + " Q0 " + getExternalDocid(result.docScores.getDocid(i)) + " "
                  + (result.docScores.scores.size()-i) + " " + result.docScores.getDocidScore(i)
                  + " run-1");
    	
    	/*List<QryResult> list = new ArrayList<QryResult>();
    	Comparator<QryResult> comparator = new Comparator<QryResult>();
    			{
    		     public int compare(QryResult q1,QryResult q2)
    		     {
    		    	 
    		     }
    			}*/
 	//System.out.println(result.docScores.scores.size());
  /*for(int i=0;i<result.docScores.scores.size();i++)  //Sort the results by scores
  	  {     
    		//System.out.println(i);
    		for(int j=i+1;j<result.docScores.scores.size();j++)
    		{
  		      if(result.docScores.getDocidScore(i)<result.docScores.getDocidScore(j))
  		      {
  		    	result.docScores.swap(i,j);  
  		      }
    		}
  	  }*/
    /*  for (i = 0; i < result.docScores.scores.size() && i < 100; i++) {
        System.out.println(QryID + " Q0 "
			   + getExternalDocid (result.docScores.getDocid(i))
			   + " "+(i+1)+" "
			   + result.docScores.getDocidScore(i)
			   + " run-1 "); */
      }
    }
   }
  
  /**
   * Given a query string, returns the terms one at a time with stopwords
   * removed and the terms stemmed using the Krovetz stemmer. 
   * 
   * Use this method to process raw query terms. 
   * 
   * @param query String containing query
   * @return Array of query tokens
   * @throws IOException
   */
  static String[] tokenizeQuery(String query) throws IOException {
    
    TokenStreamComponents comp = analyzer.createComponents("dummy", new StringReader(query));
    TokenStream tokenStream = comp.getTokenStream();

    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    
    List<String> tokens = new ArrayList<String>();
    while (tokenStream.incrementToken()) {
      String term = charTermAttribute.toString();
      tokens.add(term);
    }
    return tokens.toArray(new String[tokens.size()]);
  }

}
