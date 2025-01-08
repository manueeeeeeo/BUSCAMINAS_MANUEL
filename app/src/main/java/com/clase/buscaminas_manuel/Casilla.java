package com.clase.buscaminas_manuel;

import java.io.Serializable;

/**
 * @author Manuel
 * @version  1.0*/

public class Casilla implements Serializable {
    // Declaramos las variables necesarias
    private boolean mina = false; // Para saber si es una mina o no
    private boolean casillaDescubierta = false; // Para saber si la casilla está descubierta o no
    private boolean casillaMarcada = false; // Para saber si hemos marcado la casilla como posible mina
    private int numero = 0; // Para saber e número de minas que tiene alrededor

    /**
     * @return
     * Método que nos devueve el booleano de ti tiene o no una mina*/
    public boolean tieneMina(){
        return mina;
    }

    /**
     * Método que nos permite marcar una casilla como que es una mina*/
    public void ponerMina(){
        this.mina = true;
    }

    /**
     * Método que nos permite marcar una casilla como descubierta*/
    public void descubrirCasilla(){
        this.casillaDescubierta = true;
    }

    /**
     * @return
     * Método que nos devuelve si la casilla está descubierta o no*/
    public boolean isCasillaDescubierta(){
        return casillaDescubierta;
    }

    /**
     * Método para marcar o desmarcar una casilla como posible mina, es decir,
     * si le llamamo comprobamos si la casilla no está descubierta, ya que si está descubierta
     * no podremos señalarla como posible mina, una vez comprobado, lo que hacemos en invertir
     * el valor de casillaMarcada, es decir si es falso, la ponemos verdadero y si es
     * verdadero la ponemos falso*/
    public void posibleMarca(){
        if(!casillaDescubierta){
            this.casillaMarcada = !casillaMarcada;
        }
    }

    /**
     * @return
     * Método que nos devuelve si una casilla ha sido marcada como posible mina*/
    public boolean isCasillaMarcada(){
        return casillaMarcada;
    }

    /**
     * @return
     * Método que nos devuelve el número de minas que tiene una casilla alrededor*/
    public int getNumero(){
        return numero;
    }

    /**
     * Método para establecer el número de minas alrededor*/
    public void setNumero(int numero) {
        this.numero = numero;
    }

    /**
     * Método que nos permite ir incrementando de uno en uno el "contador" de
     * minas que tenemos alrededor de está casilla*/
    public void incrementarNumero(){
        this.numero++;
    }
}
