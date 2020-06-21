//パッケージのインポート
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
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
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame {
	private StartPanel sp = new StartPanel(this, "sp");//その他パネルはボタンクリック時に作成
	private ChooseLevelPanel clp;//難易度選択パネル
	private SubStartPanel ssp;//サブスタートパネル
	private MenuForNetworkMatchingPanel mfnmp;//ネットワーク対局用メニュー画面パネル
	private ReadRecordPanel rrp;//戦績ランキング閲覧パネル
	private SearchingForOpponentPanel sfop;//マッチングパネル
	private MatchingPanel mp;//対局パネル
	//private Player player;//プレイヤ
	private Container cont; // コンテナ
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private int w = 810;
	private int h = 540;
	private boolean flag;
	private boolean result;
	
	//パネル・ダイアログ（内部クラス）
	class StartPanel extends JPanel implements MouseListener{//スタート画面用パネル
		JButton bN, bL;//ネットワーク対局、ローカル対局選択用ボタン
		Client client;
		String str;
		int w = 810;
		int h = 540;
		//コンストラクタ
		StartPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//ネットワーク対局選択用ボタン
			bN = new JButton("ネットワーク対局");
			this.add(bN); //ネットワーク対局選択用ボタンをパネルに追加
			bN.setBounds(w/3+40, h/3, 200, 50);
			bN.addMouseListener(this);//マウス操作を認識できるようにする
			bN.setActionCommand("network");//ボタンを識別するための名前を付加する
			//ローカル対局選択用ボタン
			bL = new JButton("ローカル対局");
			this.add(bL);//ローカル対局選択用ボタンをパネルに追加
			//bL.setPreferredSize(new Dimension(200, 50));//サイズを設定
			bL.setBounds(w/3+40, h/3+80, 200, 50);
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
			sendMessage(command); //テスト用にメッセージを送信
			this.setVisible(false);
			if(theButton == bN) {//ネットワーク対局ボタンクリック時
				client.ssp = new SubStartPanel(client, "ssp"); 
				client.PanelChange((JPanel)this, (JPanel)client.ssp);
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
		int w = 810;
		int h = 540;
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
			labelT.setBounds(24*w/50, h/11, 300, 40);
			//Easyボタン
			bEasy = new JButton("Easy");
			this.add(bEasy);
			bEasy.setBounds(2*w/5, 3*h/11, 200, 50);
			bEasy.addMouseListener(this);//マウス操作を認識できるようにする
			bEasy.setActionCommand("easy");//ボタンを識別するための名前を付加する
			//Normalボタン
			bNormal = new JButton("Normal");
			this.add(bNormal);
			bNormal.setBounds(2*w/5, 5*h/11, 200, 50);
			bNormal.addMouseListener(this);//マウス操作を認識できるようにする
			bNormal.setActionCommand("normal");//ボタンを識別するための名前を付加する
			//Hardボタン
			bHard = new JButton("Hard");
			this.add(bHard);
			bHard.setBounds(2*w/5, 7*h/11, 200, 50);
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
			sendMessage(command); //テスト用にメッセージを送信
			this.setVisible(false);
			if(theButton == bBack) {//戻るボタンクリック時
				client.PanelChange((JPanel)this, (JPanel)client.sp);
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
	
	class SubStartPanel extends JPanel implements MouseListener{//サブスタート画面（アカウント作成、ログイン）用パネル
		JButton bMA, bLI,bBack;//アカウント、ログイン選択用ボタン
		Client client;
		String str;
		int w = 810;
		int h = 540;
		//コンストラクタ
		SubStartPanel(Client c, String s){
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
			sendMessage(command); //テスト用にメッセージを送信
			if(theButton == bBack) {//戻るボタンクリック時
				this.setVisible(false);
				client.PanelChange((JPanel)this, (JPanel)client.sp);
			} else if (theButton == bMA) {//アカウント作成ボタンクリック時
				MakeAccountSubDialog masd = new MakeAccountSubDialog(client, "masd");
				masd.setLocation(w/6, h/6);
				masd.setVisible(true);
			} else if (theButton == bLI) {//ログインボタンクリック時
				LoginSubDialog lsd = new LoginSubDialog(client, "lsd");
				lsd.setLocation(w/6, h/6);
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
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//正常処理
					setVisible(false);
					/*
					client.connectServer();//サーバに接続
					client.makeAccount(labelN.getText(), passwordstr);//アカウント作成処理
					*/
					SucceededToMakeAccountSubDialog atmasd = new SucceededToMakeAccountSubDialog(client, "atmasd");
					atmasd.setLocation(client.w/6, client.h/6);
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
	//アカウント作成成功画面用パネル
	
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
			client.ssp.setVisible(false);
			client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
			client.PanelChange((JPanel)client.ssp, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//ログイン画面用パネル
	//ログイン画面用パネル
	
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
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == bOK) {//OKボタンクリック時
				String passwordstr = new String(pass.getPassword());
				if((name.getText() != null) && !passwordstr.isEmpty()) {//入力に抜けがなければ
					/*
					client.connectServer();//サーバに接続 
					if(Login(labelID.getText(), passwordstr)){//ログイン成功時
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
					stlsd.setLocation(client.w/4, client.h/4);
					stlsd.setVisible(true);
				} else {//入力に抜けがある場合
					dispose();//破棄
					FailedToLoginSubDialog ftlsd = new FailedToLoginSubDialog(client, "ftlsd");//失敗時のダイアログを作成
					ftlsd.setLocation(client.w/4, client.h/4);
					ftlsd.setVisible(true);
				}
			} else {//キャンセルボタンクリック時
				dispose();
			}
		}
	}
	//ログイン成功画面用パネル
	
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
				client.ssp.setVisible(false);
				client.mfnmp = new MenuForNetworkMatchingPanel(client, "mfnmp");
				client.PanelChange((JPanel)client.ssp, (JPanel)client.mfnmp);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//ログイン失敗画面用パネル
	
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
			lsd.setLocation(client.w/6, client.h/6);
			lsd.setVisible(true);
		}
	}
	//ネットワーク対局メニュー画面用パネル
	
	class MenuForNetworkMatchingPanel extends JPanel implements MouseListener{//ネットワーク対局用メニュー画面パネル
		JLabel labelID;//ID用ラベル
		JLabel labelR;//レート用ラベル
		JButton bRank;//ランクマッチボタン
		JButton bSpecial;//スペシャルマッチボタン
		JButton bInfo;//戦績・ランキング閲覧ボタン
		JButton bBack;//戻るボタン
		Client client;
		String str;
		int w = 810;
		int h = 540;
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
			bRank.setBounds(11*w/30, 2*h/5-30, 200, 50);
			bRank.addMouseListener(this);//マウス操作を認識できるようにする
			bRank.setActionCommand("rank");//ボタンを識別するための名前を付加する
			//スペシャルマッチボタン
			bSpecial = new JButton("スペシャルマッチ");
			this.add(bSpecial);
			bSpecial.setBounds(11*w/30, 3*h/5-30, 200, 50);
			bSpecial.addMouseListener(this);//マウス操作を認識できるようにする
			bSpecial.setActionCommand("special");//ボタンを識別するための名前を付加する
			//戦績・ランキング閲覧ボタン
			bInfo = new JButton("戦績・ランキング");
			this.add(bInfo);
			bInfo.setBounds(7*w/10, 11*h/13, 200, 50);
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
			sendMessage(command); //テスト用にメッセージを送信
			this.setVisible(false);//パネルを見えなくする
			if(theButton == bBack) {//戻るボタンクリック時
				client.PanelChange((JPanel)this, (JPanel)client.sp);
			} else if (theButton == bInfo) {//戦績ランキング閲覧ボタンクリック時
				client.rrp = new ReadRecordPanel(client, "rrp");
				client.PanelChange((JPanel)this, (JPanel)client.rrp);
			} else if (theButton == bRank) {//ランクマッチボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "network");
				client.sfop = new SearchingForOpponentPanel(client, "sfop");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);
			} else {//スペシャルマッチボタンクリック時
				client.mp = new MatchingPanel(client, "mp", "network");
				client.sfop = new SearchingForOpponentPanel(client, "sfop");
				client.PanelChange((JPanel)this, (JPanel)client.sfop);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//戦績・ランキング閲覧画面用パネル
	
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
		int w = 810;
		int h = 540;
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
			sendMessage(command); //テスト用にメッセージを送信
			this.setVisible(false);
			client.PanelChange((JPanel)this, (JPanel)client.mfnmp);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//マッチング中画面用パネル
	
	class SearchingForOpponentPanel extends JPanel{//マッチングうパネル
		JLabel labelMessage;//メッセージ用ラベル
		Client client;
		String str;
		int w = 810;
		int h = 540;
		//コンストラクタ
		SearchingForOpponentPanel(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//メッセージ用ラベル
			labelMessage = new JLabel("プレイヤを探しています...");
			this.add(labelMessage);
			labelMessage.setBounds(w/3, 3*h/7, 300, 50);
		}
		//パネル名の取得
		public String getName() {
			return str;
		}
	}
	//対局画面用パネル
	
	class MatchingPanel extends JPanel implements MouseListener{//対局画面用パネル
		JButton buttonArray[];//オセロ盤用のボタン配列
		JButton bOption, bResign; //設定、投了ボタン
		JLabel colorLabel; // 色表示用ラベル
		JLabel turnLabel; // 手番表示用ラベル
		JLabel labelPlayer1;//自分のプレイヤ名
		JLabel labelVS;//vsラベル
		JLabel labelPlayer2;//相手のプレイヤ名
		JLabel labelMode1;//モード表示用ラベル1
		JLabel labelMode2;//モード表示用ラベル2
		JLabel labelBlackNumber;//黒の数
		JLabel labelWhiteNumber;//白の数
		JLabel labelTimer;//残り時間ラベル
		JLabel labelEffect;//特殊効果の表示ラベル
		JTextArea taLog;//ログ
		ImageIcon blackIcon, whiteIcon, boardIcon; //アイコン
		Client client;
		String str;
		String mode;
		CheckWhetherToResignSubDialog cwtrsd;
		int w = 810;
		int h = 540;
		//コンストラクタ
		MatchingPanel(Client c, String s, String m){//modeはnetwork, easy, normal, hardのいずれか
			client = c;
			str = s;
			mode = m;
			//テスト用に局面情報を初期化
			String [] grids = 
				{"board","board","board","board","board","board","board","board",
				"board","board","board","board","board","board","board","board",
				"board","board","board","board","board","board","board","board",
				"board","board","board","black","white","board","board","board",
				"board","board","board","white","black","board","board","board",
				"board","board","board","board","board","board","board","board",
				"board","board","board","board","board","board","board","board",
				"board","board","board","board","board","board","board","board"};
			int row = 8; //オセロ盤の縦横マスの数
			//ウィンドウ設定
			this.setSize(w, h);//ウィンドウのサイズを設定
			this.setLayout(null);
			//アイコン設定(画像ファイルをアイコンとして使う)
			whiteIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/White.jpg");
			blackIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/Black.jpg");
			boardIcon = new ImageIcon("/Users/tomoyainazawa/selfProgram/Java/ProjectLearning/src/GreenFrame.jpg");
			//オセロ盤の生成
			buttonArray = new JButton[row * row];//ボタンの配列を作成
			for(int i = 0 ; i < row * row ; i++){
				if(grids[i].equals("black")){ buttonArray[i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
				if(grids[i].equals("white")){ buttonArray[i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
				if(grids[i].equals("board")){ buttonArray[i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
				this.add(buttonArray[i]);//ボタンの配列をペインに貼り付け
				// ボタンを配置する
				int x = 40+(i % row) * 38;
				int y = 40 + (int) (i / row) * 38;
				buttonArray[i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
				buttonArray[i].addMouseListener(this);//マウス操作を認識できるようにする
				buttonArray[i].setActionCommand(Integer.toString(i));//ボタンを識別するための名前(番号)を付加する
			}
			//設定ボタン
			bOption = new JButton("設定");//終了ボタンを作成
			this.add(bOption); //終了ボタンをパネルに追加
			bOption.setBounds(4*w/5, 5, 150, 35);//終了ボタンの境界を設定
			bOption.addMouseListener(this);//マウス操作を認識できるようにする
			bOption.setActionCommand("option");//ボタンを識別するための名前を付加する
			//投了ボタン
			bResign = new JButton("投了");//パスボタンを作成
			this.add(bResign); //パスボタンをパネルに追加
			bResign.setBounds(4*w/5, 45, 150, 35);//パスボタンの境界を設定
			bResign.addMouseListener(this);//マウス操作を認識できるようにする
			bResign.setActionCommand("resign");//ボタンを識別するための名前を付加する
			//黒駒の数用ラベル
			labelBlackNumber = new JLabel("黒　" + "2" + "枚");
			this.add(labelBlackNumber);
			labelBlackNumber.setBounds(6*w/11,  5*h/13, 300, 40);
			//白駒の数用ラベル
			labelWhiteNumber = new JLabel("白　" + "2" + "枚");
			this.add(labelWhiteNumber);
			labelWhiteNumber.setBounds(6*w/11, 6*h/13, 300, 40);
			//戦況ログ用テキストエリア 
			taLog = new JTextArea(350, 220);
			this.add(taLog);
			taLog.setBounds(6*w/11, 7*h/13, 350, 220);
			//modeがnetworkであるとき
			if(mode.equals("network")) {
				//色表示用ラベル
				colorLabel = new JLabel("あなたは"+"黒です");//色情報を表示するためのラベルを作成
				colorLabel.setBounds(w/5, 7*h/10, row * 45 + 10, 30);//境界を設定
				this.add(colorLabel);//色表示用ラベルをパネルに追加
				//手番表示用ラベル
				turnLabel = new JLabel("あなたの番です");//手番情報を表示するためのラベルを作成
				turnLabel.setBounds(w/5, 8*h/10, row * 45 + 10, 30);//境界を設定
				this.add(turnLabel);//手番情報ラベルをパネルに追加
				//プレイヤ情報用ラベル
				labelPlayer1 = new JLabel("プレイヤ名　" + "レート");
				this.add(labelPlayer1);
				labelPlayer1.setBounds(6*w/11, h/13, 300, 40);
				//vsラベル
				labelVS = new JLabel("vs");
				this.add(labelVS);
				labelVS.setBounds(7*w/11, 2*h/13, 300, 40);
				//相手情報表示用ラベル
				labelPlayer2 = new JLabel("プレイヤ名　" + "レート");
				this.add(labelPlayer2);
				labelPlayer2.setBounds(6*w/11, 3*h/13, 300, 40);
				//残り時間表示用ラベル
				labelTimer = new JLabel("**:**");
				this.add(labelTimer);
				labelTimer.setBounds(7*w/11, 5*h/13, 300, 40);
				//特殊効果発動是非表示用ラベル
				labelEffect = new JLabel("発動中特殊効果");
				this.add(labelEffect);
				labelEffect.setBounds(7*w/11, 6*h/13, 300 ,40);
			}
			//それ以外の時
			else {
				//色表示用ラベル
				colorLabel = new JLabel("あなたは黒です");
				colorLabel.setBounds(w/5, 3*h/4, row * 45 + 10, 30);//境界を設定
				this.add(colorLabel);//色表示用ラベルをパネルに追加
				//モード表示
				labelMode1 = new JLabel(mode);
				labelMode1.setBounds(6*w/11, 3*h/26, 300, 40);
				labelMode2 = new JLabel("モード");
				labelMode2.setBounds(6*w/11, 5*h/26, 300 ,40);
				this.add(labelMode1);
				this.add(labelMode2);
			}
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
			sendMessage(command); //テスト用にメッセージを送信
			if(theButton == bResign) {//投了ボタンクリック時
				CheckWhetherToResignSubDialog cwtrsd = new CheckWhetherToResignSubDialog(client, "cwtrsd");//確認用ダイアログ作成
				cwtrsd.setLocation(w/4,h/4);
				cwtrsd.setVisible(true);
			} else if(theButton == bOption) {//設定ボタンクリック時
				OptionSubDialog osd = new OptionSubDialog(client, "osd");//設定用ダイアログ作成
				osd.setLocation(w/6, h/6);
				osd.setVisible(true);
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//設定画面用パネル
	
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
			radioON1 = new JRadioButton("ON");
			this.add(radioON1);
			radioON1.setBounds(w/2, 2*h/7, 100, 40);
			radioON1.addActionListener(this);//マウス操作を認識できるようにする
			radioON1.setActionCommand("on1");//ボタンを識別するための名前を付加する
			//ONラジオボタン2
			radioON2 = new JRadioButton("ON");
			this.add(radioON2);
			radioON2.setBounds(w/2, 3*h/7, 100, 40);
			radioON2.addActionListener(this);//マウス操作を認識できるようにする
			radioON2.setActionCommand("on2");//ボタンを識別するための名前を付加する
			//ONラジオボタン3
			radioON3 = new JRadioButton("ON");
			this.add(radioON3);
			radioON3.setBounds(w/2, 4*h/7, 100, 40);
			radioON3.addActionListener(this);//マウス操作を認識できるようにする
			radioON3.setActionCommand("on3");//ボタンを識別するための名前を付加する
			//OFFラジオボタン1
			radioOFF1 = new JRadioButton("OFF");
			this.add(radioOFF1);
			radioOFF1.setBounds(7*w/10, 2*h/7, 100, 40);
			radioOFF1.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF1.setActionCommand("off1");//ボタンを識別するための名前を付加する
			//OFFラジオボタン2
			radioOFF2 = new JRadioButton("OFF");
			this.add(radioOFF2);
			radioOFF2.setBounds(7*w/10, 3*h/7, 100, 40);
			radioOFF2.addActionListener(this);//マウス操作を認識できるようにする
			radioOFF2.setActionCommand("off2");//ボタンを識別するための名前を付加する
			//OFFラジオボタン3
			radioOFF3 = new JRadioButton("OFF");
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
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//投了確認画面用サブダイアログ
	
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
				EndMatchingSubDialog emsd = new EndMatchingSubDialog(client, "emsd");//対戦終了時ダイアログ作成
				emsd.setLocation(client.w/6, client.h/6);
				emsd.setVisible(true);
				
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//再戦希望集計中画面用パネル
	
	class TotalizingSubDialog extends JDialog implements ActionListener{//再戦希望集計中パネル
		JLabel labelMessage;//メッセージラベル
		JButton bCan;//キャンセルボタン
		Client client;
		String str;
		int w = 480;
		int h = 320;
		//コンストラクタ
		TotalizingSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//メッセージ用ラベル
			labelMessage = new JLabel("再戦希望を集計中です...");
			this.add(labelMessage);
			labelMessage.setBounds(w*3/8, 5*h/11, 300, 40);
			//キャンセルボタン
			bCan = new JButton("キャンセル");
			this.add(bCan);
			bCan.setBounds(22*w/56, 8*h/11, 120, 40);
			bCan.addActionListener(this);//マウス操作を認識できるようにする
			bCan.setActionCommand("cancel");//ボタンを識別するための名前を付加する
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			dispose();//このダイアログを破棄
			EndMatchingSubDialog emsd = new EndMatchingSubDialog(client, "emsd");//対局終了時ダイアログの作成
			emsd.setLocation(client.w/6, client.h/6);
			emsd.setVisible(true);
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}
	//再戦不可時用パネル
	
	class UnableToRematchSubDialog extends TotalizingSubDialog{//再戦不可時ダイアログ
		JButton bOK;//OKボタン
		UnableToRematchSubDialog(Client c, String s){
			super(c ,s);
			//メッセージ用ラベル
			this.remove(labelMessage);
			labelMessage = new JLabel("対戦相手が再戦拒否しました");
			this.add(labelMessage);
			labelMessage.setBounds(w*3/8, 5*h/11, 300, 40);
			//OKボタン
			bOK = new JButton("OK");
			this.remove(bCan);
			this.add(bOK);
			bOK.setBounds(22*w/56, 8*h/11, 120, 40);
		}
		//マウスクリック時の処理
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	//対戦終了時パネル
	
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
		EndMatchingSubDialog(Client c, String s){
			client = c;
			str = s;
			//ウィンドウ設定
			this.setSize(w, h);
			this.setLayout(null);
			//結果用ラベル
			labelResult = new JLabel("YOU WIN!");
			this.add(labelResult);
			labelResult.setBounds(2*w/5, h/7, 300, 50);
			//プレイヤ1情報用ラベル
			labelPlayer1 = new JLabel("player1" + " ○ " + "43");
			this.add(labelPlayer1);
			labelPlayer1.setBounds(2*w/7, 2*h/7, 300, 30);
			//プレイヤ1のレート用ラベル
			labelRate1 = new JLabel("1664" + "(+" + "123" + ")*");
			this.add(labelRate1);
			labelRate1.setBounds(2*w/7, 3*h/7, 300, 30);
			//プレイヤ2情報用ラベル
			labelPlayer2 = new JLabel("player2" + " ● " + "21");
			this.add(labelPlayer2);
			labelPlayer2.setBounds(2*w/7, 4*h/7, 300, 30);
			//プレイヤ2のレート用ラベル
			labelRate2 = new JLabel("1435" + "(-" + "63" + ")*");
			this.add(labelRate2);
			labelRate2.setBounds(2*w/7, 5*h/7, 300, 30);
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
				TotalizingSubDialog tsd = new TotalizingSubDialog(client, "tsd");//再戦希望集計時ダイアログの作成
				tsd.setLocation(client.w/6, client.h/6);
				tsd.setVisible(true);
			} else {//戻るボタンクリック時
				dispose();//破棄
				client.mp.setVisible(false);//対局パネルの非表示
				client.PanelChange((JPanel)client.mp, (JPanel)client.mfnmp);//ネットワーク対局用メニュー画面パネルの作成
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
		cont.add(sp, BorderLayout.CENTER);
		
		sp.setVisible(true);
	}
	
	//メソッド
	//画面遷移関数
	public void PanelChange(JPanel jpF, JPanel jpT) {//画面をjpFからjpTへ遷移
		//遷移元
		if(jpF.getName().equals("sp")) {
			cont.remove(sp);
		} else if (jpF.getName().equals("ssp")) {
			cont.remove(ssp);
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
		} else if (jpT.getName().equals("ssp")){
			cont.add(ssp, BorderLayout.CENTER);
			ssp.setVisible(true);
		} else if(jpT.getName().equals("sp")) {
			cont.add(sp, BorderLayout.CENTER);
			sp.setVisible(true);
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
	
	public void makeAccount(String name, String password) {//アカウントの作成
		sendMessage("makeAccount");//目的宣言
		sendMessage(name);
		sendMessage(password);
		sendMessage("end");//終了サイン
	}
	
	public boolean Login(String id, String password) {//ログイン認証
		sendMessage("login");
		sendMessage(id);
		sendMessage(password);
		sendMessage("end");
		
		flag = false;//receiverがログイン認証結果が完了したらtrue
		result = false;//ログイン認証結果が成功ならtrue
		int i = 0;
		
		while(++i<10) {
			try {
				Thread.sleep(1000);
				if(flag) {
					flag = false;
					return result;
				}
			}
			catch(InterruptedException e) {}
		}
		return result;
	}
	
	public void connectServer(String ipAddress, int port){	// サーバに接続
		Socket socket = null;
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

	//public boolean sendConfirmationForConnection(){} サーバに接続確認のための信号を送信
	
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
			try{
				while(true) {//データを受信し続ける
					String inputLine = br.readLine();//目的を読み込む
					if(inputLine.equals("sendAccountInfo")) {//目的がアカウント情報の送信だった場合
						int i=0;
						String accountInfo[] = new String[3];//アカウント情報用配列
						receiveMessage(inputLine);//データ受信用メソッドを呼び出す
						while(true) {//データを受信し続ける
							inputLine = br.readLine();//１行読み込む
							if(!inputLine.equals("end")) {//終了サインじゃなかったら
								accountInfo[i] = inputLine;//アカウント情報用配列に格納
								i++;
							}
							else break;
						}
						//oclient.player = new Player(accountInfo[0], accountInfo[1], accountInfo[2]);
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
				}
			} catch (IOException e){
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
	}

	public void receiveMessage(String msg){	// メッセージの受信
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
	}
	public void updateDisp(){	// 画面を更新する
	}
	public void acceptOperation(String command){	// プレイヤの操作を受付
		//時間を同時に測る
	}
	public int CalculateRate(int rateB, int rate1, int rate2, int result) { //レート計算
		if(result == 0) {//敗北時
			return rateB - (int)(16+0.04*(rate1-rate2));
		} else {//勝利時
			return rateB + (int)(16+0.04*(rate2-rate1));
		}
	}
  	

	//main
	public static void main(String args[]){ 
		Client oclient = new Client();
		oclient.setVisible(true);
		oclient.connectServer("localhost", 10000);
	}
}