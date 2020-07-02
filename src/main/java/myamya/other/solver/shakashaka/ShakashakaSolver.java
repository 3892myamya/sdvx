package myamya.other.solver.shakashaka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class ShakashakaSolver implements Solver {

	public static class ShakashakaGenerator implements Generator {
		static class ShakashakaSolverForGenerator extends ShakashakaSolver {
			private final int limit;

			public ShakashakaSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			/**
			 * -2:解なし
			 * -1:limit(多くの場合複数解)
			 * 0 >= 唯一解
			 */
			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						int recursiveCnt = 0;
						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
							if (!candSolve(field, recursiveCnt)) {
								return -2;
							}
							recursiveCnt++;
						}
						if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
							return -1;
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

		public ShakashakaGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ShakashakaGenerator(10, 10).generate();
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public GeneratorResult generate() {
			ShakashakaSolver.Field wkField = new ShakashakaSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			int level = 0;
			int failCnt = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				List<Position> numberPosList = new ArrayList<>();
				while (true) {
					// ランダムに１マス選ぶ
					int index = indexList.get((int) (Math.random() * indexList.size()));
					Position targetPos = new Position(index / width, index % width);
					// 既に数字配置済なら選び直し
					if (wkField.getNumbers()[targetPos.getyIndex()][targetPos.getxIndex()] != null) {
						continue;
					}
					// 前後左右に数字配置済だったり、囲むような配置の場合は高確率でやり直し
					if (targetPos.getyIndex() != 0) {
						Position pivot = new Position(targetPos.getyIndex() - 1, targetPos.getxIndex());
						if (wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex()] != null
								&& Math.random() < 0.95) {
							continue;
						}
						int aroundCnt = 0;
						if (pivot.getyIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == wkField.getXLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
							aroundCnt++;
						}
						if (pivot.getyIndex() == wkField.getYLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
							aroundCnt++;
						}
						if (aroundCnt >= 2 && Math.random() < 0.8) {
							continue;
						}
					}
					if (targetPos.getxIndex() != wkField.getXLength() - 1) {
						Position pivot = new Position(targetPos.getyIndex(), targetPos.getxIndex() + 1);
						if (wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex()] != null
								&& Math.random() < 0.95) {
							continue;
						}
						int aroundCnt = 0;
						if (pivot.getyIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == wkField.getXLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
							aroundCnt++;
						}
						if (pivot.getyIndex() == wkField.getYLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
							aroundCnt++;
						}
						if (aroundCnt >= 2 && Math.random() < 0.8) {
							continue;
						}
					}
					if (targetPos.getyIndex() != wkField.getYLength() - 1) {
						Position pivot = new Position(targetPos.getyIndex() + 1, targetPos.getxIndex());
						if (wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex()] != null
								&& Math.random() < 0.95) {
							continue;
						}
						int aroundCnt = 0;
						if (pivot.getyIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == wkField.getXLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
							aroundCnt++;
						}
						if (pivot.getyIndex() == wkField.getYLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
							aroundCnt++;
						}
						if (aroundCnt >= 2 && Math.random() < 0.8) {
							continue;
						}
					}
					if (targetPos.getxIndex() != 0) {
						Position pivot = new Position(targetPos.getyIndex(), targetPos.getxIndex() - 1);
						if (wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex()] != null
								&& Math.random() < 0.95) {
							continue;
						}
						int aroundCnt = 0;
						if (pivot.getyIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == wkField.getXLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
							aroundCnt++;
						}
						if (pivot.getyIndex() == wkField.getYLength() - 1
								|| wkField.getNumbers()[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
							aroundCnt++;
						}
						if (pivot.getxIndex() == 0
								|| wkField.getNumbers()[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
							aroundCnt++;
						}
						if (aroundCnt >= 2 && Math.random() < 0.8) {
							continue;
						}
					}
					// 数字を配置してチェック
					ShakashakaSolver.Field virtual = new ShakashakaSolver.Field(wkField, true);
					virtual.numbers[targetPos.getyIndex()][targetPos.getxIndex()] = (int) (Math.random() * 5);
					if (targetPos.getyIndex() != 0) {
						virtual.tateWall[targetPos.getyIndex() - 1][targetPos.getxIndex()] = Wall.EXISTS;
					}
					if (targetPos.getxIndex() != virtual.getXLength() - 1) {
						virtual.yokoWall[targetPos.getyIndex()][targetPos.getxIndex()] = Wall.EXISTS;
					}
					if (targetPos.getyIndex() != virtual.getYLength() - 1) {
						virtual.tateWall[targetPos.getyIndex()][targetPos.getxIndex()] = Wall.EXISTS;
					}
					if (targetPos.getxIndex() != 0) {
						virtual.yokoWall[targetPos.getyIndex()][targetPos.getxIndex() - 1] = Wall.EXISTS;
					}
					// 解けるかチェック
					int solveResult = new ShakashakaSolverForGenerator(virtual, 500).solve2();
					if (solveResult == -2) {
						failCnt++;
						if (failCnt > 500) {
							failCnt = 0;
							wkField = new ShakashakaSolver.Field(height, width);
							numberPosList.clear();
						}
						continue;
					} else {
						wkField.numbers[targetPos.getyIndex()][targetPos
								.getxIndex()] = virtual.numbers[targetPos.getyIndex()][targetPos.getxIndex()];
						if (targetPos.getyIndex() != 0) {
							wkField.tateWall[targetPos.getyIndex() - 1][targetPos.getxIndex()] = Wall.EXISTS;
						}
						if (targetPos.getxIndex() != wkField.getXLength() - 1) {
							wkField.yokoWall[targetPos.getyIndex()][targetPos.getxIndex()] = Wall.EXISTS;
						}
						if (targetPos.getyIndex() != wkField.getYLength() - 1) {
							wkField.tateWall[targetPos.getyIndex()][targetPos.getxIndex()] = Wall.EXISTS;
						}
						if (targetPos.getxIndex() != 0) {
							wkField.yokoWall[targetPos.getyIndex()][targetPos.getxIndex() - 1] = Wall.EXISTS;
						}
						numberPosList.add(targetPos);
						if (solveResult != -1 && Math.random() < 0.9) {
							level = solveResult;
							break;
						}
					}
				}
				// ヒントを限界まで減らす
				Collections.shuffle(numberPosList);
				for (Position numberPos : numberPosList) {
					ShakashakaSolver.Field virtual = new ShakashakaSolver.Field(wkField, true);
					virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
					int solveResult = new ShakashakaSolverForGenerator(virtual, 500).solve2();
					if (solveResult >= 0) {
						wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						level = solveResult;
					}
				}
				break;

			}
			level = (int) Math.sqrt(level * 20 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ FULL_NUMS.substring(wkField.getNumbers()[yIndex][xIndex],
											wkField.getNumbers()[yIndex][xIndex] + 1)
									+ "</text>");
						}
					}
				}
			}

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
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?shakashaka/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = numbers[yIndex][xIndex];
					String numStr = null;
					if (num == -1) {
						numStr = ".";
					} else {
						Integer numP1 = null;
						if (i + 1 < getYLength() * getXLength()) {
							int yIndexP1 = (i + 1) / getXLength();
							int xIndexP1 = (i + 1) % getXLength();
							numP1 = numbers[yIndexP1][xIndexP1];
						}
						Integer numP2 = null;
						if (numP1 == null && i + 2 < getYLength() * getXLength()) {
							int yIndexP2 = (i + 2) / getXLength();
							int xIndexP2 = (i + 2) % getXLength();
							numP2 = numbers[yIndexP2][xIndexP2];
						}
						if (numP1 == null && numP2 == null) {
							if (num == 0) {
								numStr = "a";
							} else if (num == 1) {
								numStr = "b";
							} else if (num == 2) {
								numStr = "c";
							} else if (num == 3) {
								numStr = "d";
							} else if (num == 4) {
								numStr = "e";
							}
							i++;
							i++;
						} else if (numP1 == null) {
							if (num == 0) {
								numStr = "5";
							} else if (num == 1) {
								numStr = "6";
							} else if (num == 2) {
								numStr = "7";
							} else if (num == 3) {
								numStr = "8";
							} else if (num == 4) {
								numStr = "9";
							}
							i++;
						} else {
							numStr = String.valueOf(num);
						}
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
			int kuroCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != -1) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + kuroCnt);
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					// 数字なし黒マス。周囲の壁が確定
					numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					if (pos.getyIndex() != 0) {
						tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getxIndex() != getXLength() - 1) {
						yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getyIndex() != getYLength() - 1) {
						tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getxIndex() != 0) {
						yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
					}
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						// 数字あり黒マス。周囲の壁が確定
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

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
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("○");
						} else {
							sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
									numbers[yIndex][xIndex] + 1));
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
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
					sb.append(masu[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!rectSolve()) {
				return false;
			}
			if (!whiteWallSolve()) {
				return false;
			}
			if (!finalSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * つながった黒マスは長方形になるように配置する。
		 * 長方形にできなかった場合はfalseを返す。
		 */
		public boolean rectSolve() {
			boolean advance = false;
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = numbers[yIndex][xIndex] != null ? Masu.NOT_BLACK : masu[yIndex][xIndex];
					Masu masu2 = numbers[yIndex][xIndex + 1] != null ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
					Masu masu3 = numbers[yIndex + 1][xIndex] != null ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
					Masu masu4 = numbers[yIndex + 1][xIndex + 1] != null ? Masu.NOT_BLACK
							: masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.NOT_BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						advance = true;
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			if (advance) {
				return rectSolve();
			}
			return true;
		}

		/**
		 * 柱から伸びる壁は1にならない。矛盾する場合falseを返す。
		 */
		private boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					int exists = 0;
					int notExists = 0;
					Wall wall1 = tateWall[yIndex][xIndex];
					Wall wall2 = tateWall[yIndex][xIndex + 1];
					Wall wall3 = yokoWall[yIndex][xIndex];
					Wall wall4 = yokoWall[yIndex + 1][xIndex];
					if (wall1 == Wall.EXISTS) {
						exists++;
					} else if (wall1 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall2 == Wall.EXISTS) {
						exists++;
					} else if (wall2 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall3 == Wall.EXISTS) {
						exists++;
					} else if (wall3 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall4 == Wall.EXISTS) {
						exists++;
					} else if (wall4 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (exists == 1 && notExists == 3) {
						return false;
					}
					if (notExists == 3) {
						if (wall1 == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wall2 == Wall.SPACE) {
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
						}
						if (wall3 == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wall4 == Wall.SPACE) {
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
						}
					}
					if (notExists == 2 && exists == 1) {
						if (wall1 == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wall2 == Wall.SPACE) {
							tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
						}
						if (wall3 == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wall4 == Wall.SPACE) {
							yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの壁枚数は4、白マスの壁枚数は0枚、2枚(対面でないこと)のいずれかになる。
		 * 違反する場合はfalseを返す。
		 */
		private boolean whiteWallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						int exists = 0;
						int notExists = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							exists++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							exists++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							exists++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							exists++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if (exists > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (notExists > 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (exists > 2
									|| (exists == 1 && notExists == 3)) {
								return false;
							} else if (exists > 0) {
								// 壁2枚が確定。
								if ((wallUp == Wall.EXISTS && wallDown == Wall.EXISTS)
										|| (wallRight == Wall.EXISTS
												&& wallLeft == Wall.EXISTS)) {
									return false;
								} else if ((wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
										|| (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
									return false;
								}
								if (wallUp == Wall.EXISTS && wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.EXISTS && wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.EXISTS && wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.EXISTS && wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.NOT_EXISTS && wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.NOT_EXISTS && wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (notExists > 2) {
								// 壁なしが確定。
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							}
						} else if (masu[yIndex][xIndex] == Masu.BLACK) {
							if (notExists > 0) {
								return false;
							}
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスは上下左右にあるマスについて、白マスがいくつあるかを示している。
		 * 矛盾する場合はfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 || numbers[yIndex - 1][xIndex] != null ? Masu.BLACK
								: masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 || numbers[yIndex][xIndex + 1] != null ? Masu.BLACK
								: masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 || numbers[yIndex + 1][xIndex] != null ? Masu.BLACK
								: masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 || numbers[yIndex][xIndex - 1] != null ? Masu.BLACK
								: masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < whiteCnt) {
							// 白マス過剰
							return false;
						}
						if (numbers[yIndex][xIndex] == whiteCnt) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
						}
						if (numbers[yIndex][xIndex] > 4 - blackCnt) {
							// 白マス不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - blackCnt) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * フィールドに1つは白マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK && numbers[yIndex][xIndex] == null) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] == null) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public ShakashakaSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public ShakashakaSolver(Field field) {
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
		System.out.println(new ShakashakaSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 20));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 20 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 20).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE && field.numbers[yIndex][xIndex] == null) {
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}