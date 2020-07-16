import java.io.BufferedReader;//テキストファイルを読み込むためのクラス,1行ずつ読み込むreadlineメソッドが用意されています。
//テキストファイルなどの文字コードを指定して読み込むためには、
//InputStreamReader及びFileInputStreamクラスを拡張したBufferedReaderクラスを使用します。
import java.io.IOException;
import java.io.InputStreamReader;//指定したストリームを、指定した文字コードで構成されるテキストファイルとして読み込みます
import java.io.PrintWriter;//文字コードを指定してファイルに書き込みを行います。
//一例として、BufferedWriter、OutputStreamWriter及びFileOutputStreamクラスを拡張した、PrintWriterクラス
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class Server{
	private static final java.lang.String String = null;
	private int port; // サーバの待ち受けポート、ネットワークに接続されたマシンは、
	//IPアドレスとポートの組み合わせ(これをソケットといいます)で識別されます。
	private boolean [] online; //オンライン状態管理用配列
	private PrintWriter[] out; //データ送信用オブジェクト
	private Receiver [] receiver; //データ受信用オブジェクト
    public int clientnum;
    HashMap <String,Player> Playerslist = new HashMap <String,Player>();

	//コンストラクタ
	public Server(int port) { //待ち受けポートを引数とする
		this.port = port; //待ち受けポートを渡す
		out = new PrintWriter [2]; //データ送信用オブジェクトを2クライアント分用意
		receiver = new Receiver [2]; //データ受信用オブジェクトを2クライアント分用意
		online = new boolean[100]; //オンライン状態管理用配列を用意
	}

	// データ受信用 スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ
		//private PrintWriter pw;
		private int playerNo; //プレイヤを識別するための番号


		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket, int playerNo){
			try{
				this.playerNo = playerNo; //プレイヤ番号を渡す
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));//文字ストリームをバッファリングする
//socket.getInputStream(),この接続からの入力を受け取る入力ストリームを返します。
				//out[playerNo[i]]  = new PrintWriter(br,true);
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
					System.out.println(playerNo + "から" + inputLine + "を受け取りました。");

				    if(inputLine.equals("makeAccount")) {//目的が「アカウント作成」   OK
				    	String user[] = new String[2];//id,password
				    	int i=0;
						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								user[i] = inputLine;
								i++;
							}else {
								break;
							}
						}registerUser(user[0],user[1]);//sendAccountInfo,[0]name,[1]password,end　を返す

					}
				    else if(inputLine.equals("login")) {//目的が「ログイン認証要請」　　　OK
						//ここで「合致データ確認メソッド」を入れる、[ある時]「ユーザ情報転送メソッド」,「戦ラ送信メソッド」で送る

						int i=0;
						String accountInfo[] = new String[2];//アカウント情報用配列,id,password
						Player player;//自分のユーザ情報を呼び出す、ための照らし合わせ

						while(true) {//データを受信し続ける
							inputLine = br.readLine();//１行読み込む
							if(!inputLine.equals("end")) {//終了サインじゃなかったら
								accountInfo[i] = inputLine;//アカウント情報用配列に格納
								i++;
							}
							else {
								break;
							}
						}
						confirmDate(accountInfo[0],accountInfo[1],player);//idとpassword

					}

					else if(inputLine.equals("findingOpponent")) {//目的が「対戦相手とのマッチング」
		//この時、相手の戦ラも必要だよな？、「ユーザ情報転送メソッド」と「戦ラ送信メソッド」
						//アカウント作成の時のIDの送信もある、その時「ユーザ登録メソッド」と「ユーザ情報転送メソッド」


						Player player;
						sendMessage("sendFindingResult",player.getMyPlayerNo());
						int opponentPlayerNo = player.getOpponentPlayerNo();//相手のplayerNo


					}

					else if(inputLine!=null) {//目的が「操作情報の送信」
						int i=0;
						String operationInfo[] = new String[2];//アカウント情報用配列

						while(true) {//データを受信し続ける
							inputLine = br.readLine();//１行読み込む
							if(!inputLine.equals("end")) {//終了サインじゃなかったら
								operationInfo[i] = inputLine;//アカウント情報用配列に格納
								i++;
							}
							else {
								break;
							}
						}Player player;
						forwardMessage(Integer.parseInt(operationInfo[0]),Integer.parseInt(operationInfo[1]));//x,y座標
					}


					else if(inputLine.equals("noticeEndMatching")) {//対局終了時をクライアントからサーバに知らせる  OK
						int i=0;
						String operationInfo[] = new String[2];//アカウント情報用配列
						Player player;
						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								operationInfo[i] = inputLine;//アカウント情報用配列に格納
								i++;
							}
							else {
								break;
							}
						}
						String id=player.getID();//レート更新
						Player value = Playerslist.get(id);
						int r[] = new int[6];
						r = value.getRecord();
						r[5] = Integer.parseInt(operationInfo[1]);
						value.setRecord(r);//レート更新終わり
						Playerslist.replace(id, value);

						if(operationInfo[0]=="timeup" || operationInfo[0]=="resign") {//相手に投了、時間切れ送信
							sendMessage("noticeEndMatching",player.getOpponentPlayerNo());
							sendMessage(operationInfo[0],player.getOpponentPlayerNo());
							sendMessage("end",player.getOpponentPlayerNo());
						}
					}

					else if(inputLine.equals("sendRate")) {//上でクライアントからレートもらって更新  OK

						while(true) {
							inputLine = br.readLine();

							Object id;//レート更新
							Player value = Playerslist.get(id);
							int r[] = new int[6];
							r = value.getRecord();
							r[5] = Integer.parseInt(inputLine);
							value.setRecord(r);//レート更新終わり
							Playerslist.replace(String,value);
						}
					}
					else if(inputLine.equals("getRecord")) {
						Player player;
						sendMessage("sendRecord",player.getMyPlayerNo());

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
			int i=0;
			int playerNo =i;
			online[playerNo]=true;//接続中
			while (true) {
				Socket socket = ss.accept();//新規接続を受け付ける
				System.out.println("クライアントと接続しました。");
				out[playerNo]  = new PrintWriter(socket.getOutputStream(), true);//データ送信オブジェクトを用意
				receiver[playerNo] = new Receiver(socket,playerNo);//データ受信オブジェクト(スレッド)を用意
				receiver[playerNo] .start();//データ送信オブジェクト(スレッド)を起動
				receiver[i]=new Receiver(socket,playerNo);
				i++;
			}


		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}

	public void printStatus(int playerNo){ //クライアント接続状態の確認   OK
		if(online[playerNo]=true) {
			System.out.println("接続中");
		}else {
			System.out.println("接続なし");
		}
	}

	public int sendColor(int id1,int id2,int playerNo){ //先手後手情報(白黒)の送信
        if(id1>id2) {
	         return -1;//黒、先手?
        }else {
	         return 1;//白、後手?
        }
    }

	public void forwardMessage(int x,int y,Player player){ //操作情報の転送、盤面置いた部分を受け流す　　OK

		sendMessage("sendOperation",player.getOpponentPlayerNo());//相手に送る
		sendMessage(Integer.toString(x),player.getOpponentPlayerNo());//x座標
		sendMessage(Integer.toString(y),player.getOpponentPlayerNo());//y座標
		sendMessage("end",player.getOpponentPlayerNo());

	}

    public void collectGrade() {//戦績・ランキング情報の集計
//レート,プレイヤ名の配列情報から降順でソートする全体ランキング
//総戦績、投了数、対戦上位10名、ランキングの位とレート、全体のランキング
    }

    public void sendGrade(String[][] rank,Player player , Player player) {//戦績・ランキング情報の送信
        Map<String, Player> playerlist = new HashMap<String, Player>();

        // 2.Map.Entryのリストを作成する
        List<Entry<String, Player>> list_entries = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());

        // 3.比較関数Comparatorを使用してMap.Entryの値を比較する(昇順)
        Collections.sort(list_entries, new Comparator<Entry<String, Player>>() {
            public int compare(Entry<String, Player> obj1, Entry<String, Player> obj2) {
                // 4. 昇順
                return obj1.getValue().compareTo(obj2.getValue());
            }
        });

        System.out.println("昇順でのソート");
        // 5. ループで要素順に値を取得する
        for(Entry<String, Player> entry : list_entries) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

    	//上の情報を送るだけ
    }

    public void registerUser(String name,String password) {//ユーザ情報の登録,IDを与えてサーバで保存する  OK
    	Random rand = new Random();
        int num = rand.nextInt(90000000) + 10000000;
        int id = num;//ランダム文字列でIDを与える
        Player player = new Player(name, Integer.toString(id), password);//Playerクラス呼び出し
        Playerslist.put( Integer.toString(id), player);//HashMapの Playerslistにkeyはid,valueはplayer

        //0.名前　1.パスワード　2.戦　3.勝　4.負　5.分　6.勝率(%)　7.投了数　8.レート　9.ランキング　10.自分のplayerNo　11.相手のplayerNo


        sendMessage("sendAccountInfo",player.getMyPlayerNo());      //(msg,送る先)
		sendMessage(name,player.getMyPlayerNo());
		sendMessage(password,player.getMyPlayerNo());
		sendMessage(Integer.toString(id),player.getMyPlayerNo());
		sendMessage("end",player.getMyPlayerNo());
    }

    public void forwardUser(Player player) {//ユーザ情報の転送     OK
    	sendMessage(Integer.toString(player.getRecord()[0]),player.getMyPlayerNo());//戦
    	sendMessage(Integer.toString(player.getRecord()[1]),player.getMyPlayerNo());//勝
    	sendMessage(Integer.toString(player.getRecord()[2]),player.getMyPlayerNo());//負
    	sendMessage(Integer.toString(player.getRecord()[3]),player.getMyPlayerNo());//分
    	sendMessage(Integer.toString(player.getRecord()[4]),player.getMyPlayerNo());//投了数
    	sendMessage(Integer.toString(player.getRecord()[5]),player.getMyPlayerNo());//レート


    	//テスト標準出力//、対戦時の相手の情報の転送
    }

    public void confirmDate(String id,String password,Player player) {//合致データの確認　　　OK
    	//ログイン時の転送（下のconfirmDateでデータ[ある時]送信,[ない時]エラーメッセージ送信）
    	if(player.getID() == id && player.getPass() == password) {
    		sendMessage("sendLoginResult",player.getMyPlayerNo());
    		sendMessage("succeeded",player.getMyPlayerNo());
    		forwardUser(player);//上の「ユーザ情報の転送」メソッド
    		sendMessage("end",player.getMyPlayerNo());
    	}else{
    		sendMessage("sendLoginResult",player.getMyPlayerNo());
    		sendMessage("failed",player.getMyPlayerNo());
    		sendMessage("end",player.getMyPlayerNo());
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

    }







    public void sendMessage(String msg,int playerNo){
		out[playerNo].println(msg);//送信データをバッファに書き出す
		out[playerNo].flush();//送信データを送る
		System.out.println(playerNo + "にメッセージ " + msg + " を送信しました"); //テスト標準出力
	}


	public static void main(String[] args){ //main
		Server server = new Server(1111); //待ち受けポート10000番でサーバオブジェクトを準備
		server.acceptClient(); //クライアント受け入れを開始

	}
}