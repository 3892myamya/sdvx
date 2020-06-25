package myamya.other.solver.tentaisho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class TentaishoSolver implements Solver {
	public static class TentaishoGenerator implements Generator {

		static class TentaishoSolverForGenerator extends TentaishoSolver {
			private final int limit;

			public TentaishoSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						count++;
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -2;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -2;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -2;
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

		private final int height;
		private final int width;

		public TentaishoGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TentaishoGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			long start = System.nanoTime();
			TentaishoSolver.Field wkField = null;
			Map<Integer, Position> planetPosMap = null;
			Map<Integer, Set<Position>> planetAreaMap = null;
			List<Position> candPosList = new ArrayList<>();
			for (int yIndex = 0; yIndex < height * 2 - 1; yIndex++) {
				for (int xIndex = 0; xIndex < width * 2 - 1; xIndex++) {
					candPosList.add(new Position(yIndex, xIndex));
				}
			}
			// 未連結の1x1のマス一覧
			Map<Position, Integer> yetPosMap = null;
			while (true) {
				// 初期配置作成
				Collections.shuffle(candPosList);
				planetPosMap = new HashMap<>();
				yetPosMap = new HashMap<>();
				planetAreaMap = new HashMap<>();
				int number = 0;
				for (Position pos : candPosList) {
					boolean isOk = true;
					// 中点の四角は発生率低めにする
					if (pos.getyIndex() % 2 == 1) {
						if (Math.random() * 3 < 2) {
							isOk = false;
						}
					}
					if (pos.getxIndex() % 2 == 1) {
						if (Math.random() * 3 < 2) {
							isOk = false;
						}
					}
					if (isOk) {
						for (Position alreadyPos : planetPosMap.values()) {
							// すでに追加した位置とかぶっていれば追加しない。
							if (pos.getyIndex() % 2 == 1) {
								if ((pos.getyIndex() == alreadyPos.getyIndex() - 2
										|| pos.getyIndex() == alreadyPos.getyIndex() - 1
										|| pos.getyIndex() == alreadyPos.getyIndex()
										|| pos.getyIndex() == alreadyPos.getyIndex() + 1
										|| pos.getyIndex() == alreadyPos.getyIndex() + 2)) {
									// かぶってるかも
								} else {
									continue;
								}
							} else {
								if ((pos.getyIndex() == alreadyPos.getyIndex() - 1
										|| pos.getyIndex() == alreadyPos.getyIndex()
										|| pos.getyIndex() == alreadyPos.getyIndex() + 1)) {
									// かぶってるかも
								} else {
									continue;
								}
							}
							if (pos.getxIndex() % 2 == 1) {
								if ((pos.getxIndex() == alreadyPos.getxIndex() - 2
										|| pos.getxIndex() == alreadyPos.getxIndex() - 1
										|| pos.getxIndex() == alreadyPos.getxIndex()
										|| pos.getxIndex() == alreadyPos.getxIndex() + 1
										|| pos.getxIndex() == alreadyPos.getxIndex() + 2)) {
									// かぶってるかも
								} else {
									continue;
								}
							} else {
								if ((pos.getxIndex() == alreadyPos.getxIndex() - 1
										|| pos.getxIndex() == alreadyPos.getxIndex()
										|| pos.getxIndex() == alreadyPos.getxIndex() + 1)) {
									// かぶってるかも
								} else {
									continue;
								}
							}
							isOk = false;
							break;
						}
					}
					if (isOk) {
						planetPosMap.put(number, pos);
						Set<Position> wkSet = new HashSet<Position>();
						wkSet.add(new Position(pos.getyIndex() / 2, pos.getxIndex() / 2));
						if (pos.getyIndex() % 2 != 0) {
							wkSet.add(new Position(pos.getyIndex() + 1 / 2, pos.getxIndex() / 2));
						}
						if (pos.getxIndex() % 2 != 0) {
							wkSet.add(new Position(pos.getyIndex() / 2, pos.getxIndex() + 1 / 2));
						}
						if (pos.getyIndex() % 2 != 0 && pos.getxIndex() % 2 != 0) {
							wkSet.add(new Position(pos.getyIndex() + 1 / 2, pos.getxIndex() + 1 / 2));
						}
						planetAreaMap.put(number, wkSet);
						if (pos.getyIndex() % 2 == 0 && pos.getxIndex() % 2 == 0) {
							yetPosMap.put(new Position(pos.getyIndex() / 2, pos.getxIndex() / 2), number);
						}
						number++;
					}
				}
				wkField = new TentaishoSolver.Field(height, width, planetPosMap);
				if (new TentaishoSolverForGenerator(wkField, 500).solve2() >= 0) {
					// 解けたら初期配置確定。まあ解けないことないはずだけど一応
					break;
				}
			}
			int breakCnt = 0;
			while (true) {
				if (breakCnt >= 2000) {
					break;
				}
				// 星を1つずつ減らす
				// 基準の星を選ぶ
				int useNumber = new ArrayList<>(planetPosMap.keySet()).get((int) (Math.random() * planetPosMap.size()));
				int innerBreakCnt = 0;
				while (true) {
					// ある程度1つの天体がまとまって大きくなるようにする
					if (innerBreakCnt >= 7) {
						break;
					}
					Position pivotPlanetPos = planetPosMap.get(useNumber);
					// 基準の星に隣接するマスを1つ選ぶ
					Set<Position> useArea = planetAreaMap.get(useNumber);
					Set<Position> candPosSet = new HashSet<>();
					for (Position pos : useArea) {
						Position upPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						Position rightPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						Position downPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						Position leftPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (!useArea.contains(upPos) && yetPosMap.containsKey(upPos)) {
							candPosSet.add(upPos);
						}
						if (!useArea.contains(rightPos) && yetPosMap.containsKey(rightPos)) {
							candPosSet.add(rightPos);
						}
						if (!useArea.contains(downPos) && yetPosMap.containsKey(downPos)) {
							candPosSet.add(downPos);
						}
						if (!useArea.contains(leftPos) && yetPosMap.containsKey(leftPos)) {
							candPosSet.add(leftPos);
						}
					}
					if (!candPosSet.isEmpty()) {
						Position candPos = new ArrayList<>(candPosSet).get((int) (Math.random() * candPosSet.size()));
						// そのマスと点対称になるマスを選ぶ
						Position anotherPos = new Position(
								(pivotPlanetPos.getyIndex() * 2 - candPos.getyIndex() * 2) / 2,
								(pivotPlanetPos.getxIndex() * 2 - candPos.getxIndex() * 2) / 2);
						if (yetPosMap.containsKey(anotherPos)) {
							// 消せたら消して解きチェック。
							HashMap<Integer, Position> newPlanetPosMap = new HashMap<>(planetPosMap);
							newPlanetPosMap.remove(yetPosMap.get(candPos));
							newPlanetPosMap.remove(yetPosMap.get(anotherPos));
							Field virtual = new TentaishoSolver.Field(height, width, newPlanetPosMap);
							if (new TentaishoSolverForGenerator(virtual, 500).solve2() < 0) {
								// 解けなかったらやり直し。
								// 消せない回数+解けない回数が一定回数に達したら抜ける
								breakCnt++;
								innerBreakCnt++;
							} else {
								// 解ければ星を減らす。
								planetPosMap = newPlanetPosMap;
								planetAreaMap.remove(yetPosMap.get(candPos));
								planetAreaMap.remove(yetPosMap.get(anotherPos));
								if (useArea.size() == 1) {
									yetPosMap.remove(
											new Position(pivotPlanetPos.getyIndex() / 2,
													pivotPlanetPos.getxIndex() / 2));
								}
								yetPosMap.remove(candPos);
								yetPosMap.remove(anotherPos);
								useArea.add(candPos);
								useArea.add(anotherPos);
							}
						} else {
							// 消せない場合はやり直し。
							breakCnt++;
							innerBreakCnt++;
						}
					} else {
						// 消せない場合はやり直し。
						breakCnt++;
						innerBreakCnt++;
					}
				}
			}
			wkField = new TentaishoSolver.Field(height, width, planetPosMap);
			int level = new TentaishoSolverForGenerator(wkField, 500).solve2();
			level = (int) Math.sqrt(level * 15 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(星：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin)
							+ "\" width=\""
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

			// 丸描画
			for (Entry<Integer, Position> entry : wkField.getNumbers().entrySet()) {
				sb.append(
						"<circle cy=\"" + (entry.getValue().getyIndex() * (baseSize / 2) + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (entry.getValue().getxIndex() * (baseSize / 2) + (baseSize / 2) + baseSize)
								+ "\" r=\""
								+ 2
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
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

		private final int height;
		private final int width;

		// 確定数字の情報。ここでのPositionは長さ10(0-9)の場合は19通り(0-18)の候補を取ることに注意。
		private Map<Integer, Position> numbers;
		// 確定数字が白か黒か。黒ならtrue
		private final Map<Integer, Boolean> isBlack;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 領域の確定した数字
		private Set<Integer> fixedNumber;

		public Map<Integer, Position> getNumbers() {
			return numbers;
		}

		/**
		 * TODO 通常のエンコードとは違うURLになる。
		 * また、星の色はすべて白になる。
		 */
		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?tentaisho/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < (getYLength() * 2 - 1) * (getXLength() * 2 - 1); i++) {
				int yIndex = i / (getXLength() * 2 - 1);
				int xIndex = i % (getXLength() * 2 - 1);
				if (!numbers.containsValue(new Position(yIndex, xIndex))) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = 0;
					String numStr = Integer.toHexString(num);
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
			return String.valueOf(numbers.size());
		}

		public boolean isBlack(Integer number) {
			return isBlack.get(number);
		}

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public int getYLength() {
			return height;
		}

		public int getXLength() {
			return width;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			this.height = height;
			this.width = width;
			numbersCand = new ArrayList[height][width];
			numbers = new HashMap<>();
			isBlack = new HashMap<>();
			int index = 0;
			int number = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					if (ch == '.') {
						//
					} else {
						Position pos = new Position(index / (getXLength() * 2 - 1), index % (getXLength() * 2 - 1));
						int numelicValue = Character.getNumericValue(ch);
						numbers.put(number, pos);
						isBlack.put(number, numelicValue % 2 != 0);
						numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2] = new ArrayList<>();
						numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2].add(number);
						if (pos.getyIndex() % 2 != 0) {
							numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2] = new ArrayList<>();
							numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2].add(number);
						}
						if (pos.getxIndex() % 2 != 0) {
							numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
							numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2].add(number);
						}
						if (pos.getyIndex() % 2 != 0 && pos.getxIndex() % 2 != 0) {
							numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
							numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2].add(number);
						}
						number++;
						index = index + numelicValue / 2;
					}
					index++;
				}
			}
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex] == null) {
						numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (Integer i : numbers.keySet()) {
							numbersCand[yIndex][xIndex].add(i);
						}
					}
				}
			}
			fixedNumber = new HashSet<>();
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			isBlack = other.isBlack;
			height = other.height;
			width = other.width;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			fixedNumber = new HashSet<>(other.fixedNumber);
		}

		/**
		 * planetListから作成。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width, Map<Integer, Position> planetPosMap) {
			this.height = height;
			this.width = width;
			numbers = planetPosMap;
			numbersCand = new ArrayList[height][width];
			isBlack = new HashMap<>();
			for (Entry<Integer, Position> entry : planetPosMap.entrySet()) {
				int number = entry.getKey();
				Position pos = entry.getValue();
				isBlack.put(number, false);
				numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2] = new ArrayList<>();
				numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2].add(number);
				if (pos.getyIndex() % 2 != 0) {
					numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2] = new ArrayList<>();
					numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2].add(number);
				}
				if (pos.getxIndex() % 2 != 0) {
					numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
					numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2].add(number);
				}
				if (pos.getyIndex() % 2 != 0 && pos.getxIndex() % 2 != 0) {
					numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
					numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2].add(number);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex] == null) {
						numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (Integer i : numbers.keySet()) {
							numbersCand[yIndex][xIndex].add(i);
						}
					}
				}
			}
			fixedNumber = new HashSet<>();
		}

		/**
		 * プレーン。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			this.height = height;
			this.width = width;
			numbersCand = new ArrayList[height][width];
			numbers = new HashMap<>();
			isBlack = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
				}
			}
			fixedNumber = new HashSet<>();
		}

		private static final String HALF_NUMS_36 = "0 1 2 3 4 5 6 7 8 9 a b c d e f g h i j k l m n o p q r s t u v w x y z ";
		private static final String FULL_NUMS_36 = "０１２３４５６７８９ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = Integer.toString(numbersCand[yIndex][xIndex].get(0), 36);
						int index = HALF_NUMS_36.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS_36.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					} else {
						sb.append("　");
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * ある数字が確定している場合、点対称のマスも確定する。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						if (fixedNumber.contains(number)) {
							continue;
						}
						Position pivot = numbers.get(number);
						Position pos = new Position(yIndex, xIndex);
						Position anotherPos = new Position((pivot.getyIndex() * 2 - pos.getyIndex() * 2) / 2,
								(pivot.getxIndex() * 2 - pos.getxIndex() * 2) / 2);
						if (anotherPos.getyIndex() < 0 || anotherPos.getyIndex() >= getYLength()
								|| anotherPos.getxIndex() < 0 || anotherPos.getxIndex() >= getXLength()
								|| !numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].contains(number)) {
							return false;
						} else {
							numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].clear();
							numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].add(number);
						}
					}
				}
			}
			return true;
		}

		/**
		 * 基準マスに対して点対称のマスが回収不可能の場合、もう一方も回収できない。
		 */
		public boolean targetSolve() {
			for (Entry<Integer, Position> entry : numbers.entrySet()) {
				Integer number = entry.getKey();
				if (fixedNumber.contains(number)) {
					continue;
				}
				Position pivot = entry.getValue();
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						Position anotherPos = new Position((pivot.getyIndex() * 2 - pos.getyIndex() * 2) / 2,
								(pivot.getxIndex() * 2 - pos.getxIndex() * 2) / 2);
						if (anotherPos.getyIndex() < 0 || anotherPos.getyIndex() >= getYLength()
								|| anotherPos.getxIndex() < 0 || anotherPos.getxIndex() >= getXLength()
								|| !numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].contains(number)) {
							numbersCand[yIndex][xIndex].remove(number);
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字がつながっていないマスは回収できない。
		 */
		public boolean connectSolve() {
			for (Entry<Integer, Position> entry : numbers.entrySet()) {
				Integer number = entry.getKey();
				if (fixedNumber.contains(number)) {
					continue;
				}
				Set<Position> numberPosSet = new HashSet<>();
				Position numberPos = new Position(entry.getValue().getyIndex() / 2, entry.getValue().getxIndex() / 2);
				numberPosSet.add(numberPos);
				Set<Object> token = new HashSet<>();
				setContinuePosSet(number, numberPos, numberPosSet, null, token);
				if (token.isEmpty()) {
					// トークンが空なら、その数字の領域は確定。
					fixedNumber.add(number);
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (!numberPosSet.contains(new Position(yIndex, xIndex))) {
							numbersCand[yIndex][xIndex].remove(number);
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右にnumを含むマスを無制限につなげていく。
		 * 候補が限られない場合はtokenにダミー情報を入れる。
		 */
		private void setContinuePosSet(Integer number, Position pos, Set<Position> continuePosSet, Direction from,
				Set<Object> token) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.DOWN, token);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.LEFT, token);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.UP, token);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.RIGHT, token);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			if (!numberSolve()) {
				return false;
			}
			if (!targetSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public TentaishoSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TentaishoSolver(Field field) {
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
		System.out.println(new TentaishoSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			count++;
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
		System.out.println("難易度:" + (count * 15));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 15 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 15).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.numbersCand[yIndex][xIndex].size() != 1) {
					for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
						count++;
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.numbersCand[yIndex][xIndex].clear();
						virtual.numbersCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.numbersCand[yIndex][xIndex].size() == 0) {
						return false;
					}
				}
				if (field.numbersCand[yIndex][xIndex].size() == 1) {
					if (!field.solveAndCheck()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}