package it.ristorantelorma.view.restaurant;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import it.ristorantelorma.controller.SimpleLogger;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.FoodType;
import it.ristorantelorma.model.Result;

/**
 * Contains the menu table stuff.
 */
final class MenuTable {

    private static final String CLASS_NAME = MenuTable.class.getName();
    private static final Logger LOGGER = SimpleLogger.getLogger(CLASS_NAME);
    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int PADDING = 5;

    private MenuTable() {
        throw new UnsupportedOperationException(
            "Utility class and cannot be instantiated"
        );
    }

    /**
     * Get the customized menu table.
     * @param data
     * @param types
     * @return custom menu table
     */
    public static JTable getTable(final Collection<Object[]> data, final Collection<FoodType> types) {
        final JTable table = new JTable(new TableModel(data));

        // First column: id (int)
        final TableColumn idColumn = table.getColumnModel().getColumn(0);
        final StringRenderer idRenderer = new StringRenderer();
        idRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        idColumn.setCellRenderer(idRenderer);

        // Second column: name (String)
        table.getColumnModel().getColumn(1).setCellRenderer(new StringRenderer());

        // Third column: price (BigDecimal)
        final TableColumn prColumn = table.getColumnModel().getColumn(2);
        prColumn.setCellRenderer(new PriceColumn());
        prColumn.setCellEditor(new PriceColumn());

        // Fourth column: type (JComboBox<FoodType>)
        final TableColumn tpColumn = table.getColumnModel().getColumn(3);
        tpColumn.setCellRenderer(new TypeColumn(
            types.stream().sorted(Comparator.comparing(FoodType::getName)).toArray(FoodType[]::new)
        ));
        tpColumn.setCellEditor(new TypeColumn(
            types.stream().sorted(Comparator.comparing(FoodType::getName)).toArray(FoodType[]::new)
        ));

        // Fifth column: update/delete buttons
        final TableColumn btColumn = table.getColumnModel().getColumn(4);
        btColumn.setCellRenderer(new ButtonColumn(table));
        btColumn.setCellEditor(new ButtonColumn(table));

        // Set minimum column width (only for the first 2 columns)
        for (final int column : new Integer[]{0, 1}) {
            int maxWidth = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                final FontMetrics metrics = table.getFontMetrics(table.getFont());
                final Object value = table.getValueAt(row, column);
                int width = 0;
                if (value != null) {
                    switch (value) {
                        case String s -> {
                            width = metrics.stringWidth(s);
                            break;
                        }
                        case Integer i -> {
                            width = metrics.stringWidth(i.toString());
                            break;
                        }
                        case BigDecimal d -> {
                            width = metrics.stringWidth(d.toPlainString());
                            break;
                        }
                        default -> {
                            width = metrics.stringWidth(value.toString());
                            break;
                        }
                    }
                }
                maxWidth = Math.max(maxWidth, width);
            }
            maxWidth += PADDING;
            table.getColumnModel().getColumn(column).setMinWidth(maxWidth);
            table.getColumnModel().getColumn(column).setPreferredWidth(maxWidth);
        }

        return table;
    }

    static class TableModel extends AbstractTableModel {

        private static final long serialVersionUID = 826759376L;

        private final String[] columnNames = {
            "Codice",       // 0
            "Nome",         // 1
            "Prezzo",       // 2
            "Tipologia",    // 3
            "Azioni",       // 4
        };
        private List<Object[]> data;

        TableModel(final Collection<Object[]> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(final int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (rowIndex < getRowCount() && columnIndex < getColumnCount() - 1) {
                return data.get(rowIndex)[columnIndex];
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(final int column) {
            return getValueAt(0, column).getClass();
        }

        @Override
        public boolean isCellEditable(final int row, final int column) {
            return column != 0;
        }

        @Override
        public void setValueAt(final Object value, final int row, final int column) {
            if (row < getRowCount() && column < getColumnCount() - 1) {
                data.get(row)[column] = value;
            }
            fireTableCellUpdated(row, column);
        }

        public void removeRow(final int row) {
            final List<Object[]> oldData = data;
            data = new ArrayList<>(oldData.size() - 1);
            for (int i = 0; i < oldData.size(); i++) {
                if (i != row) {
                    data.add(oldData.get(i));
                }
            }
            fireTableRowsDeleted(row, row);
        }

        public void addRow(final Object[] rowValues) {
            data.add(rowValues);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }
    }

    static final class StringRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 8859274012L;

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            return super.getTableCellRendererComponent(table, value, false, false, row, column);
        }
    }

    static final class PriceColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

        public static final long serialVersionUID = 73981204712L;

        private final JFormattedTextField comp;

        PriceColumn() {
            comp = new JFormattedTextField();
            final NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setCurrency(Currency.getInstance("EUR"));
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            final NumberFormatter formatter = new NumberFormatter(format) {
                @Override
                public Object stringToValue(final String text) throws ParseException {
                    String t = text;
                    if (t != null && !t.startsWith(format.getCurrency().getSymbol())) {
                        t = format.getCurrency().getSymbol() + t;
                    }
                    return super.stringToValue(t);
                }
            };
            comp.setFormatterFactory(new DefaultFormatterFactory(formatter));
            comp.setValue(BigDecimal.ZERO);
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            comp.setValue(((BigDecimal) value).setScale(2, RoundingMode.HALF_UP));
            table.getColumnModel().getColumn(column).setMinWidth(comp.getMinimumSize().width);
            return comp;
        }

        @Override
        public Object getCellEditorValue() {
            return new BigDecimal(comp.getValue().toString()).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
                final int row, final int column) {
            if (value != null) {
                comp.setValue(((BigDecimal) value).setScale(2, RoundingMode.HALF_UP));
            }
            return comp;
        }
    }

    static final class TypeColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

        public static final long serialVersionUID = 7394823461L;
        private final JComboBox<FoodType> selector;

        TypeColumn(final FoodType[] values) {
            selector = new JComboBox<>(values);
            selector.setRenderer(
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
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            selector.setSelectedItem(value);
            table.getColumnModel().getColumn(column).setMinWidth(selector.getMinimumSize().width);
            return selector;
        }

        @Override
        public Object getCellEditorValue() {
            return selector.getSelectedItem();
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
                final int row, final int column) {
            return selector;
        }
    }

    static final class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

        public static final long serialVersionUID = 739457201L;
        private static final String UPDATE = "✎";
        private static final String DELETE = "🗑";

        private final JPanel panel;
        private final JButton updateButton;
        private final JButton deleteButton;
        private int row;

        ButtonColumn(final JTable table) {
            panel = new JPanel(new FlowLayout());

            final Connection conn = DatabaseConnectionManager.getInstance().getConnection();

            updateButton = new JButton(UPDATE);
            updateButton.addActionListener(e -> {
                final int id = (Integer) table.getValueAt(row, 0);
                final String name = (String) table.getValueAt(row, 1);
                final BigDecimal price = (BigDecimal) table.getValueAt(row, 2);
                final FoodType type = (FoodType) table.getValueAt(row, 3);
                final Result<Optional<Food>> resFood = Food.DAO.find(conn, id);
                if (!resFood.isSuccess() || resFood.getValue().isEmpty()) {
                    LOGGER.log(Level.WARNING, "Failed search of Food with ID: " + id);
                }
                final Food food = resFood.getValue().get();
                final Result<Food> resNewFood = Food.DAO.update(conn, food, name, price, type);
                if (!resNewFood.isSuccess()) {
                    JOptionPane.showMessageDialog(
                        panel,
                        "Errore nel aggiornamento della vivanda.\n" + resNewFood.getErrorMessage(),
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        panel,
                        "Vivanda aggiornata correttamente",
                        "Successo",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });
            deleteButton = new JButton(DELETE);
            deleteButton.addActionListener(e -> {
                final int id = (Integer) table.getValueAt(row, 0);
                final Result<Optional<Food>> resFood = Food.DAO.find(conn, id);
                if (!resFood.isSuccess() || resFood.getValue().isEmpty()) {
                    LOGGER.log(Level.WARNING, "Failed search of Food with ID: " + id);
                }
                final Food food = resFood.getValue().get();
                final Result<?> resDelFood = Food.DAO.delete(conn, food);
                if (!resDelFood.isSuccess()) {
                    JOptionPane.showMessageDialog(
                        panel,
                        "Errore nella rimozione della vivanda.\n" + resDelFood.getErrorMessage(),
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    final int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        ((TableModel) table.getModel()).removeRow(selectedRow);
                        JOptionPane.showMessageDialog(
                            panel,
                            "Vivanda rimossa correttamente",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            });
            panel.add(updateButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final int height = Math.max(
                table.getRowHeight(row),
                panel.getMinimumSize().height
            );
            table.setRowHeight(row, height);
            table.getColumnModel().getColumn(column).setMinWidth(panel.getMinimumSize().width);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
                final int row, final int column) {
            this.row = row;
            return panel;
        }
    }
}
