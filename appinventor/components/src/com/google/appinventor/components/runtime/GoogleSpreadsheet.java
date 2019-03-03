package com.google.appinventor.components.runtime;


import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import android.app.Activity;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.AsynchUtil;
//import com.google.appinventor.components.runtime.util.Utils;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

@DesignerComponent(version = YaVersion.SPREADSHEET_COMPONENT_VERSION,
        description = "Non-visible blah blah blah",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/spreadsheet.png")
@UsesLibraries(libraries = "cloudinary-android-1.12.0.jar, cloudinary-core-1.18.0.jar, gson-2.1.jar")
// https://mvnrepository.com/artifact/com.cloudinary/cloudinary-android/1.4.5
// https://mvnrepository.com/artifact/com.cloudinary/cloudinary-core/1.4.5

@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class GoogleSpreadsheet extends AndroidNonvisibleComponent implements Component {
    private String LOG_TAG = "GoogleSpreadsheet";
    private int maxRows = 5000;
    private int maxCols = 200;

    protected Activity activity;
    private String apiEndpoint = "";
    private Map<String, Integer> columnNames = new HashMap<String, Integer>();
    protected String[][] matrix = ((String[][]) Array.newInstance(String.class, new int[]{maxRows, maxCols}));
    private final Form form;
    protected String sheetName = "Your Sheet Name";
//    private String keyExtension = "Your key extension";
    private Context context;

    public GoogleSpreadsheet(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        this.form = container.$form();
        context = container.$context();
    }

    @DesignerProperty(defaultValue = "Enter the Cloudstitch API Endpoint", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty
    public void ApiEndpoint(String endpoint) {
        this.apiEndpoint = endpoint;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "")
    public String ApiEndpoint() {
        return this.apiEndpoint;
    }

    @DesignerProperty(defaultValue = "Enter Spreadsheet name", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty
    public void SheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Retrieves the Spreadsheet name")
    public String SheetName() {
        return this.sheetName;
    }

    @SimpleFunction(description = "For the given ApiEndpoint and Spreadsheet, retrieves all data from the spreadsheet.")
    public void GetSpreadsheetData() {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncGetData();
            }
        });
    }

    private void asyncGetData() {
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiEndpoint).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            bufferedReader.close();
            if (conn.getResponseCode() == 200) {
                JSONArray jsonArray = (JSONArray) new JSONObject(sb.toString().toString()).get(sheetName);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String[] keyValuePairs = jsonArray.get(i).toString().replace("{", "").replace("}", "").replace("\"", "").split(",");
                    if (i == 0) {
                        matrix = (String[][]) Array.newInstance(String.class, new int[]{jsonArray.length(), keyValuePairs.length});
                    }
                    for (int j = 0; j < keyValuePairs.length; j++) {
                        String[] entry = keyValuePairs[j].split(":");
                        matrix[i][j] = entry[1].trim();
                        if (i == 0) {
                            columnNames.put(entry[0].trim().toLowerCase(), j);
                        }
                    }
                }
                AfterAction(true, sb.toString(), "GetData");

            } else {
                sb = new StringBuilder(conn.getResponseMessage());
                AfterAction(false, sb.toString(), "GetData");
            }
        } catch (Exception e) {
            AfterAction(false, e.getLocalizedMessage(), "GetData");
        }
    }

    @SimpleFunction(description = "Retrieves all data for an entire column")
    public YailList GetColumnData(String columnName) {
        if (!columnNames.containsKey(columnName.toLowerCase())) {
            return YailList.makeEmptyList();
        }

        Integer columnNumber = columnNames.get(columnName.toLowerCase());
        Object[] columnList = new String[matrix.length];
        for (int j = 0; j < matrix.length; j++) {
            columnList[j] = matrix[j][columnNumber];
        }
        return YailList.makeList(columnList);
    }

    @SimpleFunction
    public YailList GetRowData(int rowNumber) {
        return YailList.makeList(matrix[rowNumber - 1]);
    }

    @SimpleFunction(description = "For the given columnName and rowNumber, retrieves the spreadsheet cell data")
    public String GetCellData(String columnName, int rowNumber) {

        return matrix[rowNumber - 1][columnNames.get(columnName.toLowerCase())];
    }

    @SimpleFunction(description = "Stores data into spreadsheet. dataToStore must be in json format. " +
            "Will trigger AfterAction")
    public void StoreData(final String dataToStore) {

        AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
                asyncStoreData(dataToStore);
            }
        });
    }

    private void asyncStoreData(String data) {
        String result = "";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiEndpoint + "/" + sheetName).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
            conn.getOutputStream().write(data.getBytes());
            result = conn.getResponseMessage();
            AfterAction(true, result, "StoreData");
        } catch (Exception e) {
//            result = e.getMessage();
//            Log.e(LOG_TAG, e.getMessage());
            AfterAction(false, e.getMessage(), "StoreData");
        }
    }

    @SimpleEvent(description = "Triggered after an actions such as storing data has occured. ")
    public void AfterAction(final boolean wasSuccess, final String message, final String action) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                EventDispatcher.dispatchEvent(GoogleSpreadsheet.this, "AfterAction", wasSuccess, message, action);
            }
        });
    }
}
