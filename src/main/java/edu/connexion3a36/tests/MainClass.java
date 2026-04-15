package edu.connexion3a36.tests;

import edu.connexion3a36.tools.MyConnection;

public class MainClass {
    public static void main(String[] args) {
        MyConnection mc1 = MyConnection.getInstance();
        MyConnection mc2 = MyConnection.getInstance();
        System.out.println(mc1.hashCode() + " - " + mc2.hashCode());
    }
}