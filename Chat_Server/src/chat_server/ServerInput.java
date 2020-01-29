/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author IMC
 */
public final class ServerInput implements Runnable {

    private final static Logger logger = Logger.getLogger(Server.class.getName());
    private DataInputStream DIS;
    private String message;
    private Client c1;
    private Client[] c2;

    public ServerInput(Client[] c, int i) {
        this.c1 = c[i];
        this.c2 = c;
        int a = 1;
        try {
            DIS = new DataInputStream(c1.getSocket().getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        String stare = "";
        //in acest while iau porecla clientului, se repeta pana cand serverul primeste o porecla unica
        while (a == 1) {
            try {
                message = DIS.readUTF();
                StringTokenizer st = new StringTokenizer(message);
                //st.nextToken();
                stare = st.nextToken().toString();
                if (st.hasMoreTokens()) {
                    c1.setName(st.nextToken().toString());
                }
                if (st.hasMoreTokens()) {
                    c1.setPassword(st.nextToken().toString());
                }
//               else {
//                    c1.setPassword("");
//                }
                //c1.nume=mesaj;
            } catch (IOException e) {
                System.out.println("[eroare]: " + stare);
            }
            if (stare.equals("/quit")) {
                c1.setOk(false);
                try {
                    c1.getSocket().close();
                } catch (IOException ex) {
                    logger.severe(ex.toString());
                }
                a = 0;
                break;
            } else if (isUnique(c1.getName()) && isRightFormat(c1.getName()) && isRightFormat(c1.getPassword()) && stare.equals("reg")) {
                insertToDB(c1.getName(), c1.getPassword());
                a = 0;
                try {
                    afiseaza(c1.getSocket(), "bine");
                } catch (IOException ex) {
                    logger.severe(ex.toString());
                }
            } else if ((checkPerson(c1.getName(), c1.getPassword())) && isRightFormat(c1.getName()) && isRightFormat(c1.getPassword()) && stare.equals("log")) {
                a = 0;
                try {
                    afiseaza(c1.getSocket(), "bine");
                } catch (IOException ex) {
                    logger.severe(ex.toString());
                }
                try {
                    afiseaza(c1.getSocket(), "/prieteni " + listaPrieteni(c1.getName()));
                } catch (IOException ex) {
                    logger.severe(ex.toString());
                }
                stare(c1.getName(), " @");//c1.nume devine online pentru toti
            } else {
                try {
                    afiseaza(c1.getSocket(), "Eroare");
                } catch (IOException ex) {
                    Logger.getLogger(ServerInput.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (!message.equals("/quit")) {
            new Thread(this).start();
        }
    }

    //Verific daca stringul "s" contine doar cifre, litere si caracterul "-"
    public Boolean isRightFormat(String s) {
        if (s == null || s.length() > 16) {
            return false;
        }
        char chr[] = null;
        if (s != null) {
            chr = s.toCharArray();
        }
        for (int i = 0; i < chr.length; i++) {
            if (!((chr[i] >= '0' && chr[i] <= '9') || (chr[i] >= 'A' && chr[i] <= 'Z') || (chr[i] >= 'a' && chr[i] <= 'z') || (chr[i] == '-'))) {
                return false;
            }
        }
        return true;
    }

    //afiseaza16  prietenii persoanei person
    public String listaPrieteni(String person) {
        String s1 = "", s2 = "";
        String sql = "select f.* from people_friends join people p on people_id=p.id join friends f on friends_id=f.id where p.porecla='" + person + "'";
        ResultSet rs = null;
        Socket socket = null;
        try {
            rs = Server.getStatement().executeQuery(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            while (rs.next()) {
                s2 = rs.getString("porecla");
                s1 += s2;
                if ((socket = cauta(c2, s2)) != null) {
                    s1 += " @";//online
                } else {
                    s1 += " *";//offline
                }
                s1 += " ";
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerInput.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s1;
    }
//introduce cont nou si in people si in friends (many to many)

    //se afiseaza sirul de caractere "str" la clientul cu socket "s"
    public synchronized void afiseaza(Socket s, String str) throws IOException {
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        out.writeUTF(str);
    }

    public synchronized void transfera(DataInputStream stream, DataOutputStream out) throws IOException {
        int k;
        while (((k = stream.read()) != -1) && k != 0) {
            out.write(k);
        }
        out.write(0);
    }

    //se afiseaza numele tuturor clientilor inclusiv clientului curent
    public void list(Client c[], Socket s) throws IOException {
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        for (int i = 1; i < Server.getCounter(); i++) {
            if (c[i].getOk()) {
                out.writeUTF(c[i].getName());
            }
        }
    }

    //returneaza socketul cu numele s daca exista, altfel returneaza null
    public Socket cauta(Client c[], String s) {
        for (int i = 1; i < Server.getCounter(); i++) {
            if (c[i].getName().equals(s) && c[i].getOk()) {
                return c[i].getSocket();
            }
        }
        return null;
    }

    //trimite mesajul "str" catre toti clientii in afara de cel cu socketul "s"
    public void bcast(Client c[], Socket s, String str) throws IOException {
        for (int i = 1; i < Server.getCounter(); i++) {
            if (c[i].getSocket() != s && c[i].getOk()) {
                afiseaza(c[i].getSocket(), str);
            }
        }
    }

    //trimite mesajul "str" catre clientul cu porecla "nume"
    public void msg(Client c[], Socket s, String nume, String str) throws IOException {
        Socket s1;
        if ((s1 = cauta(c, nume)) != null) {
            //if (s1==s) afiseaza(s,"Nu iti poti trimite mesaj tie insuti");
            //else afiseaza(s1,str);
            afiseaza(s1, str);
        } else {
            //afiseaza(s, "Nu exista nici un client cu numele " + nume);
            System.out.println("Nu exista nici un client cu numele " + nume);
        }
    }

    //schimba porecla clientului "c" su stringul "s"
    public void nick(Client c, String s) {
        c.setName(s);
    }

    public void stare(String s, String str) {
        Socket stest = null;
        StringTokenizer st = new StringTokenizer(listaPrieteni(s));
        while (st.hasMoreTokens()) {
            if ((stest = cauta(c2, st.nextToken())) != null) {
                try {
                    afiseaza(stest, "/stare " + s + str);
                } catch (IOException ex) {
                    logger.severe(ex.toString());
                }
            }
        }
    }

    public synchronized void insertToDB(String porecla, String parola) {
        String sql = "insert into people(porecla,parola) values('" + porecla + "','" + parola + "')";
        try {
            Server.getStatement().executeUpdate(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        sql = "insert into friends(porecla) values('" + porecla + "')";
        try {
            Server.getStatement().executeUpdate(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
    }

    //adauga prietenie intre person si friend
    public synchronized void addFriend(String person, String friend) {
        if (checkFriend(person, friend)) {
            return;
        }
        String sql = "insert into people_friends(people_id,friends_id) values((select id from people where porecla='" + person + "'),(select id from friends where porecla='" + friend + "'))";
        String sql1 = "insert into people_friends(people_id,friends_id) values((select id from people where porecla='" + friend + "'),(select id from friends where porecla='" + person + "'))";

        try {
            Server.getStatement().executeUpdate(sql);
            Server.getStatement().executeUpdate(sql1);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
    }
//daca vreau sa imi creez cont cu o porecla deja folosita

    public boolean isUnique(String porecla) {
        ResultSet rs = null;
        String sql = "select * from friends where porecla='" + porecla + "'";
        try {
            rs = Server.getStatement().executeQuery(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            if (!rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        return false;
    }
    //returneaza true daca exista in baza de date persoana cu (porecla,parola)

    public boolean checkPerson(String porecla, String parola) {
        ResultSet rs = null;
        String sql = "select * from people where porecla='" + porecla + "' and parola='" + parola + "'";
        try {
            rs = Server.getStatement().executeQuery(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        return false;
    }
//returneaza true daca exista o relatie de prietenie intre cei doi

    public boolean checkFriend(String person, String friend) {
        String sql = "select * from people_friends join people p on people_id=p.id join friends f on friends_id=f.id where p.porecla='" + person + "' and f.porecla='" + friend + "'";
        ResultSet rs = null;
        try {
            rs = Server.getStatement().executeQuery(sql);
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        try {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            logger.severe(ex.toString());
        }
        return false;
    }

    @Override
    public void run() {
        while (c1.getOk()) {
            try {
                String s3 = "", s1 = "", s2 = "";;
                Socket stest = null;
                message = DIS.readUTF();
                StringTokenizer st = new StringTokenizer(message);
                if (st.hasMoreTokens()) {
                    s3 = st.nextToken();
                }
                if (message.equals("/quit")) {
                    c1.setOk(false);
                    c1.getSocket().close();
                    stare(c1.getName(), " *");
                } else if (message.equals("/list")) {
                    list(c2, c1.getSocket());
                } else if (s3.equals("/msg")) {
                    //separ stringul in cuvinte pt a alege parametrii metodei "msg"

                    s1 = st.nextToken();
                    while (st.hasMoreTokens()) {
                        s2 += st.nextToken() + " ";
                    }
                    msg(c2, c1.getSocket(), s1, "[" + c1.getName() + "]: " + s2);
                } else if (s3.equals("/transfer")) {
                    //separ stringul in cuvinte pt a alege parametrii metodei "msg"
                    String fisier = "";
                    s1 = st.nextToken();
                    while (st.hasMoreTokens()) {
                        fisier += st.nextToken() + " ";
                    }
                    int p = fisier.lastIndexOf("\\");
                    fisier = fisier.substring(p + 1, fisier.length());
                    msg(c2, c1.getSocket(), s1, "[" + c1.getName() + "]: " + "se trimite fisierul " + fisier);
                    Socket soc;
                    DataOutputStream out = null;
                    if ((soc = cauta(c2, s1)) != null) {
                        out = new DataOutputStream(soc.getOutputStream());
                    } else {
                        afiseaza(c1.getSocket(), "Nu exista nici un client cu numele " + s1);
                    }
                    msg(c2, c1.getSocket(), s1, "/transfer " + fisier);
                    System.out.println("Se primeste fisierul : "
                            + fisier);
                    FileOutputStream fos = new FileOutputStream(fisier);
                    transfera(DIS, out);
                    /*int k;
                    while( ((k=DIS.read()) != -1)&& k!=0) out.write(k);
                    out.write(0);*/
                    fos.close();
                    System.out.println("Fisier primit !");
                } else if (s3.equals("/cauta")) {
                    System.out.println("aici");
                    if (st.hasMoreTokens()) {
                        s1 = st.nextToken();
                    }
                    if (!isUnique(s1)) {
                        if ((stest = cauta(c2, s1)) != null) {
                            afiseaza(c1.getSocket(), "/adauga " + s1 + " @");
                        } else {
                            afiseaza(c1.getSocket(), "/adauga " + s1 + " *");
                        }
                        addFriend(c1.getName(), s1);
                    } else {
                        afiseaza(c1.getSocket(), "/adauga prost");
                    }
                } else if (s3.equals("/msgconf")) {
                    s1 = st.nextToken();
                    while (st.hasMoreTokens()) {
                        s2 += st.nextToken() + " ";
                    }
                    msg(c2, c1.getSocket(), s1, "/msgconf " + "[" + c1.getName() + "]: " + s2);
                } else if (s3.equals("/confquit")) {
                    s1 = st.nextToken();
                    while (st.hasMoreTokens()) {
                        s2 += st.nextToken() + " ";
                    }
                    msg(c2, c1.getSocket(), s1, "/confquit " + c1.getName());
                } else if (s3.equals("/conf")) {
                    //if (st.hasMoreTokens()) s2 = st.nextToken();
                    String numePrieteni[] = new String[30];
                    int nrPrieteni;
                    nrPrieteni = 0;
                    while (st.hasMoreTokens()) {
                        numePrieteni[nrPrieteni] = st.nextToken();
                        nrPrieteni++;
                    }
                    for (int i = 0; i < nrPrieteni; i++) {
                        s2 = "";
                        for (int j = 0; j < nrPrieteni; j++) {
                            if (j != i) {
                                s2 += numePrieteni[j] + " ";
                            }
                        }
                        msg(c2, c1.getSocket(), numePrieteni[i], "/invitatie " + c1.getName() + " " + s2);
                    }
                } else if (s3.equals("/bcast")) {
                    bcast(c2, c1.getSocket(), "[" + c1.getName() + "]: " + message.substring(7));
                } else if (s3.equals("/nick")) {
                    if (isUnique(message.substring(6))) {
                        if (isRightFormat(message.substring(6))) {
                            nick(c1, message.substring(6));
                        } else if (message.substring(6).length() > 16) {
                            afiseaza(c1.getSocket(), "Porecla trebuie sa aiba maxim 16 caractere");
                        } else {
                            afiseaza(c1.getSocket(), "In componenta poreclei nu puteti folosi alte caractere in afara de litere, cifre si - ");
                        }
                    } else if (c1.getName().equals(message.substring(6))) {
                        afiseaza(c1.getSocket(), "Ai deja aceasta porecla");
                    } else {
                        afiseaza(c1.getSocket(), "Porecla este deja folosita de alt utilizator");
                    }
                }
                System.out.println("[" + c1.getName() + "]: " + message);
            } catch (IOException e) {
                System.out.println("[eroare1]: " + e.getMessage());

            }
        }
    }

}
