
public class Computer {
	//メンバ変数
	int level;	//level:CPUの難易度
	int evaluation_map[][] = {{30, -12, 0, -1, -1, 0, -12, 30},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {-1, -3, -1, -1, -1, -1, -3, -1},
							  {0, -3, 0, -1, -1, 0, -3, 0},
							  {-12, -15, -3, -3, -3, -3, -15, -12},
							  {30, -12, 0, -1, -1, 0, -12, 30}
							 };	//evaluation_map:石を置いた時の評価値表
	int phase;	//phase:序盤〜終盤に分割して思考を変える．使わないかも

	//コンストラクタ
	public Computer(int level){
		//コンストラクタの中身
		this.level = level;
	}

	//メソッド
	//ランダムに打つ
	public void random() {
		//盤面取得
		//置ける場所(2)を探して
		//ランダムに一つ選び
		//置く
	}

	//α-β法による探索
		/*turn:自分の番か相手の番かを表現
		 *depth:探索の深さ
		 *alpha:αカットする値
		 *beta:βカットする値*/
	public int alphabeta(int turn, int depth, int alpha, int beta) {
		int value = 0;	//(多分)ノードの評価値
		return value;
	}

	//盤面評価
		//turn:自分の番か相手の番かを表現
	public double evaluate_Board(int turn) {
		double evaluation = 0;	//evaluation:盤面に置かれている石とevaluation_mapから算出された盤面評価値
		//盤面取得
		//turn=-1なら黒，=1なら白の場所をチェックし，evaluattion_mapの値で評価
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				/*if(board[y][x] == turn){
				 *evaluation += evaluation_map[y][x];
				 */
			}
		}
		return evaluation;
	}

}

//test!