package com.pushpal.popularmoviesstage1.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.model.MovieTrailer;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private Context context;
    private final List<MovieTrailer> trailers;

    public TrailerAdapter(List<MovieTrailer> trailers) {
        this.trailers = trailers;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        return new TrailerViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_trailer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        final MovieTrailer movieCast = trailers.get(position);

        String imageURL = "http://img.youtube.com/vi/" + movieCast.getVideoKey() + "/mqdefault.jpg";
        Picasso.with(context)
                .load(imageURL)
                .placeholder(R.drawable.person)
                .into(holder.trailerImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("vnd.youtube://" + movieCast.getVideoKey())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_trailer)
        ImageView trailerImage;

        @BindView(R.id.card_view)
        CardView cardView;

        TrailerViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
