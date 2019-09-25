package myamya.other.solver;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.Position;

public abstract class HintPattern {

	public static void main(String[] args) {
		System.out.println(new AscSlashPattern(5, 5).getPosSetList());
	}

	static class FreeHintPattern extends HintPattern {

		public FreeHintPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < getHeight() * getWidth(); i++) {
				Set<Position> set = new HashSet<>();
				int yIndex = i / getWidth();
				int xIndex = i % getWidth();
				set.add(new Position(yIndex, xIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class PointHintPattern extends HintPattern {

		public PointHintPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i <= ((getHeight() * getWidth() + 1) / 2); i++) {
				Set<Position> set = new HashSet<>();
				int yIndex = i / getWidth();
				int xIndex = i % getWidth();
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(getHeight() - 1 - yIndex, getWidth() - 1 - xIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class HorizonalHintPattern extends HintPattern {

		public HorizonalHintPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < (getHeight()) * ((getWidth() + 1) / 2); i++) {
				Set<Position> set = new HashSet<>();
				int yIndex = i / ((getWidth() + 1) / 2);
				int xIndex = i % ((getWidth() + 1) / 2);
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(yIndex, getWidth() - 1 - xIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class VarticalHintPattern extends HintPattern {

		public VarticalHintPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < ((getHeight() + 1) / 2) * (getWidth()); i++) {
				Set<Position> set = new HashSet<>();
				int yIndex = i % ((getWidth() + 1) / 2);
				int xIndex = i / ((getWidth() + 1) / 2);
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(getHeight() - 1 - yIndex, xIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class DescSlashPattern extends HintPattern {

		public DescSlashPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			if (getHeight() != getWidth()) {
				throw new IllegalStateException();
			}
			int length = getHeight();
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < length * length; i++) {
				int yIndex = i / length;
				int xIndex = i % length;
				if (yIndex > xIndex) {
					continue;
				}
				Set<Position> set = new HashSet<>();
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(xIndex, yIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class AscSlashPattern extends HintPattern {

		public AscSlashPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			if (getHeight() != getWidth()) {
				throw new IllegalStateException();
			}
			int length = getHeight();
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < length * length; i++) {
				int yIndex = i / length;
				int xIndex = i % length;
				if (yIndex + xIndex >= length) {
					continue;
				}
				Set<Position> set = new HashSet<>();
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(length - 1 - xIndex, length - 1 - yIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class AllHintPattern extends HintPattern {

		public AllHintPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < ((getHeight() + 1) / 2) * ((getWidth() + 1) / 2); i++) {
				Set<Position> set = new HashSet<>();
				int yIndex = i / ((getWidth() + 1) / 2);
				int xIndex = i % ((getWidth() + 1) / 2);
				set.add(new Position(yIndex, xIndex));
				set.add(new Position(getHeight() - 1 - yIndex, xIndex));
				set.add(new Position(yIndex, getWidth() - 1 - xIndex));
				set.add(new Position(getHeight() - 1 - yIndex, getWidth() - 1 - xIndex));
				result.add(set);
			}
			return result;
		}

	}

	static class ManjiPattern extends HintPattern {

		public ManjiPattern(int height, int width) {
			super(height, width);
		}

		@Override
		public List<Set<Position>> getPosSetList() {
			if (getHeight() != getWidth()) {
				throw new IllegalStateException();
			}
			int length = getHeight();
			int count = (length / 2) * (length / 2);
			if (length % 2 == 1) {
				count = count + (length / 2) + 1;
			}
			List<Set<Position>> result = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				Set<Position> set = new HashSet<>();
				if (length % 2 == 1 && i == count - 1) {
					set.add(new Position(length / 2, length / 2));
				} else {
					int yIndex = i / (length / 2);
					int xIndex = i % (length / 2);
					set.add(new Position(yIndex, xIndex));
					set.add(new Position(length - 1 - xIndex, yIndex));
					set.add(new Position(length - 1 - yIndex, length - 1 - xIndex));
					set.add(new Position(xIndex, length - 1 - yIndex));
				}
				result.add(set);
			}
			return result;
		}

	}

	private static final Map<Integer, Class<? extends HintPattern>> HINT_PATTERN_MAP;
	static {
		HINT_PATTERN_MAP = new HashMap<>();
		HINT_PATTERN_MAP.put(0, FreeHintPattern.class);
		HINT_PATTERN_MAP.put(1, PointHintPattern.class);
		HINT_PATTERN_MAP.put(2, HorizonalHintPattern.class);
		HINT_PATTERN_MAP.put(3, VarticalHintPattern.class);
		HINT_PATTERN_MAP.put(4, DescSlashPattern.class);
		HINT_PATTERN_MAP.put(5, AscSlashPattern.class);
		HINT_PATTERN_MAP.put(6, AllHintPattern.class);
		HINT_PATTERN_MAP.put(7, ManjiPattern.class);
	}

	private final int height;
	private final int width;

	HintPattern(int height, int width) {
		this.height = height;
		this.width = width;
	}

	public abstract List<Set<Position>> getPosSetList();

	public static HintPattern getByVal(int val, int height, int width) {
		if (HINT_PATTERN_MAP.containsKey(val)) {
			Class<?>[] types = { int.class, int.class };
			try {
				return HINT_PATTERN_MAP.get(val).getConstructor(types).newInstance(height, width);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

}