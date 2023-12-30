package myamya.other.solver.league;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.PuzzleType;
import myamya.other.solver.Solver;

public class LeagueSolver implements Solver {
	public static class LeagueGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class LeagueSolverForGenerator extends LeagueSolver {
			private final int limit;

			public LeagueSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						count = count + field.cnt;
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -2;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -2;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -2;
									}
									if (field.getStateDump().equals(befStr)) {
										return -1;
									}
								}
							}
						}
					}
				} catch (CountOverException e) {
					return -1;
				}
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					throw new CountOverException();
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public LeagueGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new LeagueGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			LeagueSolver.Field wkField = new LeagueSolver.Field(height, width);
			List<Position> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					indexList.add(new Position(yIndex, xIndex));
				}
			}
			int level = 0;
			long start = System.nanoTime();
			String solutionStr;
			while (true) {
				// 問題生成部
				// 数字を配置
				Collections.shuffle(indexList);
				boolean isOk = false;
				for (Position pos : indexList) {
					isOk = false;
					List<Integer> numIdxList = new ArrayList<>();
					for (int number : wkField.numbersCand[pos.getyIndex()][pos.getxIndex()]) {
						numIdxList.add(number);
					}
					Collections.shuffle(numIdxList);
					for (int masuNum : numIdxList) {
						Field virtual = new Field(wkField);
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(masuNum);
						if (virtual.solveAndCheck()) {
							isOk = true;
							wkField.numbersCand = virtual.numbersCand;
							break;
						}
					}
					if (!isOk) {
						break;
					}
				}
				if (!isOk) {
					// 破綻したら0から作り直す。
					wkField = new Field(height, width);
					continue;
				}
				System.out.println(wkField);
				// マスを戻す
				List<Position> fixedMasuList = new ArrayList<>();
				Integer[][] solutionNumbers = new Integer[height][width];
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (yIndex != xIndex) {
							fixedMasuList.add(new Position(yIndex, xIndex));
							wkField.numbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
							solutionNumbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
						}
					}
				}
				// 解けるかな？
				level = new LeagueSolverForGenerator(wkField, 100).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new LeagueSolver.Field(height, width);
				} else {
					Collections.shuffle(fixedMasuList);
					for (Position pos : fixedMasuList) {
						LeagueSolver.Field virtual = new LeagueSolver.Field(wkField);
						virtual.numbers[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						if (pos.getyIndex() == pos.getxIndex()) {
							virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						} else {
							for (int number = 1; number < wkField.getYLength(); number++) {
								virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
							}
						}
						int solveResult = new LeagueSolverForGenerator(virtual, 1000).solve2();
						if (solveResult >= 0) {
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
							if (pos.getyIndex() == pos.getxIndex()) {
								wkField.numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
							} else {
								for (int number = 1; number < wkField.getYLength(); number++) {
									wkField.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
								}
							}
							level = solveResult;
						}
					}
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							if (wkField.numbers[yIndex][xIndex] != null) {
								solutionNumbers[yIndex][xIndex] = null;
							}
						}
					}
					solutionStr = PenpaEditLib.convertSolutionNumbers(solutionNumbers, PuzzleType.LEAGUE);
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertLeagueField(wkField.numbers);
			level = (int) Math.sqrt(level * 10 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (yIndex == xIndex) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
					} else {
						if (wkField.getNumbersCand()[yIndex][xIndex].size() == 1) {
							String numberStr = String.valueOf(wkField.getNumbersCand()[yIndex][xIndex].get(0));
							String masuStr;
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
		}

		public String getHintCount() {
			int numCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numCnt++;
					}
				}
			}
			return String.valueOf(numCnt);
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
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					if (yIndex == xIndex) {
						numbersCand[yIndex][xIndex].add(0);
					} else {
						for (int number = 1; number < getYLength(); number++) {
							numbersCand[yIndex][xIndex].add(number);
						}
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
					// 16 - 255は '-'
					// 256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
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
			numbers = new Integer[other.getYLength()][other.getXLength()];
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		/**
		 * プレーンなフィールド生成。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					if (yIndex == xIndex) {
						numbersCand[yIndex][xIndex].add(0);
					} else {
						for (int number = 1; number < getYLength(); number++) {
							numbersCand[yIndex][xIndex].add(number);
						}
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			numbers = PenpaEditLib.getNumbers(fieldStr);
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);
			numbersCand = new ArrayList[yLength][xLength];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					if (yIndex == xIndex) {
						numbersCand[yIndex][xIndex].add(0);
					} else if (numbers[yIndex][xIndex] != null) {
						numbersCand[yIndex][xIndex].add(numbers[yIndex][xIndex]);
					} else {
						for (int number = 1; number < getYLength(); number++) {
							numbersCand[yIndex][xIndex].add(number);
						}
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
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
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
		 * 縦方向・横方向・斜め方向チェック処理
		 */
		public boolean lineSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						for (int targerX = 0; targerX < getXLength(); targerX++) {
							if (xIndex != targerX) {
								numbersCand[yIndex][targerX].remove(numbersCand[yIndex][xIndex].get(0));
								if (numbersCand[yIndex][targerX].isEmpty()) {
									return false;
								}
							}
						}
					}
				}
			}
			for (int number = 1; number < getYLength(); number++) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					int xCand = -1;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (numbersCand[yIndex][xIndex].contains(number)) {
							if (xCand == -1) {
								xCand = xIndex;
							} else {
								xCand = -2;
								break;
							}
						}
					}
					if (xCand == -1) {
						return false;
					}
					if (xCand != -2) {
						numbersCand[yIndex][xCand].clear();
						numbersCand[yIndex][xCand].add(number);
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[xIndex][yIndex].retainAll(numbersCand[yIndex][xIndex]);
					if (numbersCand[xIndex][yIndex].isEmpty()) {
						return false;
					}
				}
			}
			return true;
		}

		int cnt = 0;

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			cnt++;
			if (!lineSolve()) {
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

	protected final Field field;
	protected int count = 0;

	public LeagueSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public LeagueSolver(Field field) {
		this.field = new Field(field);
	}

	public LeagueSolver(String fieldStr) {
		this.field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// http://pzv.jp/p.html?league/14/14/nbi9ib8d1i396hdk5m1gatckbi3h8g4h27dhcr4u7j8q46n78lda1k7i9i3i12jag8i4icg1bjak
		// http://pzv.jp/p.html?league/6/6/h45x1o2i
		String url = "http://pzv.jp/p.html?league/20/20/i2deg5hfg8gai-13g-11g69k2h1mci4i2i-11ra-13h87-10h5g1gch-10q28-13k4q7jdp93n8g-12hbg7i9h-13i1h-10j5cfk1gag-11g-12q6j-13g5h-138l-10gdlen4l1ge-12fkci6ap3pb9i6dn-12kdobh9eh4hfl-11hdia-119k3m-13gfg6icm8w2i7nf4hajbg"; // urlを入れれば試せる
		// String url = "http://pzv.jp/p.html?league/4/4/s32g"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new LeagueSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
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