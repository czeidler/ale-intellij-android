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

import com.android.ide.common.rendering.api.ViewInfo;
import com.intellij.android.designer.designSurface.graphics.DrawingStyle;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.android.designer.model.RadViewLayoutWithData;
import com.intellij.designer.designSurface.*;
import com.intellij.designer.model.RadComponent;
import com.intellij.openapi.application.ApplicationManager;
import nz.ac.auckland.ale.IEditOperation;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IALMLayoutSpecs;
import nz.ac.auckland.alm.algebra.SoundLayoutBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class RadALMLayout extends RadViewLayoutWithData implements ILayoutDecorator {
  private static final String[] LAYOUT_PARAMS = {"ALMLayout_Layout"};

  ResizeSelectionDecorator selectionDecorator;
  LayoutSpecManager myLayoutSpecManager;

  ALMLayoutDecorator myRelativeDecorator;

  protected LayoutSpecManager getLayoutSpecManager() {
    RadViewComponent layout = (RadViewComponent)myContainer;
    IALMLayoutSpecs almLayoutSpecs = (IALMLayoutSpecs)layout.getViewInfo().getViewObject();

    if (myLayoutSpecManager != null && myLayoutSpecManager.isValid() && myLayoutSpecManager.getALMLayoutSpecs() == almLayoutSpecs)
      return myLayoutSpecManager;

    myLayoutSpecManager = new LayoutSpecManager(almLayoutSpecs, layout);

    SoundLayoutBuilder.fillWithEmptySpaces(myLayoutSpecManager.getLayoutSpec());

    return myLayoutSpecManager;
  }

  private void invalidateLayoutSpecManager() {
    myLayoutSpecManager = null;
  }

  @NotNull
  @Override
  public String[] getLayoutParams() {
    return LAYOUT_PARAMS;
  }

  @Override
  public EditOperation processChildOperation(OperationContext context) {
    if (context.isMove() || context.isCreate() || context.isAdd()) {
      if (context.isTree())
        return null;
      return new ALMLayoutDragOperation(myContainer, context, getLayoutSpecManager());
    }
    if (context.is(ALMLayoutResizeOperation.TYPE))
      return new ALMLayoutResizeOperation(myContainer, context, getLayoutSpecManager());
    return null;
  }

  private StaticDecorator getRelativeDecorator() {
    if (myRelativeDecorator == null) {
      myRelativeDecorator = new ALMLayoutDecorator(myContainer, this);
    }
    return myRelativeDecorator;
  }

  @Override
  public void addStaticDecorators(List<StaticDecorator> decorators, List<RadComponent> selection) {
    for (RadComponent component : selection) {
      if (component.getParent() == myContainer) {
        if (!(myContainer.getParent().getLayout() instanceof ILayoutDecorator)) {
          decorators.add(getRelativeDecorator());
        }
        return;
      }
    }
    super.addStaticDecorators(decorators, selection);
  }

  @Override
  public ComponentDecorator getChildSelectionDecorator(RadComponent component, List<RadComponent> selection) {
    if (selectionDecorator == null) {
      if (myContainer instanceof RadViewComponent) {
        selectionDecorator = new ResizeSelectionDecorator(DrawingStyle.SELECTION);
      }
    }

    return selectionDecorator;
  }

  @Override
  public void removeComponentFromContainer(final RadComponent component) {
    super.removeComponentFromContainer(component);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        // just reload the the
        invalidateLayoutSpecManager();
        myLayoutSpecManager = getLayoutSpecManager();
        RadViewComponent viewComponent = (RadViewComponent)component;
        Area orgArea = myLayoutSpecManager.readOrgAreaFromRadComponent(viewComponent);
        if (orgArea == null)
          return;
        Area clone = myLayoutSpecManager.getOrgToClonedArea(orgArea);
        IEditOperation deleteOperation = myLayoutSpecManager.getLayoutEditor().getDeleteOperation(clone);
        deleteOperation.perform();

        LayoutSpecXmlWriter xmlWriter = new LayoutSpecXmlWriter(myLayoutSpecManager);
        xmlWriter.write();

        invalidateLayoutSpecManager();
      }
    });
  }
}
