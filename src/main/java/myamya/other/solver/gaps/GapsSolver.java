package myamya.other.solver.gaps;

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

public class GapsSolver implements Solver {
	private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
	private static final String FULL_NUMS = "０１２３４５６７８９";

	public static class GapsGenerator implements Generator {

		static class GapsSolverForGenerator extends GapsSolver {
			private final int limit;

			public GapsSolverForGenerator(Field field, int limit) {
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

		public GapsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new GapsGenerator(12, 12).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			GapsSolver.Field wkField = new GapsSolver.Field(height, width);
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
							GapsSolver.Field virtual = new GapsSolver.Field(wkField);
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
							wkField = new GapsSolver.Field(height, width);
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
					int blackPos = -1;
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							if (blackPos == -1) {
								blackPos = xIndex;
							} else {
								wkField.leftHints[yIndex] = xIndex - blackPos - 1;
								break;
							}
						}
					}
					hintPosList.add(new Position(yIndex, -1));
				}
				// 横のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int blackPos = -1;
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							if (blackPos == -1) {
								blackPos = yIndex;
							} else {
								wkField.upHints[xIndex] = yIndex - blackPos - 1;
								break;
							}
						}
					}
					hintPosList.add(new Position(-1, xIndex));
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new GapsSolverForGenerator(new GapsSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new GapsSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(hintPosList);
					for (Position pos : hintPosList) {
						GapsSolver.Field virtual = new GapsSolver.Field(wkField, true);
						if (pos.getyIndex() == -1) {
							virtual.upHints[pos.getxIndex()] = null;
						} else if (pos.getxIndex() == -1) {
							virtual.leftHints[pos.getyIndex()] = null;
						}
						int solveResult = new GapsSolverForGenerator(virtual, 20000).solve2();
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

			level = (int) Math.sqrt(level / 3) + 1;
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
		// ヒント情報。
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
//
//			masu = new Masu[12][12];
//			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
//					masu[yIndex][xIndex] = Masu.SPACE;
//				}
//			}
//			upHints = new Integer[12];
//			leftHints = new Integer[12];
//
//			upHints[1] = 2;
//			upHints[5] = 3;
//			upHints[6] = 4;
//			upHints[10] = 3;
//			upHints[11] = 4;
//
//			leftHints[0] = 3;
//			leftHints[3] = 10;
//			leftHints[6] = 4;
//			leftHints[9] = 7;
//			leftHints[11] = 1;

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
		 * 縦・横のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				// 横方向調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < 2) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = 2 - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}

			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				// 縦方向調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < 2) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = 2 - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * Gapsは黒マスと黒マスの間の白マスの数を示す。
		 */
		private boolean gapsSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					// 横方向調査
					int blackXIndex = -1;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackXIndex = xIndex;
							break;
						}
					}
					if (blackXIndex != -1) {
						boolean leftCand = blackXIndex - leftHints[yIndex] - 1 >= 0
								&& masu[yIndex][blackXIndex - leftHints[yIndex] - 1] != Masu.NOT_BLACK;
						boolean rightCand = blackXIndex + leftHints[yIndex] + 1 < getXLength()
								&& masu[yIndex][blackXIndex + leftHints[yIndex] + 1] != Masu.NOT_BLACK;
						if (!leftCand && !rightCand) {
							return false;
						}
						if (leftCand && !rightCand) {
							masu[yIndex][blackXIndex - leftHints[yIndex] - 1] = Masu.BLACK;
						}
						if (!leftCand && rightCand) {
							masu[yIndex][blackXIndex + leftHints[yIndex] + 1] = Masu.BLACK;
						}
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (xIndex == blackXIndex || xIndex == blackXIndex - leftHints[yIndex] - 1
									|| xIndex == blackXIndex + leftHints[yIndex] + 1) {
								continue;
							}
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					// 縦方向調査
					int blackYIndex = -1;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackYIndex = yIndex;
							break;
						}
					}
					if (blackYIndex != -1) {
						boolean upCand = blackYIndex - upHints[xIndex] - 1 >= 0
								&& masu[blackYIndex - upHints[xIndex] - 1][xIndex] != Masu.NOT_BLACK;
						boolean downCand = blackYIndex + upHints[xIndex] + 1 < getXLength()
								&& masu[blackYIndex + upHints[xIndex] + 1][xIndex] != Masu.NOT_BLACK;
						if (!upCand && !downCand) {
							return false;
						}
						if (upCand && !downCand) {
							masu[blackYIndex - upHints[xIndex] - 1][xIndex] = Masu.BLACK;
						}
						if (!upCand && downCand) {
							masu[blackYIndex + upHints[xIndex] + 1][xIndex] = Masu.BLACK;
						}
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (yIndex == blackYIndex || yIndex == blackYIndex - upHints[xIndex] - 1
									|| yIndex == blackYIndex + upHints[xIndex] + 1) {
								continue;
							}
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 星の周りを白マスに確定する。
		 */
		public boolean roundSolve() {
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
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!roundSolve()) {
				return false;
			}
			if (!gapsSolve()) {
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
	public GapsSolver(String fieldStr) {
		this.field = new Field(fieldStr);
	}

	public GapsSolver(Field field) {
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
		System.out.println(new GapsSolver(fieldStr).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
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
