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

import java.util.ArrayList;
import java.util.List;


public class EmptyAreaFinder {
  AreaCandidate targetArea = new AreaCandidate();
  AreaCandidate maxArea = new AreaCandidate();
  List<XTab> maxAreaXTabs = new ArrayList<XTab>();
  List<YTab> maxAreaYTabs = new ArrayList<YTab>();

  final LayoutStructure layoutStructure;

  public EmptyAreaFinder(LayoutStructure layoutStructure) {
    this.layoutStructure = layoutStructure;
  }

  public AreaCandidate getTargetArea() {
    return targetArea;
  }

  public AreaCandidate getMaxArea() {
    return maxArea;
  }

  public List<XTab> getMaxAreaXTabs() {
    return maxAreaXTabs;
  }

  public List<YTab> getMaxAreaYTabs() {
    return maxAreaYTabs;
  }

  public boolean find(Area.Rect rect, float x, float y, float snapDistance) {
    // Find max area: first find the min area then maximize it
    maxArea.left = layoutStructure.findTabLeftOf(x);
    maxArea.top = layoutStructure.findTabAbove(y);
    maxArea.right = layoutStructure.findTabRightOf(x);
    maxArea.bottom = layoutStructure.findTabBellow(y);
    if (maxArea.left == null || maxArea.top == null || maxArea.right == null || maxArea.bottom == null)
      return false;
    if (!isEmpty(maxArea))
      return false;
    maxArea = maximizeArea(maxArea, maxAreaXTabs, maxAreaYTabs);

    // place rect in it
    if (Math.abs(maxArea.left.getValue() - rect.left) < snapDistance)
      targetArea.left = maxArea.left;
    if (Math.abs(maxArea.top.getValue() - rect.top) < snapDistance)
      targetArea.top = maxArea.top;
    if (Math.abs(maxArea.right.getValue() - rect.right) < snapDistance)
      targetArea.right = maxArea.right;
    if (Math.abs(maxArea.bottom.getValue() - rect.bottom) < snapDistance)
      targetArea.bottom = maxArea.bottom;

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

    return true;
  }

  private AreaCandidate maximizeArea(AreaCandidate area, List<XTab> containingXTabs, List<YTab> containingYTabs) {
    while (true) {
      double leftDelta = 0;
      double rightDelta = 0;
      double topDelta = 0;
      double bottomDelta = 0;
      AreaCandidate leftCandidate = null;
      AreaCandidate rightCandidate = null;
      AreaCandidate topCandidate = null;
      AreaCandidate bottomCandidate = null;
      if (area.left != layoutStructure.getLayoutSpec().getLeft()) {
        XTab left = layoutStructure.findTabLeftOf(area.left.getValue());
        if (left != null) {
          leftCandidate = new AreaCandidate(area);
          leftCandidate.left = left;
          if (isEmpty(leftCandidate)) leftDelta = Math.abs(left.getValue() - area.left.getValue()) * area.getHeight();
          else leftCandidate = null;
        }
      }
      if (area.right != layoutStructure.getLayoutSpec().getRight()) {
        XTab right = layoutStructure.findTabRightOf(area.right.getValue());
        if (right != null) {
          rightCandidate = new AreaCandidate(area);
          rightCandidate.right = right;
          if (isEmpty(rightCandidate)) rightDelta = Math.abs(right.getValue() - area.right.getValue()) * area.getHeight();
          else rightCandidate = null;
        }
      }
      if (area.top != layoutStructure.getLayoutSpec().getTop()) {
        YTab top = layoutStructure.findTabAbove(area.top.getValue());
        if (top != null) {
          topCandidate = new AreaCandidate(area);
          topCandidate.top = top;
          if (isEmpty(topCandidate)) topDelta = Math.abs(top.getValue() - area.top.getValue()) * area.getWidth();
          else topCandidate = null;
        }
      }
      if (area.bottom != layoutStructure.getLayoutSpec().getBottom()) {
        YTab bottom = layoutStructure.findTabBellow(area.bottom.getValue());
        if (bottom != null) {
          bottomCandidate = new AreaCandidate(area);
          bottomCandidate.bottom = bottom;
          if (isEmpty(bottomCandidate)) bottomDelta = Math.abs(bottom.getValue() - area.bottom.getValue()) * area.getWidth();
          else bottomCandidate = null;
        }
      }

      if (leftCandidate == null && rightCandidate == null && topCandidate == null && bottomCandidate == null) return area;
      if (leftCandidate != null && leftDelta > rightDelta && leftDelta > topDelta && leftDelta > bottomDelta) {
        containingXTabs.add(area.left);
        area = leftCandidate;
      } else if (rightCandidate != null && rightDelta > topDelta && rightDelta > bottomDelta) {
        containingXTabs.add(area.right);
        area = rightCandidate;
      } else if (topCandidate != null && topDelta > bottomDelta) {
        containingYTabs.add(area.top);
        area = topCandidate;
      } else if (bottomCandidate != null) {
        containingYTabs.add(area.bottom);
        area = bottomCandidate;
      }
    }
  }

  private boolean isEmpty(AreaCandidate areaRect) {
    for (Area area : layoutStructure.getAreas()) {
      if (area.getRect().intersects((float)areaRect.left.getValue() + 0.1f, (float)areaRect.top.getValue() + 0.1f,
                     (float)areaRect.right.getValue() - 0.1f, (float)areaRect.bottom.getValue() - 0.1f)) {
        return false;
      }
    }
    return true;
  }
}
