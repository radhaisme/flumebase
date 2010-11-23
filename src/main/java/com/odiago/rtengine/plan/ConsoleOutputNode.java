// (c) Copyright 2010 Odiago, Inc.

package com.odiago.rtengine.plan;

import java.util.List;

import com.odiago.rtengine.util.StringUtils;

/**
 * Node that emits specific fields from all input records to the console.
 */
public class ConsoleOutputNode extends PlanNode {
  
  /** The set of field names and types to emit to the console. */
  private List<String> mOutputFields;

  public ConsoleOutputNode(List<String> fields) {
    mOutputFields = fields;
  }

  public List<String> getFields() {
    return mOutputFields;
  }

  @Override 
  public void formatParams(StringBuilder sb) {
    sb.append("ConsoleOutput(");
    StringUtils.formatList(sb, mOutputFields);
    sb.append(")\n");
    formatAttributes(sb);
  }
}
