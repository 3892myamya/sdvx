package myamya.other.solver.lookair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Solver;

public class LookairSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 四角の配置の候補
		protected List<Sikaku> squareCand;
		// 確定した四角候補
		protected List<Sikaku> squareFixed;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			//			sb.append("http://pzv.jp/p.html?tasquare/" + getXLength() + "/" + getYLength() + "/");
			//			int interval = 0;
			//			for (int i = 0; i < getYLength() * getXLength(); i++) {
			//				int yIndex = i / getXLength();
			//				int xIndex = i % getXLength();
			//				if (numbers[yIndex][xIndex] == null) {
			//					interval++;
			//					if (interval == 20) {
			//						sb.append("z");
			//						interval = 0;
			//					}
			//				} else {
			//					Integer num = numbers[yIndex][xIndex];
			//					String numStr = null;
			//					if (num == -1) {
			//						numStr = ".";
			//					} else {
			//						numStr = Integer.toHexString(num);
			//						if (numStr.length() == 2) {
			//							numStr = "-" + numStr;
			//						} else if (numStr.length() == 3) {
			//							numStr = "+" + numStr;
			//						}
			//					}
			//					if (interval == 0) {
			//						sb.append(numStr);
			//					} else {
			//						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			//						sb.append(numStr);
			//						interval = 0;
			//					}
			//				}
			//			}
			//			if (interval != 0) {
			//				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			//			}
			//			if (sb.charAt(sb.length() - 1) == '.') {
			//				sb.append("/");
			//			}
			return sb.toString();
		}

		public String getHintCount() {
			int kuroCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != -1) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + kuroCnt);
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = new ArrayList<>();
			squareFixed = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (int size = 1; size <= (getXLength() < getYLength() ? getXLength() : getYLength()); size++) {
						int maxY = yIndex + size > getYLength() ? getYLength() - size : yIndex;
						int maxX = xIndex + size > getXLength() ? getXLength() - size : xIndex;
						for (int y = yIndex; y <= maxY; y++) {
							for (int x = xIndex; x <= maxX; x++) {
								Sikaku sikaku = new Sikaku(new Position(y, x),
										new Position(y + size - 1, x + size - 1));
								squareCand.add(sikaku);
							}
						}
					}
				}
			}
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			numbers = other.numbers;
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		/**
		 * 候補情報からマスを復元する
		 */
		public Masu[][] getMasu() {
			Masu[][] masu = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			for (Sikaku fixed : squareFixed) {
				for (int yIndex = fixed.getLeftUp().getyIndex(); yIndex <= fixed.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = fixed.getLeftUp().getxIndex(); xIndex <= fixed.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			for (Sikaku cand : squareCand) {
				for (int yIndex = cand.getLeftUp().getyIndex(); yIndex <= cand.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = cand.getLeftUp().getxIndex(); xIndex <= cand.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
			}
			return masu;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			Masu[][] masu = getMasu();
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("□");
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
						sb.append(masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。
		 */
		private boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				// TODO 同じ列にあり、間に他の四角が挟まらない同サイズの四角も候補から消す
				Sikaku removeSikaku1 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex() - 1, fixed.getLeftUp().getxIndex()),
						new Position(fixed.getRightDown().getyIndex() + 1, fixed.getRightDown().getxIndex()));
				Sikaku removeSikaku2 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex(), fixed.getLeftUp().getxIndex() - 1),
						new Position(fixed.getRightDown().getyIndex(), fixed.getRightDown().getxIndex() + 1));
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(removeSikaku1) || sikaku.isDuplicate(removeSikaku2)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 四角を配置して数字の条件を満たせるかを調査する。
		 * 満たせない場合falseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int num = numbers[yIndex][xIndex];
						int fixMe = 0;
						int fixUp = 0;
						int fixRight = 0;
						int fixDown = 0;
						int fixLeft = 0;
						for (Sikaku fixed : squareFixed) {
							if (fixed.isDuplicate(new Position(yIndex, xIndex))) {
								fixMe = 1;
							}
							if (fixed.isDuplicate(new Position(yIndex - 1, xIndex))) {
								fixUp = 1;
							}
							if (fixed.isDuplicate(new Position(yIndex, xIndex + 1))) {
								fixRight = 1;
							}
							if (fixed.isDuplicate(new Position(yIndex + 1, xIndex))) {
								fixDown = 1;
							}
							if (fixed.isDuplicate(new Position(yIndex, xIndex - 1))) {
								fixLeft = 1;
							}
						}
						int fixCnt = fixMe + fixUp + fixRight + fixDown + fixLeft;
						if (num < fixCnt) {
							return false;
						}
						int candMe = fixMe;
						int candUp = fixUp;
						int candRight = fixRight;
						int candDown = fixDown;
						int candLeft = fixLeft;
						for (Sikaku cand : squareCand) {
							if (cand.isDuplicate(new Position(yIndex, xIndex))) {
								candMe = 1;
							}
							if (cand.isDuplicate(new Position(yIndex - 1, xIndex))) {
								candUp = 1;
							}
							if (cand.isDuplicate(new Position(yIndex, xIndex + 1))) {
								candRight = 1;
							}
							if (cand.isDuplicate(new Position(yIndex + 1, xIndex))) {
								candDown = 1;
							}
							if (cand.isDuplicate(new Position(yIndex, xIndex - 1))) {
								candLeft = 1;
							}
						}
						int candCnt = candMe + candUp + candRight + candDown + candLeft;
						if (num > candCnt) {
							continue;
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public LookairSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public LookairSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?lookair/10/10/b101c1b2d11b2r3b3d2l1j1j2a5d2g1b"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new LookairSolver(height, width, param).solve());
	}

	@Override
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 2 / 3);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		System.out.println(field);
		String str = field.getStateDump();
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator
				.hasNext();) {
			count++;
			Sikaku oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.squareCand.remove(oneCand);
			virtual.squareFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.squareCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.squareCand = virtual2.squareCand;
				field.squareFixed = virtual2.squareFixed;
			} else if (!allowNotBlack) {
				field.squareCand = virtual.squareCand;
				field.squareFixed = virtual.squareFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}