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
  Candidate targetCandidate;
  boolean detachX = false;
  boolean detachY = false;
  IDirection<XTab, YTab> xDirection;
  IDirection<YTab, XTab> yDirection;

  class Candidate {
    public AlgebraData algebraData;
    public EmptySpace emptySpace;
  }

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
      getResizeCandidateTabs(xCandidates, resizeArea, layoutEditor.getAlgebraData().getXTabEdges(), xDirection);
      targetXTab = resize(xCandidates, x, xDirection, new BottomDirection());
      if (targetXTab == null && !layoutEditor.isOverTab(xDirection.getTab(resizeArea), x) && resizeArea.getRect().contains(x, y)
          && Math.abs(xDirection.getTab(resizeArea).getValue() - x) < layoutEditor.getDetachThresholdModel()) {
        if (detach(xDirection, new BottomDirection()))
          detachX = true;
      }
    }
    if (yDirection != null) {
      getResizeCandidateTabs(yCandidates, resizeArea, layoutEditor.getAlgebraData().getYTabEdges(), yDirection);
      targetYTab = resize(yCandidates, y, yDirection, new RightDirection());
      if (targetYTab == null && !layoutEditor.isOverTab(yDirection.getTab(resizeArea), y) && resizeArea.getRect().contains(x, y)
          && Math.abs(yDirection.getTab(resizeArea).getValue() - y) < layoutEditor.getDetachThresholdModel()) {
        if (detach(yDirection, new RightDirection()))
          detachY = true;
      }
    }
  }

  private Candidate getNewCandidate() {
    if (targetCandidate == null) {
      Candidate candidate = new Candidate();
      candidate.algebraData = layoutEditor.cloneWithReplacedEmptySpaces(layoutEditor.getAlgebraData());
      candidate.emptySpace = TilingAlgebra.makeAreaEmpty(candidate.algebraData, resizeArea);
      return candidate;
    } else {
      Candidate candidate = new Candidate();
      EmptySpace oldSpace = targetCandidate.emptySpace;
      EmptySpace replacement = new EmptySpace(oldSpace.getLeft(), oldSpace.getTop(), oldSpace.getRight(), oldSpace.getBottom());
      candidate.algebraData = layoutEditor.cloneWithReplacedEmptySpaces(targetCandidate.algebraData, oldSpace, replacement);
      candidate.emptySpace = replacement;
      return candidate;
    }
  }

  private <Tab extends Variable> Tab resize(List<Tab> tabs, float position, IDirection direction, IDirection orthDirection) {
    for (Tab tab : tabs) {
      if (layoutEditor.isOverTab(tab, position)) {
        Candidate candidate = getNewCandidate();
        EmptySpace resizeSpace = candidate.emptySpace;
        if (TilingAlgebra.resize(candidate.algebraData, resizeSpace, tab, direction, orthDirection)) {
          targetCandidate = candidate;
          return tab;
        }
      }
    }
    return null;
  }

  private <Tab extends Variable, OrthTab extends Variable>
  boolean detach(IDirection<Tab, OrthTab> direction, IDirection<OrthTab, Tab> orthDirection) {
    Candidate candidate = getNewCandidate();
    AlgebraData data = candidate.algebraData;
    EmptySpace resizeSpace = candidate.emptySpace;

    while (TilingAlgebra.extend(data, resizeSpace, direction));

    Tab newTab = direction.createTab();
    data.removeArea(resizeSpace);
    EmptySpace newEmptySpace = new EmptySpace();
    direction.setTabs(newEmptySpace, direction.getTab(resizeSpace), direction.getOrthogonalTab1(resizeSpace),
                      direction.getOppositeTab(resizeSpace), direction.getOrthogonalTab2(resizeSpace));
    direction.setOppositeTab(newEmptySpace, newTab);
    direction.setTab(resizeSpace, newTab);

    data.addArea(resizeSpace);
    data.addArea(newEmptySpace);

    // still connected to at least one border
    Area dummy = new Area(resizeSpace.getLeft(), resizeSpace.getTop(), resizeSpace.getRight(), resizeSpace.getBottom());
    TilingAlgebra.addAreaAtEmptySpace(data, dummy, resizeSpace);
    List<Area> group = LayoutItemPath.detect(dummy, data.getXTabEdges(), data.getYTabEdges());
    if (!FillGap.isConnectedToBorder1OrBorder2(data, group, direction))
      return false;
    data.removeArea(dummy);
    data.addArea(resizeSpace);

    targetCandidate = candidate;
    return true;
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
    XTab resizedTabX = xDirection.getTab(resizeArea);
    YTab resizedTabY = yDirection.getTab(resizeArea);

    AlgebraData algebraData = layoutEditor.getAlgebraData();
    algebraData.removeArea(resizeArea);
    // replace empty spaces
    while (algebraData.getEmptySpaces().size() > 0)
      algebraData.removeArea(algebraData.getEmptySpaces().get(0));
    for (EmptySpace emptySpace : targetCandidate.algebraData.getEmptySpaces())
      algebraData.addArea(emptySpace);

    EmptySpace target = targetCandidate.emptySpace;
    resizeArea.setTo(target.getLeft(), target.getTop(), target.getRight(), target.getBottom());
    TilingAlgebra.addAreaAtEmptySpace(algebraData, resizeArea, target);

    if (targetXTab != null)
      FillGap.fill(algebraData, resizedTabX, xDirection, xDirection.getOppositeDirection());
    if (targetYTab != null)
      FillGap.fill(algebraData, resizedTabY, yDirection, yDirection.getOppositeDirection());
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

