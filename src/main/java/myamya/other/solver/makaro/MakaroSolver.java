package myamya.other.solver.makaro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class MakaroSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 矢印情報
		protected final Direction[][] arrows;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Direction[][] getArrows() {
			return arrows;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			arrows = new Direction[height][width];
			numbersCand = new ArrayList[height][width];
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
			// 部屋の大きさにより、初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (Set<Position> room : rooms) {
						if (room.contains(new Position(yIndex, xIndex))) {
							for (int number = 0; number < room.size(); number++) {
								numbersCand[yIndex][xIndex].add(number + 1);
							}
							break;
						}
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
					//11 - 99は '-'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2)) + 1;
							Position pos = new Position(index / getXLength(), index % getXLength());
							numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
							numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
							if (num > 9) {
								num = Integer.parseInt(String.valueOf(ch), 16) - 10;
								Position pos = new Position(index / getXLength(), index % getXLength());
								numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
								numbersCand[pos.getyIndex()][pos.getxIndex()].add(-1);
								arrows[pos.getyIndex()][pos.getxIndex()] = Direction.getByNum(num);
							} else {
								num = Integer.parseInt(String.valueOf(ch), 16) + 1;
								Position pos = new Position(index / getXLength(), index % getXLength());
								numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
								numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
							}
						}
					}
					index++;
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
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						if (numbersCand[yIndex][xIndex].get(0) == -1) {
							if (arrows[yIndex][xIndex] == null) {
								sb.append("■");
							} else {
								sb.append(arrows[yIndex][xIndex].getDirectString());
							}
						} else {
							String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
							int index = HALF_NUMS.indexOf(numStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(numStr);
							}
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
					} else {
						sb.append("　");
					}
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		int cnt = 0;

		/**
		 * 同じ部屋にいる数字を候補から除外する。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				for (Position pos : room) {
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
						cnt++;
						for (Position sameRoomPos : room) {
							if (!sameRoomPos.equals(pos)) {
								numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()]
										.remove(numbersCand[pos.getyIndex()][pos.getxIndex()].get(0));
							}
							if (numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()].size() == 0) {
								return false;
							}
						}
					} else {
						for (int cand : numbersCand[pos.getyIndex()][pos.getxIndex()]) {
							boolean isHiddenSingle = true;
							for (Position sameRoomPos : room) {
								if (!sameRoomPos.equals(pos)) {
									if (numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()].contains(cand)) {
										isHiddenSingle = false;
										break;
									}
								}
							}
							if (isHiddenSingle) {
								numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
								numbersCand[pos.getyIndex()][pos.getxIndex()].add(cand);
								break;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 矢印がある場合、矢印の向き先は矢印が向いていない3マスよりも大きくなる。
		 * 矛盾する場合falseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						cnt++;
						List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
						List<Integer> rightCands = xIndex == getXLength() - 1 ? null
								: numbersCand[yIndex][xIndex + 1];
						List<Integer> downCands = yIndex == getYLength() - 1 ? null
								: numbersCand[yIndex + 1][xIndex];
						List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];
						if (arrows[yIndex][xIndex] == Direction.UP) {
							int minOther = Collections.max(Arrays.asList(new Integer[] {
									rightCands == null ? -1 : Collections.min(rightCands),
									downCands == null ? -1 : Collections.min(downCands),
									leftCands == null ? -1 : Collections.min(leftCands)
							}));
							for (Iterator<Integer> iterator = upCands.iterator(); iterator.hasNext();) {
								int number = iterator.next();
								if (number <= minOther) {
									iterator.remove();
								}
							}
							if (upCands.isEmpty()) {
								return false;
							}
							int maxUp = upCands == null ? -1 : Collections.max(upCands);
							if (rightCands != null) {
								for (Iterator<Integer> iterator = rightCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxUp) {
										iterator.remove();
									}
								}
								if (rightCands.isEmpty()) {
									return false;
								}
							}
							if (downCands != null) {
								for (Iterator<Integer> iterator = downCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxUp) {
										iterator.remove();
									}
								}
								if (downCands.isEmpty()) {
									return false;
								}
							}
							if (leftCands != null) {
								for (Iterator<Integer> iterator = leftCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxUp) {
										iterator.remove();
									}
								}
								if (leftCands.isEmpty()) {
									return false;
								}
							}
						} else if (arrows[yIndex][xIndex] == Direction.RIGHT) {
							int minOther = Collections.max(Arrays.asList(new Integer[] {
									upCands == null ? -1 : Collections.min(upCands),
									downCands == null ? -1 : Collections.min(downCands),
									leftCands == null ? -1 : Collections.min(leftCands)
							}));
							for (Iterator<Integer> iterator = rightCands.iterator(); iterator.hasNext();) {
								int number = iterator.next();
								if (number <= minOther) {
									iterator.remove();
								}
							}
							if (rightCands.isEmpty()) {
								return false;
							}
							int maxRight = rightCands == null ? -1 : Collections.max(rightCands);
							if (upCands != null) {
								for (Iterator<Integer> iterator = upCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxRight) {
										iterator.remove();
									}
								}
								if (upCands.isEmpty()) {
									return false;
								}
							}
							if (downCands != null) {
								for (Iterator<Integer> iterator = downCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxRight) {
										iterator.remove();
									}
								}
								if (downCands.isEmpty()) {
									return false;
								}
							}
							if (leftCands != null) {
								for (Iterator<Integer> iterator = leftCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxRight) {
										iterator.remove();
									}
								}
								if (leftCands.isEmpty()) {
									return false;
								}
							}
						} else if (arrows[yIndex][xIndex] == Direction.DOWN) {
							int minOther = Collections.max(Arrays.asList(new Integer[] {
									upCands == null ? -1 : Collections.min(upCands),
									rightCands == null ? -1 : Collections.min(rightCands),
									leftCands == null ? -1 : Collections.min(leftCands)
							}));
							for (Iterator<Integer> iterator = downCands.iterator(); iterator.hasNext();) {
								int number = iterator.next();
								if (number <= minOther) {
									iterator.remove();
								}
							}
							if (downCands.isEmpty()) {
								return false;
							}
							int maxDown = downCands == null ? -1 : Collections.max(downCands);
							if (upCands != null) {
								for (Iterator<Integer> iterator = upCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxDown) {
										iterator.remove();
									}
								}
								if (upCands.isEmpty()) {
									return false;
								}
							}
							if (rightCands != null) {
								for (Iterator<Integer> iterator = rightCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxDown) {
										iterator.remove();
									}
								}
								if (rightCands.isEmpty()) {
									return false;
								}
							}
							if (leftCands != null) {
								for (Iterator<Integer> iterator = leftCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxDown) {
										iterator.remove();
									}
								}
								if (leftCands.isEmpty()) {
									return false;
								}
							}
						} else if (arrows[yIndex][xIndex] == Direction.LEFT) {
							int minOther = Collections.max(Arrays.asList(new Integer[] {
									upCands == null ? -1 : Collections.min(upCands),
									rightCands == null ? -1 : Collections.min(rightCands),
									downCands == null ? -1 : Collections.min(downCands)
							}));
							for (Iterator<Integer> iterator = leftCands.iterator(); iterator.hasNext();) {
								int number = iterator.next();
								if (number <= minOther) {
									iterator.remove();
								}
							}
							if (leftCands.isEmpty()) {
								return false;
							}
							int maxLeft = leftCands == null ? -1 : Collections.max(leftCands);
							if (upCands != null) {
								for (Iterator<Integer> iterator = upCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxLeft) {
										iterator.remove();
									}
								}
								if (upCands.isEmpty()) {
									return false;
								}
							}
							if (rightCands != null) {
								for (Iterator<Integer> iterator = rightCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxLeft) {
										iterator.remove();
									}
								}
								if (rightCands.isEmpty()) {
									return false;
								}
							}
							if (downCands != null) {
								for (Iterator<Integer> iterator = downCands.iterator(); iterator.hasNext();) {
									int number = iterator.next();
									if (number >= maxLeft) {
										iterator.remove();
									}
								}
								if (downCands.isEmpty()) {
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 同じ数字は隣り合わない。違反する場合falseを返す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						cnt++;
						Integer serveyNum = numbersCand[yIndex][xIndex].get(0);
						if (serveyNum == -1) {
							continue;
						}
						List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
						List<Integer> rightCands = xIndex == getXLength() - 1 ? null
								: numbersCand[yIndex][xIndex + 1];
						List<Integer> downCands = yIndex == getYLength() - 1 ? null
								: numbersCand[yIndex + 1][xIndex];
						List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];
						if (upCands != null) {
							upCands.remove(serveyNum);
							if (upCands.isEmpty()) {
								return false;
							}
						}
						if (rightCands != null) {
							rightCands.remove(serveyNum);
							if (rightCands.isEmpty()) {
								return false;
							}
						}
						if (downCands != null) {
							downCands.remove(serveyNum);
							if (downCands.isEmpty()) {
								return false;
							}
						}
						if (leftCands != null) {
							leftCands.remove(serveyNum);
							if (leftCands.isEmpty()) {
								return false;
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
			if (!roomSolve()) {
				return false;
			}
			if (!arrowSolve()) {
				return false;
			}
			if (!nextSolve()) {
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

	public MakaroSolver(int height, int width, String param) {
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
		System.out.println(new MakaroSolver(height, width, param).solve());
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
			count = count + field.cnt;
			field.cnt = 0;
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
		while (true) {
			String befStr = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							int oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							count = count + virtual.cnt;
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
			if (field.getStateDump().equals(befStr)) {
				break;
			}
		}
		return true;
	}
}