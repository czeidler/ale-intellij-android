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

import com.intellij.android.designer.model.RadViewComponent;

import java.util.List;


class DragState {
  final RadViewComponent draggedComponent;
  final List<RadViewComponent> components;

  public DragState(RadViewComponent draggedComponent, List<RadViewComponent> components) {
    this.draggedComponent = draggedComponent;
    this.components = components;
  }

  public DragState(DragState previousState) {
    this.draggedComponent = previousState.draggedComponent;
    this.components = previousState.components;
  }
}

class SwapState extends DragState {
  public SwapState(RadViewComponent draggedComponent, List<RadViewComponent> components) {
    super(draggedComponent, components);
  }

  public SwapState(DragState previousState) {
    super(previousState);
  }
}
