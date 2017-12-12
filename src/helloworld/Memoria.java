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
    private ArrayList<Pair> posicionesAgentes;
    //private ArrayList<Pair> rastro;
 
    private ArrayList<TipoVehiculo> miEquipo;
    
    int ObjetivoX = -1;
    int ObjetivoY = -1;
    
    /**
    * @author Grego
    * Si se usa este constructor hay que inicializar manualmente la posición de los agentes y el tipo
    */
    
    Memoria(){
        this.inicializarMemoria();
    }
    /**
    * @author Grego
    * Este constructor recibe todos los parametros para poder inicializar toda la memoria
    * 
    * @param xn {Pos x agente n} 
    * @param yn {Pos y agente n}
    * @param tipon {tipo agente n}
    */
    
    Memoria(int x1,int y1,TipoVehiculo tipo1,int x2,int y2,TipoVehiculo tipo2,int x3,int y3,TipoVehiculo tipo3,int x4,int y4,TipoVehiculo tipo4 ){
        this.inicializarMemoria();
        inicializrPosAgentes(x1,y1,x2,y2,x3,y3,x4,y4);
        inicializarMiEquipo(tipo1, tipo2, tipo3, tipo4);
    }
    
    /**
    * @author grego
    * Inicializacion de memoria matriz 1000x1000 para no salirnos de los 
    * limites maximo de mapa 500x500 "4" indica memoria libre o mapa sin descubrir
    */
     
    private void inicializarMemoria(){
            
    this.mapa = new ArrayList<ArrayList<Integer>>();
    
    
    for(int i = 0; i < 1000; i++){
        this.mapa.add(new ArrayList<Integer>());
            for (int j = 0; j < 1000; j++){
                this.mapa.get(i).add(4);
            }
    }
    System.out.println("Tengo memoria");
    }
  
    /**
    * @author Grego
    * Esta función inicializa las posiciones de los agentes
    * 
    * @param xn {Pos x agente n} 
    * @param yn {Pos y agente n}
    */

     public void inicializrPosAgentes(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4){
        Pair posicionAgente1 = new Pair(x1,y1);
        Pair posicionAgente2 = new Pair(x2,y2);
        Pair posicionAgente3 = new Pair(x3,y3);
        Pair posicionAgente4 = new Pair(x4,y4);

        this.posicionesAgentes.add(posicionAgente1);
        this.posicionesAgentes.add(posicionAgente2);
        this.posicionesAgentes.add(posicionAgente3);
        this.posicionesAgentes.add(posicionAgente4);

    }
    /**
    * @author Grego
    * Asigna los tipos de los que son cada agente
    * @param tipon {tipo agente n}
    */
     public void inicializarMiEquipo(TipoVehiculo tipo1,TipoVehiculo tipo2, TipoVehiculo tipo3,TipoVehiculo tipo4){
        this.miEquipo.add(tipo1);
        this.miEquipo.add(tipo2);
        this.miEquipo.add(tipo3);
        this.miEquipo.add(tipo4);

 }
    /**
    * @author grego
    * Función para  ir actualizando la posición de cada agente, llamar justo despues del movimiento
    * @param {numero de agente Agente}
    * @param {nueva posición del agente x}
    * @param {nueva posición del agente y}
    */
    public void actuPosAgentes(int Agente, int x,int y){
             Pair posicionAgente = new Pair(x,y);
             this.posicionesAgentes.set(Agente, posicionAgente);
    }
    //
    //Getter para obtener lo que tenemos alrededor recive por parametro el agente desde el que 
    //queremos consultar
    
    /*
    * @author grego
    * Consulta lo que hay al norte del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getN(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxY--;        
        return mapa.get(auxY).get(auxX);
    }

    /*
    * @author grego
    * Consulta lo que hay al sur del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getS(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxY++;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al este del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getE(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX++;
       
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al oeste del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getW(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX--;
        return mapa.get(auxY).get(auxX);
    }
    /*
    * @author grego
    * Consulta lo que hay al norEste del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getNE(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX++;
        auxY--;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al norOeste del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getNW(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX--;
        auxY--;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al surEste del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getSE(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX++;
        auxY++;
        return mapa.get(auxY).get(auxX);
    }

    /*
    * @author grego
    * Consulta lo que hay al surOeste del agente
    * @param {numero de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getSW(int agente){
        int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxX--;
        auxY++;
        return mapa.get(auxY).get(auxX);
    }   

    /*
    * @author grego
    * obtiene el tipo del agente
    * @param {numero de agente Agente}
    * @return {tipo de agente}
    */
    public TipoVehiculo getTipo(int agente){
        return miEquipo.get(agente);
    }
    
    /*
    * @author grego
    * actualiza el mapa
    * @param {movimiento a realizar} 
    * @param {numero de agente Agente}
    * @param {percepcion del agente} 
    */
    
    public void actuMapa(String movementCommand, int agente, ArrayList<ArrayList<Integer>> radar){
        
        int MenX = (Integer)posicionesAgentes.get(agente).getKey();
        int MenY = (Integer)posicionesAgentes.get(agente).getValue();
        
        //Ajusto mi posicion en funcion del movimiento
        if (movementCommand.equals("moveW") ){
            MenX--;
            System.out.println("Voy al Oeste");

        }else if (movementCommand.equals("moveE")){
            MenX++;
            System.out.println("Voy al Este");    

        }else if (movementCommand.equals("moveN")){
            MenY--;
            System.out.println("Voy al Norte");    

        }else if (movementCommand.equals("moveS")){
            MenY++;
            System.out.println("Voy al Sur");    
        
        }else if (movementCommand.equals("moveNW")){
            MenX--;
            MenY--;
            System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveNE")){
            MenX++;
            MenY--;
            System.out.println("Voy al NorEste");    
        
        }else if (movementCommand.equals("moveSW")){
            MenX--;
            MenY++;
            System.out.println("Voy al NorOeste");    
        
        }else if (movementCommand.equals("moveSE")){
            MenX++;
            MenY++;
            System.out.println("Voy al NorOeste");    
        } 
        
        //Grabamos en memoria la posición reajustada del cliente
        Pair posicionAgente = new Pair(MenX,MenY);
        this.posicionesAgentes.set(agente, posicionAgente);
        
        
        //Contadores para la matriz del radar
        int conRadarI = 0;
        int conRadarJ = 0;
        
        int tope = radar.size();
        int ini = tope/2;
        
        //Agrego la nueva informacion a la memoria
        for(int i = MenY - ini ; i < MenY + tope; i++){
            for (int j = MenX - ini ; j < MenX + tope; j++){
                mapa.get(i).set(j,radar.get(conRadarI).get(conRadarJ));
                conRadarJ++;     
            }
            System.out.println();
            conRadarI++;
            conRadarJ = 0;     
        }
        
        //El id del agente en el mapa es el agente +10
        int idAgente = agente+10;
        
        mapa.get(MenX).set(MenY,idAgente);
        
        
    
    }
    
    /**
    * @author grego
    *
    *Visisualiza el mapa desde el centro de la memoria
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapaCentro(int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + 500;
        int A = a/2 + 500;   
        
       
        //Muestro los datos del centro de la memoria
        for(int i = 500 - l/2 ; i < L; i++){
            for (int j = 500 - a/2; j < A; j++){
                System.out.print(mapa.get(i).get(j));
            }
            System.out.println();
        }

    }
    
        /**
    * @author grego
    *
    *Visisualiza el mapa desde el centro de la memoria
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapa(int x, int y,int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + y;
        int A = a/2 + x;   
        
       
        //Muestro los datos del centro pasado por parametro
        for(int i = y - l/2 ; i < L; i++){
            for (int j = x - a/2; j < A; j++){
                System.out.print(mapa.get(i).get(j));
            }
            System.out.println();
        }

    }
    
    /**
    * @author grego
    *
    *Obtiene un mapa parcial de memoria
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    * @param x {posicion en x desde conde obtener} 
    * @param y {posicion en y desde conde obtener} 
    * @return {tArrayList<ArrayList<Integer>>} mapa parcial
    */
    public ArrayList<ArrayList<Integer>> obtenerMapaParcial(int x, int y,int a, int l){
     
     
        //Delimito el centro
        int L = l/2 + y;
        int A = a/2 + x;
        ArrayList<ArrayList<Integer>> recorte;
       
        recorte = new ArrayList<ArrayList<Integer>>();
       
        //Cojo los datos del centro de la memoria
        for(int i = y - l/2 ; i < L; i++){
            recorte.add(new ArrayList<Integer>());
            for (int j = x - a/2; j < A; j++){
                recorte.get(i).add(mapa.get(i).get(j));
            }
        }
        return recorte;
    }
    
}
