package myamya.other.solver.nonogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class NonogramSolver implements Solver {
	public static class NonogramGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class NonogramSolverForGenerator extends NonogramSolver {

			private final int limit;

			public NonogramSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				while (!field.isSolved()) {
					String befStr = field.getStateDump();
					if (!field.solveAndCheck()) {
						return -1;
					}
					int recursiveCnt = 0;
					while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
						if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
							return -1;
						}
						recursiveCnt++;
					}
					if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
						return -1;
					}
				}
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					return false;
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public NonogramGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NonogramGenerator(15, 15).generate();
		}

		@Override
		public GeneratorResult generate() {
			NonogramSolver.Field wkField = new NonogramSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				for (Integer index : indexList) {
					int yIndex = indexList.get(index) / width;
					int xIndex = indexList.get(index) % width;
					wkField.masu[yIndex][xIndex] = Math.random() * 2 < 1 ? Masu.BLACK : Masu.NOT_BLACK;
				}
				// ヒント配置
				// 横のヒント
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int wkCnt = 0;
					List<Integer> wkList = new ArrayList<>();
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							wkCnt++;
						} else {
							if (wkCnt != 0) {
								wkList.add(wkCnt);
								wkCnt = 0;
							}
						}
					}
					Collections.reverse(wkList);
					for (int i = 0; i < wkList.size(); i++) {
						wkField.leftHints[yIndex][i] = wkList.get(i);
					}
				}
				// 横のヒント
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int wkCnt = 0;
					List<Integer> wkList = new ArrayList<>();
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							wkCnt++;
						} else {
							if (wkCnt != 0) {
								wkList.add(wkCnt);
								wkCnt = 0;
							}
						}
					}
					if (wkCnt != 0) {
						wkList.add(wkCnt);
					}
					Collections.reverse(wkList);
					for (int i = 0; i < wkList.size(); i++) {
						wkField.leftHints[yIndex][i] = wkList.get(i);
					}
				}
				// 縦のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int wkCnt = 0;
					List<Integer> wkList = new ArrayList<>();
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							wkCnt++;
						} else {
							if (wkCnt != 0) {
								wkList.add(wkCnt);
								wkCnt = 0;
							}
						}
					}
					if (wkCnt != 0) {
						wkList.add(wkCnt);
					}
					Collections.reverse(wkList);
					for (int i = 0; i < wkList.size(); i++) {
						wkField.upHints[xIndex][i] = wkList.get(i);
					}
				}
				wkField.makeCand();
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new NonogramSolverForGenerator(new NonogramSolver.Field(wkField), 50).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new NonogramSolver.Field(height, width);
				} else {
					break;
				}
			}
			System.out.println("仮置きレベル:" + level);
			double dlevel = Math.sqrt((((level * 50) + wkField.cnt) * 3) / 3) + 1;
			level = (int) (Math.sqrt((((level * 50) + wkField.cnt) * 3) / 3) + 1);
			String status = "Lv:" + String.format("%.1f", dlevel) + "の問題を獲得！";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			int upHintsAdjust = (wkField.getYLength() + 1) / 2;
			int leftHintsAdjust = (wkField.getXLength() + 1) / 2;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\""
							+ ((wkField.getYLength() + upHintsAdjust) * baseSize + 2 * baseSize
									+ margin)
							+ "\" width=\""
							+ ((wkField.getXLength() + leftHintsAdjust) * baseSize + 2 * baseSize)
							+ "\" >");
			// 横ヒント配置
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				List<Integer> wkList = new ArrayList<>(Arrays.asList(wkField.getLeftHints()[yIndex]));
				Collections.reverse(wkList);
				for (int xIndex = 0; xIndex < leftHintsAdjust; xIndex++) {
					if (wkList.get(xIndex) != null) {
						String numberStr = String.valueOf(wkList.get(xIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + ((yIndex + upHintsAdjust) * baseSize + baseSize - 4 + margin)
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
			// 縦ヒント配置
			for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
				List<Integer> wkList = new ArrayList<>(Arrays.asList(wkField.getUpHints()[xIndex]));
				Collections.reverse(wkList);
				for (int yIndex = 0; yIndex < upHintsAdjust; yIndex++) {
					if (wkList.get(yIndex) != null) {
						String numberStr = String.valueOf(wkList.get(yIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
								+ "\" x=\""
								+ ((xIndex + leftHintsAdjust) * baseSize + baseSize + 2)
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ ((yIndex + upHintsAdjust) * baseSize + margin)
							+ "\" x1=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ ((yIndex + upHintsAdjust) * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + 2 * baseSize)
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
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ ((yIndex + upHintsAdjust) * baseSize + baseSize + margin)
							+ "\" x1=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + baseSize)
							+ "\" y2=\""
							+ ((yIndex + upHintsAdjust) * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + baseSize + baseSize)
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
			wkField.printHints();
			System.out.println(wkField.getPuzPreURL());
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// ヒント情報。
		// upHintsは、横→縦の構造になっているので注意
		protected Integer[][] upHints;
		protected Integer[][] leftHints;
		// 列の候補情報
		private Map<Integer, List<String>> varticalCand;
		// 行の候補情報
		private Map<Integer, List<String>> horizonalCand;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getUpHints() {
			return upHints;
		}

		public Integer[][] getLeftHints() {
			return leftHints;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?nonogram/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			int upHintsAdjust = (getYLength() + 1) / 2;
			int leftHintsAdjust = (getXLength() + 1) / 2;
			for (int i = 0; i < (getXLength() * upHintsAdjust) + (getYLength() * leftHintsAdjust); i++) {
				int useIndex = i;
				boolean useLeftHints = false;
				if (i >= getXLength() * upHintsAdjust) {
					useIndex = i - (getXLength() * upHintsAdjust);
					useLeftHints = true;
				}
				int yIndex = useLeftHints ? useIndex / leftHintsAdjust : useIndex % upHintsAdjust;
				int xIndex = useLeftHints ? useIndex % leftHintsAdjust : useIndex / upHintsAdjust;
				if (useLeftHints ? leftHints[yIndex][xIndex] == null : upHints[xIndex][yIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = useLeftHints ? leftHints[yIndex][xIndex] : upHints[xIndex][yIndex];
					String numStr;
					if (num == -1) {
						numStr = ".";
					} else {
						numStr = Integer.toHexString(num);
					}
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			return "";
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			upHints = new Integer[width][(height + 1) / 2];
			leftHints = new Integer[height][(width + 1) / 2];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			varticalCand = new HashMap<>();
			horizonalCand = new HashMap<>();
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			upHints = new Integer[width][(height + 1) / 2];
			leftHints = new Integer[height][(width + 1) / 2];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// ヒントは、縦のヒント→横のヒントをしたからつなげていく
			int index = 0;
			int upHintsAdjust = (getYLength() + 1) / 2;
			int leftHintsAdjust = (getXLength() + 1) / 2;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
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
					if (index >= (upHintsAdjust * getXLength())) {
						int useIndex = index - (upHintsAdjust * getXLength());
						Position pos = new Position(useIndex / leftHintsAdjust,
								useIndex % leftHintsAdjust);
						leftHints[pos.getyIndex()][pos.getxIndex()] = capacity;
					} else {
						Position pos = new Position(index / upHintsAdjust, index % upHintsAdjust);
						upHints[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			makeCand();
			printHints();
		}

		private void printHints() {
			StringBuilder sb = new StringBuilder();
			sb.append("leftHints:");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				List<Integer> numbers = new ArrayList<>(Arrays.asList(leftHints[yIndex]));
				Collections.reverse(numbers);
				for (Iterator<Integer> iterator = numbers.iterator(); iterator.hasNext();) {
					Integer integer = iterator.next();
					if (integer == null) {
						iterator.remove();
					}
				}
				sb.append(numbers);
				sb.append(System.lineSeparator());
			}
			sb.append("upHints:");
			sb.append(System.lineSeparator());
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				List<Integer> numbers = new ArrayList<>(Arrays.asList(upHints[xIndex]));
				Collections.reverse(numbers);
				for (Iterator<Integer> iterator = numbers.iterator(); iterator.hasNext();) {
					Integer integer = iterator.next();
					if (integer == null) {
						iterator.remove();
					}
				}
				sb.append(numbers);
				sb.append(System.lineSeparator());
			}
			System.out.println(sb);
			System.out.println("ヒント複雑度:" + cnt);
		}

		int cnt = 0;

		/**
		 * 縦横の候補数。最初に一度だけ呼ぶこと
		 */
		private void makeCand() {
			varticalCand = new HashMap<>();
			horizonalCand = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				List<Integer> numbers = new ArrayList<>(Arrays.asList(leftHints[yIndex]));
				Collections.reverse(numbers);
				for (Iterator<Integer> iterator = numbers.iterator(); iterator.hasNext();) {
					Integer integer = iterator.next();
					if (integer == null) {
						iterator.remove();
					}
				}
				List<String> candMasuList = new ArrayList<>();
				makeCombination(getXLength(), candMasuList, new StringBuilder(), numbers);
				cnt = cnt + candMasuList.size();
				horizonalCand.put(yIndex, candMasuList);
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				List<Integer> numbers = new ArrayList<>(Arrays.asList(upHints[xIndex]));
				Collections.reverse(numbers);
				for (Iterator<Integer> iterator = numbers.iterator(); iterator.hasNext();) {
					Integer integer = iterator.next();
					if (integer == null) {
						iterator.remove();
					}
				}
				List<String> candMasuList = new ArrayList<>();
				makeCombination(getYLength(), candMasuList, new StringBuilder(), numbers);
				cnt = cnt + candMasuList.size();
				varticalCand.put(xIndex, candMasuList);
			}
			cnt = (int) Math.sqrt(cnt);
		}

		private static void makeCombination(int size, List<String> candMasuList, StringBuilder candMasu,
				List<Integer> numbers) {
			if (numbers.size() == 0) {
				for (int i = 0; i < size; i++) {
					candMasu.append("・");
				}
				candMasuList.add(candMasu.toString());
				return;
			}
			int total = numbers.size() - 1;
			for (int num : numbers) {
				total = total + num;
			}
			for (int topBan = 0; topBan < size - total + 1; topBan++) {
				for (int i = 0; i < topBan; i++) {
					candMasu.append("・");
				}
				int num = numbers.remove(0);
				for (int i = 0; i < num; i++) {
					candMasu.append("■");
				}
				if (numbers.size() != 0) {
					candMasu.append("・");
					makeCombination(size - num - topBan - 1,
							candMasuList, candMasu, numbers);
					for (int i = 0; i < topBan + num + 1; i++) {
						candMasu.setLength(candMasu.length() -
								1);
					}
				} else {
					for (int i = 0; i < size - num - topBan; i++) {
						candMasu.append("・");
					}
					candMasuList.add(candMasu.toString());
					for (int i = 0; i < size; i++) {
						candMasu.setLength(candMasu.length() -
								1);
					}
				}
				numbers.add(0, num);
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			varticalCand = new HashMap<>();
			horizonalCand = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				varticalCand.put(xIndex, new ArrayList<>(other.varticalCand.get(xIndex)));
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				horizonalCand.put(yIndex, new ArrayList<>(other.horizonalCand.get(yIndex)));
			}
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!candSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 候補情報をもとに白マス・黒マスを確定する。
		 * 矛盾が生じたらfalseを返す。
		 */
		private boolean candSolve() {
			for (Entry<Integer, List<String>> entry : horizonalCand.entrySet()) {
				int yIndex = entry.getKey();
				List<String> candList = entry.getValue();
				for (Iterator<String> iterator = candList.iterator(); iterator.hasNext();) {
					String state = iterator.next();
					for (int idx = 0; idx < state.length(); idx++) {
						Position pos = new Position(yIndex, idx);
						if ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
								&& state.charAt(idx) == '・')
								|| ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK)
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
						Position pos = new Position(yIndex, idx);
						if (fixState.charAt(idx) == '■') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						} else if (fixState.charAt(idx) == '・') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						}
					}
				}
			}
			for (Entry<Integer, List<String>> entry : varticalCand.entrySet()) {
				int xIndex = entry.getKey();
				List<String> candList = entry.getValue();
				for (Iterator<String> iterator = candList.iterator(); iterator.hasNext();) {
					String state = iterator.next();
					for (int idx = 0; idx < state.length(); idx++) {
						Position pos = new Position(idx, xIndex);
						if ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
								&& state.charAt(idx) == '・')
								|| ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK)
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
						Position pos = new Position(idx, xIndex);
						if (fixState.charAt(idx) == '■') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						} else if (fixState.charAt(idx) == '・') {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						} else {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
		}

	}

	protected final Field field;
	protected int count;

	public NonogramSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NonogramSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://puzz.link/p?nonogram/15/15/1312j1314j21121i41121i2113j2131j1122j4212j2114j1411j61l2132j11122i2143j1112j1123j35l234k1213j11122i21114i111k3411j211111h231k5112j11112i3213j222211h12211i"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NonogramSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (((count * 50) + field.cnt)) * 3);
		System.out.println(field);
		double dlevel = Math.sqrt((((count * 50) + field.cnt) * 3) / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount((((count * 50) + field.cnt)) * 3).toString() + "(Lv:" + String.format("%.1f", dlevel) + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
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

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}