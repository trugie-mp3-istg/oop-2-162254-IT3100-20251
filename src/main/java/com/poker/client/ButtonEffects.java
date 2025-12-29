package com.poker.client;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ButtonEffects {

    /**
     * Thêm hiệu ứng bấm cho nút (click effect)
     * Nút sẽ co lại khi bấm và trở về bình thường
     */
    public static void addClickEffect(Button button) {
        button.setOnMousePressed(event -> {
            // Phát âm thanh bấm nút
            AudioManager.playButtonClickSound();
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(0.97);
            scale.setToY(0.97);
            scale.play();
        });

        button.setOnMouseReleased(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    /**
     * Thêm hiệu ứng hover cho nút
     * Nút sẽ phóng to một chút khi di chuột vào
     */
    public static void addHoverEffect(Button button) {
        button.setOnMouseEntered(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            // Thêm shadow khi hover
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.BLACK);
            shadow.setRadius(15);
            shadow.setSpread(0.3);
            button.setEffect(shadow);
        });

        button.setOnMouseExited(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            // Xóa shadow khi rời chuột
            button.setEffect(null);
        });
    }

    /**
     * Thêm cả click effect và hover effect
     */
    public static void addAllEffects(Button button) {
        addClickEffect(button);
        addHoverEffect(button);
    }

    /**
     * Thêm hiệu ứng pulse (nhấp nháy)
     * Dùng cho các nút quan trọng
     */
    public static void addPulseEffect(Button button) {
        ScaleTransition pulse1 = new ScaleTransition(Duration.millis(400), button);
        pulse1.setToX(1.05);
        pulse1.setToY(1.05);

        ScaleTransition pulse2 = new ScaleTransition(Duration.millis(400), button);
        pulse2.setToX(1.0);
        pulse2.setToY(1.0);

        pulse1.setOnFinished(event -> pulse2.play());
        pulse2.setOnFinished(event -> pulse1.play());

        pulse1.play();
    }

    /**
     * Thêm hiệu ứng bounce (nảy)
     * Nút sẽ nảy lên khi bấm
     */
    public static void addBounceEffect(Button button) {
        button.setOnMousePressed(event -> {
            TranslateTransition translate = new TranslateTransition(Duration.millis(150), button);
            translate.setByY(3);
            translate.play();

            TranslateTransition translateBack = new TranslateTransition(Duration.millis(150), button);
            translateBack.setByY(-3);
            translateBack.setDelay(Duration.millis(150));
            translateBack.play();
        });
    }
}
