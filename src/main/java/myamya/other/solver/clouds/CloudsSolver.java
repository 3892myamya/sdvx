package myamya.other.solver.clouds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.UpLeftHints;
import myamya.other.solver.Solver;

public class CloudsSolver implements Solver {
	private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
	private static final String FULL_NUMS = "０１２３４５６７８９";

	public static class CloudsGenerator implements Generator {

		static class CloudsSolverForGenerator extends CloudsSolver {
			private final int limit;

			public CloudsSolverForGenerator(Field field, int limit) {
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

		public CloudsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new CloudsGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			CloudsSolver.Field wkField = new CloudsSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					if (index >= height * width) {
						wkField = new CloudsSolver.Field(height, width);
						index = 0;
						continue;
					}
					int yIndex = indexList.get(index) / width;
					int xIndex = indexList.get(index) % width;
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							CloudsSolver.Field virtual = new CloudsSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new CloudsSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 解答の記憶
				solutionStr = PenpaEditLib.convertSolutionMasuHintsBw(wkField.masu);
				List<Position> hintPosList = new ArrayList<Position>();
				// 縦のヒント
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int cnt = 0;
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							cnt++;
						}
					}
					wkField.leftHints[yIndex] = cnt;
					hintPosList.add(new Position(yIndex, -1));
				}
				// 横のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int cnt = 0;
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							cnt++;
						}
					}
					wkField.upHints[xIndex] = cnt;
					hintPosList.add(new Position(-1, xIndex));
				}
				boolean blackExists = false;
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							blackExists = true;
						}
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				if (!blackExists) {
					// 黒がなければやり直し
					wkField = new CloudsSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
					continue;
				}
				// 解けるかな？
				level = new CloudsSolverForGenerator(new CloudsSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new CloudsSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(hintPosList);
					for (Position pos : hintPosList) {
						CloudsSolver.Field virtual = new CloudsSolver.Field(wkField, true);
						if (pos.getyIndex() == -1) {
							virtual.upHints[pos.getxIndex()] = null;
						} else if (pos.getxIndex() == -1) {
							virtual.leftHints[pos.getyIndex()] = null;
						}
						int solveResult = new CloudsSolverForGenerator(virtual, 20000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							if (pos.getyIndex() == -1) {
								wkField.upHints[pos.getxIndex()] = null;
							} else if (pos.getxIndex() == -1) {
								wkField.leftHints[pos.getyIndex()] = null;
							}
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertHintsField(height, wkField.upHints, wkField.leftHints);
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);

			level = (int) Math.sqrt(level / 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount().split("/")[0] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
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
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
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
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
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
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		// マスの情報
		protected Masu[][] masu;
		// ヒント情報
		protected Integer[] upHints;
		protected Integer[] leftHints;

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
		}

		public String getHintCount() {
			int hintCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					hintCnt++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					hintCnt++;
				}
			}
			return String.valueOf(hintCnt);
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			upHints = new Integer[other.getXLength()];
			leftHints = new Integer[other.getYLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				leftHints[yIndex] = other.leftHints[yIndex];
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				upHints[xIndex] = other.upHints[xIndex];
			}
		}

		/**
		 * プレーンなフィールド生成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			upHints = new Integer[width];
			leftHints = new Integer[height];
		}

		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			// ヒントの分余白があるので-1している
			Integer yLength = Integer.valueOf(fieldInfo[2]) - 1;
			Integer xLength = Integer.valueOf(fieldInfo[1]) - 1;
			masu = new Masu[yLength][xLength];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			UpLeftHints upLeftHints = PenpaEditLib.getUpLeftHints(fieldStr);
			upHints = upLeftHints.getUpHints();
			leftHints = upLeftHints.getLeftHints();

//			masu = new Masu[8][8];
//			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
//					masu[yIndex][xIndex] = Masu.SPACE;
//				}
//			}
//			upHints = new Integer[8];
//			leftHints = new Integer[8];
//
//			upHints[0] = 6;
//			upHints[1] = 6;
//			upHints[2] = 3;
//			upHints[4] = 5;
//			upHints[5] = 2;
//			upHints[6] = 6;
//
//			leftHints[0] = 6;
//			leftHints[1] = 6;
//			leftHints[2] = 3;
//			leftHints[3] = 2;
//			leftHints[5] = 4;
//			leftHints[7] = 2;

		}

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
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。
		 */
		protected boolean cloudsSolve() {
			boolean advance = false;
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
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
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
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
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			if (advance) {
				return cloudsSolve();
			}
			// 幅が1になったらアウト
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (masuUp == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK) {
							return false;
						}
						if (masuUp == Masu.SPACE && masuDown == Masu.NOT_BLACK) {
							masu[yIndex - 1][xIndex] = Masu.BLACK;
						}
						if (masuUp == Masu.NOT_BLACK && masuDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.BLACK;
						}
						if (masuRight == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
							return false;
						}
						if (masuRight == Masu.SPACE && masuLeft == Masu.NOT_BLACK) {
							masu[yIndex][xIndex + 1] = Masu.BLACK;
						}
						if (masuRight == Masu.NOT_BLACK && masuLeft == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.BLACK;
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						if (masuUp == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuRight == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * hintsは黒マスの数を示す。
		 */
		private boolean hintsSolve() {
//			 縦のヒント
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (leftHints[yIndex] < blackCnt) {
						return false;
					}
					if (leftHints[yIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (leftHints[yIndex] == blackCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (leftHints[yIndex] == blackCnt + spaceCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			// 横のヒント
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (upHints[xIndex] < blackCnt) {
						return false;
					}
					if (upHints[xIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (upHints[xIndex] == blackCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (upHints[xIndex] == blackCnt + spaceCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!cloudsSolve()) {
				return false;
			}
			if (!hintsSolve()) {
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return true;
		}

	}

	protected final Field field;
	protected int count = 0;

	// penpa-edit向けコンストラクタ
	public CloudsSolver(String fieldStr) {
		this.field = new Field(fieldStr);
	}

	public CloudsSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,13,13,38,0,1,1,532,532,144,144\n" + "[1,0,1,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"38\":[\"2\",1,\"1\"],\"42\":[\"3\",1,\"1\"],\"43\":[\"4\",1,\"1\"],\"47\":[\"3\",1,\"1\"],\"48\":[\"4\",1,\"1\"],\"53\":[\"3\",1,\"1\"],\"104\":[\"10\",1,\"1\"],\"155\":[\"4\",1,\"1\"],\"206\":[\"7\",1,\"1\"],\"240\":[\"1\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[54,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,1,1]";
		System.out.println(new CloudsSolver(fieldStr).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		System.out.println(field);
		while (!field.isSolved()) {
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
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
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			count++;
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
		}
		return true;
	}

}
