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
    private static ArrayList<ArrayList<Integer>> sensor;
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
        this.inbox  = null;
        this.conversationID = "";
        this.reply_withID = "";
        System.out.println("\n\n\nHola Mundo soy un agente llamado " + this.getName());

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
            subscribe();
            checkin();
            
            /*while(!Agente.tipoVehiculo.equals(TipoVehiculo.COCHE)){
                checkin();
            }*/
            
            refuel();
            doQuery_ref();
            //performMove("moveS");

            cancel();
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
/*
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
    */

    /**
    * @author Ruben 
    * Funcion que busca una nueva direccion donde dirigirnos en caso de que no veamos el objetivo y no podamos seguir avanzando en la direccion anterior
    *
    */
    
    /*
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
    
    */
    
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
                  
                  JsonObject objetoSensor = Json.parse(inbox.getContent()).asObject();
                  objetoSensor = objetoSensor.get("result").asObject();
                  JsonArray vectorSensor = objetoSensor.get("sensor").asArray();
                  leerSensor(vectorSensor);
              } 
          } catch (InterruptedException ex) {
              Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return !"".equals("");
        
    };
    
    /**
    * @author nacho
    */
    private void leerSensor(JsonArray respuesta) throws JSONException{
        
        // Mostramos los datos proporcionados por los sensores
        System.out.println("Los datos recibidos son: ");
        System.out.println("\n Radar: ");
        
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
                break;
            
            case 1: 
                tipoVehiculo = TipoVehiculo.COCHE;
                fuelRate = 1;
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