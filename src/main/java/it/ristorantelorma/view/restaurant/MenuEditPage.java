package it.ristorantelorma.view.restaurant;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.FoodType;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.view.ViewUtils;
import it.ristorantelorma.view.ViewUtils.Form;

/**
 * Menu editing window.
 */
public final class MenuEditPage {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 250;
    private static final int FONT_SIZE = 14;

    private final JFrame frame;
    private final JTable table;

    /**
     * @param restaurant
     */
    public MenuEditPage(final Restaurant restaurant) {
        frame = new JFrame("RestaurantManagementPage");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        final JLabel title = new JLabel(restaurant.getRestaurantName(), SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.PLAIN, FONT_SIZE * 3 / 2)); // Around FONT_SIZE * 1.5
        frame.add(title, BorderLayout.NORTH);

        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();

        final Result<Collection<FoodType>> resTypes = FoodType.DAO.list(conn);
        if (!resTypes.isSuccess()) {
            JOptionPane.showMessageDialog(
                null,
                "Errore nella raccolta della lista tipologie.\n" + resTypes.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            table = new JTable();
            return;
        }
        final Collection<FoodType> types = resTypes.getValue();

        final Result<Collection<Food>> resFoods = Food.DAO.list(conn, restaurant);
        if (!resFoods.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella raccolta della lista vivande.\n" + resFoods.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            table = new JTable();
            return;
        }
        final Collection<Food> foods = resFoods.getValue().stream().sorted(Comparator.comparing(Food::getId)).toList();
        final List<Object[]> content = new ArrayList<>();
        for (final Food food : foods) {
            content.add(new Object[]{
                food.getId(),
                food.getName(),
                food.getPrice(),
                food.getType(),
            });
        }

        table = MenuTable.getTable(content, types);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        final JButton addButton = new JButton("Aggiungi");
        addButton.addActionListener(e -> {
            final JDialog dialog = new JDialog(frame, "Aggiungi vivanda", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setLocationRelativeTo(frame);

            final JLabel aTitle = new JLabel("", SwingConstants.CENTER);
            dialog.add(aTitle, BorderLayout.NORTH);


            final Form form = new Form();
            final JTextField nameField = new JTextField();
            form.addField("Nome:", nameField);

            final JFormattedTextField priceField = new JFormattedTextField();
            final NumberFormat format = NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            final NumberFormatter formatter = new NumberFormatter(format);
            priceField.setFormatterFactory(new DefaultFormatterFactory(formatter));
            priceField.setValue(BigDecimal.ZERO);
            form.addField("Prezzo: €", priceField);

            final JComboBox<FoodType> typeField = new JComboBox<>(types.toArray(FoodType[]::new));
            typeField.setRenderer(
                new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(final JList<?> list, final Object value,
                            final int index, final boolean isSelected, final boolean cellHasFocus) {
                        final JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus
                        );
                        if (value != null) {
                            label.setText(((FoodType) value).getName());
                        }
                        return label;
                    }
                }
            );
            form.addField("Tipo:", typeField);

            final JButton submit = new JButton("Invia");
            submit.setEnabled(false); // Default
            submit.addActionListener(f -> {
                final String name = nameField.getText();
                final BigDecimal price = new BigDecimal(priceField.getValue().toString()).setScale(2, RoundingMode.HALF_UP);
                final FoodType type = (FoodType) typeField.getSelectedItem();
                final Result<Food> resFood = Food.DAO.insert(conn, name, restaurant, price, type);
                if (!resFood.isSuccess()) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "Errore nell'inserimento della vivanda.\n" + resFoods.getErrorMessage(),
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                ((MenuTable.TableModel) table.getModel()).addRow(
                    new Object[]{resFood.getValue().getId(), name, price, type}
                );
                dialog.dispose();
            });
            form.addCenterComponent(submit);

            final DocumentListener listener = new DocumentListener() {
                private void handler() {
                    final String name = nameField.getText();
                    final BigDecimal price = new BigDecimal(priceField.getValue().toString()).setScale(2, RoundingMode.HALF_UP);
                    if (ViewUtils.isBlank(name) || price.compareTo(BigDecimal.ZERO) <= 0) {
                        submit.setEnabled(false);
                    } else {
                        submit.setEnabled(true);
                    }
                }
                @Override
                public void insertUpdate(final DocumentEvent e) {
                    handler();
                }
                @Override
                public void removeUpdate(final DocumentEvent e) {
                    handler();
                }
                @Override
                public void changedUpdate(final DocumentEvent e) {
                    handler();
                }
            };
            nameField.getDocument().addDocumentListener(listener);
            priceField.getDocument().addDocumentListener(listener);

            dialog.add(form, BorderLayout.CENTER);
            dialog.setVisible(true);
        });
        frame.add(addButton, BorderLayout.SOUTH);
    }

    /**
     * Shows or hides the Window depending on the value of parameter b.
     * @see JFrame#setVisible(boolean)
     * @param b if true makes the window visible, otherwise hides the window
     */
    public void setVisible(final boolean b) {
        frame.setVisible(b);
    }
}
