package org.aion.wallet.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.aion.api.server.ApiAion;
import org.aion.base.type.Address;
import org.aion.wallet.WalletApi;

import java.io.IOException;
import java.util.List;

public class MainWindow extends Application {

    private final ApiAion walletApi = new WalletApi();

    @Override
    public void start(final Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        Scene scene = new Scene(root, 500, 350);

        primaryStage.setOnCloseRequest(t -> ActionOnClose());

        final List<String> accounts = walletApi.getAccounts();
        if (!accounts.isEmpty()) {
            final String account = accounts.get(0);
            try {
                primaryStage.setTitle(String.valueOf(walletApi.getBalance(Address.wrap(account))));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            primaryStage.setTitle("Aion Wallet Title");
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void ActionOnClose() {
        Platform.exit();
        System.exit(0);
    }
}
