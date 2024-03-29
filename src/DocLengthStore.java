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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.MultiFields;

/**
 * DocLengthStore is used to access the document lengths of indexed docs.
 */
public class DocLengthStore  {

  private IndexReader reader;
  private  Map<String, NumericDocValues> values = new HashMap<String, NumericDocValues>();

  /**
   * @param reader IndexReader object created in {@link QryEval}.
   */
  public DocLengthStore(IndexReader reader) throws IOException {
    this.reader = reader;
    for (String field : MultiFields.getIndexedFields(reader)) {
      this.values.put(field, MultiDocValues.getNormValues(reader, field));      
    }
  }

  /**
   * Returns the length of the specified field in the specified document.
   *
   * @param fieldname Name of field to access lengths. "body" is the default
   * field.
   * @param docid The internal docid in the lucene index.
   */
  public long getDocLength(String fieldname, int docid) throws IOException {
    return values.get(fieldname).get(docid);
  }
}
