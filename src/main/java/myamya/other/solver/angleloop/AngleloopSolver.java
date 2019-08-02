package myamya.other.solver.angleloop;

import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class AngleloopSolver implements Solver {
	public enum Angle {
		ACUTE("▲"), RIGHT("□"), OBTUSE("☆");

		String str;

		Angle(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static class Field {
		// マーク
		private final Angle[][] angles;
		// つなぎ方の候補
		private Map<Position, Map<Position, Masu>> candidates;

		public Angle[][] getNumbers() {
			return angles;
		}

		public Map<Position, Map<Position, Masu>> getCandidates() {
			return candidates;
		}

		public int getYLength() {
			return angles.length;
		}

		public int getXLength() {
			return angles[0].length;
		}

		public Field(int height, int width, String param) {
			angles = new Angle[height + 1][width + 1];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = Character.getNumericValue(ch);
				if (interval < 10) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == 'a') {
						angles[pos.getyIndex()][pos.getxIndex()] = Angle.ACUTE;
					} else if (ch == 'b') {
						angles[pos.getyIndex()][pos.getxIndex()] = Angle.RIGHT;
					} else if (ch == 'c') {
						angles[pos.getyIndex()][pos.getxIndex()] = Angle.OBTUSE;
					}
					index++;
				}
			}
			// 移動方法の候補を作成
			candidates = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (angles[yIndex][xIndex] != null) {
						Map<Position, Masu> oneCandidates = new HashMap<>();
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							for (int targetX = 0; targetX < getXLength(); targetX++) {
								if (angles[targetY][targetX] != null && (yIndex != targetY || xIndex != targetX)) {
									oneCandidates.put(new Position(targetY, targetX), Masu.SPACE);
								}
							}
						}
						candidates.put(new Position(yIndex, xIndex), oneCandidates);
					}
				}
			}

			// スピードアップのため、同じ角度にある記号で一番近いもの以外は線をひかないことを先に確定
			for (Entry<Position, Map<Position, Masu>> candidate : candidates.entrySet()) {
				Position pos1 = candidate.getKey();
				// 角度とそれに対する最短距離のmapを作成。
				Map<Integer, Integer> directionDistanceMap = new HashMap<>();
				for (Entry<Position, Masu> target : candidate.getValue().entrySet()) {
					Position pos2 = target.getKey();
					int direction = getDirection(pos1, pos2);
					int distance = getDistance(pos1, pos2);
					if (directionDistanceMap.get(direction) == null || directionDistanceMap.get(direction) > distance) {
						directionDistanceMap.put(direction, distance);
					}
				}
				// 角度ごとに最短距離でないものは白マス確定
				for (Entry<Position, Masu> target : candidate.getValue().entrySet()) {
					Position pos2 = target.getKey();
					int direction = getDirection(pos1, pos2);
					int distance = getDistance(pos1, pos2);
					if (directionDistanceMap.get(direction) != distance) {
						candidate.getValue().put(target.getKey(), Masu.NOT_BLACK);
					}
				}
			}

			// 最初に確認用に1回形を出す。
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (angles[yIndex][xIndex] != null) {
						sb.append(angles[yIndex][xIndex]);
					} else {
						sb.append("　");
					}
				}
				sb.append(System.lineSeparator());
			}
			System.out.println(sb);
		}

		/**
		 * 2点の方向を計算する。
		 */
		public int getDirection(Position pos1, Position pos2) {
			double angle = Math.atan2(pos1.getxIndex() - pos2.getxIndex(), pos1.getyIndex() - pos2.getyIndex());
			return (int) Math.round(Math.toDegrees(angle));
		}

		/**
		 * 2点の距離を計算する。
		 */
		public int getDistance(Position pos1, Position pos2) {
			return (int) Math
					.round(Math.sqrt((pos2.getxIndex() - pos1.getxIndex()) * (pos2.getxIndex() - pos1.getxIndex())
							+ (pos2.getyIndex() - pos1.getyIndex()) * (pos2.getyIndex() - pos1.getyIndex())));
		}

		public Field(Field other) {
			angles = other.angles;
			candidates = new HashMap<>();
			for (Entry<Position, Map<Position, Masu>> entry : other.candidates.entrySet()) {
				candidates.put(entry.getKey(), new HashMap<>(entry.getValue()));
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Entry<Position, Map<Position, Masu>> entry : candidates.entrySet()) {
				sb.append(entry.getKey());
				sb.append("(");
				sb.append(angles[entry.getKey().getyIndex()][entry.getKey().getxIndex()]);
				sb.append(")");
				sb.append("={");
				for (Entry<Position, Masu> innerEntry : entry.getValue().entrySet()) {
					sb.append(innerEntry);
					sb.append(",");
				}
				if (!entry.getValue().isEmpty()) {
					sb.setLength(sb.length() - 1);
				}
				sb.append("}");
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (Entry<Position, Map<Position, Masu>> entry : candidates.entrySet()) {
				sb.append(entry.getKey());
				for (Entry<Position, Masu> innerEntry : entry.getValue().entrySet()) {
					sb.append(innerEntry);
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!mirrorSolve()) {
				return false;
			}
			if (!angleSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 自分がつながる(つながらない)ときは相手もそう。
		 */
		private boolean mirrorSolve() {
			for (Entry<Position, Map<Position, Masu>> candidate : candidates.entrySet()) {
				for (Entry<Position, Masu> target : candidate.getValue().entrySet()) {
					if (target.getValue() != Masu.SPACE) {
						Masu tagretMasu = candidates.get(target.getKey()).get(candidate.getKey());
						if (tagretMasu == Masu.SPACE) {
							candidates.get(target.getKey()).put(candidate.getKey(), target.getValue());
						} else if (tagretMasu != target.getValue()) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 一つの候補のうち黒(線を引く先)は2つだけになる。
		 */
		private boolean countSolve() {
			for (Map<Position, Masu> candidate : candidates.values()) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Masu masu : candidate.values()) {
					if (masu == Masu.BLACK) {
						blackCnt++;
					} else if (masu == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < 2) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = 2 - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Entry<Position, Masu> entry : candidate.entrySet()) {
						if (entry.getValue() == Masu.SPACE) {
							candidate.put(entry.getKey(), Masu.NOT_BLACK);
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Entry<Position, Masu> entry : candidate.entrySet()) {
						if (entry.getValue() == Masu.SPACE) {
							candidate.put(entry.getKey(), Masu.BLACK);
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒(線を引く先)に対し角度を満たさなかったり、他の線と交差している場合falseを返す。
		 */
		private boolean angleSolve() {
			for (Entry<Position, Map<Position, Masu>> candidate : candidates.entrySet()) {
				Position pivotPos = candidate.getKey();
				for (Entry<Position, Masu> entry : candidate.getValue().entrySet()) {
					if (entry.getValue() == Masu.BLACK) {
						Position targetPos = entry.getKey();
						// 角度のチェック
						for (Entry<Position, Masu> otherEntry : candidate.getValue().entrySet()) {
							if (otherEntry.getValue() == Masu.NOT_BLACK) {
								continue;
							}
							Position otherTargetPos = otherEntry.getKey();
							if (!targetPos.equals(otherTargetPos)) {
								int kakudo = getKakudo(targetPos, otherTargetPos, pivotPos);
								if (180 == kakudo || -180 == kakudo) {
									// 180度はどんな記号でもだめ
									if (otherEntry.getValue() == Masu.BLACK) {
										return false;
									}
									candidate.getValue().put(otherEntry.getKey(), Masu.NOT_BLACK);
								} else if (angles[pivotPos.getyIndex()][pivotPos.getxIndex()] == Angle.RIGHT) {
									// 直角は90度のみ
									if (!(kakudo == 90
											|| kakudo == -90
											|| kakudo == 270
											|| kakudo == -270)) {
										if (otherEntry.getValue() == Masu.BLACK) {
											return false;
										}
										candidate.getValue().put(otherEntry.getKey(), Masu.NOT_BLACK);
									}
								} else if (angles[pivotPos.getyIndex()][pivotPos.getxIndex()] == Angle.ACUTE) {
									// 鋭角は90度より小さい角度のみ
									if (!((-90 < kakudo && kakudo < 90) || kakudo < -270 || 270 < kakudo)) {
										if (otherEntry.getValue() == Masu.BLACK) {
											return false;
										}
										candidate.getValue().put(otherEntry.getKey(), Masu.NOT_BLACK);
									}
								} else if (angles[pivotPos.getyIndex()][pivotPos.getxIndex()] == Angle.OBTUSE) {
									// 鈍角は90度より大きい角度のみ
									if (!((-270 < kakudo && kakudo < -90) || (90 < kakudo && kakudo < 270))) {
										if (otherEntry.getValue() == Masu.BLACK) {
											return false;
										}
										candidate.getValue().put(otherEntry.getKey(), Masu.NOT_BLACK);
									}
								}
							}
						}
						// 交差チェック
						for (Entry<Position, Map<Position, Masu>> otherCandidate : candidates.entrySet()) {
							Position otherPivotPos = otherCandidate.getKey();
							if (!otherPivotPos.equals(pivotPos) && !otherPivotPos.equals(targetPos)) {
								for (Entry<Position, Masu> otherEntry : otherCandidate.getValue().entrySet()) {
									if (otherEntry.getValue() == Masu.NOT_BLACK) {
										continue;
									}
									Position otherTargetPos = otherEntry.getKey();
									if (!otherTargetPos.equals(pivotPos) && !otherTargetPos.equals(targetPos)) {
										if (isCross(pivotPos, targetPos, otherPivotPos, otherTargetPos)) {
											if (otherEntry.getValue() == Masu.BLACK) {
												return false;
											}
											otherCandidate.getValue().put(otherTargetPos, Masu.NOT_BLACK);
										}
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
		 * 3点の角度を計算する。
		 */
		public int getKakudo(Position pos1, Position pos2, Position pivot) {
			double angle1 = Math.atan2(pos1.getxIndex() - pivot.getxIndex(), pos1.getyIndex() - pivot.getyIndex());
			double angle2 = Math.atan2(pos2.getxIndex() - pivot.getxIndex(), pos2.getyIndex() - pivot.getyIndex());
			return (int) Math.round(Math.toDegrees(angle1 - angle2));
		}

		/**
		 * 2つの線がクロスしてるか調べる。
		 */
		private boolean isCross(Position fixedFrom, Position fixedTo, Position targetFrom, Position targetTo) {
			return Line2D.linesIntersect(fixedFrom.getxIndex(), fixedFrom.getyIndex(), fixedTo.getxIndex(),
					fixedTo.getyIndex(), targetFrom.getxIndex(), targetFrom.getyIndex(), targetTo.getxIndex(),
					targetTo.getyIndex());
		}

		/**
		 * 記号をできるだけつなぎ、全記号が回収できるか調査する。
		 * 回収できない候補があったらfalseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> connectPosSet = new HashSet<>();
			for (Entry<Position, Map<Position, Masu>> candidate : candidates.entrySet()) {
				for (Entry<Position, Masu> innerEntry : candidate.getValue().entrySet()) {
					if (connectPosSet.size() == 0) {
						connectPosSet.add(innerEntry.getKey());
						setContinuePosSet(innerEntry.getKey(), connectPosSet);
					} else {
						if (!connectPosSet.contains(innerEntry.getKey())) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に白確定でない位置をつないでいく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			Map<Position, Masu> oneCand = candidates.get(pos);
			for (Entry<Position, Masu> candidate : oneCand.entrySet()) {
				if (candidate.getValue() != Masu.NOT_BLACK && !continuePosSet.contains(candidate.getKey())) {
					continuePosSet.add(candidate.getKey());
					setContinuePosSet(candidate.getKey(), continuePosSet);
				}
			}
		}

		public boolean isSolved() {
			for (Map<Position, Masu> oneCandidates : candidates.values()) {
				for (Masu masu : oneCandidates.values()) {
					if (masu == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}
	}

	private final Field field;
	private int count = 0;

	public AngleloopSolver(int height, int width, String param) {
		field = new Field(height, width, param);
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
		System.out.println(new AngleloopSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * count / 100));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * count / 100).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (Entry<Position, Map<Position, Masu>> entry : field.candidates.entrySet()) {
				for (Entry<Position, Masu> innerEntry : entry.getValue().entrySet()) {
					if (innerEntry.getValue() == Masu.SPACE) {
						count++;
						Field virtual = new Field(field);
						virtual.candidates.get(entry.getKey()).put(innerEntry.getKey(), Masu.BLACK);
						boolean allowBlack = virtual.solveAndCheck();
						if (allowBlack && recursive > 0) {
							if (!candSolve(virtual, recursive - 1)) {
								allowBlack = false;
							}
						}
						Field virtual2 = new Field(field);
						virtual2.candidates.get(entry.getKey()).put(innerEntry.getKey(), Masu.NOT_BLACK);
						boolean allowNotBlack = virtual2.solveAndCheck();
						if (allowNotBlack && recursive > 0) {
							if (!candSolve(virtual2, recursive - 1)) {
								allowNotBlack = false;
							}
						}
						if (!allowBlack && !allowNotBlack) {
							return false;
						} else if (!allowBlack) {
							field.candidates = virtual2.candidates;
						} else if (!allowNotBlack) {
							field.candidates = virtual.candidates;
						}
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