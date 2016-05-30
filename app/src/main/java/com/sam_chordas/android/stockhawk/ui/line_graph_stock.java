package com.sam_chordas.android.stockhawk.ui;
/**
 * Created by vaibhav.seth on 5/30/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class line_graph_stock extends ActionBarActivity {

    LineChart lineChart;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        Intent i = getIntent();
        String sym = i.getStringExtra("symbol");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        Calendar d = Calendar.getInstance();
        d.setTime(new Date()); // Now use today date.

        d.add(Calendar.DATE, -20); // Adding 5 days
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println(dateFormat.format(c.getTime()));
        String query = "select * from yahoo.finance.historicaldata where symbol ='" + sym + "' and startDate = '"+dateFormat.format(d.getTime())+"' and endDate = '"+dateFormat.format(c.getTime())+"'";

        uri = Uri.parse("https://query.yahooapis.com/v1/public/yql").buildUpon().appendQueryParameter("q", query).appendQueryParameter("format","json").
                appendQueryParameter("diagnostics","true").appendQueryParameter("env","store://datatables.org/alltableswithkeys").appendQueryParameter("callback","").build();
        Log.v("url", uri.toString());
        lineChart = (LineChart) findViewById(R.id.linechart);

        AsyncTaskForLineGraph asyncTaskForLineGraph = new AsyncTaskForLineGraph();
        asyncTaskForLineGraph.execute(uri.toString());
    }
    class AsyncTaskForLineGraph extends AsyncTask<String,String,String> {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        @Override
        protected String doInBackground(String... params) {

            String response = getResponseFromApi(params[0]);
            int val = 1;
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject jsonObject1 = jsonObject.getJSONObject("query");
                JSONObject jsonObject3 = jsonObject1.getJSONObject("results");
                JSONArray jsonArray = jsonObject3.getJSONArray("quote");
                Log.v("array",jsonArray.length()+"");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    entries.add(new Entry((int) Float.parseFloat(jsonObject2.getString("Adj_Close")),val));
                    val++;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for(int i = 0 ; i < 15 ; i++)
                labels.add(String.valueOf(i));

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            int animateSeconds = 1000;
            super.onPostExecute(s);
            LineDataSet dataset = new LineDataSet(entries, "Stock Values over time");
            dataset.setDrawCircles(true);
            dataset.setDrawValues(true);
            LineData data = new LineData(labels,dataset);
            lineChart.setDescription("Graph for Stock Values");
            lineChart.setData(data);
            lineChart.animateY(animateSeconds);

        }

        public String getResponseFromApi(String url) {
            String response="";

            try {
                response = fetchData(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        String fetchData(String url) throws IOException{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }
}
