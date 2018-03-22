package org.aion.wallet.ui.components.partials;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.aion.mcf.account.Keystore;

public class AddAccountDialog {

    @FXML
    private TextField newPassword;

    @FXML
    private TextField retypedPassword;

    public void createAccount() {
        if (newPassword.getText() != null && retypedPassword.getText() != null && newPassword.getText().equals(retypedPassword.getText())) {
            Keystore.create(newPassword.getText());
        }
    }

    public void unlockAccount() {

    }
}
