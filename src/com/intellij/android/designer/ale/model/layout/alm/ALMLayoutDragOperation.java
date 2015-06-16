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
import com.intellij.designer.designSurface.FeedbackLayer;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.ale.LayoutEditor;
import nz.ac.auckland.alm.Area;

import java.awt.*;


public class ALMLayoutDragOperation extends ALMLayoutOperation {
  public ALMLayoutDragOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context, layoutSpecManager);
  }

  @Override
  public void showFeedback() {
    super.showFeedback();

    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();

    RadViewComponent selection = RadViewComponent.getViewComponents(myComponents).get(0);
    Area moveArea = myLayoutSpecManager.getAreaFor(selection);
    Rectangle selectionRect = selection.getBounds();
    Point modelMouseLocation = getModelMousePosition();
    if (moveArea == null) {
      LayoutEditor layoutEditor = myLayoutSpecManager.getLayoutEditor();
      int width = (int)(50 / layoutEditor.getModelViewScale());
      int height = (int)(40 / layoutEditor.getModelViewScale());
      selectionRect = new Rectangle(modelMouseLocation.x - width / 2, modelMouseLocation.y - height / 2, width, height);
    }
    Area.Rect dragRect = new Area.Rect(selectionRect.x, selectionRect.y, selectionRect.x + selectionRect.width,
                                       selectionRect.y + selectionRect.height);
    LayoutEditor layoutEditor = myLayoutSpecManager.getLayoutEditor();
    IEditOperation editOperation = layoutEditor.detectDragOperation(moveArea, dragRect, modelMouseLocation.x, modelMouseLocation.y);
    myFeedbackPainter.setEditOperation(editOperation);

    final Rectangle dragRectView = selection.fromModel(layer, selectionRect);
    Point moveDelta = myContext.getMoveDelta();
    if (moveDelta == null)
      moveDelta = new Point(0, 0);

    myFeedbackPainter.setDragRect(dragRectView.x + moveDelta.x, dragRectView.y + moveDelta.y, (int)dragRectView.getWidth(),
                                    (int)dragRectView.getHeight());

    myFeedbackPainter.repaint();
  }
}

