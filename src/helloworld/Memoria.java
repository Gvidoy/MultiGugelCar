/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import java.util.ArrayList;
import javafx.util.Pair;

/**
 *
 * @author Grego
 */
public class Memoria {
    
    //Variables para la memoria
    private ArrayList<ArrayList<Integer>> mapa;

    //Rastro del primer vehiculo que busca el objetivo
    //private ArrayList<Pair> rastro;
    private ArrayList<Pair> posicionesAgentes;
    private ArrayList<Pair> rastro;
 


    
    
    /**
    * @author grego
    */
    
    //Inicializacion de memoria matriz 1000x1000 para no salirnos de los
    //limites maximo de mapa 500x500 "4" indica memoria libre o mapa sin descubrir
    
    private void inicializarMemoria(){
            
    this.mapa = new ArrayList<ArrayList<Integer>>();
    

    
    this.rastro = new ArrayList<Pair>();
  //  Pair PosicionRastro = new Pair(menX,menY);
    
  
    
  //  this.rastro.add(PosicionRastro);
    
    System.out.println(rastro.get(0).getValue());
    
    for(int i = 0; i < 1000; i++){
        this.mapa.add(new ArrayList<Integer>());
            for (int j = 0; j < 1000; j++){
                this.mapa.get(i).add(4);
            }
    }
    System.out.println("Tengo memoria");
    }
  
    /**
    * @author grego
    * Funcion de inicialización llamar obligatoriamente 1 vez al principio despues de llamar a Query_Ref
    */
     public void inicializrPosAgentes(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4){
        Pair posicionAgente1 = new Pair(x1,y1);
        Pair posicionAgente2 = new Pair(x2,y2);
        Pair posicionAgente3 = new Pair(x3,y3);
        Pair posicionAgente4 = new Pair(x4,y4);

        this.posicionesAgentes.add(posicionAgente1);
        this.posicionesAgentes.add(posicionAgente1);
        this.posicionesAgentes.add(posicionAgente1);
        this.posicionesAgentes.add(posicionAgente1);

 }
    /**
    * @author grego
    * Función para  ir actualizando la posición de cada agente, llamar justo despues del movimiento
    */
    public void actuPosAgentes(int Agente, int x,int y){
             Pair posicionAgente = new Pair(x,y);
             this.posicionesAgentes.set(Agente, posicionAgente);
    }
    

    
}
