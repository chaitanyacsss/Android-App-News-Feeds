package com.home.chaitu.highs.dataFetch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Xml;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.home.chaitu.highs.model.NewsItem;
import com.home.chaitu.highs.ui.adapter.NewsAdapter;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaitu on 14-08-2016.
 */
public class FeedParser extends AsyncTask {

    private URL feedUrl;
    static final String RSS = "rss";
    static final String CHANNEL = "channel";
    static final String ITEM = "item";

    static final String PUB_DATE = "pubDate";
    static final String DESCRIPTION = "description";
    static final String LINK = "link";
    static final String TITLE = "title";
    List<NewsItem> news = new ArrayList<>();
    private ProgressBar bar;
    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout srl;

    public FeedParser(Context context, ProgressBar bar, RecyclerView recyclerView, SwipeRefreshLayout srl) {
        this.context = context;
        this.bar = bar;
        this.recyclerView = recyclerView;
        this.srl = srl;
    }

    @Override
    protected Object doInBackground(Object... feedUrlString) {
        try {
            feedUrl = new URL(feedUrlString[0].toString());
            news = parse();

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        if (news.isEmpty()) {
            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
        NewsAdapter mAdapter = new NewsAdapter(news, context);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        bar.setVisibility(View.GONE);
        srl.setRefreshing(false);
    }


    protected InputStream getInputStream() {
        InputStream inputStream = null;
        try {
            if (isOnline()) {
                URLConnection conn = feedUrl.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                inputStream = conn.getInputStream();
            }

        } catch (Exception e) {
        }
        return inputStream;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public List<NewsItem> parse() {
        final List<NewsItem> messages = new ArrayList<>();
        final NewsItem currentMessage = new NewsItem();
        RootElement root = new RootElement(RSS);
        Element itemList = root.getChild(CHANNEL);
        Element item = itemList.getChild(ITEM);
        item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
                currentMessage.setTitle(getTitle(body).get(0));
            }
        });
        item.getChild(LINK).setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
                try {
                    currentMessage.setURL(getStoryUrl(body));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
                currentMessage.setImageUrl(getImageUrl(body));
                currentMessage.setDescription(getDescription(body));
            }
        });
        item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
                currentMessage.setDate(body);
            }
        });
        item.setEndElementListener(new EndElementListener() {
            public void end() {
                messages.add(currentMessage.copy());
            }
        });
        try {
            if (this.getInputStream() != null) {
                Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
            }
        } catch (Exception e) {
            // throw new RuntimeException(e);
            //  Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
        }
        return messages;
    }

    private List<String> getTitle(String body) {
        int titleLength = body.lastIndexOf("- ");
        String title = null;
        String site = null;
        if (titleLength != -1) {
            title = body.substring(0, titleLength);
            site = body.substring(titleLength, body.length());
        }
        return Arrays.asList(title, site);
    }


    private String getDescription(String body) {
        List<String> descriptionList = Arrays.asList(StringEscapeUtils.unescapeXml(body).split("\\s*<br><font size=\"-1\">\\s*"));
        String description = descriptionList.get(2);
        int i = description.lastIndexOf(". ");
        if (i < (description.length() - 1) && i != -1) {
            description = description.substring(0, i + 1);
            description = descriptionCurationForYouTube(description);
            return description.toString();
        } else {
            return null;
        }
    }

    @NonNull
    private String descriptionCurationForYouTube(String description) {
        int j = description.lastIndexOf("License. Standard YouTube License.");
        if (j < (description.length() - 1) && j != -1) {
            int k = description.lastIndexOf(" Category.");
            if (k < (description.length() - 1) && k != -1) {
                description = description.substring(0, k);
            }
        }
        return description;
    }


    private URL getStoryUrl(String body) throws MalformedURLException, UnsupportedEncodingException {
        String storyUrl = null;
        if (!body.isEmpty()) {
            List<String> links = Arrays.asList(body.split("\\s*;\\s*"));
            if (!links.isEmpty()) {
                String[] link = links.get(0).split("&url=");
                if (link.length > 1) {
                    storyUrl = link[1];
                } else {
                    storyUrl = links.get(0);
                }
            }
        }
        return new URL(URLDecoder.decode(storyUrl, "UTF-8"));
    }


    private String getImageUrl(String body) {
        List<String> links = Arrays.asList(body.split("\\s*;\\s*"));
        String imageUrlString = null;
        for (String link : links) {
            if (link.contains("img src")) {
                Pattern pattern = Pattern.compile("src=\"(.*?)\"");
                Matcher matcher = pattern.matcher(link);
                if (matcher.find()) {
                    imageUrlString = matcher.group(1);
                } else {
                    imageUrlString = null;
                }
            }
        }
        return imageUrlString;
    }


}
