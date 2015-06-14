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
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.model.RadVisualComponent;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.ale.IEditOperationFeedback;
import nz.ac.auckland.ale.ResizeOperation;
import nz.ac.auckland.ale.SwapOperation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


class FeedbackPainter extends JComponent {
  final LayoutSpecManager myLayoutSpecManager;
  IEditOperation myEditOperation;
  Rectangle myDragRect = new Rectangle();

  public FeedbackPainter(LayoutSpecManager layoutSpecManager) {
    myLayoutSpecManager = layoutSpecManager;
  }

  public void setDragRect(int x, int y, int width, int height) {
    myDragRect.setBounds(x, y, width, height);
  }

  public void setEditOperation(IEditOperation editOperation) {
    myEditOperation = editOperation;
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    DesignerGraphics g = new DesignerGraphics(graphics, this);
    paint(g);
  }

  private void paint(@NotNull DesignerGraphics graphics) {
    graphics.fillRect(myDragRect.x, myDragRect.y, myDragRect.width, myDragRect.height);

    if (myEditOperation == null)
      return;
    IEditOperationFeedback editOperationFeedback = myEditOperation.getFeedback();

    if (editOperationFeedback instanceof SwapOperation.Feedback) {
      SwapOperation.Feedback swapFeedback = (SwapOperation.Feedback)editOperationFeedback;
      RadVisualComponent target = myLayoutSpecManager.getComponentFor(swapFeedback.getTargetArea());

      Rectangle targetRect = target.fromModel(getParent(), target.getBounds());
      graphics.drawRect(targetRect.x, targetRect.y, targetRect.width, targetRect.height);
    } else if (editOperationFeedback instanceof ResizeOperation.Feedback) {
      ResizeOperation.Feedback feedback = (ResizeOperation.Feedback)editOperationFeedback;
    }
  }
}
