package myamya.other.solver.easyasabc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class EasyasabcSolver implements Solver {
	public static class EasyasabcGenerator implements Generator {
		static class EasyasabcSolverForGenerator extends EasyasabcSolver {
			private final int limit;

			public EasyasabcSolverForGenerator(Field field, int limit) {
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

		public EasyasabcGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new EasyasabcGenerator(5, 5).generate();
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
				// 適当なラテン方陣を作り、kindを超えたら0にする。
				int kind = (int) ((height + 1 + (Math.random() * 2)) / 2);
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
						if (latin[yIndex][xIndex] > kind) {
							latin[yIndex][xIndex] = 0;
						}
					}
				}
				// 0を含む市松模様は唯一解にならないのでここではじく。
				boolean isOk = true;
				outer: for (int yIndex = 0; yIndex < height - 1; yIndex++) {
					for (int xIndex = 0; xIndex < width - 1; xIndex++) {
						if (latin[yIndex][xIndex] == 0 && latin[yIndex + 1][xIndex + 1] == 0
								&& latin[yIndex][xIndex + 1] == latin[yIndex + 1][xIndex]) {
							isOk = false;
							break outer;
						}
						if (latin[yIndex][xIndex + 1] == 0 && latin[yIndex + 1][xIndex] == 0
								&& latin[yIndex][xIndex] == latin[yIndex + 1][xIndex + 1]) {
							isOk = false;
							break outer;
						}
					}
				}
				if (!isOk) {
					continue;
				}
				// ヒントを埋める
				Integer[] upHints = new Integer[width];
				Integer[] downHints = new Integer[width];
				Integer[] leftHints = new Integer[height];
				Integer[] rightHints = new Integer[height];
				for (int yIndex = 0; yIndex < height; yIndex++) {
					int hint = 0;
					for (int xIndex = 0; xIndex < width; xIndex++) {
						if (latin[yIndex][xIndex] != 0) {
							hint = latin[yIndex][xIndex];
							break;
						}
					}
					leftHints[yIndex] = hint;
					hint = 0;
					for (int xIndex = width - 1; xIndex >= 0; xIndex--) {
						if (latin[yIndex][xIndex] != 0) {
							hint = latin[yIndex][xIndex];
							break;
						}
					}
					rightHints[yIndex] = hint;
				}
				for (int xIndex = 0; xIndex < width; xIndex++) {
					int hint = 0;
					for (int yIndex = 0; yIndex < height; yIndex++) {
						if (latin[yIndex][xIndex] != 0) {
							hint = latin[yIndex][xIndex];
							break;
						}
					}
					upHints[xIndex] = hint;
					hint = 0;
					for (int yIndex = height - 1; yIndex >= 0; yIndex--) {
						if (latin[yIndex][xIndex] != 0) {
							hint = latin[yIndex][xIndex];
							break;
						}
					}
					downHints[xIndex] = hint;
				}
				List<Integer> indexList = new ArrayList<>();
				// 解けるかな？
				wkField = new Field(height, width, kind, upHints, downHints, leftHints, rightHints);
				level = new EasyasabcSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
				} else {
					// ヒントを限界まで減らす
					for (int i = 0; i < height * 2 + width * 2; i++) {
						indexList.add(i);
					}
					Collections.shuffle(indexList);
					for (Integer posBase : indexList) {
						Integer[] targetLeftHints = copyArray(leftHints);
						Integer[] targetRightHints = copyArray(rightHints);
						Integer[] targetUpHints = copyArray(upHints);
						Integer[] targetDownHints = copyArray(downHints);
						if (posBase < height) {
							targetLeftHints[posBase] = null;
						} else if (posBase < height * 2) {
							targetRightHints[posBase - height] = null;
						} else if (posBase < height * 2 + width) {
							targetUpHints[posBase - height * 2] = null;
						} else {
							targetDownHints[posBase - height * 2 - width] = null;
						}
						Field virtual = new Field(height, width, kind, targetUpHints, targetDownHints, targetLeftHints,
								targetRightHints);
						int solveResult = new EasyasabcSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							leftHints = targetLeftHints;
							rightHints = targetRightHints;
							upHints = targetUpHints;
							downHints = targetDownHints;
						}
					}
					wkField = new Field(height, width, kind, upHints, downHints, leftHints, rightHints);
					break;
				}
			}
			level = (int) Math.sqrt(level * 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
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
				if (wkField.getRightHints()[yIndex] != null) {
					String numberStr = String.valueOf(wkField.getRightHints()[yIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4)
							+ "\" x=\""
							+ (wkField.getXLength() * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
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
				if (wkField.getDownHints()[xIndex] != null) {
					String numberStr = String.valueOf(wkField.getDownHints()[xIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (wkField.getYLength() * baseSize + baseSize + baseSize - 4)
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
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

		// 何種類か
		protected int kind;

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] downHints;
		protected Integer[] leftHints;
		protected Integer[] rightHints;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getDownHints() {
			return downHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public Integer[] getRightHints() {
			return rightHints;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		private static final String FOR_URL = "0123456789abcdef";

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?easyasabc/" + getXLength() + "/" + getYLength() + "/" + kind + "/");
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
			for (Integer downHint : downHints) {
				if (downHint != null) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
						interval = 0;
					}
					sb.append(FOR_URL.charAt(downHint));
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
			for (Integer rightHint : rightHints) {
				if (rightHint != null) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
						interval = 0;
					}
					sb.append(FOR_URL.charAt(rightHint));
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
				if (rightHints[yIndex] != null) {
					cnt++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					cnt++;
				}
				if (downHints[xIndex] != null) {
					cnt++;
				}
			}
			return String.valueOf(cnt);
		}

		/**
		 * ヒントを指定して盤面作成。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width, int kind, Integer[] upHints, Integer[] downHints, Integer[] leftHints,
				Integer[] rightHints) {
			numbersCand = new ArrayList[height][width];
			this.kind = kind;
			this.upHints = upHints;
			this.downHints = downHints;
			this.leftHints = leftHints;
			this.rightHints = rightHints;
			// 初期候補数字を決定
			// 0からkindまで
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 0; i <= kind; i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, int kind, String param) {
			numbersCand = new ArrayList[height][width];
			this.kind = kind;
			upHints = new Integer[width];
			downHints = new Integer[width];
			leftHints = new Integer[height];
			rightHints = new Integer[height];
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
						} else if (index < width * 2) {
							downHints[index - width] = capacity;
						} else if (index < (width * 2 + height)) {
							leftHints[index - width * 2] = capacity;
						} else {
							rightHints[index - (width * 2 + height)] = capacity;
						}
					}
					index++;
				}
			}
			// 初期候補数字を決定
			// 0からkindまで
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 0; i <= kind; i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			kind = other.kind;
			upHints = other.upHints;
			downHints = other.downHints;
			leftHints = other.leftHints;
			rightHints = other.rightHints;
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
					if (index == 0) {
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
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("■");
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
				sb.append("←");
				if (rightHints[yIndex] != null) {
					String numStr = String.valueOf(rightHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append(System.lineSeparator());
			}
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↑");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (downHints[xIndex] != null) {
					String numStr = String.valueOf(downHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
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
		 * 同じ列・行にいる数字を候補から除外する。0は列(行の長さ)-種類の数だけ入る。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer myNumber = numbersCand[yIndex][xIndex].get(0);
						int zeroCnt = myNumber == 0 ? 1 : 0;
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							if (yIndex != targetY) {
								if (myNumber == 0) {
									if (zeroCnt == getYLength() - kind) {
										numbersCand[targetY][xIndex].remove(myNumber);
									} else if (numbersCand[targetY][xIndex].size() == 1
											&& numbersCand[targetY][xIndex].get(0) == 0) {
										zeroCnt++;
									}
								} else {
									numbersCand[targetY][xIndex].remove(myNumber);
								}
								if (numbersCand[targetY][xIndex].size() == 0) {
									return false;
								}
							}
						}
						zeroCnt = myNumber == 0 ? 1 : 0;
						for (int targetX = 0; targetX < getXLength(); targetX++) {
							if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
								if (myNumber == 0) {
									if (zeroCnt == getXLength() - kind) {
										numbersCand[yIndex][targetX].remove(myNumber);
									} else if (numbersCand[yIndex][targetX].size() == 1
											&& numbersCand[yIndex][targetX].get(0) == 0) {
										zeroCnt++;
									}
								} else {
									numbersCand[yIndex][targetX].remove(myNumber);
								}
								if (numbersCand[yIndex][targetX].size() == 0) {
									return false;
								}
							}
						}
					} else {
						for (int cand : numbersCand[yIndex][xIndex]) {
							boolean isHiddenSingle = true;
							int zeroCnt = 0;
							for (int targetY = 0; targetY < getYLength(); targetY++) {
								if (yIndex != targetY) {
									if (numbersCand[targetY][xIndex].contains(cand)) {
										if (cand == 0) {
											if (zeroCnt < getYLength() - kind - 1) {
												zeroCnt++;
											} else {
												isHiddenSingle = false;
												break;
											}
										} else {
											isHiddenSingle = false;
											break;
										}
									}
								}
							}
							if (isHiddenSingle) {
								numbersCand[yIndex][xIndex].clear();
								numbersCand[yIndex][xIndex].add(cand);
								break;
							}
							isHiddenSingle = true;
							zeroCnt = 0;
							for (int targetX = 0; targetX < getXLength(); targetX++) {
								if (xIndex != targetX) {
									if (numbersCand[yIndex][targetX].contains(cand)) {
										if (cand == 0) {
											if (zeroCnt < getXLength() - kind - 1) {
												zeroCnt++;
											} else {
												isHiddenSingle = false;
												break;
											}
										} else {
											isHiddenSingle = false;
											break;
										}
									}
								}
							}
							if (isHiddenSingle) {
								numbersCand[yIndex][xIndex].clear();
								numbersCand[yIndex][xIndex].add(cand);
								break;
							}
						}

					}
				}
			}
			return true;
		}

		/**
		 * ヒントは0を除く最初に見える数字である。
		 */
		private boolean hintSolve() {
			for (int x = 0; x < getXLength(); x++) {
				Integer upHint = upHints[x];
				if (upHint != null) {
					for (int y = 0; y < getYLength(); y++) {
						if (numbersCand[y][x].contains(upHint)) {
							break;
						} else if (!numbersCand[y][x].contains(0)) {
							return false;
						}
					}
				}
				Integer downHint = downHints[x];
				if (downHint != null) {
					for (int y = getYLength() - 1; y >= 0; y--) {
						if (numbersCand[y][x].contains(downHint)) {
							break;
						} else if (!numbersCand[y][x].contains(0)) {
							return false;
						}
					}
				}
			}
			for (int y = 0; y < getYLength(); y++) {
				Integer leftHint = leftHints[y];
				if (leftHint != null) {
					for (int x = 0; x < getXLength(); x++) {
						if (numbersCand[y][x].contains(leftHint)) {
							break;
						} else if (!numbersCand[y][x].contains(0)) {
							return false;
						}
					}
				}
				Integer rightHint = rightHints[y];
				if (rightHint != null) {
					for (int x = getXLength() - 1; x >= 0; x--) {
						if (numbersCand[y][x].contains(rightHint)) {
							break;
						} else if (!numbersCand[y][x].contains(0)) {
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
			if (!roomSolve()) {
				return false;
			}
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

	public EasyasabcSolver(int height, int width, int kind, String param) {
		field = new Field(height, width, kind, param);
	}

	public EasyasabcSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?easyasabc/7/7/4/i332g21324h3g1212h22123g"; //urlを入れれば試せる
		String[] params = url.split("/");
		int kind = Integer.parseInt(params[params.length - 2]);
		int height = Integer.parseInt(params[params.length - 3]);
		int width = Integer.parseInt(params[params.length - 4]);
		String param = params[params.length - 1];
		System.out.println(new EasyasabcSolver(height, width, kind, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 3 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
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
					if (field.numbersCand[yIndex][xIndex].size() == 1) {
						if (!field.solveAndCheck()) {
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
