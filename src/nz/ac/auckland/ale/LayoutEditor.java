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
import nz.ac.auckland.alm.algebra.AlgebraData;
import nz.ac.auckland.alm.algebra.EmptyAreaCleaner;
import nz.ac.auckland.linsolve.Variable;


public class LayoutEditor {
  final LayoutSpec layoutSpec;
  private Area createdArea;
  private AlgebraData algebraData;
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

  public Area getCreatedArea() {
    return createdArea;
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

    EmptyAreaCleaner cleaner = new EmptyAreaCleaner(algebraData);
    cleaner.clean();
    algebraData.applyToLayoutSpec(layoutSpec);
    algebraData = null;
  }

  private Area ensureSourceArea(Area area) {
    if (area != null)
      return area;

    XTab left = new XTab();
    left.setValue(-10);
    YTab top = new YTab();
    top.setValue(-10);
    XTab right = new XTab();
    right.setValue(-5);
    YTab bottom = new YTab();
    bottom.setValue(-5);
    createdArea = new Area(left, top, right, bottom);
    return createdArea;
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
    Area sourceArea = ensureSourceArea(movedArea);

    Area mouseOver = findAreaAt(dragX, dragY);
    if (mouseOver != null) {
      // insert between
      currentEditOperation = new MoveBetweenOperation(this, sourceArea, mouseOver, dragX, dragY);
      if (currentEditOperation.canPerform())
        return currentEditOperation;
      // swap
      mouseOver = findContentAreaAt(dragX, dragY);
      if (mouseOver != null) {
        currentEditOperation = new SwapOperation(this, sourceArea, mouseOver);
        if (currentEditOperation.canPerform()) return currentEditOperation;
      }
    }

    currentEditOperation = new MoveOperation(this, sourceArea, dragRect, dragX, dragY);

    return currentEditOperation;
  }

  public IEditOperation detectResizeOperation(Area moveArea, XTab movedXTab, YTab movedYTab, float dragX, float dragY) {
    currentEditOperation = new ResizeOperation(this, moveArea, movedXTab, movedYTab, dragX, dragY);
    return currentEditOperation;
  }

  public IEditOperation getDeleteOperation(Area area) {
    return new RemoveOperation(this, area);
  }

  public boolean isOverTab(Variable tab, float modelCoordinate) {
    double diff = Math.abs(tab.getValue() - modelCoordinate);
    return diff * getModelViewScale() < tabWidthView;
  }

  public AlgebraData getAlgebraData() {
    if (algebraData == null)
      algebraData = new AlgebraData(layoutSpec, null);
    return algebraData;
  }

  public Area findContentAreaAt(float x, float y) {
    for (Area area : getAlgebraData().getAreas()) {
      if (area.getContentRect().contains(x, y))
        return area;
    }
    return null;
  }

  public Area findAreaAt(float x, float y) {
    for (Area area : getAlgebraData().getAreas()) {
      if (area.getRect().contains(x, y))
        return area;
    }
    return null;
  }

  static public AlgebraData cloneWithReplacedEmptySpaces(AlgebraData algebraData) {
    return cloneWithReplacedEmptySpaces(algebraData, null, null);
  }

  static public AlgebraData cloneWithReplacedEmptySpaces(AlgebraData layoutStructure, EmptySpace old, EmptySpace replacement) {
    assert old == null || old.getLeft() == replacement.getLeft();
    assert old == null || old.getTop() == replacement.getTop();
    assert old == null || old.getRight() == replacement.getRight();
    assert old == null || old.getBottom() == replacement.getBottom();

    AlgebraData clone = new AlgebraData(layoutStructure.getLeft(), layoutStructure.getTop(), layoutStructure.getRight(),
                                        layoutStructure.getBottom());
    for (Area area : layoutStructure.getAreas())
      clone.addArea(area);

    for (EmptySpace emptySpace : layoutStructure.getEmptySpaces()) {
      EmptySpace emptySpaceClone;
      if (old != emptySpace) {
        emptySpaceClone = new EmptySpace(emptySpace.getLeft(), emptySpace.getTop(), emptySpace.getRight(), emptySpace.getBottom());
      } else
        emptySpaceClone = replacement;
      clone.addArea(emptySpaceClone);
    }
    return clone;
  }
}
