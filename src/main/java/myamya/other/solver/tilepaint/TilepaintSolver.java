package myamya.other.solver.tilepaint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class TilepaintSolver implements Solver {
	public static class TilepaintGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ExtendedField extends TilepaintSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, List<Set<Position>> rooms) {
				super(height, width, rooms);
			}

			/**
			 * 生成時は部屋のチェックは不要
			 */
			public boolean groupSolve() {
				return true;
			}
		}

		static class TilepaintSolverForGenerator extends TilepaintSolver {
			private final int limit;

			public TilepaintSolverForGenerator(Field field, int limit) {
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

		public TilepaintGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TilepaintGenerator(20, 20).generate();
		}

		@Override
		public GeneratorResult generate() {
			TilepaintSolver.Field wkField = new ExtendedField(height, width,
					RoomMaker.roomMake(height, width, -1, (int) (Math.sqrt(height) * 2)));
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
				// 数字を配置
				while (true) {
					boolean isOk = false;
					boolean black = false;
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
								TilepaintSolver.Field virtual = new ExtendedField(wkField);
								if (masuNum < 1) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								} else if (masuNum < 2) {
									black = true;
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
					if (!isOk || !black) {
						// 破綻したら0から作り直す。また全白マス問題は出ないようにする
						wkField = new ExtendedField(height, width,
								RoomMaker.roomMake(height, width, -1, (int) (Math.sqrt(height) * 2)));
					} else {
						break;
					}
				}
				// ブロックの数字確定
				for (Group group : wkField.groups) {
					int cnt = 0;
					for (Position pos : group.member) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							cnt++;
						}
					}
					group.cnt = cnt;
					boolean vartical = group.member.get(0).getyIndex() != group.member.get(1).getyIndex();
					if (vartical) {
						wkField.blocks.get(new Position(group.member.get(0).getyIndex() - 1,
								group.member.get(0).getxIndex())).downCnt = cnt;
					} else {
						wkField.blocks.get(new Position(group.member.get(0).getyIndex(),
								group.member.get(0).getxIndex() - 1)).leftCnt = cnt;
					}
				}
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (!wkField.blocks.containsKey(new Position(yIndex, xIndex))) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new TilepaintSolverForGenerator(wkField, 20000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width,
							RoomMaker.roomMake(height, width, -1, (int) (Math.sqrt(height) * 2)));
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
			level = (int) Math.sqrt(level * 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/空中ヒント：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 3 * baseSize) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 3 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex - 1, xIndex - 1);
					myamya.other.solver.tilepaint.TilepaintSolver.Block block = wkField.getBlocks().get(pos);
					if (block != null || yIndex == 0 || xIndex == 0) {
						sb.append("<path d=\"M "
								+ (xIndex * baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + margin)
								+ " L"
								+ (xIndex * baseSize + baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + margin)
								+ " L"
								+ (xIndex * baseSize + baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + baseSize + margin)
								+ " Z\" >"
								+ "</path>");
						if (block != null && pos.getxIndex() < wkField.getXLength() - 1 && (block.getLeftCnt() != 0
								|| !wkField.getBlocks()
										.containsKey(new Position(pos.getyIndex(), pos.getxIndex() + 1)))) {
							String masuStr;
							String numberStr = String.valueOf(block.getLeftCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2))
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize + 2) / 2
									+ "\" textLength=\""
									+ (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr
									+ "</text>");
						}
						sb.append("<path d=\"M "
								+ (xIndex * baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + margin)
								+ " L"
								+ (xIndex * baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + baseSize + margin)
								+ " L"
								+ (xIndex * baseSize + baseSize + baseSize)
								+ " "
								+ (yIndex * baseSize + baseSize + margin)
								+ " Z\" >"
								+ "</path>");
						if (block != null && pos.getyIndex() < wkField.getYLength() - 1 && (block.getDownCnt() != 0
								|| !wkField.getBlocks()
										.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex())))) {
							String masuStr;
							String numberStr = String.valueOf(block.getDownCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize + 2) / 2
									+ "\" textLength=\""
									+ (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr
									+ "</text>");

						}
					}
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength() + 1; xIndex++) {
					boolean oneYokoWall = yIndex <= 0 || xIndex <= 0 || yIndex >= wkField.getYLength() + 1
							|| xIndex >= wkField.getXLength()
							|| wkField.getYokoWall()[yIndex - 1][xIndex - 1];
					if (oneYokoWall) {
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
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
					boolean oneTateWall = yIndex <= 0 || xIndex <= 0 || yIndex >= wkField.getYLength()
							|| xIndex >= wkField.getXLength() + 1
							|| wkField.getTateWall()[yIndex - 1][xIndex - 1];
					if (oneTateWall) {
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
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Block {

		@Override
		public String toString() {
			return "[" + downCnt + "," + leftCnt + "]";
		}

		private int downCnt;
		private int leftCnt;

		public Block(int downCnt, int leftCnt) {
			this.downCnt = downCnt;
			this.leftCnt = leftCnt;
		}

		public int getDownCnt() {
			return downCnt;
		}

		public int getLeftCnt() {
			return leftCnt;
		}

	}

	public static class Group {
		@Override
		public String toString() {
			return "Group [cnt=" + cnt + ", member=" + member + "]";
		}

		// 黒マスが何マスあるか。数字がない場合は-1
		private int cnt;
		// 部屋に属するマスの集合
		private final List<Position> member;

		public Group(int cnt, List<Position> member) {
			this.cnt = cnt;
			this.member = member;
		}

		public int getCnt() {
			return cnt;
		}

		public List<Position> getMember() {
			return member;
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;
		// ブロック情報
		protected Map<Position, Block> blocks;
		// グループ情報
		protected List<Group> groups;

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?tilepaint/" + getXLength() + "/" + getYLength() + "/");
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
				if (!blocks.containsKey(new Position(yIndex, xIndex))) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = blocks.get(new Position(yIndex, xIndex)).getDownCnt();
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
					num = blocks.get(new Position(yIndex, xIndex)).getLeftCnt();
					numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
					}
					sb.append(numStr);
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				Integer num = blocks.get(new Position(-1, xIndex)).getDownCnt();
				String numStr = Integer.toHexString(num);
				if (numStr.length() == 2) {
					numStr = "-" + numStr;
				} else if (numStr.length() == 3) {
					numStr = "+" + numStr;
				}
				sb.append(numStr);
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				Integer num = blocks.get(new Position(yIndex, -1)).getLeftCnt();
				String numStr = Integer.toHexString(num);
				if (numStr.length() == 2) {
					numStr = "-" + numStr;
				} else if (numStr.length() == 3) {
					numStr = "+" + numStr;
				}
				sb.append(numStr);
			}
			return sb.toString();
		}

		public String getHintCount() {
			int numCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blocks.containsKey(new Position(yIndex, xIndex))) {
						numCnt++;
					}
				}
			}
			return rooms.size() + "/" + numCnt;
		}

		public Map<Position, Block> getBlocks() {
			return blocks;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			blocks = new HashMap<>();
			int readPos = 0;
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
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
				if (index / getXLength() >= getYLength()) {
					readPos = i;
					break;
				}
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						blocks.put(pos, new Block(0, 0));
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					} else {
						int downCnt;
						if (ch == '-') {
							downCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2),
									16);
							i++;
							i++;
						} else if (ch == '+') {
							downCnt = Integer.parseInt(
									"" +
											param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos
													+ 3),
									16);
							i++;
							i++;
							i++;
						} else {
							downCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
						i++;
						ch = param.charAt(i);
						int rightCnt;
						if (ch == '-') {
							rightCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2),
									16);
							i++;
							i++;
						} else if (ch == '+') {
							rightCnt = Integer.parseInt(
									"" +
											param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos
													+ 3),
									16);
							i++;
							i++;
							i++;
						} else {
							rightCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
						blocks.put(pos, new Block(downCnt, rightCnt));
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					}
					index++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				char ch = param.charAt(readPos);
				int downCnt;
				if (ch == '-') {
					downCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2),
							16);
					readPos++;
					readPos++;
				} else if (ch == '+') {
					downCnt = Integer.parseInt(
							"" +
									param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos
											+ 3),
							16);
					readPos++;
					readPos++;
					readPos++;
				} else {
					downCnt = Integer.parseInt(String.valueOf(ch), 16);
				}
				blocks.put(new Position(-1, xIndex), new Block(downCnt, 0));
				readPos++;
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				char ch = param.charAt(readPos);
				int rightCnt;
				if (ch == '-') {
					rightCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2),
							16);
					readPos++;
					readPos++;
				} else if (ch == '+') {
					rightCnt = Integer.parseInt(
							"" +
									param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos
											+ 3),
							16);
					readPos++;
					readPos++;
					readPos++;
				} else {
					rightCnt = Integer.parseInt(String.valueOf(ch), 16);
				}
				blocks.put(new Position(yIndex, -1), new Block(0, rightCnt));
				readPos++;
			}
			// グループ確定
			groups = new ArrayList<>();
			// 横方向
			for (int yIndex = -1; yIndex < getYLength(); yIndex++) {
				List<Position> groupPosList = new ArrayList<>();
				int useCnt = 0;
				for (int xIndex = -1; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					Block block = blocks.get(pos);
					if (block != null) {
						if (!groupPosList.isEmpty()) {
							groups.add(new Group(useCnt, groupPosList));
							groupPosList = new ArrayList<>();
						}
						useCnt = block.getLeftCnt();
					} else {
						if (yIndex == -1 || xIndex == -1) {
							//
						} else {
							groupPosList.add(pos);
						}
					}
				}
				if (!groupPosList.isEmpty()) {
					groups.add(new Group(useCnt, groupPosList));
				}
			}
			// 縦方向
			for (int xIndex = -1; xIndex < getXLength(); xIndex++) {
				List<Position> groupPosList = new ArrayList<>();
				int useCnt = 0;
				for (int yIndex = -1; yIndex < getYLength(); yIndex++) {
					Position pos = new Position(yIndex, xIndex);
					Block block = blocks.get(pos);
					if (block != null) {
						if (!groupPosList.isEmpty()) {
							groups.add(new Group(useCnt, groupPosList));
							groupPosList = new ArrayList<>();
						}
						useCnt = block.getDownCnt();
					} else {
						if (yIndex == -1 || xIndex == -1) {
							//
						} else {
							groupPosList.add(pos);
						}
					}
				}
				if (!groupPosList.isEmpty()) {
					groups.add(new Group(useCnt, groupPosList));
				}
			}
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

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
			blocks = other.blocks;
			groups = other.groups;
		}

		/**
		 * プレーンフィールド
		 */
		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			while (true) {
				blocks = new HashMap<>();
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (Math.random() < 0.005) {
							// 1/200の確率でブロック生成
							blocks.put(new Position(yIndex, xIndex), new Block(0, 0));
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					blocks.put(new Position(-1, xIndex), new Block(0, 0));
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					blocks.put(new Position(yIndex, -1), new Block(0, 0));
				}
				groups = new ArrayList<>();
				// 横方向
				for (int yIndex = -1; yIndex < getYLength(); yIndex++) {
					List<Position> groupPosList = new ArrayList<>();
					int useCnt = 0;
					for (int xIndex = -1; xIndex < getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						Block block = blocks.get(pos);
						if (block != null) {
							if (!groupPosList.isEmpty()) {
								groups.add(new Group(useCnt, groupPosList));
								groupPosList = new ArrayList<>();
							}
							useCnt = block.getLeftCnt();
						} else {
							if (yIndex == -1 || xIndex == -1) {
								//
							} else {
								groupPosList.add(pos);
							}
						}
					}
					if (!groupPosList.isEmpty()) {
						groups.add(new Group(useCnt, groupPosList));
					}
				}
				// 縦方向
				for (int xIndex = -1; xIndex < getXLength(); xIndex++) {
					List<Position> groupPosList = new ArrayList<>();
					int useCnt = 0;
					for (int yIndex = -1; yIndex < getYLength(); yIndex++) {
						Position pos = new Position(yIndex, xIndex);
						Block block = blocks.get(pos);
						if (block != null) {
							if (!groupPosList.isEmpty()) {
								groups.add(new Group(useCnt, groupPosList));
								groupPosList = new ArrayList<>();
							}
							useCnt = block.getDownCnt();
						} else {
							if (yIndex == -1 || xIndex == -1) {
								//
							} else {
								groupPosList.add(pos);
							}
						}
					}
					if (!groupPosList.isEmpty()) {
						groups.add(new Group(useCnt, groupPosList));
					}
				}
				boolean isOk = true;
				for (Group group : groups) {
					if (group.member.size() < 2) {
						// 1マスのみのグループはできなくする。
						isOk = false;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
			// ブロックを1マスの部屋として独立する
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				outer: for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (blocks.containsKey(pos)) {
						for (Set<Position> room : rooms) {
							if (room.contains(pos)) {
								if (room.size() != 1) {
									room.remove(pos);
								} else {
									continue outer;
								}
								break;
							}
						}
						Set<Position> newRoom = new HashSet<>();
						newRoom.add(pos);
						rooms.add(newRoom);
					}
				}
			}
			this.rooms = new ArrayList<>();
			// 部屋の探索順序を合わせないとレベル表記がずれるので、
			// 回りくどいが、左上の部屋から加えるようにする。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (Set<Position> room : rooms) {
						Set<Position> target = new HashSet<>(room);
						if (room.contains(new Position(yIndex, xIndex)) &&
								!this.rooms.contains(target)) {
							this.rooms.add(target);
						}
					}
				}
			}
		}

		private static final String NUMBER_TO_ALPHABETS = "-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = -1; yIndex < getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					Block block = blocks.get(pos);
					if (block != null) {
						sb.append(NUMBER_TO_ALPHABETS.charAt(block.getDownCnt()));
						sb.append(NUMBER_TO_ALPHABETS.charAt(block.getLeftCnt()));
					} else {
						if (yIndex == -1 || xIndex == -1) {
							sb.append("■");
						} else {
							sb.append(masu[yIndex][xIndex]);
						}
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
		 * 同じ部屋のマスは同じ色になる。
		 * 混在している場合falseを返す。
		 */
		private boolean roomSolve() {
			for (Set<Position> room : rooms) {
				boolean blackExist = false;
				boolean notBlackExist = false;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						if (notBlackExist) {
							return false;
						}
						blackExist = true;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						if (blackExist) {
							return false;
						}
						notBlackExist = true;
					}
				}
				for (Position pos : room) {
					if (notBlackExist) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					}
					if (blackExist) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
					}
				}
			}
			return true;
		}

		public boolean groupSolve() {
			for (Group group : groups) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : group.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < group.getCnt()) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = group.getCnt() - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : group.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : group.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * チェックを実行。
		 * 数字の合計を満たす組み合わせがない場合falseを返す。
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!groupSolve()) {
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
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public TilepaintSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TilepaintSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?tilepaint/10/10/r5vpmn6qtvjdnarih9dkibf6vp5757vsdkrvzr44zzzm77175568868881747485"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new TilepaintSolver(height, width, param).solve());
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
		System.out.println("難易度:" + count * 5);
		System.out.println(field);
		int level = (int) Math.sqrt(count * 5 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Set<Position> room : field.rooms) {
			// マスは各部屋1マスずつ調べればよい。
			Position pos = new ArrayList<>(room).get(0);
			if (field.masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
				count++;
				if (!oneCandSolve(field, pos.getyIndex(), pos.getxIndex(), recursive)) {
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
		return true;
	}
}