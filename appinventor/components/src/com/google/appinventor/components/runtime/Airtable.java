/* Created by jerin jacob */

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@DesignerComponent(category = ComponentCategory.STORAGE,
        description = "xxxxxx Update OdeMessages",
        version = YaVersion.AIRTABLE_COMPONENT_VERSION,
        nonVisible = true,
        iconName = "images/airtable.png")

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject
public class Airtable extends AndroidNonvisibleComponent implements Component{
    private static final String LOG_TAG = "Airtable";

    private final Activity activity;
    //variables neeeded for getting data from airtable.
    private String apiKey="API-KEY";
    private String baseId="BASE-ID";
    private String tableName="Table 1";
    private String viewName = "Grid view";

    private String getAllData;
    private int getAllDataResponseCode;
    private String AIRTABLE_URL = "https://api.airtable.com/v0/";
    public Airtable(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        TableName(tableName);
    }

    //Designer properties

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String ApiKey() {
        return apiKey;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "API-KEY")
    @SimpleProperty(description = "Airtable API Key")
    public void ApiKey(String key) {
        this.apiKey = key;
    }


    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String BaseId() {
        return baseId;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "BASE-ID")
    @SimpleProperty(description = "Airtable Base ID")
    public void BaseId(String baseId) {
        this.baseId = baseId;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String TableName() {
        return tableName;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Table 1")
    @SimpleProperty(description = "Airtable Table Name")
    public void TableName(String tableName) {
        this.tableName = tableName;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String ViewName() {
        return viewName;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Grid view")
    @SimpleProperty(description = "Airtable Grid view name")
    public void ViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Methods
     **/
    //Get al data from airtable as json
    private void GetAllData() {
        String token = "Bearer " + apiKey;
        HttpURLConnection con=null;
        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?&view=" + viewName).replaceAll(" ", "%20"));
            con = (HttpURLConnection) url.openConnection();

            //Request header
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", "application/json");

            int responseCode = con.getResponseCode();
            Log.d(LOG_TAG, "\nSending 'GET' request to URL : " + url);
            Log.d(LOG_TAG, "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject obj = new JSONObject(response.toString());
            final JSONArray jsonarray = obj.getJSONArray("records");
            if (obj.has("offset")) {
                GetAllWithOffset(obj.getString("offset"), jsonarray);
            } else {
                getAllData = jsonarray.toString();
                getAllDataResponseCode = con.getResponseCode();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotAllRows(getAllDataResponseCode, getAllData, jsonarray.length());
                    }
                });
            }
        } catch (Exception ioe) {
            Log.e(LOG_TAG, "Error in GetAllData: " + ioe.getLocalizedMessage());
            //Toast.makeText(activity, "Unable", Toast.LENGTH_SHORT).show();
        } finally {
            if (con != null) con.disconnect();
        }
    }

    //GetAllData if have offset
    private void GetAllWithOffset(final String offset, JSONArray jsonArray) {
        String token = "Bearer " + apiKey;
        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?&view=" + viewName + "&offset=" + offset).replaceAll(" ", "%20"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //Request header
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", "application/json");

            final int responseCode = con.getResponseCode();
            Log.d(LOG_TAG, "\nSending 'GET' request to URL : " + url);
            Log.d(LOG_TAG, "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String output = response.toString();
            JSONObject obj = new JSONObject(output);
            JSONArray jsonarray = obj.getJSONArray("records");
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonArray.put(jsonarray.getJSONObject(i));
            }

            if (obj.has("offset")) {
                GetAllWithOffset(obj.getString("offset"), jsonArray);
            } else {
                getAllData = jsonArray.toString();
                getAllDataResponseCode = con.getResponseCode();
                final int totalRows = jsonArray.length();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotAllRows(getAllDataResponseCode, getAllData, totalRows);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("GetAllData", "Error in GetAllWithOffset. Error: " + e.getLocalizedMessage());
            // Toast.makeText(activity, "Unable", Toast.LENGTH_SHORT).show();
        }
    }

    //Get value of specified cell
    @SimpleFunction(description = "Use this block to get data for a Cell. Specify row number and column name")
    public void GetCell(final int rowNumber, final String columnName) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    Cell(rowNumber, columnName);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error getting cell data:" + e.getLocalizedMessage());
                    // t.printStackTrace();
                }
            }
        });
    }


    public void Cell(int rowNumber, String columnName) throws Exception {
        String token = "Bearer " + apiKey;

        URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?fields[]=" + columnName + "&view=" + viewName).replaceAll(" ", "%20"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //Request header
        con.setRequestProperty("Authorization", token);
        con.setRequestProperty("Content-Type", "application/json");

        final int responseCode = con.getResponseCode();
        Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
        Log.d(LOG_TAG,  "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String output = response.toString();
        JSONObject obj = new JSONObject(output);
        JSONArray jsonarray = obj.getJSONArray("records");
        if (obj.has("offset")) {
            GetCellWithOffset(obj.getString("offset"), jsonarray, columnName, rowNumber);
        } else {
            JSONObject jsonobject = jsonarray.getJSONObject(rowNumber - 1);
            final String rowId = jsonobject.getString("id");
            final String createdTime = jsonobject.getString("createdTime");
            final String cell = (((JSONObject) jsonarray.get(rowNumber - 1)).getJSONObject("fields").getString(columnName));

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GotCell(responseCode, cell, rowId, createdTime);
                }
            });
        }
    }

    //GetAllData if have offset
    private void GetCellWithOffset(final String offset, JSONArray jsonArray, String columnName, int rowNumber) {
        String token = "Bearer " + apiKey;
        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?fields[]=" + columnName + "&view=" + viewName + "&offset=" + offset).replaceAll(" ", "%20"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //Request header
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", "application/json");

            final int responseCode = con.getResponseCode();
            Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
            Log.d(LOG_TAG,  "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String output = response.toString();
            JSONObject obj = new JSONObject(output);
            JSONArray jsonarray = obj.getJSONArray("records");
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonArray.put(jsonarray.getJSONObject(i));
            }

            if (obj.has("offset")) {
                GetCellWithOffset(obj.getString("offset"), jsonArray, columnName, rowNumber);
            } else {
                JSONObject jsonobject = jsonArray.getJSONObject(rowNumber - 1);
                final String rowId = jsonobject.getString("id");
                final String createdTime = jsonobject.getString("createdTime");
                final String cell = (((JSONObject) jsonArray.get(rowNumber - 1)).getJSONObject("fields").getString(columnName));

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotCell(responseCode, cell, rowId, createdTime);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in GetCellWithOffset. Error: " + e.getLocalizedMessage());
            // Toast.makeText(activity, "Unable", Toast.LENGTH_SHORT).show();
        }
    }


    //Get value of column
    @SimpleFunction(description = "Use this block to get data for a column. Triggeres GotColumn block")
    public void GetColumn(final String columnName, final int maxRecord) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    Column(columnName, maxRecord);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in GetColumn:" + e.getLocalizedMessage());
                    // t.printStackTrace();
                }
            }
        });
    }

    public void Column(String columnName, int maxRecord) throws Exception {
        String token = "Bearer " + apiKey;
        final List<String> lrowId = new ArrayList<String>();
        final List<String> lcreatedTime = new ArrayList<String>();
        final List<String> lcolumn = new ArrayList<String>();

        URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?fields[]=" + columnName + "&maxRecords=" + maxRecord + "&view=" + viewName).replaceAll(" ", "%20"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //Request header
        con.setRequestProperty("Authorization", token);
        con.setRequestProperty("Content-Type", "application/json");

        final int responseCode = con.getResponseCode();
        Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
        Log.d(LOG_TAG,  "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String output = response.toString();
        JSONObject obj = new JSONObject(output);
        JSONArray jsonarray = obj.getJSONArray("records");

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String rowId = jsonobject.getString("id");
            String createdTime = jsonobject.getString("createdTime");
            String columnValue = jsonobject.getJSONObject("fields").getString(columnName);

            lrowId.add(rowId);
            lcreatedTime.add(createdTime);
            lcolumn.add(columnValue);
        }
        //if contains offset
        if (obj.has("offset")) {
            ColumnWithOffset(lrowId, lcreatedTime, lcolumn, columnName, maxRecord, obj.getString("offset"));
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GotColumn(responseCode, lcolumn, lrowId, lcreatedTime);
                }
            });
        }
    }

    //return column if contains offset
    private void ColumnWithOffset(final List<String> rowIds, final List<String> createdTimes, final List<String> columnValues,
                                  String columnName, int maxRecord, String offset) throws Exception {
        String token = "Bearer " + apiKey;
        URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?fields[]=" + columnName + "&maxRecords=" + maxRecord + "&view=" + viewName + "&offset=" + offset).replaceAll(" ", "%20"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //Request header
        con.setRequestProperty("Authorization", token);
        con.setRequestProperty("Content-Type", "application/json");

        final int responseCode = con.getResponseCode();
        Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
        Log.d(LOG_TAG,  "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String output = response.toString();
        JSONObject obj = new JSONObject(output);
        JSONArray jsonarray = obj.getJSONArray("records");

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String rowId = jsonobject.getString("id");
            String createdTime = jsonobject.getString("createdTime");
            String columnValue = jsonobject.getJSONObject("fields").getString(columnName);

            rowIds.add(rowId);
            createdTimes.add(createdTime);
            columnValues.add(columnValue);
        }
        if (obj.has("offset")) {
            ColumnWithOffset(rowIds, createdTimes, columnValues, columnName, maxRecord, obj.getString("offset"));
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GotColumn(responseCode, columnValues, rowIds, createdTimes);
                }
            });
        }
    }


    //Get list of values from row
    @SimpleFunction(description = "Retrieves data for a specic row. Triggeres GotRow block")
    public void GetRow(final int rowNumber) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    getRow(rowNumber);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in GetRow:" + e.getLocalizedMessage());
                    //t.printStackTrace();
                }
            }
        });
    }


    public void getRow(int rowNumber) {
        String token = "Bearer " + apiKey;
        final List<String> row = new ArrayList<String>();

        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?&view=" + viewName).replaceAll(" ", "%20"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //Request header
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", "application/json");

            final int responseCode = con.getResponseCode();
            Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
            Log.d(LOG_TAG,  "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String output = response.toString();
            JSONObject obj = new JSONObject(output);
            JSONArray jsonarray = obj.getJSONArray("records");
            if (obj.has("offset")) {
                RowWithOffset(rowNumber, obj.getString("offset"), row, jsonarray);
            } else {
                JSONObject jsonobject = jsonarray.getJSONObject(rowNumber - 1).getJSONObject("fields");
                JSONArray keys = jsonobject.names();
                for (int i = 0; i < keys.length(); ++i) {
                    String key = keys.getString(i); // Here's your key
                    String value = jsonobject.getString(key); // Here's your value
                    row.add(value);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotRow(responseCode, row);
                    }
                });
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in getRow. Error: " + e.getLocalizedMessage());
            // Toast.makeText(activity, "Unable", Toast.LENGTH_SHORT).show();
        }
    }

    //load more rows if have offset
    private void RowWithOffset(final int rowNum, final String offset, final List<String> row, JSONArray jsonArray) {
        String token = "Bearer " + apiKey;
        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "?&view=" + viewName + "&offset=" + offset).replaceAll(" ", "%20"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //Request header
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", "application/json");

            final int responseCode = con.getResponseCode();
            Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
            Log.d(LOG_TAG,  "Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String output = response.toString();
            JSONObject obj = new JSONObject(output);
            JSONArray jsonarray = obj.getJSONArray("records");
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonArray.put(jsonarray.getJSONObject(i));
            }

            if (obj.has("offset")) {
                RowWithOffset(rowNum, obj.getString("offset"), row, jsonArray);
            } else {
                JSONObject jsonobject = jsonArray.getJSONObject(rowNum - 1).getJSONObject("fields");
                JSONArray keys = jsonobject.names();
                for (int i = 0; i < keys.length(); ++i) {
                    String key = keys.getString(i); // Here's your key
                    String value = jsonobject.getString(key); // Here's your value
                    row.add(value);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GotRow(responseCode, row);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in RowWithOffset. Error: " + e.getLocalizedMessage());
            // Toast.makeText(activity, "Unable", Toast.LENGTH_SHORT).show();
        }
    }


    //Update value of cell
    @SimpleFunction(description = "Can be used to update data for a specific cell using row number and column name. Triggeres GotCell block")
    public void SetCell(final int rowNumber, final String columnName, final String value) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    setCell(rowNumber, columnName, value);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in SetCell:" + e.getLocalizedMessage());
                    // Toast.makeText(activity, "Unable to update", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setCell(int rowNumber, String columnName, String value) throws Exception {
        GetAllData();
        String token = "Bearer " + apiKey;

        JSONArray jsonarray = new JSONArray(getAllData);
        JSONObject jsonobject = jsonarray.getJSONObject(rowNumber - 1);
        String rowId = jsonobject.getString("id");

        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "/" + rowId).replaceAll(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String parameters = "{\"fields\": {\"" + columnName + "\": \"" + value + "\"},\"typecast\": true}";

            conn.setRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Authorization", token);
            conn.setRequestMethod("PATCH");


            conn.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            writer.writeBytes(parameters);
            writer.flush();
            if (writer != null)
                writer.close();

            if (conn.getResponseCode() != 200) {
                Log.d(LOG_TAG,  conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            final int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CellChanged(responseCode);
                    }
                });


            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in setCell:" + e.getLocalizedMessage());
            // e.printStackTrace();
        }
    }

    @SimpleFunction(description = "Deletes data at a specific row number. Triggeres DeletedRowByNumber block")
    public void DeleteRowNum(final int rowNumber) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteRowNum(rowNumber);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in DeleteRowNum:" + e.getLocalizedMessage());
                    //t.printStackTrace();
                }
            }
        });
    }


    private void deleteRowNum(int rowNumber) throws Exception {
        GetAllData();
        String token = "Bearer " + apiKey;

        JSONArray jsonarray = new JSONArray(getAllData);
        JSONObject jsonobject = jsonarray.getJSONObject(rowNumber - 1);
        String rowId = jsonobject.getString("id");

        URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "/" + rowId).replaceAll(" ", "%20"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //Request header
        con.setRequestProperty("Authorization", token);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("DELETE");

        final int responseCode = con.getResponseCode();
        Log.d(LOG_TAG,  "\nSending 'GET' request to URL : " + url);
        Log.d(LOG_TAG,  "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String output = response.toString();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeletedRowByNumber(responseCode);
            }
        });

    }

    @SimpleFunction(description = "Add list of Column Names and List of Values. Triggers RowCreated block")
    public void CreateRow(final YailList columnNames, final YailList values) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    createRow(columnNames, values);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in CreateRow:" + e.getLocalizedMessage());
                    // t.printStackTrace();
                }
            }
        });
    }


    public void createRow(YailList columnNames, YailList values) throws Exception {
        String token = "Bearer " + apiKey;
        String[] colums = columnNames.toStringArray();
        String[] arrayValues = values.toStringArray();


        JSONObject obj = new JSONObject();
        for (int i = 0; i < colums.length; i++) {
            obj.put(colums[i], arrayValues[i]);
        }
        String jsonRecord = obj.toString();

        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName).replaceAll(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String parameters = "{\"fields\":" + jsonRecord + ",\"typecast\": true}";

            conn.setRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Authorization", token);
            conn.setRequestMethod("POST");


            conn.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            writer.writeBytes(parameters);
            writer.flush();
            if (writer != null)
                writer.close();

            if (conn.getResponseCode() != 200) {
                Log.d(LOG_TAG,  conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            final int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RowCreated(responseCode);
                    }
                });


            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in createRow:" + e.getLocalizedMessage());
            //e.printStackTrace();
        }
    }


    //Update row values by number
    @SimpleFunction(description = "Use this block to update data in a row. " +
            "columnNames is list of column names and values is a list of value for each column name. " +
            "Make sure lists are synchronized")
    public void UpdateRowByNum(final int rowNumber, final YailList columnNames, final YailList values) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    updateRowByNum(rowNumber, columnNames, values);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in UpdateRowByNum:" + e.getLocalizedMessage());
                    // t.printStackTrace();
                }
            }
        });
    }


    private void updateRowByNum(int rowNumber, YailList columnNames, YailList values) throws Exception {
        GetAllData();

        String token = "Bearer " + apiKey;
        String[] colums = columnNames.toStringArray();
        String[] arrayValues = values.toStringArray();

        JSONArray jsonarray = new JSONArray(getAllData);
        JSONObject jsonobject = jsonarray.getJSONObject(rowNumber - 1);
        String rowId = jsonobject.getString("id");

        //createing json string
        JSONObject fields = new JSONObject();
        for (int i = 0; i < colums.length; i++) {
            fields.put(colums[i], arrayValues[i]);
        }
        String jsonRecord = fields.toString();

        try {
            URL url = new URL((AIRTABLE_URL + baseId + "/" + tableName + "/" + rowId).replaceAll(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String parameters = "{\"fields\":" + jsonRecord + ",\"typecast\": true}";

            conn.setRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Authorization", token);
            conn.setRequestMethod("PUT");


            conn.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            writer.writeBytes(parameters);
            writer.flush();
            if (writer != null)
                writer.close();

            if (conn.getResponseCode() != 200) {
                Log.d(LOG_TAG,  conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                final int responseCode = conn.getResponseCode();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RowUpdated(responseCode);
                    }
                });

            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in updateRowByNum:" + e.getLocalizedMessage());
            // e.printStackTrace();
        }
    }

    //Get rows
    @SimpleFunction(description = "Block for retrieving all data. Will trigger GetAllData block. Will trigger GotAllRows block")
    public void GetAllRows() {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    GetAllData();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in GetAllRows:" + e.getLocalizedMessage());
                    // t.printStackTrace();
                }
            }
        });
    }


    //Events
    @SimpleEvent(description = "Triggered after requesting to get data for a Cell")
    public void GotCell(int responseCode, String value, String rowId, String createdTime) {
        EventDispatcher.dispatchEvent(this, "GotCell", responseCode, value, rowId, createdTime);
    }

    @SimpleEvent(description = "Triggered after requesting to get data for a specific Column")
    public void GotColumn(int responseCode, List<String> values, List<String> rowIds, List<String> createdTimes) {
        EventDispatcher.dispatchEvent(this, "GotColumn", responseCode, values, rowIds, createdTimes);
    }

    @SimpleEvent(description = "Triggered after requesting to get data for a specific row")
    public void GotRow(int responseCode, List<String> values) {
        EventDispatcher.dispatchEvent(this, "GotRow", responseCode, values);
    }

    @SimpleEvent(description = "Triggered after requesting to ")
    public void CellChanged(int responseCode) {
        EventDispatcher.dispatchEvent(this, "CellChanged", responseCode);
    }

    @SimpleEvent(description = "Triggered after a row is deleted from spreadsheet")
    public void DeletedRowByNumber(int responseCode) {
        EventDispatcher.dispatchEvent(this, "DeletedRowByNumber", responseCode);
    }

    @SimpleEvent(description = "Triggered after a row is created in the spreadsheet")
    public void RowCreated(int responseCode) {
        EventDispatcher.dispatchEvent(this, "RowCreated", responseCode);
    }

    @SimpleEvent(description = "Triggered after spreadsheet rows are updated")
    public void RowUpdated(int responseCode) {
        EventDispatcher.dispatchEvent(this, "RowUpdated", responseCode);
    }

    @SimpleEvent(description = "Triggered after requesting to get all rows")
    public void GotAllRows(int responseCode, String responseContent, int totalRows) {
        EventDispatcher.dispatchEvent(this, "GotAllRows", responseCode, responseContent, totalRows);
    }
}
