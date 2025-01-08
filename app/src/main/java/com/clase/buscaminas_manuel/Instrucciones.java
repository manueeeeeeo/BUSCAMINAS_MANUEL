package com.clase.buscaminas_manuel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class Instrucciones extends DialogFragment {
    OnRespuesta respuesta;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Usamos la clase Builder para construir el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Escribimos el título
        builder.setTitle("Instrucciones");
        //Escribimos la pregunta
        builder.setMessage("Primero que todo has de elegir el nivel que vas a jugar. Una vez elegido, tendras que tocar una casilla " +
                "y dependiendo del número que salga tendrás que ir viendo cuantas bombas hay alrededor de ese casilla. Si pulsas " +
                "una casilla que sea una bomba perderas la partida. Si estas muy seguro de la localización de una bomba, haz un click " +
                "largo sobre la casilla para marcarla.Si señalas que hay una bomba en una que no es así, perderas también " +
                "\nMUCHA SUERTE!!!!");
        //añadimos el botón de Si y su acción asociada
        builder.setPositiveButton("¡OKEY!", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                respuesta.onRespuesta("Okey");
            }
        });
        //añadimos el botón de No y su acción asociada
        //builder.setNegativeButton("¡NO!", new DialogInterface.OnClickListener() {
        //public void onClick(DialogInterface dialog, int id) {
        //respuesta.onRespuesta("Es un chico!");
        //}
        //});
        // Crear el AlertDialog y devolverlo
        return builder.create();
    }

    public interface OnRespuesta {
        void onRespuesta(String s);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        respuesta=(OnRespuesta)context;
    }
}
