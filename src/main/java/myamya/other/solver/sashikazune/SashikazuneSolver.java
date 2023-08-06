package myamya.other.solver.sashikazune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class SashikazuneSolver implements Solver {
	public static class SashikazuneGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class SashikazuneSolverForGenerator extends SashikazuneSolver {
			private final int limit;
			private int innerCount = 0;

			public SashikazuneSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					count = field.lshapeCand.size();
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -1;
						}
						int recursiveCnt = 0;
						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
							count = count * 2;
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
				if (innerCount >= limit) {
					throw new CountOverException();
				} else {
					innerCount++;
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public SashikazuneGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SashikazuneGenerator(4, 4).generate();
		}

		@Override
		public GeneratorResult generate() {
			SashikazuneSolver.Field wkField = new SashikazuneSolver.Field(height, width);
			List<Lshape> lshapeCandBase = new ArrayList<>(wkField.lshapeCand);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Integer> indexList = new ArrayList<>();
				List<Lshape> useCand = new ArrayList<>(wkField.lshapeCand);
				for (int i = 0; i < useCand.size(); i++) {
					indexList.add(i);
				}
				Collections.shuffle(indexList);
				// 問題生成部
				while (!wkField.isSolved()) {
					Lshape oneCand = useCand.get(index);
					if (wkField.lshapeCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							SashikazuneSolver.Field virtual = new SashikazuneSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.lshapeCand.remove(oneCand);
							} else if (masuNum < 2) {
								virtual.lshapeCand.remove(oneCand);
								virtual.lshapeFixed.add(oneCand);
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.lshapeCand = virtual.lshapeCand;
								wkField.lshapeFixed = virtual.lshapeFixed;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new SashikazuneSolver.Field(height, width, lshapeCandBase);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め。各L字型にランダムで2個数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				for (Lshape fixed : wkField.lshapeFixed) {
					List<Position> posList = new ArrayList<>(fixed.getPosMap().keySet());
					Collections.shuffle(posList);
					wkField.numbers[posList.get(0).getyIndex()][posList.get(0).getxIndex()] = fixed.getPosMap()
							.get(posList.get(0));
					wkField.numbers[posList.get(1).getyIndex()][posList.get(1).getxIndex()] = fixed.getPosMap()
							.get(posList.get(1));
					numberPosList.add(posList.get(0));
					numberPosList.add(posList.get(1));
				}
				// System.out.println(wkField);
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new SashikazuneSolverForGenerator(wkField, 5).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new SashikazuneSolver.Field(height, width, lshapeCandBase);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						SashikazuneSolver.Field virtual = new SashikazuneSolver.Field(wkField);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						virtual.initCand();
						int solveResult = new SashikazuneSolverForGenerator(virtual, 5).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 数字描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");

					}
				}
			}
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	// L字型
	public static class Lshape {
		private final Map<Position, Integer> posMap;
		private final Set<Position> yokoWall;
		private final Set<Position> tateWall;

		Lshape(Position curvePos, int toY, int toX) {
			posMap = new LinkedHashMap<>();
			posMap.put(curvePos, 1);
			if (curvePos.getyIndex() < toY) {
				for (int y = curvePos.getyIndex() + 1; y <= toY; y++) {
					posMap.put(new Position(y, curvePos.getxIndex()), y - curvePos.getyIndex() + 1);
				}
			} else {
				for (int y = curvePos.getyIndex() - 1; y >= toY; y--) {
					posMap.put(new Position(y, curvePos.getxIndex()), curvePos.getyIndex() - y + 1);
				}
			}
			if (curvePos.getxIndex() < toX) {
				for (int x = curvePos.getxIndex() + 1; x <= toX; x++) {
					posMap.put(new Position(curvePos.getyIndex(), x), x - curvePos.getxIndex() + 1);
				}
			} else {
				for (int x = curvePos.getxIndex() - 1; x >= toX; x--) {
					posMap.put(new Position(curvePos.getyIndex(), x), curvePos.getxIndex() - x + 1);
				}
			}
			yokoWall = new HashSet<>();
			tateWall = new HashSet<>();
			for (Position pos : posMap.keySet()) {
				if (!posMap.containsKey(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
					tateWall.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (!posMap.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
					tateWall.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (!posMap.containsKey(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
					yokoWall.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
				if (!posMap.containsKey(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
					yokoWall.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
			}
		}

		// 自分が占めるマス一覧を返す
		public Map<Position, Integer> getPosMap() {
			return posMap;
		}

		// 重複を判断して返す
		public boolean isDuplicate(Lshape other) {
			for (Position item : other.posMap.keySet()) {
				if (posMap.keySet().contains(item)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return posMap.toString();
		}

		/**
		 * 自分が作ることになる横壁の位置を返す。外壁(-1、xlength)も含むので注意
		 */
		public Set<Position> getYokoWall() {
			return yokoWall;
		}

		/**
		 * 自分が作ることになる縦壁の位置を返す。外壁(-1、ylength)も含むので注意
		 */
		public Set<Position> getTateWall() {
			return tateWall;
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		protected final Integer[][] numbers;
		// 配置の候補
		protected List<Lshape> lshapeCand;
		// 確定した候補
		protected List<Lshape> lshapeFixed;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?sashikazune/" + getXLength() + "/" + getYLength() + "/");
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

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			lshapeCand = makeLshapeCandBase();
			lshapeFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。候補の算出のため、数字マスが埋まっている前提。
		 */
		protected List<Lshape> makeLshapeCandBase() {
			List<Lshape> result = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position curvePos = new Position(yIndex, xIndex);
					for (int toY = 0; toY < getYLength(); toY++) {
						for (int toX = 0; toX < getXLength(); toX++) {
							if (toY != yIndex && toX != xIndex) {
								Lshape lshape = new Lshape(curvePos, toY, toX);
								boolean isAdd = true;
								int numCnt = 0;
								for (Entry<Position, Integer> e : lshape.getPosMap().entrySet()) {
									if (numbers[e.getKey().getyIndex()][e.getKey().getxIndex()] != null) {
										numCnt++;
										// 数字が3個以上入ったらNG
										if (numCnt >= 3) {
											isAdd = false;
											break;
										}
										// 数字が違ったらNG
										if (numbers[e.getKey().getyIndex()][e.getKey().getxIndex()] != e.getValue()) {
											isAdd = false;
											break;
										}
									}
								}
								if (isAdd) {
									result.add(lshape);
								}
							}
						}
					}
				}
			}
			return result;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int capacity;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		public Field(Field other) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			lshapeCand = new ArrayList<>(other.lshapeCand);
			lshapeFixed = new ArrayList<>(other.lshapeFixed);
		}

		public Field(int height, int width, List<Lshape> lshapeCandBase) {
			numbers = new Integer[height][width];
			lshapeCand = new ArrayList<>(lshapeCandBase);
			lshapeFixed = new ArrayList<>();
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (Lshape fixed : lshapeFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (Lshape cand : lshapeCand) {
				candWallPosSet.addAll(cand.getYokoWall());
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return yokoWall;
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getTateWall() {
			Wall[][] tateWall = new Wall[getYLength() - 1][getXLength()];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (Lshape fixed : lshapeFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (Lshape cand : lshapeCand) {
				candWallPosSet.addAll(cand.getTateWall());
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return tateWall;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Wall[][] yokoWall = getYokoWall();
			Wall[][] tateWall = getTateWall();
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
			return lshapeFixed.size() + ":" + lshapeCand.size();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。すでにかぶっていたらfalse。
		 */
		private boolean sikakuSolve() {
			for (Lshape fixed : lshapeFixed) {
				for (Lshape otherFixed : lshapeFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<Lshape> iterator = lshapeCand.iterator(); iterator.hasNext();) {
					Lshape oneCand = iterator.next();
					if (oneCand.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 埋まらないマスが出来てしまった場合falseを返す。 逆に、あるマスを埋められる候補が1つしかない場合、確定形に昇格する。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 既に確定しているマスを除外
					Position pos = new Position(yIndex, xIndex);
					boolean isFixed = false;
					for (Lshape fixed : lshapeFixed) {
						if (fixed.getPosMap().keySet().contains(pos)) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					List<Lshape> pickup = new ArrayList<>();
					for (Lshape cand : lshapeCand) {
						if (cand.getPosMap().keySet().contains(pos)) {
							pickup.add(cand);
						}
					}
					// 候補が1つもなければfalse
					if (pickup.isEmpty()) {
						return false;
					}
					// 候補が1つに定まっていれば確定
					if (pickup.size() == 1) {
						lshapeCand.remove(pickup.get(0));
						lshapeFixed.add(pickup.get(0));
					}
					// 候補全てに含まれるポジションリストを作成
					Set<Position> allPos = new HashSet<>(pickup.get(0).getPosMap().keySet());
					for (Lshape onePick : pickup) {
						allPos.retainAll(onePick.getPosMap().keySet());
					}
					// 候補全てに含まれるマスを含む候補のうちpickup外のものを削除
					for (Position targetPos : allPos) {
						for (Iterator<Lshape> iterator = lshapeCand.iterator(); iterator.hasNext();) {
							Lshape oneCand = iterator.next();
							if (oneCand.getPosMap().keySet().contains(targetPos) && !pickup.contains(oneCand)) {
								iterator.remove();
							}
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			return lshapeCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public SashikazuneSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public SashikazuneSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "";// urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new SashikazuneSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		count = field.lshapeCand.size();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				count = count * 2;
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
		for (Iterator<Lshape> iterator = field.lshapeCand.iterator(); iterator.hasNext();) {
			Lshape oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.lshapeCand.remove(oneCand);
			virtual.lshapeFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.lshapeCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.lshapeCand = virtual2.lshapeCand;
				field.lshapeFixed = virtual2.lshapeFixed;
			} else if (!allowNotBlack) {
				field.lshapeCand = virtual.lshapeCand;
				field.lshapeFixed = virtual.lshapeFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}