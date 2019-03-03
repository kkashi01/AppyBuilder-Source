package com.google.appinventor.client.settings.user;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

/**
 * BlocksSettings encapsulates user settings related to the Blockly workspace.
 * @author ewpatton
 *
 */
public final class BlocksSettings extends Settings {

  /**
   * Create a new BlocksSettings instance with the default values.
   * @param user
   */
  public BlocksSettings(UserInfoProvider user) {
    super(SettingsConstants.BLOCKS_SETTINGS);

    // Making grids show by default
    String showGrids = "true";
    addProperty(new EditableProperty(this, SettingsConstants.GRID_ENABLED,
            showGrids, EditableProperty.TYPE_INVISIBLE));
    addProperty(new EditableProperty(this, SettingsConstants.SNAP_ENABLED,
        "false", EditableProperty.TYPE_INVISIBLE));
  }
}
