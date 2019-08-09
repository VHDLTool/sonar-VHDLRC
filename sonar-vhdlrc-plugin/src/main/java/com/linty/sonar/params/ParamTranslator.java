package com.linty.sonar.params;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;

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
  public static final ImmutableList<String> STRING_FIELDS_LIST = ImmutableList.of(
    PREFIX,
    SUFFIX,
    CONTAIN,
    EQUAL);
  
  public final static String STAR = "*";
  
  //Constants of IntParam
  //Possible values of <hb:Relation> in handbook
  public final static String LT  = "LT";
  public final static String LET = "LET";
  public final static String E   = "E";
  public final static String GET = "GET";
  public final static String GT  = "GT";
  public static final ImmutableBiMap<String, String> INT_FIELDS_MAP = ImmutableBiMap.of(
    LT  , "<"  ,
    LET , "<=" ,
    E   , "="  ,
    GET , ">=" ,
    GT  , ">"  );
  
  //Constants of RangeParam
  //Possible values of <hb:Range> in handbook
  public final static String LT_GT  = "LT_GT";
  public final static String LET_GT = "LET_GT";
  public final static String LET_GET   = "LET_GET";
  public final static String LT_GET = "LT_GET";
  public static final ImmutableBiMap<String, String> RANGE_FIELDS_MAP = ImmutableBiMap.of(
    LT_GT   , "< <"   ,
    LET_GT  , "<= <"  ,
    LET_GET , "<= <=" ,
    LT_GET  , "< <="  );

}
