/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import java.util.ArrayList;

/**
 *
 * @author Grego
 */
public class Memoria {
    
    //Variables para la memoria
    private ArrayList<ArrayList<Integer>> mapa;
/*
    //Rastro del primer vehiculo que busca el objetivo
    private ArrayList<Pair> posicionesAgentes;
    //private ArrayList<Pair> rastro;
 
    private ArrayList<TipoVehiculo> miEquipo;
   */ 
    private ArrayList<Vehiculo> equipo;
    int ObjetivoX = -1;
    int ObjetivoY = -1;
    
    /**
    * @author Grego
    * Constructor por defecto
    * hay que que anadir los agentes despues de inicializar
    */
    
    Memoria(){
        this.inicializarMemoria();
        //Obligatorio llamar uan vez por agente
        //addIniPosAgente(int x,int y)
        //addIniAgenteEquipo(TipoVehiculo tipo)
    }

    
    /**
    * @author grego
    * Inicializacion de memoria matriz 1000x1000 para no salirnos de los 
    * limites maximo de mapa 500x500 "4" indica memoria libre o mapa sin descubrir
    */
     
    private void inicializarMemoria(){
            
    this.mapa = new ArrayList<ArrayList<Integer>>();
    this.equipo = new ArrayList<Vehiculo>();
    ;
    
    for(int i = 0; i < 1000; i++){
        this.mapa.add(new ArrayList<Integer>());
            for (int j = 0; j < 1000; j++){
                this.mapa.get(i).add(-1);
            }
    }
    System.out.println("Tengo memoria");
    }
    
    /*
    * @author grego
    * Crea y anade un vehiculo a la memoria
    * @param x {Pos x agente}
    * @param y {Pos y agente}
    * @param nombre {nombre agente}
    * @param tipo {Tipo de vehiculo}
    */
  
    public void addVehiculo(int x, int y, String Nombre,TipoVehiculo tipo){

        Vehiculo nuevoVehiculo = new Vehiculo(x,y,Nombre,tipo);
        this.equipo.add(nuevoVehiculo);
        
        System.out.println("Anadido en memoria el "+ tipo + " : "+ Nombre + "  " + x + "-" + y );   

    }
    
    /**
    * @author Grego
    * Esta función Agrega la posicion inicial de un agente
    * 
    * @param xn {Pos x agente n} 
    * @param yn {Pos y agente n}
    */

     //public void addIniPosAgente(int x,int y){
       // Pair posicionAgente = new Pair(x,y);
        //this.posicionesAgentes.add(posicionAgente);


    //}
    /**
    * @author Grego
    * Asigna el tipo de un agente
    * @param tipon {tipo agente n}
    */
     //public void addIniAgenteEquipo(TipoVehiculo tipo){
       // this.miEquipo.add(tipo);
 //}
    /**
    * @author Grego
    * Busca la posicion de un vehiculo en el array para poder hacerle get/set
    * @param Nombre {Nombre agente}
    * @returm pos en array de Vehiculos
    */
    
    public int buscarVehiculo(String Nombre){
        boolean encontrado = false;
        int pos = 0;
        for(int i = 0; i < this.equipo.size() || encontrado == false; i++){
            if(this.equipo.get(i).getNombre().equals(Nombre)){
                pos = i;
                encontrado = true;
            }
        }
        
        return pos;
    }
    
    
    /**
    * @author grego
    * Función para  ir actualizando la posición de cada agente, llamar justo despues del movimiento
    * @param {nombre de agente Agente}
    * @param {nueva posición del agente x}
    * @param {nueva posición del agente y}
    */
    public void actuPosAgentes(String nombreAgente, int x,int y){
             //Pair posicionAgente = new Pair(x,y);
             //this.posicionesAgentes.set(Agente, posicionAgente);
             int pos = this.buscarVehiculo(nombreAgente);
             this.equipo.get(pos).setX(x);
             this.equipo.get(pos).setY(y);
    }
    //
    //Getter para obtener lo que tenemos alrededor recive por parametro el agente desde el que 
    //queremos consultar
    
    /*
    * @author grego
    * Consulta lo que hay al norte del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getN(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        //int auxX =  (Integer)posicionesAgentes.get(agente).getKey();
        //int auxY = (Integer)posicionesAgentes.get(agente).getValue();
        auxY--;        
        return mapa.get(auxY).get(auxX);
    }

    /*
    * @author grego
    * Consulta lo que hay al sur del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getS(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxY++;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al este del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getE(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX++;
       
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al oeste del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getW(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX--;
        return mapa.get(auxY).get(auxX);
    }
    /*
    * @author grego
    * Consulta lo que hay al norEste del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getNE(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX++;
        auxY--;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al norOeste del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getNW(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX--;
        auxY--;
        return mapa.get(auxY).get(auxX);
    }
    
    /*
    * @author grego
    * Consulta lo que hay al surEste del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getSE(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX++;
        auxY++;
        return mapa.get(auxY).get(auxX);
    }

    /*
    * @author grego
    * Consulta lo que hay al surOeste del agente
    * @param {nombre de agente Agente}
    * @return {lo que hay en esa posicion int}
    */
    public int getSW(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        int auxX = this.equipo.get(pos).getX();
        int auxY = this.equipo.get(pos).getY();
        auxX--;
        auxY++;
        return mapa.get(auxY).get(auxX);
    }   

    /*
    * @author grego
    * obtiene el tipo del agente
    * @param {nombre de agente Agente}
    * @return {tipo de agente}
    */
    public TipoVehiculo getTipo(String nombreAgente){
        int pos = this.buscarVehiculo(nombreAgente);
        return this.equipo.get(pos).getTipo();
    }
    
    /*
    * @author grego, ruben, nacho
    * actualiza el mapa
    * @param {movimiento a realizar} 
    * @param {nombre de agente Agente}
    * @param {percepcion del agente} 
    */
    
    public void actuMapa(String nombreAgente,int x, int y, ArrayList<ArrayList<Integer>> radar){
        
        //int pos = this.buscarVehiculo(nombreAgente);
        this.actuPosAgentes(nombreAgente, x,y);
        int MenX = x;
        int MenY = y;
        int pos = 0;
       
        pos = buscarVehiculo(nombreAgente);
                
        //Contadores para la matriz del radar
        int conRadarI = 0;
        int conRadarJ = 0;
       
        
        int tope = radar.size();
        int ini = tope/2;
        
        
        //Agrego la nueva informacion a la memoria
        for(int i = MenY - ini ; i <= MenY + ini; i++){
            for (int j = MenX - ini ; j <= MenX + ini; j++){
                    if(i>=0 && j>=0){
                        mapa.get(i).set(j,radar.get(conRadarI).get(conRadarJ));
                    }
                conRadarJ++; 
                
            }
            System.out.println();
            conRadarI++;
            conRadarJ = 0;     
        }
        System.out.println("He actualizado el mapa");
        //El id del agente en el mapa es el agente +10
        //int idAgente = 8;
        
        //mapa.get(MenX).set(MenY,idAgente);
   /*   
        for(int i = 0; i < equipo.size(); i++ ){
            if(equipo.get(i).getY() >= (MenY - ini ) && equipo.get(i).getY() <= (MenY + ini ) ){
                if(equipo.get(i).getX() >= (MenX - ini ) && equipo.get(i).getX() <= (MenX + ini ) ){
                    mapa.get(equipo.get(i).getX()).set(equipo.get(i).getY(), i+10);
            }
            }
        }
        */
    
    }
    
    /**
    * @author grego
    *
    *Visisualiza el mapa desde el centro de la memoria
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapaCentro(){
     
        //Muestro los datos del centro de la memoria
        for(int i = 0 ; i < mapa.size(); i++){
            for (int j = 0; j < mapa.get(i).size(); j++){
                System.out.print(mapa.get(i).get(j));
            }
            System.out.println();
        }

    }
    
    /**
    * @author grego, nacho, ruben
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
        for(int i = y - (l/2) ; i < L; i++){
            for (int j = x - (a/2); j < A; j++){
                System.out.print(mapa.get(i).get(j));
            }
            System.out.println();
        }

    }
    
        /**
    * @author grego, nacho, ruben
    *
    *Visisualiza el mapa desde el centro de la memoria
    * @param a {Ancho ncasillar} 
    * @param l {Alto ncasillas}
    */
    public void verMapaCoche(String nombreAgente,int a, int l){
        
        System.out.println("entro en verMapa");
        int pos = this.buscarVehiculo(nombreAgente);
        
        int x = this.equipo.get(pos).getX();
        int y = this.equipo.get(pos).getY();
     
        //Delimito el centro
        int L = l/2 + y;
        int A = a/2 + x;   
        
       
        //Muestro los datos del centro pasado por parametro
        for(int i = y - (l/2) ; i < L; i++){
            
            for (int j = x -(a/2); j < A; j++){
                if(i>=0 && j>=0){
                    System.out.printf("%3d" ,mapa.get(i).get(j));
   
                }/*else{
                        System.out.printf("%3d" ,2);
                }*/
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
