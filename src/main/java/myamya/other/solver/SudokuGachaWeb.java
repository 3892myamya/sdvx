package myamya.other.solver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.akari.AkariSolver.AkariGenerator;
import myamya.other.solver.bag.BagSolver.BagGenerator;
import myamya.other.solver.balance.BalanceSolver.BalanceGenerator;
import myamya.other.solver.barns.BarnsSolver.BarnsGenerator;
import myamya.other.solver.box.BoxSolver.BoxGenerator;
import myamya.other.solver.building.BuildingSolver.BuildingGenerator;
import myamya.other.solver.creek.CreekSolver.CreekGenerator;
import myamya.other.solver.geradeweg.GeradewegSolver.GeradewegGenerator;
import myamya.other.solver.gokigen.GokigenSolver.GokigenGenerator;
import myamya.other.solver.hakoiri.HakoiriSolver.HakoiriGenerator;
import myamya.other.solver.herugolf.HerugolfSolver.HerugolfGenerator;
import myamya.other.solver.heyawake.HeyawakeSolver.HeyawakeGenerator;
import myamya.other.solver.hitori.HitoriSolver.HitoriGenerator;
import myamya.other.solver.icebarn.IcebarnSolver.IcebarnGenerator;
import myamya.other.solver.kurodoko.KurodokoSolver.KurodokoGenerator;
import myamya.other.solver.kurotto.KurottoSolver.KurottoGenerator;
import myamya.other.solver.masyu.MasyuSolver.MasyuGenerator;
import myamya.other.solver.midloop.MidloopSolver.MidloopGenerator;
import myamya.other.solver.minarism.MinarismSolver.MinarismGenerator;
import myamya.other.solver.moonsun.MoonsunSolver.MoonsunGenerator;
import myamya.other.solver.nanro.NanroSolver.NanroGenerator;
import myamya.other.solver.norinori.NorinoriSolver.NorinoriGenerator;
import myamya.other.solver.nurikabe.NurikabeSolver.NurikabeGenerator;
import myamya.other.solver.nurimaze.NurimazeSolver.NurimazeGenerator;
import myamya.other.solver.nurimisaki.NurimisakiSolver.NurimisakiGenerator;
import myamya.other.solver.pipelink.PipelinkSolver.PipelinkGenerator;
import myamya.other.solver.putteria.PutteriaSolver.PutteriaGenerator;
import myamya.other.solver.reflect.ReflectSolver.ReflectGenerator;
import myamya.other.solver.renban.RenbanSolver.RenbanGenerator;
import myamya.other.solver.ripple.RippleSolver.RippleGenerator;
import myamya.other.solver.sashigane.SashiganeSolver.SashiganeGenerator;
import myamya.other.solver.shakashaka.ShakashakaSolver.ShakashakaGenerator;
import myamya.other.solver.shugaku.ShugakuSolver.ShugakuGenerator;
import myamya.other.solver.simpleloop.SimpleloopSolver.SimpleloopGenerator;
import myamya.other.solver.slither.SlitherSolver.SlitherGenerator;
import myamya.other.solver.snake.SnakeSolver.SnakeGenerator;
import myamya.other.solver.starbattle.StarBattleSolver.StarBattleGenerator;
import myamya.other.solver.sudoku.SudokuSolver.SudokuGenerator;
import myamya.other.solver.sukoro.SukoroSolver.SukoroGenerator;
import myamya.other.solver.tapa.TapaSolver.TapaGenerator;
import myamya.other.solver.tasquare.TasquareSolver.TasquareGenerator;
import myamya.other.solver.tatamibari.TatamibariSolver.TatamibariGenerator;
import myamya.other.solver.tateyoko.TateyokoSolver.TateyokoGenerator;
import myamya.other.solver.tentaisho.TentaishoSolver.TentaishoGenerator;
import myamya.other.solver.tents.TentsSolver.TentsGenerator;
import myamya.other.solver.tilepaint.TilepaintSolver.TilepaintGenerator;
import myamya.other.solver.usoone.UsooneSolver.UsooneGenerator;
import myamya.other.solver.walllogic.WalllogicSolver.WalllogicGenerator;
import myamya.other.solver.yajikazu.YajikazuSolver.YajikazuGenerator;
import myamya.other.solver.yajilin.YajilinSolver.YajilinGenerator;
import myamya.other.solver.yajitatami.YajitatamiSolver.YajitatamiGenerator;
import myamya.other.solver.yinyang.YinyangSolver.YinyangGenerator;
import net.arnx.jsonic.JSON;

@WebServlet("/SudokuGacha")
public class SudokuGachaWeb extends HttpServlet {

	abstract static class GeneratorThlead extends Thread {
		private GeneratorResult result = new GeneratorResult(
				"申し訳ありません。時間内に抽選が完了しませんでした。時間をおいて再度お試しください。");

		@Override
		public void run() {
			result = getGenerator().generate();
		}

		abstract Generator getGenerator();
	}

	static class SudokuGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		SudokuGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new SudokuGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class NurimisakiGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		NurimisakiGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new NurimisakiGenerator(height, width);
		}

	}

	static class ShakashakaGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		ShakashakaGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new ShakashakaGenerator(height, width);
		}

	}

	static class GokigenGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		GokigenGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new GokigenGenerator(height, width, HintPattern.getByVal(pattern, height + 1, width + 1));
		}

	}

	static class CreekGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		CreekGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new CreekGenerator(height, width, HintPattern.getByVal(pattern, height + 1, width + 1));
		}

	}

	static class TasquareGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		TasquareGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new TasquareGenerator(height, width);
		}

	}

	static class ReflectGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		ReflectGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new ReflectGenerator(height, width);
		}

	}

	static class SlitherGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		SlitherGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new SlitherGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class AkariGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		AkariGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new AkariGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class TapaGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		TapaGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new TapaGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class SashiganeGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		SashiganeGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new SashiganeGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class MasyuGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		MasyuGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new MasyuGenerator(height, width);
		}

	}

	static class GeradewegGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		GeradewegGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new GeradewegGenerator(height, width);
		}

	}

	static class BagGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		BagGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new BagGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class KurodokoGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		KurodokoGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new KurodokoGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class BarnsGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		BarnsGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new BarnsGenerator(height, width);
		}

	}

	static class MidloopGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		MidloopGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new MidloopGenerator(height, width);
		}

	}

	static class SukoroGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		SukoroGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new SukoroGenerator(height, width, HintPattern.getByVal(height >= 9 ? 0 : pattern, height, width));
		}

	}

	static class BalanceGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		BalanceGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new BalanceGenerator(height, width);
		}

	}

	static class MinarismGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		MinarismGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new MinarismGenerator(height, width);
		}

	}

	static class BoxGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		BoxGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new BoxGenerator(height, width);
		}

	}

	static class KurottoGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		KurottoGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new KurottoGenerator(height, width);
		}

	}

	static class TentsGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		TentsGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new TentsGenerator(height, width);
		}

	}

	static class WalllogicGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		WalllogicGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new WalllogicGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class NurikabeGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		NurikabeGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new NurikabeGenerator(height, width);
		}

	}

	static class SimpleloopGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		SimpleloopGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new SimpleloopGenerator(height, width);
		}

	}

	static class YinyangGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		YinyangGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new YinyangGenerator(height, width);
		}

	}

	static class HeyawakeGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		HeyawakeGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new HeyawakeGenerator(height, width);
		}

	}

	static class PipelinkGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		PipelinkGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new PipelinkGenerator(height, width);
		}

	}

	static class SnakeGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		SnakeGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new SnakeGenerator(height, width);
		}

	}

	static class TatamibariGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		TatamibariGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new TatamibariGenerator(height, width);
		}

	}

	static class TateyokoGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;
		protected final int pattern;

		TateyokoGeneratorThlead(int height, int width, int pattern) {
			this.height = height;
			this.width = width;
			this.pattern = pattern;
		}

		@Override
		Generator getGenerator() {
			return new TateyokoGenerator(height, width, HintPattern.getByVal(pattern, height, width));
		}

	}

	static class BuildingGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		BuildingGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new BuildingGenerator(height, width);
		}

	}

	static class YajikazuGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		YajikazuGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new YajikazuGenerator(height, width);
		}

	}

	static class ShugakuGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		ShugakuGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new ShugakuGenerator(height, width);
		}

	}

	static class StarbattleGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		StarbattleGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			// サイズ8以上なら★2、それ以下なら★1
			return new StarBattleGenerator(height, width, height >= 8 ? 2 : 1);
		}

	}

	static class PutteriaGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		PutteriaGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new PutteriaGenerator(height, width);
		}

	}

	static class NorinoriGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		NorinoriGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new NorinoriGenerator(height, width);
		}

	}

	static class TentaishoGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		TentaishoGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new TentaishoGenerator(height, width);
		}

	}

	static class YajilinGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		YajilinGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new YajilinGenerator(height, width);
		}

	}

	static class HerugolfGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		HerugolfGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new HerugolfGenerator(height, width);
		}

	}

	static class NanroGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		NanroGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new NanroGenerator(height, width);
		}

	}

	static class HakoiriGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		HakoiriGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new HakoiriGenerator(height, width);
		}

	}

	static class RippleGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		RippleGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new RippleGenerator(height, width);
		}

	}

	static class UsooneGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		UsooneGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new UsooneGenerator(height, width);
		}

	}

	static class NurimazeGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		NurimazeGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new NurimazeGenerator(height, width);
		}

	}

	static class TilepaintGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		TilepaintGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new TilepaintGenerator(height, width);
		}

	}

	static class HitoriGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		HitoriGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new HitoriGenerator(height, width);
		}

	}

	static class MoonsunGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		MoonsunGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new MoonsunGenerator(height, width);
		}

	}

	static class IcebarnGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		IcebarnGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new IcebarnGenerator(height, width);
		}

	}

	static class YajitatamiGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		YajitatamiGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new YajitatamiGenerator(height, width);
		}

	}

	static class RenbanGeneratorThlead extends GeneratorThlead {
		protected final int height;
		protected final int width;

		RenbanGeneratorThlead(int height, int width) {
			this.height = height;
			this.width = width;
		}

		@Override
		Generator getGenerator() {
			return new RenbanGenerator(height, width);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String type = request.getParameter("type");
			GeneratorThlead t;
			int height = Integer.parseInt(request.getParameter("height"));
			int width = Integer.parseInt(request.getParameter("width"));
			if (!type.equals("tilepaint") && (height > 10 || width > 10)) {
				throw new IllegalArgumentException();
			}
			if (type.equals("sudoku")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new SudokuGeneratorThlead(height, width, pattern);
			} else if (type.equals("nurimisaki")) {
				t = new NurimisakiGeneratorThlead(height, width);
			} else if (type.equals("shakashaka")) {
				t = new ShakashakaGeneratorThlead(height, width);
			} else if (type.equals("gokigen")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new GokigenGeneratorThlead(height, width, pattern);
			} else if (type.equals("creek")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new CreekGeneratorThlead(height, width, pattern);
			} else if (type.equals("tasquare")) {
				t = new TasquareGeneratorThlead(height, width);
			} else if (type.equals("reflect")) {
				t = new ReflectGeneratorThlead(height, width);
			} else if (type.equals("slither")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new SlitherGeneratorThlead(height, width, pattern);
			} else if (type.equals("akari")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new AkariGeneratorThlead(height, width, pattern);
			} else if (type.equals("tapa")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new TapaGeneratorThlead(height, width, pattern);
			} else if (type.equals("sashigane")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new SashiganeGeneratorThlead(height, width, pattern);
			} else if (type.equals("masyu")) {
				t = new MasyuGeneratorThlead(height, width);
			} else if (type.equals("geradeweg")) {
				t = new GeradewegGeneratorThlead(height, width);
			} else if (type.equals("bag")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new BagGeneratorThlead(height, width, pattern);
			} else if (type.equals("kurodoko")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new KurodokoGeneratorThlead(height, width, pattern);
			} else if (type.equals("barns")) {
				t = new BarnsGeneratorThlead(height, width);
			} else if (type.equals("midloop")) {
				t = new MidloopGeneratorThlead(height, width);
			} else if (type.equals("sukoro")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new SukoroGeneratorThlead(height, width, pattern);
			} else if (type.equals("balance")) {
				t = new BalanceGeneratorThlead(height, width);
			} else if (type.equals("minarism")) {
				t = new MinarismGeneratorThlead(height, width);
			} else if (type.equals("box")) {
				t = new BoxGeneratorThlead(height, width);
			} else if (type.equals("kurotto")) {
				t = new KurottoGeneratorThlead(height, width);
			} else if (type.equals("tents")) {
				t = new TentsGeneratorThlead(height, width);
			} else if (type.equals("walllogic")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new WalllogicGeneratorThlead(height, width, pattern);
			} else if (type.equals("nurikabe")) {
				t = new NurikabeGeneratorThlead(height, width);
			} else if (type.equals("simpleloop")) {
				t = new SimpleloopGeneratorThlead(height, width);
			} else if (type.equals("yinyang")) {
				t = new YinyangGeneratorThlead(height, width);
			} else if (type.equals("heyawake")) {
				t = new HeyawakeGeneratorThlead(height, width);
			} else if (type.equals("pipelink")) {
				t = new PipelinkGeneratorThlead(height, width);
			} else if (type.equals("snake")) {
				t = new SnakeGeneratorThlead(height, width);
			} else if (type.equals("tatamibari")) {
				t = new TatamibariGeneratorThlead(height, width);
			} else if (type.equals("tateyoko")) {
				int pattern = Integer.parseInt(request.getParameter("pattern"));
				t = new TateyokoGeneratorThlead(height, width, pattern);
			} else if (type.equals("building")) {
				t = new BuildingGeneratorThlead(height, width);
			} else if (type.equals("yajikazu")) {
				t = new YajikazuGeneratorThlead(height, width);
			} else if (type.equals("shugaku")) {
				t = new ShugakuGeneratorThlead(height, width);
			} else if (type.equals("starbattle")) {
				t = new StarbattleGeneratorThlead(height, width);
			} else if (type.equals("putteria")) {
				t = new PutteriaGeneratorThlead(height, width);
			} else if (type.equals("norinori")) {
				t = new NorinoriGeneratorThlead(height, width);
			} else if (type.equals("tentaisho")) {
				t = new TentaishoGeneratorThlead(height, width);
			} else if (type.equals("yajilin")) {
				t = new YajilinGeneratorThlead(height, width);
			} else if (type.equals("herugolf")) {
				t = new HerugolfGeneratorThlead(height, width);
			} else if (type.equals("nanro")) {
				t = new NanroGeneratorThlead(height, width);
			} else if (type.equals("hakoiri")) {
				t = new HakoiriGeneratorThlead(height, width);
			} else if (type.equals("ripple")) {
				t = new RippleGeneratorThlead(height, width);
			} else if (type.equals("usoone")) {
				t = new UsooneGeneratorThlead(height, width);
			} else if (type.equals("nurimaze")) {
				t = new NurimazeGeneratorThlead(height, width);
			} else if (type.equals("tilepaint")) {
				t = new TilepaintGeneratorThlead(height, width);
			} else if (type.equals("hitori")) {
				t = new HitoriGeneratorThlead(height, width);
			} else if (type.equals("moonsun")) {
				t = new MoonsunGeneratorThlead(height, width);
			} else if (type.equals("icebarn")) {
				t = new IcebarnGeneratorThlead(height, width);
			} else if (type.equals("yajitatami")) {
				t = new YajitatamiGeneratorThlead(height, width);
			} else if (type.equals("renban")) {
				t = new RenbanGeneratorThlead(height, width);
			} else {
				throw new IllegalArgumentException();
			}
			t.start();
			t.join(28000);
			resultMap.put("status", t.result.getStatus());
			resultMap.put("result", t.result.getResult());
			resultMap.put("link", t.result.getLink());
			resultMap.put("url", t.result.getUrl());
			resultMap.put("txt", t.result.getTxt());
			resultMap.put("level", t.result.getLevel());
		} catch (

		Exception e) {
			e.printStackTrace();
			resultMap.put("status", "予期せぬエラーが発生しました。");
			resultMap.put("result", "");
		}
		try (
				PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

}