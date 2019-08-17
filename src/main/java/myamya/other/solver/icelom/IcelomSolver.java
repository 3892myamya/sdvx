package myamya.other.solver.icelom;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class IcelomSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;
		// アイスバーン全体
		private final Set<Position> icebarnPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public Wall[][] getYokoExtraWall() {
			return yokoExtraWall;
		}

		public Wall[][] getTateExtraWall() {
			return tateExtraWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, int start, int goal) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (xIndex == 0 || xIndex == getXLength()) {
						yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (yIndex == 0 || yIndex == getYLength()) {
						tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			icebarnPosSet = new HashSet<>();
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
						Position pos = new Position((cnt - mod + 0) / (getXLength()), (cnt - mod + 0) % (getXLength()));
						if (bit / 16 % 2 == 1) {
							icebarnPosSet.add(pos);
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
					if (mod >= 1) {
						Position pos = new Position((cnt - mod + 1) / (getXLength()), (cnt - mod + 1) % (getXLength()));
						if (bit / 8 % 2 == 1) {
							icebarnPosSet.add(pos);
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
					if (mod >= 2) {
						Position pos = new Position((cnt - mod + 2) / (getXLength()), (cnt - mod + 2) % (getXLength()));
						if (bit / 4 % 2 == 1) {
							icebarnPosSet.add(pos);
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
					if (mod >= 3) {
						Position pos = new Position((cnt - mod + 3) / (getXLength()), (cnt - mod + 3) % (getXLength()));
						if (bit / 2 % 2 == 1) {
							icebarnPosSet.add(pos);
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
					if (mod >= 4) {
						Position pos = new Position((cnt - mod + 4) / (getXLength()), (cnt - mod + 4) % (getXLength()));
						if (bit / 1 % 2 == 1) {
							icebarnPosSet.add(pos);
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				}
			}
			int index = 0;
			int maxNum = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
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
						if (maxNum < capacity) {
							maxNum = capacity;
						}
					}
					index++;
				}
			}
			// 入口と出口
			if (start < width) {
				tateExtraWall[0][start] = Wall.NOT_EXISTS;
				if (numbers[0][start] == null) {
					numbers[0][start] = 0;
				}
			} else if (start < width * 2) {
				tateExtraWall[getYLength()][start - width] = Wall.NOT_EXISTS;
				if (numbers[getYLength() - 1][start - width] == null) {
					numbers[getYLength() - 1][start - width] = 0;
				}
			} else if (start < width * 2 + height) {
				yokoExtraWall[start - width - width][0] = Wall.NOT_EXISTS;
				if (numbers[start - width - width][0] == null) {
					numbers[start - width - width][0] = 0;
				}
			} else {
				yokoExtraWall[start - width - width - height][getXLength()] = Wall.NOT_EXISTS;
				if (numbers[start - width - width - height][getXLength() - 1] == null) {
					numbers[start - width - width - height][getXLength() - 1] = 0;
				}
			}

			if (goal < width) {
				tateExtraWall[0][goal] = Wall.NOT_EXISTS;
				if (numbers[0][goal] == null) {
					numbers[0][goal] = maxNum + 1;
				}
			} else if (goal < width * 2) {
				tateExtraWall[getYLength()][goal - width] = Wall.NOT_EXISTS;
				if (numbers[getYLength() - 1][goal - width] == null) {
					numbers[getYLength() - 1][goal - width] = maxNum + 1;
				}
			} else if (goal < width * 2 + height) {
				yokoExtraWall[goal - width - width][0] = Wall.NOT_EXISTS;
				if (numbers[goal - width - width][0] == null) {
					numbers[goal - width - width][0] = maxNum + 1;
				}
			} else {
				yokoExtraWall[goal - width - width - height][getXLength()] = Wall.NOT_EXISTS;
				if (numbers[goal - width - width - height][getXLength() - 1] == null) {
					numbers[goal - width - width - height][getXLength() - 1] = maxNum + 1;
				}
			}

		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
			icebarnPosSet = other.icebarnPosSet;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						sb.append(yokoExtraWall[yIndex][xIndex]);
						if (xIndex != getXLength()) {
							if (icebarnPosSet.contains(new Position(yIndex, xIndex))) {
								sb.append("○");
							} else if (numbers[yIndex][xIndex] != null) {
								String numStr = String.valueOf(numbers[yIndex][xIndex]);
								int index = HALF_NUMS.indexOf(numStr);
								if (index >= 0) {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(numStr);
								}
							} else {
								sb.append(masu[yIndex][xIndex]);
							}
						}
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
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
				return solveAndCheck();
			} else {
				if (!flowSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 流れの通りにたどっていったとき、流れに逆から侵入したらfalseを返す。
		 * また、流れに逆らってたどって行ったとき、流れの通りに侵入したらfalseを返す。
		 */
		private boolean flowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position pos = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (!checkAllFlow(pos, continuePosSet, null, numbers[yIndex][xIndex], 0)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 流れをチェックする。0は流れ不確定、1は正方向、-1は逆方向。
		 * myNunberは今自分が持っている数字。
		 */
		private boolean checkAllFlow(Position pos, Set<Position> continuePosSet, Direction from, Integer myNunber,
				int flow) {
			boolean isIceBarn = icebarnPosSet.contains(pos);
			if ((isIceBarn && from == Direction.DOWN) ||
					(!isIceBarn && from != Direction.UP
							&& tateExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (nextPos.getyIndex() == -1) {
					return true;
				}
				int nextFlow = flow;
				Integer nextMyNumber = myNunber;
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (myNunber != null) {
						if (myNunber + 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == -1) {
								return false;
							} else {
								nextFlow = 1;
							}
						} else if (myNunber - 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == 1) {
								return false;
							} else {
								nextFlow = -1;
							}
						} else {
							return false;
						}
					}
					nextMyNumber = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.DOWN, nextMyNumber, nextFlow);

			}
			if ((isIceBarn && from == Direction.LEFT) || (!isIceBarn && from != Direction.RIGHT
					&& yokoExtraWall[pos.getyIndex()][pos.getxIndex() + 1] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (nextPos.getxIndex() == getXLength()) {
					return true;
				}
				int nextFlow = flow;
				Integer nextMyNumber = myNunber;
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (myNunber != null) {
						if (myNunber + 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == -1) {
								return false;
							} else {
								nextFlow = 1;
							}
						} else if (myNunber - 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == 1) {
								return false;
							} else {
								nextFlow = -1;
							}
						} else {
							return false;
						}
					}
					nextMyNumber = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.LEFT, nextMyNumber, nextFlow);

			}
			if ((isIceBarn && from == Direction.UP) || (!isIceBarn && from != Direction.DOWN
					&& tateExtraWall[pos.getyIndex() + 1][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (nextPos.getyIndex() == getYLength()) {
					return true;
				}
				int nextFlow = flow;
				Integer nextMyNumber = myNunber;
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (myNunber != null) {
						if (myNunber + 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == -1) {
								return false;
							} else {
								nextFlow = 1;
							}
						} else if (myNunber - 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == 1) {
								return false;
							} else {
								nextFlow = -1;
							}
						} else {
							return false;
						}
					}
					nextMyNumber = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.UP, nextMyNumber, nextFlow);

			}
			if ((isIceBarn && from == Direction.RIGHT) || (!isIceBarn && from != Direction.LEFT
					&& yokoExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (nextPos.getxIndex() == -1) {
					return true;
				}

				int nextFlow = flow;
				Integer nextMyNumber = myNunber;
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (myNunber != null) {
						if (myNunber + 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == -1) {
								return false;
							} else {
								nextFlow = 1;
							}
						} else if (myNunber - 1 == numbers[nextPos.getyIndex()][nextPos.getxIndex()]) {
							if (nextFlow == 1) {
								return false;
							} else {
								nextFlow = -1;
							}
						} else {
							return false;
						}
					}
					nextMyNumber = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.RIGHT, nextMyNumber, nextFlow);

			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋める。
		 * また、白マス隣接セルの周辺の壁の数は、アイスバーンなら0または2(直進)、
		 * それ以外なら2になるので、矛盾する場合はfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = tateExtraWall[yIndex][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = yokoExtraWall[yIndex][xIndex + 1];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = tateExtraWall[yIndex + 1][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = yokoExtraWall[yIndex][xIndex];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (icebarnPosSet.contains(new Position(yIndex, xIndex))) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 氷の上の場合、自分が不確定マスなら壁は0マスか2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| (existsCount == 1 && notExistsCount == 3)) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 氷の上の場合、自分が白マスなら壁は0マスか2マス
							if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
								return false;
							}
							// カーブ禁止
							if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
									|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
									|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
									|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)
									|| (wallUp == Wall.EXISTS && wallDown == Wall.NOT_EXISTS)
									|| (wallDown == Wall.EXISTS && wallUp == Wall.NOT_EXISTS)
									|| (wallRight == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS)
									|| (wallLeft == Wall.EXISTS && wallRight == Wall.NOT_EXISTS)) {
								return false;
							}
							if (existsCount == 1 && notExistsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (existsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 3) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallUp == Wall.EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.NOT_EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.NOT_EXISTS) {
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
						}
					} else {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 氷の上でない場合、自分が不確定マスなら壁は2マスか4マス
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
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 氷の上でない場合、自分が白マスなら壁は必ず2マス
							if (existsCount > 2 || notExistsCount > 2) {
								return false;
							}
							if (existsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 周囲の壁を閉鎖
						if (tateExtraWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						if (yokoExtraWall[yIndex][xIndex + 1] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
						if (tateExtraWall[yIndex + 1][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
						if (yokoExtraWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
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
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public IcelomSolver(int height, int width, String param, int start, int goal) {
		field = new Field(height, width, param, start, goal);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 4]);
		int width = Integer.parseInt(params[params.length - 5]);
		String param = params[params.length - 3];
		int start = Integer.parseInt(params[params.length - 2]);
		int goal = Integer.parseInt(params[params.length - 1]);
		System.out.println(new IcelomSolver(height, width, param, start, goal).solve());
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
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + count * 2);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
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
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

}