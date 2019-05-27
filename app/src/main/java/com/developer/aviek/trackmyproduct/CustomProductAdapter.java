package com.developer.rishabh.trackmyproduct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomProductAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater inflater;
    private Context context;
    private  int layout;
    ArrayList<Products> productList;
    ArrayList<Products> filterList;
    CustomFilter filter;

    public CustomProductAdapter(Context context,int layout, ArrayList<Products> productList){
        this.productList = productList;
        this.context = context;
        this.filterList = productList;
        this.layout = layout;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView title;
        TextView oldPrice;
        TextView newPrice;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        View row = convertView;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(row == null) {
            row = inflater.inflate(layout, null);

            holder.title = row.findViewById(R.id.productName);
            holder.imageView = row.findViewById(R.id.imageView);
            holder.oldPrice = row.findViewById(R.id.oldPrice);
            holder.newPrice = row.findViewById(R.id.newPrice);
            row.setTag(holder);
        }
        else{
            holder = (ViewHolder) row.getTag();
        }

        Products f = productList.get(position);

        holder.title.setText(f.getTitle());

        Picasso.get().load(f.getImage()).into(holder.imageView);

        holder.oldPrice.setText("Old: $"+f.getOldPrice().toString());
        holder.newPrice.setText("New: $"+f.getNewPrice().toString());

        return row;
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CustomFilter();
        }
        return filter;
    }

    class CustomFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0 ){
                constraint = constraint.toString().toUpperCase();
                ArrayList<Products> filters = new ArrayList<Products>();

                for(int i = 0 ; i < filterList.size(); i++){
                    if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                        Products f = new Products(filterList.get(i).getProductId(),filterList.get(i).getImage(),
                                filterList.get(i).getOldPrice(),filterList.get(i).getNewPrice(),
                                filterList.get(i).getTitle(),filterList.get(i).getCompanyName());
                        filters.add(f);
                    }
                }
                results.count = filters.size();
                results.values = filters;
            }
            else{
                results.count = filterList.size();
                results.values = filterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList = (ArrayList<Products>) results.values;
            notifyDataSetChanged();
        }
    }
}
