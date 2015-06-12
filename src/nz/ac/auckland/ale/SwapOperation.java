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

public class SwapOperation extends AbstractEditOperation {
  Area draggedArea;
  Area targetArea;

  static public SwapOperation swap(LayoutEditor layoutEditor, Area draggedArea, Area area) {
    if (draggedArea == area)
      return null;
    return new SwapOperation(layoutEditor, draggedArea, area);
  }

  private SwapOperation(LayoutEditor layoutEditor, Area draggedArea, Area area) {
    super(layoutEditor);

    this.draggedArea = draggedArea;
    this.targetArea = area;
  }

  @Override
  public void perform() {
    XTab left = draggedArea.getLeft();
    YTab top = draggedArea.getTop();
    XTab right = draggedArea.getRight();
    YTab bottom = draggedArea.getBottom();

    draggedArea.setTo(targetArea.getLeft(), targetArea.getTop(), targetArea.getRight(), targetArea.getBottom());
    targetArea.setTo(left, top, right, bottom);
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new SwapOperationFeedback(targetArea);
  }
}
