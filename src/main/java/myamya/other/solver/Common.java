package myamya.other.solver;

public class Common {
	public enum Masu {
		SPACE("　"), BLACK("■"), NOT_BLACK("・");

		String str;

		Masu(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public enum Difficulty {
		RAKURAKU("らくらく", 0), OTEGORO("おてごろ", 1), TAIHEN("たいへん", 2), AZEN("アゼン", 3);

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
	}

	public static class Position {

		@Override
		public String toString() {
			return "Position [yIndex=" + yIndex + ", xIndex=" + xIndex + "]";
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
		UP("u", 1, "↑"), RIGHT("r", 4, "→"), DOWN("d", 2, "↓"), LEFT("l", 3, "←");
		private final String str;
		private final int num;
		private final String directString;

		Direction(String str, int num, String directString) {
			this.str = str;
			this.num = num;
			this.directString = directString;
		}

		@Override
		public String toString() {
			return str;
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
	}
}
