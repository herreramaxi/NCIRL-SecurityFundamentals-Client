
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Maximiliano Herrera
 */
public class MainJFrame extends javax.swing.JFrame {

    private final String WEBAPI_URL = "https://ncirl-sf-ca.herokuapp.com";
    //private String WEBAPI_URL = "http://localhost:3001";
    private final JDialogLogin _fLogin;
    private final CryptographyTools _cryptoTools;

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() throws Exception {

        initComponents();

        _cryptoTools = new CryptographyTools();
        _fLogin = new JDialogLogin(this, true, WEBAPI_URL, _cryptoTools);
        _fLogin.setLocationRelativeTo(null);
        _fLogin.setVisible(true);

        if (!_fLogin.isAuthenticated()) {
            System.out.println("no authenticated");

            if (_fLogin.isExitByLoginWithErrors()) {
                JOptionPane.showMessageDialog(this, "There was an error when trying to login, check the logs", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (_fLogin.isExitByLogin()) {
                JOptionPane.showMessageDialog(this, "Wrong user credentials, closing application", "Error", JOptionPane.ERROR_MESSAGE);
            }

            System.exit(0);
        }

        getUsers();
        System.out.println("authenticated");
    }

    private Boolean send(String firstName, String lastName, String email, String password, String data) throws Exception {
        String hashedPassword = _cryptoTools.getHash(password);
        String dataCiphered = _cryptoTools.encrypt(data);
        String hash = _cryptoTools.getHash(firstName + lastName + email + hashedPassword + dataCiphered);
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(WEBAPI_URL + "/api/users/addOrUpdate");
            httppost.addHeader("Authorization", getBasicHeader());

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("firstName", firstName));
            params.add(new BasicNameValuePair("lastName", lastName));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", hashedPassword));
            params.add(new BasicNameValuePair("data", dataCiphered));
            params.add(new BasicNameValuePair("hash", hash));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    BufferedReader b = new BufferedReader(new InputStreamReader(instream));
                    String resp = b.readLine();
                    System.out.println(resp);
                }
            }

            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private void getUsers() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(WEBAPI_URL + "/api/users/getAll");
        httpget.addHeader("Authorization", getBasicHeader());

        HttpResponse httpresponse = httpclient.execute(httpget);
        HttpEntity entity = httpresponse.getEntity();
        StringBuilder sb = new StringBuilder();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                BufferedReader b = new BufferedReader(new InputStreamReader(instream));
                sb.append(b.readLine());
            }
        }

        System.out.println(sb.toString());
        ArrayList<User> users = DeserealizeJson(sb.toString());
        updateTableModel(users);
    }

    private Boolean resetDatabase() throws Exception {

        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(WEBAPI_URL + "/api/users/resetUsers");
            httppost.addHeader("Authorization", getBasicHeader());

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    BufferedReader b = new BufferedReader(new InputStreamReader(instream));
                    String resp = b.readLine();
                    System.out.println(resp);
                }
            }

            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private String getBasicHeader() {
        String emailPass = _fLogin.getEmail() + ":" + _fLogin.getPasswordHashed();
        String basicToken = new String(Base64.getEncoder().encode(emailPass.getBytes()));
        return "Basic " + basicToken;
    }

    private ArrayList<User> DeserealizeJson(String jsonData) {
        ArrayList<User> users = new ArrayList<>();
        JSONObject obj = new JSONObject(jsonData);
        JSONArray usersData = obj.getJSONArray("users");
        int n = usersData.length();
        for (int i = 0; i < n; ++i) {
            JSONObject user = usersData.getJSONObject(i);
            User u = new User(user.getString("firstName"),
                    user.getString("lastName"),
                    user.getString("email"),
                    user.getString("password"),
                    user.getString("data"));

            users.add(u);
        }

        return users;
    }

    private void updateTableModel(ArrayList<User> users) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Object[] row = new Object[6];
        model.setRowCount(0);
        for (int i = 0; i < users.size(); i++) {
            row[0] = users.get(i).getFirstName();
            row[1] = users.get(i).getLastname();
            row[2] = users.get(i).getEmail();
            row[3] = users.get(i).getPasword();
            row[4] = users.get(i).getData();
            row[5] = _cryptoTools.decrypt(users.get(i).getData());
            model.addRow(row);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SendButton = new javax.swing.JButton();
        PasswTf = new javax.swing.JPasswordField();
        CryptograpyLb = new javax.swing.JLabel();
        NameLb = new javax.swing.JLabel();
        PasswLb = new javax.swing.JLabel();
        NickNameLb = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        NamePlace = new javax.swing.JLabel();
        nicknamePlace = new javax.swing.JLabel();
        firstName = new javax.swing.JTextField();
        lastName = new javax.swing.JTextField();
        EmailLb = new javax.swing.JLabel();
        EmailTf = new javax.swing.JTextField();
        NickNameLb1 = new javax.swing.JLabel();
        jTextFieldData = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButtonResetDatabase = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        SendButton.setText("Synchronize with server");
        SendButton.setToolTipText("Send data to server and receivean updated list of users");
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        CryptograpyLb.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        CryptograpyLb.setText("Cryptography");

        NameLb.setText("First name:");

        PasswLb.setText("Password:");

        NickNameLb.setText("LastName:");

        EmailLb.setText("Email:");

        NickNameLb1.setText("Data: ");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "First name", "Last name", "Email", "Hashed password", "Data (encrypted)", "Data (decrypted)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jButtonResetDatabase.setText("Reset database");
        jButtonResetDatabase.setToolTipText("Test functionality for restoring database, delete all users except admin");
        jButtonResetDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetDatabaseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(223, 223, 223)
                        .addComponent(CryptograpyLb)
                        .addGap(18, 18, 18)
                        .addComponent(nicknamePlace, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(NickNameLb1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldData, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(NameLb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(firstName, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(EmailLb, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(EmailTf, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(NickNameLb)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(PasswLb, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lastName, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                            .addComponent(PasswTf))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonResetDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(250, 250, 250)
                .addComponent(NamePlace, javax.swing.GroupLayout.DEFAULT_SIZE, 11, Short.MAX_VALUE)
                .addGap(131, 131, 131))
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addComponent(CryptograpyLb)
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(NickNameLb)
                        .addComponent(SendButton))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(NameLb)
                        .addComponent(NamePlace)
                        .addComponent(nicknamePlace)
                        .addComponent(firstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(PasswTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonResetDatabase))
                    .addComponent(EmailLb)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(PasswLb)
                        .addComponent(EmailTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NickNameLb1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
        try {
            Boolean success = send(firstName.getText(),
                    lastName.getText(),
                    EmailTf.getText(),
                    new String(PasswTf.getPassword()),
                    jTextFieldData.getText());

            if (!success) {
                JOptionPane.showMessageDialog(this, "Error when trying to synchronize with the server", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Synchronization successful");
                getUsers();
            }
        } catch (Exception ex) {
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SendButtonActionPerformed

    private void jButtonResetDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetDatabaseActionPerformed
        // TODO add your handling code here:
        int dialogResult = JOptionPane.showConfirmDialog(null, "Please confirm you would like to restore database, delete all users except admin", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            System.out.println("reset db");
            try {
                Boolean resetSuccesful = resetDatabase();

                if (resetSuccesful) {
                    JOptionPane.showMessageDialog(this, "The users database was successfully restored");
                    getUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "There was an error when trying to restore users database", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("no reset db");
        }
    }//GEN-LAST:event_jButtonResetDatabaseActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainJFrame frame = new MainJFrame();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CryptograpyLb;
    private javax.swing.JLabel EmailLb;
    private javax.swing.JTextField EmailTf;
    private javax.swing.JLabel NameLb;
    private javax.swing.JLabel NamePlace;
    private javax.swing.JLabel NickNameLb;
    private javax.swing.JLabel NickNameLb1;
    private javax.swing.JLabel PasswLb;
    private javax.swing.JPasswordField PasswTf;
    private javax.swing.JButton SendButton;
    private javax.swing.JTextField firstName;
    private javax.swing.JButton jButtonResetDatabase;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldData;
    private javax.swing.JTextField lastName;
    private javax.swing.JLabel nicknamePlace;
    // End of variables declaration//GEN-END:variables
}
