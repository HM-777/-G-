import java.util.Arrays;

public class PlayerDriver {

	public static void main(String [] args) throws Exception{

		Player player = new Player(null,null,null);
		int a[] = {10,8,1,1,0,1500};

		System.out.println("setNameで「本田翼」を入力します");
		player.setName("本田翼");
		System.out.println("getName出力: " + player.getName());
		System.out.println();

		System.out.println("setOnlineで「true」を入力します");
		player.setOnline(true);
		System.out.println("getOnline出力: " + player.getOnline());
		System.out.println();

		System.out.println("setColorで「black」を入力します");
		player.setColor("black");
		System.out.println("getColor出力: " + player.getColor());
		System.out.println();

		System.out.println("setIDで「100」と「abc」を入力します");
		player.setID("100", "abc");
		System.out.println("getID出力: " + player.getID() +", "+ player.getPass());
		System.out.println();

		System.out.println("MakeAccountで「100」と「abc」を入力します");
		player.MakeAccount("100", "abc");
		System.out.println("getAccount出力: " + player.getAccount());
		System.out.println();

		System.out.println("setRecordで「10,8,1,1,0,1500」を入力します");
		player.setRecord(a);
		System.out.println("getRecord出力（配列データそのまま）:" + player.getRecord());
		System.out.println("Arrays.toString(a)で配列の中身を念のため表示" + Arrays.toString(a));
		System.out.println();

		System.out.println("setMyPlaerNoで「100」を入力します");
		player.setMyPlayerNo(100);
		System.out.println("getMyPlaerNo出力: " + player.getMyPlayerNo());
		System.out.println();

		System.out.println("setOpponentPlaerNoで「101」を入力します");
		player.setOpponentPlayerNo(101);
		System.out.println("getOpponentPlaerNo出力: " + player.getOpponentPlayerNo());
	}

}
