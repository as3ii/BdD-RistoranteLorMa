package it.ristorantelorma.view.restaurant;

import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.FoodType;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.user.RestaurantUser;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import it.ristorantelorma.model.MacroType;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collection;

/**
 * Finestra che mostra e gestisce il menù del ristorante.
 */
public final class RestaurantMenuPage {
    private final JFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final Restaurant restaurant;
    private final Connection connection;

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 400;
    /**
     * Crea la finestra del menù per il ristorante.
     * @param user utente ristorante
     */
    public RestaurantMenuPage(final RestaurantUser user) {
        this.connection = DatabaseConnectionManager.getInstance().getConnection();
        this.restaurant = Restaurant.DAO.findByUsername(connection, user.getUsername()).getValue().orElse(null);

        final JFrame frame = new JFrame("Menù del Ristorante");
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        this.tableModel = new DefaultTableModel(new Object[]{"ID", "Nome", "Prezzo", "Tipologia"}, 0);
        this.table = new JTable(tableModel);
        final JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel();
        final JButton addButton = new JButton("Aggiungi Vivanda");
        final JButton deleteButton = new JButton("Elimina Vivanda");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());
        deleteButton.addActionListener(e -> deleteSelectedFood());

        loadFoods();
        this.frame = frame;
    }

    private void loadFoods() {
        tableModel.setRowCount(0);
        if (restaurant == null) {
            return;
        }
        final Result<Collection<Food>> result = Restaurant.DAO.listFoods(connection, restaurant);
        if (result.isSuccess()) {
            for (final Food food : result.getValue()) {
                tableModel.addRow(new Object[]{food.getId(), food.getName(), food.getPrice(), food.getType().getName()});
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Errore nel caricamento delle vivande: " + result.getErrorMessage());
        }
    }

    private void showAddDialog() {
        final JTextField nameField = new JTextField();
        final JTextField priceField = new JTextField();
        final JComboBox<String> macroTypeBox = new JComboBox<>(new String[]{"cibo", "bevanda"});
        // Recupera le tipologie dalla tabella tipo_vivande
        final JComboBox<String> tipologiaComboBox = new JComboBox<>();
        final Result<Collection<FoodType>> tipiVivandeResult = FoodType.DAO.list(connection);
        if (tipiVivandeResult.isSuccess()) {
            for (final FoodType ft : tipiVivandeResult.getValue()) {
                tipologiaComboBox.addItem(ft.getName());
            }
        }
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nome:"));
        panel.add(nameField);
        panel.add(new JLabel("Prezzo:"));
        panel.add(priceField);
        panel.add(new JLabel("Macro-Tipologia (cibo/bevanda):"));
        panel.add(macroTypeBox);
        panel.add(new JLabel("Tipologia (es. Antipasto, Primo, Dolce):"));
        panel.add(tipologiaComboBox);
        final int result = JOptionPane.showConfirmDialog(frame, panel, "Aggiungi Vivanda", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            final String name = nameField.getText().trim();
            final String priceStr = priceField.getText().trim();
            final String macroTypeStr = (String) macroTypeBox.getSelectedItem();
            final String tipologiaStr = (String) tipologiaComboBox.getSelectedItem();
            try {
                final BigDecimal price = new BigDecimal(priceStr);
                // Controllo se esiste già la tipologia
                final Result<Collection<FoodType>> tipiVivandeResult1 = FoodType.DAO.list(connection);
                boolean tipoVivandaExists = false;
                if (tipiVivandeResult1.isSuccess()) {
                    for (final FoodType ft : tipiVivandeResult1.getValue()) {
                        if (ft.getName().equalsIgnoreCase(tipologiaStr)
                            && ft.getType().toSQLStr().equalsIgnoreCase(macroTypeStr)) {
                            tipoVivandaExists = true;
                            break;
                        }
                    }
                }
                if (!tipoVivandaExists) {
                    // Inserisci la tipologia se non esiste
                    FoodType.DAO.insert(connection, tipologiaStr, MacroType.fromString(macroTypeStr));
                }
                // Recupera la tipologia
                final FoodType type = FoodType.DAO.find(connection, tipologiaStr).getValue().orElse(null);
                if (type == null) {
                    JOptionPane.showMessageDialog(frame, "Tipologia non valida.");
                    return;
                }
                final Result<Food> insertResult = Restaurant.DAO.addFood(connection, restaurant, name, price, type);
                if (insertResult.isSuccess()) {
                    loadFoods();
                } else {
                    JOptionPane.showMessageDialog(frame, "Errore: " + insertResult.getErrorMessage());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Prezzo non valido.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, "Macro-Tipologia non valida.");
            }
        }
    }

    private void deleteSelectedFood() {
        final int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Seleziona una vivanda da eliminare.");
            return;
        }
        final int foodId = (int) tableModel.getValueAt(selectedRow, 0);
        final int confirm = JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler eliminare questa vivanda?",
                                                    "Conferma", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            final Result<Boolean> deleteResult = Restaurant.DAO.deleteFood(connection, foodId);
            if (deleteResult.isSuccess() && deleteResult.getValue()) {
                loadFoods();
            } else {
                JOptionPane.showMessageDialog(frame, "Errore: " + deleteResult.getErrorMessage());
            }
        }
    }

    /**
     * Mostra la finestra del menù ristorante.
     */
    public void show() {
        frame.setVisible(true);
    }
}
