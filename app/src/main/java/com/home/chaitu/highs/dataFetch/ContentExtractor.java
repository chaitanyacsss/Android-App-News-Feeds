package com.home.chaitu.highs.dataFetch;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import org.w3c.dom.Document;

import java.net.URL;

import api.AlchemyAPI;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * Created by chaitu on 31-08-2016.
 */
public class ContentExtractor extends AsyncTask {
    TextView extendedDesc;
    DotProgressBar dotProgressBar;
    String title;
    String ALCHEMY_API_KEY = "6f4c6830f31ea8cc97c0d367aba7006ab4e5ccfe";

    public ContentExtractor(TextView extendedDesc, DotProgressBar dotProgressBar, String title) {
        this.extendedDesc = extendedDesc;
        this.dotProgressBar = dotProgressBar;
        this.title = title;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String articleText = null;
        try {
            articleText = (String) boilerpipeExtract((String) params[0]);
            //articleText = alchemyArticleExtract((String) params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleText;
    }

    @NonNull
    private static Object boilerpipeExtract(String urlString) {
        String finalContent = "";
        try {
            URL url = new URL(urlString);
            String articleText = ArticleExtractor.INSTANCE.getText(url);
            String articleTextWithFirstLineTrimmed = articleText.substring(articleText.indexOf('\n') + 1);
            String lines[] = articleTextWithFirstLineTrimmed.split("\\r?\\n");
            for (String line : lines) {
                int spaces = line == null ? 0 : line.length() - line.replace(" ", "").length();
                if (spaces > 1 && !line.toLowerCase().contains("© copyright") && !line.toLowerCase().contains("click here")
                        && !line.contains("First Published") && !line.toLowerCase().contains("facebook twitter") && !line.toLowerCase().contains("follow us on")
                        && !line.toLowerCase().contains("0 0 0") && !line.toLowerCase().contains("getty images") && !line.toLowerCase().contains("enlarge this image") && !line.toLowerCase().contains("share on messenger")) {
                    finalContent += line + "\n";
                }
            }
            return finalContent;
        } catch (Exception e) {
            return finalContent;
        }
    }

  /*  public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.theguardian.com/football/2016/sep/09/claudio-ranieri-leicester-city-earn-places");
        // String articleText = ArticleExtractor.INSTANCE.getText(url);
        System.out.println((String) boilerpipeExtract(url.toString()));
        //System.out.println(articleText);
    }*/

    private String alchemyArticleExtract(String url) throws Exception {
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString(ALCHEMY_API_KEY);

        Document doc = alchemyObj.URLGetText(url);
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName("text").item(0).getTextContent();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        extendedDesc.setText((String) o);
        if (dotProgressBar.getVisibility() == View.VISIBLE) {
            dotProgressBar.setVisibility(View.GONE);
        }
    }
}

