package org.aion.wallet.ui.components;


import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import org.aion.api.server.ApiAion;
import org.aion.wallet.WalletApi;
import org.aion.wallet.ui.events.EventBusFactory;
import org.aion.wallet.ui.events.HeaderPaneButtonEvent;
import org.aion.wallet.util.BalanceFormatter;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Cristian Ilca, Centrys Inc.
 * The middle part of the wallet - will hold all the views and be responsible for changing the views when clicking
 * on the button in the header
 */
public class ContentPane implements Initializable{

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registerEventBusConsumer();
        ObservableList<String> accountListViewItems = accountListView.getItems();

        List<String> accounts = aionWallet.getAccounts();
        for(String account : accounts) {
            accountListViewItems.add(account + " - " + BalanceFormatter.formatBalance(getAccountBalance(account)));
        }
    }

    private void registerEventBusConsumer() {
        EventBusFactory eventBusFactory = EventBusFactory.getInstance();
        eventBusFactory.getBus(HeaderPaneButtonEvent.ID).register(this);
    }

    @Subscribe
    private void handleHeaderPaneButtonEvent(HeaderPaneButtonEvent event) {

    }

    //TODO: the rest of the file has to be moved into it's own component; leaving it for now for functionality

    @FXML
    ListView<String> accountListView;

    ApiAion aionWallet = new WalletApi();
    private BigInteger getAccountBalance(String account) {
        BigInteger balance = null;
        try {
            balance = aionWallet.getBalance(account);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(balance != null) {
            return balance;
        }
        return BigInteger.ZERO;
    }

    public void openAddAccountDialog(MouseEvent mouseEvent) {
        final Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);

        Node eventSource = (Node) mouseEvent.getSource();
        popup.setX(eventSource.getScene().getWindow().getX() + eventSource.getScene().getWidth() / 4);
        popup.setY(eventSource.getScene().getWindow().getY() + eventSource.getScene().getHeight() / 4);

        Pane addAccountDialog = null;
        try {
            addAccountDialog = FXMLLoader.load(getClass().getResource("partials/AddAccountDialog.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        popup.getContent().addAll(addAccountDialog);
        popup.show(eventSource.getScene().getWindow());
    }
}
