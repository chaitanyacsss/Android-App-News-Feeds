package com.home.chaitu.highs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.home.chaitu.highs.R;
import com.home.chaitu.highs.dataFetch.ContentExtractor;
import com.home.chaitu.highs.dataFetch.FeedParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* TODO:
* 2) data caching!!
* 4) embellish webView
* 5) Curate articles
* 7) group similar articles
* 8) varied/multiple sources
* 9) side swipe for category shift
* 10)url Updates fetching - cron like job
* 11)integrate webView to card
* 12)Summarize article!*/
public class NewsActivity extends AppCompatActivity
        implements ActivityBase {

    public static String FEED_URL_FINAL;
    //static String FEED_URL_STRING = "https://news.google.co.in/news?cf=all&hl=en&pz=1&ned=in";
    static String FEED_URL_STRING = "https://news.google.com/news/rss/headlines/section/topic/";
    static String NO_TOPIC_FEED = "https://news.google.com/news/rss/?ned=in&hl=en-IN&gl=IN";
    //static String AND_TOPIC = "&topic=";
    public static final String OUTPUT_RSS = "?ned=in&hl=en-IN&gl=IN";
    static String topic = null;
    private RecyclerView recyclerView;
    static String feedUrlString;
    private ProgressBar bar;
    private String TAG = "logging at";
    private SwipeRefreshLayout srl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //log.d(TAG, "onCreate: 1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        srl = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        srl.setOnRefreshListener(this);
        setFeedUrlString();
        //log.d(TAG, "onCreate: 2");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        touchListenerInit();
        Log.i("", "done");
    }

    private void setFeedUrlString() {
        if (FEED_URL_FINAL == null) {
            //FEED_URL_FINAL = FEED_URL_STRING + AND_TOPIC + topic + OUTPUT_RSS;
            FEED_URL_FINAL = FEED_URL_STRING + topic + OUTPUT_RSS;
            if(topic == null){
                FEED_URL_FINAL = NO_TOPIC_FEED;
                Toast.makeText(this,"Headlines",Toast.LENGTH_LONG);
            }
        }
        this.feedUrlString = FEED_URL_FINAL;
        //log.d(TAG, "setFeedUrlString: " + FEED_URL_FINAL);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        bar.setVisibility(View.VISIBLE);
        //Log.d(TAG, "onPostCreate: " + feedUrlString);
        UpdateNews();
        // buttonClick();
    }

    private void touchListenerInit() {
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

            });

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    final TextView cardUrlView = (TextView) child.findViewById(R.id.url);
                    final TextView extendedDesc = (TextView) child.findViewById(R.id.extendedDesc);
                    final TextView cardDescription = (TextView) child.findViewById(R.id.description);
                    final TextView cardTitle = (TextView) child.findViewById(R.id.title);
                    final TextView cardDate = (TextView) child.findViewById(R.id.date);
                    final ImageView cardImage = (ImageView) child.findViewById(R.id.imageUrl);
                    final DotProgressBar dotProgressBar = (DotProgressBar) child.findViewById(R.id.dot_progress_bar);
                    toggleViewVisibility(cardUrlView);
                    cardUrlView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleViewVisibility(cardUrlView);
                            //callContentExtraction(extendedDesc, cardUrlView, cardDescription, cardTitle, cardDate, cardImage, dotProgressBar);
                            callWebViewActivity(cardUrlView);
                        }
                    });
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    private void callContentExtraction(TextView extendedDesc, TextView cardUrlView, TextView cardDescription, TextView cardTitle, TextView cardDate, ImageView cardImage, DotProgressBar dotProgressBar) {
        String pattern = WebViewActivity.YOUTUBE_URL_REGEX;
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(cardUrlView.getText());
        if (matcher.find()) {
            callWebViewActivity(cardUrlView);
        } else {
            toggleViewVisibility(extendedDesc);
            toggleViewVisibility(cardDescription);
            toggleViewVisibility(cardTitle);
            toggleViewVisibility(cardDate);
            toggleImageViewVisibility(cardImage);
            dotProgressBar.setStartColor(R.color.app_primary_light);
            dotProgressBar.setEndColor(R.color.app_primary_light);
            if (dotProgressBar.getVisibility() == View.GONE || dotProgressBar.getVisibility() == View.INVISIBLE) {
                dotProgressBar.setVisibility(View.VISIBLE);
            } else if (cardTitle.getVisibility() == View.VISIBLE && dotProgressBar.getVisibility() == View.VISIBLE) {
                dotProgressBar.setVisibility(View.GONE);
            }
            new ContentExtractor(extendedDesc, dotProgressBar, cardTitle.getText().toString()).execute(cardUrlView.getText());
        }
    }


    private void callWebViewActivity(TextView urlView) {
        Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(WebViewActivity.WEB_VIEW_URL, urlView.getText());
        getApplication().startActivity(i);
    }

    public static void toggleViewVisibility(TextView v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void toggleImageViewVisibility(ImageView v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void UpdateNews() {
        srl.setRefreshing(true);
        new FeedParser(getApplicationContext(), bar, recyclerView, srl).execute(feedUrlString);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, "No configurable settings", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.topstories) {
            topic = null;
            //log.d(TAG, "onNavigationItemSelected: ");
        } else if (id == R.id.sports) {
            topic = "SPORTS/Sports";
        } else if (id == R.id.india) {
            topic = "NATION.en_in/India";
        } else if (id == R.id.world) {
            topic = "WORLD.en_in/World";
        } else if (id == R.id.entertainment) {
            topic = "ENTERTAINMENT.en_in/Entertainment";
        } else if (id == R.id.business) {
            topic = "BUSINESS.en_in/Business";
        } else if (id == R.id.tech) {
            topic = "TECHNOLOGY.en_in/Technology";
        } else if (id == R.id.science) {
            topic = "SCIENCE.en_in/Science";
        } else if (id == R.id.health) {
            topic = "HEALTH.en_in/Health";
        }
        //log.d(TAG, "onNavigationItemSelected: ");
        //this.FEED_URL_FINAL = FEED_URL_STRING + AND_TOPIC + topic + OUTPUT_RSS;

        this.FEED_URL_FINAL = FEED_URL_STRING + topic + OUTPUT_RSS;
        if (topic == null){
            this.FEED_URL_FINAL = NO_TOPIC_FEED;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        setFeedUrlString();
        //bar.setVisibility(View.VISIBLE);
        UpdateNews();
        return true;
    }

    @Override
    public void onRefresh() {
        //Toast.makeText(this, "Refreshing!", Toast.LENGTH_SHORT).show();
        UpdateNews();
        // ((SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout)).setRefreshing(false);
    }
}
