package myamya.other.solver.cleek;

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
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class CleekSolver implements Solver {

	public static class CleekGenerator implements Generator {
		static class CleekSolverForGenerator extends CleekSolver {
			private final int limit;

			public CleekSolverForGenerator(Field field, int limit) {
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
						if (!candSolve(field, recursiveCnt)) {
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

		private final int height;
		private final int width;

		public CleekGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new CleekGenerator(4, 4).generate();
		}

		@Override
		public GeneratorResult generate() {
			CleekSolver.Field wkField = new CleekSolver.Field(height, width);
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
							CleekSolver.Field virtual = new CleekSolver.Field(wkField, true);
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
							wkField = new CleekSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
						int blackCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == wkField.getXLength() ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuRightDown = xIndex == wkField.getXLength() || yIndex == wkField.getYLength()
								? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuDownLeft = yIndex == wkField.getYLength() || xIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							blackCnt++;
						}
						wkField.extraNumbers[yIndex][xIndex] = blackCnt;
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new CleekSolverForGenerator(wkField, 10000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new CleekSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						CleekSolver.Field virtual = new CleekSolver.Field(wkField, true);
						virtual.extraNumbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new CleekSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.extraNumbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			//			int baseSize = 20;
			//			int margin = 5;
			//			sb.append(
			//					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
			//							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
			//							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
			//						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
			//								+ "\" x=\""
			//								+ (xIndex * baseSize + baseSize)
			//								+ "\" width=\""
			//								+ (baseSize)
			//								+ "\" height=\""
			//								+ (baseSize)
			//								+ "\">"
			//								+ "</rect>");
			//						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
			//							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin)
			//									+ "\" x=\""
			//									+ (xIndex * baseSize + baseSize + 2)
			//									+ "\" fill=\""
			//									+ "white"
			//									+ "\" font-size=\""
			//									+ (baseSize - 5)
			//									+ "\" textLength=\""
			//									+ (baseSize - 5)
			//									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
			//									+ FULL_NUMS.substring(wkField.getNumbers()[yIndex][xIndex],
			//											wkField.getNumbers()[yIndex][xIndex] + 1)
			//									+ "</text>");
			//						}
			//					}
			//				}
			//			}
			//
			//			// 横壁描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneYokoWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			// 縦壁描画
			//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + baseSize + baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneTateWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			sb.append("</svg>");
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
		// 数字の情報。外壁も考慮するため注意
		private final Integer[][] extraNumbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public String getHintCount() {
			// TODO 自動生成されたメソッド・スタブ
			return "1/1";
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
			extraNumbers = new Integer[height + 1][width + 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			extraNumbers = new Integer[height + 1][width + 1];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / (getXLength() + 1), index % (getXLength() + 1));
				if (ch == '.') {
					extraNumbers[pos.getyIndex()][pos.getxIndex()] = -1;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
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
			extraNumbers = other.extraNumbers;
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			extraNumbers = new Integer[other.getYLength() + 1][other.getXLength() + 1];
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					extraNumbers[yIndex][xIndex] = other.extraNumbers[yIndex][xIndex];
				}
			}
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] == null) {
						sb.append("□");
					} else if (extraNumbers[yIndex][xIndex] == -1) {
						sb.append("○");
					} else {
						sb.append(FULL_NUMS.substring(extraNumbers[yIndex][xIndex],
								extraNumbers[yIndex][xIndex] + 1));
					}
					if (xIndex != getXLength()) {
						sb.append("□");
					}
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(masu[yIndex][xIndex]);
						sb.append("□");
					}
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
		 * 数字指定のあるマスの周囲の黒マス個数を数え、確定する箇所は埋める。
		 * 矛盾したらfalseを返す。
		 */
		public boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] != null && extraNumbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuUpRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuRightDown = xIndex == getXLength() || yIndex == getYLength() ? Masu.NOT_BLACK
								: masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuRightDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuDownLeft = yIndex == getYLength() || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuDownLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeftUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (extraNumbers[yIndex][xIndex] < blackCnt) {
							// 黒マス過剰
							return false;
						}
						if (extraNumbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 黒マス不足
							return false;
						}
						if (extraNumbers[yIndex][xIndex] == blackCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (extraNumbers[yIndex][xIndex] == 4 - whiteCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
							}
						}

					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!aroundSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}
	}

	protected final Field field;
	protected int count;

	public CleekSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public CleekSolver(Field field) {
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
		System.out.println(new CleekSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					// 周囲に空白が少ない＆ヒントが多い個所を優先して調査
					Masu masuUp = yIndex == 0 ? Masu.BLACK
							: field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK
							: field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK
							: field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK
							: field.masu[yIndex][xIndex - 1];
					int whiteCnt = 0;
					if (masuUp == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuRight == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuDown == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuLeft == Masu.SPACE) {
						whiteCnt++;
					}
					Integer numberUpRight = field.extraNumbers[yIndex][xIndex + 1];
					Integer numberRightDown = field.extraNumbers[yIndex + 1][xIndex + 1];
					Integer numberDownLeft = field.extraNumbers[yIndex + 1][xIndex];
					Integer numberLeftUp = field.extraNumbers[yIndex][xIndex];
					int noHintCnt = 0;
					if (numberUpRight == null || numberUpRight == -1) {
						noHintCnt++;
					}
					if (numberRightDown == null || numberRightDown == -1) {
						noHintCnt++;
					}
					if (numberDownLeft == null || numberDownLeft == -1) {
						noHintCnt++;
					}
					if (numberLeftUp == null || numberLeftUp == -1) {
						noHintCnt++;
					}
					if (noHintCnt + whiteCnt > 6) {
						// 4だと解けない問題が出ることがある。5と6はかなり微妙で問題により速度差が出る。
						continue;
					}
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
		} else if (!allowNotBlack) {
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
