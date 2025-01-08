package com.clase.buscaminas_manuel;

import android.widget.GridLayout;

import java.io.Serializable;

/**
 * @author Manuel
 * @version  1.0*/

public class Partida implements Serializable {
    // Declaro las variables necesarias
    private boolean juegoAcabado = false; // Para saber si el juego ha acabado o no
    private Tablero tablero; // Para tener el tablero de la partida
    private int minas = 0; // El número de minas que podemos poner

    /**
     * @param filas
     * @param columnas
     * @param minas
     * Constructor para obtener los datos necesarios y crear un nuevo tablero*/
    public Partida(int filas, int columnas, int minas){
        this.minas = minas;
        this.tablero = new Tablero(filas, columnas);
    }

    /**
     * Método para empezar la partida*/
    public void emepzarPartida(){
        tablero.rellenarTablero(minas);
    }

    /**
     * Método para obtener el tablero*/
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * @return
     * Método para obtener si la variable juegoAcabado indica que se ha acabado*/
    public boolean isJuegoAcabado() {
        return juegoAcabado;
    }

    /**
     * @return
     * Método para obtener el número de minas*/
    public int getMinas() {
        return minas;
    }

    /**
     * @param minas
     * Método para establecer el número de minas*/
    public void setMinas(int minas) {
        this.minas = minas;
    }

    /**
     * @param juegoAcabado
     * Método que establece el juego acabado como la variable que le pasemos*/
    public void setJuegoAcabado(boolean juegoAcabado) {
        this.juegoAcabado = juegoAcabado;
    }

    /**
     * @param tablero
     * Método en donde obtenemos el tablero del juego*/
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    /**
     * @param fila
     * @param columna
     * @param gridLayout
     * Método en donde descubrimos las casillas de manera logica, es decir, si el juego está
     * acabado no podemos descubrir, sino, obtenemos la casilla exacta, si la casilla tiene mina, el juego se acaba
     * y volvemos, suno tiene mina, llamamos al otro método de descubrir las casillas pero ahora si de manera lógica*/
    public boolean descubrirCasilla(int fila, int columna, GridLayout gridLayout){
        if (juegoAcabado) { // Comprobamos si ha acabado
            return false; // Vovemos con falso
        } else { // Si el juego no ha acanado aun
            Casilla casilla = tablero.getCasilla(fila, columna); // Creamos la casilla

            // Si tiene una mina, termina el juego
            if (casilla.tieneMina()) {
                juegoAcabado = true;
                return false; // Volvemos en falso
            } else {
                // Llamamos a la lógica de descubrimiento y expansión recursiva
                tablero.descubriCasilla(fila, columna, gridLayout);
                return true; // Volvemos
            }
        }
    }

    /**
     * Método para marcar una posible mina*/
    public void marcarCasilla(int fila, int columna){
        tablero.marcarPosibleMina(fila, columna);
    }

    /**
     * @return
     * Método que comprueba si hemos ganado cambiando la variable de juegoAcabado a verdadera*/
    public boolean comprobarVictoria(){
        if (tablero.comprobarSiHasGanado()) { // Comprobamos si hemos ganado
            juegoAcabado = true; // Finalizar juego
            return true; // Volvemos
        }
        return false; // Si no volvemos con false
    }

    /**
     * @param fila
     * @param columna
     * Método para obtener una casilla específica*/
    public Casilla getCasilla(int fila, int columna) {
        return tablero.getCasilla(fila, columna);
    }
}