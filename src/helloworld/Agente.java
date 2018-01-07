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
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import javafx.util.Pair;


/**
 *
 * @author Dani, nacho, ruben
 */
public class Agente extends SingleAgent {
 
    public static final String MAPA = "map3";
    private static String nombreLider = "Liderrr1";
    private ACLMessage outbox;
    private String conversationID;
    private String reply_withID;
    private int badp_count;
    
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
    private int num_pasos;
    private Integer indice_ultima_direccion;
    private int contador;
    private int contador_movimientos;
    private boolean movimiento_paralelo;
    private int c;
    
    private boolean objetivo_encontrado;
    
    private int centro_mapa;
    private boolean finalizado;
    private int energy;
    

    public Agente() throws Exception {
        super(null);
      
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        this.finalizado = false;
        this.outbox = null;
        this.badp_count = 0;
        queue = new MessageQueue(300);
        
        
        this.conversationID = "";
        this.reply_withID = "";
        System.out.println("\nHola Mundo soy un agente llamado " + this.getName());

        battery = 100;
        mapa = new ArrayList();
        
        this.last_move = "";
        this.coord_x_objetivo = 0;
        this.coord_y_objetivo = 0;
        this.num_pasos = 0;
        this.indice_ultima_direccion = null;
        this.contador=0;
        this.objetivo_encontrado = false;
        this.enObjetivo = false;
        this.movimiento_paralelo = false;
        this.c = 0;
        
        this.contador_movimientos = 0;
    }
   
    /**
     * @author grego ruben, dani, nacho, kudry
     */
    @Override
    public void execute(){
     
        if(!finalizado){
            try {

            if(!askForConversationID()){
                Thread.sleep(3000);
                subscribe();
            }

                checkin();

                doQuery_ref();
                Thread.sleep(3000);
                
                enviar_datos_inicales();

                System.out.println("-------------Procedemos a buscar el objetivo--------------------");
                while(!objetivo_encontrado && !finalizado){

                    this.nuevaLogica();
                    this.doQuery_ref();
                    if(contador%10 == 0 && !objetivo_encontrado){
                        solicitarCoordenadasObjetivo();
                        System.out.println("Solicitando coordenadas del objetivo");
                    }
                    contador++;
                    System.out.println("----------------iteraciones: " + contador);
                    if(this.badp_count > 30){
                        ejecutarCancel();
                    }
                }      


                System.out.println("El objetivo se encuentra en las coordenadas: (" + this.coord_x_objetivo + "," + this.coord_y_objetivo + ").");

                while(!this.enObjetivo  && !finalizado){
                    System.out.println("hacia el objetivo.");
                    int indice_direccion = irHaciaObjetivo();
                    String next_move = this.traducirIndiceDireccion(indice_direccion);
                    performMove(next_move);
                    this.doQuery_ref();
                    if(this.badp_count > 30){
                          ejecutarCancel();
                    }
                }
                
            ejecutarCancel();

            } catch (InterruptedException | JSONException ex) {
                Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);

            }
            System.out.println("Agente [" +this.getName()+ "] finalizado.");
        }
       
}
    
/******************************************************************************
 * Funciones
 ******************************************************************************/

    @Override
    public AgentID getAid() {
        return super.getAid(); //To change body of generated methods, choose Tools | Templates.
    }
    
    /*
    * @author Ruben
    * Funcion que nos transforma las coordenadas locales del objetivo en valores absolutos
    */
    private void obtenerCoordenadasObjetivo() {
        
        int centro = 0;
        int x_local = this.coord_x_objetivo;
        int y_local = this.coord_y_objetivo;
        
        // En primer lugar debemos distinguir el tipo de vehiculo que ha encontrado el objetivo
        switch(this.tipoVehiculo){
            
            // En segundo lugar, usamos una variable auxiliar que varia dependiendo del vehiculo que ha encontrado el objetivo.
            // Esta variable nos ayudara a establecer las coordenadas correctas.
            case DRON:
                centro = 1;
                break;
            case COCHE:
                centro = 2;
                break;
            case CAMION:
                centro = 5;
                break;
        }

        if(this.coord_x_objetivo < centro){
            if(this.coord_y_objetivo < centro){
                // EL OBJETIVO SE ENCUENTRA EN EL PRIMER CUADRANTE
                System.out.println("El objetivo se encuentra en el PRIMER CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            } else if(this.coord_y_objetivo > centro){
                // EL OBJETIVO SE ENCUENTRA EN EL TERCER CUADRANTE
                System.out.println("El objetivo se encuentra en el TERCER CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            }
        } else if(this.coord_x_objetivo > centro){
            if(this.coord_y_objetivo < this.y){
                // EL OBJETIVO SE ENCUENTRA EN EL SEGUNDO CUADRANTE
                System.out.println("El objetivo se encuentra en el SEGUNDO CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            } else if(this.coord_y_objetivo > centro){
                // EL OBJETIVO SE ENCUENTRA EN EL CUARTO CUADRANTE
                System.out.println("El objetivo se encuentra en el CUARTO CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            }
        } else if(this.coord_x_objetivo == centro){
            // EL OBJETIVO SE ENCUENTRA EN LA MISMA COLUMNA QUE EL VEHICULO
            System.out.println("El objetivo se encuentra en LA MISMA COLUMNA QUE EL VEHICULO");
            this.coord_x_objetivo = this.x;
            this.coord_y_objetivo = this.y - centro + y_local;
        }
        
        if(this.coord_y_objetivo == centro){
            // EL OBJETIVO SE ENCUENTRA EN LA MISMA FILA QUE EL VEHICULO
            System.out.println("El objetivo se encuentra en LA MISMA FILA QUE EL VEHICULO");
            this.coord_x_objetivo = this.x - centro + x_local;
            this.coord_y_objetivo = this.y;
        }
        
        
    }
    
    /**
     * @author Ruben
     * Funcion que nos devuelve true, si el vehiculo esta viendo el objetivo en su radar y false en caso contrario
     */
    private boolean objetivoAlAlcance(){
        boolean encontrado = false;
        
        // Recorremos todo el radar del agente, hasta terminar o encontrar el objetivo
        for(int i = 0; i < this.range && !encontrado; i++){
            for(int j = 0; j < this.range && !encontrado; j++){
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
     * Metodo que envia al lider sobre las coordenadas absolutas del objetivo encontrado
     */
    private void enviarCoordenadasObjetivo(){
        
        System.out.println("El objetivo ha sido encontrado. Enviando las coordenadas al lider...");
        
        ACLMessage out = new ACLMessage();
        out.setSender(this.getAid());
        out.setReceiver(new AgentID(Agente.nombreLider));
        
        JSONObject content = new JSONObject();
        
        try {
            content.put("x", this.coord_x_objetivo);
            content.put("y", this.coord_y_objetivo);
        } catch (JSONException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        out.setContent(content.toString());
        out.setConversationId("envioCoordenadasObjetivo");
        out.setPerformative(ACLMessage.INFORM);
        
        this.send(out);
    }
    
    
    /**
     * @author Ruben
     * Funcion que coge el indice de una direccion y nos lo traduce en la direccion real que tendra que tomar el vehiculo. Seguira el siguiente patron:
     *  posicion 0: moveNW
     *  posicion 1: moveN
     *  posicion 2: moveNE 
     *  posicion 3: moveW
     *  posicion 4: moveE
     *  posicion 5: moveSW
     *  posicion 6: moveS
     *  posicion 7: moveSE
     * @param indice_direccion
     * @return direccion
     */
    
    private String traducirIndiceDireccion(int indice){
        String direccion = "";
        
        switch(indice){
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
        
        return direccion;
    }
    
    /**
     * @author dani, nacho
     * @param move
     * @return 
     */
    private boolean askLider(String move){
        System.out.println("llego a enviar al lider");
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
            jsonLogin.put("world", MAPA);
            
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
    * @author Dani
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
                  this.badp_count++;
                   System.out.println("Bad count: " + this.badp_count ) ;


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
                this.badp_count++;
                System.out.println("Bad count: " + this.badp_count ) ;
            }
            
            else if (inbox.getPerformativeInt() == ACLMessage.INFORM){ 

                System.out.println("INFORM: " + inbox.getContent() + " con REPLY-ID: " + inbox.getReplyWith()); 
                this.reply_withID = inbox.getReplyWith();

                JSONObject json = new JSONObject(inbox.getContent());
                JSONObject json2 = new JSONObject(json.get("result").toString());
                this.battery = Integer.parseInt(json2.get("battery").toString());
                this.x = Integer.parseInt(json2.get("x").toString());
                this.y = Integer.parseInt(json2.get("y").toString());
                this.energy = Integer.parseInt(json2.get("energy").toString());
                JsonObject objetoSensor = Json.parse(inbox.getContent()).asObject();
                objetoSensor = objetoSensor.get("result").asObject();
                JsonArray vectorSensor = objetoSensor.get("sensor").asArray();
                leerSensor(vectorSensor);
                System.out.println("Datos guardados: battery: " + this.battery + " x: " +this.x + " y: " + this.y );

                if(!"".equals(this.last_move)){
                    System.out.println("Hacemos peticion para actualizar mapa");

                    setDestinatario(Agente.nombreLider);
                    outbox.setPerformative(ACLMessage.INFORM);  
                    outbox.setContent(this.x + "-" + this.y + "-" + this.getName() + "-" + this.sensor + "-" + this.energy);
                    outbox.setConversationId("DatosSensor");
                    this.send(outbox);
                    while (queue.isEmpty()){Thread.sleep(1);};
                    ACLMessage inb = queue.Pop();
                    if (inb.getPerformativeInt() == ACLMessage.INFORM ){
                        System.out.println("Se ha actualizado el mapa en agente y lider");
                    }
                }

            }
            
            else if(inbox.getPerformativeInt() == ACLMessage.AGREE){
                JSONObject json = new JSONObject(inbox.getContent());
                coord_x_objetivo = Integer.parseInt(json.get("x").toString());
                coord_y_objetivo = Integer.parseInt(json.get("y").toString());

                if(coord_x_objetivo != 0 && coord_y_objetivo != 0){
                    objetivo_encontrado = true;
                }
                System.out.println("Recibiendo coordenadas del objetivo");
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
    
    /**
     * @author Dani
     * @return
     * @throws InterruptedException 
     */
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
    * @author
    */
    private void solicitarCoordenadasObjetivo(){
        setDestinatario(Agente.nombreLider);
        outbox.setConversationId("solicitarCoordenadasObjetivo");
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(this.conversationID);
        this.send(outbox);
    }

    /**
    * @author Dani
    */
     private void enviar_datos_inicales() throws InterruptedException {
        
        System.out.println("Envio datos Iniciales");

        setDestinatario(Agente.nombreLider);
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(this.x + "," + this.y + "," + this.tipoVehiculo);
        outbox.setConversationId("DatosI");
        this.send(outbox);     
        
        while (queue.isEmpty()){
           Thread.sleep(1);
        };
        
        ACLMessage inb = queue.Pop();
            if (inb.getPerformativeInt() == ACLMessage.INFORM ){ 
        }
        System.out.println("datos iniciales recibidos por lider " + this.x + "," + this.y + "," + this.tipoVehiculo);
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


    /**
     * @author Nacho, grego, dani, ruben, kudry
     * @throws InterruptedException 
     */
    private void nuevaLogica() throws InterruptedException {
        
        String next_move = ""; 
	
        // ESTRATEGIA de REFUEL. basica, habra que MODIFICARLA
	if(this.battery <= 10)
            try {
                this.refuel();
            } catch (InterruptedException ex) {
                Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

        
	// En primer lugar, comprobamos si el objetivo se encuentra al alcance para actuar de una manera u otra.
        this.objetivo_encontrado = objetivoAlAlcance();
       

        if(this.objetivo_encontrado){
            
            System.out.println("OBJETIVO ENCONTRADO!!!");
            
            System.out.println("Las coordenadas locales del objetivo son: (" + this.coord_x_objetivo + "," + this.coord_y_objetivo + ")");
            
            // TRANSFORMAR LAS COORDENADAS DEL OBJETIVO EN VALORES ABSOLUTOS
            System.out.println("Transformando las coordenadas del objetivo...");
            obtenerCoordenadasObjetivo();
                       
            System.out.println("Las coordenadas absolutas del objetivo son: (" + this.coord_x_objetivo + "," + this.coord_y_objetivo + ")");
            
            // Notificamos al lider que hemos encontrado el objetivo y nos dirigimos hacia el

            // NOTIFICAR AL LIDER DE LAS COORDENADAS ABSOLUTAS DEL OBJETIVO
            enviarCoordenadasObjetivo();
            
            // FIN DE LA BUSQUEDA DEL OBJETIVO

            int indice_direccion = irHaciaObjetivo();
            next_move = this.traducirIndiceDireccion(indice_direccion);
        }
        else {
            
            boolean[] posibles_movimientos = new boolean[8];
            
            for(int i=0; i < 8; i++)
                posibles_movimientos[i] = true;
            
            
        
            // Ahora recorremos el radar del vehiculo, si en alguna de las direcciones nos encontramos un obstaculo o
            // el limite del mundo, pondremos en la posicion del vector de dicha direccion a 'false'.
            // Dependiendo de cual sea el vehiculo, recorremos unas posiciones u otras

            int inicio = 0, fin = 0, centro = 0;
        
            switch(this.tipoVehiculo){
                case DRON:
                    inicio = 0;
                    fin = 2;
                    centro = 1;
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
        
            for(int i = inicio; i <= fin; i++){
                for(int j = inicio; j <= fin; j++){
                
                    if(i != centro || j != centro){
                        int lectura = this.sensor.get(i).get(j);
                        //int p = this.buscarLimite();
                
                        if((lectura == 1 && (this.tipoVehiculo == TipoVehiculo.COCHE 
                                || this.tipoVehiculo == TipoVehiculo.CAMION))  || lectura == 2)
                            posibles_movimientos[aux] = false;
                        
                        //if(p != -1){
                        //    posibles_movimientos[p] = false;
                        //}
                        aux++;
                    }
                }
            }
        
            // Ya tenemos los posibles movimientos
            
            // Si hemos tomado una direccion y podemos seguir en la misma
            if(this.indice_ultima_direccion != null && posibles_movimientos[this.indice_ultima_direccion] && this.c < this.range*2.5){
                
                next_move = this.traducirIndiceDireccion(this.indice_ultima_direccion);
                
                if(this.indice_ultima_direccion == 1 || this.indice_ultima_direccion == 6)
                    this.c++;
                //this.contador_movimientos++;
                // Se ejecuta si no estoy haciendo movimientos paralelos y cuando estoy haciendo movimientos paralelos y no me he pasado del contador
                //if(!this.movimiento_paralelo || (this.movimiento_paralelo && this.contador_movimientos < this.range)){
                //    next_move = this.traducirIndiceDireccion(this.indice_ultima_direccion);
                //    this.contador_movimientos++;
                //}
            } else {
                
                this.c = 0;
                this.movimiento_paralelo = false;
                
                if(this.indice_ultima_direccion != null){
                    posibles_movimientos[7-this.indice_ultima_direccion] = false;
                }
            
                Integer indice_direccion = null;
                boolean encontrado = false;
                ArrayList<Integer> direcciones = new ArrayList();
        
                for (int i=0; i < posibles_movimientos.length; i++){
                    if(posibles_movimientos[i] == true){
                        //encontrado = true;
                        direcciones.add(i);
                    }
                }
                
                Iterator<Integer> it = direcciones.iterator();
                
                while(it.hasNext()){
                    Integer i = it.next();
                    
                    if(i == 1 || i == 3 || i == 4 || i == 6){
                        it.remove();
                    }
                }
                
                Random r = new Random();
                
                indice_direccion = direcciones.get(r.nextInt(direcciones.size()));
                
                // Ya tenemos calculada la horizontal
                next_move = traducirIndiceDireccion(indice_direccion);
                this.indice_ultima_direccion = indice_direccion;
                
                
                //this.contador_movimientos = 0;
                
                int punto_cardinal = buscarLimite();
                
                if(punto_cardinal != -1){
                    this.contador_movimientos++;
                    
                    if(this.contador_movimientos == 3){
                        if(posibles_movimientos[1]){
                            next_move = "moveN";
                            this.indice_ultima_direccion = 1;
                            this.c++;
                        } else {
                            next_move = "moveS";
                            this.indice_ultima_direccion = 6;
                            this.c++;
                        }
                            
                        
                        this.contador_movimientos = 0;
                    }
                }
                /*if(punto_cardinal > 0){
                    switch(punto_cardinal){
                        case 1:
                            this.contador_movimientos = 0;
                            next_move = "moveS";
                            this.indice_ultima_direccion = 6;
                            this.movimiento_paralelo = true;
                            break;
                        case 4:
                            this.contador_movimientos = 0;
                            next_move = "moveW";
                            this.indice_ultima_direccion = 3;
                            this.movimiento_paralelo = true;
                            break;
                        case 3:
                            this.contador_movimientos = 0;
                            this.indice_ultima_direccion = 4;
                            next_move = "moveE";
                            this.movimiento_paralelo = true;
                        case 6:
                            this.contador_movimientos = 0;
                            this.indice_ultima_direccion = 1;
                            next_move = "moveN";
                            this.movimiento_paralelo = true;
                            break;
                        case -1:
                            next_move = traducirIndiceDireccion(indice_direccion);
                            this.indice_ultima_direccion = indice_direccion;
                            break;
                    }
                }*/
            }
        }
        
        System.out.println("La direccion escogida es: " + next_move);
        
        
        if(this.contador %10 == 0){
            preguntarSiHayObjetivo();
        }
        if(askLider(next_move))
            this.performMove(next_move);
    }

    /**
     * @author ruben, grego, nacho
     * @return 
     */
    private int irHaciaObjetivo() {
        
        
        // ESTRATEGIA de REFUEL. basica, habra que MODIFICARLA
	if(this.battery <= 10){
            try {
                this.refuel();
            } catch (InterruptedException ex) {
                Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(this.x < this.coord_x_objetivo){
            
            // Estamos a la IZQ del objetivo
            if(this.y < this.coord_y_objetivo){
                // Estamos ENCIMA del objetivo. Nos movemos hacia el SE
                System.out.println("La direccion escogida es SE");
                return 7;
            } else if(this.y > this.coord_y_objetivo){
                // Estamos DEBAJO del objetivo. Nos movemos hacia el NE
                System.out.println("La direccion escogida es NE");
                return 2;
            } else {
                // Estamos en la misma fila que el objetivo. Nos movemos hacia el E
                System.out.println("La direccion escogida es E");
                return 4;
            }
        } else if(this.x > this.coord_x_objetivo){
            
            // Estamos a la DER del objetivo.
            if(this.y < this.coord_y_objetivo){
                // Estamos ENCIMA del objetivo. Nos movemos hacia el SW
                System.out.println("La direccion escogida es SW");
                return 5;
            } else if(this.y > this.coord_y_objetivo){
                // Estamos DEBAJO del objetivo. Nos movemos hacia el NW
                System.out.println("La direccion escogida es NW");
                return 0;
            } else {
                // Estamos en la misma fila que el objetivo. Nos movemos hacia el W
                System.out.println("La direccion escogida es W");
                return 3;
            }
        } else {
            
            // Estamos en la MISMA COLUMNA que el objetivo.
            if(this.y < this.coord_y_objetivo){
                // Estamos POOOOOOR ENCIMA del objetivo. Nos movemos hacia el S
                System.out.println("La direccion escogida es S");
                return 6;
            } else if(this.y > this.coord_y_objetivo){
                // Estamos DEBAJO del objetivo. Nos movemos hacia el N
                System.out.println("La direccion escogida es N");
                return 1;
            } else {
                this.enObjetivo = true;
                System.out.println("En objetivoo!!!!!");
            }
        }
        
        return -1;
    }
    
    /**
     * @author ruben, grego, nacho
     * @return 
     */
    public int buscarLimite(){
        
        int inicio = 0, fin = 0, centro = 0;
        int tam = this.sensor.get(0).size()-1;
        
            switch(this.tipoVehiculo){
                case DRON:
                    centro = 1;
                    break;
                case COCHE:
                    centro = 2;
                    break;
                case CAMION:
                    centro = 5;
                    break;
        }
        
        if(this.sensor.get(0).get(centro) == 2){
            return 1;
        } else if(this.sensor.get(centro).get(0) == 2){
            return 3;
        } else if(this.sensor.get(centro).get(tam) == 2){
            return 4;
        } else if(this.sensor.get(tam).get(centro) == 2){
            return 6;
        }
        
        return -1;
    }

    /**
     * @author dani
     */
    private void ejecutarCancel() {
       
        setDestinatario(Agente.nombreLider);
        outbox.setConversationId("peticionCancel");
        outbox.setPerformative(ACLMessage.REQUEST);
        this.finalizado = true;
      //  this.send(outbox);  

        
    }

    /**
     * @author kudry
     * @throws InterruptedException 
     */
    private void preguntarSiHayObjetivo() throws InterruptedException {
       setDestinatario(Agente.nombreLider);
        outbox.setConversationId("askObjetivo");
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);
        ACLMessage inbox = new ACLMessage();

        while (queue.isEmpty()){ Thread.sleep(1);};   
        inbox = queue.Pop();
              
        switch (inbox.getPerformativeInt()) {
            case ACLMessage.INFORM:
               String content = inbox.getContent();
               String[] data = content.split(",");
               this.coord_x_objetivo = Integer.parseInt(data[0]);
               this.coord_y_objetivo = Integer.parseInt(data[1]);
               this.objetivo_encontrado = true;
                System.out.println("TENGO COORDENADAS DE OTRO AGENTE");
            break;
        }

    }
    
}