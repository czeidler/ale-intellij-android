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

public class MoveBetweenOperation extends AbstractEditOperation {
  final private Area movedArea;

  public MoveBetweenOperation(LayoutEditor layoutEditor, Area movedArea, float dragX, float dragY) {
    super(layoutEditor);
    this.movedArea = movedArea;
  }

  @Override
  public boolean canPerform() {
    return false;
  }

  @Override
  public void perform() {

  }

  public class Feedback implements IEditOperationFeedback {

  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return new Feedback();
  }
}
