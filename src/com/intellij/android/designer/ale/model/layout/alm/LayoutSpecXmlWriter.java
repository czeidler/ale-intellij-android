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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
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

  static class Edge {
    public List<Area> areas1 = new ArrayList<Area>();
    public List<Area> areas2 = new ArrayList<Area>();

    static private <Tab> Edge getEdge(Tab tab, Map<Tab, Edge> map) {
      Edge edge = map.get(tab);
      if (edge != null)
        return edge;
      edge = new Edge();
      map.put(tab, edge);
      return edge;
    }

    static public void fillEdges(List<Area> areas, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap) {
      for (Area area : areas) {
        Edge leftEdge = getEdge(area.getLeft(), xMap);
        leftEdge.areas2.add(area);
        Edge topEdge = getEdge(area.getTop(), yMap);
        topEdge.areas2.add(area);
        Edge rightEdge = getEdge(area.getRight(), xMap);
        rightEdge.areas1.add(area);
        Edge bottomEdge = getEdge(area.getBottom(), yMap);
        bottomEdge.areas1.add(area);
      }
    }
  }

  interface IDirection {
    <Tab> Edge getEdge(Area area, Map<Tab, Edge> map);
    Variable getTab(Area area);
    Variable getTab(LayoutSpec layoutSpec);
    List<Area> getAreas(Edge edge);
    List<Area> getOppositeAreas(Edge edge);
    String getTabTag();
    String getOppositeTabTag();
    String getConnectionTag();
    String getAlignTag();
  }

  class LeftDirection implements IDirection {
    @Override
    public <Tab> Edge getEdge(Area area, Map<Tab, Edge> map) {
      return map.get(area.getLeft());
    }

    @Override
    public Variable getTab(Area area) {
      return area.getLeft();
    }

    @Override
    public Variable getTab(LayoutSpec layoutSpec) {
      return layoutSpec.getLeft();
    }

    @Override
    public List<Area> getAreas(Edge edge) {
      return edge.areas1;
    }

    @Override
    public List<Area> getOppositeAreas(Edge edge) {
      return edge.areas2;
    }

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
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_LEFT;
    }
  }

  class RightDirection implements IDirection {
    @Override
    public <Tab> Edge getEdge(Area area, Map<Tab, Edge> map) {
      return map.get(area.getRight());
    }

    @Override
    public Variable getTab(Area area) {
      return area.getRight();
    }

    @Override
    public Variable getTab(LayoutSpec layoutSpec) {
      return layoutSpec.getRight();
    }

    @Override
    public List<Area> getAreas(Edge edge) {
      return edge.areas2;
    }

    @Override
    public List<Area> getOppositeAreas(Edge edge) {
      return edge.areas1;
    }

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
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_RIGHT;
    }
  }

  class TopDirection implements IDirection {
    @Override
    public <Tab> Edge getEdge(Area area, Map<Tab, Edge> map) {
      return map.get(area.getTop());
    }

    @Override
    public Variable getTab(Area area) {
      return area.getTop();
    }

    @Override
    public Variable getTab(LayoutSpec layoutSpec) {
      return layoutSpec.getTop();
    }

    @Override
    public List<Area> getAreas(Edge edge) {
      return edge.areas1;
    }

    @Override
    public List<Area> getOppositeAreas(Edge edge) {
      return edge.areas2;
    }

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
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_TOP;
    }
  }

  class BottomDirection implements IDirection {
    @Override
    public <Tab> Edge getEdge(Area area, Map<Tab, Edge> map) {
      return map.get(area.getBottom());
    }

    @Override
    public Variable getTab(Area area) {
      return area.getBottom();
    }

    @Override
    public Variable getTab(LayoutSpec layoutSpec) {
      return layoutSpec.getBottom();
    }

    @Override
    public List<Area> getAreas(Edge edge) {
      return edge.areas2;
    }

    @Override
    public List<Area> getOppositeAreas(Edge edge) {
      return edge.areas1;
    }

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
    public String getAlignTag() {
      return ATTR_LAYOUT_ALIGN_BOTTOM;
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

  private <Tab> void writeSpecs(RadViewComponent viewComponent, Area area, Map<Tab, Edge> map, IDirection direction) {
    // border?
    if (direction.getTab(area) == direction.getTab(myLayoutSpec)) {
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
      clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
      return;
    }

    Edge edge = direction.getEdge(area, map);
    // tab
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
          if (getAttrValue(myLayoutSpecManager.getComponentFor(opNeighbour), direction.getTabTag()).equals(tabName)) {
            tabFound = true;
            break;
          }
        }
      }
      if (tabFound) {
        clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
        clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
        return;
      }
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
    }

    // If valid connection exist leave

    // Valid connected to connection?
    String connectedToId = getAttrValue(viewComponent, direction.getTabTag());
    if (!connectedToId.equals("")) {
      List<Area> areas = direction.getAreas(edge);
      for (Area neighbour : areas) {
        String neighbourId = myLayoutSpecManager.getComponentFor(neighbour).getId();
        if (neighbourId != null && neighbourId.equals(connectedToId)) {
          clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
          clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
          return;
        }
      }
    }

    // Valid align with connection?
    String alignedToId = getAttrValue(viewComponent, direction.getAlignTag());
    if (!alignedToId.equals("")) {
      List<Area> areas = direction.getOppositeAreas(edge);
      for (Area neighbour : areas) {
        String neighbourId = myLayoutSpecManager.getComponentFor(neighbour).getId();
        if (neighbourId != null && neighbourId.equals(alignedToId)) {
          clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
          clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
          return;
        }
      }
    }

    // Add a either a connect or align connection:
    Area connectToArea;
    String connectAttribute;
    if (direction.getAreas(edge).size() > 0) {
      connectToArea = direction.getAreas(edge).get(0);
      connectAttribute = direction.getConnectionTag();
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
    } else {
      connectToArea = direction.getOppositeAreas(edge).get(0);
      connectAttribute = direction.getAlignTag();
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
    }
    viewComponent.setAttribute(connectAttribute, ALE_URI, myLayoutSpecManager.getComponentFor(connectToArea).ensureId());
  }

  public void applyNewSpecs() {
    Map<XTab, Edge> xTabEdgeMap = new HashMap<XTab, Edge>();
    Map<YTab, Edge> yTabEdgeMap = new HashMap<YTab, Edge>();
    Edge.fillEdges(myLayoutSpec.getAreas(), xTabEdgeMap, yTabEdgeMap);

    for (Map.Entry<RadComponent, Area> entry : myLayoutSpecManager.getRadViewToAreaMap().entrySet()) {
      RadViewComponent viewComponent = (RadViewComponent)entry.getKey();
      Area area = entry.getValue();
      writeSpecs(viewComponent, area, xTabEdgeMap, new LeftDirection());
      writeSpecs(viewComponent, area, yTabEdgeMap, new TopDirection());
      writeSpecs(viewComponent, area, xTabEdgeMap, new RightDirection());
      writeSpecs(viewComponent, area, yTabEdgeMap, new BottomDirection());
    }
  }

}
