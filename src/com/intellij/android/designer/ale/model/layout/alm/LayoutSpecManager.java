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
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.alm.*;

import java.util.HashMap;
import java.util.Map;


class LayoutSpecManager {
  RadComponent myLayout;
  LayoutSpec myLayoutSpec;
  final Map<RadComponent, Area> myRadViewToAreaMap = new HashMap<RadComponent, Area>();
  final Map<Area, RadComponent> myAreaToRadViewMap = new HashMap<Area, RadComponent>();

  private XTab getTab(Map<XTab, XTab> oldToCloneMap, XTab oldTab) {
    XTab tab = oldToCloneMap.get(oldTab);
    if (tab == null) {
      tab = new XTab(oldTab.getName());
      tab.setValue(oldTab.getValue());
      oldToCloneMap.put(oldTab, tab);
    }
    return tab;
  }

  private YTab getTab(Map<YTab, YTab> oldToCloneMap, YTab oldTab) {
    YTab tab = oldToCloneMap.get(oldTab);
    if (tab == null) {
      tab = new YTab(oldTab.getName());
      tab.setValue(oldTab.getValue());
      oldToCloneMap.put(oldTab, tab);
    }
    return tab;
  }

  public void setTo(IALMLayoutSpecs almLayoutSpecs, RadComponent layout) {
    myLayout = layout;
    myLayoutSpec = new LayoutSpec();
    myRadViewToAreaMap.clear();
    myAreaToRadViewMap.clear();

    final Map<XTab, XTab> oldToCloneXTabs = new HashMap<XTab, XTab>();
    final Map<YTab, YTab> oldToCloneYTabs = new HashMap<YTab, YTab>();
    oldToCloneXTabs.put(almLayoutSpecs.getLeftTab(), myLayoutSpec.getLeft());
    oldToCloneXTabs.put(almLayoutSpecs.getRightTab(), myLayoutSpec.getRight());
    oldToCloneYTabs.put(almLayoutSpecs.getTopTab(), myLayoutSpec.getTop());
    oldToCloneYTabs.put(almLayoutSpecs.getBottomTab(), myLayoutSpec.getBottom());
    myLayoutSpec.getLeft().setValue(almLayoutSpecs.getLeftTab().getValue());
    myLayoutSpec.getTop().setValue(almLayoutSpecs.getTopTab().getValue());
    myLayoutSpec.getRight().setValue(almLayoutSpecs.getRightTab().getValue());
    myLayoutSpec.getBottom().setValue(almLayoutSpecs.getBottomTab().getValue());

    for (RadComponent child : layout.getChildren()) {
      RadViewComponent viewComponent = (RadViewComponent)child;
      Object view = viewComponent.getViewInfo().getViewObject();
      Area area = almLayoutSpecs.getArea(view);

      XTab left = getTab(oldToCloneXTabs, area.getLeft());
      YTab top = getTab(oldToCloneYTabs, area.getTop());
      XTab right = getTab(oldToCloneXTabs, area.getRight());
      YTab bottom = getTab(oldToCloneYTabs, area.getBottom());

      Area clone = myLayoutSpec.addArea(left, top, right, bottom);
      clone.setAlignment(area.getHorizontalAlignment(), area.getVerticalAlignment());
      clone.setMinSize(area.getMinSize());
      clone.setPreferredSize(area.getPreferredSize());
      clone.setMaxSize(area.getMaxSize());

      myRadViewToAreaMap.put(viewComponent, clone);
      myAreaToRadViewMap.put(clone, viewComponent);
    }
  }

  public Area getAreaFor(RadComponent radComponent) {
    return myRadViewToAreaMap.get(radComponent);
  }

  public RadViewComponent getComponentFor(Area area) {
    return (RadViewComponent)myAreaToRadViewMap.get(area);
  }

  public LayoutSpec getLayoutSpec() {
    return myLayoutSpec;
  }

  public Map<RadComponent, Area> getRadViewToAreaMap() {
    return myRadViewToAreaMap;
  }

  public Map<Area, RadComponent> getAreaToRadViewMap() {
    return myAreaToRadViewMap;
  }
}
