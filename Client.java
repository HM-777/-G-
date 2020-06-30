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

public class Client extends JFrame {
	//private StartPanel sp = new StartPanel(this, "sp");
	private MenuPanel mnp = new MenuPanel(this, "mnp");//その他パネルはボタンクリック時に作成
	private ChooseLevelPanel clp;//難易度選択パネル
	private OptionToLoginPanel otlp;//アカウント作成またはログイン画面用パネル
	private MenuForNetworkMatchingPanel mfnmp;//ネットワーク対局用メニュー画面パネル
	private ReadRecordPanel rrp;//戦績ランキング閲覧パネル
	private SearchingForOpponentPanel sfop;//マッチングパネル
	private MatchingPanel mp;//対局パネル
	//private Player player;//プレイヤ
	//private Player opponent;
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
	static Clip bgm1= createClip(new File("RapGod.wav"));
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
			title2.setBounds(w/5-15, h/8, 600, 200);
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
		//Othello othello;
		int row = 8;//オセロ盤の縦横マスの数
		int grids[][];
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
		JTextField tfEffect;//特殊効果の表示ラベル
		JTextArea taLog;//ログ
		ImageIcon blackIcon, whiteIcon, boardIcon; //アイコン
		Clip se = createClip(new File("othello.wav"));//SE
		boolean flagWait;
		int previousRate1;
		int previousRate2;
		CheckWhetherToResignSubDialog cwtrsd;
		int w = 750;
		int h = 500;
		//コンストラクタ
		MatchingPanel(Client c, String s, String m){//modeはnetwork, easy, normal, hardのいずれか
			client = c;
			str = s;
			mode = m;
			bgm1.loop(Clip.LOOP_CONTINUOUSLY);
			//テスト用に局面情報を初期化
			grids = new int[row][row];
			for (int x=0; x<row; x++) {
				for (int y=0; y<row; y++) {
					grids[y][x] = 0;
				}
			}
			grids[3][3] = -1;
			grids[4][4] = -1;
			grids[3][4] = 1;
			grids[4][3] = 1;
			
			//ウィンドウ設定
			this.setSize(w, h);//ウィンドウのサイズを設定
			this.setLayout(null);
			//アイコン設定(画像ファイルをアイコンとして使う)
			whiteIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/White.jpg");
			blackIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/Black.jpg");
			boardIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/GreenFrame.jpg");
			//オセロ盤の生成
			buttonArray = new JButton[row][row];//ボタンの配列を作成
			for(int j=0; j<row; j++) {
				int y = 40 + j*38;
				for(int i=0; i<row; i++) {
					if(grids[j][i] == 1){ buttonArray[j][i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
					if(grids[j][i] == -1){ buttonArray[j][i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
					if(grids[j][i] == 0){ buttonArray[j][i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
					//ボタンを配置する
					int x = 40 + i*38;
					this.add(buttonArray[j][i]);
					buttonArray[j][i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
					buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
					buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
				}
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
			tfBlackNumber = new JTextField("　黒　" + "2" + "枚");
			this.add(tfBlackNumber);
			tfBlackNumber.setBounds(6*w/11,  5*h/13-20, 80, 40);
			//白駒の数用ラベル
			tfWhiteNumber = new JTextField("　白　" + "2" + "枚");
			this.add(tfWhiteNumber);
			tfWhiteNumber.setBounds(6*w/11, 6*h/13-20, 80, 40);
			//戦況ログ用テキストエリア
			taLog = new JTextArea(330, 200);
			this.add(taLog);
			taLog.setBounds(6*w/11-20, 7*h/13-15, 330, 200);
			//modeがネットワーク対戦であるとき
			if(mode.equals("rank") || mode.equals("special")) {
				//色表示用ラベル
				tfColor = new JTextField("あなたは"+"黒です");//色情報を表示するためのラベルを作成
				tfColor.setBounds(w/5-30, 3*h/4, 160, 40);//境界を設定
				this.add(tfColor);//色表示用ラベルをパネルに追加
				//手番表示用ラベル
				tfTurn = new JTextField("あなたの番です");//手番情報を表示するためのラベルを作成
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
				/*
				timer = new Timer(this);
				timer.addTo(this);
				timer.start();
				*/
				labelTimer = new JLabel("**:**");
				this.add(labelTimer);
				labelTimer.setBounds(7*w/11, 5*h/13, 300, 40);
				//特殊効果発動是非表示用ラベル
				tfEffect = new JTextField("発動中特殊効果");
				this.add(tfEffect);
				tfEffect.setBounds(7*w/11, 6*h/13, 300 ,40);
				//手番確認とそれに応じた処理
				/*
				 if(othello.getTurn){//手番が自分
				 	timer.stop(false);//タイマー始動
				 	flagWait = false;
				 }
				 else
				 	flagWait = true;
				*/
			}
			//それ以外の時
			else {
				//色表示用ラベル
				tfColor = new JTextField("　　あなたは黒です");
				tfColor.setBounds(w/5-30, 3*h/4+10, 160, 40);//境界を設定
				this.add(tfColor);//色表示用ラベルをパネルに追加
				//モード表示
				labelMode1 = new JLabel(mode);
				labelMode1.setBounds(6*w/11, 3*h/26, 300, 40);
				labelMode2 = new JLabel("モード");
				labelMode2.setBounds(6*w/11, 5*h/26, 300 ,40);
				this.add(labelMode1);
				this.add(labelMode2);
			}
		}

		//メソッド
		public String getName() {//パネル名の取得
			return str;
		}

		public String getMode() {//モードの取得
			return mode;
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
				tf = new JTextField(String.valueOf(numMinute) + "：" + String.valueOf(numSecond));
			}

			public void run(String s) {
				while(!this.flagFinish) {
					if(!this.flagStop) {
						if(String.valueOf((numSecond)%60).length()<2) {
							tf.setText(String.valueOf(numMinute) + "：0" + String.valueOf(numSecond));
							if(numSecond==0) {
								if(numMinute == 0) {
									//finishMatching("timeup");
									break;
								}
								numMinute--;
								numSecond = 60;
							}
						}
						else tf.setText(String.valueOf(numMinute) + "：" + String.valueOf(numSecond));
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
			}
		}

		class Confirmer extends Thread{ //サーバに接続確認のための信号を送信するためのスレッド内部クラス
			Boolean flagFinish = false;

			public void run() {
				while(!this.flagFinish) {
					sendMessage("confirmation");
					try{
						Thread.sleep(50000);
					}
					catch(InterruptedException e) {}
				}
			}
			void finish() {
				this.flagFinish = true;
			}
		}

		public void finishMatching(String str) {//対局を終了
			if(mode.equals("rank")) {
				
			} 
			else if(mode.equals("special")) {
				
			}
			else {
				
			}
			/*
			timer.finish();
			waiter.finish();
			confirmaer.finish();
			previousRate1 = player.rate;
			previousRate2 = opponent.rate;
			player.rate = calculateRate(player.rate, opponent.rate, str);
			if(str.equals("win")) opponent.rate = calculateRate(opponent.rate, work, "lose");
			else opponent.rate = calculateRate(opponent.rate, work, "win");
			client.emsd = new EndMatchingSubDialog(client, "emsd");//対戦終了時ダイアログ作成
			emsd.setLocation(client.w/6, client.h/6);
			emsd.setVisible(true);
			*/
			sendMessage("endMatching");
			sendMessage(str);//"win"または"lose"または"resign"
			//sendMessage(player.rate);//更新されたrateを送る
			sendMessage("end");
		}

		public int calculateRate(int rate1, int rate2, int result) { //レート計算
			if(result == 0) {//敗北時
				return rate1 - (int)(16+0.04*(rate1-rate2));
			} else {//勝利時
				return rate1 + (int)(16+0.04*(rate2-rate1));
			}
		}

		public void updateDisp(int x, int y){ // 画面を更新する
			//更新
			taLog.append("(" + String.valueOf(x+1) + "," + String.valueOf(y+1) + ") ");
			se.start();//SEを流す
			flagWait = false;
		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			if(theButton == bResign) {//投了ボタンクリック時
				CheckWhetherToResignSubDialog cwtrsd = new CheckWhetherToResignSubDialog(client, "cwtrsd");//確認用ダイアログ作成
				cwtrsd.setLocation(w/4,h/4);
				cwtrsd.setVisible(true);
			} else if(theButton == bOption) {//設定ボタンクリック時
				OptionSubDialog osd = new OptionSubDialog(client, "osd");//設定用ダイアログ作成
				osd.setLocation(w/6, h/6);
				osd.setVisible(true);
			} else {//オセロを操作をしたとき
				int x = Integer.parseInt(command) % 8;
				int y = Integer.parseInt(command) / 8;
				if((mode.equals("rank") || mode.equals("rank")) && !flagWait) {
					updateDisp(x,y);
					flagWait = true;
				} else if (!mode.equals("rank") && !mode.equals("special")){

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
		int w = 480;
		int h = 320;
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
			labelBGM.setBounds(w/4, 2*h/7, 150, 40);
			//SE用ラベル
			labelSE = new JLabel("SE");
			this.add(labelSE);
			labelSE.setBounds(w/4, 3*h/7, 150, 40);
			//置ける場所表示用ラベル
			labelPlacable = new JLabel("置ける場所表示");
			this.add(labelPlacable);
			labelPlacable.setBounds(w/4, 4*h/7, 150, 40);
			//ONラジオボタン1
			radioON1 = new JRadioButton("ON",true);
			this.add(radioON1);
			radioON1.setBounds(w/2, 2*h/7, 100, 40);
			radioON1.addActionListener(this);//マウス操作を認識できるようにする
			radioON1.setActionCommand("on1");//ボタンを識別するための名前を付加する
			//ONラジオボタン2
			radioON2 = new JRadioButton("ON",true);
			this.add(radioON2);
			radioON2.setBounds(w/2, 3*h/7, 100, 40);
			radioON2.addActionListener(this);//マウス操作を認識できるようにする
			radioON2.setActionCommand("on2");//ボタンを識別するための名前を付加する
			//ONラジオボタン3
			radioON3 = new JRadioButton("ON",true);
			this.add(radioON3);
			radioON3.setBounds(w/2, 4*h/7, 100, 40);
			radioON3.addActionListener(this);//マウス操作を認識できるようにする
			radioON3.setActionCommand("on3");//ボタンを識別するための名前を付加する
			//OFFラジオボタン1
			radioOFF1 = new JRadioButton("OFF",false);
			this.add(radioOFF1);
			radioOFF1.setBounds(7*w/10, 2*h/7, 100, 40);
			radioOFF1.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF1.setActionCommand("off1");//ボタンを識別するための名前を付加する
			//OFFラジオボタン2
			radioOFF2 = new JRadioButton("OFF",false);
			this.add(radioOFF2);
			radioOFF2.setBounds(7*w/10, 3*h/7, 100, 40);
			radioOFF2.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF2.setActionCommand("off2");//ボタンを識別するための名前を付加する
			//OFFラジオボタン3
			radioOFF3 = new JRadioButton("OFF",false);
			this.add(radioOFF3);
			radioOFF3.setBounds(7*w/10, 4*h/7, 100, 40);
			radioOFF3.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF3.setActionCommand("off3");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(3*w/7, 6*h/7, 120, 40);
			bBack.addActionListener(this);//マウス操作を認識できるようにする
			bBack.setActionCommand("back");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bBack)
				dispose();
			else if(e.getSource() == radioON1) {//BGMのONボタン
				if(radioON1.isSelected() == false) {
					radioON1.setSelected(true);
					radioOFF1.setSelected(false);
					bgm1.loop(Clip.LOOP_CONTINUOUSLY);
				}
			}
			else if(e.getSource() == radioOFF1) {//BGMのOFFボタン
				if(radioOFF2.isSelected() == false) {
					radioON1.setSelected(false);
					radioOFF1.setSelected(true);
					bgm1.stop();
				}
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
			}
			else if(e.getSource() == radioOFF3) {//置ける場所表示のOFFボタン
				client.PLACABLE = false;
				radioON3.setSelected(false);
				radioOFF3.setSelected(true);
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
			labelMessage.setBounds(w/3, 2*h/5, w/2, h/10);
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
			dispose();//このダイアログを破棄
			if(e.getSource() == bOK) {//OKボタンクリック時
				//client.mp.endMatching("lose");
				//テスト処理（本当はいらない）
				EndMatchingSubDialog emsd = new EndMatchingSubDialog(client, "emsd", "lose");//対戦終了時ダイアログ作成
				emsd.setLocation(client.w/6, client.h/6);
				emsd.setVisible(true);

			}
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
			//結果用ラベル
			if(result.equals("win"))
				labelResult = new JLabel("YOU WIN!");
			else
				labelResult = new JLabel("YOU LOST!");
			this.add(labelResult);
			labelResult.setBounds(2*w/5, h/7, 300, 50);
			//プレイヤ1情報用ラベル
			labelPlayer1 = new JLabel("player1" + " ○ " + "43");
			this.add(labelPlayer1);
			labelPlayer1.setBounds(2*w/7, 2*h/7, 300, 30);

			if(client.mp.getMode().equals("network")) {//ネットワーク対戦時
				//プレイヤ1のレート用ラベル
				labelRate1 = new JLabel("1664" + "(+" + "63" + ")*");//ここでcalculateRate()メソッドを使う
				this.add(labelRate1);
				labelRate1.setBounds(2*w/7, 3*h/7, 300, 30);
				//プレイヤ2情報用ラベル
				labelPlayer2 = new JLabel("player2" + " ● " + "21");
				this.add(labelPlayer2);
				labelPlayer2.setBounds(2*w/7, 4*h/7, 300, 30);
				//プレイヤ2のレート用ラベル
				labelRate2 = new JLabel("1435" + "(-" + "63" + ")*");//ここでcalculateRate()メソッドを使う
				this.add(labelRate2);
				labelRate2.setBounds(2*w/7, 5*h/7, 300, 30);
			}
			else {//ローカル対戦時
				labelPlayer2 = new JLabel("computer" + "●" + "21");
				this.add(labelPlayer2);
				labelPlayer2.setBounds(2*w/7, 4*h/7, 300, 30);
			}

			//再戦希望ボタン
			bRematch = new JButton("再戦");
			this.add(bRematch);
			bRematch.setBounds(1*w/5, 6*h/7, 120, 30);
			bRematch.addActionListener(this);//マウス操作を認識できるようにする
			bRematch.setActionCommand("rematch");//ボタンを識別するための名前を付加する
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(3*w/5, 6*h/7, 120, 30);
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
				if(client.mp.getMode().equals("network")) {//ネットワーク対局なら
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

				if(client.mp.getMode().equals("network")) //ネットワーク対局なら
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
								//Client.player = new Player(accountInfo[0], accountInfo[1], accountInfo[2]);
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