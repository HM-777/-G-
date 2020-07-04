import java.util.Scanner;

public class Othello {
	private  int row = 8;	//オセロ盤の縦横マス数(2の倍数のみ)
	// String [] grids = new String [row * row]; //局面情報
	public  int [][] grids = new int [row][row];
    public int [][][] save_grids=new int [60][row][row];//保存用盤面
	private  int turn; //手番の色
	private int moves_count=0;//手数


    private  int empty=0;//盤面が空である
    private  int placeable=2;//置くことが出来る
    private  int black=-1;
    private  int white=1;

    private boolean placeableflag=false;//おける場所のフラグ

    //パス用変数
    private boolean pass_flag=false;

    private int pass_count=0;

    //黒い駒の数集計用
    private  int cnt_black = 0;
    //白い駒の数集計用
    private  int cnt_white = 0;



    // コンストラクタ
	public Othello(int i){//i=1 special i=0 nomal

		turn = black; //黒が先手
		for(int x=0;x<row;x++) {
		for(int y = 0 ; y < row ; y++){
			grids[y][x] = 0; //初めは石が置かれていない


		}}
		grids[3][3] = white;
		grids[4][4] = white;
		grids[3][4] = black;
		grids[4][3] = black;
	}




	// メソッド

	public void savegrids() {
		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				save_grids[moves_count][i][j]=grids[i][j];
			}
		}
	}
	  public void undo() {
			for(int i=0;i<row;i++) {
				for(int j=0;j<row;j++) {
					grids[i][j]=save_grids[moves_count-1][i][j];
				}
			}
			moves_count--;


	    }
	public int getRow(){ //縦横のマス数を取得
		return row;
	}
	public int getBlackstone() {//黒石の数を取得
		cnt_black=0;
		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				if(grids[i][j]==-1) {
					cnt_black++;
				}
			}

		}


		return cnt_black;
	}
	public int getWhitestone() {//白石の数を取得
cnt_white=0;

		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				if(grids[i][j]==white) {
					cnt_white++;
				}
			}

		}
		return cnt_white;
	}
	public int getTurn(){ // 手番情報を取得
		return turn;
	}
	public int[] [] getGrids(){ // 局面情報を取得
		return grids;
	}
	public void changeTurn(){ //　手番を変更
	turn*=-1;
	}
	public boolean isGameover(){	// 対局終了を判断

		//まだ空いている座標があるかのフラグ
	    boolean existempty = false;
       cnt_black=0;
       cnt_white=0;

	    for(int i=0;i<8;i++) {
	    	for(int j=0;j<8;j++) {
	    		if(grids[i][j]==empty||grids[i][j]==placeable) {//
	    			existempty=true;//空か置くこと可能ならばゲーム継続可能
	    		}else if(grids[i][j]==black) {
	    			cnt_black++;//黒石のカウント
	    		}else if(grids[i][j]==white) {
	    			cnt_white++;//白いしのカウント
	    		}
	    	}
	    }

  if(cnt_black==0||cnt_white==0) {//一色で染まった場合終了
	  existempty=false;
  }

  if(pass_count>1) {//すでにパスが二回連続で行われているならば終了
	  existempty=false;
  }



		return existempty;
	}


	public int checkWinner(){	// 勝敗を判断
		int winner=10;//初期値　この値が返った場合試合が終わっていない
		if(isGameover()==false) {
			if(cnt_black==cnt_white) {
				winner=0;// draw

			}else if(cnt_black>cnt_white) {
				winner=black; //黒勝ち
			}else if(cnt_black<cnt_white) {
				winner=white;//白勝ち
			}
		}
		return winner;
	}



	public void checkPlaceable() {//おける場所探索
pass_flag=true;

		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				//空の盤面のみチェックする
				if(grids[i][j]==empty||grids[i][j]==placeable) {
			    turnLeftUp(j, i,false);
			    turnUp(j, i,false);
			    turnRightUp(j, i,false);
			    turnLeft(j, i,false);
			    turnRight(j, i,false);
			    turnLeftDown(j, i,false);
			    turnDown(j, i,false);
			    turnRightDown(j, i,false);

			    if(placeableflag==true) {//もしおける場所が見つかったならおける場所変数を入れてパスフラグを折りパスカウントをリセット
			    	pass_flag=false;
			    	grids[i][j]=placeable;
			    	pass_count=0;
			    	placeableflag=false;
			    }else {
			    	grids[i][j]=empty;
			    }
				}
			}
		}
		if(pass_flag==true) {
			pass_count++;
			changeTurn();//パスフラグがtrueのままならパスカウントを増加させて手番変更
		}
	}
 public void setStone(int x, int y) {



    // 駒を配置できる場合
    if (grids[y][x]==placeable) {

        //盤面を保存する
        savegrids();


    	grids[y][x] = turn;


      // ひっくり返す処理
      turnStone(x, y);
    //手数を増やす
      moves_count++;

      changeTurn();//ターン変更
    }

  }

   public void turnStone(int x, int y) {

    // 8方向の駒の配置を確認し、ひっくり返す

    turnLeftUp(x, y,true);
    turnUp(x, y,true);
    turnRightUp(x, y,true);
    turnLeft(x, y,true);
    turnRight(x, y,true);
    turnLeftDown(x, y,true);
    turnDown(x, y,true);
    turnRightDown(x, y,true);

  }


   //flagは盤面に操作を反映する場合にはtrueしない場合はfalse
  public void turnLeftUp(int x, int y,boolean flag) {
    if (y > 1 && x > 1) {

      // となりの駒
      int next = grids[y - 1][x - 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x - i < 0 || y - i < 0 || grids[y - i][x - i]==empty||grids[y-i][x-i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x - i]==turn) {
            // 自駒の場合
if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
 if(flag==true) {           for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x - t] = turn;

            }
 }

            break;
          }
        }
      }

    }
  }

   public void turnUp(int x, int y,boolean flag) {
    if (y > 1) {

      // となりの駒
      int next = grids[y - 1][x];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (y - i < 0 || grids[y - i][x]==empty||grids[y-i][x]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true) {       for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

   public void turnRightUp(int x, int y,boolean flag) {
    if (y > 1 && x < 6) {

      // となりの駒
      int next = grids[y - 1][x + 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x + i > 7 || y - i < 0 || grids[y - i][x + i]==empty||grids[y-i][x+i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x + i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {   for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x + t] =turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

   public void turnDown(int x, int y,boolean flag) {
    if (y < 6) {

      // となりの駒
      int next = grids[y + 1][x];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (y + i > 7 ||grids[y + i][x]==empty||grids[y+i][x]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {       for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

  public void turnRight(int x, int y,boolean flag) {
    if (x < 6) {

      // となりの駒
      int next = grids[y][x + 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x + i > 7 || grids[y][x + i]==empty||grids[y][x+i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y][x + i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {      for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y][x + t] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

   public void turnLeftDown(int x, int y,boolean flag) {
    if (y < 6 && x > 1) {

      // となりの駒
      int next = grids[y + 1][x - 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x - i < 0 || y + i > 7 || grids[y + i][x - i]==empty||grids[y+i][x-i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x - i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x - t] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

   public void turnLeft(int x, int y,boolean flag) {
    if (x > 1) {

      // となりの駒
      int next =grids[y][x - 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x - i < 0 || grids[y][x - i]==empty||grids[y][x-i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y][x - i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {        for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y][x - t] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

   public void turnRightDown(int x, int y,boolean flag) {
    if (y < 6 && x < 6) {

      // となりの駒
     int  next = grids[y + 1][x + 1];

      // となりの駒が裏駒の場合
      if (next==turn*-1) {

        // さらにその一つとなりから順に確認
        for (int i = 2; true; i++) {

          if (x + i > 7 || y + i > 7 || grids[y + i][x + i]==empty||grids[y+i][x+i]==placeable) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x + i]==turn) {
            // 自駒の場合
          	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x + t] = turn;

            }
        	  }
            break;
          }
        }
      }

    }
  }

public void draw() {
	System.out.println("　０１２３４５６７");
	for(int j=0;j<8;j++) {
		System.out.print(" "+j);
	for(int i=0;i<8;i++) {
		if(grids[j][i]==white)
		System.out.print("○");
		if(grids[j][i]==black)
			System.out.print("●");
		if(grids[j][i]==placeable)
			System.out.print("△");
		if(grids[j][i]==empty)
			System.out.print("  ");
	}
System.out.println("");
	}
}

public static void main(String args[]) {
	Othello a=new Othello(0);
	Scanner scan=new Scanner(System.in);
	a.checkPlaceable();
	a.draw();


	while(a.isGameover()==true) {
		a.checkPlaceable();
		a.draw();
		if(a.pass_flag==false) {
System.out.println("x=");
int x=scan.nextInt();
System.out.println("y=");
int y=scan.nextInt();



	a.setStone(x,y);
	}

	System.out.println(a.moves_count);
	}


if(a.checkWinner()==a.white) {
	System.out.println("White win");
	System.out.println("white:"+a.getWhitestone()+"black:"+a.getBlackstone());
}else if(a.checkWinner()==a.black) {
	System.out.println("black win");
}else if(a.checkWinner()==0) {
	System.out.println("draw");

}


}

}