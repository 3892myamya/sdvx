package myamya.other.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.SolverWeb.MasyuSolverThread.UraMasyuSolverThread;
import myamya.other.solver.SolverWeb.NurimisakiSolverThread.TrimisakiSolverThread;
import myamya.other.solver.akari.AkariSolver;
import myamya.other.solver.bag.BagSolver;
import myamya.other.solver.barns.BarnsSolver;
import myamya.other.solver.country.CountrySolver;
import myamya.other.solver.dosufuwa.DosufuwaSolver;
import myamya.other.solver.fillomino.FillominoSolver;
import myamya.other.solver.firefly.FireflySolver;
import myamya.other.solver.firefly.FireflySolver.Firefly;
import myamya.other.solver.heyawake.HeyawakeSolver;
import myamya.other.solver.hitori.HitoriSolver;
import myamya.other.solver.kurodoko.KurodokoSolver;
import myamya.other.solver.kurodoko.KurodokoSolver.NumberMasu;
import myamya.other.solver.lits.LitsSolver;
import myamya.other.solver.loopsp.LoopspSolver;
import myamya.other.solver.masyu.MasyuSolver;
import myamya.other.solver.masyu.MasyuSolver.Pearl;
import myamya.other.solver.nagare.NagareSolver;
import myamya.other.solver.norinori.NorinoriSolver;
import myamya.other.solver.numlin.NumlinSolver;
import myamya.other.solver.nurikabe.NurikabeSolver;
import myamya.other.solver.nurimisaki.NurimisakiSolver;
import myamya.other.solver.nurimisaki.NurimisakiSolver.Misaki;
import myamya.other.solver.pipelink.PipelinkSolver;
import myamya.other.solver.reflect.ReflectSolver;
import myamya.other.solver.ringring.RingringSolver;
import myamya.other.solver.ripple.RippleSolver;
import myamya.other.solver.sashigane.SashiganeSolver;
import myamya.other.solver.sashigane.SashiganeSolver.Mark;
import myamya.other.solver.satogaeri.SatogaeriSolver;
import myamya.other.solver.shakashaka.ShakashakaSolver;
import myamya.other.solver.shikaku.ShikakuSolver;
import myamya.other.solver.shikaku.ShikakuSolver.Sikaku;
import myamya.other.solver.shimaguni.ShimaguniSolver;
import myamya.other.solver.slither.KurohouiSolver;
import myamya.other.solver.slither.SlitherSolver;
import myamya.other.solver.starbattle.StarBattleSolver;
import myamya.other.solver.stostone.StostoneSolver;
import myamya.other.solver.sudoku.SudokuSolver;
import myamya.other.solver.tapa.TapaSolver;
import myamya.other.solver.yajikazu.YajikazuSolver;
import myamya.other.solver.yajikazu.YajikazuSolver.Arrow;
import myamya.other.solver.yajilin.YajilinSolver;
import myamya.other.solver.yinyang.YinyangSolver;
import myamya.other.solver.yregions.YajilinRegionsSolver;
import net.arnx.jsonic.JSON;

/**
 * Servlet implementation class TestWeb
 */
@WebServlet("/SolverWeb")
public class SolverWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public SolverWeb() {
		super();
	}

	interface SolverThlead {
		public String makeCambus();
	}

	static abstract class AbsSolverThlead extends Thread implements SolverThlead {
		protected Solver solver;
		private String status = "時間切れです。途中経過を返します。";
		protected final int height;
		protected final int width;
		protected final String param;

		AbsSolverThlead(int height, int width, String param) {
			this.height = height;
			this.width = width;
			this.param = param;
		}

		abstract protected Solver getSolver();

		@Override
		public void run() {
			solver = getSolver();
			status = solver.solve();
		}

		public String getStatus() {
			return status;
		}
	}

	static class YajilinSolverThlead extends AbsSolverThlead {
		private final boolean out;

		YajilinSolverThlead(int height, int width, String param, boolean out) {
			super(height, width, param);
			this.out = out;
		}

		@Override
		protected Solver getSolver() {
			return new YajilinSolver(height, width, param, out);
		}

		@Override
		public String makeCambus() {
			YajilinSolver.Field field = ((YajilinSolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					YajilinSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					String str = "";
					if (oneMasu.toString().equals("・")) {
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toStringWeb();
						}
					} else {
						str = oneMasu.toStringWeb();
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NurikabeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NurikabeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NurikabeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NurikabeSolver.Field field = ((NurikabeSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else {
						String masuStr = null;
						if (field.getNumbers()[yIndex][xIndex] != null) {
							if (field.getNumbers()[yIndex][xIndex] == -1) {
								masuStr = "？";
							} else {
								String capacityStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
								int index = HALF_NUMS.indexOf(capacityStr);
								if (index >= 0) {
									masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
								} else {
									masuStr = capacityStr;
								}
							}
						} else {
							masuStr = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class StostoneSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		StostoneSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new StostoneSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			StostoneSolver.Field field = ((StostoneSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 数字描画
			for (StostoneSolver.Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white"
									: "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ fillColor
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HeyawakeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HeyawakeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HeyawakeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			HeyawakeSolver.Field field = ((HeyawakeSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 数字描画
			for (HeyawakeSolver.Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white"
									: "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ fillColor
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class LitsSolverThread extends AbsSolverThlead {
		LitsSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new LitsSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			LitsSolver.Field field = ((LitsSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NorinoriSolverThread extends AbsSolverThlead {
		NorinoriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NorinoriSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NorinoriSolver.Field field = ((NorinoriSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ShimaguniSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShimaguniSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ShimaguniSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ShimaguniSolver.Field field = ((ShimaguniSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 数字描画
			for (ShimaguniSolver.Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white"
									: "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ fillColor
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ShikakuSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShikakuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ShikakuSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ShikakuSolver.Field field = ((ShikakuSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			ShikakuSolver.Wall[][] yokoWall = new ShikakuSolver.Wall[field.getYLength()][field.getXLength() - 1];
			ShikakuSolver.Wall[][] tateWall = new ShikakuSolver.Wall[field.getYLength() - 1][field.getXLength()];
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = ShikakuSolver.Wall.NOT_EXISTS;
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = ShikakuSolver.Wall.NOT_EXISTS;
				}
			}
			for (Set<Sikaku> sikakuSet : field.getRoomCand().values()) {
				if (sikakuSet.size() == 1) {
					Sikaku sikaku = (Sikaku) sikakuSet.toArray()[0];
					for (int yIndex = sikaku.getLeftUp().getyIndex(); yIndex <= sikaku.getRightDown()
							.getyIndex(); yIndex++) {
						if (sikaku.getLeftUp().getxIndex() > 0) {
							yokoWall[yIndex][sikaku.getLeftUp().getxIndex() - 1] = ShikakuSolver.Wall.EXISTS;
						}
						if (sikaku.getRightDown().getxIndex() < field.getXLength() - 1) {
							yokoWall[yIndex][sikaku.getRightDown().getxIndex()] = ShikakuSolver.Wall.EXISTS;
						}
					}
					for (int xIndex = sikaku.getLeftUp().getxIndex(); xIndex <= sikaku.getRightDown()
							.getxIndex(); xIndex++) {
						if (sikaku.getLeftUp().getyIndex() > 0) {
							tateWall[sikaku.getLeftUp().getyIndex() - 1][xIndex] = ShikakuSolver.Wall.EXISTS;
						}
						if (sikaku.getRightDown().getyIndex() < field.getYLength() - 1) {
							tateWall[sikaku.getRightDown().getyIndex()][xIndex] = ShikakuSolver.Wall.EXISTS;
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| yokoWall[yIndex][xIndex] == ShikakuSolver.Wall.EXISTS;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| tateWall[yIndex][xIndex] == ShikakuSolver.Wall.EXISTS;
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
			// 数字描画
			for (ShikakuSolver.Room room : field.getRooms()) {
				int roomCapacity = room.getCapacity();
				if (roomCapacity != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomCapacity);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getPivot();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5 + margin)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class AkariSolverThread extends AbsSolverThlead {
		AkariSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new AkariSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			AkariSolver.Field field = ((AkariSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					AkariSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.isWall()) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
						if (oneMasu.getCnt() != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneMasu.toString()
									+ "</text>");
						}
					} else if (oneMasu == AkariSolver.Masu.AKARI) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NurimisakiSolverThread extends AbsSolverThlead {

		static class TrimisakiSolverThread extends NurimisakiSolverThread {
			TrimisakiSolverThread(int height, int width, String param) {
				super(height, width, param);
			}

			@Override
			protected Solver getSolver() {
				return new NurimisakiSolver(height, width, param, true);
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NurimisakiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NurimisakiSolver(height, width, param, false);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NurimisakiSolver.Field field = ((NurimisakiSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					NurimisakiSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else if (oneMasu instanceof Misaki) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (((Misaki) oneMasu).getCnt() != -1) {
							String numberStr = String.valueOf(((Misaki) oneMasu).getCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
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

					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HitoriSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HitoriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HitoriSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			HitoriSolver.Field field = ((HitoriSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else {
						if (oneMasu.toString().equals("・")) {
							sb.append("<rect y=\"" + (yIndex * baseSize)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" width=\""
									+ (baseSize)
									+ "\" height=\""
									+ (baseSize)
									+ "\" fill=\"lime\" >"
									+ "</rect>");
						}
						String numberStr = String.valueOf(field.numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class KurodokoSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KurodokoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KurodokoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			KurodokoSolver.Field field = ((KurodokoSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					KurodokoSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else if (oneMasu instanceof NumberMasu) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (((NumberMasu) oneMasu).getCnt() != -1) {
							String numberStr = String.valueOf(((NumberMasu) oneMasu).getCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
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

					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class DosufuwaSolverThread extends AbsSolverThlead {
		DosufuwaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new DosufuwaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			DosufuwaSolver.Field field = ((DosufuwaSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					DosufuwaSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else if (oneMasu.toString().equals("○")) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");

					} else if (oneMasu.toString().equals("●")) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");

					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class YinyangSolverThread extends AbsSolverThlead {
		YinyangSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new YinyangSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			YinyangSolver.Field field = ((YinyangSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getFixedPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					YinyangSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu == YinyangSolver.Masu.WHITE) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");

					} else if (oneMasu == YinyangSolver.Masu.BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SlitherSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SlitherSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SlitherSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			SlitherSolver.Field field = ((SlitherSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
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
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					boolean oneYokoWall = field.getYokoExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<line y1=\""
								+ (yIndex * baseSize + margin)
								+ "\" x1=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize)
								+ "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = field.getTateExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<line y1=\""
								+ (yIndex * baseSize + margin)
								+ "\" x1=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" y2=\""
								+ (yIndex * baseSize + margin)
								+ "\" x2=\""
								+ (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class KurohouiSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KurohouiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KurohouiSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			KurohouiSolver.Field field = ((KurohouiSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + ((field.getYLength() + 1) * baseSize + 2 * baseSize) + "\" width=\""
							+ ((field.getXLength() + 1) * baseSize + 2 * baseSize) + "\" >");
			sb.append("<rect y=\"" + 0
					+ "\" x=\""
					+ (baseSize)
					+ "\" width=\""
					+ (baseSize)
					+ "\" height=\""
					+ (baseSize)
					+ "\" fill=\"gray\" >"
					+ "</rect>");
			sb.append("<rect y=\"" + (1)
					+ "\" x=\""
					+ (baseSize + 1)
					+ "\" width=\""
					+ (baseSize - 2)
					+ "\" height=\""
					+ (baseSize - 2)
					+ "\">"
					+ "</rect>");
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				sb.append("<rect y=\"" + 0
						+ "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize)
						+ "\" width=\""
						+ (baseSize)
						+ "\" height=\""
						+ (baseSize)
						+ "\" fill=\"gray\" >"
						+ "</rect>");
				sb.append("<rect y=\"" + (1)
						+ "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize + 1)
						+ "\" width=\""
						+ (baseSize - 2)
						+ "\" height=\""
						+ (baseSize - 2)
						+ "\">"
						+ "</rect>");
			}
			sb.append("<rect y=\"" + 0
					+ "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize)
					+ "\" width=\""
					+ (baseSize)
					+ "\" height=\""
					+ (baseSize)
					+ "\" fill=\"gray\" >"
					+ "</rect>");
			sb.append("<rect y=\"" + (1)
					+ "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize + 1)
					+ "\" width=\""
					+ (baseSize - 2)
					+ "\" height=\""
					+ (baseSize - 2)
					+ "\">"
					+ "</rect>");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				sb.append("<rect y=\"" + (yIndex + 1) * baseSize
						+ "\" x=\""
						+ (baseSize)
						+ "\" width=\""
						+ (baseSize)
						+ "\" height=\""
						+ (baseSize)
						+ "\" fill=\"gray\" >"
						+ "</rect>");
				sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1)
						+ "\" x=\""
						+ (baseSize + 1)
						+ "\" width=\""
						+ (baseSize - 2)
						+ "\" height=\""
						+ (baseSize - 2)
						+ "\">"
						+ "</rect>");
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1)
								+ "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize + 1)
								+ "\" width=\""
								+ (baseSize - 2)
								+ "\" height=\""
								+ (baseSize - 2)
								+ "\">"
								+ "</rect>");
					} else {
						sb.append("<text y=\"" + ((yIndex + 1) * baseSize + baseSize - 4)
								+ "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
				sb.append("<rect y=\"" + (yIndex + 1) * baseSize
						+ "\" x=\""
						+ ((field.getXLength() + 1) * baseSize + baseSize)
						+ "\" width=\""
						+ (baseSize)
						+ "\" height=\""
						+ (baseSize)
						+ "\" fill=\"gray\" >"
						+ "</rect>");
				sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1)
						+ "\" x=\""
						+ ((field.getXLength() + 1) * baseSize + baseSize + 1)
						+ "\" width=\""
						+ (baseSize - 2)
						+ "\" height=\""
						+ (baseSize - 2)
						+ "\">"
						+ "</rect>");
			}
			sb.append("<rect y=\"" + (field.getYLength() + 1) * baseSize
					+ "\" x=\""
					+ (baseSize)
					+ "\" width=\""
					+ (baseSize)
					+ "\" height=\""
					+ (baseSize)
					+ "\" fill=\"gray\" >"
					+ "</rect>");
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1)
					+ "\" x=\""
					+ (baseSize + 1)
					+ "\" width=\""
					+ (baseSize - 2)
					+ "\" height=\""
					+ (baseSize - 2)
					+ "\">"
					+ "</rect>");
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				sb.append("<rect y=\"" + (field.getYLength() + 1) * baseSize
						+ "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize)
						+ "\" width=\""
						+ (baseSize)
						+ "\" height=\""
						+ (baseSize)
						+ "\" fill=\"gray\" >"
						+ "</rect>");
				sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1)
						+ "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize + 1)
						+ "\" width=\""
						+ (baseSize - 2)
						+ "\" height=\""
						+ (baseSize - 2)
						+ "\">"
						+ "</rect>");
			}
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize)
					+ "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize)
					+ "\" width=\""
					+ (baseSize)
					+ "\" height=\""
					+ (baseSize)
					+ "\" fill=\"gray\" >"
					+ "</rect>");
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1)
					+ "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize + 1)
					+ "\" width=\""
					+ (baseSize - 2)
					+ "\" height=\""
					+ (baseSize - 2)
					+ "\">"
					+ "</rect>");
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					int number = field.getNumbers()[yIndex][xIndex];
					if (number != -1) {
						String numberStr;
						String wkstr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(wkstr);
						if (index >= 0) {
							numberStr = FULL_NUMS.substring(index / 2,
									index / 2 + 1);
						} else {
							numberStr = wkstr;
						}
						String fillColor = field.getMasu()[yIndex][xIndex] == Common.Masu.BLACK ? "white"
								: "black";
						sb.append("<text y=\"" + ((yIndex + 1) * baseSize + baseSize - 5)
								+ "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize + 2)
								+ "\" fill=\""
								+ fillColor
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ numberStr
								+ "</text>");

					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class YajikazuSolverThread extends AbsSolverThlead {
		YajikazuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new YajikazuSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			YajikazuSolver.Field field = ((YajikazuSolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					Arrow oneArrow = field.getArrows()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
						if (oneArrow != null) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" font-size=\""
									+ (baseSize - 2)
									+ "\" fill=\""
									+ "gray"
									+ "\" textLength=\""
									+ (baseSize - 2)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneArrow.toStringForweb()
									+ "</text>");

						}
					} else {
						if (oneArrow != null) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" font-size=\""
									+ (baseSize - 2)
									+ "\" textLength=\""
									+ (baseSize - 2)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneArrow.toStringForweb()
									+ "</text>");

						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" fill=\""
								+ "lime"
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString()
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MasyuSolverThread extends AbsSolverThlead {

		static class UraMasyuSolverThread extends MasyuSolverThread {
			UraMasyuSolverThread(int height, int width, String param) {
				super(height, width, param);
			}

			@Override
			protected Solver getSolver() {
				return new MasyuSolver(height, width, param, true);
			}
		}

		MasyuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MasyuSolver(height, width, param, false);
		}

		@Override
		public String makeCambus() {
			MasyuSolver.Field field = ((MasyuSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Pearl onePearl = field.getPearl()[yIndex][xIndex];
					if (onePearl == Pearl.SIRO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");

					} else if (onePearl == Pearl.KURO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 3)
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS
								: field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SashiganeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SashiganeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SashiganeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			SashiganeSolver.Field field = ((SashiganeSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Mark oneMark = field.getMark()[yIndex][xIndex];
					if (oneMark instanceof SashiganeSolver.Circle) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (((SashiganeSolver.Circle) oneMark).getCnt() != -1) {
							String numberStr = String.valueOf(((SashiganeSolver.Circle) oneMark).getCnt());
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
					} else if (oneMark != null) {
						int lengthAdjust = 0;
						if (oneMark instanceof SashiganeSolver.Arrow
								&& (((SashiganeSolver.Arrow) oneMark).getDirection() == Direction.UP
										|| ((SashiganeSolver.Arrow) oneMark).getDirection() == Direction.DOWN)) {
							lengthAdjust = 4;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + (lengthAdjust / 2))
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2 - lengthAdjust)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMark.toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
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
			return sb.toString();
		}
	}

	static class BagSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		BagSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BagSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			BagSolver.Field field = ((BagSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			Wall[][] yokoWall = new Wall[field.getYLength()][field.getXLength() - 1];
			Wall[][] tateWall = new Wall[field.getYLength() - 1][field.getXLength()];
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = (xIndex == -1 && field.getMasu()[yIndex][0].toString().equals("・"))
							||
							(xIndex == field.getXLength() - 1
									&& field.getMasu()[yIndex][field.getXLength() - 1].toString().equals("・"))
							||
							(xIndex != -1 && xIndex != field.getXLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("・")
									&& field.getMasu()[yIndex][xIndex + 1].toString().equals("■"))
							||
							(xIndex != -1 && xIndex != field.getXLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("■")
									&& field.getMasu()[yIndex][xIndex + 1].toString().equals("・"));
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
						sb.append("stroke=\"#000\" ");
						sb.append(">"
								+ "</line>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = (yIndex == -1 && field.getMasu()[0][xIndex].toString().equals("・"))
							||
							(yIndex == field.getYLength() - 1
									&& field.getMasu()[field.getYLength() - 1][xIndex].toString().equals("・"))
							||
							(yIndex != -1 && yIndex != field.getYLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("・")
									&& field.getMasu()[yIndex + 1][xIndex].toString().equals("■"))
							||
							(yIndex != -1 && yIndex != field.getYLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("■")
									&& field.getMasu()[yIndex + 1][xIndex].toString().equals("・"));
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
						sb.append("stroke=\"#000\" ");
						sb.append(">"
								+ "</line>");
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr;
						String wkstr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(wkstr);
						if (index >= 0) {
							numberStr = FULL_NUMS.substring(index / 2,
									index / 2 + 1);
						} else {
							numberStr = wkstr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ numberStr
								+ "</text>");

					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class TapaSolverThread extends AbsSolverThlead {
		private static final String FULL_NUMS = "０１２３４５６７８９";

		TapaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TapaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			TapaSolver.Field field = ((TapaSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					} else {
						if (field.getNumbers()[yIndex][xIndex] != null) {
							if (field.getNumbers()[yIndex][xIndex].size() == 1) {
								String masuStr = null;
								if (field.getNumbers()[yIndex][xIndex].get(0) == -1) {
									masuStr = "？";
								} else {
									masuStr = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(0),
											field.getNumbers()[yIndex][xIndex].get(0) + 1);
								}
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" font-size=\""
										+ (baseSize - 2)
										+ "\" textLength=\""
										+ (baseSize - 2)
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr
										+ "</text>");
							} else if (field.getNumbers()[yIndex][xIndex].size() == 2) {
								String masuStr1 = null;
								if (field.getNumbers()[yIndex][xIndex].get(0) == -1) {
									masuStr1 = "？";
								} else {
									masuStr1 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(0),
											field.getNumbers()[yIndex][xIndex].get(0) + 1);
								}
								String masuStr2 = null;
								if (field.getNumbers()[yIndex][xIndex].get(1) == -1) {
									masuStr2 = "？";
								} else {
									masuStr2 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(1),
											field.getNumbers()[yIndex][xIndex].get(1) + 1);
								}
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr1
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2))
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr2
										+ "</text>");
							} else if (field.getNumbers()[yIndex][xIndex].size() == 3) {
								String masuStr1 = null;
								if (field.getNumbers()[yIndex][xIndex].get(0) == -1) {
									masuStr1 = "？";
								} else {
									masuStr1 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(0),
											field.getNumbers()[yIndex][xIndex].get(0) + 1);
								}
								String masuStr2 = null;
								if (field.getNumbers()[yIndex][xIndex].get(1) == -1) {
									masuStr2 = "？";
								} else {
									masuStr2 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(1),
											field.getNumbers()[yIndex][xIndex].get(1) + 1);
								}
								String masuStr3 = null;
								if (field.getNumbers()[yIndex][xIndex].get(2) == -1) {
									masuStr3 = "？";
								} else {
									masuStr3 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(2),
											field.getNumbers()[yIndex][xIndex].get(2) + 1);
								}
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr1
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4))
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr2
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2))
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr3
										+ "</text>");
							} else if (field.getNumbers()[yIndex][xIndex].size() == 4) {
								String masuStr1 = null;
								if (field.getNumbers()[yIndex][xIndex].get(0) == -1) {
									masuStr1 = "？";
								} else {
									masuStr1 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(0),
											field.getNumbers()[yIndex][xIndex].get(0) + 1);
								}
								String masuStr2 = null;
								if (field.getNumbers()[yIndex][xIndex].get(1) == -1) {
									masuStr2 = "？";
								} else {
									masuStr2 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(1),
											field.getNumbers()[yIndex][xIndex].get(1) + 1);
								}
								String masuStr3 = null;
								if (field.getNumbers()[yIndex][xIndex].get(2) == -1) {
									masuStr3 = "？";
								} else {
									masuStr3 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(2),
											field.getNumbers()[yIndex][xIndex].get(2) + 1);
								}
								String masuStr4 = null;
								if (field.getNumbers()[yIndex][xIndex].get(3) == -1) {
									masuStr4 = "？";
								} else {
									masuStr4 = FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex].get(3),
											field.getNumbers()[yIndex][xIndex].get(3) + 1);
								}
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 3 / 4) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr1
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4))
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr2
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4))
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr3
										+ "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 3 / 4) - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2))
										+ "\" font-size=\""
										+ (baseSize) / 2
										+ "\" textLength=\""
										+ (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr4
										+ "</text>");
							}
						} else {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" font-size=\""
									+ (baseSize - 2)
									+ "\" textLength=\""
									+ (baseSize - 2)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneMasu.toString()
									+ "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ShakashakaSolverThread extends AbsSolverThlead {
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShakashakaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ShakashakaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ShakashakaSolver.Field field = ((ShakashakaSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			sb.append("<line y1=\""
					+ (margin)
					+ "\" x1=\""
					+ (baseSize)
					+ "\" y2=\""
					+ (margin)
					+ "\" x2=\""
					+ (field.getXLength() * baseSize + baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">"
					+ "</line>");

			sb.append("<line y1=\""
					+ (field.getYLength() * baseSize + margin)
					+ "\" x1=\""
					+ (baseSize)
					+ "\" y2=\""
					+ (field.getYLength() * baseSize + margin)
					+ "\" x2=\""
					+ (field.getXLength() * baseSize + baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">"
					+ "</line>");

			sb.append("<line y1=\""
					+ (margin)
					+ "\" x1=\""
					+ (baseSize)
					+ "\" y2=\""
					+ (field.getYLength() * baseSize + margin)
					+ "\" x2=\""
					+ (baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">"
					+ "</line>");

			sb.append("<line y1=\""
					+ (margin)
					+ "\" x1=\""
					+ (field.getXLength() * baseSize + baseSize)
					+ "\" y2=\""
					+ (field.getYLength() * baseSize + margin)
					+ "\" x2=\""
					+ (field.getXLength() * baseSize + baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">"
					+ "</line>");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex],
											field.getNumbers()[yIndex][xIndex] + 1)
									+ "</text>");
						}
					} else {
						Masu oneMasu = field.getMasu()[yIndex][xIndex];
						if (oneMasu == Masu.BLACK) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" font-size=\""
									+ (baseSize - 2)
									+ "\" textLength=\""
									+ (baseSize - 2)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ "・"
									+ "</text>");
						} else if (oneMasu == Masu.NOT_BLACK) {
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" font-size=\""
										+ (baseSize - 2)
										+ "\" textLength=\""
										+ (baseSize - 2)
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ "・"
										+ "</text>");
							} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.EXISTS && left == Wall.EXISTS) {
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
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
									&& down == Wall.EXISTS && left == Wall.NOT_EXISTS) {
								sb.append("<path d=\"M "
										+ (xIndex * baseSize + baseSize + baseSize)
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
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.NOT_EXISTS && left == Wall.EXISTS) {
								sb.append("<path d=\"M "
										+ (xIndex * baseSize + baseSize)
										+ " "
										+ (yIndex * baseSize + margin)
										+ " L"
										+ (xIndex * baseSize + baseSize + baseSize)
										+ " "
										+ (yIndex * baseSize + margin)
										+ " L"
										+ (xIndex * baseSize + baseSize)
										+ " "
										+ (yIndex * baseSize + baseSize + margin)
										+ " Z\" >"
										+ "</path>");
							} else if (up == Wall.EXISTS && right == Wall.EXISTS
									&& down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
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
							}
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NumlinSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NumlinSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NumlinSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			NumlinSolver.Field field = ((NumlinSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getNumbers()[yIndex][xIndex];
					String str = "";
					if (number != null) {
						if (number == -1) {
							str = "？";
						} else {
							String numberStr = String.valueOf(number);
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								str = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								str = numberStr;
							}
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 3)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 1)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					} else {
						Wall up = yIndex == 0 ? Wall.EXISTS
								: field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┐";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class StarBattleSolverThread extends AbsSolverThlead {
		private final int starCnt;

		StarBattleSolverThread(int height, int width, int starCnt, String param) {
			super(height, width, param);
			this.starCnt = starCnt;
		}

		@Override
		protected Solver getSolver() {
			return new StarBattleSolver(height, width, starCnt, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			StarBattleSolver.Field field = ((StarBattleSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize - 2)
							+ "\" fill=\""
							+ "green"
							+ "\" textLength=\""
							+ (baseSize - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ (oneMasu.toString().equals("■") ? "★" : oneMasu.toString())
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SudokuSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SudokuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SudokuSolver(height, width, param, true) {
				public String solve() {
					String result = super.solve();
					if (result.contains("解けました")) {
						int level = (int) Math.sqrt(count) - 10;
						level = level < 1 ? 1 : level;
						String difficulty = level <= 4 ? "らくらく"
								: level <= 14 ? "おてごろ" : level <= 39 ? "たいへん" : level <= 99 ? "アゼン" : "ハバネロ";
						return "解けました。推定難易度:" + difficulty + "(Lv:" + level + ")";
					} else {
						return result;
					}
				}
			};
		}

		@Override
		public String makeCambus() {
			SudokuSolver.Field field = ((SudokuSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
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

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| xIndex % field.getRoomWidth() == field.getRoomWidth() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| yIndex % field.getRoomHeight() == field.getRoomHeight() - 1;
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
			return sb.toString();
		}
	}

	static class CountrySolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		CountrySolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new CountrySolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			CountrySolver.Field field = ((CountrySolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS
								: field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "green"
								+ "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 数字描画
			for (CountrySolver.Room room : field.getRooms()) {
				int roomWhiteCount = room.getWhiteCnt();
				if (roomWhiteCount != -1) {
					String roomWhiteCountStr;
					String wkstr = String.valueOf(roomWhiteCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomWhiteCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomWhiteCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5 + margin)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ "black"
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomWhiteCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class FillominoSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		FillominoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new FillominoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			FillominoSolver.Field field = ((FillominoSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getOriginNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
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
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NagareSolverThread extends AbsSolverThlead {

		NagareSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NagareSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NagareSolver.Field field = ((NagareSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];

					if (oneMasu.toString().equals("■")) {
						if (field.getBlockPosSet().contains(new Position(yIndex, xIndex))) {
							sb.append("<rect y=\"" + (yIndex * baseSize)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" width=\""
									+ (baseSize)
									+ "\" height=\""
									+ (baseSize)
									+ "\">"
									+ "</rect>");
							if (field.getDirection()[yIndex][xIndex] != null) {
								int lengthAdjust = 0;
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + -4)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (lengthAdjust / 2))
										+ "\" font-size=\""
										+ (baseSize - 2)
										+ "\" textLength=\""
										+ (baseSize - 2 - lengthAdjust)
										+ "\" fill=\""
										+ "white"
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ field.getDirection()[yIndex][xIndex].getTriangle()
										+ "</text>");
							}
						}
					} else {
						if (oneMasu.toString().equals("・")) {
							if (field.getDirection()[yIndex][xIndex] != null) {
								int lengthAdjust = 0;
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + -4)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (lengthAdjust / 2))
										+ "\" font-size=\""
										+ (baseSize - 2)
										+ "\" textLength=\""
										+ (baseSize - 2 - lengthAdjust)
										+ "\" fill=\""
										+ "black"
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ field.getDirection()[yIndex][xIndex].getTriangle()
										+ "</text>");
							}
							String str = "";
							Wall up = yIndex == 0 ? Wall.EXISTS
									: field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = oneMasu.toString();
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" font-size=\""
									+ (baseSize)
									+ "\" textLength=\""
									+ (baseSize)
									+ "\" fill=\""
									+ "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ str
									+ "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class RippleSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		RippleSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new RippleSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			RippleSolver.Field field = ((RippleSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
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
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class YajilinRegionsSolverThlead extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final boolean out;

		YajilinRegionsSolverThlead(int height, int width, String param, boolean out) {
			super(height, width, param);
			this.out = out;
		}

		@Override
		protected Solver getSolver() {
			return new YajilinRegionsSolver(height, width, param, out);
		}

		@Override
		public String makeCambus() {
			YajilinRegionsSolver.Field field = ((YajilinRegionsSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS
								: field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "green"
								+ "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					} else if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin + 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" width=\""
								+ (baseSize - 4)
								+ "\" height=\""
								+ (baseSize - 4)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 数字描画
			for (YajilinRegionsSolver.Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2,
								index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white"
									: "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 5)
							+ "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ fillColor
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SatogaeriSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SatogaeriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SatogaeriSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SatogaeriSolver.Field field = ((SatogaeriSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (Entry<Position, Set<Position>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					Position movedPos = new ArrayList<>(entry.getValue()).get(0);
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\""
							+ (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2))
							+ "\" r=\""
							+ (baseSize / 2 - 2)
							+ "\" fill=\"gray\", stroke=\"gray\">"
							+ "</circle>");
					sb.append("<circle cy=\"" + (movedPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\""
							+ (movedPos.getxIndex() * baseSize + baseSize + (baseSize / 2))
							+ "\" r=\""
							+ (baseSize / 2 - 2)
							+ "\" fill=\"lightgray\", stroke=\"black\">"
							+ "</circle>");
					if (movedPos.getyIndex() < originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex > movedPos.getyIndex(); yIndex--) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize - (yIndex == movedPos.getyIndex() + 1 ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\""
									+ (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											- (yIndex == originPos.getyIndex() ? baseSize / 2 : 0) + margin)
									+ "\" x2=\""
									+ (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">"
									+ "</line>");
						}
					} else if (movedPos.getxIndex() > originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex < movedPos.getxIndex(); xIndex++) {
							sb.append("<line y1=\""
									+ (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											+ (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" y2=\""
									+ (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											+ (xIndex == movedPos.getxIndex() - 1 ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">"
									+ "</line>");
						}
					} else if (movedPos.getyIndex() > originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex < movedPos.getyIndex(); yIndex++) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + (yIndex == originPos.getyIndex() ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\""
									+ (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											+ (yIndex == movedPos.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
									+ "\" x2=\""
									+ (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">"
									+ "</line>");
						}
					} else if (movedPos.getxIndex() < originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex > movedPos.getxIndex(); xIndex--) {
							sb.append("<line y1=\""
									+ (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											- (xIndex == movedPos.getxIndex() + 1 ? baseSize / 2 : 0))
									+ "\" y2=\""
									+ (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											- (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">"
									+ "</line>");
						}
					}
					if (field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] != -1) {
						String numberStr = String
								.valueOf(field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (movedPos.getyIndex() * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (movedPos.getxIndex() * baseSize + baseSize + 2)
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
					}
				} else {
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\""
							+ (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2))
							+ "\" r=\""
							+ (baseSize / 2 - 2)
							+ "\" fill=\"white\", stroke=\"black\">"
							+ "</circle>");
					if (field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] != -1) {
						String numberStr = String
								.valueOf(field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (originPos.getyIndex() * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (originPos.getxIndex() * baseSize + baseSize + 2)
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

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getRoomYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getRoomTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class FireflySolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		FireflySolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new FireflySolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			FireflySolver.Field field = ((FireflySolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Firefly firefly = field.getFireflies()[yIndex][xIndex];
					String str = "";
					if (firefly != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (firefly.getDirection() == Direction.UP) {
							sb.append("<circle cy=\"" + (yIndex * baseSize)
									+ "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2))
									+ "\" r=\""
									+ 2
									+ "\" fill=\"black\", stroke=\"black\">"
									+ "</circle>");
						} else if (firefly.getDirection() == Direction.RIGHT) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
									+ "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize)
									+ "\" r=\""
									+ 2
									+ "\" fill=\"black\", stroke=\"black\">"
									+ "</circle>");
						} else if (firefly.getDirection() == Direction.DOWN) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize)
									+ "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2))
									+ "\" r=\""
									+ 2
									+ "\" fill=\"black\", stroke=\"black\">"
									+ "</circle>");
						} else if (firefly.getDirection() == Direction.LEFT) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
									+ "\" cx=\""
									+ (xIndex * baseSize + baseSize)
									+ "\" r=\""
									+ 2
									+ "\" fill=\"black\", stroke=\"black\">"
									+ "</circle>");
						}
						if (firefly.getCount() != -1) {
							String numberStr = String.valueOf(firefly.getCount());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								str = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ str
									+ "</text>");
						}
					} else {
						if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
							Wall up = yIndex == 0 ? Wall.EXISTS
									: field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
									&& down == Wall.EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS
									&& down == Wall.NOT_EXISTS &&
									left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = "？";
							}
						} else if (field.getMasu()[yIndex][xIndex] == Masu.SPACE) {
							str = "？";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ (str.equals("？") ? "black"
										: "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class BarnsSolverThread extends AbsSolverThlead {

		BarnsSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BarnsSolver(height, width, param, true);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			BarnsSolver.Field field = ((BarnsSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircle()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
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
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ "green"
							+ "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getFirstYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getFirstTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class LoopspSolverThread extends AbsSolverThlead {

		LoopspSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new LoopspSolver(height, width, param);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			LoopspSolver.Field field = ((LoopspSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ fillColor
							+ "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4)
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
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class PipelinkrSolverThread extends AbsSolverThlead {

		PipelinkrSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BarnsSolver(height, width, param, false);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			BarnsSolver.Field field = ((BarnsSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircle()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ fillColor
							+ "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getFirstYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getFirstTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class PipelinkSolverThread extends AbsSolverThlead {

		PipelinkSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new PipelinkSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			PipelinkSolver.Field field = ((PipelinkSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ fillColor
							+ "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ReflectSolverThread extends AbsSolverThlead {

		ReflectSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ReflectSolver(height, width, param);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ReflectSolver.Field field = ((ReflectSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String masuStr = null;
						if (field.getNumbers()[yIndex][xIndex] > 0) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						}
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS && left == Wall.EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1)
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
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS && left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1)
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
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
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
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1)
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
						}
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getCrossPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ fillColor
							+ "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class RingringSolverThread extends AbsSolverThlead {

		RingringSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new RingringSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			RingringSolver.Field field = ((RingringSolver) solver).getField();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "black"
								+ "\">"
								+ "</rect>");
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS
							: field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
							: field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS
							: field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┐";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
							&& down == Wall.NOT_EXISTS &&
							left == Wall.NOT_EXISTS) {
						str = "┼";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" fill=\""
							+ "green"
							+ "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ str
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<String> parts = getURLparts(request.getParameter("url"));
			int height = Integer.parseInt(parts.get(2));
			int width = Integer.parseInt(parts.get(1));
			String puzzleType = parts.get(0);
			String param = parts.get(3).split("@")[0];
			AbsSolverThlead t;
			if (puzzleType.contains("yajilin") || puzzleType.contains("yajirin")) {
				if (puzzleType.contains("regions")) {
					t = new YajilinRegionsSolverThlead(height, width, param, puzzleType.contains("out"));
				} else {
					t = new YajilinSolverThlead(height, width, param, puzzleType.contains("out"));
				}
			} else if (puzzleType.contains("nurikabe")) {
				t = new NurikabeSolverThread(height, width, param);
			} else if (puzzleType.contains("stostone")) {
				t = new StostoneSolverThread(height, width, param);
			} else if (puzzleType.contains("heyawake")) {
				t = new HeyawakeSolverThread(height, width, param);
			} else if (puzzleType.contains("lits")) {
				t = new LitsSolverThread(height, width, param);
			} else if (puzzleType.contains("norinori")) {
				t = new NorinoriSolverThread(height, width, param);
			} else if (puzzleType.contains("shimaguni")) {
				t = new ShimaguniSolverThread(height, width, param);
			} else if (puzzleType.contains("shikaku")) {
				t = new ShikakuSolverThread(height, width, param);
			} else if (puzzleType.contains("akari") || puzzleType.contains("lightup")) {
				t = new AkariSolverThread(height, width, param);
			} else if (puzzleType.contains("yinyang")) {
				t = new YinyangSolverThread(height, width, param);
			} else if (puzzleType.contains("nurimisaki")) {
				t = new NurimisakiSolverThread(height, width, param);
			} else if (puzzleType.contains("trimisaki")) {
				t = new TrimisakiSolverThread(height, width, param);
			} else if (puzzleType.contains("hitori")) {
				t = new HitoriSolverThread(height, width, param);
			} else if (puzzleType.contains("dosufuwa")) {
				t = new DosufuwaSolverThread(height, width, param);
			} else if (puzzleType.contains("kurodoko")) {
				t = new KurodokoSolverThread(height, width, param);
			} else if (puzzleType.contains("slither")) {
				t = new SlitherSolverThread(height, width, param);
			} else if (puzzleType.contains("kurohoui")) {
				t = new KurohouiSolverThread(height, width, param);
			} else if (puzzleType.contains("yajikazu")) {
				t = new YajikazuSolverThread(height, width, param);
			} else if (puzzleType.contains("mashu") || puzzleType.contains("masyu")
					|| puzzleType.contains("pearl")) {
				t = puzzleType.contains("ura") ? new UraMasyuSolverThread(height, width, param)
						: new MasyuSolverThread(height, width, param);
			} else if (puzzleType.contains("sashigane")) {
				t = new SashiganeSolverThread(height, width, param);
			} else if (puzzleType.contains("bag")) {
				t = new BagSolverThread(height, width, param);
			} else if (puzzleType.contains("shakashaka")) {
				t = new ShakashakaSolverThread(height, width, param);
			} else if (puzzleType.contains("tapa")) {
				t = new TapaSolverThread(height, width, param);
			} else if (puzzleType.contains("starbattle")) {
				int starCnt = Integer.parseInt(parts.get(3));
				param = parts.get(4).split("@")[0];
				t = new StarBattleSolverThread(height, width, starCnt, param);
			} else if (puzzleType.contains("sudoku")) {
				t = new SudokuSolverThread(height, width, param);
			} else if (puzzleType.contains("country")) {
				t = new CountrySolverThread(height, width, param);
			} else if (puzzleType.contains("fillomino")) {
				t = new FillominoSolverThread(height, width, param);
			} else if (puzzleType.contains("firefly")) {
				t = new FireflySolverThread(height, width, param);
			} else if (puzzleType.contains("nagare")) {
				t = new NagareSolverThread(height, width, param);
			} else if (puzzleType.contains("ripple") || puzzleType.contains("hakyukoka")) {
				t = new RippleSolverThread(height, width, param);
			} else if (puzzleType.contains("sato")) {
				t = new SatogaeriSolverThread(height, width, param);
			} else if (puzzleType.contains("barns")) {
				t = new BarnsSolverThread(height, width, param);
			} else if (puzzleType.contains("loopsp")) {
				t = new LoopspSolverThread(height, width, param);
			} else if (puzzleType.contains("pipelink")) {
				if (puzzleType.contains("pipelinkr")) {
					t = new PipelinkrSolverThread(height, width, param);
				} else {
					t = new PipelinkSolverThread(height, width, param);
				}
			} else if (puzzleType.contains("reflect")) {
				t = new ReflectSolverThread(height, width, param);
			} else if (puzzleType.contains("ringring")) {
				t = new RingringSolverThread(height, width, param);
				//			} else if (puzzleType.contains("numlin") || puzzleType.contains("numberlink")) {
				//				t = new NumlinSolverThread(height, width, param);
			} else {
				throw new IllegalArgumentException();
			}
			t.start();
			t.join(28000);
			resultMap.put("result", t.makeCambus());
			resultMap.put("status", t.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", "");
			resultMap.put("status", "エラーが発生しました。urlを確認してください。短縮URLは認識できない場合があります。");
		}
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

	private final String SCRAPING_TARGET = "<iframe src=\"";

	private List<String> getURLparts(String urlStr) throws ProtocolException, IOException {
		List<String> parts = Arrays.asList(urlStr.split("/"));
		if (parts.get(2).equals("puzsq.sakura.ne.jp")) {
			// URLがパズルスクエアだったら、スクレイピングしてぱずぷれのURLを抽出
			URL url = new URL(urlStr);
			try (BufferedReader reader = getReader(url)) {
				String line;
				while ((line = reader.readLine()) != null) {
					int idx = line.indexOf(SCRAPING_TARGET);
					if (idx != -1) {
						String pzprURLStr = line.substring(idx + SCRAPING_TARGET.length()).split("\"")[0];
						return getURLparts(pzprURLStr);
					}
				}
				throw new IllegalStateException();
			}
		} else {
			List<String> puzpreParts = Arrays.asList(urlStr.split("\\?"));
			puzpreParts = Arrays.asList(puzpreParts.get(puzpreParts.size() - 1).split("/"));
			return puzpreParts;
		}
	}

	/**
	 * urlのリーダーを取得します。
	 */
	public static BufferedReader getReader(URL url) throws IOException, ProtocolException {
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("GET");
		http.connect();
		return new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
	}
}