package com.home.chaitu.highs.ui.adapter;

import android.content.Context;
import android.opengl.Visibility;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.home.chaitu.highs.R;
import com.home.chaitu.highs.dataFetch.FetchImage;
import com.home.chaitu.highs.model.NewsItem;
import com.home.chaitu.highs.ui.NewsActivity;

import java.util.List;

/**
 * Created by chaitu on 14-08-2016.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    private List<NewsItem> newsList;
    private Context context;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date, description, url, extendedDesc;
        public ImageView img;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
            date = (TextView) view.findViewById(R.id.date);
            img = (ImageView) view.findViewById(R.id.imageUrl);
            url = (TextView) view.findViewById(R.id.url);
            extendedDesc = (TextView) view.findViewById(R.id.extendedDesc);
        }
    }


    public NewsAdapter(List<NewsItem> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.title.setText(newsItem.getTitle());
        holder.date.setText(newsItem.getDate());
        holder.description.setText(newsItem.getDescription());
        //Log.d("onBindViewHolder: ", String.valueOf(newsItem.getUrl()));
        holder.url.setText(newsItem.getUrl().toString());
        new FetchImage(context).execute(holder.img, newsItem);
    }


    @Override
    public int getItemCount() {
        return newsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}