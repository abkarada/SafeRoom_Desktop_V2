package com.saferoom.controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform; // Kapatma işlevi için import edildi
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class LoginController {

    // --- FXML Değişkenleri ---
    @FXML private VBox rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMe;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button signInButton;
    @FXML private JFXButton googleLoginButton;
    @FXML private JFXButton githubLoginButton;
    @FXML private Hyperlink signUpLink;
    @FXML private Button closeButton; // YENİ: Kapatma butonu eklendi

    // Sürükleme için ofset değişkenleri
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        signInButton.setOnAction(event -> handleSignIn());
        signUpLink.setOnAction(event -> handleSignUp());
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
        googleLoginButton.setOnAction(event -> handleGoogleLogin());
        githubLoginButton.setOnAction(event -> handleGitHubLogin());
        passwordField.setOnAction(event -> handleSignIn());
        closeButton.setOnAction(event -> handleClose()); // YENİ: Kapatma butonu olayı eklendi
    }

    private void handleSignIn() {
        // ... (Bu metodun içeriği doğru, değişiklik gerekmiyor) ...
        System.out.println("Giriş başarılı, ana pencere açılıyor...");
        try {
            Stage loginStage = (Stage) rootPane.getScene().getWindow();
            loginStage.close();
            Stage mainStage = new Stage();
            mainStage.initStyle(StageStyle.UNDECORATED);
            mainStage.setTitle("SafeRoom");
            Parent mainRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/saferoom/view/MainView.fxml")));
            Scene mainScene = new Scene(mainRoot, 1280, 800);
            String cssPath = "/com/saferoom/styles/styles.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                mainScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            mainStage.setScene(mainScene);
            mainStage.setResizable(true);
            mainStage.setMinWidth(1024);
            mainStage.setMinHeight(768);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ana sayfa yüklenemedi.");
        }
    }

    private void handleSignUp() {
        System.out.println("Kayıt ekranına geçiş yapılıyor...");
        try {
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.close();

            Stage registerStage = new Stage();
            registerStage.initStyle(StageStyle.UNDECORATED);
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/saferoom/view/RegisterView.fxml")));

            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                registerStage.setX(event.getScreenX() - xOffset);
                registerStage.setY(event.getScreenY() - yOffset);
            });

            Scene scene = new Scene(root);
            String cssPath = "/com/saferoom/styles/styles.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            registerStage.setScene(scene);
            registerStage.setResizable(false);
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * YENİ: Uygulamayı tamamen kapatır.
     */
    private void handleClose() {
        System.out.println("Uygulama kapatılıyor...");
        Platform.exit();
    }

    // ... (Diğer metodlar değişmedi) ...
    private void handleForgotPassword() { showAlert("Şifre Kurtarma", "Bu özellik yakında eklenecektir."); }
    private void handleGoogleLogin() { showAlert("Google ile Giriş", "Bu özellik yakında eklenecektir."); }
    private void handleGitHubLogin() { showAlert("GitHub ile Giriş", "Bu özellik yakında eklenecektir."); }
    private void showAlert(String title, String content) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
    private void showError(String message) { Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle("Hata"); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait(); }
}
