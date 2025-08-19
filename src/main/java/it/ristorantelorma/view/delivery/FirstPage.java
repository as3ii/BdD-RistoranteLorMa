package it.ristorantelorma.view.delivery;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import it.ristorantelorma.view.authentication.LoginPage;
import it.ristorantelorma.view.authentication.RegisterPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Rappresenta la pagina iniziale dell'applicazione che fornisce le opzioni per
 * effettuare il login o registrarsi.
 */
public class FirstPage {

    private static final Color BUTTON_BACKGROUND_COLOR = new Color(60, 179, 113); // Verde chiaro
    private static final Color BUTTON_BACKGROUND_HOVER_COLOR = new Color(34, 139, 34); // Verde scuro
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final int BUTTON_FONT_SIZE = 16;
    private static final Dimension BUTTON_DIMENSION = new Dimension(200, 50);
    private static final int BUTTON_SPACING = 10;
    private static final int PADDING = 10;

    private final JButton loginButton;
    private final JButton registerButton;
    private final JFrame mainFrame;

    /**
     * Costruttore di FirstPage.
     * Inizializza la finestra principale e i componenti dell'interfaccia.
     */
    public FirstPage() {
        this.mainFrame = new JFrame("Ristorante LorMa");
        this.loginButton = createButton("Login");
        this.registerButton = createButton("Registrati");
        initializeFrame();
        initializeUI();
    }

    /**
     * Inizializza le proprietà base del frame principale.
     */
    private void initializeFrame() {
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(400, 300);
        this.mainFrame.setLocationRelativeTo(null); // Centra la finestra
        this.mainFrame.setResizable(false);
    }

    /**
     * Aggiorna il contenuto del frame principale e applica la funzione consumer specificata.
     *
     * @param consumer una funzione Consumer che modifica il content pane
     */
    private void freshPane(final Consumer<Container> consumer) {
        final Container cp = this.mainFrame.getContentPane();
        cp.removeAll();
        cp.validate();
        cp.repaint();
        consumer.accept(cp);
        this.mainFrame.pack();
    }

    /**
     * Inizializza l'interfaccia utente per la prima pagina, incluso il layout e i bottoni.
     */
    private void initializeUI() {
        freshPane(container -> {
            final var padding = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
            ((JComponent) container).setBorder(padding);
            container.setLayout(new BorderLayout());
            container.setBackground(Color.WHITE);

            // Crea un pannello per i bottoni
            final JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.setBackground(Color.WHITE);

            buttonPanel.add(Box.createVerticalGlue()); // Spazio per centrare i bottoni
            buttonPanel.add(createAlignedButtonPanel(loginButton));
            buttonPanel.add(Box.createRigidArea(new Dimension(0, BUTTON_SPACING))); // Spazio tra i bottoni
            buttonPanel.add(createAlignedButtonPanel(registerButton));
            buttonPanel.add(Box.createVerticalGlue()); // Spazio per centrare i bottoni

            // Aggiunge il pannello dei bottoni al centro del frame principale
            container.add(buttonPanel, BorderLayout.CENTER);
        });

        // Ridimensiona la finestra per adattarsi al contenuto
        this.mainFrame.setSize(400, 300);
    }

    /**
     * Crea un bottone con il testo specificato, styling personalizzato e action listeners.
     *
     * @param text il testo da visualizzare sul bottone
     * @return il JButton creato
     */
    private JButton createButton(final String text) {
        final JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, BUTTON_FONT_SIZE));
        button.setBackground(BUTTON_BACKGROUND_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(BUTTON_DIMENSION);

        // Aggiunge effetto hover - cambia colore di sfondo quando il mouse entra
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent evt) {
                button.setBackground(BUTTON_BACKGROUND_HOVER_COLOR);
            }

            @Override
            public void mouseExited(final MouseEvent evt) {
                button.setBackground(BUTTON_BACKGROUND_COLOR);
            }
        });

        // Aggiunge action listener per gestire i click
        button.addActionListener(e -> {
            if ("Login".equals(text)) {
                handleLoginButtonClick();
            } else if ("Registrati".equals(text)) {
                handleRegisterButtonClick();
            }
        });

        return button;
    }

    /**
     * Crea un pannello con il bottone specificato centrato orizzontalmente.
     *
     * @param button il bottone da centrare
     * @return il JPanel contenente il bottone centrato
     */
    private JPanel createAlignedButtonPanel(final JButton button) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(Box.createHorizontalGlue());
        panel.add(button);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    /**
     * Gestisce il click del bottone Login.
     * Apre la pagina di login.
     */
    private void handleLoginButtonClick() {
        this.hide();
        new LoginPage(this).show();
    }

    /**
     * Gestisce il click del bottone Registrati.
     * Per ora stampa solo un messaggio nella console.
     */
    private void handleRegisterButtonClick() {
        this.hide();
        new RegisterPage(this).show();
    }

    /**
     * Mostra la finestra principale.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }

    /**
     * Nasconde la finestra principale.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }

    /**
     * Metodo main per testare la FirstPage.
     * Crea e visualizza la finestra.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FirstPage().show();
        });
    }
}
