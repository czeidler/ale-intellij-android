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

import com.intellij.designer.designSurface.OperationContext;
import com.intellij.designer.model.RadComponent;


public class ALMLayoutResizeOperation extends ALMLayoutOperation {
  public static final String TYPE = "alm_resize";

  public ALMLayoutResizeOperation(RadComponent container, OperationContext context, LayoutSpecManager layoutSpecManager) {
    super(container, context, layoutSpecManager);
  }

  @Override
  public void showFeedback() {
    super.showFeedback();
  }
}
