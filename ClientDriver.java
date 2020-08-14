import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClientDriver extends JFrame{
	private Socket socket;
	private PrintWriter out;
	private Receiver receiver;
	private MatchingPanel mp = new MatchingPanel();
	private Container cont;
	//private int port = 12119;

	class MatchingPanel extends JPanel implements MouseListener{
		Player player;
		Othello othello;
		ImageIcon whiteIcon, blackIcon, boardIcon, placeableIcon;
		JButton buttonArray[][];
		JTextField turn;
		int winner;

		MatchingPanel(){
			player = new Player("0","0","0");
			othello = new Othello(0);
			//ウィンドウ設定
			this.setSize(350, 500);//ウィンドウのサイズを設定
			this.setLayout(null);
			//アイコン設定(画像ファイルをアイコンとして使う)
			whiteIcon = new ImageIcon(new File("White.jpg").getAbsolutePath());
			blackIcon = new ImageIcon(new File("Black.jpg").getAbsolutePath());
			boardIcon = new ImageIcon(new File("GreenFrame.jpg").getAbsolutePath());
			placeableIcon = new ImageIcon(new File("Placeable.jpg").getAbsolutePath());
			//盤面生成
			buttonArray = new JButton[8][8];
			for(int j=0; j<8; j++) {
				int yy = 20 + j*38;
				for(int i=0; i<8; i++) {
					if(othello.getGrids()[j][i] == 1){ buttonArray[j][i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == 2){ buttonArray[j][i] = new JButton(placeableIcon);}//盤面状態に応じたアイコンを設定

					int xx = 20 + i*38;
					this.add(buttonArray[j][i]);
					buttonArray[j][i].setBounds(xx, yy, 45, 45);//ボタンの大きさと位置を設定する．
					buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
					buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
				}
			}
			othello.checkPlaceable();
			updateDisp();
			//手番表示用テキストフィールド
			turn = new JTextField(10);
			turn.setEditable(false);
			turn.setBounds(25,370,300,50);
			turn.setHorizontalAlignment(JTextField.CENTER);
			this.add(turn);
			turn.setText("あなたの番です");
			//IPアドレス入力用ポップアップ画面表示
			ConnectServerSubDialog cssd;
			cssd = new ConnectServerSubDialog();//再戦希望集計時ダイアログの作成
			cssd.setVisible(true);
			cssd.setLocation(55, 170);
		}

		public void updateDisp() {
			for(int j=0; j<8; j++) {
				for(int i=0; i<8; i++) {
					if(othello.getGrids()[j][i] == 1){ buttonArray[j][i].setIcon(whiteIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i].setIcon(blackIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i].setIcon(boardIcon);}//盤面状態に応じたアイコンを設定
					else if(othello.getGrids()[j][i] == 2){ buttonArray[j][i].setIcon(placeableIcon);}//盤面状態に応じたアイコンを設定
				}
			}
		}

		public void acceptAction(int x, int y) {
			othello.checkPlaceable();
			othello.setStone(x, y);//ターンが変わる
			System.out.println("相手が(" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ")に石を置きました。");
			if(!othello.checkPlaceable()) {//置けない場合
				othello.changeTurn();
				if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
					EndMatchingSubDialog emsd;
					if((winner == -1 && player.getColor().equals("black"))) {
						emsd = new EndMatchingSubDialog("win");//再戦希望集計時ダイアログの作成
						System.out.println("対局が終了しました。あなたの勝ちです。");
					}
					else if(winner == 0) {
						emsd = new EndMatchingSubDialog("draw");//再戦希望集計時ダイアログの作成
						System.out.println("対局が終了しました。引き分けです。");
					}
					else {
						emsd = new EndMatchingSubDialog("lose");//再戦希望集計時ダイアログの作成
						System.out.println("対局が終了しました。あなたの負けです。");
					}
					emsd.setVisible(true);
					emsd.setLocation(55, 170);
				}
				updateDisp();
			}
			else {
				updateDisp();
			}
			if(othello.getTurn() == -1)
				turn.setText("あなたの番です");
			else
				turn.setText("CPUの番です");
		}

		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			int x = Integer.parseInt(command) % 8;
			int y = Integer.parseInt(command) / 8;
			System.out.println("あなたが(" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ")に石を置きました。");
			if(othello.getGrids()[y][x] == 2 && othello.getTurn() == -1) {
				othello.setStone(x, y);//ターンが変わる
				updateDisp();
				sendMessage("sendOperation");
				sendMessage(String.valueOf(x));
				sendMessage(String.valueOf(y));
				if(!othello.checkPlaceable()) {//相手がおけない場合
					if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
						EndMatchingSubDialog emsd;
						if((winner == -1 && player.getColor().equals("black"))) {
							emsd = new EndMatchingSubDialog("win");//再戦希望集計時ダイアログの作成
							System.out.println("対局が終了しました。あなたの勝ちです。");
						}
						else if(winner == 0) {
							emsd = new EndMatchingSubDialog("draw");//再戦希望集計時ダイアログの作成
							System.out.println("対局が終了しました。引き分けです。");
						}
						else {
							emsd = new EndMatchingSubDialog("lose");//再戦希望集計時ダイアログの作成
							System.out.println("対局が終了しました。あなたの負けです。");
						}
						emsd.setVisible(true);
						emsd.setLocation(55, 170);
					}
					othello.changeTurn();
					othello.checkPlaceable();
					updateDisp();
				}
				if(othello.getTurn() == -1)
					turn.setText("あなたの番です");
				else
					turn.setText("CPUの番です");
			}
		}
		public void mouseEntered(MouseEvent e) {//マウスがオブジェクトに入ったときの処理
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		public void mouseExited(MouseEvent e) {//マウスがオブジェクトから出たときの処理
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class ConnectServerSubDialog extends JDialog implements MouseListener{
		JLabel message;
		JTextField tf;
		JButton ok;
		JLabel error;
		ConnectServerSubDialog(){
			//ウィンドウ設定
			this.setSize(240, 200);
			this.setLayout(null);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			//メッセージラベル
			message = new JLabel("IPアドレスを入力してください");
			message.setBounds(20,30,200,40);
			message.setHorizontalAlignment(JTextField.CENTER);
			this.add(message);
			//テキストフィールド
			tf = new JTextField(20);
			tf.setBounds(20, 80,200,40);
			this.add(tf);
			//okボタン
			ok = new JButton("OK");
			ok.setBounds(20,125, 200, 30);
			ok.addMouseListener(this);
			this.add(ok);
		}
		public void mouseClicked(MouseEvent e) {
			if(connectServer(tf.getText(),12119)) {
				dispose();
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class EndMatchingSubDialog extends JDialog implements MouseListener{
		JLabel labelResult;
		JButton ok;
		EndMatchingSubDialog(String result){
			//ウィンドウ設定
			this.setSize(240, 160);
			this.setLayout(null);
			if(result.equals("win")) {
				labelResult = new JLabel("YOU WON!");
			}
			else if(result.equals("lose")) {
				labelResult = new JLabel("YOU LOST!");
			}
			else {
				labelResult = new JLabel("DRAW!");
			}
			labelResult.setBounds(20,10,200,45);
			this.add(labelResult);
			//okボタン
			JButton ok = new JButton("OK");
			ok.setBounds(20,50,200,45);
			ok.addMouseListener(this);
			this.add(ok);
		}
		public void mouseClicked(MouseEvent e) {
			try {
				socket.close();
				System.out.println("サーバとの接続を解除し、プログラムを終了します。");
			}
			catch(IOException ioe) {}
			System.exit(1);
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	public boolean connectServer(String ipAddress, int port){	// サーバに接続
		socket = null;
		try {
			socket = new Socket(ipAddress, port); //サーバ(ipAddress, port)に接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
			receiver = new Receiver(socket); //受信用オブジェクトの準備
			receiver.start();//受信用オブジェクト(スレッド)起動
			System.out.println("接続されました。");
			return true;
		} catch (UnknownHostException e) {
			System.err.println("ホストのIPアドレスが判定できません: " + e);
			return false;
		} catch (IOException e) {
			System.err.println("サーバ接続時にエラーが発生しました: " + e);
			return false;
		}
	}

	public void sendMessage(String msg){	// サーバに情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
	}

	// データ受信用スレッド(内部クラス)
		class Receiver extends Thread {
			private InputStreamReader sisr; //受信データ用文字ストリーム
			private BufferedReader br; //文字ストリーム用のバッファ

			// 内部クラスReceiverのコンストラクタ
			Receiver (Socket socket){
				try{
					sisr = new InputStreamReader(socket.getInputStream()); //受信したバイトデータを文字ストリームに
					br = new BufferedReader(sisr);//文字ストリームをバッファリングする
				} catch (IOException e) {
					System.err.println("データ受信時にエラーが発生しました: " + e);
				}
			}
			// 内部クラス Receiverのメソッド
			public void run(){
				while(true) {//データを受信し続ける
					try {
						String inputLine = br.readLine();//目的を読み込む

						if(inputLine.equals("sendOperation")) {
							inputLine = br.readLine();
							int x = Integer.parseInt(inputLine);
							inputLine = br.readLine();
							int y = Integer.parseInt(inputLine);
							mp.acceptAction(x,y);
						}
						else if(inputLine.equals("end")) {}
						Thread.sleep(500);
					}
					catch (IOException e){
						System.err.println("データ受信時にエラーが発生しました: " + e);
						break;
					} catch(InterruptedException e) {
						System.err.println("データ受信時にエラーが発生しました: " + e);
						break;
					}
				}
			}
		}

		ClientDriver(){
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("ClientDriver");
			setSize(350,500);

			cont = getContentPane();
			cont.add(mp, BorderLayout.CENTER);
		}

		public static void main(String args[]){
			ClientDriver driver = new ClientDriver();
			driver.setVisible(true);
			driver.setResizable(false);
		}
}
