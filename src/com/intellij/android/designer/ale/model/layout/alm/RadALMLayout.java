/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.android.designer.ale.model.layout.alm;

import com.intellij.android.designer.designSurface.graphics.DrawingStyle;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.android.designer.model.RadViewLayoutWithData;
import com.intellij.designer.designSurface.*;
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.alm.IALMLayoutSpecs;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class RadALMLayout extends RadViewLayoutWithData implements ILayoutDecorator {
  private static final String[] LAYOUT_PARAMS = {"ALMLayout_Layout"};

  ResizeSelectionDecorator selectionDecorator;

  @NotNull
  @Override
  public String[] getLayoutParams() {
    return LAYOUT_PARAMS;
  }

  @Override
  public EditOperation processChildOperation(OperationContext context) {
    if (context.isMove()) {
      if (context.isTree()) {
        return null;
      }
      return new ALMLayoutDragOperation(myContainer, context);
    }
    return null;
  }

  @Override
  public void addStaticDecorators(List<StaticDecorator> decorators, List<RadComponent> selection) {
    /*for (RadComponent component : selection) {
      if (component.getParent() == myContainer) {
        if (!(myContainer.getParent().getLayout() instanceof ILayoutDecorator)) {
          decorators.add(getRelativeDecorator());
        }
        return;
      }
    }*/
    super.addStaticDecorators(decorators, selection);
  }

  @Override
  public ComponentDecorator getChildSelectionDecorator(RadComponent component, List<RadComponent> selection) {
    if (selectionDecorator == null) {
      if (myContainer instanceof RadViewComponent) {
        RadViewComponent container = (RadViewComponent)myContainer;
        IALMLayoutSpecs almLayoutSpecs = (IALMLayoutSpecs)container.getViewInfo().getViewObject();
        selectionDecorator = new ResizeSelectionDecorator(DrawingStyle.SELECTION, almLayoutSpecs);
      }
    }

    return selectionDecorator;
  }
}
