//パッケージのインポート
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
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
import javax.swing.border.LineBorder;

public class Client extends JFrame implements WindowListener{
	private MenuPanel mnp = new MenuPanel(this, "mnp");//その他パネルはボタンクリック時に作成
	private OptionSubDialog osd;//設定用ダイアログ作成
	private ChooseLevelPanel clp;//難易度選択パネル
	private OptionToLoginPanel otlp;//アカウント作成またはログイン画面用パネル
	private MenuForNetworkMatchingPanel mfnmp;//ネットワーク対局用メニュー画面パネル
	private ReadRecordPanel rrp;//戦績ランキング閲覧パネル
	private SearchingForOpponentPanel sfop;//マッチングパネル
	private MatchingPanel mp;//対局パネル
	private Player player;//プレイヤ
	private Player opponent;//相手
	private Othello othello;
	private EndMatchingSubDialog emsd;
	private Container cont; // コンテナ
	private PrintWriter out;//データ送信用オブジェクト
	private Socket socket;
	private Receiver receiver; //データ受信用オブジェクト
	private int w = 750;
	private int h = 500;
	private boolean flag;//通信時に使用
	private boolean result;//通信時に使用
	private boolean specialFlag;//スペシャルマッチか否か
	private int specialNumber;
	private boolean BGM = true;
	private boolean SE = true;
	private boolean PLACABLE = true;
	static Clip bgm1= createClip(new File("宮田大翔 - Irreversible (original).wav"));
	static Font font;

	//パネル・ダイアログ（内部クラス）
	class MenuPanel extends JPanel implements MouseListener{//スタート画面用パネル
		JLabel background;//背景用ラベル
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

			//ネットワーク対局選択用ボタン
			bN = new JButton("ネットワーク対局");
			this.add(bN);
			bN.setBackground(Color.BLACK);
			bN.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bN.setBounds(w/3+30, h/2+60, 200, 40);
			bN.addMouseListener(this);
			bN.setActionCommand("ネットワーク対局");
			//ローカル対局選択用ボタン
			bL = new JButton("ローカル対局");
			this.add(bL);
			bL.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bL.setBounds(w/3+30, h/2+120, 200, 40);
			bL.addMouseListener(this);
			bL.setActionCommand("ローカル対局");
			//背景の設定
			background = new JLabel(new ImageIcon(new File("title.gif").getAbsolutePath()));
			this.add(background);
			background.setBounds(0,0,w,h);
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bN) {//ネットワーク対局ボタンクリック時
				ConnectSubDialog csd = new ConnectSubDialog(client, "csd");
				csd.setVisible(true);
				csd.setLocation(w/6+370, h/6+200);
			} else {//ローカル対局ボタンクリック時
				this.setVisible(false);
				client.clp = new ChooseLevelPanel(client, "clp");
				client.PanelChange((JPanel)this, (JPanel)client.clp);
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

	class ConnectSubDialog extends JDialog implements MouseListener {
		JButton bOK, bCan;//OKボタン、キャンセルボタン
		JLabel message;//メッセージラベル
		JLabel error;//エラーラベル
		JTextField ip;//ipアドレス入力テキストフィールド
		Client client;
		String str;
		int w = 480;
		int h = 320;

		ConnectSubDialog(Client c, String s){
			client = c;
			str = s;
			setSize(w,h);
			setTitle("サーバに接続");
			setLayout(null);
			setResizable(false);
			setModalityType(ModalityType.APPLICATION_MODAL);
			setLocation(client.w/6+370, client.h/6+200);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(255,240,197));
			//メッセージラベル
			message = new JLabel("IPアドレスを入力してください");
			message.setForeground(new Color(255,0,0));
			message.setFont(new Font("PixelMplus10", Font.PLAIN, 14));
			this.add(message);
			message.setBounds(w/4-25, 2*h/9, 300, 40);
			//IPアドレス入力用テキストフィールド
			ip = new JTextField(30);
			ip.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(ip);
			ip.setBounds(w/4-25, 3*h/9+10, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			bOK.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(bOK);
			bOK.setBounds(w/3-20, 13*h/18, 90, 30);
			bOK.addMouseListener(this);
			bOK.setActionCommand("OK");
			//キャンセルボタン
			bCan = new JButton("キャンセル");
			bCan.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			this.add(bCan);
			bCan.setBounds(7*w/12-20, 13*h/18, 90, 30);
			bCan.addMouseListener(this);
			bCan.setActionCommand("キャンセル");
		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bOK) {//OKボタンクリック時
				if(connectServer(ip.getText(), 12345)) {//正常処理
					client.otlp = new OptionToLoginPanel(client, "otlp");
					client.mnp.setVisible(false);
					client.PanelChange((JPanel)mnp, (JPanel)client.otlp);
					dispose();
				} else {//エラー時
					System.out.println("接続に失敗しました。");
					error = new JLabel("接続に失敗しました");
					error.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
					this.add(error);
					error.setBounds(w/4+80, 5*h/9-10, 300, 40);
				}
			} else {//キャンセルボタンクリック時
				dispose();
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class ChooseLevelPanel extends JPanel implements MouseListener{//難易度選択画面
		JLabel labelT,labelT2;//タイトル用ラベル
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
			this.setVisible(true);
			//タイトル用ラベル
			labelT = new JLabel("LEVEL");
			labelT.setFont(new Font("PixelMplus10", Font.PLAIN, 70));
			labelT.setForeground(Color.YELLOW);
			this.add(labelT);
			labelT.setBounds(20*w/50, h/11+20, 300, 50);
			//タイトル用ラベル
			labelT2 = new JLabel("LEVEL");
			labelT2.setFont(new Font("PixelMplus10", Font.PLAIN, 70));
			labelT2.setForeground(Color.BLUE);
			this.add(labelT2);
			labelT2.setBounds(20*w/50-5, h/11+20, 300, 50);
			//Easyボタン
			bEasy = new JButton("Easy");
			this.add(bEasy);
			bEasy.setFont(new Font("PixelMplus10", Font.PLAIN, 14));
			bEasy.setBounds(2*w/5-20, 3*h/11+30, 200, 50);
			bEasy.addMouseListener(this);
			bEasy.setActionCommand("Easy");
			//Normalボタン
			bNormal = new JButton("Normal");
			this.add(bNormal);
			bNormal.setFont(new Font("PixelMplus10", Font.PLAIN, 14));
			bNormal.setBounds(2*w/5-20, 5*h/11+30, 200, 50);
			bNormal.addMouseListener(this);
			bNormal.setActionCommand("Normal");
			//Hardボタン
			bHard = new JButton("Hard");
			this.add(bHard);
			bHard.setFont(new Font("PixelMplus10", Font.PLAIN, 14));
			bHard.setBounds(2*w/5-20, 7*h/11+30, 200, 50);
			bHard.addMouseListener(this);
			bHard.setActionCommand("Hard");
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);
			bBack.setActionCommand("戻る");
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			this.setVisible(false);
			if(theButton == bBack) {//戻るボタンクリック時
				client.PanelChange((JPanel)this, (JPanel)client.mnp);
			} else if (theButton == bEasy) {//Easyボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "easy");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			} else if (theButton == bNormal) {//Normalボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "normal");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			} else if(theButton == bHard){//Hardボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "hard");
				client.PanelChange((JPanel)this, (JPanel)client.mp);
			}
			client.mp.taLog.append("対局開始！\n");
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

	class OptionToLoginPanel extends JPanel implements MouseListener{//アカウント作成またはログイン用パネル
		JButton bMA, bLI,bBack;//アカウント、ログイン、戻るボタン
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
			this.setBackground(new Color(130, 0, 170));
			//アカウント作成用ボタン
			bMA = new JButton("アカウント作成");
			this.add(bMA);
			bMA.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bMA.setBounds(w/3+40, h/3, 200, 50);
			bMA.addMouseListener(this);
			bMA.setActionCommand("アカウント作成");
			//ログイン用ボタン
			bLI = new JButton("ログイン");
			this.add(bLI);
			bLI.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bLI.setBounds(w/3+40, h/3+80, 200, 50);
			bLI.addMouseListener(this);
			bLI.setActionCommand("ログイン");
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);
			bBack.setActionCommand("戻る");
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bBack) {//戻るボタンクリック時
				this.setVisible(false);
				client.PanelChange((JPanel)this, (JPanel)client.mnp);
				try {socket.close();}
				catch(IOException ioe) {System.out.println("失敗");}
			} else if (theButton == bMA) {//アカウント作成ボタンクリック時
				MakeAccountSubDialog masd = new MakeAccountSubDialog(client, "masd");
				masd.setLocation(w/6+370, h/6+200);
				masd.setVisible(true);
			} else if (theButton == bLI) {//ログインボタンクリック時
				LoginSubDialog lsd = new LoginSubDialog(client, "lsd");
				lsd.setLocation(w/6+370, h/6+200);
				lsd.setVisible(true);
			} else {
				osd.setVisible(true);
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

	class MakeAccountSubDialog extends JDialog implements MouseListener{//アカウント作成時ダイアログ
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
			setResizable(false);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//名前入力用ラベル
			labelN = new JLabel("名前を入力してください");
			labelN.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(labelN);
			labelN.setForeground(Color.WHITE);
			labelN.setBounds(w/4-30, 2*h/9-20, 300, 40);
			//名前入力用テキストフィールド
			name = new JTextField(30);
			name.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(name);
			name.setBounds(w/4-30, 3*h/9-20, 300, 40);
			//パスワード入力用ラベル
			labelP = new JLabel("パスワードを入力してください");
			labelP.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(labelP);
			labelP.setForeground(Color.WHITE);
			labelP.setBounds(w/4-30, 4*h/9-20, 300, 40);
			//パスワードフィールド
			pass = new JPasswordField(30);
			pass.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(pass);
			pass.setBounds(w/4-30, 5*h/9-20, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			bOK.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(bOK);
			bOK.setBounds(w/3-20, 13*h/18, 90, 30);
			bOK.addMouseListener(this);
			bOK.setActionCommand("OK");
			//キャンセルボタン
			bCan = new JButton("キャンセル");
			bCan.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			this.add(bCan);
			bCan.setBounds(7*w/12-20, 13*h/18, 90, 30);
			bCan.addMouseListener(this);
			bCan.setActionCommand("キャンセル");

		}
		//メソッド
		public void makeAccount(String name, String password) {//アカウントの作成
			sendMessage("makeAccount");
			sendMessage(name);
			sendMessage(password);
			sendMessage("end");

			client.flag = false;//receiverがログイン認証結果をサーバから受け取ったらtrueにする
			client.result = false;//ログイン認証結果が成功ならRecieverのrunメソッドでtrue
			int i = 0;

			while(++i<=6) {
				try {
					Thread.sleep(1000);
					if(client.flag) {
						setVisible(false);
						SucceededToMakeAccountSubDialog stmasd = new SucceededToMakeAccountSubDialog(client, "stmasd");
						stmasd.setLocation(client.w/6+370, client.h/6+200);
						stmasd.setVisible(true);
						break;
					}
				}
				catch(InterruptedException e) {}
			}
			JLabel labelError = new JLabel("失敗しました");
			labelError.setForeground(Color.GREEN);
			this.add(labelError);
			labelError.setBounds(w/3+30, 6*h/9-10, 300, 20);
		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//正常処理
					makeAccount(name.getText(),passwordstr);
				} else {//エラー時
					JLabel labelError = new JLabel("入力に誤りがあります");
					labelError.setForeground(Color.GREEN);
					this.add(labelError);
					labelError.setBounds(w/3+10, 6*h/9-10, 300, 20);
				}
			} else {//キャンセルボタンクリック時
				dispose();
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class SucceededToMakeAccountSubDialog extends JDialog implements MouseListener{//アカウント作成成功時ダイアログ
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
			setResizable(false);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//メッセージ用ラベル
			labelM = new JLabel("アカウントを作成しました");
			labelM.setFont(new Font("PixelMplus10", Font.PLAIN, 14));
			labelM.setForeground(Color.WHITE);
			this.add(labelM);
			labelM.setBounds(w/3-10, 2*h/9-10, 300, 40);
			//ユーザID表示用ラベル
			labelID = new JLabel("ユーザID");
			labelID.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			labelID.setForeground(Color.WHITE);
			this.add(labelID);
			labelID.setBounds(w/4-35, 3*h/9-10, 300, 40);
			//ユーザID表示用テキストフィールド
			ID = new JTextField("30");
			ID.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(ID);
			ID.setBounds(w/4-35, 4*h/9-10, 300, 40);
			ID.setText(player.getID());
			ID.setEditable(false);
			//注意用ラベル
			labelC1 = new JLabel("ユーザIDとパスワードは次回");
			labelC2 = new JLabel("ログイン時に必要になります");
			labelC1.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			labelC2.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(labelC1);
			this.add(labelC2);
			labelC1.setBounds(w/3, 5*h/9, 300, 40);
			labelC2.setBounds(w/3, 11*h/18, 300, 40);
			labelC1.setForeground(Color.ORANGE);
			labelC2.setForeground(Color.ORANGE);
			//OKボタン
			bOK = new JButton("OK");
			bOK.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(bOK);
			bOK.setBounds(w*2/5, 7*h/9, 90, 30);
			bOK.addMouseListener(this);
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			dispose();
			client.otlp.setVisible(false);
			client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
			client.PanelChange((JPanel)client.otlp, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
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
			setResizable(false);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//ユーザID入力用ラベル
			labelID = new JLabel("ユーザIDを入力してください");
			labelID.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.remove(labelN);
			this.add(labelID);
			labelID.setForeground(Color.WHITE);
			labelID.setBounds(w/4-30, 2*h/9-20, 300, 40);
			//ユーザID入力用テキストフィールド
			ID = new JTextField(30);
			ID.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.remove(name);
			this.add(ID);
			ID.setBounds(w/4-30, 3*h/9-20, 300, 40);
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

			while(++i<=5) {
				try {
					Thread.sleep(1000);
					if(client.flag) {
						client.flag = false;
						return client.result;//結果（成功ならtrue,失敗ならfalse）を返す
					}
				}
				catch(InterruptedException e) {System.out.println("失敗");}
			}
			return client.result;//5秒待っても来なかった場合にもfalseを返す
		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//入力に抜けがない場合
					if(login(ID.getText(), passwordstr)){//ログイン成功時
						dispose();
						SucceededToLoginSubDialog stlsd = new SucceededToLoginSubDialog(client, "stlsd");
						stlsd.setLocation(client.w/4+370, client.h/4+200);
						stlsd.setVisible(true);
					}else {//ログイン失敗時
						dispose();
						FailedToLoginSubDialog ftlsd = new FailedToLoginSubDialog(client, "ftlsd");
						ftlsd.setLocation(client.w/4+370, client.h/4+200);
						ftlsd.setVisible(true);
					}
				} else {//入力に抜けがある場合
					dispose();//破棄
					FailedToLoginSubDialog ftlsd = new FailedToLoginSubDialog(client, "ftlsd");//失敗時のダイアログを作成
					ftlsd.setLocation(client.w/4+370, client.h/4+200);
					ftlsd.setVisible(true);
				}
			} else {//キャンセルボタンクリック時
				dispose();
			}
		}
	}

	class SucceededToLoginSubDialog extends JDialog implements MouseListener{//ログイン成功時ダイアログ
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
			setResizable(false);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//メッセージ表示用ラベル
			message = new JLabel("ログインに成功しました");
			message.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			message.setForeground(Color.WHITE);
			this.add(message);
			message.setBounds(w/3-10, 5*h/12-30, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			this.add(bOK);
			bOK.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			bOK.setBounds(2*w/5-10, 3*h/5, 90, 30);
			bOK.addMouseListener(this);
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			dispose();
			client.otlp.setVisible(false);
			client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
			client.PanelChange((JPanel)client.otlp, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class FailedToLoginSubDialog extends SucceededToLoginSubDialog{//ログイン失敗時ダイアログ
		JButton bRetry;//キャンセルボタン
		//コンストラクタ
		FailedToLoginSubDialog(Client c, String s){
			super(c, s);
			setResizable(false);
			//メッセージの書き換え
			this.remove(message);
			message = new JLabel("ログインに失敗しました");
			message.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			message.setForeground(Color.WHITE);
			this.add(message);
			message.setBounds(w/3-20, 5*h/12-30, 300, 40);
			//キャンセルボタン
			bRetry = new JButton("Retry");
			this.remove(bOK);
			this.add(bRetry);
			bRetry.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			bRetry.setBounds(2*w/5-20, 3*h/5, 90, 30);
			bRetry.addMouseListener(this);//マウス操作を認識できるようにする
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			dispose();
			LoginSubDialog lsd = new LoginSubDialog(client, "lsd");
			lsd.setLocation(client.w/6+350, client.h/6+200);
			lsd.setVisible(true);
		}
	}

	class MenuForNetworkMatchingPanel extends JPanel implements MouseListener{//ネットワーク対局用メニュー画面パネル
		JLabel labelID;//ID用ラベル
		JLabel labelR;//レート用ラベル
		JLabel labelMode1,labelMode2;//MODEラベル
		JButton bRank;//ランクマッチボタン
		JButton bSpecial;//スペシャルマッチボタン
		JButton bInfo;//戦績・ランキング閲覧ボタン
		JButton bBack;//戻るボタン
		JTextArea ta;
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
			this.setBackground(new Color(130, 0, 170));
			//ID&レート用ラベル
			ta = new JTextArea("  プレイヤID："+player.getID() + "\n  レーティング："+String.valueOf(player.getRecord()[5]));
			ta.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			ta.setForeground(Color.WHITE);
			ta.setMargin(new Insets(10, 20, 10, 10));
			ta.setBackground(Color.BLACK);
			ta.setEditable(false);
			this.add(ta);
			ta.setBounds(6*w/9+50, h/20, 160, 35);
			ta.setBorder(new LineBorder(new Color(250,250,250), 2, false));
			//MODEラベル1
			labelMode1 = new JLabel("MODE");
			labelMode1.setFont(new Font("PixelMplus10", Font.PLAIN, 70));
			labelMode1.setForeground(Color.YELLOW);
			this.add(labelMode1);
			labelMode1.setBounds(20*w/50+13, h/11+40, 300, 80);
			//MODEラベル2
			labelMode1 = new JLabel("MODE");
			labelMode1.setFont(new Font("PixelMplus10", Font.PLAIN, 70));
			labelMode1.setForeground(Color.BLUE);
			this.add(labelMode1);
			labelMode1.setBounds(20*w/50+10, h/11+40, 300, 80);
			//ランクマッチボタン
			bRank = new JButton("ランクマッチ");
			bRank.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(bRank);
			bRank.setBounds(11*w/30 +5, 2*h/5+10, 200, 50);
			bRank.addMouseListener(this);
			bRank.setActionCommand("ランクマッチ");
			//スペシャルマッチボタン
			bSpecial = new JButton("スペシャルマッチ");
			bSpecial.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(bSpecial);
			bSpecial.setBounds(11*w/30+5, 3*h/5, 200, 50);
			bSpecial.addMouseListener(this);
			bSpecial.setActionCommand("スペシャルマッチ");
			//戦績・ランキング閲覧ボタン
			bInfo = new JButton("戦績・ランキング");
			bInfo.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(bInfo);
			bInfo.setBounds(7*w/10+15, 8*h/9, 200, 35);
			bInfo.addMouseListener(this);
			bInfo.setActionCommand("戦績・ランキング");
			//戻るボタン
			bBack = new JButton("戻る");
			bBack.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);
			bBack.setActionCommand("戻る");
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			this.setVisible(false);
			if(theButton == bBack) {//戻るボタンクリック時
				this.setVisible(true);
				CheckWhetherToLogoutSubDialog cwtlsd = new CheckWhetherToLogoutSubDialog(client, "cwtlsd");
				cwtlsd.setLocation(w/6+370, h/6+200);
				cwtlsd.setVisible(true);
			} else if (theButton == bInfo) {//戦績ランキング閲覧ボタンクリック時
				client.rrp = new ReadRecordPanel(client, "rrp");
				client.PanelChange((JPanel)this, (JPanel)client.rrp);
			} else if (theButton == bRank) {//ランクマッチボタンクリック時
				client.sfop = new SearchingForOpponentPanel(client, "sfop", "rank");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);

			} else if (theButton == bSpecial) {//スペシャルマッチボタンクリック時
				client.sfop = new SearchingForOpponentPanel(client, "sfop", "special");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);
			} else {
				osd.setVisible(true);
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class CheckWhetherToLogoutSubDialog extends JDialog implements MouseListener{
		JLabel messageLabel;//メッセージラベル
		JButton ok;//okボタン
		JButton can;//キャンセルボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;

		CheckWhetherToLogoutSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w,h);
			this.setLayout(null);
			setResizable(false);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//メッセージ表示用ラベル
			messageLabel = new JLabel("ログアウトしますか？");
			messageLabel.setForeground(Color.WHITE);
			messageLabel.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(messageLabel);
			messageLabel.setBounds(w/4+55, 4*h/9-40, 300, 40);
			//OKボタン
			ok = new JButton("OK");
			this.add(ok);
			ok.setBounds(w/3-20, 13*h/18, 90, 30);
			ok.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			ok.addMouseListener(this);
			ok.setActionCommand("OK");
			//キャンセルボタン
			can = new JButton("キャンセル");
			this.add(can);
			can.setBounds(7*w/12-20, 13*h/18, 90, 30);
			can.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			can.addMouseListener(this);
			can.setActionCommand("キャンセル");
		}

		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			if(theButton == ok) {
				client.mfnmp.setVisible(false);
				client.PanelChange((JPanel)client.mfnmp, (JPanel)client.mnp);
				try {socket.close();}
				catch(IOException ioe) {System.out.println("失敗");}
			}
			dispose();
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
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
		JLabel labelTitle4;//勝率用ラベル
		JLabel labelRatio;
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
			this.setBackground(new Color(130, 0, 170));
			//総戦績用ラベル
			labelTitle1 = new JLabel("総戦績");
			labelTitle1.setHorizontalAlignment(JTextField.CENTER);
			labelTitle1.setBorder(new LineBorder(Color.RED, 2, false));
			labelTitle1.setForeground(Color.WHITE);
			labelTitle1.setFont(new Font("PixelMplus10", Font.BOLD, 17));
			this.add(labelTitle1);
			labelTitle1.setBounds(30, 1*h/12, 80, 30);
			//戦績ラベル
			labelScore = new JLabel(player.getRecord()[0] + "戦 " + player.getRecord()[1] + "勝 "
					+ player.getRecord()[2] + "負 " + player.getRecord()[3] + "分");
			labelScore.setFont(new Font("PixelMplus10", Font.PLAIN, 17));
			labelScore.setForeground(Color.WHITE);
			this.add(labelScore);
			labelScore.setBounds(60, 2*h/12, 300, 30);
			//勝率用ラベル
			labelTitle4 = new JLabel("勝率");
			labelTitle4.setForeground(Color.WHITE);
			labelTitle4.setHorizontalAlignment(JTextField.CENTER);
			labelTitle4.setBorder(new LineBorder(Color.RED, 2, false));
			labelTitle4.setFont(new Font("PixelMplus10", Font.BOLD, 17));
			this.add(labelTitle4);
			labelTitle4.setBounds(30, 3*h/12, 80, 30);
			//勝率ラベル
			if(player.getRecord()[0] != 0)
				labelRatio = new JLabel(NumberFormat.getPercentInstance().format((float)player.getRecord()[1]/player.getRecord()[0]));
			else
				labelRatio = new JLabel("0%");
			labelRatio.setFont(new Font("PixelMplus10", Font.PLAIN, 17));
			labelRatio.setForeground(Color.WHITE);
			this.add(labelRatio);
			labelRatio.setBounds(60, 4*h/12, 300, 30);
			//投了数用ラベル
			labelTitle2 = new JLabel("投了数");
			labelTitle2.setForeground(Color.WHITE);
			labelTitle2.setHorizontalAlignment(JTextField.CENTER);
			labelTitle2.setBorder(new LineBorder(Color.RED, 2, false));
			labelTitle2.setFont(new Font("PixelMplus10", Font.BOLD, 17));
			this.add(labelTitle2);
			labelTitle2.setBounds(30, 5*h/12, 80, 30);
			//投了数ラベル
			labelResignation = new JLabel(player.getRecord()[4] + "回");
			labelResignation.setForeground(Color.WHITE);
			labelResignation.setFont(new Font("PixelMplus10", Font.PLAIN, 17));
			this.add(labelResignation);
			labelResignation.setBounds(60, 6*h/12, 300, 30);
			//ランキング用ラベル
			labelTitle3 = new JLabel("ランキング");
			labelTitle3.setForeground(Color.WHITE);
			labelTitle3.setHorizontalAlignment(JTextField.CENTER);
			labelTitle3.setBorder(new LineBorder(Color.RED, 2, false));
			labelTitle3.setFont(new Font("PixelMplus10", Font.BOLD, 17));
			this.add(labelTitle3);
			labelTitle3.setBounds(30, 7*h/12, 100, 30);
			//ランキング表示テキストエリア
			taRanking = new JTextArea("", 300, 400);
			taRanking.setForeground(Color.GREEN);
			taRanking.append(String.format("%-11s"," ") + String.format("%-10s", "rate") + "name");
			taRanking.setFont(new Font("PixelMplus10", Font.PLAIN, 17));
			//taRanking.setForeground(Color.WHITE);
			taRanking.setBackground(Color.BLACK);
			taRanking.setLineWrap(true);
			taRanking.setWrapStyleWord(true);
			taRanking.setEditable(false);
			JScrollPane spRanking = new JScrollPane(taRanking);
			spRanking.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			this.add(spRanking);
			spRanking.setBounds(30+w/2, 1*h/18, 320, 430);
			sendMessage("getRecord");
			//sendMessage("getRanking");
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(10, 8*h/9, 150, 35);
			bBack.addMouseListener(this);

		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//ランキング情報の設定（recieverで用いる）
		public void setRanking(String str) {
			taRanking.append("\n" + str);
		}
		//ユーザのランクの設定（recieverで用いる）
		public void setRank(String str) {
			labelRanking = new JLabel(str + "位（" + player.getRecord()[5] + "）");
			labelRanking.setFont(new Font("PixelMplus10", Font.PLAIN, 17));
			labelRanking.setForeground(Color.WHITE);
			this.add(labelRanking);
			labelRanking.setBounds(60, 8*h/12, 300, 40);
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			this.setVisible(false);
			PanelChange((JPanel)this, (JPanel)client.mfnmp);
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
			this.setBackground(new Color(130, 0, 170));
			//メッセージ用ラベル
			tfMessage = new JTextField("プレイヤを探しています...");
			tfMessage.setFont(new Font("PixelMplus10", Font.BOLD, 13));
			this.add(tfMessage);
			tfMessage.setEditable(false);
			tfMessage.setHorizontalAlignment(JTextField.CENTER);
			tfMessage.setBackground(Color.BLACK);
			tfMessage.setForeground(Color.WHITE);
			tfMessage.setBorder(new LineBorder(new Color(240,240,240), 2, false));
			tfMessage.setBounds(w/3-20, 3*h/7, 300, 50);
			//対戦相手を探す
			findOpponent fo = new findOpponent(this);
			fo.start();
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//対戦相手マッチング用スレッド
		class findOpponent extends Thread{
			SearchingForOpponentPanel panel;

			findOpponent(SearchingForOpponentPanel p){
				panel = p;
			}

			public void run() {
				client.flag = false;
				client.result = false;
				if(mode.equals("rank"))
					specialFlag = false;
				else
					specialFlag = true;

				sendMessage("findingOpponent");
				sendMessage(mode);
				sendMessage("end");

				while(true) {
					try {
						if(client.flag) {
							if(client.result) {
								client.mp = new MatchingPanel(client, "mp", mode);
								client.PanelChange((JPanel)client.sfop, (JPanel)client.mp);
								mp.repaint();
								mp.taLog.append("対局開始！\n");
							} else {
								tfMessage.setText("対戦相手が見つかりませんでした");
								Thread.sleep(5000);
								client.sfop.setVisible(false);
								PanelChange((JPanel)client.sfop, (JPanel)client.mfnmp);
							}
							break;
						}
						Thread.sleep(1000);
					}
					catch(InterruptedException e) {}
				}
			}
		}

	}

	class MatchingPanel extends JPanel implements MouseListener{//対局画面用パネル
		Computer cpu;//CPUオブジェクト
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
		//Confirmer confirmer;//サーバに接続確認信号を送信するスレッド
		JTextField tfEffect;//特殊効果の表示ラベル
		int effectNumber;//発生中エフェクト数
		JTextArea taLog;//ログ
		ImageIcon blackIcon, whiteIcon, boardIcon, placeableIcon, unplaceableIcon, eventIcon,
		eventPlaceableIcon, hiddenIcon, explosionBlackIcon, explosionWhiteIcon, explosionUnplaceableIcon,
		blackHackunIcon, whiteHackunIcon, blackHackunExplosionIcon, whiteHackunExplosionIcon; //アイコン
		Clip se;//SE
		int previousRate[] = new int[2];//対局前レート保持用配列
		int cpuOperation;//CPUのseekメソッド実行の戻り値を格納
		int winner;//OthelloのcheckWinnerメソッドの戻り値を格納
		int event = 0;//発生しているイベントを判別
		boolean flagEvent = false;//特定のイベント発生中判別
		boolean nikaiFlag = false;//二回行動発生中判別
		int dispValue[];//画面描画時に使用
		CheckWhetherToResignSubDialog cwtrsd;
		int w = 750;
		int h = 500;
		//コンストラクタ
		MatchingPanel(Client c, String s, String m){//modeはrank, special, easy, normal, hardのいずれか
			client = c;
			str = s;
			mode = m;
			FloatControl control = (FloatControl)bgm1.getControl(FloatControl.Type.MASTER_GAIN);
			control.setValue((float)Math.log10(0.5) * 20);
			bgm1.setFramePosition(0);
			if(BGM) {
				bgm1.loop(Clip.LOOP_CONTINUOUSLY);
			}
			if(mode.equals("rank") || mode.equals("special")){//ネットワーク対局の場合
				if(mode.equals("rank")) othello = new Othello(0);//ランクマッチの場合
				else {
					othello = new Othello(1);//スペシャルマッチの場合
					othello.s_generategrids(specialNumber);
				}

				if(player.getColor().equals("black")) {
					if(mode.equals("rank"))
						othello.checkPlaceable();
					else
						othello.s_checkPlaceable();
				}
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
			this.setSize(w, h);
			this.setLayout(null);
			this.setBackground(new Color(130, 0, 170));
			//アイコン設定(画像ファイルをアイコンとして使う)
			whiteIcon = new ImageIcon(new File("White.jpg").getAbsolutePath());
			blackIcon = new ImageIcon(new File("Black.jpg").getAbsolutePath());
			boardIcon = new ImageIcon(new File("GreenFrame.jpg").getAbsolutePath());
			placeableIcon = new ImageIcon(new File("Placeable.jpg").getAbsolutePath());
			hiddenIcon = new ImageIcon(new File("Hidden.jpg").getAbsolutePath());//雲
			if(mode.equals("special")) {
				unplaceableIcon = new ImageIcon(new File("IrreversibleStone.jpg").getAbsolutePath());//お邪魔石
				eventIcon = new ImageIcon(new File("Event.jpg").getAbsolutePath());//イベントマス
				eventPlaceableIcon = new ImageIcon(new File("EventPlaceable.jpg").getAbsolutePath());//イベントマスかつ置ける
				explosionBlackIcon = new ImageIcon(new File("explosionBlack2.gif").getAbsolutePath());//黒石破壊
				explosionWhiteIcon = new ImageIcon(new File("explosionWhite2.gif").getAbsolutePath());//白石破壊
				explosionUnplaceableIcon = new ImageIcon(new File("explosionIrreversibleSquare.gif").getAbsolutePath());//お邪魔石破壊
				blackHackunIcon = new ImageIcon(new File("BlackHackun.jpg").getAbsolutePath());//親ハックン
				whiteHackunIcon = new ImageIcon(new File("GrayHackun.jpg").getAbsolutePath());//子ハックン
				blackHackunExplosionIcon = new ImageIcon(new File("explosionBlack.gif").getAbsolutePath());//親ハックン破壊
				whiteHackunExplosionIcon = new ImageIcon(new File("explosionWhite.gif").getAbsolutePath());//子ハックン破壊
			}
			//オセロ盤面の初期化
			buttonArray = new JButton[row][row];
			dispValue = new int[6];
			if(System.getProperty("os.name").toLowerCase().startsWith("mac")) {
				dispValue[0] = 40;
				dispValue[1] = 38;
				dispValue[2] = 20;
				dispValue[3] = 25;
				dispValue[4] = 15;
				dispValue[5] = 350;
			}
			else {
				dispValue[0] = 15;
				dispValue[1] = 43;
				dispValue[2] = -8;
				dispValue[3] = 15;
				dispValue[4] = 5;
				dispValue[5] = 360;
			}
			JLabel horizontalNumber;
			JLabel verticalNumber;
			for(int i=1; i<9; i++) {
				horizontalNumber = new JLabel(String.valueOf(i));
				horizontalNumber.setBounds(dispValue[2] + i*dispValue[1], dispValue[5], 30, 30);
				horizontalNumber.setFont(new Font("PixelMplus10", Font.PLAIN, dispValue[3]));
				horizontalNumber.setForeground(new Color(243,152,0));
				this.add(horizontalNumber);
				verticalNumber = new JLabel(String.valueOf(i));
				verticalNumber.setFont(new Font("PixelMplus10", Font.PLAIN, dispValue[3]));
				verticalNumber.setForeground(new Color(243,152,0));
				verticalNumber.setBounds(dispValue[4], dispValue[5] - i*dispValue[1], 30, 30);
				this.add(verticalNumber);
			}
			//設定ボタン
			bOption = new JButton("設定");
			this.add(bOption);
			bOption.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bOption.setBounds(4*w/5, 35, 120, 35);
			bOption.addMouseListener(this);
			bOption.setActionCommand("設定");
			//投了ボタン
			bResign = new JButton("投了");
			this.add(bResign);
			bResign.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			bResign.setBounds(4*w/5, 75, 120, 35);
			bResign.addMouseListener(this);
			bResign.setActionCommand("投了");
			//黒駒の数用ラベル
			tfBlackNumber = new JTextField("●　" + othello.getBlackstone() + "枚");
			tfBlackNumber.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			tfBlackNumber.setHorizontalAlignment(JTextField.CENTER);
			tfBlackNumber.setEditable(false);
			this.add(tfBlackNumber);
			tfBlackNumber.setBounds(6*w/11,  5*h/13-30, 80, 40);
			//白駒の数用ラベル
			tfWhiteNumber = new JTextField("●　" + othello.getWhitestone() + "枚");
			tfWhiteNumber.setForeground(Color.WHITE);
			tfWhiteNumber.setBackground(Color.BLACK);
			tfWhiteNumber.setBorder(new LineBorder(new Color(240,240,240), 2, false));
			tfWhiteNumber.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			tfWhiteNumber.setHorizontalAlignment(JTextField.CENTER);
			tfWhiteNumber.setEditable(false);
			this.add(tfWhiteNumber);
			tfWhiteNumber.setBounds(6*w/11, 6*h/13-25, 80, 40);
			//戦況ログ用テキストエリア
			taLog = new JTextArea(330, 200);
			taLog.setBackground(Color.BLACK);
			taLog.setForeground(Color.WHITE);
			taLog.setEditable(false);
			taLog.setLineWrap(true);
			taLog.setWrapStyleWord(true);
			JScrollPane scroll = new JScrollPane(taLog);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setBounds(6*w/11-20, 7*h/13-5, 330, 200);
			this.add(scroll);
			//残り時間表示用ラベル
			timer = new Timer(this);
			timer.addTo(this);
			timer.start();
			if(player.getColor().equals("black")) timer.stop(false);
			else timer.stop(true);
			//modeがネットワーク対戦であるとき
			if(mode.equals("rank") || mode.equals("special")) {
				//色表示用ラベル
				if(player.getColor().equals("black"))
					tfColor = new JTextField("あなたは黒です");
				else
					tfColor = new JTextField("あなたは白です");
				tfColor.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
				tfColor.setHorizontalAlignment(JTextField.CENTER);
				tfColor.setBackground(Color.BLACK);
				tfColor.setForeground(Color.WHITE);
				tfColor.setBorder(new LineBorder(new Color(240,240,240), 2, false));
				tfColor.setEditable(false);
				tfColor.setBounds(w/5-30, 3*h/4+10, 160, 40);
				this.add(tfColor);
				//手番表示用ラベル
				if(player.getColor().equals("black"))
					tfTurn = new JTextField("あなたの番です");
				else
					tfTurn = new JTextField("相手の番です");
				tfTurn.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
				tfTurn.setHorizontalAlignment(JTextField.CENTER);
				tfTurn.setBackground(Color.BLACK);
				tfTurn.setForeground(Color.WHITE);
				tfTurn.setBorder(new LineBorder(new Color(240,240,240), 2, false));
				tfTurn.setEditable(false);
				tfTurn.setBounds(w/5-30, 3*h/4+53, 160, 40);
				this.add(tfTurn);
				labelPlayer1 = new JLabel(player.getName() + " " + player.getRecord()[5]);
				labelPlayer1.setFont(new Font("PixelMplus10", Font.ITALIC, 25));
				labelPlayer1.setForeground(new Color(255,255,0));
				labelPlayer1.setHorizontalAlignment(JTextField.CENTER);
				this.add(labelPlayer1);
				labelPlayer1.setBounds(6*w/11-70, h/13, 300, 40);
				//vsラベル
				labelVS = new JLabel("vs");
				labelVS.setFont(new Font("PixelMplus10", Font.ITALIC, 25));
				labelVS.setForeground(new Color(255,255,0));
				this.add(labelVS);
				labelVS.setBounds(7*w/11, 2*h/13-10, 300, 40);
				//相手情報表示用ラベル
				labelPlayer2 = new JLabel(opponent.getName() + " " + opponent.getRecord()[5]);
				labelPlayer2.setFont(new Font("PixelMplus10", Font.ITALIC, 25));
				labelPlayer2.setHorizontalAlignment(JTextField.CENTER);
				labelPlayer2.setForeground(new Color(255,255,0));
				this.add(labelPlayer2);
				labelPlayer2.setBounds(6*w/11-70, 3*h/13-20, 300, 40);
				if(mode.equals("special")) {
					//特殊効果発動是非表示用ラベル
					effectNumber=1;
					tfEffect = new JTextField("発動中特殊効果なし");
					tfEffect.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
					tfEffect.setBackground(Color.BLACK);
					tfEffect.setForeground(Color.WHITE);
					tfEffect.setBorder(new LineBorder(new Color(240,240,240), 2, false));
					tfEffect.setHorizontalAlignment(JTextField.CENTER);
					tfEffect.setEditable(false);
					this.add(tfEffect);
					tfEffect.setBounds(7*w/11+60, 6*h/13-25, 150, 40);
				}
			}
			//ローカル対局のとき
			else {
				//色表示用ラベル
				tfColor = new JTextField("あなたは黒です");
				tfColor.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
				tfColor.setBackground(Color.BLACK);
				tfColor.setForeground(Color.WHITE);
				tfColor.setBorder(new LineBorder(new Color(240,240,240), 2, false));
				tfColor.setHorizontalAlignment(JTextField.CENTER);
				tfColor.setEditable(false);
				tfColor.setBounds(w/5-30, 3*h/4+25, 160, 40);
				this.add(tfColor);
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
			//設定画面の生成
			osd = new OptionSubDialog(client, "osd");
			osd.setLocation(client.w/6+430, client.h/6+250);
			osd.setVisible(false);
			//盤面の描画
			if(mode.equals("special")) {
				for(int j=0; j<row; j++) {
					int yy = dispValue[0] + j*dispValue[1];
					for(int i=0; i<row; i++) {
						if(othello.getGrids()[j][i] == 1){ buttonArray[j][i] = new JButton(whiteHackunIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i] = new JButton(blackHackunIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i]%10==3) {buttonArray[j][i] = new JButton(unplaceableIcon);}
						else if(othello.getGrids()[j][i] == 2){ //盤面状態に応じたアイコンを設定
							if(PLACABLE) buttonArray[j][i] = new JButton(placeableIcon);
							else buttonArray[j][i] = new JButton(boardIcon);
						}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==0){ buttonArray[j][i] = new JButton(eventIcon);}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==2) {
							if(PLACABLE) buttonArray[j][i] = new JButton(eventPlaceableIcon);
							else buttonArray[j][i] = new JButton(eventIcon);
						}

						int xx = dispValue[0] + i*dispValue[1];
						this.add(buttonArray[j][i]);
						buttonArray[j][i].setBounds(xx, yy, 45, 45);//ボタンの大きさと位置を設定する．
						buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
						buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
					}
				}
			}
			else {
				for(int j=0; j<row; j++) {
					int yy = dispValue[0] + j*dispValue[1];
					for(int i=0; i<row; i++) {
						if(othello.getGrids()[j][i] == 1){ buttonArray[j][i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
						else if(othello.getGrids()[j][i]%10==3) {buttonArray[j][i] = new JButton(unplaceableIcon);}
						else if(othello.getGrids()[j][i] == 2){ //盤面状態に応じたアイコンを設定
							if(PLACABLE) buttonArray[j][i] = new JButton(placeableIcon);
							else buttonArray[j][i] = new JButton(boardIcon);
						}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==0){ buttonArray[j][i] = new JButton(eventIcon);}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==2) {
							if(PLACABLE) buttonArray[j][i] = new JButton(eventPlaceableIcon);
							else buttonArray[j][i] = new JButton(eventIcon);
						}

						int xx = dispValue[0] + i*dispValue[1];
						this.add(buttonArray[j][i]);
						buttonArray[j][i].setBounds(xx, yy, 45, 45);//ボタンの大きさと位置を設定する．
						buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
						buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
					}
				}
			}
		}

		//メソッド
		public String getName() {//パネル名の取得
			return str;
		}

		public String getMode() {//モードの取得
			return mode;
		}

		public int[] getPreviousRate() {//対局前のレート取得
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
				this.tf.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
				this.tf.setBackground(Color.BLACK);
				this.tf.setForeground(Color.WHITE);
				this.tf.setBorder(new LineBorder(new Color(240,240,240), 2, false));
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
									finishMatching("timeup", false);
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
				if(mode.equals("special"))
					this.tf.setBounds(7*w/11+60,  5*h/13-30, 150, 40);
				else
					this.tf.setBounds(7*w/11+60, 5*h/13-10, 150, 40);
			}
		}

		public void finishMatching(String str, boolean exception) {//対局を終了(str:win, lose, draw, resign, timeup, )
			timer.finish();
			bgm1.stop();
			bgm1.flush();
			if(mode.equals("rank") || mode.equals("special")) {//ネットワーク対局時
				previousRate[0] = player.getRecord()[5];
				previousRate[1] = opponent.getRecord()[5];
				if(mode.equals("rank")) {//ランクマッチ時
					if(!str.equals("draw")) {
						int[] r = new int[6];
						if(str.equals("win")) {
							r = player.getRecord();
							r[5] = calculateRate(previousRate[0], previousRate[1], str);
							player.setRecord(r);
							r = opponent.getRecord();
							r[5] = calculateRate(previousRate[1], previousRate[0], "lose");
							opponent.setRecord(r);
						}
						else {
							player.getRecord()[5] = calculateRate(player.getRecord()[5], opponent.getRecord()[5], "lose");
							opponent.getRecord()[5] = calculateRate(previousRate[1], previousRate[0], "win");
						}
					}
				}
				if(exception == false) {
					sendMessage("noticeEndMatching");
					sendMessage(str);
					sendMessage(String.valueOf(player.getRecord()[5]));
					sendMessage("end");
				}
			}
			int[] r = new int[6];
			r = player.getRecord();
			r[0]++;
			if(str.equals("win")) {
				client.emsd = new EndMatchingSubDialog(client, "emsd", str);
				r[1]++;
			}
			else if(str.equals("draw")) {
				client.emsd = new EndMatchingSubDialog(client, "emsd", str);
				r[3]++;
			}
			else {
				client.emsd = new EndMatchingSubDialog(client, "emsd", "lose");
				r[2]++;
				if(str.equals("resign"))
					r[4]++;
			}
			player.setRecord(r);
			emsd.setLocation(client.w/6+350, client.h/6+200);
			emsd.setVisible(true);
		}

		public int calculateRate(int rate1, int rate2, String result) { //レート計算
			if(result.equals("lose")) {//敗北時
				if(!((rate2-rate1)>=400))
					return rate1 - (int)(16+0.04*(rate1-rate2));
				else
					return rate1;
			} else {//勝利時
				if(!((rate1-rate2)>=400))
					return rate1 + (int)(16+0.04*(rate2-rate1));
				else
					return rate1;
			}
		}

		public void acceptAction(int x, int y) {//ランクマッチ相手からの操作の受付とそれに応じた処理
			if(mode.equals("rank")) {
				othello.checkPlaceable();
				othello.setStone(x, y);
				updateDisp(x, y);
				if(SE) {
					se = createClip(new File("othello.wav"));
					se.start();
				}

				if(!othello.checkPlaceable()) {//置けない場合
					othello.changeTurn();
					if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
						if((winner == -1 && player.getColor().equals("black")) || (winner == 1 && player.getColor().equals("white")))
							finishMatching("win", false);
						else if(winner == 0)
							finishMatching("draw", false);
						else
							finishMatching("lose", false);
					}
					updateDisp(8,8);
				}
				else {
					updateDisp(x, y);
					timer.stop(false);
				}

			}
			else {
				if(!flagEvent) {
					othello.s_checkPlaceable();
					event = othello.s_setStone(x,y);
				}
				switch(event) {
					case 1://破壊
						if(flagEvent) {
							if(othello.getGrids()[y][x]==-1) {
								othello.destroystone(x,y);
								othello.changeTurn();//自分のターンにする
								othello.s_checkPlaceable();
								updateDisp(8,8);
								buttonArray[y][x].setIcon(blackHackunExplosionIcon);
							}
							else {
								othello.destroystone(x,y);
								othello.changeTurn();//自分のターンにする
								othello.s_checkPlaceable();
								updateDisp(8,8);
								if(SE) {
									se = createClip(new File("othello.wav"));
									se.start();//SEを流す
								}
								buttonArray[y][x].setIcon(whiteHackunExplosionIcon);
							}
							if(SE) {
								se = createClip(new File("bomb1.wav"));
								se.start();//SEを流す
							}
							flagEvent = false;
							event = 0;
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を破壊しました\n");
							if(othello.s_checkPlaceable()) { //自分がおけるなら
								timer.stop(false);
							}
							else {//置けないなら
								othello.changeTurn();//相手のターンにする
								taLog.append("自動パスされました\n");
							}
						}
						else {
							flagEvent = true;
							othello.changeTurn();
							updateDisp(x,y);
							othello.changeTurn();
						}
						break;
					case 3://二階行動
						othello.changeTurn();//自分のターンにする
						updateDisp(x,y);
						if(SE) {
							se = createClip(new File("othello.wav"));
							se.start();//SEを流す
						}
						othello.changeTurn();//相手のターンに戻す
						event = 0;
						if(!othello.s_checkPlaceable()) {//相手が置けないなら
							othello.changeTurn();//自分のターンにして
							othello.s_checkPlaceable();
							timer.stop(false);
							updateDisp(8,8);
							taLog.append("自動パスされました\n");
							if(effectNumber==1) tfEffect.setText(" ");
							else tfEffect.setText("発動中：革命");
						} else nikaiFlag=true;
						if(SE) {
							se = createClip(new File("shock1.wav"));
							se.start();//SEを流す
						}
						break;
					case 4://邪魔石
						if(flagEvent) {
							othello.setgarbage(x, y);
							othello.changeTurn();
							flagEvent = false;
							event = 0;
							updateDisp(8,8);
							if(SE) {
								se = createClip(new File("incorrect2.wav"));
								se.start();//SEを流す
							}
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") にお邪魔石を置きました\n");
							if(othello.s_checkPlaceable()) {//自分がおけるなら
								updateDisp(8,8);
								timer.stop(false);
							}
							else{//置けないなら
								othello.changeTurn();//相手のターンにする
								taLog.append("自動パスされました\n");
							}
						}
						else {
							flagEvent = true;
							othello.changeTurn();//自分のターンにする
							updateDisp(x,y);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							othello.changeTurn();//相手のターンに戻す
						}
						break;
					case 5://上下１マスに石を置く
						othello.set_cross(x,y);
						othello.changeTurn();
						event = 0;
						updateDisp(8,8);
						if(SE) {
							se = createClip(new File("othello.wav"));
							se.start();//SEを流す
						}
						taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を中心に石をクロス置きしました\n");
						if(othello.s_checkPlaceable()) {
							timer.stop(false);
							updateDisp(8,8);
						}
						else {
							othello.changeTurn();
							taLog.append("自動パスされました\n");
						}
						break;
					case 6://盤面を反転させる
						othello.inversion();
						if(SE) {
							se = createClip(new File("othello.wav"));
							se.start();//SEを流す
						}
						othello.changeTurn();
						event = 0;
						updateDisp(x,y);
						taLog.append("盤面が反転されました\n");
						if(othello.s_checkPlaceable()) {
							updateDisp(8,8);
							timer.stop(false);
						}
						else {
							othello.changeTurn();
							taLog.append("自動パスされました\n");
						}
						break;
					case 8://革命
						othello.revolution();
						if(SE) {
							se = createClip(new File("othello.wav"));
							se.start();//SEを流す
						}
						othello.changeTurn();
						updateDisp(x,y);
						event = 0;
						if(othello.s_checkPlaceable()) {
							updateDisp(8,8);
							timer.stop(false);
						}
						else {
							othello.changeTurn();
							taLog.append("自動パスされました\n");
						}
						if(SE) {
							se = createClip(new File("shock1.wav"));
							se.start();//SEを流す
						}
						break;
					default:
						othello.changeTurn();
						if(SE) {
							se = createClip(new File("othello.wav"));
							se.start();//SEを流す
						}
						if(event==7){
							switch(othello.getTurn()) {
							case 1:
								if(player.getColor().equals("black"))
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
								else
									taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
								break;
							case -1:
								if(player.getColor().equals("white"))
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
								else
									taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
							}
							taLog.append("盤面が隠されました\n");
							if(SE) {
								//se = createClip(new File("othello.wav"));
								//se.start();//SEを流す
							}
							for(int j=0; j<row; j++) {
								int yy = dispValue[0] + j*dispValue[1];
								for(int i=0; i<row; i++) {
									buttonArray[j][i].setIcon(hiddenIcon);
									int xx = dispValue[0] + i*dispValue[1];
									this.add(buttonArray[j][i]);
									buttonArray[j][i].setBounds(xx, yy, 45, 45);//ボタンの大きさと位置を設定する．
									buttonArray[j][i].addMouseListener(this);//マウス操作を認識できるようにする
									buttonArray[j][i].setActionCommand(Integer.toString(j*8+i));//ボタンを識別するための名前(番号)を付加する
								}
							}
							tfBlackNumber.setText("黒　" + othello.getBlackstone() + "枚");
							tfWhiteNumber.setText("白　" + othello.getWhitestone() + "枚");

							if(SE) {
								se = createClip(new File("shrink1.wav"));
								se.start();//SEを流す
							}
						}
						else
							updateDisp(x,y);
						if(nikaiFlag) {
							nikaiFlag=false;
							if(effectNumber==1) tfEffect.setText("発動中特殊効果なし");
							else tfEffect.setText("発動中：革命");
						}
						if(othello.s_checkPlaceable()) {
							if(event!=7)
								updateDisp(8,8);
							timer.stop(false);
						}
						else {
							othello.changeTurn();
							taLog.append("自動パスされました\n");
						}
						event=0;
				}

				if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
					if((winner == -1 && player.getColor().equals("black")) || (winner == 1 && player.getColor().equals("white")))
						finishMatching("win", false);
					else if(winner == 0)
						finishMatching("draw", false);
					else
						finishMatching("lose", false);
				}
			}
			if((othello.getTurn() == -1 && player.getColor().equals("black")) || (othello.getTurn() == 1 && player.getColor().equals("white")))
				tfTurn.setText("あなたの番です");
			else
				tfTurn.setText("相手の番です");
		}

		public void updateDisp(int x, int y){ // 画面を更新する
			if(x!=8 && y!=8) {//x==8とy==8は例外
				switch(othello.getTurn()) {
					case 1:
						if(player.getColor().equals("black"))
							taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
						else
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に黒を置きました\n");
						break;
					case -1:
						if(player.getColor().equals("white"))
							taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
						else
							taLog.append("相手が (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") に白を置きました\n");
				}
			}
			if(mode.equals("special")) {
				for(int j=0; j<row; j++) {
					for(int i=0; i<row; i++) {
						if(othello.getGrids()[j][i] == 1){ buttonArray[j][i].setIcon(whiteHackunIcon);}
						else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i].setIcon(blackHackunIcon);}
						else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i].setIcon(boardIcon);}
						else if(othello.getGrids()[j][i]%10==3) {buttonArray[j][i].setIcon(unplaceableIcon);}
						else if(othello.getGrids()[j][i] == 2){
							if(PLACABLE) buttonArray[j][i].setIcon(placeableIcon);
							else buttonArray[j][i].setIcon(boardIcon);
						}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==0){ buttonArray[j][i].setIcon(eventIcon);}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==2) {
							if(PLACABLE) buttonArray[j][i].setIcon(eventPlaceableIcon);
							else buttonArray[j][i].setIcon(eventIcon);
						}
					}
				}
			}
			else {
				for(int j=0; j<row; j++) {
					for(int i=0; i<row; i++) {
						if(othello.getGrids()[j][i] == 1){ buttonArray[j][i].setIcon(whiteIcon);}
						else if(othello.getGrids()[j][i] == -1){ buttonArray[j][i].setIcon(blackIcon);}
						else if(othello.getGrids()[j][i] == 0 || othello.getGrids()[j][i] == 5){ buttonArray[j][i].setIcon(boardIcon);}
						else if(othello.getGrids()[j][i]%10==3) {buttonArray[j][i].setIcon(unplaceableIcon);}
						else if(othello.getGrids()[j][i] == 2){
							if(PLACABLE) buttonArray[j][i].setIcon(placeableIcon);
							else buttonArray[j][i].setIcon(boardIcon);
						}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==0){ buttonArray[j][i].setIcon(eventIcon);}
						else if(othello.getGrids()[j][i]/10!=0 && othello.getGrids()[j][i]%10==2) {
							if(PLACABLE) buttonArray[j][i].setIcon(eventPlaceableIcon);
							else buttonArray[j][i].setIcon(eventIcon);
						}
					}
				}
			}

			tfBlackNumber.setText("●　" + othello.getBlackstone() + "枚");
			tfWhiteNumber.setText("●　" + othello.getWhitestone() + "枚");

			if(event != 0) {
				switch(event) {
					case 3:
						if(effectNumber==1)
							tfEffect.setText("発動中：二回行動");
						else
							tfEffect.setText("発動中：革命,二回行動");
						break;
					case 8:
						tfEffect.setText("発動中：革命");
						effectNumber++;
						break;
				}
			}

		}

		public void mouseClicked(MouseEvent e) {//マウスクリック時の処理
			JButton theButton = (JButton)e.getComponent();
			String command = theButton.getActionCommand();
			if(theButton == bResign) {//投了ボタンクリック時
				CheckWhetherToResignSubDialog cwtrsd = new CheckWhetherToResignSubDialog(client, "cwtrsd");
				cwtrsd.setLocation(client.w/6+430, client.h/6+250);
				cwtrsd.setVisible(true);
			}
			else if(theButton == bOption) {//設定ボタンクリック時
				osd.setVisible(true);
			}
			else if ((player.getColor().equals("black")&&othello.getTurn()==-1) || (player.getColor().equals("white")&&othello.getTurn()==1)){//オセロを操作をしたとき
				int x = Integer.parseInt(command) % 8;
				int y = Integer.parseInt(command) / 8;
				//ランクマッチ時
				if(mode.equals("rank") && othello.getGrids()[y][x] == 2) {
					othello.setStone(x, y);//ターンが変わる
					if(SE) {
						se = createClip(new File("othello.wav"));
						se.start();//SEを流す
					}
					updateDisp(x,y);
					timer.stop(true);
					sendMessage("sendOperation");
					sendMessage(String.valueOf(x));
					sendMessage(String.valueOf(y));
					sendMessage("end");
					if(!othello.checkPlaceable()) {//相手がおけない場合
						if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
							if((winner == -1 && player.getColor().equals("black")) || (winner == 1 && player.getColor().equals("white")))
								finishMatching("win", false);
							else if(winner == 0)
								finishMatching("draw", false);
							else
								finishMatching("lose", false);
						}

						othello.changeTurn();
						othello.checkPlaceable();
						updateDisp(8,8);
						timer.stop(false);
					}

					if((othello.getTurn() == -1 && player.getColor().equals("black")) || (othello.getTurn() == 1 && player.getColor().equals("white")))
						tfTurn.setText("あなたの番です");
					else
						tfTurn.setText("相手の番です");

				}
				//スペシャルマッチ時
				else if(mode.equals("special") && (othello.getGrids()[y][x]%10 == 2 || flagEvent == true)) {
					//1: 破壊 3:二階行動 4:お邪魔石 5:上下ヒトマスを自分のに 6:盤面を反転させる 7:盤面を隠す 8:革命
					if(!flagEvent)
						event = othello.s_setStone(x,y);
					othello.s_draw();
					switch(event){
						case 1://破壊
							if(flagEvent && (othello.getGrids()[y][x]==-1||othello.getGrids()[y][x]==1||othello.getGrids()[y][x]==3) ) {
								if(othello.getGrids()[y][x]==-1) {//黒なら
									othello.destroystone(x,y);
									updateDisp(8,8);
									System.out.println(othello.getGrids()[y][x]);
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を破壊しました\n");
									buttonArray[y][x].setIcon(blackHackunExplosionIcon);
								}
								else if(othello.getGrids()[y][x]==1){//白なら
									othello.destroystone(x,y);
									updateDisp(8,8);
									System.out.println(othello.getGrids()[y][x]);
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を破壊しました\n");
									buttonArray[y][x].setIcon(whiteHackunExplosionIcon);
								}
								else if(othello.getGrids()[y][x]==3){
									othello.destroystone(x,y);
									updateDisp(8,8);
									System.out.println(othello.getGrids()[y][x]);
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を破壊しました\n");
									buttonArray[y][x].setIcon(explosionUnplaceableIcon);
								}
								othello.changeTurn();
								flagEvent = false;
								event = 0;
								sendMessage("sendOperation");
								sendMessage(String.valueOf(x));
								sendMessage(String.valueOf(y));
								sendMessage("end");
								timer.stop(true);
								if(SE) {
									se = createClip(new File("bomb1.wav"));
									se.start();//SEを流す
								}

							}
							else if(flagEvent == false){
								flagEvent = true;
								othello.changeTurn();
								updateDisp(x,y);
								if(SE) {
									se = createClip(new File("othello.wav"));
									se.start();//SEを流す
								}
								taLog.append("任意のマスの石を破壊できます\n");
								othello.changeTurn();//自分のターンにする
								sendMessage("sendOperation");
								sendMessage(String.valueOf(x));
								sendMessage(String.valueOf(y));
								sendMessage("end");
							}
							break;
						case 3://二階行動
							othello.changeTurn();
							updateDisp(x,y);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							othello.changeTurn();//自分のターンに戻す
							event = 0;
							sendMessage("sendOperation");
							sendMessage(String.valueOf(x));
							sendMessage(String.valueOf(y));
							sendMessage("end");
							if(othello.s_checkPlaceable()) {//もし置けるなら
								updateDisp(8,8);
								nikaiFlag = true;
							} else {//置けないなら
								taLog.append("置けるマスがなく二回行動に失敗しました\n");
								othello.changeTurn();
								if(effectNumber==1) tfEffect.setText(" ");
								else tfEffect.setText("発動中：革命");
								timer.stop(true);
							}
							if(SE) {
								se = createClip(new File("shock1.wav"));
								se.start();//SEを流す
							}
							break;
						case 4://お邪魔石
							if(flagEvent) {
								if(othello.getGrids()[y][x]%10!=-1 && othello.getGrids()[y][x]%10!=1) {//石が置かれていないのを確認して
									othello.setgarbage(x, y);
									othello.changeTurn();
									flagEvent = false;
									event = 0;
									updateDisp(8,8);
									taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") にお邪魔石を置きました\n");
									sendMessage("sendOperation");
									sendMessage(String.valueOf(x));
									sendMessage(String.valueOf(y));
									sendMessage("end");
									timer.stop(true);
									if(SE) {
										se = createClip(new File("incorrect2.wav"));
										se.start();//SEを流す
									}
								}
							}
							else {
								flagEvent = true;
								othello.changeTurn();
								updateDisp(x,y);
								if(SE) {
									se = createClip(new File("othello.wav"));
									se.start();//SEを流す
								}
								taLog.append("任意のマスを置けなくできます\n");
								othello.changeTurn();//自分のターンに戻す
								sendMessage("sendOperation");
								sendMessage(String.valueOf(x));
								sendMessage(String.valueOf(y));
								sendMessage("end");
							}
							break;
						case 5://上下１マスに石を置く
							othello.set_cross(x,y);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							othello.changeTurn();//相手のターンにする
							event = 0;
							updateDisp(8,8);
							taLog.append("あなたが (" + String.valueOf(x+1) + "," + String.valueOf(8-y) + ") を中心に石をクロス置きしました\n");
							sendMessage("sendOperation");
							sendMessage(String.valueOf(x));
							sendMessage(String.valueOf(y));
							sendMessage("end");
							timer.stop(true);
							break;
						case 6://盤面を反転させる
							othello.inversion();
							othello.changeTurn();
							event = 0;
							updateDisp(x,y);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							taLog.append("盤面が反転されました\n");
							sendMessage("sendOperation");
							sendMessage(String.valueOf(x));
							sendMessage(String.valueOf(y));
							sendMessage("end");
							timer.stop(true);
							break;
						case 8://革命
							othello.revolution();
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							othello.changeTurn();
							updateDisp(x,y);
							event = 0;
							sendMessage("sendOperation");
							sendMessage(String.valueOf(x));
							sendMessage(String.valueOf(y));
							sendMessage("end");
							timer.stop(true);
							if(SE) {
								se = createClip(new File("shock1.wav"));
								se.start();//SEを流す
							}
							break;
						default://イベントマスでなかったら
							othello.changeTurn();//相手のターンにする
							updateDisp(x,y);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							if(event==7)
								taLog.append("相手の盤面を隠しました\n");
							event = 0;
							sendMessage("sendOperation");
							sendMessage(String.valueOf(x));
							sendMessage(String.valueOf(y));
							sendMessage("end");
							if(nikaiFlag) {
								nikaiFlag = false;
								if(effectNumber==1) tfEffect.setText("発動中特殊効果なし");
								else tfEffect.setText("発動中：革命");
							}
							timer.stop(true);
					}

					if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
						if((winner == -1 && player.getColor().equals("black")) || (winner == 1 && player.getColor().equals("white")))
							finishMatching("win", false);
						else if(winner == 0)
							finishMatching("draw", false);
						else
							finishMatching("lose", false);
					}

					if(event==0 && nikaiFlag==false && !othello.s_checkPlaceable()) {//相手がおけない場合
						System.out.println("check");
						othello.changeTurn();
						othello.s_checkPlaceable();
						updateDisp(8,8);
						timer.stop(false);
						taLog.append("自動パスされました\n");
					}

					if((othello.getTurn() == -1 && player.getColor().equals("black")) || (othello.getTurn() == 1 && player.getColor().equals("white")))
						tfTurn.setText("あなたの番です");
					else
						tfTurn.setText("相手の番です");
				}
				//ローカル対局時
				else if (!mode.equals("rank") && !mode.equals("special") && othello.getGrids()[y][x] == 2){
					othello.setStone(x, y);//ターンが変わる
					updateDisp(x,y);
					if(SE) {
						se = createClip(new File("othello.wav"));
						se.start();//SEを流す
					}
					timer.stop(true);
					if(othello.checkPlaceable()) {//コンピュータに打つ手がある場合
						do {
							cpuOperation = cpu.seek(othello.getGrids());
							othello.setStone(cpuOperation%10, (cpuOperation-cpuOperation%10)/10);//ターンが変わる
							updateDisp(cpuOperation%10,(cpuOperation-cpuOperation%10)/10);
							if(SE) {
								se = createClip(new File("othello.wav"));
								se.start();//SEを流す
							}
							if(othello.checkPlaceable()) break;
							else othello.changeTurn();
						} while(othello.checkPlaceable());
					}
					else{//コンピュータに打つ手がない場合
						othello.changeTurn();
						othello.checkPlaceable();
					}
					try {
						Thread.sleep(500);
						updateDisp(8, 8);
					}
					catch(InterruptedException ie) {}
					if(!((winner = othello.checkWinner()) == 10)) {//勝敗判断
						if(winner == -1) finishMatching("win", false);
						else if(winner == 0) finishMatching("draw", false);
						else finishMatching("lose", false);
					}
					timer.stop(false);


				}
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class OptionSubDialog extends JDialog implements MouseListener{//設定ダイアログ
		JLabel labelBGM;//BGMラベル
		JLabel labelSE;//SEラベル
		JLabel labelPlacable;//置ける場所表示ラベル
		JRadioButton radioON1;//BGM ONボタン
		JRadioButton radioON2;//SE ONボタン
		JRadioButton radioON3;//置ける場所表示 ONボタン
		JRadioButton radioOFF1;//BGM OFFボタン
		JRadioButton radioOFF2;//SE OFFボタン
		JRadioButton radioOFF3;//置ける場所表示 OFFボタン
		JButton bBack;//戻るボタン
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
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			setResizable(false);
			setTitle("設定");
			//BGM用ラベル
			labelBGM = new JLabel("BGM");
			labelBGM.setForeground(Color.WHITE);
			labelBGM.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(labelBGM);
			labelBGM.setBounds(w/4-40, 2*h/7-30, 150, 40);
			//SE用ラベル
			labelSE = new JLabel("SE");
			labelSE.setForeground(Color.WHITE);
			labelSE.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(labelSE);
			labelSE.setBounds(w/4-40, 3*h/7-30, 150, 40);
			//置ける場所表示用ラベル
			labelPlacable = new JLabel("置ける場所表示");
			labelPlacable.setForeground(Color.WHITE);
			labelPlacable.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			this.add(labelPlacable);
			labelPlacable.setBounds(w/4-40, 4*h/7-30, 150, 40);
			//ONラジオボタン1
			if(BGM) {
				radioON1 = new JRadioButton("ON",true);
				radioOFF1 = new JRadioButton("OFF",false);
			}
			else {
				radioON1 = new JRadioButton("ON",false);
				radioOFF1 = new JRadioButton("OFF",true);
			}
			this.add(radioON1);
			radioON1.setBounds(w/2, 2*h/7-25, 55, 25);
			radioON1.setForeground(Color.WHITE);
			radioON1.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioON1.addMouseListener(this);
			radioON1.setActionCommand("BGM ON");
			//ONラジオボタン2
			if(client.SE) {
				radioON2 = new JRadioButton("ON",true);
				radioOFF2 = new JRadioButton("OFF",false);
			}
			else {
				radioON2 = new JRadioButton("ON",false);
				radioOFF2 = new JRadioButton("OFF",true);
			}
			this.add(radioON2);
			radioON2.setBounds(w/2, 3*h/7-25, 55, 25);
			radioON2.setForeground(Color.WHITE);
			radioON2.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioON2.addMouseListener(this);
			radioON2.setActionCommand("SE ON");
			//ONラジオボタン3
			if(client.PLACABLE) {
				radioON3 = new JRadioButton("ON",true);
				radioOFF3 = new JRadioButton("OFF",false);
			}
			else {
				radioON3 = new JRadioButton("ON",false);
				radioOFF3 = new JRadioButton("OFF",true);
			}
			this.add(radioON3);
			radioON3.setBounds(w/2, 4*h/7-25, 55, 25);
			radioON3.setForeground(Color.WHITE);
			radioON3.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioON3.addMouseListener(this);
			radioON3.setActionCommand("置ける場所表示 ON");
			//OFFラジオボタン1
			this.add(radioOFF1);
			radioOFF1.setBounds(7*w/10, 2*h/7-25, 60, 25);
			radioOFF1.setForeground(Color.WHITE);
			radioOFF1.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioOFF1.addMouseListener(this);
			radioOFF1.setActionCommand("BGM OFF");
			//OFFラジオボタン2
			this.add(radioOFF2);
			radioOFF2.setBounds(7*w/10, 3*h/7-25, 60, 25);
			radioOFF2.setForeground(Color.WHITE);
			radioOFF2.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioOFF2.addMouseListener(this);
			radioOFF2.setActionCommand("SE OFF");
			//OFFラジオボタン3
			this.add(radioOFF3);
			radioOFF3.setBounds(7*w/10, 4*h/7-25, 60, 25);
			radioOFF3.setForeground(Color.WHITE);
			radioOFF3.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			radioOFF3.addMouseListener(this);
			radioOFF3.setActionCommand("置ける場所表示 OFF");
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			bBack.setBounds(3*w/7-15, 6*h/7-20, 90, 24);
			bBack.addMouseListener(this);
			bBack.setActionCommand("戻る");
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			if(e.getSource() == bBack) {
				setVisible(false);
			}
			else if(e.getSource() == radioON1) {//BGMのONボタン
				radioON1.setSelected(true);
				radioOFF1.setSelected(false);
				bgm1.loop(Clip.LOOP_CONTINUOUSLY);
			}
			else if(e.getSource() == radioOFF1) {//BGMのOFFボタン
				System.out.println("offが押された");
				radioOFF1.setSelected(true);
				radioON1.setSelected(false);
				bgm1.stop();
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
				if((player.getColor().equals("black")&&othello.getTurn()==-1) || (player.getColor().equals("white")&&othello.getTurn()==1))
					mp.updateDisp(8, 8);
			}
			else if(e.getSource() == radioOFF3) {//置ける場所表示のOFFボタン
				client.PLACABLE = false;
				radioON3.setSelected(false);
				radioOFF3.setSelected(true);
				mp.updateDisp(8, 8);
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}


	class CheckWhetherToResignSubDialog extends JDialog implements MouseListener{//投了確認時ダイアログ
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
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			setResizable(false);
			//メッセージラベル
			labelMessage = new JLabel("本当に投了しますか?");
			labelMessage.setForeground(Color.WHITE);
			labelMessage.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			this.add(labelMessage);
			labelMessage.setBounds(w/3, 2*h/5-20, w/2, h/10);
			//OKボタン
			bOK = new JButton("OK");
			bOK.setFont(new Font("PixelMplus10", Font.PLAIN, 13));
			this.add(bOK);
			bOK.setBounds(w*3/15, 6*h/8, w/4, h/10);
			bOK.addMouseListener(this);
			//Cancelボタン
			bCan = new JButton("キャンセル");
			bCan.setFont(new Font("PixelMplus10", Font.PLAIN, 10));
			this.add(bCan);
			bCan.setBounds(8*w/15, 6*h/8, w/4, h/10);
			bCan.addMouseListener(this);
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			setVisible(false);
			if((JButton)e.getComponent() == bOK) //OKボタンクリック時
				mp.finishMatching("resign", false);
			dispose();//このダイアログを破棄
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class TotalizingSubDialog extends JDialog{//再戦希望集計中パネル
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
			setResizable(false);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(50,50,80));
			//メッセージ用ラベル
			tfMessage = new JTextField("再戦希望を集計中です...");
			tfMessage.setEditable(false);
			tfMessage.setFont(new Font("PixelMplus10", Font.PLAIN, 12));
			tfMessage.setHorizontalAlignment(JTextField.CENTER);
			this.add(tfMessage);
			tfMessage.setBounds(w*3/8-35, 5*h/11-30, 200, 40);
			//再戦を希望する
			new tryToRematch().start();
		}
		//メソッド
		class tryToRematch extends Thread{
			public void run() {
				if(client.mp.getMode().equals("rank"))
					sendMessage("rankRematch");
				else
					sendMessage("specialRematch");
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
								mp.repaint();
								mp.taLog.append("対局開始！\n");
								dispose();
							} else {
								tfMessage.setText("相手が再戦を希望しませんでした");
								Thread.sleep(2500);
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
		}

	}

	class EndMatchingSubDialog extends JDialog implements MouseListener{//対局終了時ダイアログ
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
			setResizable(false);
			Container contentPane = getContentPane();
		    contentPane.setBackground(new Color(243,152,0));
		    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			this.setModalityType(ModalityType.APPLICATION_MODAL);
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
			labelResult.setBounds(1*w/5-50, h/7, 400, 50);
			labelResult.setHorizontalAlignment(JTextField.CENTER);
			labelResult.setFont(new Font("PixelMplus10", Font.PLAIN, 60));

			if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) {//ネットワーク対戦時
				//プレイヤ1のレート用ラベル
				if((player.getRecord()[5] - mp.getPreviousRate()[0])<0)
					labelRate1 = new JLabel(player.getRecord()[5] + " ( " + (player.getRecord()[5] - mp.getPreviousRate()[0]) + " )");
				else
					labelRate1 = new JLabel(player.getRecord()[5] + " ( +" + (player.getRecord()[5] - mp.getPreviousRate()[0]) + " )");
				this.add(labelRate1);
				labelRate1.setBounds(2*w/7+65, 3*h/7+5, 300, 30);
				labelRate1.setFont(new Font("PixelMplus10", Font.PLAIN, 15));
				//プレイヤ2のレート用ラベル
				if((opponent.getRecord()[5] - mp.getPreviousRate()[1])<0)
					labelRate2 = new JLabel(opponent.getRecord()[5] + " ( " + (opponent.getRecord()[5] - mp.getPreviousRate()[1]) + ")");
				else
					labelRate2 = new JLabel(opponent.getRecord()[5] + " ( +" + (opponent.getRecord()[5] - mp.getPreviousRate()[1]) + ")");
				this.add(labelRate2);
				labelRate2.setBounds(2*w/7+65, 5*h/7-25, 300, 30);
				labelRate2.setFont(new Font("PixelMplus10", Font.PLAIN, 15));

				if(player.getColor().equals("black")) {
					labelPlayer1 = new JLabel(player.getName() + " ● " + othello.getBlackstone());
					labelPlayer2 = new JLabel(opponent.getName() + " ● " + othello.getWhitestone());
					labelPlayer2.setForeground(Color.WHITE);
					labelRate2.setForeground(Color.WHITE);
				}
				else {
					labelPlayer1 = new JLabel(player.getName() + " ● " + othello.getWhitestone());
					labelPlayer1.setForeground(Color.WHITE);
					labelRate1.setForeground(Color.WHITE);
					labelPlayer2 = new JLabel(opponent.getName() + " ● " + othello.getBlackstone());
				}
				//プレイヤ1情報用ラベル
				labelPlayer1.setBounds(2*w/7-45, 2*h/7+25, 300, 30);
				labelPlayer1.setHorizontalAlignment(JTextField.CENTER);
				this.add(labelPlayer1);

				//プレイヤ2情報用ラベル
				labelPlayer2.setBounds(2*w/7-45, 4*h/7-5, 300, 30);
				labelPlayer2.setHorizontalAlignment(JTextField.CENTER);
				this.add(labelPlayer2);

			}
			else {//ローカル対戦時
				//プレイヤ1情報用ラベル
				labelPlayer1 = new JLabel("player" + " ● " + othello.getBlackstone());
				labelPlayer1.setBounds(2*w/7+40, 2*h/7+45, 300, 30);
				this.add(labelPlayer1);
				//プレイヤ2情報用ラベル
				labelPlayer2 = new JLabel("   cpu" + " ● " + othello.getWhitestone());
				labelPlayer2.setBounds(2*w/7+40, 4*h/7-10, 300, 30);
				labelPlayer2.setForeground(Color.WHITE);
				this.add(labelPlayer2);
			}

			labelPlayer1.setFont(new Font("PixelMplus10", Font.PLAIN, 15));
			labelPlayer2.setFont(new Font("PixelMplus10", Font.PLAIN, 15));
			//再戦希望ボタン
			bRematch = new JButton("再戦");
			this.add(bRematch);
			bRematch.setBounds(1*w/5+30, 6*h/7-20, 90, 24);
			bRematch.addMouseListener(this);
			bRematch.setActionCommand("再戦");
			//戻るボタン
			bBack = new JButton("戻る");
			this.add(bBack);
			bBack.setBounds(3*w/5-20, 6*h/7-20, 90, 24);
			bBack.addMouseListener(this);
			bBack.setActionCommand("戻る");
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();
			if(theButton == bRematch) {//再戦ボタンクリック時
				this.setVisible(false);
				if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) {
					TotalizingSubDialog tsd = new TotalizingSubDialog(client, "tsd");
					tsd.setLocation(client.w/6+370, client.h/6+200);
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
				dispose();
				client.mp.setVisible(false);

				if(client.mp.getMode().equals("rank") || client.mp.getMode().equals("special")) {
					mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
					client.PanelChange((JPanel)client.mp, (JPanel)client.mfnmp);
				}
				else
					client.PanelChange((JPanel)client.mp, (JPanel)client.clp);
			}
		}
		public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.HAND_CURSOR));}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}


	// コンストラクタ
	public Client() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("わくわくはっくおせろ");
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
		} else if (jpF.getName().equals("sfop")) {
			cont.remove(sfop);
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

	public boolean connectServer(String ipAddress, int port){	// サーバに接続
		socket = null;
		try {
			socket = new Socket(ipAddress, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			receiver = new Receiver(socket);
			receiver.start();
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

	public void sendMessage(String msg){
		out.println(msg);
		out.flush();
	}

	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr;
		private BufferedReader br;

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket){
			try{
				sisr = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(sisr);
			} catch (IOException e) {
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド
		public void run(){
			while(true) {//データを受信し続ける
				try {
					String inputLine = br.readLine();//目的を読み込む
					if(inputLine.equals("sendAccountInfo")) {//目的がアカウント情報の送信の場合
						int i=0;
						String accountInfo[] = new String[3];
						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								accountInfo[i++] = inputLine;
							}
							else {
								player = new Player(accountInfo[0], accountInfo[1], accountInfo[2]);
								flag = true;
								int[] r = new int [6];
								for(int a=0; a<5;a++) {
									r[a] = 0;
								}
								r[5] = 1500;
								player.setRecord(r);
								break;
							}
						}
					}
					else if(inputLine.equals("sendLoginResult")) {//目的がログイン結果の送信の場合
						inputLine = br.readLine();
						System.out.println(inputLine);
						if(inputLine.equals("succeeded")) {
							result = true;
							String[] userInfo = new String[9];
							int i=0;
							while(true) {
								inputLine = br.readLine();
								System.out.println(inputLine);
								if(!inputLine.equals("end")) {
									userInfo[i++] = inputLine;
								} else {
									player = new Player(userInfo[0],userInfo[1],userInfo[2]);
									int[] r = new int [6];
									for(int a=0; a<6;a++) {
										r[a] = Integer.parseInt(userInfo[a+3]);
									}
									player.setRecord(r);
									break;
								}
							}
						} else if(inputLine.equals("failed")){
							result = false;
						}
						flag = true;
					}
					else if(inputLine.equals("sendFindingResult")) {//目的が対戦相手のマッチング結果の送信の場合
						System.out.println(inputLine);
						inputLine = br.readLine();
						System.out.println(inputLine);
						if(inputLine.equals("succeeded")) {
							result = true;
							int i=0;
							String matchingInfo[] = new String[4];
							while(true) {
								inputLine = br.readLine();
								if(!inputLine.equals("end")) {
									System.out.println(inputLine);
									matchingInfo[i++] = inputLine;
								} else {
									player.setColor(matchingInfo[0]);
									opponent = new Player(matchingInfo[1],"0","0");
									int[] r = new int [6];
									for(int a=0; a<5;a++) {
										r[i] = 0;
									}
									r[5] = Integer.parseInt(matchingInfo[2]);
									opponent.setRecord(r);
									if(specialFlag)
										//specialNumber = Integer.parseInt(matchingInfo[3]);
										specialNumber = 0;
									break;
								}
							}
						}
						else if(inputLine.equals("failed")) {
							result = false;
						}
						Thread.sleep(500);
						flag = true;
					}
					else if(inputLine.equals("sendRematchResult")) {//目的が再戦集計結果の送信だったら
						inputLine = br.readLine();
						if(inputLine.equals("succeeded")) result = true;
						else result = false;
						flag = true;
					}
					else if(inputLine.equals("sendOperation")) {//目的が操作情報の転送だったら
						inputLine = br.readLine();
						int x = Integer.parseInt(inputLine);
						inputLine = br.readLine();
						int y = Integer.parseInt(inputLine);
						mp.acceptAction(x,y);
					}
					else if(inputLine.equals("sendRecord")) {//目的が戦績・ランキング情報の送信だったら
						int i= 1;
						System.out.println(inputLine);
						while(!(inputLine = br.readLine()).equals("rate")) {
							System.out.println(inputLine);
							rrp.setRanking(" " + String.format("%-10s", String.valueOf(i++) + ".") + inputLine);
						}
						System.out.println(inputLine);
						inputLine=br.readLine();
						System.out.println(inputLine);
						rrp.setRank(inputLine);
						flag = true;
					}
					else if(inputLine.equals("noticeEndMatching")) {//目的が対局終了の知らせだったら
						br.readLine();
						mp.finishMatching("win", true);
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

	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		try {socket.close();}
		catch(IOException ioe) {System.out.println("接続解除に失敗しました");}
	}
	public void windowClosed(WindowEvent e) {
		try {socket.close();}
		catch(IOException ioe) {System.out.println("接続解除に失敗しました");}
		}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

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
	}


}