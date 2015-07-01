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
import nz.ac.auckland.alm.algebra.LambdaTransformation;
import nz.ac.auckland.alm.algebra.LayoutStructure;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;

public class MoveOperation extends AbstractEditOperation {
  final private Area movedArea;
  AreaCandidate targetArea;
  EmptyAreaFinder emptyAreaFinder;

  public MoveOperation(LayoutEditor layoutEditor, Area movedArea, Area.Rect dragRect, float dragX, float dragY) {
    super(layoutEditor);

    this.movedArea = movedArea;

    LayoutStructure layoutStructure = layoutEditor.getLayoutStructure();
    EmptySpace space = layoutStructure.makeAreaEmpty(movedArea);

    emptyAreaFinder = new EmptyAreaFinder(layoutEditor.getLayoutStructure());
    if (!emptyAreaFinder.find(dragX, dragY)) {
      emptyAreaFinder = null;
      return;
    }
    findTargetArea(dragRect, layoutEditor.getSnapModel());

    layoutStructure.addAreaAtEmptySpace(movedArea, space);
  }

  private void findTargetArea(Area.Rect rect, float snapDistance) {
    AreaCandidate maxArea = emptyAreaFinder.getMaxArea();
    targetArea = new AreaCandidate();

    // place rect in it
    targetArea.left = getClosestSnapTab(maxArea.left, maxArea.right, emptyAreaFinder.getMaxAreaXTabs(), rect.left, snapDistance);
    targetArea.right = getClosestSnapTab(maxArea.left, maxArea.right, emptyAreaFinder.getMaxAreaXTabs(), rect.right, snapDistance);
    targetArea.top = getClosestSnapTab(maxArea.top, maxArea.bottom, emptyAreaFinder.getMaxAreaYTabs(), rect.top, snapDistance);
    targetArea.bottom = getClosestSnapTab(maxArea.top, maxArea.bottom, emptyAreaFinder.getMaxAreaYTabs(), rect.bottom, snapDistance);

    if (targetArea.left == null && targetArea.right == null) {
      targetArea.left = maxArea.left;
      targetArea.right = maxArea.right;
    } else if (targetArea.left == null) {
      targetArea.left = new XTab();
      targetArea.left.setValue(targetArea.right.getValue() - rect.getWidth());
    } else if (targetArea.right == null) {
      targetArea.right = new XTab();
      targetArea.right.setValue(targetArea.left.getValue() + rect.getWidth());
    }

    if (targetArea.top == null && targetArea.bottom == null) {
      targetArea.top = maxArea.top;
      targetArea.bottom = maxArea.bottom;
    } else if (targetArea.top == null) {
      targetArea.top = new YTab();
      targetArea.top.setValue(targetArea.bottom.getValue() - rect.getHeight());
    } else if (targetArea.bottom == null) {
      targetArea.bottom = new YTab();
      targetArea.bottom.setValue(targetArea.top.getValue() + rect.getHeight());
    }
  }

  private <Tab extends Variable> Tab getClosestSnapTab(Tab border1, Tab border2, List<Tab> tabs, float value, float snapDistance) {
    List<Tab> allTabs = new ArrayList(tabs);
    allTabs.add(border1);
    allTabs.add(border2);

    Tab closest = null;
    float minDistance = Float.MAX_VALUE;
    for (Tab tab : allTabs) {
      float distance = (float)Math.abs(tab.getValue() - value);
      if (distance < snapDistance && distance < minDistance) {
        minDistance = distance;
        closest = tab;
      }
    }
    return closest;
  }

  @Override
  public boolean canPerform() {
    return emptyAreaFinder != null;
  }

  @Override
  public void perform() {
    LayoutStructure structure = layoutEditor.getLayoutStructure();
    LambdaTransformation trafo = new LambdaTransformation(structure);
    // remove item before editing it
    structure.makeAreaEmpty(movedArea);

    EmptySpace space = trafo.makeSpace(targetArea.left, targetArea.top, targetArea.right, targetArea.bottom);
    if (space == null)
      throw new RuntimeException("algebra error!");

    structure.addAreaAtEmptySpace(movedArea, space);
  }

  public class Feedback implements IEditOperationFeedback {
    public AreaCandidate getTargetArea() {
      return targetArea;
    }
    public AreaCandidate getMaxArea() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxArea();
    }
    public List<XTab> getMaxAreaXTabs() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxAreaXTabs();
    }

    public List<YTab> getMaxAreaYTabs() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxAreaYTabs();
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
