package dev.dskarpets;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class GUI {
    private DatabaseManager dbManager;
    private JTable table;
    private DefaultTableModel tableModel;

    public GUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Панель кнопок
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton updateButton = new JButton("Update");
        JButton searchButton = new JButton("Search");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(searchButton);

        // Таблиця даних
        tableModel = new DefaultTableModel(new Object[]{"Key", "Data"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Дії кнопок
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String data = JOptionPane.showInputDialog("Enter data:");
                if (data != null && !data.trim().isEmpty()) {
                    try {
                        dbManager.addRecord(new Record(dbManager.getNextKey(), data));
                        refreshTable();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyStr = JOptionPane.showInputDialog("Enter key to delete:");
                try {
                    int key = Integer.parseInt(keyStr);
                    dbManager.deleteRecord(key);
                    refreshTable();
                } catch (NumberFormatException | IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid key or error occurred.");
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyStr = JOptionPane.showInputDialog("Enter key to update:");
                try {
                    int key = Integer.parseInt(keyStr);
                    String newData = JOptionPane.showInputDialog("Enter new data:");
                    if (newData != null && !newData.trim().isEmpty()) {
                        dbManager.updateRecord(key, newData);
                        refreshTable();
                    }
                } catch (NumberFormatException | IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid key or error occurred.");
                }
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyStr = JOptionPane.showInputDialog("Enter key to search:");
                try {
                    int key = Integer.parseInt(keyStr);
                    Record record = dbManager.search(key);
                    int comparisons = dbManager.getComparisonCount();
                    if (record != null) {
                        JOptionPane.showMessageDialog(frame, "Found: " + record + "\nComparisons: " + comparisons);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Record not found.\nComparisons: " + comparisons);
                    }
                } catch (NumberFormatException | IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid key or error occurred.");
                }
            }
        });

        refreshTable();
        frame.setVisible(true);
    }

    private void refreshTable() {
        try {
            List<Record> records = dbManager.loadAllRecords();
            tableModel.setRowCount(0);
            for (Record record : records) {
                tableModel.addRow(new Object[]{record.getKey(), record.getData()});
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

