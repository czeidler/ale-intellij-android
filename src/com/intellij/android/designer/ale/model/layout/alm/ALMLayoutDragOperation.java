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

import com.intellij.android.designer.designSurface.AbstractEditOperation;
import com.intellij.android.designer.designSurface.graphics.DesignerGraphics;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.designSurface.FeedbackLayer;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.ale.LayoutEditor;
import nz.ac.auckland.alm.Area;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


public class ALMLayoutDragOperation extends AbstractEditOperation {
  private FeedbackPainter myFeedbackPainter;
  private LayoutSpecManager myLayoutSpecManager;
  private IEditOperation myEditOperation;

  public ALMLayoutDragOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context);

    this.myLayoutSpecManager = layoutSpecManager;
  }

  @Override
  public void showFeedback() {
    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
    if (myFeedbackPainter == null) {
      myFeedbackPainter = new FeedbackPainter();
      layer.add(myFeedbackPainter);
      myFeedbackPainter.setBounds(0, 0, layer.getWidth(), layer.getHeight());
    }

    RadViewComponent selection = RadViewComponent.getViewComponents(myComponents).get(0);

    LayoutEditor layoutEditor = new LayoutEditor(myLayoutSpecManager.getLayoutSpec());
    Area moveArea = myLayoutSpecManager.getAreaFor(selection);
    Point modelMouseLoc = selection.toModel(layer, myContext.getLocation());
    Rectangle selectionRect = selection.getBounds();
    Area.Rect dragRect = new Area.Rect(selectionRect.x, selectionRect.y, selectionRect.x + selectionRect.width,
                                       selectionRect.y + selectionRect.height);
    myEditOperation = layoutEditor.detectOperation(moveArea, dragRect, modelMouseLoc.x, modelMouseLoc.y);
    myFeedbackPainter.setEditOperation(myEditOperation);

    final Rectangle dragRectView = selection.fromModel(layer, selectionRect);
    Point moveDelta = myContext.getMoveDelta();
    myFeedbackPainter.setDragRect(dragRectView.x + moveDelta.x, dragRectView.y + moveDelta.y, (int)dragRectView.getWidth(),
                                  (int)dragRectView.getHeight());
    myFeedbackPainter.repaint();
  }

  @Override
  public void eraseFeedback() {
    if (myFeedbackPainter != null) {
      FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
      layer.remove(myFeedbackPainter);
      layer.repaint();
    }
  }

  @Override
  public boolean canExecute() {
    if (myEditOperation != null)
      return true;
    return false;
  }

  @Override
  public void execute() throws Exception {
    if (myEditOperation == null) {
      super.execute();
      return;
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        myEditOperation.perform();
        LayoutSpecXmlWriter xmlWriter = new LayoutSpecXmlWriter(myLayoutSpecManager);
        xmlWriter.write();
      }
    });
  }

}

