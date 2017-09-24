package br.ufsm.piveta.system.forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Login extends JFrame {

    public interface OnLoginFormIsDone {
        void callback(Login loginFrame, Credentials credentials);
        void cancel(Login loginFrame);
    }

    @SuppressWarnings("WeakerAccess")
    public static class Credentials {
        protected final String username;
        protected final String password;

        public Credentials(String username, String password){
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }


    public Login(OnLoginFormIsDone onLoginFormIsDone){

        super("Library System");

        setSize(270, 115);
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth())/2 - getWidth()/2),
                (int) ((screenSize.getHeight())/2 - getHeight()/2)
        );

        JPanel panel = new JPanel();
        add(panel);

        panel.setLayout(null);

        JLabel userLabel = new JLabel("User");
        userLabel.setBounds(10, 10, 80, 25);
        userLabel.setDisplayedMnemonic('u');
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 10, 160, 25);
        userLabel.setLabelFor(userText);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10, 40, 80, 25);
        passwordLabel.setDisplayedMnemonic('p');
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 40, 160, 25);
        passwordLabel.setLabelFor(passwordText);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(75, 80, 90, 25);
        loginButton.addActionListener(e ->
            onLoginFormIsDone.callback(this,
                    new Credentials(userText.getText(),String.valueOf(passwordText.getPassword())))
        );
        loginButton.setMnemonic('l');
        panel.add(loginButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(170, 80, 90, 25);
        cancelButton.addActionListener( e ->
            onLoginFormIsDone.cancel(this)
        );
        cancelButton.setMnemonic('c');
        panel.add(cancelButton);

        Login that = this;

        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                onLoginFormIsDone.cancel(that);
            }
        });

    }



}
