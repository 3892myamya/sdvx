package myamya.chuni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import myamya.chuni.ChuniKadaiEnums.ClearLamp;
import myamya.chuni.ChuniKadaiEnums.EffectDiv;
import myamya.chuni.ChuniKadaiEnums.ResponseDiv;
import myamya.chuni.ChuniKadaiEnums.ScoreDiv;
import myamya.chuni.ChuniKadaiEnums.Target;
import net.arnx.jsonic.JSON;

public class ChuniKadaiClasses {
	/**
	 * スコアツール登録データ全体を表すクラス。
	 */
	@Getter
	@ToString
	static class ProfileInfo {

		private final List<TrackInfo> trackList;
		private final String name;

		@SuppressWarnings("unchecked")
		ProfileInfo(Map<String, Object> map, Map<String, Object> musicInfoMap) {
			trackList = new ArrayList<>();
			Map<String, Map<String, Object>> scoreMap = ((Map<String, Map<String, Object>>) map.get("score_data"));
			for (Entry<String, Map<String, Object>> entry : scoreMap.entrySet()) {
				trackList.add(new TrackInfo(entry.getValue(),
						(List<Map<String, Object>>) ((Map<String, Object>) musicInfoMap.get(entry.getKey()))
								.get("difficulty")));
			}
			name = (String) ((Map<String, Object>) map.get("user_info")).get("userName");
		}
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
		TrackInfo(Map<String, Object> map, List<Map<String, Object>> difficultyList) {
			id = ((BigDecimal) map.get("music_id")).intValue();
			title = (String) map.get("music_name");
			effectMap = new HashMap<>();
			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("scoreData");
			for (Map<String, Object> innerMap : list) {
				int difficulty = ((BigDecimal) innerMap.get("difficulty")).intValue();
				for (Map<String, Object> difficultyMap : difficultyList) {
					if (((BigDecimal) difficultyMap.get("diff")).intValue() == difficulty) {
						EffectDiv effectDiv = EffectDiv.getByVal(((BigDecimal) innerMap.get("difficulty")).intValue());
						if (effectDiv != null) {
							effectMap.put(effectDiv, new EffectInfo(innerMap, difficultyMap));
						}
					}
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
		private final BigDecimal level;
		private final ClearLamp clear;
		private final int score;

		EffectInfo(Map<String, Object> map, Map<String, Object> difficultyMap) {
			level = (BigDecimal) difficultyMap.get("level");
			score = ((BigDecimal) map.get("score")).intValue();
			if ((Boolean) map.get("alljustice") == true) {
				clear = ClearLamp.AJ;
			} else if ((Boolean) map.get("fullcombo") == true) {
				clear = ClearLamp.FC;
			} else if ((Boolean) map.get("success") == true) {
				clear = ClearLamp.C;
			} else if (((BigDecimal) map.get("playcount")).intValue() == 0) {
				clear = ClearLamp.NOPLAY;
			} else {
				clear = ClearLamp.NC;
			}
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
			if ((statusInfoMap == null) ||
					(lastUpdate.getDayOfYear() != nowTime.getDayOfYear())) {
				// 統計情報の取得に1回も成功していない、またはその日最初の実行であればデータ更新
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
			Map<Integer, Object> statusMap = getStatusMap();
			Map<String, Map<EffectDiv, StatusInfo>> result = new HashMap<>();
			for (Entry<Integer, Object> entry : statusMap.entrySet()) {
				@SuppressWarnings("unchecked")
				StatusInfo statusInfo = new StatusInfo(
						(Map<String, Object>) entry.getValue());
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
		 * 統計情報をMapとして読み込む。
		 */
		@SuppressWarnings("unchecked")
		private Map<Integer, Object> getStatusMap() throws ProtocolException, IOException {
			StringBuilder sb = new StringBuilder();
			URL url = new URL("https://chuniviewer.net/GetMusicAnalysisData.php");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setUseCaches(false);
			http.setRequestMethod("POST");
			http.connect();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}
			return JSON.decode(sb.toString(), Map.class);
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
		private final BigDecimal pFc;
		private final BigDecimal pAj;
		private final BigDecimal pMAX;
		private final BigDecimal pS;
		private final BigDecimal pSS;
		private final BigDecimal pSSp;
		private final BigDecimal pSSS;
		private final BigDecimal pSSSp;

		public StatusInfo(Map<String, Object> map) {
			title = (String) map.get("name");
			effectDiv = EffectDiv.getByVal(((BigDecimal) map.get("difficulty_id")).intValue());
			pFc = new BigDecimal((String) map.get("fc_rate")).multiply(new BigDecimal(100));
			pAj = new BigDecimal((String) map.get("aj_rate")).multiply(new BigDecimal(100));
			pMAX = new BigDecimal((String) map.get("max_rate")).multiply(new BigDecimal(100));
			pS = new BigDecimal((String) map.get("s_rate")).multiply(new BigDecimal(100));
			pSS = new BigDecimal((String) map.get("ss_rate")).multiply(new BigDecimal(100));
			pSSp = new BigDecimal((String) map.get("ssp_rate")).multiply(new BigDecimal(100));
			pSSS = new BigDecimal((String) map.get("sss_rate")).multiply(new BigDecimal(100));
			pSSSp = new BigDecimal((String) map.get("aj_rate")).multiply(new BigDecimal(100));
		}

		/**
		 * スコア区分から登録者内位置の推定に利用する統計情報のペアを返す
		 */
		public RatePair getRatePair(ScoreDiv scoreDiv) {
			if (scoreDiv == ScoreDiv.UNDER_S) {
				return new RatePair(BigDecimal.valueOf(100), getPS());
			} else if (scoreDiv == ScoreDiv.S) {
				return new RatePair(getPS(), getPSS());
			} else if (scoreDiv == ScoreDiv.SS) {
				return new RatePair(getPSS(), getPSSp());
			} else if (scoreDiv == ScoreDiv.SS_PLUS) {
				return new RatePair(getPSSp(), getPSSS());
			} else if (scoreDiv == ScoreDiv.SSS) {
				return new RatePair(getPSSS(), getPSSSp());
			} else if (scoreDiv == ScoreDiv.SSS_PLUS) {
				return new RatePair(getPSSSp(), getPMAX());
			} else if (scoreDiv == ScoreDiv.MAX) {
				return new RatePair(getPMAX(), BigDecimal.valueOf(0));
			} else {
				throw new IllegalStateException("invalid scoreDiv:" + scoreDiv);
			}
		}

		/**
		 * 指定された目標に対応する割合を返す
		 */
		public BigDecimal getPercent(Target target) {
			if (target == ScoreDiv.MAX) {
				return pMAX.setScale(2, RoundingMode.DOWN);
			} else if (target == ClearLamp.AJ) {
				return pAj.setScale(2, RoundingMode.DOWN);
			} else if (target == ClearLamp.FC) {
				return pFc.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.SSS_PLUS) {
				return pSSSp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.SSS) {
				return pSSS.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.SS_PLUS) {
				return pSSp.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.SS) {
				return pSS.setScale(2, RoundingMode.DOWN);
			} else if (target == ScoreDiv.S) {
				return pS.setScale(2, RoundingMode.DOWN);
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
		private final BigDecimal level;
		private final BigDecimal estimateRate;
		private final String estimateRateString;

		/**
		 * 自身をmap化します。JSONシリアライズが主な目的です。
		 */
		public Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap<>();
			result.put("title", title);
			result.put("effect_div", effectDiv.getShortStr());
			result.put("level", level.scale() == 0 ? level.intValue() : level.intValue() + "+");
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
