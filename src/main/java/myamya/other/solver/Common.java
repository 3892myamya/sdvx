package myamya.other.solver;

import java.util.HashSet;
import java.util.Set;

public class Common {
	public enum Masu {
		SPACE("　", 0), NOT_BLACK("・", 1), BLACK("■", 2);

		String str;
		int val;

		Masu(String str, int val) {
			this.str = str;
			this.val = val;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Masu getByVal(int val) {
			for (Masu one : Masu.values()) {
				if (one.val == val) {
					return one;
				}
			}
			return null;
		}

	}

	public enum Wall {
		SPACE("＊"), NOT_EXISTS("　"), EXISTS("□");

		String str;

		Wall(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public enum Difficulty {
		RAKURAKU("らくらく", 0), OTEGORO("おてごろ", 1), TAIHEN("たいへん", 2), AZEN("アゼン", 3), HABANERO("ハバネロ", 4);

		String str;
		int val;

		Difficulty(String str, int val) {
			this.str = str;
			this.val = val;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Difficulty getByVal(int val) {
			for (Difficulty one : Difficulty.values()) {
				if (one.val == val) {
					return one;
				}
			}
			return null;
		}

		public static Difficulty getByCount(int count) {
			if (count < 50) {
				return Difficulty.RAKURAKU;
			} else if (count < 500) {
				return Difficulty.OTEGORO;
			} else if (count < 5000) {
				return Difficulty.TAIHEN;
			} else if (count < 50000) {
				return Difficulty.AZEN;
			} else {
				return Difficulty.HABANERO;
			}

		}
	}

	public static class Position {

		@Override
		public String toString() {
			return "[" + yIndex + "," + xIndex + "]";
		}

		private final int yIndex;
		private final int xIndex;

		public Position(int yIndex, int xIndex) {
			this.yIndex = yIndex;
			this.xIndex = xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public int getxIndex() {
			return xIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + xIndex;
			result = prime * result + yIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (xIndex != other.xIndex)
				return false;
			if (yIndex != other.yIndex)
				return false;
			return true;
		}

	}

	/**
	 * 方向を示す列挙型
	 */
	public enum Direction {
		UP("u", 1, "↑", "▲"), RIGHT("r", 4, "→", "►"), DOWN("d", 2, "↓", "▼"), LEFT("l", 3, "←", "◄");
		private final String str;
		private final int num;
		private final String directString;
		private final String triangle;

		Direction(String str, int num, String directString, String triangle) {
			this.str = str;
			this.num = num;
			this.directString = directString;
			this.triangle = triangle;
		}

		@Override
		public String toString() {
			return str;
		}

		public int toNum() {
			return num;
		}

		public Direction opposite() {
			if (this == UP) {
				return DOWN;
			} else if (this == RIGHT) {
				return LEFT;
			} else if (this == DOWN) {
				return UP;
			} else if (this == LEFT) {
				return RIGHT;
			} else {
				return null;
			}
		}

		public static Direction getByStr(String str) {
			for (Direction one : Direction.values()) {
				if (one.toString().equals(str)) {
					return one;
				}
			}
			return null;
		}

		public static Direction getByNum(int num) {
			for (Direction one : Direction.values()) {
				if (one.num == num) {
					return one;
				}
			}
			return null;
		}

		public String getDirectString() {
			return directString;
		}

		public String getTriangle() {
			return triangle;
		}

	}

	/**
	 * 四角の情報
	 */
	public static class Sikaku {
		private final Position leftUp;
		private final Position rightDown;

		public Position getLeftUp() {
			return leftUp;
		}

		public Position getRightDown() {
			return rightDown;
		}

		/**
		 * 面積を返す
		 */
		public int getAreaSize() {
			return (rightDown.getyIndex() - leftUp.getyIndex() + 1) * (rightDown.getxIndex() - leftUp.getxIndex() + 1);
		}

		/**
		 * 自分とotherの領域がかぶっている場合trueを返す。
		 */
		public boolean isDuplicate(Sikaku other) {
			if (this.rightDown.getyIndex() < other.leftUp.getyIndex()) {
				return false;
			}
			if (this.rightDown.getxIndex() < other.leftUp.getxIndex()) {
				return false;
			}
			if (this.leftUp.getyIndex() > other.rightDown.getyIndex()) {
				return false;
			}
			if (this.leftUp.getxIndex() > other.rightDown.getxIndex()) {
				return false;
			}
			return true;
			// 自分の上が相手の下より上、かつ、自分の右が相手の左より右
			// または、自分の左上が、相手の左上と右下の間にあれば、重複している
		}

		/**
		 * 自分とotherの領域がかぶったり接している場合trueを返す。
		 */
		public boolean isDuplicateBig(Sikaku other) {
			if (this.rightDown.getyIndex() < other.leftUp.getyIndex() - 1 ||
					this.rightDown.getxIndex() < other.leftUp.getxIndex() ||
					this.leftUp.getyIndex() > other.rightDown.getyIndex() + 1 ||
					this.leftUp.getxIndex() > other.rightDown.getxIndex()) {
				if (this.rightDown.getyIndex() < other.leftUp.getyIndex() ||
						this.rightDown.getxIndex() < other.leftUp.getxIndex() - 1 ||
						this.leftUp.getyIndex() > other.rightDown.getyIndex() ||
						this.leftUp.getxIndex() > other.rightDown.getxIndex() + 1) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
			// 自分の上が相手の下より上、かつ、自分の右が相手の左より右
			// または、自分の左上が、相手の左上と右下の間にあれば、重複している
		}

		/**
		 * 自分とposの領域がかぶっている場合trueを返す。
		 */
		public boolean isDuplicate(Position pos) {
			if (this.rightDown.getyIndex() < pos.getyIndex()) {
				return false;
			}
			if (this.rightDown.getxIndex() < pos.getxIndex()) {
				return false;
			}
			if (this.leftUp.getyIndex() > pos.getyIndex()) {
				return false;
			}
			if (this.leftUp.getxIndex() > pos.getxIndex()) {
				return false;
			}
			return true;
			// 自分の上が相手の下より上、かつ、自分の右が相手の左より右
			// または、自分の左上が、相手の左上と右下の間にあれば、重複している
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((leftUp == null) ? 0 : leftUp.hashCode());
			result = prime * result + ((rightDown == null) ? 0 : rightDown.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Sikaku other = (Sikaku) obj;
			if (leftUp == null) {
				if (other.leftUp != null)
					return false;
			} else if (!leftUp.equals(other.leftUp))
				return false;
			if (rightDown == null) {
				if (other.rightDown != null)
					return false;
			} else if (!rightDown.equals(other.rightDown))
				return false;
			return true;
		}

		public Sikaku(Position leftUp, Position rightDown) {
			super();
			this.leftUp = leftUp;
			this.rightDown = rightDown;
		}

		@Override
		public String toString() {
			return "[" + leftUp + ", " + rightDown + "]";
		}

		/**
		 * 自身の四角内に含まれる位置をランダムで1つ返す。
		 */
		public Position getRandomPos() {
			int yLength = getRightDown().getyIndex() - getLeftUp().getyIndex();
			int xLength = getRightDown().getxIndex() - getLeftUp().getxIndex();
			return new Position((int) (getLeftUp().getyIndex() + (Math.random() * yLength)),
					(int) (getLeftUp().getxIndex() + (Math.random() * xLength)));
		}

		/**
		 * 自分が包含する位置のセットを返す。
		 */
		public Set<Position> getPosSet() {
			Set<Position> result = new HashSet<>();
			for (int y = leftUp.yIndex; y <= rightDown.yIndex; y++) {
				for (int x = leftUp.xIndex; x <= rightDown.xIndex; x++) {
					result.add(new Position(y, x));
				}
			}
			return result;
		}
	}

	public static class GeneratorResult {

		private final String status;
		private final String result;
		private final String link;
		private final String url;
		private final int level;
		private final String txt;

		public GeneratorResult(String status) {
			this.status = status;
			result = "";
			link = "";
			url = "";
			level = 0;
			txt = "";
		}

		public GeneratorResult(String status, String result, String link, String url, int level, String txt) {
			this.status = status;
			this.result = result;
			this.link = link;
			this.url = url;
			this.level = level;
			this.txt = txt;
		}

		public String getStatus() {
			return status;
		}

		public String getResult() {
			return result;
		}

		public String getLink() {
			return link;
		}

		public String getUrl() {
			return url;
		}

		public int getLevel() {
			return level;
		}

		public String getTxt() {
			return txt;
		}

	}

	public static class CountOverException extends RuntimeException {
		// ガチャでカウントオーバーなのにまれにOK判定が出ることがあるらしいので、
		// 強制的に例外扱いにするためのクラス。
	}

	public static class AkariBattleResult {

		private final String status;
		private final String fieldStr;
		private final boolean end;

		public AkariBattleResult(String status) {
			this.status = status;
			fieldStr = "";
			end = false;
		}

		public AkariBattleResult(String status, String fieldStr, boolean end) {
			this.status = status;
			this.fieldStr = fieldStr;
			this.end = end;
		}

		public String getStatus() {
			return status;
		}

		public String getFieldStr() {
			return fieldStr;
		}

		public boolean isEnd() {
			return end;
		}

	}

}
