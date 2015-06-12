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
import com.intellij.android.designer.designSurface.graphics.DirectionResizePoint;
import com.intellij.android.designer.designSurface.graphics.DrawingStyle;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.designSurface.DecorationLayer;
import com.intellij.designer.model.RadComponent;
import com.intellij.designer.utils.Position;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IALMLayoutSpecs;

import java.awt.*;


public class ResizeSelectionDecorator extends com.intellij.designer.designSurface.selection.ResizeSelectionDecorator {
  final DrawingStyle style;

  public ResizeSelectionDecorator(DrawingStyle style) {
    super(Color.RED /* should not be used */, 1 /* should not be used */);
    this.style = style;

    final String TYPE = "alm_resize";
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.NORTH_WEST, TYPE,
                                      "Change layout:width x layout:height, top x left alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.NORTH, TYPE, "Change layout:height, top alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.NORTH_EAST, TYPE,
                                      "Change layout:width x layout:height, top x right alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.EAST, TYPE, "Change layout:width, right alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.SOUTH_EAST, TYPE,
                                      "Change layout:width x layout:height, bottom x right alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.SOUTH, TYPE, "Change layout:height, bottom alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.SOUTH_WEST, TYPE,
                                      "Change layout:width x layout:height, bottom x left alignment"));
    addPoint(new DirectionResizePoint(DrawingStyle.SELECTION, Position.WEST, TYPE, "Change layout:width, left alignment"));
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent component) {
    Rectangle bounds = getBounds(layer, component);
    DesignerGraphics.drawRect(style, g, bounds.x, bounds.y, bounds.width, bounds.height);

    RadViewComponent container = (RadViewComponent)component.getParent();
    IALMLayoutSpecs almLayoutSpecs = (IALMLayoutSpecs)container.getViewInfo().getViewObject();

    if (component instanceof RadViewComponent) {
      Object viewObject = ((RadViewComponent)component).getViewInfo().getViewObject();
      Area area = almLayoutSpecs.getArea(viewObject);
      Area.Rect rect = area.getRect();

      Rectangle areaBounds = LayoutSpecManager.fromModel(layer, almLayoutSpecs, component.getParent(), rect);
      DesignerGraphics.drawRect(style, g, areaBounds.x, areaBounds.y, areaBounds.width, areaBounds.height);
    }
  }
}
