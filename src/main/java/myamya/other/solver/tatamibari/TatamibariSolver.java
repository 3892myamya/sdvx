package myamya.other.solver.tatamibari;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker.RoomMaker2;
import myamya.other.solver.Solver;

public class TatamibariSolver implements Solver {
	public static class TatamibariGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class TatamibariSolverForGenerator extends TatamibariSolver {

			private final int limit;

			public TatamibariSolverForGenerator(Field field, int limit) {
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
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -1;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -1;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -1;
									}
									if (field.getStateDump().equals(befStr)) {
										return -1;
									}
								}
							}
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

		private static final int NUM = 1;
		private static final int DENOM = 2;

		private final int height;
		private final int width;

		public TatamibariGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TatamibariGenerator(3, 3).generate();
		}

		@Override
		public GeneratorResult generate() {
			RoomMaker2 roomMaker2 = new RoomMaker2(height, width, NUM, DENOM, true);
			TatamibariSolver.Field wkField = new TatamibariSolver.Field(height, width, roomMaker2);
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// ランダムに印をつける。
				Set<Sikaku> alreadySikaku = new HashSet<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Sikaku sikaku = wkField.roomCand[yIndex][xIndex].get(0);
						if (!alreadySikaku.contains(sikaku)) {
							int yLength = sikaku.getRightDown().getyIndex() - sikaku.getLeftUp().getyIndex();
							int xLength = sikaku.getRightDown().getxIndex() - sikaku.getLeftUp().getxIndex();
							Position pos = sikaku.getRandomPos();
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = yLength > xLength ? 1
									: yLength < xLength ? 2 : 3;
							alreadySikaku.add(sikaku);
						}
					}
				}
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new TatamibariSolverForGenerator(new TatamibariSolver.Field(wkField), 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					roomMaker2 = new RoomMaker2(height, width, NUM, DENOM, true);
					wkField = new TatamibariSolver.Field(height, width, roomMaker2);
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
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
			// 記号描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						if (wkField.getNumbers()[yIndex][xIndex] == 1) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + 3 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">"
									+ "</line>");
						} else if (wkField.getNumbers()[yIndex][xIndex] == 2) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize + 3)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">"
									+ "</line>");
						} else if (wkField.getNumbers()[yIndex][xIndex] == 3) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + 3 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">"
									+ "</line>");
							sb.append("<line y1=\""
									+ (yIndex * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize + 3)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">"
									+ "</line>");
						}
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 記号の情報。1が縦、2が横、3が十字。
		private final Integer[][] numbers;
		// 記号ごとの切り分け方の候補
		private List<Sikaku>[][] roomCand;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?tatamibari/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = numbers[yIndex][xIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numberCnt++;
					}
				}
			}
			return String.valueOf(numberCnt);
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public List<Sikaku>[][] getRoomCand() {
			return roomCand;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 部屋の切り方の候補をあらかじめ決めておき、その候補を順次減らす方法を取る。
			// 部屋の切り方の候補はそう多くはならないので。
			initCand();
		}

		/**
		 * 候補の切り方の初期化をする。
		 */
		@SuppressWarnings("unchecked")
		public void initCand() {
			roomCand = new ArrayList[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						List<Sikaku> sikakuList = new ArrayList<>();
						for (int ySize = 1; ySize <= getYLength(); ySize++) {
							for (int xSize = 1; xSize <= getXLength(); xSize++) {
								if (numbers[yIndex][xIndex] == 1) {
									// 縦長限定
									if (ySize <= xSize) {
										continue;
									}
								} else if (numbers[yIndex][xIndex] == 2) {
									// 横長限定
									if (xSize <= ySize) {
										continue;
									}
								} else if (numbers[yIndex][xIndex] == 3) {
									// 正方形限定
									if (xSize != ySize) {
										continue;
									}
								}
								int minY = yIndex - ySize + 1 < 0 ? 0 : yIndex - ySize + 1;
								int maxY = yIndex + ySize > getYLength() ? getYLength() - ySize : yIndex;
								int minX = xIndex - xSize + 1 < 0 ? 0 : xIndex - xSize + 1;
								int maxX = xIndex + xSize > getXLength() ? getXLength() - xSize : xIndex;

								for (int y = minY; y <= maxY; y++) {
									for (int x = minX; x <= maxX; x++) {
										Sikaku sikaku = new Sikaku(new Position(y, x),
												new Position(y + ySize - 1, x + xSize - 1));
										boolean addSikaku = true;
										// 他の部屋のpivotが含まれる候補をあらかじめ除外する。
										outer: for (int otherY = 0; otherY < getYLength(); otherY++) {
											for (int otherX = 0; otherX < getXLength(); otherX++) {
												if (numbers[otherY][otherX] != null
														&& (yIndex != otherY || xIndex != otherX)) {
													Position otherPos = new Position(otherY, otherX);
													if (sikaku.isDuplicate(otherPos)) {
														addSikaku = false;
														break outer;
													}
												}
											}
										}
										if (addSikaku) {
											sikakuList.add(sikaku);
										}
									}
								}
							}
						}
						roomCand[yIndex][xIndex] = sikakuList;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			roomCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.roomCand[yIndex][xIndex] != null) {
						roomCand[yIndex][xIndex] = new ArrayList<>(other.roomCand[yIndex][xIndex]);
					}
				}
			}

		}

		/**
		 * プレーンなフィールド作成
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width, RoomMaker2 roomMaker2) {
			numbers = new Integer[height][width];
			roomCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					List<Sikaku> sikakuList = new ArrayList<>();
					sikakuList.add(roomMaker2.getSikaku(new Position(yIndex, xIndex)));
					roomCand[yIndex][xIndex] = sikakuList;
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						sb.append(existYokoWall(yIndex, xIndex) ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(existTateWall(yIndex, xIndex) ? "□" : "　");
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

		public boolean existTateWall(int yIndex, int xIndex) {
			for (int y = 0; y < getYLength(); y++) {
				for (int x = 0; x < getXLength(); x++) {
					if (roomCand[y][x] != null && roomCand[y][x].size() == 1) {
						if (roomCand[y][x].get(0).getLeftUp().getxIndex() <= xIndex
								&& roomCand[y][x].get(0).getRightDown().getxIndex() >= xIndex) {
							if (roomCand[y][x].get(0).getLeftUp().getyIndex() - 1 == yIndex
									|| roomCand[y][x].get(0).getRightDown().getyIndex() == yIndex) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		public boolean existYokoWall(int yIndex, int xIndex) {
			for (int y = 0; y < getYLength(); y++) {
				for (int x = 0; x < getXLength(); x++) {
					if (roomCand[y][x] != null && roomCand[y][x].size() == 1) {
						if (roomCand[y][x].get(0).getLeftUp().getyIndex() <= yIndex
								&& roomCand[y][x].get(0).getRightDown().getyIndex() >= yIndex) {
							if (roomCand[y][x].get(0).getLeftUp().getxIndex() - 1 == xIndex
									|| roomCand[y][x].get(0).getRightDown().getxIndex() == xIndex) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null) {
						sb.append(roomCand[yIndex][xIndex].size());
					}
				}
			}
			return sb.toString();

		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!allSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null) {
						if (roomCand[yIndex][xIndex].size() != 1) {
							return false;
						}
					}
				}
			}
			return solveAndCheck();
		}

		/**
		 * 各部屋の他の部屋とかぶったり、4つ角ができる候補を消す。
		 * 候補の数が0になってしまったらfalseを返す。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (roomCand[yIndex][xIndex] != null && roomCand[yIndex][xIndex].size() == 1) {
						Sikaku mySikaku = roomCand[yIndex][xIndex].get(0);
						for (int otherY = 0; otherY < getYLength(); otherY++) {
							for (int otherX = 0; otherX < getXLength(); otherX++) {
								if (roomCand[otherY][otherX] != null
										&& (yIndex != otherY || xIndex != otherX)) {
									for (Iterator<Sikaku> iterator = roomCand[otherY][otherX].iterator(); iterator
											.hasNext();) {
										Sikaku anotherSikaku = iterator.next();
										if (mySikaku.isDuplicate(anotherSikaku)
												||
												new Position(mySikaku.getLeftUp().getyIndex() - 1,
														mySikaku.getLeftUp().getxIndex() - 1)
																.equals(anotherSikaku.getRightDown())
												||
												new Position(mySikaku.getRightDown().getyIndex() + 1,
														mySikaku.getRightDown().getxIndex() + 1)
																.equals(anotherSikaku.getLeftUp())) {
											iterator.remove();
										}
									}
									if (roomCand[otherY][otherX].isEmpty()) {
										return false;
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
		 * 畳は全ての部屋を回収しなければならない。
		 */
		private boolean allSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isOK = false;
					outer: for (int y = 0; y < getYLength(); y++) {
						for (int x = 0; x < getXLength(); x++) {
							if (roomCand[y][x] != null) {
								for (Sikaku sikaku : roomCand[y][x]) {
									if (sikaku.isDuplicate(pos)) {
										isOK = true;
										break outer;
									}
								}
							}
						}
					}
					if (!isOK) {
						return false;
					}
				}
			}
			return true;
		}
	}

	protected final Field field;
	protected int count = 0;

	public TatamibariSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TatamibariSolver(Field field) {
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
		System.out.println(new TatamibariSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.roomCand[yIndex][xIndex] != null) {
					if (field.roomCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Sikaku> iterator = field.roomCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							Sikaku oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.roomCand[yIndex][xIndex].clear();
							virtual.roomCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (field.roomCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (field.roomCand[yIndex][xIndex].size() == 1) {
						if (!field.solveAndCheck()) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
