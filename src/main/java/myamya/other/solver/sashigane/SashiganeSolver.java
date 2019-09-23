package myamya.other.solver.sashigane;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class SashiganeSolver implements Solver {

	/**
	 * 矢印のマス
	 */
	public static class Arrow {
		private final Direction direction;

		public Arrow(Direction direction) {
			this.direction = direction;
		}

		public Direction getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return direction.getDirectString();
		}
	}

	/**
	 * 丸のマス
	 */
	public static class Circle {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "○①②③④⑤⑥⑦⑧⑨";

		private int cnt;

		Circle(int cnt) {
			this.cnt = cnt;
		}

		@Override
		public String toString() {
			if (cnt == -1) {
				return "○";
			}
			if (cnt > 99) {
				return "99";
			}
			String capacityStr = String.valueOf(cnt);
			int index = HALF_NUMS.indexOf(capacityStr);
			if (index >= 0) {
				return FULL_NUMS.substring(index / 2, index / 2 + 1);
			} else {
				return capacityStr;
			}
		}

		public int getCnt() {
			return cnt;
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghij";
		static final String ALPHABET_FROM_K = "klmnopqrstuvwxyz";

		// 矢印の情報
		private Arrow[][] arrows;
		// 丸の情報
		private Circle[][] circles;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Arrow[][] getArrows() {
			return arrows;
		}

		public Circle[][] getCircles() {
			return circles;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return arrows.length;
		}

		public int getXLength() {
			return arrows[0].length;
		}

		public Field(int height, int width, String param) {
			arrows = new Arrow[height][width];
			circles = new Circle[height][width];
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_K.indexOf(ch);
				if (interval != -1) {
					index = index + interval;
				} else {
					int arrowInt = ALPHABET_FROM_G.indexOf(ch);
					if (arrowInt != -1) {
						Direction direction = Direction.getByNum(arrowInt + 1);
						Position pos = new Position(index / getXLength(), index % getXLength());
						arrows[pos.getyIndex()][pos.getxIndex()] = new Arrow(direction);
						if (pos.getyIndex() != 0) {
							if (direction != Direction.UP) {
								tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
							} else {
								tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
							}
						}
						if (pos.getxIndex() != getXLength() - 1) {
							if (direction != Direction.RIGHT) {
								yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
							} else if (pos.getyIndex() != 0) {
								yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							}
						}
						if (pos.getyIndex() != getYLength() - 1) {
							if (direction != Direction.DOWN) {
								tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
							} else {
								tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							}
						}
						if (pos.getxIndex() != 0) {
							if (direction != Direction.LEFT) {
								yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
							} else if (pos.getyIndex() != 0) {
								yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
							}
						}
					} else {
						//16 - 255は '-'
						//256 - 999は '+'
						try {
							int cnt;
							if (ch == '-') {
								cnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
								i++;
								i++;
							} else if (ch == '+') {
								cnt = Integer.parseInt(
										"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
										16);
								i++;
								i++;
								i++;
							} else if (ch == '.') {
								cnt = -1;
							} else {
								cnt = Integer.parseInt(String.valueOf(ch), 16);
							}
							Position pos = new Position(index / getXLength(), index % getXLength());
							circles[pos.getyIndex()][pos.getxIndex()] = new Circle(cnt);
						} catch (NumberFormatException e) {
							// 関係ない文字が来ていた場合は1文字進めるため何もしない
						}
					}
				}
				index++;
			}
			// 連続する○の間に壁をいれる。これを先にやると何かと都合がいいので。
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (circles[yIndex][xIndex] != null && circles[yIndex][xIndex + 1] != null) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (circles[yIndex][xIndex] != null && circles[yIndex + 1][xIndex] != null) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					}
				}
			}
		}

		public Field(Field other) {
			arrows = other.arrows;
			circles = other.circles;
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
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex].toString());
					} else if (circles[yIndex][xIndex] != null) {
						sb.append(circles[yIndex][xIndex].toString());
					} else {
						sb.append("　");
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
		 * 各マスの壁は必ず2か3になる。
		 * また、○のマスはカーブが確定する。満たさない場合falseを返す。
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
					if (circles[yIndex][xIndex] != null) {
						if ((wallUp == Wall.EXISTS && wallDown == Wall.EXISTS)
								|| (wallRight == Wall.EXISTS && wallLeft == Wall.EXISTS)) {
							return false;
						}
						if ((wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
								|| (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
							return false;
						}
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
					} else {
						if (existsCount > 3 || notExistsCount > 2) {
							return false;
						}
						if (existsCount == 3 && notExistsCount == 0) {
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
						if (notExistsCount == 2 && existsCount < 2) {
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
					}
				}
			}
			return true;
		}

		/**
		 * 曲がり角が確定しているマスについて、伸ばせる方向にとりあえず最低限の1マス伸ばす。
		 * 伸ばせない場合はfalseを返す。
		 */
		private boolean curveSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null) {
						int notExistsCount = 0;
						boolean toUp = false, toRight = false, toDown = false, toLeft = false;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.NOT_EXISTS) {
							notExistsCount++;
							toUp = true;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.NOT_EXISTS) {
							notExistsCount++;
							toRight = true;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.NOT_EXISTS) {
							notExistsCount++;
							toDown = true;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.NOT_EXISTS) {
							notExistsCount++;
							toLeft = true;
						}
						if (notExistsCount == 2 && !(toUp && toDown) && !(toRight && toLeft)) {
							// とりあえず1マスは伸ばす
							int i = 1;
							if (toUp) {
								if (xIndex != 0) {
									if (yokoWall[yIndex - i][xIndex - 1] == Wall.NOT_EXISTS) {
										return false;
									} else if (yokoWall[yIndex - i][xIndex - 1] == Wall.SPACE) {
										yokoWall[yIndex - i][xIndex - 1] = Wall.EXISTS;
									}
								}
								if (xIndex != getXLength() - 1) {
									if (yokoWall[yIndex - i][xIndex] == Wall.NOT_EXISTS) {
										return false;
									} else if (yokoWall[yIndex - i][xIndex] == Wall.SPACE) {
										yokoWall[yIndex - i][xIndex] = Wall.EXISTS;
									}
								}
							}
							if (toRight) {
								if (yIndex != 0) {
									if (tateWall[yIndex - 1][xIndex + i] == Wall.NOT_EXISTS) {
										return false;
									} else if (tateWall[yIndex - 1][xIndex + i] == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex + i] = Wall.EXISTS;
									}
								}
								if (yIndex != getYLength() - 1) {
									if (tateWall[yIndex][xIndex + i] == Wall.NOT_EXISTS) {
										return false;
									} else if (tateWall[yIndex][xIndex + i] == Wall.SPACE) {
										tateWall[yIndex][xIndex + i] = Wall.EXISTS;
									}
								}
							}
							if (toDown) {
								if (xIndex != 0) {
									if (yokoWall[yIndex + i][xIndex - 1] == Wall.NOT_EXISTS) {
										return false;
									} else if (yokoWall[yIndex + i][xIndex - 1] == Wall.SPACE) {
										yokoWall[yIndex + i][xIndex - 1] = Wall.EXISTS;
									}
								}
								if (xIndex != getXLength() - 1) {
									if (yokoWall[yIndex + i][xIndex] == Wall.NOT_EXISTS) {
										return false;
									} else if (yokoWall[yIndex + i][xIndex] == Wall.SPACE) {
										yokoWall[yIndex + i][xIndex] = Wall.EXISTS;
									}
								}
							}
							if (toLeft) {
								if (yIndex != 0) {
									if (tateWall[yIndex - 1][xIndex - i] == Wall.NOT_EXISTS) {
										return false;
									} else if (tateWall[yIndex - 1][xIndex - i] == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex - i] = Wall.EXISTS;
									}
								}
								if (yIndex != getYLength() - 1) {
									if (tateWall[yIndex][xIndex - i] == Wall.NOT_EXISTS) {
										return false;
									} else if (tateWall[yIndex][xIndex - i] == Wall.SPACE) {
										tateWall[yIndex][xIndex - i] = Wall.EXISTS;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各マスの端からスタートして、曲がる回数が0回または2回以上が確定する場合falseを返す。
		 */
		public boolean shapeLSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					boolean toUp = false, toRight = false, toDown = false, toLeft = false;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
						toUp = true;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
						toRight = true;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
						toDown = true;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
						toLeft = true;
					}
					if (existsCount == 3 && notExistsCount == 1) {
						Position curvePos = null;
						Direction nextDirection = null;
						// 確定カーブまで前進
						// 確定カーブにたどり着くまでにカーブ候補がなければfalse
						if (toUp) {
							for (int i = 1;; i++) {
								if (yIndex - i == 0 || tateWall[yIndex - i - 1][xIndex] == Wall.EXISTS
										|| circles[yIndex - i][xIndex] != null) {
									boolean canRight = true, canLeft = true;
									if (xIndex == getXLength() - 1 || yokoWall[yIndex - i][xIndex] == Wall.EXISTS) {
										canRight = false;
									}
									if (xIndex == 0 || yokoWall[yIndex - i][xIndex - 1] == Wall.EXISTS) {
										canLeft = false;
									}
									if (!canRight && !canLeft) {
										return false;
									} else if (!canRight) {
										nextDirection = Direction.LEFT;
									} else if (!canLeft) {
										nextDirection = Direction.RIGHT;
									}
									curvePos = new Position(yIndex - i, xIndex);
									break;
								} else if (tateWall[yIndex - i - 1][xIndex] == Wall.SPACE) {
									break;
								} else {
									boolean fixedRight = false, fixedLeft = false;
									if (xIndex != getXLength() - 1 && yokoWall[yIndex - i][xIndex] == Wall.NOT_EXISTS) {
										fixedRight = true;
									}
									if (xIndex != 0 && yokoWall[yIndex - i][xIndex - 1] == Wall.NOT_EXISTS) {
										fixedLeft = true;
									}
									if (fixedRight && fixedLeft) {
										return false;
									} else if (fixedRight) {
										curvePos = new Position(yIndex - i, xIndex);
										nextDirection = Direction.RIGHT;
										break;
									} else if (fixedLeft) {
										curvePos = new Position(yIndex - i, xIndex);
										nextDirection = Direction.LEFT;
										break;
									}
								}
							}
						}
						if (toRight) {
							for (int i = 1;; i++) {
								if (xIndex + i == getXLength() - 1 || yokoWall[yIndex][xIndex + i] == Wall.EXISTS
										|| circles[yIndex][xIndex + i] != null) {
									boolean canUp = true, canDown = true;
									if (yIndex == 0 || tateWall[yIndex - 1][xIndex + i] == Wall.EXISTS) {
										canUp = false;
									}
									if (yIndex == getYLength() - 1 || tateWall[yIndex][xIndex + i] == Wall.EXISTS) {
										canDown = false;
									}
									if (!canUp && !canDown) {
										return false;
									} else if (!canUp) {
										nextDirection = Direction.DOWN;
									} else if (!canDown) {
										nextDirection = Direction.UP;
									}
									curvePos = new Position(yIndex, xIndex + i);
									break;
								} else if (yokoWall[yIndex][xIndex + i] == Wall.SPACE) {
									break;
								} else {
									boolean fixedUp = false, fixedDown = false;
									if (yIndex != 0 && tateWall[yIndex - 1][xIndex + i] == Wall.NOT_EXISTS) {
										fixedUp = true;
									}
									if (yIndex != getYLength() - 1 && tateWall[yIndex][xIndex + i] == Wall.NOT_EXISTS) {
										fixedDown = true;
									}
									if (fixedUp && fixedDown) {
										return false;
									} else if (fixedUp) {
										curvePos = new Position(yIndex, xIndex + i);
										nextDirection = Direction.UP;
										break;
									} else if (fixedDown) {
										curvePos = new Position(yIndex, xIndex + i);
										nextDirection = Direction.DOWN;
										break;
									}
								}
							}
						}
						if (toDown) {
							for (int i = 1;; i++) {
								if (yIndex + i == getYLength() - 1 || tateWall[yIndex + i][xIndex] == Wall.EXISTS
										|| circles[yIndex + i][xIndex] != null) {
									boolean canRight = true, canLeft = true;
									if (xIndex == getXLength() - 1 || yokoWall[yIndex + i][xIndex] == Wall.EXISTS) {
										canRight = false;
									}
									if (xIndex == 0 || yokoWall[yIndex + i][xIndex - 1] == Wall.EXISTS) {
										canLeft = false;
									}
									if (!canRight && !canLeft) {
										return false;
									} else if (!canRight) {
										nextDirection = Direction.LEFT;
									} else if (!canLeft) {
										nextDirection = Direction.RIGHT;
									}
									curvePos = new Position(yIndex + i, xIndex);
									break;
								} else if (tateWall[yIndex + i][xIndex] == Wall.SPACE) {
									break;
								} else {
									boolean fixedRight = false, fixedLeft = false;
									if (xIndex != getXLength() - 1 && yokoWall[yIndex + i][xIndex] == Wall.NOT_EXISTS) {
										fixedRight = true;
									}
									if (xIndex != 0 && yokoWall[yIndex + i][xIndex - 1] == Wall.NOT_EXISTS) {
										fixedLeft = true;
									}
									if (fixedRight && fixedLeft) {
										return false;
									} else if (fixedRight) {
										curvePos = new Position(yIndex + i, xIndex);
										nextDirection = Direction.RIGHT;
										break;
									} else if (fixedLeft) {
										curvePos = new Position(yIndex + i, xIndex);
										nextDirection = Direction.LEFT;
										break;
									}
								}
							}
						}
						if (toLeft) {
							for (int i = 1;; i++) {
								if (xIndex - i == 0 || yokoWall[yIndex][xIndex - i - 1] == Wall.EXISTS
										|| circles[yIndex][xIndex - i] != null) {
									boolean canUp = true, canDown = true;
									if (yIndex == 0 || tateWall[yIndex - 1][xIndex - i] == Wall.EXISTS) {
										canUp = false;
									}
									if (yIndex == getYLength() - 1 || tateWall[yIndex][xIndex - i] == Wall.EXISTS) {
										canDown = false;
									}
									if (!canUp && !canDown) {
										return false;
									} else if (!canUp) {
										nextDirection = Direction.DOWN;
									} else if (!canDown) {
										nextDirection = Direction.UP;
									}
									curvePos = new Position(yIndex, xIndex - i);
									break;
								} else if (yokoWall[yIndex][xIndex - i - 1] == Wall.SPACE) {
									break;
								} else {
									boolean fixedUp = false, fixedDown = false;
									if (yIndex != 0 && tateWall[yIndex - 1][xIndex - i] == Wall.NOT_EXISTS) {
										fixedUp = true;
									}
									if (yIndex != getYLength() - 1 && tateWall[yIndex][xIndex - i] == Wall.NOT_EXISTS) {
										fixedDown = true;
									}
									if (fixedUp && fixedDown) {
										return false;
									} else if (fixedUp) {
										curvePos = new Position(yIndex, xIndex - i);
										nextDirection = Direction.UP;
										break;
									} else if (fixedDown) {
										curvePos = new Position(yIndex, xIndex - i);
										nextDirection = Direction.DOWN;
										break;
									}
								}
							}
						}
						// カーブの先の方向が決まっている
						if (nextDirection != null) {
							// カーブの先でさらにカーブしたり、○を取り込んだら失敗
							int curveY = curvePos.getyIndex();
							int curveX = curvePos.getxIndex();
							if (nextDirection == Direction.UP) {
								for (int i = 1;; i++) {
									if (curveX != getXLength() - 1 && yokoWall[curveY - i][curveX] == Wall.NOT_EXISTS) {
										return false;
									}
									if (curveX != 0 && yokoWall[curveY - i][curveX - 1] == Wall.NOT_EXISTS) {
										return false;
									}
									if (circles[curveY - i][curveX] != null) {
										return false;
									}
									if (curveY - i == 0 || tateWall[curveY - i - 1][curveX] != Wall.NOT_EXISTS) {
										break;
									}
								}
							} else if (nextDirection == Direction.RIGHT) {
								for (int i = 1;; i++) {
									if (curveY != getYLength() - 1 && tateWall[curveY][curveX + i] == Wall.NOT_EXISTS) {
										return false;
									}
									if (curveY != 0 && tateWall[curveY - 1][curveX + i] == Wall.NOT_EXISTS) {
										return false;
									}
									if (circles[curveY][curveX + i] != null) {
										return false;
									}
									if (curveX + i == getXLength() - 1
											|| yokoWall[curveY][curveX + i] != Wall.NOT_EXISTS) {
										break;
									}
								}
							} else if (nextDirection == Direction.DOWN) {
								for (int i = 1;; i++) {
									if (curveX != getXLength() - 1 && yokoWall[curveY + i][curveX] == Wall.NOT_EXISTS) {
										return false;
									}
									if (curveX != 0 && yokoWall[curveY + i][curveX - 1] == Wall.NOT_EXISTS) {
										return false;
									}
									if (circles[curveY + i][curveX] != null) {
										return false;
									}
									if (curveY + i == getYLength() - 1
											|| tateWall[curveY + i][curveX] != Wall.NOT_EXISTS) {
										break;
									}
								}
							} else if (nextDirection == Direction.LEFT) {
								for (int i = 1;; i++) {
									if (curveY != getYLength() - 1 && tateWall[curveY][curveX - i] == Wall.NOT_EXISTS) {
										return false;
									}
									if (curveY != 0 && tateWall[curveY - 1][curveX - i] == Wall.NOT_EXISTS) {
										return false;
									}
									if (circles[curveY][curveX - i] != null) {
										return false;
									}
									if (curveX - i == 0 || yokoWall[curveY][curveX - i - 1] != Wall.NOT_EXISTS) {
										break;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスから自身+上下または左右にそれぞれ何マス白マスを伸ばせる可能性があるか調べ、
		 * 一方向に延びることが確定している場合、残る1方向の不足分を伸ばす。
		 * 2方向を足しても数字が届かない場合falseを返す。
		 * また、上下または左右にを足した数が確定白マスに等しければその先をふさぐ。
		 * この時は、確定白マスが数字を上回っていたらfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex] != null && circles[yIndex][xIndex].getCnt() != -1) {
						int upSpaceCnt = 0;
						int rightSpaceCnt = 0;
						int downSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int upWhiteCnt = 0;
						int rightWhiteCnt = 0;
						int downWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean upCounting = true;
						boolean rightCounting = true;
						boolean downCounting = true;
						boolean leftupCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] == Wall.EXISTS || circles[targetY][xIndex] != null) {
								break;
							}
							if (tateWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] == Wall.EXISTS || circles[yIndex][targetX] != null) {
								break;
							}
							if (yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] == Wall.EXISTS || circles[targetY][xIndex] != null) {
								break;
							}
							if (tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] == Wall.EXISTS || circles[yIndex][targetX] != null) {
								break;
							}
							if (yokoWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								leftupCounting = false;
							}
							if (leftupCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCount = 1 + upSpaceCnt + downSpaceCnt + rightSpaceCnt + leftSpaceCnt;
						int aroundWhiteCount = 1 + upWhiteCnt + downWhiteCnt + rightWhiteCnt + leftWhiteCnt;
						int useNumber = circles[yIndex][xIndex].getCnt();
						if (aroundSpaceCount < useNumber) {
							// 伸ばせない
							return false;
						}
						if (aroundWhiteCount > useNumber) {
							// 伸ばしすぎ
							return false;
						}
						int fixedWhiteUp = useNumber
								- (1 + rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteRight = useNumber
								- (1 + upSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteDown = useNumber
								- (1 + upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
						int fixedWhitetLeft = useNumber
								- (1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt);
						if (fixedWhiteUp > 0) {
							for (int i = 1; i <= fixedWhiteUp; i++) {
								if (xIndex != 0) {
									if (yokoWall[yIndex - i][xIndex - 1] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex - i][xIndex - 1] = Wall.EXISTS;
								}
								if (xIndex != getXLength() - 1) {
									if (yokoWall[yIndex - i][xIndex] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex - i][xIndex] = Wall.EXISTS;
								}
								if (tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
									return false;
								}
								tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (fixedWhiteRight > 0) {
							for (int i = 1; i <= fixedWhiteRight; i++) {
								if (yIndex != 0) {
									if (tateWall[yIndex - 1][xIndex + i] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex - 1][xIndex + i] = Wall.EXISTS;
								}
								if (yIndex != getYLength() - 1) {
									if (tateWall[yIndex][xIndex + i] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex][xIndex + i] = Wall.EXISTS;
								}
								if (yokoWall[yIndex][xIndex + i - 1] == Wall.EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex + i - 1] = Wall.NOT_EXISTS;
							}
						}
						if (fixedWhiteDown > 0) {
							for (int i = 1; i <= fixedWhiteDown; i++) {
								if (xIndex != 0) {
									if (yokoWall[yIndex + i][xIndex - 1] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex + i][xIndex - 1] = Wall.EXISTS;
								}
								if (xIndex != getXLength() - 1) {
									if (yokoWall[yIndex + i][xIndex] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex + i][xIndex] = Wall.EXISTS;
								}
								if (tateWall[yIndex + i - 1][xIndex] == Wall.EXISTS) {
									return false;
								}
								tateWall[yIndex + i - 1][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (fixedWhitetLeft > 0) {
							for (int i = 1; i <= fixedWhitetLeft; i++) {
								if (yIndex != 0) {
									if (tateWall[yIndex - 1][xIndex - i] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex - 1][xIndex - i] = Wall.EXISTS;
								}
								if (yIndex != getYLength() - 1) {
									if (tateWall[yIndex][xIndex - i] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex][xIndex - i] = Wall.EXISTS;
								}
								if (yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex - i] = Wall.NOT_EXISTS;
							}
						}
						if (aroundWhiteCount == useNumber) {
							// 長さ限界に到達
							if (yIndex - upWhiteCnt - 1 >= 0) {
								if (tateWall[yIndex - upWhiteCnt - 1][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex - upWhiteCnt - 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								if (yokoWall[yIndex][xIndex + rightWhiteCnt] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex + rightWhiteCnt] = Wall.EXISTS;
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								if (tateWall[yIndex + downWhiteCnt][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex + downWhiteCnt][xIndex] = Wall.EXISTS;
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								if (yokoWall[yIndex][xIndex - leftWhiteCnt - 1] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex - leftWhiteCnt - 1] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!curveSolve()) {
				return false;
			}
			if (!numberSolve()) {
				return false;
			}
			if (!shapeLSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	public SashiganeSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "";//urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new SashiganeSolverNeo(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString();
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