package myamya.other.solver.hakoiri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HakoiriSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 表出数字。1は○、2は△、3は□
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
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
			// 初期候補数字を決定。0-3まで
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 3; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
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
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 部屋のサイズが3の場合は0を候補から消す
			for (Set<Position> room : rooms) {
				if (room.size() == 3) {
					for (Position pos : room) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() != 1) {
							numbersCand[pos.getyIndex()][pos.getxIndex()].remove(new Integer(0));
						}
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
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
				sb.append("■");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("■");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						int number = numbersCand[yIndex][xIndex].get(0);
						sb.append(number == 1 ? "○" : number == 2 ? "△" : number == 3 ? "□" : "・");
					} else {
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] == true ? "■" : "　");
					}
				}
				sb.append("■");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("■");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "■" : "　");
						if (xIndex != getXLength() - 1) {
							sb.append("■");
						}
					}
					sb.append("■");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("■");
			}
			sb.append(System.lineSeparator());
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!roomSolve()) {
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
		 * 同じ数字は縦横斜めに隣り合わない。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
					List<Integer> rightCands = xIndex == getXLength() - 1 ? null
							: numbersCand[yIndex][xIndex + 1];
					List<Integer> downCands = yIndex == getYLength() - 1 ? null
							: numbersCand[yIndex + 1][xIndex];
					List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];

					List<Integer> ulCands = yIndex == 0 || xIndex == 0 ? null : numbersCand[yIndex - 1][xIndex - 1];
					List<Integer> urCands = yIndex == 0 || xIndex == getXLength() - 1 ? null
							: numbersCand[yIndex - 1][xIndex + 1];
					List<Integer> drCands = yIndex == getYLength() - 1 || xIndex == getXLength() - 1 ? null
							: numbersCand[yIndex + 1][xIndex + 1];
					List<Integer> dlCands = yIndex == getYLength() - 1 || xIndex == 0 ? null
							: numbersCand[yIndex + 1][xIndex - 1];

					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						if (number == 0) {
							continue;
						}
						// 同じ数字は隣り合わない
						if (upCands != null) {
							upCands.remove(number);
							if (upCands.isEmpty()) {
								return false;
							}
						}
						if (rightCands != null) {
							rightCands.remove(number);
							if (rightCands.isEmpty()) {
								return false;
							}
						}
						if (downCands != null) {
							downCands.remove(number);
							if (downCands.isEmpty()) {
								return false;
							}
						}
						if (leftCands != null) {
							leftCands.remove(number);
							if (leftCands.isEmpty()) {
								return false;
							}
						}
						if (ulCands != null) {
							ulCands.remove(number);
							if (ulCands.isEmpty()) {
								return false;
							}
						}
						if (urCands != null) {
							urCands.remove(number);
							if (urCands.isEmpty()) {
								return false;
							}
						}
						if (drCands != null) {
							drCands.remove(number);
							if (drCands.isEmpty()) {
								return false;
							}
						}
						if (dlCands != null) {
							dlCands.remove(number);
							if (dlCands.isEmpty()) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 0以外の数字は、同じ部屋に必ず1つ入る。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				boolean contains1 = false;
				boolean contains2 = false;
				boolean contains3 = false;
				for (Position pos : room) {
					contains1 = contains1 || numbersCand[pos.getyIndex()][pos.getxIndex()].contains(1);
					contains2 = contains2 || numbersCand[pos.getyIndex()][pos.getxIndex()].contains(2);
					contains3 = contains3 || numbersCand[pos.getyIndex()][pos.getxIndex()].contains(3);
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) != 0) {
							for (Position sameRoomPos : room) {
								if (!sameRoomPos.equals(pos)) {
									numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()]
											.remove(numbersCand[pos.getyIndex()][pos.getxIndex()].get(0));
								}
								if (numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()].size() == 0) {
									return false;
								}
							}
						}
					} else {
						for (int cand : numbersCand[pos.getyIndex()][pos.getxIndex()]) {
							if (cand == 0) {
								continue;
							}
							boolean isHiddenSingle = true;
							for (Position sameRoomPos : room) {
								if (!sameRoomPos.equals(pos)) {
									if (numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()].contains(cand)) {
										isHiddenSingle = false;
										break;
									}
								}
							}
							if (isHiddenSingle) {
								numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
								numbersCand[pos.getyIndex()][pos.getxIndex()].add(cand);
								break;
							}
						}
					}
				}
				// 1,2,3のいずれかが部屋に入らない場合はfalse
				if (!contains1 || !contains2 || !contains3) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 数字マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> numberPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) != 0) {
						Position numberPos = new Position(yIndex, xIndex);
						if (numberPosSet.size() == 0) {
							numberPosSet.add(numberPos);
							setContinuePosSet(numberPos, numberPosSet, null);
						} else {
							if (!numberPosSet.contains(numberPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に0確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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

	private final Field field;
	private int count = 0;

	public HakoiriSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "";//urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new HakoiriSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.numbersCand[yIndex][xIndex].size() != 1) {
					count++;
					for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
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
			}
		}
		return true;
	}
}