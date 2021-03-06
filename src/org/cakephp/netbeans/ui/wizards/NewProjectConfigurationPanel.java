/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
/*
 * NewProjectConfigurationPanel.java
 *
 * Created on 2011/09/27
 */
package org.cakephp.netbeans.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.cakephp.netbeans.options.CakePhpOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author junichi11
 */
public class NewProjectConfigurationPanel extends javax.swing.JPanel {

    private static final String GITHUB_API_REPOS_TAGS = "https://api.github.com/repos/cakephp/cakephp/tags"; // NOI18N
    private static final long serialVersionUID = 7874450246517944114L;
    private Map<String, String> tagsMap = new HashMap<String, String>();
    private String errorMessage;
    private boolean isNetworkError = false;
    private static final Logger LOGGER = Logger.getLogger(NewProjectConfigurationPanel.class.getName());

    /**
     * Creates new form NewProjectConfigurationPanel
     */
    @NbBundle.Messages({
        "LBL_ConnectErrorMessage=Is not connected to the network.",
        "LBL_NewProjectWizardErrorMessage=Please, connect to the network or set CakePHP local file option"
    })
    public NewProjectConfigurationPanel() {
        initComponents();
        unzipRadioButton.setSelected(true);
        unzipFileNameTextField.setText(""); // NOI18N
        setLocalPathLabel();

        try {
            // Get JSON
            URL tagsJson = new URL(GITHUB_API_REPOS_TAGS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(tagsJson.openStream(), "UTF-8")); // NOI18N
            StringBuilder contents = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                contents.append(str);
            }

            JSONArray json = new JSONArray(contents.toString());
            String[] tagsArray = new String[json.length()];
            for (int i = 0; i < json.length(); i++) {
                JSONObject jObject = (JSONObject) json.get(i);
                tagsArray[i] = jObject.getString("name"); // NOI18N
                tagsMap.put(jObject.getString("name"), jObject.getString("zipball_url")); // NOI18N
            }
            Arrays.sort(tagsArray, new ComparatorImpl());
            versionList.setListData(tagsArray);
            versionList.setSelectedIndex(0);
        } catch (JSONException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            isNetworkError = true;
            LOGGER.log(Level.WARNING, Bundle.LBL_ConnectErrorMessage());
        }
    }

    public Map<String, String> getTagsMap() {
        return tagsMap;
    }

    public JList getVersionList() {
        return versionList;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public JRadioButton getUnzipRadioButton() {
        return unzipRadioButton;
    }

    public void setGitCommandLabel(String command) {
        gitCommandLabel.setText(command);
    }

    public JCheckBox getDatabaseCheckBox() {
        return databaseCheckBox;
    }

    public JTextField getDatabaseTextField() {
        return databaseTextField;
    }

    public JTextField getDatasourceTextField() {
        return datasourceTextField;
    }

    public JTextField getEncodingTextField() {
        return encodingTextField;
    }

    public JTextField getHostTextField() {
        return hostTextField;
    }

    public JTextField getUnzipFileNameTextField() {
        return unzipFileNameTextField;
    }

    public JTextField getLoginTextField() {
        return loginTextField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JCheckBox getPersistentCheckBox() {
        return persistentCheckBox;
    }

    public JTextField getPrefixTextField() {
        return prefixTextField;
    }

    public boolean useLocalFile() {
        return unzipLocalFileRadioButton.isSelected();
    }

    /**
     * Set local path label.
     */
    private void setLocalPathLabel() {
        // get Option
        CakePhpOptions options = CakePhpOptions.getInstance();

        // set local path
        String localFilePath = options.getLocalZipFilePath();
        if (StringUtils.isEmpty(localFilePath)) {
            localPathLabel.setText("");
            localPathLabel.setEnabled(false);
        } else {
            File file = new File(localFilePath);
            localPathLabel.setText(file.getName());
        }
    }

    private boolean isEnabledGit() {
        try {
            Process process = Runtime.getRuntime().exec("git"); // NOI18N
            process.waitFor();
        } catch (InterruptedException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    private boolean isEnabledLocalPath() {
        String localZipFilePath = CakePhpOptions.getInstance().getLocalZipFilePath();
        return !StringUtils.isEmpty(localZipFilePath);
    }

    public boolean isNetworkError() {
        return isNetworkError;
    }

    public void setError() {
        if (isNetworkError) {
            if (!isEnabledLocalPath()) {
                errorMessage = Bundle.LBL_NewProjectWizardErrorMessage();
            } else {
                errorMessage = null;
                unzipRadioButton.setEnabled(false);
                gitCloneRadioButton.setEnabled(false);
                unzipLocalFileRadioButton.setEnabled(true);
            }
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

        buttonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        versionList = new javax.swing.JList();
        versionLabel = new javax.swing.JLabel();
        unzipRadioButton = new javax.swing.JRadioButton();
        gitCloneRadioButton = new javax.swing.JRadioButton();
        gitCommandLabel = new javax.swing.JLabel();
        databaseCheckBox = new javax.swing.JCheckBox();
        datasourceLabel = new javax.swing.JLabel();
        datasourceTextField = new javax.swing.JTextField();
        persistentCheckBox = new javax.swing.JCheckBox();
        hostLabel = new javax.swing.JLabel();
        loginLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        databaseLabel = new javax.swing.JLabel();
        prefixLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        loginTextField = new javax.swing.JTextField();
        databaseTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        prefixTextField = new javax.swing.JTextField();
        encodingTextField = new javax.swing.JTextField();
        unzipFileNameTextField = new javax.swing.JTextField();
        unzipLocalFileRadioButton = new javax.swing.JRadioButton();
        localPathLabel = new javax.swing.JLabel();

        setAutoscrolls(true);

        versionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        versionList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(versionList);

        versionLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.versionLabel.text")); // NOI18N

        buttonGroup.add(unzipRadioButton);
        unzipRadioButton.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.unzipRadioButton.text")); // NOI18N

        buttonGroup.add(gitCloneRadioButton);
        gitCloneRadioButton.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.gitCloneRadioButton.text")); // NOI18N
        gitCloneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gitCloneRadioButtonActionPerformed(evt);
            }
        });

        gitCommandLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.gitCommandLabel.text")); // NOI18N

        databaseCheckBox.setSelected(true);
        databaseCheckBox.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.databaseCheckBox.text")); // NOI18N

        datasourceLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.datasourceLabel.text")); // NOI18N

        datasourceTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.datasourceTextField.text")); // NOI18N

        persistentCheckBox.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.persistentCheckBox.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.hostLabel.text")); // NOI18N

        loginLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.loginLabel.text")); // NOI18N

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.passwordLabel.text")); // NOI18N

        databaseLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.databaseLabel.text")); // NOI18N

        prefixLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.prefixLabel.text")); // NOI18N

        encodingLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.encodingLabel.text")); // NOI18N

        hostTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.hostTextField.text")); // NOI18N

        loginTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.loginTextField.text")); // NOI18N

        databaseTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.databaseTextField.text")); // NOI18N

        passwordField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.passwordField.text")); // NOI18N

        prefixTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.prefixTextField.text")); // NOI18N

        encodingTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.encodingTextField.text")); // NOI18N

        unzipFileNameTextField.setEditable(false);
        unzipFileNameTextField.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.unzipFileNameTextField.text")); // NOI18N
        unzipFileNameTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        buttonGroup.add(unzipLocalFileRadioButton);
        unzipLocalFileRadioButton.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.unzipLocalFileRadioButton.text")); // NOI18N
        unzipLocalFileRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unzipLocalFileRadioButtonActionPerformed(evt);
            }
        });

        localPathLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.localPathLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(unzipLocalFileRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(localPathLabel))
                    .addComponent(gitCloneRadioButton)
                    .addComponent(gitCommandLabel)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(versionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(unzipRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(unzipFileNameTextField))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(datasourceLabel)
                            .addComponent(passwordLabel)
                            .addComponent(databaseLabel)
                            .addComponent(encodingLabel)
                            .addComponent(prefixLabel)
                            .addComponent(hostLabel)
                            .addComponent(persistentCheckBox)
                            .addComponent(loginLabel))
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loginTextField)
                            .addComponent(prefixTextField)
                            .addComponent(hostTextField)
                            .addComponent(datasourceTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(passwordField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(databaseTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(encodingTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(15, 15, 15))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(databaseCheckBox)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(datasourceLabel)
                            .addComponent(datasourceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(persistentCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hostLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loginLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(unzipRadioButton)
                            .addComponent(unzipFileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(databaseCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(versionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(unzipLocalFileRadioButton)
                            .addComponent(localPathLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gitCloneRadioButton)
                            .addComponent(passwordLabel))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(databaseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(databaseLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(prefixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(prefixLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gitCommandLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encodingLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void gitCloneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gitCloneRadioButtonActionPerformed
        try {
            Process process = Runtime.getRuntime().exec("git"); // NOI18N
            process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            gitCloneRadioButton.setSelected(false);
            unzipRadioButton.setSelected(true);
            gitCloneRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_gitCloneRadioButtonActionPerformed

    private void unzipLocalFileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unzipLocalFileRadioButtonActionPerformed
        // TODO add your handling code here:
        if (localPathLabel.getText().isEmpty()) {
            unzipLocalFileRadioButton.setSelected(false);
            unzipRadioButton.setSelected(true);
            unzipLocalFileRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_unzipLocalFileRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JCheckBox databaseCheckBox;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JTextField databaseTextField;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JTextField datasourceTextField;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JTextField encodingTextField;
    private javax.swing.JRadioButton gitCloneRadioButton;
    private javax.swing.JLabel gitCommandLabel;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel localPathLabel;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JTextField loginTextField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox persistentCheckBox;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JTextField prefixTextField;
    private javax.swing.JTextField unzipFileNameTextField;
    private javax.swing.JRadioButton unzipLocalFileRadioButton;
    private javax.swing.JRadioButton unzipRadioButton;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JList versionList;
    // End of variables declaration//GEN-END:variables

    //~ inner class
    private static class ComparatorImpl implements Comparator<String> {

        public ComparatorImpl() {
        }
        private static final String NUMBER_REGEX = "[0-9]+"; // NOI18N
        private static final String SPLIT_REGEX = "[., -]"; // NOI18N

        @Override
        public int compare(String a, String b) {
            String[] aArray = a.split(SPLIT_REGEX);
            String[] bArray = b.split(SPLIT_REGEX);
            int aLength = aArray.length;
            int bLength = bArray.length;
            for (int i = 0; i < aLength; i++) {
                if (i == aLength - 1) {
                    if ((bLength - aLength) < 0) {
                        return -1;
                    }
                }
                String aString = aArray[i];
                String bString = bArray[i];
                if (aString.matches(NUMBER_REGEX) && bString.matches(NUMBER_REGEX)) {
                    try {
                        Integer aInt = Integer.parseInt(aString);
                        Integer bInt = Integer.parseInt(bString);
                        if (aInt == bInt) {
                            continue;
                        } else {
                            return bInt - aInt;
                        }
                    } catch (NumberFormatException ex) {
                        return 1;
                    }
                } else {
                    return b.compareTo(a);
                }
            }
            return 1;
        }
    }
}
