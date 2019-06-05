package myamya.other.solver.yajilin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class YajilinSolver implements Solver {

	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	enum MasuImpl implements Masu {
		/** 白マス */
		SPACE("　", new ArrayList<>(), false, true),
		/** 黒マス */
		BLACK("■", new ArrayList<>(), false, false),
		/** 黒にならないことが確定したマス */
		NOT_BLACK("・", new ArrayList<>(), false, true),
		/** */
		UP_RIGHT("└", Arrays.asList(new Direction[] { Direction.UP, Direction.RIGHT }), true, false),
		/** */
		UP_DOWN("│", Arrays.asList(new Direction[] { Direction.UP, Direction.DOWN }), true, false),
		/** */
		UP_LEFT("┘", Arrays.asList(new Direction[] { Direction.UP, Direction.LEFT }), true, false),
		/** */
		RIGHT_DOWN("┌", Arrays.asList(new Direction[] { Direction.RIGHT, Direction.DOWN }), true, false),
		/** */
		RIGHT_LEFT("─", Arrays.asList(new Direction[] { Direction.RIGHT, Direction.LEFT }), true, false),
		/** */
		DOWN_LEFT("┐", Arrays.asList(new Direction[] { Direction.DOWN, Direction.LEFT }), true, false),
		/** 外壁。 */
		WALL("？", new ArrayList<>(), false, false);

		private final String str;
		private final List<Direction> targetDirection;
		private final boolean isPath;
		private final boolean isNotFixed;

		MasuImpl(String str, List<Direction> targetDirection, boolean isPath, boolean isNotFixed) {
			this.str = str;
			this.targetDirection = targetDirection;
			this.isPath = isPath;
			this.isNotFixed = isNotFixed;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public List<Direction> getTargetDirection() {
			return targetDirection;
		}

		@Override
		public boolean isPath() {
			return isPath;
		}

		@Override
		public String toStringForweb() {
			return toString();
		}

		@Override
		public boolean isNotFixed() {
			return isNotFixed;
		}

	}

	/**
	 * 1マスを示すクラス
	 */
	public interface Masu {
		/**
		 * 自分が向いている方向リスト
		 */
		List<Direction> getTargetDirection();

		boolean isPath();

		boolean isNotFixed();

		Object toStringForweb();
	}

	/**
	 * 矢印のマス
	 */
	static class Arrow implements Masu {
		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, char ch) {
			this.direction = direction;
			this.count = Character.getNumericValue(ch);
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return direction.toString() + (count > 10 ? ALPHABET.charAt(count - 10) : count);
		}

		public String toStringForweb() {
			return direction.getDirectString() + count;
		}

		@Override
		public List<Direction> getTargetDirection() {
			return new ArrayList<>();
		}

		@Override
		public boolean isPath() {
			return false;
		}

		@Override
		public boolean isNotFixed() {
			return false;
		}

	}

	/**
	 * 盤面全体
	 */
	public static class Field {
		private final Masu[][] masu;

		public Field(List<String> fieldStr) {
			int height = fieldStr.size();
			int width = fieldStr.get(0).length() / 2;
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				String line = fieldStr.get(yIndex);
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					String oneMashStr = line.substring(xIndex * 2, xIndex * 2 + 2);
					if (oneMashStr.equals("00")) {
						masu[yIndex][xIndex] = MasuImpl.SPACE;
					} else {
						masu[yIndex][xIndex] = new Arrow(Direction.getByStr(oneMashStr.substring(0, 1)),
								oneMashStr.charAt(1));
					}
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = MasuImpl.SPACE;
				}
			}
			int index = 0;
			Direction direction = null;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch) + 1;
				if (interval != 0) {
					index = index + interval;
				} else {
					if (direction == null) {
						direction = Direction.getByNum(Character.getNumericValue(ch));
						if (direction == null) {
							masu[index / getXLength()][index % getXLength()] = MasuImpl.WALL;
							index++;
							i++;
						}
					} else {
						if (Character.getNumericValue(ch) != -1) {
							masu[index / getXLength()][index % getXLength()] = new Arrow(direction, ch);
						} else {
							masu[index / getXLength()][index % getXLength()] = MasuImpl.WALL;
						}
						index++;
						direction = null;
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
		}

		/**
		 * 盤面の文字列表現を返す
		*/
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Masu[] line : masu) {
				for (Masu masu : line) {
					sb.append(masu.toString());
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		/**
		 * 盤面の文字列表現を返す
		*/
		public String toStringForweb() {
			StringBuilder sb = new StringBuilder();
			for (Masu[] line : masu) {
				for (Masu masu : line) {
					sb.append(masu.toStringForweb());
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		/**
		 * パズルが解けているか。
		 * スペースがない状態を解けているとみなす
		 */
		public boolean isSolved() {
			for (Masu[] line : masu) {
				for (Masu masu : line) {
					if (masu == MasuImpl.SPACE || masu == MasuImpl.NOT_BLACK) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 指定した座標がヤジリンのルール上問題ないかを調べる。
		 */
		private boolean oneIsOk(int yIndex, int xIndex) {
			if (yIndex < 0 || xIndex < 0 || yIndex >= getYLength() || xIndex >= getXLength()) {
				// はみ出してるマスは調査対象外
				return true;
			}
			// 各方向にあるマスをマッピング
			Masu nowMasu = masu[yIndex][xIndex];
			if (nowMasu instanceof Arrow || nowMasu == MasuImpl.WALL) {
				// 矢印、？マスは調査対象外
				return true;
			}
			Map<Direction, Masu> masuMap = getMasuMap(yIndex, xIndex);
			if (nowMasu == MasuImpl.BLACK) {
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (entry.getValue() == MasuImpl.BLACK
							|| entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
						// 黒マスは、隣接マスに黒マスや自分向きのマスがあってはならない
						return false;
					}
				}
			} else if (nowMasu == MasuImpl.NOT_BLACK) {
				int cnt = 0;
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (!entry.getValue().isNotFixed()
							&& !entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
						cnt++;
					}
				}
				// 黒マスでないマスは、隣接マスに向いたときに受け入れ不可能なマスが2個を超えてはならない
				if (cnt > 2) {
					return false;
				}
			} else {
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (nowMasu.getTargetDirection().contains(entry.getKey())) {
						if (!entry.getValue().isNotFixed()
								&& !entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
							// 自分が向いている方向にあるマスが自分向きまたはスペースでなければならない
							return false;
						}
					} else {
						if (entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
							// 自分が向いていない方向にあるマスが自分向きであってはならない
							return false;
						}
					}
				}
				// 閉路チェック
				Set<Position> root = new HashSet<>();
				Position firstPosition = new Position(yIndex, xIndex);
				Position position = new Position(yIndex, xIndex);
				Direction from = nowMasu.getTargetDirection().get(0).opposite();
				boolean isLoop = false;
				while (true) {
					Masu rootingMasu = masu[position.getyIndex()][position.getxIndex()];
					if (!rootingMasu.isPath()) {
						break;
					}
					Direction nextDirection = rootingMasu.getTargetDirection().get(0) != from
							? rootingMasu.getTargetDirection().get(0)
							: rootingMasu.getTargetDirection().get(1);
					from = nextDirection.opposite();
					if (nextDirection == Direction.UP) {
						position = new Position(position.getyIndex() - 1, position.getxIndex());
					} else if (nextDirection == Direction.RIGHT) {
						position = new Position(position.getyIndex(), position.getxIndex() + 1);
					} else if (nextDirection == Direction.DOWN) {
						position = new Position(position.getyIndex() + 1, position.getxIndex());
					} else if (nextDirection == Direction.LEFT) {
						position = new Position(position.getyIndex(), position.getxIndex() - 1);
					}
					root.add(position);
					if (firstPosition.equals(position)) {
						// 閉路が完成
						isLoop = true;
						break;
					}
				}
				if (isLoop) {
					for (int y = 0; y < getYLength(); y++) {
						for (int x = 0; x < getXLength(); x++) {
							Masu checkMasu = masu[y][x];
							if (checkMasu.isPath()
									&& !root.contains(new Position(y, x))) {
								// 閉路に含まれない道があればアウト
								return false;
							}
						}
					}
				}
			}
			// 偶数判定
			boolean checkEven = true;
			int left = 0;
			int right = 0;
			for (int y = 0; y < getYLength(); y++) {
				Masu checkMasu = masu[y][xIndex];
				if (checkMasu == MasuImpl.SPACE || checkMasu == MasuImpl.NOT_BLACK) {
					checkEven = false;
					break;
				}
				if (checkMasu == MasuImpl.UP_LEFT || checkMasu == MasuImpl.DOWN_LEFT
						|| checkMasu == MasuImpl.RIGHT_LEFT) {
					left++;
				}
				if (checkMasu == MasuImpl.UP_RIGHT || checkMasu == MasuImpl.RIGHT_DOWN
						|| checkMasu == MasuImpl.RIGHT_LEFT) {
					right++;
				}
			}
			if (checkEven && (left % 2 != 0 || right % 2 != 0)) {
				return false;
			}
			checkEven = true;
			int up = 0;
			int down = 0;
			for (int x = 0; x < getXLength(); x++) {
				Masu checkMasu = masu[yIndex][x];
				if (checkMasu == MasuImpl.SPACE || checkMasu == MasuImpl.NOT_BLACK) {
					checkEven = false;
					break;
				}
				if (checkMasu == MasuImpl.UP_LEFT || checkMasu == MasuImpl.UP_RIGHT
						|| checkMasu == MasuImpl.UP_DOWN) {
					up++;
				}
				if (checkMasu == MasuImpl.DOWN_LEFT || checkMasu == MasuImpl.RIGHT_DOWN
						|| checkMasu == MasuImpl.UP_DOWN) {
					down++;
				}
			}
			if (checkEven && (up % 2 != 0 || down % 2 != 0)) {
				return false;
			}
			return true;
		}

		/**
		 * 自分のマスの前後左右を取得する
		 */
		private Map<Direction, Masu> getMasuMap(int yIndex, int xIndex) {
			Map<Direction, Masu> masuMap = new HashMap<>();
			masuMap.put(Direction.UP, yIndex == 0 ? MasuImpl.WALL : masu[yIndex - 1][xIndex]);
			masuMap.put(Direction.RIGHT, xIndex == getXLength() - 1 ? MasuImpl.WALL : masu[yIndex][xIndex + 1]);
			masuMap.put(Direction.DOWN, yIndex == getYLength() - 1 ? MasuImpl.WALL : masu[yIndex + 1][xIndex]);
			masuMap.put(Direction.LEFT, xIndex == 0 ? MasuImpl.WALL : masu[yIndex][xIndex - 1]);
			return masuMap;
		}

		/**
		 * 全てのマスを調査し、候補を確定する。
		 * もし、何も置けないマスを発見したら、falseを返す。
		 */
		public boolean serveyAll(int recursiveCnt) {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!serveyOne(recursiveCnt, yIndex, xIndex)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * あるマスを調査し、候補を確定する。
		 * もし、何も置けなかったらfalseを返す。
		 */
		private boolean serveyOne(int recursiveCnt, int yIndex, int xIndex) {
			if (yIndex < 0 || xIndex < 0 || yIndex >= getYLength() || xIndex >= getXLength()) {
				// はみ出してるマスは調査対象外
				return true;
			}
			Masu nowMasu = masu[yIndex][xIndex];
			if (!nowMasu.isNotFixed()) {
				return true;
			}
			// 候補を1つずつ調査
			List<Masu> masuCand = new LinkedList<>(
					Arrays.asList(new MasuImpl[] { MasuImpl.BLACK, MasuImpl.UP_RIGHT, MasuImpl.UP_DOWN,
							MasuImpl.UP_LEFT, MasuImpl.RIGHT_DOWN, MasuImpl.RIGHT_LEFT, MasuImpl.DOWN_LEFT }));
			if (nowMasu == MasuImpl.NOT_BLACK) {
				masuCand.remove(MasuImpl.BLACK);
			}
			for (Iterator<Masu> iterator = masuCand.iterator(); iterator.hasNext();) {
				Masu masu = (Masu) iterator.next();
				Field virtual = new Field(this);
				virtual.masu[yIndex][xIndex] = masu;
				// ためしに入れてみてチェック
				if (!virtual.oneIsOk(yIndex, xIndex)) {
					iterator.remove();
					continue;
				}
				// 再帰調査をする場合
				if (recursiveCnt != 0) {
					// 置いたマスの周辺4マスに置けなくなるマスが発生する場合、候補から除外
					if (!virtual.serveyOne(recursiveCnt - 1, yIndex - 1, xIndex) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex, xIndex + 1) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex + 1, xIndex) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex, xIndex - 1)) {
						iterator.remove();
					}
				}
			}
			if (masuCand.size() == 0) {
				// 候補が0件の場合、このままでは正答にたどり着かない
				return false;
			}
			if (masuCand.size() == 1) {
				// 候補が1つに絞れたら確定する。
				masu[yIndex][xIndex] = masuCand.get(0);
				// 出口のマスまたは黒マスに隣接するスペースは黒マスでないことが確定する
				if (masu[yIndex][xIndex].isPath()) {
					for (Direction direction : masu[yIndex][xIndex].getTargetDirection()) {
						if (direction == Direction.UP) {
							if (masu[yIndex - 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.RIGHT) {
							if (masu[yIndex][xIndex + 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.DOWN) {
							if (masu[yIndex + 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.LEFT) {
							if (masu[yIndex][xIndex - 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
							}
						}
					}
				} else if (masu[yIndex][xIndex] == MasuImpl.BLACK) {
					for (Direction direction : Direction.values()) {
						if (direction == Direction.UP) {
							if (yIndex > 0 && masu[yIndex - 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.RIGHT) {
							if (xIndex < getXLength() - 1 && masu[yIndex][xIndex + 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.DOWN) {
							if (yIndex < getYLength() - 1 && masu[yIndex + 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.LEFT) {
							if (xIndex > 0 && masu[yIndex][xIndex - 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		public boolean serveyArrow(int recursiveCnt, int serveyLevel, int yStart, int xStart) {
			for (int yIndex = yStart; yIndex < getYLength(); yIndex++) {
				for (int xIndex = xStart; xIndex < getXLength(); xIndex++) {
					xStart = 0;
					if (!(masu[yIndex][xIndex] instanceof Arrow)) {
						// 矢印以外であれば調査対象外
						continue;
					}
					Arrow arrow = (Arrow) masu[yIndex][xIndex];
					List<Position> searchPositionList = new ArrayList<>();
					if (arrow.getDirection() == Direction.UP) {
						for (int searchY = yIndex - 1; searchY >= 0; searchY--) {
							searchPositionList.add(new Position(searchY, xIndex));
						}
					} else if (arrow.getDirection() == Direction.RIGHT) {
						for (int searchX = xIndex + 1; searchX < getXLength(); searchX++) {
							searchPositionList.add(new Position(yIndex, searchX));
						}
					} else if (arrow.getDirection() == Direction.DOWN) {
						for (int searchY = yIndex + 1; searchY < getYLength(); searchY++) {
							searchPositionList.add(new Position(searchY, xIndex));
						}
					} else if (arrow.getDirection() == Direction.LEFT) {
						for (int searchX = xIndex - 1; searchX >= 0; searchX--) {
							searchPositionList.add(new Position(yIndex, searchX));
						}
					}
					List<Integer> fixedBlackPositionIndexList = new ArrayList<>();
					for (int i = 0; i < searchPositionList.size(); i++) {
						Position pos = searchPositionList.get(i);
						if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
							fixedBlackPositionIndexList.add(i);
						}
					}
					// 考えられる黒マスの置き方を全て列挙
					List<Set<Integer>> combination = getCombination(searchPositionList.size(), arrow.getCount());
					for (Iterator<Set<Integer>> iterator = combination.iterator(); iterator.hasNext();) {
						Set<Integer> oneCombi = iterator.next();
						boolean isConflict = false;
						for (int idx : fixedBlackPositionIndexList) {
							if (!oneCombi.contains(idx)) {
								isConflict = true;
								break;
							}
						}
						// 既に決まった黒マスを含まない候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						Field virtual = new Field(this);
						for (Integer idx : oneCombi) {
							int targetyIndex = searchPositionList.get(idx).getyIndex();
							int targetxIndex = searchPositionList.get(idx).getxIndex();
							if (virtual.masu[targetyIndex][targetxIndex] != MasuImpl.SPACE
									&& virtual.masu[targetyIndex][targetxIndex] != MasuImpl.BLACK) {
								isConflict = true;
								break;
							}
						}
						// 黒マスが置けない場所に黒マスを置こうとする候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							int targetyIndex = searchPositionList.get(i).getyIndex();
							int targetxIndex = searchPositionList.get(i).getxIndex();
							if (oneCombi.contains(i)) {
								virtual.masu[targetyIndex][targetxIndex] = MasuImpl.BLACK;
							} else if (virtual.masu[targetyIndex][targetxIndex] == MasuImpl.SPACE) {
								virtual.masu[targetyIndex][targetxIndex] = MasuImpl.NOT_BLACK;
							}
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							int targetyIndex = searchPositionList.get(i).getyIndex();
							int targetxIndex = searchPositionList.get(i).getxIndex();
							// 仮置きしたうえでルール違反を調査
							if (!virtual.oneIsOk(targetyIndex, targetxIndex) ||
									!virtual.serveyOne(serveyLevel, targetyIndex - 1, targetxIndex) ||
									!virtual.serveyOne(serveyLevel, targetyIndex, targetxIndex + 1) ||
									!virtual.serveyOne(serveyLevel, targetyIndex + 1, targetxIndex) ||
									!virtual.serveyOne(serveyLevel, targetyIndex, targetxIndex - 1)) {
								isConflict = true;
								break;
							}
						}
						// ルール違反の候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						// 再帰調査をする場合
						if (recursiveCnt != 0) {
							// 黒マスが置けなくなる組み合わせがあった場合候補から除外
							if (!virtual.serveyArrow(recursiveCnt - 1, serveyLevel, yIndex, xIndex)) {
								iterator.remove();
							}
						}
					}
					if (combination.size() == 0) {
						return false;
					} else if (combination.size() == 1) {
						// 候補が1通りなので黒、黒以外がすべて確定
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (combination.get(0).contains(i)) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.BLACK;
								for (Direction direction : Direction.values()) {
									if (direction == Direction.UP) {
										if (pos.getyIndex() > 0
												&& masu[pos.getyIndex() - 1][pos.getxIndex()] == MasuImpl.SPACE) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.RIGHT) {
										if (pos.getxIndex() < getXLength() - 1
												&& masu[pos.getyIndex()][pos.getxIndex() + 1] == MasuImpl.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.DOWN) {
										if (pos.getyIndex() < getYLength() - 1
												&& masu[pos.getyIndex() + 1][pos.getxIndex()] == MasuImpl.SPACE) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.LEFT) {
										if (pos.getxIndex() > 0
												&& masu[pos.getyIndex()][pos.getxIndex() - 1] == MasuImpl.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = MasuImpl.NOT_BLACK;
										}
									}
								}
							} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.NOT_BLACK;
							}
						}
					} else {
						// 部分的に黒、黒以外が確定
						Set<Integer> candIdx = null;
						for (Set<Integer> idxSet : combination) {
							if (candIdx == null) {
								candIdx = new HashSet<>(idxSet);
							} else {
								candIdx.retainAll(idxSet);
							}
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (candIdx.contains(i)) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.BLACK;
								for (Direction direction : Direction.values()) {
									if (direction == Direction.UP) {
										if (pos.getyIndex() > 0
												&& masu[pos.getyIndex() - 1][pos.getxIndex()] == MasuImpl.SPACE) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.RIGHT) {
										if (pos.getxIndex() < getXLength() - 1
												&& masu[pos.getyIndex()][pos.getxIndex() + 1] == MasuImpl.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.DOWN) {
										if (pos.getyIndex() < getYLength() - 1
												&& masu[pos.getyIndex() + 1][pos.getxIndex()] == MasuImpl.SPACE) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = MasuImpl.NOT_BLACK;
										}
									} else if (direction == Direction.LEFT) {
										if (pos.getxIndex() > 0
												&& masu[pos.getyIndex()][pos.getxIndex() - 1] == MasuImpl.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = MasuImpl.NOT_BLACK;
										}
									}
								}
							}
						}
						candIdx = null;
						for (Set<Integer> idxSet : combination) {
							if (candIdx == null) {
								candIdx = new HashSet<>(idxSet);
							} else {
								candIdx.addAll(idxSet);
							}
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (!candIdx.contains(i) && masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * sizeからget個分取り出す組み合わせを列挙して返します。
		 */
		static List<Set<Integer>> getCombination(int size, int get) {
			List<Set<Integer>> result = new ArrayList<>();
			if (get != 0) {
				Field.addPod(size, get, new HashSet<>(), result, 0);
			} else {
				result.add(new HashSet<>());
			}
			return result;
		}

		/**
		 * 再帰処理用
		 */
		private static void addPod(int size, int get, Set<Integer> pod, List<Set<Integer>> finalPod, int startPos) {
			for (int i = startPos; i < size; i++) {
				if (pod.contains(i) || pod.contains(i - 1)) {
					continue;
				}
				pod = new HashSet<>(pod);
				pod.add(i);
				if (pod.size() < get) {
					addPod(size, get, pod, finalPod, i);
				} else {
					finalPod.add(pod);
				}
				pod = new HashSet<>(pod);
				pod.remove(i);
			}
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}
	}

	private final Field field;

	public YajilinSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	/**
	 * ヤジリンを解いて結果を標準出力に出します。
	 * argsには1行ごとに行の情報を渡します。
	 * 1マスは2文字で表されます。以下が例です。
	 * 00 ： 白マス
	 * u0 ： 上矢印の0
	 * r1 ： 右矢印の1
	 * d2 ： 下矢印の2
	 * l3 ： 左矢印の3
	 */
	public String solve() {
		try {
			long startTime = System.nanoTime();
			System.out.println(field);
			int recursive = 0;
			String before;
			boolean invalid = false;
			while (!field.isSolved()) {
				System.out.println("recursive:" + recursive);
				before = field.toString();
				System.out.println("servey masu...");
				// 確定マスの調査
				if (!field.serveyAll(recursive + 1)) {
					invalid = true;
					break;
				}
				System.out.println(field);
				System.out.println();
				System.out.println("time:" + ((System.nanoTime() - startTime) / 1000000));
				if (field.isSolved()) {
					break;
				}
				if (!field.toString().equals(before)) {
					recursive = 0;
					continue;
				}
				if (recursive >= 3) {
					// 再帰をある程度増やしてダメなら、ギブアップ…
					break;
				}
				// 矢印マスの調査
				System.out.println("servey arrow...");
				if (!field.serveyArrow(recursive, 0, 0, 0)) {
					invalid = true;
					break;
				}
				System.out.println(field);
				System.out.println();
				System.out.println("time:" + ((System.nanoTime() - startTime) / 1000000));
				if (field.isSolved()) {
					break;
				}
				if (!field.toString().equals(before)) {
					recursive = 0;
					continue;
				}
				System.out.println("servey arrow recursive...");
				if (!field.serveyArrow(recursive, (recursive >= 2) ? 2 : 1, 0, 0)) {
					invalid = true;
					break;
				}
				System.out.println(field);
				System.out.println();
				System.out.println("time:" + ((System.nanoTime() - startTime) / 1000000));
				if (field.isSolved()) {
					break;
				}
				if (field.toString().equals(before)) {
					recursive++;
				} else {
					recursive = 0;
				}
			}
			if (invalid) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			} else if (field.isSolved()) {
				return "解けました";
			} else {
				return "解けませんでした。途中経過を返します。";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "解いている途中で予期せぬエラーが発生しました。";
		}
	}

}
