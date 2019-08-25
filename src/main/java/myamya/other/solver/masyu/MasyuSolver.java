package myamya.other.solver.masyu;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class MasyuSolver implements Solver {

	/**
	 * 真珠
	 */
	public enum Pearl {
	SIRO("○", 1), KURO("●", 2);

		String str;
		int val;

		Pearl(String str, int val) {
			this.str = str;
			this.val = val;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Pearl getByVal(int val) {
			for (Pearl one : Pearl.values()) {
				if (one.val == val) {
					return one;
				}
			}
			return null;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 真珠の情報
		private Pearl[][] pearl;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public Pearl[][] getPearl() {
			return pearl;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean ura) {
			masu = new Masu[height][width];
			pearl = new Pearl[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					if (pos1 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl.getByVal(ura ? pos1 % 2 + 1 : pos1);
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos2 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl.getByVal(ura ? pos2 % 2 + 1 : pos2);
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos3 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl.getByVal(ura ? pos3 % 2 + 1 : pos3);
					}
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			pearl = other.pearl;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (pearl[yIndex][xIndex] != null) {
						sb.append(pearl[yIndex][xIndex]);
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
						if (xIndex != getXLength() - 1) {
							sb.append("□");
						}
					}
					sb.append("□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 白真珠は直進のちカーブ、黒真珠はカーブのち直進。
		 * 条件を満たさない場合falseを返す。
		 */
		private boolean pearlSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (pearl[yIndex][xIndex] != null) {
						if (pearl[yIndex][xIndex] == Pearl.SIRO) {
							if (!toStraightCheck(yIndex, xIndex)) {
								return false;
							}
							toStraight(yIndex, xIndex);
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
							if (wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
								boolean canUpCurve = toCurveCheck(yIndex - 1, xIndex);
								boolean canDownCurve = toCurveCheck(yIndex + 1, xIndex);
								if (!canUpCurve && !canDownCurve) {
									return false;
								}
								if (!canUpCurve) {
									toCurve(yIndex + 1, xIndex);
								}
								if (!canDownCurve) {
									toCurve(yIndex - 1, xIndex);
								}
							}
							if (wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
								boolean canRightCurve = toCurveCheck(yIndex, xIndex + 1);
								boolean canLeftCurve = toCurveCheck(yIndex, xIndex - 1);
								if (!canRightCurve && !canLeftCurve) {
									return false;
								}
								if (!canRightCurve) {
									toCurve(yIndex, xIndex - 1);
								}
								if (!canLeftCurve) {
									toCurve(yIndex, xIndex + 1);
								}
							}
						} else if (pearl[yIndex][xIndex] == Pearl.KURO) {
							if (!toCurveCheck(yIndex, xIndex)) {
								return false;
							}
							toCurve(yIndex, xIndex);
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
							if (wallUp == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex - 1, xIndex)) {
									return false;
								}
								toStraight(yIndex - 1, xIndex);
							}
							if (wallRight == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex, xIndex + 1)) {
									return false;
								}
								toStraight(yIndex, xIndex + 1);
							}
							if (wallDown == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex + 1, xIndex)) {
									return false;
								}
								toStraight(yIndex + 1, xIndex);
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex, xIndex - 1)) {
									return false;
								}
								toStraight(yIndex, xIndex - 1);
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 指定した位置のマスが直進可能かチェックする。できない場合falseを返す。
		 */
		private boolean toStraightCheck(int yIndex, int xIndex) {
			if (pearl[yIndex][xIndex] == Pearl.KURO) {
				return false;
			}
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
					|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
					|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallUp == Wall.EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallDown == Wall.EXISTS && wallUp == Wall.NOT_EXISTS)
					|| (wallRight == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallLeft == Wall.EXISTS && wallRight == Wall.NOT_EXISTS)) {
				return false;
			}
			return true;
		}

		/**
		 * 指定した位置のマスを直進させる。必ずチェック処理後に呼ぶこと。
		 */
		private void toStraight(int yIndex, int xIndex) {
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if (wallUp == Wall.EXISTS || wallDown == Wall.EXISTS ||
					wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
				}
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.EXISTS;
				}
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
				}
			}
			if (wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS ||
					wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.EXISTS;
				}
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
				}
			}
		}

		/**
		 * 指定した位置のマスがカーブ可能かチェックする。できない場合falseを返す。
		 */
		private boolean toCurveCheck(int yIndex, int xIndex) {
			if (pearl[yIndex][xIndex] == Pearl.SIRO) {
				return false;
			}
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if ((wallUp == Wall.EXISTS && wallDown == Wall.EXISTS)
					|| (wallRight == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
				return false;
			}
			return true;
		}

		/**
		 * 指定した位置のマスをカーブさせる。できない場合falseを返す。
		 */
		private void toCurve(int yIndex, int xIndex) {
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if (wallUp == Wall.EXISTS) {
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallRight == Wall.EXISTS) {
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
				}
			}
			if (wallDown == Wall.EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallLeft == Wall.EXISTS) {
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallUp == Wall.NOT_EXISTS) {
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.EXISTS;
				}
			}
			if (wallRight == Wall.NOT_EXISTS) {
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
				}
			}
			if (wallDown == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
				}
			}
			if (wallLeft == Wall.NOT_EXISTS) {
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.EXISTS;
				}
			}
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか4マス
						if ((existsCount == 3 && notExistsCount == 1)
								|| notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
						if (wallUp == Wall.SPACE) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は必ず2マス
						if (existsCount > 2 || notExistsCount > 2) {
							return false;
						}
						if (notExistsCount == 2) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						} else if (existsCount == 2) {
							if (wallUp == Wall.SPACE) {
								if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
									return false;
								}
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (wallRight == Wall.SPACE) {
								if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (wallDown == Wall.SPACE) {
								if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (wallLeft == Wall.SPACE) {
								if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
									return false;
								}
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Set<Position> blackCandPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						blackCandPosSet.add(pos);
					}
				}
			}
			blackCandPosSet.removeAll(whitePosSet);
			for (Position pos : blackCandPosSet) {
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!pearlSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
//				if (!paritySolve()) {
//					return false;
//				}
			}
			return true;
		}

		/**
		 * ましゅのルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}
		/**
		 * 盤面を市松模様とみなした場合、奇属性と偶属性の白マスの数は同じになる。
		 */
		private boolean paritySolve() {
			int evenWhite = 0;
			int oddWhite = 0;
			int evenSpace = 0;
			int oddSpace = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if ((yIndex + xIndex) % 2 == 0) {
							evenWhite++;
						} else {
							oddWhite++;
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						if ((yIndex + xIndex) % 2 == 0) {
							evenSpace++;
						} else {
							oddSpace++;
						}
					}
				}
			}
			if (evenWhite + evenSpace < oddWhite) {
				return false;
			}
			if (oddWhite + oddSpace < evenWhite) {
				return false;
			}
			if (evenWhite + evenSpace == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 == 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			if (oddSpace == 0 && evenWhite == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 == 0) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			if (oddWhite + oddSpace == evenWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			if (evenSpace == 0 && evenWhite == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 != 0) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public MasyuSolver(int height, int width, String param, boolean ura) {
		field = new Field(height, width, param, ura);
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
		System.out.println(new MasyuSolver(height, width, param, false).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}