package myamya.other.solver.hebi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HebiSolver implements Solver {
	/**
	 * 矢印のマス
	 */
	public static class Arrow {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, int count) {
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

		public String toStringForweb() {
			String wkstr = String.valueOf(count);
			int index = HALF_NUMS.indexOf(wkstr);
			if (index >= 0) {
				wkstr = FULL_NUMS.substring(index / 2,
						index / 2 + 1);
			}
			return direction.getDirectString() + wkstr;
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 矢印情報
		private final Arrow[][] arrows;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			arrows = new Arrow[height][width];
			numbersCand = new ArrayList[height][width];
			// 0(空白)から5まで候補を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 5; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
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
								arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = new Arrow(direction, -1);
								// 矢印マスは候補数字0確定
								numbersCand[arrowPos.getyIndex()][arrowPos.getxIndex()].clear();
								numbersCand[arrowPos.getyIndex()][arrowPos.getxIndex()].add(0);
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
						arrow = new Arrow(direction, Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i)));
					} else {
						arrow = new Arrow(direction, Character.getNumericValue(ch));
					}
					arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = arrow;
					// 矢印マスは候補数字0確定
					numbersCand[arrowPos.getyIndex()][arrowPos.getxIndex()].clear();
					numbersCand[arrowPos.getyIndex()][arrowPos.getxIndex()].add(0);
					adjust = false;
					index++;
					direction = null;
				}
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			arrows = other.arrows;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
					} else {
						if (numbersCand[yIndex][xIndex].size() == 0) {
							sb.append("×");
						} else if (numbersCand[yIndex][xIndex].size() == 1) {
							String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
							int index = HALF_NUMS.indexOf(numStr);
							if (index == 0) {
								sb.append("・");
							} else {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							}
						} else if (numbersCand[yIndex][xIndex].size() == 2) {
							sb.append(numbersCand[yIndex][xIndex].get(0));
							sb.append(numbersCand[yIndex][xIndex].get(1));
						} else {
							sb.append("　");
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * 矢印の向いている先で最初に登場する数字の位置が確定する場合、その数字を確定する。
		 * 数字が置けない状況であればfalseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null && arrows[yIndex][xIndex].getCount() != -1) {
						int idx = 0;
						Position pivot = new Position(yIndex, xIndex);
						Arrow arrow = arrows[yIndex][xIndex];
						List<Position> candPosList = new ArrayList<>();
						if (arrow.getDirection() == Direction.UP) {
							while (pivot.getyIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
								if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
									break;
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].contains(arrow.getCount())) {
									candPosList.add(pos);
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
									int number = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
									if (number != 0) {
										if (arrow.getCount() == 0) {
											return false;
										} else {
											break;
										}
									}
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.RIGHT) {
							while (pivot.getxIndex() + 1 + idx < getXLength()) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
								if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
									break;
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].contains(arrow.getCount())) {
									candPosList.add(pos);
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
									int number = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
									if (number != 0) {
										if (arrow.getCount() == 0) {
											return false;
										} else {
											break;
										}
									}
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.DOWN) {
							while (pivot.getyIndex() + 1 + idx < getYLength()) {
								Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
								if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
									break;
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].contains(arrow.getCount())) {
									candPosList.add(pos);
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
									int number = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
									if (number != 0) {
										if (arrow.getCount() == 0) {
											return false;
										} else {
											break;
										}
									}
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.LEFT) {
							while (pivot.getxIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
								if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
									break;
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].contains(arrow.getCount())) {
									candPosList.add(pos);
								}
								if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
									int number = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
									if (number != 0) {
										if (arrow.getCount() == 0) {
											return false;
										} else {
											break;
										}
									}
								}
								idx++;
							}
						}
						if (candPosList.size() == 0) {
							// 矢印の数字を置く場所がない場合
							return false;
						}
						if (candPosList.size() == 1 || arrow.getCount() == 0) {
							for (Position pos : candPosList) {
								// 矢印の数字を置く場所が確定した場合
								numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
								numbersCand[pos.getyIndex()][pos.getxIndex()].add(arrow.getCount());
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 2-1が確定している場合、その先にヘビがいてはならないため0が確定する。
		 * 2-1の先に0以外があった場合falseを返す。
		 */
		private boolean headSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) == 2) {
						Position pivot = new Position(yIndex, xIndex);
						int idx = 1;
						if (yIndex != 0) {
							if (numbersCand[yIndex - 1][xIndex].size() == 1
									&& numbersCand[yIndex - 1][xIndex].get(0) == 1) {
								while (pivot.getyIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
									if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
										break;
									}
									if (!numbersCand[pos.getyIndex()][pos.getxIndex()].contains(0)) {
										return false;
									}
									numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
									numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
									idx++;
								}
							}
						}
						if (xIndex != getXLength() - 1) {
							if (numbersCand[yIndex][xIndex + 1].size() == 1
									&& numbersCand[yIndex][xIndex + 1].get(0) == 1) {
								while (pivot.getxIndex() + 1 + idx < getXLength()) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
									if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
										break;
									}
									if (!numbersCand[pos.getyIndex()][pos.getxIndex()].contains(0)) {
										return false;
									}
									numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
									numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
									idx++;
								}
							}
						}
						if (yIndex != getYLength() - 1) {
							if (numbersCand[yIndex + 1][xIndex].size() == 1
									&& numbersCand[yIndex + 1][xIndex].get(0) == 1) {
								while (pivot.getyIndex() + 1 + idx < getYLength()) {
									Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
									if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
										break;
									}
									if (!numbersCand[pos.getyIndex()][pos.getxIndex()].contains(0)) {
										return false;
									}
									numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
									numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
									idx++;
								}
							}
						}
						if (xIndex != 0) {
							if (numbersCand[yIndex][xIndex - 1].size() == 1
									&& numbersCand[yIndex][xIndex - 1].get(0) == 1) {
								while (pivot.getxIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
									if (arrows[pos.getyIndex()][pos.getxIndex()] != null) {
										break;
									}
									if (!numbersCand[pos.getyIndex()][pos.getxIndex()].contains(0)) {
										return false;
									}
									numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
									numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
									idx++;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * ヘビは1-5になり、異なるヘビは隣接しないので以下の隣接条件が成り立つ。
		 * 1の隣には5が0、4が1以下、3が0、2が1、1が0。
		 * 2の隣には5が1以下、4が0、3が1、2が0、1が1。
		 * 3の隣には5が0、4が1、3が0、2が1、1が0。
		 * 4の隣には5が1、4が0、3が1、2が0、1が以下。
		 * 5の隣には5が0、4が1、3が0、2が1以下、1が0。
		 * 上記を満たすように候補を減らし、違反した場合はfalseを返す。
		 */
		private boolean snakeSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) != 0) {
						int number = numbersCand[yIndex][xIndex].get(0);
						if (number % 2 == 1) {
							int cntForce2 = 0;
							int cntForce4 = 0;
							int cnt2 = 0;
							int cnt4 = 0;
							if (yIndex != 0) {
								numbersCand[yIndex - 1][xIndex].remove(new Integer(1));
								numbersCand[yIndex - 1][xIndex].remove(new Integer(3));
								numbersCand[yIndex - 1][xIndex].remove(new Integer(5));
								if (numbersCand[yIndex - 1][xIndex].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex - 1][xIndex].contains(2)) {
									cnt2++;
									if (numbersCand[yIndex - 1][xIndex].size() == 1) {
										cntForce2++;
									}
								}
								if (numbersCand[yIndex - 1][xIndex].contains(4)) {
									cnt4++;
									if (numbersCand[yIndex - 1][xIndex].size() == 1) {
										cntForce4++;
									}
								}
							}
							if (xIndex != getXLength() - 1) {
								numbersCand[yIndex][xIndex + 1].remove(new Integer(1));
								numbersCand[yIndex][xIndex + 1].remove(new Integer(3));
								numbersCand[yIndex][xIndex + 1].remove(new Integer(5));
								if (numbersCand[yIndex][xIndex + 1].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex][xIndex + 1].contains(2)) {
									cnt2++;
									if (numbersCand[yIndex][xIndex + 1].size() == 1) {
										cntForce2++;
									}
								}
								if (numbersCand[yIndex][xIndex + 1].contains(4)) {
									cnt4++;
									if (numbersCand[yIndex][xIndex + 1].size() == 1) {
										cntForce4++;
									}
								}
							}
							if (yIndex != getYLength() - 1) {
								numbersCand[yIndex + 1][xIndex].remove(new Integer(1));
								numbersCand[yIndex + 1][xIndex].remove(new Integer(3));
								numbersCand[yIndex + 1][xIndex].remove(new Integer(5));
								if (numbersCand[yIndex + 1][xIndex].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex + 1][xIndex].contains(2)) {
									cnt2++;
									if (numbersCand[yIndex + 1][xIndex].size() == 1) {
										cntForce2++;
									}
								}
								if (numbersCand[yIndex + 1][xIndex].contains(4)) {
									cnt4++;
									if (numbersCand[yIndex + 1][xIndex].size() == 1) {
										cntForce4++;
									}
								}
							}
							if (xIndex != 0) {
								numbersCand[yIndex][xIndex - 1].remove(new Integer(1));
								numbersCand[yIndex][xIndex - 1].remove(new Integer(3));
								numbersCand[yIndex][xIndex - 1].remove(new Integer(5));
								if (numbersCand[yIndex][xIndex - 1].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex][xIndex - 1].contains(2)) {
									cnt2++;
									if (numbersCand[yIndex][xIndex - 1].size() == 1) {
										cntForce2++;
									}
								}
								if (numbersCand[yIndex][xIndex - 1].contains(4)) {
									cnt4++;
									if (numbersCand[yIndex][xIndex - 1].size() == 1) {
										cntForce4++;
									}
								}
							}
							if (cntForce2 > 1 || cntForce4 > 1) {
								return false;
							}
							if (number == 1 || number == 3) {
								if (cnt2 == 0) {
									return false;
								}
								if (cntForce2 == 0 && cnt2 == 1) {
									if (yIndex != 0 && numbersCand[yIndex - 1][xIndex].contains(2)) {
										numbersCand[yIndex - 1][xIndex].clear();
										numbersCand[yIndex - 1][xIndex].add(2);
									} else if (xIndex != getXLength() - 1
											&& numbersCand[yIndex][xIndex + 1].contains(2)) {
										numbersCand[yIndex][xIndex + 1].clear();
										numbersCand[yIndex][xIndex + 1].add(2);
									} else if (yIndex != getYLength() - 1
											&& numbersCand[yIndex + 1][xIndex].contains(2)) {
										numbersCand[yIndex + 1][xIndex].clear();
										numbersCand[yIndex + 1][xIndex].add(2);
									} else if (xIndex != 0 && numbersCand[yIndex][xIndex - 1].contains(2)) {
										numbersCand[yIndex][xIndex - 1].clear();
										numbersCand[yIndex][xIndex - 1].add(2);
									}
								}
							}
							if (number == 3 || number == 5) {
								if (cnt4 == 0) {
									return false;
								}
								if (cntForce4 == 0 && cnt4 == 1) {
									if (yIndex != 0 && numbersCand[yIndex - 1][xIndex].contains(4)) {
										numbersCand[yIndex - 1][xIndex].clear();
										numbersCand[yIndex - 1][xIndex].add(4);
									} else if (xIndex != getXLength() - 1
											&& numbersCand[yIndex][xIndex + 1].contains(4)) {
										numbersCand[yIndex][xIndex + 1].clear();
										numbersCand[yIndex][xIndex + 1].add(4);
									} else if (yIndex != getYLength() - 1
											&& numbersCand[yIndex + 1][xIndex].contains(4)) {
										numbersCand[yIndex + 1][xIndex].clear();
										numbersCand[yIndex + 1][xIndex].add(4);
									} else if (xIndex != 0 && numbersCand[yIndex][xIndex - 1].contains(4)) {
										numbersCand[yIndex][xIndex - 1].clear();
										numbersCand[yIndex][xIndex - 1].add(4);
									}
								}
							}
						} else {
							int cntForce1 = 0;
							int cntForce3 = 0;
							int cntForce5 = 0;
							int cnt1 = 0;
							int cnt3 = 0;
							int cnt5 = 0;
							if (yIndex != 0) {
								numbersCand[yIndex - 1][xIndex].remove(new Integer(2));
								numbersCand[yIndex - 1][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex - 1][xIndex].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex - 1][xIndex].contains(1)) {
									cnt1++;
									if (numbersCand[yIndex - 1][xIndex].size() == 1) {
										cntForce1++;
									}
								}
								if (numbersCand[yIndex - 1][xIndex].contains(3)) {
									cnt3++;
									if (numbersCand[yIndex - 1][xIndex].size() == 1) {
										cntForce3++;
									}
								}
								if (numbersCand[yIndex - 1][xIndex].contains(5)) {
									cnt5++;
									if (numbersCand[yIndex - 1][xIndex].size() == 1) {
										cntForce5++;
									}
								}
							}
							if (xIndex != getXLength() - 1) {
								numbersCand[yIndex][xIndex + 1].remove(new Integer(2));
								numbersCand[yIndex][xIndex + 1].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex + 1].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex][xIndex + 1].contains(1)) {
									cnt1++;
									if (numbersCand[yIndex][xIndex + 1].size() == 1) {
										cntForce1++;
									}
								}
								if (numbersCand[yIndex][xIndex + 1].contains(3)) {
									cnt3++;
									if (numbersCand[yIndex][xIndex + 1].size() == 1) {
										cntForce3++;
									}
								}
								if (numbersCand[yIndex][xIndex + 1].contains(5)) {
									cnt5++;
									if (numbersCand[yIndex][xIndex + 1].size() == 1) {
										cntForce5++;
									}
								}
							}
							if (yIndex != getYLength() - 1) {
								numbersCand[yIndex + 1][xIndex].remove(new Integer(2));
								numbersCand[yIndex + 1][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex + 1][xIndex].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex + 1][xIndex].contains(1)) {
									cnt1++;
									if (numbersCand[yIndex + 1][xIndex].size() == 1) {
										cntForce1++;
									}
								}
								if (numbersCand[yIndex + 1][xIndex].contains(3)) {
									cnt3++;
									if (numbersCand[yIndex + 1][xIndex].size() == 1) {
										cntForce3++;
									}
								}
								if (numbersCand[yIndex + 1][xIndex].contains(5)) {
									cnt5++;
									if (numbersCand[yIndex + 1][xIndex].size() == 1) {
										cntForce5++;
									}
								}
							}
							if (xIndex != 0) {
								numbersCand[yIndex][xIndex - 1].remove(new Integer(2));
								numbersCand[yIndex][xIndex - 1].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex - 1].size() == 0) {
									return false;
								}
								if (numbersCand[yIndex][xIndex - 1].contains(1)) {
									cnt1++;
									if (numbersCand[yIndex][xIndex - 1].size() == 1) {
										cntForce1++;
									}
								}
								if (numbersCand[yIndex][xIndex - 1].contains(3)) {
									cnt3++;
									if (numbersCand[yIndex][xIndex - 1].size() == 1) {
										cntForce3++;
									}
								}
								if (numbersCand[yIndex][xIndex - 1].contains(5)) {
									cnt5++;
									if (numbersCand[yIndex][xIndex - 1].size() == 1) {
										cntForce5++;
									}
								}
							}
							if (cntForce1 > 1 || cntForce3 > 1 || cntForce5 > 1) {
								return false;
							}
							if (cnt3 == 0) {
								return false;
							}
							if (cntForce3 == 0 && cnt3 == 1) {
								if (yIndex != 0 && numbersCand[yIndex - 1][xIndex].contains(3)) {
									numbersCand[yIndex - 1][xIndex].clear();
									numbersCand[yIndex - 1][xIndex].add(3);
								} else if (xIndex != getXLength() - 1
										&& numbersCand[yIndex][xIndex + 1].contains(3)) {
									numbersCand[yIndex][xIndex + 1].clear();
									numbersCand[yIndex][xIndex + 1].add(3);
								} else if (yIndex != getYLength() - 1
										&& numbersCand[yIndex + 1][xIndex].contains(3)) {
									numbersCand[yIndex + 1][xIndex].clear();
									numbersCand[yIndex + 1][xIndex].add(3);
								} else if (xIndex != 0 && numbersCand[yIndex][xIndex - 1].contains(3)) {
									numbersCand[yIndex][xIndex - 1].clear();
									numbersCand[yIndex][xIndex - 1].add(3);
								}
							}
							if (number == 2) {
								if (cnt1 == 0) {
									return false;
								}
								if (cntForce1 == 0 && cnt1 == 1) {
									if (yIndex != 0 && numbersCand[yIndex - 1][xIndex].contains(1)) {
										numbersCand[yIndex - 1][xIndex].clear();
										numbersCand[yIndex - 1][xIndex].add(1);
									} else if (xIndex != getXLength() - 1
											&& numbersCand[yIndex][xIndex + 1].contains(1)) {
										numbersCand[yIndex][xIndex + 1].clear();
										numbersCand[yIndex][xIndex + 1].add(1);
									} else if (yIndex != getYLength() - 1
											&& numbersCand[yIndex + 1][xIndex].contains(1)) {
										numbersCand[yIndex + 1][xIndex].clear();
										numbersCand[yIndex + 1][xIndex].add(1);
									} else if (xIndex != 0 && numbersCand[yIndex][xIndex - 1].contains(1)) {
										numbersCand[yIndex][xIndex - 1].clear();
										numbersCand[yIndex][xIndex - 1].add(1);
									}
								}
							} else if (number == 4) {
								if (cnt5 == 0) {
									return false;
								}
								if (cntForce5 == 0 && cnt5 == 1) {
									if (yIndex != 0 && numbersCand[yIndex - 1][xIndex].contains(5)) {
										numbersCand[yIndex - 1][xIndex].clear();
										numbersCand[yIndex - 1][xIndex].add(5);
									} else if (xIndex != getXLength() - 1
											&& numbersCand[yIndex][xIndex + 1].contains(5)) {
										numbersCand[yIndex][xIndex + 1].clear();
										numbersCand[yIndex][xIndex + 1].add(5);
									} else if (yIndex != getYLength() - 1
											&& numbersCand[yIndex + 1][xIndex].contains(5)) {
										numbersCand[yIndex + 1][xIndex].clear();
										numbersCand[yIndex + 1][xIndex].add(5);
									} else if (xIndex != 0 && numbersCand[yIndex][xIndex - 1].contains(5)) {
										numbersCand[yIndex][xIndex - 1].clear();
										numbersCand[yIndex][xIndex - 1].add(5);
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
		 * 14(25)が隣り合っているときは、隣に23(34)を入れて2x2の正方形にしなければならない。
		 * (そうしないとヘビが辺を共有してしまうため。)
		 * 矛盾したらfalseを返す。
		 */
		private boolean snakeSolve2() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						if (numbersCand[yIndex][xIndex].get(0) == 1) {
							if (yIndex != 0
									&& numbersCand[yIndex - 1][xIndex].size() == 1
									&& numbersCand[yIndex - 1][xIndex].get(0) == 4) {
								// 上4元1
								boolean canRight = false;
								boolean canLeft = false;
								if (xIndex != getXLength() - 1
										&& numbersCand[yIndex - 1][xIndex + 1].contains(3)
										&& numbersCand[yIndex][xIndex + 1].contains(2)) {
									canRight = true;
								}
								if (xIndex != 0
										&& numbersCand[yIndex - 1][xIndex - 1].contains(3)
										&& numbersCand[yIndex][xIndex - 1].contains(2)) {
									canLeft = true;
								}
								if (!canRight && !canLeft) {
									return false;
								} else if (canRight && !canLeft) {
									numbersCand[yIndex - 1][xIndex + 1].clear();
									numbersCand[yIndex - 1][xIndex + 1].add(3);
									numbersCand[yIndex][xIndex + 1].clear();
									numbersCand[yIndex][xIndex + 1].add(2);
								} else if (!canRight && canLeft) {
									numbersCand[yIndex - 1][xIndex - 1].clear();
									numbersCand[yIndex - 1][xIndex - 1].add(3);
									numbersCand[yIndex][xIndex - 1].clear();
									numbersCand[yIndex][xIndex - 1].add(2);
								}
							} else if (xIndex != getXLength() - 1
									&& numbersCand[yIndex][xIndex + 1].size() == 1
									&& numbersCand[yIndex][xIndex + 1].get(0) == 4) {
								// 右4元1
								boolean canUp = false;
								boolean canDown = false;
								if (yIndex != 0
										&& numbersCand[yIndex - 1][xIndex].contains(2)
										&& numbersCand[yIndex - 1][xIndex + 1].contains(3)) {
									canUp = true;
								}
								if (yIndex != getYLength() - 1
										&& numbersCand[yIndex + 1][xIndex].contains(2)
										&& numbersCand[yIndex + 1][xIndex + 1].contains(3)) {
									canDown = true;
								}
								if (!canUp && !canDown) {
									return false;
								} else if (canUp && !canDown) {
									numbersCand[yIndex - 1][xIndex].clear();
									numbersCand[yIndex - 1][xIndex].add(2);
									numbersCand[yIndex - 1][xIndex + 1].clear();
									numbersCand[yIndex - 1][xIndex + 1].add(3);
								} else if (!canUp && canDown) {
									numbersCand[yIndex + 1][xIndex].clear();
									numbersCand[yIndex + 1][xIndex].add(2);
									numbersCand[yIndex + 1][xIndex + 1].clear();
									numbersCand[yIndex + 1][xIndex + 1].add(3);
								}
							} else if (yIndex != getYLength() - 1
									&& numbersCand[yIndex + 1][xIndex].size() == 1
									&& numbersCand[yIndex + 1][xIndex].get(0) == 4) {
								// 下4元1
								boolean canRight = false;
								boolean canLeft = false;
								if (xIndex != getXLength() - 1
										&& numbersCand[yIndex][xIndex + 1].contains(2)
										&& numbersCand[yIndex + 1][xIndex + 1].contains(3)) {
									canRight = true;
								}
								if (xIndex != 0
										&& numbersCand[yIndex][xIndex - 1].contains(2)
										&& numbersCand[yIndex + 1][xIndex - 1].contains(3)) {
									canLeft = true;
								}
								if (!canRight && !canLeft) {
									return false;
								} else if (canRight && !canLeft) {
									numbersCand[yIndex][xIndex + 1].clear();
									numbersCand[yIndex][xIndex + 1].add(2);
									numbersCand[yIndex + 1][xIndex + 1].clear();
									numbersCand[yIndex + 1][xIndex + 1].add(3);
								} else if (!canRight && canLeft) {
									numbersCand[yIndex][xIndex - 1].clear();
									numbersCand[yIndex][xIndex - 1].add(2);
									numbersCand[yIndex + 1][xIndex - 1].clear();
									numbersCand[yIndex + 1][xIndex - 1].add(3);
								}
							} else if (xIndex != 0
									&& numbersCand[yIndex][xIndex - 1].size() == 1
									&& numbersCand[yIndex][xIndex - 1].get(0) == 4) {
								// 左4元1
								boolean canUp = false;
								boolean canDown = false;
								if (yIndex != 0
										&& numbersCand[yIndex - 1][xIndex - 1].contains(3)
										&& numbersCand[yIndex - 1][xIndex].contains(2)) {
									canUp = true;
								}
								if (yIndex != getYLength() - 1
										&& numbersCand[yIndex + 1][xIndex - 1].contains(3)
										&& numbersCand[yIndex + 1][xIndex].contains(2)) {
									canDown = true;
								}
								if (!canUp && !canDown) {
									return false;
								} else if (canUp && !canDown) {
									numbersCand[yIndex - 1][xIndex - 1].clear();
									numbersCand[yIndex - 1][xIndex - 1].add(3);
									numbersCand[yIndex - 1][xIndex].clear();
									numbersCand[yIndex - 1][xIndex].add(2);
								} else if (!canUp && canDown) {
									numbersCand[yIndex + 1][xIndex - 1].clear();
									numbersCand[yIndex + 1][xIndex - 1].add(3);
									numbersCand[yIndex + 1][xIndex].clear();
									numbersCand[yIndex + 1][xIndex].add(2);
								}
							}
						} else if (numbersCand[yIndex][xIndex].get(0) == 2) {
							if (yIndex != 0
									&& numbersCand[yIndex - 1][xIndex].size() == 1
									&& numbersCand[yIndex - 1][xIndex].get(0) == 5) {
								// 上5元2
								boolean canRight = false;
								boolean canLeft = false;
								if (xIndex != getXLength() - 1
										&& numbersCand[yIndex - 1][xIndex + 1].contains(4)
										&& numbersCand[yIndex][xIndex + 1].contains(3)) {
									canRight = true;
								}
								if (xIndex != 0
										&& numbersCand[yIndex - 1][xIndex - 1].contains(4)
										&& numbersCand[yIndex][xIndex - 1].contains(3)) {
									canLeft = true;
								}
								if (!canRight && !canLeft) {
									return false;
								} else if (canRight && !canLeft) {
									numbersCand[yIndex - 1][xIndex + 1].clear();
									numbersCand[yIndex - 1][xIndex + 1].add(4);
									numbersCand[yIndex][xIndex + 1].clear();
									numbersCand[yIndex][xIndex + 1].add(3);
								} else if (!canRight && canLeft) {
									numbersCand[yIndex - 1][xIndex - 1].clear();
									numbersCand[yIndex - 1][xIndex - 1].add(4);
									numbersCand[yIndex][xIndex - 1].clear();
									numbersCand[yIndex][xIndex - 1].add(3);
								}
							} else if (xIndex != getXLength() - 1
									&& numbersCand[yIndex][xIndex + 1].size() == 1
									&& numbersCand[yIndex][xIndex + 1].get(0) == 5) {
								// 右5元2
								boolean canUp = false;
								boolean canDown = false;
								if (yIndex != 0
										&& numbersCand[yIndex - 1][xIndex].contains(3)
										&& numbersCand[yIndex - 1][xIndex + 1].contains(4)) {
									canUp = true;
								}
								if (yIndex != getYLength() - 1
										&& numbersCand[yIndex + 1][xIndex].contains(3)
										&& numbersCand[yIndex + 1][xIndex + 1].contains(4)) {
									canDown = true;
								}
								if (!canUp && !canDown) {
									return false;
								} else if (canUp && !canDown) {
									numbersCand[yIndex - 1][xIndex].clear();
									numbersCand[yIndex - 1][xIndex].add(3);
									numbersCand[yIndex - 1][xIndex + 1].clear();
									numbersCand[yIndex - 1][xIndex + 1].add(4);
								} else if (!canUp && canDown) {
									numbersCand[yIndex + 1][xIndex].clear();
									numbersCand[yIndex + 1][xIndex].add(3);
									numbersCand[yIndex + 1][xIndex + 1].clear();
									numbersCand[yIndex + 1][xIndex + 1].add(4);
								}
							} else if (yIndex != getYLength() - 1
									&& numbersCand[yIndex + 1][xIndex].size() == 1
									&& numbersCand[yIndex + 1][xIndex].get(0) == 5) {
								// 下5元2
								boolean canRight = false;
								boolean canLeft = false;
								if (xIndex != getXLength() - 1
										&& numbersCand[yIndex][xIndex + 1].contains(3)
										&& numbersCand[yIndex + 1][xIndex + 1].contains(4)) {
									canRight = true;
								}
								if (xIndex != 0 &&
										numbersCand[yIndex][xIndex - 1].contains(3)
										&& numbersCand[yIndex + 1][xIndex - 1].contains(4)) {
									canLeft = true;
								}
								if (!canRight && !canLeft) {
									return false;
								} else if (canRight && !canLeft) {
									numbersCand[yIndex][xIndex + 1].clear();
									numbersCand[yIndex][xIndex + 1].add(3);
									numbersCand[yIndex + 1][xIndex + 1].clear();
									numbersCand[yIndex + 1][xIndex + 1].add(4);
								} else if (!canRight && canLeft) {
									numbersCand[yIndex][xIndex - 1].clear();
									numbersCand[yIndex][xIndex - 1].add(3);
									numbersCand[yIndex + 1][xIndex - 1].clear();
									numbersCand[yIndex + 1][xIndex - 1].add(4);
								}
							} else if (xIndex != 0
									&& numbersCand[yIndex][xIndex - 1].size() == 1
									&& numbersCand[yIndex][xIndex - 1].get(0) == 5) {
								// 左5元2
								boolean canUp = false;
								boolean canDown = false;
								if (yIndex != 0
										&& numbersCand[yIndex - 1][xIndex - 1].contains(4)
										&& numbersCand[yIndex - 1][xIndex].contains(3)) {
									canUp = true;
								}
								if (yIndex != getYLength() - 1
										&& numbersCand[yIndex + 1][xIndex - 1].contains(4)
										&& numbersCand[yIndex + 1][xIndex].contains(3)) {
									canDown = true;
								}
								if (!canUp && !canDown) {
									return false;
								} else if (canUp && !canDown) {
									numbersCand[yIndex - 1][xIndex - 1].clear();
									numbersCand[yIndex - 1][xIndex - 1].add(4);
									numbersCand[yIndex - 1][xIndex].clear();
									numbersCand[yIndex - 1][xIndex].add(3);
								} else if (!canUp && canDown) {
									numbersCand[yIndex + 1][xIndex - 1].clear();
									numbersCand[yIndex + 1][xIndex - 1].add(4);
									numbersCand[yIndex + 1][xIndex].clear();
									numbersCand[yIndex + 1][xIndex].add(3);
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
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!arrowSolve()) {
				return false;
			}
			if (!headSolve()) {
				return false;
			}
			if (!snakeSolve()) {
				return false;
			}
			if (!snakeSolve2()) {
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
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public HebiSolver(int height, int width, String param) {
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
		System.out.println(new HebiSolver(height, width, param).solve());
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
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
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
		System.out.println(field);
		// へびいちごはどうやらここでの再帰はしない方がはやい
		//		while (true) {
		//			String befStr = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.numbersCand[yIndex][xIndex].size() != 1) {
					count++;
					for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.numbersCand[yIndex][xIndex].clear();
						virtual.numbersCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.numbersCand[yIndex][xIndex].size() == 0) {
						return false;
					}
				}
			}
		}
		//			if (field.getStateDump().equals(befStr)) {
		//				break;
		//			}
		//		}
		return true;
	}
}