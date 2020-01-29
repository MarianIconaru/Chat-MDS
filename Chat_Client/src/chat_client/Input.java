/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_client;

/**
 *
 * @author
 */
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//class Client
//{
//    public static Boolean ok=true;
//}

class Input implements Runnable {

    DataInputStream stream;
    public static Boolean ok = true;
    public static String fisier = "";

    public Input(Socket s) throws IOException {
        stream = new DataInputStream(s.getInputStream());

        new Thread(this).start();
    }

    public void run() {
        String mesaj = "";
        boolean a = true;
        while (ok) {
            while (a) {
                try {
                    if (ok) {
                        mesaj = stream.readUTF();
                    }
                } catch (IOException ex) {
                    System.out.println("Socketul a fost deja inchis");
                }
                Login.eroare.setText(mesaj);
                if (mesaj.equals("bine")) {
                    a = false;
                }
            }
            try {
                mesaj = stream.readUTF();
                System.out.println(mesaj);
                StringTokenizer st = new StringTokenizer(mesaj);
                String s1 = "";
                if (st.hasMoreTokens()) {
                    s1 = st.nextToken();
                }
                if (s1.equals("/adauga")) {
                    String s2 = st.nextToken();
                    if (!s2.equals("prost")) {
                        Meniu.m.adauga(s2, st.nextToken());
                    } else {
                        JOptionPane.showMessageDialog(Meniu.m, "Nu exista nici un utilizator cu aceasta porecla", "Eroare", JOptionPane.ERROR_MESSAGE);
                    }

                } else if (s1.equals("/prieteni")) {
                    while (st.hasMoreTokens()) {
                        Meniu.m.adauga(st.nextToken(), st.nextToken());
                    }
                } else if (s1.equals("/transfer")) {
                    String fisier1 = st.nextToken();
                    fisier1 = "D:\\Chat\\" + fisier1;
                    System.out.println("Se primeste fisierul : "
                            + fisier1);
                    FileOutputStream fos = new FileOutputStream(fisier1);
                    int k;
                    while (((k = stream.read()) != -1) && k != 0) {
                        fos.write(k);
                    }
                    fos.close();
                    System.out.println("Fisier primit !");
                } /*else if (s1.equals("/incerc"))
                {
                    String s2=st.nextToken();
                    int poz=Meniu.m.pozitie(s2);
                    if (Meniu.c[poz] == null )
                    {
                        Meniu.c[poz] = new Chat_Client(s1.substring(1, s1.length()-2));
                        Meniu.c[poz].setVisible(true);
                    }
                    int option = JOptionPane.showConfirmDialog(Meniu.c[poz],
                                                            "Would you like green eggs and ham?",
                                                            "An Inane Question",
                                                            JOptionPane.YES_NO_OPTION);

                }*/ else if (s1.equals("/stare")) {
                    String s2 = st.nextToken();
                    //System.out.println(s2);
                    if (st.nextToken().equals("@")) {
                        Meniu.m.stare(true, s2);
                    } else {
                        Meniu.m.stare(false, s2);
                    }
                } else if (s1.equals("/invitatie")) {
                    //int poz=Meniu.m.pozitie(s1.substring(1, s1.length()-2));
                    //System.out.println(poz);
                    String s2 = "";
                    while (st.hasMoreTokens()) {
                        s2 += st.nextToken() + " ";
                    }
                    if (Meniu.conf == null) {
                        Meniu.conf = new Conferinta(s2);
                        Meniu.conf.setVisible(true);
                    }
                    //System.out.println(s1.substring(1, s1.length()-2));
                    Meniu.conf.jTextArea1.setText(Meniu.conf.jTextArea1.getText() + mesaj + "\n");
                } else if (s1.equals("/confquit")) {
                    String s2 = "";
                    if (st.hasMoreTokens()) {
                        s2 = st.nextToken();
                    }
                    int temp = 0;
                    if (Meniu.conf != null) {
                        Meniu.conf.jTextArea1.setText(Meniu.conf.jTextArea1.getText() + s2 + " a iesit din conferinta\n");
                        for (int i = 0; i < Meniu.conf.nrNumePrieteni; i++) {
                            if (s2.equals(Meniu.conf.numePrieteni[i])) {
                                temp = i;
                            }
                        }
                        Meniu.conf.numePrieteni[temp] = Meniu.conf.numePrieteni[Meniu.conf.nrNumePrieteni - 1];
                        Meniu.conf.nrNumePrieteni--;
                    }
                } else if (s1.equals("/msgconf")) {
                    String s2 = "";
                    while (st.hasMoreTokens()) {
                        s2 += st.nextToken() + " ";
                    }
                    Meniu.conf.jTextArea1.setText(Meniu.conf.jTextArea1.getText() + s2 + "\n");
                } else {
                    //if (s1.equals("["+Chat_Client.c.getTitle()+"]:"));
                    //System.out.println(s1);
                    //System.out.println(Meniu.m.c[0].getTitle());
                    int poz = Meniu.m.pozitie(s1.substring(1, s1.length() - 2));
                    //System.out.println(poz);
                    if (Meniu.c[poz] == null) {
                        Meniu.c[poz] = new Chat_Client(s1.substring(1, s1.length() - 2));
                        Meniu.c[poz].setVisible(true);
                    }
                    //System.out.println(s1.substring(1, s1.length()-2));
                    Meniu.c[poz].jTextArea1.setText(Meniu.c[poz].jTextArea1.getText() + mesaj + "\n");
                }
            } catch (IOException e) {
                if (ok) {
                    System.out.println("[eroare1]: " + e.getMessage());
                }

                return;

            }
        }
    }
}
