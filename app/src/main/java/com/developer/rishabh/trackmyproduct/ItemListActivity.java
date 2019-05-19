package com.developer.rishabh.trackmyproduct;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemListActivity extends AppCompatActivity implements android.support.v7.widget.SearchView.OnQueryTextListener {

    //List of Folders
    ArrayList<Products> productsArrayList;
    CustomProductAdapter adapter = null;

    //Database
    SQLiteDatabase myDatabase;

    //Variables
    String finalMadeURL = "";
    ArrayList<Double> oldPrice = new ArrayList<Double>();
    ArrayList<String> productId = new ArrayList<String>();
    ArrayList<String> companyName = new ArrayList<String>();

    boolean isCreate = true;

    //Progress Bar
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        GridView gridView = (GridView) findViewById(R.id.gridview);
        myDatabase = this.openOrCreateDatabase("TrackMyProducts", MODE_PRIVATE, null);
        productsArrayList = new ArrayList<Products>();
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemListActivity.this,AddURLActivity.class);
                startActivity(intent);
            }
        });

        adapter = new CustomProductAdapter(this, R.layout.custom_single_product, productsArrayList);
        gridView.setAdapter(adapter);

        fetchDetails();
        callData();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ItemListActivity.this,itemDetailsActivity.class);
                intent.putExtra("productId",productsArrayList.get(position).getProductId());
                intent.putExtra("companyName",productsArrayList.get(position).getCompanyName());
                intent.putExtra("oldPrice",productsArrayList.get(position).getOldPrice());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.tick:
                //progressDialog.show();
                productsArrayList.clear();
                productId.clear();
                companyName.clear();
                oldPrice.clear();
                fetchDetails();
                callData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.refresh_search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;

    }

    public void fetchDetails(){

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
        Cursor c = myDatabase.rawQuery("SELECT * FROM myProducts ",null);
        if(c.moveToFirst()) {
            do{
                productId.add( c.getString(c.getColumnIndex("productId")));
                companyName.add(c.getString(c.getColumnIndex("companyName")));
                oldPrice.add(Double.parseDouble(c.getString(c.getColumnIndex("price"))));
            }while(c.moveToNext());
        }
    }

    public void callData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        for(int i = 0; i < productId.size(); i++){
            //progressDialog.show();
            if(companyName.get(i).equals("walmart"))
                finalMadeURL = makeWalmartURL(productId.get(i));
            else
                finalMadeURL = makeEbayURL(productId.get(i));
            StringRequest request =  new StringRequest(Request.Method.GET,finalMadeURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String company = "";
                        Double old = 0.0;
                        String itemId = "";

                        try{
                            itemId = ""+jsonObject.getInt("itemId");
                        }
                        catch (Exception e){
                            itemId = ""+jsonObject.getJSONObject("Item").getString("ItemID");
                        }

                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
                        Cursor c = myDatabase.rawQuery("SELECT * FROM myProducts WHERE productId='"+itemId+"'",null);
                        if(c.moveToFirst()) {
                            do{
                                company = c.getString(c.getColumnIndex("companyName"));
                                old = Double.parseDouble(c.getString(c.getColumnIndex("price")));
                            }while(c.moveToNext());
                        }

                        Double newPrice;
                        String title = "";
                        String data = "";

                        if(company.equals("walmart")){
                            newPrice = jsonObject.getDouble("salePrice");
                            title = jsonObject.getString("name");
                            data = jsonObject.getString("thumbnailImage");
                                                    }
                        else{
                            newPrice = jsonObject.getJSONObject("Item").getJSONObject("ConvertedCurrentPrice").getDouble("Value");
                            title = jsonObject.getJSONObject("Item").getString("Title");
                            data = jsonObject.getJSONObject("Item").getString("GalleryURL");
                        }

                        Products p = new Products(itemId,data,old,newPrice,title,company);
                        productsArrayList.add(p);
                        //Picasso.get().load(imageURL).into();

                        adapter.notifyDataSetChanged();
                        //progressDialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ItemListActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if(!isCreate) {
            int count = 0;
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS myProducts (url VARCHAR, productId VARCHAR, companyName VARCHAR, price VARCHAR, date VARCHAR)");
            Cursor c = myDatabase.rawQuery("SELECT * FROM myProducts ", null);
            count = c.getCount();

            if (count != productsArrayList.size()) {
                //progressDialog.show();
                productsArrayList.clear();
                productId.clear();
                companyName.clear();
                oldPrice.clear();
                fetchDetails();
                callData();
            }
        }
        else
            isCreate = false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }
}
