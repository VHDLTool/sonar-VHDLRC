package com.linty.sonar.params;

import com.google.common.collect.ImmutableList;

public class ZamiaStringParam extends ZamiaParam {
  
  private final static String DESCRIPTION = "Comma seperated pattern to match, ex : *aa,*b*,c ";
  private final static String PREFIX = "Prefix";
  private final static String SUFFIX = "Suffix";
  private final static String CONTAINS = "Contains";
  private final static String EQUAL = "Equal"; 
  private final static String STAR = "*";
  
  protected static ImmutableList<String> FIELDS = ImmutableList.of(
    DESCRIPTION,
    PREFIX,
    SUFFIX,
    CONTAINS,
    EQUAL);
   
  public ZamiaStringParam(String paramId) {
    super(paramId);
    PARAM_DESCRIPTION = DESCRIPTION;
  }

}
