package com.google.appinventor.components.runtime;

import android.view.View;
import android.widget.Space;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Just a simple filler / spacer class to add fills between components
 *
 */
@DesignerComponent(version = YaVersion.SPACER_COMPONENT_VERSION,
        description = "xxxxxxxxxx add into OdeMessages",
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class Spacer extends AndroidViewComponent implements Component {
    private final static String LOG_TAG = "Spacer";

    private final Space spacer;


    public Spacer(ComponentContainer container) {
        super(container);

        spacer = new Space(container.$context());
        container.$add(this);
         BackgroundColor(Component.COLOR_LTGRAY);
         Height(10);
    }

    // This was added, but causes build issue if color is changed. Keeping method, and just forcing lightGray.
    // MockSpacer sets it NOT VISIBLE
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
//    @SimpleProperty(description = "Background color of this component. This is just for UI. For apk it won't be visible")
    public void BackgroundColor(int argb) {
        // disable changing color. Causes build issue.
        getView().setBackgroundColor(argb);
    }

    @Override
    public View getView() {
        return spacer;
    }

}
