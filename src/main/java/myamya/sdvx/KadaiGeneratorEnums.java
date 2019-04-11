package myamya.sdvx;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import myamya.sdvx.KadaiGeneratorClasses.EstimateRateCalculator;
import myamya.sdvx.KadaiGeneratorClasses.EstimateRateCalculator.PerfectEstimateRateCalculator;
import myamya.sdvx.KadaiGeneratorClasses.EstimateRateCalculator.ScoreEstimateRateCalculator;

public class KadaiGeneratorEnums {

	/**
	 * 分析モードを表す列挙型
	 * TODO 処理内に出てくるModeのIF文を全部こっちに寄せる
	 */
	@RequiredArgsConstructor
	@Getter
	enum Mode {
	/** スコアが伸びそうな曲 */
	FOR_SCORE(1, new ScoreEstimateRateCalculator()) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString() + "%";
		}

	},
	/** 武器曲 */
	FOR_WEAPON(2, new ScoreEstimateRateCalculator()) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString() + "%";
		}

	},
	/** PERFECTが狙えそうな曲 */
	FOR_PERFECT(3, new PerfectEstimateRateCalculator()) {
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
	FOR_BORDER(4, null) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString();
		}
	},

	/** クリア達成状況モード */
	FOR_CLEAR(5, null) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString();
		}
	},

	/** ボルフォースモード */
	FOR_VOLFORCE(6, null) {
		@Override
		public String getDispEstimateRate(BigDecimal estimateRate) {
			return estimateRate.toPlainString();
		}
	};

		private final int value;
		private final EstimateRateCalculator estimateRateCalculator;

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
		NOVICE("NOVICE", "NOV"),
		/** */
		ADVANCED("ADVANCED", "ADV"),
		/** */
		EXHAUST("EXHAUST", "EXH"),
		/** */
		MAXIMUM("MAXIMUM", "MXM"),
		/** */
		INFINITE("INFINITE", "INF"),
		/** */
		GRAVITY("GRAVITY", "GRV"),
		/** */
		HEAVENLY("HEAVENLY", "HVN"),
		/** */
		VIVID("VIVID", "VVD");

		private final String str;
		private final String shortStr;

		public static EffectDiv getByStr(String key) {
			for (EffectDiv effectDiv : EffectDiv.values()) {
				if (effectDiv.getStr().equalsIgnoreCase(key)) {
					return effectDiv;
				}
			}
			return null;
		}
	}

	/**
	 * クリアランプ区分を示す列挙型
	 */
	@RequiredArgsConstructor
	@Getter
	enum ClearLamp {
		/** */
		NOPLAY("NOPLAY", "NOPLAY", 6, new BigDecimal(0)),
		/** */
		CRASH("CRASH", "CRASH", 5, new BigDecimal(0.5)),
		/** */
		COMP("COMP", "COMP", 4, new BigDecimal(1)),
		/** */
		EX_COMP("EX COMP", "EXCOMP", 3, new BigDecimal(1.02)),
		/** */
		UC("UC", "UC", 2, new BigDecimal(1.05)),
		/** */
		PER("PER", "PER", 1, new BigDecimal(1.1));

		private final String str;
		private final String shortStr;
		private final int val;
		private final BigDecimal volForceBase;

		public static ClearLamp getByVal(int val) {
			for (ClearLamp clearLamp : ClearLamp.values()) {
				if (clearLamp.getVal() == val) {
					return clearLamp;
				}
			}
			return null;
		}

		public static ClearLamp getByStr(String str) {
			for (ClearLamp clearLamp : ClearLamp.values()) {
				if (clearLamp.getStr().equalsIgnoreCase(str)) {
					return clearLamp;
				}
			}
			return null;
		}

		/**
		 * 自身のクリア状況がotherを上回っているかを返す。
		 */
		public boolean isClear(ClearLamp other) {
			// 値が小さいほうが格上なので
			return this.val <= other.val;
		}
	}

	/**
	 * スコアツール内のスコア区分を示す列挙型
	 */
	@RequiredArgsConstructor
	@Getter
	enum ScoreDiv {
		/** */
		UNDER_A(0, 8699999, new BigDecimal(0)),
		/** */
		A(8700000, 8999999, new BigDecimal(0.88)),
		/** */
		A_PLUS(9000000, 9299999, new BigDecimal(0.91)),
		/** */
		AA(9300000, 9499999, new BigDecimal(0.94)),
		/** */
		AA_PLUS(9500000, 9699999, new BigDecimal(0.97)),
		/** */
		AAA(9700000, 9799999, new BigDecimal(1.00)),
		/** */
		AAA_PLUS(9800000, 9899999, new BigDecimal(1.02)),
		/** */
		S(9900000, 9949999, new BigDecimal(1.05)),
		/** */
		S_995(9950000, 9979999, new BigDecimal(1.05)),
		/** */
		S_998(9980000, 9999999, new BigDecimal(1.05)),
		/** */
		PER(10000000, 10000000, new BigDecimal(1.05));

		private final int min;
		private final int max;
		private final BigDecimal volForceBase;

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
