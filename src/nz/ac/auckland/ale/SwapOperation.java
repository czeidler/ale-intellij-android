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
import nz.ac.auckland.alm.algebra.AlgebraData;

public class SwapOperation extends AbstractEditOperation {
  final Area draggedArea;
  Area targetArea;

  public SwapOperation(LayoutEditor layoutEditor, Area draggedArea, Area mouseOver) {
    super(layoutEditor);
    this.draggedArea = draggedArea;

    if (draggedArea != mouseOver)
      targetArea = mouseOver;
  }

  @Override
  public boolean canPerform() {
    return targetArea != null;
  }

  @Override
  public void perform() {
    // update the layout structure
    AlgebraData layoutStructure = layoutEditor.getAlgebraData();
    // remove items before editing them
    layoutStructure.removeArea(draggedArea);
    layoutStructure.removeArea(targetArea);

    XTab left = draggedArea.getLeft();
    YTab top = draggedArea.getTop();
    XTab right = draggedArea.getRight();
    YTab bottom = draggedArea.getBottom();

    draggedArea.setTo(targetArea.getLeft(), targetArea.getTop(), targetArea.getRight(), targetArea.getBottom());
    targetArea.setTo(left, top, right, bottom);

    layoutStructure.addArea(draggedArea);
    layoutStructure.addArea(targetArea);
  }

  public class Feedback implements IEditOperationFeedback {
    public Area getTargetArea() {
      return targetArea;
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
