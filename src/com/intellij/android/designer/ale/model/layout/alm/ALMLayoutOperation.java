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
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.android.designer.model.layout.relative.MultiLineTooltipManager;
import com.intellij.designer.designSurface.FeedbackLayer;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;

import java.awt.*;


class ALMLayoutOperation extends AbstractEditOperation {
  protected FeedbackPainter myFeedbackPainter;
  private MultiLineTooltipManager myTooltip;
  protected LayoutSpecManager myLayoutSpecManager;

  public ALMLayoutOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context);

    this.myLayoutSpecManager = layoutSpecManager;
  }

  @Override
  public void showFeedback() {
    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
    if (myFeedbackPainter == null) {
      myTooltip = new MultiLineTooltipManager(layer, 1);
      myFeedbackPainter = new FeedbackPainter(myLayoutSpecManager, myTooltip);
      layer.add(myFeedbackPainter);
      myFeedbackPainter.setBounds(0, 0, layer.getWidth(), layer.getHeight());
    }

    // Position the tooltip
    Point location = myContext.getLocation();
    myTooltip.update((RadViewComponent) myContainer, location);
  }

  @Override
  public void eraseFeedback() {
    if (myFeedbackPainter != null) {
      FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
      layer.remove(myFeedbackPainter);
      layer.repaint();

      myTooltip.dispose();
      myTooltip = null;
    }
  }

  @Override
  public boolean canExecute() {
    return myLayoutSpecManager.myLayoutEditor.canPerform();
  }

  @Override
  public void execute() throws Exception {
    if (!myLayoutSpecManager.myLayoutEditor.canPerform())
      return;

    // super.execute creates, pastes or adds the item
    if (myContext.isCreate() || myContext.isPaste() || myContext.isAdd()) {
      super.execute();
      RadComponent insertComponent = myContainer.getChildren().get(myContainer.getChildren().size() - 1);
      myLayoutSpecManager.addComponent(myLayoutSpecManager.myLayoutEditor.getCreatedArea(), insertComponent);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        myLayoutSpecManager.myLayoutEditor.perform();
        LayoutSpecXmlWriter xmlWriter = new LayoutSpecXmlWriter(myLayoutSpecManager);
        xmlWriter.write();
        myLayoutSpecManager.invalidate();
      }
    });
  }

  protected Point getModelMousePosition() {
    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
    return LayoutSpecManager.toModel(layer, myLayoutSpecManager.getALMLayoutSpecs(), myContainer, myContext.getLocation());
  }
}
