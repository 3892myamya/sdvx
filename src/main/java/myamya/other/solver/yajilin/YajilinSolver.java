package myamya.other.solver.yajilin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class YajilinSolver implements Solver {

	/**
	 * 矢印を候補を列挙して解くためのサブソルバー
	 */
	static class ArrowSolver {

		/**
		 * 最大候補利用数
		 * 0にすると候補を利用したトライをしなくなる
		 */
		private static final int CANDMAX = 100;

		private final int height;
		private final int width;
		private final Arrow arrow;

		public ArrowSolver(int height, int width, Arrow arrow) {
			this.height = height;
			this.width = width;
			this.arrow = arrow;
		}

		public static void main(String[] args) {
			List<String> result = new ArrowSolver(10, 10, new Arrow(new Position(9, 5), Direction.UP, '3'))
					.solve();
			System.out.println(result);
		}

		/**
		 * 盤面の高さ・幅・矢印の位置・矢印の内容からマスの候補を返却する。
		 */
		public List<String> solve() {
			List<String> result = new ArrayList<>();
			oneSolve(result, new StringBuilder(), arrow.getPosition(), 0);
			return result.size() >= CANDMAX ? null : result;
		}

		private void oneSolve(List<String> result, StringBuilder sb,
				Position nowPos, int nowBlack) {
			if (result.size() >= CANDMAX) {
				return;
			}
			if (arrow.getCount() == nowBlack) {
				StringBuilder newSb = new StringBuilder(sb);
				if (arrow.getDirection() == Direction.UP) {
					for (int yIndex = nowPos.getyIndex() - 1; yIndex >= 0; yIndex--) {
						newSb.append(MasuImpl.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.RIGHT) {
					for (int xIndex = nowPos.getxIndex() + 1; xIndex < width; xIndex++) {
						newSb.append(MasuImpl.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.DOWN) {
					for (int yIndex = nowPos.getyIndex() + 1; yIndex < height; yIndex++) {
						newSb.append(MasuImpl.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.LEFT) {
					for (int xIndex = nowPos.getxIndex() - 1; xIndex >= 0; xIndex--) {
						newSb.append(MasuImpl.NOT_BLACK.toString());
					}
				}
				result.add(newSb.toString());
				return;
			} else {
				if (arrow.getDirection() == Direction.UP) {
					if (nowPos.getyIndex() == 0) {
						return;
					} else {
						if (nowPos.getyIndex() != 1) {
							sb.append(MasuImpl.BLACK.toString());
							sb.append(MasuImpl.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() - 2, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(MasuImpl.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() - 1, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(MasuImpl.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex() - 1, nowPos.getxIndex()), nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.RIGHT) {
					if (nowPos.getxIndex() == width - 1) {
						return;
					} else {
						if (nowPos.getxIndex() != width - 2) {
							sb.append(MasuImpl.BLACK.toString());
							sb.append(MasuImpl.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 2),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(MasuImpl.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(MasuImpl.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1),
								nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.DOWN) {
					if (nowPos.getyIndex() == height - 1) {
						return;
					} else {
						if (nowPos.getyIndex() != height - 2) {
							sb.append(MasuImpl.BLACK.toString());
							sb.append(MasuImpl.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() + 2, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(MasuImpl.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() + 1, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(MasuImpl.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex() + 1, nowPos.getxIndex()), nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.LEFT) {
					if (nowPos.getxIndex() == 0) {
						return;
					} else {
						if (nowPos.getxIndex() != 1) {
							sb.append(MasuImpl.BLACK.toString());
							sb.append(MasuImpl.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 2),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(MasuImpl.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(MasuImpl.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1),
								nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
			}
		}

	}

	public interface Masu {
		@Override
		public String toString();

		public String toStringWeb();
	}

	public enum MasuImpl implements Masu {
		SPACE("　"), BLACK("■"), NOT_BLACK("・");

		String str;

		MasuImpl(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public String toStringWeb() {
			return str;
		}
	}

	/**
	 * 矢印のマス
	 */
	public static class Arrow implements Masu {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final Position position;
		private final Direction direction;
		private final int count;

		public Arrow(Position position, Direction direction, int count) {
			this.position = position;
			this.direction = direction;
			this.count = count;
		}

		public Position getPosition() {
			return position;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return count == -1 ? "？" : (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

		public String toStringForweb() {
			String wkstr = String.valueOf(count);
			int index = HALF_NUMS.indexOf(wkstr);
			if (index >= 0) {
				wkstr = FULL_NUMS.substring(index / 2,
						index / 2 + 1);
			}
			return direction.getDirectString() + wkstr;
		}

		@Override
		public String toStringWeb() {
			return count == -1 ? "？" : direction.getDirectString() + count;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 矢印の候補情報
		private final Map<Arrow, List<String>> arrowsInfo;
		// バリアントルール-ループ内黒ます禁止
		private final boolean out;

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

		public Field(int height, int width, String param, boolean out) {
			masu = new Masu[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			arrowsInfo = new HashMap<>();
			this.out = out;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = MasuImpl.SPACE;
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
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (direction == null) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (5 <= val && val <= 9) {
							val = val - 5;
							adjust = true;
						}
						direction = Direction.getByNum(val);
						if (direction == null) {
							if (adjust) {
								i++;
							} else {
								Position arrowPos = new Position(index / getXLength(), index % getXLength());
								masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = new Arrow(arrowPos, direction, -1);
								// 周囲の壁を閉鎖
								if (arrowPos.getyIndex() != 0) {
									tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getxIndex() != getXLength() - 1) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getyIndex() != getYLength() - 1) {
									tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getxIndex() != 0) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.EXISTS;
								}
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					Position arrowPos = new Position(index / getXLength(), index % getXLength());
					Arrow arrow;
					if (adjust) {
						i++;
						arrow = new Arrow(arrowPos, direction, Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i)));
					} else {
						arrow = new Arrow(arrowPos, direction, Character.getNumericValue(ch));
					}
					masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = arrow;
					arrowsInfo.put(arrow,
							new ArrowSolver(getYLength(), getXLength(), arrow)
									.solve());
					// 周囲の壁を閉鎖
					if (arrowPos.getyIndex() != 0) {
						tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getxIndex() != getXLength() - 1) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getyIndex() != getYLength() - 1) {
						tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getxIndex() != 0) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.EXISTS;
					}
					adjust = false;
					index++;
					direction = null;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			arrowsInfo = new HashMap<>();
			out = other.out;
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
			for (Entry<Arrow, List<String>> entry : other.arrowsInfo.entrySet()) {
				arrowsInfo.put(entry.getKey(), entry.getValue() == null ? null : new ArrayList<>(entry.getValue()));
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
		 * 矢印に対し、指定した黒マス数を満たせなくなる場合falseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] instanceof Arrow) {
						Arrow arrow = (Arrow) masu[yIndex][xIndex];
						if (arrow.getCount() == -1) {
							continue;
						}
						//												int adjust = 0;
						//												int blackCount = 0;
						//												int notBlackCount = 0;
						//												while (true) {
						//													Position pos = null;
						//													if (arrow.getDirection() == Direction.UP) {
						//														if (yIndex - 1 - adjust < 0) {
						//															break;
						//														}
						//														pos = new Position(yIndex - 1 - adjust, xIndex);
						//													}
						//													if (arrow.getDirection() == Direction.RIGHT) {
						//														if (xIndex + 1 + adjust >= getXLength()) {
						//															break;
						//														}
						//														pos = new Position(yIndex, xIndex + 1 + adjust);
						//													}
						//													if (arrow.getDirection() == Direction.DOWN) {
						//														if (yIndex + 1 + adjust >= getYLength()) {
						//															break;
						//														}
						//														pos = new Position(yIndex + 1 + adjust, xIndex);
						//													}
						//													if (arrow.getDirection() == Direction.LEFT) {
						//														if (xIndex - 1 - adjust < 0) {
						//															break;
						//														}
						//														pos = new Position(yIndex, xIndex - 1 - adjust);
						//													}
						//													if (masu[pos.getyIndex()][pos.getxIndex()] instanceof Arrow) {
						//														Arrow anotherArrow = (Arrow) masu[pos.getyIndex()][pos.getxIndex()];
						//														if (anotherArrow.getDirection() == arrow.getDirection() && anotherArrow.getCount() != -1) {
						//															int betweenCnt = arrow.getCount() - anotherArrow.getCount();
						//															if (betweenCnt < blackCount) {
						//																return false;
						//															}
						//															if (betweenCnt > (adjust - notBlackCount)) {
						//																return false;
						//															}
						//															break;
						//														}
						//													} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
						//														blackCount++;
						//													} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.NOT_BLACK) {
						//														notBlackCount++;
						//													}
						//													adjust++;
						//												}
						if (arrowsInfo.get(arrow) != null) {
							List<String> candList = arrowsInfo.get(arrow);
							for (Iterator<String> iterator = candList.iterator(); iterator.hasNext();) {
								String state = iterator.next();
								for (int idx = 0; idx < state.length(); idx++) {
									Position pos = null;
									if (arrow.getDirection() == Direction.UP) {
										pos = new Position(yIndex - 1 - idx, xIndex);
									}
									if (arrow.getDirection() == Direction.RIGHT) {
										pos = new Position(yIndex, xIndex + 1 + idx);
									}
									if (arrow.getDirection() == Direction.DOWN) {
										pos = new Position(yIndex + 1 + idx, xIndex);
									}
									if (arrow.getDirection() == Direction.LEFT) {
										pos = new Position(yIndex, xIndex - 1 - idx);
									}
									if ((masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK
											&& state.charAt(idx) == '・')
											|| ((masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.NOT_BLACK
													|| masu[pos.getyIndex()][pos.getxIndex()] instanceof Arrow)
													&& state.charAt(idx) == '■')) {
										iterator.remove();
										break;
									}
								}
							}
							if (candList.size() == 0) {
								return false;
							} else {
								StringBuilder fixState = new StringBuilder(candList.get(0));
								for (String cand : candList) {
									for (int idx = 0; idx < fixState.length(); idx++) {
										char a = fixState.charAt(idx);
										char b = cand.charAt(idx);
										if ((a == '■' && b == '・') || (a == '・' && b == '■')) {
											fixState.setCharAt(idx, '　');
										}
									}
								}
								for (int idx = 0; idx < fixState.length(); idx++) {
									Position pos = null;
									if (arrow.getDirection() == Direction.UP) {
										pos = new Position(yIndex - 1 - idx, xIndex);
									}
									if (arrow.getDirection() == Direction.RIGHT) {
										pos = new Position(yIndex, xIndex + 1 + idx);
									}
									if (arrow.getDirection() == Direction.DOWN) {
										pos = new Position(yIndex + 1 + idx, xIndex);
									}
									if (arrow.getDirection() == Direction.LEFT) {
										pos = new Position(yIndex, xIndex - 1 - idx);
									}
									if (masu[pos.getyIndex()][pos.getxIndex()] instanceof Arrow) {
										continue;
									}
									if (fixState.charAt(idx) == '■') {
										masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.BLACK;
									} else if (fixState.charAt(idx) == '・') {
										masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.NOT_BLACK;
									} else {
										masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.SPACE;
									}
								}
							}
						} else {
							Position pivot = new Position(yIndex, xIndex);
							int idx = 0;
							int blackCnt = 0;
							int spaceCnt = 0;
							boolean nextCanSpace = true;
							if (arrow.getDirection() == Direction.UP) {
								while (pivot.getyIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
									if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.RIGHT) {
								while (pivot.getxIndex() + 1 + idx < getXLength()) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
									if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.DOWN) {
								while (pivot.getyIndex() + 1 + idx < getYLength()) {
									Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
									if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.LEFT) {
								while (pivot.getxIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
									if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getCount() > blackCnt + spaceCnt) {
								return false;
							}
						}
					}
				}
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
					if (masu[yIndex][xIndex] == MasuImpl.BLACK) {
						Masu masuUp, masuRight, masuDown, masuLeft;
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							masuUp = masu[yIndex - 1][xIndex];
						} else {
							masuUp = MasuImpl.NOT_BLACK;
						}
						if (xIndex != getXLength() - 1) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
							masuRight = masu[yIndex][xIndex + 1];
						} else {
							masuRight = MasuImpl.NOT_BLACK;
						}
						if (yIndex != getYLength() - 1) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
							masuDown = masu[yIndex + 1][xIndex];
						} else {
							masuDown = MasuImpl.NOT_BLACK;
						}
						if (xIndex != 0) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							masuLeft = masu[yIndex][xIndex - 1];
						} else {
							masuLeft = MasuImpl.NOT_BLACK;
						}
						if (masuUp == MasuImpl.BLACK || masuRight == MasuImpl.BLACK || masuDown == MasuImpl.BLACK
								|| masuLeft == MasuImpl.BLACK) {
							return false;
						}
						if (masuUp == MasuImpl.SPACE) {
							masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
						}
						if (masuRight == MasuImpl.SPACE) {
							masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
						}
						if (masuDown == MasuImpl.SPACE) {
							masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
						}
						if (masuLeft == MasuImpl.SPACE) {
							masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
						}
					} else {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							if (masu[yIndex - 1][xIndex] == MasuImpl.BLACK) {
								return false;
							}
							masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex + 1] == MasuImpl.BLACK) {
								return false;
							}
							masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							if (masu[yIndex + 1][xIndex] == MasuImpl.BLACK) {
								return false;
							}
							masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex - 1] == MasuImpl.BLACK) {
								return false;
							}
							masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
							notExistsCount++;
						}
						// 自分が白マスなら壁は必ず2マス
						if (masu[yIndex][xIndex] == MasuImpl.NOT_BLACK) {
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
									if (masu[yIndex - 1][xIndex] == MasuImpl.BLACK) {
										return false;
									}
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
								}
								if (wallRight == Wall.SPACE) {
									if (masu[yIndex][xIndex + 1] == MasuImpl.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
								}
								if (wallDown == Wall.SPACE) {
									if (masu[yIndex + 1][xIndex] == MasuImpl.BLACK) {
										return false;
									}
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
								}
								if (wallLeft == Wall.SPACE) {
									if (masu[yIndex][xIndex - 1] == MasuImpl.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
								}
							}
						} else if (masu[yIndex][xIndex] == MasuImpl.SPACE) {
							// 自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount == 2) {
								// 壁が2個の場合、壁のない方の白マス確定(出口理論)
								if (wallUp != Wall.EXISTS) {
									if (masu[yIndex - 1][xIndex] == MasuImpl.BLACK) {
										return false;
									}
									masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
								}
								if (wallRight != Wall.EXISTS) {
									if (masu[yIndex][xIndex + 1] == MasuImpl.BLACK) {
										return false;
									}
									masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
								}
								if (wallDown != Wall.EXISTS) {
									if (masu[yIndex + 1][xIndex] == MasuImpl.BLACK) {
										return false;
									}
									masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
								}
								if (wallLeft != Wall.EXISTS) {
									if (masu[yIndex][xIndex - 1] == MasuImpl.BLACK) {
										return false;
									}
									masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
								}
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = MasuImpl.BLACK;
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
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = MasuImpl.NOT_BLACK;
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
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == MasuImpl.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.isEmpty()) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
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
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!arrowSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (out) {
					if (!outsideSolve()) {
						return false;
					}
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ヤジリンのルール上、各列をふさぐ壁は必ず偶数になる。
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
		 * ループ内黒マス禁止ヤリジン専用の解法
		 * 黒マスから前後左右に向かっていき、外壁か黒マスのいずれかにぶつかるまでに
		 * 壁の数が奇数個であれば失敗。
		 * また、黒マスで未確定壁が残り1マスの場合は、偶数個になるように壁の有無が確定する。
		 */
		private boolean outsideSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pivot = new Position(yIndex, xIndex);
					// 左側横壁上方向
					int idx = 0;
					int whiteCnt = 0;
					int spaceCnt = 0;
					Position spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}
					// 右側横壁上方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 左側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 右側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 上側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 上側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != MasuImpl.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean oneOutSideCheck(Position pivot, int whiteCnt, int spaceCnt, Position spacePos, boolean isYoko) {
			if (spaceCnt == 0 && whiteCnt % 2 == 1) {
				if (masu[pivot.getyIndex()][pivot.getxIndex()] == MasuImpl.BLACK) {
					return false;
				} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == MasuImpl.SPACE) {
					masu[pivot.getyIndex()][pivot.getxIndex()] = MasuImpl.NOT_BLACK;
				}
			} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == MasuImpl.BLACK && spaceCnt == 1) {
				if (whiteCnt % 2 == 1) {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					}
				} else {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == MasuImpl.SPACE) {
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

	public YajilinSolver(int height, int width, String param, boolean out) {
		field = new Field(height, width, param, out);
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
		System.out.println(new YajilinSolver(height, width, param, false).solve());
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
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == MasuImpl.SPACE) {
					// 周囲に空白が少ない個所を優先して調査
					Masu masuUp = yIndex == 0 ? MasuImpl.BLACK
							: field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 ? MasuImpl.BLACK
							: field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 ? MasuImpl.BLACK
							: field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? MasuImpl.BLACK
							: field.masu[yIndex][xIndex - 1];
					int whiteCnt = 0;
					if (masuUp == MasuImpl.SPACE) {
						whiteCnt++;
					}
					if (masuRight == MasuImpl.SPACE) {
						whiteCnt++;
					}
					if (masuDown == MasuImpl.SPACE) {
						whiteCnt++;
					}
					if (masuLeft == MasuImpl.SPACE) {
						whiteCnt++;
					}
					if (whiteCnt > 3) {
						continue;
					}
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (recursive > 0) {
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
		virtual.masu[yIndex][xIndex] = MasuImpl.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = MasuImpl.NOT_BLACK;
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
