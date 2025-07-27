package it.ristorantelorma.view.customer;

import javax.swing.*;
import java.awt.*;

public class RestaurantsPage extends JFrame {

    public RestaurantsPage() {
        setTitle("RestaurantsPage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        // Panel principale
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Lista ristoranti (nome e cucina)
        String[][] data = {
                { "Osteria dei Sapori", "Emiliana" },
                { "Ristorante del Porto", "Frutti di mare" },
                { "Ramen Bar", "Giapponese" },
                { "Sushi Time", "Giapponese" },
                { "Sushi Master", "Giapponese" },
                { "Ristorante dei Sapori", "Italiana" },
                { "Ristorante della Nonna", "Italiana" },
                { "Trattoria da Mario", "Italiana" },
                { "Ristorante da Genova", "Ligure" },
                { "Ristorante Mare e Monti", "Mediterranea" },
                { "La Taverna del Piemonte", "Piemontese" },
                { "La Pizzeria di Napoli", "Pizza" },
                { "Pizzeria di Franco", "Pizza" },
                { "Trattoria Siciliana", "Siciliana" },
                { "Ristorante del Sud", "Siciliana" },
                { "Osteria Toscanella", "Toscana" },
                { "Ristorante La Florentina", "Toscana" },
                { "Eccomi Kebab", "Turca" },
                { "La Trattoria di Venezia", "Veneta" }
        };
        String[] columns = { "", "" };

        JTable table = new JTable(data, columns);
        table.setEnabled(false);
        table.setRowSelectionAllowed(true); // Permetti selezione riga
        table.setShowGrid(false);
        table.setTableHeader(null);
        table.setFont(new Font("Dialog", Font.PLAIN, 18));
        table.setRowHeight(28);

        // Listener per click sulla riga
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    String restaurantName = data[row][0];
                    String cuisine = data[row][1];
                    // Apri la finestra ResMenu
                    ResMenu resMenu = new ResMenu(restaurantName, cuisine);
                    resMenu.setVisible(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottone Logout
        JPanel buttonPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        buttonPanel.add(logoutButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}