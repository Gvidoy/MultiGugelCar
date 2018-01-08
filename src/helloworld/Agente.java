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
 
    public static final String MAPA = "map5";
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
    private ArrayList<ArrayList<Integer>> mapa_recorte;
    
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
        this.mapa_recorte = new ArrayList();
    }
   
    /**
     * @author grego ruben, dani, nacho, kudry
     */
    @Override
    public void execute(){
     
        if(!finalizado){
            try {
                
            int step = 0;

            if(!askForConversationID()){
                Thread.sleep(3000);
                subscribe();
            }

                checkin();

                doQuery_ref();
                Thread.sleep(3000);
                
                enviar_datos_inicales();
                
                

                System.out.println("El agente " + this.getName() + " está BUSCANDO EL OBJETIVO...");
                while(!objetivo_encontrado && !finalizado){

                    this.nuevaLogica();
                    this.doQuery_ref();
                    if(contador%10 == 0 && !objetivo_encontrado){
                        solicitarCoordenadasObjetivo();
                        System.out.println("El agente " + this.getName() + " está SOLICITANDO LAS COORDENADAS AL LIDER...");
                    }
                    contador++;
                    System.out.println("----------------iteraciones: " + contador);
                    if(this.badp_count > 30){
                        ejecutarCancel();
                    }
                   
                }      


                //System.out.println("El objetivo se encuentra en las coordenadas: (" + this.coord_x_objetivo + "," + this.coord_y_objetivo + ").");

                ArrayList<Integer> direcciones = new ArrayList();
                
                boolean objetivo_actualizado = false;
                
                boolean veo_vehiculo = false;
                
                while(!this.enObjetivo && step < 300){
                    System.out.println("!!!!!!!!!!!!! [" + this.getName() + "] ESTOY YENDO HACIA EL OBJETIVO " + this.coord_x_objetivo + "," 
                            + this.coord_y_objetivo + "!!!!!!!!!!!!!.");
                    
                    if(!objetivo_actualizado){
                        // Intentamos actualizar el objetivo
                        objetivo_actualizado = actualizarObjetivo();                        
                    }
                            
                    direcciones = irHaciaObjetivo();
            
                    int indice_direccion = direcciones.get(0);
                    String next_move = this.traducirIndiceDireccion(indice_direccion);
                    
                    //if(veo_vehiculo){
                        // Preguntamos al lider
                        while(!askLider(next_move)){
                            direcciones.remove(0);
                            indice_direccion = direcciones.get(0);
                            next_move = this.traducirIndiceDireccion(indice_direccion);
                        }
                    //}                    
                    
                    this.performMove(next_move);
                    
                    this.doQuery_ref();
                    if(this.badp_count > 30){
                          ejecutarCancel();
                    }
                    
                    step++;
                    System.out.println("[" + this.getName() + "] STEP NUMERO " + step);
                }
                
                System.out.println("!!!!!!! [" + this.getName() + "] HE LLEGADO AL OBJETIVOOO. ENVIO LAS COORDENADAS!!!!!!");
                              
                System.out.println("!!!!!! LAS COORDENADAS DEL OBJETIVO SON: " + this.coord_x_objetivo + "," + this.coord_y_objetivo);
                ejecutarCancel();

            } catch (InterruptedException | JSONException ex) {
                Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);

            }
            //System.out.println("Agente [" +this.getName()+ "] finalizado.");
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
                //System.out.println("El objetivo se encuentra en el PRIMER CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            } else if(this.coord_y_objetivo > centro){
                // EL OBJETIVO SE ENCUENTRA EN EL TERCER CUADRANTE
                //System.out.println("El objetivo se encuentra en el TERCER CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            }
        } else if(this.coord_x_objetivo > centro){
            if(this.coord_y_objetivo < this.y){
                // EL OBJETIVO SE ENCUENTRA EN EL SEGUNDO CUADRANTE
                //System.out.println("El objetivo se encuentra en el SEGUNDO CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            } else if(this.coord_y_objetivo > centro){
                // EL OBJETIVO SE ENCUENTRA EN EL CUARTO CUADRANTE
                //System.out.println("El objetivo se encuentra en el CUARTO CUADRANTE");
                this.coord_x_objetivo = this.x - centro + x_local;
                this.coord_y_objetivo = this.y - centro + y_local;
            }
        } else if(this.coord_x_objetivo == centro){
            // EL OBJETIVO SE ENCUENTRA EN LA MISMA COLUMNA QUE EL VEHICULO
            //System.out.println("El objetivo se encuentra en LA MISMA COLUMNA QUE EL VEHICULO");
            this.coord_x_objetivo = this.x;
            this.coord_y_objetivo = this.y - centro + y_local;
        }
        
        if(this.coord_y_objetivo == centro){
            // EL OBJETIVO SE ENCUENTRA EN LA MISMA FILA QUE EL VEHICULO
            //System.out.println("El objetivo se encuentra en LA MISMA FILA QUE EL VEHICULO");
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
        
        System.out.println("He llegado al objetivo. Enviando las coordenadas al lider...");
        
        ACLMessage out = new ACLMessage();
        out.setSender(this.getAid());
        out.setReceiver(new AgentID(Agente.nombreLider));
        
        // Buscamos la primera posicion en la que se encuentra un 3. Se almacenan la 'x' y la 'y' local del objetivo
        this.objetivoAlAlcance();
        
        // Transformamos la 'x' y la 'y' local en coordenadas absolutas
        this.obtenerCoordenadasObjetivo();
        
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
        //System.out.println("llego a enviar al lider");
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
        //System.out.println("\n["+this.getName()+"] Mensaje: "+ outbox.getPerformative() + " enviado");

          try {
              
                while (queue.isEmpty()){
                   Thread.sleep(1);
                };
                 ACLMessage inb = queue.Pop();
                //System.out.println("\n["+this.getName()+"] Mensaje : "+ inb.getPerformative() + " Recibido");           
              if (inb.getPerformativeInt() == ACLMessage.FAILURE || inb.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  //System.out.println("Failure: " + inb.getContent() + " - " + this.getName());        
              }
              if (inb.getPerformativeInt() == ACLMessage.INFORM){
                  this.conversationID = inb.getConversationId();
                  //System.out.println("Aceptada " + this.conversationID + " - " + this.getName());
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
        
        //System.out.println("Enviando checkin...");
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(this.conversationID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        
        this.send(outbox);     
          try {
              
                while (queue.isEmpty()){Thread.sleep(1);}; 
                ACLMessage inb = queue.Pop();
//                //System.out.println("MENSAJER COMPLETO: " +
//                   "Conversation ID: " + inb.getConversationId()
//                   + " SENDER: " + inb.getSender().name
//                   + " REPLY WITH: " + inb.getReplyWith()
//                   + "Performativa: " + inb.getPerformative()
                         
               // );
                
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
                    //System.out.println("No es de ningun tipo");
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
                  //System.out.println(" - INFORM: " + inbox.getContent());     
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

                //System.out.println("INFORM: " + inbox.getContent() + " con REPLY-ID: " + inbox.getReplyWith()); 
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
                    //System.out.println("Hacemos peticion para actualizar mapa");

                    setDestinatario(Agente.nombreLider);
                    outbox.setPerformative(ACLMessage.INFORM);  
                    outbox.setContent(this.x + "-" + this.y + "-" + this.getName() + "-" + this.sensor + "-" + this.energy);
                    outbox.setConversationId("DatosSensor");
                    this.send(outbox);
                    while (queue.isEmpty()){Thread.sleep(1);};
                    ACLMessage inb = queue.Pop();
                    if (inb.getPerformativeInt() == ACLMessage.INFORM ){
                        //System.out.println("Se ha actualizado el mapa en agente y lider");
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
        System.out.println("[" + this.getName() +  "]\n Radar: ");
        
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
        ArrayList<Integer> direcciones = new ArrayList();
        Integer indice_direccion = null;
	
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
            
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![" + this.getName() + "] OBJETIVO ENCONTRADO. VOY AL OBJETIVO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            
            // Traducimos las coordenadas locales del objetivo en absolutas y nos dirigimos hacia el
            this.obtenerCoordenadasObjetivo();
            
            System.out.println("!!!!! MANDO LAS COORDENADAS AL LIDER Y ME ESTOY DIRIGIENDO HACIA EL PUNTO: " + this.coord_x_objetivo + "," + this.coord_y_objetivo);
            
            // FIN DE LA BUSQUEDA DEL OBJETIVO
            
            // Mandamos las coordenadas del objetivo al lider
            enviarCoordenadasObjetivo();

            direcciones = irHaciaObjetivo();
            
            indice_direccion = direcciones.get(0);
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
                                || this.tipoVehiculo == TipoVehiculo.CAMION))  
                                || lectura == 2)
                            posibles_movimientos[aux] = false;
                        
                        if(lectura == 4){
                            System.out.println("!!!!!!!!!! HE ENCONTRADO UN VEHICULO ANTES DEL OBJETIVO. VOY A CHOCAAAAR"
                                    + " !!!!!!!!!!!");
                            posibles_movimientos[aux] = false;
                        }
                        
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
                
            } else {
                
                this.c = 0;
                this.movimiento_paralelo = false;
                
                if(this.indice_ultima_direccion != null){
                    posibles_movimientos[7-this.indice_ultima_direccion] = false;
                }
            
                boolean encontrado = false;
                direcciones = new ArrayList();
        
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
            }
        }
        
        System.out.println("La direccion escogida es: " + next_move);
        
        
        if(this.contador %10 == 0){
            preguntarSiHayObjetivo();
        }
        
        while(!askLider(next_move)){
            direcciones.remove(0);
            indice_direccion = direcciones.get(0);
            next_move = this.traducirIndiceDireccion(indice_direccion);
        }
        
        this.performMove(next_move);
        this.last_move = "last";
    }

    /**
     * @author ruben, grego, nacho
     *  
     */
    private ArrayList<Integer> irHaciaObjetivo() {
        ArrayList<Integer> direcciones = new ArrayList();
        
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
                direcciones.add(7);
                direcciones.add(6);
                direcciones.add(4);
                direcciones.add(5);
                direcciones.add(2);
                direcciones.add(3);
                direcciones.add(1);
                
                direcciones.add(0);
            } else if(this.y > this.coord_y_objetivo){
                
                // Estamos DEBAJO del objetivo. Nos movemos hacia el NE
                System.out.println("La direccion escogida es NE");
                direcciones.add(2);
                direcciones.add(1);
                direcciones.add(4);
                direcciones.add(7);
                direcciones.add(0);
                direcciones.add(3);
                direcciones.add(6);
                
                direcciones.add(5);
            } else {
                
                // Estamos en la misma fila que el objetivo. Nos movemos hacia el E
                System.out.println("La direccion escogida es E");
                direcciones.add(4);
                direcciones.add(2);
                direcciones.add(7);
                direcciones.add(6);
                direcciones.add(1);
                direcciones.add(5);
                direcciones.add(0);
                
                direcciones.add(3);
            }
        } else if(this.x > this.coord_x_objetivo){
            
            // Estamos a la DER del objetivo.
            if(this.y < this.coord_y_objetivo){
                
                // Estamos ENCIMA del objetivo. Nos movemos hacia el SW
                System.out.println("La direccion escogida es SW");
                direcciones.add(5);
                direcciones.add(6);
                direcciones.add(3);
                direcciones.add(0);
                direcciones.add(7);
                direcciones.add(1);
                direcciones.add(4);
                
                direcciones.add(2);
            } else if(this.y > this.coord_y_objetivo){
                
                // Estamos DEBAJO del objetivo. Nos movemos hacia el NW
                System.out.println("La direccion escogida es NW");
                direcciones.add(0);
                direcciones.add(1);
                direcciones.add(3);
                direcciones.add(2);
                direcciones.add(5);
                direcciones.add(4);
                direcciones.add(6);
                
                direcciones.add(7);
            } else {
                
                // Estamos en la misma fila que el objetivo. Nos movemos hacia el W
                System.out.println("La direccion escogida es W");
                direcciones.add(3);
                direcciones.add(0);
                direcciones.add(5);
                direcciones.add(1);
                direcciones.add(6);
                direcciones.add(2);
                direcciones.add(7);
                
                direcciones.add(4);
            }
        } else {
            
            // Estamos en la MISMA COLUMNA que el objetivo.
            if(this.y < this.coord_y_objetivo){
                
                // Estamos POOOOOOR ENCIMA del objetivo. Nos movemos hacia el S
                System.out.println("La direccion escogida es S");
                direcciones.add(6);
                direcciones.add(5);
                direcciones.add(7);
                direcciones.add(3);
                direcciones.add(4);
                direcciones.add(0);
                direcciones.add(2);
                
                direcciones.add(1);                
            } else if(this.y > this.coord_y_objetivo){
                
                // Estamos DEBAJO del objetivo. Nos movemos hacia el N
                System.out.println("La direccion escogida es N");
                direcciones.add(1);
                direcciones.add(0);
                direcciones.add(2);
                direcciones.add(3);
                direcciones.add(4);
                direcciones.add(5);
                direcciones.add(7);
                
                direcciones.add(6);
            } else {
                this.enObjetivo = true;
                System.out.println("!!!!!!!!!!!!!!! EN OBJETIVOOOOO !!!!!!!!!!!!!!!");
            }
        }
        
        int inicio = 0, fin = 0, centro = 0;
        boolean posibles_movimientos[] = new boolean[8];
        //int aux = 0;
        
        for(int i=0; i < 8; i++){
            posibles_movimientos[i] = true;
        }
        
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
                                || this.tipoVehiculo == TipoVehiculo.CAMION))  || lectura == 2 || lectura == 4)
                            posibles_movimientos[aux] = false;
                        
                        //if(p != -1){
                        //    posibles_movimientos[p] = false;
                        //}
                        aux++;
                    }
                }
            }
        
        Iterator<Integer> it = direcciones.iterator();
                
        while(it.hasNext()){
            Integer i = it.next();
            
            if(posibles_movimientos[i] == false){
                it.remove();
            }
        }
        
        return direcciones;
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

    private boolean actualizarObjetivo() {
        
        boolean encontrado = false;
        
        //Recorremos el sensor en busca de un 3
        for(int i=0; i < this.range && !encontrado; i++){
            for(int j=0; j < this.range && !encontrado; j++){
                int lectura = this.sensor.get(i).get(j);
                
                if(lectura == 3){
                    encontrado = true;
                    //this.coord_x_objetivo = j;
                    //this.coord_y_objetivo = i;
                }
            }
        }

        // Tenemos las locales, las transformamos a las absolutas
        if(encontrado){
            System.out.println("HE ENCONTRADO UN 3 LOCAL. PIDO EL MAPA AL LIDER...");
            this.pedirMapa();
            encontrado = false;
            for(int i=0; i < this.range && !encontrado; i++){
                for(int j=0; j < this.range && !encontrado; j++){
                    int lectura = this.mapa_recorte.get(i).get(j);

                    if(lectura == 3){
                        encontrado = true;
                        this.coord_x_objetivo = j;
                        this.coord_y_objetivo = i;
                    }
                }
            }
            
            if(encontrado){
                this.obtenerCoordenadasObjetivo();
                System.out.println("!!!! HE ACTUALIZADO EL OBJETIVO. LAS NUEVAS COORDENADAS SON: " + this.coord_x_objetivo + "," + this.coord_y_objetivo);
            }
        }
        
        return encontrado;
    }

    private boolean buscarVehiculo() {
        boolean encontrado = false;
        
        // Recorremos el sensor en busca de un 4
        for(int i=0; i < this.range && !encontrado; i++){
            for(int j=0; j < this.range && !encontrado; j++){
                int lectura = this.sensor.get(i).get(j);
                
                if((i != this.centro_mapa || j != this.centro_mapa) && lectura == 4){
                    encontrado = true;
                }
            }
        }
        
        // Tenemos las locales, las transformamos a las absolutas
        if(encontrado){
            //this.obtenerCoordenadasObjetivo();
            System.out.println("!!!!!!!!!!!!!!!!!!!!! HE ENCONTRADO UN VEHICULO !!!!!!!!!!!!!!!!!");
        }
        
        return encontrado;
    }

    private void pedirMapa() {
        
        this.setDestinatario(Agente.nombreLider);
        //this.outbox.setSender(new AgentID(this.getAid().name));
        //this.outbox.setReceiver(new AgentID(Agente.nombreLider));
        this.outbox.setPerformative(ACLMessage.QUERY_REF);
        this.outbox.setConversationId("pedirMapa");
        this.send(this.outbox);
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            
            while(this.queue.isEmpty()){
                Thread.sleep(1);
            }
            
            inbox = this.queue.Pop();
            
            int performativa = inbox.getPerformativeInt();
            
            switch(performativa){
                case ACLMessage.INFORM:
                    System.out.println("Mapa recibido del Lider!");
                    
                    //JsonObject objeto = Json.parse(inbox.getContent()).asObject();
                    //JsonArray vector = objeto.get("mapa").asArray();
                    String[] a = inbox.getContent().split("-");
                    int cont = 0;
                    
                    for(int i = 0; i < this.range; i++){
                        this.mapa_recorte.add(new ArrayList());
                        for(int j = 0; j < this.range; j++){
                            this.mapa_recorte.get(i).add(Integer.parseInt(a[cont]));
                            cont++;
                        }
                    }
                    
                    System.out.println("Imprimimos el mapa recibido del lider...");
                    
                    for(int i = 0; i < this.range; i++){
                        //System.out.printf("Fila " + i + ": ");
                        for(int j = 0; j < this.range; j++){
                            System.out.printf(this.mapa_recorte.get(i).get(j) + " ");
                        }
                        
                        System.out.println();
                    }
                    
                    break;
                case ACLMessage.FAILURE:
                    System.out.println("ERROR. El lider no quiere enviar el mapa.");
                    
                    break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}