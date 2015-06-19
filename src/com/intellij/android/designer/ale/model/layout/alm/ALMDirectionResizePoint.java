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
package com.intellij.android.designer.ale.model.layout.alm;

import com.intellij.android.designer.designSurface.graphics.DesignerGraphics;
import com.intellij.android.designer.designSurface.graphics.DrawingStyle;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.designSurface.DecorationLayer;
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IALMLayoutSpecs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;


public class ALMDirectionResizePoint  extends com.intellij.designer.designSurface.selection.DirectionResizePoint {
  private final DrawingStyle myStyle;

  public ALMDirectionResizePoint(DrawingStyle style, int direction, Object type, @Nullable String description) {
    //noinspection UseJBColor
    super(Color.RED /* should not be used */, Color.RED /* should not be used */, direction, type, description);
    myStyle = style;
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent component) {
    Point location = getLocation(layer, component);
    int size = getSize();
    DesignerGraphics.drawStrokeFilledRect(myStyle, g, location.x, location.y, size, size);
  }

  @Override
  protected int getSize() {
    return 7;
  }

  @Override
  protected int getNeighborhoodSize() {
    return 2;
  }

  @Override
  protected Point getLocation(DecorationLayer layer, RadComponent component) {
    Point location = super.getLocation(layer, component);
    if (myXSeparator == 0) {
      location.x++;
    }
    if (myYSeparator == 0) {
      location.y++;
    }

    return location;
  }

  @Override
  protected Rectangle getBounds(DecorationLayer layer, RadComponent component) {
    if (!(component instanceof RadViewComponent))
      return super.getBounds(layer, component);

    IALMLayoutSpecs almLayoutSpecs = LayoutSpecManager.getLayoutSpec((RadViewComponent)component);
    Area area = LayoutSpecManager.getArea((RadViewComponent)component);
    if (area == null) {
      // not there yet wait and return an empty rect
      return new Rectangle();
    }
    Area.Rect rect = area.getRect();

    return LayoutSpecManager.fromModel(layer, almLayoutSpecs, component.getParent(), rect);
  }
}
