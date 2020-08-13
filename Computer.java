import java.util.ArrayList;
import java.util.Random;

public class Computer {
	//定義
	int w_kaihoudo = 2;
	int w_kakuteiseki = 8;
	//メンバ変数
	int level;	//level:CPUの難易度 探索の深さの値
	int color;	//color:-1なら先手(黒)，1なら後手(白)
	int kaihoudo[] = new int[7];	//kaihoudo:開放度
	int evaluationMap[][] = {{30, -12, 0, -1, -1, 0, -12, 30},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {30, -12, 0, -1, -1, 0, -12, 30}
							 };	//evaluationMap:石を置いた時の評価値表
	ArrayList<Integer> canputlist_x = new ArrayList<Integer>();
	ArrayList<Integer> canputlist_y = new ArrayList<Integer>();	//置ける場所の一覧
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
	private int random() {
		//リスト初期化
		canputlist_x.clear();
		canputlist_y.clear();
		//盤面取得
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
	private int alphabeta(int turn, int depth, int alpha, int beta) {
		int value ;	//ノードの評価値
		int childValue;
		int X = 100, Y = 100;	//評価の高い場所
		//末端ノードなら盤面評価値を返す
		if(depth == 0) {
			//int k = evaluateBoard();
			//System.out.println("E:" + k);
			return  evaluateBoard() - w_kaihoudo*kaihoudo[0]+ w_kakuteiseki*checkKakuteiseki();
		}
		// 自分のターンなら最小値，相手のターンなら最大値をとりあえず設定
		if(turn == color) {
			value = Integer.MIN_VALUE;
		}else{
			value = Integer.MAX_VALUE;
		}
		//置ける場所(2)を探す
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == 2) {
					//試しに打つ
					othello.setStone(x, y);
					kaihoudo[depth-1] = evaluateKaihoudo(turn);
					othello.checkPlaceable();
					//手番を変えて次の評価値を求める
					childValue = alphabeta(-turn, depth-1, alpha, beta);
					//末端ノードまで来るか，子ノードの評価値が計算されているなら以下の処理へ
					//自分のターンなら子ノードから最大値を選択
					if(turn == color) {
						if(childValue >= value) {
							//評価値がvalueより大きければ更新
							value = childValue;
							alpha = value;
							X = x;
							Y = y;
							}
						if(value > beta) {	//βカット
							othello.undo();
							return value;
						}
					//相手のターンなら子ノードから最小値を選択(自分にとって不都合な手を指す)
					}else{
						if(childValue <= value) {
								//評価値がvalueより小さければ更新
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
		//先頭ノードだったら打つ場所を返す
		if(depth == level) {
			return 10*Y + X; //1つの値で返す必要があるのでこの形に．Xは(戻り値)%10,Yは((戻り値)-(戻り値)%10)/10で復元できる．
		//それ以外はノードの評価値を返す
		}else {
			//開放度を各ノードで加味
			return value - w_kaihoudo*kaihoudo[depth-1];
		}
	}


	//全探索
	private int exhaustiveSearch(int turn, int depth, int alpha, int beta) {
	othello.checkPlaceable();
		int value;
		int childValue;
		int pass= 0;
		int X = 100, Y = 100;
		//ゲーム終了まで進んだら，黒石と白石の数の差を返す
		if(othello.isGameover() == true) {
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
		//パス時の処理
		if(pass == 0) {
			othello.changeTurn();
			othello.checkPlaceable();
			value =  exhaustiveSearch(-turn, depth, alpha, beta);
		}
		//先頭ノードだったら打つ場所を返す
		if(depth == level) {
				//System.out.println("zentansaku " + value);
				return 10*Y + X; //1つの値で返す必要があるのでこの形に．Xは(戻り値)%10,Yは((戻り値)-(戻り値)%10)/10で復元できる．
				}else {
			return value;
		}
	}


	//盤面評価
	private int evaluateBoard() {
		int evaluation = 0;	//evaluation:盤面に置かれている石とevaluation_mapから算出された盤面評価値
		//石が置かれているすべての場所に対し，evaluattionMapの値で評価
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == 1){
					evaluation += evaluationMap[y][x];
				}else if(othello.grids[y][x] == -1) {
					evaluation -= evaluationMap[y][x];
				}
			}
		}
		return evaluation;
	}
	//開放度計算
	private int evaluateKaihoudo(int turn) {
		int kaihoudo = 0;
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(othello.grids[y][x] == 5) {
					kaihoudo++;
				}
			}
		}
		if(turn == color) {
			return kaihoudo;
		}else {
			return -kaihoudo;
		}
	}
	//確定石をカウント
	private int checkKakuteiseki() {
		int column,row,count=0;
		/*
		 * flag:辺が染まっているかをチェック
		 * flag[0]:左上→右上
		 * flag[1]:左上→左下
		 * flag[2]:右上→右下
		 * flag[3]:左下→右下
		 */
		int flag[] = {0, 0, 0, 0};
		//左上
		if(othello.grids[0][0] == -1 || othello.grids[0][0] == 1) {
			column = 1;
			row = 1;
			if(othello.grids[0][0] == color) {
				count++;
			}else if(othello.grids[0][0] == -color) {
				count--;
			}
			//→右上
			while(othello.grids[0][column] == othello.grids[0][0] && column <= 6) {
				if(othello.grids[0][column] == color) {
					count++;
				}else if(othello.grids[0][column] == -color) {
					count--;
				}
				column++;
			}
			if(othello.grids[0][7] == othello.grids[0][0]) {
				flag[0] = 1;
			}
			//→左下
			while(othello.grids[row][0] == othello.grids[0][0] && row <= 6) {
				if(othello.grids[row][0] == color) {
					count++;
				}else if(othello.grids[row][0] == -color) {
					count--;
				}
				row++;
			}
			if(othello.grids[7][0] == othello.grids[0][0]) {
				flag[1] = 1;
			}

		}
		//右上
		if(othello.grids[0][7] == -1 || othello.grids[0][7] == 1) {
			column = 6;
			row = 1;
			if(othello.grids[0][7] == color) {
				count++;
			}else if(othello.grids[0][7] == -color) {
				count--;
			}
			//→左上
			while(othello.grids[0][column] == othello.grids[0][7] && column >= 1 && flag[0] == 0) {
				if(othello.grids[0][column] == color) {
					count++;
				}else if(othello.grids[0][column] == -color) {
					count--;
				}
				column--;
			}
			//→右下
			while(othello.grids[row][7] == othello.grids[0][7] && row <= 6) {
				if(othello.grids[row][7] == color) {
						count++;
				}else if(othello.grids[row][7] == -color) {
						count--;
				}
					row++;
				}
			if(othello.grids[7][0] == othello.grids[7][7]) {
				flag[2] = 1;
			}

		}
		//左下
		if(othello.grids[7][0] == -1 || othello.grids[7][0] == 1) {
			column = 1;
			row = 6;
			if(othello.grids[7][0] == color) {
				count++;
			}else if(othello.grids[7][0] == -color) {
				count--;
			}
			//→右下
			while(othello.grids[7][column] == othello.grids[7][0] && column <= 6) {
				if(othello.grids[7][column] == color) {
					count++;
				}else if(othello.grids[7][column] == -color) {
					count--;
				}
				column++;
			}
			if(othello.grids[7][7] == othello.grids[7][0]) {
				flag[3] = 1;
			}
			//→左上
			while(othello.grids[row][0] == othello.grids[7][0] && row >= 1 && flag[1] == 0) {
				if(othello.grids[row][0] == color) {
					count++;
				}else if(othello.grids[row][0] == -color) {
					count--;
				}
				row--;
			}
		}
		//右下
		if(othello.grids[7][7] == -1 || othello.grids[7][7] == 1) {
			column = 6;
			row = 6;
			if(othello.grids[7][7] == color) {
				count++;
			}else if(othello.grids[7][7] == -color) {
				count--;
			}
			//→左下
			while(othello.grids[7][column] == othello.grids[7][7] && column >= 1 && flag[3] == 0) {
				if(othello.grids[7][column] == color) {
					count++;
				}else if(othello.grids[7][column] == -color) {
					count--;
				}
				column--;
			}
			//→左上
			while(othello.grids[row][7] == othello.grids[7][7] && row >= 1 && flag[2] == 0) {
				if(othello.grids[row][7] == color) {
					count++;
				}else if(othello.grids[row][7] == -color) {
					count--;
				}
				row--;
			}
		}
		//System.out.println(count);
		return count;
	}

	//Clientに呼び出されるメソッド．戻り値は座標
			//grids[][]:思考する盤面
	public int seek(int grids[][]) {
		//現在の盤面をコピー
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				othello.grids[y][x] = grids[y][x];
			}
		}
		//Easy
		if(level == 1) {
			return random();
		//Normal,Hard
		}else{
			//一番最初だけこの処理が必要
			if(othello.getTurn() != color) {
				//System.out.println("CHANGE TURN!");
				othello.changeTurn();
			}
			//levelがhardなら残り14手から全探索開始
			if(othello.getBlackstone() + othello.getWhitestone() >= 50 && level == 7) {
				return exhaustiveSearch(color, level, Integer.MIN_VALUE, Integer.MAX_VALUE);
			}else{
				return alphabeta(color, level, Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
		}
	}



}
