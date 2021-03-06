package myamya.other.solver.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class SudokuSolver implements Solver {

	public static class SudokuGenerator implements Generator {

		private final int height;
		private final int width;
		private final HintPattern hintPattern;

		public SudokuGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new SudokuGenerator(4, 4, HintPattern.getByVal(7, 4, 4)).generate();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public GeneratorResult generate() {
			SudokuSolver.ExtendedField wkField = new SudokuSolver.ExtendedField(height, width);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				while (!wkField.isSolved()) {
					int yIndex = index / width;
					int xIndex = index % width;
					if (wkField.numbersCand[yIndex][xIndex].size() != 0) {
						int numIdx = (int) (Math.random() * wkField.numbersCand[yIndex][xIndex].size());
						SudokuSolver.ExtendedField virtual = new SudokuSolver.ExtendedField(wkField);
						virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
						virtual.numbersCand[yIndex][xIndex].add(wkField.numbersCand[yIndex][xIndex].get(numIdx));
						if (!virtual.solveAndCheck()) {
							// 破綻したら0から作り直す。
							wkField = new SudokuSolver.ExtendedField(height, width);
							index = 0;
							continue;
						} else {
							wkField.numbersCand = virtual.numbersCand;
						}
					}
					index++;
				}
				List<Set<Position>> numberPosSetList = hintPattern.getPosSetList();
				Collections.shuffle(numberPosSetList);
				boolean isAdvance = false;
				for (Set<Position> numberPosSet : numberPosSetList) {
					SudokuSolver.ExtendedField virtual = new SudokuSolver.ExtendedField(wkField);
					for (Position numberPos : numberPosSet) {
						virtual.numbersCand[numberPos.getyIndex()][numberPos.getxIndex()] = new ArrayList<>();
						for (int number = 0; number < wkField.getYLength(); number++) {
							virtual.numbersCand[numberPos.getyIndex()][numberPos.getxIndex()].add(number + 1);
						}
					}
					int solveResult = new SudokuSolver(virtual) {
						public int solve2() {
							while (!field.isSolved()) {
								String befStr = field.getStateDump();
								if (!field.solveAndCheck()) {
									return -1;
								}
								count = count + field.cnt;
								field.cnt = 0;
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 0)) {
										return -1;
									}
									if (field.getStateDump().equals(befStr)) {
										if (!candSolve(field, 1)) {
											return -1;
										}
										if (field.getStateDump().equals(befStr)) {
											return -1;
										}
									}
								}
							}
							int level = (int) Math.sqrt(count) - 10;
							return (level < 1 ? 1 : level);
						}

						@Override
						protected boolean candSolve(Field field, int recursive) {
							if (this.count >= 100000) {
								return false;
							} else {
								return super.candSolve(field, recursive);
							}
						}
					}.solve2();
					if (solveResult != -1) {
						for (Position numberPos : numberPosSet) {
							wkField.numbersCand[numberPos.getyIndex()][numberPos.getxIndex()] = new ArrayList<>();
							for (int number = 0; number < wkField.getYLength(); number++) {
								wkField.numbersCand[numberPos.getyIndex()][numberPos.getxIndex()].add(number + 1);
							}
						}
						level = solveResult;
						isAdvance = true;
					}
				}
				if (isAdvance) {
					break;
				} else {
					// 数字が減らなければ作りなおし
					wkField = new SudokuSolver.ExtendedField(height, width);
					index = 0;
				}
			}
			SudokuSolver.ExtendedField field = new SudokuSolver.ExtendedField(wkField.numbersCand);
			String status = "Lv:" + level + "の問題を獲得！(ヒント数" + field.getHintCount() + ")";
			String url = field.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
					}

				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| xIndex % field.getRoomWidth() == field.getRoomWidth() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| yIndex % field.getRoomHeight() == field.getRoomHeight() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(field.getHintCount());
			System.out.println(field);
			String txt = field.getTxt();
			return new GeneratorResult(status, sb.toString(), link, url, level, txt);

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[][] getNumbers() {
			return numbers;
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
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
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
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		public Field(List<Integer>[][] numbersCand) {
			numbers = new Integer[numbersCand.length][numbersCand[0].length];
			this.numbersCand = numbersCand;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						numbers[yIndex][xIndex] = numbersCand[yIndex][xIndex].get(0);
					}
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

		boolean updated = false;
		int cnt = 0;

		/**
		 * 縦・横・同じ部屋の数字を除外する。
		 * 数字が入れられないマスができてしまったらfalseを返す。
		 */
		protected boolean solveAndCheck() {
			while (true) {
				updated = false;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (numbersCand[yIndex][xIndex].size() != 1) {
							cnt++;
							for (int targetY = 0; targetY < getYLength(); targetY++) {
								if (numbersCand[targetY][xIndex].size() == 1 && yIndex != targetY) {
									if (numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0))) {
										updated = true;
									}
								}
							}
							for (int targetX = 0; targetX < getXLength(); targetX++) {
								if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
									if (numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0))) {
										updated = true;
									}
								}
							}
							for (int targetY = yIndex / getRoomHeight()
									* getRoomHeight(); targetY < (yIndex / getRoomHeight() * getRoomHeight())
											+ getRoomHeight(); targetY++) {
								for (int targetX = xIndex / getRoomWidth()
										* getRoomWidth(); targetX < (xIndex / getRoomWidth() * getRoomWidth())
												+ getRoomWidth(); targetX++) {
									if ((numbersCand[targetY][targetX].size() == 1)
											&& (xIndex != targetX || yIndex != targetY)) {
										if (numbersCand[yIndex][xIndex]
												.remove((numbersCand[targetY][targetX]).get(0))) {
											updated = true;
										}
									}
								}
							}
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex][xIndex].size() != 1) {
							for (int cand : numbersCand[yIndex][xIndex]) {
								boolean isHiddenSingle = true;
								for (int targetY = 0; targetY < getYLength(); targetY++) {
									if (yIndex != targetY) {
										if (numbersCand[targetY][xIndex].contains(cand)) {
											isHiddenSingle = false;
											break;
										}
									}
								}
								if (isHiddenSingle) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(cand);
									updated = true;
									break;
								}
								isHiddenSingle = true;
								for (int targetX = 0; targetX < getXLength(); targetX++) {
									if (xIndex != targetX) {
										if (numbersCand[yIndex][targetX].contains(cand)) {
											isHiddenSingle = false;
											break;
										}
									}
								}
								if (isHiddenSingle) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(cand);
									updated = true;
									break;
								}
								isHiddenSingle = true;
								outside: for (int targetY = yIndex / getRoomHeight()
										* getRoomHeight(); targetY < (yIndex / getRoomHeight() * getRoomHeight())
												+ getRoomHeight(); targetY++) {
									for (int targetX = xIndex / getRoomWidth()
											* getRoomWidth(); targetX < (xIndex / getRoomWidth() * getRoomWidth())
													+ getRoomWidth(); targetX++) {
										if ((xIndex != targetX || yIndex != targetY)) {
											if (numbersCand[targetY][targetX].contains(cand)) {
												isHiddenSingle = false;
												break outside;
											}
										}
									}
								}
								if (isHiddenSingle) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(cand);
									updated = true;
									break;
								}
							}
						}
					}
				}
				if (!updated) {
					break;
				}
			}
			return true;
		}

		public int getRoomWidth() {
			return 3;
		}

		public int getRoomHeight() {
			return 3;
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

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?sudoku/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						interval++;
					} else {
						if (interval == 0) {
							sb.append(numbers[yIndex][xIndex]);
						} else {
							while (interval > 20) {
								sb.append("z");
								interval = interval - 20;
							}
							sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
							sb.append(numbers[yIndex][xIndex]);
							interval = 0;
						}
					}
				}
			}
			if (interval != 0) {
				while (interval > 20) {
					sb.append("z");
					interval = interval - 20;
				}
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			return sb.toString();
		}

		public String getTxt() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						sb.append(".");
					} else {
						sb.append(numbers[yIndex][xIndex]);
					}
				}
			}
			return sb.toString();
		}

		public int getHintCount() {
			int result = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						result++;
					}
				}
			}
			return result;
		}

	}

	public static class ExtendedField extends Field {
		// 1部屋の高さ
		private final int roomHeight;
		// 1部屋の幅
		private final int roomWidth;

		/**
		 * プレーンな盤面生成
		 */
		public ExtendedField(int height, int width) {
			super(height, width);
			roomHeight = getYLength() == 4 ? 2
					: getYLength() == 6 ? 2
							: getYLength() == 9 ? 3 : getYLength() == 16 ? 4 : getYLength() == 25 ? 5 : 0;
			roomWidth = getXLength() == 4 ? 2
					: getXLength() == 6 ? 3
							: getXLength() == 9 ? 3 : getXLength() == 16 ? 4 : getXLength() == 25 ? 5 : 0;
		}

		public ExtendedField(int height, int width, String param) {
			super(height, width, param);
			roomHeight = getYLength() == 4 ? 2
					: getYLength() == 6 ? 2
							: getYLength() == 9 ? 3 : getYLength() == 16 ? 4 : getYLength() == 25 ? 5 : 0;
			roomWidth = getXLength() == 4 ? 2
					: getXLength() == 6 ? 3
							: getXLength() == 9 ? 3 : getXLength() == 16 ? 4 : getXLength() == 25 ? 5 : 0;
		}

		public ExtendedField(ExtendedField other) {
			super(other);
			roomHeight = other.roomHeight;
			roomWidth = other.roomWidth;
		}

		public ExtendedField(List<Integer>[][] numbersCand) {
			super(numbersCand);
			roomHeight = getYLength() == 4 ? 2
					: getYLength() == 6 ? 2
							: getYLength() == 9 ? 3 : getYLength() == 16 ? 4 : getYLength() == 25 ? 5 : 0;
			roomWidth = getXLength() == 4 ? 2
					: getXLength() == 6 ? 3
							: getXLength() == 9 ? 3 : getXLength() == 16 ? 4 : getXLength() == 25 ? 5 : 0;
		}

		@Override
		public int getRoomWidth() {
			return roomWidth;
		}

		@Override
		public int getRoomHeight() {
			return roomHeight;
		}

	}

	protected final Field field;
	private final boolean isExtended;
	protected int count;

	public SudokuSolver(int height, int width, String param) {
		field = new Field(height, width, param);
		isExtended = false;
	}

	public SudokuSolver(int height, int width, String param, boolean isExtended) {
		if (isExtended) {
			field = new ExtendedField(height, width, param);
			this.isExtended = true;
		} else {
			field = new Field(height, width, param);
			this.isExtended = false;
		}
	}

	public SudokuSolver(ExtendedField field) {
		this.field = new ExtendedField(field);
		this.isExtended = true;
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
		System.out.println(new SudokuSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			count = count + field.cnt;
			field.cnt = 0;
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						System.out.println(field);
						return "解けませんでした。途中経過を返します。";
					}
				}
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		int level = (int) Math.sqrt(count) - 10;
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:" + (level < 1 ? 1 : level);
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		while (true) {
			field.updated = false;
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							int oneCand = iterator.next();
							Field virtual = isExtended ? new ExtendedField((ExtendedField) field) : new Field(field);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							count = count + virtual.cnt;
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
								field.updated = true;
							}
						}
						if (field.numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			if (!field.updated) {
				break;
			}
		}
		return true;
	}

}