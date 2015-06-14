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
  final Area resizeArea;
  final List<XTab> xCandidates = new ArrayList<XTab>();
  final List<YTab> yCandidates = new ArrayList<YTab>();
  XTab targetXTab;
  YTab targetYTab;
  boolean detachX = false;
  boolean detachY = false;
  IDirection xDirection;
  IDirection yDirection;

  public ResizeOperation(LayoutEditor layoutEditor, Area resizeArea, XTab xTab, YTab yTab, float x, float y) {
    super(layoutEditor);

    this.resizeArea = resizeArea;
    if (xTab != null) {
      if (resizeArea.getLeft() == xTab)
        xDirection = new LeftDirection();
      else if (resizeArea.getRight() == xTab)
        xDirection = new RightDirection();
      assert xDirection != null;
    }
    if (yTab != null) {
      if (resizeArea.getTop() == yTab)
        yDirection = new TopDirection();
      else if (resizeArea.getBottom() == yTab)
        yDirection = new BottomDirection();
      assert yDirection != null;
    }
    if (xDirection != null) {
      getResizeCandidateTabs(xCandidates, resizeArea, layoutEditor.getXTabEdges(), xDirection);
      targetXTab = getTabAt(xCandidates, x);
      if (targetXTab == null && layoutEditor.contentAreaContains(resizeArea, x, y))
        detachX = true;
    }
    if (yDirection != null) {
      getResizeCandidateTabs(yCandidates, resizeArea, layoutEditor.getYTabEdges(), yDirection);
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

  private <Tab extends Variable> void getResizeCandidateTabs(List<Tab> candidates, Area area, Map<Tab, Edge> edges, IDirection direction) {
    candidates.clear();
    for (Map.Entry<Tab, Edge> entry : edges.entrySet()) {
      Tab currentTab = entry.getKey();
      if (currentTab == direction.getTab(area) || currentTab == direction.getOppositeTab(area))
        continue;
      if (!Edge.isInChain(edges.get(currentTab), direction.getOppositeTab(area), edges, direction))
        candidates.add(currentTab);
    }
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
      xDirection.setTab(resizeArea, xTab);
    if (yTab != null)
      yDirection.setTab(resizeArea, yTab);
  }

  public class Feedback implements IEditOperationFeedback {
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

