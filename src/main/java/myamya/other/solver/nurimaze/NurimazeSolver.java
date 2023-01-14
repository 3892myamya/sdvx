package myamya.other.solver.nurimaze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class NurimazeSolver implements Solver {
	public static class NurimazeGenerator implements Generator {

		static class NurimazeSolverForGenerator extends NurimazeSolver {
			private final int limit;

			public NurimazeSolverForGenerator(Field field, int limit) {
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

		public NurimazeGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NurimazeGenerator(10, 10).generate();
		}

		// ある地点から最も遠い地点を調べる
		private Map<Integer, List<Position>> getDistanceMap(Field wkField, Position firstPos) {
			Map<Integer, List<Position>> distanceMap = new TreeMap<>(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o2 - o1;
				}
			});
			Set<Position> resolvePosSet = new HashSet<>();
			List<Position> nextPosList = new ArrayList<>();
			int distance = 0;
			nextPosList.add(firstPos);
			distanceMap.put(distance, nextPosList);
			resolvePosSet.addAll(nextPosList);
			while (true) {
				nextPosList = new ArrayList<>();
				List<Position> nowPosList = distanceMap.get(distance);
				for (Position nowPos : nowPosList) {
					if (nowPos.getyIndex() != 0) {
						Position candPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
						if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
								&& !resolvePosSet.contains(candPos)) {
							nextPosList.add(candPos);
							resolvePosSet.add(candPos);
						}
					}
					if (nowPos.getxIndex() != wkField.getXLength() - 1) {
						Position candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
						if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
								&& !resolvePosSet.contains(candPos)) {
							nextPosList.add(candPos);
							resolvePosSet.add(candPos);
						}
					}
					if (nowPos.getyIndex() != wkField.getYLength() - 1) {
						Position candPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
						if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
								&& !resolvePosSet.contains(candPos)) {
							nextPosList.add(candPos);
							resolvePosSet.add(candPos);
						}
					}
					if (nowPos.getxIndex() != 0) {
						Position candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
						if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
								&& !resolvePosSet.contains(candPos)) {
							nextPosList.add(candPos);
							resolvePosSet.add(candPos);
						}
					}
				}
				if (nextPosList.isEmpty()) {
					break;
				} else {
					distanceMap.put(++distance, nextPosList);
				}
			}
			return distanceMap;
		}

		// スタートからゴールまでの経路にある位置情報リストを返す。
		private List<Position> getRoute(Field wkField, Position startPos, Position goalPos) {
			List<Position> result = new ArrayList<>();
			result.add(startPos);
			Position nowPos = startPos;
			getRouteReq(result, wkField, nowPos, goalPos);
			return result;
		}

		private boolean getRouteReq(List<Position> result, Field wkField, Position nowPos, Position goalPos) {
			if (nowPos.equals(goalPos)) {
				return true;
			}
			if (nowPos.getyIndex() != 0) {
				Position candPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
				if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
						&& !result.contains(candPos)) {
					result.add(candPos);
					if (getRouteReq(result, wkField, candPos, goalPos)) {
						return true;
					} else {
						result.remove(candPos);
					}
				}
			}
			if (nowPos.getxIndex() != wkField.getXLength() - 1) {
				Position candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
				if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
						&& !result.contains(candPos)) {
					result.add(candPos);
					if (getRouteReq(result, wkField, candPos, goalPos)) {
						return true;
					} else {
						result.remove(candPos);
					}
				}
			}
			if (nowPos.getyIndex() != wkField.getYLength() - 1) {
				Position candPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
				if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
						&& !result.contains(candPos)) {
					result.add(candPos);
					if (getRouteReq(result, wkField, candPos, goalPos)) {
						return true;
					} else {
						result.remove(candPos);
					}
				}
			}
			if (nowPos.getxIndex() != 0) {
				Position candPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
				if (wkField.masu[candPos.getyIndex()][candPos.getxIndex()] == Masu.NOT_BLACK
						&& !result.contains(candPos)) {
					result.add(candPos);
					if (getRouteReq(result, wkField, candPos, goalPos)) {
						return true;
					} else {
						result.remove(candPos);
					}
				}
			}
			return false;
		}

		@Override
		public GeneratorResult generate() {
			NurimazeSolver.Field wkField = new NurimazeSolver.Field(height, width,
					RoomMaker.roomMake(height, width, -1, 3));
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
								NurimazeSolver.Field virtual = new Field(wkField);
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
						wkField = new NurimazeSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, 3));
					} else {
						// スタート、ゴール、ルートを決める
						Position pivot = null;
						Collections.shuffle(indexList);
						for (Position pos : indexList) {
							if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
								pivot = pos;
								break;
							}
						}
						Map<Integer, List<Position>> startPosCand = getDistanceMap(wkField, pivot);
						Position startPos = null;
						while (startPos == null) {
							outer: for (Entry<Integer, List<Position>> entry : startPosCand.entrySet()) {
								Collections.shuffle(entry.getValue());
								for (Position pos : entry.getValue()) {
									// 遠いマスから順に一定確率で抽選
									if (Math.random() < 0.2) {
										startPos = pos;
										break outer;
									}
								}
							}
						}
						Map<Integer, List<Position>> endPosCand = getDistanceMap(wkField, startPos);
						Position endPos = null;
						while (endPos == null) {
							outer: for (Entry<Integer, List<Position>> entry : endPosCand.entrySet()) {
								if (entry.getKey() > 0) {
									// ゴールはかならず1マス以上離す
									Collections.shuffle(entry.getValue());
									for (Position pos : entry.getValue()) {
										// 遠いマスから順に一定確率で抽選
										if (Math.random() < 0.2) {
											endPos = pos;
											break outer;
										}
									}
								}
							}
						}
						wkField.mark[startPos.getyIndex()][startPos.getxIndex()] = Mark.START;
						wkField.mark[endPos.getyIndex()][endPos.getxIndex()] = Mark.GOAL;
						// ルートを決定
						List<Position> routePosSet = getRoute(wkField, startPos, endPos);
						for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
								wkField.route[yIndex][xIndex] = routePosSet.contains(new Position(yIndex, xIndex))
										? Masu.BLACK
										: Masu.NOT_BLACK;
							}
						}
						break;
					}
				}
				// マスを戻す＆マーク配置
				List<Position> fixedMasuList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							wkField.route[yIndex][xIndex] = Masu.SPACE;
						} else if (wkField.mark[yIndex][xIndex] == null) {
							wkField.mark[yIndex][xIndex] = wkField.route[yIndex][xIndex] == Masu.BLACK ? Mark.OK
									: Mark.NG;
							fixedMasuList.add(new Position(yIndex, xIndex));
						}
					}
				}
				// 解けるかな？
				level = new NurimazeSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new NurimazeSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, 3));
				} else {
					Collections.shuffle(fixedMasuList);
					for (Position pos : fixedMasuList) {
						NurimazeSolver.Field virtual = new NurimazeSolver.Field(wkField, true);
						virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						virtual.mark[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.route[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						int solveResult = new NurimazeSolverForGenerator(virtual, 4000).solve2();
						if (solveResult != -1) {
							wkField.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
							wkField.mark[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.route[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
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
					for (List<Position> room : wkField.rooms) {
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
					for (List<Position> room : wkField.rooms) {
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
			level = (int) Math.sqrt(level * 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/○/△：" + wkField.getHintCount() + ")";
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
					if (wkField.mark[yIndex][xIndex] != null) {
						Mark mark = wkField.mark[yIndex][xIndex];
						if (mark == Mark.OK) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "black"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + mark.toString() + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex];
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
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateWall()[yIndex][xIndex];
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

	/**
	 * マーク
	 */
	public enum Mark {
		START("Ｓ", 1), GOAL("Ｇ", 2), OK("○", 3), NG("△", 4);

		String str;
		int val;

		Mark(String str, int val) {
			this.str = str;
			this.val = val;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Mark getByVal(int val) {
			for (Mark one : Mark.values()) {
				if (one.val == val) {
					return one;
				}
			}
			return null;
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_5 = "56789abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 経路情報 Masu.BLACKなら経路に含まれる。Masu.NOT_BLACKなら含まれない。
		private Masu[][] route;
		// マーク情報
		private Mark[][] mark;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<List<Position>> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?nurimaze/" + getXLength() + "/" + getYLength() + "/");
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
				if (mark[yIndex][xIndex] == null) {
					interval++;
					if (interval == 31) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = mark[yIndex][xIndex].val;
					String numStr = String.valueOf(num);
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_5.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_5.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int okCnt = 0;
			int ngCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (mark[yIndex][xIndex] == Mark.OK) {
						okCnt++;
					}
					if (mark[yIndex][xIndex] == Mark.NG) {
						ngCnt++;
					}
				}
			}
			return rooms.size() + "/" + okCnt + "/" + ngCnt;
		}

		public Masu[][] getRoute() {
			return route;
		}

		public Mark[][] getMark() {
			return mark;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public List<List<Position>> getRooms() {
			return rooms;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			route = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					route[yIndex][xIndex] = Masu.SPACE;
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
					for (List<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						rooms.add(new ArrayList<>(continuePosSet));
					}
				}
			}
			// マークを確定
			mark = new Mark[height][width];
			int index = 0;
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int interval = ALPHABET_FROM_5.indexOf(ch);
				if (interval != -1) {
					index = index + interval;
				} else {
					Position markPos = new Position(index / getXLength(), index % getXLength());
					mark[markPos.getyIndex()][markPos.getxIndex()] = Mark.getByVal(Character.getNumericValue(ch));
					if (mark[markPos.getyIndex()][markPos.getxIndex()] == Mark.START
							|| mark[markPos.getyIndex()][markPos.getxIndex()] == Mark.OK
							|| mark[markPos.getyIndex()][markPos.getxIndex()] == Mark.GOAL) {
						route[markPos.getyIndex()][markPos.getxIndex()] = Masu.BLACK;
					} else if (mark[markPos.getyIndex()][markPos.getxIndex()] == Mark.NG) {
						route[markPos.getyIndex()][markPos.getxIndex()] = Masu.NOT_BLACK;
					}
					masu[markPos.getyIndex()][markPos.getxIndex()] = Masu.NOT_BLACK;
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			route = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					route[yIndex][xIndex] = other.route[yIndex][xIndex];
				}
			}
			// 壁・部屋は参照渡しで使い回し(一度Fieldができたら変化しないはずなので。)
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
			mark = other.mark;
		}

		/**
		 * ジェネレータ用。プレーンなフィールド作る
		 */
		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			route = new Masu[height][width];
			mark = new Mark[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					route[yIndex][xIndex] = Masu.SPACE;
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			this.rooms = new ArrayList<>();
			// 部屋の探索順序を合わせないとレベル表記がずれるので、
			// 回りくどいが、左上の部屋から加えるようにする。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (Set<Position> room : rooms) {
						ArrayList<Position> target = new ArrayList<>(room);
						if (room.contains(new Position(yIndex, xIndex)) && !this.rooms.contains(target)) {
							this.rooms.add(target);
						}
					}
				}
			}

		}

		/**
		 * ジェネレータ用
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			route = new Masu[other.getYLength()][other.getXLength()];
			mark = new Mark[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					route[yIndex][xIndex] = other.route[yIndex][xIndex];
					mark[yIndex][xIndex] = other.mark[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
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
					if (mark[yIndex][xIndex] != null) {
						sb.append(mark[yIndex][xIndex]);
					} else {
						if (route[yIndex][xIndex] == Masu.BLACK) {
							sb.append("Ｒ");
						} else {
							sb.append(masu[yIndex][xIndex]);
						}
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
					sb.append(route[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 同じ部屋のマスは同じ色になる。 混在している場合falseを返す。
		 */
		private boolean roomSolve() {
			for (List<Position> room : rooms) {
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

		/**
		 * 置くと池・逆池ができるマスを白・黒マスにする。 既に池・逆池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
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
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていなかったりループの場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				HashSet<Position> serveyWhitePos = new HashSet<>(whitePosSet);
				while (!serveyWhitePos.isEmpty()) {
					Set<Position> continuePosSet = new HashSet<>();
					Position onePos = (Position) serveyWhitePos.toArray()[0];
					continuePosSet.add(onePos);
					if (!loopCheck(onePos, continuePosSet, null)) {
						return false;
					}
					serveyWhitePos.removeAll(continuePosSet);
				}
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueWhitePosSet(typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 白確定マスをつなぎ、ループができてる場合falseを返す。
		 */
		private boolean loopCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!mazeSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!connectRouteSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 経路には以下の特徴がある。 <br>
		 * ・経路は必ず白マス。<br>
		 * ・黒マスは必ず経路外。 <br>
		 * ・S、Gに接する経路は必ず1マス。 <br>
		 * ・S、G以外の経路マスに接する経路は必ず2マス。 <br>
		 * ・2つの経路マスに接する白マスは必ず経路。 <br>
		 * ・3つの経路マスに接する白マスはNG。<br>
		 * ・3つ以上の経路でないマスに接するマスは必ず経路外。<br>
		 */
		private boolean mazeSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (route[yIndex][xIndex] == Masu.BLACK) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu routeUp = yIndex == 0 ? Masu.NOT_BLACK : route[yIndex - 1][xIndex];
						Masu routeRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : route[yIndex][xIndex + 1];
						Masu routeDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : route[yIndex + 1][xIndex];
						Masu routeLeft = xIndex == 0 ? Masu.NOT_BLACK : route[yIndex][xIndex - 1];
						if (routeUp == Masu.BLACK) {
							blackCnt++;
						} else if (routeUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (routeRight == Masu.BLACK) {
							blackCnt++;
						} else if (routeRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (routeDown == Masu.BLACK) {
							blackCnt++;
						} else if (routeDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (routeLeft == Masu.BLACK) {
							blackCnt++;
						} else if (routeLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (route[yIndex][xIndex] == Masu.BLACK) {
							if (mark[yIndex][xIndex] == Mark.START || mark[yIndex][xIndex] == Mark.GOAL) {
								if (blackCnt > 1) {
									return false;
								}
								if (whiteCnt > 3) {
									return false;
								}
								if (blackCnt == 1) {
									if (routeUp == Masu.SPACE) {
										route[yIndex - 1][xIndex] = Masu.NOT_BLACK;
									}
									if (routeRight == Masu.SPACE) {
										route[yIndex][xIndex + 1] = Masu.NOT_BLACK;
									}
									if (routeDown == Masu.SPACE) {
										route[yIndex + 1][xIndex] = Masu.NOT_BLACK;
									}
									if (routeLeft == Masu.SPACE) {
										route[yIndex][xIndex - 1] = Masu.NOT_BLACK;
									}
								}
								if (whiteCnt == 3) {
									if (routeUp == Masu.SPACE) {
										route[yIndex - 1][xIndex] = Masu.BLACK;
									}
									if (routeRight == Masu.SPACE) {
										route[yIndex][xIndex + 1] = Masu.BLACK;
									}
									if (routeDown == Masu.SPACE) {
										route[yIndex + 1][xIndex] = Masu.BLACK;
									}
									if (routeLeft == Masu.SPACE) {
										route[yIndex][xIndex - 1] = Masu.BLACK;
									}
								}
							} else {
								if (blackCnt > 2) {
									return false;
								}
								if (whiteCnt > 2) {
									return false;
								}
								if (blackCnt == 2) {
									if (routeUp == Masu.SPACE) {
										route[yIndex - 1][xIndex] = Masu.NOT_BLACK;
									}
									if (routeRight == Masu.SPACE) {
										route[yIndex][xIndex + 1] = Masu.NOT_BLACK;
									}
									if (routeDown == Masu.SPACE) {
										route[yIndex + 1][xIndex] = Masu.NOT_BLACK;
									}
									if (routeLeft == Masu.SPACE) {
										route[yIndex][xIndex - 1] = Masu.NOT_BLACK;
									}
								}
								if (whiteCnt == 2) {
									if (routeUp == Masu.SPACE) {
										route[yIndex - 1][xIndex] = Masu.BLACK;
									}
									if (routeRight == Masu.SPACE) {
										route[yIndex][xIndex + 1] = Masu.BLACK;
									}
									if (routeDown == Masu.SPACE) {
										route[yIndex + 1][xIndex] = Masu.BLACK;
									}
									if (routeLeft == Masu.SPACE) {
										route[yIndex][xIndex - 1] = Masu.BLACK;
									}
								}
							}
						} else {
							if (blackCnt > 2) {
								return false;
							} else if (blackCnt == 2) {
								if (route[yIndex][xIndex] == Masu.NOT_BLACK) {
									return false;
								}
								route[yIndex][xIndex] = Masu.BLACK;
							} else if (whiteCnt > 2) {
								if (route[yIndex][xIndex] == Masu.BLACK) {
									return false;
								}
								route[yIndex][xIndex] = Masu.NOT_BLACK;

							}
						}
					} else if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (route[yIndex][xIndex] == Masu.BLACK) {
							return false;
						}
						route[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * ルートが1つながりになっていない場合falseを返す。
		 */
		public boolean connectRouteSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (route[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinueRoutePosSet(blackPos, blackPosSet, null);
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
		 * posを起点に上下左右に白確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueRoutePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& route[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueRoutePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& route[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueRoutePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& route[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueRoutePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& route[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueRoutePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
					if (route[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public NurimazeSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NurimazeSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NurimazeSolver(height, width, param).solve());
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
		int level = (int) Math.sqrt(count * 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		// System.out.println(field);
		for (List<Position> room : field.rooms) {
			// マスは各部屋1マスずつ調べればよい。
			Collections.shuffle(room);
			Position pos = room.get(0);
			if (field.masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
				count++;
				if (!oneCandSolve(field, pos.getyIndex(), pos.getxIndex(), recursive)) {
					return false;
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.route[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandRouteSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
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
			field.route = virtual2.route;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.route = virtual.route;
		}
		return true;
	}

	/**
	 * 1つの経路に対する仮置き調査
	 */
	private boolean oneCandRouteSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.route[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.route[yIndex][xIndex] = Masu.NOT_BLACK;
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
			field.route = virtual2.route;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.route = virtual.route;
		}
		return true;
	}
}