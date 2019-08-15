package myamya.other.solver.kakuro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class KakuroSolver implements Solver {

	public static class Block {

		@Override
		public String toString() {
			return "[" + downCnt + "," + leftCnt + "]";
		}

		private final int downCnt;
		private final int leftCnt;

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
		private final int cnt;
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
		static final String ALPHABET_FROM_K = "klmnopqrstuvwxyz";
		static final String ALPHABET_FOR_NUMBER = "0123456789abcdefghijABCDEFGHIJKLMNOPQRSTUVWXYZ";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// ブロック情報
		protected final Map<Position, Block> blocks;
		// グループ情報
		protected List<Group> groups;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Map<Position, Block> getBlocks() {
			return blocks;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbersCand = new ArrayList[height][width];
			blocks = new HashMap<>();
			int readPos = 0;
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				if (index / getXLength() >= getYLength()) {
					readPos = i;
					break;
				}
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_K.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						blocks.put(pos, new Block(0, 0));
					} else {
						int downCnt = ALPHABET_FOR_NUMBER.indexOf(ch);
						i++;
						int rightCnt = ALPHABET_FOR_NUMBER.indexOf(param.charAt(i));
						blocks.put(pos, new Block(downCnt, rightCnt));
					}
					index++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (!blocks.containsKey(new Position(0, xIndex))) {
					blocks.put(new Position(-1, xIndex),
							new Block(ALPHABET_FOR_NUMBER.indexOf(param.charAt(readPos)), 0));
					readPos++;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (!blocks.containsKey(new Position(yIndex, 0))) {
					blocks.put(new Position(yIndex, -1),
							new Block(0, ALPHABET_FOR_NUMBER.indexOf(param.charAt(readPos))));
					readPos++;
				}
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
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					if (!blocks.containsKey(new Position(yIndex, xIndex))) {
						for (int number = 1; number <= 9; number++) {
							numbersCand[yIndex][xIndex].add(number);
						}
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			blocks = other.blocks;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			groups = other.groups;
		}

		private static final String NUMBER_TO_ALPHABETS = "-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

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
							if (numbersCand[yIndex][xIndex].size() == 0) {
								sb.append("×");
							} else if (numbersCand[yIndex][xIndex].size() == 1) {
								String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
								int index = HALF_NUMS.indexOf(numStr);
								if (index >= 0) {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(numStr);
								}
							} else if (numbersCand[yIndex][xIndex].size() == 2) {
								sb.append(numbersCand[yIndex][xIndex].get(0));
								sb.append(numbersCand[yIndex][xIndex].get(1));
							} else {
								sb.append("　");
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		int cnt = 0;

		/**
		 * チェックを実行。
		 * 数字の合計を満たす組み合わせがない場合falseを返す。
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			for (Group group : groups) {
				for (Position pos : group.getMember()) {
					// 自分以外のグループ内のマス
					List<Position> otherMember = new ArrayList<>(group.getMember());
					otherMember.remove(pos);
					for (Iterator<Integer> iterator = numbersCand[pos.getyIndex()][pos.getxIndex()].iterator(); iterator
							.hasNext();) {
						cnt++;
						// 自分の数字を決めたときに数字の合計を満たす組み合わせがあるか。
						int oneCand = iterator.next();
						Set<Integer> useNumber = new HashSet<>();
						useNumber.add(oneCand);
						if (!setCandNum(group.getCnt(), otherMember, useNumber, oneCand)) {
							// なければその数字を消す
							iterator.remove();
						}
					}
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 0) {
						return false;
					}
				}
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * member+1のサイズになったときにsumを満たせる候補があればtrueを、なければfalseを返す。
		 */
		private boolean setCandNum(int cnt, List<Position> member, Set<Integer> useNumber, int sum) {
			if (useNumber.size() == member.size() + 1) {
				if (sum == cnt) {
					return true;
				}
			} else {
				Position pos = member.get(useNumber.size() - 1);
				for (int oneCand : numbersCand[pos.getyIndex()][pos.getxIndex()]) {
					if (!useNumber.contains(oneCand)) {
						// 同一数字除外
						useNumber.add(oneCand);
						if (setCandNum(cnt, member, useNumber, sum + oneCand)) {
							return true;
						}
						useNumber.remove(oneCand);
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() > 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public KakuroSolver(int height, int width, String param) {
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
		System.out.println(new KakuroSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		System.out.println(field);
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			count = count + field.cnt;
			field.cnt = 0;
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
		System.out.println("難易度:" + ((long) count * (long) count / 20000));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount((int) ((long) count * (long) count / 20000)).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.blocks.containsKey(new Position(yIndex, xIndex))) {
					continue;
				}
				if (field.numbersCand[yIndex][xIndex].size() != 1) {
					for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
						count++;
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.numbersCand[yIndex][xIndex].clear();
						virtual.numbersCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						count = count + virtual.cnt;
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