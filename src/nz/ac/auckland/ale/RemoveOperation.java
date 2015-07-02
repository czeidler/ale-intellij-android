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
import nz.ac.auckland.alm.algebra.TilingAlgebra;


public class RemoveOperation extends AbstractEditOperation {
  final Area area;

  public RemoveOperation(LayoutEditor layoutEditor, Area area) {
    super(layoutEditor);

    this.area = area;
  }

  @Override
  public boolean canPerform() {
    return true;
  }

  @Override
  public void perform() {
    TilingAlgebra.makeAreaEmpty(layoutEditor.getAlgebraData(), area);
  }

  @Override
  public IEditOperationFeedback getFeedback() {
    return null;
  }
}
