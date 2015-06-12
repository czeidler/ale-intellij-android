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
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.ale.IEditOperationFeedback;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


class FeedbackPainter extends JComponent {
  IEditOperation myEditOperation;
  Rectangle dragRect = new Rectangle();

  public FeedbackPainter() {
  }

  public void setDragRect(int x, int y, int width, int height) {
    dragRect.setBounds(x, y, width, height);
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
    graphics.fillRect(dragRect.x, dragRect.y, dragRect.width, dragRect.height);

    if (myEditOperation == null)
      return;
    IEditOperationFeedback editOperationFeedback = myEditOperation.getFeedback();
    
  }
}
