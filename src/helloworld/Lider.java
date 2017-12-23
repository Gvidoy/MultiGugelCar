/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author nacho, ruben
 */
public class Lider extends SingleAgent{
     
    private static Memoria memoria = new Memoria();
    private String conversationID;
    private ACLMessage inbox;
    /**funciones
     * - Enviar mapa tamaño n
     * - request accion (busca objetivo o ve a objetivo)
     * - responder a una peticion de movimiento (PROPOSE) con AGREE/REFUSE consultando el mapa
     * - Actualizar mapa.
     * - consultar la memoria
     * - inform. 
     */

    public Lider() throws Exception {
        super(null);
    }

    public Lider(AgentID aid) throws Exception {
        super(aid);
        this.conversationID = "";
       
    }

    @Override
    public void execute(){

        System.out.println("\nHola soy el Lidl \n");
        
        while(true){
            try {
                inbox = this.receiveACLMessage();
                sendConversationID();
            } catch (InterruptedException ex) {
                Logger.getLogger(Lider.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        
    }
        public void sendConversationID() throws InterruptedException{
        //  ACLMessage inbox = new ACLMessage();
       //   inbox = this.receiveACLMessage();
          System.out.println("Mensaje recivido");
          ACLMessage outbox;
          
            if (inbox.getPerformativeInt() == ACLMessage.REQUEST){
                
                if(this.conversationID.equals("")){
                   
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    outbox.setReceiver(inbox.getSender());      
                    outbox.setPerformative(ACLMessage.FAILURE);  
                    this.send(outbox);    
                    System.out.println("Lid, Failure: No hay conversation ID.");
                }else{
                        
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    outbox.setReceiver(inbox.getSender());      
                    outbox.setPerformative(ACLMessage.INFORM);  
                    outbox.setConversationId(this.conversationID);
                    this.send(outbox);
                    System.out.println("Lid, Enviado: Si hay conversation ID.");
                }
                
              //  this.conversationID = inbox.getConversationId();
              //  System.out.println("Aceptada " + this.conversationID);
            }else if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                this.conversationID = inbox.getContent();
                System.out.println("Clave: " + this.conversationID + " recibida en lider");
            }
    };
    

    
    
}
