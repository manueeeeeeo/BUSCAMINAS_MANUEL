package com.clase.buscaminas_manuel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Manuel
 * @version 1.0*/

public class BombaAdapter extends ArrayAdapter<String> {
    // Declaro las variables necesarias para la clase
    private final int[] icons; // Variable dende guardaremos los iconos de las bombas
    private final String[] items; // Variable donde guardaremos los nomrbes de las bombas

    /**
     * @param context
     * @param resource
     * @param icons
     * @param items
     * @param textViewResourceId
     * Constructor en el que le paso todos los parametros necesarios para inicializar
     * la clase*/
    public BombaAdapter(Context context, int resource, int textViewResourceId, String[] items, int[] icons) {
        super(context, resource, textViewResourceId, items);
        this.icons = icons;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return setupView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return setupView(position, convertView, parent);
    }

    private View setupView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_bomba, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.item_icon);
        TextView text = convertView.findViewById(R.id.item_text);

        icon.setImageResource(icons[position]);
        text.setText(items[position]);

        return convertView;
    }
}