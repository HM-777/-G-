import java.util.ArrayList;
import java.util.Random;

public class Computer {
	//メンバ変数
	int level;	//level:CPUの難易度 探索の深さの値？
	int color;	//color:-1なら先手(黒)，1なら後手(白)
	int X = 0, Y = 0;	//評価の高い場所
	int childValue;	//childValue:子ノードの評価値
	int evaluationMap[][] = {{30, -12, 0, -1, -1, 0, -12, 30},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {30, -12, 0, -1, -1, 0, -12, 30}
							 };	//evaluationMap:石を置いた時の評価値表
	int board[][]; //board:盤面情報を格納する変数
	ArrayList<Integer> canputlist_x = new ArrayList<Integer>();
	ArrayList<Integer> canputlist_y = new ArrayList<Integer>();	//置ける場所の一覧
	int phase = 0;	//phase:序盤〜終盤に分割して思考を変える．使わないかも
	Random random = new Random();	//random:乱数生成器

	//コンストラクタ
	public Computer(int level, int color){
		//コンストラクタの中身
		this.level = level;
		this.color = color;
	}

	//以下メソッド
	//ランダムに打つ
	public void random() {
		//盤面取得
		//board = .getGrid();
		//置ける場所(2)を探してcanputlistにチェックし
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(board[y][x] == 2) {
					canputlist_x.add(x);
					canputlist_y.add(y);
				}
			}
		}
		//ランダムに一つ選び
		int i = random.nextInt(canputlist_x.size());
		//置く
		//.setStone(canputlist_x.get(i), canputlist_y.get(i));

	}

	//α-β法による探索
		/*turn:自分の番か相手の番かを表現
		 *depth:探索の深さ
		 *alpha:αカットする値
		 *beta:βカットする値*/
	//あまりに時間がかかるなら打ち切り？
	public int alphabeta(int turn, int depth, int alpha, int beta) {
		int value ;	//(多分)ノードの評価値
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
		//置ける場所(2)を探す
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(board[y][x] == 2) {
					//試しに打つ
					//.setStone(x, y);
					//手番を変える
					//.changeTurn()
					//次の評価値を求める
					childValue = alphabeta(turn*(-1), depth-1, alpha, beta);
					//末端ノードまで来るか，子ノードの評価値が計算されているなら以下の処理へ
					//自分のターンなら子ノードから最大値を選択
						//バグってたらこの辺怪しい
					if(turn == color) {
						if(childValue >= value) {
							//評価値がvalueと等しければ，1/2の確率で更新
							if(childValue == value) {
								if(random.nextInt(2) == 0) {
									value = childValue;
									alpha = value;
									X = x;
									Y = y;
								}
							//評価値がvalueより大きければ更新
							}else {
							value = childValue;
							alpha = value;
							X = x;
							Y = y;
							}
						}
						if(value > beta) {	//βカット
							// undo
							return value;
						}
					//相手のターンなら子ノードから最小値を選択
					}else{
						if(childValue <= value) {
							//評価値がvalueと等しければ，1/2の確率で更新
							if(random.nextInt(2) == 0) {
								value = childValue;
								beta = value;
								X = x;
								Y = y;
								//評価値がvalueより小さければ更新
							}else {
								value = childValue;
								beta = value;
								X = x;
								Y = y;
							}
						}
						if(value < alpha) {	//αカット
							// undo
							return value;
						}
					}
					// undo
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

	//盤面評価
		//turn:自分の番か相手の番かを表現
	private int evaluateBoard(int turn) {	//元々doubleだったけど現状intになりました
		int evaluation = 0;	//evaluation:盤面に置かれている石とevaluation_mapから算出された盤面評価値
		//盤面取得
		//board[][] = .getGrids();
		//turn=-1なら黒，=1なら白の場所をチェックし，evaluattionMapの値で評価
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				/*if(board[y][x] == turn){
				 *evaluation += evaluationMap[y][x];	//開放度を計算して，その値をかけてみる？
				 */
			}
		}
		return evaluation;
	}

}
