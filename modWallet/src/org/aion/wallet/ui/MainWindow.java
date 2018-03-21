package org.aion.wallet.ui;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.aion.api.server.ApiAion;
import org.aion.base.type.Address;
import org.aion.wallet.WalletApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindow extends Application
        implements Initializable
{
    private double xOffset;
    private double yOffset;

    private final ApiAion walletApi = new WalletApi();

    @FXML
    private TextField balanceField;

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

        stage.setTitle("Aion Wallet");
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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final List<String> accounts = walletApi.getAccounts();
        if (!accounts.isEmpty()) {
            final Address address = Address.wrap(accounts.get(0));
            try {
                final BigDecimal balance = new BigDecimal(walletApi.getBalance(address));
                final BigDecimal decimalPlaces = new BigDecimal(BigInteger.valueOf(1000000000).multiply(BigInteger.valueOf(1000000000)));
                balanceField.setText(String.valueOf(balance.divide(decimalPlaces, 10, RoundingMode.HALF_EVEN)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
