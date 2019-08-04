package myamya.other.solver.nondango;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NondangoSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 1;
		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
								% 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						rooms.add(continuePosSet);
					}
				}
			}
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * getXLength()) - 1) {
					if (mod >= 0) {
						masu[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1 ? null
								: Masu.SPACE;
					}
					if (mod >= 1) {
						masu[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1 ? null
								: Masu.SPACE;
					}
					if (mod >= 2) {
						masu[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1 ? null
								: Masu.SPACE;
					}
					if (mod >= 3) {
						masu[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1 ? null
								: Masu.SPACE;
					}
					if (mod >= 4) {
						masu[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1 ? null
								: Masu.SPACE;
					}
				}
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
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
					sb.append(masu[yIndex][xIndex] == null ? "　"
							: masu[yIndex][xIndex] == Masu.BLACK ? "●"
									: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "○" : "◎");
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "　");
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
			return sb.toString();
		}

		/**
		 * 部屋の黒マスは1個のみ。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < BLACK_CNT) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = BLACK_CNT - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 同じ色のマスが縦横斜めに3個以上並んではダメ。違反の場合falseを返す。
		 */
		private boolean dangoSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != null) {
						Masu masuUL1 = yIndex <= 0 || xIndex <= 0 ? null : masu[yIndex - 1][xIndex - 1];
						Masu masuUL2 = yIndex <= 1 || xIndex <= 1 ? null : masu[yIndex - 2][xIndex - 2];

						Masu masuU1 = yIndex <= 0 ? null : masu[yIndex - 1][xIndex];
						Masu masuU2 = yIndex <= 1 ? null : masu[yIndex - 2][xIndex];

						Masu masuUR1 = yIndex <= 0 || xIndex >= getXLength() - 1 ? null : masu[yIndex - 1][xIndex + 1];
						Masu masuUR2 = yIndex <= 1 || xIndex >= getXLength() - 2 ? null : masu[yIndex - 2][xIndex + 2];

						Masu masuR1 = xIndex >= getXLength() - 1 ? null : masu[yIndex][xIndex + 1];
						Masu masuR2 = xIndex >= getXLength() - 2 ? null : masu[yIndex][xIndex + 2];

						Masu masuDR1 = yIndex >= getYLength() - 1 || xIndex >= getXLength() - 1 ? null
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDR2 = yIndex >= getYLength() - 2 || xIndex >= getXLength() - 2 ? null
								: masu[yIndex + 2][xIndex + 2];

						Masu masuD1 = yIndex >= getYLength() - 1 ? null : masu[yIndex + 1][xIndex];
						Masu masuD2 = yIndex >= getYLength() - 2 ? null : masu[yIndex + 2][xIndex];

						Masu masuDL1 = yIndex >= getYLength() - 1 || xIndex <= 0 ? null : masu[yIndex + 1][xIndex - 1];
						Masu masuDL2 = yIndex >= getYLength() - 2 || xIndex <= 1 ? null : masu[yIndex + 2][xIndex - 2];

						Masu masuL1 = xIndex <= 0 ? null : masu[yIndex][xIndex - 1];
						Masu masuL2 = xIndex <= 1 ? null : masu[yIndex][xIndex - 2];

						if (masuUL2 == Masu.BLACK && masuUL1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUL2 == Masu.NOT_BLACK && masuUL1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDR1 == Masu.BLACK && masuUL1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuDR1 == Masu.NOT_BLACK && masuUL1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDR1 == Masu.BLACK && masuDR2 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuDR1 == Masu.NOT_BLACK && masuDR2 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}

						if (masuU2 == Masu.BLACK && masuU1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuU2 == Masu.NOT_BLACK && masuU1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuD1 == Masu.BLACK && masuU1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuD1 == Masu.NOT_BLACK && masuU1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuD1 == Masu.BLACK && masuD2 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuD1 == Masu.NOT_BLACK && masuD2 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}

						if (masuUR2 == Masu.BLACK && masuUR1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR2 == Masu.NOT_BLACK && masuUR1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDL1 == Masu.BLACK && masuUR1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuDL1 == Masu.NOT_BLACK && masuUR1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDL1 == Masu.BLACK && masuDL2 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuDL1 == Masu.NOT_BLACK && masuDL2 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}

						if (masuR2 == Masu.BLACK && masuR1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuR2 == Masu.NOT_BLACK && masuR1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuL1 == Masu.BLACK && masuR1 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuL1 == Masu.NOT_BLACK && masuR1 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuL1 == Masu.BLACK && masuL2 == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuL1 == Masu.NOT_BLACK && masuL2 == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!dangoSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public NondangoSolver(int height, int width, String param) {
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
		System.out.println(new NondangoSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 10).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
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