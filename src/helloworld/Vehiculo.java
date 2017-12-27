package helloworld;


import helloworld.TipoVehiculo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Grego
 */
public class Vehiculo {
    private String nombre;
    private int x;
    private int y;
    private TipoVehiculo tipo;

    public Vehiculo(int x, int y, String nombre, TipoVehiculo tipo) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    
}
