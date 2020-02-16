package myamya.other.solver.minarism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class MinarismSolver implements Solver {
	public static class MinarismGenerator implements Generator {
		static class MinarismSolverForGenerator extends MinarismSolver {
			private final int limit;

			public MinarismSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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

		public MinarismGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MinarismGenerator(5, 5).generate();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public GeneratorResult generate() {
			MinarismSolver.Field wkField = new MinarismSolver.Field(height, width);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int yIndex = index / width;
					int xIndex = index % width;
					if (wkField.numbersCand[yIndex][xIndex].size() != 0) {
						int numIdx = (int) (Math.random() * wkField.numbersCand[yIndex][xIndex].size());
						MinarismSolver.Field virtual = new MinarismSolver.Field(wkField);
						virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
						virtual.numbersCand[yIndex][xIndex].add(wkField.numbersCand[yIndex][xIndex].get(numIdx));
						if (!virtual.solveAndCheck()) {
							// 破綻したら0から作り直す。
							wkField = new MinarismSolver.Field(height, width);
							index = 0;
							continue;
						} else {
							wkField.numbersCand = virtual.numbersCand;
						}
					}
					index++;
				}
				List<Integer> indexList = new ArrayList<>();
				for (int i = 0; i < ((height * (width - 1)) + ((height - 1) * width)); i++) {
					indexList.add(i);
				}
				// 記号付与
				for (Integer posBase : indexList) {
					boolean toYokoWall;
					int yIndex, xIndex;
					if (posBase < height * (width - 1)) {
						toYokoWall = true;
						yIndex = posBase / (width - 1);
						xIndex = posBase % (width - 1);
					} else {
						toYokoWall = false;
						posBase = posBase - (height * (width - 1));
						yIndex = posBase / width;
						xIndex = posBase % width;
					}
					// 1/2の確率で数字か不等号かにする
					if (toYokoWall) {
						int masu1 = wkField.numbersCand[yIndex][xIndex].get(0);
						int masu2 = wkField.numbersCand[yIndex][xIndex + 1].get(0);
						if (Math.random() * 2 < 1) {
							wkField.yokoWallNum[yIndex][xIndex] = masu1 < masu2 ? -2 : -1;
						} else {
							wkField.yokoWallNum[yIndex][xIndex] = masu1 < masu2 ? masu2 - masu1 : masu1 - masu2;
						}
					} else {
						int masu1 = wkField.numbersCand[yIndex][xIndex].get(0);
						int masu2 = wkField.numbersCand[yIndex + 1][xIndex].get(0);
						if (Math.random() * 2 < 1) {
							wkField.tateWallNum[yIndex][xIndex] = masu1 < masu2 ? -2 : -1;
						} else {
							wkField.tateWallNum[yIndex][xIndex] = masu1 < masu2 ? masu2 - masu1 : masu1 - masu2;
						}
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (int i = 1; i <= height || i <= width; i++) {
							wkField.numbersCand[yIndex][xIndex].add(i);
						}
					}
				}

				// 解けるかな？
				level = new MinarismSolverForGenerator(new Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(indexList);
					for (Integer posBase : indexList) {
						System.out.println(wkField);
						Field virtual = new Field(wkField, true);
						boolean toYokoWall;
						int yIndex, xIndex;
						if (posBase < height * (width - 1)) {
							toYokoWall = true;
							yIndex = posBase / (width - 1);
							xIndex = posBase % (width - 1);
						} else {
							toYokoWall = false;
							posBase = posBase - (height * (width - 1));
							yIndex = posBase / width;
							xIndex = posBase % width;
						}
						if (toYokoWall) {
							virtual.yokoWallNum[yIndex][xIndex] = 0;
						} else {
							virtual.tateWallNum[yIndex][xIndex] = 0;
						}
						int solveResult = new MinarismSolverForGenerator(virtual, 1500).solve2();
						if (solveResult != -1) {
							if (toYokoWall) {
								wkField.yokoWallNum[yIndex][xIndex] = 0;
							} else {
								wkField.tateWallNum[yIndex][xIndex] = 0;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 20 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
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
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 数字情報
		// -2、-1は不等号(左上向き、右下向き)
		private final int[][] yokoWallNum;
		private final int[][] tateWallNum;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public String getHintCount() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public int[][] getYokoWall() {
			return yokoWallNum;
		}

		public int[][] getTateWall() {
			return tateWallNum;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbersCand = new ArrayList[height][width];
			// パラメータを解釈して壁の有無を入れる
			yokoWallNum = new int[height][width - 1];
			tateWallNum = new int[height - 1][width];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_I.indexOf(ch);
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
						} else if (ch == 'g') {
							num = -2;
						} else if (ch == 'h') {
							num = -1;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						if (index / (getXLength() - 1) < getYLength()) {
							Position pos = new Position(index / (getXLength() - 1), index % (getXLength() - 1));
							yokoWallNum[pos.getyIndex()][pos.getxIndex()] = (num);
						} else {
							Position pos = new Position((index - (getXLength() - 1) * getYLength()) / getXLength(),
									(index - (getXLength() - 1) * getYLength()) % getXLength());
							tateWallNum[pos.getyIndex()][pos.getxIndex()] = (num);
						}
					}
					index++;
				}
			}
			// 縦横の数字により、初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 1; i <= getYLength() || i <= getXLength(); i++) {
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
			yokoWallNum = other.yokoWallNum;
			tateWallNum = other.tateWallNum;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 1; i <= getYLength() || i <= getXLength(); i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
			yokoWallNum = new int[height][width - 1];
			tateWallNum = new int[height - 1][width];
		}

		@SuppressWarnings("unchecked")
		public Field(Field other, boolean flag) {
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			yokoWallNum = new int[other.getYLength()][other.getXLength() - 1];
			tateWallNum = new int[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWallNum[yIndex][xIndex] = other.yokoWallNum[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWallNum[yIndex][xIndex] = other.tateWallNum[yIndex][xIndex];
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
					if (xIndex != getXLength() - 1) {
						String numStr = String.valueOf(yokoWallNum[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						String numStr = String.valueOf(tateWallNum[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
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

		/**
		 * 同じ列・行にいる数字を候補から除外する。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							if (numbersCand[targetY][xIndex].size() == 1 && yIndex != targetY) {
								if (numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0))) {
								}
							}
						}
						for (int targetX = 0; targetX < getXLength(); targetX++) {
							if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
								if (numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0))) {
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
							if (getYLength() >= getXLength()) {
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
									break;
								}
							}
							isHiddenSingle = true;
							if (getXLength() >= getYLength()) {
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
									break;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 周辺数字の条件を満たさない数字を候補から除外する。
		 */
		private boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int upWallNum = yIndex == 0 ? 0 : tateWallNum[yIndex - 1][xIndex];
					int rightWallNum = xIndex == getXLength() - 1 ? 0 : yokoWallNum[yIndex][xIndex];
					int downWallNum = yIndex == getYLength() - 1 ? 0 : tateWallNum[yIndex][xIndex];
					int leftWallNum = xIndex == 0 ? 0 : yokoWallNum[yIndex][xIndex - 1];
					if (upWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex - 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					} else if (upWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex - 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					} else if (upWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + upWallNum);
							retainCand.add(cand - upWallNum);
						}
						numbersCand[yIndex - 1][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					}

					if (rightWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex + 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}
					} else if (rightWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex + 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}

					} else if (rightWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + rightWallNum);
							retainCand.add(cand - rightWallNum);
						}
						numbersCand[yIndex][xIndex + 1].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}
					}

					if (downWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex + 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
								if (numbersCand[yIndex + 1][xIndex].size() == 0) {
									return false;
								}
							}
						}
					} else if (downWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex + 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
							if (numbersCand[yIndex + 1][xIndex].size() == 0) {
								return false;
							}
						}

					} else if (downWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + downWallNum);
							retainCand.add(cand - downWallNum);
						}
						numbersCand[yIndex + 1][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex + 1][xIndex].size() == 0) {
							return false;
						}
					}

					if (leftWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex - 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
					} else if (leftWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex - 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
					} else if (leftWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + leftWallNum);
							retainCand.add(cand - leftWallNum);
						}
						numbersCand[yIndex][xIndex - 1].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex - 1].size() == 0) {
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
			if (!aroundSolve()) {
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

	public MinarismSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MinarismSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?minarism/5/5/2jhigjhkhj4l2r2ihi3l"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new MinarismSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 20));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 20).toString();
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