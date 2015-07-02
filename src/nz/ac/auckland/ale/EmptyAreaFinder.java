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

  AlgebraData algebraData;

  public EmptyAreaFinder(AlgebraData myAlgebraData) {
    this.algebraData = cloneWithReplacedEmptySpaces(myAlgebraData, null, null);
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
    EmptySpace space = findEmptySpace(x, y);
    if (space == null)
      return false;
    // start the search from the minimal space
    EmptySpace minSpace = minimizeArea(space, x, y);
    space = maximizeArea(minSpace, maxAreaXTabs, maxAreaYTabs);

    maxArea = new AreaCandidate(space.getLeft(), space.getTop(), space.getRight(), space.getBottom());
    return true;
  }

  private EmptySpace findEmptySpace(float x, float y) {
    for (EmptySpace space : algebraData.getEmptySpaces()) {
      if (space.getRect().contains(x, y))
        return space;
    }
    return null;
  }

  interface ITabFinder<Tab extends Variable> {
    Tab find(double value);
  }

  class LeftTabFinder implements ITabFinder<XTab> {
    @Override
    public XTab find(double value) {
      return findFirstSmallerTab(value, algebraData.getSortedXTabs(), algebraData.getXTabEdges(), new LeftDirection());
    }
  }

  class RightTabFinder implements ITabFinder<XTab> {
    @Override
    public XTab find(double value) {
      return findFirstLargerTab(value, algebraData.getSortedXTabs(), algebraData.getXTabEdges(), new RightDirection());
    }
  }

  class TopTabFinder implements ITabFinder<YTab> {
    @Override
    public YTab find(double value) {
      return findFirstSmallerTab(value, algebraData.getSortedYTabs(), algebraData.getYTabEdges(), new TopDirection());
    }
  }

  class BottomTabFinder implements ITabFinder<YTab> {
    @Override
    public YTab find(double value) {
      return findFirstLargerTab(value, algebraData.getSortedYTabs(), algebraData.getYTabEdges(), new BottomDirection());
    }
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

  private AlgebraData cloneWithReplacedEmptySpaces(AlgebraData layoutStructure, EmptySpace old, EmptySpace replacement) {
    AlgebraData clone = new AlgebraData(layoutStructure.getLeft(), layoutStructure.getTop(), layoutStructure.getRight(), layoutStructure.getBottom());
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

  private EmptySpace minimizeArea(EmptySpace space, float x, float y) {
    Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
    Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();
    space = minimizeSide(algebraData, space, x, new LeftDirection(), xTabEdgeMap, new LeftTabFinder());
    space = minimizeSide(algebraData, space, x, new RightDirection(), xTabEdgeMap, new RightTabFinder());
    space = minimizeSide(algebraData, space, y, new TopDirection(), yTabEdgeMap, new TopTabFinder());
    space = minimizeSide(algebraData, space, y, new BottomDirection(), yTabEdgeMap, new BottomTabFinder());
    return space;
  }

  private <Tab extends Variable> EmptySpace minimizeSide(AlgebraData data, EmptySpace space, double target, IDirection direction,
                                                         Map<Tab, Edge> map, ITabFinder<Tab> tabFinder) {
    while (true) {
      Tab tab = tabFinder.find(target);
      assert tab != null; // there should always be a tab within the empty space!
      if (tab == direction.getTab(space))
        break;
      target = tab.getValue();
      // try to split
      EmptySpace newSpace = TilingAlgebra.split(data, space, tab, map, direction);
      if (newSpace != null)
        return newSpace;
    }
    return space;
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
      AlgebraData leftStructure = null;
      EmptySpace rightCandidate = null;
      AlgebraData rightStructure = null;
      EmptySpace topCandidate = null;
      AlgebraData topStructure = null;
      EmptySpace bottomCandidate = null;
      AlgebraData bottomStructure = null;
      Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
      Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();

      if (area.getLeft() != algebraData.getLeft()) {
        leftCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        leftStructure = cloneWithReplacedEmptySpaces(algebraData, area, leftCandidate);
        leftStructure.removeArea(area);
        leftStructure.addArea(leftCandidate);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);
        if (trafo.extend(leftCandidate, left, xTabEdgeMap, bottom, yTabEdgeMap)) {
          leftSize = getSize(leftCandidate);
        } else {
          leftCandidate = null;
          leftStructure = null;
        }
      }
      if (area.getRight() != algebraData.getRight()) {
        rightCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        rightStructure = cloneWithReplacedEmptySpaces(algebraData, area, rightCandidate);
        rightStructure.removeArea(area);
        rightStructure.addArea(rightCandidate);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);
        if (trafo.extend(rightCandidate, right, xTabEdgeMap, bottom, yTabEdgeMap)) {
          rightSize = getSize(rightCandidate);
        } else {
          rightCandidate = null;
          rightStructure = null;
        }
      }
      if (area.getTop() != algebraData.getTop()) {
        topCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        topStructure = cloneWithReplacedEmptySpaces(algebraData, area, topCandidate);
        topStructure.removeArea(area);
        topStructure.addArea(topCandidate);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);
        if (trafo.extend(topCandidate, top, yTabEdgeMap, right, xTabEdgeMap)) {
          topSize = getSize(topCandidate);
        } else {
          topCandidate = null;
          topStructure = null;
        }
      }
      if (area.getBottom() != algebraData.getBottom()) {
        bottomCandidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        bottomStructure = cloneWithReplacedEmptySpaces(algebraData, area, bottomCandidate);
        bottomStructure.removeArea(area);
        bottomStructure.addArea(bottomCandidate);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);
        if (trafo.extend(bottomCandidate, bottom, yTabEdgeMap, right, xTabEdgeMap)) {
          bottomSize = getSize(bottomCandidate);
        } else {
          bottomCandidate = null;
          bottomStructure = null;
        }
      }

      if (leftCandidate == null && rightCandidate == null && topCandidate == null && bottomCandidate == null) return area;
      if (leftCandidate != null && leftSize > rightSize && leftSize > topSize && leftSize > bottomSize) {
        algebraData = leftStructure;
        area = leftCandidate;
        containingXTabs.add(area.getLeft());
      } else if (rightCandidate != null && rightSize > topSize && rightSize > bottomSize) {
        algebraData = rightStructure;
        area = rightCandidate;
        containingXTabs.add(area.getRight());
      } else if (topCandidate != null && topSize > bottomSize) {
        algebraData = topStructure;
        area = topCandidate;
        containingYTabs.add(area.getTop());
      } else if (bottomCandidate != null) {
        algebraData = bottomStructure;
        area = bottomCandidate;
        containingYTabs.add(area.getBottom());
      }
    }
  }

  private double getSize(IArea area) {
    return (area.getRight().getValue() - area.getLeft().getValue()) * (area.getBottom().getValue() - area.getTop().getValue());
  }
}
