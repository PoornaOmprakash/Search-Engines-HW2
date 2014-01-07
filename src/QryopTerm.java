/*
 *  Copyright (c) 2013, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.IOException;

public class QryopTerm extends Qryop {

  String term;
  public String field;

  /* Constructors */
  public QryopTerm(String t) {
    this.term = t;
    this.field = "body"; /* Default field if none is specified */
  }

  /* Constructor */
  public QryopTerm(String t, String f) {
    this.term = t;
    this.field = f;
  }
  
  
  String qryTerm()
  {
	  return this.term;
  }
  /**
   * Evaluate the query operator.
   */
  public QryResult evaluate() throws IOException {
    QryResult result = new QryResult();
    //System.out.println(term+" "+field);
    result.invertedList = new InvList(this.term, this.field);
    return result;
  }
}
