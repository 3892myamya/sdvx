package myamya.other.solver.nothree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class NothreeSolver implements Solver {
	public static class NothreeGenerator implements Generator {

		static class NothreeSolverForGenerator extends NothreeSolver {
			private final int limit;

			public NothreeSolverForGenerator(Field field, int limit) {
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

		public NothreeGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NothreeGenerator(15, 15).generate();
		}

		@Override
		public GeneratorResult generate() {
			NothreeSolver.Field wkField = new NothreeSolver.Field(height, width);
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
							NothreeSolver.Field virtual = new NothreeSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 3) {
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
							wkField = new NothreeSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
//				// ○埋め
				boolean existBlack = false;
				List<Position> circlePosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength() * 2 - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() * 2 - 1; xIndex++) {
						int blackCnt = 0;
						boolean tateDouble = yIndex % 2 == 1;
						boolean yokoDouble = xIndex % 2 == 1;
						if (wkField.masu[yIndex / 2][xIndex / 2] == Masu.BLACK) {
							blackCnt++;
							existBlack = true;
						}
						if (tateDouble && wkField.masu[(yIndex / 2) + 1][xIndex / 2] == Masu.BLACK) {
							blackCnt++;
						}
						if (yokoDouble && wkField.masu[yIndex / 2][(xIndex / 2) + 1] == Masu.BLACK) {
							blackCnt++;
						}
						if (tateDouble && yokoDouble
								&& wkField.masu[(yIndex / 2) + 1][(xIndex / 2) + 1] == Masu.BLACK) {
							blackCnt++;
						}
						if (blackCnt == 1) {
							wkField.circles[yIndex][xIndex] = true;
							circlePosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				if (!existBlack) {
					// 全白ます問題は出ないようにする
					wkField = new NothreeSolver.Field(height, width);
					index = 0;
					continue;
				}
				// System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new NothreeSolverForGenerator(new NothreeSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new NothreeSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(circlePosList);
					for (Position circlePos : circlePosList) {
						NothreeSolver.Field virtual = new NothreeSolver.Field(wkField);
						virtual.circles[circlePos.getyIndex()][circlePos.getxIndex()] = false;
						int solveResult = new NothreeSolverForGenerator(virtual, 1600).solve2();
						if (solveResult != -1) {
							wkField.circles[circlePos.getyIndex()][circlePos.getxIndex()] = false;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
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
			// 星描画
			for (int yIndex = 0; yIndex < wkField.getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() * 2 - 1; xIndex++) {
					if (wkField.getCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * (baseSize / 2) + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * (baseSize / 2) + baseSize + (baseSize / 2)) + "\" r=\"" + 3
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
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
		static final String INTERBALS = "02468ace";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		protected Masu[][] masu;
		// 丸の情報。縦と横の長さはそれぞれマスに対して2倍-1になる
		private boolean[][] circles;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex]) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?nothree/" + getXLength() + "/" + getYLength() + "/");
			boolean findCircle = false;
			int interval = 0;
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex]) {
						if (!findCircle) {
							if (interval != 0) {
								sb.append(ALPHABET_FROM_G.charAt(interval - 1));
							}
							findCircle = true;
						} else {
							sb.append(INTERBALS.charAt(interval));
						}
						interval = 0;
					} else {
						if (findCircle) {
							if (interval == 7) {
								sb.append(INTERBALS.charAt(interval));
								findCircle = false;
								interval = 0;
							}
						} else {
							if (interval == 20) {
								sb.append(ALPHABET_FROM_G.charAt(interval - 1));
								interval = 0;
							}
						}
						interval++;
					}
				}
			}
			if (findCircle) {
				sb.append("e");
			} else {
				sb.append(ALPHABET_FROM_G.charAt(interval - 1));
			}
			return sb.toString();
		}

		public boolean[][] getCircles() {
			return circles;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			circles = new boolean[height * 2 - 1][width * 2 - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			circles = new boolean[height * 2 - 1][width * 2 - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// e= 自分が丸＆インターバル7
			// 0、2、4、6、8、a、c =自分が丸＆インターバル0-6
			// g-z = 自身に丸なし + インターバル1
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval;
				} else {
					int targetY = index / (getXLength() * 2 - 1);
					int targetX = index % (getXLength() * 2 - 1);
					circles[targetY][targetX] = true;
					interval = INTERBALS.indexOf(ch);
					if (interval != -1) {
						index = index + interval;
					}
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			circles = new boolean[other.getYLength() * 2 - 1][other.getXLength() * 2 - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					circles[yIndex][xIndex] = other.circles[yIndex][xIndex];
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
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex]) {
						if (yIndex % 2 == 0 && xIndex % 2 == 0) {
							sb.append(masu[yIndex / 2][xIndex / 2] == Masu.BLACK ? "●" : "○");
						} else {
							sb.append("○");
						}
					} else {
						if (yIndex % 2 == 0 && xIndex % 2 == 0) {
							sb.append(masu[yIndex / 2][xIndex / 2]);
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

		/**
		 * 黒マス隣接セルを白マスにする。 黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
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
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				setContinueWhitePosSet(typicalWhitePos, continuePosSet);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
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
		 * 
		 * @param recursive
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
				if (!nothreeSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 黒マスが等間隔に3つ並んだらfalseを返す。
		 */
		private boolean nothreeSolve() {
			// 横方向のチェック
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int firstXIndex = -1;
				int interval = -1;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (firstXIndex != -1 && interval != -1) {
							if (xIndex == firstXIndex + interval + interval) {
								return false;
							} else {
								firstXIndex = firstXIndex + interval;
								interval = xIndex - firstXIndex;
							}
						} else if (firstXIndex != -1 && interval == -1) {
							interval = xIndex - firstXIndex;
						} else if (firstXIndex == -1 && interval == -1) {
							firstXIndex = xIndex;
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						firstXIndex = -1;
						interval = -1;
					}
				}
			}
			// 縦方向のチェック
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int firstYIndex = -1;
				int interval = -1;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (firstYIndex != -1 && interval != -1) {
							if (yIndex == firstYIndex + interval + interval) {
								return false;
							} else {
								firstYIndex = firstYIndex + interval;
								interval = yIndex - firstYIndex;
							}
						} else if (firstYIndex != -1 && interval == -1) {
							interval = yIndex - firstYIndex;
						} else if (firstYIndex == -1 && interval == -1) {
							firstYIndex = yIndex;
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						firstYIndex = -1;
						interval = -1;
					}
				}
			}
			return true;
		}

		/**
		 * 丸に所属する黒マスは1つだけ。違反する場合false
		 */
		private boolean circleSolve() {
			for (int yIndex = 0; yIndex < getYLength() * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() * 2 - 1; xIndex++) {
					if (circles[yIndex][xIndex]) {
						boolean tateDouble = yIndex % 2 == 1;
						boolean yokoDouble = xIndex % 2 == 1;
						if (!tateDouble && !yokoDouble) {
							if (masu[yIndex / 2][xIndex / 2] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex / 2][xIndex / 2] = Masu.BLACK;
						} else if (tateDouble && !yokoDouble) {
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
						} else if (!tateDouble && yokoDouble) {
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
						} else if (tateDouble && yokoDouble) {
							Masu masu1 = masu[yIndex / 2][xIndex / 2];
							Masu masu2 = masu[(yIndex / 2) + 1][xIndex / 2];
							Masu masu3 = masu[yIndex / 2][(xIndex / 2) + 1];
							Masu masu4 = masu[(yIndex / 2) + 1][(xIndex / 2) + 1];
							int blackCnt = 0;
							int spaceCnt = 0;
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
							if (masu1 == Masu.SPACE) {
								spaceCnt++;
							}
							if (masu2 == Masu.SPACE) {
								spaceCnt++;
							}
							if (masu3 == Masu.SPACE) {
								spaceCnt++;
							}
							if (masu4 == Masu.SPACE) {
								spaceCnt++;
							}
							// 黒マス過剰
							if (1 < blackCnt) {
								return false;
							}
							// 黒マス不足
							if (1 > blackCnt + spaceCnt) {
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
							if (1 == blackCnt + spaceCnt) {
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

	protected final Field field;
	protected int count;

	public NothreeSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NothreeSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?nothree/15/15/peaceraekezhae2ezi6em26ejeoac8ezzoegejepeo26eleraeeoezqeh4ei4ezqezgacaejekevelei8aeznegeiezqekcceoecc2cepe"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NothreeSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
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
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.masu[y][x] == virtual.masu[y][x]) {
						field.masu[y][x] = virtual.masu[y][x];
					}
				}
			}
		}
		return true;
	}
}