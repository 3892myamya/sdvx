package myamya.other.solver.kurarin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class KurarinSolver implements Solver {

	public static class KurarinGenerator implements Generator {

		static class KurarinSolverForGenerator extends KurarinSolver {
			private final int limit;

			public KurarinSolverForGenerator(Field field, int limit) {
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

		public KurarinGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new KurarinGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			KurarinSolver.Field wkField = new KurarinSolver.Field(height, width);
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
						for (int i = 0; i < 3; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							KurarinSolver.Field virtual = new KurarinSolver.Field(wkField);
							if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 3) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new KurarinSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
					if (indexList.size() <= index) {
						// 破綻したら0から作り直す。
						wkField = new KurarinSolver.Field(height, width);
						index = 0;
						continue;
					}
				}
				// ○埋め
				System.out.println(wkField);
				List<Position> prioCirclePosList = new ArrayList<>();
				List<Position> circlePosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength() * 2 - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() * 2 - 1; xIndex++) {
						int blackCnt = 0;
						int whiteCnt = 0;
						boolean tateDouble = yIndex % 2 == 1;
						boolean yokoDouble = xIndex % 2 == 1;
						if (wkField.masu[yIndex / 2][xIndex / 2] == Masu.BLACK) {
							blackCnt++;
						} else if (wkField.masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (tateDouble) {
							if (wkField.masu[(yIndex / 2) + 1][xIndex / 2] == Masu.BLACK) {
								blackCnt++;
							} else if (wkField.masu[(yIndex / 2) + 1][xIndex / 2] == Masu.NOT_BLACK) {
								whiteCnt++;
							}
						}
						if (yokoDouble) {
							if (wkField.masu[yIndex / 2][(xIndex / 2) + 1] == Masu.BLACK) {
								blackCnt++;
							} else if (wkField.masu[yIndex / 2][(xIndex / 2) + 1] == Masu.NOT_BLACK) {
								whiteCnt++;
							}
						}
						if (tateDouble && yokoDouble) {
							if (wkField.masu[(yIndex / 2) + 1][(xIndex / 2) + 1] == Masu.BLACK) {
								blackCnt++;
							} else if (wkField.masu[(yIndex / 2) + 1][(xIndex / 2) + 1] == Masu.NOT_BLACK) {
								whiteCnt++;
							}
						}
						if (blackCnt > whiteCnt) {
							wkField.circles[yIndex][xIndex] = 1;
						} else if (blackCnt < whiteCnt) {
							wkField.circles[yIndex][xIndex] = 3;
						} else {
							wkField.circles[yIndex][xIndex] = 2;
						}
						if (!tateDouble && !yokoDouble) {
							// 1マスだけの丸を優先的に減らしていく。
							prioCirclePosList.add(new Position(yIndex, xIndex));
						} else {
							circlePosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				// System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 縦壁
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new KurarinSolverForGenerator(new KurarinSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new KurarinSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(prioCirclePosList);
					for (Position circlePos : prioCirclePosList) {
						KurarinSolver.Field virtual = new KurarinSolver.Field(wkField);
						virtual.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
						int solveResult = new KurarinSolverForGenerator(virtual, 100).solve2();
						if (solveResult != -1) {
							// System.out.println(solveResult);
							wkField.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
							level = solveResult;
						}
					}
					Collections.shuffle(circlePosList);
					for (Position circlePos : circlePosList) {
						KurarinSolver.Field virtual = new KurarinSolver.Field(wkField);
						virtual.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
						int solveResult = new KurarinSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							// System.out.println(solveResult);
							wkField.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
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
			// 丸描画
			for (int yIndex = 0; yIndex < wkField.getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() * 2 - 1; xIndex++) {
					if (wkField.getCircles()[yIndex][xIndex] != 0) {
						sb.append("<circle cy=\"" + (yIndex * (baseSize / 2) + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * (baseSize / 2) + baseSize + (baseSize / 2)) + "\" r=\"" + 3 + "\" fill=");
						if (wkField.getCircles()[yIndex][xIndex] == 1) {
							sb.append("\"black\"");
						} else if (wkField.getCircles()[yIndex][xIndex] == 2) {
							sb.append("\"#CCC\"");
						} else if (wkField.getCircles()[yIndex][xIndex] == 3) {
							sb.append("\"white\"");
						}
						sb.append(", stroke=\"black\">" + "</circle>");
					}
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
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// マスの情報
		protected Masu[][] masu;
		// 丸の情報。縦と横の長さはそれぞれマスに対して2倍-1になる。何もなしが0、黒が1、灰が2、白が3
		private int[][] circles;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public int[][] getCircles() {
			return circles;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzprxs.vercel.app/p?kurarin/" + getXLength() + "/" + getYLength() + "/");
			boolean findCircle = false;
			int interval = 0;
			int maxIndex = (getYLength() * 2 - 1) * (getXLength() * 2 - 1);
			for (int index = 0; index < maxIndex; index++) {
				int targetY1 = index / (getXLength() * 2 - 1);
				int targetX1 = index % (getXLength() * 2 - 1);
				index++;
				int targetY2 = index / (getXLength() * 2 - 1);
				int targetX2 = index % (getXLength() * 2 - 1);
				if (circles[targetY1][targetX1] != 0 || (index < maxIndex && circles[targetY2][targetX2] != 0)) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
					}
					sb.append(Integer.toHexString(circles[targetY1][targetX1] * 4 + circles[targetY2][targetX2]));
					interval = 0;
				} else {
					interval++;
					if (interval == 20) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
						interval = 0;
					}
				}
			}
			if (findCircle) {
				sb.append(ALPHABET_FROM_G.charAt(interval - 1));
			}
			return sb.toString();
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex] != 0) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
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

		public Field(int height, int width) {
			masu = new Masu[height][width];
			circles = new int[height * 2 - 1][width * 2 - 1];
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
			circles = new int[height * 2 - 1][width * 2 - 1];
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
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + (interval * 2) + 1;
				} else {
					int value1 = Character.digit(ch, 16) / 4;
					int value2 = Character.digit(ch, 16) % 4;
					int targetY = index / (getXLength() * 2 - 1);
					int targetX = index % (getXLength() * 2 - 1);
					circles[targetY][targetX] = value1;
					index++;
					targetY = index / (getXLength() * 2 - 1);
					targetX = index % (getXLength() * 2 - 1);
					circles[targetY][targetX] = value2;
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			circles = new int[other.getYLength() * 2 - 1][other.getXLength() * 2 - 1];
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
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					circles[yIndex][xIndex] = other.circles[yIndex][xIndex];
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
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex] != 0) {
						String circleStr = String.valueOf(circles[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(circleStr);
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					} else {
						if (yIndex % 2 == 0 && xIndex % 2 == 0) {
							sb.append(masu[yIndex / 2][xIndex / 2]);
						} else if (yIndex % 2 == 0 && xIndex % 2 == 1) {
							sb.append(yokoWall[yIndex / 2][xIndex / 2]);
						} else if (yIndex % 2 == 1 && xIndex % 2 == 0) {
							sb.append(tateWall[yIndex / 2][xIndex / 2]);
						} else {
							sb.append("□");
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
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
		 * 丸に所属する黒マスの数がおかしい場合false。
		 */
		private boolean circleSolve() {
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex] != 0) {
						boolean tateDouble = yIndex % 2 == 1;
						boolean yokoDouble = xIndex % 2 == 1;
						if (!tateDouble && !yokoDouble) {
							if (circles[yIndex][xIndex] == 1) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
							} else if (circles[yIndex][xIndex] == 3) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
							}
						} else if (tateDouble && !yokoDouble) {
							if (circles[yIndex][xIndex] == 1) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK
										|| masu[(yIndex / 2) + 1][xIndex / 2] == Masu.NOT_BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
								masu[(yIndex / 2) + 1][xIndex / 2] = Masu.BLACK;
							} else if (circles[yIndex][xIndex] == 2) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK
										&& masu[(yIndex / 2) + 1][xIndex / 2] == Masu.NOT_BLACK) {
									return false;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK
										&& masu[(yIndex / 2) + 1][xIndex / 2] == Masu.BLACK) {
									return false;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK) {
									masu[(yIndex / 2) + 1][xIndex / 2] = Masu.BLACK;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK) {
									masu[(yIndex / 2) + 1][xIndex / 2] = Masu.NOT_BLACK;
								}
								if (masu[(yIndex / 2) + 1][xIndex / 2] == Masu.NOT_BLACK) {
									masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
								}
								if (masu[(yIndex / 2) + 1][xIndex / 2] == Masu.BLACK) {
									masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
								}
							} else if (circles[yIndex][xIndex] == 3) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK
										|| masu[(yIndex / 2) + 1][xIndex / 2] == Masu.BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
								masu[(yIndex / 2) + 1][xIndex / 2] = Masu.NOT_BLACK;

							}
						} else if (!tateDouble && yokoDouble) {
							if (circles[yIndex][xIndex] == 1) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK
										|| masu[yIndex / 2][(xIndex / 2) + 1] == Masu.NOT_BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
								masu[yIndex / 2][(xIndex / 2) + 1] = Masu.BLACK;
							} else if (circles[yIndex][xIndex] == 2) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK
										&& masu[yIndex / 2][(xIndex / 2) + 1] == Masu.NOT_BLACK) {
									return false;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK
										&& masu[yIndex / 2][(xIndex / 2) + 1] == Masu.BLACK) {
									return false;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK) {
									masu[yIndex / 2][(xIndex / 2) + 1] = Masu.BLACK;
								}
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK) {
									masu[yIndex / 2][(xIndex / 2) + 1] = Masu.NOT_BLACK;
								}
								if (masu[yIndex / 2][(xIndex / 2) + 1] == Masu.NOT_BLACK) {
									masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
								}
								if (masu[yIndex / 2][(xIndex / 2) + 1] == Masu.BLACK) {
									masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
								}
							} else if (circles[yIndex][xIndex] == 3) {
								if (masu[yIndex / 2][xIndex / 2] == Masu.BLACK
										|| masu[yIndex / 2][(xIndex / 2) + 1] == Masu.BLACK) {
									return false;
								}
								masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
								masu[yIndex / 2][(xIndex / 2) + 1] = Masu.NOT_BLACK;
							}
						} else if (tateDouble && yokoDouble) {
							Masu masu1 = masu[yIndex / 2][xIndex / 2];
							Masu masu2 = masu[(yIndex / 2) + 1][xIndex / 2];
							Masu masu3 = masu[yIndex / 2][(xIndex / 2) + 1];
							Masu masu4 = masu[(yIndex / 2) + 1][(xIndex / 2) + 1];
							int blackCnt = 0;
							int whiteCnt = 0;
							if (masu1 == Masu.BLACK) {
								blackCnt++;
							}
							if (masu2 == Masu.BLACK) {
								blackCnt++;
							}
							if (masu3 == Masu.BLACK) {
								blackCnt++;
							}
							if (masu4 == Masu.BLACK) {
								blackCnt++;
							}
							if (masu1 == Masu.NOT_BLACK) {
								whiteCnt++;
							}
							if (masu2 == Masu.NOT_BLACK) {
								whiteCnt++;
							}
							if (masu3 == Masu.NOT_BLACK) {
								whiteCnt++;
							}
							if (masu4 == Masu.NOT_BLACK) {
								whiteCnt++;
							}
							if (circles[yIndex][xIndex] == 1) {
								// 白マス過剰
								if (1 < whiteCnt) {
									return false;
								}
								if (1 == whiteCnt) {
									if (masu1 == Masu.SPACE) {
										masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
									}
									if (masu2 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][xIndex / 2] = Masu.BLACK;
									}
									if (masu3 == Masu.SPACE) {
										masu[yIndex / 2][(xIndex / 2) + 1] = Masu.BLACK;
									}
									if (masu4 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][(xIndex / 2) + 1] = Masu.BLACK;
									}
								}
							} else if (circles[yIndex][xIndex] == 2) {
								// 白マス過剰
								if (2 < whiteCnt) {
									return false;
								}
								if (2 == whiteCnt) {
									if (masu1 == Masu.SPACE) {
										masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
									}
									if (masu2 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][xIndex / 2] = Masu.BLACK;
									}
									if (masu3 == Masu.SPACE) {
										masu[yIndex / 2][(xIndex / 2) + 1] = Masu.BLACK;
									}
									if (masu4 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][(xIndex / 2) + 1] = Masu.BLACK;
									}
								}
								// 黒マス過剰
								if (2 < blackCnt) {
									return false;
								}
								if (2 == blackCnt) {
									if (masu1 == Masu.SPACE) {
										masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
									}
									if (masu2 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][xIndex / 2] = Masu.NOT_BLACK;
									}
									if (masu3 == Masu.SPACE) {
										masu[yIndex / 2][(xIndex / 2) + 1] = Masu.NOT_BLACK;
									}
									if (masu4 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][(xIndex / 2) + 1] = Masu.NOT_BLACK;
									}
								}
							} else if (circles[yIndex][xIndex] == 3) {
								// 黒マス過剰
								if (1 < blackCnt) {
									return false;
								}
								if (1 == blackCnt) {
									if (masu1 == Masu.SPACE) {
										masu[yIndex / 2][xIndex / 2] = Masu.NOT_BLACK;
									}
									if (masu2 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][xIndex / 2] = Masu.NOT_BLACK;
									}
									if (masu3 == Masu.SPACE) {
										masu[yIndex / 2][(xIndex / 2) + 1] = Masu.NOT_BLACK;
									}
									if (masu4 == Masu.SPACE) {
										masu[(yIndex / 2) + 1][(xIndex / 2) + 1] = Masu.NOT_BLACK;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マス周辺の壁が2、黒マス周辺の壁が4。違反はfalseを返す。
		 */
		public boolean nextSolve() {
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
			Set<Position> blackCandPosSet = new HashSet<>();
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
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						blackCandPosSet.add(pos);
					}
				}
			}
			if (whitePosSet.size() != 0) {
				blackCandPosSet.removeAll(whitePosSet);
				for (Position pos : blackCandPosSet) {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
		 * ルール上、各列をふさぐ壁は必ず偶数になる。 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!circleSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
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
					if (masu[yIndex][xIndex] != Masu.BLACK) {
						return true;
					}
				}
			}
			return false;
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

	public KurarinSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public KurarinSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new KurarinSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
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