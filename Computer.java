import java.util.ArrayList;
import java.util.Random;

public class Computer {
	//メンバ変数
	int level;	//level:CPUの難易度 探索の深さの値？
	int color;	//color:-1なら先手(黒)，1なら後手(白)
	//int childValue;	//childValue:子ノードの評価値
	/*int evaluationMap[][] = {{30, -12, 0, -1, -1, 0, -12, 30},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {30, -12, 0, -1, -1, 0, -12, 30}
							 };	//evaluationMap:石を置いた時の評価値表*/
	int evaluationMap[][] = {{68, -12, 53, -8, -8, 53, -12, 68},
			  {-12, -62, -33, -7, -7, -33, -62, -12},
			  {53, -33, 26, 8, 8, 26, -33, 53},
			  {-8, -7, 8, -18, -18, 8, -7, -8},
			  {-8, -7, 8, -18, -18, 8, -7, -8},
			  {53, -33, 26, 8, 8, 26, -33, 53},
			  {-12, -62, -33, -7, -7, -33, -62, -12},
			  {68, -12, 53, -8, -8, 53, -12, 68},
			 };
	//int board[][]; //board:盤面情報を格納する変数→othello.grids[][]に変更
	ArrayList<Integer> canputlist_x = new ArrayList<Integer>();
	ArrayList<Integer> canputlist_y = new ArrayList<Integer>();	//置ける場所の一覧
	//int phase = 0;	//phase:序盤〜終盤に分割して思考を変える．使わないかも
	Random random = new Random();	//random:乱数生成器
	Othello othello = new Othello(0);	//othello:Othelloクラスのメソッドを利用

	//コンストラクタ
	public Computer(int level, int color){
		//コンストラクタの中身
		this.level = level;
		this.color = color;
	}

	//以下メソッド
	//ランダムに打つ
	public int random() {
		//リスト初期化
		canputlist_x.clear();
		canputlist_y.clear();
		//盤面取得
		//board = .getGrids();
		//置ける場所(2)を探してcanputlistにチェックし
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == 2) {
					canputlist_x.add(x);
					canputlist_y.add(y);
				}
			}
		}
		//ランダムに一つ選び
		int i = random.nextInt(canputlist_x.size());
		//置く場所を返す
		return 10*canputlist_y.get(i) + canputlist_x.get(i);

	}

	//α-β法による探索
		/*turn:自分の番か相手の番かを表現
		 *depth:探索の深さ
		 *alpha:αカットする値
		 *beta:βカットする値*/
	//あまりに時間がかかるなら打ち切り？
	public int alphabeta(int turn, int depth, int alpha, int beta) {
		int value ;	//(多分)ノードの評価値
		int childValue;
		int X = 100, Y = 100;	//評価の高い場所
		//末端ノードなら盤面評価値を返す
		if(depth == 0) {
			return evaluateBoard(turn);
		}
		// 自分のターンなら最小値，相手のターンなら最大値をとりあえず設定
		if(turn == color) {
			value = Integer.MIN_VALUE;
		}else{
			value = Integer.MAX_VALUE;
		}
		//置ける場所(2)を探す.パスのときが考慮されているか検討
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == 2) {
					//試しに打つ
					othello.setStone(x, y);
					othello.checkPlaceable();
					//手番を変える(現在はsetStoneにchangeTurnが内包)

					//次の評価値を求める
					childValue = alphabeta(-turn, depth-1, alpha, beta);
					//末端ノードまで来るか，子ノードの評価値が計算されているなら以下の処理へ
					//自分のターンなら子ノードから最大値を選択
						//バグってたらこの辺怪しい
					if(turn == color) {
						if(childValue >= value) {
							//評価値がvalueと等しければ，1/2の確率で更新
							//if(childValue == value) {
								//if(random.nextInt(2) == 0) {
									//value = childValue;
									//alpha = value;
									//X = x;
									//Y = y;
								//}
							//評価値がvalueより大きければ更新
							//}else {
							value = childValue;
							alpha = value;
							X = x;
							Y = y;
							}
						//}
						if(value > beta) {	//βカット
							othello.undo();
							return value;
						}
					//相手のターンなら子ノードから最小値を選択(自分にとって不都合な手を指す)
					}else{
						if(childValue <= value) {
							//評価値がvalueと等しければ，1/2の確率で更新
							//if(childValue == value) {
								//if(random.nextInt(2) == 0) {
									//value = childValue;
									//beta = value;
									//X = x;
									//Y = y;
								//}
								//評価値がvalueより小さければ更新
							//}else {
								value = childValue;
								beta = value;
								X = x;
								Y = y;
							}
						//}
						if(value < alpha) {	//αカット
							othello.undo();
							return value;
						}
					}
					othello.undo();
				}
			}
		}
		//先頭ノードだったら打つ場所を返す
		if(depth == level) {
			return 10*Y + X; //1つの値で返す必要があるのでこの形に．Xは(戻り値)%10,Yは((戻り値)-(戻り値)%10)/10で復元できる．
		//それ以外はノードの評価値を返す
		}else {
			return value;
		}
	}


	//全探索
		//とりあえず単純に実装するけど高速化の必要があるかも
		private int exhaustiveSearch(int turn, int depth, int alpha, int beta) {
			othello.checkPlaceable();
			//System.out.println("T:"+othello.getTurn());
			//System.out.println("Pass:"+othello.pass_count);
			int value;
			int childValue;
			int pass= 0;
			int X = 100, Y = 100;
			//System.out.println(depth);
			//ゲーム終了まで進んだら，黒石と白石の数の差を返す
			//if(othello.getBlackstone() + othello.getWhitestone() == 64 || othello.pass_count == 2) {
			if(othello.isGameover() == true) {
				//System.out.println("owari");
				if(color == -1) {
					return othello.getBlackstone() - othello.getWhitestone();
				}else {
					return othello.getWhitestone() - othello.getBlackstone();
				}
			}
			if(turn == color) {
				value = Integer.MIN_VALUE;
			}else{
				value = Integer.MAX_VALUE;
			}
			for(int y = 0; y < 8; y++) {
				for(int x = 0; x < 8; x++) {
					if(othello.grids[y][x] == 2) {
						pass++;
						//System.out.println("put " + othello.getTurn());
						othello.setStone(x, y);
						othello.checkPlaceable();
						childValue =  exhaustiveSearch(-turn, depth-1, alpha, beta);
						if(turn == color) {
							if(childValue >= value) {
								value = childValue;
								alpha = value;
								X = x;
								Y = y;
								}
							if(value > beta) {	//βカット
								othello.undo();
								return value;
							}
						}else {
							if(childValue <= value) {
									value = childValue;
									beta = value;
									X = x;
									Y = y;
								}
							if(value < alpha) {	//αカット
								othello.undo();
								return value;
							}
						}
						othello.undo();
					}
				}
			}
			//System.out.println(depth + " " + value);
			if(pass == 0) {
				othello.changeTurn();
				othello.checkPlaceable();
				//othello.draw();
				//System.out.println(othello.pass_count);
				value =  exhaustiveSearch(-turn, depth, alpha, beta);
			}
			//先頭ノードだったら打つ場所を返す
			if(depth == level) {
					System.out.println("zentansaku " + value);
					return 10*Y + X; //1つの値で返す必要があるのでこの形に．Xは(戻り値)%10,Yは((戻り値)-(戻り値)%10)/10で復元できる．
			}else {
				return value;
			}
		}


	//盤面評価
		//turn:自分の番か相手の番かを表現
	private int evaluateBoard(int turn) {	//元々doubleだったけど現状intになりました
		int evaluation = 0;	//evaluation:盤面に置かれている石とevaluation_mapから算出された盤面評価値
		//盤面取得
		//board[][] = .getGrids();
		//turn=-1なら黒，=1なら白が置かれているすべての場所に対し，evaluattionMapの値で評価
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == turn){
					evaluation += evaluationMap[y][x];	//開放度を計算して，その値をかけてみる？
				}else if(othello.grids[y][x] == -turn) {
					evaluation -= evaluationMap[y][x];
				}
			}
		}
		return evaluation;
	}

	//Clientに呼び出されるメソッド．戻り値は座標
			//grids[][]:思考する盤面
		public int think(int grids[][]) {
			//現在の盤面をコピー
			for(int y = 0; y < 8; y++) {
				for(int x = 0; x < 8; x++) {
					othello.grids[y][x] = grids[y][x];

				}
			}

			if(level == 1) {
				return random();
			}else{
				//残り14手から全探索開始
				//!このif文は怪しい...
				if(othello.getTurn() != color) {
					System.out.println("CHANGE TURN!");
					othello.changeTurn();
				}
				if(othello.getBlackstone() + othello.getWhitestone() >= 50) {
					return exhaustiveSearch(color, level, Integer.MIN_VALUE, Integer.MAX_VALUE);
				}
				else{
					return alphabeta(color, level, Integer.MIN_VALUE, Integer.MAX_VALUE);
				}
			}
		}



}