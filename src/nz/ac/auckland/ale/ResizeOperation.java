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
import nz.ac.auckland.linsolve.Variable;

public class ResizeOperation extends AbstractEditOperation {
  Area targetArea;
  Variable targetTab;

  static public ResizeOperation detect(LayoutEditor layoutEditor, Area movedArea, XTab xTab, YTab yTab) {
    ResizeOperation resizeOperation = new ResizeOperation(layoutEditor);
    if (!resizeOperation.detect(movedArea, xTab, yTab))
      return null;
    return resizeOperation;
  }

  private ResizeOperation(LayoutEditor layoutEditor) {
    super(layoutEditor);
  }

  private boolean detect(Area movedArea, XTab xTab, YTab yTab) {

    return false;
  }

  @Override
  public void perform() {

  }

  public class Feedback implements IEditOperationFeedback {
    public Area getTargetArea() {
      return targetArea;
    }

    public Variable getTargetTab() {
      return targetTab;
    }
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
