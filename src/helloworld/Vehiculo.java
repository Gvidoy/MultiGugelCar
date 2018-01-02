/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

/**
 *
 * @author Grego
 * Clase que almacena los datos de los vehiculos
 */
public class Vehiculo {
    private String nombre;
    private int x;
    private int y;
    private TipoVehiculo tipo;

        /**
    * @author Grego
    * Constructor 
    * @param x {Pos x agente}
    * @param y {Pos y agente}
    * @param nombre {nombre agente}
    * @param tipo {Tipo de vehiculo}
    */
    public Vehiculo(int x, int y, String nombre, TipoVehiculo tipo) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }
    
    /*
    * @author grego
    * Consulta lo que hay al nombre del agente
    * @return {nombre String}
    */

    public String getNombre() {
        return nombre;
    }
    
    /*
    * @author grego
    * Consulta la posición X
    * @return {pos x int}
    */

    public int getX() {
        return x;
    }

    /*
    * @author grego
    * Consulta la posición y
    * @return {pos y int}
    */
    
    public int getY() {
        return y;
    }
    
    /*
    * @author grego
    * Consulta de que tipo es el vehiculo
    * @return {tipo TipoVehiculo}
    */

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
