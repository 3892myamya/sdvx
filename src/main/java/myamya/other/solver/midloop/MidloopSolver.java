package myamya.other.solver.midloop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class MidloopSolver implements Solver {

	public static class MidloopGenerator implements Generator {

		static class MidloopSolverForGenerator extends MidloopSolver {
			private final int limit;

			public MidloopSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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

		public static class PosAndYokotate {

			@Override
			public String toString() {
				return "[yokotate=" + yokotate + ", pos=" + pos + "]";
			}

			private final int yokotate; //0ならマス、1なら横、2なら縦
			private final Position pos;

			public PosAndYokotate(int yokotate, Position pos) {
				this.yokotate = yokotate;
				this.pos = pos;
			}

			public int getYokotate() {
				return yokotate;
			}

			public Position getPos() {
				return pos;
			}

		}

		private final int height;
		private final int width;

		public MidloopGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MidloopGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			Field wkField = new Field(height, width);
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
							Field virtual = new Field(wkField);
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
								wkField.masu = virtual.masu;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 黒丸埋め初期化
				// できるだけ埋める
				List<PosAndYokotate> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							int upWhiteCnt = 0;
							int rightWhiteCnt = 0;
							int downWhiteCnt = 0;
							int leftWhiteCnt = 0;
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (wkField.tateWall[targetY][xIndex] == Wall.EXISTS) {
									break;
								}
								upWhiteCnt++;
							}
							for (int targetX = xIndex + 1; targetX < wkField.getXLength(); targetX++) {
								if (wkField.yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
									break;
								}
								rightWhiteCnt++;
							}
							for (int targetY = yIndex + 1; targetY < wkField.getYLength(); targetY++) {
								if (wkField.tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
									break;
								}
								downWhiteCnt++;
							}
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (wkField.yokoWall[yIndex][targetX] == Wall.EXISTS) {
									break;
								}
								leftWhiteCnt++;
							}
							if (upWhiteCnt == downWhiteCnt && rightWhiteCnt == leftWhiteCnt) {
								numberPosList.add(new PosAndYokotate(0, new Position(yIndex, xIndex)));
								wkField.circles[yIndex][xIndex] = true;
							}
						}
					}
				}
				// 横壁
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						if (wkField.yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							int rightWhiteCnt = 0;
							int leftWhiteCnt = 0;
							for (int targetX = xIndex + 2; targetX < wkField.getXLength(); targetX++) {
								if (wkField.yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
									break;
								}
								rightWhiteCnt++;
							}
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (wkField.yokoWall[yIndex][targetX] == Wall.EXISTS) {
									break;
								}
								leftWhiteCnt++;
							}
							if (rightWhiteCnt == leftWhiteCnt) {
								numberPosList.add(new PosAndYokotate(1, new Position(yIndex, xIndex)));
								wkField.yokoCircles[yIndex][xIndex] = true;
							}
						}
					}
				}
				// 縦壁
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							int upWhiteCnt = 0;
							int downWhiteCnt = 0;
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (wkField.tateWall[targetY][xIndex] == Wall.EXISTS) {
									break;
								}
								upWhiteCnt++;
							}
							for (int targetY = yIndex + 2; targetY < wkField.getYLength(); targetY++) {
								if (wkField.tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
									break;
								}
								downWhiteCnt++;
							}
							if (upWhiteCnt == downWhiteCnt) {
								numberPosList.add(new PosAndYokotate(2, new Position(yIndex, xIndex)));
								wkField.tateCircles[yIndex][xIndex] = true;
							}
						}
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (!wkField.circles[yIndex][xIndex]) {
							// 黒丸は白確定なので戻さない
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						if (!wkField.yokoCircles[yIndex][xIndex]) {
							wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				// 縦壁
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (!wkField.tateCircles[yIndex][xIndex]) {
							wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new MidloopSolverForGenerator(new Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (PosAndYokotate numberPos : numberPosList) {
						Field virtual = new Field(wkField, true);
						int yIndex = numberPos.getPos().getyIndex();
						int xIndex = numberPos.getPos().getxIndex();
						if (numberPos.getYokotate() == 0) {
							virtual.circles[yIndex][xIndex] = false;
							virtual.masu[yIndex][xIndex] = Masu.SPACE;
						} else if (numberPos.getYokotate() == 1) {
							virtual.yokoCircles[yIndex][xIndex] = false;
							virtual.yokoWall[yIndex][xIndex] = Wall.SPACE;
						} else if (numberPos.getYokotate() == 2) {
							virtual.tateCircles[yIndex][xIndex] = false;
							virtual.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
						int solveResult = new MidloopSolverForGenerator(virtual, 3000).solve2();
						if (solveResult != -1) {
							if (numberPos.getYokotate() == 0) {
								wkField.circles[yIndex][xIndex] = false;
								wkField.masu[yIndex][xIndex] = Masu.SPACE;
							} else if (numberPos.getYokotate() == 1) {
								wkField.yokoCircles[yIndex][xIndex] = false;
								wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
							} else if (numberPos.getYokotate() == 2) {
								wkField.tateCircles[yIndex][xIndex] = false;
								wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 6 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(黒丸:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
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
					if (!oneYokoWall && wkField.getYokoCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize)
								+ "\" r=\""
								+ 4
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
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
					if (!oneTateWall && wkField.getTateCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ 4
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
				}
			}
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ 4
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
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
		static final String INTERALS = "13579bdf";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		// マスに黒丸があればtrue
		private boolean[][] circles;
		// 横通路に黒丸があればtrue
		private boolean[][] yokoCircles;
		// 縦通路に黒丸があればtrue
		private boolean[][] tateCircles;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?midloop/" + getXLength() + "/" + getYLength() + "/");
			boolean findCircle = false;
			int interval = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex]) {
						if (!findCircle) {
							if (interval != 0) {
								sb.append(ALPHABET_FROM_G.charAt(interval - 1));
							}
							findCircle = true;
						} else {
							sb.append(INTERALS.charAt(interval));
						}
						interval = 0;
					} else {
						if (findCircle) {
							if (interval == 7) {
								sb.append(INTERALS.charAt(interval));
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
					if (xIndex != getXLength() - 1) {
						if (yokoCircles[yIndex][xIndex]) {
							if (!findCircle) {
								if (interval != 0) {
									sb.append(ALPHABET_FROM_G.charAt(interval - 1));
								}
								findCircle = true;
							} else {
								sb.append(INTERALS.charAt(interval));
							}
							interval = 0;
						} else {
							if (findCircle) {
								if (interval == 7) {
									sb.append(INTERALS.charAt(interval));
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
				if (yIndex != getYLength() - 1) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateCircles[yIndex][xIndex]) {
							if (!findCircle) {
								if (interval != 0) {
									sb.append(ALPHABET_FROM_G.charAt(interval - 1));
								}
								findCircle = true;
							} else {
								sb.append(INTERALS.charAt(interval));
							}
							interval = 0;
						} else {
							if (findCircle) {
								if (interval == 7) {
									sb.append(INTERALS.charAt(interval));
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
						if (xIndex != getXLength() - 1) {
							if (findCircle) {
								if (interval == 7) {
									sb.append(INTERALS.charAt(interval));
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
			}
			if (findCircle) {
				sb.append("f");
			} else {
				sb.append(ALPHABET_FROM_G.charAt(interval - 1));
			}
			return sb.toString();
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex]) {
						cnt++;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoCircles[yIndex][xIndex]) {
						cnt++;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateCircles[yIndex][xIndex]) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public boolean[][] getCircles() {
			return circles;
		}

		public boolean[][] getYokoCircles() {
			return yokoCircles;
		}

		public boolean[][] getTateCircles() {
			return tateCircles;
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
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			circles = new boolean[height][width];
			yokoCircles = new boolean[height][width - 1];
			tateCircles = new boolean[height - 1][width];
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
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			circles = new boolean[height][width];
			yokoCircles = new boolean[height][width - 1];
			tateCircles = new boolean[height - 1][width];
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
			// f = 自分が黒丸＆インターバル7
			// 1、3、5、7、9、b、d =自分が黒丸＆インターバル0-6
			// g-z = 自身に丸なし + インターバル1
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval;
				} else {
					int wkValue = ((getXLength() * 2) - 1);
					int targetY = (index / wkValue) / 2;
					int targetX = (index % wkValue) / 2;
					if ((index / wkValue) % 2 == 1) {
						if ((index % wkValue) % 2 == 1) {
							// ここに来ることはないはず…
						} else {
							tateWall[targetY][targetX] = Wall.NOT_EXISTS;
							tateCircles[targetY][targetX] = true;
						}
					} else {
						if ((index % wkValue) % 2 == 1) {
							yokoWall[targetY][targetX] = Wall.NOT_EXISTS;
							yokoCircles[targetY][targetX] = true;
						} else {
							masu[targetY][targetX] = Masu.NOT_BLACK;
							circles[targetY][targetX] = true;
						}
					}
					interval = INTERALS.indexOf(ch);
					if (interval != -1) {
						index = index + interval;
					}
				}
				index++;
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
			circles = other.circles;
			yokoCircles = other.yokoCircles;
			tateCircles = other.tateCircles;
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			circles = new boolean[other.getYLength()][other.getXLength()];
			yokoCircles = new boolean[other.getYLength()][other.getXLength() - 1];
			tateCircles = new boolean[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					circles[yIndex][xIndex] = other.circles[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
					yokoCircles[yIndex][xIndex] = other.yokoCircles[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
					tateCircles[yIndex][xIndex] = other.tateCircles[yIndex][xIndex];
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
					if (circles[yIndex][xIndex]) {
						sb.append("●");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						if (yokoCircles[yIndex][xIndex]) {
							sb.append("●");
						} else {
							sb.append(yokoWall[yIndex][xIndex]);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateCircles[yIndex][xIndex]) {
							sb.append("●");
						} else {
							sb.append(tateWall[yIndex][xIndex]);
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
		 * 黒丸は必ずその直線を結ぶ中点になる。ならない場合falseを返す。
		 */
		private boolean circleSolve() {
			// マスに対する調査
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex]) {
						int upSpaceCnt = 0;
						int rightSpaceCnt = 0;
						int downSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int upWhiteCnt = 0;
						int rightWhiteCnt = 0;
						int downWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean upCounting = true;
						boolean rightCounting = true;
						boolean downCounting = true;
						boolean leftupCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								leftupCounting = false;
							}
							if (leftupCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}
						// 一方で確定している長さが確保できない場合false
						if (upWhiteCnt > downSpaceCnt) {
							return false;
						}
						if (rightWhiteCnt > leftSpaceCnt) {
							return false;
						}
						if (downWhiteCnt > upSpaceCnt) {
							return false;
						}
						if (leftWhiteCnt > rightSpaceCnt) {
							return false;
						}
						// 確定した長さだけ伸ばす
						for (int i = 0; i < downWhiteCnt; i++) {
							tateWall[yIndex - 1 - i][xIndex] = Wall.NOT_EXISTS;
						}
						for (int i = 0; i < leftWhiteCnt; i++) {
							yokoWall[yIndex][xIndex + i] = Wall.NOT_EXISTS;
						}
						for (int i = 0; i < upWhiteCnt; i++) {
							tateWall[yIndex + i][xIndex] = Wall.NOT_EXISTS;
						}
						for (int i = 0; i < rightWhiteCnt; i++) {
							yokoWall[yIndex][xIndex - 1 - i] = Wall.NOT_EXISTS;
						}
						// 長さ完全確定時は反対の壁を閉じる
						if (downWhiteCnt == downSpaceCnt && yIndex - downWhiteCnt > 0) {
							tateWall[yIndex - 1 - downWhiteCnt][xIndex] = Wall.EXISTS;
						}
						if (leftWhiteCnt == leftSpaceCnt && xIndex + leftWhiteCnt < getXLength() - 1) {
							yokoWall[yIndex][xIndex + leftWhiteCnt] = Wall.EXISTS;
						}
						if (upWhiteCnt == upSpaceCnt && yIndex + upSpaceCnt < getYLength() - 1) {
							tateWall[yIndex + upWhiteCnt][xIndex] = Wall.EXISTS;
						}
						if (rightWhiteCnt == rightSpaceCnt && xIndex - rightWhiteCnt > 0) {
							yokoWall[yIndex][xIndex - 1 - rightWhiteCnt] = Wall.EXISTS;
						}
					}
				}
			}
			// 横壁に対する調査
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoCircles[yIndex][xIndex]) {
						int rightSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int rightWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean rightCounting = true;
						boolean leftupCounting = true;

						for (int targetX = xIndex + 2; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								leftupCounting = false;
							}
							if (leftupCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}

						// 一方で確定している長さが確保できない場合false
						if (rightWhiteCnt > leftSpaceCnt) {
							return false;
						}
						if (leftWhiteCnt > rightSpaceCnt) {
							return false;
						}
						// 確定した長さだけ伸ばす
						for (int i = 0; i < leftWhiteCnt; i++) {
							yokoWall[yIndex][xIndex + 1 + i] = Wall.NOT_EXISTS;
						}
						for (int i = 0; i < rightWhiteCnt; i++) {
							yokoWall[yIndex][xIndex - 1 - i] = Wall.NOT_EXISTS;
						}
						// 長さ完全確定時は反対の壁を閉じる
						if (leftWhiteCnt == leftSpaceCnt && xIndex + leftWhiteCnt < getXLength() - 2) {
							yokoWall[yIndex][xIndex + 1 + leftWhiteCnt] = Wall.EXISTS;
						}
						if (rightWhiteCnt == rightSpaceCnt && xIndex - rightWhiteCnt > 0) {
							yokoWall[yIndex][xIndex - 1 - rightWhiteCnt] = Wall.EXISTS;
						}
					}
				}
			}
			// 縦壁に対する調査
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateCircles[yIndex][xIndex]) {
						int upSpaceCnt = 0;
						int downSpaceCnt = 0;

						int upWhiteCnt = 0;
						int downWhiteCnt = 0;

						boolean upCounting = true;
						boolean downCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetY = yIndex + 2; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						// 一方で確定している長さが確保できない場合false
						if (upWhiteCnt > downSpaceCnt) {
							return false;
						}
						if (downWhiteCnt > upSpaceCnt) {
							return false;
						}
						// 確定した長さだけ伸ばす
						for (int i = 0; i < downWhiteCnt; i++) {
							tateWall[yIndex - 1 - i][xIndex] = Wall.NOT_EXISTS;
						}
						for (int i = 0; i < upWhiteCnt; i++) {
							tateWall[yIndex + 1 + i][xIndex] = Wall.NOT_EXISTS;
						}
						// 長さ完全確定時は反対の壁を閉じる
						if (downWhiteCnt == downSpaceCnt && yIndex - downWhiteCnt > 0) {
							tateWall[yIndex - 1 - downWhiteCnt][xIndex] = Wall.EXISTS;
						}
						if (upWhiteCnt == upSpaceCnt && yIndex + upSpaceCnt < getYLength() - 2) {
							tateWall[yIndex + 1 + upWhiteCnt][xIndex] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
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
						if ((existsCount == 3 && notExistsCount == 1)
								|| notExistsCount > 2) {
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
			blackCandPosSet.removeAll(whitePosSet);
			for (Position pos : blackCandPosSet) {
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
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
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
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
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
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

	public MidloopSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MidloopSolver(Field field) {
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
		System.out.println(new MidloopSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 6));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 6 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 6).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					//					Masu masuLeft = field.masu[yIndex][xIndex];
					//					Masu masuRight = field.masu[yIndex][xIndex + 1];
					//					if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
					//						continue;
					//					}
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
					//					Masu masuUp = field.masu[yIndex][xIndex];
					//					Masu masuDown = field.masu[yIndex + 1][xIndex];
					//					if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
					//						continue;
					//					}
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