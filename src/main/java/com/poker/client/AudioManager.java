package com.poker.client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class AudioManager {
    
    private static MediaPlayer buttonClickPlayer;
    private static MediaPlayer chipSoundPlayer;
    private static MediaPlayer roundEndPlayer;
    private static MediaPlayer backgroundMusicPlayer;
    private static MediaPlayer logoutSoundPlayer;
    
    /**
     * Phát âm thanh khi bấm nút
     */
    public static void playButtonClickSound() {
        try {
            URL soundURL = AudioManager.class.getResource("/audio/button_click.mp3");
            if (soundURL == null) {
                System.out.println("Warning: button_click.mp3 not found");
                return;
            }
            
            Media sound = new Media(soundURL.toString());
            buttonClickPlayer = new MediaPlayer(sound);
            buttonClickPlayer.setVolume(0.3);
            buttonClickPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing button sound: " + e.getMessage());
        }
    }
    
    /**
     * Phát âm thanh khi đặt chip/cược
     */
    public static void playChipSound() {
        try {
            URL soundURL = AudioManager.class.getResource("/audio/chip.mp3");
            if (soundURL == null) {
                System.out.println("Warning: chip.mp3 not found");
                return;
            }
            
            Media sound = new Media(soundURL.toString());
            chipSoundPlayer = new MediaPlayer(sound);
            chipSoundPlayer.setVolume(0.4);
            chipSoundPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing chip sound: " + e.getMessage());
        }
    }
    
    /**
     * Phát âm thanh khi kết thúc ván
     */
    public static void playRoundEndSound() {
        try {
            URL soundURL = AudioManager.class.getResource("/audio/round_end.mp3");
            if (soundURL == null) {
                System.out.println("Warning: round_end.mp3 not found");
                return;
            }
            
            Media sound = new Media(soundURL.toString());
            roundEndPlayer = new MediaPlayer(sound);
            roundEndPlayer.setVolume(0.5);
            roundEndPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing round end sound: " + e.getMessage());
        }
    }
    
    /**
     * Phát âm thanh khi logout
     */
    public static void playLogoutSound() {
        try {
            URL soundURL = AudioManager.class.getResource("/audio/button_click.mp3");
            if (soundURL == null) {
                System.out.println("Warning: button_click.mp3 not found");
                return;
            }
            
            Media sound = new Media(soundURL.toString());
            logoutSoundPlayer = new MediaPlayer(sound);
            logoutSoundPlayer.setVolume(0.3);
            logoutSoundPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing logout sound: " + e.getMessage());
        }
    }
    
    /**
     * Phát nhạc nền (loop) trên màn hình login
     */
    public static void playLoginBackgroundMusic() {
        try {
            URL musicURL = AudioManager.class.getResource("/audio/login_background.mp3");
            if (musicURL == null) {
                System.out.println("Warning: login_background.mp3 not found");
                return;
            }
            
            Media music = new Media(musicURL.toString());
            backgroundMusicPlayer = new MediaPlayer(music);
            backgroundMusicPlayer.setVolume(0.2);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }
    
    /**
     * Dừng nhạc nền
     */
    public static void stopLoginBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer = null;
        }
    }
    
    /**
     * Thay đổi âm lượng nút bấm (0.0 - 1.0)
     */
    public static void setButtonSoundVolume(double volume) {
        if (buttonClickPlayer != null) {
            buttonClickPlayer.setVolume(Math.max(0, Math.min(1, volume)));
        }
    }
    
    /**
     * Thay đổi âm lượng nhạc nền (0.0 - 1.0)
     */
    public static void setBackgroundMusicVolume(double volume) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(Math.max(0, Math.min(1, volume)));
        }
    }
}
