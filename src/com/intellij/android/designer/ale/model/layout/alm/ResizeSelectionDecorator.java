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

import java.awt.*;


public class ResizeSelectionDecorator extends com.intellij.designer.designSurface.selection.ResizeSelectionDecorator {
  final DrawingStyle style;
  final IALMLayoutSpecs almLayoutSpecs;

  public ResizeSelectionDecorator(DrawingStyle style, IALMLayoutSpecs almLayoutSpecs) {
    super(Color.RED /* should not be used */, 1 /* should not be used */);
    this.style = style;
    this.almLayoutSpecs = almLayoutSpecs;
  }

  Rectangle transform(DecorationLayer layer, RadComponent layout, Area.Rect rect) {
    Rectangle areaBounds = new Rectangle(Math.round(rect.left), Math.round(rect.top), Math.round(rect.getWidth()),
                                         Math.round(rect.getHeight()));
    Rectangle aleLayoutBounds = new Rectangle((int)almLayoutSpecs.getLeftTab().getValue(), (int)almLayoutSpecs.getTopTab().getValue(),
                                            (int)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue()),
                                            (int)(almLayoutSpecs.getBottomTab().getValue() - almLayoutSpecs.getTopTab().getValue()));
    final Rectangle layoutBounds = layout.fromModel(layer, layout.getBounds());
    aleLayoutBounds = layout.fromModel(layer, aleLayoutBounds);
    areaBounds = layout.fromModel(layer, areaBounds);
    double offsetX = layoutBounds.getX() - aleLayoutBounds.getX();
    double offsetY = layoutBounds.getY() - aleLayoutBounds.getY();
    areaBounds.translate((int)offsetX, (int)offsetY);
    return areaBounds;
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent component) {
    Rectangle bounds = getBounds(layer, component);
    DesignerGraphics.drawRect(style, g, bounds.x, bounds.y, bounds.width, bounds.height);

    if (component instanceof RadViewComponent) {
      Object viewObject = ((RadViewComponent)component).getViewInfo().getViewObject();
      Area area = almLayoutSpecs.getArea(viewObject);
      Area.Rect rect = area.getRect();

      Rectangle areaBounds = transform(layer, component.getParent(), rect);
      DesignerGraphics.drawRect(style, g, areaBounds.x, areaBounds.y, areaBounds.width, areaBounds.height);
    }
  }
}
