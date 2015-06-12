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

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class LayoutSpecManager {
  boolean myIsValid = true;
  RadComponent myLayout;
  IALMLayoutSpecs myALMLayoutSpecs;
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
    myIsValid = true;
    myLayout = layout;
    myALMLayoutSpecs = almLayoutSpecs;
    myLayoutSpec = LayoutSpec.clone(almLayoutSpecs.getAreas(), almLayoutSpecs.getCustomConstraints(), almLayoutSpecs.getLeftTab(),
                                    almLayoutSpecs.getTopTab(), almLayoutSpecs.getRightTab(), almLayoutSpecs.getBottomTab());
    myRadViewToAreaMap.clear();
    myAreaToRadViewMap.clear();

    List<Area> areas = almLayoutSpecs.getAreas();

    for (RadComponent child : layout.getChildren()) {
      RadViewComponent viewComponent = (RadViewComponent)child;
      Area area = almLayoutSpecs.getArea(viewComponent.getViewInfo().getViewObject());
      Area clone = myLayoutSpec.getAreas().get(areas.indexOf(area));

      myRadViewToAreaMap.put(viewComponent, clone);
      myAreaToRadViewMap.put(clone, viewComponent);
    }
  }

  public boolean isValid() {
    return myIsValid;
  }

  public void invalidate() {
    myIsValid = false;
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

  public IALMLayoutSpecs getALMLayoutSpecs() {
    return myALMLayoutSpecs;
  }

  static public Rectangle fromModel(Component layer, IALMLayoutSpecs almLayoutSpecs, RadComponent layout, Area.Rect rect) {
    Rectangle areaBounds = new Rectangle(Math.round(rect.left), Math.round(rect.top), Math.round(rect.getWidth()),
                                         Math.round(rect.getHeight()));
    Rectangle aleLayoutBounds = new Rectangle((int)almLayoutSpecs.getLeftTab().getValue(), (int)almLayoutSpecs.getTopTab().getValue(),
                                              (int)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue()),
                                              (int)(almLayoutSpecs.getBottomTab().getValue() - almLayoutSpecs.getTopTab().getValue()));
    final Rectangle layoutBounds = layout.fromModel(layer, layout.getBounds());
    aleLayoutBounds = layout.fromModel(layer, aleLayoutBounds);
    areaBounds = layout.fromModel(layer, areaBounds);
    double offsetX = layoutBounds.getX() - aleLayoutBounds.getX();
    double offsetY = layoutBounds.getY() - aleLayoutBounds.getY();
    areaBounds.translate((int)offsetX, (int)offsetY);
    return areaBounds;
  }

  static public Point toModel(Component layer, IALMLayoutSpecs almLayoutSpecs, RadComponent layout, Point point) {
    Rectangle aleLayoutBounds = new Rectangle((int)almLayoutSpecs.getLeftTab().getValue(), (int)almLayoutSpecs.getTopTab().getValue(),
                                              (int)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue()),
                                              (int)(almLayoutSpecs.getBottomTab().getValue() - almLayoutSpecs.getTopTab().getValue()));
    Point modelPoint = layout.toModel(layer, point);
    final Rectangle layoutBounds = layout.getBounds();

    double offsetX = aleLayoutBounds.getX() - layoutBounds.getX();
    double offsetY = aleLayoutBounds.getY() - layoutBounds.getY();
    modelPoint.translate((int)offsetX, (int)offsetY);
    return modelPoint;
  }
}
