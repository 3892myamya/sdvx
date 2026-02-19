package myamya.other.solver.sansaroad;

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
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class SansaroadSolver implements Solver {

	public static class SansaroadGenerator implements Generator {

		static class SansaroadSolverForGenerator extends SansaroadSolver {
			private final int limit;

			public SansaroadSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends SansaroadSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			@Override
			public boolean nextSolve() {
				// ▽があとから決まるので、ここではじいてしまうとダメ。
				// 白マスの場合に、周囲の白マス数が3、黒マス数が2までを許容する
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.BLACK : masu[yIndex][xIndex - 1];
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
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if (2 < blackCnt) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (2 < blackCnt) {
								return false;
							}
							if (2 == blackCnt) {
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
							if (1 > 4 - whiteCnt) {
								return false;
							}
							if (1 == 4 - whiteCnt) {
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
						}
					}
				}
				return true;
			}
		}

		private final int height;
		private final int width;

		public SansaroadGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SansaroadGenerator(15, 15).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
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
							SansaroadSolver.Field virtual = new ExtendedField(wkField);
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
							wkField = new ExtendedField(height, width);
							index = 0;
							continue;
						}
					}
					index++;
					if (indexList.size() <= index) {
						// 破綻したら0から作り直す。
						wkField = new ExtendedField(height, width);
						index = 0;
						continue;
					}
				}
				// System.out.println(wkField);
				// ▽埋め
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							int blackCnt = 0;
							int whiteCnt = 0;
							Masu masuUp = yIndex == 0 ? Masu.BLACK : wkField.masu[yIndex - 1][xIndex];
							Masu masuRight = xIndex == wkField.getXLength() - 1 ? Masu.BLACK
									: wkField.masu[yIndex][xIndex + 1];
							Masu masuDown = yIndex == wkField.getYLength() - 1 ? Masu.BLACK
									: wkField.masu[yIndex + 1][xIndex];
							Masu masuLeft = xIndex == 0 ? Masu.BLACK : wkField.masu[yIndex][xIndex - 1];
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
							if (blackCnt == 1 && whiteCnt == 3) {
								wkField.triangles[yIndex][xIndex] = true;
							}
						}
					}
				}
				// ○埋め
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
						if (tateDouble || yokoDouble) {
							if (blackCnt > whiteCnt) {
								wkField.circles[yIndex][xIndex] = 1;
							} else if (blackCnt < whiteCnt) {
								wkField.circles[yIndex][xIndex] = 3;
							} else {
								wkField.circles[yIndex][xIndex] = 2;
							}
							circlePosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				// System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (!wkField.triangles[yIndex][xIndex]) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new SansaroadSolverForGenerator(new SansaroadSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
					index = 0;
				} else {
					Collections.shuffle(circlePosList);
					for (Position circlePos : circlePosList) {
						SansaroadSolver.Field virtual = new SansaroadSolver.Field(wkField);
						virtual.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
						int solveResult = new SansaroadSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							// System.out.println(solveResult);
							wkField.circles[circlePos.getyIndex()][circlePos.getxIndex()] = 0;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 10 / 3) + 1;

			String status = "Lv:" + level + "の問題を獲得！(" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + baseSize + margin) + "\" width=\""
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
			// 三角描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getTriangles()[yIndex][xIndex]) {
						sb.append("<path d=\"M " + (xIndex * baseSize + baseSize + baseSize / 2) + " "
								+ (yIndex * baseSize + baseSize - 3 + margin) + " L" + (xIndex * baseSize + baseSize + 3) + " "
								+ (yIndex * baseSize + 4 + margin) + " L"
								+ (xIndex * baseSize + baseSize + baseSize - 3) + " "
								+ (yIndex * baseSize + 4 + margin) + " Z\" "
								+ "\" fill=\"white\", stroke=\"black\">" + "</path>");
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
		// 下向き三角の情報。trueなら三角あり=三叉路
		protected boolean[][] triangles;
		// 丸の情報。縦と横の長さはそれぞれマスに対して2倍-1になる。何もなしが0、黒が1、灰が2、白が3
		protected int[][] circles;

		public int[][] getCircles() {
			return circles;
		}

		public boolean[][] getTriangles() {
			return triangles;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzprxs.vercel.app/p?sansaroad/" + getXLength() + "/" + getYLength() + "/");
			boolean findCircle = false;
			int interval = 0;
			int maxIndex = (getYLength() * 2 - 1) * (getXLength() * 2 - 1);
			for (int index = 0; index < maxIndex; index++) {
				int targetY1 = index / (getXLength() * 2 - 1);
				int targetX1 = index % (getXLength() * 2 - 1);
				index++;
				int targetY2 = index / (getXLength() * 2 - 1);
				int targetX2 = index % (getXLength() * 2 - 1);
				int param1 = targetY1 % 2 == 0 && targetX1 % 2 == 0 && triangles[targetY1 / 2][targetX1 / 2] ? 1
						: circles[targetY1][targetX1];
				int param2 = index < maxIndex ? circles[targetY2][targetX2] : 0;
				if (param1 != 0 || (index < maxIndex && param2 != 0)) {
					if (interval != 0) {
						sb.append(ALPHABET_FROM_G.charAt(interval - 1));
					}
					sb.append(Integer.toHexString(param1 * 4 + param2));
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
			int triCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (triangles[yIndex][xIndex]) {
						triCnt++;
					}
				}
			}
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex] != 0) {
						cnt++;
					}
				}
			}
			return String.valueOf("三角：" + triCnt + "、丸：" + cnt);
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
			triangles = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					triangles[yIndex][xIndex] = false;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			circles = new int[height * 2 - 1][width * 2 - 1];
			triangles = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					triangles[yIndex][xIndex] = false;
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
					if (targetY % 2 == 0 && targetX % 2 == 0 && value1 == 1) {
						triangles[targetY / 2][targetX / 2] = true;
						masu[targetY / 2][targetX / 2] = Masu.NOT_BLACK;
					} else {
						circles[targetY][targetX] = value1;
					}
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
			triangles = new boolean[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					triangles[yIndex][xIndex] = other.triangles[yIndex][xIndex];
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
							if (triangles[yIndex / 2][xIndex / 2]) {
								sb.append("▽");
							} else {
								sb.append(masu[yIndex / 2][xIndex / 2]);
							}
						} else if (yIndex % 2 == 0 && xIndex % 2 == 1) {
							sb.append("　");
						} else if (yIndex % 2 == 1 && xIndex % 2 == 0) {
							sb.append("　");
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
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
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
		 * 白マス周辺の白マスは2、三叉路周囲の白マスは3。違反はfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int blackCnt = 0;
					int whiteCnt = 0;
					Masu masuUp = yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK : masu[yIndex][xIndex - 1];
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
					if (triangles[yIndex][xIndex]) {
						if (1 < blackCnt) {
							// 黒マス過剰
							return false;
						}
						if (1 == blackCnt) {
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
						if (1 > 4 - whiteCnt) {
							// 黒マス不足
							return false;
						}
						if (1 == 4 - whiteCnt) {
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
					} else {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if (2 < blackCnt) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (2 < blackCnt) {
								return false;
							}
							if (2 == blackCnt) {
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
							if (2 > 4 - whiteCnt) {
								return false;
							}
							if (2 == 4 - whiteCnt) {
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
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白2x2禁
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
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
		 * posを起点に上下左右に黒マス以外を無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
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
			if (!circleSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
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
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public SansaroadSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public SansaroadSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?sansaroad/15/15/j4g4h3kfzi4g4zv8zl3jcv4h2zzj4m4x2j4m4w4zk8g3l4g4w8vcn2p4zx1o1l4k2q8icg8"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new SansaroadSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
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