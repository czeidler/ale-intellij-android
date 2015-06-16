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
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;


public class LayoutEditor {
  final LayoutSpec layoutSpec;
  private Area removedArea;
  private Area addedArea;
  private LayoutStructure layoutStructure;
  IEditOperation currentEditOperation;
  // view / model coordinates
  float modelViewScale = 1;
  // tab width in view coordinates
  float tabWidthView = 8;
  float detachThresholdView = 80;
  float snapView = 20;

  public LayoutEditor(LayoutSpec layoutSpec) {
    this.layoutSpec = layoutSpec;
  }

  public void setRemovedArea(Area removedArea) {
    this.removedArea = removedArea;
  }

  public Area getAddedArea() {
    return addedArea;
  }

  public void setModelViewScale(float modelViewScale) {
    this.modelViewScale = modelViewScale;
  }

  public float getModelViewScale() {
    return modelViewScale;
  }

  public void setTabWidthView(float tabWidthView) {
    this.tabWidthView = tabWidthView;
  }

  public void setDetachThresholdView(float detachThresholdView) {
    this.detachThresholdView = detachThresholdView;
  }

  public float getDetachThresholdModel() {
    return detachThresholdView * getModelViewScale();
  }

  public void setSnapView(float snapView) {
    this.snapView = snapView;
  }

  public float getSnapModel() {
    return snapView * getModelViewScale();
  }

  public boolean canPerform() {
    if (currentEditOperation != null && currentEditOperation.canPerform())
      return true;
    return false;
  }

  public void perform() {
    currentEditOperation.perform();
    layoutStructure = null;
  }

  /**
   * Find edit operation.
   *
   * @param movedArea the area to be moved, can be null
   * @param dragRect the target area of the (dragged) area
   * @param dragX the drag x position
   * @param dragY the drag y position
   * @return null if no suitable operation has been found
   */
  public IEditOperation detectDragOperation(Area movedArea, Area.Rect dragRect, float dragX, float dragY) {
    // swap
    currentEditOperation = new SwapOperation(this, movedArea, dragX, dragY);
    if (currentEditOperation.canPerform())
      return currentEditOperation;

    Area sourceArea = movedArea;
    if (movedArea == null) {
      addedArea = layoutSpec.addArea(layoutSpec.getLeft(), layoutSpec.getTop(), new XTab(), new YTab());
      sourceArea = addedArea;
    }

    currentEditOperation = new MoveBetweenOperation(this, sourceArea, dragX, dragY);
    if (!currentEditOperation.canPerform())
      currentEditOperation = new MoveOperation(this, sourceArea, dragRect, dragX, dragY);

    if (!currentEditOperation.canPerform() && addedArea != null) {
      addedArea.remove();
      addedArea = null;
    }
    return currentEditOperation;
  }

  public IEditOperation detectResizeOperation(Area moveArea, XTab movedXTab, YTab movedYTab, float dragX, float dragY) {
    currentEditOperation = new ResizeOperation(this, moveArea, movedXTab, movedYTab, dragX, dragY);
    return currentEditOperation;
  }

  public boolean isOverTab(Variable tab, float modelCoordinate) {
    double diff = Math.abs(tab.getValue() - modelCoordinate);
    return diff * getModelViewScale() < tabWidthView;
  }

  public LayoutStructure getLayoutStructure() {
    if (layoutStructure == null)
      layoutStructure = new LayoutStructure(layoutSpec, removedArea);
    return layoutStructure;
  }

  public LayoutSpec getLayoutSpec() {
    return layoutSpec;
  }

  public Area findContentAreaAt(float x, float y, Area veto) {
    for (Area area : layoutSpec.getAreas()) {
      if (area == veto)
        continue;
      if (area.getContentRect().contains(x, y))
        return area;
    }
    return null;
  }
}
