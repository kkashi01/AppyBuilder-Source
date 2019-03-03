package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


public class MockPushNotification extends MockNonVisibleComponent {

    public static final String TYPE = "PushNotification";
    private static final String PROPERTY_NAME_PROJECT_BUCKET = "TopicPath";
    private static boolean warningGiven = false; // Whether or not we have given experimental warning

    private boolean persistToken = false;
    private String vCode="";
	private String userEmail="";
    private String devBucket="_topic_";
    private String projectName = "";

    /**
     * Creates a new instance of a non-visible component whose icon is
     * loaded dynamically (not part of the icon image bundle)
     *
     * @param editor
     * @param type
     * @param iconImage
     */
    public MockPushNotification(SimpleEditor editor, String type, Image iconImage) {
        super(editor, type, iconImage);
    }

    /**
     * Initializes the "ProjectBucket", "DeveloperBucket", "FirebaseToken"
     * properties dynamically.
     *
     * @param widget the iconImage for the MockFirebaseDB
     */
    @Override
    public final void initComponent(Widget widget) {
        super.initComponent(widget);

        // Firebase paths must not contain '.', '#', '$', '[', or ']'
        userEmail = Ode.getInstance().getUser().getUserEmail().replace(".", ":") + "";

		userEmail = Ode.getInstance().getUser().getUserEmail();
		int idx = userEmail.indexOf("@");
		userEmail = userEmail.substring(0,idx);
		
        // Since I'm allowing user to do his own notification, I'm just resetting the devBucket to ""
        devBucket="_topic_";
        DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
        projectName = "";
        if (currentProject != null) {
            projectName = currentProject.name;
        }

        changeProperty(PROPERTY_NAME_PROJECT_BUCKET, devBucket + "/" + userEmail + "/" + projectName);
    }

    /**
     * Called when the component is dropped in the Designer window
     * we give a warning that firebase is still experimental.
     */
    @Override
    public void onCreateFromPalette() {
        if (!warningGiven) {
            warningGiven = true;
            Ode.getInstance().warningDialog(MESSAGES.warningDialogTitle(),
                    MESSAGES.pushNotificationExperimentalWarning(), MESSAGES.okButton());
        }
    }

}
