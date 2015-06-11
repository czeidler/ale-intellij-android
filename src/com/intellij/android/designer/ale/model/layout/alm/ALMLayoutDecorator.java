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
import com.intellij.designer.designSurface.DecorationLayer;
import com.intellij.designer.designSurface.StaticDecorator;
import com.intellij.designer.model.RadComponent;

import java.awt.*;


public class ALMLayoutDecorator extends StaticDecorator {
  final RadALMLayout.PaintInfo myPaintInfo;

  public ALMLayoutDecorator(RadComponent container, RadALMLayout.PaintInfo paintInfo) {
    super(container);

    this.myPaintInfo = paintInfo;
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent container) {
    /*if (myPaintInfo.dragRectangle != null) {
      DesignerGraphics graphics = new DesignerGraphics(g, layer);
      graphics.fillRect(myPaintInfo.dragRectangle.x, myPaintInfo.dragRectangle.y, myPaintInfo.dragRectangle.width,
                        myPaintInfo.dragRectangle.height);
    }*/
  }
}
