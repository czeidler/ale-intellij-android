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

import com.android.ide.common.rendering.api.ViewInfo;
import com.intellij.android.designer.model.RadViewComponent;
import com.intellij.designer.model.RadComponent;
import nz.ac.auckland.ale.LayoutEditor;
import nz.ac.auckland.alm.*;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class LayoutSpecManager {
  boolean myIsValid = true;
  RadComponent myLayoutContainer;
  IALMLayoutSpecs myALMLayoutSpecs;
  LayoutSpec myLayoutSpec;
  LayoutEditor myLayoutEditor;
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

  public Area findRemovedArea(List<IArea> areas) {
    for (IArea area : areas) {
      if (!myAreaToRadViewMap.containsKey(area))
        return (Area)area;
    }
    return null;
  }

  public void setTo(IALMLayoutSpecs almLayoutSpecs, RadComponent layout) {
    myIsValid = true;
    myLayoutContainer = layout;
    myALMLayoutSpecs = almLayoutSpecs;
    myLayoutSpec = LayoutSpec.clone(almLayoutSpecs.getAreas(), almLayoutSpecs.getCustomConstraints(), almLayoutSpecs.getLeftTab(),
                                    almLayoutSpecs.getTopTab(), almLayoutSpecs.getRightTab(), almLayoutSpecs.getBottomTab());
    myRadViewToAreaMap.clear();
    myAreaToRadViewMap.clear();

    List<IArea> areas = almLayoutSpecs.getAreas();

    for (RadComponent child : layout.getChildren()) {
      RadViewComponent viewComponent = (RadViewComponent)child;
      IArea area = almLayoutSpecs.getArea(viewComponent.getViewInfo().getViewObject());
      IArea clone = myLayoutSpec.getAreas().get(areas.indexOf(area));

      myRadViewToAreaMap.put(viewComponent, (Area)clone);
      myAreaToRadViewMap.put((Area)clone, viewComponent);
    }

    myLayoutEditor = new LayoutEditor(myLayoutSpec);
    float view = layout.getBounds().width;
    float model = (float)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue());
    myLayoutEditor.setModelViewScale(view / model);
    myLayoutEditor.setTabWidthView(8);
    myLayoutEditor.setDetachThresholdView(80);
    myLayoutEditor.setSnapView(20);
  }

  public void addComponent(Area addedArea, RadComponent insertComponent) {
    myRadViewToAreaMap.put(insertComponent, addedArea);
    myAreaToRadViewMap.put(addedArea, insertComponent);
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

  public List<RadViewComponent> getChildren() {
    return RadViewComponent.getViewComponents(myLayoutContainer.getChildren());
  }

  public LayoutSpec getLayoutSpec() {
    return myLayoutSpec;
  }

  public LayoutEditor getLayoutEditor() {
    return myLayoutEditor;
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

  public Rectangle fromModel(Component layer, Area.Rect rect) {
    return fromModel(layer, myALMLayoutSpecs, myLayoutContainer, rect);
  }

  public Point fromModel(Component layer, Point point) {
    return fromModel(layer, myALMLayoutSpecs, myLayoutContainer, point);
  }

  public Rectangle toModel(Component layer, Rectangle rect) {
    return toModel(layer, myALMLayoutSpecs, myLayoutContainer, rect);
  }

  public Point toModel(Component layer, Point point) {
    return toModel(layer, myALMLayoutSpecs, myLayoutContainer, point);
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

  static public Point fromModel(Component layer, IALMLayoutSpecs almLayoutSpecs, RadComponent layout, Point point) {
    Rectangle aleLayoutBounds = new Rectangle((int)almLayoutSpecs.getLeftTab().getValue(), (int)almLayoutSpecs.getTopTab().getValue(),
                                              (int)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue()),
                                              (int)(almLayoutSpecs.getBottomTab().getValue() - almLayoutSpecs.getTopTab().getValue()));
    final Rectangle layoutBounds = layout.fromModel(layer, layout.getBounds());
    aleLayoutBounds = layout.fromModel(layer, aleLayoutBounds);
    Point pointView = layout.fromModel(layer, point);
    double offsetX = layoutBounds.getX() - aleLayoutBounds.getX();
    double offsetY = layoutBounds.getY() - aleLayoutBounds.getY();
    pointView.translate((int)offsetX, (int)offsetY);
    return pointView;
  }

  static public Rectangle toModel(Component layer, IALMLayoutSpecs almLayoutSpecs, RadComponent layout, Rectangle rect) {
    Rectangle aleLayoutBounds = new Rectangle((int)almLayoutSpecs.getLeftTab().getValue(), (int)almLayoutSpecs.getTopTab().getValue(),
                                              (int)(almLayoutSpecs.getRightTab().getValue() - almLayoutSpecs.getLeftTab().getValue()),
                                              (int)(almLayoutSpecs.getBottomTab().getValue() - almLayoutSpecs.getTopTab().getValue()));
    Rectangle modelRect = layout.toModel(layer, rect);
    final Rectangle layoutBounds = layout.getBounds();

    double offsetX = aleLayoutBounds.getX() - layoutBounds.getX();
    double offsetY = aleLayoutBounds.getY() - layoutBounds.getY();
    modelRect.translate((int)offsetX, (int)offsetY);
    return modelRect;
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

  static public IALMLayoutSpecs getLayoutSpec(RadViewComponent child) {
    Object viewObject = ((RadViewComponent)child.getParent()).getViewInfo().getViewObject();
    assert viewObject instanceof IALMLayoutSpecs;
    return  (IALMLayoutSpecs)viewObject;
  }

  static public Area getArea(RadViewComponent component) {
    ViewInfo viewInfo = component.getViewInfo();
    if (viewInfo == null)
      return null;
    Object view = viewInfo.getViewObject();
    assert view != null;
    return getLayoutSpec(component).getArea(view);
  }
}
