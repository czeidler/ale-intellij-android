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
package nz.ac.auckland.ale;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;

import java.util.List;

public class MoveOperation extends AbstractEditOperation {
  final private Area movedArea;
  EmptyAreaFinder emptyAreaFinder;

  public MoveOperation(LayoutEditor layoutEditor, Area movedArea, Area.Rect dragRect, float dragX, float dragY) {
    super(layoutEditor);

    this.movedArea = movedArea;

    emptyAreaFinder = new EmptyAreaFinder(layoutEditor.getLayoutStructure());
    if (!emptyAreaFinder.find(dragRect, dragX, dragY, layoutEditor.getSnapModel())) {
      emptyAreaFinder = null;
      return;
    }
  }

  @Override
  public boolean canPerform() {
    return emptyAreaFinder != null;
  }

  @Override
  public void perform() {
    AreaCandidate targetArea = emptyAreaFinder.getTargetArea();
    movedArea.setTo(targetArea.left, targetArea.top, targetArea.right, targetArea.bottom);
  }

  public class Feedback implements IEditOperationFeedback {
    public AreaCandidate getTargetArea() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getTargetArea();
    }
    public AreaCandidate getMaxArea() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxArea();
    }
    public List<XTab> getMaxAreaXTabs() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxAreaXTabs();
    }

    public List<YTab> getMaxAreaYTabs() {
      if (emptyAreaFinder == null)
        return null;
      return emptyAreaFinder.getMaxAreaYTabs();
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
