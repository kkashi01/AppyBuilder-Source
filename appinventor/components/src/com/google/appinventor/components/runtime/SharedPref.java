package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;
import org.json.JSONException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {
    public static final String TAG_LAST_UPDATE = "TAG_LAST_UPDATE";
    public static final String TAG_INIT_DELAY = "TAG_INIT_DELAY";
    public static final String TAG_MAX_DAYS = "TAG_MAX_DAYS";
    public static final String TAG_ACCOUNT = "TAG_ACCOUNT";
    public static final String DEFAULT_SHARE_PREF = "__PRIVATE__";

    private SharedPreferences sharedPreferences;
    private String dbName;

    public SharedPref(Context context) {
        this(context, DEFAULT_SHARE_PREF);
    }

    public SharedPref(Context context, String dbName) {
        sharedPreferences = context.getSharedPreferences(dbName, Context.MODE_PRIVATE);
        this.dbName = dbName;
    }

    public Object GetValue(final String tag, final Object valueIfTagNotThere)  {
        if (dbName == null || sharedPreferences == null) {
            //todo: return what
            throw new YailRuntimeError("Failed on database initialization", "Invalid database name");
        }

        try {
            String value = sharedPreferences.getString(tag, "");
            // If there's no entry with tag as a key then return the empty string.
            //    was  return (value.length() == 0) ? "" : JsonUtil.getObjectFromJson(value);
            return (value.length() == 0) ? valueIfTagNotThere : JsonUtil.getObjectFromJson(value);
        } catch (JSONException e) {
            throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Creation Error.");
        }
    }

    public List<String> GetTagsAndValues() {
        List<String> keyList = new ArrayList<String>();
        Map<String, ?> keyValues = sharedPreferences.getAll();
        keyList.addAll(keyValues.keySet());

        // searchkey: admob These are the admob tag and keys
        keyList.remove("TAG_ACCOUNT");
        keyList.remove("TAG_LAST_UPDATE");
        keyList.remove("TAG_INIT_DELAY");
        keyList.remove("TAG_MAX_DAYS");

        java.util.Collections.sort(keyList);
        List<String> resultList = new ArrayList<String>();
        for (String aKey : keyList) {
            resultList.add(aKey + "=" + keyValues.get(aKey));
        }
        return resultList;
    }

    public void StoreValue(final String tag, final Object valueToStore) {
        final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
        try {
            sharedPrefsEditor.putString(tag, JsonUtil.getJsonRepresentation(valueToStore));
            sharedPrefsEditor.commit();
        } catch (JSONException e) {
            throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
        }
    }
}
