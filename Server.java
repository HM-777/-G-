import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server{
	private static final String[][] String = null;
	private int port; // サーバの待ち受けポート
	private boolean [] online; //オンライン状態管理用配列
	private PrintWriter [] out; //データ送信用オブジェクト
	private Receiver [] receiver; //データ受信用オブジェクト
    public int clientnum;
	//コンストラクタ
	public Server(int port) { //待ち受けポートを引数とする
		this.port = port; //待ち受けポートを渡す
		out = new PrintWriter [2]; //データ送信用オブジェクトを2クライアント分用意
		receiver = new Receiver [2]; //データ受信用オブジェクトを2クライアント分用意
		online = new boolean[2]; //オンライン状態管理用配列を用意
	}

	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ
		private int playerNo=0; //プレイヤを識別するための番号
		private boolean flag;
		private boolean result;

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket, int playerNo){
			try{
				this.playerNo = playerNo; //プレイヤ番号を渡す
				sisr = new InputStreamReader(socket.getInputStream());//受信したバイトデータを文字ストリームに
				br = new BufferedReader(sisr);//文字ストリームをバッファリングする

			} catch (IOException e) {
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド、ここで受信する系は受信してメソッド「」を呼び出す
		//そのメッドの中で集計だったりする流れ
		public void run(){
			try{
				while(true) {// データを受信し続ける
					String inputLine = br.readLine();//データを一行分読み込む、目的を読み込む

				    if(inputLine.equals("makeAccount")) {//目的がアカウント作成だった場合
				    	String user[] = new String[2];
				    	int i=0;
						//ここで「合致データ確認メソッド」を入れる、[ある時]「ユーザ情報転送メソッド」,「戦ラ送信メソッド」で送る
						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								user[i] = inputLine;
								i++;
							}else {
								break;
							}
						}registerUser(user[0],user[1]);

					}
				    else if(inputLine.equals("login")) {//目的がログイン認証要請の送信だった場合
						//ここで「合致データ確認メソッド」を入れる、[ある時]「ユーザ情報転送メソッド」,「戦ラ送信メソッド」で送る

						int i=0;
						String accountInfo[] = new String[3];//アカウント情報用配列

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

					else if(inputLine.equals("findingOpponent")) {//目的が対戦の時の相手のユーザ情報の送信だった場合、
		//この時、相手の戦ラも必要だよな？、「ユーザ情報転送メソッド」と「戦ラ送信メソッド」
						//アカウント作成の時のIDの送信もある、その時「ユーザ登録メソッド」と「ユーザ情報転送メソッド」
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

					else if(inputLine.equals("sendOperation")) {//目的が操作情報の送信だった場合
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


					else if(inputLine.equals("noticeEndMatching")) {//対局終了時をクライアントからサーバに知らせる

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
					else if(inputLine.equals("戦績")) {//目的が戦績の送信だった場合、これは分けるの？

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
			} catch (IOException e){ // 接続が切れたとき
				System.err.println("プレイヤ " + playerNo + "との接続が切れました．");
				online[playerNo] = false; //プレイヤの接続状態を更新する
				printStatus(playerNo); //接続状態を出力する
			}
		}
	}

	// メソッド

	public void acceptClient(){ //クライアントの接続(サーバの起動)
		try {
			System.out.println("サーバが起動しました．");
			ServerSocket ss = new ServerSocket(port); //ポートにバインドされたサーバソケットを用意ServerSocketはクラス
			while (true) {
				Socket socket = ss.accept();//新規接続を受け付ける
				clientnum++;
				if(clientnum % 2==0) {
				break;
				}

			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}

	public int printStatus(int clientnum){ //クライアント接続状態の確認int playerNo、常に動いてる
		if(clientnum==2) {
			return 1;//接続中,言葉なのか数字なのか
		}else return 0;//接続切断
	}

	public String sendColor(int id1,int id2){ //先手後手情報(白黒)の送信
        if(id1>id2) {
	         return "白";//数字で送るのか
        }else {
	         return "黒";
        }
    }

	public String forwardMessage(int[][] msg, int playerNo){ //操作情報の転送、置いた部分を受け流す
		if() {//1から来たなら2に流す
			return msg;//4-5白みたいな情報
		}else return msg;//2から来たなら1に流す
	}

    public void collectGrade() {//戦績・ランキング情報の集計
//レート,プレイヤ名の配列情報から降順でソートする全体ランキング
//総戦績、投了数、対戦上位10名、ランキングの位とレート、全体のランキング
    }

    public void sendGrade(String[][] rank) {//戦績・ランキング情報の送信

    	//上の情報を送るだけ
    }

    public void registerUser(String name,String passward) {//ユーザ情報の登録,IDを与えてサーバで保存する
    	Random rand = new Random();
        int num = rand.nextInt(90000000) + 10000000;
        int id = num;//ランダム文字列でIDを与える？
        String a[][][] =new String[90000000][10][1000000];
        //二次元配列の要素一次元目はID、そのIDの紐付け情報が二次元目。
        //0.名前　1.パスワード　2.戦　3.勝　4.敗　5.%　6.投了数　7.レート　8.ランキング　9.相手のID
        a[id][0][0]=name;
        a[id][1][0]=passward;
        a[id][2][0]=Integer.toString(0);
        a[id][3][0]= Integer.toString(0);
        a[id][4][0]= Integer.toString(0);
        a[id][5][0]= Integer.toString(0);
        a[id][6][0]= Integer.toString(0);
        a[id][7][0]= Integer.toString(0);
        a[id][8][0]= Integer.toString(1500);
        a[id][9][0]= Integer.toString(0);//「集計メソッド」


        System.out.println("あなたのIDは "+ id + " です。");
        sendMessage("sendAccountInfo");
		sendMessage(name);
		sendMessage(passward);
		sendMessage(Integer.toString(id));
    }

    public void forwardUser(String msg) {//ユーザ情報の転送
    	out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
    	//ログイン時の転送（下のconfirmDateでデータ[ある時]送信,[ない時]エラーメッセージ送信）
    	//テスト標準出力//、対戦時の相手の情報の転送
    }

    public void confirmDate(String name,String passward) {//合致データの確認
//IDとパスから線形探索で探す

        int a[] = {66,2,10,43,45,52,73,65,12,39,97,76,83,11,57,63};
        System.out.print("ユーザ名とパスワードを入力して下さい: ");

        int pos = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i].name == name && a[i].passward == passward) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            System.out.println("見つかりません");
        } else {
            System.out.println();//ここでユーザ情報与えるけど書き出すのか、結びついたセット的なのないのか？
        }

    }

    public int collectReplay(int replay1,int replay2) {//再戦希望の集計
//00（戻る）,01（相手が再戦希望しませんでした）,11（再戦）で場合分け
    	if(replay1 == 1 && replay2 == 1) {
    		return 1;//再戦しないことを示す1
    	}else {
    		return 0;//再戦しないことを示す0
    	}
    }


    public void forwardReplay(String msg) {//再戦希望の転送
//クライアントから受け取って、collectReplay(受信)
//結果をクライアントに(送信)
//やることなし？それか再戦希望の文字を0,1にして集計メソッド呼び出して戻り値、それを文字にしてSystem.out() ?

    }


    public void sendMessage(String msg){	// サーバに情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
	}


	public static void main(String[] args){ //main
		Server server = new Server(1114); //待ち受けポート10000番でサーバオブジェクトを準備
		server.acceptClient(); //クライアント受け入れを開始
	}
}