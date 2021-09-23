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
import java.util.Collections;
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

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.SolverWeb.MasyuSolverThread.UraMasyuSolverThread;
import myamya.other.solver.SolverWeb.NurimisakiSolverThread.TrimisakiSolverThread;
import myamya.other.solver.akari.AkariSolver;
import myamya.other.solver.angleloop.AngleloopSolver;
import myamya.other.solver.angleloop.AngleloopSolver.Angle;
import myamya.other.solver.aqre.AqreSolver;
import myamya.other.solver.aquarium.AquariumSolver;
import myamya.other.solver.bag.BagSolver;
import myamya.other.solver.balance.BalanceSolver;
import myamya.other.solver.barns.BarnsSolver;
import myamya.other.solver.box.BoxSolver;
import myamya.other.solver.building.BuildingSolver;
import myamya.other.solver.castle.CastleSolver;
import myamya.other.solver.cells.CellsSolver;
import myamya.other.solver.chocobanana.ChocobananaSolver;
import myamya.other.solver.chocona.ChoconaSolver;
import myamya.other.solver.chocona.ChoconaSolver.Room;
import myamya.other.solver.cojun.CojunSolver;
import myamya.other.solver.compass.CompassSolver;
import myamya.other.solver.compass.CompassSolver.Compass;
import myamya.other.solver.country.CountrySolver;
import myamya.other.solver.creek.CreekSolver;
import myamya.other.solver.detour.DetourSolver;
import myamya.other.solver.doppelblock.DoppelblockSolver;
import myamya.other.solver.dosufuwa.DosufuwaSolver;
import myamya.other.solver.dotchiloop.DotchiloopSolver;
import myamya.other.solver.doubleback.DoublebackSolver;
import myamya.other.solver.easyasabc.EasyasabcSolver;
import myamya.other.solver.fillmat.FillmatSolver;
import myamya.other.solver.fillomino.FillominoSolver;
import myamya.other.solver.firefly.FireflySolver;
import myamya.other.solver.firefly.FireflySolver.Firefly;
import myamya.other.solver.geradeweg.GeradewegSolver;
import myamya.other.solver.gokigen.GokigenSolver;
import myamya.other.solver.hakoiri.HakoiriSolver;
import myamya.other.solver.hanare.HanareSolver;
import myamya.other.solver.hashikake.HashikakeSolver;
import myamya.other.solver.hebi.HebiSolver;
import myamya.other.solver.herugolf.HerugolfSolver;
import myamya.other.solver.heteromino.HeterominoSolver;
import myamya.other.solver.heyabon.HeyabonSolver;
import myamya.other.solver.heyawake.HeyawakeSolver;
import myamya.other.solver.hitori.HitoriSolver;
import myamya.other.solver.icebarn.IcebarnSolver;
import myamya.other.solver.icelom.IcelomSolver;
import myamya.other.solver.ichimaga.IchimagaSolver;
import myamya.other.solver.juosan.JuosanSolver;
import myamya.other.solver.kakuro.KakuroSolver;
import myamya.other.solver.kakuro.KakuroSolver.Block;
import myamya.other.solver.kazunori.KazunoriSolver;
import myamya.other.solver.kropki.KropkiSolver;
import myamya.other.solver.kurochute.KurochuteSolver;
import myamya.other.solver.kurodoko.KurodokoSolver;
import myamya.other.solver.kurotto.KurottoSolver;
import myamya.other.solver.las.LasSolver;
import myamya.other.solver.linedozen.LinedozenSolver;
import myamya.other.solver.lits.LitsSolver;
import myamya.other.solver.lookair.LookairSolver;
import myamya.other.solver.loopsp.LoopspSolver;
import myamya.other.solver.makaro.MakaroSolver;
import myamya.other.solver.masyu.MasyuSolver;
import myamya.other.solver.masyu.MasyuSolver.Pearl;
import myamya.other.solver.maxi.MaxiSolver;
import myamya.other.solver.meander.MeanderSolver;
import myamya.other.solver.mejilink.MejilinkSolver;
import myamya.other.solver.midloop.MidloopSolver;
import myamya.other.solver.minarism.MinarismSolver;
import myamya.other.solver.mines.MinesSolver;
import myamya.other.solver.mochikoro.MochikoroSolver;
import myamya.other.solver.moonsun.MoonsunSolver;
import myamya.other.solver.nagare.NagareSolver;
import myamya.other.solver.nagenawa.NagenawaSolver;
import myamya.other.solver.nanro.NanroSolver;
import myamya.other.solver.nibunnogo.NibunnogoSolver;
import myamya.other.solver.nondango.NondangoSolver;
import myamya.other.solver.nonogram.NonogramSolver;
import myamya.other.solver.norinori.NorinoriSolver;
import myamya.other.solver.numlin.NumlinSolver;
import myamya.other.solver.nuribou.NuribouSolver;
import myamya.other.solver.nurikabe.NurikabeSolver;
import myamya.other.solver.nurimaze.NurimazeSolver;
import myamya.other.solver.nurimaze.NurimazeSolver.Mark;
import myamya.other.solver.nurimisaki.NurimisakiSolver;
import myamya.other.solver.pipelink.PipelinkSolver;
import myamya.other.solver.putteria.PutteriaSolver;
import myamya.other.solver.rectslider.RectsliderSolver;
import myamya.other.solver.reflect.ReflectSolver;
import myamya.other.solver.renban.RenbanSolver;
import myamya.other.solver.ringring.RingringSolver;
import myamya.other.solver.ripple.RippleSolver;
import myamya.other.solver.roma.RomaSolver;
import myamya.other.solver.sashigane.SashiganeSolver;
import myamya.other.solver.satogaeri.SatogaeriSolver;
import myamya.other.solver.shakashaka.ShakashakaSolver;
import myamya.other.solver.shikaku.ShikakuSolver;
import myamya.other.solver.shimaguni.ShimaguniSolver;
import myamya.other.solver.shugaku.ShugakuSolver;
import myamya.other.solver.shwolf.ShwolfSolver;
import myamya.other.solver.simplegako.SimplegakoSolver;
import myamya.other.solver.simpleloop.SimpleloopSolver;
import myamya.other.solver.slither.KurohouiSolver;
import myamya.other.solver.slither.SlitherSolver;
import myamya.other.solver.snake.SnakeSolver;
import myamya.other.solver.starbattle.StarBattleSolver;
import myamya.other.solver.stostone.StostoneSolver;
import myamya.other.solver.sudoku.SudokuSolver;
import myamya.other.solver.sukoro.SukoroSolver;
import myamya.other.solver.sukororoom.SukororoomSolver;
import myamya.other.solver.tapa.TapaSolver;
import myamya.other.solver.tasquare.TasquareSolver;
import myamya.other.solver.tatamibari.TatamibariSolver;
import myamya.other.solver.tateyoko.TateyokoSolver;
import myamya.other.solver.tentaisho.TentaishoSolver;
import myamya.other.solver.tents.TentsSolver;
import myamya.other.solver.tilepaint.TilepaintSolver;
import myamya.other.solver.usoone.UsooneSolver;
import myamya.other.solver.view.ViewSolver;
import myamya.other.solver.wafusuma.WafusumaSolver;
import myamya.other.solver.walllogic.WalllogicSolver;
import myamya.other.solver.wblink.WblinkSolver;
import myamya.other.solver.whitelink.WhitelinkSolver;
import myamya.other.solver.yajikazu.YajikazuSolver;
import myamya.other.solver.yajikazu.YajikazuSolver.Arrow;
import myamya.other.solver.yajilin.YajilinSolver;
import myamya.other.solver.yajitatami.YajitatamiSolver;
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
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else {
						if (field.getArrows()[yIndex][xIndex] != null) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
									+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ field.getArrows()[yIndex][xIndex].toStringWeb() + "</text>");
						} else {
							String str = "";
							if (oneMasu.toString().equals("・")) {
								Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
								Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
										: field.getYokoWall()[yIndex][xIndex];
								Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
										: field.getTateWall()[yIndex][xIndex];
								Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
								if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
										&& left == Wall.EXISTS) {
									str = "└";
								} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
										&& left == Wall.EXISTS) {
									str = "│";
								} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
										&& left == Wall.NOT_EXISTS) {
									str = "┘";
								} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
										&& left == Wall.EXISTS) {
									str = "┌";
								} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
										&& left == Wall.NOT_EXISTS) {
									str = "─";
								} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
										&& left == Wall.NOT_EXISTS) {
									str = "┐";
								} else {
									str = oneMasu.toString();
								}
							} else {
								str = oneMasu.toString();
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "green" + "\" font-size=\""
									+ (baseSize) + "\" textLength=\"" + (baseSize)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}

					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HeyawakeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final boolean ayeheya;

		HeyawakeSolverThread(int height, int width, String param, boolean ayeheya) {
			super(height, width, param);
			this.ayeheya = ayeheya;
		}

		@Override
		protected Solver getSolver() {
			return new HeyawakeSolver(height, width, param, ayeheya);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			HeyawakeSolver.Field field = ((HeyawakeSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.isExistYokoWall(yIndex, xIndex);
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.isExistTateWall(yIndex, xIndex);
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| tateWall[yIndex][xIndex] == ShikakuSolver.Wall.EXISTS;
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
			// 数字描画
			for (ShikakuSolver.Room room : field.getRooms()) {
				int roomCapacity = room.getCapacity();
				if (roomCapacity != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomCapacity);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getPivot();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5 + margin) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ roomBlackCountStr + "</text>");
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

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			AkariSolver.Field field = ((AkariSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					} else if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getMasu()[yIndex][xIndex].toString() + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getMisaki()[yIndex][xIndex] || field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getMisaki()[yIndex][xIndex]) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						}
						if (field.getNumbers()[yIndex][xIndex] != null) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
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
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize + margin) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"gray\" >" + "\">" + "</rect>");
					} else {
						if (oneMasu.toString().equals("・")) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
									+ (baseSize) + "\" fill=\"palegreen\" >" + "</rect>");
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String numberStr = String.valueOf(field.numbers[yIndex][xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}

					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					DosufuwaSolver.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (oneMasu.toString().equals("○")) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");

					} else if (oneMasu.toString().equals("●")) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");

					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getFixedPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu == Masu.NOT_BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");

					} else if (oneMasu == Masu.BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 点描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					sb.append(
							"<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\"" + (xIndex * baseSize + baseSize)
									+ "\" r=\"" + 1 + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					boolean oneYokoWall = field.getYokoExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = field.getTateExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">" + "</line>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ ((field.getYLength() + 1) * baseSize + 2 * baseSize) + "\" width=\""
					+ ((field.getXLength() + 1) * baseSize + 2 * baseSize) + "\" >");
			sb.append("<rect y=\"" + 0 + "\" x=\"" + (baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
					+ (baseSize) + "\" fill=\"gray\" >" + "</rect>");
			sb.append("<rect y=\"" + (1) + "\" x=\"" + (baseSize + 1) + "\" width=\"" + (baseSize - 2) + "\" height=\""
					+ (baseSize - 2) + "\">" + "</rect>");
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				sb.append("<rect y=\"" + 0 + "\" x=\"" + ((xIndex + 1) * baseSize + baseSize) + "\" width=\""
						+ (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"gray\" >" + "</rect>");
				sb.append("<rect y=\"" + (1) + "\" x=\"" + ((xIndex + 1) * baseSize + baseSize + 1) + "\" width=\""
						+ (baseSize - 2) + "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
			}
			sb.append("<rect y=\"" + 0 + "\" x=\"" + ((field.getXLength() + 1) * baseSize + baseSize) + "\" width=\""
					+ (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"gray\" >" + "</rect>");
			sb.append("<rect y=\"" + (1) + "\" x=\"" + ((field.getXLength() + 1) * baseSize + baseSize + 1)
					+ "\" width=\"" + (baseSize - 2) + "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				sb.append("<rect y=\"" + (yIndex + 1) * baseSize + "\" x=\"" + (baseSize) + "\" width=\"" + (baseSize)
						+ "\" height=\"" + (baseSize) + "\" fill=\"gray\" >" + "</rect>");
				sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1) + "\" x=\"" + (baseSize + 1) + "\" width=\""
						+ (baseSize - 2) + "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1) + "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize + 1) + "\" width=\"" + (baseSize - 2)
								+ "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + ((yIndex + 1) * baseSize + baseSize - 4) + "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" fill=\"" + "lime" + "\" textLength=\"" + (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + oneMasu.toString() + "</text>");
					}
				}
				sb.append("<rect y=\"" + (yIndex + 1) * baseSize + "\" x=\""
						+ ((field.getXLength() + 1) * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
						+ (baseSize) + "\" fill=\"gray\" >" + "</rect>");
				sb.append("<rect y=\"" + ((yIndex + 1) * baseSize + 1) + "\" x=\""
						+ ((field.getXLength() + 1) * baseSize + baseSize + 1) + "\" width=\"" + (baseSize - 2)
						+ "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
			}
			sb.append("<rect y=\"" + (field.getYLength() + 1) * baseSize + "\" x=\"" + (baseSize) + "\" width=\""
					+ (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"gray\" >" + "</rect>");
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1) + "\" x=\"" + (baseSize + 1)
					+ "\" width=\"" + (baseSize - 2) + "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				sb.append("<rect y=\"" + (field.getYLength() + 1) * baseSize + "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
						+ (baseSize) + "\" fill=\"gray\" >" + "</rect>");
				sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1) + "\" x=\""
						+ ((xIndex + 1) * baseSize + baseSize + 1) + "\" width=\"" + (baseSize - 2) + "\" height=\""
						+ (baseSize - 2) + "\">" + "</rect>");
			}
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize) + "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
					+ (baseSize) + "\" fill=\"gray\" >" + "</rect>");
			sb.append("<rect y=\"" + ((field.getYLength() + 1) * baseSize + 1) + "\" x=\""
					+ ((field.getXLength() + 1) * baseSize + baseSize + 1) + "\" width=\"" + (baseSize - 2)
					+ "\" height=\"" + (baseSize - 2) + "\">" + "</rect>");
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					int number = field.getNumbers()[yIndex][xIndex];
					if (number != -1) {
						String numberStr;
						String wkstr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(wkstr);
						if (index >= 0) {
							numberStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							numberStr = wkstr;
						}
						String fillColor = field.getMasu()[yIndex][xIndex] == Common.Masu.BLACK ? "white" : "black";
						sb.append("<text y=\"" + ((yIndex + 1) * baseSize + baseSize - 5) + "\" x=\""
								+ ((xIndex + 1) * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
								+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + numberStr + "</text>");

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
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					Arrow oneArrow = field.getArrows()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
						if (oneArrow != null) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
									+ "gray" + "\" textLength=\"" + (baseSize - 2)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + oneArrow.toStringForweb() + "</text>");

						}
					} else {
						if (oneArrow != null) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
									+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneArrow.toStringForweb() + "</text>");

						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Pearl onePearl = field.getPearl()[yIndex][xIndex];
					if (onePearl == Pearl.SIRO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");

					} else if (onePearl == Pearl.KURO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircles()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getCircles()[yIndex][xIndex].getCnt() != -1) {
							String numberStr = String.valueOf(field.getCircles()[yIndex][xIndex].getCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else if (field.getArrows()[yIndex][xIndex] != null) {
						int lengthAdjust = 0;
						if (field.getArrows()[yIndex][xIndex] == Direction.UP
								|| field.getArrows()[yIndex][xIndex] == Direction.DOWN) {
							lengthAdjust = 6;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + (lengthAdjust / 2)) + "\" font-size=\""
								+ (baseSize - 2) + "\" textLength=\"" + (baseSize - 2 - lengthAdjust)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getArrows()[yIndex][xIndex].getDirectString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
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
			return sb.toString();
		}
	}

	static class YajitatamiSolverThread extends AbsSolverThlead {
		YajitatamiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new YajitatamiSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			YajitatamiSolver.Field field = ((YajitatamiSolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoFirstWall()[yIndex][xIndex];
					boolean oneYokoResolveWall = !oneYokoWall && field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else if (oneYokoResolveWall) {
						sb.append("stroke=\"green\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateFirstWall()[yIndex][xIndex];
					boolean oneTateResolveWall = !oneTateWall && field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else if (oneTateResolveWall) {
						sb.append("stroke=\"green\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					YajitatamiSolver.Arrow oneArrow = field.getArrows()[yIndex][xIndex];
					if (oneArrow != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneArrow.toStringForweb() + "</text>");

					}
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
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
							|| (xIndex == field.getXLength() - 1
									&& field.getMasu()[yIndex][field.getXLength() - 1].toString().equals("・"))
							|| (xIndex != -1 && xIndex != field.getXLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("・")
									&& field.getMasu()[yIndex][xIndex + 1].toString().equals("■"))
							|| (xIndex != -1 && xIndex != field.getXLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("■")
									&& field.getMasu()[yIndex][xIndex + 1].toString().equals("・"));
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = (yIndex == -1 && field.getMasu()[0][xIndex].toString().equals("・"))
							|| (yIndex == field.getYLength() - 1
									&& field.getMasu()[field.getYLength() - 1][xIndex].toString().equals("・"))
							|| (yIndex != -1 && yIndex != field.getYLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("・")
									&& field.getMasu()[yIndex + 1][xIndex].toString().equals("■"))
							|| (yIndex != -1 && yIndex != field.getYLength() - 1
									&& field.getMasu()[yIndex][xIndex].toString().equals("■")
									&& field.getMasu()[yIndex + 1][xIndex].toString().equals("・"));
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
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr;
						String wkstr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(wkstr);
						if (index >= 0) {
							numberStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							numberStr = wkstr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ numberStr + "</text>");

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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
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
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
										+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr + "</text>");
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
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize + 2) / 2
										+ "\" textLength=\"" + (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr1 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" font-size=\""
										+ (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr2 + "</text>");
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
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) / 2
										+ "\" textLength=\"" + (baseSize) / 2 + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr1 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4)) + "\" font-size=\""
										+ (baseSize) / 2 + "\" textLength=\"" + (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr2 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" font-size=\""
										+ (baseSize) / 2 + "\" textLength=\"" + (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr3 + "</text>");
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
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 3 / 4) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) / 2
										+ "\" textLength=\"" + (baseSize) / 2 + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr1 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4)) + "\" font-size=\""
										+ (baseSize) / 2 + "\" textLength=\"" + (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr2 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 4)) + "\" font-size=\""
										+ (baseSize) / 2 + "\" textLength=\"" + (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr3 + "</text>");
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 3 / 4) - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" font-size=\""
										+ (baseSize) / 2 + "\" textLength=\"" + (baseSize) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr4 + "</text>");
							}
						} else {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
									+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ oneMasu.toString() + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			sb.append("<line y1=\"" + (margin) + "\" x1=\"" + (baseSize) + "\" y2=\"" + (margin) + "\" x2=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">" + "</line>");

			sb.append("<line y1=\"" + (field.getYLength() * baseSize + margin) + "\" x1=\"" + (baseSize) + "\" y2=\""
					+ (field.getYLength() * baseSize + margin) + "\" x2=\"" + (field.getXLength() * baseSize + baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">" + "</line>");

			sb.append("<line y1=\"" + (margin) + "\" x1=\"" + (baseSize) + "\" y2=\""
					+ (field.getYLength() * baseSize + margin) + "\" x2=\"" + (baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">" + "</line>");

			sb.append("<line y1=\"" + (margin) + "\" x1=\"" + (field.getXLength() * baseSize + baseSize) + "\" y2=\""
					+ (field.getYLength() * baseSize + margin) + "\" x2=\"" + (field.getXLength() * baseSize + baseSize)
					+ "\" stroke-width=\"1\" fill=\"none\"");
			sb.append("stroke=\"#000\" ");
			sb.append(">" + "</line>");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ FULL_NUMS.substring(field.getNumbers()[yIndex][xIndex],
											field.getNumbers()[yIndex][xIndex] + 1)
									+ "</text>");
						}
					} else {
						Masu oneMasu = field.getMasu()[yIndex][xIndex];
						if (oneMasu == Masu.BLACK) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
									+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ "・" + "</text>");
						} else if (oneMasu == Masu.NOT_BLACK) {
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
										+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ "・" + "</text>");
							} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.EXISTS) {
								sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " L"
										+ (xIndex * baseSize + baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								sb.append("<path d=\"M " + (xIndex * baseSize + baseSize + baseSize) + " "
										+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " L"
										+ (xIndex * baseSize + baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + margin) + " L"
										+ (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize + margin)
										+ " L" + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
										+ (yIndex * baseSize + margin) + " L"
										+ (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize + margin)
										+ " L" + (xIndex * baseSize + baseSize + baseSize) + " "
										+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 3) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 1) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
								+ "</text>");
					} else {
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
							+ "green" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ (oneMasu.toString().equals("■") ? "★" : oneMasu.toString()) + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						return "解けました。推定難易度:" + Difficulty.getByCount(level * level * 3).toString() + "(Lv:" + level
								+ ")";
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| xIndex % field.getRoomWidth() == field.getRoomWidth() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| yIndex % field.getRoomHeight() == field.getRoomHeight() - 1;
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						roomWhiteCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomWhiteCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5 + margin) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "black"
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomWhiteCountStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getOriginNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];

					if (oneMasu.toString().equals("■")) {
						if (field.getBlockPosSet().contains(new Position(yIndex, xIndex))) {
							sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
									+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
							if (field.getDirection()[yIndex][xIndex] != null) {
								int lengthAdjust = 0;
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + -4) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (lengthAdjust / 2)) + "\" font-size=\""
										+ (baseSize - 2) + "\" textLength=\"" + (baseSize - 2 - lengthAdjust)
										+ "\" fill=\"" + "white" + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ field.getDirection()[yIndex][xIndex].getTriangle() + "</text>");
							}
						}
					} else {
						if (oneMasu.toString().equals("・")) {
							if (field.getDirection()[yIndex][xIndex] != null) {
								int lengthAdjust = 0;
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + -4) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (lengthAdjust / 2)) + "\" font-size=\""
										+ (baseSize - 2) + "\" textLength=\"" + (baseSize - 2 - lengthAdjust)
										+ "\" fill=\"" + "black" + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ field.getDirection()[yIndex][xIndex].getTriangle() + "</text>");
							}
							String str = "";
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = oneMasu.toString();
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" fill=\"" + "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					} else if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (Entry<Position, Set<Position>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					Position movedPos = new ArrayList<>(entry.getValue()).get(0);
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"gray\", stroke=\"gray\">" + "</circle>");
					sb.append("<circle cy=\"" + (movedPos.getyIndex() * baseSize + (baseSize / 2) + margin) + "\" cx=\""
							+ (movedPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"lightgray\", stroke=\"black\">" + "</circle>");
					if (movedPos.getyIndex() < originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex > movedPos.getyIndex(); yIndex--) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize - (yIndex == movedPos.getyIndex() + 1 ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											- (yIndex == originPos.getyIndex() ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() > originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex < movedPos.getxIndex(); xIndex++) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											+ (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											+ (xIndex == movedPos.getxIndex() - 1 ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getyIndex() > originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex < movedPos.getyIndex(); yIndex++) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + (yIndex == originPos.getyIndex() ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											+ (yIndex == movedPos.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() < originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex > movedPos.getxIndex(); xIndex--) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											- (xIndex == movedPos.getxIndex() + 1 ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											- (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
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
						sb.append("<text y=\"" + (movedPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (movedPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				} else {
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
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
						sb.append("<text y=\"" + (originPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (originPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}

			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getRoomYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getRoomTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Firefly firefly = field.getFireflies()[yIndex][xIndex];
					String str = "";
					if (firefly != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (firefly.getDirection() == Direction.UP) {
							sb.append("<circle cy=\"" + (yIndex * baseSize) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else if (firefly.getDirection() == Direction.RIGHT) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else if (firefly.getDirection() == Direction.DOWN) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else if (firefly.getDirection() == Direction.LEFT) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						}
						if (firefly.getCount() != -1) {
							String numberStr = String.valueOf(firefly.getCount());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								str = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ str + "</text>");
						}
					} else {
						if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = "？";
							}
						} else if (field.getMasu()[yIndex][xIndex] == Masu.SPACE) {
							str = "？";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + (str.equals("？") ? "black" : "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircle()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"" + "lightblue"
								+ "\">" + "</rect>");
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getFirstYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getFirstTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + fillColor + "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircle()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + fillColor + "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getFirstYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getFirstTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + fillColor + "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			int margin = 5;
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " L"
									+ (xIndex * baseSize + baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "white" + "\" font-size=\""
										+ (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
							}
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M " + (xIndex * baseSize + baseSize + baseSize) + " "
									+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " L"
									+ (xIndex * baseSize + baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" fill=\"" + "white"
										+ "\" font-size=\"" + (baseSize + 2) / 2 + "\" textLength=\""
										+ (baseSize + 2) / 2 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
										+ "</text>");
							}
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize + baseSize)
									+ " " + (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) + margin - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "white" + "\" font-size=\""
										+ (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
							}
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " "
									+ (yIndex * baseSize + margin) + " L" + (xIndex * baseSize + baseSize + baseSize)
									+ " " + (yIndex * baseSize + margin) + " L"
									+ (xIndex * baseSize + baseSize + baseSize) + " "
									+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) + margin - 1) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" fill=\"" + "white"
										+ "\" font-size=\"" + (baseSize + 2) / 2 + "\" textLength=\""
										+ (baseSize + 2) / 2 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
										+ "</text>");
							}
						}
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					String fillColor;
					if (field.getCrossPosSet().contains(new Position(yIndex, xIndex))) {
						fillColor = "black";
					} else {
						fillColor = "green";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + fillColor + "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
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
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"" + "black"
								+ "\">" + "</rect>");
					}
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class RectsliderSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		RectsliderSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new RectsliderSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			RectsliderSolver.Field field = ((RectsliderSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (Entry<Position, Set<Position>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					Position movedPos = new ArrayList<>(entry.getValue()).get(0);
					sb.append("<rect y=\"" + (originPos.getyIndex() * baseSize + margin) + "\" x=\""
							+ (originPos.getxIndex() * baseSize + baseSize) + "\" width=\"" + (baseSize)
							+ "\" height=\"" + (baseSize) + "\" fill=\"" + "gray" + "\">" + "</rect>");
					sb.append("<rect y=\"" + (movedPos.getyIndex() * baseSize + margin) + "\" x=\""
							+ (movedPos.getxIndex() * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
							+ (baseSize) + "\" fill=\"" + "black" + "\">" + "</rect>");
					if (movedPos.getyIndex() < originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex > movedPos.getyIndex(); yIndex--) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize - (yIndex == movedPos.getyIndex() + 1 ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											- (yIndex == originPos.getyIndex() ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() > originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex < movedPos.getxIndex(); xIndex++) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											+ (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											+ (xIndex == movedPos.getxIndex() - 1 ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getyIndex() > originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex < movedPos.getyIndex(); yIndex++) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + (yIndex == originPos.getyIndex() ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											+ (yIndex == movedPos.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() < originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex > movedPos.getxIndex(); xIndex--) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											- (xIndex == movedPos.getxIndex() + 1 ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											- (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
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
						sb.append("<text y=\"" + (movedPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (movedPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "white"
								+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				} else {
					sb.append("<rect y=\"" + (originPos.getyIndex() * baseSize + margin) + "\" x=\""
							+ (originPos.getxIndex() * baseSize + baseSize) + "\" width=\"" + (baseSize)
							+ "\" height=\"" + (baseSize) + "\" fill=\"white\", stroke=\"black" + "\">" + "</rect>");
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
						sb.append("<text y=\"" + (originPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (originPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}

			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HebiSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HebiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HebiSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			HebiSolver.Field field = ((HebiSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getArrows()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"black\" >" + "</rect>");
						if (field.getArrows()[yIndex][xIndex].getCount() != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ field.getArrows()[yIndex][xIndex].toStringForweb() + "</text>");
						}
					} else if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr;
						if (field.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
							numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						} else {
							numberStr = "・";
						}
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" fill=\""
								+ (masuStr.equals("・") ? "pink" : "green") + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class ShwolfSolverThread extends AbsSolverThlead {
		ShwolfSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ShwolfSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			ShwolfSolver.Field field = ((ShwolfSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
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

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu masu = field.getMasu()[yIndex][xIndex];
					if (masu == Masu.NOT_BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");

					} else if (masu == Masu.BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}

				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.getPiles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ShugakuSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ShugakuSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ShugakuSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ShugakuSolver.Field field = ((ShugakuSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"gray\" >" + "</rect>");
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != 5) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"black\" >" + "</rect>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (field.getMakura()[yIndex][xIndex] == Masu.BLACK) {
							sb.append("<rect y=\"" + (yIndex * baseSize + 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 4) + "\" width=\"" + (baseSize - 8)
									+ "\" height=\"" + (baseSize - 8) + "\" fill=\"white\", stroke=\"black" + "\">"
									+ "</rect>");
						}
					} else if (field.getMasu()[yIndex][xIndex] == Masu.SPACE) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class AngleloopSolverThread extends AbsSolverThlead {
		AngleloopSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new AngleloopSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			AngleloopSolver.Field field = ((AngleloopSolver) solver).getField();
			int baseSize = 30;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">" + "</line>");
				}
			}
			// 記号描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getAngles()[yIndex][xIndex] != null) {
						if (field.getAngles()[yIndex][xIndex] != Angle.RIGHT) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + (baseSize / 2) - 8) + "\" x=\""
									+ (xIndex * baseSize + (baseSize / 2) + 5) + "\" font-size=\"" + (baseSize - 10)
									+ "\" textLength=\"" + (baseSize - 10) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ field.getAngles()[yIndex][xIndex] + "</text>");
						} else {
							sb.append("<rect y=\"" + (yIndex * baseSize + baseSize - 8) + "\" x=\""
									+ (xIndex * baseSize + (baseSize / 2) + 7) + "\" width=\"" + (baseSize - 15)
									+ "\" height=\"" + (baseSize - 15)
									+ "\" fill=\"gray\" stroke-width=\"1\" stroke=\"black\">" + "</rect>");
						}
					}
				}
			}
			for (Entry<Position, Map<Position, Masu>> entry : field.getCandidates().entrySet()) {
				for (Entry<Position, Masu> innerEntry : entry.getValue().entrySet()) {
					if (innerEntry.getValue() == Masu.BLACK) {
						sb.append("<line y1=\"" + (entry.getKey().getyIndex() * baseSize + baseSize) + "\" x1=\""
								+ (entry.getKey().getxIndex() * baseSize + baseSize) + "\" y2=\""
								+ (innerEntry.getKey().getyIndex() * baseSize + baseSize) + "\" x2=\""
								+ (innerEntry.getKey().getxIndex() * baseSize + baseSize)
								+ "\" stroke-width=\"2\" fill=\"none\"");
						sb.append("stroke=\"green\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HashikakeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HashikakeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HashikakeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			HashikakeSolver.Field field = ((HashikakeSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			// 橋描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.getYokoWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
						if (field.getYokoWallGate()[yIndex][xIndex] == 1) {
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
									+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
							int targetX = xIndex + 1;
							while (true) {
								if (targetX >= field.getXLength() - 1
										|| field.getYokoWall()[yIndex][targetX] != Wall.NOT_EXISTS
										|| field.getYokoWallGate()[yIndex][targetX] != 0) {
									break;
								}
								sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" x=\""
										+ (targetX * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
										+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
								targetX++;
							}
						} else if (field.getYokoWallGate()[yIndex][xIndex] == 2) {
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 3)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
									+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize * 2 / 3)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
									+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
							int targetX = xIndex + 1;
							while (true) {
								if (targetX >= field.getXLength() - 1
										|| field.getYokoWall()[yIndex][targetX] != Wall.NOT_EXISTS
										|| field.getYokoWallGate()[yIndex][targetX] != 0) {
									break;
								}
								sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 3)) + "\" x=\""
										+ (targetX * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
										+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
								sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize * 2 / 3)) + "\" x=\""
										+ (targetX * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (baseSize)
										+ "\" height=\"" + (1) + "\" fill=\"green\">" + "</rect>");
								targetX++;
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getTateWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
						if (field.getTateWallGate()[yIndex][xIndex] == 1) {
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (1)
									+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
							int targetY = yIndex + 1;
							while (true) {
								if (targetY >= field.getYLength() - 1
										|| field.getTateWall()[targetY][xIndex] != Wall.NOT_EXISTS
										|| field.getTateWallGate()[targetY][xIndex] != 0) {
									break;
								}
								sb.append("<rect y=\"" + (targetY * baseSize + (baseSize / 2)) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" width=\"" + (1)
										+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
								targetY++;
							}
						} else if (field.getTateWallGate()[yIndex][xIndex] == 2) {
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" width=\"" + (1)
									+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize * 2 / 3)) + "\" width=\"" + (1)
									+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
							int targetY = yIndex + 1;
							while (true) {
								if (targetY >= field.getYLength() - 1
										|| field.getTateWall()[targetY][xIndex] != Wall.NOT_EXISTS
										|| field.getTateWallGate()[targetY][xIndex] != 0) {
									break;
								}
								sb.append("<rect y=\"" + (targetY * baseSize + (baseSize / 2)) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" width=\"" + (1)
										+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
								sb.append("<rect y=\"" + (targetY * baseSize + (baseSize / 2)) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize * 2 / 3)) + "\" width=\"" + (1)
										+ "\" height=\"" + (baseSize) + "\" fill=\"green\">" + "</rect>");
								targetY++;
							}
						}
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class CellsSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final int cells;

		CellsSolverThread(int height, int width, String param, int cells) {
			super(height, width, param);
			this.cells = cells;
		}

		@Override
		protected Solver getSolver() {
			return new CellsSolver(height, width, param, cells);
		}

		@Override
		public String makeCambus() {
			CellsSolver.Field field = ((CellsSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null && field.getNumbers()[yIndex][xIndex] == -1) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbers()[yIndex][xIndex] != null && field.getNumbers()[yIndex][xIndex] != -1) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class KurochuteSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KurochuteSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KurochuteSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			KurochuteSolver.Field field = ((KurochuteSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NondangoSolverThread extends AbsSolverThlead {
		NondangoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NondangoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NondangoSolver.Field field = ((NondangoSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu != null) {
						if (oneMasu == Masu.BLACK) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");

						} else {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");

						}
						if (oneMasu == Masu.SPACE) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 4)
									+ "\" textLength=\"" + (baseSize - 4) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ "？" + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class JuosanSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		JuosanSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new JuosanSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			JuosanSolver.Field field = ((JuosanSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + "─"
								+ "</text>");
					} else if (oneMasu.toString().equals("■")) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + "│"
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
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (JuosanSolver.Room room : field.getRooms()) {
				int roomCount = room.getCnt();
				if (roomCount != -1) {
					String roomCountStr;
					String wkstr = String.valueOf(roomCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 12) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "black"
							+ "\" font-size=\"" + (baseSize / 2 - 2) + "\" textLength=\"" + (baseSize / 2 - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MoonsunSolverThread extends AbsSolverThlead {
		MoonsunSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MoonsunSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MoonsunSolver.Field field = ((MoonsunSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMoonSuns()[yIndex][xIndex] != 0) {
						if (field.getMoonSuns()[yIndex][xIndex] == 1) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (field.getMoonSuns()[yIndex][xIndex] == 2) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin - 2) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) - 2) + "\" r=\""
									+ (baseSize / 2 - 3) + "\" fill=\"white\", stroke=\"white\">" + "</circle>");
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ScrinSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ScrinSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MochikoroSolver(height, width, param, false, true);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			MochikoroSolver.Field field = ((MochikoroSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			Masu[][] masu = field.getMasu();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
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
			// 緑に塗る
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"palegreen\" >" + "</rect>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = (xIndex == -1 && masu[yIndex][0].toString().equals("・"))
							|| (xIndex == field.getXLength() - 1
									&& masu[yIndex][field.getXLength() - 1].toString().equals("・"))
							|| (xIndex != -1 && xIndex != field.getXLength() - 1
									&& masu[yIndex][xIndex].toString().equals("・")
									&& masu[yIndex][xIndex + 1].toString().equals("■"))
							|| (xIndex != -1 && xIndex != field.getXLength() - 1
									&& masu[yIndex][xIndex].toString().equals("■")
									&& masu[yIndex][xIndex + 1].toString().equals("・"));
					if (oneYokoWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = (yIndex == -1 && masu[0][xIndex].toString().equals("・"))
							|| (yIndex == field.getYLength() - 1
									&& masu[field.getYLength() - 1][xIndex].toString().equals("・"))
							|| (yIndex != -1 && yIndex != field.getYLength() - 1
									&& masu[yIndex][xIndex].toString().equals("・")
									&& masu[yIndex + 1][xIndex].toString().equals("■"))
							|| (yIndex != -1 && yIndex != field.getYLength() - 1
									&& masu[yIndex][xIndex].toString().equals("■")
									&& masu[yIndex + 1][xIndex].toString().equals("・"));
					if (oneTateWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			// 点描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					sb.append(
							"<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\"" + (xIndex * baseSize + baseSize)
									+ "\" r=\"" + 1 + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class UsooneSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		UsooneSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new UsooneSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			UsooneSolver.Field field = ((UsooneSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// マス
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getUsos()[yIndex][xIndex] == Masu.BLACK) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 1)
									+ "\" textLength=\"" + (baseSize - 1) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ "×" + "</text>");
						} else if (field.getUsos()[yIndex][xIndex] == Masu.NOT_BLACK) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"lime\">" + "</circle>");
						}
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					} else if (oneMasu.toString().equals("・")) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class KakuroSolverThread extends AbsSolverThlead {

		KakuroSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KakuroSolver(height, width, param);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			KakuroSolver.Field field = ((KakuroSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex - 1, xIndex - 1);
					Block block = field.getBlocks().get(pos);
					if (block != null || yIndex == 0 || xIndex == 0) {
						sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize) + " L"
								+ (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize) + " L"
								+ (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize + baseSize)
								+ " Z\" >" + "</path>");
						if (block != null && block.getLeftCnt() != 0) {
							String masuStr;
							String numberStr = String.valueOf(block.getLeftCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" fill=\"" + "white"
									+ "\" font-size=\"" + (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
						sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize) + " L"
								+ (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize + baseSize) + " L"
								+ (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize + baseSize)
								+ " Z\" >" + "</path>");
						if (block != null && block.getDownCnt() != 0) {
							String masuStr;
							String numberStr = String.valueOf(block.getDownCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");

						}
					} else if (field.getNumbersCand()[pos.getyIndex()][pos.getxIndex()].size() == 1) {
						String numberStr = String
								.valueOf(field.getNumbersCand()[pos.getyIndex()][pos.getxIndex()].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "green" + "\" font-size=\""
								+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class KropkiSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KropkiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KropkiSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			KropkiSolver.Field field = ((KropkiSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "green" + "\" font-size=\""
								+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (!oneYokoWall) {
						if (field.getYokoWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						}
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
					if (!oneTateWall) {
						if (field.getTateWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (field.getTateWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NanroSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NanroSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NanroSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			NanroSolver.Field field = ((NanroSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1
							&& field.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SukoroSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SukoroSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SukoroSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SukoroSolver.Field field = ((SukoroSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1
							&& field.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class HakoiriSolverThread extends AbsSolverThlead {

		HakoiriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HakoiriSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			HakoiriSolver.Field field = ((HakoiriSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1
							&& field.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
						int number = field.getNumbersCand()[yIndex][xIndex].get(0);
						if (number == 1) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (number == 2) {
							sb.append("<path d=\"M " + (xIndex * baseSize + baseSize + baseSize / 2) + " "
									+ (yIndex * baseSize + 4) + " L" + (xIndex * baseSize + baseSize + 3) + " "
									+ (yIndex * baseSize + baseSize - 3) + " L"
									+ (xIndex * baseSize + baseSize + baseSize - 3) + " "
									+ (yIndex * baseSize + baseSize - 3) + " Z\" "
									+ "\" fill=\"white\", stroke=\"black\">" + "</path>");
						} else if (number == 3) {
							sb.append("<rect y=\"" + (yIndex * baseSize + (baseSize / 2) - 6) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) - 6) + "\" width=\""
									+ (baseSize - 7) + "\" height=\"" + (baseSize - 7)
									+ "\" fill=\"white\" stroke-width=\"1\" stroke=\"black\">" + "</rect>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HanareSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HanareSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HanareSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			HanareSolver.Field field = ((HanareSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (field.getFixedMasuSet().contains(pos)) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						String numberStr = String.valueOf(field.getRoomSize(pos));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\""
								+ (field.getFixedMasuSet().contains(pos) ? "black" : "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" fill=\"" + "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + field.getMasu()[yIndex][xIndex].toString()
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
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class IcebarnSolverThread extends AbsSolverThlead {

		private final int start;
		private final int goal;

		IcebarnSolverThread(int height, int width, String param, int start, int goal) {
			super(height, width, param);
			this.start = start;
			this.goal = goal;
		}

		@Override
		protected Solver getSolver() {
			return new IcebarnSolver(height, width, param, start, goal);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			IcebarnSolver.Field field = ((IcebarnSolver) solver).getField();
			int baseSize = 20;
			int margin = baseSize;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (field.getIcebarnPosSet().contains(pos)) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"" + "lightblue" + "\">" + "</rect>");
					}
					String str = "";
					Wall up = field.getTateExtraWall()[yIndex][xIndex];
					Wall right = field.getYokoExtraWall()[yIndex][xIndex + 1];
					Wall down = field.getTateExtraWall()[yIndex + 1][xIndex];
					Wall left = field.getYokoExtraWall()[yIndex][xIndex];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			// 横壁描画
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			// 横矢印描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					if (field.getYokoExtraWallDirection()[yIndex][xIndex] != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize - 7) + "\" font-size=\"" + (baseSize - 6)
								+ "\" textLength=\"" + (baseSize - 6) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getYokoExtraWallDirection()[yIndex][xIndex].getDirectString() + "</text>");
					}
				}
			}
			// 縦矢印描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getTateExtraWallDirection()[yIndex][xIndex] != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 5) + "\" font-size=\"" + (baseSize - 10)
								+ "\" textLength=\"" + (baseSize - 10) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getTateExtraWallDirection()[yIndex][xIndex].getDirectString() + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class RomaSolverThread extends AbsSolverThlead {

		RomaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new RomaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			RomaSolver.Field field = ((RomaSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1
							&& field.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
						int number = field.getNumbersCand()[yIndex][xIndex].get(0);
						if (number == 5) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else {
							int lengthAdjust = 0;
							Direction dir = Direction.getByNum(number);
							if (dir == Direction.UP || dir == Direction.DOWN) {
								lengthAdjust = 6;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (lengthAdjust / 2)) + "\" font-size=\""
									+ (baseSize - 2) + "\" textLength=\"" + (baseSize - 2 - lengthAdjust)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + dir.getDirectString() + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HerugolfSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		HerugolfSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HerugolfSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			HerugolfSolver.Field field = ((HerugolfSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			// 池描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getPond()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"" + "lightblue" + "\">" + "</rect>");
					}
				}
			}
			// ホール描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getHole()[yIndex][xIndex]) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + "Ｈ"
								+ "</text>");
					}
				}
			}
			for (Entry<Position, List<List<Position>>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					List<Position> movedPosList = new ArrayList<>(entry.getValue()).get(0);
					for (int i = 0; i < movedPosList.size() - 1; i++) {
						Position fixedFrom = movedPosList.get(i);
						Position fixedTo = movedPosList.get(i + 1);
						if (i == 0) {
							sb.append("<circle cy=\"" + (fixedFrom.getyIndex() * baseSize + (baseSize / 2) + margin)
									+ "\" cx=\"" + (fixedFrom.getxIndex() * baseSize + baseSize + (baseSize / 2))
									+ "\" r=\"" + (baseSize / 2 - 2) + "\" fill=\"gray\", stroke=\"gray\">"
									+ "</circle>");
						}
						sb.append("<circle cy=\"" + (fixedFrom.getyIndex() * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\"" + (fixedFrom.getxIndex() * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\"" + (baseSize / 4 - 2) + "\" fill=\"gray\", stroke=\"gray\">" + "</circle>");
						if (i == movedPosList.size() - 2) {
							sb.append("<circle cy=\"" + (fixedTo.getyIndex() * baseSize + (baseSize / 2) + margin)
									+ "\" cx=\"" + (fixedTo.getxIndex() * baseSize + baseSize + (baseSize / 2))
									+ "\" r=\"" + (baseSize / 2 - 2) + "\" fill=\"lightgray\", stroke=\"black\">"
									+ "</circle>");
							if (field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] != -1) {
								String numberStr = String.valueOf(
										field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] - i - 1);
								int index = HALF_NUMS.indexOf(numberStr);
								String masuStr = null;
								if (index >= 0) {
									masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
								} else {
									masuStr = numberStr;
								}
								sb.append("<text y=\"" + (fixedTo.getyIndex() * baseSize + baseSize + margin - 4)
										+ "\" x=\"" + (fixedTo.getxIndex() * baseSize + baseSize + 2)
										+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
										+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
							}
						}
						if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
							for (int yIndex = fixedFrom.getyIndex(); yIndex > fixedTo.getyIndex(); yIndex--) {
								sb.append("<line y1=\""
										+ (yIndex * baseSize - (yIndex == fixedTo.getyIndex() + 1 ? baseSize / 2 : 0)
												+ margin)
										+ "\" x1=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
										+ "\" y2=\""
										+ (yIndex * baseSize + baseSize
												- (yIndex == fixedFrom.getyIndex() ? baseSize / 2 : 0) + margin)
										+ "\" x2=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
										+ "\" stroke-width=\"1\" fill=\"none\"");
								sb.append("stroke=\"green\" ");
								sb.append(">" + "</line>");
							}
						} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
							for (int xIndex = fixedFrom.getxIndex(); xIndex < fixedTo.getxIndex(); xIndex++) {
								sb.append("<line y1=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
										+ "\" x1=\""
										+ (xIndex * baseSize + baseSize
												+ (xIndex == fixedFrom.getxIndex() ? baseSize / 2 : 0))
										+ "\" y2=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
										+ "\" x2=\""
										+ (xIndex * baseSize + baseSize + baseSize
												+ (xIndex == fixedTo.getxIndex() - 1 ? baseSize / 2 : 0))
										+ "\" stroke-width=\"1\" fill=\"none\"");
								sb.append("stroke=\"green\" ");
								sb.append(">" + "</line>");
							}
						} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
							for (int yIndex = fixedFrom.getyIndex(); yIndex < fixedTo.getyIndex(); yIndex++) {
								sb.append("<line y1=\""
										+ (yIndex * baseSize + (yIndex == fixedFrom.getyIndex() ? baseSize / 2 : 0)
												+ margin)
										+ "\" x1=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
										+ "\" y2=\""
										+ (yIndex * baseSize + baseSize
												+ (yIndex == fixedTo.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
										+ "\" x2=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
										+ "\" stroke-width=\"1\" fill=\"none\"");
								sb.append("stroke=\"green\" ");
								sb.append(">" + "</line>");
							}
						} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
							for (int xIndex = fixedFrom.getxIndex(); xIndex > fixedTo.getxIndex(); xIndex--) {
								sb.append("<line y1=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
										+ "\" x1=\""
										+ (xIndex * baseSize + baseSize
												- (xIndex == fixedTo.getxIndex() + 1 ? baseSize / 2 : 0))
										+ "\" y2=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
										+ "\" x2=\""
										+ (xIndex * baseSize + baseSize + baseSize
												- (xIndex == fixedFrom.getxIndex() ? baseSize / 2 : 0))
										+ "\" stroke-width=\"1\" fill=\"none\"");
								sb.append("stroke=\"green\" ");
								sb.append(">" + "</line>");
							}
						}
					}
				} else {
					// 途中まで確定した候補を表示
					List<Position> halfwayCand = new ArrayList<>();
					int idx = 0;
					outer: while (true) {
						Position oneCand = null;
						for (List<Position> movedPosList : entry.getValue()) {
							if (movedPosList.size() < idx) {
								break outer;
							} else {
								if (oneCand != null && !oneCand.equals(movedPosList.get(idx))) {
									break outer;
								} else {
									oneCand = movedPosList.get(idx);
								}
							}
						}
						halfwayCand.add(oneCand);
						idx++;
					}
					if (halfwayCand.size() == 1) {
						sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\"" + (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">"
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
									+ "\" x=\"" + (originPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					} else {
						for (int i = 0; i < halfwayCand.size() - 1; i++) {
							Position fixedFrom = halfwayCand.get(i);
							Position fixedTo = halfwayCand.get(i + 1);
							if (i == 0) {
								sb.append("<circle cy=\"" + (fixedFrom.getyIndex() * baseSize + (baseSize / 2) + margin)
										+ "\" cx=\"" + (fixedFrom.getxIndex() * baseSize + baseSize + (baseSize / 2))
										+ "\" r=\"" + (baseSize / 2 - 2) + "\" fill=\"gray\", stroke=\"gray\">"
										+ "</circle>");
							}
							sb.append("<circle cy=\"" + (fixedFrom.getyIndex() * baseSize + (baseSize / 2) + margin)
									+ "\" cx=\"" + (fixedFrom.getxIndex() * baseSize + baseSize + (baseSize / 2))
									+ "\" r=\"" + (baseSize / 4 - 2) + "\" fill=\"gray\", stroke=\"gray\">"
									+ "</circle>");
							if (i == halfwayCand.size() - 2) {
								sb.append("<circle cy=\"" + (fixedTo.getyIndex() * baseSize + (baseSize / 2) + margin)
										+ "\" cx=\"" + (fixedTo.getxIndex() * baseSize + baseSize + (baseSize / 2))
										+ "\" r=\"" + (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">"
										+ "</circle>");
								if (field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] != -1) {
									String numberStr = String.valueOf(
											field.getNumbers()[originPos.getyIndex()][originPos.getxIndex()] - i - 1);
									int index = HALF_NUMS.indexOf(numberStr);
									String masuStr = null;
									if (index >= 0) {
										masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
									} else {
										masuStr = numberStr;
									}
									sb.append("<text y=\"" + (fixedTo.getyIndex() * baseSize + baseSize + margin - 4)
											+ "\" x=\"" + (fixedTo.getxIndex() * baseSize + baseSize + 2)
											+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
											+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
								}
							}
							if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
								for (int yIndex = fixedFrom.getyIndex(); yIndex > fixedTo.getyIndex(); yIndex--) {
									sb.append("<line y1=\""
											+ (yIndex * baseSize
													- (yIndex == fixedTo.getyIndex() + 1 ? baseSize / 2 : 0) + margin)
											+ "\" x1=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
											+ "\" y2=\""
											+ (yIndex * baseSize + baseSize
													- (yIndex == fixedFrom.getyIndex() ? baseSize / 2 : 0) + margin)
											+ "\" x2=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
											+ "\" stroke-width=\"1\" fill=\"none\"");
									sb.append("stroke=\"green\" ");
									sb.append(">" + "</line>");
								}
							} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
								for (int xIndex = fixedFrom.getxIndex(); xIndex < fixedTo.getxIndex(); xIndex++) {
									sb.append("<line y1=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
											+ "\" x1=\""
											+ (xIndex * baseSize + baseSize
													+ (xIndex == fixedFrom.getxIndex() ? baseSize / 2 : 0))
											+ "\" y2=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
											+ "\" x2=\""
											+ (xIndex * baseSize + baseSize + baseSize
													+ (xIndex == fixedTo.getxIndex() - 1 ? baseSize / 2 : 0))
											+ "\" stroke-width=\"1\" fill=\"none\"");
									sb.append("stroke=\"green\" ");
									sb.append(">" + "</line>");
								}
							} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
								for (int yIndex = fixedFrom.getyIndex(); yIndex < fixedTo.getyIndex(); yIndex++) {
									sb.append("<line y1=\""
											+ (yIndex * baseSize + (yIndex == fixedFrom.getyIndex() ? baseSize / 2 : 0)
													+ margin)
											+ "\" x1=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
											+ "\" y2=\""
											+ (yIndex * baseSize + baseSize
													+ (yIndex == fixedTo.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
											+ "\" x2=\"" + (fixedTo.getxIndex() * baseSize + baseSize + baseSize / 2)
											+ "\" stroke-width=\"1\" fill=\"none\"");
									sb.append("stroke=\"green\" ");
									sb.append(">" + "</line>");
								}
							} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
								for (int xIndex = fixedFrom.getxIndex(); xIndex > fixedTo.getxIndex(); xIndex--) {
									sb.append("<line y1=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
											+ "\" x1=\""
											+ (xIndex * baseSize + baseSize
													- (xIndex == fixedTo.getxIndex() + 1 ? baseSize / 2 : 0))
											+ "\" y2=\"" + (fixedTo.getyIndex() * baseSize + baseSize / 2 + margin)
											+ "\" x2=\""
											+ (xIndex * baseSize + baseSize + baseSize
													- (xIndex == fixedFrom.getxIndex() ? baseSize / 2 : 0))
											+ "\" stroke-width=\"1\" fill=\"none\"");
									sb.append("stroke=\"green\" ");
									sb.append(">" + "</line>");
								}
							}
						}
					}
				}

			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class IcelomSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		private final int start;
		private final int goal;
		private final boolean isIcelom2;

		IcelomSolverThread(int height, int width, String param, int start, int goal, boolean isIcelom2) {
			super(height, width, param);
			this.start = start;
			this.goal = goal;
			this.isIcelom2 = isIcelom2;
		}

		@Override
		protected Solver getSolver() {
			return new IcelomSolver(height, width, param, start, goal, isIcelom2);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			IcelomSolver.Field field = ((IcelomSolver) solver).getField();
			int baseSize = 20;
			int margin = baseSize;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getNumbers()[yIndex][xIndex] != -1
								&& !(field.getStartPos() != null
										&& field.getStartPos().equals(new Position(yIndex, xIndex)))
								&& !(field.getGoalPos() != null
										&& field.getGoalPos().equals(new Position(yIndex, xIndex)))) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
									+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
					if (field.getIcebarnPosSet().contains(pos)) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"" + "lightblue" + "\">" + "</rect>");
					}
					String str = "";
					Wall up = field.getTateExtraWall()[yIndex][xIndex];
					Wall right = field.getYokoExtraWall()[yIndex][xIndex + 1];
					Wall down = field.getTateExtraWall()[yIndex + 1][xIndex];
					Wall left = field.getYokoExtraWall()[yIndex][xIndex];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┼";
					} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
						str = "┌";
					} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "　";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
							+ "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MakaroSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MakaroSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MakaroSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MakaroSolver.Field field = ((MakaroSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						if (field.getNumbersCand()[yIndex][xIndex].get(0) == -1) {
							sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
									+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
							if (field.getArrows()[yIndex][xIndex] != null) {
								int lengthAdjust = 0;
								Direction dir = field.getArrows()[yIndex][xIndex];
								if (dir == Direction.UP || dir == Direction.DOWN) {
									lengthAdjust = 6;
								}
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
										+ (xIndex * baseSize + baseSize + (lengthAdjust / 2)) + "\" fill=\"" + "white"
										+ "\" font-size=\"" + (baseSize - 2) + "\" textLength=\""
										+ (baseSize - 2 - lengthAdjust) + "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ dir.getDirectString() + "</text>");
							}
						} else {
							String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
							String masuStr;
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class TentaishoSolverThread extends AbsSolverThlead {

		TentaishoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TentaishoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			TentaishoSolver.Field field = ((TentaishoSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// マスを塗る
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						Integer number = field.getNumbersCand()[yIndex][xIndex].get(0);
						if (field.isBlack(number)) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
									+ (baseSize) + "\" fill=\"darkgray\" >" + "</rect>");
						}
					}
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| !new ArrayList<>(field.getNumbersCand()[yIndex][xIndex])
									.removeAll(field.getNumbersCand()[yIndex][xIndex + 1]);
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| !new ArrayList<>(field.getNumbersCand()[yIndex][xIndex])
									.removeAll(field.getNumbersCand()[yIndex + 1][xIndex]);
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
			// 丸描画
			for (Entry<Integer, Position> entry : field.getNumbers().entrySet()) {
				if (field.isBlack(entry.getKey())) {
					sb.append("<circle cy=\""
							+ (entry.getValue().getyIndex() * (baseSize / 2) + (baseSize / 2) + margin) + "\" cx=\""
							+ (entry.getValue().getxIndex() * (baseSize / 2) + (baseSize / 2) + baseSize) + "\" r=\""
							+ 2 + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
				} else {
					sb.append("<circle cy=\""
							+ (entry.getValue().getyIndex() * (baseSize / 2) + (baseSize / 2) + margin) + "\" cx=\""
							+ (entry.getValue().getxIndex() * (baseSize / 2) + (baseSize / 2) + baseSize) + "\" r=\""
							+ 2 + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class HeyabonSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final boolean heyabon;

		HeyabonSolverThread(int height, int width, String param, boolean heyabon) {
			super(height, width, param);
			this.heyabon = heyabon;
		}

		@Override
		protected Solver getSolver() {
			return new HeyabonSolver(height, width, param, heyabon);
		}

		@Override
		public String makeCambus() {
			HeyabonSolver.Field field = ((HeyabonSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (Entry<Position, Set<Position>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					Position movedPos = new ArrayList<>(entry.getValue()).get(0);
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"gray\", stroke=\"gray\">" + "</circle>");
					sb.append("<circle cy=\"" + (movedPos.getyIndex() * baseSize + (baseSize / 2) + margin) + "\" cx=\""
							+ (movedPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"lightgray\", stroke=\"black\">" + "</circle>");
					if (movedPos.getyIndex() < originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex > movedPos.getyIndex(); yIndex--) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize - (yIndex == movedPos.getyIndex() + 1 ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											- (yIndex == originPos.getyIndex() ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() > originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex < movedPos.getxIndex(); xIndex++) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											+ (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											+ (xIndex == movedPos.getxIndex() - 1 ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getyIndex() > originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex < movedPos.getyIndex(); yIndex++) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + (yIndex == originPos.getyIndex() ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											+ (yIndex == movedPos.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() < originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex > movedPos.getxIndex(); xIndex--) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											- (xIndex == movedPos.getxIndex() + 1 ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											- (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
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
						sb.append("<text y=\"" + (movedPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (movedPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				} else {
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
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
						sb.append("<text y=\"" + (originPos.getyIndex() * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (originPos.getxIndex() * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}

			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getRoomYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getRoomTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class GokigenSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		GokigenSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new GokigenSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			GokigenSolver.Field field = ((GokigenSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">" + "</line>");
				}
			}
			// 斜め線描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
								+ ((yIndex + 1) * baseSize + baseSize / 2 + margin) + "\" x2=\""
								+ ((xIndex + 1) * baseSize + baseSize + baseSize / 2)
								+ "\" stroke-width=\"2\" fill=\"none\"");
						sb.append("stroke=\"black\" ");
						sb.append(">" + "</line>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
								+ ((xIndex + 1) * baseSize + baseSize + baseSize / 2) + "\" y2=\""
								+ ((yIndex + 1) * baseSize + baseSize / 2 + margin) + "\" x2=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2)
								+ "\" stroke-width=\"2\" fill=\"none\"");
						sb.append("stroke=\"black\" ");
						sb.append(">" + "</line>");
					}
				}
			}

			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					Integer number = field.getExtraNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 3) + "\" font-size=\"" + (baseSize - 6)
								+ "\" textLength=\"" + (baseSize - 6) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class CreekSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		CreekSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new CreekSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			CreekSolver.Field field = ((CreekSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// マス描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" fill=\"" + "#444" + "\">" + "</rect>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getMasu()[yIndex][xIndex] + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					Integer number = field.getExtraNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 3) + "\" font-size=\"" + (baseSize - 6)
								+ "\" textLength=\"" + (baseSize - 6) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class TasquareSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		TasquareSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TasquareSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			TasquareSolver.Field field = ((TasquareSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\" fill=\"white\" stroke-width=\"1\" stroke=\"black\">" + "\">"
								+ "</rect>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class GeradewegSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		GeradewegSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new GeradewegSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			GeradewegSolver.Field field = ((GeradewegSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr = null;
						if (numberStr.equals("-1")) {
							masuStr = "？";
						} else {
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class KurottoSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KurottoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KurottoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			KurottoSolver.Field field = ((KurottoSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class TatamibariSolverThread extends AbsSolverThlead {

		TatamibariSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TatamibariSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			TatamibariSolver.Field field = ((TatamibariSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.existYokoWall(yIndex, xIndex);
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.existTateWall(yIndex, xIndex);
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
			// 記号描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getNumbers()[yIndex][xIndex] == 1) {
							sb.append("<line y1=\"" + (yIndex * baseSize + 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						} else if (field.getNumbers()[yIndex][xIndex] == 2) {
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + 3) + "\" y2=\""
									+ (yIndex * baseSize + baseSize / 2 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						} else if (field.getNumbers()[yIndex][xIndex] == 3) {

							sb.append("<line y1=\"" + (yIndex * baseSize + 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + 3) + "\" y2=\""
									+ (yIndex * baseSize + baseSize / 2 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"2\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class WblinkSolverThread extends AbsSolverThlead {

		WblinkSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new WblinkSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			WblinkSolver.Field field = ((WblinkSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			// 記号描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getWhite()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
					}
					if (field.getBlack()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
				}
			}

			for (Entry<Position, Set<Position>> entry : field.getCandidates().entrySet()) {
				Position originPos = entry.getKey();
				if (entry.getValue().size() == 1) {
					Position movedPos = new ArrayList<>(entry.getValue()).get(0);
					if (movedPos.getyIndex() < originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex > movedPos.getyIndex(); yIndex--) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize - (yIndex == movedPos.getyIndex() + 1 ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											- (yIndex == originPos.getyIndex() ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() > originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex < movedPos.getxIndex(); xIndex++) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											+ (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											+ (xIndex == movedPos.getxIndex() - 1 ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getyIndex() > originPos.getyIndex()) {
						for (int yIndex = originPos.getyIndex(); yIndex < movedPos.getyIndex(); yIndex++) {
							sb.append("<line y1=\""
									+ (yIndex * baseSize + (yIndex == originPos.getyIndex() ? baseSize / 2 : 0)
											+ margin)
									+ "\" x1=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" y2=\""
									+ (yIndex * baseSize + baseSize
											+ (yIndex == movedPos.getyIndex() - 1 ? baseSize / 2 : 0) + margin)
									+ "\" x2=\"" + (movedPos.getxIndex() * baseSize + baseSize + baseSize / 2)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					} else if (movedPos.getxIndex() < originPos.getxIndex()) {
						for (int xIndex = originPos.getxIndex(); xIndex > movedPos.getxIndex(); xIndex--) {
							sb.append("<line y1=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x1=\""
									+ (xIndex * baseSize + baseSize
											- (xIndex == movedPos.getxIndex() + 1 ? baseSize / 2 : 0))
									+ "\" y2=\"" + (movedPos.getyIndex() * baseSize + baseSize / 2 + margin)
									+ "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize
											- (xIndex == originPos.getxIndex() ? baseSize / 2 : 0))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"green\" ");
							sb.append(">" + "</line>");
						}
					}
				} else {
					sb.append("<circle cy=\"" + (originPos.getyIndex() * baseSize + (baseSize / 2) + margin)
							+ "\" cx=\"" + (originPos.getxIndex() * baseSize + baseSize + (baseSize / 2)) + "\" r=\""
							+ (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class AquariumSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String numberParam;
		private final boolean sameHeight;

		AquariumSolverThread(int height, int width, String param, String numberParam, boolean sameHeight) {
			super(height, width, param);
			this.numberParam = numberParam;
			this.sameHeight = sameHeight;
		}

		@Override
		protected Solver getSolver() {
			return new AquariumSolver(height, width, param, numberParam, sameHeight);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			AquariumSolver.Field field = ((AquariumSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize) + "\" >");
			// ヒント描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.getVarticalHints()[xIndex] != null) {
					String masuStr;
					String capacityStr = String.valueOf(field.getVarticalHints()[xIndex]);
					int index = HALF_NUMS.indexOf(capacityStr);
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = capacityStr;
					}
					sb.append("<text y=\"" + (baseSize + margin - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
							+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				if (field.getHorizonalHints()[yIndex] != null) {
					String masuStr;
					String capacityStr = String.valueOf(field.getHorizonalHints()[yIndex]);
					int index = HALF_NUMS.indexOf(capacityStr);
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = capacityStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4 + margin) + "\" x=\""
							+ (baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" textLength=\"" + (baseSize - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" fill=\"" + "dodgerblue" + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" fill=\"" + "dodgerblue" + "\" textLength=\"" + (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\""
								+ (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (1) + "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class CompassSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		CompassSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new CompassSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			CompassSolver.Field field = ((CompassSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 30;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 記号描画
			for (Entry<Integer, Compass> entry : field.getCompasses().entrySet()) {
				int yIndex = entry.getValue().getPos().getyIndex();
				int xIndex = entry.getValue().getPos().getxIndex();
				sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\"" + (xIndex * baseSize + baseSize)
						+ "\" y2=\"" + (yIndex * baseSize + baseSize + margin) + "\" x2=\""
						+ (xIndex * baseSize + baseSize + baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
				sb.append("stroke=\"#000\" ");
				sb.append(">" + "</line>");
				sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
						+ (xIndex * baseSize + baseSize + baseSize) + "\" y2=\""
						+ (yIndex * baseSize + baseSize + margin) + "\" x2=\"" + (xIndex * baseSize + baseSize)
						+ "\" stroke-width=\"1\" fill=\"none\"");
				sb.append("stroke=\"#000\" ");
				sb.append(">" + "</line>");
				if (entry.getValue().getLeftCnt() != -1) {
					String str;
					String numberStr = String.valueOf(entry.getValue().getLeftCnt());
					int index = HALF_NUMS.indexOf(numberStr);
					if (index >= 0) {
						str = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						str = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 2 / 3) + margin - 1) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) / 3 + "\" textLength=\""
							+ (baseSize) / 3 + "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
				if (entry.getValue().getUpCnt() != -1) {
					String str;
					String numberStr = String.valueOf(entry.getValue().getUpCnt());
					int index = HALF_NUMS.indexOf(numberStr);
					if (index >= 0) {
						str = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						str = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 3) + margin - 1) + "\" x=\""
							+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" font-size=\"" + (baseSize) / 3
							+ "\" textLength=\"" + (baseSize) / 3 + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
							+ "</text>");
				}
				if (entry.getValue().getDownCnt() != -1) {
					String str;
					String numberStr = String.valueOf(entry.getValue().getDownCnt());
					int index = HALF_NUMS.indexOf(numberStr);
					if (index >= 0) {
						str = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						str = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1) + "\" x=\""
							+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" font-size=\"" + (baseSize) / 3
							+ "\" textLength=\"" + (baseSize) / 3 + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
							+ "</text>");
				}
				if (entry.getValue().getRightCnt() != -1) {
					String str;
					String numberStr = String.valueOf(entry.getValue().getRightCnt());
					int index = HALF_NUMS.indexOf(numberStr);
					if (index >= 0) {
						str = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						str = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 2 / 3) + margin - 1) + "\" x=\""
							+ (xIndex * baseSize + baseSize + (baseSize * 2 / 3)) + "\" font-size=\"" + (baseSize) / 3
							+ "\" textLength=\"" + (baseSize) / 3 + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
							+ "</text>");
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| !new ArrayList<>(field.getNumbersCand()[yIndex][xIndex])
									.removeAll(field.getNumbersCand()[yIndex][xIndex + 1]);
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == field.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| !new ArrayList<>(field.getNumbersCand()[yIndex][xIndex])
									.removeAll(field.getNumbersCand()[yIndex + 1][xIndex]);
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex == -1 || yIndex == field.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class CastleSolverThlead extends AbsSolverThlead {

		CastleSolverThlead(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new CastleSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			CastleSolver.Field field = ((CastleSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == field.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex == -1 || yIndex == field.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					if (field.getArrows()[yIndex][xIndex] != null) {
						str = field.getArrows()[yIndex][xIndex].toStringWeb();
						String fillColor;
						String textColor;

						if (field.getArrows()[yIndex][xIndex].outSide() == Masu.BLACK) {
							fillColor = "black";
							textColor = "white";
						} else if (field.getArrows()[yIndex][xIndex].outSide() == Masu.NOT_BLACK) {
							fillColor = "white";
							textColor = "black";
						} else {
							fillColor = "lightgray";
							textColor = "black";
						}
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" stroke=\"" + "black" + "\" fill=\"" + fillColor + "\">" + "</rect>");
						if (field.getArrows()[yIndex][xIndex].getCount() != -1) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 3) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 1)
									+ "\" textLength=\"" + (baseSize - 1) + "\" fill=\"" + textColor
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}
					} else {
						Masu oneMasu = field.getMasu()[yIndex][xIndex];
						if (oneMasu.toString().equals("・")) {
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = oneMasu.toString();
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" fill=\"" + "green"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class DoublebackSolverThread extends AbsSolverThlead {
		DoublebackSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new DoublebackSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			DoublebackSolver.Field field = ((DoublebackSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getBlackPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" stroke=\"" + "black" + "\" fill=\"" + "black" + "\">" + "</rect>");
					} else {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class ChoconaSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ChoconaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ChoconaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ChoconaSolver.Field field = ((ChoconaSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NagenawaSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NagenawaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NagenawaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			NagenawaSolver.Field field = ((NagenawaSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┼";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (NagenawaSolver.Room room : field.getRooms()) {
				int roomCount = room.getWhiteCnt();
				if (roomCount != -1) {
					String roomCountStr;
					String wkstr = String.valueOf(roomCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 12) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize) + "\" fill=\"" + "black"
							+ "\" font-size=\"" + (baseSize / 2 - 2) + "\" textLength=\"" + (baseSize / 2 - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ViewSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		ViewSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new ViewSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			ViewSolver.Field field = ((ViewSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" fill=\""
								+ (field.getFixedPosSet().contains(new Position(yIndex, xIndex)) ? "black" : "green")
								+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					} else {
						if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
									+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "green" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "×" + "</text>");
						} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "green" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "○" + "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NuribouSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NuribouSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NuribouSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NuribouSolver.Field field = ((NuribouSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MidloopSolverThread extends AbsSolverThlead {

		MidloopSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MidloopSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MidloopSolver.Field field = ((MidloopSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (!oneYokoWall && field.getYokoCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 4
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
					if (!oneTateWall && field.getTateCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 4
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircles()[yIndex][xIndex]) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 4
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MaxiSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MaxiSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MaxiSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MaxiSolver.Field field = ((MaxiSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "・";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (MaxiSolver.Room room : field.getRooms()) {
				int roomWhiteCount = room.getWhiteCnt();
				if (roomWhiteCount != -1) {
					String roomWhiteCountStr;
					String wkstr = String.valueOf(roomWhiteCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomWhiteCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomWhiteCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 12) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "black"
							+ "\" font-size=\"" + (baseSize / 2 - 2) + "\" textLength=\"" + (baseSize / 2 - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomWhiteCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class BalanceSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		BalanceSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BalanceSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			BalanceSolver.Field field = ((BalanceSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"" + (field.isBlackNum()[yIndex][xIndex] ? "black" : "white")
								+ "\", stroke=\"black\">" + "</circle>");
						if (field.getNumbers()[yIndex][xIndex] != 0) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							String masuStr = null;
							if (numberStr.equals("-1")) {
								masuStr = "？";
							} else {
								int numIdx = HALF_NUMS.indexOf(numberStr);
								if (numIdx >= 0) {
									masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
								} else {
									masuStr = numberStr;
								}
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\""
									+ (field.isBlackNum()[yIndex][xIndex] ? "white" : "black")
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					}
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("・")) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = oneMasu.toString();
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class KazunoriSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		KazunoriSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new KazunoriSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			KazunoriSolver.Field field = ((KazunoriSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (xIndex != -1 && xIndex != field.getXLength() - 1
							&& field.getYokoWallNum()[yIndex][xIndex] != 0) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 5
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						String numberStr = String.valueOf(field.getYokoWallNum()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 6 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2) + 5) + "\" font-size=\"" + 10
								+ "\" textLength=\"" + 10 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
								+ "</text>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
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
					if (yIndex != -1 && yIndex != field.getYLength() - 1
							&& field.getTateWallNum()[yIndex][xIndex] != 0) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 5
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						String numberStr = String.valueOf(field.getTateWallNum()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + (baseSize / 2) - 6 + margin)
								+ "\" x=\"" + (xIndex * baseSize + baseSize + 5) + "\" font-size=\"" + 10
								+ "\" textLength=\"" + 10 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
								+ "</text>");
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MinarismSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MinarismSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MinarismSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MinarismSolver.Field field = ((MinarismSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (xIndex != -1 && xIndex != field.getXLength() - 1) {
						if (field.getYokoWallNum()[yIndex][xIndex] > 0) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 5
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
							String numberStr = String.valueOf(field.getYokoWallNum()[yIndex][xIndex]);
							String masuStr;
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 6 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) + 5) + "\" font-size=\"" + 10
									+ "\" textLength=\"" + 10 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
									+ "</text>");
						} else if (field.getYokoWallNum()[yIndex][xIndex] == -2) {
							sb.append("<line y1=\"" + (yIndex * baseSize + (baseSize / 2) - 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize + 3) + "\" y2=\""
									+ (yIndex * baseSize + (baseSize / 2) + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
							sb.append("<line y1=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3) + "\" y2=\""
									+ (yIndex * baseSize + (baseSize / 2) + 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize + 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						} else if (field.getYokoWallNum()[yIndex][xIndex] == -1) {
							sb.append("<line y1=\"" + (yIndex * baseSize + (baseSize / 2) - 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3) + "\" y2=\""
									+ (yIndex * baseSize + (baseSize / 2) + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize + 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
							sb.append("<line y1=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + baseSize + 3) + "\" y2=\""
									+ (yIndex * baseSize + (baseSize / 2) + 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + baseSize - 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						}
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
					if (yIndex != -1 && yIndex != field.getYLength() - 1) {
						if (field.getTateWallNum()[yIndex][xIndex] > 0) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 5
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
							String numberStr = String.valueOf(field.getTateWallNum()[yIndex][xIndex]);
							String masuStr;
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + (baseSize / 2) - 6 + margin)
									+ "\" x=\"" + (xIndex * baseSize + baseSize + 5) + "\" font-size=\"" + 10
									+ "\" textLength=\"" + 10 + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
									+ "</text>");
						} else if (field.getTateWallNum()[yIndex][xIndex] == -2) {
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) - 3) + "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize - 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" y2=\""
									+ (yIndex * baseSize + baseSize + 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) + 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						} else if (field.getTateWallNum()[yIndex][xIndex] == -1) {
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize - 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) - 3) + "\" y2=\""
									+ (yIndex * baseSize + baseSize + 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2))
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
							sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + 3 + margin) + "\" x1=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" y2=\""
									+ (yIndex * baseSize + baseSize - 3 + margin) + "\" x2=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2) + 3)
									+ "\" stroke-width=\"1\" fill=\"none\"");
							sb.append("stroke=\"#000\" ");
							sb.append(">" + "</line>");
						}
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class DoppelblockSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		DoppelblockSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new DoppelblockSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			DoppelblockSolver.Field field = ((DoppelblockSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String masuStr = "";
					if (field.getColumnCands()[xIndex].size() == 0) {
						masuStr = "×";
					} else if (field.getColumnCands()[xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getColumnCands()[xIndex].get(0).get(yIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							if (idx == 0) {
								sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
										+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
										+ "\" height=\"" + (baseSize) + "\" fill=\"black\" >" + "</rect>");
							} else {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							}
						} else {
							masuStr = numberStr;
						}
					} else if (field.getLineCands()[yIndex].size() == 1) {
						String numberStr = String.valueOf(field.getLineCands()[yIndex].get(0).get(xIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							if (idx == 0) {
								sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
										+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
										+ "\" height=\"" + (baseSize) + "\" fill=\"black\" >" + "</rect>");
							} else {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							}
						} else {
							masuStr = numberStr;
						}
					} else {
						int candNum = -1;
						if (candNum != 0) {
							for (List<Integer> cc : field.getColumnCands()[xIndex]) {
								if (cc.get(yIndex) != candNum) {
									if (candNum == -1) {
										candNum = cc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							for (List<Integer> lc : field.getLineCands()[yIndex]) {
								if (lc.get(xIndex) != candNum) {
									if (candNum == -1) {
										candNum = lc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							String numberStr = String.valueOf(candNum);
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						} else {
							sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
									+ "\" height=\"" + (baseSize) + "\" fill=\"black\" >" + "</rect>");
						}
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "green"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize + baseSize) + "\" width=\"" + (1) + "\" height=\""
								+ (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (1) + "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class SimpleloopSolverThread extends AbsSolverThlead {

		SimpleloopSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SimpleloopSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SimpleloopSolver.Field field = ((SimpleloopSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == field.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex == -1 || yIndex == field.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					if (field.getMasu()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" stroke=\"" + "black" + "\" fill=\"" + "black" + "\">" + "</rect>");
					} else {
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.EXISTS) {
							str = "┌";
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┐";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
								+ "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class BoxSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		BoxSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BoxSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			BoxSolver.Field field = ((BoxSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" fill=\"black\" >" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getMasu()[yIndex][xIndex].toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2)) + "\" cx=\""
						+ (field.getXLength() * baseSize + baseSize + baseSize + (baseSize / 2)) + "\" r=\""
						+ (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
				String numberStr = String.valueOf(yIndex + 1);
				int index = HALF_NUMS.indexOf(numberStr);
				String masuStr = null;
				if (index >= 0) {
					masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
				} else {
					masuStr = numberStr;
				}
				sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\""
						+ (field.getXLength() * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
						+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
						+ "</text>");
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				sb.append("<circle cy=\"" + (field.getYLength() * baseSize + baseSize + (baseSize / 2)) + "\" cx=\""
						+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
						+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
				String numberStr = String.valueOf(xIndex + 1);
				int index = HALF_NUMS.indexOf(numberStr);
				String masuStr = null;
				if (index >= 0) {
					masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
				} else {
					masuStr = numberStr;
				}
				sb.append("<text y=\"" + (field.getYLength() * baseSize + baseSize + baseSize - 4) + "\" x=\""
						+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
						+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
						+ "</text>");

			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MejilinkSolverThread extends AbsSolverThlead {

		MejilinkSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MejilinkSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			MejilinkSolver.Field field = ((MejilinkSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 点描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					sb.append(
							"<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\"" + (xIndex * baseSize + baseSize)
									+ "\" r=\"" + 1 + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					boolean oneYokoWall = field.getYokoExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
					} else if (field.getYokoHeyaWall()[yIndex][xIndex]) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ ((xIndex - 1) * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = field.getTateExtraWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
					} else if (field.getTateHeyaWall()[yIndex][xIndex]) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class TentsSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		TentsSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TentsSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			TentsSolver.Field field = ((TentsSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getTrees()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + 13) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize + 8) + "\" width=\"" + (baseSize / 5)
								+ "\" height=\"" + (baseSize / 5) + "\" fill=\"black\" >" + "</rect>");
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + 8) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2)) + "\" r=\""
								+ (baseSize / 2 - 4) + "\" fill=\"green\", stroke=\"black\">" + "</circle>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" fill=\"" + "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "△" + "</text>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" fill=\"" + "green"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + field.getMasu()[yIndex][xIndex].toString()
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 横線描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.getYokoWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2) + "\" x1=\""
								+ (xIndex * baseSize + 2 * baseSize + baseSize / 2) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + baseSize / 2) + "\" x2=\""
								+ (xIndex * baseSize + 3 * baseSize + baseSize / 2)
								+ "\" stroke-width=\"1\" fill=\"green\"");
						sb.append("stroke=\"green\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			// 縦線描画
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getTateWall()[yIndex][xIndex] == Wall.NOT_EXISTS) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2) + "\" x1=\""
								+ (xIndex * baseSize + 3 * baseSize - baseSize / 2) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + baseSize + baseSize / 2) + "\" x2=\""
								+ (xIndex * baseSize + 3 * baseSize - baseSize / 2)
								+ "\" stroke-width=\"1\" fill=\"green\"");
						sb.append("stroke=\"green\" ");
						sb.append(">" + "</line>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class WalllogicSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		WalllogicSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new WalllogicSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			WalllogicSolver.Field field = ((WalllogicSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == field.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex == -1 || yIndex == field.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					} else {
						String str = "";
						if (field.getNumbersCand()[yIndex][xIndex].size() == 1
								&& field.getNumbersCand()[yIndex][xIndex].get(0) == 1) {
							if (yIndex == 0 || !field.getNumbersCand()[yIndex - 1][xIndex].contains(1)) {
								str = "↑";
							} else {
								str = "│";
							}
						} else if (field.getNumbersCand()[yIndex][xIndex].size() == 1
								&& field.getNumbersCand()[yIndex][xIndex].get(0) == 2) {
							if (xIndex == field.getXLength() - 1
									|| !field.getNumbersCand()[yIndex][xIndex + 1].contains(2)) {
								str = "→";
							} else {
								str = "─";
							}
						} else if (field.getNumbersCand()[yIndex][xIndex].size() == 1
								&& field.getNumbersCand()[yIndex][xIndex].get(0) == 3) {
							if (yIndex == field.getYLength() - 1
									|| !field.getNumbersCand()[yIndex + 1][xIndex].contains(3)) {
								str = "↓";
							} else {
								str = "│";
							}
						} else if (field.getNumbersCand()[yIndex][xIndex].size() == 1
								&& field.getNumbersCand()[yIndex][xIndex].get(0) == 4) {
							if (xIndex == 0 || !field.getNumbersCand()[yIndex][xIndex - 1].contains(4)) {
								str = "←";
							} else {
								str = "─";
							}
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class SnakeSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SnakeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SnakeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SnakeSolver.Field field = ((SnakeSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" fill=\"gray\" >" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getMasu()[yIndex][xIndex].toString() + "</text>");
					}
					if (field.getOnRoutePosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2)) + "\" r=\""
								+ (baseSize / 2 - 2) + "\" fill=\"white\", stroke=\"black\">" + "</circle>");
					} else if (field.getEndPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2)) + "\" r=\""
								+ (baseSize / 2 - 2) + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class DetourSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		DetourSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new DetourSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			DetourSolver.Field field = ((DetourSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┐";
					} else {
						str = "・";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (1) + "\" height=\"" + (baseSize)
								+ "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (DetourSolver.Room room : field.getRooms()) {
				int roomWhiteCount = room.getCurveCnt();
				if (roomWhiteCount != -1) {
					String roomWhiteCountStr;
					String wkstr = String.valueOf(roomWhiteCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomWhiteCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomWhiteCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 12) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "black"
							+ "\" font-size=\"" + (baseSize / 2 - 2) + "\" textLength=\"" + (baseSize / 2 - 2)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomWhiteCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NurimazeSolverThread extends AbsSolverThlead {
		NurimazeSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NurimazeSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			NurimazeSolver.Field field = ((NurimazeSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");

			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
					if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"" + "palegreen" + "\">" + "</rect>");

					}
					if (field.getRoute()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"" + "lightskyblue" + "\">" + "</rect>");
					}
					if (field.getMark()[yIndex][xIndex] != null) {
						if (field.getMark()[yIndex][xIndex] == Mark.OK) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
									+ "\" fill=\"" + "lightskyblue" + "\", stroke=\"black\">" + "</circle>");
						} else {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "black"
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ field.getMark()[yIndex][xIndex].toString() + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class TateyokoSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		TateyokoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TateyokoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			TateyokoSolver.Field field = ((TateyokoSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					String str = "";
					if (oneMasu.toString().equals("・")) {
						str = "─";
					} else if (oneMasu.toString().equals("■")) {
						str = "│";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 3) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 1) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" stroke=\"green\" stroke-width=\"1"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");

				}
			}
			// 数字
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getBlackPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					} else {
						if (field.getNumbers()[yIndex][xIndex] != null) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "black" + "\" font-size=\""
									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class BuildingSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		BuildingSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new BuildingSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			BuildingSolver.Field field = ((BuildingSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4) + "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\"" + (baseSize + 2)
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String masuStr = "";
					if (field.getColumnCands()[xIndex].size() == 0) {
						masuStr = "×";
					} else if (field.getColumnCands()[xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getColumnCands()[xIndex].get(0).get(yIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
					} else if (field.getLineCands()[yIndex].size() == 1) {
						String numberStr = String.valueOf(field.getLineCands()[yIndex].get(0).get(xIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
					} else {
						int candNum = -1;
						if (candNum != 0) {
							for (List<Integer> cc : field.getColumnCands()[xIndex]) {
								if (cc.get(yIndex) != candNum) {
									if (candNum == -1) {
										candNum = cc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							for (List<Integer> lc : field.getLineCands()[yIndex]) {
								if (lc.get(xIndex) != candNum) {
									if (candNum == -1) {
										candNum = lc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							String numberStr = String.valueOf(candNum);
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						}
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "green"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				if (field.getRightHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getRightHints()[yIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (field.getXLength() * baseSize + baseSize + baseSize + 2) + "\" font-size=\""
							+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				if (field.getDownHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getDownHints()[xIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (field.getYLength() * baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class PutteriaSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		PutteriaSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new PutteriaSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			PutteriaSolver.Field field = ((PutteriaSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (field.getFixedMasuSet().contains(pos)) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
					}
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						String numberStr = String.valueOf(field.getRoomSize(pos));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\""
								+ (field.getFixedMasuSet().contains(pos) ? "black" : "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" fill=\""
								+ (field.getFixedMasuSet().contains(pos) ? "black" : "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + (field.getFixedMasuSet().contains(pos) ? "×"
										: field.getMasu()[yIndex][xIndex].toString())
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class TilepaintSolverThread extends AbsSolverThlead {

		TilepaintSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new TilepaintSolver(height, width, param);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			TilepaintSolver.Field field = ((TilepaintSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex - 1, xIndex - 1);
					myamya.other.solver.tilepaint.TilepaintSolver.Block block = field.getBlocks().get(pos);
					if (block != null || yIndex == 0 || xIndex == 0) {
						sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize + margin)
								+ " L" + (xIndex * baseSize + baseSize + baseSize) + " " + (yIndex * baseSize + margin)
								+ " L" + (xIndex * baseSize + baseSize + baseSize) + " "
								+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
						if (block != null && pos.getxIndex() < field.getXLength() - 1
								&& (block.getLeftCnt() != 0 || !field.getBlocks()
										.containsKey(new Position(pos.getyIndex(), pos.getxIndex() + 1)))) {
							String masuStr;
							String numberStr = String.valueOf(block.getLeftCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) - 1 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" fill=\"" + "white"
									+ "\" font-size=\"" + (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
						sb.append("<path d=\"M " + (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize + margin)
								+ " L" + (xIndex * baseSize + baseSize) + " " + (yIndex * baseSize + baseSize + margin)
								+ " L" + (xIndex * baseSize + baseSize + baseSize) + " "
								+ (yIndex * baseSize + baseSize + margin) + " Z\" >" + "</path>");
						if (block != null && pos.getyIndex() < field.getYLength() - 1
								&& (block.getDownCnt() != 0 || !field.getBlocks()
										.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex())))) {
							String masuStr;
							String numberStr = String.valueOf(block.getDownCnt());
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 1 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "white" + "\" font-size=\""
									+ (baseSize + 2) / 2 + "\" textLength=\"" + (baseSize + 2) / 2
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");

						}
					} else {
						Masu oneMasu = field.getMasu()[pos.getyIndex()][pos.getxIndex()];
						if (oneMasu.toString().equals("■")) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
									+ (baseSize) + "\" fill=\"" + "#444" + "\">" + "</rect>");
						}
						if (oneMasu.toString().equals("・")) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
									+ (baseSize) + "\" fill=\"" + "palegreen" + "\">" + "</rect>");
						}
					}
				}
			}

			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength() + 1; xIndex++) {
					boolean oneYokoWall = yIndex <= 0 || xIndex <= 0 || yIndex >= field.getYLength() + 1
							|| xIndex >= field.getXLength() || field.getYokoWall()[yIndex - 1][xIndex - 1];
					if (oneYokoWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
						if (oneYokoWall) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
						}
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					boolean oneTateWall = yIndex <= 0 || xIndex <= 0 || yIndex >= field.getYLength()
							|| xIndex >= field.getXLength() + 1 || field.getTateWall()[yIndex - 1][xIndex - 1];
					if (oneTateWall) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						if (oneTateWall) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
						}
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class RenbanSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		RenbanSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new RenbanSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			RenbanSolver.Field field = ((RenbanSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
								+ "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1
							&& field.getNumbersCand()[yIndex][xIndex].get(0) != -1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}

	}

	static class EasyasabcSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "　ＡＢＣＤＥＦＧＨＩ";

		private final int kind;

		EasyasabcSolverThread(int height, int width, int kind, String param) {
			super(height, width, param);
			this.kind = kind;
		}

		@Override
		protected Solver getSolver() {
			return new EasyasabcSolver(height, width, kind, param);
		}

		@Override
		public String makeCambus() {
			EasyasabcSolver.Field field = ((EasyasabcSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 3 * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 3 * baseSize + baseSize) + "\" >");
			sb.append("<text y=\"" + (baseSize - 5) + "\" x=\"" + ((field.getXLength()) * baseSize + baseSize)
					+ "\" font-size=\"" + (baseSize - 4) + "\" fill=\"" + "black"
					+ "\" lengthAdjust=\"spacingAndGlyphs\">" + "(Ａ-"
					+ FULL_NUMS.substring(field.getKind(), field.getKind() + 1) + ")" + "</text>");
			for (int xIndex = 0; xIndex < field.getUpHints().length; xIndex++) {
				if (field.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getLeftHints().length; yIndex++) {
				if (field.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String masuStr = "";
					if (field.getColumnCands()[xIndex].size() == 0) {
						masuStr = "×";
					} else if (field.getColumnCands()[xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getColumnCands()[xIndex].get(0).get(yIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
					} else if (field.getLineCands()[yIndex].size() == 1) {
						String numberStr = String.valueOf(field.getLineCands()[yIndex].get(0).get(xIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
					} else {
						int candNum = -1;
						if (candNum != 0) {
							for (List<Integer> cc : field.getColumnCands()[xIndex]) {
								if (cc.get(yIndex) != candNum) {
									if (candNum == -1) {
										candNum = cc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							for (List<Integer> lc : field.getLineCands()[yIndex]) {
								if (lc.get(xIndex) != candNum) {
									if (candNum == -1) {
										candNum = lc.get(yIndex);
									} else {
										candNum = 0;
										break;
									}
								}
							}
						}
						if (candNum != 0) {
							String numberStr = String.valueOf(candNum);
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						}
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
							+ "\" textLength=\"" + (baseSize - 5) + "\" fill=\"" + "green"
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				if (field.getRightHints()[yIndex] != null) {
					String numberStr = String.valueOf(field.getRightHints()[yIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize + baseSize - 4) + "\" x=\""
							+ (field.getXLength() * baseSize + baseSize + baseSize + 2) + "\" font-size=\""
							+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			// 縦壁描画
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize + baseSize) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize + baseSize) + "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
				if (field.getDownHints()[xIndex] != null) {
					String numberStr = String.valueOf(field.getDownHints()[xIndex]);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (field.getYLength() * baseSize + baseSize + baseSize + baseSize - 4)
							+ "\" x=\"" + (xIndex * baseSize + baseSize + baseSize + 2) + "\" font-size=\""
							+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MochikoroSolverThread extends AbsSolverThlead {

		private final boolean isMochinyoro;

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MochikoroSolverThread(int height, int width, String param, boolean isMochinyoro) {
			super(height, width, param);
			this.isMochinyoro = isMochinyoro;
		}

		@Override
		protected Solver getSolver() {
			return new MochikoroSolver(height, width, param, isMochinyoro, false);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			MochikoroSolver.Field field = ((MochikoroSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class MeanderSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MeanderSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MeanderSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			MeanderSolver.Field field = ((MeanderSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + baseSize)
								+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\" fill=\"lightgray\" >"
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class IchimagaSolverThread extends AbsSolverThlead {

		private final boolean ichimagam;

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		IchimagaSolverThread(int height, int width, String param, boolean ichimagam) {
			super(height, width, param);
			this.ichimagam = ichimagam;
		}

		@Override
		protected Solver getSolver() {
			return new IchimagaSolver(height, width, param, ichimagam);
		}

		@Override
		public String makeCambus() {
			IchimagaSolver.Field field = ((IchimagaSolver) solver).getField();
			int baseSize = 20;
			StringBuilder sb = new StringBuilder();
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getNumbers()[yIndex][xIndex];
					String str = "";
					if (number != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2)) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						if (number != -1) {
							String numberStr = String.valueOf(number);
							int index = HALF_NUMS.indexOf(numberStr);
							if (index >= 0) {
								str = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ str + "</text>");
						}
					} else {
						if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
							Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
							Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
									: field.getYokoWall()[yIndex][xIndex];
							Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
									: field.getTateWall()[yIndex][xIndex];
							Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
							if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.EXISTS) {
								str = "└";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "│";
							} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┘";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.EXISTS) {
								str = "┌";
							} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "─";
							} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
									&& left == Wall.NOT_EXISTS) {
								str = "┐";
							} else {
								str = "・";
							}
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + (str.equals("？") ? "black" : "green")
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class FillmatSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		FillmatSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new FillmatSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			FillmatSolver.Field field = ((FillmatSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "black" + "\" font-size=\""
								+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			boolean[][] yokoWall = field.getYokoWall();
			boolean[][] tateWall = field.getTateWall();
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1 || yokoWall[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1 || tateWall[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class CojunSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		CojunSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new CojunSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			CojunSolver.Field field = ((CojunSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class MinesSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		MinesSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new MinesSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			MinesSolver.Field field = ((MinesSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
					} else if (field.getNumbers()[yIndex][xIndex] != null) {
						if (field.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (index >= 0) {
								masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class SukororoomSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SukororoomSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SukororoomSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SukororoomSolver.Field field = ((SukororoomSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
					}
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						if (field.getNumbersCand()[yIndex][xIndex].get(0) == 0) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ "×" + "</text>");
						} else {
							String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
							String masuStr;
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class SimplegakoSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		SimplegakoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new SimplegakoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			SimplegakoSolver.Field field = ((SimplegakoSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" fill=\"lightgray\" >" + "</rect>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class NonogramSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		NonogramSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new NonogramSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NonogramSolver.Field field = ((NonogramSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			int margin = 5;
			int upHintsAdjust = (field.getYLength() + 1) / 2;
			int leftHintsAdjust = (field.getXLength() + 1) / 2;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ ((field.getYLength() + upHintsAdjust) * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ ((field.getXLength() + leftHintsAdjust) * baseSize + 2 * baseSize) + "\" >");
			// 横ヒント配置
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				List<Integer> wkList = new ArrayList<>(Arrays.asList(field.getLeftHints()[yIndex]));
				Collections.reverse(wkList);
				for (int xIndex = 0; xIndex < leftHintsAdjust; xIndex++) {
					if (wkList.get(xIndex) != null) {
						String numberStr = String.valueOf(wkList.get(xIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + ((yIndex + upHintsAdjust) * baseSize + baseSize - 4 + margin)
								+ "\" x=\"" + (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 縦ヒント配置
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				List<Integer> wkList = new ArrayList<>(Arrays.asList(field.getUpHints()[xIndex]));
				Collections.reverse(wkList);
				for (int yIndex = 0; yIndex < upHintsAdjust; yIndex++) {
					if (wkList.get(yIndex) != null) {
						String numberStr = String.valueOf(wkList.get(yIndex));
						int idx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ ((xIndex + leftHintsAdjust) * baseSize + baseSize + 2) + "\" font-size=\""
								+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// マス描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + ((yIndex + upHintsAdjust) * baseSize + margin) + "\" x=\""
								+ ((xIndex + leftHintsAdjust) * baseSize + baseSize) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + ((yIndex + upHintsAdjust) * baseSize + baseSize - 4 + margin)
								+ "\" x=\"" + ((xIndex + leftHintsAdjust) * baseSize + baseSize) + "\" font-size=\""
								+ (baseSize - 2) + "\" textLength=\"" + (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (

					int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
					sb.append("<line y1=\"" + ((yIndex + upHintsAdjust) * baseSize + margin) + "\" x1=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + 2 * baseSize) + "\" y2=\""
							+ ((yIndex + upHintsAdjust) * baseSize + baseSize + margin) + "\" x2=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
					sb.append("<line y1=\"" + ((yIndex + upHintsAdjust) * baseSize + baseSize + margin) + "\" x1=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + baseSize) + "\" y2=\""
							+ ((yIndex + upHintsAdjust) * baseSize + baseSize + margin) + "\" x2=\""
							+ ((xIndex + leftHintsAdjust) * baseSize + baseSize + baseSize)
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
			return sb.toString();
		}

	}

	static class AqreSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		AqreSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new AqreSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			AqreSolver.Field field = ((AqreSolver) solver).getField();
			int baseSize = 20;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Common.Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + 2) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" width=\"" + (baseSize - 4) + "\" height=\""
								+ (baseSize - 4) + "\">" + "</rect>");
					} else {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2) + "\" fill=\""
								+ "lime" + "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneMasu.toString() + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize) + "\" x=\"" + (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\"" + (1) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (1)
								+ "\">" + "</rect>");
					}
				}
			}
			// 数字描画
			for (AqreSolver.Room room : field.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = field.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class LookairSolverThread extends AbsSolverThlead {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		LookairSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new LookairSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			LookairSolver.Field field = ((LookairSolver) solver).getField();
			Masu[][] masu = field.getMasu();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = masu[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "palegreen" + "\" width=\""
								+ (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
								+ (oneMasu.toString().equals("■") ? "white" : "black") + "\" textLength=\""
								+ (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class HeterominoSolverThread extends AbsSolverThlead {

		HeterominoSolverThread(int height, int width, String param) {
			super(height, width, param);
		}

		@Override
		protected Solver getSolver() {
			return new HeterominoSolver(height, width, param);
		}

		@Override
		public String makeCambus() {
			HeterominoSolver.Field field = ((HeterominoSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 黒マス描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin + 0.5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 0.5) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" stroke=\"" + "black" + "\" fill=\"" + "black" + "\">" + "</rect>");
					}
				}
			}
			// 横壁描画
			boolean[][] yokoWall = field.getYokoWall();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1 || yokoWall[yIndex][xIndex];
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize) + "\" width=\"" + (2) + "\" height=\"" + (baseSize)
								+ "\" fill=\""
								+ ((xIndex == -1 || xIndex == field.getXLength() - 1) ? "black" : "green") + "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			boolean[][] tateWall = field.getTateWall();
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1 || tateWall[yIndex][xIndex];
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\"" + (2)
								+ "\" fill=\""
								+ ((yIndex == -1 || yIndex == field.getYLength() - 1) ? "black" : "green") + "\">"
								+ "</rect>");
					}
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class NibunnogoSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String fieldStr;

		NibunnogoSolverThread(int height, int width, String param) {
			super(height, width, param);
			this.fieldStr = null;
		}

		// penpa-edit向けコンストラクタ
		public NibunnogoSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			if (fieldStr != null) {
				return new NibunnogoSolver(fieldStr);
			} else {
				return new NibunnogoSolver(height, width, param);
			}
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			NibunnogoSolver.Field field = ((NibunnogoSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// マス描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\" fill=\"" + "#444" + "\">" + "</rect>");
					} else if (field.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ field.getMasu()[yIndex][xIndex] + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
					Integer number = field.getExtraNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 3) + "\" font-size=\"" + (baseSize - 6)
								+ "\" textLength=\"" + (baseSize - 6) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class ChocobananaSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public ChocobananaSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new ChocobananaSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			ChocobananaSolver.Field field = ((ChocobananaSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "palegreen" + "\" width=\""
								+ (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
								+ (oneMasu.toString().equals("■") ? "white" : "black") + "\" textLength=\""
								+ (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class DotchiloopSolverThread extends AbsSolverThlead {

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public DotchiloopSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new DotchiloopSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			DotchiloopSolver.Field field = ((DotchiloopSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getCircles()[yIndex][xIndex] != 0) {
						if (field.getCircles()[yIndex][xIndex] == 1) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						} else if (field.getCircles()[yIndex][xIndex] == 2) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						}

					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoRoomWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateRoomWall()[yIndex][xIndex];
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					String str = "";
					Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
					Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex];
					Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS : field.getTateWall()[yIndex][xIndex];
					Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
					if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.EXISTS) {
						str = "└";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "│";
					} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┘";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.EXISTS) {
						str = "┌";
					} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS && down == Wall.EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "─";
					} else if (up == Wall.EXISTS && right == Wall.EXISTS && down == Wall.NOT_EXISTS
							&& left == Wall.NOT_EXISTS) {
						str = "┐";
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2) + "\" x=\""
							+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
							+ (baseSize) + "\" fill=\"" + "green" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str
							+ "</text>");
				}
			}
			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class LasSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public LasSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new LasSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			LasSolver.Field field = ((LasSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Masu oneMasu = field.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "palegreen" + "\" width=\""
								+ (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
					if (field.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(field.getNumbers()[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (index >= 0) {
							masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
								+ (oneMasu.toString().equals("■") ? "white" : "black") + "\" textLength=\""
								+ (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			return sb.toString();
		}
	}

	static class WhitelinkSolverThread extends AbsSolverThlead {

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public WhitelinkSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new WhitelinkSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			StringBuilder sb = new StringBuilder();
			WhitelinkSolver.Field field = ((WhitelinkSolver) solver).getField();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1;
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1;
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
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getMasu()[yIndex][xIndex] == Masu.BLACK) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" width=\"" + (baseSize) + "\" height=\""
								+ (baseSize) + "\" stroke=\"" + "gray" + "\" fill=\"" + "gray" + "\">" + "</rect>");
					} else {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS : field.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == field.getXLength() - 1 ? Wall.EXISTS
								: field.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == field.getYLength() - 1 ? Wall.EXISTS
								: field.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : field.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS
								&& left == Wall.NOT_EXISTS) {
							str = "┼";
						} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
							str = "┌";
						} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = "　";
						}
						String fillColor;
						if (field.getFirstPosSet().contains(new Position(yIndex, xIndex))) {
							fillColor = "black";
						} else {
							fillColor = "green";
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) + "\" textLength=\""
								+ (baseSize) + "\" fill=\"" + fillColor + "\" stroke=\"" + fillColor
								+ "\" stroke-width=\"1" + "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
					}
				}
			}

			sb.append("</svg>");
			return sb.toString();
		}
	}

	static class LinedozenSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public LinedozenSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new LinedozenSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			LinedozenSolver.Field field = ((LinedozenSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// ライン描画
			boolean[][] yokoConnect = new boolean[field.getYLength()][field.getXLength() - 1];
			boolean[][] tateConnect = new boolean[field.getYLength() - 1][field.getXLength()];
			// 壁セット(表示のため)
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isConnect = false;
					for (Set<Position> line : field.getLines()) {
						if (line.contains(pos)) {
							Position rightPos = new Position(yIndex, xIndex + 1);
							if (line.contains(rightPos)) {
								isConnect = true;
								break;
							}
						}
					}
					yokoConnect[yIndex][xIndex] = isConnect;
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isConnect = false;
					for (Set<Position> line : field.getLines()) {
						if (line.contains(pos)) {
							Position downPos = new Position(yIndex + 1, xIndex);
							if (line.contains(downPos)) {
								isConnect = true;
								break;
							}
						}
					}
					tateConnect[yIndex][xIndex] = isConnect;
				}
			}
			// ライン描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean forYoko = xIndex != field.getXLength() - 1 && yokoConnect[yIndex][xIndex];
					boolean forTate = yIndex != field.getYLength() - 1 && tateConnect[yIndex][xIndex];
					if (forYoko) {
						sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin - 10) + "\" x1=\""
								+ (xIndex * baseSize + baseSize + 10) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin - 10) + "\" x2=\""
								+ (xIndex * baseSize + baseSize + baseSize + 10)
								+ "\" stroke-width=\"2\" fill=\"none\"");
						sb.append("stroke=\"#888\" ");
						sb.append(">" + "</line>");
					}
					if (forTate) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin + 10) + "\" x1=\""
								+ (xIndex * baseSize + 2 * baseSize - 10) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin + 10) + "\" x2=\""
								+ (xIndex * baseSize + 2 * baseSize - 10) + "\" stroke-width=\"2\" fill=\"none\"");
						sb.append("stroke=\"#888\" ");
						sb.append(">" + "</line>");
					}
				}
			}

			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(field.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5)
								+ "\" fill=\"green\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex];
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
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex];
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
			return sb.toString();
		}
	}

	static class WafusumaSolverThread extends AbsSolverThlead {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final String fieldStr;

		// penpa-edit向けコンストラクタ
		public WafusumaSolverThread(String fieldStr) {
			super(0, 0, "");
			this.fieldStr = fieldStr;
		}

		@Override
		protected Solver getSolver() {
			return new WafusumaSolver(fieldStr);
		}

		@Override
		public String makeCambus() {
			WafusumaSolver.Field field = ((WafusumaSolver) solver).getField();
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (field.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (field.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < field.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == field.getXLength() - 1
							|| field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex != -1 && xIndex != field.getXLength() - 1
								&& field.getYokoWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == field.getYLength() - 1
							|| field.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex != -1 && yIndex != field.getYLength() - 1
								&& field.getTateWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");

						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					Integer number = field.getYokoNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + (baseSize / 2 - 5)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 7) + "\" x=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2) + 5) + "\" font-size=\""
								+ (baseSize - 11) + "\" textLength=\"" + (baseSize - 11)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					Integer number = field.getTateNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 5)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + (baseSize / 2) + margin - 7)
								+ "\" x=\"" + (xIndex * baseSize + baseSize + 5) + "\" font-size=\"" + (baseSize - 11)
								+ "\" textLength=\"" + (baseSize - 11) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
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
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			AbsSolverThlead t;
			if (request.getParameter("fieldStr") != null) {
				// penpa-edit
				String puzzleType = request.getParameter("type");
				if (puzzleType.contains("nibunnogo")) {
					t = new NibunnogoSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("chocobanana")) {
					t = new ChocobananaSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("dotchiloop")) {
					t = new DotchiloopSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("las")) {
					t = new LasSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("whitelink")) {
					t = new WhitelinkSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("linedozen")) {
					t = new LinedozenSolverThread(request.getParameter("fieldStr"));
				} else if (puzzleType.contains("wafusuma")) {
					t = new WafusumaSolverThread(request.getParameter("fieldStr"));
				} else {
					throw new IllegalArgumentException();
				}
			} else {
				List<String> parts = getURLparts(request.getParameter("url"));
				String puzzleType = parts.get(0);
				if (puzzleType.contains("icelom")) {
					boolean isIcelom2 = parts.get(1).equals("b");
					if (isIcelom2) {
						// TODO アイスローム2は未完成なので封じる
						throw new IllegalArgumentException();
					}
					int height = Integer.parseInt(parts.get(3));
					int width = Integer.parseInt(parts.get(2));
					String param = parts.get(4).split("@")[0];
					int start = Integer.parseInt(parts.get(5));
					int goal = Integer.parseInt(parts.get(6));
					t = new IcelomSolverThread(height, width, param, start, goal, isIcelom2);
				} else if (puzzleType.contains("bonsan")) {
					boolean heyabon = !parts.get(1).equals("c");
					int height = Integer.parseInt(parts.get(3));
					int width = Integer.parseInt(parts.get(2));
					String param = parts.get(4).split("@")[0];
					t = new HeyabonSolverThread(height, width, param, heyabon);
				} else if (puzzleType.contains("aquarium")) {
					boolean sameHeight = parts.get(1).equals("r");
					if (sameHeight) {
						int height = Integer.parseInt(parts.get(3));
						int width = Integer.parseInt(parts.get(2));
						String param = parts.get(4).split("@")[0];
						String numberParam = parts.get(5).split("@")[0];
						t = new AquariumSolverThread(height, width, param, numberParam, sameHeight);
					} else {
						int height = Integer.parseInt(parts.get(2));
						int width = Integer.parseInt(parts.get(1));
						String param = parts.get(3).split("@")[0];
						String numberParam = parts.get(4).split("@")[0];
						t = new AquariumSolverThread(height, width, param, numberParam, sameHeight);
					}
				} else if (puzzleType.contains("yajilin") || puzzleType.contains("yajirin")) {
					boolean isGray = parts.get(1).equals("b");
					int width;
					int height;
					String param;
					if (isGray) {
						width = Integer.parseInt(parts.get(2));
						height = Integer.parseInt(parts.get(3));
						param = parts.get(4).split("@")[0];
					} else {
						width = Integer.parseInt(parts.get(1));
						height = Integer.parseInt(parts.get(2));
						param = parts.get(3).split("@")[0];
					}
					if (puzzleType.contains("regions")) {
						t = new YajilinRegionsSolverThlead(height, width, param, puzzleType.contains("out"));
					} else {
						t = new YajilinSolverThlead(height, width, param, puzzleType.contains("out"));
					}
				} else {
					int width = Integer.parseInt(parts.get(1));
					int height = Integer.parseInt(parts.get(2));
					String param = parts.get(3).split("@")[0];
					if (puzzleType.contains("nurikabe")) {
						t = new NurikabeSolverThread(height, width, param);
					} else if (puzzleType.contains("stostone")) {
						t = new StostoneSolverThread(height, width, param);
					} else if (puzzleType.contains("heyawake") || puzzleType.contains("heyawacky")
							|| puzzleType.contains("ayeheya")) {
						t = new HeyawakeSolverThread(height, width, param, puzzleType.contains("ayeheya"));
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
					} else if (puzzleType.contains("bag") || puzzleType.contains("cave")) {
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
					} else if (puzzleType.contains("rectslider")) {
						t = new RectsliderSolverThread(height, width, param);
					} else if (puzzleType.contains("hebi") || puzzleType.contains("snakes")) {
						t = new HebiSolverThread(height, width, param);
					} else if (puzzleType.contains("shwolf")) {
						t = new ShwolfSolverThread(height, width, param);
					} else if (puzzleType.contains("shugaku")) {
						t = new ShugakuSolverThread(height, width, param);
					} else if (puzzleType.contains("angleloop")) {
						t = new AngleloopSolverThread(height, width, param);
						// } else if (puzzleType.contains("numlin") ||
						// puzzleType.contains("numberlink")) {
						// t = new NumlinSolverThread(height, width, param);
					} else if (puzzleType.contains("hashi")) {
						t = new HashikakeSolverThread(height, width, param);
					} else if (puzzleType.contains("cells")) {
						t = new CellsSolverThread(height, width, param, puzzleType.contains("fourcells") ? 4 : 5);
					} else if (puzzleType.contains("kurochute")) {
						t = new KurochuteSolverThread(height, width, param);
					} else if (puzzleType.contains("nondango")) {
						t = new NondangoSolverThread(height, width, param);
					} else if (puzzleType.contains("juosan")) {
						t = new JuosanSolverThread(height, width, param);
					} else if (puzzleType.contains("moonsun")) {
						t = new MoonsunSolverThread(height, width, param);
					} else if (puzzleType.contains("scrin")) {
						t = new ScrinSolverThread(height, width, param);
					} else if (puzzleType.contains("usoone")) {
						t = new UsooneSolverThread(height, width, param);
					} else if (puzzleType.contains("kakuro")) {
						t = new KakuroSolverThread(height, width, param);
					} else if (puzzleType.contains("kropki")) {
						t = new KropkiSolverThread(height, width, param);
					} else if (puzzleType.contains("nanro")) {
						t = new NanroSolverThread(height, width, param);
					} else if (puzzleType.contains("sukoro")) {
						if (puzzleType.contains("sukororoom")) {
							t = new SukororoomSolverThread(height, width, param);
						} else {
							t = new SukoroSolverThread(height, width, param);
						}
					} else if (puzzleType.contains("hakoiri")) {
						t = new HakoiriSolverThread(height, width, param);
					} else if (puzzleType.contains("hanare")) {
						t = new HanareSolverThread(height, width, param);
					} else if (puzzleType.contains("icebarn")) {
						int start = Integer.parseInt(parts.get(4));
						int goal = Integer.parseInt(parts.get(5));
						t = new IcebarnSolverThread(height, width, param, start, goal);
					} else if (puzzleType.contains("roma")) {
						t = new RomaSolverThread(height, width, param);
					} else if (puzzleType.contains("herugolf")) {
						t = new HerugolfSolverThread(height, width, param);
					} else if (puzzleType.contains("makaro")) {
						t = new MakaroSolverThread(height, width, param);
					} else if (puzzleType.contains("tentaisho")) {
						t = new TentaishoSolverThread(height, width, param);
					} else if (puzzleType.contains("heyabon")) {
						t = new HeyabonSolverThread(height, width, param, true);
					} else if (puzzleType.contains("gokigen")) {
						t = new GokigenSolverThread(height, width, param);
					} else if (puzzleType.contains("creek")) {
						t = new CreekSolverThread(height, width, param);
					} else if (puzzleType.contains("tasquare")) {
						t = new TasquareSolverThread(height, width, param);
					} else if (puzzleType.contains("geradeweg")) {
						t = new GeradewegSolverThread(height, width, param);
					} else if (puzzleType.contains("kurotto")) {
						t = new KurottoSolverThread(height, width, param);
					} else if (puzzleType.contains("tatamibari")) {
						t = new TatamibariSolverThread(height, width, param);
					} else if (puzzleType.contains("wblink")) {
						t = new WblinkSolverThread(height, width, param);
					} else if (puzzleType.contains("compass")) {
						t = new CompassSolverThread(height, width, param);
					} else if (puzzleType.contains("castle")) {
						t = new CastleSolverThlead(height, width, param);
					} else if (puzzleType.contains("doubleback")) {
						t = new DoublebackSolverThread(height, width, param);
					} else if (puzzleType.contains("chocona")) {
						t = new ChoconaSolverThread(height, width, param);
					} else if (puzzleType.contains("nagenawa")) {
						t = new NagenawaSolverThread(height, width, param);
					} else if (puzzleType.contains("view")) {
						t = new ViewSolverThread(height, width, param);
					} else if (puzzleType.contains("nuribou")) {
						t = new NuribouSolverThread(height, width, param);
					} else if (puzzleType.contains("midloop")) {
						t = new MidloopSolverThread(height, width, param);
					} else if (puzzleType.contains("maxi")) {
						t = new MaxiSolverThread(height, width, param);
					} else if (puzzleType.contains("balance")) {
						t = new BalanceSolverThread(height, width, param);
					} else if (puzzleType.contains("kazunori")) {
						t = new KazunoriSolverThread(height, width, param);
					} else if (puzzleType.contains("minarism")) {
						t = new MinarismSolverThread(height, width, param);
					} else if (puzzleType.contains("doppelblock")) {
						t = new DoppelblockSolverThread(height, width, param);
					} else if (puzzleType.contains("simpleloop")) {
						t = new SimpleloopSolverThread(height, width, param);
					} else if (puzzleType.contains("box")) {
						t = new BoxSolverThread(height, width, param);
					} else if (puzzleType.contains("mejilink")) {
						t = new MejilinkSolverThread(height, width, param);
					} else if (puzzleType.contains("tents")) {
						t = new TentsSolverThread(height, width, param);
					} else if (puzzleType.contains("walllogic")) {
						t = new WalllogicSolverThread(height, width, param);
					} else if (puzzleType.contains("snake")) {
						t = new SnakeSolverThread(height, width, param);
					} else if (puzzleType.contains("detour")) {
						t = new DetourSolverThread(height, width, param);
					} else if (puzzleType.contains("nurimaze")) {
						t = new NurimazeSolverThread(height, width, param);
					} else if (puzzleType.contains("tateyoko")) {
						t = new TateyokoSolverThread(height, width, param);
					} else if (puzzleType.contains("building") || puzzleType.contains("skyscraper")) {
						t = new BuildingSolverThread(height, width, param);
					} else if (puzzleType.contains("putteria")) {
						t = new PutteriaSolverThread(height, width, param);
					} else if (puzzleType.contains("tilepaint")) {
						t = new TilepaintSolverThread(height, width, param);
					} else if (puzzleType.contains("yajitatami")) {
						t = new YajitatamiSolverThread(height, width, param);
					} else if (puzzleType.contains("renban")) {
						t = new RenbanSolverThread(height, width, param);
					} else if (puzzleType.contains("easyasabc")) {
						int kind = Integer.parseInt(parts.get(3));
						param = parts.get(4).split("@")[0];
						t = new EasyasabcSolverThread(height, width, kind, param);
					} else if (puzzleType.contains("mochikoro")) {
						t = new MochikoroSolverThread(height, width, param, false);
					} else if (puzzleType.contains("mochinyoro")) {
						t = new MochikoroSolverThread(height, width, param, true);
					} else if (puzzleType.contains("meander")) {
						t = new MeanderSolverThread(height, width, param);
					} else if (puzzleType.contains("ichimaga")) {
						t = new IchimagaSolverThread(height, width, param, puzzleType.contains("ichimagam"));
					} else if (puzzleType.contains("fillmat")) {
						t = new FillmatSolverThread(height, width, param);
					} else if (puzzleType.contains("cojun")) {
						t = new CojunSolverThread(height, width, param);
					} else if (puzzleType.contains("mines")) {
						t = new MinesSolverThread(height, width, param);
					} else if (puzzleType.contains("simplegako")) {
						t = new SimplegakoSolverThread(height, width, param);
					} else if (puzzleType.contains("nonogram")) {
						t = new NonogramSolverThread(height, width, param);
					} else if (puzzleType.contains("aqre")) {
						t = new AqreSolverThread(height, width, param);
					} else if (puzzleType.contains("lookair")) {
						t = new LookairSolverThread(height, width, param);
					} else if (puzzleType.contains("heteromino")) {
						t = new HeterominoSolverThread(height, width, param);
					} else if (puzzleType.contains("nibunogo") || puzzleType.contains("nibunnogo")
							|| puzzleType.contains("nibunnnogo")) {
						// 表記ゆれ対応
						t = new NibunnogoSolverThread(height, width, param);
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
			t.start();
			t.join(28000);
			resultMap.put("result", t.makeCambus());
			resultMap.put("status", t.getStatus());
			// あんまり行儀がいいとは言えないが負荷軽減のため試しに入れてみる
			t.stop();
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
		if (parts.get(2).equals("puzsq.jp")) {
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
			puzpreParts = new ArrayList<String>(Arrays.asList(puzpreParts.get(puzpreParts.size() - 1).split("/")));
			// 波及効果バリアントはシンプルガコとみなす
			if (puzpreParts.get(1).equals("v:")) {
				puzpreParts.remove(1);
				if (puzpreParts.get(0).equals("ripple")) {
					puzpreParts.set(0, "simplegako");
				}
			}
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