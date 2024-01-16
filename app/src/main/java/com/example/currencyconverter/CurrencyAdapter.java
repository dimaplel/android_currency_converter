package com.example.currencyconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

// Custom Adapter works both for Spinner and ListView
// Source: https://www.geeksforgeeks.org/custom-arrayadapter-with-listview-in-android/,
public class CurrencyAdapter extends ArrayAdapter<String> {
    public CurrencyAdapter(Context context, String[] currencies) {
        super(context, android.R.layout.simple_spinner_item, currencies);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @SuppressLint("DefaultLocale")
    private View getCustomView(int position, View convertView, ViewGroup parent) {
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.list_item_currency, parent, false);

        TextView currencyTextView = row.findViewById(R.id.textViewCurrencyName);
        ImageView imageView = row.findViewById(R.id.countryImageView);

        String currency = getItem(position);
        assert currency != null;
        String imageName = "flag_" + currency.toLowerCase();
        int imageResource = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
        if (imageResource != 0) {
            Drawable imageDrawable = ContextCompat.getDrawable(context, imageResource);
            imageView.setImageDrawable(imageDrawable);
        }

        currencyTextView.setText(currency);
        return row;
    }
}