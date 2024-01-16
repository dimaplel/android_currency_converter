package com.example.currencyconverter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExchangeRateUpdateWorker extends Worker {
    private final OkHttpClient client = new OkHttpClient();
    private final ExchangeRateHelper dbHelper;

    public ExchangeRateUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams){
        super(context, workerParams);
        this.dbHelper = new ExchangeRateHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("ExchangeRateWorker", "Started refreshing exchange rates");
        refreshRates();
        Log.d("ExchangeRateWorker", "Finished refreshing exchange rates");

        // Source: https://stackoverflow.com/questions/56408359/how-to-show-toast-in-workmanager-dowork
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Exchange rates refreshed!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return Result.success();
    }

    private void refreshRates(){
        String queryString = "https://www.floatrates.com/daily/eur.json";

        try {
            Request request = new Request.Builder().url(queryString).build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JSONObject root = new JSONObject(responseBody);
            String[] currencies = ExchangeRateDatabase.getCurrencies();

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            for (String cur: currencies){
                try {
                    JSONObject currencyBody = root.getJSONObject(cur.toLowerCase());
                    double rateForEur = currencyBody.getDouble("rate");

                    values.clear();
                    values.put(ExchangeRateHelper.EXCHANGE_RATES_COL_CODE, cur);
                    values.put(ExchangeRateHelper.EXCHANGE_RATES_COL_RATE, rateForEur);

                    // I made a unique constraint for currency code so that
                    // only one currency entry could be possible, and I use
                    // insertWithOnConflict() method, which updates row if
                    // there is a duplicate, or insert otherwise
                    long result = db.insertWithOnConflict(
                            ExchangeRateHelper.EXCHANGE_RATES_TABLE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE);

                    if (result != -1)
                        Log.d("RatesRefresher",
                                String.format("Exchange rate for %s was set to %f", cur, rateForEur));
                    else
                        Log.w("RatesRefresher",
                                "Couldn't insert/update rate for currency " + cur);

                } catch (JSONException e) {
                    Log.w("RatesRefresher", "Couldn't find rate for currency " + cur);
                }
            }
        } catch (IOException e){
            Log.e("RatesRefresher", "Couldn't query Float Rates API");
        } catch (JSONException e){
            Log.e("RatesRefresher", "Couldn't parse response properly");
        }
    }
}
