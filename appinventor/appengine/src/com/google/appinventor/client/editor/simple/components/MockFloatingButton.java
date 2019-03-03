package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.Image;

public class MockFloatingButton extends MockNonVisibleComponent {

//    private final Image largeImage = new Image(images.admobbig());

    /**
     * Component type name.
     */
    public static final String TYPE = "FloatingButton";

    /**
     * Creates a new Mock component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockFloatingButton(SimpleEditor editor) {
        super(editor, TYPE, new Image(images.floatingButton()));
        OdeLog.log("MockFloatingButton " + editor.getStyleName() + "/" + editor.getFileId());

    }

    @Override
    public void onPropertyChange(String propertyName, String newValue){
        super.onPropertyChange(propertyName, newValue);
        refreshForm();

    }

}
