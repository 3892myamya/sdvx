package myamya.other.solver.akari;

public class Main {
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

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報。BLACKは明かりあり
		protected Masu[][] masu;
		// 数字の情報
		protected Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "Ｏ" : masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字指定のあるマスのうち、確定する箇所を照明にする。
		 * 照明が過剰・不足の場合はfalseを返す。
		 */
		public boolean akariSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 || numbers[yIndex - 1][xIndex] != null ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 || numbers[yIndex][xIndex + 1] != null
								? Masu.NOT_BLACK
								: masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 || numbers[yIndex + 1][xIndex] != null
								? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 || numbers[yIndex][xIndex - 1] != null ? Masu.NOT_BLACK
								: masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < blackCnt) {
							// 明かり過剰
							return false;
						}
						if (numbers[yIndex][xIndex] == blackCnt) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 明かり不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 照明が照らしているマスを白マス確定にする。
		 * 照明が照らしているマスに既に照明があったらfalseを返す。
		 */
		public boolean lightSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						int targetY = yIndex - 1;
						while (targetY >= 0 && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[targetY][xIndex] = Masu.NOT_BLACK;
							targetY--;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[targetY][xIndex] = Masu.NOT_BLACK;
							targetY++;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][targetX] = Masu.NOT_BLACK;
							targetX--;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][targetX] = Masu.NOT_BLACK;
							targetX++;
						}
					}
				}
			}
			return true;
		}

		/**
		 * どこからも光が当たる可能性がないマスを照明にする。
		 * どこからも光が当たる可能性がないマスが白マス確定マスだった場合falseを返す。
		 */
		public boolean shadowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK || masu[yIndex][xIndex] == Masu.SPACE) {
						if (numbers[yIndex][xIndex] != null) {
							continue;
						}
						boolean isOk = false;
						int targetY = yIndex - 1;
						while (targetY >= 0 && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetY--;
						}
						if (isOk) {
							continue;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetY++;
						}
						if (isOk) {
							continue;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetX--;
						}
						if (isOk) {
							continue;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetX++;
						}
						if (isOk) {
							continue;
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!akariSolve()) {
				return false;
			}
			if (!lightSolve()) {
				return false;
			}
			if (!shadowSolve()) {
				return false;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(this);
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] == null) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public Main(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Main(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?akari/10/10/bjchaj.n.hclbgdncg.na.ncjbhaja"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new Main(height, width, param).solve());
	}

	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 15));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 15 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 15).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE && field.numbers[yIndex][xIndex] == null) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.masu = virtual2.masu;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}
