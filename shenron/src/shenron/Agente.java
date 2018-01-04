/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shenron;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ruben
 */
class Agente extends SingleAgent {
    
    Agente() throws Exception{
        super(null);
    }
    
    Agente(AgentID nombre) throws Exception{
        super(nombre);
    }
    
    @Override
    public void execute(){
        // Hacemos el REQUEST a shenron
        this.enviarRequest();
    }

    private void enviarRequest() {
        System.out.println("Enviando REQUEST a Shenron...");
        
        ACLMessage outbox = new ACLMessage();
        outbox.setReceiver(new AgentID("Shenron"));
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        
        JsonObject objeto = new JsonObject();
        
        objeto.add("user", "Escorpion");
        objeto.add("password", "Russo");
        
        outbox.setContent(objeto.toString());
        send(outbox);
        
        System.out.println("REQUEST enviado a Shenron. Esperando respuesta...");
        
        try {
            ACLMessage inbox = this.receiveACLMessage();
            
            System.out.println("Respuesta recibida!");
            int performative = inbox.getPerformativeInt();
            
            switch(performative){
                case ACLMessage.FAILURE:
                    System.out.println("ERROR. Failure. El contenido del mensaje es: " + inbox.getContent());
                    break;
                case ACLMessage.INFORM:
                    System.out.println("CORRECTO!. El contenido del mensaje es: " + inbox.getContent());
                    break;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
