    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.varia.NullAppender;
import org.apache.log4j.xml.DOMConfigurator;


/**
 *
 * @author Dani
 */
public class Helloworld {

    public static final String NOMBRE_AGENTE = "Juanicon";
    private static String nombreLider = "Liderrr";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

       org.apache.log4j.BasicConfigurator.configure(new NullAppender());
       Agente a,b,c,d; 
       Lider lidl;
       
       AgentsConnection.connect("isg2.ugr.es",6000,"Bellatrix","Escorpion","Russo",false);
        try {
            lidl = new Lider(new AgentID(nombreLider));
            lidl.start();

            a = new Agente(new AgentID(NOMBRE_AGENTE));
            a .start();
            Thread.sleep(10000); 
            b = new Agente(new AgentID("Gregon"));
            b .start();
            Thread.sleep(3000); 
           c = new Agente(new AgentID("Blade5n"));
            c .start();
              Thread.sleep(3000); 
            d = new Agente(new AgentID("Paco5nn"));
            d .start();
        } catch (Exception ex) {
            System.out.println("Error al crear el agente ");
        }
 
    }
    
}
