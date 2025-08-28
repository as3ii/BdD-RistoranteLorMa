package it.ristorantelorma;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.view.FirstPage;

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
            new FirstPage().show();
        });
    }
}
