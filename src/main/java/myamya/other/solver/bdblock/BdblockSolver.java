package myamya.other.solver.bdblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class BdblockSolver implements Solver {
	public static class BdblockGenerator implements Generator {

		static class ExtendedField extends BdblockSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			/**
			 * 作問中は壁の数が1にならなければOK
			 */
			@Override
			public boolean wallSolve() {
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = yokoWall[yIndex][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallRight = tateWall[yIndex][xIndex + 1];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallDown = yokoWall[yIndex + 1][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallLeft = tateWall[yIndex][xIndex];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						if (existsCount == 1 && notExistsCount == 3) {
							return false;
						}
						if (existsCount == 1 && notExistsCount == 2) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
						} else if (existsCount == 0 && notExistsCount == 3) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
					}
				}
				return true;
			}

			@Override
			public boolean notStandAloneSolve() {
				return true;
			}

			public List<Set<Position>> makeRooms() {
				List<Set<Position>> result = new ArrayList<>();
				Set<Position> whitePosSet = new HashSet<>();
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
				while (!whitePosSet.isEmpty()) {
					Position pivot = new ArrayList<>(whitePosSet).get(0);
					Set<Position> continuePosSet = new HashSet<>();
					continuePosSet.add(pivot);
					Set<Integer> findNumber = new HashSet<>();
					setContinueCandPosSet(pivot, continuePosSet, null, findNumber);
					result.add(continuePosSet);
					whitePosSet.removeAll(continuePosSet);
				}
				return result;
			}
		}

		static class BdblockSolverForGenerator extends BdblockSolver {
			private final int limit;

			public BdblockSolverForGenerator(Field field, int limit) {
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

		public BdblockGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new BdblockGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
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
							ExtendedField virtual = new ExtendedField(wkField);
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
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new ExtendedField(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字・星埋め
				List<Set<Position>> rooms = wkField.makeRooms();
				Collections.shuffle(rooms);
				Integer roomIdx = 0;
				List<Position> numberPosList = new ArrayList<>();
				Map<Integer, Integer> roomNumberCnt = new HashMap<>();
				for (Set<Position> room : rooms) {
					roomIdx++;
					for (Position onePos : room) {
						wkField.numbers[onePos.getyIndex()][onePos.getxIndex()] = roomIdx;
						numberPosList.add(onePos);
					}
					roomNumberCnt.put(roomIdx, room.size());
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						int existsCount = 0;
						Wall wallUp = wkField.yokoWall[yIndex][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallRight = wkField.tateWall[yIndex][xIndex + 1];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallDown = wkField.yokoWall[yIndex + 1][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallLeft = wkField.tateWall[yIndex][xIndex];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						}
						if (existsCount == 3 || existsCount == 4) {
							wkField.hoshi[yIndex + 1][xIndex + 1] = true;
						}
					}
				}
				// 外枠の星
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					if (wkField.tateWall[yIndex][0] == Wall.EXISTS) {
						wkField.hoshi[yIndex + 1][0] = true;
					}
					if (wkField.tateWall[yIndex][wkField.getXLength() - 1] == Wall.EXISTS) {
						wkField.hoshi[yIndex + 1][wkField.getXLength()] = true;
					}
				}
				for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
					if (wkField.yokoWall[0][xIndex] == Wall.EXISTS) {
						wkField.hoshi[0][xIndex + 1] = true;
					}
					if (wkField.yokoWall[wkField.getYLength() - 1][xIndex] == Wall.EXISTS) {
						wkField.hoshi[wkField.getYLength()][xIndex + 1] = true;
					}
				}
				System.out.println(wkField);
				// マスを戻す。
				for (int yIndex = 1; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 1; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new BdblockSolverForGenerator(wkField, 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						BdblockSolver.Field virtual = new BdblockSolver.Field(wkField);
						int targetNum = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
						if (roomNumberCnt.get(targetNum) == 1) {
							continue;
						}
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new BdblockSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							roomNumberCnt.put(targetNum, roomNumberCnt.get(targetNum) - 1);
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
			System.out.println(wkField);
			level = (int) Math.sqrt(level * 6 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(星：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex != -1 && xIndex != wkField.getXLength() - 1
								&& wkField.getYokoWall()[yIndex][xIndex] == Wall.EXISTS) {
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
							|| wkField.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex != -1 && yIndex != wkField.getYLength() - 1
								&& wkField.getTateWall()[yIndex][xIndex] == Wall.EXISTS) {
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
			// 星描画
			for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
					if (wkField.getHoshi()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + (baseSize / 2 - 6)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
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
		static final String ALPHABET_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// 星の情報。trueなら星あり
		protected boolean[][] hoshi;
		// 数字情報
		protected final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public boolean[][] getHoshi() {
			return hoshi;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
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

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?nurikabe/" + getXLength() + "/" + getYLength() + "/");
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

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			hoshi = new boolean[height + 1][width + 1];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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

		public Field(int height, int width, String param, String hoshiParam) {
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			hoshi = new boolean[height + 1][width + 1];
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
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
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
			readPos = 0;
			index = 0;
			boolean nextOn = false;
			for (int i = readPos; i < hoshiParam.length(); i++) {
				if (nextOn) {
					Position pos = new Position(index / (getXLength() + 1), index % (getXLength() + 1));
					hoshi[pos.getyIndex()][pos.getxIndex()] = true;
					index++;
				}
				char ch = hoshiParam.charAt(i);
				int interval = ALPHABET_NUMBER.indexOf(ch);
				if (interval == -1) {
					index = index + 36;
					nextOn = false;
				} else {
					index = index + interval;
					nextOn = true;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				tateWall[yIndex][0] = hoshi[yIndex + 1][0] ? Wall.EXISTS : Wall.NOT_EXISTS;
				tateWall[yIndex][getXLength() - 1] = hoshi[yIndex + 1][getXLength()] ? Wall.EXISTS : Wall.NOT_EXISTS;
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				yokoWall[0][xIndex] = hoshi[0][xIndex + 1] ? Wall.EXISTS : Wall.NOT_EXISTS;
				yokoWall[getYLength() - 1][xIndex] = hoshi[getYLength()][xIndex + 1] ? Wall.EXISTS : Wall.NOT_EXISTS;
			}
		}

		public Field(Field other) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			hoshi = other.hoshi;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
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
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				if (xIndex % 2 == 0 && hoshi[0][xIndex / 2]) {
					sb.append("●");
				} else {
					sb.append("□");
				}
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("■");
						} else {
							String numStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(numStr);
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
					sb.append(hoshi[yIndex + 1][0] ? "●" : "□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
						if (xIndex != getXLength() - 1) {
							sb.append(hoshi[yIndex + 1][xIndex + 1] ? "●" : "□");
						}
					}
					sb.append(hoshi[yIndex + 1][getXLength()] ? "●" : "□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				if (xIndex % 2 == 0 && hoshi[getYLength()][xIndex / 2]) {
					sb.append("●");
				} else {
					sb.append("□");
				}
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
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!notStandAloneSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 点がある場所は壁が3か4,点がない場所は壁が0か2になる。矛盾したらfalse
		 */
		protected boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yokoWall[yIndex][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = tateWall[yIndex][xIndex + 1];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yokoWall[yIndex + 1][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = tateWall[yIndex][xIndex];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (hoshi[yIndex + 1][xIndex + 1]) {
						if (notExistsCount > 1) {
							return false;
						}
						if (notExistsCount == 1) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
					} else {
						if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
							return false;
						}
						if (existsCount == 2) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						} else if (existsCount == 1 && notExistsCount == 2) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
						} else if (existsCount == 0 && notExistsCount == 3) {
							if (wallUp == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
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

		/**
		 * 数字が0個だったり、別の数字が混ざったり、同じ数字を回収しきらない島ができてはならない。
		 */
		protected boolean notStandAloneSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					whitePosSet.add(new Position(yIndex, xIndex));
				}
			}
			Set<Position> candPosSet = new HashSet<>(whitePosSet);
			while (!whitePosSet.isEmpty()) {
				Position pivot = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				// 数字ダブり
				if (!setContinuePosSet(pivot, continuePosSet, null, new HashSet<>())) {
					return false;
				}
				whitePosSet.removeAll(continuePosSet);
			}
			while (!candPosSet.isEmpty()) {
				Position pivot = new ArrayList<>(candPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				Set<Integer> findNumber = new HashSet<>();
				setContinueCandPosSet(pivot, continuePosSet, null, findNumber);
				// 数字がない
				if (findNumber.isEmpty()) {
					return false;
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						// 同じ数字未回収
						if (!continuePosSet.contains(new Position(yIndex, xIndex))
								&& findNumber.contains(numbers[yIndex][xIndex])) {
							return false;
						}
					}
				}
				candPosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に伸びる可能性があるマスをつなげていく。見つけた数字をfindNumberに入れる。
		 */
		protected void setContinueCandPosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Set<Integer> findNumber) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				findNumber.add(numbers[pos.getyIndex()][pos.getxIndex()]);
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.EXISTS) {
					continuePosSet.add(nextPos);
					setContinueCandPosSet(nextPos, continuePosSet, Direction.DOWN, findNumber);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[nextPos.getyIndex()][nextPos.getxIndex() - 1] != Wall.EXISTS) {
					continuePosSet.add(nextPos);
					setContinueCandPosSet(nextPos, continuePosSet, Direction.LEFT, findNumber);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[nextPos.getyIndex() - 1][nextPos.getxIndex()] != Wall.EXISTS) {
					continuePosSet.add(nextPos);
					setContinueCandPosSet(nextPos, continuePosSet, Direction.UP, findNumber);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.EXISTS) {
					continuePosSet.add(nextPos);
					setContinueCandPosSet(nextPos, continuePosSet, Direction.RIGHT, findNumber);
				}
			}
		}

		/**
		 * posを起点に上下左右に必ず伸びるマスをつなげていく。 違う数字を見つけた時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Set<Integer> findNumber) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				if (findNumber.isEmpty()) {
					findNumber.add(numbers[pos.getyIndex()][pos.getxIndex()]);
				} else if (!findNumber.contains(numbers[pos.getyIndex()][pos.getxIndex()])) {
					return false;
				}
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[nextPos.getyIndex()][nextPos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[nextPos.getyIndex()][nextPos.getxIndex() - 1] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[nextPos.getyIndex() - 1][nextPos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[nextPos.getyIndex()][nextPos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, findNumber)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	protected final Field field;
	protected int count = 0;

	public BdblockSolver(int height, int width, String param, String hoshiParam) {
		field = new Field(height, width, param, hoshiParam);
	}

	public BdblockSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?bdblock/11/11/..../r123q5i6o1m2m1m4m9m7o8i8q124r"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 3]);
		int width = Integer.parseInt(params[params.length - 4]);
		String param = params[params.length - 1];
		String hoshiParam = params[params.length - 2];
		System.out.println(new BdblockSolver(height, width, param, hoshiParam).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
