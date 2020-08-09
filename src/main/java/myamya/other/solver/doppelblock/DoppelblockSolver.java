package myamya.other.solver.doppelblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class DoppelblockSolver implements Solver {
	public static class DoppelblockGenerator implements Generator {
		static class DoppelblockSolverForGenerator extends DoppelblockSolver {
			private final int limit;

			public DoppelblockSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -1;
						}
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -1;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -1;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -1;
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

		public DoppelblockGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new DoppelblockGenerator(7, 7).generate();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public GeneratorResult generate() {
			int level = -1;
			long start = System.nanoTime();
			Field wkField = null;
			while (true) {
				// 問題生成部
				// 適当なラテン方陣を作る
				int[][] latin = new int[height][width];
				for (int yIndex = 0; yIndex < height; yIndex++) {
					for (int xIndex = 0; xIndex < width; xIndex++) {
						ArrayList<Integer> cand = new ArrayList<>();
						for (int i = 1; i <= height; i++) {
							cand.add(i);
						}
						Collections.shuffle(cand);
						boolean isFill = false;
						for (int num : cand) {
							boolean isOk = true;
							for (int targetY = 0; targetY < height; targetY++) {
								if (yIndex != targetY) {
									if (latin[targetY][xIndex] == num) {
										isOk = false;
										break;
									}
								}
							}
							for (int targetX = 0; targetX < width; targetX++) {
								if (xIndex != targetX) {
									if (latin[yIndex][targetX] == num) {
										isOk = false;
										break;
									}
								}
							}
							if (isOk) {
								latin[yIndex][xIndex] = num;
								isFill = true;
								break;
							}
						}
						if (!isFill) {
							// 失敗したら作りなおし
							latin = new int[height][width];
							yIndex = 0;
							xIndex = -1;
						}
					}
				}
				for (int yIndex = 0; yIndex < height; yIndex++) {
					for (int xIndex = 0; xIndex < width; xIndex++) {
						if (latin[yIndex][xIndex] > height - 2) {
							latin[yIndex][xIndex] = 0;
						}
					}
				}
				// ヒントを埋める
				Integer[] upHints = new Integer[width];
				Integer[] leftHints = new Integer[height];
				for (int yIndex = 0; yIndex < height; yIndex++) {
					int hint = 0;
					boolean counting = false;
					for (int xIndex = 0; xIndex < width; xIndex++) {
						if (latin[yIndex][xIndex] == 0) {
							if (counting) {
								break;
							} else {
								counting = true;
							}
						} else {
							if (counting) {
								hint = hint + latin[yIndex][xIndex];
							}
						}
					}
					leftHints[yIndex] = hint;
				}
				for (int xIndex = 0; xIndex < width; xIndex++) {
					int hint = 0;
					boolean counting = false;
					for (int yIndex = 0; yIndex < height; yIndex++) {
						if (latin[yIndex][xIndex] == 0) {
							if (counting) {
								break;
							} else {
								counting = true;
							}
						} else {
							if (counting) {
								hint = hint + latin[yIndex][xIndex];
							}
						}
					}
					upHints[xIndex] = hint;
				}
				List<Integer> indexList = new ArrayList<>();
				// 解けるかな？
				wkField = new Field(height, width, upHints, leftHints);
				level = new DoppelblockSolverForGenerator(wkField, 5000).solve2();
				System.out.println(level);
				if (level == -1) {
					// 解けなければやり直し
				} else {
					// ヒントを限界まで減らす
					for (int i = 0; i < height + width; i++) {
						indexList.add(i);
					}
					Collections.shuffle(indexList);
					for (Integer posBase : indexList) {
						Integer[] targetLeftHints = copyArray(leftHints);
						Integer[] targetUpHints = copyArray(upHints);
						if (posBase < height) {
							targetLeftHints[posBase] = null;
						} else {
							targetUpHints[posBase - height] = null;
						}
						Field virtual = new Field(height, width, targetUpHints, targetLeftHints);
						int solveResult = new DoppelblockSolverForGenerator(virtual, 50000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							leftHints = targetLeftHints;
							upHints = targetUpHints;
						}
					}
					wkField = new Field(height, width, upHints, leftHints);
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < wkField.getUpHints().length; xIndex++) {
				if (wkField.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(wkField.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getLeftHints().length; yIndex++) {
				if (wkField.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(wkField.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4)
							+ "\" x=\""
							+ (baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
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
			for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
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
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

		/**
		 * 配列をもとの配列に影響のない形にコピーする。
		 */
		private Integer[] copyArray(Integer[] array) {
			Integer[] result = new Integer[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = array[i];
			}
			return result;
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<List<Integer>>[] lineCands;
		protected List<List<Integer>>[] columnCands;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] leftHints;

		public List<List<Integer>>[] getLineCands() {
			return lineCands;
		}

		private static final String FOR_URL = "0123456789abcdef";

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?doppelblock/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (Integer upHint : upHints) {
				if (upHint != null) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
						interval = 0;
					}
					sb.append(FOR_URL.charAt(upHint));
				} else {
					interval++;
				}
			}
			for (Integer leftHint : leftHints) {
				if (leftHint != null) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
						interval = 0;
					}
					sb.append(FOR_URL.charAt(leftHint));
				} else {
					interval++;
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.charAt(interval - 1));
			}
			return sb.toString();
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					cnt++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					cnt++;
				}
			}
			return String.valueOf(cnt);
		}

		public List<List<Integer>>[] getColumnCands() {
			return columnCands;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public int getYLength() {
			return lineCands.length;
		}

		public int getXLength() {
			return columnCands.length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			lineCands = new ArrayList[height];
			columnCands = new ArrayList[width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						if (index < width) {
							upHints[index] = capacity;
						} else {
							leftHints[index - width] = capacity;
						}
					}
					index++;
				}
			}
			makeCand();
		}

		private void makeCand() {
			// 縦横の数字により、初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				List<List<Integer>> oneCands = new ArrayList<>();
				makeCombo(oneCands, new ArrayList<>(), getXLength(), leftHints[yIndex], false, 0);
				lineCands[yIndex] = oneCands;
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				List<List<Integer>> oneCands = new ArrayList<>();
				makeCombo(oneCands, new ArrayList<>(), getXLength(), upHints[xIndex], false, 0);
				columnCands[xIndex] = oneCands;
			}
		}

		public void makeCombo(List<List<Integer>> result, List<Integer> wk, int size, Integer posiHint,
				boolean discover, int cnt) {
			for (int i = 1; i <= size; i++) {
				if (!wk.contains(i)) {
					wk.add(i);
					boolean isOk = true;
					boolean nextDiscover = discover;
					int nextCnt = cnt;
					if (posiHint != null) {
						if (i > size - 2) {
							if (discover) {
								isOk = nextCnt == posiHint;
							} else {
								nextDiscover = true;
							}
						} else {
							if (discover) {
								nextCnt = nextCnt + i;
							}
						}
					}
					if (isOk) {
						if (wk.size() == size) {
							List<Integer> useWk = new ArrayList<>();
							for (int j = 0; j < wk.size(); j++) {
								if (wk.get(j) > size - 2) {
									useWk.add(0);
								} else {
									useWk.add(wk.get(j));
								}
							}
							if (!result.contains(useWk)) {
								result.add(new ArrayList<>(useWk));
							}
						} else {
							makeCombo(result, wk, size, posiHint, nextDiscover, nextCnt);
						}
					}
					wk.remove(wk.size() - 1);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			lineCands = new ArrayList[other.getYLength()];
			columnCands = new ArrayList[other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				lineCands[yIndex] = new ArrayList<>(other.lineCands[yIndex]);
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				columnCands[xIndex] = new ArrayList<>(other.columnCands[xIndex]);
			}
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, Integer[] upHints, Integer[] leftHints) {
			lineCands = new ArrayList[height];
			columnCands = new ArrayList[width];
			this.upHints = upHints;
			this.leftHints = leftHints;
			makeCand();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					String numStr = String.valueOf(upHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					String numStr = String.valueOf(leftHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (columnCands[xIndex].size() == 0) {
						sb.append("×");
					} else if (columnCands[xIndex].size() == 1) {
						String numStr = String.valueOf(columnCands[xIndex].get(0).get(yIndex));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else if (lineCands[yIndex].size() == 1) {
						String numStr = String.valueOf(lineCands[yIndex].get(0).get(xIndex));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else {
						int candNum = -1;
						if (candNum != 0) {
							for (List<Integer> cc : columnCands[xIndex]) {
								if (cc.get(yIndex) != candNum) {
									if (candNum == -1) {
										candNum = cc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							for (List<Integer> lc : lineCands[yIndex]) {
								if (lc.get(xIndex) != candNum) {
									if (candNum == -1) {
										candNum = lc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						String numStr = String.valueOf(candNum);
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("　");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
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
				sb.append(lineCands[yIndex].size());
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append(columnCands[xIndex].size());
			}
			return sb.toString();
		}

		/**
		 * ある候補列が1通りになっているときにそぐわない候補を除外する。
		 */
		private boolean hintSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (lineCands[yIndex].size() == 1) {
					List<Integer> fixCand = lineCands[yIndex].get(0);
					for (int targetX = 0; targetX < getXLength(); targetX++) {
						for (Iterator<List<Integer>> iterator = columnCands[targetX].iterator(); iterator.hasNext();) {
							List<Integer> targetCand = iterator.next();
							if (fixCand.get(targetX) != targetCand.get(yIndex)) {
								iterator.remove();
							}
						}
						if (columnCands[targetX].size() == 0) {
							return false;
						}
					}
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (columnCands[xIndex].size() == 1) {
					List<Integer> fixCand = columnCands[xIndex].get(0);
					for (int targetY = 0; targetY < getYLength(); targetY++) {
						for (Iterator<List<Integer>> iterator = lineCands[targetY].iterator(); iterator.hasNext();) {
							List<Integer> targetCand = iterator.next();
							if (fixCand.get(targetY) != targetCand.get(xIndex)) {
								iterator.remove();
							}
						}
						if (lineCands[targetY].size() == 0) {
							return false;
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
			if (!hintSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (lineCands[yIndex].size() != 1) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (columnCands[xIndex].size() != 1) {
					return false;
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public DoppelblockSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public DoppelblockSolver(Field field) {
		this.field = new Field(field);
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
		System.out.println(new DoppelblockSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		System.out.println(field);
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
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				if (field.lineCands[yIndex].size() != 1) {
					for (Iterator<List<Integer>> iterator = field.lineCands[yIndex].iterator(); iterator
							.hasNext();) {
						count++;
						List<Integer> oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.lineCands[yIndex].clear();
						virtual.lineCands[yIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.lineCands[yIndex].size() == 0) {
						return false;
					}
				}
				if (field.lineCands[yIndex].size() == 1) {
					if (!field.solveAndCheck()) {
						return false;
					}
				}
			}
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.columnCands[xIndex].size() != 1) {
					for (Iterator<List<Integer>> iterator = field.columnCands[xIndex].iterator(); iterator
							.hasNext();) {
						count++;
						List<Integer> oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.columnCands[xIndex].clear();
						virtual.columnCands[xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.columnCands[xIndex].size() == 0) {
						return false;
					}
				}
				if (field.columnCands[xIndex].size() == 1) {
					if (!field.solveAndCheck()) {
						return false;
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