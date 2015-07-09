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
  EmptySpace maxArea;
  final List<XTab> maxAreaXTabs = new ArrayList<XTab>();
  final List<YTab> maxAreaYTabs = new ArrayList<YTab>();

  AlgebraData algebraData;

  public EmptyAreaFinder(AlgebraData algebraData) {
    this.algebraData = algebraData;
  }

  public EmptySpace getMaxArea() {
    return maxArea;
  }

  public List<XTab> getMaxAreaXTabs() {
    return maxAreaXTabs;
  }

  public List<YTab> getMaxAreaYTabs() {
    return maxAreaYTabs;
  }

  public AlgebraData getTransformedAlgebraData() {
    return algebraData;
  }

  public boolean find(float x, float y) {
    EmptySpace space = findEmptySpace(x, y);
    if (space == null)
      return false;
    // start the search from the minimal space
    EmptySpace minSpace = minimizeArea(space, x, y);
    maxArea = maximizeArea(minSpace, maxAreaXTabs, maxAreaYTabs);
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
      if (tab == null)
        return space;
      Variable spaceTab = direction.getTab(space);
      if (tab == spaceTab || LayoutSpec.fuzzyEquals(tab, spaceTab))
        break;
      target = tab.getValue();
      // try to split
      EmptySpace newSpace = TilingAlgebra.split(data, space, tab, map, direction);
      if (newSpace != null)
        return space;
    }
    return space;
  }

  static class MaximizeCandidate<Tab extends Variable, OrthTab extends Variable> {
    public EmptySpace candidate;
    public AlgebraData data;
    final public List<Tab> containingTabs;
    final public IDirection<Tab, OrthTab> direction;

    public MaximizeCandidate(List<Tab> containingTabs, IDirection<Tab, OrthTab> direction) {
      this.containingTabs = containingTabs;
      this.direction = direction;
    }

    public double maximize(EmptySpace area, AlgebraData orgData, Tab border, IDirection<OrthTab, Tab> orthDirection) {
      double size = 0;
      if (direction.getTab(area) != border) {
        candidate = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        data = LayoutEditor.cloneWithReplacedEmptySpaces(orgData, area, candidate);
        if (TilingAlgebra.extend(data, candidate, direction)) {
          size = getSize(candidate);
        } else {
          candidate = null;
          data = null;
        }
      }
      return size;
    }

    private double getSize(IArea area) {
      return (area.getRight().getValue() - area.getLeft().getValue()) * (area.getBottom().getValue() - area.getTop().getValue());
    }
  }

  private EmptySpace maximizeArea(EmptySpace area, List<XTab> containingXTabs, List<YTab> containingYTabs) {
    IDirection<XTab, YTab> left = new LeftDirection();
    IDirection<YTab, XTab> top = new TopDirection();
    IDirection<XTab, YTab> right = new RightDirection();
    IDirection<YTab, XTab> bottom = new BottomDirection();

    while (true) {
      MaximizeCandidate<XTab, YTab> leftCandidate = new MaximizeCandidate<XTab, YTab>(containingXTabs, left);
      MaximizeCandidate<YTab, XTab> topCandidate = new MaximizeCandidate<YTab, XTab>(containingYTabs, top);
      MaximizeCandidate<XTab, YTab> rightCandidate = new MaximizeCandidate<XTab, YTab>(containingXTabs, right);
      MaximizeCandidate<YTab, XTab> bottomCandidate = new MaximizeCandidate<YTab, XTab>(containingYTabs, bottom);

      double leftSize = leftCandidate.maximize(area, algebraData, algebraData.getLeft(), bottom);
      double rightSize = rightCandidate.maximize(area, algebraData, algebraData.getRight(), bottom);
      double topSize = topCandidate.maximize(area, algebraData, algebraData.getTop(), right);
      double bottomSize = bottomCandidate.maximize(area, algebraData, algebraData.getBottom(), right);

      // choose best candidate
      MaximizeCandidate candidate = null;
      if (leftCandidate.candidate == null && rightCandidate.candidate == null && topCandidate.candidate == null
          && bottomCandidate.candidate == null) return area;
      if (leftCandidate.candidate != null && leftSize > rightSize && leftSize > topSize && leftSize > bottomSize) {
        candidate = leftCandidate;
      } else if (rightCandidate.candidate != null && rightSize > topSize && rightSize > bottomSize) {
        candidate = rightCandidate;
      } else if (topCandidate.candidate != null && topSize > bottomSize) {
        candidate = topCandidate;
      } else if (bottomCandidate.candidate != null) {
        candidate = bottomCandidate;
      }
      if (candidate != null) {
        candidate.containingTabs.add(candidate.direction.getTab(area));
        area = candidate.candidate;
        algebraData = candidate.data;
      }
    }
  }
}
