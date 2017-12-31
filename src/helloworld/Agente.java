/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.parser.JSONParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.eclipsesource.json.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;
import javafx.util.Pair;


/**
 *
 * @author Dani, nacho, ruben
 */
public class Agente extends SingleAgent {
 
    private static String nombreLider = "Lidllll";
    private ACLMessage outbox;
    private String conversationID;
    private String reply_withID;
    
    //mensajes
    private MessageQueue queue;

    private  int battery;
    private  int x;
    private  int y;
    private  ArrayList<ArrayList<Integer>> sensor;
    private  ArrayList<ArrayList<Integer>> mapa;  
    private  Boolean enObjetivo; 
    private  TipoVehiculo tipoVehiculo;
    
    private  int fuelRate;
    private  int range;
    private  Boolean fly;
    
    private String last_move;
    private int coord_x_objetivo;
    private int coord_y_objetivo;
    private Pair<Integer,Integer> checkpoint;
    private int num_pasos;
    private int indice_ultima_direccion;
    
    private boolean objetivo_encontrado;
    
    private int centro_mapa;
    

    public Agente() throws Exception {
        super(null);
      
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        
        this.outbox = null;

        queue = new MessageQueue(20);
        
        
        this.conversationID = "";
        this.reply_withID = "";
        System.out.println("\nHola Mundo soy un agente llamado " + this.getName());

        battery = 100;
        mapa = new ArrayList();
        
        this.last_move = "";
        this.coord_x_objetivo = 0;
        this.coord_y_objetivo = 0;
        this.checkpoint = new Pair(0,0);
        this.num_pasos = 0;
        this.indice_ultima_direccion = 0;
        
        this.objetivo_encontrado = false;
    }
   
    //public void init();
    @Override
    public void execute(){
   
        try {
       
           System.out.println("voy a ver si hay clave " + " - " + this.getName());
        if(!askForConversationID()){
            Thread.sleep(4000);
            System.out.println("No hay, voy a suscribirme " + " - " + this.getName());
            subscribe();
        }
            
            checkin();
            
            doQuery_ref();

            Thread.sleep(3000);
            enviar_datos_inicales();
            
/*
            while(!this.objetivo_encontrado){
                this.buscarObjetivo();
                this.doQuery_ref();
            }
    */        
            System.out.println("El objetivo se encuentra en las coordenadas: (" + this.coord_x_objetivo + "," + this.coord_y_objetivo + ").");

        } catch (InterruptedException | JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);

        }
       
}
    
/******************************************************************************
 * Funciones
 ******************************************************************************/
//git
    @Override
    public AgentID getAid() {
        return super.getAid(); //To change body of generated methods, choose Tools | Templates.
    }
    
    /*
    * @author Ruben
    * Funcion que nos transforma las coordenadas locales del objetivo en valores absolutos
    */
    private void obtenerCoordenadasObjetivo() {
        
        int aux = 0;
        
        // En primer lugar debemos distinguir el tipo de vehiculo que ha encontrado el objetivo
        switch(this.tipoVehiculo){
            
            // En segundo lugar, usamos una variable auxiliar que varia dependiendo del vehiculo que ha encontrado el objetivo.
            // Esta variable nos ayudara a establecer las coordenadas correctas.
            case DRON:
                aux = -1;
                break;
            case COCHE:
                aux = -2;
                break;
            case CAMION:
                aux = -5;
                break;
        }
        
        // Obtenemos la coordenada 'x' del objetivo en valores absolutos
        this.coord_x_objetivo = this.x-aux+this.coord_x_objetivo;
        
        // Hacemos lo mismo con la coordenada 'y'
        this.coord_y_objetivo = this.y-aux+this.coord_y_objetivo;
        
    }
    
    /**
     * @author Ruben
     * Funcion que nos devuelve true, si el vehiculo esta viendo el objetivo en su radar y false en caso contrario
     */
    private boolean objetivoAlAlcance(){
        boolean encontrado = false;
        
        // Recorremos todo el radar del agente, hasta terminar o encontrar el objetivo
        for(int i=0; i < this.range && !encontrado; i++){
            for(int j=0; j < this.range && !encontrado; j++){
                if(sensor.get(i).get(j) == 3){
                    // Una vez encontrado el objetivo, guardamos las coordenadas 'x' e 'y' del mismo
                    encontrado = true;
                    coord_x_objetivo = j;
                    coord_y_objetivo = i;
                }
            }
        }
        
        return encontrado;
    }
    
    /**
    * @author Ruben 
    * Funcion para buscar el objetivo. Puede ser un coche, un camion o un dron. Cada uno lo buscará de una manera diferente.
    *
    */

    private void buscarObjetivo(){

	String next_move = "";
	
        // ESTRATEGIA de REFUEL. basica, habra que MODIFICARLA
	if(this.battery <= 5)
            try {
                this.refuel();
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

	// En primer lugar, comprobamos si el objetivo se encuentra al alcance para actuar de una manera u otra.
        this.objetivo_encontrado = objetivoAlAlcance();

        if(this.objetivo_encontrado){
            
            // TRANSFORMAR LAS COORDENADAS DEL OBJETIVO EN VALORES ABSOLUTOS
            obtenerCoordenadasObjetivo();
            
            // Notificamos al lider que hemos encontrado el objetivo y nos dirigimos hacia el

            // NOTIFICAR AL LIDER DE LAS COORDENADAS ABSOLUTAS DEL OBJETIVO
            
            // FIN DE LA BUSQUEDA DEL OBJETIVO

             //INICIO DEL COMPORTAMIENTO PARA IR HASTA EL OBJETIVO

            // Dependiendo de donde se encuentre el objetivo nos moveremos en una direccion u otra
            if(this.coord_x_objetivo < this.x){
                
                // Si el OBJETIVO se encuentra a nuestra IZQUIERDA, nos moveremos hacia el OESTE
                // ¿Hacia el NW, W o SW? Depende de si la coordenada 'y' del objetivo es MENOR, MAYOR o IGUAL que la del vehiculo
                
                if(this.coord_y_objetivo < this.y)
                    next_move = "moveNW";
                
                else if(this.coord_y_objetivo == this.y)
                    next_move = "moveW";
                
                else
                    next_move = "moveSW";
            }
            else if(this.coord_x_objetivo == this.x){
                
                // Si el OBJETIVO se encuentra en nuestra misma coordenada 'x', entonces, solo puede estar encima o debajo nuestra.
                // Por lo tanto, nos tendremos que mover o al SUR o al NORTE. De nuevo, depende del valor de la coordenada 'y'
                
                if(this.coord_y_objetivo < this.y)
                    next_move = "moveN";
            
                else
                    next_move = "moveS";
            }
            else{
                
                // La ultima opcion es que el OBJETIVO se encuentra a nuestra DERECHA, en tal caso debemos de ir al ESTE.
                // ¿Hacia el NE, E o SE? Depende de si la coordenada 'y' del objetivo es MENOR, MAYOR o IGUAL que la del vehiculo
                if(this.coord_y_objetivo < this.y)
                    next_move = "moveNE";
                
                else if(this.coord_y_objetivo == this.y)
                    next_move = "moveE";
                
                else
                    next_move = "moveSE";
                    
            } 
        } // FIN DEL IF DE OBJETIVO ENCONTRADO
        else {
            
            // Si no hemos encontrado al objetivo en nuestro radar, debemos elegir una direccion.
            // Para ello, tendremos en cuenta el ultimo movimiento que hicimos.
            
            if(this.last_move.equals("")){
                
                // Si este es el primer movimiento del agente, tendremos que decidir que direccion sera la inicial.
                // En este caso tendremos en cuenta el numero de posiciones libres que hay en cada direccion. La que tenga el mayor numero sera la elegida
    		
                next_move = buscarNuevaDireccion();
                
                //this.checkpoint = new Pair(Agente.x, Agente.y);
                this.last_move = next_move;
            }else {
                
                // Si entramos en este condicinal es porque YA HEMOS IDO EN UNA DIRECCION.
                // Por lo tanto, intentaremos IR EN LA MISMA DIRECCION (siempre que nos sea permitido)
                
                boolean puedo_repetir = puedoRepetirDireccion();
                
                if(puedo_repetir && this.num_pasos <= 15){
                    next_move = this.last_move;
                    this.num_pasos++;
                }
                else{
                    this.num_pasos = 0;
                    next_move = buscarNuevaDireccion();
                    this.last_move = next_move;
                }
                
            } // CIERRE DEL BLOQUE IF/ELSE LAST_MOVE("")
        } // CIERRE DEL ELSE (!encontrado objetivo)
        
        try {
            if(askLider(next_move)){
                 this.performMove(next_move);
            }else{
                // TODO 
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean askLider(String move){
        
        setDestinatario(this.nombreLider);
        outbox.setConversationId("solicitarMovimiento");
        outbox.setInReplyTo(reply_withID);
        outbox.setContent(move);
        outbox.setPerformative(ACLMessage.QUERY_IF);  
        this.send(outbox);   
        
        
        Boolean moverse = false;
        ACLMessage inbox = new ACLMessage();
          try {
               while (queue.isEmpty()){ Thread.sleep(1);};   
                inbox = queue.Pop();
              
            switch (inbox.getPerformativeInt()) {
                case ACLMessage.FAILURE:
                    System.out.println("Failure: " + inbox.getContent());
                    this.reply_withID = inbox.getReplyWith();
                    break;
                case ACLMessage.NOT_UNDERSTOOD:
                    System.out.println("Failure: " + inbox.getContent());
                    this.reply_withID = inbox.getReplyWith();
                    break;
                case ACLMessage.CONFIRM:
                    moverse = true;
                    break;
                case ACLMessage.DISCONFIRM:
                    moverse = false;
                    break;
                default:
                    break;
            }
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
          return moverse; 
    }
    
    /**
     * @author Ruben
     * Funcion que nos devuelve true en caso de que el vehiculo pueda volver a ir en la misma direccion que en el movimiento anterior.
     * False en caso contrario.
     */
    
    private boolean puedoRepetirDireccion(){
        
        // Necesitaremos comprobar la casilla ('x','y') a la que nos dirigiremos en caso de repetir direccion.
        // Dicha casilla depende del tipo de vehiculo que sepamos y de la direccion que hayamos tomado en el ultimo movimiento.
        // Por ultimo, solo podremos ir a dicha casilla si hay un 0 en ella
        
        int coord_x = 0, coord_y = 0;
                
        switch(this.last_move){
            case "moveNW":
                switch(this.tipoVehiculo){
                    case COCHE:
                        coord_y = coord_x = 1;
                                
                        break;
                    case CAMION:
                        coord_y = coord_x = 4;
                                
                        break;
                }
                        
                break;
            case "moveN":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 0;
                        coord_x = 1;
                                
                        break;
                    case COCHE:
                        coord_y = 1;
                        coord_x = 2;
                                
                        break;
                    case CAMION:
                        coord_y = 4;
                        coord_x = 5;
                                
                        break;
                }
                        
                break;
            case "moveNE":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 0;
                        coord_x = 2;
                                
                        break;
                    case COCHE:
                        coord_y = 1;
                        coord_x = 3;
                                
                        break;
                    case CAMION:
                        coord_y = 4;
                        coord_x = 6;
                                
                        break;
                }
                        
                break;
            case "moveW":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 1;
                        coord_x = 0;
                                
                        break;
                    case COCHE:
                        coord_y = 2;
                        coord_x = 1;
                                
                        break;
                    case CAMION:
                        coord_y = 5;
                        coord_x = 4;
                                
                        break;
                }
                        
                break;
            case "moveE":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 1;
                        coord_x = 2;
                                
                        break;
                    case COCHE:
                        coord_y = 2;
                        coord_x = 3;
                                
                        break;
                    case CAMION:
                        coord_y = 5;
                        coord_x = 6;
                                
                        break;
                }
                        
                break;
            case "moveSW":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 2;
                        coord_x = 0;
                                
                        break;
                    case COCHE:
                        coord_y = 3;
                        coord_x = 1;
                                
                        break;
                    case CAMION:
                        coord_y = 6;
                        coord_x = 4;
                                
                        break;
                }
                        
                break;
            case "moveS":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 2;
                        coord_x = 1;
                                
                        break;
                    case COCHE:
                        coord_y = 3;
                        coord_x = 2;
                                
                        break;
                    case CAMION:
                        coord_y = 6;
                        coord_x = 5;
                                
                        break;
                }
                        
                break;
            case "moveSE":
                switch(this.tipoVehiculo){
                    case DRON:
                        coord_y = 2;
                        coord_x = 2;
                                
                        break;
                    case COCHE:
                        coord_y = 3;
                        coord_x = 3;
                                
                        break;
                    case CAMION:
                        coord_y = 6;
                        coord_x = 6;
                                
                        break;
                }
                        
                break;
        }
                
        if(this.sensor.get(coord_y).get(coord_x) == 0)
            return true;
        else
            return false;
            
    }

    /**
    * @author Ruben 
    * Funcion que utilizamos cuando el vehiculo debe buscar una direccion por primera vez 
    * o cuando esta buscando una nueva direccion que tomar ya que no puede volver a ir en la misma direccion que la ultima vez
    */
    
    private String buscarNuevaDireccion(){
        
        // La logica sera DIFERENTE para cada tipo de vehiculo. En el dron no tendremos en cuenta los obstaculos, en el resto si.
        
        // Comenzamos con un vector con todas las direcciones posibles. El vector sera de datos de tipo 'boolean'.
        // Si la direccion 'i' se encuentra a 'true' significa que es una direccion que puede tomar el vehiculo. En caso de que sea
        // 'false' significa que el vehiculo no puede tomarla, bien porque hay un obstaculo, bien porque se encuentra el limite
        // del mundo.
        // Las direcciones se indexaran del siguiente modo:
        //  posicion 0: moveNW
        //  posicion 1: moveN
        //  posicion 2: moveNE 
        //  posicion 3: moveW
        //  posicion 4: moveE
        //  posicion 5: moveSW
        //  posicion 6: moveS
        //  posicion 7: moveSE
        
        boolean[] posibles_movimientos = new boolean[8];
        
        for(int i=0; i < 8; i++)
            posibles_movimientos[i] = true;
        
        // Ahora recorremos el radar del vehiculo, si en alguna de las direcciones nos encontramos un obstaculo o
        // el limite del mundo, pondremos en la posicion del vector de dicha direccion a 'false'.
        // Dependiendo de cual sea el vehiculo, recorremos unas posiciones u otras

        int inicio = 0, fin = 0, centro = 0;
        
        switch(this.tipoVehiculo){
            case DRON:
                // El caso del dron es particular y las unicas direcciones que tendra prohibidas son aquellas que
                // tienen el limite del mundo
                
                int aux = 0;
                
                for(int i=0; i < 3; i++)
                    for(int j=0; j < 3; j++){
                        
                        if(this.sensor.get(i).get(j)==2)
                            posibles_movimientos[aux] = false;
                        
                        aux++;
                    }
                
                break;
            case COCHE:
                inicio = 1;
                fin = 3;
                centro = 2;
                break;
            case CAMION:
                inicio = 4;
                fin = 6;
                centro = 5;
                break;
        }
        
        int aux = 0;
        
        for(int i=inicio; i <= fin; i++)
            for(int j=inicio; j <= fin; j++){
                
                if(i != centro || j != centro){
                    int lectura = this.sensor.get(i).get(j);
                
                    if(lectura == 1 || lectura == 2)
                        posibles_movimientos[aux] = false;
                
                    aux++;
                }
            }
        
        // Contamos el numero de posiciones libres que hay en cada una de las direcciones admitidas para el vehiculo
        
        int maximo = 0, indice_direccion = 0;
        
        int indice_prohibido = 7 - this.indice_ultima_direccion;
        
        for(int k=0; k < 8; k++)
            if(posibles_movimientos[k] && k != indice_prohibido){
                
                // Si la direccion esta admitida, contamos el numero de casillas libres que hay en esa direccion.
                // Dependiendo de la direccion que sea y del vehiculo, tendremos que recorrer unas casillas del sensor u otras
                
                int inicio_x = 0, inicio_y = 0, fin_x = 0, fin_y = 0, contador = 0;
                Random n = new Random();
                
                switch(k){
                    case 0:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = inicio_y = 0;
                                fin_x = fin_y = 1;
                                break;
                            case CAMION:
                                inicio_x = inicio_y = 0;
                                fin_x = fin_y = 4;
                                break;
                        }
                        
                        break;
                    case 1:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 1;
                                inicio_y = 0;
                                fin_x = 3;
                                fin_y = 1;
                                break;
                            case CAMION:
                                inicio_x = 3;
                                inicio_y = 0;
                                fin_x = 7;
                                fin_y = 4;
                                break;
                        }
                        
                        break;
                    case 2:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 3;
                                inicio_y = 0;
                                fin_x = 4;
                                fin_y = 1;
                                break;
                            case CAMION:
                                inicio_x = 6;
                                inicio_y = 0;
                                fin_x = 10;
                                fin_y = 4;
                                break;
                        }
                        
                        break;
                    case 3:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 0;
                                inicio_y = 1;
                                fin_x = 1;
                                fin_y = 3;
                                break;
                            case CAMION:
                                inicio_x = 0;
                                inicio_y = 3;
                                fin_x = 4;
                                fin_y = 7;
                                break;
                        }
                        
                        break;
                    case 4:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 3;
                                inicio_y = 1;
                                fin_x = 4;
                                fin_y = 3;
                                break;
                            case CAMION:
                                inicio_x = 6;
                                inicio_y = 3;
                                fin_x = 10;
                                fin_y = 7;
                                break;
                        }
                        
                        break;
                    case 5:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 0;
                                inicio_y = 3;
                                fin_x = 1;
                                fin_y = 4;
                                break;
                            case CAMION:
                                inicio_x = 0;
                                inicio_y = 6;
                                fin_x = 4;
                                fin_y = 10;
                                break;
                        }
                        
                        break;
                    case 6:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = 1;
                                inicio_y = 3;
                                fin_x = 3;
                                fin_y = 4;
                                break;
                            case CAMION:
                                inicio_x = 3;
                                inicio_y = 6;
                                fin_x = 7;
                                fin_y = 10;
                                break;
                        }
                        
                        break;
                    case 7:
                        switch(this.tipoVehiculo){
                            case DRON:
                                contador = n.nextInt();
                                break;
                            case COCHE:
                                inicio_x = inicio_y = 3;
                                fin_x = fin_y = 4;
                                break;
                            case CAMION:
                                inicio_x = inicio_y = 6;
                                fin_x = fin_y = 10;
                                break;
                        }
                        
                        break;
                } // FIN DEL SWITCH
                
                // Recorremos las casillas correspondientes y contamos el numero de casillas libres
                
                for(int i=inicio_y; i <= fin_y; i++)
                    for(int j=inicio_x; j <= fin_x; j++)
                        if(this.sensor.get(i).get(j) == 0)
                            contador++;
                
                // Si el numero de casillas es mayor que el maximo, actualizamos el maximo 
                // y guardamos el indice de dicha direccion
                if(contador > maximo){
                    maximo = contador;
                    indice_direccion = k;
                } 
                else if(contador == maximo){
                    int r = n.nextInt(2);
                    
                    if(r == 1)
                        indice_direccion = k;
                }
            } // CIERRE DEL IF
        
        // Ya solo falta obtener la direccion elegida a partir del indice anterior
        
        String direccion = "";
        
        switch(indice_direccion){
            case 0:
                direccion = "moveNW";
                break;
            case 1:
                direccion = "moveN";
                break;
            case 2:
                direccion = "moveNE";
                break;
            case 3:
                direccion = "moveW";
                break;
            case 4:
                direccion = "moveE";
                break;
            case 5:
                direccion = "moveSW";
                break;
            case 6:
                direccion = "moveS";
                break;
            case 7:
                direccion = "moveSE";
                break;
        }
        
        this.indice_ultima_direccion = indice_direccion;
        
        return direccion;
    }
    
    /*
    private void parsearSensor() {
        ACLMessage inbox = new ACLMessage();
        
        //HE cambiado esto.
        String fuente = inbox.getContent();
        
        // Parseamos el String original y lo almacenamos en un objeto
        JsonObject objeto = Json.parse(fuente).asObject();
        
        // Extraemos los valores asociados a la clave "result"."sensor"
        //System.out.println("Datos recibidos del sensor:");
        
        /*for(JsonValue j : objeto.get("result").asObject().get("sensor").asArray()){
            System.out.println(j.asInt() + ", ");
        }* /
        
        JsonArray vector = objeto.get("result").asObject().get("sensor").asArray();
        
        int aux = 0;
        
        for(int i=0; i < 5; i++){
            for(int j=0; j < 5; j++){
                Agente.sensor[i][j] = vector.get(aux).asInt();
                
                aux++;
            }
        }
        
        System.out.println("Datos guardados en nuestro sensor propio:");
        
        for(int i=0; i < 5; i++){
            System.out.println(Agente.sensor[i][0] + ", " + Agente.sensor[i][1] + ", " + Agente.sensor[i][2] + ", " + Agente.sensor[i][3] + ", " + Agente.sensor[i][4] + ", ");
        }
    }
    */
    /**
    * @author Dani
    */
    public void setDestinatario(String nombre){
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(nombre));   
    };
    
    /**
     * @author Ruben
     * 
     * Funcion para obtener el id de la conversacion y el reply with de un mensaje de respuesta de la plataforma
     * /
    private void obtenerDatos(){
        this.conversationID = this.inbox.getConversationId();
        this.reply_withID = this.inbox.getReplyWith();
    }
    
    * */
    /**
    * @author Dani, Oleksandr 
    */
    
    public boolean subscribe() throws InterruptedException{
        JSONObject jsonLogin = new JSONObject();
        
        try {
            jsonLogin.put("world", "map1");
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        setDestinatario("Bellatrix");
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.SUBSCRIBE);  
        this.send(outbox);     
        System.out.println("\n["+this.getName()+"] Mensaje: "+ outbox.getPerformative() + " enviado");

          try {
              
                while (queue.isEmpty()){
                   Thread.sleep(1);
                };
                 ACLMessage inb = queue.Pop();
                System.out.println("\n["+this.getName()+"] Mensaje : "+ inb.getPerformative() + " Recibido");

              
              if (inb.getPerformativeInt() == ACLMessage.FAILURE || inb.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inb.getContent() + " - " + this.getName());        
              }
              if (inb.getPerformativeInt() == ACLMessage.INFORM){
                  this.conversationID = inb.getConversationId();
                  System.out.println("Aceptada " + this.conversationID + " - " + this.getName());
                  enviar_clave_lider();
                
              }
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return !"".equals("");     
    };

 
    /**
    * @author Dani, nacho
    */

    public boolean checkin() throws InterruptedException, JSONException{

        JSONObject jsonLogin = new JSONObject();
        
        try {
            jsonLogin.put("command", "checkin");
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Enviando checkin...");
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(this.conversationID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     
        System.out.println("aqui llego");
          try {
              
                while (queue.isEmpty()){Thread.sleep(1);}; 
                ACLMessage inb = queue.Pop();
                System.out.println("MENSAJER COMPLETO: " +
                   "Conversation ID: " + inb.getConversationId()
                   + " SENDER: " + inb.getSender().name
                   + " REPLY WITH: " + inb.getReplyWith()
                   + "Performativa: " + inb.getPerformative()
                         
                );
                
            switch (inb.getPerformativeInt()) {
                case ACLMessage.FAILURE:
                case ACLMessage.NOT_UNDERSTOOD:
                    System.out.println("Failure: " + inb.getContent());
                    this.reply_withID = inb.getReplyWith();
                    cancel();
                    break;
                case ACLMessage.INFORM:
                    this.conversationID = inb.getConversationId();
                    this.reply_withID = inb.getReplyWith();
                    JSONObject json = new JSONObject(inb.getContent());
                    if(json.has("capabilities")){
                        JSONObject json2 = new JSONObject(json.get("capabilities").toString());
                        int fuel= json2.getInt("fuelrate");
                        inicializarTipoVehiculo(fuel);
                    } else {
                        cancel();
                    } break;
                default:
                    System.out.println("No es de ningun tipo");
                    break;
            }
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }

         System.out.println("Soy un:" + this.tipoVehiculo + " Y soy el agente: " + this.getName());
        return !"".equals("");
        
    }

    /**
    * @author Dani
    */

    public boolean refuel() throws InterruptedException{

        JSONObject jsonLogin = new JSONObject();
        
        try {
            jsonLogin.put("command", "refuel");
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(this.conversationID);
        outbox.setInReplyTo(reply_withID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     

          try {
            while (queue.isEmpty()){Thread.sleep(1);};
        
           ACLMessage inbox = queue.Pop();              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println(" - Failure: " + inbox.getContent());  
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println(" - INFORM: " + inbox.getContent());     
                  this.reply_withID = inbox.getReplyWith();
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        return !"".equals("");
        
    };

    
    
    /**
    * @author Dani,
    */

    public boolean performMove(String movimiento) throws InterruptedException{
        
        JSONObject jsonLogin = new JSONObject();
        
        try {
            jsonLogin.put("command", movimiento);
            
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(conversationID);
        outbox.setInReplyTo(reply_withID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     

          try {
              while (queue.isEmpty()){Thread.sleep(1);};
        
                ACLMessage inbox = queue.Pop();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent()); 
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM: " + inbox.getContent());   
                  this.reply_withID = inbox.getReplyWith();
                  
                  // ACTUALIZAMOS LA BATERIA. En este caso es un COCHE, por lo que TRAS CADA MOVIMIENTO, la bateria se reduce en 4 UNIDADES
                  this.battery -= 4;
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }

        return !"".equals("");
        
    };

    /**
    * @author Dani, nacho
    */


    public ACLMessage doQuery_ref() throws InterruptedException, JSONException{
        setDestinatario("Bellatrix");
        outbox.setConversationId(conversationID);
        outbox.setInReplyTo(reply_withID);
        outbox.setContent("");
        outbox.setPerformative(ACLMessage.QUERY_REF);  
        this.send(outbox);     
        ACLMessage inbox = new ACLMessage();
          try {
               while (queue.isEmpty()){ Thread.sleep(1);};   
                inbox = queue.Pop();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent());
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM: " + inbox.getContent() + " con REPLY-ID: " + inbox.getReplyWith()); 
                  this.reply_withID = inbox.getReplyWith();
                  
                  JSONObject json = new JSONObject(inbox.getContent());
                  JSONObject json2 = new JSONObject(json.get("result").toString());
                  this.battery = Integer.parseInt(json2.get("battery").toString());
                  this.x = Integer.parseInt(json2.get("x").toString());
                  this.y = Integer.parseInt(json2.get("y").toString());
                  
                  JsonObject objetoSensor = Json.parse(inbox.getContent()).asObject();
                  objetoSensor = objetoSensor.get("result").asObject();
                  JsonArray vectorSensor = objetoSensor.get("sensor").asArray();
                  leerSensor(vectorSensor);
                  System.out.println("Datos guardados: battery: " + this.battery + " x: " +this.x + " y: " + this.y ); 
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return inbox;
        
    }
    
    /**
    * @author nacho
    */
    private void leerSensor(JsonArray respuesta) throws JSONException{
        
        // Mostramos los datos proporcionados por los sensores
        System.out.println("Los datos recibidos son: ");
        System.out.println("\n Radar: ");
        
        sensor = new ArrayList<ArrayList<Integer>>();
       
        int contador = 0;
        String print = "";
        for(int i = 0; i<range; i++){
            sensor.add(new ArrayList<Integer>());
            for(int j = 0; j<range; j++){
                JsonValue  value = respuesta.get(contador);
                int a = value.asInt();
                sensor.get(i).add(a);
                print+= sensor.get(i).get(j) + " ";
                contador++;
            }
            System.out.println(print);
            print = "";
          }
              
    }
    
    /**
    * @author nacho
    */
    private void inicializarTipoVehiculo(int fuel){
        switch(fuel){
            case 2:
                tipoVehiculo = TipoVehiculo.DRON;
                fuelRate = 2;
                fly = true;
                range = 3;
                centro_mapa = 1;
                break;
            
            case 1: 
                tipoVehiculo = TipoVehiculo.COCHE;
                fuelRate = 1;
                fly = false;
                range = 5;
                centro_mapa = 2;
                break;
                
            case 4: 
                tipoVehiculo = TipoVehiculo.CAMION;
                fuelRate = 4;
                fly = false;
                range = 11;
                centro_mapa = 5;
                break;
                
        }
        
        
                
    }
    
      public boolean askForConversationID() throws InterruptedException{
 
   

        setDestinatario(this.nombreLider);
        outbox.setPerformative(ACLMessage.REQUEST); 
        outbox.setConversationId("sendKey");
        this.send(outbox);

        while (queue.isEmpty()){
           Thread.sleep(1);
        };
        
        ACLMessage inb = queue.Pop();
            if (inb.getPerformativeInt() == ACLMessage.FAILURE ){
                System.out.println("No hay ID todavia.");
                return false;
            }
            if (inb.getPerformativeInt() == ACLMessage.INFORM){
                  this.conversationID = inb.getConversationId();
                  System.out.println("Aceptada " + this.conversationID);
            } 
          
        
        return true;
        
    };
    
    /**
    * @author Dani
    */
      
    private void enviar_clave_lider() {
        setDestinatario(Agente.nombreLider);
        outbox.setConversationId("sendKey");
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(this.conversationID);
        this.send(outbox);     

       
    }
    

    /**
    * @author Dani
    */
     private void enviar_datos_inicales() {
        
        System.out.println("Envio datos Iniciales");

        setDestinatario(Agente.nombreLider);
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(this.x + "," + this.y + "," + this.tipoVehiculo);
        outbox.setConversationId("DatosI");
        this.send(outbox);     

        System.out.println("He enviado los datos iniciales " + this.x + "," + this.y + "," + this.tipoVehiculo);
    }
    
    /**
    * @author Dani
    */
    public void onMessage(ACLMessage msg)  {
        try {
            queue.Push(msg);
            System.out.println("\n["+this.getName()+"] Encolando: "+ msg.getPerformative() + " de " + msg.getSender().name );
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void cancel(){
    
    }
    
}