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

/**
 *
 * @author nacho, ruben
 */
public class Lider extends SingleAgent{
    
    private String mapa = "map5";

    
     
    private Memoria memoria = new Memoria();
    private final int limiteIDLE = 150; 
    private boolean finalizado;
    private int agentCount;
    private String conversationID;
    private ACLMessage outbox;
    private MessageQueue queue;
    private int coord_x_objetivo;
    private int coord_y_objetivo;
    private int cancelCount;
//    private boolean cerrojo;
//    private String obrera_buscadora;

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
//        this.cerrojo = false;
//        this.obrera_buscadora = null;
    }

    /**
     * @author ruben, nacho, grego, dani, kudry
     */
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
                    Thread.sleep(100); // Espera 1 segundo hasta siguiente chequeo
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
                            //System.out.println("Se ha solicitado una peticion de movimiento");
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
                    case "pedirMapa":
                        
                        System.out.println("!!!!!!!! HE RECIBIDO LA PETICION DEL MAPA DEL VEHICULO!!!!!");
                        // El vehiculo nos ha pedido un pedazo de mapa
                        
                        if(inbox.getPerformativeInt() == ACLMessage.QUERY_REF){
                         String a = this.enviarMapa(inbox);
                            
                       outbox = new ACLMessage();
                       outbox.setSender(this.getAid());
                       outbox.setPerformative(ACLMessage.INFORM);
                       outbox.setReceiver(inbox.getSender()); 
                       outbox.setContent(a);
                       this.send(outbox);
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
   
    /**
     * @author dani, kudry
     * @param msg
     * @throws InterruptedException 
     */
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

    /**
     * @author dani
     * @param msg 
     */
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
       
        System.out.println("Mensaje recivido de " + msg.getSender().getLocalName() + " " + msg.getPerformative() + " ID " + msg.getConversationId()); 

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
        
//        if(tipo == TipoVehiculo.DRON && !this.cerrojo){
//            this.cerrojo = true;
//            this.obrera_buscadora = msg.getSender().getLocalName();
//        }
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.INFORM);
        outbox.setReceiver(msg.getSender());      
        this.send(outbox);
    }

    /**
     * @author  nacho, ruben, grego
     * @param inb
     * @return 
     */
    private boolean comprobarMovimiento(ACLMessage inb) {
        
//        // El primer filtro es si es nuestro zangano o no
//        if(inb.getSender().getLocalName().equals(this.obrera_buscadora) && this.cerrojo){
//            return true;
//        }
//        
//        if(!this.cerrojo){
//            
//        }
        
        String movimiento = inb.getContent();
        String agent = inb.getSender().name;
        boolean puede = false;
        
        System.out.println("Movimiento recibido:" + movimiento);
        
        if(memoria.getTipo(agent) == TipoVehiculo.DRON){
            // Logica para el DRON. El DRON se movera siempre que no haya otro vehiculo en la casilla
            // y no sea el borde del mundo
            switch(movimiento){
                case "moveS":
                    if(memoria.getS(agent) < 81 && memoria.getS(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveN":
                    if(memoria.getN(agent) < 81 && memoria.getN(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveSW":
                    if(memoria.getSW(agent) < 81 && memoria.getSW(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveSE":
                    if(memoria.getSE(agent) < 81 && memoria.getSE(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveNE":
                    if(memoria.getNE(agent) < 81 && memoria.getNE(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveNW":
                    if(memoria.getNW(agent) < 81 && memoria.getNW(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveW":
                    if(memoria.getW(agent) < 81 && memoria.getW(agent) != 2)
                        puede = true;
                    
                    break;
                case "moveE":
                    if(memoria.getE(agent) < 81 && memoria.getE(agent) != 2)
                        puede = true;
                    
                    break;
            }
        } else {
            // Logica para el resto de vehiculos. El resto de vehiculos se podran mover siempre
            // que sea un pedazo de mapa no descubierto o si es objetivo
            switch(inb.getContent()){
                case "moveS":
                    if(memoria.getS(agent) <= 0 || memoria.getS(agent) == 3)
                        return true;
                    
                    break;
                case "moveN":
                    if(memoria.getN(agent) <= 0 || memoria.getN(agent) == 3)
                        return true;
                    
                    break;
                case "moveSW":
                    if(memoria.getSW(agent) <= 0 || memoria.getSW(agent) == 3)
                        return true;
                    
                    break;
                case "moveSE":
                    if(memoria.getSE(agent) <= 0 || memoria.getSE(agent) == 3)
                        return true;
                
                    break;
                case "moveNE":
                    if(memoria.getNE(agent) <= 0 || memoria.getNE(agent) == 3)
                        return true;
                
                    break;
                case "moveNW":
                    if(memoria.getNW(agent) <= 0 || memoria.getNW(agent) == 3)
                        return true;
                
                    break;
                case "moveW":
                    if(memoria.getW(agent) <= 0 || memoria.getW(agent) == 3)
                        return true;
                    
                    break;
                case "moveE":
                    if(memoria.getE(agent) <= 0 || memoria.getE(agent) == 3)
                        return true;
                
                    break;
            } // FIN DEL SWITCH
        } // FIN DEL ELSE
        
        return puede;
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

           FileOutputStream fos = new FileOutputStream(this.mapa + "-"+ this.conversationID + ".png");
           fos.write(data);
           fos.close();
           System.out.println("Traza Guardada en mitraza.png");

       } catch (FileNotFoundException ex) {
           Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
           Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
       }
     } 

    /**
     * @author ruben, nacho, grego, dani
     * @param inbox 
     */
    private void actualizarMapaLider(ACLMessage inbox) {
            
        //System.out.println(inbox.getContent());
        
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
            //System.out.println("Enviando a Memoria los parametros: " + nombreV + "," + px + "," + py + "," + sensor);
            memoria.actuMapa(nombreV, px, py, sensor);
        
    System.out.println("Tengo memoria");
          
  
    }

    /**
     * @author
     * @param inbox 
     */
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

    private String enviarMapa(ACLMessage inbox) {
        
        System.out.println("!!!!!!!! HE ENTRADO EN ENVIARMAPA!!!!!");
        
        String nombre_agente = inbox.getSender().name;
        int tam = 0;
        
        switch(this.memoria.getTipo(nombre_agente)){
            case DRON:
                tam = 9;
                
                break;
            case COCHE:
                tam = 25;
                
                break;
            case CAMION:
                tam = 121;
                
                break;
        }
        
        ArrayList<Integer> mapa_lider = this.memoria.obtenerMapaParcial(nombre_agente);
        
        System.out.println("El mapa recibido tiene: " + mapa_lider.size());
        
        
//        this.outbox = new ACLMessage();
//        this.outbox.setSender(new AgentID(this.getAid().name));
//        this.outbox.setReceiver(new AgentID(nombre_agente));
        
        JsonObject objeto = new JsonObject();
        //JsonArray vector = new JsonArray();
        String vector = "";
        for(int i = 0; i < tam; i++){
            //vector.add(mapa_lider.get(i));
            vector += mapa_lider.get(i) + "-";
        }
        
        
        
        //objeto.add("mapa", vector);
        
//        this.outbox.setConversationId("pedirMapa");
//        this.outbox.setContent(vector);
//        this.outbox.setPerformative(ACLMessage.INFORM);
//       
//        this.send(this.outbox);
        System.out.println("ENVIANDO MENSAJE A " + nombre_agente);
        return vector; 
    }
    
    
}
