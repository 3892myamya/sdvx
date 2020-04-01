package myamya.other.solver.shwolf;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class ShwolfSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private final Masu[][] masu;
		// 壁の交点の柱の存在
		private final boolean[][] piles;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getPiles() {
			return piles;
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

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			piles = new boolean[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					piles[yIndex][xIndex] = false;
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
			int readPos = 0;
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int interval;
				if (ch == '.') {
					interval = 36;
					index = index + interval;
					continue;
				} else {
					interval = Character.getNumericValue(ch);
				}
				index = index + interval;
				if (index > (getYLength() - 1) * (getXLength() - 1)) {
					break;
				}
				Position pos = new Position(index / (getXLength() - 1), index % (getXLength() - 1));
				piles[pos.getyIndex()][pos.getxIndex()] = true;
				index++;
			}
			index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = pos1 == 0 ? Masu.SPACE
							: pos1 == 1 ? Masu.NOT_BLACK : Masu.BLACK;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = pos2 == 0 ? Masu.SPACE
							: pos2 == 1 ? Masu.NOT_BLACK : Masu.BLACK;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = pos3 == 0 ? Masu.SPACE
							: pos3 == 1 ? Masu.NOT_BLACK : Masu.BLACK;
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = other.masu;
			piles = other.piles;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
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
					sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "●"
							: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "○" : "　");
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
							sb.append(piles[yIndex][xIndex] ? "■" : "□");
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
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			//			String str = getStateDump();
			if (!pileSolve()) {
				return false;
			}
			if (!roomSolve()) {
				return false;
			}
			if (!insidePileSolve()) {
				return false;
			}
			//			if (!getStateDump().equals(str)) {
			//				return solveAndCheck();
			//			}
			return true;
		}

		/**
		 * 柱から伸びる壁がある場合、それが外につながらないのはダメ。
		 */
		private boolean insidePileSolve() {
			Set<Position> alreadyServeyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (piles[yIndex][xIndex]) {
						Wall wall1 = tateWall[yIndex][xIndex];
						Wall wall2 = tateWall[yIndex][xIndex + 1];
						Wall wall3 = yokoWall[yIndex][xIndex];
						Wall wall4 = yokoWall[yIndex + 1][xIndex];
						if (wall1 == Wall.EXISTS || wall2 == Wall.EXISTS || wall3 == Wall.EXISTS
								|| wall4 == Wall.EXISTS) {
							Position pos = new Position(yIndex, xIndex);
							if (!alreadyServeyPosSet.contains(pos)) {
								HashSet<Position> continuePosSet = new HashSet<>();
								continuePosSet.add(pos);
								if (!checkInsidePile(pos, continuePosSet, null)) {
									return false;
								} else {
									alreadyServeyPosSet.addAll(continuePosSet);
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁あり確定の連結部を直進優先でつなぐ。外枠に接する壁が見つかった時点でtrueを返す。
		 */
		private boolean checkInsidePile(Position pos, HashSet<Position> continuePosSet, Direction from) {
			// 直進不可
			if (from != Direction.UP && (from == Direction.DOWN || piles[pos.getyIndex()][pos.getxIndex()])
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.NOT_EXISTS) {
				if (pos.getyIndex() == 0) {
					return true;
				}
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkInsidePile(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (from != Direction.RIGHT && (from == Direction.LEFT || piles[pos.getyIndex()][pos.getxIndex()])
					&& tateWall[pos.getyIndex()][pos.getxIndex() + 1] != Wall.NOT_EXISTS) {
				if (pos.getxIndex() == getXLength() - 2) {
					return true;
				}
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkInsidePile(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (from != Direction.DOWN && (from == Direction.UP || piles[pos.getyIndex()][pos.getxIndex()])
					&& yokoWall[pos.getyIndex() + 1][pos.getxIndex()] != Wall.NOT_EXISTS) {
				if (pos.getyIndex() == getYLength() - 2) {
					return true;
				}
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkInsidePile(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (from != Direction.LEFT && (from == Direction.RIGHT || piles[pos.getyIndex()][pos.getxIndex()])
					&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.NOT_EXISTS) {
				if (pos.getxIndex() == 0) {
					return true;
				}
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkInsidePile(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 柱ありから伸びる壁は0枚か2枚、
		 * 柱なしから伸びる壁は0枚か2枚(直進)か4枚になる。
		 * 違反する場合はfalseを返す。
		 */
		private boolean pileSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					boolean pileExists = piles[yIndex][xIndex];
					int exists = 0;
					int notExists = 0;
					Wall wall1 = tateWall[yIndex][xIndex];
					Wall wall2 = tateWall[yIndex][xIndex + 1];
					Wall wall3 = yokoWall[yIndex][xIndex];
					Wall wall4 = yokoWall[yIndex + 1][xIndex];
					if (wall1 == Wall.EXISTS) {
						exists++;
					} else if (wall1 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall2 == Wall.EXISTS) {
						exists++;
					} else if (wall2 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall3 == Wall.EXISTS) {
						exists++;
					} else if (wall3 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall4 == Wall.EXISTS) {
						exists++;
					} else if (wall4 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (pileExists) {
						// 壁枚数は0か2
						if (exists > 2 || (exists == 1 && notExists == 3)) {
							return false;
						} else if (notExists == 3) {
							// 壁枚数0確定
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
						} else if (notExists == 2 && exists == 1) {
							// 壁枚数2確定
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
						} else if (notExists == 1 && exists == 2) {
							// 壁枚数2確定
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
						}
					} else {
						// 壁枚数は0か2(直進)か4
						if ((exists == 1 && notExists == 3) || (exists == 3 && notExists == 1)) {
							return false;
						} else if (notExists == 3) {
							// 壁枚数0確定
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
						} else if (exists == 3) {
							// 壁枚数4確定
							tateWall[yIndex][xIndex] = Wall.EXISTS;
							tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
							yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
						} else {
							// 直進できるところを直進
							if (wall1 == Wall.EXISTS) {
								if (wall2 == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wall2 == Wall.EXISTS) {
								if (wall1 == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall3 == Wall.EXISTS) {
								if (wall4 == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
							if (wall4 == Wall.EXISTS) {
								if (wall3 == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall1 == Wall.NOT_EXISTS) {
								if (wall2 == Wall.EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wall2 == Wall.NOT_EXISTS) {
								if (wall1 == Wall.EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall3 == Wall.NOT_EXISTS) {
								if (wall4 == Wall.EXISTS) {
									return false;
								}
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall4 == Wall.NOT_EXISTS) {
								if (wall3 == Wall.EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		static class BooleanWrapper {
			private boolean value;

			public BooleanWrapper(boolean value) {
				this.value = value;
			}

			public boolean isValue() {
				return value;
			}

			public void setValue(boolean value) {
				this.value = value;
			}
		}

		/**
		 * 同じ部屋にヤギとオオカミが同居したり、誰もいない部屋があってはならない。
		 * 違反した場合falseを返す。
		 */
		private boolean roomSolve() {
			Set<Position> alreadyServeyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!alreadyServeyPosSet.contains(pos)) {
						HashSet<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						BooleanWrapper whiteExists = new BooleanWrapper(masu[yIndex][xIndex] == Masu.NOT_BLACK);
						BooleanWrapper blackExists = new BooleanWrapper(masu[yIndex][xIndex] == Masu.BLACK);
						if (!setContinuePosSet(pos, continuePosSet, null, whiteExists, blackExists)) {
							return false;
						} else {
							alreadyServeyPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			alreadyServeyPosSet.clear();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!alreadyServeyPosSet.contains(pos)) {
						HashSet<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (masu[yIndex][xIndex] == Masu.SPACE && !setContinueCandPosSet(pos, continuePosSet, null)) {
							return false;
						} else {
							alreadyServeyPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁なし確定のマスをつなぐ。ヤギとオオカミの同居が確定した時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				BooleanWrapper whiteExists, BooleanWrapper blackExists) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					whiteExists.setValue(
							whiteExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK);
					blackExists.setValue(
							blackExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK);
					if (whiteExists.isValue() && blackExists.isValue()) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, whiteExists, blackExists)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					whiteExists.setValue(
							whiteExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK);
					blackExists.setValue(
							blackExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK);
					if (whiteExists.isValue() && blackExists.isValue()) {
						return false;
					}

					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, whiteExists, blackExists)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					whiteExists.setValue(
							whiteExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK);
					blackExists.setValue(
							blackExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK);
					if (whiteExists.isValue() && blackExists.isValue()) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP, whiteExists, blackExists)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					whiteExists.setValue(
							whiteExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK);
					blackExists.setValue(
							blackExists.isValue() || masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK);
					if (whiteExists.isValue() && blackExists.isValue()) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, whiteExists, blackExists)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁ありでないマスをつなぐ。ヤギとオオカミがどっちかいればtrueを返す
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.SPACE) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.SPACE) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.SPACE) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.SPACE) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
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

	public ShwolfSolver(int height, int width, String param) {
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
		System.out.println(new ShwolfSolver(height, width, param).solve());
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
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 3));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 3).toString();
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
