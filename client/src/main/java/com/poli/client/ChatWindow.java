package com.poli.client;

import com.poli.common.Config;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class ChatWindow extends JFrame implements ServerConnection.Listener {

    private ServerConnection connection;

    private final JTextField ipField = new JTextField(Config.SERVER_HOST);
    private final JTextField portField = new JTextField(String.valueOf(Config.SERVER_PORT));
    private final JTextField usernameField = new JTextField();
    private final JButton connectButton = new JButton("Conectar");

    /** Se llena con la lista de usuarios conectados. */
    private final DefaultComboBoxModel<String> users = new DefaultComboBoxModel<>();
    private final JComboBox<String> userCombo = new JComboBox<>(users);

    private final DefaultListModel<String> messages = new DefaultListModel<>();
    private final JList<String> messageList = new JList<>(messages);

    private final JTextField messageField = new JTextField();
    private final JButton sendButton = new JButton("Enviar");
    private final JButton disconnectButton = new JButton("Desconectar");

    public ChatWindow() {
        super("Chat Cliente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new BorderLayout());
        top.add(buildConnectionPanel(), BorderLayout.NORTH);
        top.add(buildUserPanel(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(buildMessagesPanel(), BorderLayout.CENTER);
        add(buildInputPanel(), BorderLayout.SOUTH);

        connectButton.addActionListener(e -> connect());
        sendButton.addActionListener(e -> send());
        messageField.addActionListener(e -> send()); // Envio con la tecla Enter.
        disconnectButton.addActionListener(e -> disconnect());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (connection != null) {
                    connection.close(); // Informa al servidor que se desconecta antes de cerrar la ventana.
                }
            }
        });

        setConnected(false);
        setSize(420, 480);
        setLocationRelativeTo(null);
    }

    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos de conexión"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, c, 0, "IP del Servidor:", ipField);
        addRow(panel, c, 1, "Puerto:", portField);
        addRow(panel, c, 2, "Nombre de usuario:", usernameField);

        c.gridx = 1;
        c.gridy = 3;
        panel.add(connectButton, c);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Usuario para conversar"));
        panel.add(userCombo, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildMessagesPanel() {
        messageList.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        return new JScrollPane(messageList);
    }

    private JPanel buildInputPanel() {
        JPanel buttons = new JPanel(new java.awt.GridLayout(1, 2, 5, 0));
        buttons.add(sendButton);
        buttons.add(disconnectButton);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private void connect() {
        String host = ipField.getText().trim();
        String username = usernameField.getText().trim();
        if (host.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la IP del servidor y el nombre de usuario.");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El puerto debe ser un número.");
            return;
        }

        ServerConnection newConnection = new ServerConnection(this);
        try {
            newConnection.connect(host, port, username);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor: " + ex.getMessage());
            return;
        }
        connection = newConnection;
        setConnected(true);
        addMessage("Conectado al servidor como: " + username);
    }

    private void send() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        String recipient = (String) userCombo.getSelectedItem();
        if (recipient == null) {
            JOptionPane.showMessageDialog(this, "Seleccione el usuario con el que quiere conversar.");
            return;
        }
        // El servidor envía el mensaje solo al destinatario.
        connection.sendMessage(recipient, text);
        addMessage("Yo a " + recipient + "--> " + text);
        messageField.setText("");
    }

    private void disconnect() {
        if (connection != null) {
            connection.close();
        }
        endSession("Desconectado del servidor.");
    }

    private void endSession(String reason) {
        connection = null;
        users.removeAllElements();
        addMessage(reason);
        setConnected(false);
    }

    /** El server envía la lista de usuarios conectados (excluyendo al qie esta conectado). */
    @Override
    public void onUsers(List<String> connectedUsers) {
        Object selected = users.getSelectedItem();
        users.removeAllElements();
        connectedUsers.forEach(users::addElement);
        if (selected != null && connectedUsers.contains(selected)) {
            users.setSelectedItem(selected); 
        }
    }

    /** El server envía un mensaje privado dirigido a un usuario especifico. */
    @Override
    public void onMessage(String sender, String text) {
        addMessage(sender + "--> " + text);
    }

    @Override
    public void onError(String error) {
        addMessage("Error: " + error);
    }

    /** Se pierde la conexión con el servidor. */
    @Override
    public void onDisconnected() {
        if (connection != null) {
            endSession("Se perdió la conexión con el servidor.");
        }
    }

    private void addMessage(String message) {
        messages.addElement(message);
        messageList.ensureIndexIsVisible(messages.size() - 1);
    }

    /** Los datos de conexion son editables solo mientras esté desconectado. 
     * Los controles de chat solo mientras esté conectado. */
    private void setConnected(boolean connected) {
        ipField.setEnabled(!connected);
        portField.setEnabled(!connected);
        usernameField.setEnabled(!connected);
        connectButton.setEnabled(!connected);

        userCombo.setEnabled(connected);
        messageList.setEnabled(connected);
        messageField.setEnabled(connected);
        sendButton.setEnabled(connected);
        disconnectButton.setEnabled(connected);

        if (connected) {
            messageField.requestFocusInWindow();
        }
    }
}
