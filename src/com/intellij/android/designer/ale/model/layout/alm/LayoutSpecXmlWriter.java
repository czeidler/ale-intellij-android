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
import nz.ac.auckland.ale.*;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
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

  private <Tab> void writeSpecs(RadViewComponent viewComponent, Area area, Map<Tab, Edge> map, List<String> freeTabNames,
                                ITagDirection direction, List<Area> handledAreas) {
    // border?
    if (direction.getTab(area) == direction.getTab(myLayoutSpec)) {
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
      clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
      return;
    }

    Edge edge = direction.getEdge(area, map);
    assert edge != null;
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
          if (opNeighbour == area)
            continue;
          if (getAttrValue(myLayoutSpecManager.getComponentFor(opNeighbour), direction.getTabTag()).equals(tabName)) {
            tabFound = true;
            break;
          }
        }
      }
      if (tabFound) {
        freeTabNames.add(tabName);
        viewComponent.setAttribute(direction.getTabTag(), ALE_URI, tabName);
        clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
        clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
        return;
      }
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
    }

    // If valid connection exist leave

    // Valid connected to connection?
    String connectedToId = getAttrValue(viewComponent, direction.getTabTag());
    if (!connectedToId.isEmpty()) {
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
    if (!alignedToId.isEmpty()) {
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

    // Add either a connect or an align tag:
    Area connectToArea;
    String connectAttribute;
    String checkForDuplicatesAttribute;
    List<Area> checkForDuplicatesAreas;
    if (direction.getAreas(edge).size() > 0) {
      // connect to
      connectToArea = direction.getAreas(edge).get(0);
      connectAttribute = direction.getConnectionTag();
      checkForDuplicatesAttribute = direction.getOppositeConnectionTag();
      checkForDuplicatesAreas = direction.getAreas(edge);
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
    }
    else if (direction.getOppositeAreas(edge).size() > 1) {
      // align with
      connectToArea = direction.getOppositeAreas(edge).get(0);
      // don't align with itself
      if (connectToArea == area)
        connectToArea = direction.getOppositeAreas(edge).get(1);
      connectAttribute = direction.getAlignTag();
      checkForDuplicatesAttribute = direction.getOppositeAlignTag();
      checkForDuplicatesAreas = direction.getOppositeAreas(edge);
      clearAttribute(viewComponent, ALE_URI, direction.getTabTag());
      clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
    } else {
      // add tab
      String uniqueTabName = getUniqueTabName(freeTabNames, direction.getTab(area));
      freeTabNames.add(uniqueTabName);
      viewComponent.setAttribute(direction.getTabTag(), ALE_URI, uniqueTabName);
      clearAttribute(viewComponent, ALE_URI, direction.getConnectionTag());
      clearAttribute(viewComponent, ALE_URI, direction.getAlignTag());
      return;
    }
    // Check for an existing valid connection and clear the attribute if there is one. This avoids redundant attributes.
    for (Area neighbour : checkForDuplicatesAreas) {
      if (!handledAreas.contains(neighbour))
        continue;
      connectedToId = getAttrValue(myLayoutSpecManager.getComponentFor(neighbour), checkForDuplicatesAttribute);
      if (connectedToId.equals(viewComponent.getId())) {
        clearAttribute(viewComponent, ALE_URI, connectAttribute);
        return;
      }
    }
    viewComponent.setAttribute(connectAttribute, ALE_URI, myLayoutSpecManager.getComponentFor(connectToArea).ensureId());
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
    final List<String> freeXTabNames = new ArrayList<String>();
    final List<String> freeYTabNames = new ArrayList<String>();

    List<Area> handledAreas = new ArrayList<Area>();
    for (Map.Entry<RadComponent, Area> entry : myLayoutSpecManager.getRadViewToAreaMap().entrySet()) {
      RadViewComponent viewComponent = (RadViewComponent)entry.getKey();
      Area area = entry.getValue();
      writeSpecs(viewComponent, area, xTabEdgeMap, freeXTabNames, new LeftTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, yTabEdgeMap, freeYTabNames, new TopTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, xTabEdgeMap, freeXTabNames, new RightTagDirection(), handledAreas);
      writeSpecs(viewComponent, area, yTabEdgeMap, freeYTabNames, new BottomTagDirection(), handledAreas);
      handledAreas.add(area);
    }
  }

}
