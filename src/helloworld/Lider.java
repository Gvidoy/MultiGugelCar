/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
                     Thread.sleep(500); // Espera 1 segundo hasta siguiente chequeo
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
                 switch(inbox.getConversationId()){
                     case "DatosI":
                         recibirDatosIniciales(inbox);
                         break;
                     case "solicitarMovimiento":
                         if(inbox.getPerformativeInt() == ACLMessage.QUERY_IF){
                             comprobarMovimiento(inbox);
                         }
                         break;
                     case "sendKey":
                            sendConversationID(inbox);
                           break;
                         
                 }
                 
              

             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }

         }
    }
        public void sendConversationID(ACLMessage msg) throws InterruptedException{

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
                       generarMapaTraza(inb);
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
       
        System.out.println("Mensaje recividoloo de " + msg.getSender().getLocalName() + " " + msg.getPerformative() + " ID " + msg.getConversationId()); 

          String mensaje = msg.getContent();
          
          String[] partes = mensaje.split(",");
          int xv =  Integer.parseInt(partes[0]);
          int yv =  Integer.parseInt(partes[1]);
  
          TipoVehiculo tipo = TipoVehiculo.COCHE;
          switch(partes[2]){
              case "DRON":
                  tipo = TipoVehiculo.DRON;
              break;
              case "CAMION":
                  tipo = TipoVehiculo.CAMION;
              break;
          }
          memoria.addVehiculo(xv,yv,msg.getSender().getLocalName(),tipo);
  
      
    }

    private boolean comprobarMovimiento(ACLMessage inb) {
        String agent = inb.getSender().name;
        switch(inb.getContent()){
            case "moveS":
             if(memoria.getS(agent) == 0 || memoria.getS(agent) == 12){return true;};
            break;
            case "moveN":
             if(memoria.getN(agent) == 0 || memoria.getN(agent) == 12){return true;};
            break;
            case "moveSW":
             if(memoria.getSW(agent) == 0 || memoria.getSW(agent) == 12){return true;};
            break;
            case "moveSE":
             if(memoria.getSE(agent) == 0 || memoria.getSE(agent) == 12){return true;};
            break;
            case "moveNE":
             if(memoria.getNE(agent) == 0 || memoria.getNE(agent) == 12){return true;};
            break;
            case "moveNW":
             if(memoria.getNW(agent) == 0 || memoria.getNW(agent) == 12){return true;};
            break;
            case "moveW":
             if(memoria.getW(agent) == 0 || memoria.getW(agent) == 12){return true;};
            break;
            case "moveE":
             if(memoria.getE(agent) == 0 || memoria.getE(agent) == 12){return true;};
            break;
        }
        
        if(memoria.getTipo(agent) == TipoVehiculo.DRON){ return true;};
        return false;
    }
        
     public void generarMapaTraza(ACLMessage inbox){
        try {
            System.out.println("Recibiendo traza");
            JsonObject injson = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = injson.get("trace").asArray();

            byte data[] = new byte [ja.size()];
            for(int i = 0; i<data.length; i++){
                data[i] = (byte) ja.get(i).asInt();
            }
            
            FileOutputStream fos = new FileOutputStream("mitraza.png");
            fos.write(data);
            fos.close();
            System.out.println("Traza Guardada en mitraza.png");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
      } 
    
    
}
