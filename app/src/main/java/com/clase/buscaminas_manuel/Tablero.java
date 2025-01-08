package com.clase.buscaminas_manuel;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.GridLayout;

import java.io.Serializable;

/**
 * @author Manuel
 * @version  1.0*/

public class Tablero implements Serializable {
    // Declaramos las variables necesarias
    private Casilla [][] casillas; // Matriz realizada con objetos de casillas
    private int filas = 0; // El número de filas
    private int columnas = 0; // El número de columnas

    /**
     * @param columnas
     * @param filas
     * Constructor para obtener las filas y columnas y crear una matriz nueva*/
    public Tablero(int filas, int columnas){
        this.filas = filas;
        this.columnas = columnas;
        this.casillas = new Casilla[filas][columnas];
        inicializarTableroCasillas(); // Llamamos al método para crear todas las casillas necesarias
    }

    /**
     * Método para crear todas las casillas del juego*/
    public void inicializarTableroCasillas(){
        for(int i = 0; i<filas;i++){
            for(int j = 0; j<columnas;j++){
                casillas[i][j] = new Casilla(); // Voy creando una por una todas las casillas
            }
        }
    }

    /**
     * @param minas
     * Método para colocar todas las minas por el tablero*/
    public void rellenarTablero(int minas){
        int minasColocadas = 0; // Contador para no pasarnos de minas

        while(minasColocadas<minas){
            int fila = (int)(Math.random()*filas); // Obtenemos un número aleatorio de la fila dentro del rango
            int columna = (int)(Math.random()*columnas); // Obtenemos un número aleatorio de la columna dentro del rango

            if(!casillas[fila][columna].tieneMina()){ // Comprobamos si la casilla ya tiene una mina
                casillas[fila][columna].ponerMina(); // En caso de que no la tenga se la ponemos
                actualizarCasillasAdyacentes(fila,columna); // Procedemos a contar las casillas adyascentes
                minasColocadas++; // Sumamos una al contador
            }
        }
    }

    /**
     * @param columna
     * @param fila
     * Método para obtener el numero */
    private void actualizarCasillasAdyacentes(int fila, int columna){
        for (int i = fila - 1; i <= fila + 1; i++) {
            for (int j = columna - 1; j <= columna + 1; j++) {
                if (esCasillaValida(i, j) && !casillas[i][j].tieneMina()) { // En caso de que la casilla seá valida y esa casilla no tenga una mina
                    casillas[i][j].incrementarNumero(); // Incrementamos en uno el número de minas alrededor de esa
                }
            }
        }
    }

    /**
     * @param fila
     * @param columna
     * @return
     * Método que nos permite comprobar si la casilla es valida o no*/
    private boolean esCasillaValida(int fila, int columna){
        boolean valida = false; // Creamos una variable booleana
        if(fila>=0 && fila<filas && columna>=0 && columna<columnas){
            // Y comprobamos si la variable de fila y columna no sobrepasan los
            //limites de la supuesta matriz.
            valida = true;
        }else{
            valida = false;
        }
        return valida;
    }

    /**
     * @param columna
     * @param fila
     * @return
     * Método para obtener una casilla en concreto y así obtener toda la información de está*/
    public Casilla getCasilla(int fila, int columna){
        return casillas[fila][columna];
    }

    /**
     * @param x
     * @param y
     * @param gridLayout
     * Método el cuál vamos a utilizar para poder ir descubriendo las casillas,
     * lo que en resumen hace es comprueba otra vez si la fila y la columna no sobrepasan
     * los limites, comprueba también si la casilla ya está descubierta o si es una mina,
     * si pasa todos los filtros anteriores, creamos el botón (casilla correspondiente)
     * si el número de esa casilla es mayor que 0, ponemos el valor, colocamos el color rojo de fondo
     * y el texto en blanco y marcamos la casilla como descubierta, en caso que no tenga minas alrededor
     * ponemos esa casilla en rojo y procedemos de manera recursiva a comprobar todas las casillas adyascentes
     * a esta.*/
    public void descubriCasilla(int x, int y, GridLayout gridLayout) {
        if (x < 0 || x >= filas || y < 0 || y >= columnas) { // Comprobamos los limites
            return;
        }
        Casilla casilla = casillas[x][y]; // Creamos la casilla con los datos necesarios
        if (casilla.isCasillaDescubierta() || casilla.tieneMina()) { // Comprobamos que no este levantada ni tenga una mina
            return;
        }
        Button boton = (Button) gridLayout.getChildAt(x * columnas + y); // Creamos el botón
        if (casilla.getNumero() > 0) { // En caso de que tenga minas alrededor
            boton.setText(String.valueOf(casilla.getNumero())); // Ponemos cuantas minas tiene alredor
            boton.setTextColor(Color.WHITE); // Establecemos el texto como blanco
            boton.setBackgroundColor(Color.RED); // Establecemos el fondo como rojo
            casilla.descubrirCasilla(); // Marcamos la casilla como descubierta
        } else { // En caso de que no tenga minas alrededor
            boton.setText(""); // No ponemos nada de texto ya que es 0
            boton.setBackgroundColor(Color.RED); // Ponemos el fondo como rojo
            casilla.descubrirCasilla(); // Marcamos la casilla como descubierta
            // Y ahora procedemos a la consulta de todas las casillas adyascentes y llamado de manera recursiva a este método.
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (x + dx >= 0 && x + dx < filas && y + dy >= 0 && y + dy < columnas) { // Comprobamos los limites
                        descubriCasilla(x + dx, y + dy, gridLayout);
                    }
                }
            }
        }
    }

    /**
     * @param fila
     * @param columna
     * Método para señalar donde nosotros creeemos que hay una mina*/
    public void marcarPosibleMina(int fila, int columna){
        casillas[fila][columna].posibleMarca();
    }

    /**
     * Método para ir comprobando si el usuario/jugador ha ganado*/
    public boolean comprobarSiHasGanado(){
        // Generamos un for dentro de otro for
        for(int i = 0;i<filas;i++){
            for(int j = 0;j<columnas;j++){
                // Comprobamos una por una si la casilla tiene mina y esta marcada como posible mina
                if(casillas[i][j].tieneMina() && !casillas[i][j].isCasillaMarcada()){
                    return false; // En caso tenga mina y no este marcada como posible mina devolvemos false
                }
            }
        }
        return true; // Si pasamos el bucle y todas las casillas marcan la condición hemos ganado
    }

    /**
     * Obtención de las filas*/
    public int getFilas() {
        return filas;
    }

    /**
     * @param filas
     * Establecer las filas*/
    public void setFilas(int filas) {
        this.filas = filas;
    }

    /**
     * Obtención de las columnas*/
    public int getColumnas() {
        return columnas;
    }

    /**
     * @param columnas
     * Establecer las columnas*/
    public void setColumnas(int columnas) {
        this.columnas = columnas;
    }

    /**
     * @param gridLayout
     * Método para descubrir todas las casillas cuando el jugador pierde, es muy similar al
     * método para descubrir de manera recursiva, pero en este se descubren todas para así indicar
     * que el usuario ha perdido*/
    public void descubrirTodasLasCasillasCuandoPierde(GridLayout gridLayout, int imagenBomba) {
        // Recorremos todas las casillas
        for (int x = 0; x < filas; x++) {
            for (int y = 0; y < columnas; y++) {
                Casilla casilla = casillas[x][y]; // Creamos la casilla exacta para cada coordenada de la matriz
                Button boton = (Button) gridLayout.getChildAt(x * columnas + y); // Creamos el botón también

                // Si la casilla tiene una mina
                if (casilla.tieneMina()) {
                    boton.setBackgroundResource(imagenBomba);
                } else if (casilla.getNumero() > 0) {
                    // Si la casilla tiene alguna mina cercana
                    boton.setText(String.valueOf(casilla.getNumero())); // Ponemos cuantas minas tiene alredor
                    boton.setTextColor(Color.WHITE); // Establecemos el texto como blanco
                    boton.setBackgroundColor(Color.RED); // Establecemos el fondo como rojo
                } else {
                    // Si no tiene minas cercanas y es un espacio vacío, la dejamos vacía
                    boton.setText("");
                    boton.setBackgroundColor(Color.RED); // Ponemos el color del fondo del botónD
                }
            }
        }
    }
}
