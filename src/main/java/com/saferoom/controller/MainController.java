package com.saferoom.controller;

import com.jfoenix.controls.JFXButton;
import com.saferoom.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    private static MainController instance;

    // FXML Değişkenleri
    @FXML private BorderPane mainPane;
    @FXML private StackPane contentArea;
    @FXML private VBox navBox;
    @FXML private JFXButton dashboardButton;
    @FXML private JFXButton roomsButton;
    @FXML private JFXButton messagesButton;
    @FXML private JFXButton friendsButton;
    @FXML private JFXButton fileVaultButton;
    // @FXML private JFXButton settingsButton; <-- KALDIRILDI
    @FXML private JFXButton notificationsButton;
    @FXML private StackPane profileBox;
    @FXML private Label usernameLabel;
    @FXML private Label userAvatar;
    @FXML private Region statusDot;

    @FXML
    public void initialize() {
        instance = this;
        // Buton olay atamaları
        dashboardButton.setOnAction(event -> handleDashboard());
        roomsButton.setOnAction(event -> handleRooms());
        messagesButton.setOnAction(event -> handleMessages());
        friendsButton.setOnAction(event -> handleFriends());
        fileVaultButton.setOnAction(event -> handleFileVault());
        // settingsButton.setOnAction(event -> handleSettings()); <-- KALDIRILDI
        notificationsButton.setOnAction(event -> System.out.println("Notifications clicked!"));

        setupProfileMenu();

        // Başlangıçta Dashboard'u yükle
        handleDashboard();
    }

    private void setupProfileMenu() {
        ContextMenu userMenu = new ContextMenu();
        userMenu.getStyleClass().add("user-menu");

        MenuItem onlineItem = createStatusMenuItem("Online", "status-online");
        MenuItem idleItem = createStatusMenuItem("Idle", "status-idle");
        MenuItem dndItem = createStatusMenuItem("Do Not Disturb", "status-dnd");
        MenuItem offlineItem = createStatusMenuItem("Offline", "status-offline");

        SeparatorMenuItem separator = new SeparatorMenuItem();
        MenuItem settingsItem = new MenuItem("Settings");

        // Settings menü elemanına tıklama olayı eklendi
        settingsItem.setOnAction(event -> {
            clearActiveButton(); // Sol menüdeki aktif seçimleri temizle
            loadView("SettingsView.fxml"); // Ayarlar panelini yükle
            System.out.println("Ayarlar paneli profil menüsünden açıldı.");
        });

        MenuItem logoutItem = new MenuItem("Logout");

        userMenu.getItems().addAll(onlineItem, idleItem, dndItem, offlineItem, separator, settingsItem, logoutItem);

        profileBox.setOnMouseClicked(event -> {
            userMenu.show(profileBox, Side.BOTTOM, 0, 10);
        });
    }

    private MenuItem createStatusMenuItem(String statusText, String styleClass) {
        MenuItem menuItem = new MenuItem(statusText);
        menuItem.setOnAction(event -> {
            statusDot.getStyleClass().setAll("status-dot", styleClass);
            System.out.println("Status changed to: " + statusText);
        });
        return menuItem;
    }

    public static MainController getInstance() {
        return instance;
    }

    private void handleDashboard() { setActiveButton(dashboardButton); loadView("DashBoardView.fxml"); }
    public void handleRooms() { setActiveButton(roomsButton); loadView("RoomsView.fxml"); }
    private void handleMessages() { setActiveButton(messagesButton); loadView("MessagesView.fxml"); }
    private void handleFriends() { setActiveButton(friendsButton); loadView("FriendsView.fxml"); }
    public void handleFileVault() { setActiveButton(fileVaultButton); loadView("FileVaultView.fxml"); }
    // private void handleSettings() { ... } <-- METOT KALDIRILDI

    public void loadSecureRoomView() {
        loadFullScreenView("SecureRoomView.fxml", true);
    }

    public void loadJoinMeetView() {
        loadFullScreenView("JoinMeetView.fxml", false);
    }

    private void loadFullScreenView(String fxmlFile, boolean isSecureRoom) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/" + fxmlFile));
            Parent root = loader.load();

            if (mainPane != null && mainPane.getScene() != null) {
                Scene scene = mainPane.getScene();
                if (isSecureRoom) {
                    SecureRoomController controller = loader.getController();
                    controller.setReturnScene(scene);
                } else {
                    JoinMeetController controller = loader.getController();
                    controller.setReturnScene(scene);
                }
                scene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorInContentArea(fxmlFile + " yüklenemedi.");
        }
    }

    private void loadView(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(MainApp.class.getResource("view/" + fxmlFile)));
            contentArea.getChildren().setAll(root);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showErrorInContentArea("Görünüm yüklenemedi: " + fxmlFile);
        }
    }

    private void showErrorInContentArea(String message) {
        Label errorLabel = new Label(message + "\nLütfen dosya yolunu ve içeriğini kontrol edin.");
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-alignment: center;");
        errorLabel.setWrapText(true);
        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().setAll(errorBox);
    }

    private void setActiveButton(JFXButton activeButton) {
        clearActiveButton();
        activeButton.getStyleClass().add("active");
    }

    private void clearActiveButton() {
        navBox.getChildren().forEach(node -> node.getStyleClass().remove("active"));
    }
}