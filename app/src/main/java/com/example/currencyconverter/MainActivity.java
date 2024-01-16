package com.example.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;


public class MainActivity extends AppCompatActivity {
    Spinner spinnerFrom, spinnerTo;
    Button calculateButton;
    EditText editTextAmount;
    ExchangeRateDatabase erd;
    CurrencyAdapter currencyAdapter;
    ShareActionProvider shareActionProvider;
    ExchangeRateHelper dbHelper = new ExchangeRateHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        erd = new ExchangeRateDatabase();
        String[] currencies = ExchangeRateDatabase.getCurrencies();
        refreshFromDb();

        currencyAdapter = new CurrencyAdapter(
                this,
                currencies);

        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        calculateButton = findViewById(R.id.buttonCalculate);
        editTextAmount = findViewById(R.id.editTextAmount);

        spinnerFrom.setAdapter(currencyAdapter);
        spinnerTo.setAdapter(currencyAdapter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String currencyFrom = spinnerFrom.getSelectedItem().toString();
        String currencyTo = spinnerTo.getSelectedItem().toString();
        float enteredValue = Float.parseFloat(editTextAmount.getText().toString());

        editor.putString("CurrencyFrom", currencyFrom);
        editor.putString("CurrencyTo", currencyTo);
        editor.putFloat("EnteredValue", enteredValue);
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume(){
        super.onResume();

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String defaultCurrency = ExchangeRateDatabase.getCurrencies()[0];

        String currencyFrom = prefs.getString("CurrencyFrom", defaultCurrency);
        String currencyTo = prefs.getString("CurrencyTo", defaultCurrency);
        float enteredValue = prefs.getFloat("EnteredValue", 0.0f);

        int positionFrom = currencyAdapter.getPosition(currencyFrom);
        int positionTo = currencyAdapter.getPosition(currencyTo);

        spinnerFrom.setSelection(positionFrom);
        spinnerTo.setSelection(positionTo);
        editTextAmount.setText(Float.toString(enteredValue));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.my_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);

        setShareText(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_currency_list){
            Intent intent = new Intent(this, CurrencyListActivity.class);
            startActivity(intent);
            return true;
        } if (id == R.id.action_refresh_rates){
            WorkRequest refreshRatesRequest =
                    new OneTimeWorkRequest.Builder(ExchangeRateUpdateWorker.class).build();
            WorkManager.getInstance(this).enqueue(refreshRatesRequest);
            refreshFromDb();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void onClick(View v) {
        String currencyFrom = spinnerFrom.getSelectedItem().toString();
        String currencyTo = spinnerTo.getSelectedItem().toString();
        double enteredValue = Double.parseDouble(editTextAmount.getText().toString());

        double convertedValue = erd.convert(enteredValue, currencyFrom, currencyTo);
        TextView amountText = findViewById(R.id.textViewResult);

        @SuppressLint("DefaultLocale") String shareMessage = String.format(
                "Currency converter says: %.2f %s are %.2f %s",
                enteredValue, currencyFrom, convertedValue, currencyTo
        );

        Log.i("CurrencyConverter", "Setting Extra Text to: " + shareMessage);
        amountText.setText(String.format("%.2f", convertedValue));
        setShareText(shareMessage);
    }

    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }

    private void refreshFromDb(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                ExchangeRateHelper.EXCHANGE_RATES_COL_CODE,
                ExchangeRateHelper.EXCHANGE_RATES_COL_RATE
        };

        Cursor cur = db.query(ExchangeRateHelper.EXCHANGE_RATES_TABLE, projection,
                null, null, null, null, null);

        while (cur.moveToNext()){
            String currencyCode = cur.getString(cur.getColumnIndexOrThrow(ExchangeRateHelper.EXCHANGE_RATES_COL_CODE));
            double rate = cur.getDouble(cur.getColumnIndexOrThrow(ExchangeRateHelper.EXCHANGE_RATES_COL_RATE));
            erd.setExchangeRate(currencyCode, rate);
        }

        cur.close();
    }


}