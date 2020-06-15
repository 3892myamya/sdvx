package myamya.other.solver.starbattle;

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

public class StarBattleSolver implements Solver {

	public static class StarBattleGenerator implements Generator {

		static class ExtendedField extends StarBattleSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, int starCnt) {
				super(height, width, starCnt);
			}

			/**
			 * 生成時は部屋のチェックは不要
			 */
			public boolean roomSolve() {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					// 横方向調査
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (blackCnt + spaceCnt < super.starCnt) {
						// 黒マス不足
						return false;
					}
					// 置かねばならない黒マスの数
					int retainBlackCnt = super.starCnt - blackCnt;
					if (retainBlackCnt < 0) {
						// 黒マス超過
						return false;
					} else if (retainBlackCnt == 0) {
						// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					} else if (spaceCnt == retainBlackCnt) {
						// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}

				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 縦方向調査
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (blackCnt + spaceCnt < super.starCnt) {
						// 黒マス不足
						return false;
					}
					// 置かねばならない黒マスの数
					int retainBlackCnt = super.starCnt - blackCnt;
					if (retainBlackCnt < 0) {
						// 黒マス超過
						return false;
					} else if (retainBlackCnt == 0) {
						// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					} else if (spaceCnt == retainBlackCnt) {
						// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
				return true;
			}
		}

		static class StarBattleSolverForGenerator extends StarBattleSolver {
			private final int limit;

			public StarBattleSolverForGenerator(Field field, int limit) {
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
		protected final int starCnt;

		public StarBattleGenerator(int height, int width, int starCnt) {
			this.height = height;
			this.width = width;
			this.starCnt = starCnt;
		}

		public static void main(String[] args) {
			new StarBattleGenerator(9, 9, 2).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width, starCnt);
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
				// 星を配置
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
						wkField = new ExtendedField(height, width, starCnt);
					} else {
						break;
					}
				}
				List<Set<Position>> roomCandList = new ArrayList<>();
				List<Position> yetPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						yetPosList.add(new Position(yIndex, xIndex));
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							Set<Position> wkSet = new HashSet<>();
							wkSet.add(new Position(yIndex, xIndex));
							roomCandList.add(wkSet);
						}
					}
				}
				List<Set<Position>> rooms = new ArrayList<>();
				// 部屋割り
				if (starCnt == 2) {
					while (true) {
						// 基準部屋をランダムで選ぶ
						Set<Position> room = roomCandList.get((int) (Math.random() * roomCandList.size()));
						boolean bad = false;
						int continueCnt = 0;
						while (true) {
							// 基準マスをランダムで選ぶ
							Position targetPos = new ArrayList<Position>(room)
									.get((int) (Math.random() * room.size()));
							// 基準マスからランダムで1マス伸ばす
							List<Position> candPosList = new ArrayList<>();
							if (targetPos.getyIndex() != 0) {
								Position nextPos = new Position(targetPos.getyIndex() - 1, targetPos.getxIndex());
								if (yetPosList.contains(nextPos)) {
									candPosList.add(nextPos);
								}
							}
							if (targetPos.getxIndex() != wkField.getXLength() - 1) {
								Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() + 1);
								if (yetPosList.contains(nextPos)) {
									candPosList.add(nextPos);
								}
							}
							if (targetPos.getyIndex() != wkField.getYLength() - 1) {
								Position nextPos = new Position(targetPos.getyIndex() + 1, targetPos.getxIndex());
								if (yetPosList.contains(nextPos)) {
									candPosList.add(nextPos);
								}
							}
							if (targetPos.getxIndex() != 0) {
								Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() - 1);
								if (yetPosList.contains(nextPos)) {
									candPosList.add(nextPos);
								}
							}
							// どの方向にも伸ばせなかったら基準マス選び直し。
							if (candPosList.isEmpty()) {
								continueCnt++;
								if (continueCnt == 10) {
									// 伸ばせないが10回続いたらあきらめる
									bad = true;
									break;
								} else {
									continue;
								}
							}
							Position nextPos = candPosList.get((int) (Math.random() * candPosList.size()));
							// 他の部屋との連結判定
							Set<Position> otherRoom = null;
							for (Set<Position> otherRoomCand : roomCandList) {
								if (room != otherRoomCand) {
									if (otherRoomCand.contains(nextPos)) {
										otherRoom = otherRoomCand;
										break;
									}
								}
							}
							if (otherRoom != null) {
								// 部屋連結処理
								room.addAll(otherRoom);
								rooms.add(room);
								roomCandList.remove(room);
								roomCandList.remove(otherRoom);
								yetPosList.removeAll(room);
								break;
							} else {
								// 単に自分の部屋を伸ばす。
								room.add(nextPos);
								yetPosList.remove(nextPos);
							}
						}
						if (bad) {
							// 諦めた場合は最初からやりなおす
							rooms.clear();
							roomCandList.clear();
							yetPosList.clear();
							for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
								for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
									yetPosList.add(new Position(yIndex, xIndex));
									if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
										Set<Position> wkSet = new HashSet<>();
										wkSet.add(new Position(yIndex, xIndex));
										roomCandList.add(wkSet);
									}
								}
							}
						} else if (rooms.size() == wkField.getYLength()) {
							break;
						}
					}
				} else {
					for (Set<Position> roomCand : roomCandList) {
						yetPosList.removeAll(roomCand);
					}
					rooms = roomCandList;
				}
				// ここまで来たら部屋割りはうまくいっている
				// 余ったマスを適当に部屋につなげる。
				while (true) {
					if (yetPosList.isEmpty()) {
						break;
					}
					// 基準部屋をランダムで選ぶ
					Set<Position> room = rooms.get((int) (Math.random() * rooms.size()));
					// 基準マスをランダムで選ぶ
					Position targetPos = new ArrayList<Position>(room).get((int) (Math.random() * room.size()));
					// 基準マスからランダムで1マス伸ばす
					List<Position> candPosList = new ArrayList<>();
					if (targetPos.getyIndex() != 0) {
						Position nextPos = new Position(targetPos.getyIndex() - 1, targetPos.getxIndex());
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getxIndex() != wkField.getXLength() - 1) {
						Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() + 1);
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getyIndex() != wkField.getYLength() - 1) {
						Position nextPos = new Position(targetPos.getyIndex() + 1, targetPos.getxIndex());
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getxIndex() != 0) {
						Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() - 1);
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					// どの方向にも伸ばせなかったら基準マス選び直し。
					if (candPosList.isEmpty()) {
						continue;
					}
					Position nextPos = candPosList.get((int) (Math.random() * candPosList.size()));
					room.add(nextPos);
					yetPosList.remove(nextPos);
				}
				wkField.rooms = rooms;
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new StarBattleSolverForGenerator(wkField, 10000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, starCnt);
				} else {
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
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin)
							+ "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			sb.append("<text y=\"" + (-1 * baseSize + baseSize + margin - 5)
					+ "\" x=\""
					+ ((wkField.getXLength() - 1) * baseSize + baseSize)
					+ "\" font-size=\""
					+ (baseSize - 4)
					+ "\" fill=\""
					+ "black"
					+ "\" lengthAdjust=\"spacingAndGlyphs\">"
					+ wkField.starCnt + "★"
					+ "</text>");
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		protected Masu[][] masu;
		// 星の数
		protected final int starCnt;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		protected List<Set<Position>> rooms;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?starbattle/" + getXLength() + "/" + getYLength() + "/" + starCnt + "/");
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
			return sb.toString();
		}

		public String getHintCount() {
			return "";
		}

		public Masu[][] getMasu() {
			return masu;
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

		public Field(int height, int width, int starCnt, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			this.starCnt = starCnt;
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
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			starCnt = other.starCnt;
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		/**
		 * プレーンなフィールド生成
		 */
		public Field(int height, int width, int starCnt) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			this.starCnt = starCnt;
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			rooms = new ArrayList<>();
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
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
					sb.append(masu[yIndex][xIndex]);
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "＊");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "＊");
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
			return sb.toString();
		}

		/**
		 * 縦・横・部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				// 横方向調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < starCnt) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = starCnt - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}

			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				// 縦方向調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < starCnt) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = starCnt - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}

			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < starCnt) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = starCnt - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 星の周りを白マスに確定する。
		 */
		public boolean roundSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRightDown = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDownLeft = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK || masuUpRight == Masu.BLACK || masuRightDown == Masu.BLACK
								|| masuDownLeft == Masu.BLACK || masuLeftUp == Masu.BLACK) {
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
						if (masuUpRight == Masu.SPACE) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuRightDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDownLeft == Masu.SPACE) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLeftUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!roundSolve()) {
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
				}
			}
			return true;
		}

	}

	protected final Field field;
	protected int count = 0;

	public StarBattleSolver(int height, int width, int starCnt, String param) {
		field = new Field(height, width, starCnt, param);
	}

	public StarBattleSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 3]);
		int width = Integer.parseInt(params[params.length - 4]);
		int starCnt = Integer.parseInt(params[params.length - 2]);
		String param = params[params.length - 1];
		System.out.println(new StarBattleSolver(height, width, starCnt, param).solve());
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
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
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
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			count++;
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
			}
		}
		return true;
	}

}
