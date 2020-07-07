//パッケージのインポート
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client extends JFrame {
	//private StartPanel sp = new StartPanel(this, "sp");
	private MenuPanel mnp = new MenuPanel(this, "mnp");//その他パネルはボタンクリック時に作成
	private ChooseLevelPanel clp;//難易度選択パネル
	private OptionToLoginPanel otlp;//アカウント作成またはログイン画面用パネル
	private MenuForNetworkMatchingPanel mfnmp;//ネットワーク対局用メニュー画面パネル
	private ReadRecordPanel rrp;//戦績ランキング閲覧パネル
	private SearchingForOpponentPanel sfop;//マッチングパネル
	private MatchingPanel mp;//対局パネル
	private Player player;//プレイヤ
	private Player opponent;//相手
	Othello othello;
	private EndMatchingSubDialog emsd;
	private Container cont; // コンテナ
	private PrintWriter out;//データ送信用オブジェクト
	private Socket socket;
	private Receiver receiver; //データ受信用オブジェクト
	private int w = 750;
	private int h = 500;
	private boolean flag;//通信時に使用
	private boolean result;//通信時に使用
	private boolean SE = true;
	private boolean PLACABLE = true;
	//static Clip bgm1= createClip(new File("RapGod.wav"));
	static Font font;

	//パネル・ダイアログ（内部クラス）
	class MenuPanel extends JPanel implements MouseListener{//スタート画面用パネル
		JLabel title;
		JLabel title2;
		JButton bN, bL;//ネットワーク対局、ローカル対局選択用ボタン
		Client client;
		String str;
		int w = 750;
		int h = 500;
		//コンストラクタ
		MenuPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			this.setBackground(new Color(130, 0, 170));
			//タイトル1
			title = new JLabel("REVERSI");
			title.setFont(new Font("PixelMplus10", Font.PLAIN, 140));
			title.setForeground(Color.GREEN);
			this.add(title);
			title.setBounds(w/5-5, h/8, 600, 200);
			//タイトル2
			title2 = new JLabel("REVERSI");
			title2.setFont(new Font("PixelMplus10", Font.PLAIN, 140));
			title2.setForeground(Color.RED);
			this.add(title2);
			title2.setBounds(w/5-14, h/8, 600, 200);
			//ネットワーク対局選択用ボタン
			bN = new JButton("ネットワーク対局");
			this.add(bN); //ネットワーク対局選択用ボタンをパネルに追加
			bN.setBackground(Color.GREEN);
			bN.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bN.setBounds(w/3+30, h/2+40, 200, 40);
			bN.addMouseListener(this);//マウス操作を認識できるようにする
			bN.setActionCommand("network");//ボタンを識別するための名前を付加する
			//ローカル対局選択用ボタン
			bL = new JButton("ローカル対局");
			this.add(bL);//ローカル対局選択用ボタンをパネルに追加
			bL.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bL.setBounds(w/3+30, h/2+100, 200, 40);
			bL.addMouseListener(this);//マウス操作を認識できるようにする
			bL.setActionCommand("local");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			this.setVisible(false);
			if(theButton == bN) {//ネットワーク対局ボタンクリック時
				client.otlp = new OptionToLoginPanel(client, "otlp");
				client.PanelChange((JPanel)this, (JPanel)client.otlp);
			} else {//ローカル対局ボタンクリック時
				client.clp = new ChooseLevelPanel(client, "clp");
				client.PanelChange((JPanel)this, (JPanel)client.clp);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class ChooseLevelPanel extends JPanel implements MouseListener{//難易度選択画面
		JLabel labelT;//タイトル用ラベル
		JButton bEasy;//Easyボタン
		JButton bNormal;//Normalボタン
		JButton bHard;//Hardボタン
		JButton bBack;//戻るボタン
		Client client;
		String str;
		int w = 750;
		int h = 500;
		//コンストラクタ
		ChooseLevelPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			this.setBackground(new Color(130, 0, 170));
			//タイトル用ラベル
			labelT = new JLabel("難易度選択");
			this.add(labelT);
			labelT.setBounds(24*w/50-20, h/11, 300, 40);
			//Easyボタン
			bEasy = new JButton("Easy");
			this.add(bEasy);
			bEasy.setBounds(2*w/5-20, 3*h/11, 200, 50);
			bEasy.addMouseListener(this);//マウス操作を認識できるようにする
			bEasy.setActionCommand("easy");//ボタンを識別するための名前を付加する
			//Normalボタン
			bNormal = new JButton("Normal");
			this.add(bNormal);
			bNormal.setBounds(2*w/5-20, 5*h/11, 200, 50);
			bNormal.addMouseListener(this);//マウス操作を認識できるようにする
			bNormal.setActionCommand("normal");//ボタンを識別するための名前を付加する
			//Hardボタン
			bHard = new JButton("Hard");
			this.add(bHard);
			bHard.setBounds(2*w/5-20, 7*h/11, 200, 50);
			bHard.addMouseListener(this);//マウス操作を認識できるようにする
			bHard.setActionCommand("hard");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			this.setVisible(false);
			if(theButton == bBack) {//戻るボタンクリック時
				client.PanelChange((JPanel)this, (JPanel)client.mnp);
			} else if (theButton == bEasy) {//Easyボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "easy");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			} else if (theButton == bNormal) {//Normalボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "normal");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			} else {//Hardボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "hard");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class OptionToLoginPanel extends JPanel implements MouseListener{//アカウント作成またはログイン用パネル
		JButton bMA, bLI,bBack;//アカウント、ログイン選択用ボタン
		Client client;
		String str;
		int w = 750;
		int h = 500;
		//コンストラクタ
		OptionToLoginPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//アカウント作成用ボタン
			bMA = new JButton("アカウント作成");
			this.add(bMA); //アカウント作成用ボタンをパネルに追加
			bMA.setBounds(w/3+40, h/3, 200, 50);
			bMA.addMouseListener(this);//マウス操作を認識できるようにする
			bMA.setActionCommand("makeAccount");//ボタンを識別するための名前を付加する
			//ログイン用ボタン
			bLI = new JButton("ログイン");
			this.add(bLI);//ログイン用ボタンをパネルに追加
			bLI.setBounds(w/3+40, h/3+80, 200, 50);
			bLI.addMouseListener(this);//マウス操作を認識できるようにする
			bLI.setActionCommand("login");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			if(theButton == bBack) {//戻るボタンクリック時
				this.setVisible(false);
				client.PanelChange((JPanel)this, (JPanel)client.mnp);
			} else if (theButton == bMA) {//アカウント作成ボタンクリック時
				MakeAccountSubDialog masd = new MakeAccountSubDialog(client, "masd");
				masd.setLocation(w/6+350, h/6+200);
				masd.setVisible(true);
			} else if (theButton == bLI) {//ログインボタンクリック時
				LoginSubDialog lsd = new LoginSubDialog(client, "lsd");
				lsd.setLocation(w/6+350, h/6+200);
				lsd.setVisible(true);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	class MakeAccountSubDialog extends JDialog implements ActionListener{//アカウント作成時ダイアログ
		JLabel labelN;//名前入力用ラベル
		JTextField name;//名前入力用テキストフィールド
		JLabel labelP;//パスワード入力用ラベル
		JPasswordField pass;//パスワードフィールド
		JButton bOK, bCan;//OK、キャンセルボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;
		//コンストラクタ
		MakeAccountSubDialog(Client c, String s){

			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w,h);
			setTitle("アカウント作成");
			this.setLayout(null);
			//名前入力用ラベル
			labelN = new JLabel("名前を入力してください");
			this.add(labelN);
			labelN.setBounds(w/4, 2*h/9, 300, 40);
			//名前入力用テキストフィールド
			name = new JTextField(30);
			this.add(name);
			name.setBounds(w/4, 3*h/9, 300, 40);
			//パスワード入力用ラベル
			labelP = new JLabel("パスワードを入力してください");
			this.add(labelP);
			labelP.setBounds(w/4, 4*h/9, 300, 40);
			//パスワードフィールド
			pass = new JPasswordField(30);
			this.add(pass);
			pass.setBounds(w/4, 5*h/9, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			this.add(bOK);
			bOK.setBounds(w/3, 13*h/18, 90, 30);
			bOK.addActionListener(this);//マウス操作を認識できるようにする
			bOK.setActionCommand("ok");//ボタンを識別するための名前を付加する
			//キャンセルボタン
			bCan = new JButton("キャンセル");
			this.add(bCan);
			bCan.setBounds(7*w/12, 13*h/18, 90, 30);
			bCan.addActionListener(this);//マウス操作を認識できるようにする
			bCan.setActionCommand("cancel");//ボタンを識別するための名前を付加する
		}
		//メソッド
		public void makeAccount(String name, String password) {//アカウントの作成
			sendMessage("makeAccount");//目的宣言
			sendMessage(name);
			sendMessage(password);
			sendMessage("end");//終了サイン

			client.flag = false;//receiverがログイン認証結果をサーバから受け取ったらtrueにする
			client.result = false;//ログイン認証結果が成功ならRecieverのrunメソッドでtrue
			int i = 0;

			while(++i<=10) {
				try {
					Thread.sleep(1000);
					if(client.flag) break;
				}
				catch(InterruptedException e) {}
			}
		}

		public void actionPerformed(ActionEvent e) {//マウスクリック時の処理
			if(e.getSource() == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//正常処理
					setVisible(false);
					/*
					connectServer();//サーバに接続
					makeAccount(labelN.getText(), passwordstr);//アカウント作成処理
					*/
					SucceededToMakeAccountSubDialog atmasd = new SucceededToMakeAccountSubDialog(client, "atmasd");
					atmasd.setLocation(client.w/6+350, client.h/6+200);
					atmasd.setVisible(true);
				} else {//エラー時
					JLabel labelError = new JLabel("入力に誤りがあります");
					labelError.setForeground(Color.GREEN);
					this.add(labelError);
					labelError.setBounds(w/3, h/9, 300, 20);
				}
			} else {//キャンセルボタンクリック時
				dispose();
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class SucceededToMakeAccountSubDialog extends JDialog implements ActionListener{//アカウント作成成功時ダイアログ
		JLabel labelM;//メッセージ用ラベル
		JLabel labelID;//ユーザID表示用ラベル
		JTextField ID;//ユーザID表示用テキストフィールド
		JLabel labelC1;//注意用ラベル
		JLabel labelC2;//注意用ラベル
		JButton bOK;//OKボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;
		//コンストラクタ
		SucceededToMakeAccountSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w,h);
			this.setLayout(null);
			//メッセージ用ラベル
			labelM = new JLabel("アカウントを作成しました");
			this.add(labelM);
			labelM.setBounds(w/3, 2*h/9, 300, 40);
			//ユーザID表示用ラベル
			labelID = new JLabel("ユーザID");
			this.add(labelID);
			labelID.setBounds(w/4, 3*h/9, 300, 40);
			//ユーザID表示用テキストフィールド
			ID = new JTextField("30");
			this.add(ID);
			ID.setBounds(w/4, 4*h/9, 300, 40);
			ID.setText("player.getID()");
			ID.setEditable(false);
			//注意用ラベル
			labelC1 = new JLabel("ユーザIDとパスワードは次回");
			labelC2 = new JLabel("ログイン時に必要になります");
			this.add(labelC1);
			this.add(labelC2);
			labelC1.setBounds(w/3, 5*h/9, 300, 40);
			labelC2.setBounds(w/3, 11*h/18, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			this.add(bOK);
			bOK.setBounds(w*2/5, 7*h/9, 90, 30);
			bOK.addActionListener(this);//マウス操作を認識できるようにする
			bOK.setActionCommand("ok");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			dispose();
			client.otlp.setVisible(false);
			client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
			client.PanelChange((JPanel)client.otlp, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class LoginSubDialog extends MakeAccountSubDialog{//ログイン時ダイアログ
		JLabel labelID;//ID入力用ラベル
		JTextField ID;//ID入力用テキストフィールド
		LoginSubDialog(Client c, String s){
			super(c, s);
			//ウィンドウ設定
			setTitle("ログイン");
			//ユーザID入力用ラベル
			labelID = new JLabel("ユーザIDを入力してください");
			this.remove(labelN);
			this.add(labelID);
			labelID.setBounds(w/4, 2*h/9, 300, 40);
			//ユーザID入力用テキストフィールド
			ID = new JTextField(30);
			this.remove(name);
			this.add(ID);
			ID.setBounds(w/4, 3*h/9, 300, 40);
		}
		//メソッド
		public boolean login(String id, String password) {//ログイン認証
			sendMessage("login");
			sendMessage(id);
			sendMessage(password);
			sendMessage("end");

			client.flag = false;//receiverがログイン認証結果をサーバから受け取ったらtrueにする
			client.result = false;//ログイン認証結果が成功ならRecieverのrunメソッドでtrue
			int i = 0;

			while(++i<=10) {//10秒間認証結果を待つ
				try {
					Thread.sleep(1000);
					if(client.flag) {//認証結果が送られてきたら
						client.flag = false;//flagをfalseに戻して
						return client.result;//結果（成功ならtrue,失敗ならfalse）を返す
					}
				}
				catch(InterruptedException e) {}
			}
			return client.result;//10秒待っても来なかったら一応falseを返す
		}

		public void actionPerformed(ActionEvent e) {//マウスクリック時の処理
			if(e.getSource() == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//入力に抜けがなければ
					/*
					client.connectServer("localhost", 10003);//サーバに接続
					if(login(ID.getText(), passwordstr)){//ログイン成功時
						dispose();
						SucceededToLoginSubDialog stlsd = new SucceededToLoginSubDialog(client, "stlsd");
						stlsd.setLocation(client.w/4, client.h/4);
						stlsd.setVisible(true);
					} else {
						dispose();
						FailedToLoginSubDialog ftlsd = new FailedToLoginSubDialog(client, "ftlsd");
						ftlsd.setLocation(client.w/4, client.h/4);
						ftlsd.setVisible(true);
					}

					*/

					//テスト用処理
					dispose();//このダイアログを破棄
					SucceededToLoginSubDialog stlsd = new SucceededToLoginSubDialog(client, "stlsd");//成功時のダイアログを作成
					stlsd.setLocation(client.w/4+350, client.h/4+200);
					stlsd.setVisible(true);
				} else {//入力に抜けがある場合
					dispose();//破棄
					FailedToLoginSubDialog ftlsd = new FailedToLoginSubDialog(client, "ftlsd");//失敗時のダイアログを作成
					ftlsd.setLocation(client.w/4+350, client.h/4+200);
					ftlsd.setVisible(true);
				}
			} else {//キャンセルボタンクリック時
				dispose();
				/*
				try {
					client.socket.close();
				}
				catch(IOException ioe) {}
				 */
			}
		}
	}

	class SucceededToLoginSubDialog extends JDialog implements ActionListener{//ログイン成功時ダイアログ
		JLabel message;//メッセージ表示用ラベル
		JButton bOK;//OKボタン
		int w = 360;
		int h = 240;
		Client client;
		String str;
		//コンストラクタ
		SucceededToLoginSubDialog(Client c, String s){
			client = c;
			str = s;
			// ウィンドウ設定
			this.setSize(w,h);
			this.setLayout(null);
			//メッセージ表示用ラベル
			message = new JLabel("ログインに成功しました");
			this.add(message);
			message.setBounds(w/3, 5*h/12, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			this.add(bOK);
			bOK.setBounds(2*w/5, 3*h/5, 90, 30);
			bOK.addActionListener(this);//マウス操作を認識できるようにする
			bOK.setActionCommand("ok");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bOK) {
				dispose();
				client.otlp.setVisible(false);
				client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
				client.PanelChange((JPanel)client.otlp, (JPanel)client.mfnmp);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class FailedToLoginSubDialog extends SucceededToLoginSubDialog{//ログイン失敗時ダイアログ
		JButton bRetry;//キャンセルボタン
		//コンストラクタ
		FailedToLoginSubDialog(Client c, String s){
			super(c, s);
			//メッセージの書き換え
			this.remove(message);
			message = new JLabel("ログインに失敗しました");
			this.add(message);
			message.setBounds(w/3, 5*h/12, 300, 40);
			//キャンセルボタン
			bRetry = new JButton("Retry");
			this.remove(bOK);
			this.add(bRetry);
			bRetry.setBounds(2*w/5, 3*h/5, 90, 30);
			bRetry.addActionListener(this);//マウス操作を認識できるようにする
			bRetry.setActionCommand("retry");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			dispose();
			LoginSubDialog lsd = new LoginSubDialog(client, "lsd");
			lsd.setLocation(client.w/6+350, client.h/6+200);
			lsd.setVisible(true);
		}
	}

	class MenuForNetworkMatchingPanel extends JPanel implements MouseListener{//ネットワーク対局用メニュー画面パネル
		JLabel labelID;//ID用ラベル
		JLabel labelR;//レート用ラベル
		JButton bRank;//ランクマッチボタン
		JButton bSpecial;//スペシャルマッチボタン
		JButton bInfo;//戦績・ランキング閲覧ボタン
		JButton bBack;//戻るボタン
		Client client;
		String str;
		int w = 750;
		int h = 500;
		//コンストラクタ
		MenuForNetworkMatchingPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//ID用ラベル
			labelID = new JLabel("プレイヤ名："+"player.getUserInfo().id");
			this.add(labelID);
			labelID.setBounds(6*w/9, h/20, 300, 30);
			//レート用ラベル
			labelR = new JLabel("レーティング："+"player.getUserInfo().rate");
			this.add(labelR);
			labelR.setBounds(6*w/9, h*1/10, 300, 30);
			//ランクマッチボタン
			bRank = new JButton("ランクマッチ");
			this.add(bRank);
			bRank.setBounds(11*w/30 +5, 2*h/5-30, 200, 50);
			bRank.addMouseListener(this);//マウス操作を認識できるようにする
			bRank.setActionCommand("rank");//ボタンを識別するための名前を付加する
			//スペシャルマッチボタン
			bSpecial = new JButton("スペシャルマッチ");
			this.add(bSpecial);
			bSpecial.setBounds(11*w/30+5, 3*h/5-30, 200, 50);
			bSpecial.addMouseListener(this);//マウス操作を認識できるようにする
			bSpecial.setActionCommand("special");//ボタンを識別するための名前を付加する
			//戦績・ランキング閲覧ボタン
			bInfo = new JButton("戦績・ランキング");
			this.add(bInfo);
			bInfo.setBounds(7*w/10+15, 8*h/9, 200, 35);
			bInfo.addMouseListener(this);//マウス操作を認識できるようにする
			bInfo.setActionCommand("info");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			this.setVisible(false);//パネルを見えなくする
			if(theButton == bBack) {//戻るボタンクリック時
				client.PanelChange((JPanel)this, (JPanel)client.mnp);
				try {socket.close();}
				catch(IOException ioe) {}
			} else if (theButton == bInfo) {//戦績ランキング閲覧ボタンクリック時
				client.rrp = new ReadRecordPanel(client, "rrp");
				client.PanelChange((JPanel)this, (JPanel)client.rrp);
			} else if (theButton == bRank) {//ランクマッチボタンクリック時
				client.sfop = new SearchingForOpponentPanel(client, "sfop", "rank");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);
			} else {//スペシャルマッチボタンクリック時
				client.sfop = new SearchingForOpponentPanel(client, "sfop", "special");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class ReadRecordPanel extends JPanel implements MouseListener{//戦績ランキング閲覧パネル
		JLabel labelTitle1;//総戦績用ラベル
		JLabel labelScore;//戦績ラベル
		JLabel labelTitle2;//投了数用ラベル
		JLabel labelResignation;//投了数ラベル
		JTextArea taRecord;//戦績表示テキストエリア
		JLabel labelTitle3;//ランキング用ラベル
		JLabel labelRanking;//ランキングラベル
		JTextArea taRanking;//ランキング表示テキストエリア
		JButton bBack;//戻るボタン
		Client client;
		String str;
		int w = 750;
		int h = 500;
		//コンストラクタ
		ReadRecordPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//総戦績用ラベル
			labelTitle1 = new JLabel("総戦績");
			this.add(labelTitle1);
			labelTitle1.setBounds(30, 1*h/12, 300, 40);
			//戦績ラベル
			labelScore = new JLabel("*********");
			this.add(labelScore);
			labelScore.setBounds(30, 2*h/12, 300, 40);
			//投了数用ラベル
			labelTitle2 = new JLabel("投了数");
			this.add(labelTitle2);
			labelTitle2.setBounds(30, 3*h/12, 300, 40);
			//投了数ラベル
			labelResignation = new JLabel("*********");
			this.add(labelResignation);
			labelResignation.setBounds(30, 4*h/12, 300, 40);
			//戦績表示テキストエリア
			taRecord = new JTextArea("**********", 100, 200);
			JScrollPane spRecord = new JScrollPane(taRecord);
			this.add(spRecord);
			spRecord.setBounds(30, h/2, 350, 200);
			//ランキング用ラベル
			labelTitle3 = new JLabel("ランキング");
			this.add(labelTitle3);
			labelTitle3.setBounds(30+w/2, 1*h/12, 300, 40);
			//ランキングラベル
			labelRanking = new JLabel("**********");
			this.add(labelRanking);
			labelRanking.setBounds(30+w/2, 2*h/12, 300, 40);
			//ランキング表示テキストエリア
			taRanking = new JTextArea("**********", 100, 200);
			JScrollPane spRanking = new JScrollPane(taRanking);
			this.add(spRanking);
			spRanking.setBounds(30+w/2, 6*h/18, 350, 290);
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			this.setVisible(false);
			PanelChange((JPanel)this, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class SearchingForOpponentPanel extends JPanel{//マッチングパネル
		JTextField tfMessage;//メッセージ用ラベル
		Client client;
		String str;
		String mode;
		int w = 750;
		int h = 500;
		//コンストラクタ
		SearchingForOpponentPanel(Client c, String s, String m){
			client = c;
			str = s;
			mode = m;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//メッセージ用ラベル
			tfMessage = new JTextField("プレイヤを探しています...");
			this.add(tfMessage);
			tfMessage.setBounds(w/3, 3*h/7, 300, 50);
			//対戦相手を探す
			findOpponent();
		}
		//パネル名の取得
		public String getName() {
			return str;
		}

		public void findOpponent() {
			sendMessage("lookingForOpponent");
			sendMessage(mode);
			sendMessage("end");

			int timeLimit = 60;
			client.flag = false;
			client.result = false;

			while(true) {
				try {
					if(timeLimit-- != 0) {
						if(client.flag) {
							if(client.result) {
								this.setVisible(false);
								client.mp = new MatchingPanel(client, "mp", mode);
								PanelChange((JPanel)this, (JPanel)client.mp);
							} else {
								tfMessage.setText("対戦相手が見つかりませんでした");
								Thread.sleep(5000);
								this.setVisible(false);
								PanelChange((JPanel)this, (JPanel)client.mfnmp);
							}
							break;
						}
					}
					else {
						tfMessage.setText("対戦相手が見つかりませんでした");
						this.setVisible(false);
						PanelChange((JPanel)this, (JPanel)client.mfnmp);
						sendMessage("done");
						break;
					}
					Thread.sleep(1000);
				}
				catch(InterruptedException e) {}
			}
		}
	}

	class MatchingPanel extends JPanel implements MouseListener{//対局画面用パネル
		Computer cpu;
		int row = 8;//オセロ盤の縦横マスの数
		Client client;
		String str;
		String mode;
		JButton buttonArray[][];//オセロ盤用のボタン配列
		JButton bOption, bResign; //設定、投了ボタン
		JTextField tfColor; // 色表示用ラベル
		JTextField tfTurn; // 手番表示用ラベル
		JLabel labelPlayer1;//自分のプレイヤ名
		JLabel labelVS;//vsラベル
		JLabel labelPlayer2;//相手のプレイヤ名
		JLabel labelMode1;//モード表示用ラベル1
		JLabel labelMode2;//モード表示用ラベル2
		JTextField tfBlackNumber;//黒の数
		JTextField tfWhiteNumber;//白の数
		JLabel labelTimer;//残り時間ラベル
		Timer timer;//タイマースレッド
		Confirmer confirmer;//サーバに接続確認信号を送信するスレッド
		JTextField tfEffect;//特殊効果の表示ラベル
		JTextArea taLog;//ログ
		ImageIcon blackIcon, whiteIcon, boardIcon, placeableIcon; //アイコン
		//Clip se;//SE
		int previousRate[] = new int[2];
		int cpuOperation;
		int winner;
		CheckWhetherToResignSubDialog cwtrsd;
		OptionSubDialog osd;
		int w = 750;
		int h = 500;
		//コンストラクタ
		MatchingPanel(Client c, String s, String m){//modeはrank, special, easy, normal, hardのいずれか
			client = c;
			str = s;
			mode = m;
			//FloatControl control = (FloatControl)bgm1.getControl(FloatControl.Type.MASTER_GAIN);
			//control.setValue((float)Math.log10(0.1) * 20);
			//bgm1.setFramePosition(0);
			//bgm1.loop(Clip.LOOP_CONTINUOUSLY);
			//盤面情報の初期化
			if(mode.equals("rank") || mode.equals("special")){//ネットワーク対局の場合
				if(mode.equals("rank")) othello = new Othello(0);//ランクマッチの場合
				else othello = new Othello(1);//スペシャルマッチの場合

				if(player.getColor().equals("white"))//手番が後手なら
					othello.changeTurn();
				else
					othello.checkPlaceable();
			}
			else {//ローカル対局の場合
				player = new Player("0","0","0");
				othello = new Othello(0);
				if(mode.equals("easy"))//easyモード
					cpu = new Computer(1,1);
				else if(mode.equals("normal"))//normalモード
					cpu = new Computer(5,1);
				else if(mode.equals("hard"))//hardモード
					cpu = new Computer(7,1);

				othello.checkPlaceable();
				player.setColor("black");
			}

			//ウィンドウ設定
			this.setSize(w, h);//ウィンドウのサイズを設定
			this.setLayout(null);
			this.setBackground(new Color(130, 0, 170));
			//アイコン設定(画像ファイルをアイコンとして使う)
			whiteIcon = new ImageIcon(new File("White.jpg").getAbsolutePath());
			blackIcon = new ImageIcon(new File("Black.jpg").getAbsolutePath());
			boardIcon = new ImageIcon(new File("GreenFrame.jpg").getAbsolutePath());
			placeableIcon = new ImageIcon(new File("Placeable.jpg").getAbsolutePath());
			//オセロ盤の生成
			buttonArray = new JButton[row][row];//ボタンの配列を作成
			updateDisp(9,9);
			JLabel horizontalNumber;
			JLabel verticalNumber;
			for(int i=1; i<9; i++) {
				horizontalNumber = new JLabel(String.valueOf(i));
				horizontalNumber.setBounds(20 + i*38, 350, 30, 30);
				horizontalNumber.setFont(new Font("PixelMplus10", Font.PLAIN, 25));
				horizontalNumber.setForeground(new Color(243,152,0));
				this.add(horizontalNumber);
				verticalNumber = new JLabel(String.valueOf(i));
				verticalNumber.setFont(new Font("PixelMplus10", Font.PLAIN, 25));
				verticalNumber.setForeground(new Color(243,152,0));
				verticalNumber.setBounds(12, 350 - i*38, 30, 30);
				this.add(verticalNumber);
			}
			//設定ボタン
			bOption = new JButton("設定");//終了ボタンを作成
			this.add(bOption); //終了ボタンをパネルに追加
			bOption.setBounds(4*w/5, 35, 120, 35);//終了ボタンの境界を設定
			bOption.addMouseListener(this);//マウス操作を認識できるようにする
			bOption.setActionCommand("option");//ボタンを識別するための名前を付加する
			//投了ボタン
			bResign = new JButton("投了");//パスボタンを作成
			this.add(bResign); //パスボタンをパネルに追加
			bResign.setBounds(4*w/5, 75, 120, 35);//パスボタンの境界を設定
			bResign.addMouseListener(this);//マウス操作を認識できるようにする
			bResign.setActionCommand("resign");//ボタンを識別するための名前を付加する
			//黒駒の数用ラベル
			tfBlackNumber = new JTextField("黒　" + othello.getBlackstone() + "枚");
			tfBlackNumber.setHorizontalAlignment(JTextField.CENTER);
			tfBlackNumber.setEditable(false);
			this.add(tfBlackNumber);
			tfBlackNumber.setBounds(6*w/11,  5*h/13-30, 80, 40);
			//白駒の数用ラベル
			tfWhiteNumber = new JTextField("白　" + othello.getWhitestone() + "枚");
			tfWhiteNumber.setHorizontalAlignment(JTextField.CENTER);
			tfWhiteNumber.setEditable(false);
			this.add(tfWhiteNumber);
			tfWhiteNumber.setBounds(6*w/11, 6*h/13-25, 80, 40);
			//戦況ログ用テキストエリア
			taLog = new JTextArea(330, 200);
			taLog.setEditable(false);
			taLog.setLineWrap(true);
			taLog.setWrapStyleWord(true);
			JScrollPane scroll = new JScrollPane(taLog);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setBounds(6*w/11-20, 7*h/13-15, 330, 200);
			this.add(scroll);
			//this.add(taLog);
			//modeがネットワーク対戦であるとき
			if(mode.equals("rank") || mode.equals("special")) {
				//色表示用ラベル
				tfColor = new JTextField("あなたは黒です");//色情報を表示するためのラベルを作成
				tfColor.setHorizontalAlignment(JTextField.CENTER);
				tfColor.setEditable(false);
				tfColor.setBounds(w/5-30, 3*h/4, 160, 40);//境界を設定
				this.add(tfColor);//色表示用ラベルをパネルに追加
				//手番表示用ラベル
				tfTurn = new JTextField("あなたの番です");//手番情報を表示するためのラベルを作成
				tfTurn.setHorizontalAlignment(JTextField.CENTER);
				tfTurn.setEditable(false);
				tfTurn.setBounds(w/5-30, 3*h/4+50, row * 45 + 10, 40);//境界を設定
				this.add(tfTurn);//手番情報ラベルをパネルに追加
				//プレイヤ情報用ラベル
				labelPlayer1 = new JLabel("プレイヤ名 " + "レート");
				this.add(labelPlayer1);
				labelPlayer1.setBounds(6*w/11, h/13, 300, 40);
				//vsラベル
				labelVS = new JLabel("vs");
				this.add(labelVS);
				labelVS.setBounds(7*w/11, 2*h/13, 300, 40);
				//相手情報表示用ラベル
				labelPlayer2 = new JLabel("プレイヤ名 " + "レート");
				this.add(labelPlayer2);
				labelPlayer2.setBounds(6*w/11, 3*h/13, 300, 40);
				//残り時間表示用ラベル
				timer = new Timer(this);
				timer.addTo(this);
				timer.start();
				if(othello.getTurn() == -1) timer.stop(false);//先手ならタイマー始動
				//特殊効果発動是非表示用ラベル
				tfEffect = new JTextField("発動中特殊効果");
				tfEffect.setHorizontalAlignment(JTextField.CENTER);
				tfEffect.setEditable(false);
				this.add(tfEffect);
				tfEffect.setBounds(7*w/11, 6*h/13, 300 ,40);
			}
			//それ以外の時
			else {
				//残り時間表示用ラベル
				timer = new Timer(this);
				timer.addTo(this);
				timer.start();
				timer.stop(false);//タイマー始動
				//色表示用ラベル
				tfColor = new JTextField("あなたは黒です");
				tfColor.setHorizontalAlignment(JTextField.CENTER);
				tfColor.setEditable(false);
				tfColor.setBounds(w/5-30, 3*h/4+25, 160, 40);//境界を設定
				this.add(tfColor);//色表示用ラベルをパネルに追加
				//モード表示
				labelMode1 = new JLabel(mode.toUpperCase());
				labelMode1.setFont(new Font("PixelMplus10", Font.PLAIN, 50));
				labelMode1.setForeground(new Color(255,255,0));
				labelMode1.setHorizontalAlignment(JTextField.CENTER);
				labelMode1.setBounds(6*w/11-80, 3*h/26-20, 300, 50);
				labelMode2 = new JLabel("MODE");
				labelMode2.setFont(new Font("PixelMplus10", Font.PLAIN, 40));
				labelMode2.setForeground(new Color(255,255,0));
				labelMode2.setHorizontalAlignment(JTextField.CENTER);
				labelMode2.setBounds(6*w/11-80, 5*h/26-15, 300 ,50);
				this.add(labelMode1);
				this.add(labelMode2);
			}
			osd = new OptionSubDialog(client, "osd");//設定用ダイアログ作成
			osd.setLocation(client.w/6+350, client.h/6+200);
			osd.setVisible(false);
		}

		//メソッド
		public String getName() {//パネル名の取得
			return str;
		}

		public String getMode() {//モードの取得
			return mode;
		}

		public int[] getPreviousRate() {
			return previousRate;
		}

		class Timer extends Thread{ //タイマー用スレッド内部クラス
			int numMinute = 10;
			int numSecond = 0;
			JTextField tf;
			public boolean flagStop = false;
			boolean flagFinish = false;
			MatchingPanel panel;

			Timer(MatchingPanel p){
				panel = p;
				this.tf = new JTextField("残り時間  " + String.valueOf(numMinute) + "：0" + String.valueOf(numSecond));
				this.tf.setHorizontalAlignment(JTextField.CENTER);
				this.tf.setEditable(false);
			}

			public void run() {
				while(!this.flagFinish) {
					if(!this.flagStop) {
						if(String.valueOf(numSecond).length()<2) {
							this.tf.setText("残り時間  " + String.valueOf(numMinute) + "：0" + String.valueOf(numSecond));
							if(numSecond==0) {
								if(numMinute == 0) {
									finishMatching("timeup");
									break;
								}
								numMinute--;
								numSecond = 60;
							}
						}
						else this.tf.setText("残り時間  " + String.valueOf(numMinute) + "：" + String.valueOf(numSecond));
						numSecond--;
					}
					try {
						Thread.sleep(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
			void stop(boolean b) {
				this.flagStop = b;
			}
			void finish() {
				this.flagFinish = true;
			}
			void addTo(JPanel p) {
				p.add(this.tf);
				if(mode.equals("rank") || mode.equals("special"))
					this.tf.setBounds(7*w/11+50, 5*h/13, 150, 40);
				else
					this.tf.setBounds(7*w/11+60, 5*h/13-10, 150, 40);
			}
		}

		class Confirmer extends Thread{ //サーバに接続確認のための信号を送信するためのスレッド内部クラス
			Boolean flagFinish = false;

			public void run() {
				while(!this.flagFinish) {
					sendMessage("confirmation");
					try{
						Thread.sleep(5000);
					}
					catch(InterruptedException e) {}
				}
			}
			void finish() {
				this.flagFinish = true;
			}
		}

		public void finishMatching(String str) {//対局を終了(str:win, lose, draw, resign, timeup)
			timer.finish();
			//bgm1.stop();
			//bgm1.flush();
			if(mode.equals("rank") || mode.equals("special")) {//ネットワーク対局時
				confirmer.finish();
				previousRate[0] = player.getRecord()[4];
				previousRate[1] = opponent.getRecord()[4];
				if(mode.equals("rank")) {//ランクマッチ時
					if(!str.equals("draw"))
						player.getRecord()[4] = calculateRate(player.getRecord()[4], opponent.getRecord()[4], str);
					if(str.equals("win")) opponent.getRecord()[4] = calculateRate(opponent.getRecord()[4], previousRate[0], "lose");
					if(str.equals("lose")) opponent.getRecord()[4] = calculateRate(opponent.getRecord()[4], previousRate[0], "win");
				}
				sendMessage(str);
				if(!str.equals("draw"))
					sendMessage(String.valueOf(player.getRecord()[4]));
				sendMessage("end");
			}
			if(str.equals("timeup"))
				client.emsd = new EndMatchingSubDialog(client, "emsd", "lose");//対戦終了時ダイアログ作成
			else
				client.emsd = new EndMatchingSubDialog(client, "emsd", str);//対戦終了時ダイアログ作成
			emsd.setLocation(client.w/6+350, client.h/6+200);
			emsd.setVisible(true);
		}

		public int calculateRate(int rate1, int rate2, String result) { //レート計算
			if(result.equals("lose") || result.equals("timeup") || result.equals("resign")) {//敗北時
				return rate1 - (int)(16+0.04*(rate1-rate2));
			} else {//勝利時
				return rate1 + (int)(16+0.04*(rate2-rate1));
			}
		}

		public void acceptAction(int x, int y) {//ネットワーク対局相手からの操作の受付とそれに応じた処理
			if(!(x == 8 && y == 8)) //相手がコマを置けた場合
				othello.setStone(x, y);

			othello.checkPlaceable();
			updateDisp(x, y);
			if(!((winner = othello.checkWinner()) == 10)) {//勝敗がついた場合
				if(winner == -1){//黒の勝利
					if(player.getColor().equals("black")) finishMatching("win");
					else finishMatching("lose");
				}
				else finishMatching("draw");
			}
		}

		public void updateDisp(int x, int y){ // 画面を更新する
			if(x!=8 && y!=8 && x!=9 && y!=9) {//x==8とy==8は例外
				switch(othello.getTurn()) {
					case -1:
						if(player.getColor().equals("black"))
							taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
						else
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
						break;
					case 1:
						if(player.getColor().equals("white"))
							taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
						else
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
				}
				if(SE) {
					//se = createClip(new File("othello.wav"));
					//se.start();//SEを流す
				}
			}
			for(int j=0; j<row; j++) {
				int yy = 40 + j*38;
				for(int i=0; i<row; i++) {
					if(x!=9 && y!=9) this.remove(buttonArray[j][i]);
					if(othello.getGrids()[j][i] == 1){ buttonArray[j][i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
					if(othello.getGrids()[j][i] == -1){ buttonArray[j][i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
					if(othello.getGrids()[j][i] == 0){ buttonArray[j][i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
					if(othello.getGrids()[j][i] == 2){ //盤面状態に応じたアイコンを設定
						if(PLACABLE) buttonArray[j][i] = new JButton(placeableIcon);
						else buttonArray[j][i] = new JButton(boardIcon);
					}
					int xx = 40 + i*38;
					this.add(buttonArray[j][i]);
					buttonArray[j][i].setBounds(xx, yy, 45, 45);//ボタンの大きさと位置を設定する．
					buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
					buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
				}
			}
			if(x!=9 && y!=9) {
				tfBlackNumber.setText("黒　" + othello.getBlackstone() + "枚");
				tfWhiteNumber.setText("白　" + othello.getWhitestone() + "枚");
			}

		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			if(theButton == bResign) {//投了ボタンクリック時
				CheckWhetherToResignSubDialog cwtrsd = new CheckWhetherToResignSubDialog(client, "cwtrsd");//確認用ダイアログ作成
				cwtrsd.setLocation(client.w/6+430, client.h/6+250);
				cwtrsd.setVisible(true);
			} else if(theButton == bOption) {//設定ボタンクリック時
				osd.setVisible(true);
			} else {//オセロを操作をしたとき
				int x = Integer.parseInt(command) % 8;
				int y = Integer.parseInt(command) / 8;

				if((mode.equals("rank") || mode.equals("special")) && othello.getGrids()[y][x] == 2) {//ネットワーク対局時
					othello.setStone(x, y);
					//サーバに操作情報を送信
					updateDisp(x,y);

					if((othello.getTurn() == -1 && player.getColor().equals("black")) || (othello.getTurn() == 1 && player.getColor().equals("white"))) {
						tfTurn.setText("あなたの番です");
					} else {
						tfTurn.setText("相手の番です");
					}

					if(!((winner = othello.checkWinner()) == 10)) {//勝敗がついた場合
						if((winner == -1 && player.getColor().equals("black")) || (winner == 1 && player.getColor().equals("white")))
							finishMatching("win");
						else if(winner == 0)
							finishMatching("draw");
						else
							finishMatching("lose");
					}
				}
				else if (!mode.equals("rank") && !mode.equals("special") && othello.getGrids()[y][x] == 2){//ローカル対局時
					othello.setStone(x, y);//ターンが変わる
					updateDisp(x,y);

					if(!((winner = othello.checkWinner()) == 10)) {//勝敗がついた場合
						if(winner == -1) finishMatching("win");
						else if(winner == 0) finishMatching("draw");
						else finishMatching("lose");
					}

					if(othello.checkPlaceable()) {//コンピュータに打つ手がある場合
						do {
							try {
								Thread.sleep(400);
							}
							catch(InterruptedException ie) {}
							if(mode.equals("easy"))
								cpuOperation = cpu.random();
							else
								cpuOperation = cpu.think(othello.getGrids());
							othello.setStone(cpuOperation%10, (cpuOperation-cpuOperation%10)/10);
							updateDisp(cpuOperation%10,(cpuOperation-cpuOperation%10)/10);
							if(!((winner = othello.checkWinner()) == 10)) {//勝敗がついた場合
								if(winner == -1) finishMatching("win");
								else if(winner == 0) finishMatching("draw");
								else finishMatching("lose");
							}
							if(othello.checkPlaceable()) break;
							else othello.changeTurn();
						} while(othello.checkPlaceable());
					}
					else{//コンピュータに打つ手がない場合
						othello.changeTurn();
						othello.checkPlaceable();
					}
					updateDisp(8, 8);
				}

			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class OptionSubDialog extends JDialog implements ActionListener{//設定ダイアログ
		JLabel labelBGM;
		JLabel labelSE;
		JLabel labelPlacable;
		JRadioButton radioON1;
		JRadioButton radioON2;
		JRadioButton radioON3;
		JRadioButton radioOFF1;
		JRadioButton radioOFF2;
		JRadioButton radioOFF3;
		JButton bBack;
		Client client;
		String str;
		int w = 360;
		int h = 240;
		//コンストラクタ
		OptionSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			//BGM用ラベル
			labelBGM = new JLabel("BGM");
			this.add(labelBGM);
			labelBGM.setBounds(w/4-40, 2*h/7-30, 150, 40);
			//SE用ラベル
			labelSE = new JLabel("SE");
			this.add(labelSE);
			labelSE.setBounds(w/4-40, 3*h/7-30, 150, 40);
			//置ける場所表示用ラベル
			labelPlacable = new JLabel("置ける場所表示");
			this.add(labelPlacable);
			labelPlacable.setBounds(w/4-40, 4*h/7-30, 150, 40);
			//ONラジオボタン1
			radioON1 = new JRadioButton("ON",true);
			this.add(radioON1);
			radioON1.setBounds(w/2, 2*h/7-30, 100, 40);
			radioON1.addActionListener(this);//マウス操作を認識できるようにする
			radioON1.setActionCommand("on1");//ボタンを識別するための名前を付加する
			//ONラジオボタン2
			radioON2 = new JRadioButton("ON",true);
			this.add(radioON2);
			radioON2.setBounds(w/2, 3*h/7-30, 100, 40);
			radioON2.addActionListener(this);//マウス操作を認識できるようにする
			radioON2.setActionCommand("on2");//ボタンを識別するための名前を付加する
			//ONラジオボタン3
			radioON3 = new JRadioButton("ON",true);
			this.add(radioON3);
			radioON3.setBounds(w/2, 4*h/7-30, 100, 40);
			radioON3.addActionListener(this);//マウス操作を認識できるようにする
			radioON3.setActionCommand("on3");//ボタンを識別するための名前を付加する
			//OFFラジオボタン1
			radioOFF1 = new JRadioButton("OFF",false);
			this.add(radioOFF1);
			radioOFF1.setBounds(7*w/10, 2*h/7-30, 100, 40);
			radioOFF1.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF1.setActionCommand("off1");//ボタンを識別するための名前を付加する
			//OFFラジオボタン2
			radioOFF2 = new JRadioButton("OFF",false);
			this.add(radioOFF2);
			radioOFF2.setBounds(7*w/10, 3*h/7-30, 100, 40);
			radioOFF2.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF2.setActionCommand("off2");//ボタンを識別するための名前を付加する
			//OFFラジオボタン3
			radioOFF3 = new JRadioButton("OFF",false);
			this.add(radioOFF3);
			radioOFF3.setBounds(7*w/10, 4*h/7-30, 100, 40);
			radioOFF3.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF3.setActionCommand("off3");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(3*w/7-15, 6*h/7-20, 90, 24);
			bBack.addActionListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bBack)
				setVisible(false);
			else if(e.getSource() == radioON1) {//BGMのONボタン
				if(radioON1.isSelected() == false) {

				}
				radioON1.setSelected(true);
				radioOFF1.setSelected(false);
				//bgm1.loop(Clip.LOOP_CONTINUOUSLY);
			}
			else if(e.getSource() == radioOFF1) {//BGMのOFFボタン
				if(radioOFF1.isSelected() == false) {

				}
				radioON1.setSelected(false);
				radioOFF1.setSelected(true);
				//bgm1.stop();
			}
			else if(e.getSource() == radioON2) {//SEのONボタン
				client.SE = true;
				radioON2.setSelected(true);
				radioOFF2.setSelected(false);
			}
			else if(e.getSource() == radioOFF2) {//SEのOFFボタン
				client.SE = false;
				radioON2.setSelected(false);
				radioOFF2.setSelected(true);
			}
			else if(e.getSource() == radioON3) {//置ける場所表示のONボタン
				client.PLACABLE = true;
				radioON3.setSelected(true);
				radioOFF3.setSelected(false);
				mp.updateDisp(8, 8);
			}
			else if(e.getSource() == radioOFF3) {//置ける場所表示のOFFボタン
				client.PLACABLE = false;
				radioON3.setSelected(false);
				radioOFF3.setSelected(true);
				mp.updateDisp(8, 8);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class CheckWhetherToResignSubDialog extends JDialog implements ActionListener{//投了確認時ダイアログ
		JLabel labelMessage;//メッセージラベル
		JButton bOK;//OKボタン
		JButton bCan;//キャンセルボタン
		Client client;
		String str;
		int w = 360;
		int h = 240;
		//コンストラクタ
		CheckWhetherToResignSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			//メッセージラベル
			labelMessage = new JLabel("本当に投了しますか?");
			this.add(labelMessage);
			labelMessage.setBounds(w/3, 2*h/5-20, w/2, h/10);
			//OKボタン
			bOK = new JButton("OK");
			this.add(bOK);
			bOK.setBounds(w*3/15, 6*h/8, w/4, h/10);
			bOK.addActionListener(this);//マウス操作を認識できるようにする
			bOK.setActionCommand("ok");//ボタンを識別するための名前を付加する
			//Cancelボタン
			bCan = new JButton("キャンセル");
			this.add(bCan);
			bCan.setBounds(8*w/15, 6*h/8, w/4, h/10);
			bCan.addActionListener(this);//マウス操作を認識できるようにする
			bCan.setActionCommand("cancel");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bOK) //OKボタンクリック時
				mp.finishMatching("resign");
			dispose();//このダイアログを破棄
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class TotalizingSubDialog extends JDialog implements ActionListener{//再戦希望集計中パネル
		JTextField tfMessage;//メッセージラベル
		JButton bCan;//キャンセルボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;
		boolean cancelFlag;
		//コンストラクタ
		TotalizingSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//メッセージ用ラベル
			tfMessage = new JTextField("再戦希望を集計中です...");
			this.add(tfMessage);
			tfMessage.setBounds(w*3/8, 5*h/11, 300, 40);
			//キャンセルボタン
			bCan = new JButton("キャンセル");
			this.add(bCan);
			bCan.setBounds(22*w/56, 8*h/11, 120, 40);
			bCan.addActionListener(this);//マウス操作を認識できるようにする
			bCan.setActionCommand("cancel");//ボタンを識別するための名前を付加する
			//再戦を希望する
			tryToRematch();
		}
		//メソッド
		public void tryToRematch() {
			sendMessage("hopeToRematch");
			sendMessage("end");

			client.flag = false;
			client.result = false;
			cancelFlag = false;

			while(!cancelFlag) {
				try {
					if(client.flag) {
						if(client.result) {
							cont.remove(mp);
							client.mp = new MatchingPanel(client, "mp", client.mp.getMode());
							client.mp.setVisible(true);
							cont.add(mp);
							dispose();
						} else {
							tfMessage.setText("相手が再戦を希望しませんでした");
							Thread.sleep(5000);
							client.emsd.setVisible(true);
							dispose();
						}
						break;
					}
					Thread.sleep(1000);
				}
				catch(InterruptedException e) {}
			}
		}

		public void actionPerformed(ActionEvent e) {//マウスクリック時の処理
			cancelFlag = true;
			dispose();//このダイアログを破棄
			client.emsd.setVisible(true);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class EndMatchingSubDialog extends JDialog implements ActionListener{//対局終了時ダイアログ
		JLabel labelResult;//結果表示ラベル
		JLabel labelPlayer1;//自分のプレイヤ名
		JLabel labelRate1;//自分のレート
		JLabel labelPlayer2;//相手のプレイヤ名
		JLabel labelRate2;//相手のレート
		JButton bRematch;//再戦ボタン
		JButton bBack;//戻るボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;
		//コンストラクタ
		EndMatchingSubDialog(Client c, String s, String result){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(243,152,0));
			//結果用ラベル
			if(result.equals("win")) {
				labelResult = new JLabel("YOU WON!");
				labelResult.setForeground(Color.RED);
			}
			else if(result.equals("lose") || result.equals("resign")) {
				labelResult = new JLabel("YOU LOST!");
				labelResult.setForeground(Color.BLUE);
			}
			else {
				labelResult = new JLabel("DRAW!");
				labelResult.setForeground(new Color(130, 0, 170));
			}
			this.add(labelResult);
			labelResult.setBounds(1*w/5, h/7, 300, 50);
			labelResult.setHorizontalAlignment(JTextField.CENTER);
			labelResult.setFont(new Font("PixelMplus10", Font.PLAIN, 60));

			if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) {//ネットワーク対戦時
				if(player.getColor().equals("black")) {
					labelPlayer1 = new JLabel(player.getName() + " ● " + othello.getBlackstone());
					labelPlayer2 = new JLabel(opponent.getName() + " ● " + othello.getWhitestone());
				}
				else {
					labelPlayer1 = new JLabel(opponent.getName() + " ● " + othello.getWhitestone());
					labelPlayer2 = new JLabel(player.getName() + " ● " + othello.getBlackstone());
				}
				//プレイヤ1情報用ラベル
				labelPlayer1.setBounds(2*w/7, 2*h/7, 300, 30);
				this.add(labelPlayer1);
				//プレイヤ1のレート用ラベル
				labelRate1 = new JLabel(player.getRecord()[4] + "(" + (mp.getPreviousRate()[0] - player.getRecord()[4]) + ")*");//ここでcalculateRate()メソッドを使う
				this.add(labelRate1);
				labelRate1.setBounds(2*w/7, 3*h/7, 300, 30);
				//プレイヤ2情報用ラベル
				labelPlayer2.setBounds(2*w/7, 4*h/7, 300, 30);
				this.add(labelPlayer2);
				//プレイヤ2のレート用ラベル
				labelRate2 = new JLabel(opponent.getRecord()[4] + "(" + (mp.getPreviousRate()[1] - opponent.getRecord()[4]) + ")*");//ここでcalculateRate()メソッドを使う
				this.add(labelRate2);
				labelRate2.setBounds(2*w/7, 5*h/7, 300, 30);
			}
			else {//ローカル対戦時
				//プレイヤ1情報用ラベル
				labelPlayer1 = new JLabel("player" + " ● " + othello.getBlackstone());
				labelPlayer1.setBounds(2*w/7+30, 2*h/7+40, 300, 30);
				this.add(labelPlayer1);
				//プレイヤ2情報用ラベル
				labelPlayer2 = new JLabel("   cpu" + " ● " + othello.getWhitestone());
				labelPlayer2.setBounds(2*w/7+30, 4*h/7, 300, 30);
				this.add(labelPlayer2);
			}

			labelPlayer1.setFont(new Font("PixelMplus10", Font.PLAIN, 20));
			labelPlayer2.setFont(new Font("PixelMplus10", Font.PLAIN, 20));
			labelPlayer2.setForeground(Color.WHITE);
			//再戦希望ボタン
			bRematch = new JButton("再戦");
			this.add(bRematch);
			bRematch.setBounds(1*w/5+30, 6*h/7-20, 90, 24);
			bRematch.addActionListener(this);//マウス操作を認識できるようにする
			bRematch.setActionCommand("rematch");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(3*w/5-20, 6*h/7-20, 90, 24);
			bBack.addActionListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bRematch) {//再戦ボタンクリック時
				this.setVisible(false);//このダイアログを隠す
				if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) {//ネットワーク対局なら
					TotalizingSubDialog tsd = new TotalizingSubDialog(client, "tsd");//再戦希望集計時ダイアログの作成
					tsd.setLocation(client.w/6, client.h/6);
					tsd.setVisible(true);
				}
				else {//ローカル対局なら
					client.mp.setVisible(false);

					if(client.mp.getMode().equals("easy"))
						client.mp = new MatchingPanel(client, "mp", "easy");
					else if(client.mp.getMode().equals("normal"))
						client.mp = new MatchingPanel(client, "mp", "normal");
					else
						client.mp = new MatchingPanel(client, "mp", "hard");

					client.cont.add(client.mp, BorderLayout.CENTER);
					client.mp.setVisible(true);
				}
			} else {//戻るボタンクリック時
				dispose();//破棄
				client.mp.setVisible(false);//対局パネルの非表示

				if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) //ネットワーク対局なら
					client.PanelChange((JPanel)client.mp, (JPanel)client.mfnmp);//ネットワーク対局用メニュー画面へ遷移
				else //ローカル対局なら
					client.PanelChange((JPanel)client.mp, (JPanel)client.clp);//レベル選択画面へ遷移
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}


	// コンストラクタ
	public Client() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("");
		setSize(w,h);

		cont = getContentPane();
		cont.add(mnp, BorderLayout.CENTER);

		mnp.setVisible(true);
	}

	//メソッド
	public void PanelChange(JPanel jpF, JPanel jpT) {//画面遷移関数（画面をjpFからjpTへ遷移）
		//遷移元
		if(jpF.getName().equals("mnp")) {
			cont.remove(mnp);
		} else if (jpF.getName().equals("otlp")) {
			cont.remove(otlp);
		} else if (jpF.getName().equals("mfnmp")) {
			cont.remove(mfnmp);
		} else if (jpF.getName().equals("rrp")) {
			cont.remove(rrp);
		} else if (jpF.getName().equals("mp")) {
			cont.remove(mp);
		} else if (jpF.getName().equals("clp")) {
			cont.remove(clp);
		}
		//遷移先
		if(jpT.getName().equals("clp")) {
			cont.add(clp, BorderLayout.CENTER);
			clp.setVisible(true);
		} else if (jpT.getName().equals("otlp")){
			cont.add(otlp, BorderLayout.CENTER);
			otlp.setVisible(true);
		} else if(jpT.getName().equals("mnp")) {
			cont.add(mnp, BorderLayout.CENTER);
			mnp.setVisible(true);
		} else if(jpT.getName().equals("mfnmp")) {
			cont.add(mfnmp, BorderLayout.CENTER);
			mfnmp.setVisible(true);
		} else if(jpT.getName().equals("rrp")) {
			cont.add(rrp, BorderLayout.CENTER);
			rrp.setVisible(true);
		} else if(jpT.getName().equals("sfop")) {
			cont.add(sfop, BorderLayout.CENTER);
			sfop.setVisible(true);
		} else if(jpT.getName().equals("mp")) {
			cont.add(mp, BorderLayout.CENTER);
			mp.setVisible(true);
		}
	}

	public static Clip createClip(File path) {//SEの挿入
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(path)){//指定されたURLのオーディオ入力ストリームを取得
			AudioFormat af = ais.getFormat();//ファイルの形式取得
			DataLine.Info dataLine = new DataLine.Info(Clip.class,af);//単一のオーディオ形式を含む指定した情報からデータラインの情報オブジェクトを構築
			Clip c = (Clip)AudioSystem.getLine(dataLine);//指定された Line.Info オブジェクトの記述に一致するラインを取得
			c.open(ais);//再生準備完了

			return c;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void connectServer(String ipAddress, int port){	// サーバに接続
		socket = null;
		try {
			socket = new Socket(ipAddress, port); //サーバ(ipAddress, port)に接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
			receiver = new Receiver(socket); //受信用オブジェクトの準備
			receiver.start();//受信用オブジェクト(スレッド)起動
		} catch (UnknownHostException e) {
			System.err.println("ホストのIPアドレスが判定できません: " + e);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("サーバ接続時にエラーが発生しました: " + e);
			System.exit(-1);
		}
	}

	public void sendMessage(String msg){	// サーバに情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
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
					if(inputLine.equals("sendAccountInfo")) {//目的がアカウント情報の送信だった場合
						int i=0;
						String accountInfo[] = new String[3];//アカウント情報用配列
						receiveMessage(inputLine);//データ受信用メソッドを呼び出す
						while(true) {//データを受信し続ける
							inputLine = br.readLine();//１行読み込む
							if(!inputLine.equals("end")) {//終了サインじゃなかったら
								accountInfo[i++] = inputLine;//アカウント情報用配列に格納
							}
							else {
								player = new Player(accountInfo[0], accountInfo[1], accountInfo[2]);
								flag = true;
								break;
							}
						}
					}
					else if(inputLine.equals("sendLoginResult")) {//目的がログイン認証結果の送信だった場合
						while(true) {
							inputLine = br.readLine();
							if(inputLine.equals("succeeded")) {
								result = true;
							} else if(inputLine.equals("failed")){
								result = false;
							} else {
								flag = true;
								break;
							}
						}
					}
					else if(inputLine.equals("sendFindingResult")) {//目的が対戦相手のマッチング結果の送信の場合
						inputLine = br.readLine();
						if(inputLine.equals("succeeded")) {//対戦相手が見つかったら
							result = true;
							int i=0;
							String matchingInfo[] = new String[3];//対局情報用配列
							while(true) {
								inputLine = br.readLine();
								if(!inputLine.equals("end")) {//endじゃない限り
									matchingInfo[i++] = inputLine;//対局情報用配列に格納
								} else {
									//Client.opponent = new Player(matchingInfo[0], matchingInfo[1], matchingInfo[2]);
									//Client.player.setColor(matchingInfo[4]);//先手後手情報の設定
									break;
								}
							}
						}
						else if(inputLine.equals("failed")) {//対戦相手が見つからなかったら
							result = false;
						}
						flag = true;
					}
					else if(inputLine.equals("sendRematchingResult")) {//目的が再戦集計結果の送信だったら
						inputLine = br.readLine();
						if(inputLine.equals("succeeded")) result = true;
						else result = false;
						flag = true;
					}
					Thread.sleep(500);
				}
				catch (IOException e){
					System.err.println("データ受信時にエラーが発生しました: " + e);
				} catch(InterruptedException e) {
					System.err.println("データ受信時にエラーが発生しました: " + e);
				}
			}
		}
	}

	public void receiveMessage(String msg){	// メッセージの受信
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
	}


	//main
	public static void main(String args[]){
		Client oclient = new Client();
		oclient.setVisible(true);
		oclient.setLocation(350, 200);
		oclient.setResizable(false);
		try{
			font = Font.createFont(Font.TRUETYPE_FONT,new File("PixelMplus10-Regular.ttf"));
		}catch(FontFormatException e){
			System.out.println("形式がフォントではありません。");
		}catch(IOException e){
			System.out.println("入出力エラーでフォントを読み込むことができませんでした。");
		}

		//oclient.connectServer("localhost", 10000);
	}
}