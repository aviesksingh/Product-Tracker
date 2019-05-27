package com.developer.rishabh.trackmyproduct;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.rishabh.trackmyproduct.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class itemDetailsActivity extends AppCompatActivity {

    ImageView image;
    TextView newPrice;
    TextView oldPrice;
    TextView title;
    TextView description;
    Button button;
    String url;
    String productId;

    //Database
    SQLiteDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        init();

        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        String companyName = intent.getStringExtra("companyName");
        if(companyName.equals("walmart"))
            getSupportActionBar().setTitle("Walmart Product");
        else
            getSupportActionBar().setTitle("Ebay Product");
        Double old = intent.getDoubleExtra("oldPrice",0.0);
        oldPrice.setText("Old Price: $"+old);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT url FROM myProducts WHERE productId='"+productId+"'", null);
        if (c.moveToFirst()) {
            do {
                url = c.getString(c.getColumnIndex("url"));
            } while (c.moveToNext());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        fetchData(productId,companyName);
    }

    public void init(){
        image = (ImageView) findViewById(R.id.imageOfproduct);
        newPrice = (TextView) findViewById(R.id.newPrice);
        oldPrice = (TextView) findViewById(R.id.oldPrice);
        title = (TextView) findViewById(R.id.name);
        description = (TextView) findViewById(R.id.description);
        button = (Button) findViewById(R.id.button);
        myDatabase = this.openOrCreateDatabase("TrackMyProducts", MODE_PRIVATE, null);
        description.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.tick:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void delete(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete the product!")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDatabase.execSQL("DELETE FROM myProducts WHERE productId='"+productId+"'");
                        Toast.makeText(getApplicationContext(), "Product Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.delete_menu,menu);
        return true;
    }

    public void fetchData(String productId, final String companyName){

        String madeURL = "";

        if(companyName.equals("walmart")){
            madeURL = makeWalmartURL(productId);
        }
        else{
            madeURL = makeEbayURL(productId);
        }

        StringRequest request =  new StringRequest(madeURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Code",response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if(companyName.equals("walmart")){
                        description.setText(jsonObject.getString("shortDescription"));
                        title.setText(jsonObject.getString("name"));
                        Picasso.get().load(jsonObject.getString("mediumImage")).into(image);
                        newPrice.setText("New Price: $"+jsonObject.getDouble("salePrice"));
                    }
                    else{
                        title.setText(jsonObject.getJSONObject("Item").getString("Title"));
                        description.setText(jsonObject.getJSONObject("Item").getString("Title"));
                        Picasso.get().load(jsonObject.getJSONObject("Item").getJSONArray("PictureURL").getString(0)).into(image);
                        newPrice.setText("New Price:$"+jsonObject.getJSONObject("Item").getJSONObject("ConvertedCurrentPrice").getDouble("Value") );
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(itemDetailsActivity.this, "Check the internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public String makeWalmartURL(String id){

        String madeURL = "https://api.walmartlabs.com/v1/items/";
        madeURL += id + "?format=json&";
        madeURL += "apiKey=" + "3cedjptrk6df8zwubyuha6ya";
        Log.i("info", madeURL);
        return madeURL;
    }

    public String makeEbayURL(String id){
        String madeURL = "http://open.api.ebay.com/shopping?";
        madeURL += "callname=GetSingleItem&";
        madeURL += "responseencoding=JSON&";
        madeURL += "appid=" + "RishabhA-TrackMyP-PRD-fdb3ea0bf-a69b7399&";
        madeURL += "siteid=0&";
        madeURL += "version=967&";
        madeURL += "ItemID=" + id ;

        return madeURL;
    }
}
