package myamya.other.solver.box;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class BoxSolver implements Solver {

	public static class BoxGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class BoxSolverForGenerator extends BoxSolver {
			private final int limit;

			public BoxSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					return false;
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public BoxGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new BoxGenerator(3, 3).generate();
		}

		@Override
		public GeneratorResult generate() {
			BoxSolver.Field wkField = new BoxSolver.Field(height, width);
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Math.random() * 2 < 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
				}
				// ヒント決定
				// 縦のヒント
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int hint = 0;
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							hint = hint + xIndex + 1;
						}
					}
					wkField.leftHints[yIndex] = hint;
				}
				// 横のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int hint = 0;
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							hint = hint + yIndex + 1;
						}
					}
					wkField.upHints[xIndex] = hint;
				}
				//System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new BoxSolverForGenerator(wkField, 25000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new BoxSolver.Field(height, width);
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level / 3);
			String status = "Lv:" + level + "の問題を獲得！";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < wkField.getUpHints().length; xIndex++) {
				if (wkField.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(wkField.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getLeftHints().length; yIndex++) {
				if (wkField.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(wkField.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4)
							+ "\" x=\""
							+ (baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
				sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2))
						+ "\" cx=\""
						+ (wkField.getXLength() * baseSize + baseSize + baseSize + (baseSize / 2))
						+ "\" r=\""
						+ (baseSize / 2 - 2)
						+ "\" fill=\"white\", stroke=\"black\">"
						+ "</circle>");
				String numberStr = String.valueOf(yIndex + 1);
				int index = HALF_NUMS.indexOf(numberStr);
				String masuStr = null;
				if (index >= 0) {
					masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
				} else {
					masuStr = numberStr;
				}
				sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4)
						+ "\" x=\""
						+ (wkField.getXLength() * baseSize + baseSize + baseSize + 2)
						+ "\" font-size=\""
						+ (baseSize - 5)
						+ "\" textLength=\""
						+ (baseSize - 5)
						+ "\" lengthAdjust=\"spacingAndGlyphs\">"
						+ masuStr
						+ "</text>");
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
				sb.append("<circle cy=\"" + (wkField.getYLength() * baseSize + baseSize + (baseSize / 2))
						+ "\" cx=\""
						+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2))
						+ "\" r=\""
						+ (baseSize / 2 - 2)
						+ "\" fill=\"white\", stroke=\"black\">"
						+ "</circle>");
				String numberStr = String.valueOf(xIndex + 1);
				int index = HALF_NUMS.indexOf(numberStr);
				String masuStr = null;
				if (index >= 0) {
					masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
				} else {
					masuStr = numberStr;
				}
				sb.append("<text y=\"" + (wkField.getYLength() * baseSize + baseSize + baseSize - 4)
						+ "\" x=\""
						+ (xIndex * baseSize + baseSize + baseSize + 2)
						+ "\" font-size=\""
						+ (baseSize - 5)
						+ "\" textLength=\""
						+ (baseSize - 5)
						+ "\" lengthAdjust=\"spacingAndGlyphs\">"
						+ masuStr
						+ "</text>");

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
		// マスの情報
		private Masu[][] masu;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] leftHints;

		public Masu[][] getMasu() {
			return masu;
		}

		private static final String FOR_URL = "0123456789abcdefghijklmnopqrstuv";

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?box/" + getXLength() + "/" + getYLength() + "/");
			for (int upHint : upHints) {
				if (upHint > 31) {
					sb.append("-");
					sb.append(FOR_URL.charAt(upHint / 32));
					sb.append(FOR_URL.charAt(upHint % 32));
				} else {
					sb.append(FOR_URL.charAt(upHint));
				}
			}
			for (int leftHint : leftHints) {
				if (leftHint > 31) {
					sb.append("-");
					sb.append(FOR_URL.charAt(leftHint / 32));
					sb.append(FOR_URL.charAt(leftHint % 32));
				} else {
					sb.append(FOR_URL.charAt(leftHint));
				}
			}
			return sb.toString();
		}

		public String getHintCount() {
			return "";
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				//16 - 255は '-'
				//256 - 999は '+'
				int capacity;
				if (ch == '.') {
					//
				} else {
					if (ch == '-') {
						capacity = Character.getNumericValue(param.charAt(i + 1)) * 32
								+ Character.getNumericValue(param.charAt(i + 2));
						i++;
						i++;
					} else {
						capacity = Character.getNumericValue(ch);
					}
					if (index < width) {
						upHints[index] = capacity;
					} else {
						leftHints[index - width] = capacity;
					}
				}
				index++;
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public int getMaxNumCand() {
			return getYLength() > getXLength() ? getYLength() : getXLength();
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					String numStr = String.valueOf(upHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					String numStr = String.valueOf(leftHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
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
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!hintSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * ヒント数字を上回ったり下回ったりすることが確定したらfalseを返す。
		 */
		private boolean hintSolve() {
			// 縦のヒント
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int min = 0;
				int max = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						min = min + xIndex + 1;
						max = max + xIndex + 1;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						max = max + xIndex + 1;
					}
				}
				if (min > leftHints[yIndex] || max < leftHints[yIndex]) {
					return false;
				}
			}
			// 横のヒント
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int min = 0;
				int max = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						min = min + yIndex + 1;
						max = max + yIndex + 1;
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						max = max + yIndex + 1;
					}
				}
				if (min > upHints[xIndex] || max < upHints[xIndex]) {
					return false;
				}
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

	public BoxSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public BoxSolver(Field field) {
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
		System.out.println(new BoxSolver(height, width, param).solve());
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
		int level = (int) Math.sqrt(count / 3);
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
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}