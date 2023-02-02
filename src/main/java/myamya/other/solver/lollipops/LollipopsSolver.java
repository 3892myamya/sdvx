package myamya.other.solver.lollipops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class LollipopsSolver implements Solver {
	public static class LollipopsGenerator implements Generator {

		static class LollipopsSolverForGenerator extends LollipopsSolver {
			private final int limit;

			public LollipopsSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

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
									if (!candSolve(field, 2)) {
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

		public LollipopsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new LollipopsGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			LollipopsSolver.Field wkField = new LollipopsSolver.Field(height, width);
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
				// 記号を配置
				Collections.shuffle(indexList);
				boolean isOk = false;
				for (Position pos : indexList) {
					isOk = false;
					List<Integer> numIdxList = new ArrayList<>();
					for (int number : wkField.masuCand[pos.getyIndex()][pos.getxIndex()]) {
						numIdxList.add(number);
					}
					Collections.shuffle(numIdxList);
					for (int masuNum : numIdxList) {
						Field virtual = new Field(wkField);
						virtual.masuCand[pos.getyIndex()][pos.getxIndex()].clear();
						virtual.masuCand[pos.getyIndex()][pos.getxIndex()].add(masuNum);
						if (-2 != new LollipopsSolverForGenerator(virtual, 5).solve2()) {
							isOk = true;
							wkField.masuCand[pos.getyIndex()][pos.getxIndex()] = virtual.masuCand[pos.getyIndex()][pos
									.getxIndex()];
							break;
						}
					}
					if (!isOk) {
						break;
					}
				}
				if (!isOk) {
					// 破綻したら0から作り直す。
					wkField = new Field(height, width);
					continue;
				}
				// マスを戻す
				List<Position> fixedMasuList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masuCand[yIndex][xIndex].get(0) != 0) {
							fixedMasuList.add(new Position(yIndex, xIndex));
							wkField.fixedMasu[yIndex][xIndex] = wkField.masuCand[yIndex][xIndex].get(0);
						} else {
							wkField.fixedMasu[yIndex][xIndex] = null;
							wkField.masuCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 3; number++) {
								wkField.masuCand[yIndex][xIndex].add(number);
							}
						}
					}
				}
				// 解けるかな？
				level = new LollipopsSolverForGenerator(wkField, 100).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new LollipopsSolver.Field(height, width);
				} else {
					Collections.shuffle(fixedMasuList);
					boolean isDeleted = false;
					for (Position pos : fixedMasuList) {
						LollipopsSolver.Field virtual = new LollipopsSolver.Field(wkField);
						virtual.fixedMasu[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.masuCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						for (int number = 0; number <= 3; number++) {
							virtual.masuCand[pos.getyIndex()][pos.getxIndex()].add(number);
						}
						int solveResult = new LollipopsSolverForGenerator(virtual, 4000).solve2();
						if (solveResult >= 0) {
							isDeleted = true;
							wkField.fixedMasu[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.masuCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
							for (int number = 0; number <= 3; number++) {
								wkField.masuCand[pos.getyIndex()][pos.getxIndex()].add(number);
							}
							level = solveResult;
						}
					}
					if (!isDeleted) {
						// 1マスも消せないはアウト
						wkField = new LollipopsSolver.Field(height, width);
					} else {
						break;
					}
				}
			}
			level = (int) Math.sqrt(level * 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(記号：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getFixedMasu()[yIndex][xIndex] != null) {
						int number =wkField.getFixedMasu()[yIndex][xIndex];
						if (number == 0) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) 
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "・" + "</text>");
						} else if (number == 1) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (number == 2) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) 
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "│" + "</text>");
						} else if (number == 3) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "─" + "</text>");
						}
					} else if (wkField.getMasuCand()[yIndex][xIndex].size() == 1) {
						int number = wkField.getMasuCand()[yIndex][xIndex].get(0);
						if (number == 0) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" fill=\"" + "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "・" + "</text>");
						} else if (number == 1) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"green\">" + "</circle>");
						} else if (number == 2) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" fill=\"" + "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "│" + "</text>");
						} else if (number == 3) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" fill=\"" + "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "─" + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
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
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
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

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 固定されたマス。0=空白、1=○、2=│、3=─とする。表示用
		protected Integer[][] fixedMasu;
		// マスの情報
		protected ArrayList<Integer>[][] masuCand;

		public Integer[][] getFixedMasu() {
			return fixedMasu;
		}

		public String getHintCount() {
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (fixedMasu[yIndex][xIndex] != null) {
						numberCnt++;
					}
				}
			}
			return String.valueOf(numberCnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?lollipops/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (fixedMasu[yIndex][xIndex] == null) {
					interval++;
					if (interval == 26) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = fixedMasu[yIndex][xIndex];
					String numStr = Integer.toHexString(num);
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public ArrayList<Integer>[][] getMasuCand() {
			return masuCand;
		}

		public int getYLength() {
			return fixedMasu.length;
		}

		public int getXLength() {
			return fixedMasu[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			fixedMasu = new Integer[height][width];
			masuCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masuCand[yIndex][xIndex] = new ArrayList<Integer>();
					masuCand[yIndex][xIndex].add(0);
					masuCand[yIndex][xIndex].add(1);
					masuCand[yIndex][xIndex].add(2);
					masuCand[yIndex][xIndex].add(3);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			fixedMasu = new Integer[height][width];
			masuCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masuCand[yIndex][xIndex] = new ArrayList<Integer>();
					masuCand[yIndex][xIndex].add(0);
					masuCand[yIndex][xIndex].add(1);
					masuCand[yIndex][xIndex].add(2);
					masuCand[yIndex][xIndex].add(3);
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					fixedMasu[pos.getyIndex()][pos.getxIndex()] = Integer.parseInt(String.valueOf(ch));
					masuCand[pos.getyIndex()][pos.getxIndex()].clear();
					masuCand[pos.getyIndex()][pos.getxIndex()].add(Integer.parseInt(String.valueOf(ch)));
					index++;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			fixedMasu = new Integer[other.getYLength()][other.getXLength()];
			masuCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					fixedMasu[yIndex][xIndex] = other.fixedMasu[yIndex][xIndex];
					masuCand[yIndex][xIndex] = new ArrayList<Integer>(other.masuCand[yIndex][xIndex]);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masuCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (masuCand[yIndex][xIndex].size() == 1) {
						int number = masuCand[yIndex][xIndex].get(0);
						sb.append(number == 1 ? "○" : number == 2 ? "│" : number == 3 ? "─" : "・");
					} else {
						sb.append("　");
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
					sb.append(masuCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * マス周辺のチェック
		 */
		public boolean roundSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					ArrayList<Integer> masuPivot = masuCand[yIndex][xIndex];
					if (masuPivot.size() == 1) {
						ArrayList<Integer> masuUp = yIndex == 0 ? new ArrayList<Integer>() {
							{
								add(0);
							}
						} : masuCand[yIndex - 1][xIndex];
						ArrayList<Integer> masuRight = xIndex == getXLength() - 1 ? new ArrayList<Integer>() {
							{
								add(0);
							}
						} : masuCand[yIndex][xIndex + 1];
						ArrayList<Integer> masuDown = yIndex == getYLength() - 1 ? new ArrayList<Integer>() {
							{
								add(0);
							}
						} : masuCand[yIndex + 1][xIndex];
						ArrayList<Integer> masuLeft = xIndex == 0 ? new ArrayList<Integer>() {
							{
								add(0);
							}
						} : masuCand[yIndex][xIndex - 1];
						if (masuPivot.get(0) == 1) {
							// ○確定マス→周辺4マスに│か─が1つだけ必要、それ以外は空白。矛盾する場合アウト
							masuUp.remove(new Integer(1));
							masuUp.remove(new Integer(3));
							masuDown.remove(new Integer(1));
							masuDown.remove(new Integer(3));
							masuRight.remove(new Integer(1));
							masuRight.remove(new Integer(2));
							masuLeft.remove(new Integer(1));
							masuLeft.remove(new Integer(2));
							if (masuUp.isEmpty() || masuDown.isEmpty() || masuRight.isEmpty() || masuLeft.isEmpty()) {
								return false;
							}
							int blackFixed = 0;
							int blackCand = 0;
							if (masuUp.contains(2)) {
								blackCand++;
								if (masuUp.size() == 1) {
									blackFixed++;
								}
							}
							if (masuDown.contains(2)) {
								blackCand++;
								if (masuDown.size() == 1) {
									blackFixed++;
								}
							}
							if (masuRight.contains(3)) {
								blackCand++;
								if (masuRight.size() == 1) {
									blackFixed++;
								}
							}
							if (masuLeft.contains(3)) {
								blackCand++;
								if (masuLeft.size() == 1) {
									blackFixed++;
								}
							}
							if (blackCand < 1 || blackFixed > 1) {
								return false;
							}
							if (blackCand == 1) {
								if (masuUp.contains(2)) {
									masuUp.remove(new Integer(0));
								} else if (masuDown.contains(2)) {
									masuDown.remove(new Integer(0));
								} else if (masuRight.contains(3)) {
									masuRight.remove(new Integer(0));
								} else if (masuLeft.contains(3)) {
									masuLeft.remove(new Integer(0));
								}
							}
							if (blackFixed == 1) {
								if (masuUp.contains(2) && masuUp.size() == 1) {
									masuDown.remove(new Integer(2));
									masuRight.remove(new Integer(3));
									masuLeft.remove(new Integer(3));
								} else if (masuDown.contains(2) && masuDown.size() == 1) {
									masuUp.remove(new Integer(2));
									masuRight.remove(new Integer(3));
									masuLeft.remove(new Integer(3));
								} else if (masuRight.contains(3) && masuRight.size() == 1) {
									masuUp.remove(new Integer(2));
									masuDown.remove(new Integer(2));
									masuLeft.remove(new Integer(3));
								} else if (masuLeft.contains(3) && masuLeft.size() == 1) {
									masuUp.remove(new Integer(2));
									masuDown.remove(new Integer(2));
									masuRight.remove(new Integer(3));
								}
							}
						} else if (masuPivot.get(0) == 2) {
							// │確定→左右2マスは空白、上下2マスに○と空白が1つずつ。矛盾する場合アウト
							masuUp.remove(new Integer(2));
							masuUp.remove(new Integer(3));
							masuDown.remove(new Integer(2));
							masuDown.remove(new Integer(3));
							masuRight.remove(new Integer(1));
							masuRight.remove(new Integer(2));
							masuRight.remove(new Integer(3));
							masuLeft.remove(new Integer(1));
							masuLeft.remove(new Integer(2));
							masuLeft.remove(new Integer(3));
							if (masuUp.isEmpty() || masuDown.isEmpty() || masuRight.isEmpty() || masuLeft.isEmpty()) {
								return false;
							}
							int blackFixed = 0;
							int blackCand = 0;
							if (masuUp.contains(1)) {
								blackCand++;
								if (masuUp.size() == 1) {
									blackFixed++;
								}
							}
							if (masuDown.contains(1)) {
								blackCand++;
								if (masuDown.size() == 1) {
									blackFixed++;
								}
							}
							if (blackCand < 1 || blackFixed > 1) {
								return false;
							}
							if (blackCand == 1) {
								if (masuUp.contains(1)) {
									masuUp.remove(new Integer(0));
								} else if (masuDown.contains(1)) {
									masuDown.remove(new Integer(0));
								}
							}
							if (blackFixed == 1) {
								if (masuUp.contains(1) && masuUp.size() == 1) {
									masuDown.remove(new Integer(1));
								} else if (masuDown.contains(1) && masuDown.size() == 1) {
									masuUp.remove(new Integer(1));
								}
							}
						} else if (masuPivot.get(0) == 3) {
							// ─確定→上下2マスは空白、左右2マスに○と空白が1つずつ。矛盾する場合アウト
							masuUp.remove(new Integer(1));
							masuUp.remove(new Integer(2));
							masuUp.remove(new Integer(3));
							masuDown.remove(new Integer(1));
							masuDown.remove(new Integer(2));
							masuDown.remove(new Integer(3));
							masuRight.remove(new Integer(2));
							masuRight.remove(new Integer(3));
							masuLeft.remove(new Integer(2));
							masuLeft.remove(new Integer(3));
							if (masuUp.isEmpty() || masuDown.isEmpty() || masuRight.isEmpty() || masuLeft.isEmpty()) {
								return false;
							}
							int blackFixed = 0;
							int blackCand = 0;
							if (masuRight.contains(1)) {
								blackCand++;
								if (masuRight.size() == 1) {
									blackFixed++;
								}
							}
							if (masuLeft.contains(1)) {
								blackCand++;
								if (masuLeft.size() == 1) {
									blackFixed++;
								}
							}
							if (blackCand < 1 || blackFixed > 1) {
								return false;
							}
							if (blackCand == 1) {
								if (masuRight.contains(1)) {
									masuRight.remove(new Integer(0));
								} else if (masuLeft.contains(1)) {
									masuLeft.remove(new Integer(0));
								}
							}
							if (blackFixed == 1) {
								if (masuRight.contains(1) && masuRight.size() == 1) {
									masuLeft.remove(new Integer(1));
								} else if (masuLeft.contains(1) && masuLeft.size() == 1) {
									masuRight.remove(new Integer(1));
								}
							}
						}
					}
				}
			}
			return true;
		}

		// 確定した記号について、上下左右は別の記号が入るまで同じ記号が入れられない。矛盾する場合NG
		public boolean betweenSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int token = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 1行目から横方向に走査
					ArrayList<Integer> targetMasu = masuCand[yIndex][xIndex];
					if (targetMasu.size() == 1 && !targetMasu.contains(0)) {
						if (token == targetMasu.get(0)) {
							return false;
						} else {
							token = targetMasu.get(0);
						}
					} else if ((token == 1 && (targetMasu.contains(2) || targetMasu.contains(3)))
							|| (token == 2 && (targetMasu.contains(1) || targetMasu.contains(3)))
							|| (token == 3 && (targetMasu.contains(1) || targetMasu.contains(2)))) {
						token = 0;
					}
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int token = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					// 1列目から縦方向に走査
					ArrayList<Integer> targetMasu = masuCand[yIndex][xIndex];
					if (targetMasu.size() == 1 && !targetMasu.contains(0)) {
						if (token == targetMasu.get(0)) {
							return false;
						} else {
							token = targetMasu.get(0);
						}
					} else if ((token == 1 && (targetMasu.contains(2) || targetMasu.contains(3)))
							|| (token == 2 && (targetMasu.contains(1) || targetMasu.contains(3)))
							|| (token == 3 && (targetMasu.contains(1) || targetMasu.contains(2)))) {
						token = 0;
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roundSolve()) {
				return false;
			}
			if (!betweenSolve()) {
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
					if (masuCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public LollipopsSolver(Field field) {
		this.field = new Field(field);
	}

	public LollipopsSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?lollipops/8/8/1c2i3f1c2p3c1i2c1a3a"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new LollipopsSolver(height, width, param).solve());
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
		int level = (int) Math.sqrt(count * 5 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masuCand[yIndex][xIndex].size() != 1) {
					count++;
					for (Iterator<Integer> iterator = field.masuCand[yIndex][xIndex].iterator(); iterator.hasNext();) {
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.masuCand[yIndex][xIndex].clear();
						virtual.masuCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.masuCand[yIndex][xIndex].size() == 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
}