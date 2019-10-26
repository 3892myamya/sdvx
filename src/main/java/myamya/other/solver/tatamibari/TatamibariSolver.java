package myamya.other.solver.tatamibari;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Solver;

public class TatamibariSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 記号の情報。1が縦、2が横、3が十字。
		private final Integer[][] numbers;
		// 記号ごとの切り分け方の候補
		private final List<Sikaku>[][] roomCand;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public List<Sikaku>[][] getRoomCand() {
			return roomCand;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			roomCand = new ArrayList[height][width];
			int readPos = 0;
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
					if (ch == '.') {
						//
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
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 部屋の切り方の候補をあらかじめ決めておき、その候補を順次減らす方法を取る。
			// 部屋の切り方の候補はそう多くはならないので。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						List<Sikaku> sikakuList = new ArrayList<>();
						for (int ySize = 1; ySize <= getYLength(); ySize++) {
							for (int xSize = 1; xSize <= getXLength(); xSize++) {
								if (numbers[yIndex][xIndex] == 1) {
									// 縦長限定
									if (ySize <= xSize) {
										continue;
									}
								} else if (numbers[yIndex][xIndex] == 2) {
									// 横長限定
									if (xSize <= ySize) {
										continue;
									}
								} else if (numbers[yIndex][xIndex] == 3) {
									// 正方形限定
									if (xSize != ySize) {
										continue;
									}
								}
								int minY = yIndex - ySize + 1 < 0 ? 0 : yIndex - ySize + 1;
								int maxY = yIndex + ySize > getYLength() ? getYLength() - ySize : yIndex;
								int minX = xIndex - xSize + 1 < 0 ? 0 : xIndex - xSize + 1;
								int maxX = xIndex + xSize > getXLength() ? getXLength() - xSize : xIndex;

								for (int y = minY; y <= maxY; y++) {
									for (int x = minX; x <= maxX; x++) {
										Sikaku sikaku = new Sikaku(new Position(y, x),
												new Position(y + ySize - 1, x + xSize - 1));
										boolean addSikaku = true;
										// 他の部屋のpivotが含まれる候補をあらかじめ除外する。
										outer: for (int otherY = 0; otherY < getYLength(); otherY++) {
											for (int otherX = 0; otherX < getXLength(); otherX++) {
												if (numbers[otherY][otherX] != null
														&& (yIndex != otherY || xIndex != otherX)) {
													Position otherPos = new Position(otherY, otherX);
													if (sikaku.isDuplicate(otherPos)) {
														addSikaku = false;
														break outer;
													}
												}
											}
										}
										if (addSikaku) {
											sikakuList.add(sikaku);
										}
									}
								}
							}
						}
						roomCand[yIndex][xIndex] = sikakuList;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			roomCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.roomCand[yIndex][xIndex] != null) {
						roomCand[yIndex][xIndex] = new ArrayList<>(other.roomCand[yIndex][xIndex]);
					}
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
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						sb.append(existYokoWall(yIndex, xIndex) ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(existTateWall(yIndex, xIndex) ? "□" : "　");
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

		public boolean existTateWall(int yIndex, int xIndex) {
			for (int y = 0; y < getYLength(); y++) {
				for (int x = 0; x < getXLength(); x++) {
					if (roomCand[y][x] != null && roomCand[y][x].size() == 1) {
						if (roomCand[y][x].get(0).getLeftUp().getxIndex() <= xIndex
								&& roomCand[y][x].get(0).getRightDown().getxIndex() >= xIndex) {
							if (roomCand[y][x].get(0).getLeftUp().getyIndex() - 1 == yIndex
									|| roomCand[y][x].get(0).getRightDown().getyIndex() == yIndex) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		public boolean existYokoWall(int yIndex, int xIndex) {
			for (int y = 0; y < getYLength(); y++) {
				for (int x = 0; x < getXLength(); x++) {
					if (roomCand[y][x] != null && roomCand[y][x].size() == 1) {
						if (roomCand[y][x].get(0).getLeftUp().getyIndex() <= yIndex
								&& roomCand[y][x].get(0).getRightDown().getyIndex() >= yIndex) {
							if (roomCand[y][x].get(0).getLeftUp().getxIndex() - 1 == xIndex
									|| roomCand[y][x].get(0).getRightDown().getxIndex() == xIndex) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null) {
						sb.append(roomCand[yIndex][xIndex].size());
					}
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
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!allSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null) {
						if (roomCand[yIndex][xIndex].size() != 1) {
							return false;
						}
					}
				}
			}
			return solveAndCheck();
		}

		/**
		 * 各部屋の他の部屋とかぶったり、4つ角ができる候補を消す。
		 * 候補の数が0になってしまったらfalseを返す。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null && roomCand[yIndex][xIndex].size() == 1) {
						Sikaku mySikaku = roomCand[yIndex][xIndex].get(0);
						for (int otherY = 0; otherY < getYLength(); otherY++) {
							for (int otherX = 0; otherX < getXLength(); otherX++) {
								if (roomCand[otherY][otherX] != null
										&& (yIndex != otherY || xIndex != otherX)) {
									for (Iterator<Sikaku> iterator = roomCand[otherY][otherX].iterator(); iterator
											.hasNext();) {
										Sikaku anotherSikaku = iterator.next();
										if (mySikaku.isDuplicate(anotherSikaku)
												||
												new Position(mySikaku.getLeftUp().getyIndex() - 1,
														mySikaku.getLeftUp().getxIndex() - 1)
																.equals(anotherSikaku.getRightDown())
												||
												new Position(mySikaku.getRightDown().getyIndex() + 1,
														mySikaku.getRightDown().getxIndex() + 1)
																.equals(anotherSikaku.getLeftUp())) {
											iterator.remove();
										}
									}
									if (roomCand[otherY][otherX].isEmpty()) {
										return false;
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
		 * 畳は全ての部屋を回収しなければならない。
		 */
		private boolean allSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isOK = false;
					outer: for (int y = 0; y < getYLength(); y++) {
						for (int x = 0; x < getXLength(); x++) {
							if (roomCand[y][x] != null) {
								for (Sikaku sikaku : roomCand[y][x]) {
									if (sikaku.isDuplicate(pos)) {
										isOK = true;
										break outer;
									}
								}
							}
						}
					}
					if (!isOK) {
						return false;
					}
				}
			}
			return true;
		}
	}

	private final Field field;
	private int count = 0;

	public TatamibariSolver(int height, int width, String param) {
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
		System.out.println(new TatamibariSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.roomCand[yIndex][xIndex] != null) {
					if (field.roomCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Sikaku> iterator = field.roomCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							Sikaku oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.roomCand[yIndex][xIndex].clear();
							virtual.roomCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (field.roomCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (field.roomCand[yIndex][xIndex].size() == 1) {
						if (!field.solveAndCheck()) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
