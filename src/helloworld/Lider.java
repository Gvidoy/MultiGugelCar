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
    private int agentCount;
    private String conversationID;
    private ACLMessage outbox;
   private MessageQueue queue;

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
        this.agentCount = -1; 
        queue = new MessageQueue(100);

    }

    @Override
    public void execute(){

        System.out.println("\nHola soy el Lidl ");
        System.out.println("["+this.getName()+"] Activado");
       int cont = 0;
        while (true)  {
             while (queue.isEmpty())  { // Iddle mientras no ha recibido nada. No bloqueante
                cont++;
                 System.out.println("["+this.getName()+"] Iddle " + cont);
                 try {
                     Thread.sleep(1000); // Espera 1 segundo hasta siguiente chequeo
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 if (cont== 17){
                    cancel();
                    break;
                 }
             }
             if(cont == 17){
                 break;
             }
             // En cuanto la cola tiene al menos un mensaje, se extraen todos
             // los que haya
            try {
                ACLMessage inbox = queue.Pop();
                 System.out.println("\n["+this.getName()+"] Procesando: "+inbox.getPerformative() + " De " + inbox.getSender().name + " ID " + inbox.getConversationId() );
                 if("DatosI".equals(inbox.getConversationId())){
                     recibirDatosIniciales(inbox);
                 }else{
                    if(inbox.getPerformativeInt() == ACLMessage.REQUEST || inbox.getPerformativeInt() == ACLMessage.INFORM){
                        sendConversationID(inbox);
                    }
                 }
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }

         }
    }
        public void sendConversationID(ACLMessage msg) throws InterruptedException{
        //  ACLMessage inbox = new ACLMessage();
          System.out.println("Mensaje recivido " + msg.getPerformative());
          ACLMessage outbox;

          if (msg.getPerformativeInt() == ACLMessage.REQUEST){
                
                if(this.conversationID.equals("")){
                   
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    outbox.setReceiver(msg.getSender());      
                    outbox.setPerformative(ACLMessage.FAILURE);  
                    this.send(outbox);    
                    System.out.println("Lid, Failure: No hay conversation ID.");
                }else{
                        
                    outbox = new ACLMessage();
                    outbox.setSender(this.getAid());
                    outbox.setReceiver(msg.getSender());      
                    outbox.setPerformative(ACLMessage.INFORM);  
                    outbox.setConversationId(this.conversationID);
                    this.send(outbox);
                    System.out.println("Lid, Enviado: Si hay conversation ID.");
                }
                
            }else if (msg.getPerformativeInt() == ACLMessage.INFORM){
                this.conversationID = msg.getContent();
                System.out.println("Clave: " + this.conversationID + " recibida en lider");
            }
            this.agentCount++;
            System.out.println("Total Agentes: " + this.agentCount );
    };
        
            
        /**
        * @author Dani
        */
        public void setDestinatario(String nombre){
            outbox = new ACLMessage();
            outbox.setSender(this.getAid());
            outbox.setReceiver(new AgentID(nombre));   
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
                   while (queue.isEmpty()){ Thread.sleep(1);}
                   ACLMessage inb = queue.Pop();
                  if (inb.getPerformativeInt() == ACLMessage.AGREE){
                      System.out.println("AGREE " + inb.getContent());
                  } 
                  while (queue.isEmpty()){ Thread.sleep(1);}
                    inb = queue.Pop(); 
                   if (inb.getPerformativeInt() == ACLMessage.INFORM){
                      System.out.println("INFORM " + inb.getContent());
                  } 

              } catch (InterruptedException ex) { 
                  Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
              }
        return true;  
    };
    
        public void onMessage(ACLMessage msg)  {
        try {
            queue.Push(msg); // Cada mensaje nuevo que llega se encola en el orden de llegada
            System.out.println("\n["+this.getName()+"] Encolando: "+ msg.getPerformative() + " de " + msg.getSender().name );
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * @author Nacho
     * @param msg 
     */
    private void recibirDatosIniciales(ACLMessage msg) {
        System.out.println("Mensaje recividoloo " + msg.getPerformative() + " ID " + msg.getConversationId()); 

          String mensaje = msg.getContent();
          
          String[] partes = mensaje.split(",");
        
          Memoria.registrarAgente(partes, msg.getSender());
          
          System.out.println("Enviado a la memoria: " + mensaje);
    }
        

    
    
}
