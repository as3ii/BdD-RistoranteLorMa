package it.ristorantelorma;

import it.ristorantelorma.model.DatabaseConnectionManager;

/**
 * Application entry point class.
 */
public final class RistoranteLorMa {

    /**
     * Entry-point class non instantiable.
     */
    private RistoranteLorMa() { }

    /**
     * Application entry-point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnectionManager.getInstance().close();
        }));
        javax.swing.SwingUtilities.invokeLater(() -> {
            new it.ristorantelorma.view.delivery.FirstPage().show();
        });
    }
}
