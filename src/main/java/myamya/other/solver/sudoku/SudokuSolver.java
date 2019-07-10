package myamya.other.solver.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class SudokuSolver implements Solver {

	public static class SudokuGenerator {

		enum HintPattern {
			NONE(81) {
				@Override
				Set<Position> getPosSet(int num) {
					Set<Position> result = new HashSet<>();
					int yIndex = num / 9;
					int xIndex = num % 9;
					result.add(new Position(yIndex, xIndex));
					return result;
				}
			},
			POINT(41) {
				@Override
				Set<Position> getPosSet(int num) {
					Set<Position> result = new HashSet<>();
					int yIndex = num / 9;
					int xIndex = num % 9;
					result.add(new Position(yIndex, xIndex));
					result.add(new Position(8 - yIndex, 8 - xIndex));
					return result;
				}
			},
			HORIZONAL(45) {
				@Override
				Set<Position> getPosSet(int num) {
					Set<Position> result = new HashSet<>();
					int yIndex = num / 5;
					int xIndex = num % 5;
					result.add(new Position(yIndex, xIndex));
					result.add(new Position(yIndex, 8 - xIndex));
					return result;
				}
			},
			VARTICAL(45) {
				@Override
				Set<Position> getPosSet(int num) {
					Set<Position> result = new HashSet<>();
					int yIndex = num % 5;
					int xIndex = num / 5;
					result.add(new Position(yIndex, xIndex));
					result.add(new Position(8 - yIndex, xIndex));
					return result;
				}
			},
			DESC_SLASH(45) {
				@Override
				Set<Position> getPosSet(int num) {
					// TODO 自動生成されたメソッド・スタブ
					return null;
				}
			},
			ASC_SLASH(45) {
				@Override
				Set<Position> getPosSet(int num) {
					// TODO 自動生成されたメソッド・スタブ
					return null;
				}
			},
			ALL(25) {
				@Override
				Set<Position> getPosSet(int num) {
					Set<Position> result = new HashSet<>();
					int yIndex = num / 5;
					int xIndex = num % 5;
					result.add(new Position(xIndex, yIndex));
					result.add(new Position(8 - xIndex, yIndex));
					result.add(new Position(xIndex, 8 - yIndex));
					result.add(new Position(8 - xIndex, 8 - yIndex));
					return result;
				}
			};
			abstract Set<Position> getPosSet(int num);

			int count;

			HintPattern(int count) {
				this.count = count;
			}

			public int getCount() {
				return count;
			}

		}

		public static void main(String[] args) {
			new SudokuGenerator().generate(HintPattern.POINT);
		}

		private void generate(HintPattern hintPattern) {
			SudokuSolver.Field field = new SudokuSolver.Field(9, 9);
			int index = 0;
			long start = System.nanoTime();
			while (!field.isSolved()) {
				System.out.println(field);
				int yIndex = index / 9;
				int xIndex = index % 9;
				if (field.numbersCand[yIndex][xIndex].size() != 0) {
					int numIdx = (int) (Math.random() * field.numbersCand[yIndex][xIndex].size());
					SudokuSolver.Field virtual = new SudokuSolver.Field(field);
					virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
					virtual.numbersCand[yIndex][xIndex].add(field.numbersCand[yIndex][xIndex].get(numIdx));
					if (!virtual.solveAndCheck()) {
						// 破綻したら0から作り直す。
						field = new SudokuSolver.Field(9, 9);
						index = 0;
						continue;
					} else {
						field.numbersCand = virtual.numbersCand;
					}
				}
				index++;
			}
			List<Integer> numIdxList = new ArrayList<>();
			for (int i = 0; i < hintPattern.getCount(); i++) {
				numIdxList.add(i);
			}
			Collections.shuffle(numIdxList);
			for (Integer numIdx : numIdxList) {
				SudokuSolver.Field virtual = new SudokuSolver.Field(field);
				for (Position pos : hintPattern.getPosSet(numIdx)) {
					virtual.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
					for (int number = 0; number < field.getYLength(); number++) {
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number + 1);
					}
				}
				if (new SudokuSolver(virtual) {
					@Override
					protected boolean candSolve(Field field, int recursive) {
						if (this.count >= 2000) {
							return false;
						} else {
							return super.candSolve(field, recursive);
						}
					}
				}.solve().contains("解けました")) {
					for (Position pos : hintPattern.getPosSet(numIdx)) {
						field.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						for (int number = 0; number < field.getYLength(); number++) {
							field.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number + 1);
						}
					}
				}
			}
			new SudokuSolver(field).solve();
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(field);
			System.out.println("■ボナペティ！■");
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		private List<Integer>[][] numbersCand;
		// 1部屋の高さ
		private final int roomHeight;
		// 1部屋の幅
		private final int roomWidth;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		/**
		 * プレーンな盤面生成
		 */
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number < getYLength(); number++) {
						numbersCand[yIndex][xIndex].add(number + 1);
					}
				}
			}
			roomHeight = getYLength() == 4 ? 2
					: getYLength() == 6 ? 2
							: getYLength() == 9 ? 3 : getYLength() == 16 ? 4 : getYLength() == 25 ? 5 : 0;
			roomWidth = getXLength() == 4 ? 2
					: getXLength() == 6 ? 3
							: getXLength() == 9 ? 3 : getXLength() == 16 ? 4 : getXLength() == 25 ? 5 : 0;
		}

		public Field(int height, int width, String param) {
			this(height, width);
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
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
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			roomHeight = other.roomHeight;
			roomWidth = other.roomWidth;
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
					if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					} else {
						sb.append("　");
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
		 * 縦・横・同じ部屋の数字を除外する。
		 * 数字が入れられないマスができてしまったらfalseを返す。
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							if (numbersCand[targetY][xIndex].size() == 1 && yIndex != targetY) {
								numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0));
							}
						}
						for (int targetX = 0; targetX < getXLength(); targetX++) {
							if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
								numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0));
							}
						}
						for (int targetY = yIndex / roomHeight
								* roomHeight; targetY < (yIndex / roomHeight * roomHeight)
										+ roomHeight; targetY++) {
							for (int targetX = xIndex / roomWidth
									* roomWidth; targetX < (xIndex / roomWidth * roomWidth)
											+ roomWidth; targetX++) {
								if ((numbersCand[targetY][targetX].size() == 1)
										&& (xIndex != targetX || yIndex != targetY)) {
									numbersCand[yIndex][xIndex].remove((numbersCand[targetY][targetX]).get(0));
								}
							}
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
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
	protected int count;

	public SudokuSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public SudokuSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?sudoku/9/9/g6g4j391g5i4g7o8i159k2k428i1o9g5i8g366j9g2g"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new SudokuSolver(height, width, param).solve());
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
	 */
	protected boolean candSolve(Field field, int recursive) {
		// 数字の候補が2のマスから優先して、候補数5のマスまで調べる。
		// 数字の候補が6以上のマスの調査を優先してうまくいく問題にはまだあたっていない。
		for (int i = 2; i < 6; i++) {
			String str = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() == i) {
						count++;
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							int oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
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
			// 進展があればiを戻してもう一度調査
			if (!field.getStateDump().equals(str)) {
				i = 1;
			}
		}
		return true;
	}

}