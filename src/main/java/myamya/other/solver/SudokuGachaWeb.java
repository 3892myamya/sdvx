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
import myamya.other.solver.creek.CreekSolver.CreekGenerator;
import myamya.other.solver.geradeweg.GeradewegSolver.GeradewegGenerator;
import myamya.other.solver.gokigen.GokigenSolver.GokigenGenerator;
import myamya.other.solver.masyu.MasyuSolver.MasyuGenerator;
import myamya.other.solver.nurimisaki.NurimisakiSolver.NurimisakiGenerator;
import myamya.other.solver.reflect.ReflectSolver.ReflectGenerator;
import myamya.other.solver.sashigane.SashiganeSolver.SashiganeGenerator;
import myamya.other.solver.shakashaka.ShakashakaSolver.ShakashakaGenerator;
import myamya.other.solver.slither.SlitherSolver.SlitherGenerator;
import myamya.other.solver.sudoku.SudokuSolver.SudokuGenerator;
import myamya.other.solver.tapa.TapaSolver.TapaGenerator;
import myamya.other.solver.tasquare.TasquareSolver.TasquareGenerator;
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
			if (height > 10 || width > 10) {
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