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
 
    private static String nombreLider = "Lidllll";
    private ACLMessage outbox;
    private String conversationID;
    private String reply_withID;
    
    //mensajes
    private MessageQueue queue;

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
    
    private String last_move;
    private int coord_x_objetivo;
    private int coord_y_objetivo;
    
    private boolean objetivo_encontrado;
    

    public Agente() throws Exception {
        super(null);
      
    }

    
    public Agente(AgentID aid) throws Exception {
        super(aid);
        
        this.outbox = null;

        queue = new MessageQueue(20);
        
        
        this.conversationID = "";
        this.reply_withID = "";
        System.out.println("\nHola Mundo soy un agente llamado " + this.getName());

        battery = 100;
        mapa = new ArrayList();
        
        this.last_move = "";
        this.coord_x_objetivo = 0;
        this.coord_y_objetivo = 0;
        
        this.objetivo_encontrado = false;
    }
   
    //public void init();
    @Override
    public void execute(){
   
        try {
       
           System.out.println("voy a ver si hay clave " + " - " + this.getName());
        if(!askForConversationID()){
            Thread.sleep(4000);
            System.out.println("No hay, voy a suscribirme " + " - " + this.getName());
            subscribe();
         }
           System.out.println("voy a hacer el checking " + " - " + this.getName());

            checkin();
            
           
      //  refuel();
        
          ACLMessage datos = doQuery_ref();
          enviar_datos_inicales(datos);
           // performMove("moveS");

        //    cancel();
       } catch (InterruptedException | JSONException ex) {
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
    
    private void obtenerCoordenadasObjetivo() {
        // Realizado para el caso del COCHE. En OTRO VEHICULO sera de OTRA FORMA
        
        switch(this.coord_x_objetivo){
            case 0:
                this.coord_x_objetivo = Agente.x-2;
                break;
            case 1:
                this.coord_x_objetivo = Agente.x-1;
                break;
            case 2:
                this.coord_x_objetivo = Agente.x;
                break;
            case 3:
                this.coord_x_objetivo = Agente.x+1;
                break;
            case 4:
                this.coord_x_objetivo = Agente.x+2;
                break;
        }
        
        switch(this.coord_y_objetivo){
            case 0:
                this.coord_y_objetivo = Agente.y-2;
                break;
            case 1:
                this.coord_y_objetivo = Agente.y-1;
                break;
            case 2:
                this.coord_y_objetivo = Agente.y;
                break;
            case 3:
                this.coord_y_objetivo = Agente.y+1;
                break;
            case 4:
                this.coord_y_objetivo = Agente.y+2;
                break;
        }
        
        this.objetivo_encontrado = true;
    }
    
    /**
    * @author Ruben 
    * Funcion para buscar el objetivo. Puede ser un coche, un camion o un dron. Cada uno lo buscará de una manera diferente.
    *
    */

    private void buscarObjetivo(){

	boolean encontrado = false;

	String next_move = "";
	
        // ESTRATEGIA de REFUEL. basica, habra que MODIFICARLA
	if(Agente.battery <= 5)
            try {
                this.refuel();
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

	// En primer lugar, comprobamos si el objetivo se encuentra al alcance
	// Dependiendo del tipo de agente, su sensor sera de un tamanio u otro. En este caso, vamos a hacerlo todo para el coche
	for(int i=0; i < 5 && !encontrado; i++){
            for(int j=0; j < 5 && !encontrado; j++){
                if(sensor[i][j] == 3){
                    encontrado = true;
                    coord_x_objetivo = j;
                    coord_y_objetivo = i;
                }
            }
        }

        if(encontrado){
            
            // TRANSFORMAR LAS COORDENADAS DEL OBJETIVO EN VALORES ABSOLUTOS
            obtenerCoordenadasObjetivo();
            
            // Notificamos al lider que hemos encontrado el objetivo y nos dirigimos hacia el

            // NOTIFICAR AL LIDER DE LAS COORDENADAS ABSOLUTAS DEL OBJETIVO

            // Dependiendo de donde se encuentre el objetivo nos moveremos en una direccion u otra
            switch(coord_y_objetivo){
    		// En el caso de que el objetivo se encuentre encima nuestra, nos moveremos hacia el NW, el N o el NE
    		case 0: case 1:
                    switch(coord_x_objetivo){
                        case 0: case 1:
                            next_move = "moveNW";
                            break;
    			case 2:
                            next_move = "moveN";
                            break;
    			case 3: case 4:
                            next_move = "moveNE";
                            break;
                    }
                    break;
    		// En el caso de que se encuentre a nuestra izq o derecha, nos moveremos hacia el este o el oeste
    		case 2:
                    switch(coord_x_objetivo){
    			case 0: case 1:
                            next_move = "moveW";
                            break;
    			case 3: case 4:
                            next_move = "moveE";
                            break;
                    }
                    break;
    		// En el caso de que se encuentre a debajo nuestra, nos moveremos hacia el SW, el S o el SE
    		case 3: case 4:
                    switch(coord_x_objetivo){
    			case 0: case 1:
                            next_move = "moveSW";
                            break;
    			case 2:
                            next_move = "moveS";
                            break;
    			case 3: case 4:
                            next_move = "moveSE";
                            break; 
                    }
                    break;
            } // FIN DEL SWITCH DEL OBJETIVO
        } // FIN DEL IF DE OBJETIVO ENCONTRADO
        else {
            if(last_move.equals("")){
    		buscarNuevaDireccion(next_move);	

	    	last_move = next_move;
            }
            else {
    		// Si YA NOS HEMOS MOVIDO EN UNA DIRECCION, INTENTAMOS IR DE NUEVO A LA MISMA
    		switch(last_move){
                    case "moveNW":
                        if(sensor[1][1] == 1)
                            next_move = last_move;
                        break;
                    case "moveN":
                        if(sensor[1][2] == 1)
                            next_move = last_move;
    			break;
                    case "moveNE":
                        if(sensor[1][3] == 1)
                            next_move = last_move;
    			break;
                    case "moveW":
                        if(sensor[2][1] == 1)
                            next_move = last_move;
    			break;
                    case "moveE":
                        if(sensor[2][3] == 1)
                            next_move = last_move;
    			break;
                    case "moveSW":
                    	if(sensor[3][1] == 1)
                            next_move = last_move;
    			break;
                    case "moveS":
                        if(sensor[3][2] == 1)
                            next_move = last_move;
    			break;
                    case "moveSE":
                        if(sensor[3][3] == 1)
                            next_move = last_move;
    			break;
    		}

    		// Si NO PODEMOS IR DE NUEVO EN LA MISMA DIRECCION, BUSCAMOS UNA NUEVA
    		buscarNuevaDireccion(next_move);

    		last_move = next_move;
            }    	
        } // CIERRE DEL ELSE (!encontrado objetivo)
        
        try {
            this.performMove(next_move);
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
    * @author Ruben 
    * Funcion que busca una nueva direccion donde dirigirnos en caso de que no veamos el objetivo y no podamos seguir avanzando en la direccion anterior
    *
    */
    private void buscarNuevaDireccion(String next_move){
	// AHORA DEPENDIENDO DE SI EL VEHICULO ES UN DRON O NO, TENDREMOS EN CUENTA LOS OBSTACULOS O NO
	// Como hemos supuesto que es un coche, tendremos en cuenta los obstaculos

	boolean[] posibles_movimientos = new boolean[8];
	int aux = 0;

	for(int i=1; i < 4; i++){
            for(int j=1; j < 4; j++){
	    	// Si encontramos un obstaculo y no estamos evaluando la posicion del agente
	        if(sensor[i][j] == 1 && (i != 2 || j != 2))
         	    posibles_movimientos[aux] = false;
	        else
	            // Si NO encontramos un OBSTACULO, podemos ir en dicha direccion
	            posibles_movimientos[aux] = true;
	            	
	        aux++;
            }
	}

	// Comprobamos las direcciones donde podemos ir y contamos el numero de casillas libres en esa zona.
	// La direccion cuya zona tenga mas casillas libres es la que tomara el agente
	int maximo = 0;
	int indice_direccion = 0;

	for(int i=0; i < 8; i++){
	    if(posibles_movimientos[i] == true){
	    	int num_casillas_libres = 0;
	    	switch (i) {
                    case 0:
                    	for(int j=0; j < 2; j++){
                            for(int k=0; k < 3; k++){
	    			if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

	    		break;
                    case 1:
                        for(int j=0; j < 2; j++){
                            for(int k=1; k < 4; k++){
	    			if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

			break;
                    case 2:
                        for(int j=0; j < 2; j++){
                            for(int k=2; k < 5; k++){
	    			if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

	    		break;
                    case 3:
	    		for(int j=1; j < 4; j++){
                            for(int k=0; k < 2; k++){
                                if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

			break;
                    case 4:
                        for(int j=1; j < 4; j++){
                            for(int k=3; k < 5; k++){
                                if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
                        }

	    		break;
                    case 5:
	    		for(int j=3; j < 5; j++){
                            for(int k=0; k < 3; k++){
                                if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

	    		break;
                    case 6:
                        for(int j=3; j < 5; j++){
                            for(int k=1; k < 4; k++){
	    			if(sensor[j][k] == 1)
                                    num_casillas_libres++;

	    			if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

	    		break;
                    case 7:
                        for(int j=3; j < 5; j++){
                            for(int k=2; k < 5; k++){
                                if(sensor[j][k] == 1)
                                    num_casillas_libres++;

                                if(num_casillas_libres > maximo){
                                    maximo = num_casillas_libres;
                                    indice_direccion = i;
	    			}
                            }
	    		}

	    		break;
	    	} // CIERRE DEL SWITCH (i)
	    } // CIERRE DEL IF (true)
	} // CIERRE DEL FOR

	switch(indice_direccion){
	    case 0:
	    	next_move = "moveNW";
	    	break;
	    case 1:
	    	next_move = "moveN";
	    	break;
	    case 2:
	    	next_move = "moveNE";
	    	break;
		case 3:
			next_move = "moveW";
	    	break;
	    case 4:
	    	next_move = "moveE";
	    	break;
	    case 5:
	    	next_move = "moveSW";
	    	break;
	    case 6:
	    	next_move = "moveS";
	    	break;
	    case 7:
	    	next_move = "moveSE";
	    	break;
	}
    }
    
    /**
    * @author Ruben 
    * Funcion para parsear el sensor recibido mediante doQueryRef. NO FUNCIONA
    *
    */
    private void parsearSensor() {
        ACLMessage inbox = new ACLMessage();
        
        //HE cambiado esto.
        String fuente = inbox.getContent();
        
        // Parseamos el String original y lo almacenamos en un objeto
        JsonObject objeto = Json.parse(fuente).asObject();
        
        // Extraemos los valores asociados a la clave "result"."sensor"
        //System.out.println("Datos recibidos del sensor:");
        
        /*for(JsonValue j : objeto.get("result").asObject().get("sensor").asArray()){
            System.out.println(j.asInt() + ", ");
        }*/
        
        JsonArray vector = objeto.get("result").asObject().get("sensor").asArray();
        
        int aux = 0;
        
        for(int i=0; i < 5; i++){
            for(int j=0; j < 5; j++){
                Agente.sensor[i][j] = vector.get(aux).asInt();
                
                aux++;
            }
        }
        
        System.out.println("Datos guardados en nuestro sensor propio:");
        
        for(int i=0; i < 5; i++){
            System.out.println(Agente.sensor[i][0] + ", " + Agente.sensor[i][1] + ", " + Agente.sensor[i][2] + ", " + Agente.sensor[i][3] + ", " + Agente.sensor[i][4] + ", ");
        }
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
      
        setDestinatario("Bellatrix");
        outbox.setConversationId(this.conversationID);
        outbox.setContent(jsonLogin.toString());
        outbox.setPerformative(ACLMessage.REQUEST);  
        this.send(outbox);     
        System.out.println("aqui llego");
          try {
              
                while (queue.isEmpty()){Thread.sleep(1);}; 
                ACLMessage inb = queue.Pop();
                System.out.println("MENSAJER COMPLETO: " +
                   "Conversation ID: " + inb.getConversationId()
                   + " SENDER: " + inb.getSender().name
                   + " REPLY WITH: " + inb.getReplyWith()
                   + "Performativa: " + inb.getPerformative()
                         
                );
                
              if (inb.getPerformativeInt() == ACLMessage.FAILURE || inb.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inb.getContent());    
                  this.reply_withID = inb.getReplyWith();

              }else if (inb.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println(" - INFORM: " + inb.getContent());
                  System.out.println(" - reply-id: " + inb.getReplyWith());
                  this.reply_withID = inb.getReplyWith();

                  JSONObject json = new JSONObject(inb.getContent());
                  if(json.has("capabilities")){
                     JSONObject json2 = new JSONObject(json.get("capabilities").toString());
                    int fuel= json2.getInt("fuelrate");
                    inicializarTipoVehiculo(fuel);
                  }
                  
                  
              }else{
                  System.out.println("No es de ningun tipo");
              }
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }

         System.out.println("Soy un:" + this.tipoVehiculo + " Y soy el agente: " + this.getName());
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
              while (queue.isEmpty()){Thread.sleep(1);};
        
                ACLMessage inbox = queue.Pop();
              
              if (inbox.getPerformativeInt() == ACLMessage.FAILURE || inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD  ){
                  System.out.println("Failure: " + inbox.getContent()); 
                  this.reply_withID = inbox.getReplyWith();

              }
              if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                  System.out.println("INFORM: " + inbox.getContent());   
                  this.reply_withID = inbox.getReplyWith();
                  
                  // ACTUALIZAMOS LA BATERIA. En este caso es un COCHE, por lo que TRAS CADA MOVIMIENTO, la bateria se reduce en 4 UNIDADES
                  Agente.battery -= 4;
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
        
        return inbox;
        
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
    
      public boolean askForConversationID() throws InterruptedException{
 
   

        setDestinatario(this.nombreLider);
        outbox.setPerformative(ACLMessage.REQUEST);  
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
        setDestinatario(this.nombreLider);
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(this.conversationID);
        this.send(outbox);     

       
    }
    

    /**
    * @author Dani
    */
    private void enviar_datos_inicales(ACLMessage datos) {
        
        System.out.println("Envio datos Iniciales");
        setDestinatario(this.nombreLider);
        outbox.setPerformative(ACLMessage.INFORM);  
        outbox.setContent(datos.getContent());
        outbox.setConversationId("DatosI");
        this.send(outbox);     
        System.out.println("He enviado los datos iniciales");

       
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
    
}