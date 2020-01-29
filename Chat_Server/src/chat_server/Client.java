/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_server;

import java.net.Socket;

/**
 *
 * @author IMC
 */
public class Client {

    private Socket socket = null;
    private String name;
    private String password;
    private Boolean ok = true;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getOk() {
        return this.ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }
}
