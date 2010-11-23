// (c) Copyright 2010 Odiago, Inc.

package com.odiago.rtengine.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic DAG of nodes.
 */
public class DAG<NODETYPE extends DAGNode<NODETYPE>> {
  
  private List<NODETYPE> roots;

  public DAG() {
    roots = new ArrayList<NODETYPE>();
  }

  /**
   * An operator which should be applied to all elements of the DAG.
   */
  public abstract static class Operator<NODE extends DAGNode<NODE>> {
    public abstract void process(NODE node) throws DAGOperatorException;
  }

  /**
   * Adds the specified node as a root of the DAG.
   */
  public void addRoot(NODETYPE node) {
    roots.add(node);
  }
  
  /** @return the set of root nodes for this DAG. */
  public List<NODETYPE> getRootSet() {
    return roots;
  }

  /**
   * Given a complete flow specification for a sub-flow 'other', add
   * its nodes to our flow specification.
   */
  public void addNodesFromDAG(DAG<NODETYPE> other) {
    roots.addAll(other.roots);
  }

  /**
   * Perform a breadth-first traversal over the flow specification,
   * applying the specified operator to each node.
   */
  public void bfs(Operator op) throws DAGOperatorException {
    clearAllMarks();
    // Work queue for BFS.
    List<NODETYPE> work = new LinkedList<NODETYPE>();
    work.addAll(roots);
    while (!work.isEmpty()) {
      NODETYPE curNode = work.remove(0);
      if (!curNode.isSeen()) {
        op.process(curNode);
        work.addAll(curNode.getChildren());
        curNode.markSeen();
      }
    }
  }

  /**
   * Perform a depth-first traversal over the flow specification,
   * applying the specified operator to each node.
   */
  public void dfs(Operator op) throws DAGOperatorException {
    clearAllMarks();
    // Work stack for DFS.
    List<NODETYPE> work = new LinkedList<NODETYPE>();
    work.addAll(roots);
    while (!work.isEmpty()) {
      NODETYPE curNode = work.remove(work.size() - 1);
      if (!curNode.isSeen()) {
        op.process(curNode);
        work.addAll(curNode.getChildren());
        curNode.markSeen();
      }
    }
  }

  /**
   * Perform a breadth-first search "in reverse", starting at the final nodes
   * in the DAG and working backward toward the roots, applying the specified
   * operator to each node.
   */
  public void reverseBfs(Operator op) throws DAGOperatorException {
    List<NODETYPE> work = new LinkedList<NODETYPE>();
    work.addAll(getLastLayer());
    clearAllMarks();
    while (!work.isEmpty()) {
      NODETYPE curNode = work.remove(0);
      if (!curNode.isSeen()) {
        op.process(curNode);
        work.addAll(curNode.getParents());
        curNode.markSeen();
      }
    }
  }

  /**
   * Clear all the seen bits in the current graph by using a separate
   * special-purpose BFS traversal.
   */
  private void clearAllMarks() {
    List<NODETYPE> work = new LinkedList<NODETYPE>();
    work.addAll(getRootSet());
    while (!work.isEmpty()) {
      NODETYPE curNode = work.remove(0);
      if (curNode.isSeen()) {
        // clear this mark bit.
        curNode.clearSeen();
        work.addAll(curNode.getChildren());
      }
    }
  }

  /**
   * @return a list of all nodes with no outputs.
   */
  public List<NODETYPE> getLastLayer() {
    
    final List<NODETYPE> lastLayer = new ArrayList<NODETYPE>();

    // Execute an operator over all nodes; gather a list of all
    // nodes which do not have children.
    try {
      bfs(new Operator<NODETYPE>() {
        @Override
        public void process(NODETYPE node) {
          if (node.getChildren().size() == 0) {
            lastLayer.add(node);
          }
        }
      });
    } catch (DAGOperatorException doe) {
      // Impossible with this operator.
    }

    return lastLayer;
  }

  /**
   * Sets 'node' as a child of every node in the current "bottom" layer
   * of the DAG.
   */
  public void attachToLastLayer(NODETYPE node) {
    List<NODETYPE> lastLayer = getLastLayer();
    for (NODETYPE parent : lastLayer) {
      parent.addChild(node);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    // Use a BFS operator to stringify all nodes in the DAG.
    try {
      bfs(new Operator<NODETYPE>() {
        @Override
        public void process(NODETYPE node) {
          sb.append(node.toString());
        }
      });
    } catch (DAGOperatorException doe) {
      // Impossible with this operator.
    }

    return sb.toString();
  }
}
