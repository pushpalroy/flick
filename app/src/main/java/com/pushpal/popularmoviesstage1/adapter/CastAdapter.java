package com.pushpal.popularmoviesstage1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.model.MovieCast;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private Context context;
    private final List<MovieCast> casts;

    public CastAdapter(List<MovieCast> casts) {
        this.casts = casts;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        return new CastViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_cast, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        MovieCast movieCast = casts.get(position);
        holder.castName.setText(movieCast.getActorName());
        holder.characterName.setText((movieCast.getCharacterName()));
        String imageURL = Constants.IMAGE_BASE_URL
                + Constants.IMAGE_SIZE_185
                + movieCast.getProfileImagePath();
        Picasso.with(context)
                .load(imageURL)
                .placeholder(R.drawable.person)
                .into(holder.castImage);
    }

    @Override
    public int getItemCount() {
        return casts.size();
    }

    class CastViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_cast_image)
        ImageView castImage;

        @BindView(R.id.tv_cast_name)
        TextView castName;

        @BindView(R.id.tv_character_name)
        TextView characterName;

        CastViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
