package com.linty.sonar.params;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import org.fest.util.VisibleForTesting;
import org.sonar.api.batch.rule.ActiveRule;

public class ParamTranslator {
  
  public static final String STRING_PARAM = "StringParam";
  public static final String POSITION = "Position";
  
  public static final String INT_PARAM = "IntParam";
  public static final String RELATION = "Relation";
  
  public static final String RANGE_PARAM = "RangeParam";
  public static final String RANGE = "Range";
  
  //Constants of StringParam
  //Possible values of <hb:Position> in handbook
  public static final String PREFIX = "Prefix";
  public static final String SUFFIX = "Suffix";
  public static final String CONTAIN = "Contain";
  public static final String EQUAL = "Equal"; 
  public static final ImmutableList<String> STRING_FIELDS_LIST = ImmutableList.of(
    PREFIX,
    SUFFIX,
    CONTAIN,
    EQUAL);
  
  public static final String STAR = "*";
  
  //Constants of IntParam
  //Possible values of <hb:Relation> in handbook
  public static final String LT  = "LT";
  public static final String LET = "LET";
  public static final String E   = "E";
  public static final String GET = "GET";
  public static final String GT  = "GT";
  public static final ImmutableBiMap<String, String> INT_FIELDS_MAP = ImmutableBiMap.of(
    LT  , "<"  ,
    LET , "<=" ,
    E   , "="  ,
    GET , ">=" ,
    GT  , ">"  );
  
  //Constants of RangeParam
  //Possible values of <hb:Range> in handbook
  public static final String LT_GT  = "LT_GT";
  public static final String LET_GT = "LET_GT";
  public static final String LET_GET   = "LET_GET";
  public static final String LT_GET = "LT_GET";
  public static final ImmutableBiMap<String, String> RANGE_FIELDS_MAP = ImmutableBiMap.of(
    LT_GT   , "<_<"   ,
    LET_GT  , "<=_<"  ,
    LET_GET , "<=_<=" ,
    LT_GET  , "<_<="  );
  
  //Method to check the parameter type
  public static boolean hasStringParam(ActiveRule ar) {
    return ar.param(ZamiaStringParam.PARAM_KEY) != null;
  }
  
  public static boolean hasIntParam(ActiveRule ar) {
    return ar.param(ZamiaIntParam.LI_KEY) != null;
  }
  
  public static boolean hasRangeParam(ActiveRule ar) {
    return ar.param(ZamiaRangeParam.MIN_KEY) != null;
  }
  
  public static String positionOf(String param) {
    if(param.startsWith(STAR) && param.endsWith(STAR)) {
      return CONTAIN;
    } else if (param.startsWith(STAR)) {
      return SUFFIX;
    } else if (param.endsWith(STAR)) {
      return PREFIX;
    } else {
      return EQUAL;
    }
  }
  
  public static String stringValueOf(String param) {
    return param.replaceAll("\\*", "");
  }
  
  public static String relationOf(String param) {
    return INT_FIELDS_MAP.inverse().get(param);
  }
  
  public static String rangeOf(String param) {
    return RANGE_FIELDS_MAP.inverse().get(param);
  }
  

  @VisibleForTesting
  protected ParamTranslator() {
    throw new IllegalStateException("Utility class");
  }
}
