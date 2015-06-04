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

  void transform(Area.Rect rect, Rectangle radRoot, DecorationLayer layer) {
    int width = layer.getWidth();
    int height = layer.getHeight();
    double radWidth = radRoot.getWidth();
    double radHeight = radRoot.getHeight();
    double scaleX = radWidth / width;
    double scaleY = radHeight / height;
    rect.left = (float)(radRoot.getX() + scaleX * rect.left);
    rect.top = (float)(radRoot.getY() + scaleY * rect.top);
    rect.right = (float)(radRoot.getX() + scaleX * rect.right);
    rect.bottom = (float)(radRoot.getY() + scaleY * rect.bottom);
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent component) {
    Rectangle bounds = getBounds(layer, component);
    DesignerGraphics.drawRect(style, g, bounds.x, bounds.y, bounds.width, bounds.height);

    if (component instanceof RadViewComponent) {
      Object viewObject = ((RadViewComponent)component).getViewInfo().getViewObject();
      Area area = almLayoutSpecs.getArea(viewObject);
      Area.Rect rect = area.getRect();
      transform(rect, component.getRoot().getBounds(), layer);
      DesignerGraphics.drawRect(style, g, Math.round(rect.left), Math.round(rect.top), Math.round(rect.getWidth()),
                                Math.round(rect.getHeight()));c
    }
  }
}
