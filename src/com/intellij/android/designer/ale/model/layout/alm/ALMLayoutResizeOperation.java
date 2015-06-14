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

import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.designer.utils.Position;
import nz.ac.auckland.ale.LayoutEditor;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;

import java.awt.*;


public class ALMLayoutResizeOperation extends ALMLayoutOperation {
  public static final String TYPE = "alm_resize";

  public ALMLayoutResizeOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context, layoutSpecManager);
  }

  @Override
  public void showFeedback() {
    super.showFeedback();

    RadViewComponent selection = RadViewComponent.getViewComponents(myComponents).get(0);
    Area moveArea = myLayoutSpecManager.getAreaFor(selection);

    int direction = myContext.getResizeDirection();
    XTab movedXTab = null;
    YTab movedYTab = null;
    if ((direction & Position.WEST) != 0)
      movedXTab = moveArea.getLeft();
    else if ((direction & Position.EAST) != 0)
      movedXTab = moveArea.getRight();
    if ((direction & Position.NORTH) != 0)
      movedYTab = moveArea.getTop();
    else if ((direction & Position.SOUTH) != 0)
      movedYTab = moveArea.getBottom();
    LayoutEditor layoutEditor = myLayoutSpecManager.getLayoutEditor();
    Point modelMouseLocation = getModelMousePosition();
    myEditOperation = layoutEditor.detectResizeOperation(moveArea, movedXTab, movedYTab, modelMouseLocation.x, modelMouseLocation.y);

    myFeedbackPainter.repaint();
  }
}
