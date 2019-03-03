package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for api level type
 *
 */
public class YoungAndroidMaxApiLevelPropertyEditor extends ChoicePropertyEditor {


  private static final Choice[] apiOptions = new Choice[] {
          new Choice("API 26", "26"),
          new Choice("API 25", "25"),
          new Choice("API 24", "24"),
          new Choice("API 23", "23"),
          new Choice("API 22", "22")
  };


  public YoungAndroidMaxApiLevelPropertyEditor() {
    super(apiOptions);
  }
}

