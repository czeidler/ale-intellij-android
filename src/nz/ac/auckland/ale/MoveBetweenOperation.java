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
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;


public class MoveBetweenOperation extends AbstractEditOperation {
  final private Area movedArea;
  final private Area targetArea;
  private IDirection direction;
  private float orthInsertPosition;

  public MoveBetweenOperation(LayoutEditor layoutEditor, Area movedArea, Area mouseOverArea, float x, float y) {
    super(layoutEditor);
    this.movedArea = movedArea;
    this.targetArea = mouseOverArea;

    LayoutStructure layoutStructure = layoutEditor.getLayoutStructure();
    EmptySpace space = layoutStructure.makeAreaEmpty(movedArea);

    if (layoutEditor.isOverTab(targetArea.getLeft(), x)) {
      direction = new LeftDirection();
      orthInsertPosition = y;
    } else if (layoutEditor.isOverTab(targetArea.getRight(), x)) {
      direction = new RightDirection();
      orthInsertPosition = y;
    } else if (layoutEditor.isOverTab(targetArea.getTop(), y)) {
      direction = new TopDirection();
      orthInsertPosition = x;
    } else if (layoutEditor.isOverTab(targetArea.getBottom(), y)) {
      direction = new BottomDirection();
      orthInsertPosition = x;
    }

    layoutStructure.addAreaAtEmptySpace(movedArea, space);
  }

  @Override
  public boolean canPerform() {
    return direction != null;
  }

  @Override
  public void perform() {
    LayoutStructure structure = layoutEditor.getLayoutStructure();
    // remove items before editing them
    structure.makeAreaEmpty(movedArea);
    structure.removeArea(targetArea);

    Variable tab = direction.getTab(targetArea);
    Variable tabOrth1 = direction.getOrthogonalTab1(targetArea);
    Variable tabOrth2 = direction.getOrthogonalTab2(targetArea);
    Variable newTab = direction.createTab();
    EmptySpace newSpace = null;
    float targetExtent = (float)(tabOrth2.getValue() - tabOrth1.getValue());
    float movePrefExtent = (float)direction.getExtent(movedArea.getPreferredSize());
    // newly created uninitialized components may have a movePrefExtent = -2; ignore them
    if (movePrefExtent > 0 && movePrefExtent < targetExtent * 0.7) {
      newSpace = new EmptySpace();
      direction.setTabs(newSpace, tab, tabOrth1, newTab, tabOrth2);
      if (Math.abs(tabOrth1.getValue() - orthInsertPosition) < Math.abs(tabOrth2.getValue() - orthInsertPosition)) {
        tabOrth2 = direction.createOrthogonalTab();
        direction.setOrthogonalTab1(newSpace, tabOrth2);
      } else {
        tabOrth1 = direction.createOrthogonalTab();
        direction.setOrthogonalTab2(newSpace, tabOrth1);
      }
    }

    direction.setTabs(movedArea, tab, tabOrth1, newTab, tabOrth2);
    direction.setTab(targetArea, newTab);

    structure.addArea(movedArea);
    structure.addArea(targetArea);
    if (newSpace != null)
      structure.addArea(newSpace);
  }

  public class Feedback implements IEditOperationFeedback {
    public Area getTargetArea() {
      return targetArea;
    }

    public IDirection getInsertDirection() {
      return direction;
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
