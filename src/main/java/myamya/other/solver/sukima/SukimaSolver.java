package myamya.other.solver.sukima;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.PuzzleType;
import myamya.other.solver.RoomMaker.RoomMaker2;
import myamya.other.solver.Solver;
import myamya.other.solver.chocobanana.ChocobananaSolver;

public class SukimaSolver implements Solver {
	public static class SukimaGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class SukimaSolverForGenerator extends SukimaSolver {

			private final int limit;

			public SukimaSolverForGenerator(Field field, int limit) {
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
							if (!candSolve(field, recursiveCnt == 2 ? 2 : recursiveCnt)) {
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

		private static final int NUM = 1;
		private static final int DENOM = 3;

		private final int height;
		private final int width;

		public SukimaGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SukimaGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			RoomMaker2 roomMaker2 = new RoomMaker2(height, width, NUM, DENOM, true);
			SukimaSolver.Field wkField = new SukimaSolver.Field(height, width, roomMaker2);
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				solutionStr = PenpaEditLib.convertSolutionRoom(height, wkField.getYokoWall(), wkField.getTateWall());
				// ランダムに数字を入れる。
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						for (Sikaku sikaku : wkField.getSquareFixed()) {
							if (sikaku.getPosSet().contains(new Position(yIndex, xIndex))) {
								int sukima = -1;
								boolean existNum = false;
								for (Position pos : sikaku.getPosSet()) {
									if (wkField.numbers[pos.getyIndex()][pos.getxIndex()] != null) {
										existNum = true;
									} else {
										sukima++;
									}
								}
								if (!existNum || Math.random() < 0.3) {
									wkField.numbers[yIndex][xIndex] = sukima;
									for (Position pos : sikaku.getPosSet()) {
										if (wkField.numbers[pos.getyIndex()][pos.getxIndex()] != null) {
											wkField.numbers[pos.getyIndex()][pos.getxIndex()] = sukima;
										}
									}
								}
							}
						}
					}
				}
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new SukimaSolverForGenerator(new SukimaSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					roomMaker2 = new RoomMaker2(height, width, NUM, DENOM, true);
					wkField = new SukimaSolver.Field(height, width, roomMaker2);
				} else {
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersField(wkField.numbers, PuzzleType.MAKEROOM);
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);
			level = (int) Math.sqrt(level * 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			Wall[][] yokoWall = wkField.getYokoWall();
			Wall[][] tateWall = wkField.getTateWall();
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| yokoWall[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex != -1 && xIndex != wkField.getXLength() - 1
								&& yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| tateWall[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex != -1 && yIndex != wkField.getYLength() - 1
								&& tateWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");

						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
								+ "black" + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 四角の配置の候補
		protected Set<Sikaku> squareCand;
		// 確定した四角候補
		protected Set<Sikaku> squareFixed;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Wall[][] getTateWall() {
			Wall[][] tateWall = new Wall[getYLength() - 1][getXLength()];
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			for (Sikaku sikaku : getSquareFixed()) {
				for (int xIndex = sikaku.getLeftUp().getxIndex(); xIndex <= sikaku.getRightDown()
						.getxIndex(); xIndex++) {
					if (sikaku.getLeftUp().getyIndex() > 0) {
						tateWall[sikaku.getLeftUp().getyIndex() - 1][xIndex] = Wall.EXISTS;
					}
					if (sikaku.getRightDown().getyIndex() < getYLength() - 1) {
						tateWall[sikaku.getRightDown().getyIndex()][xIndex] = Wall.EXISTS;
					}
				}
			}
			return tateWall;
		}

		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			for (Sikaku sikaku : getSquareFixed()) {
				for (int yIndex = sikaku.getLeftUp().getyIndex(); yIndex <= sikaku.getRightDown()
						.getyIndex(); yIndex++) {
					if (sikaku.getLeftUp().getxIndex() > 0) {
						yokoWall[yIndex][sikaku.getLeftUp().getxIndex() - 1] = Wall.EXISTS;
					}
					if (sikaku.getRightDown().getxIndex() < getXLength() - 1) {
						yokoWall[yIndex][sikaku.getRightDown().getxIndex()] = Wall.EXISTS;
					}
				}
			}
			return yokoWall;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
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

		public Field(String fieldStr) {
			numbers = PenpaEditLib.getNumbers(fieldStr);
			initCand();
		}

		private void initCand() {
			squareCand = new HashSet<>();
			squareFixed = new HashSet<>();
			// 部屋の切り方の候補をあらかじめ決めておき、その候補を順次減らす方法を取る。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int minY = yIndex;
					int minX = xIndex;
					for (int maxY = minY; maxY < getYLength(); maxY++) {
						for (int maxX = minX; maxX < getXLength(); maxX++) {
							Sikaku sikaku = new Sikaku(new Position(minY, minX), new Position(maxY, maxX));
							// 数字は数字のないマスの数である。
							int sukima = 0;
							int number = -1;
							boolean addSikaku = true;
							for (Position pos : sikaku.getPosSet()) {
								if (numbers[pos.getyIndex()][pos.getxIndex()] == null) {
									sukima++;
								} else {
									if (number == -1) {
										number = numbers[pos.getyIndex()][pos.getxIndex()];
									} else if (number != numbers[pos.getyIndex()][pos.getxIndex()]) {
										addSikaku = false;
										break;
									}
								}
							}
							if (addSikaku && sukima == number) {
								squareCand.add(sikaku);
							}
						}
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			squareCand = new HashSet<>(other.squareCand);
			squareFixed = new HashSet<>(other.squareFixed);
		}

		public Field(int height, int width, RoomMaker2 roomMaker2) {
			numbers = new Integer[height][width];
			squareCand = new HashSet<>();
			squareFixed = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					squareFixed.add(roomMaker2.getSikaku(new Position(yIndex, xIndex)));
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

		private boolean existTateWall(int yIndex, int xIndex) {
			for (Sikaku sikaku : squareFixed) {
				Set<Position> posSet = sikaku.getPosSet();
				if (!posSet.contains(new Position(yIndex, xIndex))
						&& posSet.contains(new Position(yIndex + 1, xIndex))) {
					return true;
				}
				if (posSet.contains(new Position(yIndex, xIndex))
						&& !posSet.contains(new Position(yIndex + 1, xIndex))) {
					return true;
				}
			}
			return false;
		}

		private boolean existYokoWall(int yIndex, int xIndex) {
			for (Sikaku sikaku : squareFixed) {
				Set<Position> posSet = sikaku.getPosSet();
				if (!posSet.contains(new Position(yIndex, xIndex))
						&& posSet.contains(new Position(yIndex, xIndex + 1))) {
					return true;
				}
				if (posSet.contains(new Position(yIndex, xIndex))
						&& !posSet.contains(new Position(yIndex, xIndex + 1))) {
					return true;
				}
			}
			return false;
		}

		public Set<Sikaku> getSquareFixed() {
			return squareFixed;
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();

		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
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
			return squareCand.size() == 0 && solveAndCheck();
		}

		/**
		 * 確定した四角がある場合、それとかぶる候補を消す。
		 */
		private boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 全てのマスが回収されている。
		 */
		private boolean allSolve() {
			Set<Position> allPosSet = new HashSet<>();
			for (Sikaku fixed : squareFixed) {
				allPosSet.addAll(fixed.getPosSet());
				if (allPosSet.size() == getXLength() * getYLength()) {
					return true;
				}
			}
			for (Sikaku fixed : squareCand) {
				allPosSet.addAll(fixed.getPosSet());
				if (allPosSet.size() == getXLength() * getYLength()) {
					return true;
				}
			}
			return false;
		}

	}

	protected final Field field;
	protected int count = 0;

	public SukimaSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public SukimaSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,38,0,1,1,418,418,286,286\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"30\":[\"1\",1,\"1\"],\"46\":[\"1\",1,\"1\"],\"47\":[\"5\",1,\"1\"],\"58\":[\"4\",1,\"1\"],\"60\":[\"4\",1,\"1\"],\"63\":[\"4\",1,\"1\"],\"94\":[\"4\",1,\"1\"],\"95\":[\"4\",1,\"1\"],\"101\":[\"2\",1,\"1\"],\"117\":[\"5\",1,\"1\"],\"121\":[\"4\",1,\"1\"],\"129\":[\"3\",1,\"1\"],\"144\":[\"4\",1,\"1\"],\"149\":[\"3\",1,\"1\"],\"150\":[\"2\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]"; // urlを入れれば試せる
		System.out.println(new ChocobananaSolver(fieldStr).solve());
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
		int level = (int) Math.sqrt(count * 3 / 3);
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			Sikaku oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.squareCand.remove(oneCand);
			virtual.squareFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.squareCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.squareCand = virtual2.squareCand;
				field.squareFixed = virtual2.squareFixed;
			} else if (!allowNotBlack) {
				field.squareCand = virtual.squareCand;
				field.squareFixed = virtual.squareFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}
