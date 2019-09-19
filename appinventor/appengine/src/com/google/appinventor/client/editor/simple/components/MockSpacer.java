package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockSpacer extends MockVisibleComponent {

  private final Image largeImage = new Image(images.spacerbig());
  private SimplePanel widget;
  /**
   * Component type name.
   */
  public static final String TYPE = "Spacer";

//  private Label labelWidget;
  /**
   * Creates a new Mock component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockSpacer(SimpleEditor editor) {
    super(editor, TYPE, images.spacer());

//    labelWidget = new Label();
//    MockComponentsUtil.setWidgetBackgroundColor(labelWidget, "&ff0000");
    // Initialize mock UI
    widget = new SimplePanel();
    widget.setStylePrimaryName("ode-SimpleMockContainer");
    widget.addStyleDependentName("centerContents");
//    widget.setWidget(labelWidget);
    initComponent(widget);
  }


  @Override
  public int getPreferredWidth() {
    return largeImage.getWidth();
  }

  // we override this to get the image height
  @Override
  public int getPreferredHeight() {
    return largeImage.getHeight();
  }

//  // override the width and height hints, so that automatic will in fact be fill-parent
//  @Override
//  int getWidthHint() {
//    int widthHint = super.getWidthHint();
//    if (widthHint == LENGTH_PREFERRED) {
//      widthHint = LENGTH_FILL_PARENT;
//    }
//    return widthHint;
//  }

  //    @Override int getHeightHint() {
//        int heightHint = super.getHeightHint();
//        if (heightHint == LENGTH_PREFERRED) {
//            heightHint = LENGTH_FILL_PARENT;
//        }
//        return heightHint;
//    }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    }
  }

  /*
 * Sets the label's BackgroundColor property to a new value.
 */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(widget, text);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    //We don't want to allow user to change the background color. Causes issue in building.
    // Adding this so that it won't break existing apps that have already set background color
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR) ){
      return false;
    }

    return super.isPropertyVisible(propertyName);
  }

}
