
public class ComputerDriver {

	public static void main(String[] args) {
		int set,x,y;
		Othello o = new Othello(0);
		Computer cpu1 = new Computer(1,-1);
		Computer cpu2 = new Computer(7,1);

		System.out.println("---TEST GAME---\n\n");

		while(o.isGameover() == false) {
			if(o.checkPlaceable()) {
				o.draw();
				set = cpu1.seek(o.getGrids());
				x = set % 10;
				y = (set - set%10)/10;
				System.out.println("\nRandom:" + set + " → X = " + x + ", Y = " + y + "\n");
				o.setStone(x, y);
			}else {
				o.changeTurn();
			}
			if(o.checkPlaceable()) {
				o.draw();
				set = cpu2.seek(o.getGrids());
				x = set % 10;
				y = (set - set%10)/10;
				System.out.println("\nα-β:" + set + " → X = " + x + ", Y = " + y + "\n");
				o.setStone(x, y);
			}else {
				o.changeTurn();
			}
			o.checkPlaceable();
		}

		o.draw();

		if(o.checkWinner() == 1) {
			System.out.println("White win");
			System.out.println("white:"+o.getWhitestone()+"black:"+o.getBlackstone());
		}else if(o.checkWinner() == -1) {
			System.out.println("black win");
			System.out.println("white:"+o.getWhitestone()+"black:"+o.getBlackstone());
		}else if(o.checkWinner()==0) {
			System.out.println("draw");
			System.out.println("white:"+o.getWhitestone()+"black:"+o.getBlackstone());
		}

	}

}
