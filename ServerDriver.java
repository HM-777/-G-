import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

 public class ServerDriver{
	 static int port = 12116;
     static Othello othello;
     static Computer cpu;
     static int cpuOperation;
     static HashMap <String,Player> Playerslist = new HashMap <String,Player>();
     static int id;


     public static void main(String[] args) {
 	try {
 	    ServerSocket server = new ServerSocket(port);
 	    Socket sock = null;
 	    System.out.println("接続待ち");
 	    while(true) {
 		try {
 		    sock = server.accept();

 		    System.out.println("接続完了");
 		    Random rand = new Random();
	        int num = rand.nextInt(90000000) + 10000000;
	        id = num;//ランダム文字列でIDを与える
	        Player player= new Player("yokohama", Integer.toString(id), "taro");
	        Playerslist.put( Integer.toString(id), player);
	        System.out.println("名前は" + Playerslist.get(id).getName() + "です。");
	        System.out.println("IDは" + Playerslist.get(id).getID() + "です。");
	        System.out.println("パスワードは" + Playerslist.get(id).getPass() + "です。");

 		    BufferedReader in = new BufferedReader(
                         new InputStreamReader(sock.getInputStream()));
 		    PrintWriter out = new PrintWriter(sock.getOutputStream());
 		    othello = new Othello(0);
 		    cpu = new Computer(7,1);
 		    String s;
 		    int x, y;
 		    while((s = in.readLine()) != null) {
 		    	if(s.equals("sendOperation")) {
 		    		s = in.readLine();
 		    		x = Integer.parseInt(s);
 		    		s = in.readLine();
 		    		y = Integer.parseInt(s);
 		    		othello.checkPlaceable();
 		    		othello.setStone(x,y);
 						if(othello.checkPlaceable()){
 							cpuOperation = cpu.seek(othello.getGrids());
 							othello.setStone(cpuOperation%10, (cpuOperation-cpuOperation%10)/10);//ターンが変わる
 				    	out.println("sendOperation");
 				    	out.flush();
 				    	out.println(cpuOperation%10);
 				    	out.flush();
 				    	out.println((cpuOperation-cpuOperation%10)/10);
 				    	out.flush();
 						System.out.println("(" + (cpuOperation%10+1) + "," + (8-(cpuOperation-cpuOperation%10)/10) + ")に置いた結果を送信しました。");
 				    	if(!othello.checkPlaceable()) {//相手に打つ手がない場合
 								do {
 									othello.changeTurn();
 									othello.checkPlaceable();
 									cpuOperation = cpu.seek(othello.getGrids());
 									othello.setStone(cpuOperation%10, (cpuOperation-cpuOperation%10)/10);//ターンが変わる
 							    	out.println("sendOperation");
 							    	out.flush();
 							    	out.println(cpuOperation%10);
 							    	out.flush();
 							    	out.println((cpuOperation-cpuOperation%10)/10);
 							    	out.flush();
 										System.out.println("(" + (cpuOperation%10+1) + "," + (8-(cpuOperation-cpuOperation%10)/10) + ")に置いた結果を送信しました。");
 									if(othello.checkPlaceable()) break;
 									else othello.changeTurn();
 								} while(othello.checkPlaceable());
 							}/*
 							else{//コンピュータに打つ手がない場合
 								othello.changeTurn();
 							}*/
 						}
 		    		else {
 							othello.changeTurn();
 						}
 		    	}
 		    }
 		    sock.close();
 		    System.out.println("接続が切断されました");
 		} catch (IOException e) {
 		    System.err.println(e);
 		}
 	    }
 	} catch (IOException e) {
 	    System.err.println(e);
 	}
     }
 }
