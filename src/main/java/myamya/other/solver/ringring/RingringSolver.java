package myamya.other.solver.ringring;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class RingringSolver implements Solver {
	public static class Field {
		static final String ALPHABET_FROM_H = "hijklmnopqrstuvwxyz";
		// マスの情報
		private final Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
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
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
				int interval = Character.getNumericValue(ch);
				if (interval == -1) {
					index = index + 36;
					continue;
				}
				index = index + interval;
				Position pos = new Position(index / getXLength(), index % getXLength());
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				if (pos.getyIndex() != 0) {
					tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
				}
				if (pos.getxIndex() != getXLength() - 1) {
					yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
				}
				if (pos.getyIndex() != getYLength() - 1) {
					tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
				}
				if (pos.getxIndex() != 0) {
					yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = other.masu;
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
					sb.append(masu[yIndex][xIndex]);
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
		 * 白マスの壁の数は0か2になる。違反の場合false。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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

						if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
							return false;
						}
						if (existsCount == 2) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
						} else if (existsCount == 1 && notExistsCount == 2) {
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
						} else if (notExistsCount == 3) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが四角になっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 曲がり角確定のマスから開始
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						Direction from;
						Direction canCurveDirection;
						if (wallUp == Wall.EXISTS && wallRight == Wall.EXISTS) {
							// 左へ進んで下に曲がる
							from = Direction.RIGHT;
							canCurveDirection = Direction.DOWN;
						} else if (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS) {
							// 右へ進んで下に曲がる
							from = Direction.LEFT;
							canCurveDirection = Direction.DOWN;
						} else if (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS) {
							// 左へ進んで上に曲がる
							from = Direction.RIGHT;
							canCurveDirection = Direction.UP;
						} else if (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS) {
							// 右へ進んで上に曲がる
							from = Direction.LEFT;
							canCurveDirection = Direction.UP;
						} else {
							continue;
						}
						Position originPos = new Position(yIndex, xIndex);
						if (!setContinuePosSet(originPos, originPos, from, canCurveDirection, 3)) {
							return false;
						}
					}
				}
			}
			return true;

		}

		/**
		 * posを起点に壁なし確定のマスを、直線を優先してつないでいく。
		 * 四角にならないことが確定した時点でfalseを返す。
		 * エラーとならずに出発マスまで戻ってくるか、途中で進む方向が確定できなくなったらtrueを返す。
		 * TODO もっと効率化できそう。
		 */
		private boolean setContinuePosSet(Position originPos, Position pos,
				Direction from, Direction canCurveDirection, int canCurveCnt) {
			if (canCurveCnt <= 1) {
				// 2回カーブした時点でループの各辺の長さはきまる
				if (from == Direction.DOWN) {
					// 直進して上へ
					while (pos.getyIndex() != originPos.getyIndex()) {
						pos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.LEFT) {
					// 直進して右へ
					while (pos.getxIndex() != originPos.getxIndex()) {
						pos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.UP) {
					// 直進して下へ
					while (pos.getyIndex() != originPos.getyIndex()) {
						pos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.RIGHT) {
					// 直進して左へ
					while (pos.getxIndex() != originPos.getxIndex()) {
						pos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (originPos.equals(pos)) {
							return true;
						}
					}
				}
			} else {
				if (from == Direction.DOWN) {
					// 直進して上へ
					while (pos.getyIndex() != 0 && tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
						pos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.LEFT) {
					// 直進して右へ
					while (pos.getxIndex() != getXLength() - 1
							&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						pos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.UP) {
					while (pos.getyIndex() != getYLength() - 1
							&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// 直進して下へ
						pos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						if (originPos.equals(pos)) {
							return true;
						}
					}
				} else if (from == Direction.RIGHT) {
					// 直進して左へ
					while (pos.getxIndex() != 0 && yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
						pos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (originPos.equals(pos)) {
							return true;
						}
					}
				}
				// 直進可能性判定
				// 直進できる可能性が残っているときはtrueにする
				if (pos.getyIndex() != 0 && from == Direction.DOWN
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
						&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS) {
					return true;
				}
			}
			// 直進不可
			if (pos.getyIndex() != 0 && from != Direction.UP
					&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// カーブして上へ
				if (canCurveCnt == 0 || Direction.UP != canCurveDirection) {
					return false;
				}
				canCurveDirection = from;
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				return setContinuePosSet(originPos, nextPos, Direction.DOWN, canCurveDirection,
						canCurveCnt - 1);
			} else if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// カーブして右へ
				if (canCurveCnt == 0 || Direction.RIGHT != canCurveDirection) {
					return false;
				}
				canCurveDirection = from;
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				return setContinuePosSet(originPos, nextPos, Direction.LEFT, canCurveDirection,
						canCurveCnt - 1);
			} else if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
					&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// カーブして下へ
				if (canCurveCnt == 0 || Direction.DOWN != canCurveDirection) {
					return false;
				}
				canCurveDirection = from;
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				return setContinuePosSet(originPos, nextPos, Direction.UP, canCurveDirection,
						canCurveCnt - 1);
			} else if (pos.getxIndex() != 0 && from != Direction.LEFT
					&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
				// カーブして左へ
				if (canCurveCnt == 0 || Direction.LEFT != canCurveDirection) {
					return false;
				}
				canCurveDirection = from;
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				return setContinuePosSet(originPos, nextPos, Direction.RIGHT, canCurveDirection,
						canCurveCnt - 1);
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				return solveAndCheck();
			} else {
				return true;
			}
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

	public RingringSolver(int height, int width, String param) {
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
		System.out.println(new RingringSolver(height, width, param).solve());
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
		//		String str = field.getStateDump();
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
		//		if (!field.getStateDump().equals(str)) {
		//			return candSolve(field, recursive);
		//		}
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
