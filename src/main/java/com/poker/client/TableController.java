package com.poker.client;
import java.util.ArrayList;
import java.util.List;

import com.poker.server.Server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class TableController{

    @FXML public TextField p1;
    @FXML public TextField p2;
    @FXML public TextField p3;
    @FXML public TextField p4;
    @FXML public TextField p5;
    @FXML public TextField c1;
    @FXML public TextField c2;
    @FXML public TextField c3;
    @FXML public TextField c4;
    @FXML public TextField c5;
    @FXML private TextField raisebox;
    @FXML public TextField whichPturn;

    @FXML public  ImageView card1;
    @FXML public  ImageView card2;

    public Circle circle;
    public Circle circle2;
    public Circle circle3;
    public Circle circle4;
    public Circle circle5;
    public Circle pot1;
    public Circle pot2;
    public Circle pot3;
    public Circle pot4;
    public Circle pot5;
    public Circle pot6;
    public Circle avatar1;
    public Circle avatar2;
    public Circle avatar3;
    public Circle avatar4;
    public Circle avatar5;

    @FXML public ImageView cc1;
    @FXML public ImageView cc2;
    @FXML public ImageView cc3;
    @FXML public ImageView cc4;
    @FXML public ImageView cc5;
    @FXML public ImageView card21;
    @FXML public ImageView card22;
    @FXML public ImageView card31;
    @FXML public ImageView card32;
    @FXML public ImageView card41;
    @FXML public ImageView card42;
    @FXML public ImageView card51;
    @FXML public ImageView card52;

    @FXML public Text bet;
    @FXML public Text action;
    @FXML public Text pot;
    @FXML public Text message;
    @FXML public Text action2;
    @FXML public Text action3;
    @FXML public Text action4;
    @FXML public Text action5;

    @FXML private Button startGameBtn; // Thêm

    public List<Server> players = new ArrayList<>();
    public Server leader = null;
    public boolean isStarted = false;
    private boolean isTableLeader = false;

    public void call(){

          message.setText(" ");

          if(Main.client.isTurn){
              if(Main.client.currentbet - Main.client.selfBet<= Main.client.chips){
                  if(Main.client.currentbet != Main.client.selfBet){

                      Main.client.out.println("call");
                      Main.client.selfBet = Main.client.currentbet;
                      Main.client.isTurn = false;
                  }
                  else message.setText("You can't call now. Check, raise or fold.");
              }
              else {
                  message.setText("You need to fold now!"); // today

                  Main.client.out.println("fold");
                  Main.client.isTurn = false;
                  card1.setImage(new Image("/graphic/download.jpg"));
                  card2.setImage(new Image("/graphic/download.jpg"));
              }
          }
          else message.setText("Not Your Turn!");
      }

    public void check(){

        message.setText(" ");

          if(Main.client.isTurn){
              if(Main.client.currentbet == Main.client.selfBet){
                  Main.client.out.println("check");
                  Main.client.isTurn = false;
              }
              else message.setText("You need to call the current bet!");
          }
          else message.setText("Not Your Turn!");
    }

    public void raise() {

        message.setText(" ");

        try{
            if (Main.client.isTurn) {

                if (Integer.parseInt(raisebox.getText()) - Main.client.selfBet <= Main.client.chips) {

                    if(Integer.parseInt(raisebox.getText()) > Main.client.currentbet){

                        Main.client.out.println("raise#" + raisebox.getText());
                        Main.client.selfBet = Integer.parseInt(raisebox.getText());
                        raisebox.clear();
                        Main.client.isTurn = false;
                    }
                    else message.setText("You can't raise lower than current bet");
                }
                else message.setText("Not Enough Chips!");
            }
            else message.setText("Not Your Turn!");

        }catch (NumberFormatException e){
            message.setText("Please input Numbers");
        }
    }

    public void fold(){

        message.setText(" ");

        if(Main.client.isTurn){
            Main.client.out.println("fold");
            Main.client.isTurn = false;

            card1.setImage(new Image("/graphic/download.jpg"));
            card2.setImage(new Image("/graphic/download.jpg"));
        }
    }

    /**
     * Handles All-In action: player bets all remaining chips
     */
    public void allIn(){

        message.setText(" ");

        if(Main.client.isTurn){
            if(Main.client.chips > 0){
                Main.client.out.println("allin");
                Main.client.isTurn = false;
            }
            else {
                message.setText("You have no chips to go all-in!");
            }
        }
        else {
            message.setText("Not Your Turn!");
        }
    }

    public void logout(){

        if(Main.client.canLogout){
            Main.client.out.println("logout");
            Platform.exit();
            System.exit(0);
        }

    }

    public synchronized void addPlayer(Server player) {
        players.add(player);

        if (leader == null) {
            leader = player;
        }
    }

    public synchronized void removePlayer (Server player) {
        players.remove(player);

        if (player == leader) {
            leader = players.isEmpty() ? null : players.get(0);
        }
    }

    public void startGame() {
        message.setText(" ");

        // Kiểm tra: Chỉ người đầu tiên mới có quyền start
        if (!isTableLeader) {
            message.setText("Only the table leader can start the game!");
            return;
        }

        // Kiểm tra: Tối thiểu 2 người
        int playerCount = 0;
        if (!p1.getText().equals("Empty")) playerCount++;
        if (!p2.getText().equals("Empty")) playerCount++;
        if (!p3.getText().equals("Empty")) playerCount++;
        if (!p4.getText().equals("Empty")) playerCount++;
        if (!p5.getText().equals("Empty")) playerCount++;
        
        if (playerCount < 2) {
            message.setText("At least 2 players to start.");
            return;
        }
        
        Main.client.out.println("startgame");
        message.setText("Starting game...");
    }

    // Thêm method: Gọi từ Client.java để set table leader
    public void setTableLeader(boolean isLeader) {
        this.isTableLeader = isLeader;

        // Hiển thị nút Start dựa theo có là leader hay không
        if (startGameBtn != null) {
            if (isLeader) {
                startGameBtn.setDisable(false);
                startGameBtn.setStyle("-fx-background-color: #ff6b00; -fx-background-radius: 30;");
                message.setText("You are the table leader! Click 'Start game' when ready.");
            } else {
                startGameBtn.setDisable(true);
                startGameBtn.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 30;");
                message.setText("Waiting for the table leader to start the game...");
            }
        }
    }
}