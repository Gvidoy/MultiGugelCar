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
 * @author Dani
 */
public class Agente extends SingleAgent {
 
    private ACLMessage outbox;
    private ACLMessage inbox;
    private String conversationID;
   

    public Agente() throws Exception {
        super(null);
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        
        this.outbox = null;
        this.inbox  = null;
        this.conversationID = "";
        
        System.out.println("\n\n\nHola Mundo soy un agente llamado " + this.getName());

    }
    
    //public void init();
    @Override
    public void execute(){

        try {
            subscribe();
            
            cancel();
      
        } catch (InterruptedException ex) {
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
    * @author Dani
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
    

 
     



}