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
import java.util.ArrayList;
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
    private final int limiteIDLE = 100; 
    private boolean finalizado;
    private int agentCount;
    private String conversationID;
    private ACLMessage outbox;
    private MessageQueue queue;
    private int coord_x_objetivo;
    private int coord_y_objetivo;
    private int cancelCount;

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
        this.agentCount = 0; 
        queue = new MessageQueue(9000);
        this.cancelCount = 0;
        this.coord_x_objetivo = 0;
        this.coord_y_objetivo = 0;
        this.finalizado = false;
    }

    @Override
    public void execute(){

        System.out.println("\nHola soy el Lidl ");
        System.out.println("["+this.getName()+"] Activado");
        int cont = 0;
        while (!finalizado)  {
            System.out.println("tamaño cola: "+ queue.getSize());
            while (queue.isEmpty())  { // Iddle mientras no ha recibido nada. No bloqueante
                cont++;
            
                System.out.println("["+this.getName()+"] Iddle " + cont);
                try {
                    Thread.sleep(500); // Espera 1 segundo hasta siguiente chequeo
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
               if (cont==limiteIDLE){
                    cancel();
                    break;
                }
                
            }
            if(cont == limiteIDLE){
                break;
            }
            
            // En cuanto la cola tiene al menos un mensaje, se extraen todos
            // los que haya
            try {
                cont = 0;
                ACLMessage inbox = queue.Pop();
                System.out.println("\n["+this.getName()+"] Procesando: "+inbox.getPerformative() + " De " + inbox.getSender().name + " ID " + inbox.getConversationId() );
                switch(inbox.getConversationId()){
                    case "DatosI":
                        recibirDatosIniciales(inbox);
                        break;
                    case "solicitarMovimiento":
                        if(inbox.getPerformativeInt() == ACLMessage.QUERY_IF){
                            System.out.println("Se ha solicitado una peticion de movimiento");
                            memoria.verMapaCoche(inbox.getSender().name, 20, 20);
                            
                            boolean answer = comprobarMovimiento(inbox);
                            outbox = new ACLMessage();
                            outbox.setSender(this.getAid());
                           if(answer) {
                                outbox.setPerformative(ACLMessage.CONFIRM);
                           }else{
                                outbox.setPerformative(ACLMessage.DISCONFIRM);  
                           }
                            System.out.println("La respuesta ha sido: " + answer);
                            outbox.setReceiver(inbox.getSender());      
                            this.send(outbox);
                        }
                         
                        break;
                    case "envioCoordenadasObjetivo":
                        guardarCoordenadas(inbox);
                        break;
                    
                    case "askObjetivo":
                        devolverCoordenadas(inbox);
                        break;
                    case "sendKey":
                         sendConversationID(inbox);
                        break;
                        
                    case "DatosSensor":
                       actualizarMapaLider(inbox);
                       
                       outbox = new ACLMessage();
                       outbox.setSender(this.getAid());
                       outbox.setPerformative(ACLMessage.INFORM);
                       outbox.setReceiver(inbox.getSender());      
                       this.send(outbox);
                       break;
                       
                    case "peticionCancel":
                       this.cancelCount++;
                        if(this.cancelCount ==  this.agentCount){
                               cancel();
                       this.finalizado = true;
                       }
                      
                     
                    break;
                    default:
                        break;
                        
                }
                 
           //     if(inbox.getPerformativeInt() == ACLMessage.REQUEST || inbox.getPerformativeInt() == ACLMessage.INFORM)
           //         sendConversationID(inbox);

            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }
    
    /**
     * @author Ruben
     * Funcion que guarda las coordenadas recibidas del agente que ha encontrado el objetivo
     * @param msg
     * @throws InterruptedException 
     */
    
    private void guardarCoordenadas(ACLMessage in){
        
        System.out.println("Recibidas las coordenadas del objetivo. Estas son: " + in.getContent() + ". Guardando dichas coordenadas...");
        
        JsonObject objeto = Json.parse(in.getContent()).asObject();
        
        this.coord_x_objetivo = objeto.get("x").asInt();
        this.coord_y_objetivo = objeto.get("y").asInt();
        
        System.out.println("Coordenada x del objetivo " + Integer.toString(objeto.get("x").asInt()));
        System.out.println("Coordenada y del objetivo " + Integer.toString(objeto.get("y").asInt()));
    }
   
        public void sendConversationID(ACLMessage msg) throws InterruptedException{

        //  ACLMessage inbox = new ACLMessage();
          System.out.println("Mensaje recibido " + msg.getPerformative());

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
            this.agentCount++;
            }else if (msg.getPerformativeInt() == ACLMessage.INFORM){
                this.conversationID = msg.getContent();
                System.out.println("Clave: " + this.conversationID + " recibida en lider");
            }
          
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
            outbox = new ACLMessage();
            outbox.setSender(this.getAid());
            outbox.setPerformative(ACLMessage.INFORM);
            outbox.setReceiver(msg.getSender());      
            this.send(outbox);

  
      
    }

    private boolean comprobarMovimiento(ACLMessage inb) {
        String agent = inb.getSender().name;
        System.out.println("Movimiento recibido:" + inb.getContent());
        switch(inb.getContent()){
            case "moveS":
             if(memoria.getS(agent) <= 0 || memoria.getS(agent) == 3){return true;};
            break;
            case "moveN":
             if(memoria.getN(agent) <= 0 || memoria.getN(agent) == 3){return true;};
            break;
            case "moveSW":
             if(memoria.getSW(agent) <= 0 || memoria.getSW(agent) == 3){return true;};
            break;
            case "moveSE":
             if(memoria.getSE(agent) <= 0 || memoria.getSE(agent) == 3){return true;};
            break;
            case "moveNE":
             if(memoria.getNE(agent) <= 0 || memoria.getNE(agent) == 3){return true;};
            break;
            case "moveNW":
             if(memoria.getNW(agent) <= 0 || memoria.getNW(agent) == 3){return true;};
            break;
            case "moveW":
             if(memoria.getW(agent) <= 0 || memoria.getW(agent) == 3){return true;};
            break;
            case "moveE":
             if(memoria.getE(agent) <= 0 || memoria.getE(agent) == 3){return true;};
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

    private void actualizarMapaLider(ACLMessage inbox) {
            
        System.out.println(inbox.getContent());
        
        String mensaje = inbox.getContent();
          
          String[] partes = mensaje.split("-");
          int px =  Integer.parseInt(partes[0]);
          int py = Integer.parseInt(partes[1]);
          String nombreV =  partes[2];
          String vector = partes[3];
          int energia = Integer.parseInt(partes[4]);
          if(energia == 0){
              cancel();
          }
          vector = vector.replace(",","");
          vector = vector.replace("[","");
          vector = vector.replace("]","");
          vector = vector.replace(" ","");
          char[] vec = vector.toCharArray();
       //     System.out.println("Primera fila: " + vector);
            ArrayList<ArrayList<Integer>>  sensor = new ArrayList<ArrayList<Integer>>();
            TipoVehiculo tiv = memoria.getTipo(nombreV);
            int max = 0;
            switch(tiv){
                case CAMION: max = 11;break;
                case COCHE: max = 5;break;
                case DRON: max = 3;break;
            }
            int contador = 0;

            for(int i = 0; i < max; i++){
                sensor.add(new ArrayList<Integer>());
                    for (int j = 0; j < max; j++){
                        int x = vec[contador]-'0';
                        sensor.get(i).add(x);
                        contador++;
                    }
            }
     /*     
            for (int i = 0; i < sensor.size(); i++) {
                 System.out.println(sensor.get(i));

            }
  **/         
            memoria.actuMapa(nombreV, px, py, sensor);
        
    System.out.println("Tengo memoria");
          
  
    }

    private void devolverCoordenadas(ACLMessage inbox) {
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(inbox.getSender());      
      
        if(this.coord_x_objetivo != 0 && this.coord_y_objetivo != 0 ){
             outbox.setPerformative(ACLMessage.INFORM);
             outbox.setContent(this.coord_x_objetivo + "," + this.coord_y_objetivo);
        }else{
             outbox.setPerformative(ACLMessage.DISCONFIRM);
        }
         this.send(outbox);  
    }
    
    
}
