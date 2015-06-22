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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import nz.ac.auckland.ale.*;
import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.android.SdkConstants.AUTO_URI;


class LayoutSpecXmlWriter {
  // ALMLayout layout params:
  public static final String ATTR_LAYOUT_ALIGN_LEFT = "layout_alignLeft";
  public static final String ATTR_LAYOUT_ALIGN_RIGHT = "layout_alignRight";
  public static final String ATTR_LAYOUT_ALIGN_TOP = "layout_alignTop";
  public static final String ATTR_LAYOUT_ALIGN_BOTTOM = "layout_alignBottom";
  public static final String ATTR_LAYOUT_TO_RIGHT_OF = "layout_toRightOf";
  public static final String ATTR_LAYOUT_TO_LEFT_OF = "layout_toLeftOf";
  public static final String ATTR_LAYOUT_BELOW = "layout_below";
  public static final String ATTR_LAYOUT_ABOVE = "layout_above";
  public static final String ATTR_LAYOUT_LEFT_TAB = "layout_leftTab";
  public static final String ATTR_LAYOUT_TOP_TAB = "layout_topTab";
  public static final String ATTR_LAYOUT_RIGHT_TAB = "layout_rightTab";
  public static final String ATTR_LAYOUT_BOTTOM_TAB = "layout_bottomTab";

  public static final String ALE_URI = AUTO_URI;

  interface ITagDirection extends IDirection {
    String getTabTag();
    String getOppositeTabTag();
    String getConnectionTag();
    String getOppositeConnectionTag();
    String getAlignTag();
    String getOppositeAlignTag();
  }

  static class LeftTagDirection extends LeftDirection implements ITagDirection {
    @Override
    public String getTabTag() {
      return ATTR_LAYOUT_LEFT_TAB;
    }

    @Override
    public String getOppositeTabTag() {
      return ATTR_LAYOUT_RIGHT_TAB;
    }

    @Override
    public String getConnectionTag() {
      return ATTR_LAYOUT_TO_RIGHT_OF;
    }

    @Override
    public String getOppositeConnectionTag() {
      return ATTR_LAYOUT_TO_LEFT_OF;
    }

    @Override
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_LEFT;
    }

    @Override
    public String getOppositeAlignTag() {
      return ATTR_LAYOUT_ALIGN_RIGHT;
    }
  }

  static class RightTagDirection extends RightDirection implements ITagDirection {
    @Override
    public String getTabTag() {
      return ATTR_LAYOUT_RIGHT_TAB;
    }

    @Override
    public String getOppositeTabTag() {
      return ATTR_LAYOUT_LEFT_TAB;
    }

    @Override
    public String getConnectionTag() {
      return ATTR_LAYOUT_TO_LEFT_OF;
    }

    @Override
    public String getOppositeConnectionTag() {
      return ATTR_LAYOUT_TO_RIGHT_OF;
    }

    @Override
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_RIGHT;
    }

    @Override
    public String getOppositeAlignTag() {
      return ATTR_LAYOUT_ALIGN_LEFT;
    }
  }

  static class TopTagDirection extends TopDirection implements ITagDirection {
    @Override
    public String getTabTag() {
      return ATTR_LAYOUT_TOP_TAB;
    }

    @Override
    public String getOppositeTabTag() {
      return ATTR_LAYOUT_BOTTOM_TAB;
    }

    @Override
    public String getConnectionTag() {
      return ATTR_LAYOUT_BELOW;
    }

    @Override
    public String getOppositeConnectionTag() {
      return ATTR_LAYOUT_ABOVE;
    }

    @Override
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_TOP;
    }

    @Override
    public String getOppositeAlignTag() {
      return ATTR_LAYOUT_ALIGN_BOTTOM;
    }
  }

  static class BottomTagDirection extends BottomDirection implements ITagDirection {
    @Override
    public String getTabTag() {
      return ATTR_LAYOUT_BOTTOM_TAB;
    }

    @Override
    public String getOppositeTabTag() {
      return ATTR_LAYOUT_TOP_TAB;
    }

    @Override
    public String getConnectionTag() {
      return ATTR_LAYOUT_ABOVE;
    }

    @Override
    public String getOppositeConnectionTag() {
      return ATTR_LAYOUT_BELOW;
    }

    @Override
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_BOTTOM;
    }

    @Override
    public String getOppositeAlignTag() {
      return ATTR_LAYOUT_ALIGN_TOP;
    }
  }

  final LayoutSpecManager myLayoutSpecManager;
  final LayoutSpec myLayoutSpec;

  public LayoutSpecXmlWriter(LayoutSpecManager layoutSpecManager) {
    this.myLayoutSpecManager = layoutSpecManager;
    this.myLayoutSpec = layoutSpecManager.getLayoutSpec();
  }

  private static void clearAttribute(RadViewComponent view, String uri, String attributeName) {
    XmlAttribute attribute = view.getTag().getAttribute(attributeName, uri);
    if (attribute != null)
      attribute.delete();
  }

  private static void clearAttribute(RadViewComponent view, String attributeName) {
    clearAttribute(view, ALE_URI, attributeName);
  }

  @NotNull
  static private String getAttrValue(RadViewComponent viewComponent, String attribute) {
    XmlTag tag = viewComponent.getTag();
    XmlAttribute attr = tag.getAttribute(attribute, ALE_URI);
    if (attr == null)
      return "";
    String value = attr.getValue();
    if (value == null)
      return "";
    return value;
  }

  private <Tab> void writeSpecs(RadViewComponent viewComponent, Area area, Map<Tab, Edge> map, List<String> tabNames,
                                ITagDirection direction, List<Area> handledAreas) {
    // Important: when new component are just added to the layout and the xml file is first read the new components don't have a id yet.
    // Thus, don't add references to items without an id! Also see pickArea.

    // Layout border: there are no border tags.
    if (direction.getTab(area) == direction.getTab(myLayoutSpec)) {
      clearAttribute(viewComponent, direction.getTabTag());
      clearAttribute(viewComponent, direction.getConnectionTag());
      clearAttribute(viewComponent, direction.getAlignTag());
      return;
    }

    Edge edge = direction.getEdge(area, map);
    assert edge != null;

    // tab tags
    XmlAttribute tabTag = viewComponent.getTag().getAttribute(direction.getTabTag(), ALE_URI);
    if (tabTag != null) {
      String tabName = tabTag.getValue();
      List<Area> areas = direction.getAreas(edge);
      List<Area> opAreas = direction.getOppositeAreas(edge);
      boolean tabFound = false;
      for (Area neighbour : areas) {
        if (getAttrValue(myLayoutSpecManager.getComponentFor(neighbour), direction.getOppositeTabTag()).equals(tabName)) {
          tabFound = true;
          break;
        }
      }
      if (!tabFound) {
        for (Area opNeighbour : opAreas) {
          if (opNeighbour == area)
            continue;
          if (getAttrValue(myLayoutSpecManager.getComponentFor(opNeighbour), direction.getTabTag()).equals(tabName)) {
            tabFound = true;
            break;
          }
        }
      }
      if (tabFound) {
        if (!tabNames.contains(tabName))
          tabNames.add(tabName);
        viewComponent.setAttribute(direction.getTabTag(), ALE_URI, tabName);
        clearAttribute(viewComponent, direction.getConnectionTag());
        clearAttribute(viewComponent, direction.getAlignTag());
        return;
      }
      clearAttribute(viewComponent, direction.getTabTag());
    }
    
    // Add either a connect, an align tag or a tab:
    Area connectToArea = null;
    String connectAttribute = null;
    String checkForDuplicatesAttribute = null;
    List<Area> checkForDuplicatesAreas = null;
    List<Area> neighbours = direction.getAreas(edge);
    List<Area> oppositeNeighbours = direction.getOppositeAreas(edge);
    if (neighbours.size() > 0) {
      // connect to
      connectToArea = pickArea(neighbours, null);
      connectAttribute = direction.getConnectionTag();
      checkForDuplicatesAttribute = direction.getOppositeConnectionTag();
      checkForDuplicatesAreas = direction.getAreas(edge);
      clearAttribute(viewComponent, direction.getTabTag());
      clearAttribute(viewComponent, direction.getAlignTag());
    }
    if (connectToArea == null && oppositeNeighbours.size() > 1) {
      // align with
      connectToArea = pickArea(oppositeNeighbours, area);
      connectAttribute = direction.getAlignTag();
      checkForDuplicatesAttribute = direction.getOppositeAlignTag();
      checkForDuplicatesAreas = direction.getOppositeAreas(edge);
      clearAttribute(viewComponent, direction.getTabTag());
      clearAttribute(viewComponent, direction.getConnectionTag());
    }
    if (connectToArea == null) {
      if (neighbours.size() == 0 && oppositeNeighbours.size() == 1){
        // add tab
        String uniqueTabName = getUniqueTabName(tabNames, direction.getTab(area));
        tabNames.add(uniqueTabName);
        viewComponent.setAttribute(direction.getTabTag(), ALE_URI, uniqueTabName);
        clearAttribute(viewComponent, direction.getConnectionTag());
        clearAttribute(viewComponent, direction.getAlignTag());
      } else {
        // There might be no valid connectToArea. Such an area only gets incoming connections and is handled later.
        clearAttribute(viewComponent, direction.getTabTag());
        clearAttribute(viewComponent, direction.getConnectionTag());
        clearAttribute(viewComponent, direction.getAlignTag());
      }
      return;
    }
    assert connectAttribute != null;
    assert checkForDuplicatesAttribute != null;
    assert checkForDuplicatesAreas != null;

    // Check for an existing valid connection and clear the attribute if there is one. This avoids redundant attributes.
    for (Area neighbour : checkForDuplicatesAreas) {
      if (!handledAreas.contains(neighbour))
        continue;
      String neighbourId = getAttrValue(myLayoutSpecManager.getComponentFor(neighbour), checkForDuplicatesAttribute);
      if (neighbourId.equals(viewComponent.getId())) {
        clearAttribute(viewComponent, connectAttribute);
        return;
      }
    }
    viewComponent.setAttribute(connectAttribute, ALE_URI, myLayoutSpecManager.getComponentFor(connectToArea).ensureId());
  }

  Area pickArea(List<Area> pickFrom, Area veto) {
    for (Area area : pickFrom) {
      if (area == veto)
        continue;
      RadViewComponent view = myLayoutSpecManager.getComponentFor(area);
      if (view.getId() == null)
        continue;
      return area;
    }
    return null;
  }

  static private String getUniqueTabName(List<String> tabNames, Variable tab) {
    String directionName;
    if (tab instanceof XTab)
      directionName = "x";
    else
      directionName = "y";
    for (int i = 0; ; i++) {
      String tabName = directionName + i;
      boolean containsName = false;
      for (String string : tabNames) {
        if (string.equals(tabName)) {
          containsName = true;
          break;
        }
      }
      if (!containsName)
        return tabName;
    }
  }

  public void write() {
    LayoutEditor layoutEditor = myLayoutSpecManager.getLayoutEditor();
    Map<XTab, Edge> xTabEdgeMap = layoutEditor.getLayoutStructure().getXTabEdges();
    Map<YTab, Edge> yTabEdgeMap = layoutEditor.getLayoutStructure().getYTabEdges();
    final List<String> xTabNames = new ArrayList<String>();
    final List<String> yTabNames = new ArrayList<String>();

    List<Area> handledAreas = new ArrayList<Area>();
    // We have to process the children in the correct order so don't iterate over the map directly! see writeSpecs for more info
    for (RadViewComponent viewComponent : myLayoutSpecManager.getChildren()) {
      Area area = myLayoutSpecManager.getRadViewToAreaMap().get(viewComponent);
      writeSpecs(viewComponent, area, xTabEdgeMap, xTabNames, new LeftTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, yTabEdgeMap, yTabNames, new TopTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, xTabEdgeMap, xTabNames, new RightTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, yTabEdgeMap, yTabNames, new BottomTagDirection(), handledAreas);
      handledAreas.add(area);
    }
  }

  private <Tab> void clearRemovedComponent(RadViewComponent viewComponent, Area area, RadViewComponent removedView, ITagDirection direction,
                                                  Map<Tab, Edge> map, List<String> tabNames) {
    boolean needReconnect = false;

    String componentId = removedView.getId();
    if (componentId == null)
      return;
    String connectedToId = getAttrValue(viewComponent, direction.getConnectionTag());
    if (componentId.equals(connectedToId)) {
      clearAttribute(viewComponent, direction.getConnectionTag());
      needReconnect = true;
    }
    String alignId = getAttrValue(viewComponent, direction.getAlignTag());
    if (componentId.equals(alignId)) {
      clearAttribute(viewComponent, direction.getAlignTag());
      needReconnect = true;
    }

    if (!needReconnect)
      return;
    Edge edge = direction.getEdge(area, map);
    // Add either a connect or an align tag:
    Area connectToArea = null;
    String connectAttribute = null;
    if (direction.getAreas(edge).size() > 0) {
      // connect to
      List<Area> areas = direction.getAreas(edge);
      Area removedArea = myLayoutSpecManager.findRemovedArea(areas);
      assert removedArea != null;
      connectToArea = pickArea(areas, null, removedArea);
      connectAttribute = direction.getConnectionTag();
    }
    else if (direction.getOppositeAreas(edge).size() > 1) {
      // align with
      List<Area> areas = direction.getOppositeAreas(edge);
      Area removedArea = myLayoutSpecManager.findRemovedArea(areas);
      assert removedArea != null;
      connectToArea = pickArea(areas, area, removedArea);
      connectAttribute = direction.getAlignTag();
    }
    if (connectToArea == null) {
      // insert a new tab
      String newTabName = getUniqueTabName(tabNames, direction.createTab());
      tabNames.add(newTabName);
      viewComponent.setAttribute(direction.getTabTag(), ALE_URI, newTabName);
      return;
    }
    RadViewComponent connectToView = myLayoutSpecManager.getComponentFor(connectToArea);
    viewComponent.setAttribute(connectAttribute, ALE_URI, connectToView.ensureId());
  }

  static private <Tab extends Variable> void collectTabNames(Map<Tab, Edge> tabs, List<String> names) {
    for (Map.Entry<Tab, Edge> entry : tabs.entrySet()) {
      String tabName = entry.getKey().getName();
      if (tabName != null)
        names.add(tabName);
    }
  }

  static private Area pickArea(List<Area> areas, Area veto, Area veto2) {
    for (Area area : areas) {
      if (area == veto || area == veto2)
        continue;
      return area;
    }
    return null;
  }

  public void clearRemovedComponent(RadViewComponent removedView) {
    if (removedView.getId() == null)
      return;
    LayoutEditor layoutEditor = myLayoutSpecManager.getLayoutEditor();
    Map<XTab, Edge> xTabEdgeMap = layoutEditor.getLayoutStructure().getXTabEdges();
    Map<YTab, Edge> yTabEdgeMap = layoutEditor.getLayoutStructure().getYTabEdges();
    List<String> xTabNames = new ArrayList<String>();
    collectTabNames(xTabEdgeMap, xTabNames);
    List<String> yTabNames = new ArrayList<String>();
    collectTabNames(yTabEdgeMap, yTabNames);

    for (RadViewComponent viewComponent : myLayoutSpecManager.getChildren()) {
      Area area = myLayoutSpecManager.getRadViewToAreaMap().get(viewComponent);
      clearRemovedComponent(viewComponent, area, removedView, new LeftTagDirection(), xTabEdgeMap, xTabNames);
      clearRemovedComponent(viewComponent, area, removedView, new TopTagDirection(), yTabEdgeMap, yTabNames);
      clearRemovedComponent(viewComponent, area, removedView, new RightTagDirection(), xTabEdgeMap, xTabNames);
      clearRemovedComponent(viewComponent, area, removedView, new BottomTagDirection(), yTabEdgeMap, yTabNames);
    }
  }
}
