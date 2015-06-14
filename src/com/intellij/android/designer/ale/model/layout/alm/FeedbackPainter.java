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

import com.intellij.android.designer.designSurface.feedbacks.TextFeedback;
import com.intellij.android.designer.designSurface.graphics.DesignerGraphics;
import com.intellij.android.designer.designSurface.graphics.DrawingStyle;
import com.intellij.android.designer.model.layout.relative.MultiLineTooltipManager;
import com.intellij.designer.model.RadVisualComponent;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.ale.IEditOperationFeedback;
import nz.ac.auckland.ale.ResizeOperation;
import nz.ac.auckland.ale.SwapOperation;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


class FeedbackPainter extends JComponent {
  final private LayoutSpecManager myLayoutSpecManager;
  private IEditOperation myEditOperation;
  private Rectangle myDragRect = new Rectangle();
  private MultiLineTooltipManager myTooltip;

  public FeedbackPainter(LayoutSpecManager layoutSpecManager, MultiLineTooltipManager tooltip) {
    myLayoutSpecManager = layoutSpecManager;
    myTooltip = tooltip;
  }

  public void setDragRect(int x, int y, int width, int height) {
    myDragRect.setBounds(x, y, width, height);
  }

  public void setEditOperation(IEditOperation editOperation) {
    myEditOperation = editOperation;
  }

  private void setToolTipText(String text, int line) {
    if (text == null) {
      myTooltip.setVisible(line, false);
      return;
    }

    myTooltip.setVisible(line, true);
    TextFeedback feedback = myTooltip.getFeedback(line);
    feedback.clear();
    feedback.append(text);
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
    } else if (editOperationFeedback instanceof ResizeOperation.Feedback)
      paintResizeFeedback(graphics, (ResizeOperation.Feedback)editOperationFeedback);

  }

  private void paintResizeFeedback(@NotNull DesignerGraphics graphics, @NotNull ResizeOperation.Feedback feedback) {
    DrawingStyle candidateStyle = new DrawingStyle(Color.blue, new BasicStroke(1));
    graphics.useStyle(candidateStyle);
    for (XTab xTab : feedback.getXTabCandidates())
      paintTab(graphics, xTab);
    for (YTab yTab : feedback.getYTabCandidates())
      paintTab(graphics, yTab);

    DrawingStyle resizeStyle = new DrawingStyle(Color.green, new BasicStroke(1));
    graphics.useStyle(resizeStyle);
    if (feedback.getTargetXTab() != null)
      paintTab(graphics, feedback.getTargetXTab());
    if (feedback.getTargetYTab() != null)
      paintTab(graphics, feedback.getTargetYTab());

    if (feedback.getDetachX() || feedback.getDetachY())
      setToolTipText("detach", 0);
    else
      setToolTipText(null, 0);
  }

  private void paintTab(@NotNull DesignerGraphics graphics, @NotNull XTab tab) {
    LayoutSpec layoutSpec = myLayoutSpecManager.getLayoutSpec();
    double layoutTop = layoutSpec.getTop().getValue();
    double layoutBottom = layoutSpec.getBottom().getValue();
    Point start = myLayoutSpecManager.fromModel(getParent(), new Point((int)tab.getValue(), (int)layoutTop));
    Point end = myLayoutSpecManager.fromModel(getParent(), new Point((int)tab.getValue(), (int)layoutBottom));
    graphics.drawLine(start.x, start.y, end.x, end.y);
  }

  private void paintTab(@NotNull DesignerGraphics graphics, @NotNull YTab tab) {
    LayoutSpec layoutSpec = myLayoutSpecManager.getLayoutSpec();
    double layoutLeft = layoutSpec.getLeft().getValue();
    double layoutRight = layoutSpec.getRight().getValue();
    Point start = myLayoutSpecManager.fromModel(getParent(), new Point((int)layoutLeft, (int)tab.getValue()));
    Point end = myLayoutSpecManager.fromModel(getParent(), new Point((int)layoutRight, (int)tab.getValue()));
    graphics.drawLine(start.x, start.y, end.x, end.y);
  }
}
