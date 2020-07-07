package myamya.other.solver.usoone;

import java.util.ArrayList;
import java.util.Arrays;
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
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class UsooneSolver implements Solver {
	public static class UsooneGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ExtendedField extends UsooneSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, List<Set<Position>> rooms) {
				super(height, width, rooms);
			}

			/**
			 * 生成時は部屋の嘘つき人数チェックは不要
			 */
			public boolean roomSolve() {
				return true;
			}
		}

		static class UsooneSolverForGenerator extends UsooneSolver {
			private final int limit;

			public UsooneSolverForGenerator(Field field, int limit) {
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

		public UsooneGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new UsooneGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			UsooneSolver.Field wkField = new ExtendedField(height, width,
					RoomMaker.roomMake(height, width, 2, -1));
			List<Position> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					indexList.add(new Position(yIndex, xIndex));
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				// 白マス黒マスを配置
				while (true) {
					boolean isOk = false;
					Collections.shuffle(indexList);
					for (Position pos : indexList) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							isOk = false;
							List<Integer> numIdxList = new ArrayList<>();
							for (int i = 0; i < 2; i++) {
								numIdxList.add(i);
							}
							Collections.shuffle(numIdxList);
							for (int masuNum : numIdxList) {
								ExtendedField virtual = new ExtendedField(wkField);
								if (masuNum < 1) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								} else if (masuNum < 2) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
								}
								if (virtual.solveAndCheck()) {
									isOk = true;
									wkField.masu = virtual.masu;
									break;
								}
							}
							if (!isOk) {
								break;
							}
						}
					}
					if (!isOk) {
						// 破綻したら0から作り直す。
						wkField = new ExtendedField(height, width,
								RoomMaker.roomMake(height, width, 2, -1));
					} else {
						isOk = false;
						for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
								if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
									isOk = true;
									break;
								}
							}
						}
						if (!isOk) {
							// 全白マス問題は出ないようにする
							wkField = new ExtendedField(height, width,
									RoomMaker.roomMake(height, width, 2, -1));
						} else {
							break;
						}
					}
				}
				// 各部屋にランダムで1マスずつ嘘を配置する。
				boolean isOk = true;
				for (Set<Position> room : wkField.rooms) {
					List<Position> roomsPos = new ArrayList<>(room);
					Collections.shuffle(roomsPos);
					boolean usoone = false;
					for (Position pos : roomsPos) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
							wkField.usos[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							usoone = true;
							break;
						}
					}
					if (!usoone) {
						// 部屋に嘘を配置できなかった
						isOk = false;
						break;
					}
				}
				if (!isOk) {
					// 嘘つきのいない部屋ができたらやり直し
					wkField = new ExtendedField(height, width,
							RoomMaker.roomMake(height, width, 2, -1));
					continue;
				}
				// 数字配置
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							int blackCnt = 0;
							Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex - 1][xIndex];
							Masu masuRight = xIndex == wkField.getXLength() - 1 ? Masu.NOT_BLACK
									: wkField.masu[yIndex][xIndex + 1];
							Masu masuDown = yIndex == wkField.getYLength() - 1 ? Masu.NOT_BLACK
									: wkField.masu[yIndex + 1][xIndex];
							Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex][xIndex - 1];
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
							if (wkField.usos[yIndex][xIndex] == Masu.NOT_BLACK) {
								wkField.numbers[yIndex][xIndex] = blackCnt;
							} else {
								int wallCnt = 0;
								if (yIndex == 0) {
									wallCnt++;
								}
								if (xIndex == wkField.getXLength() - 1) {
									wallCnt++;
								}
								if (yIndex == wkField.getYLength() - 1) {
									wallCnt++;
								}
								if (xIndex == 0) {
									wallCnt++;
								}
								List<Integer> numbers = new ArrayList<>(Arrays.asList(new Integer[] { 0, 1, 2, 3, 4 }));
								numbers.remove(new Integer(blackCnt));
								if (wallCnt >= 1 || Math.random() < 0.9) {
									// 4は嘘なのがバレバレなので高確率で消す
									numbers.remove(new Integer(4));
								}
								if (wallCnt == 2 || (wallCnt == 1 && Math.random() < 0.9)) {
									// 壁際の3とか
									numbers.remove(new Integer(3));
								}
								if (wallCnt == 2 && Math.random() < 0.9) {
									// 角の2とかも高確率で消す
									numbers.remove(new Integer(2));
								}
								wkField.numbers[yIndex][xIndex] = numbers.get((int) (Math.random() * numbers.size()));
							}
						}
					}
				}
				List<Position> fixedMasuList = new ArrayList<>();
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						} else {
							// 白マス = 数字マス
							if (wkField.usos[yIndex][xIndex] == Masu.NOT_BLACK) {
								fixedMasuList.add(new Position(yIndex, xIndex));
							}
							wkField.usos[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new UsooneSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width,
							RoomMaker.roomMake(height, width, 2, -1));
				} else {
					Collections.shuffle(fixedMasuList);
					for (Position pos : fixedMasuList) {
						UsooneSolver.Field virtual = new UsooneSolver.Field(wkField, true);
						virtual.numbers[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						virtual.usos[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						int solveResult = new UsooneSolverForGenerator(virtual, 2000).solve2();
						if (solveResult != -1 && level <= solveResult) {
							// ウソワンでは数字を消すと嘘が見破りやすくなり簡単になる場合があるので、
							// 前よりも難しくなったと判断した時だけ数字を消すようにする
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
							wkField.usos[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							level = solveResult;
						}
					}
					break;
				}
			}
			// 横壁設定
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : wkField.rooms) {
						if (room.contains(pos)) {
							Position rightPos = new Position(yIndex, xIndex + 1);
							if (room.contains(rightPos)) {
								isWall = false;
								break;
							}
						}
					}
					wkField.yokoWall[yIndex][xIndex] = isWall;
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : wkField.rooms) {
						if (room.contains(pos)) {
							Position downPos = new Position(yIndex + 1, xIndex);
							if (room.contains(downPos)) {
								isWall = false;
								break;
							}
						}
					}
					wkField.tateWall[yIndex][xIndex] = isWall;
				}
			}
			level = (int) Math.sqrt(level * 8 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
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
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex];
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
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateWall()[yIndex][xIndex];
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 1;
		// 固定数字
		private final Integer[][] numbers;
		// マスの情報
		protected Masu[][] masu;
		// 嘘つきかどうか。
		protected Masu[][] usos;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?usoone/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
				int yIndex1 = i / (getXLength() - 1);
				int xIndex1 = i % (getXLength() - 1);
				i++;
				int yIndex2 = -1;
				int xIndex2 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex2 = i / (getXLength() - 1);
					xIndex2 = i % (getXLength() - 1);
				}
				i++;
				int yIndex3 = -1;
				int xIndex3 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex3 = i / (getXLength() - 1);
					xIndex3 = i % (getXLength() - 1);
				}
				i++;
				int yIndex4 = -1;
				int xIndex4 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex4 = i / (getXLength() - 1);
					xIndex4 = i % (getXLength() - 1);
				}
				i++;
				int yIndex5 = -1;
				int xIndex5 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex5 = i / (getXLength() - 1);
					xIndex5 = i % (getXLength() - 1);
				}
				int num = 0;
				if (yIndex1 != -1 && xIndex1 != -1 && yokoWall[yIndex1][xIndex1]) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && yokoWall[yIndex2][xIndex2]) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && yokoWall[yIndex3][xIndex3]) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && yokoWall[yIndex4][xIndex4]) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && yokoWall[yIndex5][xIndex5]) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
			for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = -1;
				int xIndex2 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex2 = i / getXLength();
					xIndex2 = i % getXLength();
				}
				i++;
				int yIndex3 = -1;
				int xIndex3 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex3 = i / getXLength();
					xIndex3 = i % getXLength();
				}
				i++;
				int yIndex4 = -1;
				int xIndex4 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex4 = i / getXLength();
					xIndex4 = i % getXLength();
				}
				i++;
				int yIndex5 = -1;
				int xIndex5 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex5 = i / getXLength();
					xIndex5 = i % getXLength();
				}
				int num = 0;
				if (yIndex1 != -1 && xIndex1 != -1 && tateWall[yIndex1][xIndex1]) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && tateWall[yIndex2][xIndex2]) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && tateWall[yIndex3][xIndex3]) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && tateWall[yIndex4][xIndex4]) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && tateWall[yIndex5][xIndex5]) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
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
					String numStr = null;
					if (num == -1) {
						numStr = ".";
					} else {
						Integer numP1 = null;
						if (i + 1 < getYLength() * getXLength()) {
							int yIndexP1 = (i + 1) / getXLength();
							int xIndexP1 = (i + 1) % getXLength();
							numP1 = numbers[yIndexP1][xIndexP1];
						}
						Integer numP2 = null;
						if (numP1 == null && i + 2 < getYLength() * getXLength()) {
							int yIndexP2 = (i + 2) / getXLength();
							int xIndexP2 = (i + 2) % getXLength();
							numP2 = numbers[yIndexP2][xIndexP2];
						}
						if (numP1 == null && numP2 == null) {
							if (num == 0) {
								numStr = "a";
							} else if (num == 1) {
								numStr = "b";
							} else if (num == 2) {
								numStr = "c";
							} else if (num == 3) {
								numStr = "d";
							} else if (num == 4) {
								numStr = "e";
							}
							i++;
							i++;
						} else if (numP1 == null) {
							if (num == 0) {
								numStr = "5";
							} else if (num == 1) {
								numStr = "6";
							} else if (num == 2) {
								numStr = "7";
							} else if (num == 3) {
								numStr = "8";
							} else if (num == 4) {
								numStr = "9";
							}
							i++;
						} else {
							numStr = String.valueOf(num);
						}
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
			int numCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numCnt++;
					}
				}
			}
			return rooms.size() + "/" + numCnt;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Masu[][] getUsos() {
			return usos;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			masu = new Masu[height][width];
			usos = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					usos[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
								% 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						rooms.add(continuePosSet);
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						usos[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			usos = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					usos[yIndex][xIndex] = other.usos[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		/**
		 * ジェネレータ用。プレーンなフィールド作る
		 */
		public Field(int height, int width, List<Set<Position>> rooms) {
			numbers = new Integer[height][width];
			masu = new Masu[height][width];
			usos = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					usos[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			this.rooms = rooms;
		}

		/**
		 * ジェネレータ用
		 */
		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			masu = new Masu[other.getYLength()][other.getXLength()];
			usos = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					usos[yIndex][xIndex] = other.usos[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = new ArrayList<>(other.rooms);
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
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
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						sb.append(usos[yIndex][xIndex] == Masu.BLACK ? "f"
								: usos[yIndex][xIndex] == Masu.NOT_BLACK ? "t" : "?");
						sb.append(numbers[yIndex][xIndex]);
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "　");
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
					sb.append(usos[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋の嘘つきは1人のみ。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < BLACK_CNT) {
					// 嘘つき不足
					return false;
				}
				// 置かねばならない嘘つきの数
				int retainBlackCnt = BLACK_CNT - blackCnt;
				if (retainBlackCnt < 0) {
					// 嘘つき超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 嘘つき数が既に部屋の嘘つき数に等しければ、部屋の他のマスは正直
					for (Position pos : room) {
						if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							usos[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない嘘つきの数に等しければ、未確定マスは嘘つき
					for (Position pos : room) {
						if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							usos[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 嘘つきでない場合、自分の周りにある黒マスの数は数字と同じになる。
		 * 逆に、嘘つきの場合自分の周りの黒ますは数字と違う数になる。
		 * 矛盾はfalseを返す。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
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
						if (usos[yIndex][xIndex] == Masu.SPACE) {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス過剰。嘘つき
								usos[yIndex][xIndex] = Masu.BLACK;
							}
							if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
								// 黒マス不足。嘘つき
								usos[yIndex][xIndex] = Masu.BLACK;
							}
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt) {
								// 正直者
								usos[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (usos[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス過剰
								return false;
							}
							if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
								// 黒マス不足
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt) {
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
							if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
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
						} else if (usos[yIndex][xIndex] == Masu.BLACK) {
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt) {
								// 正直者
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt - 1) {
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
							if (numbers[yIndex][xIndex] == blackCnt - 1 && numbers[yIndex][xIndex] == 4 - whiteCnt) {
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
				}
			}
			return true;
		}

		/**
		 * 黒マス隣接セルを白マスにする。
		 * 黒マス隣接セルが黒マスの場合falseを返す。
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
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!numberSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
					if (usos[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public UsooneSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public UsooneSolver(Field field) {
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
		System.out.println(new UsooneSolver(height, width, param).solve());
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
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 8));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 8 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 8).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
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
				if (field.usos[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneUsoCandSolve(field, yIndex, xIndex, recursive)) {
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
			field.usos = virtual2.usos;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.usos = virtual.usos;
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneUsoCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.usos[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.usos[yIndex][xIndex] = Masu.NOT_BLACK;
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
			field.usos = virtual2.usos;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.usos = virtual.usos;
		}
		return true;
	}
}