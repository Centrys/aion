package org.aion.wallet.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.aion.api.server.ApiAion;
import org.aion.base.type.Address;
import org.aion.wallet.WalletApi;

import java.io.IOException;
import java.util.List;

public class MainWindow extends Application {
    private double xOffset;
    private double yOffset;

    private final ApiAion walletApi = new WalletApi();

    @Override
    public void start(final Stage stage) throws IOException {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("aion_logo.png")));

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(event -> handleMouseDragged(stage, event));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        stage.setOnCloseRequest(t -> actionOnClose());

        final List<String> accounts = walletApi.getAccounts();
        if (!accounts.isEmpty()) {
            final Address address = Address.wrap(accounts.get(0));
            try {
                stage.setTitle(String.valueOf(walletApi.getBalance(address)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            stage.setTitle("Aion Wallet Title");
        }
        stage.setScene(scene);
        stage.show();
    }

    private void handleMousePressed(final MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    private void handleMouseDragged(final Stage stage, final MouseEvent event) {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    public void shutDown(){
        actionOnClose();
    }

    private void actionOnClose() {
        System.exit(0);
    }
}
