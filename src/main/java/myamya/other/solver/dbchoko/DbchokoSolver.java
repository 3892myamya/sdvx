package myamya.other.solver.dbchoko;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class DbchokoSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private final Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
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
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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
			// アイスバーンとそうでないマスの判別のため一時的にマスを色分け
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength()); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						masu[(cnt - mod + 0) / (getXLength())][(cnt - mod + 0) % (getXLength())] = bit
								/ 16
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 1) {
						masu[(cnt - mod + 1) / (getXLength())][(cnt - mod + 1) % (getXLength())] = bit
								/ 8
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 2) {
						masu[(cnt - mod + 2) / (getXLength())][(cnt - mod + 2) % (getXLength())] = bit
								/ 4
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 3) {
						masu[(cnt - mod + 3) / (getXLength())][(cnt - mod + 3) % (getXLength())] = bit
								/ 2
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 4) {
						masu[(cnt - mod + 4) / (getXLength())][(cnt - mod + 4) % (getXLength())] = bit
								/ 1
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = other.masu;
			numbers = other.numbers;
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

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

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
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
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
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!shapeSolve()) {
				return false;
			}
			if (!pileSolve()) {
				return false;
			}
			if (!standAloneSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 周囲に違う色のマスがないマスが孤立してはならない。
		 */
		private boolean standAloneSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// TODO これいらないかもしれないなー
				}
			}
			return true;
		}

		/**
		 * 数字は、自分と同じ色のマスが何マス連続しているかを示している。
		 * 連続できない場合falseを返す。
		 * 連続方法が1通りしかない場合、その間の壁なしが確定する。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!checkAndSetContinuePosSet(masu[yIndex][xIndex], pivot, continueNotBlackPosSet, null,
								numbers[yIndex][xIndex])) {
							if (continueNotBlackPosSet.size() == numbers[yIndex][xIndex]) {
								for (Position pos : continueNotBlackPosSet) {
									if (numbers[yIndex][xIndex] == continueNotBlackPosSet.size()) {
										if (pos.getyIndex() != 0) {
											if (continueNotBlackPosSet
													.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
												tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
											}
										}
										if (pos.getxIndex() != getXLength() - 1) {
											if (continueNotBlackPosSet
													.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
												yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
											}
										}
										if (pos.getyIndex() != getYLength() - 1) {
											if (continueNotBlackPosSet
													.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
												tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
											}
										}
										if (pos.getxIndex() != 0) {
											if (continueNotBlackPosSet
													.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
												yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
											}
										}
									}
								}
								continue;
							} else {
								// サイズ不足
								return false;
							}
						}
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!checkAndSetContinueWhitePosSet(masu[yIndex][xIndex], pivot, continueWhitePosSet, null,
								numbers[yIndex][xIndex])) {
							// サイズ超過
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各ブロックは必ず違う色のブロックと同じ面積・同じ形状で隣接する。
		 * 隣接できない形状である場合はfalseを返す。
		 */
		private boolean shapeSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pivot = new Position(yIndex, xIndex);
					// 壁なし確定マスをつなぐ
					Set<Position> continueWhitePosSet = new HashSet<>();
					continueWhitePosSet.add(pivot);
					checkAndSetContinueWhitePosSet(masu[yIndex][xIndex], pivot, continueWhitePosSet, null, 999);

					// 隣接する別の色のマスを調査する

					// 別の色のマスを
				}
			}
			return true;
		}

		/**
		 * 2つの位置集合が同じ形かどうかを判定する。
		 */
		private boolean isSameShape(Set<Position> posSet, Set<Position> anotherPosSet) {
			if (posSet.size() != anotherPosSet.size()) {
				return false;
			}
			return true;
		}

		/**
		 * 柱から伸びる壁は1枚にならない。
		 * 違反する場合はfalseを返す。
		 */
		private boolean pileSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
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
					// 壁枚数は1以外
					if (exists == 1 && notExists == 3) {
						return false;
					} else if (notExists == 3) {
						// 壁枚数0確定
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
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に自分と同じ色かつ壁で区切られていないマスをつなぎ、
		 * サイズ不足にならないと分かった時点でtrueを返す。
		 */
		private boolean checkAndSetContinuePosSet(Masu color, Position pos, Set<Position> continuePosSet,
				Direction from, int size) {
			if (continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color &&
						tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)
						&& !(numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
								&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(color, nextPos, continuePosSet, Direction.DOWN, size)) {
						return true;
					}

				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)
						&& !(numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
								&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(color, nextPos, continuePosSet, Direction.LEFT, size)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)
						&& !(numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
								&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(color, nextPos, continuePosSet, Direction.UP, size)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)
						&& !(numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
								&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(color, nextPos, continuePosSet, Direction.RIGHT, size)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に自分と同じ色かつ壁なし確定マスをつなぎ、
		 * サイズ超過になると分かった時点でfalseを返す。
		 */
		private boolean checkAndSetContinueWhitePosSet(Masu color, Position pos, Set<Position> continuePosSet,
				Direction from, int size) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(color, nextPos, continuePosSet, Direction.DOWN, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(color, nextPos, continuePosSet, Direction.LEFT, size)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(color, nextPos, continuePosSet, Direction.UP, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == color
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(color, nextPos, continuePosSet, Direction.RIGHT, size)) {
						return false;
					}
				}
			}
			return true;
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

	public DbchokoSolver(int height, int width, String param) {
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
		System.out.println(new DbchokoSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		System.out.println(field);
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