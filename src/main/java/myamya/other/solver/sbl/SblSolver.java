package myamya.other.solver.sbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Pipemasu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.UpLeftHints;
import myamya.other.solver.Solver;

public class SblSolver implements Solver {
	private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
	private static final String FULL_NUMS = "０１２３４５６７８９";

	public static class SblGenerator implements Generator {

		static class SblSolverForGenerator extends SblSolver {
			private final int limit;

			public SblSolverForGenerator(Field field, int limit) {
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

		public SblGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SblGenerator(7, 7).generate();
		}

		@Override
		public GeneratorResult generate() {
			Map<Position, Pipemasu> answerPipeMap;
			SblSolver.Field wkField = new SblSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int posBase = indexList.get(index);
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
					if ((toYokoWall && wkField.yokoWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							SblSolver.Field virtual = new SblSolver.Field(wkField);
							if (masuNum < 1) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (masuNum < 2) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new SblSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 解答の記憶
				answerPipeMap = new HashMap<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						Pipemasu pipe = Pipemasu.getByWall(
								(pos.getyIndex() == 0
										|| wkField.tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.NOT_EXISTS),
								(pos.getxIndex() == width - 1
										|| wkField.yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.NOT_EXISTS),
								(pos.getyIndex() == height - 1
										|| wkField.tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.NOT_EXISTS),
								(pos.getxIndex() == 0
										|| wkField.yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.NOT_EXISTS));
						answerPipeMap.put(new Position(pos.getyIndex() + 1, pos.getxIndex() + 1), pipe);
					}
				}

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
				List<Position> blackPosList = new ArrayList<Position>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							blackPosList.add(new Position(yIndex, xIndex));
						} else {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				if (blackPosList.size() == wkField.getYLength() * wkField.getXLength()) {
					// 全黒禁止
					wkField = new SblSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
					continue;
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new SblSolverForGenerator(new SblSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new SblSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(blackPosList);
					for (Position pos : blackPosList) {
						SblSolver.Field virtual = new SblSolver.Field(wkField);
						virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						int solveResult = new SblSolverForGenerator(virtual, 20000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							wkField.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						} else {
							// 消せなかった黒マスは解答判定からも除外
							answerPipeMap.remove(new Position(pos.getyIndex() + 1, pos.getxIndex() + 1));
						}
					}
					Collections.shuffle(hintPosList);
					for (Position pos : hintPosList) {
						SblSolver.Field virtual = new SblSolver.Field(wkField);
						if (pos.getyIndex() == -1) {
							virtual.upHints[pos.getxIndex()] = null;
						} else if (pos.getxIndex() == -1) {
							virtual.leftHints[pos.getyIndex()] = null;
						}
						int solveResult = new SblSolverForGenerator(virtual, 20000).solve2();
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
			String solutionStr = PenpaEditLib.convertSolutionYajilin(wkField.getYLength() + 1, answerPipeMap,
					new HashSet<>());
			String fieldStr = PenpaEditLib.convertHintsFieldSBL(height, wkField.masu, wkField.upHints,
					wkField.leftHints);
			// System.out.println(fieldStr);
			// System.out.println(solutionStr);

			level = (int) Math.sqrt(level / 3 / 5) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/黒マス：" + wkField.getHintCount() + ")";
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" stroke=\"" + "black" + "\" fill=\"" + "black"
								+ "\">" + "</rect>");
					}
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
		// マスの情報 trueなら黒
		protected Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;
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
			int kuro = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						kuro++;
					}
				}
			}
			return hintCnt + "/" + kuro;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public int getYLength() {
			return leftHints.length;
		}

		public int getXLength() {
			return upHints.length;
		}

		public Field(int height, int width) {
			upHints = new Integer[width];
			leftHints = new Integer[height];
			masu = new Masu[height][width];
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

		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			// ヒントの分余白があるので-1している
			Integer yLength = Integer.valueOf(fieldInfo[2]) - 1;
			Integer xLength = Integer.valueOf(fieldInfo[1]) - 1;
			UpLeftHints upLeftHints = PenpaEditLib.getUpLeftHints(fieldStr);
			upHints = upLeftHints.getUpHints();
			leftHints = upLeftHints.getLeftHints();
			// ヒントの余白があるので1つずつずらして入れる
			Masu[][] bassMasu = PenpaEditLib.getMasu(fieldStr);
			masu = new Masu[yLength][xLength];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = bassMasu[yIndex + 1][xIndex + 1];
				}
			}
			yokoWall = new Wall[yLength][xLength - 1];
			tateWall = new Wall[yLength - 1][xLength];
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

		public Field(Field other) {
			upHints = new Integer[other.getXLength()];
			leftHints = new Integer[other.getYLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				leftHints[yIndex] = other.leftHints[yIndex];
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				upHints[xIndex] = other.upHints[xIndex];
			}
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
		}

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
					sb.append(masu[yIndex][xIndex]);
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
		 * 黒マスは正方形になる。矛盾の場合false
		 */
		protected boolean sikakuSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 縦の確定の長さ
						int fixMinY = yIndex;
						int fixMaxY = yIndex;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] != Masu.BLACK) {
								break;
							}
							fixMinY = targetY;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] != Masu.BLACK) {
								break;
							}
							fixMaxY = targetY;
						}
						// 横で確保可能な長さ
						int candMinX = 0;
						int candMaxX = Integer.MAX_VALUE;
						for (int targetY = fixMinY; targetY <= fixMaxY; targetY++) {
							int wkMinX = xIndex;
							int wkMaxX = xIndex;
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
									break;
								}
								wkMinX = targetX;
							}
							for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
								if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
									break;
								}
								wkMaxX = targetX;
							}
							if (wkMinX > candMinX) {
								candMinX = wkMinX;
							}
							if (wkMaxX < candMaxX) {
								candMaxX = wkMaxX;
							}
						}
						if (fixMaxY - fixMinY > candMaxX - candMinX) {
							return false;
						}
//						if (fixMaxY - fixMinY == candMaxX - candMinX) {
//							for (int targetY = fixMinY; targetY <= fixMaxY; targetY++) {
//								for (int targetX = candMinX; targetX <= candMaxX; targetX++) {
//									masu[targetY][targetX] = Masu.BLACK;
//								}
//							}
//						}

						// 横の確定の長さ
						int fixMinX = xIndex;
						int fixMaxX = xIndex;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] != Masu.BLACK) {
								break;
							}
							fixMinX = targetX;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] != Masu.BLACK) {
								break;
							}
							fixMaxX = targetX;
						}

						// 縦で確保可能な長さ
						int candMinY = 0;
						int candMaxY = Integer.MAX_VALUE;
						for (int targetX = fixMinX; targetX <= fixMaxX; targetX++) {
							int wkMinY = yIndex;
							int wkMaxY = yIndex;
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
									break;
								}
								wkMinY = targetY;
							}
							for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
								if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
									break;
								}
								wkMaxY = targetY;
							}
							if (wkMinY > candMinY) {
								candMinY = wkMinY;
							}
							if (wkMaxY < candMaxY) {
								candMaxY = wkMaxY;
							}
						}
						if (fixMaxX - fixMinX > candMaxY - candMinY) {
							return false;
						}
//						if (fixMaxX - fixMinX == candMaxY - candMinY) {
//							for (int targetY = candMinY; targetY <= candMaxY; targetY++) {
//								for (int targetX = fixMinX; targetX <= fixMaxX; targetX++) {
//									masu[targetY][targetX] = Masu.BLACK;
//								}
//							}
//						}

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
		 * 白マスは壁2、黒マスは壁4
		 */
		protected boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか4マス
						if ((existsCount == 3 && notExistsCount == 1) || notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
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
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は必ず2マス
						if (existsCount > 2 || notExistsCount > 2) {
							return false;
						}
						if (notExistsCount == 2) {
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
						} else if (existsCount == 2) {
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
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!hintsSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!sikakuSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
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

	// penpa-edit向けコンストラクタ
	public SblSolver(String fieldStr) {
		this.field = new Field(fieldStr);
	}

	public SblSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "\"square,11,11,38,0,1,1,456,456,112,112\n" + "[1,0,1,0]\n"
				+ "[\"1\",\"2\",\"1\"]~\"combi\"~[\"yajilin\",\"\"]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{\"78\":1,\"82\":1,\"129\":1,\"139\":1},zN:{\"36\":[\"0\",1,\"1\"],\"37\":[\"5\",1,\"1\"],\"38\":[\"2\",1,\"1\"],\"41\":[\"6\",1,\"1\"],\"42\":[\"5\",1,\"1\"],\"62\":[\"7\",1,\"1\"],\"92\":[\"0\",1,\"1\"],\"122\":[\"1\",1,\"1\"],\"137\":[\"\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[48,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,1,1,1]\"";
		System.out.println(new SblSolver(fieldStr).solve());
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
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3 / 5) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		// System.out.println(field);
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
