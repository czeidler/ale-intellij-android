/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.ac.auckland.ale;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EmptyAreaFinder {
  AreaCandidate maxArea;
  final List<XTab> maxAreaXTabs = new ArrayList<XTab>();
  final List<YTab> maxAreaYTabs = new ArrayList<YTab>();

  LayoutStructure layoutStructure;

  public EmptyAreaFinder(LayoutStructure layoutStructure) {
    this.layoutStructure = cloneWithReplacedEmptySpaces(layoutStructure, null, null);
  }

  public AreaCandidate getMaxArea() {
    return maxArea;
  }

  public List<XTab> getMaxAreaXTabs() {
    return maxAreaXTabs;
  }

  public List<YTab> getMaxAreaYTabs() {
    return maxAreaYTabs;
  }

  public boolean find(float x, float y) {
    LambdaTransformation trafo = new LambdaTransformation(layoutStructure);
    XTab left = findTabLeftOf(x);
    YTab top = findTabAbove(y);
    XTab right = findTabRightOf(x);
    YTab bottom = findTabBellow(y);
    if (left == null || top == null || right == null || bottom == null)
      return false;
    // Find max area: first find the min area then maximize it
    EmptySpace space = trafo.makeSpace(left, top, right, bottom);
    if (space == null)
      return false;
    space = maximizeArea(space, maxAreaXTabs, maxAreaYTabs);

    maxArea = new AreaCandidate(space.getLeft(), space.getTop(), space.getRight(), space.getBottom());
    return true;
  }

  private XTab findTabLeftOf(double x) {
    return findFirstSmallerTab(x, layoutStructure.getSortedXTabs(), layoutStructure.getXTabEdges(), new LeftDirection());
  }

  private XTab findTabRightOf(double x) {
    return findFirstLargerTab(x, layoutStructure.getSortedXTabs(), layoutStructure.getXTabEdges(), new RightDirection());
  }

  private YTab findTabAbove(double y) {
    return findFirstSmallerTab(y, layoutStructure.getSortedYTabs(), layoutStructure.getYTabEdges(), new TopDirection());
  }

  private YTab findTabBellow(double y) {
    return findFirstLargerTab(y, layoutStructure.getSortedYTabs(), layoutStructure.getYTabEdges(), new BottomDirection());
  }

  private <Tab extends Variable> Tab findFirstLargerTab(double value, List<Tab> tabs, Map<Tab, Edge> edges, IDirection direction) {
    for (int i = 0; i < tabs.size(); i++) {
      Tab tab = tabs.get(i);
      if (tab.getValue() > value) {
        // if there are tabs at the same position pick the first in the chain
        double tabValue = tab.getValue();
        for (i++; i < tabs.size(); i++) {
          Tab nextTab = tabs.get(i);
          if (!LayoutSpec.fuzzyEquals(tabValue, nextTab.getValue()))
            break;

          if (Edge.isInChain(nextTab, tab, edges, direction))
            tab = nextTab;
        }
        return tab;
      }
    }
    return null;
  }

  private <Tab extends Variable> Tab findFirstSmallerTab(double value, List<Tab> tabs, Map<Tab, Edge> edges, IDirection direction) {
    for (int i = tabs.size() - 1; i >= 0; i--) {
      Tab tab = tabs.get(i);
      if (tab.getValue() < value) {
        // if there are tabs at the same position pick the first in the chain
        double tabValue = tab.getValue();
        for (i--; i >= 0; i--) {
          Tab nextTab = tabs.get(i);
          if (!LayoutSpec.fuzzyEquals(tabValue, nextTab.getValue()))
            break;

          if (Edge.isInChain(nextTab, tab, edges, direction))
            tab = nextTab;
        }
        return tab;
      }
    }
    return null;
  }

  private LayoutStructure cloneWithReplacedEmptySpaces(LayoutStructure layoutStructure, EmptySpace old, EmptySpace replacement) {
    LayoutStructure clone = new LayoutStructure(layoutStructure.getLeft(), layoutStructure.getTop(), layoutStructure.getRight(),
                                                layoutStructure.getBottom());
    for (Area area : layoutStructure.getAreas())
      clone.addArea(area);

    for (EmptySpace emptySpace : layoutStructure.getEmptySpaces()) {
      EmptySpace emptySpaceClone;
      if (old != emptySpace) {
        emptySpaceClone = new EmptySpace(emptySpace.getLeft(), emptySpace.getTop(), emptySpace.getRight(), emptySpace.getBottom());
      } else
        emptySpaceClone = replacement;
      clone.addArea(emptySpaceClone);
    }
    return clone;
  }

  private EmptySpace maximizeArea(EmptySpace area, List<XTab> containingXTabs, List<YTab> containingYTabs) {
    IDirection left = new LeftDirection();
    IDirection top = new TopDirection();
    IDirection right = new RightDirection();
    IDirection bottom = new BottomDirection();

    while (true) {
      double leftSize = 0;
      double rightSize = 0;
      double topSize = 0;
      double bottomSize = 0;
      EmptySpace leftCandidate = null;
      LayoutStructure leftStructure = null;
      EmptySpace rightCandidate = null;
      LayoutStructure rightStructure = null;
      EmptySpace topCandidate = null;
      LayoutStructure topStructure = null;
      EmptySpace bottomCandidate = null;
      LayoutStructure bottomStructure = null;
      Map<XTab, Edge> xTabEdgeMap = layoutStructure.getXTabEdges();
      Map<YTab, Edge> yTabEdgeMap = layoutStructure.getYTabEdges();

      if (area.getLeft() != layoutStructure.getLeft()) {
        leftCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        leftStructure = cloneWithReplacedEmptySpaces(layoutStructure, area, leftCandidate);
        leftStructure.removeArea(area);
        leftStructure.addArea(leftCandidate);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);
        if (trafo.extend(leftCandidate, left, xTabEdgeMap, bottom, yTabEdgeMap)) {
          leftSize = getSize(leftCandidate);
        } else {
          leftCandidate = null;
          leftStructure = null;
        }
      }
      if (area.getRight() != layoutStructure.getRight()) {
        rightCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        rightStructure = cloneWithReplacedEmptySpaces(layoutStructure, area, rightCandidate);
        rightStructure.removeArea(area);
        rightStructure.addArea(rightCandidate);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);
        if (trafo.extend(rightCandidate, right, xTabEdgeMap, bottom, yTabEdgeMap)) {
          rightSize = getSize(rightCandidate);
        } else {
          rightCandidate = null;
          rightStructure = null;
        }
      }
      if (area.getTop() != layoutStructure.getTop()) {
        topCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        topStructure = cloneWithReplacedEmptySpaces(layoutStructure, area, topCandidate);
        topStructure.removeArea(area);
        topStructure.addArea(topCandidate);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);
        if (trafo.extend(topCandidate, top, yTabEdgeMap, right, xTabEdgeMap)) {
          topSize = getSize(topCandidate);
        } else {
          topCandidate = null;
          topStructure = null;
        }
      }
      if (area.getBottom() != layoutStructure.getBottom()) {
        bottomCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        bottomStructure = cloneWithReplacedEmptySpaces(layoutStructure, area, bottomCandidate);
        bottomStructure.removeArea(area);
        bottomStructure.addArea(bottomCandidate);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);
        if (trafo.extend(bottomCandidate, bottom, yTabEdgeMap, right, xTabEdgeMap)) {
          bottomSize = getSize(bottomCandidate);
        } else {
          bottomCandidate = null;
          bottomStructure = null;
        }
      }

      if (leftCandidate == null && rightCandidate == null && topCandidate == null && bottomCandidate == null) return area;
      if (leftCandidate != null && leftSize > rightSize && leftSize > topSize && leftSize > bottomSize) {
        layoutStructure = leftStructure;
        area = leftCandidate;
        containingXTabs.add(area.getLeft());
      } else if (rightCandidate != null && rightSize > topSize && rightSize > bottomSize) {
        layoutStructure = rightStructure;
        area = rightCandidate;
        containingXTabs.add(area.getRight());
      } else if (topCandidate != null && topSize > bottomSize) {
        layoutStructure = topStructure;
        area = topCandidate;
        containingYTabs.add(area.getTop());
      } else if (bottomCandidate != null) {
        layoutStructure = bottomStructure;
        area = bottomCandidate;
        containingYTabs.add(area.getBottom());
      }
    }
  }

  private double getSize(IArea area) {
    return (area.getRight().getValue() - area.getLeft().getValue()) * (area.getBottom().getValue() - area.getTop().getValue());
  }
}
