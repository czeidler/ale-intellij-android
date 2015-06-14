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
  Map<XTab, Edge> xTabEdgeMap;
  Map<YTab, Edge> yTabEdgeMap;
  // view / model coordinates
  float modelViewScale = 1;
  // tab width in view coordinates
  float tabWidthView = 8;

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
    if (areaUnder == null)
      return null;
    if (movedArea != null)
      return SwapOperation.swap(this, movedArea, areaUnder);

    return null;
  }

  public IEditOperation detectResizeOperation(Area moveArea, XTab movedXTab, YTab movedYTab, float dragX, float dragY) {
    return new ResizeOperation(this, moveArea, movedXTab, movedYTab, dragX, dragY);
  }

  public boolean isOverTab(Variable tab, float modelCoordinate) {
    double diff = Math.abs(tab.getValue() - modelCoordinate);
    return diff * getModelViewScale() < tabWidthView;
  }

  public Map<XTab, Edge> getXTabEdges() {
    if (xTabEdgeMap == null)
      fillEdges();
    return xTabEdgeMap;
  }

  public Map<YTab, Edge> getYTabEdges() {
    if (yTabEdgeMap == null)
      fillEdges();
    return yTabEdgeMap;
  }

  private void fillEdges() {
    xTabEdgeMap = new HashMap<XTab, Edge>();
    yTabEdgeMap = new HashMap<YTab, Edge>();
    Edge.fillEdges(layoutSpec.getAreas(), xTabEdgeMap, yTabEdgeMap);
  }

  public LayoutSpec getLayoutSpec() {
    return layoutSpec;
  }

  private Area findContentAreaAt(float x, float y, Area veto) {
    for (Area area : layoutSpec.getAreas()) {
      if (area == veto)
        continue;
      if (contentAreaContains(area, x, y))
        return area;
    }
    return null;
  }

  public boolean contentAreaContains(Area area, float x, float y) {
    Area.Rect rect = area.getContentRect();
    if (rect.left > x || rect.right < x)
      return false;
    if (rect.top > y || rect.bottom < y)
      return false;
    return true;
  }
}
