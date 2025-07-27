package it.ristorantelorma.view.customer;

import javax.swing.*;
import java.awt.*;

public class ResMenu extends JFrame {

    public ResMenu(String restaurantName, String cuisine) {
        setTitle("DeliveryDB");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Layout principale
        JPanel mainPanel = new JPanel(new BorderLayout());

        // --- SINISTRA: Menu ---
        String[][] menuData = {
            {"Vitel tonné", "Cibo", "18.0$"},
            {"Baci di dama", "Cibo", "9.5$"},
            {"Risotto al Barolo", "Cibo", "9.5$"},
            {"Insalata russa", "Cibo", "9.5$"},
            {"Bagna Caoda", "Cibo", "5.5$"},
            {"Bonet", "Cibo", "25.0$"},
            {"Gianduiotto", "Cibo", "19.0$"},
            {"Fritto misto alla Piemontese", "Cibo", "5.0$"},
            {"Brasato al Barolo", "Cibo", "17.0$"},
            {"Agnolotti", "Cibo", "15.0$"},
            {"Acciughe al verde", "Cibo", "12.0$"},
            {"Bicerin", "Bevanda", "5.0$"},
            {"D.O.C.G.", "Bevanda", "30.0$"},
            {"Alta Langa", "Bevanda", "35.0$"},
            {"Asti", "Bevanda", "25.0$"},
            {"Barbaresco", "Bevanda", "40.0$"},
            {"Barbera d'Asti", "Bevanda", "22.0$"},
            {"Ruchè di Castagnole Monferrato", "Bevanda", "25.0$"},
            {"Roero", "Bevanda", "28.0$"},
            {"Nizza", "Bevanda", "40.0$"}
        };
        String[] menuColumns = {"", "", "", ""};

        JPanel menuPanel = new JPanel(new BorderLayout());
        JPanel menuTablePanel = new JPanel();
        menuTablePanel.setLayout(new BoxLayout(menuTablePanel, BoxLayout.Y_AXIS));

        // Header invisibile
        JPanel header = new JPanel(new GridLayout(1, 4));
        header.add(new JLabel(""));
        header.add(new JLabel(""));
        header.add(new JLabel(""));
        header.add(new JLabel(""));
        menuTablePanel.add(header);

        // Riga per ogni piatto
        JSpinner[] quantitySpinners = new JSpinner[menuData.length];
        for (int i = 0; i < menuData.length; i++) {
            JPanel row = new JPanel(new GridLayout(1, 4));
            row.add(new JLabel(menuData[i][0]));
            row.add(new JLabel(menuData[i][1]));
            quantitySpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            row.add(quantitySpinners[i]);
            row.add(new JLabel(menuData[i][2]));
            menuTablePanel.add(row);
        }

        JScrollPane menuScroll = new JScrollPane(menuTablePanel);
        menuScroll.setPreferredSize(new Dimension(500, 400));
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // Pulsanti Back e Send Order
        JPanel buttonPanel = new JPanel();
        JButton backButton = new JButton("Back");
        JButton sendOrderButton = new JButton("Send Order");
        sendOrderButton.setBackground(Color.RED);
        sendOrderButton.setForeground(Color.WHITE);
        buttonPanel.add(backButton);
        buttonPanel.add(sendOrderButton);
        menuPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // --- DESTRA: Info, riepilogo, saldo ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(300, 400));

        JLabel balanceLabel = new JLabel("Balance: $20000.00");
        JLabel orderSummaryLabel = new JLabel("Order Summary:");
        JTextArea orderSummaryArea = new JTextArea(6, 20);
        orderSummaryArea.setEditable(false);
        JLabel totalLabel = new JLabel("Total: $0.0");

        JPanel restaurantInfoPanel = new JPanel();
        restaurantInfoPanel.setLayout(new BoxLayout(restaurantInfoPanel, BoxLayout.Y_AXIS));
        restaurantInfoPanel.add(new JLabel("Restaurant Info:"));
        restaurantInfoPanel.add(new JLabel("Restaurant: " + restaurantName));
        restaurantInfoPanel.add(new JLabel("Opening Hours: 12:30 - 22:30"));
        JButton reviewsButton = new JButton("Reviews");
        reviewsButton.setBackground(Color.BLUE);
        reviewsButton.setForeground(Color.WHITE);
        restaurantInfoPanel.add(reviewsButton);

        infoPanel.add(balanceLabel);
        infoPanel.add(orderSummaryLabel);
        infoPanel.add(orderSummaryArea);
        infoPanel.add(totalLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(restaurantInfoPanel);

        mainPanel.add(infoPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
    }
}
