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
import com.intellij.designer.designSurface.FeedbackLayer;
import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.alm.Area;

import java.awt.*;


class ALMLayoutOperation extends AbstractEditOperation {
  protected FeedbackPainter myFeedbackPainter;
  protected LayoutSpecManager myLayoutSpecManager;
  protected IEditOperation myEditOperation;

  public ALMLayoutOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context);

    this.myLayoutSpecManager = layoutSpecManager;
  }

  @Override
  public void showFeedback() {
    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
    if (myFeedbackPainter == null) {
      myFeedbackPainter = new FeedbackPainter(myLayoutSpecManager);
      layer.add(myFeedbackPainter);
      myFeedbackPainter.setBounds(0, 0, layer.getWidth(), layer.getHeight());
    }
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
    if (myEditOperation == null && myEditOperation.canPerform()) {
      super.execute();
      return;
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        myEditOperation.perform();
        LayoutSpecXmlWriter xmlWriter = new LayoutSpecXmlWriter(myLayoutSpecManager);
        xmlWriter.write();
        myLayoutSpecManager.invalidate();
      }
    });
  }

  protected Point getModelMousePosition() {
    FeedbackLayer layer = myContext.getArea().getFeedbackLayer();
    RadViewComponent selection = RadViewComponent.getViewComponents(myComponents).get(0);
    return LayoutSpecManager.toModel(layer, myLayoutSpecManager.getALMLayoutSpecs(), selection.getParent(), myContext.getLocation());
  }
}
