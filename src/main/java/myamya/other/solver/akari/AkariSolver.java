package myamya.other.solver.akari;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class AkariSolver implements Solver {

	public enum Masu {
		/** 未確定 */
		SPACE("　", false, -1),
		/** 数字なし壁 */
		WALL_F("■", true, -1),
		/** 数字あり壁 */
		WALL_0("０", true, 0), WALL_1("１", true, 1), WALL_2("２", true, 2), WALL_3("３", true, 3), WALL_4("４", true, 4),
		/** 照明あり */
		AKARI("○", false, -1),
		/** 照明なし */
		NOT_AKARI("・", false, -1);

		String str;
		boolean isWall;
		int cnt;

		Masu(String str, boolean isWall, int cnt) {
			this.str = str;
			this.isWall = isWall;
			this.cnt = cnt;
		}

		public boolean isWall() {
			return isWall;
		}

		public int getCnt() {
			return cnt;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Masu getByCnt(int cnt) {
			for (Masu one : Masu.values()) {
				if (one.cnt == cnt) {
					return one;
				}
			}
			return null;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
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
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.WALL_F;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.getByCnt(ALPHABET.indexOf(ch));
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.getByCnt(Character.getNumericValue(ch) - 5);
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.getByCnt(Character.getNumericValue(ch));
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
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
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
					int cnt = masu[yIndex][xIndex].getCnt();
					if (cnt != -1) {
						Map<Position, Masu> masuMap = new HashMap<>();
						masuMap.put(new Position(yIndex - 1, xIndex),
								yIndex == 0 ? Masu.WALL_F : masu[yIndex - 1][xIndex]);
						masuMap.put(new Position(yIndex, xIndex + 1),
								xIndex == getXLength() - 1 ? Masu.WALL_F : masu[yIndex][xIndex + 1]);
						masuMap.put(new Position(yIndex + 1, xIndex),
								yIndex == getYLength() - 1 ? Masu.WALL_F : masu[yIndex + 1][xIndex]);
						masuMap.put(new Position(yIndex, xIndex - 1),
								xIndex == 0 ? Masu.WALL_F : masu[yIndex][xIndex - 1]);
						// 調査
						int akariCnt = 0;
						int spaceCnt = 0;
						for (Masu oneMasu : masuMap.values()) {
							if (oneMasu == Masu.AKARI) {
								akariCnt++;
							} else if (oneMasu == Masu.SPACE) {
								spaceCnt++;
							}
						}
						if (akariCnt + spaceCnt < cnt) {
							// 照明不足
							return false;
						}
						// 置かねばならない照明の数
						int retainAkariCnt = cnt - akariCnt;
						if (retainAkariCnt < 0) {
							// 黒マス超過
							return false;
						} else if (retainAkariCnt == 0) {
							// 予定照明数が既に照明数に等しければ、他のマスは白マス
							for (Entry<Position, Masu> entry : masuMap.entrySet()) {
								if (entry.getValue() == Masu.SPACE) {
									masu[entry.getKey().getyIndex()][entry.getKey().getxIndex()] = Masu.NOT_AKARI;
								}
							}
						} else if (spaceCnt == retainAkariCnt) {
							// 未確定マスが置かねばならない照明の数に等しければ、未確定マスは照明
							for (Entry<Position, Masu> entry : masuMap.entrySet()) {
								if (entry.getValue() == Masu.SPACE) {
									masu[entry.getKey().getyIndex()][entry.getKey().getxIndex()] = Masu.AKARI;
								}
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
					Set<Position> notAkariPosSet = new HashSet<>();
					if (masu[yIndex][xIndex] == Masu.AKARI) {
						int targetY = yIndex - 1;
						while (targetY >= 0 && !masu[targetY][xIndex].isWall()) {
							notAkariPosSet.add(new Position(targetY, xIndex));
							targetY--;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && !masu[targetY][xIndex].isWall()) {
							notAkariPosSet.add(new Position(targetY, xIndex));
							targetY++;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && !masu[yIndex][targetX].isWall()) {
							notAkariPosSet.add(new Position(yIndex, targetX));
							targetX--;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && !masu[yIndex][targetX].isWall()) {
							notAkariPosSet.add(new Position(yIndex, targetX));
							targetX++;
						}
						for (Position pos : notAkariPosSet) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.AKARI) {
								return false;
							} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_AKARI;
							}
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
					if (masu[yIndex][xIndex] == Masu.NOT_AKARI || masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						int targetY = yIndex - 1;
						while (targetY >= 0 && !masu[targetY][xIndex].isWall()) {
							if (masu[targetY][xIndex] != Masu.NOT_AKARI) {
								isOk = true;
								break;
							}
							targetY--;
						}
						if (isOk) {
							continue;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && !masu[targetY][xIndex].isWall()) {
							if (masu[targetY][xIndex] != Masu.NOT_AKARI) {
								isOk = true;
								break;
							}
							targetY++;
						}
						if (isOk) {
							continue;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && !masu[yIndex][targetX].isWall()) {
							if (masu[yIndex][targetX] != Masu.NOT_AKARI) {
								isOk = true;
								break;
							}
							targetX--;
						}
						if (isOk) {
							continue;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && !masu[yIndex][targetX].isWall()) {
							if (masu[yIndex][targetX] != Masu.NOT_AKARI) {
								isOk = true;
								break;
							}
							targetX++;
						}
						if (isOk) {
							continue;
						}
						if (masu[yIndex][xIndex] == Masu.NOT_AKARI) {
							return false;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.AKARI;
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return true;
		}

	}

	private final Field field;

	public AkariSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new AkariSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		int difficulty = 0;
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!solveAndCheck(field)
					|| (!befStr.equals(field.getStateDump()) && !solveAndCheck(field))) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				difficulty = difficulty <= recursiveCnt ? recursiveCnt + 1 : difficulty;
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) +
				"ms.");
		System.out.println("難易度:" + difficulty);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByVal(difficulty).toString();
	}

	/**
	 * 各種チェックを1セット実行
	 * @param recursive
	 */
	private static boolean solveAndCheck(Field field) {
		if (!field.akariSolve()) {
			return false;
		}
		if (!field.lightSolve()) {
			return false;
		}
		if (!field.shadowSolve()) {
			return false;
		}
		return true;
	}

	/**
	 * 仮置きして調べる
	 */
	private static boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private static boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			Field virtual = new Field(field);
			virtual.masu[yIndex][xIndex] = Masu.AKARI;
			String befStr = virtual.getStateDump();
			boolean allowBlack = solveAndCheck(virtual)
					&& (befStr.equals(virtual.getStateDump()) || solveAndCheck(virtual));
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.masu[yIndex][xIndex] = Masu.NOT_AKARI;
			befStr = virtual2.getStateDump();
			boolean allowNotBlack = solveAndCheck(virtual2)
					&& (befStr.equals(virtual2.getStateDump()) || solveAndCheck(virtual2));
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
		}
		return true;
	}
}
