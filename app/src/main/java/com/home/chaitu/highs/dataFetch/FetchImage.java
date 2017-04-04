package com.home.chaitu.highs.dataFetch;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.home.chaitu.highs.model.NewsItem;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by chaitu on 14-08-2016.
 */
public class FetchImage extends AsyncTask {
    public static final String HTTP = "http:";
    private Context context;
    private ImageView img;

    public FetchImage(Context context) {
        this.context = context;
    }


    @Override
    protected Object doInBackground(Object... objects) {
        img = (ImageView) objects[0];
        NewsItem item = (NewsItem) objects[1];
        String src = item.getImageUrl();
        return src;
    }

    @Override
    protected void onPostExecute(Object src) {
        try {
            if (src != null) {
                //Log.d("onPostExecute: ",src.toString());
                Picasso.with(context).load(String.valueOf(new URI(HTTP + src.toString()))).resize(200, 200).into(img);
            } else {
                //Picasso.with(context).load().resize(200, 200).into(img);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
