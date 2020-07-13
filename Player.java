public class Player {

    private String myName = ""; //プレイヤ名
    private String myColor = ""; //先手後手情報(白黒)
    private String myID = "";  //プレイヤーID
    private String myPassword = "";  //パスワード
    private int myRecord[] = null;  //戦績情報
    private int myPlayerNo;
    private int opponentPlayerNo;

    public Player(String NAME,String ID, String PASSWORD) {  //コンストラクタ
    	this.myName = NAME;
    	this.myID = ID;
    	this.myPassword = PASSWORD;
    }
    public void setName(String name){ // プレイヤ名を受付
        myName = name;
    }
    public String getName(){ // プレイヤ名を取得
    	return myName;
    }
    public void setColor(String c){ // 先手後手情報の受付
        myColor = c;
    }
    public String getColor(){ // 先手後手情報の取得
        return myColor;
    }
    public void setID(String id, String pw) {  //ログイン受付
    	myID = id;
    	myPassword = pw;
    }
    public String getID() {
    	return myID;
    }
    public String getPass() {
    	return myPassword;
    }
    public void MakeAccount(String id, String pw) {  //アカウント作成
    	myID = id;
    	myPassword = pw;
    }
    public String getAccount(){  //アカウント情報取得
    	return myID + myPassword;
    }
    public void setRecord(int r[]) {  //戦績情報の受付
    	int rec = 5;
    	for(int i=0; i<rec; i++) {
    		myRecord[i] = r[i];
        }
    }
    public int[] getRecord() {  //戦績情報取得
    	return myRecord;
    }

    public void setMyPlayerNo(int i) {
    	myPlayerNo = i;
    }

    public int getMyPlayerNo() {
    	return myPlayerNo;
    }

    public void setOpponentPlayerNo(int i) {
    	opponentPlayerNo = i;
    }

    public int getOpponentPlayerNo() {
    	return opponentPlayerNo;
    }

}