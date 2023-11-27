package com.moutamid.quickdrop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.quickdrop.Model.Address;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.listener.ItemClickListener;

import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>{

    private Context mContext;
    private List<Address> addressList;
    private ItemClickListener itemClickListener;

    public LocationListAdapter(Context mContext, List<Address> addressList) {
        this.mContext = mContext;
        this.addressList = addressList;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_layout,parent,false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Address model = addressList.get(position);
        holder.nameTxt.setText(model.getName());
        holder.descriptionTxt.setText(model.getDescription());
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTxt;
        private TextView descriptionTxt;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt = itemView.findViewById(R.id.name);
            descriptionTxt = itemView.findViewById(R.id.description);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null){
                        itemClickListener.onItemClick(getAdapterPosition(),view);
                    }
                }
            });
        }
    }
    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
