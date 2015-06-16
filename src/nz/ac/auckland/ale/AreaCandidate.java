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

import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;


public class AreaCandidate {
  public XTab left;
  public YTab top;
  public XTab right;
  public YTab bottom;

  public AreaCandidate() {

  }

  public AreaCandidate(AreaCandidate area) {
    this.left = area.left;
    this.top = area.top;
    this.right = area.right;
    this.bottom = area.bottom;
  }

  public AreaCandidate(XTab left, YTab top, XTab right, YTab bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  public float getWidth() {
    return (float)(right.getValue() - left.getValue());
  }

  public float getHeight() {
    return (float)(bottom.getValue() - top.getValue());
  }
}
