/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shenron;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import jason.asSemantics.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ruben
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Conexion con el servidor
        AgentsConnection.connect("isg2.ugr.es", 6000, "test", "Escorpion", "Russo", false);
        
        try {
            
            Agente nuevo_agente = new Agente(new AgentID("Mutenroshi"));
            
            nuevo_agente.start();
        } catch (Exception ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
