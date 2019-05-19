package com.developer.rishabh.trackmyproduct;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddURLActivity extends AppCompatActivity {

    EditText urlText;
    Button walmart;
    Button ebay;
    WebView webView;

    //Company Name
    String company = "";
    String finalURL = "";
    String dateAndTime = "";
    String productID = "";
    String price = "";

    //Database
    SQLiteDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);

        init();

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urlText.setText(url);
                super.onPageStarted(view, url, favicon);
            }
        });
        webView.loadUrl("https://www.google.com");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        walmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://www.walmart.com");
            }
        });

        ebay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://www.ebay.com");
            }
        });
    }

    public void init() {
        urlText = (EditText) findViewById(R.id.url);
        walmart = (Button) findViewById(R.id.walmart);
        ebay = (Button) findViewById(R.id.ebay);

        webView = (WebView) findViewById(R.id.webview);

        myDatabase = this.openOrCreateDatabase("TrackMyProducts", MODE_PRIVATE, null);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }
        else
            super.onBackPressed();
    }

    public boolean validateURL() {
        String url = urlText.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "URL can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("www")) {
            if (url.contains("www.walmart.com")) {
                if (url.contains("/ip/")) {
                    company = "walmart";
                    return true;
                } else {
                    Toast.makeText(this, "Enter the particular product link", Toast.LENGTH_SHORT).show();
                }
            }else if(url.contains("www.ebay.com") || url.contains("m.ebay.com")){
                if(url.contains("/itm/")){
                    company = "ebay";
                    return true;
                } else{
                    Toast.makeText(this, "Enter the particular product link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter either Walmart or Ebay website URL", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Enter the valid URL", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.tick:
                if (!validateURL()) break;
                finalURL = urlText.getText().toString().trim();
                InsertIntoDatabase();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tick_menu, menu);
        return true;

    }


    public void InsertIntoDatabase() {

        String modifiedURL = finalURL;
        modifiedURL = modifiedURL.replace("https://", "");
        modifiedURL = modifiedURL.replace("http://", "");

        if(company.equals("walmart")) {
            modifiedURL = modifiedURL.replace("www.walmart.com/ip/", "");
            String[] temp = modifiedURL.split("/");
            char[] temp1 = temp[1].toCharArray();
            for (int i = 0; i < temp1.length; i++) {
                if (temp1[i] < 48 || temp1[i] > 58) break;
                productID += temp1[i];
            }
        }
        else if(company.equals("ebay")){
            modifiedURL = modifiedURL.replace("www.ebay.com/itm/", "");
            modifiedURL = modifiedURL.replace("m.ebay.com/itm/","");
            String[] temp = modifiedURL.split("/");
            char[] temp1 = temp[1].toCharArray();
            for (int i = 0; i < temp1.length; i++) {
                if (temp1[i] < 48 || temp1[i] > 58) break;
                productID += temp1[i];
            }
        }

        if (!CheckDuplicate(productID)) return;

        String madeURL = "";

        if(company.equals("walmart")) {
            madeURL = "https://api.walmartlabs.com/v1/items/";
            madeURL += productID + "?format=json&";
            madeURL += "apiKey=" + "3cedjptrk6df8zwubyuha6ya";
        }
        else if(company.equals("ebay")){
            madeURL = "http://open.api.ebay.com/shopping?";
            madeURL += "callname=GetSingleItem&";
            madeURL += "responseencoding=JSON&";
            madeURL += "appid=" + "RishabhA-TrackMyP-PRD-fdb3ea0bf-a69b7399&";
            madeURL += "siteid=0&";
            madeURL += "version=967&";
            madeURL += "ItemID=" + productID ;
        }

        StringRequest request =  new StringRequest(madeURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Code",response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            dateAndTime = sdf.format(new Date());

            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
            String sql = "INSERT INTO myProducts VALUES (?,?,?,?,?)";
            SQLiteStatement statement = myDatabase.compileStatement(sql);
            statement.clearBindings();
            statement.bindString(1,finalURL);
            statement.bindString(2,productID);
            statement.bindString(3,company);
            if(company.equals("walmart"))
                statement.bindString(4,""+jsonObject.getDouble("salePrice"));
            else if(company.equals("ebay"))
                statement.bindString(4,""+jsonObject.getJSONObject("Item").getJSONObject("ConvertedCurrentPrice").getDouble("Value"));
            statement.bindString(5,dateAndTime);
            statement.executeInsert();

            Toast.makeText(getApplicationContext(), "Product Added", Toast.LENGTH_SHORT).show();
            finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddURLActivity.this, "Please enter the proper URL", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public boolean CheckDuplicate(String ProductID) {
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT productId,companyName FROM myProducts ", null);
        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex("productId"));
                String cName = c.getString(c.getColumnIndex("companyName"));
                if (ProductID.equals(name) && cName.equals(company)) {
                    Toast.makeText(this, "Product ID Already exsist. Try Again!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } while (c.moveToNext());
        }
        return true;
    }
}
