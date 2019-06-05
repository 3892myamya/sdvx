package myamya.other.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.solver.Common.Position;
import myamya.other.solver.akari.AkariSolver;
import myamya.other.solver.heyawake.HeyawakeSolver;
import myamya.other.solver.hitori.HitoriSolver;
import myamya.other.solver.lits.LitsSolver;
import myamya.other.solver.norinori.NorinoriSolver;
import myamya.other.solver.nurikabe.NurikabeSolver;
import myamya.other.solver.nurikabe.NurikabeSolver.Room;
import myamya.other.solver.nurimisaki.NurimisakiSolver;
import myamya.other.solver.nurimisaki.NurimisakiSolver.Misaki;
import myamya.other.solver.shikaku.ShikakuSolver;
import myamya.other.solver.shikaku.ShikakuSolver.Sikaku;
import myamya.other.solver.shikaku.ShikakuSolver.Wall;
import myamya.other.solver.shimaguni.ShimaguniSolver;
import myamya.other.solver.stostone.StostoneSolver;
import myamya.other.solver.yajilin.YajilinSolver;
import myamya.other.solver.yinyang.YinyangSolver;
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

	static abstract class AbsSolveThlead extends Thread {
		protected final Solver solver;
		private String status = "時間切れです。途中経過を返します。";

		AbsSolveThlead(int height, int width, String param) {
			solver = getSolver(height, width, param);
		}

		abstract protected Solver getSolver(int height, int width, String param);

		public void run() {
			status = solver.solve();
		}

		public String getStatus() {
			return status;
		}
	}

	static class YajirinSolveThlead extends AbsSolveThlead {
		YajirinSolveThlead(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new YajilinSolver(height, width, param);
		}

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
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" font-size=\""
							+ (baseSize)
							+ "\" textLength=\""
							+ (baseSize)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ oneMasu.toStringForweb()
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NurikabeSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NurikabeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new NurikabeSolver(height, width, param);
		}

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
						for (Room room : field.rooms) {
							if (room.getPivot().equals(new Position(yIndex, xIndex))) {
								String capacityStr = String.valueOf(room.getCapacity());
								int index = HALF_NUMS.indexOf(capacityStr);
								if (index >= 0) {
									masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
								} else {
									masuStr = capacityStr;
								}
								break;
							}
						}
						if (masuStr == null) {
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

	static class StostoneSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		StostoneSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new StostoneSolver(height, width, param);
		}

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

	static class HeyawakeSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HeyawakeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new HeyawakeSolver(height, width, param);
		}

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

	static class LitsSolverThread extends AbsSolveThlead {
		LitsSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new LitsSolver(height, width, param);
		}

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

	static class NorinoriSolverThread extends AbsSolveThlead {
		NorinoriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new NorinoriSolver(height, width, param);
		}

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

	static class ShimaguniSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShimaguniSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new ShimaguniSolver(height, width, param);
		}

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

	static class ShikakuSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShikakuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new ShikakuSolver(height, width, param);
		}

		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ShikakuSolver.Field field = ((ShikakuSolver) solver).getField();
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
			for (Set<Sikaku> sikakuSet : field.getRoomCand().values()) {
				if (sikakuSet.size() == 1) {
					Sikaku sikaku = (Sikaku) sikakuSet.toArray()[0];
					for (int yIndex = sikaku.getLeftUp().getyIndex(); yIndex <= sikaku.getRightDown()
							.getyIndex(); yIndex++) {
						if (sikaku.getLeftUp().getxIndex() > 0) {
							yokoWall[yIndex][sikaku.getLeftUp().getxIndex() - 1] = Wall.EXISTS;
						}
						if (sikaku.getRightDown().getxIndex() < field.getXLength() - 1) {
							yokoWall[yIndex][sikaku.getRightDown().getxIndex()] = Wall.EXISTS;
						}
					}
					for (int xIndex = sikaku.getLeftUp().getxIndex(); xIndex <= sikaku.getRightDown()
							.getxIndex(); xIndex++) {
						if (sikaku.getLeftUp().getyIndex() > 0) {
							tateWall[sikaku.getLeftUp().getyIndex() - 1][xIndex] = Wall.EXISTS;
						}
						if (sikaku.getRightDown().getyIndex() < field.getYLength() - 1) {
							tateWall[sikaku.getRightDown().getyIndex()][xIndex] = Wall.EXISTS;
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| yokoWall[yIndex][xIndex] == Wall.EXISTS;
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
							|| tateWall[yIndex][xIndex] == Wall.EXISTS;
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

	static class AkariSolverThread extends AbsSolveThlead {
		AkariSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new AkariSolver(height, width, param);
		}

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

	static class NurimisakiSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NurimisakiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new NurimisakiSolver(height, width, param);
		}

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

	static class HitoriSolverThread extends AbsSolveThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HitoriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new HitoriSolver(height, width, param);
		}

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

	static class YinyangSolverThread extends AbsSolveThlead {
		YinyangSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver(int height, int width, String param) {
			return new YinyangSolver(height, width, param);
		}

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
								+ "\" fill=\"gray\" >"
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

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<String> parts = getURLparts(request.getParameter("url"));
			int height = Integer.parseInt(parts.get(parts.size() - 2));
			int width = Integer.parseInt(parts.get(parts.size() - 3));
			String puzzleType = parts.get(parts.size() - 4);
			String param = parts.get(parts.size() - 1).split("@")[0];
			if (puzzleType.contains("yajilin") || puzzleType.contains("yajirin")) {
				if (height * width > 400) {
					resultMap.put("result", "");
					resultMap.put("status", "申し訳ございません。401マス以上ある問題は解くことができません。");
				} else {
					YajirinSolveThlead t = new YajirinSolveThlead(height, width, param);
					t.start();
					t.join(28000);
					resultMap.put("result", t.makeCambus());
					resultMap.put("status", t.getStatus());
				}
			} else if (puzzleType.contains("nurikabe")) {
				NurikabeSolverThread t = new NurikabeSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("stostone")) {
				StostoneSolverThread t = new StostoneSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("heyawake")) {
				HeyawakeSolverThread t = new HeyawakeSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("lits")) {
				LitsSolverThread t = new LitsSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("norinori")) {
				NorinoriSolverThread t = new NorinoriSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("shimaguni")) {
				ShimaguniSolverThread t = new ShimaguniSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("shikaku")) {
				ShikakuSolverThread t = new ShikakuSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("akari") || puzzleType.contains("lightup")) {
				AkariSolverThread t = new AkariSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("yinyang")) {
				YinyangSolverThread t = new YinyangSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("nurimisaki")) {
				NurimisakiSolverThread t = new NurimisakiSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else if (puzzleType.contains("hitori")) {
				HitoriSolverThread t = new HitoriSolverThread(height, width, param);
				t.start();
				t.join(28000);
				resultMap.put("result", t.makeCambus());
				resultMap.put("status", t.getStatus());
			} else {
				throw new IllegalArgumentException();
			}
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
			return parts;
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