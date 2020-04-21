package myamya.other.solver.tents;

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

public class TentsSolver implements Solver {

	public static class TentsGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class TentsSolverForGenerator extends TentsSolver {
			private final int limit;

			public TentsSolverForGenerator(Field field, int limit) {
				super(field);
				this.field.firstSolve();
				this.limit = limit;
			}

			public int solve2() {
				try {
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

		public TentsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TentsGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			TentsSolver.Field wkField = new TentsSolver.Field(height, width);
			int level = 0;
			long start = System.nanoTime();
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			while (true) {
				// 問題生成部
				Collections.shuffle(indexList);
				for (int index : indexList) {
					int yIndex = index / width;
					int xIndex = index % width;
					// 木とテントを決める。
					// 木になれる条件
					// ・自分が未確定マスであること
					// ・自分の周りに黒マスが1つ以上置ける余地があること
					// 上記の条件を満たしたとき、1/3の確率で木になる。
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						List<Position> canBlack = new ArrayList<>();
						if (wkField.canBlack(yIndex - 1, xIndex)) {
							canBlack.add(new Position(yIndex - 1, xIndex));
						}
						if (wkField.canBlack(yIndex, xIndex + 1)) {
							canBlack.add(new Position(yIndex, xIndex + 1));
						}
						if (wkField.canBlack(yIndex + 1, xIndex)) {
							canBlack.add(new Position(yIndex + 1, xIndex));
						}
						if (wkField.canBlack(yIndex, xIndex - 1)) {
							canBlack.add(new Position(yIndex, xIndex - 1));
						}
						if (!canBlack.isEmpty()) {
							if (Math.random() * 3 < 1) {
								Collections.shuffle(canBlack);
								wkField.trees[yIndex][xIndex] = true;
								wkField.masu[canBlack.get(0).getyIndex()][canBlack.get(0).getxIndex()] = Masu.BLACK;
							}
						}
					}
				}
				// ヒント決定
				// 縦のヒント
				List<Integer> hintIndexList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int hint = 0;
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							hint++;
						}
					}
					wkField.leftHints[yIndex] = hint;
					hintIndexList.add(hintIndexList.size());
				}
				// 横のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int hint = 0;
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							hint++;
						}
					}
					wkField.upHints[xIndex] = hint;
					hintIndexList.add(hintIndexList.size());
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new TentsSolverForGenerator(wkField, 2000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new TentsSolver.Field(height, width);
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(hintIndexList);
					for (int hintIndex : hintIndexList) {
						TentsSolver.Field virtual = new TentsSolver.Field(wkField, true);
						if (hintIndex >= height) {
							virtual.upHints[hintIndex - height] = null;
						} else {
							virtual.leftHints[hintIndex] = null;
						}
						int solveResult = new TentsSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							if (hintIndex >= height) {
								wkField.upHints[hintIndex - height] = null;
							} else {
								wkField.leftHints[hintIndex] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(木:" + wkField.getHintCount().split("/")[0] + "、数字："
					+ wkField.getHintCount().split("/")[1] + ")";
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getTrees()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + 13)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize + 8)
								+ "\" width=\""
								+ (baseSize / 5)
								+ "\" height=\""
								+ (baseSize / 5)
								+ "\" fill=\"black\" >"
								+ "</rect>");
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + 8)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 4)
								+ "\" fill=\"green\", stroke=\"black\">"
								+ "</circle>");
					} else if (wkField.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ "△"
								+ "</text>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ wkField.getMasu()[yIndex][xIndex].toString()
								+ "</text>");
					}
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

	}

	public static class Field {
		static final String NUMBER_AND_ALPHABET = "0123456789abcdefgh";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;

		// 木マス
		private boolean[][] trees;

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] leftHints;

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getTrees() {
			return trees;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		/**
		 * あるマスが新たに黒マスになれるか調べる。なれる場合trueを返す。ジェネレータ用
		 */
		public boolean canBlack(int yIndex, int xIndex) {
			if (yIndex < 0 || yIndex >= getYLength() || xIndex < 0 || xIndex >= getXLength()) {
				// はみ出してたら当然、無理
				return false;
			}
			if (trees[yIndex][xIndex] || masu[yIndex][xIndex] == Masu.BLACK) {
				// 既に木や黒マスに決まっていても無理
				return false;
			}
			Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
			Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
			Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
			Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
			Masu masuUpRight = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
					: masu[yIndex - 1][xIndex + 1];
			Masu masuRightDown = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
					: masu[yIndex + 1][xIndex + 1];
			Masu masuDownLeft = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
					: masu[yIndex + 1][xIndex - 1];
			Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
			if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
					|| masuLeft == Masu.BLACK || masuUpRight == Masu.BLACK || masuRightDown == Masu.BLACK
					|| masuDownLeft == Masu.BLACK || masuLeftUp == Masu.BLACK) {
				// 自分の周りのどこかが黒ますだったらムリ
				return false;
			}
			return true;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?tents/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					Integer num = upHints[xIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				} else {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					Integer num = leftHints[yIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				} else {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			interval = 0;
			boolean fallTree = false;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (trees[yIndex][xIndex]) {
					if (!fallTree) {
						if (interval != 0) {
							sb.append(ALPHABET_FROM_I.charAt(interval - 1));
						}
					} else {
						sb.append(NUMBER_AND_ALPHABET.charAt(interval));
					}
					fallTree = true;
					interval = 0;
				} else {
					interval++;
					if (interval == 18) {
						if (fallTree) {
							sb.append("h");
						} else {
							sb.append("z");
						}
						fallTree = false;
						interval = 0;
					}
				}
			}
			if (!fallTree) {
				if (interval != 0) {
					sb.append(ALPHABET_FROM_I.charAt(interval - 1));
				}
			} else {
				sb.append(NUMBER_AND_ALPHABET.charAt(interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int treesCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (trees[yIndex][xIndex]) {
						treesCnt++;
					}
				}
			}
			int hintsCnt = 0;
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					hintsCnt++;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					hintsCnt++;
				}
			}
			return treesCnt + "/" + hintsCnt;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			trees = new boolean[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					trees[yIndex][xIndex] = false;
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
			trees = new boolean[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
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
				//16 - 255は '-'
				//256 - 999は '+'
				int capacity;
				if (ch == '.') {
					//
				} else {
					int interval;
					if (index < width + height) {
						interval = ALPHABET_FROM_G.indexOf(ch);
						if (interval != -1) {
							index = index + interval;
						} else {
							if (ch == '-') {
								capacity = Character.getNumericValue(param.charAt(i + 1)) * 16
										+ Character.getNumericValue(param.charAt(i + 2));
								i++;
								i++;
							} else {
								capacity = Character.getNumericValue(ch);
							}
							if (index < width) {
								upHints[index] = capacity;
							} else if (index < width + height) {
								leftHints[index - width] = capacity;
							}
						}
					} else {
						boolean fallTree = false;
						interval = NUMBER_AND_ALPHABET.indexOf(ch);
						if (interval != -1) {
							// 0-hにあれば、木をおいて間隔を空ける
							fallTree = true;
						} else {
							// 0-hにない(i-z)であれば、木をおかず間隔を空ける
							interval = ALPHABET_FROM_I.indexOf(ch);
						}
						if (fallTree) {
							int indexBase = index - width - height;
							int xIndex = indexBase % width;
							int yIndex = indexBase / width;
							trees[yIndex][xIndex] = true;
						}
						index = index + interval;
					}

				}
				index++;
			}
			firstSolve();
		}

		private void firstSolve() {
			/**
			 * 前後左右に木がなければ白マスが確定し、
			 * 木が連続している場合は壁が確定する。
			 */
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean mytree = trees[yIndex][xIndex];
					boolean treeUp = yIndex == 0 ? false : trees[yIndex - 1][xIndex];
					boolean treeRight = xIndex == getXLength() - 1 ? false : trees[yIndex][xIndex + 1];
					boolean treeDown = yIndex == getYLength() - 1 ? false : trees[yIndex + 1][xIndex];
					boolean treeLeft = xIndex == 0 ? false : trees[yIndex][xIndex - 1];
					if (mytree) {
						if (treeUp) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (treeRight) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (treeDown) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (treeLeft) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else {
						if (!treeUp && !treeRight && !treeDown && !treeLeft) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
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
			trees = other.trees;
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		/**
		 * trees等をイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			trees = new boolean[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			upHints = new Integer[other.getXLength()];
			leftHints = new Integer[other.getYLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					trees[yIndex][xIndex] = other.trees[yIndex][xIndex];
				}
			}
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
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				upHints[xIndex] = other.upHints[xIndex];
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				leftHints[yIndex] = other.leftHints[yIndex];
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　　");
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
				sb.append("　");
			}
			sb.append(System.lineSeparator());
			sb.append("　　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
				sb.append("　");
			}
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
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
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (trees[yIndex][xIndex]) {
						sb.append("木");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				sb.append("　　");
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!treesSolve()) {
				return false;
			}
			if (!tentsSolve()) {
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

		/**
		 * 木・黒マス(テント)は壁が3つ、白マスは壁が4つ。
		 * 違反する場合はfalseを返す。
		 */
		private boolean treesSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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
					if (notExists > 1) {
						// いつでも壁2枚以上はNG
						return false;
					}
					if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
						// 壁枚数からマスを逆算
						if (notExists > 0) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (exists == 4) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}

					if (trees[yIndex][xIndex] || masu[yIndex][xIndex] == Masu.BLACK) {
						// 木かテントマスは壁3枚
						if (exists > 3 || notExists > 1) {
							return false;
						}
						if (exists == 3) {
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
						} else if (notExists == 1) {
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
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
			return true;
		}

		/**
		 * テント(黒マス)は周囲8マスに隣あってはダメ。
		 * 違反する場合はfalseを返す。
		 */
		private boolean tentsSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRightDown = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDownLeft = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK || masuUpRight == Masu.BLACK || masuRightDown == Masu.BLACK
								|| masuDownLeft == Masu.BLACK || masuLeftUp == Masu.BLACK) {
							return false;
						}
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
						if (masuUpRight == Masu.SPACE) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuRightDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDownLeft == Masu.SPACE) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLeftUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 外周のヒントはテント(黒マス)の数。違反する場合はfalseを返す。
		 */
		private boolean hintSolve() {
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					int exists = 0;
					int notExists = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							exists++;
						} else if (trees[yIndex][xIndex] || masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							notExists++;
						}
					}
					if (exists > upHints[xIndex]) {
						// テント多すぎ
						return false;
					}
					if (getYLength() - notExists < upHints[xIndex]) {
						// テント少なすぎ
						return false;
					}
					if (exists == upHints[xIndex]) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (getYLength() - notExists == upHints[xIndex]) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					int exists = 0;
					int notExists = 0;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							exists++;
						} else if (trees[yIndex][xIndex] || masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							notExists++;
						}
					}
					if (exists > leftHints[yIndex]) {
						// テント多すぎ
						return false;
					}
					if (getXLength() - notExists < leftHints[yIndex]) {
						// テント少なすぎ
						return false;
					}
					if (exists == leftHints[yIndex]) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (getXLength() - notExists == leftHints[yIndex]) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!trees[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.SPACE) {
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

	public TentsSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TentsSolver(Field field) {
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
		System.out.println(new TentsSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 5 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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