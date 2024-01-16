package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;


public class CurrencyListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        ExchangeRateDatabase erd = new ExchangeRateDatabase();
        String[] currencies = ExchangeRateDatabase.getCurrencies();
        CurrencyAdapter adapter = new CurrencyAdapter(this, currencies);

        ListView listView = findViewById(R.id.currencyListView);
        listView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.currencyListToolbar);
        toolbar.setTitle("CurrencyList");
        setSupportActionBar(toolbar);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = (String) parent.getItemAtPosition(position);
                String capitalQuery = "geo:0,0?q=" + Uri.encode(erd.getCapital(selectedCurrency));
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(capitalQuery));
                startActivity(mapsIntent);
            }
        });
    }
}