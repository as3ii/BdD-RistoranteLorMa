package it.ristorantelorma.view.authentication;

import javax.swing.*;

import it.ristorantelorma.view.delivery.FirstPage;
import it.ristorantelorma.view.customer.RestaurantsPage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Rappresenta la pagina di login dell'applicazione.
 * Fornisce i campi per inserire username e password e i bottoni per il login.
 */
public class LoginPage {
    
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Dimension FIELD_DIMENSION = new Dimension(150, 25);
    private static final Dimension BUTTON_DIMENSION = new Dimension(80, 30);
    
    private final JFrame mainFrame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton resetButton;
    private final JButton backButton;
    private final FirstPage parentPage;
    
    /**
     * Costruttore di LoginPage.
     * 
     * @param parentPage la pagina principale da cui è stata aperta questa pagina
     */
    public LoginPage(FirstPage parentPage) {
        this.parentPage = parentPage;
        this.mainFrame = new JFrame("DeliveryDB");
        this.usernameField = new JTextField();
        this.passwordField = new JPasswordField();
        this.loginButton = createButton("Login");
        this.resetButton = createButton("Reset");
        this.backButton = createButton("Back");
        
        initializeFrame();
        initializeUI();
        setupEventHandlers();
    }
    
    /**
     * Inizializza le proprietà base del frame principale.
     */
    private void initializeFrame() {
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(400, 300);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setResizable(false);
        this.mainFrame.getContentPane().setBackground(BACKGROUND_COLOR);
    }
    
    /**
     * Inizializza l'interfaccia utente della pagina di login.
     */
    private void initializeUI() {
        this.mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username label e field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(LABEL_FONT);
        this.mainFrame.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this.usernameField.setPreferredSize(FIELD_DIMENSION);
        this.mainFrame.add(this.usernameField, gbc);
        
        // Password label e field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(LABEL_FONT);
        this.mainFrame.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this.passwordField.setPreferredSize(FIELD_DIMENSION);
        this.mainFrame.add(this.passwordField, gbc);
        
        // Pannello per i bottoni Login e Reset
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(this.loginButton);
        buttonPanel.add(this.resetButton);
        this.mainFrame.add(buttonPanel, gbc);
        
        // Bottone Back
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        this.mainFrame.add(this.backButton, gbc);
    }
    
    /**
     * Crea un bottone con lo stile specificato.
     * 
     * @param text il testo del bottone
     * @return il JButton creato
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_DIMENSION);
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Configura gli event handlers per i bottoni.
     */
    private void setupEventHandlers() {
        this.loginButton.addActionListener(e -> handleLogin());
        this.resetButton.addActionListener(e -> handleReset());
        this.backButton.addActionListener(e -> handleBack());
    }
    
    /**
     * Gestisce il click del bottone Login.
     */
    private void handleLogin() {
        String username = this.usernameField.getText();
        String password = new String(this.passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this.mainFrame, 
                "Inserisci username e password!", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("Tentativo di login con username: " + username);
        // TODO: Implementare la logica di autenticazione

        // Esempio: login sempre valido (sostituisci con la tua logica)
        boolean loginValido = true; // Sostituisci con controllo reale

        if (loginValido) {
            this.hide();
            SwingUtilities.invokeLater(() -> {
                RestaurantsPage restaurantsPage = new RestaurantsPage();
                restaurantsPage.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this.mainFrame, 
                "Username o password errati!", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Gestisce il click del bottone Reset.
     * Pulisce i campi di input.
     */
    private void handleReset() {
        this.usernameField.setText("");
        this.passwordField.setText("");
        this.usernameField.requestFocus();
    }
    
    /**
     * Gestisce il click del bottone Back.
     * Torna alla pagina principale.
     */
    private void handleBack() {
        this.hide();
        this.parentPage.show();
    }
    
    /**
     * Mostra la finestra di login.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }
    
    /**
     * Nasconde la finestra di login.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }
}