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
import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.algebra.*;
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
      getResizeCandidateTabs(xCandidates, resizeArea, layoutEditor.getLayoutStructure().getXTabEdges(), xDirection);
      targetXTab = getTabAt(xCandidates, x);
      if (targetXTab == null && !layoutEditor.isOverTab(xDirection.getTab(resizeArea), x) && resizeArea.getRect().contains(x, y)
          && Math.abs(xDirection.getTab(resizeArea).getValue() - x) < layoutEditor.getDetachThresholdModel())
        detachX = true;
    }
    if (yDirection != null) {
      getResizeCandidateTabs(yCandidates, resizeArea, layoutEditor.getLayoutStructure().getYTabEdges(), yDirection);
      targetYTab = getTabAt(yCandidates, y);
      if (targetYTab == null && !layoutEditor.isOverTab(yDirection.getTab(resizeArea), y) && resizeArea.getRect().contains(x, y)
          && Math.abs(yDirection.getTab(resizeArea).getValue() - y) < layoutEditor.getDetachThresholdModel())
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
      if (!Edge.isInChain(currentTab, direction.getOppositeTab(area), edges, direction))
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
    LayoutStructure structure = layoutEditor.getLayoutStructure();
    LambdaTransformation trafo = new LambdaTransformation(structure);
    // remove item before editing it
    structure.makeAreaEmpty(resizeArea);

    XTab xTab = targetXTab;
    if (detachX) {
      xTab = new XTab();
      xTab.setValue(xDirection.getTab(resizeArea).getValue());
    }
    YTab yTab = targetYTab;
    if (detachY) {
      yTab = new YTab();
      yTab.setValue(yDirection.getTab(resizeArea).getValue());
    }
    if (xTab != null)
      xDirection.setTab(resizeArea, xTab);
    if (yTab != null)
      yDirection.setTab(resizeArea, yTab);

    EmptySpace space = trafo.makeSpace(resizeArea.getLeft(), resizeArea.getTop(), resizeArea.getRight(), resizeArea.getBottom());
    if (space == null) {
      System.out.println("Failed to make space for: " + resizeArea);
      System.out.println(structure.getAreas());
      System.out.println(structure.getEmptySpaces());
      throw new RuntimeException("algebra error!");
    }

    structure.addAreaAtEmptySpace(resizeArea, space);
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

