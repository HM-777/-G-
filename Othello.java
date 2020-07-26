import java.util.Random;
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
    private int garbage=3;
    private int release=5;

    private boolean placeableflag=false;//おける場所のフラグ

    //パス用変数
    private boolean pass_flag=false;

    private int pass_count=0;

    //黒い駒の数集計用
    private  int cnt_black = 0;
    //白い駒の数集計用
    private  int cnt_white = 0;

private boolean revolution_flag=false;
Random random=new Random();

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
		if (i==1) {


		}

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
			changeTurn();
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
	/*if(turn==white) {
		System.out.println("stone:white");
	}else {
		System.out.println("stone:black");
	}*/
	}
	public boolean isGameover(){	// 対局終了を判断

		//まだ空いている座標があるかのフラグ
	    boolean existempty = true;
       cnt_black=0;
       cnt_white=0;

	    for(int i=0;i<8;i++) {
	    	for(int j=0;j<8;j++) {
	    		if(grids[i][j]==empty||grids[i][j]==placeable) {//
	    			existempty=false;//空か置くこと可能ならばゲーム継続可能
	    		}else if(grids[i][j]==black) {
	    			cnt_black++;//黒石のカウント
	    		}else if(grids[i][j]==white) {
	    			cnt_white++;//白いしのカウント
	    		}
	    	}
	    }

  if(cnt_black==0||cnt_white==0) {//一色で染まった場合終了
	  existempty=true;
  }

  if(pass_count>1) {//すでにパスが二回連続で行われているならば終了
	  existempty=true;
  }



		return existempty;
	}


	public int checkWinner(){	// 勝敗を判断(特殊ルールでの革命時の処理も追加)
		int winner=10;//初期値　この値が返った場合試合が終わっていない
		if(isGameover()==true) {
			if(cnt_black==cnt_white) {
				winner=0;// draw

			}else if(cnt_black>cnt_white) {
				if(revolution_flag==false)
				winner=black; //黒勝ち
				else {
					winner=white;
				}
			}else if(cnt_black<cnt_white) {
				if(revolution_flag==false)
				winner=white;//白勝ち
				else {
					winner=black;
				}
			}
		}
		return winner;
	}



	public boolean checkPlaceable() {//おける場所探索
pass_flag=true;

		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				//空の盤面のみチェックする
				if(grids[i][j]!=white&&grids[i][j]!=black) {
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
			//パスフラグがtrueのままならパスカウントを増加
		}
		return !pass_flag;
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
      for(int i=0;i<row;i++) {
    	  for(int j=0;j<row;j++) {
    		  if(grids[j][i]==placeable)
    			  grids[j][i]=empty;
    	  }
      }

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

          if (x - i < 0 || y - i < 0 || grids[y - i][x - i]==empty||grids[y-i][x-i]==placeable||grids[y-i][x-i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x - i]==turn) {
            // 自駒の場合
if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
 if(flag==true) {           for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x - t] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y-t+a)>-1&&(y-t+a)<8&&(x-t+b)>-1&&(x-t+b)<8) {
            		  if(grids[y-t+a][x-t+b]!=black&&grids[y-t+a][x-t+b]!=white)
            			  grids[y-t+a][x-t+b]=release;
            		  }
            		  }
              }

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

          if (y - i < 0 || grids[y - i][x]==empty||grids[y-i][x]==placeable||grids[y-i][x]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true) {       for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y-t+a)>-1&&(y-t+a)<8&&(x+b)>-1&&(x+b)<8) {
            		  if(grids[y-t+a][x+b]!=black&&grids[y-t+a][x+b]!=white)
            			  grids[y-t+a][x+b]=release;
            		  }
            	  }
              }

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

          if (x + i > 7 || y - i < 0 || grids[y - i][x + i]==empty||grids[y-i][x+i]==placeable||grids[y-i][x+i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y - i][x + i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {   for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y - t][x + t] =turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y-t+a)>-1&&(y-t+a)<8&&(x+t+b)>-1&&(x+t+b)<8) {
            		  if(grids[y-t+a][x+t+b]!=black&&grids[y-t+a][x+t+b]!=white)
            			  grids[y-t+a][x+t+b]=release;
            		  }
            	  }
              }

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

          if (y + i > 7 ||grids[y + i][x]==empty||grids[y+i][x]==placeable||grids[y+i][x]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {       for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y+t+a)>-1&&(y+t+a)<8&&(x+b)>-1&&(x+b)<8) {
            		  if(grids[y+t+a][x+b]!=black&&grids[y+t+a][x+b]!=white)
            			  grids[y+t+a][x+b]=release;
            		  }
            	  }
              }

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

          if (x + i > 7 || grids[y][x + i]==empty||grids[y][x+i]==placeable||grids[y][x+i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y][x + i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {      for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y][x + t] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y+a)>-1&&(y+a)<8&&(x+t+b)>-1&&(x+t+b)<8) {
            		  if(grids[y+a][x+t+b]!=black&&grids[y+a][x+t+b]!=white)
            			  grids[y+a][x+t+b]=release;
            		  }
            	  }
              }

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

          if (x - i < 0 || y + i > 7 || grids[y + i][x - i]==empty||grids[y+i][x-i]==placeable||grids[y+i][x-i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x - i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x - t] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y+t+a)>-1&&(y+t+a)<8&&(x-t+b)>-1&&(x-t+b)<8) {
            		  if(grids[y+t+a][x-t+b]!=black&&grids[y+t+a][x-t+b]!=white)
            			  grids[y+t+a][x-t+b]=release;
            		  }
            	  }
              }

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

          if (x - i < 0 || grids[y][x - i]==empty||grids[y][x-i]==placeable||grids[y][x-i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y][x - i]==turn) {
            // 自駒の場合
        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {        for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y][x - t] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y+a)>-1&&(y+a)<8&&(x-t+b)>-1&&(x-t+b)<8) {
            		  if(grids[y+a][x-t+b]!=black&&grids[y+a][x-t+b]!=white)
            			  grids[y+a][x-t+b]=release;
            		  }
            	  }
              }

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

          if (x + i > 7 || y + i > 7 || grids[y + i][x + i]==empty||grids[y+i][x+i]==placeable||grids[y+i][x+i]==release) {
            // 駒がない場合終了
            break;
          } else if (grids[y + i][x + i]==turn) {
            // 自駒の場合
          	  if(flag==false&&placeableflag==false) {placeableflag=true;}
            // あいだの駒をすべて自駒にひっくりかえす
        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
              // 配列の要素を上書き
              grids[y + t][x + t] = turn;
              for(int a=-1;a<2;a++) {
            	  for(int b=-1;b<2;b++) {
            		  if((y+t+a)>-1&&(y+t+a)<8&&(x+t+b)>-1&&(x+t+b)<8) {
            		  if(grids[y+t+a][x+t+b]!=black&&grids[y+t+a][x+t+b]!=white)
            			  grids[y+t+a][x+t+b]=release;
            		  }
            	  }
              }

            }
        	  }
            break;
          }
        }
      }

    }
  }
   //------------------------------特殊ルール------------------------------
   //盤面生成
   public void s_generategrids(int i) {
	   //　10＝石破壊　　30＝2回行動　40＝お邪魔し石　50＝上下1マスをひっくり返す　60＝盤面反転　70＝盤面隠し　80＝革命　
     //後々追加で
	   if(i==0) {
		 grids[1][6]=60;
		 grids[5][1]=10;
		 grids[3][1]=40;
		 grids[6][7]=30;
		 grids[6][5]=50;
		 grids[1][1]=80;
	   }else if(i==1) {
			 grids[1][6]=80;
			 grids[5][1]=10;
			 grids[3][1]=40;
			 grids[6][7]=30;
			 grids[7][5]=60;
			 grids[0][0]=10;
	   }else if(i==2) {
		   grids[0][1]=70;
		   grids[7][5]=30;
		   grids[1][3]=40;
		   grids[2][5]=40;
		   grids[0][7]=10;

	   }
/*while(special<9) {

			int s_x=random.nextInt(row);
			int s_y=random.nextInt(row);
if((s_x<2||s_y<2)||(s_x>7||s_y>7)) {
int event=10*(random.nextInt(8)+1);
grids[s_y][s_x]=event;
special++;
                                    }
                 }*/ //ランダム生成

   }
 //おける場所探索
   public boolean s_checkPlaceable() {
pass_flag=true;

		for(int i=0;i<row;i++) {
			for(int j=0;j<row;j++) {
				//空の盤面のみチェックする
				if(grids[i][j]%10==empty||grids[i][j]%10==placeable) {
			    s_turnLeftUp(j, i,false);
			    s_turnUp(j, i,false);
			    s_turnRightUp(j, i,false);
			    s_turnLeft(j, i,false);
			    s_turnRight(j, i,false);
			    s_turnLeftDown(j, i,false);
			    s_turnDown(j, i,false);
			    s_turnRightDown(j, i,false);

			    if(placeableflag==true) {//もしおける場所が見つかったならおける場所変数を入れてパスフラグを折りパスカウントをリセット
			    	pass_flag=false;
			    	int cash;
			    	cash=grids[i][j]/10;
			    	grids[i][j]=10*cash+placeable;
			    	pass_count=0;
			    	placeableflag=false;
			    }else {
			    	int cash;
			    	cash=grids[i][j]/10;
			    	grids[i][j]=cash*10;
			    }
				}
			}
		}
		if(pass_flag==true) {
			pass_count++;
			//パスフラグがtrueのままならパスカウントを増加
		}
		return !pass_flag;
	}
	//石設置（発動イベントを戻り値）
   public int s_setStone(int x, int y) {
int event=-1;
event=grids[y][x]/10;//イベントを抽出

		    // 駒を配置できる場合
		    if (grids[y][x]%10==placeable) {

		        //盤面を保存する



		    	grids[y][x] = turn;


		      // ひっくり返す処理
		      s_turnStone(x, y);
		    //手数を増やす
		      moves_count++;


		    }
		    return event;//イベントを戻り値とする。
		 //1＝石破壊　3＝2回行動　4＝お邪魔石　5＝上下1マスを自分の石で埋める　6＝盤面反転　7＝盤面隠し　8＝革命
		  }

	//ひっくり返す処理
   public void s_turnStone(int x, int y) {

		    // 8方向の駒の配置を確認し、ひっくり返す

		    s_turnLeftUp(x, y,true);
		    s_turnUp(x, y,true);
		    s_turnRightUp(x, y,true);
		    s_turnLeft(x, y,true);
		    s_turnRight(x, y,true);
		    s_turnLeftDown(x, y,true);
		    s_turnDown(x, y,true);
		    s_turnRightDown(x, y,true);

		  }

//石破壊（一つだと地味なんで周辺1マス破壊にするかも）←やっ１マス
   public void destroystone(int x,int y) {
int cash;

cash=grids[y][x]/10;
	   grids[y][x]=cash*10+empty;

}
 //お邪魔石(一応イベントマスにおいてもイベントは消費されないようにしてる)
   public void setgarbage(int x,int y) {
	   int cash;
	   cash=grids[y][x]/10;
	   grids[y][x]=cash*10+garbage;
	   System.out.println("中の値"+grids[y][x]);
   }
//盤面反転
   public void inversion() {
	   for(int y=0;y<row;y++) {
		   for(int x=0;x<row;x++) {
			   if(grids[y][x]==white)
				   grids[y][x]=black;
			   else if(grids[y][x]==black)
				   grids[y][x]=white;

		   }
	   }
   }
//上下1マスに石を置く（これもおもったより地味だったから上下マス全てに石配置にした）
   public void set_cross(int x,int y) {//引数はsetstoneのものと同じで
	   for(int i=-7;i<row;i++) {

	   if((y+i)>-1&&(y+i)<row) {
	   grids[y+i][x]=turn;
	   }
	   if((x+i)>-1&&(x+i)<row){
		   grids[y][x+i]=turn;
	   }

		   }
	   s_draw();
		   }
//革命
   public void revolution() {
	  if(revolution_flag==false)
	   revolution_flag=true;
	  else {
		  revolution_flag=false;
	  }
   }
   //flagは盤面に操作を反映する場合にはtrueしない場合はfalse
		  public void s_turnLeftUp(int x, int y,boolean flag) {
		    if (y > 1 && x > 1) {

		      // となりの駒
		      int next = grids[y - 1][x - 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x - i < 0 || y - i < 0 || grids[y - i][x - i]%10==empty||grids[y-i][x-i]%10==placeable||grids[y-i][x-i]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y - i][x - i]==turn) {
		            // 自駒の場合
		if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		 if(flag==true) {           for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y - t][x - t] =turn;

		            }
		 }

		            break;
		          }
		        }
		      }

		    }
		  }

		   public void s_turnUp(int x, int y,boolean flag) {
		    if (y > 1) {

		      // となりの駒
		      int next = grids[y - 1][x];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (y - i < 0 || grids[y - i][x]%10==empty||grids[y-i][x]%10==placeable||grids[y-i][x]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y - i][x]==turn) {
		            // 自駒の場合
		        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true) {       for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y - t][x] =turn;

		            }
		        	  }
		            break;
		          }
		        }
		      }

		    }
		  }

		   public void s_turnRightUp(int x, int y,boolean flag) {
		    if (y > 1 && x < 6) {

		      // となりの駒
		      int next = grids[y - 1][x + 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x + i > 7 || y - i < 0 || grids[y - i][x + i]%10==empty||grids[y-i][x+i]%10==placeable||grids[y-i][x+i]%10==garbage) {
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

		   public void s_turnDown(int x, int y,boolean flag) {
		    if (y < 6) {

		      // となりの駒
		      int next = grids[y + 1][x];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (y + i > 7 ||grids[y + i][x]%10==empty||grids[y+i][x]%10==placeable||grids[y+i][x]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y + i][x]==turn) {
		            // 自駒の場合
		        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true)  {       for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y + t][x] =turn;

		            }
		        	  }
		            break;
		          }
		        }
		      }

		    }
		  }

		  public void s_turnRight(int x, int y,boolean flag) {
		    if (x < 6) {

		      // となりの駒
		      int next = grids[y][x + 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x + i > 7 || grids[y][x + i]%10==empty||grids[y][x+i]%10==placeable||grids[y][x+i]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y][x + i]==turn) {
		            // 自駒の場合
		        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true)  {      for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y][x + t] =turn;

		            }
		        	  }
		            break;
		          }
		        }
		      }

		    }
		  }

		   public void s_turnLeftDown(int x, int y,boolean flag) {
		    if (y < 6 && x > 1) {

		      // となりの駒
		      int next = grids[y + 1][x - 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x - i < 0 || y + i > 7 || grids[y + i][x - i]%10==empty||grids[y+i][x-i]%10==placeable||grids[y+i][x-i]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y + i][x - i]==turn) {
		            // 自駒の場合
		        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y + t][x - t] =turn;

		            }
		        	  }
		            break;
		          }
		        }
		      }

		    }
		  }

		   public void s_turnLeft(int x, int y,boolean flag) {
		    if (x > 1) {

		      // となりの駒
		      int next =grids[y][x - 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x - i < 0 || grids[y][x - i]%10==empty||grids[y][x-i]%10==placeable||grids[y][x-i]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y][x - i]==turn) {
		            // 自駒の場合
		        	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true)  {        for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		        		  grids[y][x - t] =turn;

		            }
		        	  }
		            break;
		          }
		        }
		      }

		    }
		  }

		   public void s_turnRightDown(int x, int y,boolean flag) {
		    if (y < 6 && x < 6) {

		      // となりの駒
		     int  next = grids[y + 1][x + 1];

		      // となりの駒が裏駒の場合
		      if (next==turn*-1) {

		        // さらにその一つとなりから順に確認
		        for (int i = 2; true; i++) {

		          if (x + i > 7 || y + i > 7 || grids[y + i][x + i]%10==empty||grids[y+i][x+i]%10==placeable||grids[y+i][x+i]%10==garbage) {
		            // 駒がない場合終了
		            break;
		          } else if (grids[y + i][x + i]==turn) {
		            // 自駒の場合
		          	  if(flag==false&&placeableflag==false) {placeableflag=true;}
		            // あいだの駒をすべて自駒にひっくりかえす
		        	  if(flag==true)  {         for (int t = 1; t < i; t++) {
		              // 配列の要素を上書き

		              grids[y + t][x + t] =turn;

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
public void s_draw() {
	System.out.println("　０１２３４５６７");
	for(int j=0;j<8;j++) {
		System.out.print(" "+j);
	for(int i=0;i<8;i++) {

		if(grids[j][i]==white)
		System.out.print("○");
		else if(grids[j][i]==black)
			System.out.print("●");
		else if(grids[j][i]%10==garbage)
			System.out.print("□");
		else if(grids[j][i]%10==placeable)
			System.out.print("△");
		else if(grids[j][i]==empty)
			System.out.print("  ");
		else if(grids[j][i]/10!=empty&&grids[j][i]%10==empty) {

		System.out.print("☆");
	}

	}
	System.out.println("");
}
}
public void s_match(int check,int x,int y) {
	int c2;
	int x2;int y2;
	Scanner scan=new Scanner(System.in);


	if(check==1) {//石破壊
		s_draw();
		do {
		System.out.print("破壊する石を選択してください。\nx=");

		 x2=scan.nextInt();
		System.out.print("y=");
		 y2=scan.nextInt();}while(grids[y2][x2]%10==placeable||grids[y2][x2]==empty);
		destroystone(x2,y2);

	}else if(check==3) {//二階行動
changeTurn();
changeTurn();
s_checkPlaceable();
		if(pass_flag==true)
			changeTurn();
		if(pass_flag==false) {
		s_draw();
		System.out.print("もう一度行動できます。石を置く場所を選択してください。\nx=");
		 x2=scan.nextInt();
		System.out.print("y=");
		 y2=scan.nextInt();
		 c2=s_setStone(x2, y2);
		 if(c2!=0)
		 s_match(c2,x2,y2);

		}
		}else if(check==4) {
			checkPlaceable();
			s_draw();
			do {
			System.out.print("お邪魔石を置いてください。\nx=");
			 x2=scan.nextInt();
				System.out.print("y=");
				 y2=scan.nextInt();
				 }while(grids[y2][x2]==white||grids[y2][x2]==black);
				 setgarbage(x2,y2);

		}else if(check==5) {//上下1マスを埋める
			System.out.print("上下1マスに石を設置します。\n");
			set_cross(x, y);

		}else if(check==6) {//色反転
			System.out.print("盤面の石の色を全て反転します\n");
			inversion();

		}else if(check==8) {//革命

			revolution();
			if(revolution_flag==true) {
				System.out.print("以降は取得石の少ないプレイヤーの勝利となります。\n");
			}else {
				System.out.print("以降は取得石の多いプレイヤーの勝利となります。\n");
			}

		}

}


public static void main(String args[]) {//CPU同士で対戦できるようにしてる
int i=0;
int alpha=0;
int randomc=0;
while(i<30) {//ここのiが対局数

	 Othello a=new Othello(0);
	Othello b=new Othello(0);
	Computer com=new Computer(7,a.white);
	Computer com2=new Computer(1,a.black);
	Scanner scan=new Scanner(System.in);


a.s_generategrids(0);
int check;
int x,y;
	while(a.isGameover()==false) {
check=0;
		a.checkPlaceable();

if(a.turn==a.black) {

		if(a.pass_flag==true)
			a.changeTurn();
		a.draw();
		if(a.pass_flag==false) {
/*			do {
System.out.print("Player1\nx=");
 x=scan.nextInt();
System.out.print("y=");
 y=scan.nextInt();}while(a.grids[y][x]%10!=a.placeable);
check=a.s_setStone(x, y);
a.s_match(check, x, y);
a.changeTurn();*/




	int put2=com2.think(a.getGrids());
a.setStone(put2%10,(put2-put2%10)/10);
System.out.println("Game_count:"+i);
System.out.println("\nrandom_cpu_turn\n x="+put2%10+" y="+(put2-put2%10)/10);






	}}
		else if(a.turn==a.white) {
			check=0;

			if(a.pass_flag==true)
				a.changeTurn();
			a.draw();
			if(a.pass_flag==false) {

			int put=com.think(a.getGrids());
			a.setStone(put%10,(put-put%10)/10);
			System.out.println("Game_count:"+i);
			System.out.println("\nalphabeta_cpu_turn\n x="+put%10+" y="+(put-put%10)/10);

		/*		do {
				System.out.print("Player2\nx=");
				 x=scan.nextInt();
				System.out.print("y=");
				 y=scan.nextInt();}while(a.grids[y][x]%10!=a.placeable);
				check=a.s_setStone(x, y);
				a.s_match(check,x,y);
				a.changeTurn();*/


			}

			}
		}


if(a.checkWinner()==a.white) {
	System.out.println("White win");
	System.out.println("white:"+a.getWhitestone()+"black:"+a.getBlackstone());
	alpha++;
}else if(a.checkWinner()==a.black) {
	System.out.println("black win");
	System.out.println("white:"+a.getWhitestone()+"black:"+a.getBlackstone());
	randomc++;
}else if(a.checkWinner()==0) {
	System.out.println("draw");
	System.out.println("white:"+a.getWhitestone()+"black:"+a.getBlackstone());

}



i++;
}
System.out.println("alpha_win:"+alpha+"random_win:"+randomc);
}
}
