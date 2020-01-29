/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_server;

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author IMC
 */
public class Server {

    //logger + private fields
    private final static Logger logger = Logger.getLogger(Server.class.getName());
    private static ServerSocket serverSocket;
    private static volatile int counter = 0;
    private static Connection connection = null;
    private static Statement stmt = null;

    public static Statement getStatement() {
        return stmt;
    }
    public static int getCounter(){
        return counter;
    }

    public static void connectToDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            logger.severe(ex.toString());
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/messenger", "root", "root");
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            stmt = connection.createStatement();
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
    }

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(1234);
            logger.info("The server started!");
        } catch (IOException ex) {
            logger.severe("Unable to start the server");
        }
        connectToDB();

        Client[] c = new Client[39];
        while (true) {
            c[counter] = new Client();
            try {
                c[counter].setSocket(serverSocket.accept());
                logger.info("We have a client! ");
                new ServerInput(c, counter);
                ++counter;
            } catch (IOException ex) {
                logger.severe(ex.toString());
                return;
            }
        }
    }

    @Override
    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable ex) {
            logger.severe(ex.toString());
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }

    }
}
