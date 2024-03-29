package myamya.sdvx;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ProtocolException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import myamya.sdvx.KadaiGeneratorEnums.ClearLamp;
import myamya.sdvx.KadaiGeneratorEnums.EffectDiv;
import myamya.sdvx.KadaiGeneratorEnums.ResponseDiv;
import myamya.sdvx.KadaiGeneratorEnums.ScoreDiv;
import myamya.sdvx.KadaiGeneratorEnums.Target;

public class KadaiGeneratorClasses {
	/**
	 * スコアツール登録データ全体を表すクラス。
	 */
	@Getter
	@ToString
	static class ProfileInfo {

		private final List<TrackInfo> trackList;
		private final String name;

		ProfileInfo(Map<String, Object> map) {
			trackList = new ArrayList<>();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map<String, Object>) map.get("profile"))
					.get("tracks");
			for (Map<String, Object> innerMap : list) {
				trackList.add(new TrackInfo(innerMap));
			}
			name = (String) map.get("name");
		}
	}

	/**
	 * ボルフォースに関する情報
	 */
	@ToString
	@Value
	@RequiredArgsConstructor
	static class VolForceInfo {
		private final int totalVolForce;
		private final int volForceMin;
		private final List<OneEffectInfo> volForceList;
	}

	/**
	 * 1曲分のデータを示すクラス
	 */
	@Getter
	@ToString
	static class TrackInfo {
		private final int id;
		private final String title;
		private final Map<EffectDiv, EffectInfo> effectMap;

		@SuppressWarnings("unchecked")
		TrackInfo(Map<String, Object> map) {
			id = ((BigDecimal) map.get("id")).intValue();
			title = (String) map.get("title");
			effectMap = new HashMap<>();
			for (Entry<String, Object> entry : map.entrySet()) {
				EffectDiv effectDiv = EffectDiv.getByStr(entry.getKey());
				if (effectDiv != null) {
					effectMap.put(effectDiv, new EffectInfo((Map<String, Object>) entry.getValue()));
				}
			}
		}
	}

	/**
	 * 1曲の1譜面のデータを示すクラス
	 * EffectInfoと違い、自身がIDを持っている
	 */
	@Getter
	@ToString
	@RequiredArgsConstructor
	static class OneEffectInfo {
		private final int id;
		private final String title;
		private final EffectDiv EffectDiv;
		private final EffectInfo EffectInfo;
	}

	/**
	 * 1譜面分の詳細データを示すクラス
	 */
	@ToString
	@Value
	@RequiredArgsConstructor
	static class EffectInfo {
		private final int level;
		private final ClearLamp clear;
		private final int score;

		EffectInfo(Map<String, Object> map) {
			level = ((BigDecimal) map.get("level")).intValue();
			if (map.size() == 1) {
				score = 0;
				clear = ClearLamp.NOPLAY;
			} else {
				score = ((BigDecimal) map.get("score")).intValue();
				clear = ClearLamp.getByStr((String) map.get("clearlamp"));
			}
		}

		int getOneVolForce() {
			return new BigDecimal(level).multiply(new BigDecimal(score)).multiply(clear.getVolForceBase())
					.multiply(ScoreDiv.getByScore(score).getVolForceBase()).divide(new BigDecimal(500000)).intValue();
		}
	}

	/**
	 * スコアツール統計情報全体
	 */
	@ToString
	static class StatusInfoAll {
		private LocalDateTime lastUpdate;
		private Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap;

		/**
		 * スコアツールの統計情報をチェックし、必要があれば更新して返します。
		 */
		public Map<String, Map<EffectDiv, StatusInfo>> checkAndGetStatusInfoMap()
				throws IOException {
			LocalDateTime nowTime = LocalDateTime.now();
			if ((statusInfoMap == null) || ((nowTime.getDayOfWeek() == DayOfWeek.TUESDAY
					|| nowTime.getDayOfWeek() == DayOfWeek.FRIDAY) &&
					(lastUpdate.getDayOfYear() != nowTime.getDayOfYear()))) {
				// 統計情報の取得に1回も成功していない、または
				// 今日が火曜日か金曜日で、その日最初の実行であればデータ更新
				this.lastUpdate = nowTime;
				try {
					this.statusInfoMap = getStatusInfoMapFromScoreTool();
				} catch (IOException e) {
					// 一時的なネットワーク寸断かもしれないので、一応先に進ませるが、
					// statusInfoMapがnullのままだったらもはやこれまで
					if (statusInfoMap == null) {
						throw e;
					}
					e.printStackTrace();
				}
			}
			return statusInfoMap;
		}

		/**
		 * 課題曲算出向けに整形された統計情報を返す
		 */
		private Map<String, Map<EffectDiv, StatusInfo>> getStatusInfoMapFromScoreTool()
				throws IOException {
			List<List<String>> statusList = getStatusList();
			Map<String, Map<EffectDiv, StatusInfo>> result = new HashMap<>();
			boolean firstFlg = true;
			for (List<String> oneStatusList : statusList) {
				if (firstFlg) {
					firstFlg = false;
					continue;
				}
				StatusInfo statusInfo = new StatusInfo(oneStatusList);
				Map<EffectDiv, StatusInfo> innerMap = result.get(statusInfo.getTitle());
				if (innerMap == null) {
					innerMap = new HashMap<>();
					result.put(statusInfo.getTitle(), innerMap);
				}
				innerMap.put(statusInfo.getEffectDiv(), statusInfo);
			}
			return result;
		}

		/**
		 * 統計情報ファイルをプレーンな文字列リスト情報として読み込む。
		 */
		private List<List<String>> getStatusList() throws ProtocolException, IOException {
			List<List<String>> result = new ArrayList<>();
			URL url = new URL("https://nearnoah.net/csv/nearnoah_stats.csv");
			try (CsvListReader csvReader = new CsvListReader(KadaiGeneratorUtil.getReader(url),
					CsvPreference.STANDARD_PREFERENCE)) {
				List<String> oneStatusList;
				while ((oneStatusList = csvReader.read()) != null) {
					result.add(oneStatusList);
				}
			}
			return result;
		}

	}

	/**
	 * 1譜面分のスコアツール統計情報を示すクラス
	 */
	@Getter
	@ToString
	@RequiredArgsConstructor
	static class StatusInfo {
		private final String title;
		private final EffectDiv effectDiv;
		private final BigDecimal pPlayed;
		private final BigDecimal pComp;
		private final BigDecimal pExComp;
		private final BigDecimal pUc;
		private final BigDecimal pPer;
		private final BigDecimal pGradeA;
		private final BigDecimal pGradeAp;
		private final BigDecimal pGradeAA;
		private final BigDecimal pGradeAAp;
		private final BigDecimal pGradeAAA;
		private final BigDecimal pGradeAAAp;
		private final BigDecimal pGradeS;
		private final BigDecimal pGrade995;
		private final BigDecimal pGrade998;

		public StatusInfo(List<String> oneStatusList) {
			title = oneStatusList.get(0);
			effectDiv = EffectDiv.getByStr(oneStatusList.get(1));

			BigDecimal count = new BigDecimal(oneStatusList.get(4));

			pPlayed = new BigDecimal(oneStatusList.get(5)).add(new BigDecimal(oneStatusList.get(6)))
					.add(new BigDecimal(oneStatusList.get(7))).add(new BigDecimal(oneStatusList.get(8)))
					.add(new BigDecimal(oneStatusList.get(9))).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pComp = new BigDecimal(oneStatusList.get(6)).add(new BigDecimal(oneStatusList.get(7)))
					.add(new BigDecimal(oneStatusList.get(8))).add(new BigDecimal(oneStatusList.get(9)))
					.divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pExComp = new BigDecimal(oneStatusList.get(7)).add(new BigDecimal(oneStatusList.get(8)))
					.add(new BigDecimal(oneStatusList.get(9))).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pUc = new BigDecimal(oneStatusList.get(8))
					.add(new BigDecimal(oneStatusList.get(9))).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pPer = new BigDecimal(oneStatusList.get(9)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeA = new BigDecimal(oneStatusList.get(12)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeAp = new BigDecimal(oneStatusList.get(13)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeAA = new BigDecimal(oneStatusList.get(14)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeAAp = new BigDecimal(oneStatusList.get(15)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeAAA = new BigDecimal(oneStatusList.get(16)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeAAAp = new BigDecimal(oneStatusList.get(17)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGradeS = new BigDecimal(oneStatusList.get(18)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGrade995 = new BigDecimal(oneStatusList.get(19)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
			pGrade998 = new BigDecimal(oneStatusList.get(20)).divide(count, 4, RoundingMode.DOWN)
					.multiply(new BigDecimal(100));
		}

		/**
		 * スコア区分から登録者内位置の推定に利用する統計情報のペアを返す
		 */
		public RatePair getRatePair(ScoreDiv scoreDiv) {
			if (scoreDiv == ScoreDiv.UNDER_A) {
				return new RatePair(BigDecimal.valueOf(100), getPGradeA());
			} else if (scoreDiv == ScoreDiv.A) {
				return new RatePair(getPGradeA(), getPGradeAp());
			} else if (scoreDiv == ScoreDiv.A_PLUS) {
				return new RatePair(getPGradeAp(), getPGradeAA());
			} else if (scoreDiv == ScoreDiv.AA) {
				return new RatePair(getPGradeAA(), getPGradeAAp());
			} else if (scoreDiv == ScoreDiv.AA_PLUS) {
				return new RatePair(getPGradeAAp(), getPGradeAAA());
			} else if (scoreDiv == ScoreDiv.AAA) {
				return new RatePair(getPGradeAAA(), getPGradeAAAp());
			} else if (scoreDiv == ScoreDiv.AAA_PLUS) {
				return new RatePair(getPGradeAAAp(), getPGradeS());
			} else if (scoreDiv == ScoreDiv.S) {
				return new RatePair(getPGradeS(), getPGrade995());
			} else if (scoreDiv == ScoreDiv.S_995) {
				return new RatePair(getPGrade995(), getPGrade998());
			} else if (scoreDiv == ScoreDiv.S_998) {
				return new RatePair(getPGrade998(), getPPer());
			} else if (scoreDiv == ScoreDiv.PUC) {
				return new RatePair(getPPer(), BigDecimal.valueOf(0));
			} else {
				throw new IllegalStateException("invalid scoreDiv:" + scoreDiv);
			}
		}

		/**
		 * 指定された目標に対応する割合を返す
		 */
		public BigDecimal getPercent(Target target) {
			if (target == ClearLamp.PUC) {
				return pPer.setScale(2, RoundingMode.DOWN);
			} else if (target == ClearLamp.UC) {
				return pUc.setScale(2, RoundingMode.DOWN);
			} else if (target == ClearLamp.EX_COMP) {
				return pExComp.setScale(2, RoundingMode.DOWN);
			} else if (target == ClearLamp.COMP) {
				return pComp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.S_998) {
				return pGrade998.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.S_995) {
				return pGrade995.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.S) {
				return pGradeS.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.AAA_PLUS) {
				return pGradeAAAp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.AAA) {
				return pGradeAAA.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.AA_PLUS) {
				return pGradeAAp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.AA) {
				return pGradeAA.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.A_PLUS) {
				return pGradeAp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.A) {
				return pGradeA.setScale(2, RoundingMode.DOWN);
			} else {
				return null;
			}
		}

	}

	/**
	 * スコアツール内推定位置の算出に利用する率のペア
	 */
	@Value
	static class RatePair {
		BigDecimal selfRate;
		BigDecimal nextRate;
	}

	/**
	 * 1譜面分の分析結果表示に利用する情報をまとめたクラス
	 * TODO estimateRateが指す内容がModeに依存しているのがいまいちかも…
	 */
	@Getter
	@ToString
	@RequiredArgsConstructor
	static class EstimateInfo {
		private final String title;
		private final EffectDiv effectDiv;
		private final BigDecimal score;
		private final String scoreString;
		private final int level;
		private final BigDecimal estimateRate;
		private final String estimateRateString;

		/**
		 * 自身をmap化します。JSONシリアライズが主な目的です。
		 */
		public Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap<>();
			result.put("title", title);
			result.put("effect_div", effectDiv.getShortStr());
			result.put("level", level);
			result.put("score", scoreString);
			result.put("estimate_rate", estimateRateString);
			return result;
		}

	}

	/**
	 * 画面に返す用のクラス
	 */
	@Getter
	@ToString
	@RequiredArgsConstructor
	static class ResponseInfo {
		private final ResponseDiv responseDiv;
		private final List<EstimateInfo> estimateInfoList;
	}
}
