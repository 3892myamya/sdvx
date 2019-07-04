package myamya.other.solver.slalom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

/**
 *
□□□□□□□□□□□□□□□□□□□□□
□■□■□■□■□・　・　・　・　・　・□
□□□□□□□□□　□□□□□□□□□　□
□■□■□■□■□＋□■□■□■□■□・□
□□□□□□□□□　□□□□□□□□□　□
□・　＋　・　・　・□・　・　・□　□・□
□　□□□□□□□□□　□□□　□□□　□
□・□■□■□■□・　・□d3□・□■□・□
□　□□□□□□□　□□□□□　□□□　□
□＋□＋□＋□■□・□・　＋　・□○　・□
□　□□□□□□□　□　□□□□□　□□□
□・□■□■□■□・□・□u3□・　・□■□
□　□□□□□□□　□　□□□　□□□□□
□・□d5□■□■□・□・□d2□＋□■□■□
□　□□□□□□□　□　□□□　□□□□□
□・　＋　・　＋　・□・　＋　・□■□■□
□□□□□□□□□□□□□□□□□□□□□
□■□u5□■□■□■□■□u2□■□■□■□
□□□□□□□□□□□□□□□□□□□□□
□■□■□■□■□■□■□■□■□■□■□
□□□□□□□□□□□□□□□□□□□□□
 */
public class SlalomSolver implements Solver {

	/**
	 * ブロックマス
	 */
	public static class Block {
		private final Direction direction;
		private final int count;

		public Block(Direction direction, int count) {
			this.direction = direction;
			this.count = count;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return count == -1 ? "■" : (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}
	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 旗門の情報
		private final Integer[][] gates;
		// ブロックの情報
		private final Block[][] blocks;
		// スタート地点
		private final Position start;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getGates() {
			return gates;
		}

		public Block[][] getBlocks() {
			return blocks;
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

		public Field(int height, int width, String param, int startPos) {
			masu = new Masu[height][width];
			gates = new Integer[height][width];
			blocks = new Block[height][width];
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
			start = new Position(startPos / getXLength(), startPos % getXLength());
			masu[start.getyIndex()][start.getxIndex()] = Masu.NOT_BLACK;
			int index = 0;
			int nextReadPos = 0;
			List<Position> blockPosList = new ArrayList<>();
			// マスの決定
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				nextReadPos = i + 1;
				Position pos = new Position(index / getXLength(), index % getXLength());
				int val = Character.getNumericValue(ch);
				if (val >= 4) {
					index = index + val - 3;
				} else {
					if (val == 1) {
						// ブロック。黒マス＆周囲の壁が確定
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						blockPosList.add(pos);
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
					} else if (val == 2) {
						//縦向きゲート。縦の壁閉鎖。
						gates[pos.getyIndex()][pos.getxIndex()] = -1;
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (val == 3) {
						//横向きゲート。縦の壁閉鎖。
						gates[pos.getyIndex()][pos.getxIndex()] = -1;
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
					}
					index++;
				}
				if (index >= getYLength() * getXLength()) {
					break;
				}
			}
			// 黒マス決定
			int blockIndex = 0;
			for (int i = nextReadPos; i < param.length(); i++) {
				Position blockPos = blockPosList.get(blockIndex);
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					blockIndex = blockIndex + interval;
				} else {
					Direction direction;
					int count;
					if (ch == '-') {
						direction = Direction.getByNum(param.charAt(i + 1));
						count = Integer.parseInt(
								"" + param.charAt(i + 2) + param.charAt(i + 3) + param.charAt(i + 4),
								16);
						i++;
						i++;
						i++;
						i++;
					} else if (Character.getNumericValue(ch) >= 5) {
						direction = Direction.getByNum(Character.getNumericValue(ch) - 5);
						count = Integer.parseInt(
								"" + param.charAt(i + 1) + param.charAt(i + 2),
								16);
						i++;
						i++;
					} else {
						direction = Direction.getByNum(Character.getNumericValue(ch));
						count = Integer.parseInt("" + param.charAt(i + 1));
						i++;
					}
					blocks[blockPos.getyIndex()][blockPos.getxIndex()] = new Block(direction, count);
				}
				blockIndex++;
			}
			for (Position blockPos : blockPosList) {
				if (blocks[blockPos.getyIndex()][blockPos.getxIndex()] == null) {
					blocks[blockPos.getyIndex()][blockPos.getxIndex()] = new Block(null, -1);
				}
			}
			// ゲートの数字決定
			// TODO
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			blocks = other.blocks;
			gates = other.gates;
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
			start = other.start;
		}

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
					if (blocks[yIndex][xIndex] != null) {
						sb.append(blocks[yIndex][xIndex]);
					} else if (gates[yIndex][xIndex] != null) {
						sb.append("＋");
					} else if (new Position(yIndex, xIndex).equals(start)) {
						sb.append("○");
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
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * 黒マス隣接セルの隣に黒マスがある場合はfalseを返す。
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (xIndex != getXLength() - 1) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (yIndex != getYLength() - 1) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (xIndex != 0) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						// 自分が白マスなら壁は必ず2マス
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
								if (existsCount == 3) {
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
								}
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
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
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinuePosSet(typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
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

	public SlalomSolver(int height, int width, String param, int startPos) {
		field = new Field(height, width, param, startPos);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?slalom/d/10/10/e1413114152c14151414333152c171415131524252714114141em23h1325g22g15h12g/48"; //urlを入れれば試せる
		String[] params = url.split("\\?");
		params = params[params.length - 1].split("/");
		int height = Integer.parseInt(params[3]);
		int width = Integer.parseInt(params[2]);
		String param = params[4];
		int startPos = Integer.parseInt(params[5]);
		System.out.println(new SlalomSolver(height, width, param, startPos).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 2).toString();
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