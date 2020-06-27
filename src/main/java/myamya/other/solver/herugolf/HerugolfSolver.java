package myamya.other.solver.herugolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class HerugolfSolver implements Solver {
	public static class HerugolfGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class HerugolfSolverForGenerator extends HerugolfSolver {

			private final int limit;

			public HerugolfSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			/**
			 * -2:解なし
			 * -1:limit(多くの場合複数解)
			 * 0 >= 唯一解
			 */
			public int solve2() {
				try {
					while (!field.isSolved()) {
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
									if (!candSolve(field, 999)) {
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

		public HerugolfGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new HerugolfGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			HerugolfSolver.Field wkField = new HerugolfSolver.Field(height, width);
			int level = 0;
			int failCnt = 0;
			int holeCnt = 0;
			long start = System.nanoTime();
			// 問題生成部
			while (true) {
				// 穴を置く場所をランダムに決定
				List<Position> candPosList = new ArrayList<>();
				if (candPosList.isEmpty()) {
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							if (!wkField.pond[yIndex][xIndex] && !wkField.hole[yIndex][xIndex]
									&& wkField.numbers[yIndex][xIndex] == null) {
								candPosList.add(new Position(yIndex, xIndex));
							}
						}
					}
				}
				if (candPosList.isEmpty()) {
					// 置ける場所がないのに解けない→作り直し
					failCnt++;
					if (failCnt > 5000) {
						wkField = new HerugolfSolver.Field(height, width);
						failCnt = 0;
						holeCnt = 0;
					}
					continue;
				}
				Position holePos = candPosList.get((int) (Math.random() * candPosList.size()));
				// ボールの位置と数字をランダムに決定
				Position ballPos;
				int num;
				List<Position> candBallPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (!wkField.pond[yIndex][xIndex] && !wkField.hole[yIndex][xIndex]
								&& wkField.numbers[yIndex][xIndex] == null) {
							Position pos = new Position(yIndex, xIndex);
							if (holePos != pos) {
								candBallPosList.add(pos);
							}
						}
					}
				}
				ballPos = candBallPosList.get((int) (Math.random() * candBallPosList.size()));
				ArrayList<Integer> numCand = new ArrayList<>();
				numCand.add(ballPos.getyIndex());
				numCand.add(wkField.getXLength() - 1 - ballPos.getxIndex());
				numCand.add(wkField.getYLength() - 1 - ballPos.getyIndex());
				numCand.add(ballPos.getxIndex());
				int maxNum = Collections.max(numCand);
				num = (int) (Math.random() * maxNum + 1);

				// 全部の球にそれぞれ2つ以上の打ち方ができるだけあるようにする。
				HerugolfSolver.Field virtual = new HerugolfSolver.Field(wkField, true);
				virtual.hole[holePos.getyIndex()][holePos.getxIndex()] = true;
				virtual.numbers[ballPos.getyIndex()][ballPos.getxIndex()] = num;
				virtual.makeAllCandidates();
				boolean isOk = true;
				for (Entry<Position, List<List<Position>>> entry : virtual.getCandidates().entrySet()) {
					if (entry.getValue().size() < 2 && Math.random() < 0.95) {
						isOk = false;
						break;
					}
				}
				if (!isOk) {
					failCnt++;
					if (failCnt > 5000) {
						wkField = new HerugolfSolver.Field(height, width);
						failCnt = 0;
						holeCnt = 0;
					}
					continue;
				}
				// 全部のホールにそれぞれ2つ以上の球からできるだけ届くようにする
				Map<Position, List<Position>> lastPosMap = virtual.getLastPosMap();
				Map<Position, Integer> lastPosCnt = new HashMap<>();
				for (List<Position> posList : lastPosMap.values()) {
					for (Position pos : posList) {
						if (virtual.hole[pos.getyIndex()][pos.getxIndex()]) {
							Integer cnt = lastPosCnt.get(pos);
							if (cnt == null) {
								cnt = 0;
							}
							cnt++;
							lastPosCnt.put(pos, cnt);
						}
					}
				}
				for (Integer cnt : lastPosCnt.values()) {
					if (cnt < 2 && Math.random() < 0.95) {
						isOk = false;
						break;
					}
				}
				if (!isOk) {
					failCnt++;
					if (failCnt > 5000) {
						wkField = new HerugolfSolver.Field(height, width);
						failCnt = 0;
						holeCnt = 0;
					}
					continue;
				}
				// 解きチェック
				int wkResult = new HerugolfSolverForGenerator(new HerugolfSolver.Field(virtual), 2000).solve2();
				if (wkResult == -2) {
					// 解なしになった。矢印は破棄する
					failCnt++;
					if (failCnt == 5000) {
						// 失敗回数がかさんだら作りなおし
						wkField = new HerugolfSolver.Field(height, width);
						failCnt = 0;
						holeCnt = 0;
						continue;
					}
				} else if (wkResult == -1) {
					// 解なしになってないが解けず。矢印は残す
					wkField.hole[holePos.getyIndex()][holePos.getxIndex()] = true;
					wkField.numbers[ballPos.getyIndex()][ballPos.getxIndex()] = num;
					holeCnt++;
				} else {
					// 唯一解になった！
					wkField.hole[holePos.getyIndex()][holePos.getxIndex()] = true;
					wkField.numbers[ballPos.getyIndex()][ballPos.getxIndex()] = num;
					holeCnt++;
					level = wkResult * 25 + virtual.cnt * 5;
					if (holeCnt > (wkField.getYLength() - 2) / 1.2 && Math.random() < 0.1) {
						break;
					}
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(球/池：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
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
			// 池描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getPond()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "lightblue"
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// ホール描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getHole()[yIndex][xIndex]) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ "Ｈ"
								+ "</text>");
					}
				}
			}
			// 球描画
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
						String numberStr = String
								.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 移動の仕方の候補
		private Map<Position, List<List<Position>>> candidates;
		// 池のマス
		private final boolean[][] pond;
		// ゴール
		private final boolean[][] hole;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getHintCount() {
			int ballCnt = 0;
			int pondCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (pond[yIndex][xIndex]) {
						pondCnt++;
					}
					if (hole[yIndex][xIndex]) {
						ballCnt++;
					}
				}
			}
			return ballCnt + "/" + pondCnt;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?herugolf/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = -1;
				int xIndex2 = -1;
				if (i < getYLength() * getXLength()) {
					yIndex2 = i / getXLength();
					xIndex2 = i % getXLength();
				}
				i++;
				int yIndex3 = -1;
				int xIndex3 = -1;
				if (i < getYLength() * getXLength()) {
					yIndex3 = i / getXLength();
					xIndex3 = i % getXLength();
				}
				i++;
				int yIndex4 = -1;
				int xIndex4 = -1;
				if (i < getYLength() * getXLength()) {
					yIndex4 = i / getXLength();
					xIndex4 = i % getXLength();
				}
				i++;
				int yIndex5 = -1;
				int xIndex5 = -1;
				if (i < getYLength() * getXLength()) {
					yIndex5 = i / getXLength();
					xIndex5 = i % getXLength();
				}
				int num = 0;
				if (yIndex1 != -1 && xIndex1 != -1 && pond[yIndex1][xIndex1]) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && pond[yIndex2][xIndex2]) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && pond[yIndex3][xIndex3]) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && pond[yIndex4][xIndex4]) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && pond[yIndex5][xIndex5]) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null && !hole[yIndex][xIndex]) {
					interval++;
					if (interval == 18) {
						sb.append("z");
						interval = 0;
					}
				} else {
					if (hole[yIndex][xIndex]) {
						if (interval == 0) {
							sb.append("h");
						} else {
							sb.append(ALPHABET_FROM_I.substring(interval - 1, interval));
							sb.append("h");
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
							sb.append(ALPHABET_FROM_I.substring(interval - 1, interval));
							sb.append(numStr);
							interval = 0;
						}
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_I.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public boolean[][] getPond() {
			return pond;
		}

		public boolean[][] getHole() {
			return hole;
		}

		public Map<Position, List<List<Position>>> getCandidates() {
			return candidates;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			pond = new boolean[height][width];
			hole = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					pond[yIndex][xIndex] = false;
					hole[yIndex][xIndex] = false;
				}
			}
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						Position pos = new Position((cnt - mod + 0) / (getXLength()), (cnt - mod + 0) % (getXLength()));
						if (bit / 16 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 1) {
						Position pos = new Position((cnt - mod + 1) / (getXLength()), (cnt - mod + 1) % (getXLength()));
						if (bit / 8 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 2) {
						Position pos = new Position((cnt - mod + 2) / (getXLength()), (cnt - mod + 2) % (getXLength()));
						if (bit / 4 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 3) {
						Position pos = new Position((cnt - mod + 3) / (getXLength()), (cnt - mod + 3) % (getXLength()));
						if (bit / 2 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 4) {
						Position pos = new Position((cnt - mod + 4) / (getXLength()), (cnt - mod + 4) % (getXLength()));
						if (bit / 1 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_I.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == 'h') {
						hole[pos.getyIndex()][pos.getxIndex()] = true;
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
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 移動方法の候補を作成
			makeAllCandidates();
			System.out.println("移動候補作成負荷：" + cnt * 5);
		}

		/**
		 * 今の配置から打ち方の候補を作成。
		 */
		protected void makeAllCandidates() {
			candidates = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int cnt = numbers[yIndex][xIndex];
						Position pos = new Position(yIndex, xIndex);
						List<List<Position>> oneCandidateList = new ArrayList<>();
						List<Position> oneCandidate = new ArrayList<>();
						oneCandidate.add(pos);
						setCandidate(cnt, pos, oneCandidateList, oneCandidate);
						candidates.put(pos, oneCandidateList);
					}
				}
			}
		}

		int cnt = 0;

		private void setCandidate(int cnt, Position pos, List<List<Position>> oneCandidateList,
				List<Position> oneCandidate) {
			this.cnt++;
			if (pos.getyIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex() - cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					// 自分の線とのクロス判定
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							// 最後は線判定だと確実にクロスになってしまうので点で判定
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						// 数字またはホールとのクロス判定
						for (int yIndex = pos.getyIndex() - 1; yIndex > nextPos.getyIndex(); yIndex--) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() + cnt <= getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() + 1; xIndex < nextPos.getxIndex(); xIndex++) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getyIndex() + cnt <= getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int yIndex = pos.getyIndex() + 1; yIndex < nextPos.getyIndex(); yIndex++) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() - 1; xIndex > nextPos.getxIndex(); xIndex--) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
		}

		/**
		 * 今の配置から、各球が最後に到達できるマスの一覧を作成。
		 * ジェネレータ用
		 */
		protected Map<Position, List<Position>> getLastPosMap() {
			Map<Position, List<Position>> result = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int cnt = numbers[yIndex][xIndex];
						Position pos = new Position(yIndex, xIndex);
						List<Position> lastPosList = new ArrayList<>();
						List<Position> oneCandidate = new ArrayList<>();
						oneCandidate.add(pos);
						setLastPos(cnt, pos, lastPosList, oneCandidate);
						result.put(pos, lastPosList);
					}
				}
			}
			return result;
		}

		private void setLastPos(int cnt, Position pos, List<Position> lastPosList,
				List<Position> oneCandidate) {
			if (pos.getyIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex() - cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					// 自分の線とのクロス判定
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							// 最後は線判定だと確実にクロスになってしまうので点で判定
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						// 数字またはホールとのクロス判定
						for (int yIndex = pos.getyIndex() - 1; yIndex > nextPos.getyIndex(); yIndex--) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							lastPosList.add(nextPos);
							if (cnt > 1) {
								setLastPos(cnt - 1, nextPos, lastPosList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() + cnt <= getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() + 1; xIndex < nextPos.getxIndex(); xIndex++) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							lastPosList.add(nextPos);
							if (cnt > 1) {
								setLastPos(cnt - 1, nextPos, lastPosList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getyIndex() + cnt <= getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int yIndex = pos.getyIndex() + 1; yIndex < nextPos.getyIndex(); yIndex++) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							lastPosList.add(nextPos);
							if (cnt > 1) {
								setLastPos(cnt - 1, nextPos, lastPosList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() - 1; xIndex > nextPos.getxIndex(); xIndex--) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							lastPosList.add(nextPos);
							if (cnt > 1) {
								setLastPos(cnt - 1, nextPos, lastPosList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			pond = other.pond;
			hole = other.hole;
			candidates = new HashMap<>();
			for (Entry<Position, List<List<Position>>> entry : other.candidates.entrySet()) {
				candidates.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
		}

		/**
		 * プレーンなフィールド生成
		 */
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			pond = new boolean[height][width];
			hole = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 池の発生率はとりあえず1/15ぐらいで
					pond[yIndex][xIndex] = Math.random() * 15 < 1;
					hole[yIndex][xIndex] = false;
				}
			}
			candidates = new HashMap<>();
		}

		/**
		 * イミュータブルにする
		 */
		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			pond = new boolean[other.getYLength()][other.getXLength()];
			hole = new boolean[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					pond[yIndex][xIndex] = other.pond[yIndex][xIndex];
					hole[yIndex][xIndex] = other.hole[yIndex][xIndex];
				}
			}
			candidates = new HashMap<>();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						String numStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							if (index == 0) {
								sb.append("○");
							} else {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							}
						} else {
							sb.append(numStr);
						}
					} else {
						if (hole[yIndex][xIndex]) {
							sb.append("Ｈ");
						} else if (pond[yIndex][xIndex]) {
							sb.append("□");
						} else {
							sb.append("　");
						}
						for (Entry<Position, List<List<Position>>> entry : candidates.entrySet()) {
							// TODO 候補が複数あっても、途中まで同じだったらそこまで表示したい
							if (entry.getValue().size() == 1) {
								List<Position> fixedList = entry.getValue().get(0);
								for (int i = 0; i < fixedList.size() - 1; i++) {
									Position fixedFrom = fixedList.get(i);
									Position fixedTo = fixedList.get(i + 1);
									if (fixedTo.equals(new Position(yIndex, xIndex))) {
										sb.setLength(sb.length() - 1);
										if (i == fixedList.size() - 2) {
											sb.append("○");
										} else {
											sb.append("・");
										}
									} else if (isCross(fixedFrom, fixedTo, new Position(yIndex, xIndex),
											new Position(yIndex, xIndex))) {
										if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("↑");
											}
										} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("→");
											}
										} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("↓");
											}
										} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("←");
											}
										}
									}
								}
							}
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (List<List<Position>> oneCandidates : candidates.values()) {
				sb.append(oneCandidates.size());
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!moveSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 移動候補が確定している数字があるとき、その数字と交差したり、同一の部屋に入る移動候補を消す。
		 * 消した結果、移動できない数字ができたときはfalseを返す。
		 */
		private boolean moveSolve() {
			for (Entry<Position, List<List<Position>>> entry : candidates.entrySet()) {
				if (entry.getValue().size() == 1) {
					List<Position> fixedList = entry.getValue().get(0);
					for (int i = 0; i < fixedList.size() - 1; i++) {
						Position fixedFrom = fixedList.get(i);
						Position fixedTo = fixedList.get(i + 1);
						for (Entry<Position, List<List<Position>>> target : candidates.entrySet()) {
							if (!entry.getKey().equals(target.getKey())) {
								for (Iterator<List<Position>> iterator = target.getValue().iterator(); iterator
										.hasNext();) {
									List<Position> targetList = iterator.next();
									for (int j = 0; j < targetList.size() - 1; j++) {
										Position targetFrom = targetList.get(j);
										Position targetTo = targetList.get(j + 1);
										if (isCross(fixedFrom, fixedTo, targetFrom, targetTo)) {
											iterator.remove();
											break;
										}
									}
								}
								if (target.getValue().size() == 0) {
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 2つの座標がクロスしてるか調べる
		 */
		private boolean isCross(Position fixedFrom, Position fixedTo, Position targetFrom, Position targetTo) {
			int minY = fixedFrom.getyIndex() < fixedTo.getyIndex() ? fixedFrom.getyIndex() : fixedTo.getyIndex();
			int maxY = fixedFrom.getyIndex() < fixedTo.getyIndex() ? fixedTo.getyIndex() : fixedFrom.getyIndex();
			int minX = fixedFrom.getxIndex() < fixedTo.getxIndex() ? fixedFrom.getxIndex() : fixedTo.getxIndex();
			int maxX = fixedFrom.getxIndex() < fixedTo.getxIndex() ? fixedTo.getxIndex() : fixedFrom.getxIndex();
			if (targetFrom.getyIndex() < minY && targetTo.getyIndex() < minY) {
				return false;
			}
			if (targetFrom.getyIndex() > maxY && targetTo.getyIndex() > maxY) {
				return false;
			}
			if (targetFrom.getxIndex() < minX && targetTo.getxIndex() < minX) {
				return false;
			}
			if (targetFrom.getxIndex() > maxX && targetTo.getxIndex() > maxX) {
				return false;
			}
			return true;
		}

		public boolean isSolved() {
			for (List<List<Position>> oneCandidates : candidates.values()) {
				if (oneCandidates.size() != 1) {
					return false;
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public HerugolfSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public HerugolfSolver(Field field) {
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
		System.out.println(new HerugolfSolver(height, width, param).solve());
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
						if (!candSolve(field, 999)) {
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
		System.out.println("難易度:" + (count * 25 + field.cnt * 5));
		System.out.println(field);
		int level = (int) Math.sqrt((count * 25 + field.cnt * 5) / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 25 + field.cnt * 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (Entry<Position, List<List<Position>>> entry : field.candidates.entrySet()) {
				if (entry.getValue().size() != 1) {
					for (Iterator<List<Position>> iterator = entry.getValue().iterator(); iterator
							.hasNext();) {
						count++;
						List<Position> oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.candidates.get(entry.getKey()).clear();
						virtual.candidates.get(entry.getKey()).add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (entry.getValue().size() == 0) {
						return false;
					}
				}
			}
			if (field.getStateDump().equals(befStr)) {
				break;
			}
		}
		return true;
	}

}