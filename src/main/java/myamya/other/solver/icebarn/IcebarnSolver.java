package myamya.other.solver.icebarn;

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

public class IcebarnSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private final Direction[][] yokoExtraWallDirection;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private final Direction[][] tateExtraWallDirection;
		// 個別のアイスバーン
		private final List<Set<Position>> icebarns;
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
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
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
			// 黒マスでつながってるところをアイスバーンにする
			icebarns = new ArrayList<>();
			icebarnPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pos = new Position(yIndex, xIndex);
						boolean alreadyRoomed = false;
						for (Set<Position> posSet : icebarns) {
							if (posSet.contains(pos)) {
								alreadyRoomed = true;
								break;
							}
						}
						if (!alreadyRoomed) {
							Set<Position> continuePosSet = new HashSet<>();
							continuePosSet.add(pos);
							setContinuePosSet(pos, continuePosSet);
							icebarns.add(continuePosSet);
							icebarnPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			// マスをもとに戻す
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			yokoExtraWallDirection = new Direction[height][width + 1];
			tateExtraWallDirection = new Direction[height + 1][width];
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				index = index + Character.getNumericValue(ch);
				if (ch == 'z') {
					continue;
				}
				if (index < (height * (width - 1))) {
					// 左向き
					int wkIndex = index;
					yokoExtraWallDirection[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Direction.LEFT;
					yokoExtraWall[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Wall.NOT_EXISTS;
				} else if (index < (height * (width - 1)) + ((height - 1) * width)) {
					// 上向き
					int wkIndex = index - (height * (width - 1));
					tateExtraWallDirection[wkIndex / width + 1][wkIndex % width] = Direction.UP;
					tateExtraWall[wkIndex / width + 1][wkIndex % width] = Wall.NOT_EXISTS;
				} else if (index == (height * (width - 1)) + ((height - 1) * width)) {
					continue;
				} else if (index < (height * (width - 1)) + ((height - 1) * width) + (height * (width - 1))) {
					// 右向き
					int wkIndex = index - (height * (width - 1)) - ((height - 1) * width);
					yokoExtraWallDirection[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Direction.RIGHT;
					yokoExtraWall[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Wall.NOT_EXISTS;
				} else if (index < (height * (width - 1)) + ((height - 1) * width) + (height * (width - 1))
						+ ((height - 1) * width)) {
					// 下向き
					int wkIndex = index - (height * (width - 1)) - ((height - 1) * width) - (height * (width - 1));
					tateExtraWallDirection[wkIndex / width + 1][wkIndex % width] = Direction.DOWN;
					tateExtraWall[wkIndex / width + 1][wkIndex % width] = Wall.NOT_EXISTS;
				}
				index++;
			}
			// 入口と出口
			if (start < width) {
				tateExtraWallDirection[0][start] = Direction.DOWN;
				tateExtraWall[0][start] = Wall.NOT_EXISTS;
			} else if (start < width * 2) {
				tateExtraWallDirection[getYLength()][start - width] = Direction.UP;
				tateExtraWall[getYLength()][start - width] = Wall.NOT_EXISTS;
			} else if (start < width * 2 + height) {
				yokoExtraWallDirection[goal - width - height][0] = Direction.RIGHT;
				yokoExtraWall[start - width - height][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[goal - width - height - width][getXLength()] = Direction.LEFT;
				yokoExtraWall[start - width - height - width][getXLength()] = Wall.NOT_EXISTS;
			}

			if (goal < width) {
				tateExtraWallDirection[0][goal] = Direction.UP;
				tateExtraWall[0][goal] = Wall.NOT_EXISTS;
			} else if (goal < width * 2) {
				tateExtraWallDirection[getYLength()][goal - width] = Direction.DOWN;
				tateExtraWall[getYLength()][goal - width] = Wall.NOT_EXISTS;
			} else if (goal < width * 2 + height) {
				yokoExtraWallDirection[goal - width - height][0] = Direction.LEFT;
				yokoExtraWall[goal - width - height][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[goal - width - height - width][getXLength()] = Direction.RIGHT;
				yokoExtraWall[goal - width - height - width][getXLength()] = Wall.NOT_EXISTS;
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
			icebarns = other.icebarns;
			icebarnPosSet = other.icebarnPosSet;
			yokoExtraWallDirection = other.yokoExtraWallDirection;
			tateExtraWallDirection = other.tateExtraWallDirection;
		}

		// posを起点に上下左右に黒マスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWallDirection[yIndex][xIndex] != null) {
						sb.append(tateExtraWallDirection[yIndex][xIndex].getDirectString());
					} else {
						sb.append(tateExtraWall[yIndex][xIndex]);
					}
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						if (yokoExtraWallDirection[yIndex][xIndex] != null) {
							sb.append(yokoExtraWallDirection[yIndex][xIndex].getDirectString());
						} else {
							sb.append(yokoExtraWall[yIndex][xIndex]);
						}
						if (xIndex != getXLength()) {
							if (icebarnPosSet.contains(new Position(yIndex, xIndex))) {
								sb.append("氷");
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
			if (!icebarnSolve()) {
				return false;
			}
			if (!continueSolve()) {
				return false;
			}
			if (!flowSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		private boolean flowSolve() {
			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		/**
		 * 白マスを進むとゴールまで行ける。いけない場合falseを返す。
		 * @return
		 */
		private boolean continueSolve() {
			return true;
		}

		/**
		 * 各アイスバーンには1マスは白マスが必要。
		 * 違反の場合はfalseを返す。
		 */
		private boolean icebarnSolve() {
			for (Set<Position> icebarn : icebarns) {
				boolean discoverWhite = false;
				Position spacePos = null;
				for (Position pos : icebarn) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						discoverWhite = true;
						break;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						if (spacePos != null) {
							break;
						}
						spacePos = pos;
					}
				}
				if (!discoverWhite) {
					if (spacePos == null) {
						return false;
					} else {
						masu[spacePos.getyIndex()][spacePos.getxIndex()] = Masu.NOT_BLACK;
					}
				}
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
					} else {
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
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								// 氷の上の場合、自分が白マスなら壁は0マスか2マス
								if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
									return false;
								}
								// カーブ禁止
								if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
										|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
										|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
										|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)) {
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
							} else if (masu[yIndex][xIndex] == Masu.SPACE) {
								// 氷の上の場合、自分が不確定マスなら壁は0マスか2マスか4マス
								if ((existsCount == 3 && notExistsCount == 1)
										|| (existsCount == 1 && notExistsCount == 3)) {
									return false;
								}
								if (existsCount > 2) {
									masu[yIndex][xIndex] = Masu.BLACK;
									if (existsCount == 3) {
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
								} else if (notExistsCount != 0) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
							// 氷の上でない場合、自分が白マスなら壁は必ず2マス
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
							} else if (masu[yIndex][xIndex] == Masu.SPACE) {
								// 氷の上でない場合、自分が不確定マスなら壁は2マスか4マス
								if ((existsCount == 3 && notExistsCount == 1)
										|| notExistsCount > 2) {
									return false;
								}
								if (existsCount > 2) {
									masu[yIndex][xIndex] = Masu.BLACK;
									if (existsCount == 3) {
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
								} else if (notExistsCount != 0) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
								}
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

	public IcebarnSolver(int height, int width, String param, int start, int goal) {
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
		System.out.println(new IcebarnSolver(height, width, param, start, goal).solve());
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
		System.out.println("難易度:" + count * 2);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		System.out.println(field);
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