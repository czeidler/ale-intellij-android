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

import com.intellij.designer.designSurface.DecorationLayer;
import com.intellij.designer.designSurface.StaticDecorator;
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.LayoutSpec;

import java.awt.*;


public class ALMLayoutDecorator extends StaticDecorator {
  final RadALMLayout myRadALMLayout;

  public ALMLayoutDecorator(RadComponent container, RadALMLayout almLayout) {
    super(container);

    this.myRadALMLayout = almLayout;
  }

  @Override
  protected void paint(DecorationLayer layer, Graphics2D g, RadComponent container) {
    if (!layer.showSelection() ) {
      return;
    }

    LayoutSpecManager manager = myRadALMLayout.getLayoutSpecManager();
    LayoutSpec layoutSpec = manager.getLayoutSpec();
    for (IArea area : layoutSpec.getAreas()) {
      if (!(area instanceof EmptySpace))
        continue;

      Area.Rect rect = new Area.Rect((float)area.getLeft().getValue(), (float)area.getTop().getValue(), (float)area.getRight().getValue(),
                                     (float)area.getBottom().getValue());

      Rectangle areaBounds = LayoutSpecManager.fromModel(layer, manager.getALMLayoutSpecs(), container, rect);

      inset(areaBounds, 2);
      g.setColor(new Color(220, 220, 90));
      g.fillRect(areaBounds.x, areaBounds.y, areaBounds.width, areaBounds.height);
    }
  }

  private void inset(Rectangle rectangle, int inset) {
    if (rectangle.width >= 2 * inset) {
      rectangle.x += inset;
      rectangle.width -= 2 * inset;
    }

    if (rectangle.height >= 2 * inset) {
      rectangle.y += inset;
      rectangle.height -= 2 * inset;
    }
  }
}
