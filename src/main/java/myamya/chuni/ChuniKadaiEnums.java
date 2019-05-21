package myamya.chuni;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ChuniKadaiEnums {

	/**
	 * 分析モードを表す列挙型
	 * TODO 処理内に出てくるModeのIF文を全部こっちに寄せる
	 */
	@RequiredArgsConstructor
	@Getter
	enum Mode {
	/** スコアが伸びそうな曲 */
	FOR_SCORE(1) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString() + "%";
		}

	},
	/** 武器曲 */
	FOR_WEAPON(2) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString() + "%";
		}

	},
	/** 課題曲(目標設定) */
	FOR_TARGET(3) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			// TODO 自動生成されたメソッド・スタブ
			if (estimateRate.compareTo(new BigDecimal("100000")) >= 0) {
				return "99999.99";
			} else {
				return estimateRate.toPlainString();
			}
		}
	},

	/** ボーダーモード */
	FOR_BORDER(4) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString();
		}
	},

	/** クリア達成状況モード */
	FOR_CLEAR(5) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString();
		}
	};

		private final int value;

		public static Mode getByValue(int value) {
			for (Mode mode : Mode.values()) {
				if (mode.getValue() == value) {
					return mode;
				}
			}
			return null;
		}

		abstract public String getDispEstimateRate(BigDecimal estimateRate);
	}

	/**
	 * 譜面区分を示す列挙型
	 */
	@RequiredArgsConstructor
	@Getter
	enum EffectDiv {
		/** */
		BAS("BAS", "BAS", 0),
		/** */
		ADV("ADV", "ADV", 1),
		/** */
		EXP("EXP", "EXP", 2),
		/** */
		MAS("MAS", "MAS", 3),
		/** */
		WE("WE", "WE", 4);

		private final String str;
		private final String shortStr;
		private final int val;

		public static EffectDiv getByVal(int val) {
			for (EffectDiv effectDiv : EffectDiv.values()) {
				if (effectDiv.getVal() == val) {
					return effectDiv;
				}
			}
			return null;
		}
	}

	/**
	 * 目標となりうる指標。
	 */
	interface Target {

		// 自身を表す数値を返す
		int getVal();

		/**
		 * 自身を表す数値を返す。
		 */
		public static Target getByVal(int val) {
			for (ClearLamp clearLamp : ClearLamp.values()) {
				if (clearLamp.getVal() == val) {
					return clearLamp;
				}
			}
			for (ScoreDiv scoreDiv : ScoreDiv.values()) {
				if (scoreDiv.getVal() == val) {
					return scoreDiv;
				}
			}
			return null;
		}
	}

	/**
	 * クリアランプ区分を示す列挙型
	 * valはClearLampとScoreDivで重複させないこと
	 */
	@RequiredArgsConstructor
	@Getter
	enum ClearLamp implements Target {
		/** */
		NOPLAY("NOPLAY", "NOPLAY", 6),
		/** */
		NC("NC", "NC", 5),
		/** */
		C("C", "C", 4),
		/** */
		FC("FC", "FC", 3),
		/** */
		AJ("AJ", "AJ", 2);

		private final String str;
		private final String shortStr;
		private final int val;

		/**
		 * 自身のクリア状況がotherを上回っているかを返す。
		 */
		public boolean isClear(ClearLamp other) {
			// 値が小さいほうが格上なので
			return this.getVal() <= other.getVal();
		}
	}

	/**
	 * スコアツール内のスコア区分を示す列挙型
	 */
	@RequiredArgsConstructor
	@Getter
	enum ScoreDiv implements Target {
		/** */
		UNDER_S(0, 974999, 20),
		/** */
		S(975000, 999999, 18),
		/** */
		SS(1000000, 1004999, 16),
		/** */
		SS_PLUS(1005000, 1007499, 14),
		/** */
		SSS(1007500, 1009749, 12),
		/** */
		// SSS_PLUSは公式の分類ではない。ざっくり1009750率 = AJ率として計算する用
		SSS_PLUS(1009750, 1009999, 11),
		/** */
		MAX(1010000, 1010000, 10);

		private final int min;
		private final int max;
		private final int val;

		public int getRange() {
			return max + 1 - min;
		}

		public static ScoreDiv getByScore(int score) {
			for (ScoreDiv scoreDiv : ScoreDiv.values()) {
				if (scoreDiv.getMin() <= score && score <= scoreDiv.getMax()) {
					return scoreDiv;
				}
			}
			return null;
		}

		/**
		 * スコアがScoreDivを上回っているかを返す。
		 */
		public boolean isClear(int score) {
			// 値が小さいほうが格上なので
			return getByScore(score).getVal() <= this.getVal();
		}
	}

	/**
	 * スコアツール内のスコア区分を示す列挙型
	 */
	@RequiredArgsConstructor
	@Getter
	enum ResponseDiv {
		/** */
		SUCCESS(""),
		/** */
		NOT_INPUT_USER_ID("スコアツールユーザ名を入力してください"),
		/** */
		REVERSE_LVL("表示レベルの入力が逆になっています"),
		/** */
		INVALID_USER_ID("スコアツールユーザ名が見つかりません"),
		/** */
		NOT_FOUND("条件に該当する譜面がありません"),
		/** */
		UNEXPECTED_ERROR("サーバー処理でエラーが発生しました");

		private final String errorMsg;

	}
}
