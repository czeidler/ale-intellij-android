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


public class LayoutEditor {
  final LayoutSpec layoutSpec;

  public LayoutEditor(LayoutSpec layoutSpec) {
    this.layoutSpec = layoutSpec;
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
  public IEditOperation detectOperation(Area movedArea, Area.Rect dragRect, float dragX, float dragY) {
    Area areaUnder = findContentAreaAt(dragX, dragY, movedArea);
    if (areaUnder == null)
      return null;
    if (movedArea != null)
      return SwapOperation.swap(this, movedArea, areaUnder);

    return null;
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

  private boolean contentAreaContains(Area area, float x, float y) {
    Area.Rect rect = area.getContentRect();
    if (rect.left > x || rect.right < x)
      return false;
    if (rect.top > y || rect.bottom < y)
      return false;
    return true;
  }
}
