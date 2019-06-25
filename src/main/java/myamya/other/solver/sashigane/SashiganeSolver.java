package myamya.other.solver.sashigane;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class SashiganeSolver implements Solver {
	public enum Wall {
		SPACE("＊"), NOT_EXISTS("　"), EXISTS("□");

		String str;

		Wall(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public interface Mark {
		@Override
		public String toString();
	}

	/**
	 * 矢印のマス
	 */
	public static class Arrow implements Mark {
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
	public static class Circle implements Mark {

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

		// 約物の情報
		private Mark[][] mark;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Mark[][] getMark() {
			return mark;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return mark.length;
		}

		public int getXLength() {
			return mark[0].length;
		}

		public Field(int height, int width, String param) {
			mark = new Mark[height][width];
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
						mark[pos.getyIndex()][pos.getxIndex()] = new Arrow(direction);
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
						mark[pos.getyIndex()][pos.getxIndex()] = new Circle(cnt);
					}
				}
				index++;
			}
			// 連続する○の間に壁をいれる。これを先にやると何かと都合がいいので。
			// TODO circleSolveをうまく作ればいらなくなる
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (mark[yIndex][xIndex] instanceof Circle && mark[yIndex][xIndex + 1] instanceof Circle) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (mark[yIndex][xIndex] instanceof Circle && mark[yIndex + 1][xIndex] instanceof Circle) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					}
				}
			}
		}

		public Field(Field other) {
			mark = other.mark;
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
					Mark oneMark = mark[yIndex][xIndex];
					if (oneMark != null) {
						sb.append(oneMark.toString());
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
					if (mark[yIndex][xIndex] instanceof Circle) {
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
					if (!(mark[yIndex][xIndex] instanceof Arrow)) {
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
		 * 丸がついている曲がり角の方向を可能な限り確定し、その分を伸ばす。
		 * 伸ばせないことがわかったらfalseを返す。
		 * TODO 結構難しくて調整中
		 */
		private boolean circleSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (mark[yIndex][xIndex] instanceof Circle && ((Circle) mark[yIndex][xIndex]).getCnt() != -1) {
						boolean toUp = false, toRight = false, toDown = false, toLeft = false;
						boolean minCountUp = false, minCountRight = false, minCountDown = false, minCountLeft = false;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.NOT_EXISTS) {
							minCountUp = true;
						}
						if (wallUp != Wall.EXISTS) {
							toUp = true;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.NOT_EXISTS) {
							minCountRight = true;
						}
						if (wallRight != Wall.EXISTS) {
							toRight = true;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.NOT_EXISTS) {
							minCountDown = true;
						}
						if (wallDown != Wall.EXISTS) {
							toDown = true;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.NOT_EXISTS) {
							minCountLeft = true;
						}
						if (wallLeft != Wall.EXISTS) {
							toLeft = true;
						}
						int fixedDirection = (minCountUp ? 1 : 0) + (minCountRight ? 1 : 0) + (minCountDown ? 1 : 0)
								+ (minCountLeft ? 1 : 0);
						// 確定方向が2方向を超えていたら失敗
						if (fixedDirection > 2) {
							return false;
						}
						// 確定方向が2方向なら確定方向以外の調査不要
						if (fixedDirection == 2) {
							if (!minCountUp) {
								toUp = false;
							}
							if (!minCountRight) {
								toRight = false;
							}
							if (!minCountDown) {
								toDown = false;
							}
							if (!minCountLeft) {
								toLeft = false;
							}
						} else {
							// TODO ここが2以外の場合をちゃんと作りたいなあ…
							continue;
						}
						// 何マス伸ばせるか、何マス伸ばさないといけないかを計測
						int maxUp = 0, maxRight = 0, maxDown = 0, maxLeft = 0;
						int minUp = 0, minRight = 0, minDown = 0, minLeft = 0;
						if (toUp) {
							for (int i = 1;; i++) {
								maxUp++;
								if (minCountUp) {
									minUp++;
								}
								if (yIndex - i == 0 || tateWall[yIndex - i - 1][xIndex] == Wall.EXISTS
										|| mark[yIndex - i][xIndex] != null) {
									break;
								} else if (tateWall[yIndex - i - 1][xIndex] == Wall.SPACE && minCountUp) {
									minCountUp = false;
								}
							}
						}
						if (toRight) {
							for (int i = 1;; i++) {
								maxRight++;
								if (minCountRight) {
									minRight++;
								}
								if (xIndex + i == getXLength() - 1
										|| yokoWall[yIndex][xIndex + i] == Wall.EXISTS
										|| mark[yIndex][xIndex + i] != null) {
									break;
								} else if (yokoWall[yIndex][xIndex + i] == Wall.SPACE && minCountRight) {
									minCountRight = false;
								}
							}
						}
						if (toDown) {
							for (int i = 1;; i++) {
								maxDown++;
								if (minCountDown) {
									minDown++;
								}
								if (yIndex + i == getYLength() - 1
										|| tateWall[yIndex + i][xIndex] == Wall.EXISTS
										|| mark[yIndex + i][xIndex] != null) {
									break;
								} else if (tateWall[yIndex + i][xIndex] == Wall.SPACE && minCountDown) {
									minCountDown = false;
								}
							}
						}
						if (toLeft) {
							for (int i = 1;; i++) {
								maxLeft++;
								if (minCountLeft) {
									minLeft++;
								}
								if (xIndex - i == 0 || yokoWall[yIndex][xIndex - i - 1] == Wall.EXISTS
										|| mark[yIndex][xIndex - i] != null) {
									break;
								} else if (yokoWall[yIndex][xIndex - i - 1] == Wall.SPACE && minCountLeft) {
									minCountLeft = false;
								}
							}
						}
						// TODO ここを最大・最少だけじゃなくて、残りをどんなに伸ばしても足りない分みたいな考えをいれたい。
						int maxCnt = 1 + maxUp + maxRight + maxDown + maxLeft;
						int minCnt = 1 + minUp + minRight + minDown + minLeft;
						if (maxCnt < ((Circle) mark[yIndex][xIndex]).getCnt()) {
							return false;
						}
						if (minCnt > ((Circle) mark[yIndex][xIndex]).getCnt()) {
							return false;
						}
						if (maxCnt == ((Circle) mark[yIndex][xIndex]).getCnt()) {
							// 最大伸ばしで確定
							if (toUp) {
								for (int i = 1;; i++) {
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
									if (yIndex - i == 0 || tateWall[yIndex - i - 1][xIndex] == Wall.EXISTS) {
										break;
									}
									if (mark[yIndex - i][xIndex] != null) {
										tateWall[yIndex - i - 1][xIndex] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toRight) {
								for (int i = 1;; i++) {
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
									if (xIndex + i == getXLength() - 1
											|| yokoWall[yIndex][xIndex + i] == Wall.EXISTS) {
										break;
									}
									if (mark[yIndex][xIndex + i] != null) {
										yokoWall[yIndex][xIndex + i] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toDown) {
								for (int i = 1;; i++) {
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
									if (yIndex + i == getYLength() - 1
											|| tateWall[yIndex + i][xIndex] == Wall.EXISTS) {
										break;
									}
									if (mark[yIndex + i][xIndex] != null) {
										tateWall[yIndex + i][xIndex] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toLeft) {
								for (int i = 1;; i++) {
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
									if (xIndex - i == 0 || yokoWall[yIndex][xIndex - i - 1] == Wall.EXISTS) {
										break;
									}
									if (mark[yIndex][xIndex - i] != null) {
										yokoWall[yIndex][xIndex - i - 1] = Wall.EXISTS;
										break;
									}
								}
							}
						}
						if (minCnt == ((Circle) mark[yIndex][xIndex]).getCnt()) {
							// 最小伸ばしで確定
							if (toUp) {
								for (int i = 1;; i++) {
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
									if (yIndex - i == 0 || tateWall[yIndex - i - 1][xIndex] == Wall.EXISTS
											|| mark[yIndex - i][xIndex] != null) {
										break;
									}
									if (tateWall[yIndex - i - 1][xIndex] == Wall.SPACE) {
										tateWall[yIndex - i - 1][xIndex] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toRight) {
								for (int i = 1;; i++) {
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
									if (xIndex + i == getXLength() - 1
											|| yokoWall[yIndex][xIndex + i] == Wall.EXISTS
											|| mark[yIndex][xIndex + i] != null) {
										break;
									}
									if (yokoWall[yIndex][xIndex + i] == Wall.SPACE) {
										yokoWall[yIndex][xIndex + i] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toDown) {
								for (int i = 1;; i++) {
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
									if (yIndex + i == getYLength() - 1
											|| tateWall[yIndex + i][xIndex] == Wall.EXISTS
											|| mark[yIndex + i][xIndex] != null) {
										break;
									}
									if (tateWall[yIndex + i][xIndex] == Wall.SPACE) {
										tateWall[yIndex + i][xIndex] = Wall.EXISTS;
										break;
									}
								}
							}
							if (toLeft) {
								for (int i = 1;; i++) {
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
									if (xIndex - i == 0 || yokoWall[yIndex][xIndex - i - 1] == Wall.EXISTS
											|| mark[yIndex][xIndex - i] != null) {
										break;
									}
									if (yokoWall[yIndex][xIndex - i - 1] == Wall.SPACE) {
										yokoWall[yIndex][xIndex - i - 1] = Wall.EXISTS;
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
										|| mark[yIndex - i][xIndex] instanceof Circle) {
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
										|| mark[yIndex][xIndex + i] instanceof Circle) {
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
										|| mark[yIndex + i][xIndex] instanceof Circle) {
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
										|| mark[yIndex][xIndex - i] instanceof Circle) {
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
									if (mark[curveY - i][curveX] instanceof Circle) {
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
									if (mark[curveY][curveX + i] instanceof Circle) {
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
									if (mark[curveY + i][curveX] instanceof Circle) {
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
									if (mark[curveY][curveX - i] instanceof Circle) {
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
			if (!circleSolve()) {
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
		System.out.println(new SashiganeSolver(height, width, param).solve());
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
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive - 1)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive - 1)) {
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
