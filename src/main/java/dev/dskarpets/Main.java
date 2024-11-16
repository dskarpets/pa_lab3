package dev.dskarpets;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            new GUI(dbManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


