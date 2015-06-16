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
package nz.ac.auckland.ale;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Edge {
  public List<Area> areas1 = new ArrayList<Area>();
  public List<Area> areas2 = new ArrayList<Area>();

  static private <Tab> Edge getEdge(Tab tab, Map<Tab, Edge> map) {
    Edge edge = map.get(tab);
    if (edge != null) return edge;
    edge = new Edge();
    map.put(tab, edge);
    return edge;
  }

  static public  <Tab extends Variable> boolean isInChain(Edge edge, Variable tab, Map<Tab, Edge> edges, IDirection direction) {
    for (Area area : direction.getAreas(edge)) {
      Variable currentTab = direction.getTab(area);
      if (currentTab == tab)
        return true;
      if (isInChain(edges.get(currentTab), tab, edges, direction))
        return true;
    }
    return false;
  }

  static public void fillEdges(LayoutSpec layoutSpec, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap) {
    fillEdges(layoutSpec, xMap, yMap, null);
  }

  static public void fillEdges(LayoutSpec layoutSpec, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap, Area removedArea) {
    for (Area area : layoutSpec.getAreas()) {
      if (area == removedArea)
        continue;
      Edge leftEdge = getEdge(area.getLeft(), xMap);
      leftEdge.areas2.add(area);
      Edge topEdge = getEdge(area.getTop(), yMap);
      topEdge.areas2.add(area);
      Edge rightEdge = getEdge(area.getRight(), xMap);
      rightEdge.areas1.add(area);
      Edge bottomEdge = getEdge(area.getBottom(), yMap);
      bottomEdge.areas1.add(area);
    }
    // ensure that the border tabs are in the map
    getEdge(layoutSpec.getLeft(), xMap);
    getEdge(layoutSpec.getTop(), yMap);
    getEdge(layoutSpec.getRight(), xMap);
    getEdge(layoutSpec.getBottom(), yMap);
  }
}
