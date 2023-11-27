package com.moutamid.quickdrop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.quickdrop.Model.Reviews;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>{

    private Context mContext;
    private List<Reviews> reviewsList;

    public ReviewListAdapter(Context mContext, List<Reviews> reviewsList) {
        this.mContext = mContext;
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_custom_layout,parent,false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Reviews model = reviewsList.get(position);
        holder.feedbackTxt.setText(model.getFeedback());
        holder.rateTxt.setText(String.valueOf(model.getRating()));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(model.getUserId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    holder.nameTxt.setText(user.getFullname());
                    if (user.getImageUrl().equals("")){
                        Picasso.with(mContext)
                                .load(R.drawable.profile)
                                .into(holder.profileImg);
                    }else {
                        Picasso.with(mContext)
                                .load(user.getImageUrl())
                                .into(holder.profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTxt,feedbackTxt,rateTxt;
        private CircleImageView profileImg;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt = itemView.findViewById(R.id.name);
            feedbackTxt = itemView.findViewById(R.id.feedback);
            rateTxt = itemView.findViewById(R.id.rate);
            profileImg = itemView.findViewById(R.id.profile);
        }
    }

}
