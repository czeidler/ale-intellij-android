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


import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResizeOperation extends AbstractEditOperation {
  Area targetArea;
  List<XTab> xCandidates;
  List<YTab> yCandidates;
  XTab targetXTab;
  YTab targetYTab;
  boolean detachX = false;
  boolean detachY = false;
  IDirection xDirection;
  IDirection yDirection;

  public ResizeOperation(LayoutEditor layoutEditor, Area resizeArea, XTab xTab, YTab yTab, float x, float y) {
    super(layoutEditor);

    if (xTab != null) {
      if (resizeArea.getLeft() == xTab)
        xDirection = new LeftDirection();
      else if (resizeArea.getRight() == xTab)
        xDirection = new RightDirection();
    }
    assert xDirection != null;
    if (xTab != null) {
      if (resizeArea.getTop() == yTab)
        yDirection = new TopDirection();
      else if (resizeArea.getBottom() == yTab)
        yDirection = new BottomDirection();
    }
    assert yDirection != null;
    if (xDirection != null) {
      xCandidates = getResizeCandidateTabs(resizeArea, layoutEditor.getXTabEdges(), xDirection);
      targetXTab = getTabAt(xCandidates, x);
      if (targetXTab == null && layoutEditor.contentAreaContains(resizeArea, x, y))
        detachX = true;
    }
    if (yDirection != null) {
      yCandidates = getResizeCandidateTabs(resizeArea, layoutEditor.getYTabEdges(), yDirection);
      targetYTab = getTabAt(yCandidates, y);
      if (targetYTab == null && layoutEditor.contentAreaContains(resizeArea, x, y))
        detachY = true;
    }
  }

  private <Tab extends Variable> Tab getTabAt(List<Tab> tabs, float position) {
    for (Tab tab : tabs) {
      if (layoutEditor.isOverTab(tab, position))
        return tab;
    }
    return null;
  }

  private <Tab extends Variable> List<Tab> getResizeCandidateTabs(Area area, Map<Tab, Edge> edges, IDirection direction) {
    List<Tab> candidates = new ArrayList<Tab>();
    for (Map.Entry<Tab, Edge> entry : edges.entrySet()) {
      Tab currentTab = entry.getKey();
      if (currentTab == direction.getTab(area) || currentTab == direction.getOppositeTab(area))
        continue;
      if (!isInChain(edges.get(currentTab), direction.getOppositeTab(area), edges, direction))
        candidates.add(currentTab);
    }
    return candidates;
  }

  private <Tab extends Variable> boolean isInChain(Edge edge, Variable tab, Map<Tab, Edge> edges, IDirection direction) {
    for (Area area : direction.getAreas(edge)) {
      Variable currentTab = direction.getTab(area);
      if (currentTab == tab)
        return true;
      if (isInChain(edges.get(currentTab), tab, edges, direction))
        return true;
    }
    return false;
  }

  @Override
  public boolean canPerform() {
    if (targetXTab != null || targetYTab != null || detachX || detachY)
      return true;
    return false;
  }

  @Override
  public void perform() {
    XTab xTab = targetXTab;
    if (detachX)
      xTab = new XTab();
    YTab yTab = targetYTab;
    if (detachY)
      yTab = new YTab();
    if (xTab != null)
      xDirection.setTab(targetArea, xTab);
    if (yTab != null)
      yDirection.setTab(targetArea, yTab);
  }

  public class Feedback implements IEditOperationFeedback {
    public Area getTargetArea() {
      return targetArea;
    }

    public List<XTab> getXTabCandidates() {
      return xCandidates;
    }
    public List<YTab> getYTabCandidates() {
      return yCandidates;
    }

    public XTab getTargetXTab() {
      return targetXTab;
    }
    public YTab getTargetYTab() {
      return targetYTab;
    }

    public boolean getDetachX() {
      return detachX;
    }
    public boolean getDetachY() {
      return detachY;
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}

