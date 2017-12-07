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
import javafx.util.Pair;


/**
 *
 * @author Dani, nacho, ruben
 */
public class Agente extends SingleAgent {
 
    private ACLMessage outbox;
    private ACLMessage inbox;
    private String conversationID;
    private String reply_withID;
    
    private static int battery;
    private static int x;
    private static int y;
    private static int[][] sensor;
    private static ArrayList<ArrayList<Integer>> mapa;  
    private static Boolean enObjetivo; 
    private static TipoVehiculo tipoVehiculo;
    
    private static int fuelRate;
    private static int range;
    private static Boolean fly;
    

    public Agente() throws Exception {
        super(null);
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        
        this.outbox = null;
        this.inbox  = null;
        this.conversationID = "";
        this.reply_withID = "";
        System.out.println("\n\n\nHola Mundo soy un agente llamado " + this.getName());

        battery = 100;
        mapa = new ArrayList();
        
    }
   
    //public void init();
    @Override
    public void execute(){

        try {
            subscribe();
            checkin();
            refuel();
            doQuery_ref();
            //performMove("moveS");

            cancel();
      
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
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
    
    /**
    * @author Dani
    */
    public void setDestinatario(String nombre){
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(nombre));   
    };
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

          try {
              inbox = this.receiveACLMessage();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent());        
              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  this.conversationID = inbox.getConversationId();
                  System.out.println("Aceptada " + this.conversationID);
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return !"".equals("");
        
    };
    
    /**
    * @author Dani,Oleksandr
    */
    public boolean cancel(){
                
        System.out.println("\n Enviando peticion de cancelacion");
        setDestinatario("Bellatrix");
        outbox.setPerformative(ACLMessage.CANCEL);
        this.send(outbox);    
        
           try {
              inbox = this.receiveACLMessage();

              if (inbox.getPerformativeInt() == ACLMessage.AGREE){
                  System.out.println("AGREE " + inbox.getContent());
              } 
              inbox = this.receiveACLMessage();
               if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM " + inbox.getContent());
              } 

          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        

        return true;  
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
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(this.conversationID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     

          try {
              inbox = this.receiveACLMessage();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("  Failure: " + inbox.getContent());    
                  this.reply_withID = inbox.getReplyWith();

              }        

              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  this.reply_withID = inbox.getReplyWith();
                  System.out.println(" - INFORM: " + inbox.getContent());   
                  System.out.println(" - reply-id: " + inbox.getReplyWith());        
                   
                  JSONObject json = new JSONObject(inbox.getContent());
                  if(json.has("capabilities")){
                     JSONObject json2 = new JSONObject(json.get("capabilities").toString());
                    int fuel= json2.getInt("fuelrate");
                    inicializarTipoVehiculo(fuel);
                  }
                  
                  
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }

        return !"".equals("");
        
    };

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
        outbox.setConversationId(conversationID);
        outbox.setInReplyTo(reply_withID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     

          try {
              inbox = this.receiveACLMessage();
              
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
              inbox = this.receiveACLMessage();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent()); 
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM: " + inbox.getContent());   
                  this.reply_withID = inbox.getReplyWith();

              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return !"".equals("");
        
    };

    /**
    * @author Dani, nacho
    */

    public boolean doQuery_ref() throws InterruptedException, JSONException{

        setDestinatario("Bellatrix");
        outbox.setConversationId(conversationID);
        outbox.setInReplyTo(reply_withID);
        outbox.setContent("");
        outbox.setPerformative(ACLMessage.QUERY_REF);  
        this.send(outbox);     

          try {
              inbox = this.receiveACLMessage();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent());
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM: " + inbox.getContent()); 
                  this.reply_withID = inbox.getReplyWith();
                  
                  JSONObject json = new JSONObject(inbox.getContent());
                  JSONObject json2 = new JSONObject(json.get("result").toString());
                  battery = Integer.parseInt(json2.get("battery").toString());
                  x = Integer.parseInt(json2.get("x").toString());
                  y = Integer.parseInt(json2.get("y").toString());
                  //leerSensor((ArrayList<Integer>) json2.get("sensor"));
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return !"".equals("");
        
    };
    
    /**
    * @author nacho
    */
    private void leerSensor(ArrayList<Integer> respuesta){
        
        int aux = 0;
        
 
        for(int i=0; i < range; i++){
            for(int j=0; j < range; j++){
                sensor[i][j]= respuesta.get(aux);
                aux++;
                
            }
        }
        
        // Mostramos los datos proporcionados por los sensores
        System.out.println("Los datos recibidos son: ");
        System.out.println("\n Radar: ");
        
        String print = "";
        System.out.println("Lectura del radar");
        for(int i=0; i < range; i++){
            for(int j=0; j < range; j++){
                print+= this.sensor[i][j] + " ";
            }
             System.out.println(print);
        }
               
    }
    
    /**
    * @author nacho
    */
    private void inicializarTipoVehiculo(int fuel){
        switch(fuel){
            case 1:
                tipoVehiculo = TipoVehiculo.DRON;
                fuelRate = 1;
                fly = true;
                range = 3;
                break;
            
            case 2: 
                tipoVehiculo = TipoVehiculo.COCHE;
                fuelRate = 2;
                fly = false;
                range = 5;
                break;
                
            case 4: 
                tipoVehiculo = TipoVehiculo.CAMION;
                fuelRate = 4;
                fly = false;
                range = 11;
                break;
                
        }
                
    }
    
    
    
    
    
    
}