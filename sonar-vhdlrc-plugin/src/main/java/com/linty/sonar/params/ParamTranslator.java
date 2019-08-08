package com.linty.sonar.params;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ParamTranslator {
  
  public final static String STRING_PARAM = "StringParam";
  public final static String POSITION = "Position";
  
  public final static String INT_PARAM = "IntParam";
  public final static String RELATION = "Relation";
  
  public final static String RANGE_PARAM = "RangeParam";
  public final static String RANGE = "Range";
  
  //Constants of StringParam
  //Possible values of <hb:Position> in handbook
  public final static String PREFIX = "Prefix";
  public final static String SUFFIX = "Suffix";
  public final static String CONTAIN = "Contain";
  public final static String EQUAL = "Equal"; 
  
  public final static String STAR = "*";
  
  //Constants of IntParam
  //Possible values of <hb:Relation> in handbook
  public final static String LT  = "LT";
  public final static String LET = "LET";
  public final static String E   = "E";
  public final static String GET = "GET";
  public final static String GT  = "GT";
  
  public static final Map<String,String> INT_FIELDS_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  
  //Constants of RangeParam
  //Possible values of <hb:Range> in handbook
  public final static String LT_GT  = "LT_GT";
  public final static String LET_GT = "LET_GT";
  public final static String LET_GET   = "LET_GET";
  public final static String LT_GET = "LT_GET";
  
  public static final Map<String,String> RANGE_FIELDS_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  
  static {
    INT_FIELDS_MAP.put(LT,  "<");
    INT_FIELDS_MAP.put(LET, "<=");
    INT_FIELDS_MAP.put(E,   "=");
    INT_FIELDS_MAP.put(GET, ">=");
    INT_FIELDS_MAP.put(GT,  ">");
    
    RANGE_FIELDS_MAP.put(LT_GT,  "< <");
    RANGE_FIELDS_MAP.put(LET_GT, "<= <");
    RANGE_FIELDS_MAP.put(LET_GET,"<= <=");
    RANGE_FIELDS_MAP.put(LT_GET, "< <=");
    
  }
  

  
  
    
  

}
