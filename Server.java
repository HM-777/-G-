import java.io.BufferedReader;
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
import java.util.Map.Entry;
import java.util.Random;

public class Server{
	private static final java.lang.String String = null;
	private int port;
//	private boolean online;
	private PrintWriter[] out; //データ送信用オブジェクト
	private Receiver [] receiver; //データ受信用オブジェクト
    HashMap <String,Player> Playerslist = new HashMap <String,Player>();
    //keyはid,valueはそのPlayerクラスで情報管理
    HashMap <String,Integer> Ratelist = new HashMap <String,Integer>();
    //keyはid,valueはそのレートでランキング作成の際に利用する

    int[] MatchingList = new int[2];//レート戦マッチングルーム
    int[] SpecialList = new int[2];//スペシャル戦マッチングルーム
    int[] ReMatchingList = new int[2];//再戦マッチングルーム
    boolean flagMatched=false;//ルームが埋まってるか
    boolean flagSpecialMatched=false;
    boolean flagReMatched=false;
    private Player[] player=new Player[1000000];


	//コンストラクタ
	public Server(int port) { //待ち受けポートを引数とする
		this.port = port; //待ち受けポートを渡す
		out = new PrintWriter [100]; //データ送信用オブジェクトを2クライアント分用意
		receiver = new Receiver [100]; //データ受信用オブジェクトを2クライアント分用意
		MatchingList[0] = -1;
	    MatchingList[1] = -1;
	    SpecialList[0] = -1;
	    SpecialList[1] = -1;
	    ReMatchingList[0] = -1;
	    ReMatchingList[1] = -1;
	}

	// データ受信用 スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ
		private int playerNo; //プレイヤを識別するための番号
		private int opponentplayerNo;
		private int id;
		private int i;
		private boolean matchingFlag=false;


		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket, int playerNo){
			try{
				this.playerNo = playerNo; //プレイヤ番号を渡す
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			} catch (IOException e) {
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド、ここで受信してメソッド「」を呼び出す
		public void run(){
			try{
				while(true) {// データを受信し続ける
					String inputLine = br.readLine();//データを一行分読み込む、目的を読み込む
					System.out.println(playerNo + "から" + inputLine + "を受け取りました。");

					if(inputLine==null){
						if(matchingFlag){
							matchingFlag=false;
							sendMessage("noticeEndMatching",player[playerNo].getOpponentPlayerNo());
							sendMessage("resign",player[playerNo].getOpponentPlayerNo());
							sendMessage("end",player[playerNo].getOpponentPlayerNo());
							int[] r = new int [6];
							r=player[playerNo].getRecord();
							r[0]++;
							r[2]++;
							r[4]++;
							r[5]=player[playerNo].getRecord()[5] - (int)(16+0.04*(player[playerNo].getRecord()[5]-player[player[playerNo].getOpponentPlayerNo()].getRecord()[5]));
							player[playerNo].setRecord(r);
						}
//						online[id]=false;
						Playerslist.get(player[playerNo].getID()).setOnline(false);
						List<Entry<String, Player>> list = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());

					    for(Entry<String, Player> entry : list) {
					      if(entry.getValue().getOnline())
					          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続中");
					      else
					          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続なし");
					    }
						break;
					}

				    if(inputLine.equals("makeAccount")) {//目的が「アカウント作成」

				    	String user[] = new String[2];//id,password
				    	i=0;
						while(true) {//データを受信し続ける
							inputLine = br.readLine();//１行読み込む
							if(!inputLine.equals("end")) {//終了サインじゃなかったら
								user[i] = inputLine;
								i++;
							}else {
								break;
							}
						}
						registerUser(user[0],user[1]);//[0]name,[1]password
						//{sendAccountInfo,name,password,id,end}を返す

					}
				    else if(inputLine.equals("login")) {//目的が「ログイン認証要請」
				    	//player[playerNo] = Playerslist.get(id);
						i=0;
						String user[] = new String[2];


						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								user[i] = inputLine;
								i++;
							}
							else {
								break;
							}
						}
						confirmData(user[0],user[1]);//,[0]id,[1]password
						//{sendLoginResult,succeed or failed,end}を返す

					}

				    else if(inputLine.equals("findingOpponent")) {//目的が「対戦相手とのマッチング」

						//player[playerNo] = Playerslist.get(id);
						String mode= br.readLine();//rank or special
						sendMessage("sendFindingResult",player[playerNo].getMyPlayerNo());
						flagMatched=false;
						flagSpecialMatched=false;

						System.out.println("待機中");
						if(mode.equals("rank")) {
							addMatchingList(player[playerNo].getMyPlayerNo(),"rank");//(player,src)
							//この中で{sendFindingResult,succeeded,先手後手,相手のname,rate}を返す
							int t = 60;
							while(true){
								if(flagMatched == true){
									matchingFlag=true;
									break;
								}
								try {
									Thread.sleep(1000);
									t--;

								}catch(InterruptedException e){

								}
								if(t == 0) {
									removeMatchingList();

									sendMessage("failed",player[playerNo].getMyPlayerNo());//failed送信
									break;
								}
							}
						}else {//special
							addMatchingList(player[playerNo].getMyPlayerNo(),"special");
							//この中で{sendFindingResult,succeeded,先手後手,相手のname,rate}を返す
							int t = 60;
							while(true){
								if(flagSpecialMatched == true){
									matchingFlag=true;
									break;
								}
								try {
									Thread.sleep(1000);
									t--;

								}catch(InterruptedException e){

								}
								if(t == 0) {
									removeSpecialList();

									sendMessage("failed",player[playerNo].getMyPlayerNo());//failed送信
									break;
								}
							}
						}

						//removeMatchingList();//MatchingListから外す
						sendMessage("end",player[playerNo].getMyPlayerNo());

					}

					else if(inputLine.equals("sendOperation")) {//目的が「操作情報の送信」
						i=0;
						String user[] = new String[2];

						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								user[i] = inputLine;
								i++;
							}
							else {
								break;
							}
						}
						forwardMessage(Integer.parseInt(user[0]),Integer.parseInt(user[1]));//[0]x座標,[1]y座標
					}


					else if(inputLine.equals("noticeEndMatching")) {//目的が「対局終了時をクライアントからサーバに知らせる」
						i=0;
						matchingFlag=false;
						String user[] = new String[2];//アカウント情報用配列

						while(true) {
							inputLine = br.readLine();
							if(!inputLine.equals("end")) {
								user[i] = inputLine;//アカウント情報用配列に格納
								i++;
							}
							else {
								break;
							}
						}
						String id=player[playerNo].getID();//レート更新
						Player value = Playerslist.get(id);
						int r[] = new int[6];
						r = value.getRecord();
						r[5] = Integer.parseInt(user[1]);
						value.setRecord(r);
						Playerslist.replace(id, value);//レート更新完了

						if(user[0].equals("timeup") || user[0].equals("resign")) {//相手に投了、時間切れ送信
							sendMessage("noticeEndMatching",player[playerNo].getOpponentPlayerNo());
							sendMessage(user[0],player[playerNo].getOpponentPlayerNo());
							sendMessage("break",player[playerNo].getOpponentPlayerNo());
							sendMessage("end",player[playerNo].getOpponentPlayerNo());
						}
					}

					else if(inputLine.equals("getRecord")) {//目的が「ランキング送信」
						sendMessage("sendRecord",player[playerNo].getMyPlayerNo());
						sendGrade();
					}

					else if(inputLine.equals("Rematch")) {//目的が「再戦」
						//String mode=br.readLine();
						sendMessage("sendRematchResult",player[playerNo].getMyPlayerNo());
						System.out.println("待機中");
						addReMatchingList(player[playerNo].getMyPlayerNo());

						int t = 10;
						//flagReMatched = false;

						while(true){
			              if(flagReMatched == true){
			                matchingFlag=true;
			                break;
			              }
							try {
								Thread.sleep(1000);
								t--;

							}catch(InterruptedException e){

							}
							if(t == 0) {
								removeReMatchingList();

								sendMessage("failed",player[playerNo].getMyPlayerNo());//failed送信
								break;
							}

						}
						sendMessage("end",player[playerNo].getMyPlayerNo());
					}
				}
			} catch (IOException e){ // 接続が切れたとき
				System.err.println("プレイヤ " + playerNo + "との接続が切れました．");
				Playerslist.get(player[playerNo].getID()).setOnline(false);

				List<Entry<String, Player>> list = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());

			    for(Entry<String, Player> entry : list) {
			      if(entry.getValue().getOnline())
			          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続中");
			      else
			          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続なし");
			    }
			}
		}

		// メソッド
		public void registerUser(String name,String password) {//ユーザ情報の登録
	    	Random rand = new Random();
	        int num = rand.nextInt(90000000) + 10000000;
	        id = num;//ランダム文字列でIDを与える
	        List<Entry<String, Player>> list = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());

		    for(Entry<String, Player> entry : list) {
		      if(entry.getValue().getOnline())
		          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続中");
		      else
		          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続なし");
		    }
	        player[playerNo] = new Player(name, Integer.toString(id), password);
	        int[]r=new int[6];
	        for(int a=0;a<5;a++) {
	        	r[a]=0;
	        }
	        r[5]=1500;
	        player[playerNo].setRecord(r);
	        Playerslist.put( Integer.toString(id), player[playerNo]);//HashMapの Playerslistにkeyはid,valueはPlayerクラス
	        Ratelist.put( Integer.toString(id), player[playerNo].getRecord()[5]);

	        player[playerNo].setMyPlayerNo(playerNo);
	        sendMessage("sendAccountInfo",player[playerNo].getMyPlayerNo());
			sendMessage(name,player[playerNo].getMyPlayerNo());
			sendMessage(Integer.toString(id),player[playerNo].getMyPlayerNo());
			sendMessage(password,player[playerNo].getMyPlayerNo());
			sendMessage("end",player[playerNo].getMyPlayerNo());
	    }


		 public void confirmData(String id,String password) {//合致データの確認
			    	player[playerNo] = Playerslist.get(id);//idをkeyとするplayer(value)を呼び出す
			    	player[playerNo].setMyPlayerNo(playerNo);//playerNoをセットする

			    	List<Entry<String, Player>> list = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());

				    if(Playerslist.get(id).getOnline() == true) {
				    	sendMessage("failed",player[playerNo].getMyPlayerNo());
				    }else {
				    	Playerslist.get(id).setOnline(true);
				    	if(player[playerNo].getPass().equals(password)) {//そのidのpassが一致してた時
				    		sendMessage("sendLoginResult",player[playerNo].getMyPlayerNo());
				    		sendMessage("succeeded",player[playerNo].getMyPlayerNo());
				    		forwardUser(playerNo);//下の「ユーザ情報の送信」メソッド
				    		sendMessage("end",player[playerNo].getMyPlayerNo());
				    	}else{
				    		sendMessage("sendLoginResult",player[playerNo].getMyPlayerNo());
				    		sendMessage("failed",player[playerNo].getMyPlayerNo());
				    		sendMessage("end",player[playerNo].getMyPlayerNo());
				    	}
				    	for(Entry<String, Player> entry : list) {
						      if(entry.getValue().getOnline())
						          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続中");
						      else
						          System.out.println(entry.getValue().getID()+entry.getValue().getName()+":接続なし");
						    }
				    }
		    }

		 public void forwardUser(int playerNo) {//ユーザ情報の送信
			    sendMessage(player[playerNo].getName(),player[playerNo].getMyPlayerNo());//name
				sendMessage(player[playerNo].getID(),player[playerNo].getMyPlayerNo());//id
				sendMessage(player[playerNo].getPass(),player[playerNo].getMyPlayerNo());//password
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[0]),player[playerNo].getMyPlayerNo());//戦
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[1]),player[playerNo].getMyPlayerNo());//勝
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[2]),player[playerNo].getMyPlayerNo());//負
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[3]),player[playerNo].getMyPlayerNo());//分
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[4]),player[playerNo].getMyPlayerNo());//投了数
		    	sendMessage(Integer.toString(player[playerNo].getRecord()[5]),player[playerNo].getMyPlayerNo());//レート
		    }


		public void forwardMessage(int x,int y){ //操作情報の転送
			sendMessage("sendOperation",player[playerNo].getOpponentPlayerNo());
			sendMessage(Integer.toString(x),player[playerNo].getOpponentPlayerNo());//x座標
			sendMessage(Integer.toString(y),player[playerNo].getOpponentPlayerNo());//y座標
			sendMessage("end",player[playerNo].getOpponentPlayerNo());
		}

	    public void sendGrade() {//ランキング情報の送信
	    	//HashMap <String,Integer> Ratelist = new HashMap <String,Integer>();
	    	List<Entry<String, Player>> list = new ArrayList<Entry<String, Player>>(Playerslist.entrySet());
	    	for(Entry<String, Player> entry : list) {
	    		Ratelist.put(entry.getKey(),entry.getValue().getRecord()[5]);
	    	}
	    	List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(Ratelist.entrySet());
	    	Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
	            //compareを使用して値を比較する
	            public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2)
	            {
	                //降順
	                return obj2.getValue().compareTo(obj1.getValue());
	            }
	    	});
	    	int count=0;
	    	int rank=0;
	        // 7. ループで要素順に値を取得する
	        for(Entry<String, Integer> entry : list_entries) {
	           count++;

	           if(Integer.parseInt(entry.getKey()) == Integer.parseInt(player[playerNo].getID())) {
	        	   rank=count;
	           }
	           sendMessage(String.format("%-10s",String.valueOf ( Playerslist.get( entry.getKey() ).getRecord()[5] )) + Playerslist.get(entry.getKey()).getName(),player[playerNo].getMyPlayerNo());
	           //レートと名前送信
	        }
	        sendMessage("rate",player[playerNo].getMyPlayerNo());
	        sendMessage(String.valueOf(rank),player[playerNo].getMyPlayerNo());
	        sendMessage("end",player[playerNo].getMyPlayerNo());
	    }

	    public void setOpponentNo(int n) {
	    	opponentplayerNo = n;
	    }

	    public void removeMatchingList() {
	    	MatchingList[0] = -1;
				matchingFlag=false;
	    }

	    public void removeSpecialList() {
	    	SpecialList[0] = -1;
				matchingFlag=false;
	    }

	    public void removeReMatchingList() {
	    	ReMatchingList[0] = -1;
				matchingFlag=false;
	    }

			public void addMatchingList(int playerNo,String str) {//対戦リスト
		    	if(str.equals ("rank")) {

		    	    if(MatchingList[0] == -1) {//playerがいないなら
		    		MatchingList[0] = playerNo;
		    	    }
		    	    else {//playerがいるなら
		    		   MatchingList[1] = playerNo;

		    		   player[MatchingList[0]].setOpponentPlayerNo(MatchingList[1]);//相手のplayerNo格納
		    		   player[MatchingList[1]].setOpponentPlayerNo(MatchingList[0]);


		    		   flagMatched = true;//ルームが埋まっている
							 matchingFlag=true;
		    		   sendMessage("succeeded",MatchingList[0]);
		    		   sendMessage("succeeded",MatchingList[1]);
		    		   sendColor(MatchingList[0],MatchingList[1]);
		    		   sendMessage(player[MatchingList[1]].getName(),MatchingList[0]);//name
		    		   sendMessage(Integer.toString(player[MatchingList[1]].getRecord()[5]),MatchingList[0]);//rate
		    		   sendMessage(player[MatchingList[0]].getName(),MatchingList[1]);//name
		    		   sendMessage(Integer.toString(player[MatchingList[0]].getRecord()[5]),MatchingList[1]);//rate


		    		   MatchingList[0] = -1;//新しいルームのため,いない表示にする
		    		   MatchingList[1] = -1;
		    	    }
		    	}else if(str .equals ("special")){

		    	    if(SpecialList[0] == -1) {
		    	    	SpecialList[0] = playerNo;
		    	    }
		    	    else {
		    	       SpecialList[1] = playerNo;

		    		   player[SpecialList[0]].setOpponentPlayerNo(SpecialList[1]);//相手のplayerNo
		    		   player[SpecialList[1]].setOpponentPlayerNo(SpecialList[0]);

		    		   flagSpecialMatched = true;
		    		   matchingFlag=true;
		    		   sendMessage("succeeded",SpecialList[0]);
		    		   sendMessage("succeeded",SpecialList[1]);
		    		   sendColor(SpecialList[0],SpecialList[1]);
		    		   sendMessage(player[SpecialList[1]].getName(),SpecialList[0]);//1も同じように送る？
		    		   sendMessage(Integer.toString(player[SpecialList[1]].getRecord()[5]),SpecialList[0]);
		    		   sendMessage(player[SpecialList[0]].getName(),SpecialList[1]);//1も同じように送る？
		    		   sendMessage(Integer.toString(player[SpecialList[0]].getRecord()[5]),SpecialList[1]);

		    		   SpecialList[0] = -1;
		    		   SpecialList[1] = -1;
		    	    }
		    	}
		    }

			public void addReMatchingList(int playerNo) {//再戦リスト

			    if(ReMatchingList[0] == -1) {//playerがいないなら
				    ReMatchingList[0] = playerNo;
			    }
			    else {
			    	ReMatchingList[1] = playerNo;

				   player[ReMatchingList[0]].setOpponentPlayerNo(ReMatchingList[1]);//相手のplayerNo
				   player[ReMatchingList[1]].setOpponentPlayerNo(ReMatchingList[0]);


				   flagReMatched = true;
				   matchingFlag=true;
				   sendMessage("succeeded",ReMatchingList[0]);
				   sendMessage("succeeded",ReMatchingList[1]);
				   sendColor(ReMatchingList[0],ReMatchingList[1]);
				   sendMessage(player[ReMatchingList[1]].getName(),ReMatchingList[0]);
				   sendMessage(Integer.toString(player[ReMatchingList[1]].getRecord()[5]),ReMatchingList[0]);
				   sendMessage(player[ReMatchingList[0]].getName(),ReMatchingList[1]);
				   sendMessage(Integer.toString(player[ReMatchingList[0]].getRecord()[5]),ReMatchingList[1]);

				   ReMatchingList[0] = -1;
				   ReMatchingList[1] = -1;
			    }

			}

	}


	public void acceptClient(){ //クライアントの接続(サーバの起動)
		try {
			System.out.println("サーバが起動しました．");
			ServerSocket ss = new ServerSocket(port); //ポートにバインドされたサーバソケットを用意ServerSocketはクラス
			int i=0;

			while (true) {
				Socket socket = ss.accept();//新規接続を受け付ける
				int playerNo =i;//プレイヤ識別番号,ログインで毎回違う
//				online[id]=true;
				System.out.println("プレイヤ" + playerNo + "と接続しました。");
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




    public void sendColor(int p1,int p2){ //先手後手情報(白黒)の送信
        sendMessage("black",p1);
        sendMessage("white",p2);
    }

    public void sendMessage(String msg,int playerNo){
		out[playerNo].println(msg);//送信データをバッファに書き出す
		out[playerNo].flush();//送信データを送る
		System.out.println(playerNo + "にメッセージ " + msg + " を送信しました");
	}

	public static void main(String[] args){
		Server server = new Server(11169); //待ち受けポート1112番でサーバオブジェクトを準備
		server.acceptClient(); //クライアント受け入れを開始


	}
}