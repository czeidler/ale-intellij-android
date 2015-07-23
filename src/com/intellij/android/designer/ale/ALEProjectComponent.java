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
package com.intellij.android.designer.ale;

import com.intellij.android.designer.model.ViewsMetaManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.palette.Palette;

import java.io.InputStream;


public class ALEProjectComponent extends AbstractProjectComponent {
  public ALEProjectComponent(Project project) {
    super(project);

  }

  private void installAndroid() {
    ViewsMetaManager viewsMetaManager = ViewsMetaManager.getInstance(myProject);
    viewsMetaManager.getPaletteGroups();

    InputStream stream = getClass().getResourceAsStream("ale-views-meta-model.xml");
    try {
      viewsMetaManager.load(stream);
      stream.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void installSwing() {
    Palette palette = Palette.getInstance(myProject);
    //TODO: install ALM layout to palette.addIdem()
  }

  @Override
  public void initComponent() {
    installAndroid();
    installSwing();
  }
}
