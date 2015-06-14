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

import java.util.HashMap;
import java.util.Map;


public class LayoutEditor {
  final LayoutSpec layoutSpec;
  final Map<XTab, Edge> xTabEdgeMap = new HashMap<XTab, Edge>();
  final Map<YTab, Edge> yTabEdgeMap = new HashMap<YTab, Edge>();
  boolean edgesValid = false;
  IEditOperation currentEditOperation;
  // view / model coordinates
  float modelViewScale = 1;
  // tab width in view coordinates
  float tabWidthView = 8;
  float detachThresholdView = 80;

  public LayoutEditor(LayoutSpec layoutSpec) {
    this.layoutSpec = layoutSpec;
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

  public boolean canPerform() {
    if (currentEditOperation != null && currentEditOperation.canPerform())
      return true;
    return false;
  }

  public void perform() {
    currentEditOperation.perform();
    edgesValid = false;
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
    Area areaUnder = findContentAreaAt(dragX, dragY, movedArea);
    if (areaUnder == null) {
      currentEditOperation = null;
      return null;
    }
    if (movedArea != null) {
      currentEditOperation = SwapOperation.swap(this, movedArea, areaUnder);
      return currentEditOperation;
    }

    return null;
  }

  public IEditOperation detectResizeOperation(Area moveArea, XTab movedXTab, YTab movedYTab, float dragX, float dragY) {
    currentEditOperation = new ResizeOperation(this, moveArea, movedXTab, movedYTab, dragX, dragY);
    return currentEditOperation;
  }

  public boolean isOverTab(Variable tab, float modelCoordinate) {
    double diff = Math.abs(tab.getValue() - modelCoordinate);
    return diff * getModelViewScale() < tabWidthView;
  }

  public Map<XTab, Edge> getXTabEdges() {
    if (!edgesValid) {
      fillEdges();
      edgesValid = true;
    }
    return xTabEdgeMap;
  }

  public Map<YTab, Edge> getYTabEdges() {
    if (!edgesValid) {
      fillEdges();
      edgesValid = true;
    }
    return yTabEdgeMap;
  }

  private void fillEdges() {
    xTabEdgeMap.clear();
    yTabEdgeMap.clear();
    Edge.fillEdges(layoutSpec, xTabEdgeMap, yTabEdgeMap);
  }

  public LayoutSpec getLayoutSpec() {
    return layoutSpec;
  }

  private Area findContentAreaAt(float x, float y, Area veto) {
    for (Area area : layoutSpec.getAreas()) {
      if (area == veto)
        continue;
      if (area.getContentRect().contains(x, y))
        return area;
    }
    return null;
  }
}
