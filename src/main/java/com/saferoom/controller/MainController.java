package com.saferoom.controller;

import com.jfoenix.controls.JFXButton;
import com.saferoom.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    @FXML private Label userAvatar;
    @FXML private Region statusDot;
    
    // Window control buttons
    @FXML private JFXButton minimizeButton;
    @FXML private JFXButton maximizeButton;
    @FXML private JFXButton closeButton;

    // User status types
    public enum UserStatus {
        ONLINE("status-dot-online"),
        IDLE("status-dot-idle"),
        DND("status-dot-dnd"),
        OFFLINE("status-dot-offline");
        
        private final String styleClass;
        
        UserStatus(String styleClass) {
            this.styleClass = styleClass;
        }
        
        public String getStyleClass() {
            return styleClass;
        }
    }
    
    private UserStatus currentStatus = UserStatus.ONLINE;
    private ContextMenu userMenu;
    private List<MenuItem> mainMenuItems = new ArrayList<>();
    private List<MenuItem> statusMenuItems = new ArrayList<>();
    private boolean showingStatusSheet = false;
    private final String currentUserName = "Username"; // Replace when user model is available
    
    // Window drag variables
    private double xOffset = 0;
    private double yOffset = 0;
    
    // Window resize variables
    private boolean isResizing = false;
    private double resizeStartX = 0;
    private double resizeStartY = 0;
    private double resizeStartWidth = 0;
    private double resizeStartHeight = 0;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;
    
    private enum ResizeDirection {
        NONE, N, S, E, W, NE, NW, SE, SW
    }

    @FXML
    public void initialize() {
        instance = this;
        
        // Buton olay atamaları
        dashboardButton.setOnAction(event -> handleDashboard());
        roomsButton.setOnAction(event -> handleRooms());
        messagesButton.setOnAction(event -> handleMessages());
        friendsButton.setOnAction(event -> handleFriends());
        fileVaultButton.setOnAction(event -> handleFileVault());
        notificationsButton.setOnAction(event -> System.out.println("Notifications clicked!"));

        // Window control button events
        minimizeButton.setOnAction(event -> handleMinimize());
        maximizeButton.setOnAction(event -> handleMaximize());
        closeButton.setOnAction(event -> handleClose());

        // Window drag functionality for undecorated window
        setupWindowDrag();

        // Initialize user status
        setUserStatus(UserStatus.ONLINE);
        
        // Initialize user avatar with first letter of username
        if (userAvatar != null) {
            userAvatar.setText("U"); // You can replace this with actual username first letter
        }

        // Build user menu and bind to profile box
        buildUserContextMenu();
        if (profileBox != null) {
            profileBox.setOnMouseClicked(e -> {
                if (userMenu != null) {
                    if (userMenu.isShowing()) {
                        userMenu.hide();
                    } else {
                        // Position menu to the right side of the profile, inside the app
                        double offsetX = 8; // Small gap from profile
                        double offsetY = -200; // Move up so menu doesn't go below window
                        
                        userMenu.show(profileBox, Side.RIGHT, offsetX, offsetY);
                    }
                }
            });
        }

        // Başlangıçta Dashboard'u yükle
        handleDashboard();
    }

    public void setUserStatus(UserStatus status) {
        if (statusDot != null) {
            // Remove all status classes
            statusDot.getStyleClass().removeAll(
                UserStatus.ONLINE.getStyleClass(),
                UserStatus.IDLE.getStyleClass(),
                UserStatus.DND.getStyleClass(),
                UserStatus.OFFLINE.getStyleClass()
            );
            // Add the new status class
            statusDot.getStyleClass().add(status.getStyleClass());
            currentStatus = status;
            // Refresh user menu selection visuals if open
            boolean reopenUser = userMenu != null && userMenu.isShowing();
            if (userMenu != null) userMenu.hide();
            buildUserContextMenu();
            if (reopenUser && profileBox != null) {
                // Use the same positioning logic as the main menu
                double offsetX = 8; // Small gap from profile
                double offsetY = -200; // Move up so menu doesn't go below window
                userMenu.show(profileBox, Side.RIGHT, offsetX, offsetY);
                // restore sheet view
                userMenu.getItems().setAll(showingStatusSheet ? statusMenuItems : mainMenuItems);
            }
        }
    }
    
    public UserStatus getUserStatus() {
        return currentStatus;
    }

    private void buildUserContextMenu() {
        userMenu = new ContextMenu();
        userMenu.getStyleClass().add("user-menu");
        mainMenuItems.clear();
        statusMenuItems.clear();
        showingStatusSheet = false;

        // Header with avatar and name
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label("U");
        avatar.getStyleClass().add("user-avatar-header");
        Label nameLabel = new Label(currentUserName);
        nameLabel.getStyleClass().add("user-name");
        header.getChildren().addAll(avatar, nameLabel);
        CustomMenuItem headerItem = new CustomMenuItem(header, false);

        // Status main row that opens a popup selection
        HBox statusMainRow = new HBox(10);
        statusMainRow.setAlignment(Pos.CENTER_LEFT);
        statusMainRow.getStyleClass().add("user-menu-item");
        Pane currentDot = new Pane();
        currentDot.getStyleClass().addAll("status-dot-menu", currentStatus.getStyleClass());
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("user-menu-text");
        Pane grow1 = new Pane();
        HBox.setHgrow(grow1, javafx.scene.layout.Priority.ALWAYS);
        FontIcon arrow = new FontIcon("fas-chevron-right");
        arrow.getStyleClass().add("user-menu-arrow");
        statusMainRow.getChildren().addAll(currentDot, statusLabel, grow1, arrow);
        CustomMenuItem statusOpenItem = new CustomMenuItem(statusMainRow, false);
        statusMainRow.setOnMouseClicked(e -> {
            userMenu.getItems().setAll(statusMenuItems);
            showingStatusSheet = true;
        });

        // Other actions
        CustomMenuItem settingsItem = actionRow("Settings", "fas-cog", this::handleSettings);
        CustomMenuItem helpItem = actionRow("Help", "far-question-circle", () -> new Alert(AlertType.INFORMATION, "Help is coming soon.").showAndWait());

        mainMenuItems.add(headerItem);
        mainMenuItems.add(new SeparatorMenuItem());
        mainMenuItems.add(statusOpenItem);
        mainMenuItems.add(new SeparatorMenuItem());
        mainMenuItems.add(settingsItem);
        mainMenuItems.add(helpItem);
        userMenu.getItems().setAll(mainMenuItems);

        // Build status sheet (with Back)
        CustomMenuItem backItem = actionRow("Back", "fas-chevron-left", () -> {
            userMenu.getItems().setAll(mainMenuItems);
            showingStatusSheet = false;
        });
        statusMenuItems.add(backItem);
        statusMenuItems.add(new SeparatorMenuItem());
        statusMenuItems.add(statusRow("Online", UserStatus.ONLINE));
        statusMenuItems.add(statusRow("Idle", UserStatus.IDLE));
        statusMenuItems.add(statusRow("Do Not Disturb", UserStatus.DND));
        statusMenuItems.add(statusRow("Offline", UserStatus.OFFLINE));
    }

    private CustomMenuItem actionRow(String text, String iconLiteral, Runnable action) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("user-menu-item");
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("user-menu-icon");
        Label label = new Label(text);
        label.getStyleClass().add("user-menu-text");
        row.getChildren().addAll(icon, label);
        row.setOnMouseClicked(e -> {
            // Auto-close menu when clicking menu items
            if (userMenu != null && userMenu.isShowing()) {
                userMenu.hide();
            }
            action.run();
        });
        return new CustomMenuItem(row, false);
    }

    private CustomMenuItem statusRow(String text, UserStatus status) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("user-menu-item");
        Pane dot = new Pane();
        dot.getStyleClass().addAll("status-dot-menu", status.getStyleClass());
        Label label = new Label(text);
        label.getStyleClass().add("user-menu-text");
        FontIcon check = new FontIcon("fas-check");
        check.getStyleClass().add("check-icon");
        check.setVisible(currentStatus == status);
        row.getChildren().addAll(dot, label, new Pane());
        // keep check mark aligned right
        Pane grow = new Pane();
        HBox.setHgrow(grow, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().set(2, grow);
        row.getChildren().add(check);
        row.setOnMouseClicked(e -> setUserStatus(status));
        return new CustomMenuItem(row, false);
    }

    public void handleSettings() { 
        clearActiveButton(); 
        loadView("SettingsView.fxml"); 
    }

    // Window drag functionality for undecorated window
    private void setupWindowDrag() {
        final int RESIZE_BORDER = 5; // Size of resize border in pixels

        mainPane.setOnMousePressed(event -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            
            // Check if we're near the edges for resizing
            resizeDirection = getResizeDirection(event.getSceneX(), event.getSceneY(), 
                                               mainPane.getWidth(), mainPane.getHeight(), RESIZE_BORDER);
            
            if (resizeDirection != ResizeDirection.NONE) {
                isResizing = true;
                resizeStartX = stage.getX();
                resizeStartY = stage.getY();
                resizeStartWidth = stage.getWidth();
                resizeStartHeight = stage.getHeight();
            } else {
                isResizing = false;
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        mainPane.setOnMouseDragged(event -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            
            if (isResizing) {
                handleResize(stage, event.getScreenX(), event.getScreenY());
            } else {
                // Normal window dragging
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        mainPane.setOnMouseMoved(event -> {
            // Update cursor based on position
            Cursor cursor = getCursorForPosition(event.getSceneX(), event.getSceneY(), 
                                               mainPane.getWidth(), mainPane.getHeight(), RESIZE_BORDER);
            mainPane.setCursor(cursor);
        });
        
        mainPane.setOnMouseReleased(event -> {
            isResizing = false;
            resizeDirection = ResizeDirection.NONE;
        });
    }

    private ResizeDirection getResizeDirection(double x, double y, double width, double height, int border) {
        boolean isNorth = y <= border;
        boolean isSouth = y >= height - border;
        boolean isEast = x >= width - border;
        boolean isWest = x <= border;
        
        if (isNorth && isWest) return ResizeDirection.NW;
        if (isNorth && isEast) return ResizeDirection.NE;
        if (isSouth && isWest) return ResizeDirection.SW;
        if (isSouth && isEast) return ResizeDirection.SE;
        if (isNorth) return ResizeDirection.N;
        if (isSouth) return ResizeDirection.S;
        if (isEast) return ResizeDirection.E;
        if (isWest) return ResizeDirection.W;
        
        return ResizeDirection.NONE;
    }
    
    private Cursor getCursorForPosition(double x, double y, double width, double height, int border) {
        ResizeDirection direction = getResizeDirection(x, y, width, height, border);
        
        switch (direction) {
            case N:
            case S:
                return Cursor.N_RESIZE;
            case E:
            case W:
                return Cursor.E_RESIZE;
            case NE:
            case SW:
                return Cursor.NE_RESIZE;
            case NW:
            case SE:
                return Cursor.NW_RESIZE;
            default:
                return Cursor.DEFAULT;
        }
    }
    
    private void handleResize(Stage stage, double mouseX, double mouseY) {
        double deltaX = mouseX - (resizeStartX + resizeStartWidth);
        double deltaY = mouseY - (resizeStartY + resizeStartHeight);
        
        double newX = resizeStartX;
        double newY = resizeStartY;
        double newWidth = resizeStartWidth;
        double newHeight = resizeStartHeight;
        
        // Minimum window size
        final double MIN_WIDTH = 800;
        final double MIN_HEIGHT = 600;
        
        switch (resizeDirection) {
            case N:
                newY = mouseY;
                newHeight = resizeStartHeight + (resizeStartY - mouseY);
                break;
            case S:
                newHeight = resizeStartHeight + deltaY;
                break;
            case E:
                newWidth = resizeStartWidth + deltaX;
                break;
            case W:
                newX = mouseX;
                newWidth = resizeStartWidth + (resizeStartX - mouseX);
                break;
            case NE:
                newY = mouseY;
                newHeight = resizeStartHeight + (resizeStartY - mouseY);
                newWidth = resizeStartWidth + deltaX;
                break;
            case NW:
                newX = mouseX;
                newY = mouseY;
                newWidth = resizeStartWidth + (resizeStartX - mouseX);
                newHeight = resizeStartHeight + (resizeStartY - mouseY);
                break;
            case SE:
                newWidth = resizeStartWidth + deltaX;
                newHeight = resizeStartHeight + deltaY;
                break;
            case SW:
                newX = mouseX;
                newWidth = resizeStartWidth + (resizeStartX - mouseX);
                newHeight = resizeStartHeight + deltaY;
                break;
            case NONE:
            default:
                return; // No resizing needed
        }
        
        // Enforce minimum size constraints
        if (newWidth < MIN_WIDTH) {
            if (resizeDirection == ResizeDirection.W || resizeDirection == ResizeDirection.NW || resizeDirection == ResizeDirection.SW) {
                newX = newX - (MIN_WIDTH - newWidth);
            }
            newWidth = MIN_WIDTH;
        }
        
        if (newHeight < MIN_HEIGHT) {
            if (resizeDirection == ResizeDirection.N || resizeDirection == ResizeDirection.NE || resizeDirection == ResizeDirection.NW) {
                newY = newY - (MIN_HEIGHT - newHeight);
            }
            newHeight = MIN_HEIGHT;
        }
        
        // Apply the new size and position
        stage.setX(newX);
        stage.setY(newY);
        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
    }

    // Window control methods
    private void handleMinimize() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.setIconified(true);
    }

    private void handleMaximize() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            // Change icon to maximize
            if (maximizeButton != null && maximizeButton.getGraphic() instanceof FontIcon) {
                ((FontIcon) maximizeButton.getGraphic()).setIconLiteral("far-square");
            }
        } else {
            stage.setMaximized(true);
            // Change icon to restore down
            if (maximizeButton != null && maximizeButton.getGraphic() instanceof FontIcon) {
                ((FontIcon) maximizeButton.getGraphic()).setIconLiteral("far-clone");
            }
        }
    }

    private void handleClose() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
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

    public void loadServerView(String serverName, String serverIcon) {
        setActiveButton(roomsButton);
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/ServerView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set server info
            ServerController controller = loader.getController();
            controller.enterServer(serverName, serverIcon);
            controller.setMainController(this); // Pass reference to maintain window controls
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorInContentArea("ServerView.fxml yüklenemedi.");
        }
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