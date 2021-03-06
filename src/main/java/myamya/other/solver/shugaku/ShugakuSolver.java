package myamya.other.solver.shugaku;

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

public class ShugakuSolver implements Solver {
	public static class ShugakuGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ExtendedField extends ShugakuSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			public boolean isSolved() {
				return pondSolve();
			}
		}

		static class ShugakuSolverForGenerator extends ShugakuSolver {
			private final int limit;

			public ShugakuSolverForGenerator(Field field, int limit) {
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

		public ShugakuGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ShugakuGenerator(5, 5).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
			// 布団の敷き方のリスト。最初に全部候補を持っておく。
			List<List<Position>> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					// index=0が枕あり、index=1が枕なしのマス
					if (yIndex != 0) {
						List<Position> wkList = new ArrayList<>();
						wkList.add(new Position(yIndex, xIndex));
						wkList.add(new Position(yIndex - 1, xIndex));
						indexList.add(wkList);
					}
					if (xIndex != wkField.getXLength() - 1) {
						List<Position> wkList = new ArrayList<>();
						wkList.add(new Position(yIndex, xIndex));
						wkList.add(new Position(yIndex, xIndex + 1));
						indexList.add(wkList);
					}
					if (xIndex != 0) {
						List<Position> wkList = new ArrayList<>();
						wkList.add(new Position(yIndex, xIndex));
						wkList.add(new Position(yIndex, xIndex - 1));
						indexList.add(wkList);
					}
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (true) {
					Collections.shuffle(indexList);
					for (List<Position> wkList : indexList) {
						Position makuraPos = wkList.get(0);
						Position noMakuraPos = wkList.get(1);
						if (wkField.getMasu()[makuraPos.getyIndex()][makuraPos.getxIndex()] == Masu.NOT_BLACK
								|| wkField.getMasu()[noMakuraPos.getyIndex()][noMakuraPos
										.getxIndex()] == Masu.NOT_BLACK) {
							// 既に布団/柱のマスには敷かない
							continue;
						} else {
							boolean isYokoFuton = makuraPos.getyIndex() == noMakuraPos.getyIndex();
							if (isYokoFuton) {
								// 横向きの布団の場合、柱に接しないマスには敷かない
								boolean isOk = false;
								for (Position pos : wkList) {
									Integer numberUp = pos.getyIndex() == 0 ? null
											: wkField.numbers[pos.getyIndex() - 1][pos.getxIndex()];
									Integer numberRight = pos.getxIndex() == wkField.getXLength() - 1 ? null
											: wkField.numbers[pos.getyIndex()][pos.getxIndex() + 1];
									Integer numberDown = pos.getyIndex() == wkField.getYLength() - 1 ? null
											: wkField.numbers[pos.getyIndex() + 1][pos.getxIndex()];
									Integer numberLeft = pos.getxIndex() == 0 ? null
											: wkField.numbers[pos.getyIndex()][pos.getxIndex() - 1];
									if (numberUp != null || numberRight != null || numberDown != null
											|| numberLeft != null) {
										isOk = true;
										break;
									}
								}
								if (!isOk) {
									continue;
								}
							}
							// 加えて、枕が柱に接していない場合、90%の確率で敷かない
							// ※柱に接する枕の数を増やして唯一解になりやすくするため
							Integer numberUp = makuraPos.getyIndex() == 0 ? null
									: wkField.numbers[makuraPos.getyIndex() - 1][makuraPos.getxIndex()];
							Integer numberRight = makuraPos.getxIndex() == wkField.getXLength() - 1 ? null
									: wkField.numbers[makuraPos.getyIndex()][makuraPos.getxIndex() + 1];
							Integer numberDown = makuraPos.getyIndex() == wkField.getYLength() - 1 ? null
									: wkField.numbers[makuraPos.getyIndex() + 1][makuraPos.getxIndex()];
							Integer numberLeft = makuraPos.getxIndex() == 0 ? null
									: wkField.numbers[makuraPos.getyIndex()][makuraPos.getxIndex() - 1];
							if (numberUp == null && numberRight == null && numberDown == null
									&& numberLeft == null) {
								if (Math.random() * 10 < 9) {
									continue;
								}
							}
							// さらに、いかなる場合でも10％の確率で敷かない
							// ※問題のバリエーションを増やすため
							if (Math.random() * 10 < 1) {
								continue;
							}
							// 敷くことで通路が途切れるマスには敷かない
							Field virtual = new ShugakuSolver.Field(wkField);
							virtual.masu[makuraPos.getyIndex()][makuraPos.getxIndex()] = Masu.NOT_BLACK;
							virtual.makura[makuraPos.getyIndex()][makuraPos.getxIndex()] = Masu.BLACK;
							virtual.masu[noMakuraPos.getyIndex()][noMakuraPos.getxIndex()] = Masu.NOT_BLACK;
							virtual.makura[noMakuraPos.getyIndex()][noMakuraPos.getxIndex()] = Masu.NOT_BLACK;
							if (!virtual.connectSolve()) {
								continue;
							} else {
								wkField.masu[makuraPos.getyIndex()][makuraPos.getxIndex()] = Masu.NOT_BLACK;
								wkField.makura[makuraPos.getyIndex()][makuraPos.getxIndex()] = Masu.BLACK;
								wkField.masu[noMakuraPos.getyIndex()][noMakuraPos.getxIndex()] = Masu.NOT_BLACK;
								wkField.makura[noMakuraPos.getyIndex()][noMakuraPos.getxIndex()] = Masu.NOT_BLACK;
							}
						}
					}
					// 敷き終わった時点で2x2が残っていたらやり直し
					if (!wkField.isSolved()) {
						wkField = new ExtendedField(height, width);
						continue;
					}
					break;
				}
				// ヒント決定
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] != null) {
							int blackCnt = 0;
							Masu masuUp = yIndex == 0 || wkField.numbers[yIndex - 1][xIndex] != null ? Masu.NOT_BLACK
									: wkField.makura[yIndex - 1][xIndex];
							Masu masuRight = xIndex == wkField.getXLength() - 1
									|| wkField.numbers[yIndex][xIndex + 1] != null ? Masu.NOT_BLACK
											: wkField.makura[yIndex][xIndex + 1];
							Masu masuDown = yIndex == wkField.getYLength() - 1
									|| wkField.numbers[yIndex + 1][xIndex] != null ? Masu.NOT_BLACK
											: wkField.makura[yIndex + 1][xIndex];
							Masu masuLeft = xIndex == 0 || wkField.numbers[yIndex][xIndex - 1] != null ? Masu.NOT_BLACK
									: wkField.makura[yIndex][xIndex - 1];
							if (masuUp == Masu.BLACK) {
								blackCnt++;
							}
							if (masuRight == Masu.BLACK) {
								blackCnt++;
							}
							if (masuDown == Masu.BLACK) {
								blackCnt++;
							}
							if (masuLeft == Masu.BLACK) {
								blackCnt++;
							}
							wkField.numbers[yIndex][xIndex] = blackCnt;
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				// マスを戻す
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
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							wkField.makura[yIndex][xIndex] = Masu.SPACE;
						} else {
							if (yIndex != 0) {
								wkField.tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex != wkField.getXLength() - 1) {
								wkField.yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (yIndex != wkField.getYLength() - 1) {
								wkField.tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (xIndex != 0) {
								wkField.yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						}
					}
				}
				// 解けるかな？
				level = new ShugakuSolverForGenerator(wkField, 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						ShugakuSolver.Field virtual = new ShugakuSolver.Field(wkField, true);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = 5;
						int solveResult = new ShugakuSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = 5;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/丸：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (wkField.getNumbers()[yIndex][xIndex] != 5) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int idx = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		// マスの情報
		protected Masu[][] masu;
		// 枕の情報。黒なら枕あり
		protected Masu[][] makura;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;
		// 数字の情報
		protected Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?shugaku/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 30) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = numbers[yIndex][xIndex];
					String numStr = Integer.toHexString(num);
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(Character.forDigit(interval + 5, 36));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(Character.forDigit(interval + 5, 36));
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
						if (numbers[yIndex][xIndex] != 5) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + kuroCnt);
		}

		public Masu[][] getMakura() {
			return makura;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			makura = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					makura[yIndex][xIndex] = Masu.SPACE;
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
				int interval = Character.getNumericValue(ch) - 5;
				if (interval > 0) {
					index = index + interval;
				} else {
					int capacity = Integer.parseInt(String.valueOf(ch), 16);
					Position pos = new Position(index / getXLength(), index % getXLength());
					// 柱マスは周囲の壁＆枕なしが確定
					numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
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
					makura[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			makura = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					makura[yIndex][xIndex] = other.makura[yIndex][xIndex];
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

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			masu = new Masu[other.getYLength()][other.getXLength()];
			makura = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					makura[yIndex][xIndex] = other.makura[yIndex][xIndex];
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

		/**
		 * ガチャ用のコンストラクタ。柱を一定確率で作り、それ以外のマスはいったん全部黒塗りにする。
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			makura = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			while (true) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
						makura[yIndex][xIndex] = Masu.SPACE;
						numbers[yIndex][xIndex] = null;
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
				// 1/7の確率で柱発生
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (Math.random() * 7 < 1) {
							numbers[yIndex][xIndex] = 5;
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
							if (yIndex != 0) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex != getXLength() - 1) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (yIndex != getYLength() - 1) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (xIndex != 0) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							makura[yIndex][xIndex] = Masu.NOT_BLACK;
						} else {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
				boolean isOk = true;
				Set<Position> blackPosSet = new HashSet<>();
				outer: for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (numbers[yIndex][xIndex] == null) {
							Position blackPos = new Position(yIndex, xIndex);
							if (blackPosSet.size() == 0) {
								blackPosSet.add(blackPos);
								setContinuePosSet(blackPos, blackPosSet, null);
							} else {
								if (!blackPosSet.contains(blackPos)) {
									isOk = false;
									break outer;
								}
							}
						}
					}
				}
				if (isOk) {
					break;
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
						if (numbers[yIndex][xIndex] == 5) {
							sb.append("○");
						} else {
							sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
									numbers[yIndex][xIndex] + 1));
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK && makura[yIndex][xIndex] == Masu.BLACK) {
						sb.append("枕");
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK && makura[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("ふ");
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
					sb.append(makura[yIndex][xIndex]);
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
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!blackWhiteSolve()) {
				return false;
			}
			if (!poleSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!aisleSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 枕があるマスは白であり、黒マスは枕がない。
		 * 違反した場合falseを返す。
		 */
		private boolean blackWhiteSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							if (makura[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							makura[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (makura[yIndex][xIndex] == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱の数字は枕の数を表す。条件を満たさない場合falseを返す。
		 */
		private boolean poleSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != 5) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu makuraUp = yIndex == 0 ? Masu.NOT_BLACK
								: makura[yIndex - 1][xIndex];
						Masu makuraRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: makura[yIndex][xIndex + 1];
						Masu makuraDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: makura[yIndex + 1][xIndex];
						Masu makuraLeft = xIndex == 0 ? Masu.NOT_BLACK
								: makura[yIndex][xIndex - 1];
						if (makuraUp == Masu.BLACK) {
							blackCnt++;
						} else if (makuraUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraRight == Masu.BLACK) {
							blackCnt++;
						} else if (makuraRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraDown == Masu.BLACK) {
							blackCnt++;
						} else if (makuraDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraLeft == Masu.BLACK) {
							blackCnt++;
						} else if (makuraLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < blackCnt) {
							// 枕過剰
							return false;
						}
						if (numbers[yIndex][xIndex] == blackCnt) {
							if (makuraUp == Masu.SPACE) {
								makura[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraRight == Masu.SPACE) {
								makura[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (makuraDown == Masu.SPACE) {
								makura[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraLeft == Masu.SPACE) {
								makura[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 枕不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
							if (makuraUp == Masu.SPACE) {
								makura[yIndex - 1][xIndex] = Masu.BLACK;
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraRight == Masu.SPACE) {
								makura[yIndex][xIndex + 1] = Masu.BLACK;
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (makuraDown == Masu.SPACE) {
								makura[yIndex + 1][xIndex] = Masu.BLACK;
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraLeft == Masu.SPACE) {
								makura[yIndex][xIndex - 1] = Masu.BLACK;
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 */
		protected boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスでない白マスは必ず壁の数が3個に、黒マスは壁の数が4枚になる。
		 * かつ、枕のある白マスは必ず下の壁が閉じる。違反した場合はfalseを返す。
		 */
		private boolean wallSolve() {
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
							if (notExists > 1) {
								return false;
							} else if (notExists > 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (exists == 4) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (notExists > 1 || exists == 4) {
								return false;
							}
							if (wallDown == Wall.NOT_EXISTS && makura[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							if (wallDown == Wall.SPACE && makura[yIndex][xIndex] == Masu.BLACK) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (notExists == 1) {
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
							if (notExists == 0 && exists == 3) {
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
							} else if (exists != 4) {
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
			}
			return true;
		}

		/**
		 * 壁で区切られた白マスのサイズが2の場合は、そのマスの周囲に黒マスが最低1つ必要である。
		 * また、枕が1つだけ必要である。違反した場合falseを返す。
		 */
		protected boolean aisleSolve() {
			Set<Position> resolvedWhitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (!resolvedWhitePosSet.contains(whitePos)) {
							Set<Position> whitePosSet = new HashSet<>();
							whitePosSet.add(whitePos);
							if (!setContinueWhitePosSet(whitePos, whitePosSet, null)) {
								if (whitePosSet.size() > 2) {
									// 3以上のサイズの部屋禁止
									return false;
								}
							} else {
								// 部屋が確定
								for (Position pos : whitePosSet) {
									masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								int spaceCnt = 0;
								for (Position pos : whitePosSet) {
									Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
											: masu[pos.getyIndex() - 1][pos.getxIndex()];
									Masu masuRight = pos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() + 1];
									Masu masuDown = pos.getyIndex() == getYLength() - 1 ? Masu.NOT_BLACK
											: masu[pos.getyIndex() + 1][pos.getxIndex()];
									Masu masuLeft = pos.getxIndex() == 0 ? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() - 1];
									if (masuUp == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuUp == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuRight == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuRight == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuDown == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuDown == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuLeft == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuLeft == Masu.SPACE) {
										spaceCnt++;
									}
								}
								if (spaceCnt == 0) {
									// 部屋に黒マスが接しない
									return false;
								}
								if (spaceCnt == 1) {
									// 部屋に接する白マスでないマスが1マスのみならそこは黒マス。
									for (Position pos : whitePosSet) {
										Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
												: masu[pos.getyIndex() - 1][pos.getxIndex()];
										Masu masuRight = pos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
												: masu[pos.getyIndex()][pos.getxIndex() + 1];
										Masu masuDown = pos.getyIndex() == getYLength() - 1 ? Masu.NOT_BLACK
												: masu[pos.getyIndex() + 1][pos.getxIndex()];
										Masu masuLeft = pos.getxIndex() == 0 ? Masu.NOT_BLACK
												: masu[pos.getyIndex()][pos.getxIndex() - 1];
										if (masuUp == Masu.SPACE) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
											break;
										}
										if (masuRight == Masu.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
											break;
										}
										if (masuDown == Masu.SPACE) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
											break;
										}
										if (masuLeft == Masu.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
											break;
										}
									}
								}
								int makuraBlackCnt = 0;
								int makuraWhiteCnt = 0;
								for (Position pos : whitePosSet) {
									if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										makuraBlackCnt++;
									} else if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
										makuraWhiteCnt++;
									}
								}
								if (makuraBlackCnt == 2 || makuraWhiteCnt == 2) {
									// 枕が0個 or 2個
									return false;
								}
								// 枕あり確定の場合、もう一方は枕なし
								if (makuraBlackCnt == 1 && makuraWhiteCnt == 0) {
									for (Position pos : whitePosSet) {
										if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
											makura[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
											break;
										}
									}
								}
								// 枕なし確定の場合、もう一方は枕あり
								if (makuraWhiteCnt == 1 && makuraBlackCnt == 0) {
									for (Position pos : whitePosSet) {
										if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
											makura[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
											break;
										}
									}
								}
							}
							resolvedWhitePosSet.addAll(whitePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右の壁なし確定のマスをつなぐ。サイズが2の場合のみtrueを返す。
		 */
		protected boolean setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return (continuePosSet.size() == 2);
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinuePosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 */
		protected void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE || makura[yIndex][xIndex] == Masu.SPACE) {
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

	public static class Room {
		@Override
		public String toString() {
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
		}

	}

	protected final Field field;
	protected int count;

	public ShugakuSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public ShugakuSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?shugaku/8/8/81q527219472d55c185515"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new ShugakuSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 2 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
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
				if (field.masu[yIndex][xIndex] == Masu.NOT_BLACK && field.makura[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandMakuraSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		String newStr = field.getStateDump();
		if (!newStr.equals(str)) {
			return candSolve(field, recursive);
		}
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
		if (!field.getStateDump().equals(newStr)) {
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	/**
	 * 1つの枕に対する仮置き調査
	 */
	private boolean oneCandMakuraSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.makura[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.makura[yIndex][xIndex] = Masu.NOT_BLACK;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}