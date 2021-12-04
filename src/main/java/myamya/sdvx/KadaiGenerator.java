package myamya.sdvx;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import myamya.sdvx.KadaiGeneratorClasses.EffectInfo;
import myamya.sdvx.KadaiGeneratorClasses.EstimateInfo;
import myamya.sdvx.KadaiGeneratorClasses.ProfileInfo;
import myamya.sdvx.KadaiGeneratorClasses.RatePair;
import myamya.sdvx.KadaiGeneratorClasses.ResponseInfo;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfo;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfoAll;
import myamya.sdvx.KadaiGeneratorClasses.TrackInfo;
import myamya.sdvx.KadaiGeneratorEnums.ClearLamp;
import myamya.sdvx.KadaiGeneratorEnums.EffectDiv;
import myamya.sdvx.KadaiGeneratorEnums.Mode;
import myamya.sdvx.KadaiGeneratorEnums.ResponseDiv;
import myamya.sdvx.KadaiGeneratorEnums.ScoreDiv;
import myamya.sdvx.KadaiGeneratorEnums.Target;
import net.arnx.jsonic.JSON;

public class KadaiGenerator {

	private static final StatusInfoAll statusInfoAll = new StatusInfoAll();
	private static final Object locker = new Object();
	private static final Map<Integer, Map<Integer, Map<ClearLamp, Integer>>> volforceTargetMap = getVolforceTargetMap();

	/**
	 * userIdを利用してスコアツールのデータを取得し、 minLevel, maxLevelの条件に合致する分析結果を
	 * modeで指定した内容に沿ったソート順のListで返す。
	 */
	public ResponseInfo execute(String userId, int minLevel, int maxLevel, Mode mode, int boader, Target target)
			throws MalformedURLException, ProtocolException, IOException {
		// 自分の情報を取る
		Map<String, Object> profileMap = getProfileMap(userId);
		if (profileMap.containsKey("errormsg")) {
			System.err.println(profileMap.get("errormsg"));
			return new ResponseInfo(ResponseDiv.INVALID_USER_ID, new ArrayList<>());
		}
		// 自身の情報を整形
		ProfileInfo profileInfo = new ProfileInfo(profileMap);
		Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap;
		synchronized (locker) {
			// 整形された統計情報を取得
			statusInfoMap = statusInfoAll.checkAndGetStatusInfoMap();
		}
		// 曲ごとにマッチングして譜面ごとに上位何%か推定
		List<EstimateInfo> estimateInfoList = getEstimateInfoList(profileInfo, statusInfoMap, mode, boader, target);
		// estimateRateMapを降順ソートし、レベルでフィルター
		estimateInfoList = getEstimateInfoList(minLevel, maxLevel, estimateInfoList, mode);
		if (estimateInfoList.isEmpty()) {
			return new ResponseInfo(ResponseDiv.NOT_FOUND, estimateInfoList);
		} else {
			return new ResponseInfo(ResponseDiv.SUCCESS, estimateInfoList);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getProfileMap(String userId)
			throws MalformedURLException, IOException, ProtocolException {
		StringBuilder sb = new StringBuilder();
		URL url = new URL("https://nearnoah.net/api/showUserData.json?username=" + userId);
		try (BufferedReader reader = KadaiGeneratorUtil.getReader(url)) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return JSON.decode(sb.toString(), Map.class);
	}

	/**
	 * 自身の情報と統計情報より上位何%か推定
	 */
	private List<EstimateInfo> getEstimateInfoList(ProfileInfo profileInfo,
			Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap, Mode mode, int border, Target target) {
		int volForceMin = -1;
		List<EstimateInfo> estimateInfoList = new ArrayList<>();
		for (TrackInfo trackInfo : profileInfo.getTrackList()) {
			Map<EffectDiv, StatusInfo> oneStatusInfoMap = statusInfoMap.get(trackInfo.getTitle());
			if (oneStatusInfoMap == null) {
				// XXX ダブルクオート含む曲の暫定対応
				String xxx = trackInfo.getTitle().replace("\"", "\\");
				oneStatusInfoMap = statusInfoMap.get(xxx);
				if (oneStatusInfoMap == null) {
					// TODO 曲の統計情報がない場合
					continue;
				}
			}
			for (Entry<EffectDiv, EffectInfo> entry : trackInfo.getEffectMap().entrySet()) {
				StatusInfo statusInfo = oneStatusInfoMap.get(entry.getKey());
				if (statusInfo == null) {
					// TODO 譜面の統計情報がない場合
				} else {
					if (entry.getValue().getScore() == 0) {
						// TODO 未プレイの場合
					} else {
						if (mode == Mode.FOR_TARGET) {
							boolean isClear;
							if (target instanceof ClearLamp) {
								isClear = entry.getValue().getClear().isClear((ClearLamp) target);
							} else if (target instanceof ScoreDiv) {
								isClear = ((ScoreDiv) target).isClear(entry.getValue().getScore());
							} else {
								throw new IllegalArgumentException();
							}
							if (!isClear) {
								BigDecimal scoreBase = KadaiGeneratorUtil.getEstimateRate(statusInfo,
										entry.getValue().getScore());
								if (scoreBase.compareTo(BigDecimal.ZERO) != 0) {
									BigDecimal rate = statusInfo.getPercent(target).multiply(new BigDecimal(100))
											.divide(scoreBase, 2, RoundingMode.DOWN);
									estimateInfoList.add(new EstimateInfo(trackInfo.getTitle(),
											statusInfo.getEffectDiv(), BigDecimal.valueOf(entry.getValue().getScore()),
											String.valueOf(entry.getValue().getScore()), entry.getValue().getLevel(),
											rate, String.valueOf(rate) + " p"));
								}
							}
						} else if (mode == Mode.FOR_BORDER) {
							int borderScore = getBorderScore(BigDecimal.valueOf(border), statusInfo);
							estimateInfoList.add(new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
									BigDecimal.valueOf(borderScore), String.valueOf(borderScore),
									entry.getValue().getLevel(),
									BigDecimal.valueOf(entry.getValue().getScore() - borderScore),
									String.valueOf(entry.getValue().getScore() - borderScore)));
						} else if (mode == Mode.FOR_CLEAR) {
							boolean isClear;
							if (target instanceof ClearLamp) {
								isClear = entry.getValue().getClear().isClear((ClearLamp) target);
							} else if (target instanceof ScoreDiv) {
								isClear = ((ScoreDiv) target).isClear(entry.getValue().getScore());
							} else {
								throw new IllegalArgumentException();
							}
							estimateInfoList.add(new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
									statusInfo.getPercent(target), statusInfo.getPercent(target).toPlainString() + "%",
									entry.getValue().getLevel(),
									isClear ? BigDecimal.valueOf(0) : BigDecimal.valueOf(-1), isClear ? "達成" : "未達成"));
						} else if (mode == Mode.FOR_VOLFORCE) {
							if (volForceMin == -1) {
								volForceMin = KadaiGeneratorUtil.getVolForceInfo(profileInfo.getTrackList())
										.getVolForceMin();
							}
							List<EstimateInfo> recommendVolforceInfo = getRecommendVolforceInfo(trackInfo.getTitle(),
									entry.getValue(), statusInfo, volForceMin);
							estimateInfoList.addAll(recommendVolforceInfo);
						} else {
							BigDecimal estimateRate = KadaiGeneratorUtil.getEstimateRate(statusInfo,
									entry.getValue().getScore());
							estimateInfoList.add(new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
									BigDecimal.valueOf(entry.getValue().getScore()),
									String.valueOf(entry.getValue().getScore()), entry.getValue().getLevel(),
									estimateRate, mode.getDispEstimateRate(estimateRate)));
						}
					}
				}
			}

		}
		return estimateInfoList;
	}

	private List<EstimateInfo> getRecommendVolforceInfo(String title, EffectInfo effectInfo, StatusInfo statusInfo,
			int volForceMin) {
		List<EstimateInfo> result = new ArrayList<>();
		// PUCならvolforce伸びる余地なし
		if (effectInfo.getClear() == ClearLamp.PUC) {
			return result;
		}
		// 現在以上のクリアランプでボルフォース更新に必要な点数を計算
		int volForceTarget = effectInfo.getOneVolForce() + 1;
		if (volForceTarget <= volForceMin) {
			volForceTarget = volForceMin + 1;
		}
		// そのボルフォースに到達が必要なクリアランプとスコアの一覧を作成
		Map<Entry<ClearLamp, Integer>, BigDecimal> rateAdjustMap = new LinkedHashMap<>();
		for (int candVolForce = 500; candVolForce > volForceTarget; candVolForce--) {
			Map<ClearLamp, Integer> targetEntry = volforceTargetMap.get(candVolForce).get(effectInfo.getLevel());
			if (targetEntry != null) {
				for (Entry<ClearLamp, Integer> e : targetEntry.entrySet()) {
					rateAdjustMap.put(e, new BigDecimal(candVolForce - volForceTarget).divide(new BigDecimal(1000), 3,
							RoundingMode.DOWN));
				}
			}
		}
		if (rateAdjustMap.isEmpty()) {
			return result;
		}
		for (Entry<Entry<ClearLamp, Integer>, BigDecimal> e : rateAdjustMap.entrySet()) {
			ClearLamp targetClearLamp = e.getKey().getKey();
			int scoreTarget = e.getKey().getValue();
			// VFが伸びるものがあれば、統計情報と比較して狙い目指数を計算
			// 狙い目指数の計算式は(目標スコアレート * 100/(現在スコアレート))
			// または、(目標クリアレート * 100/(現在スコアレート))。
			// どっちも足りてない場合は上記2つのうち大きい方を採用する
			BigDecimal scoreBase = KadaiGeneratorUtil.getEstimateRate(statusInfo, effectInfo.getScore());
			BigDecimal targetScoreRate = KadaiGeneratorUtil.getEstimateRate(statusInfo, scoreTarget);
			BigDecimal targetClearRate = statusInfo.getPercent(targetClearLamp);
			BigDecimal rate = null;
			String scoreString = null;
			if (effectInfo.getClear() == targetClearLamp || targetClearLamp == ClearLamp.PUC) {
				// スコア更新でボルフォース対象入り
				if (targetClearLamp == ClearLamp.PUC) {
					scoreString = ClearLamp.PUC.getShortStr();
				} else {
					scoreString = String.valueOf(scoreTarget);
				}
				if (scoreBase.compareTo(BigDecimal.ZERO) != 0) {
					rate = targetScoreRate.multiply(new BigDecimal(100)).divide(scoreBase, 2, RoundingMode.DOWN);
				}
			} else if (effectInfo.getScore() > scoreTarget) {
				// ランプ更新
				if (scoreBase.compareTo(BigDecimal.ZERO) != 0) {
					scoreString = targetClearLamp.getShortStr();
					rate = targetClearRate.multiply(new BigDecimal(100)).divide(scoreBase, 2, RoundingMode.DOWN);
				}
			} else if (effectInfo.getClear().getVal() > targetClearLamp.getVal()) {
				// どっちも更新
				if (scoreBase.compareTo(BigDecimal.ZERO) != 0) {
					scoreString = String.valueOf(scoreTarget) + " + " + targetClearLamp.getShortStr();
					BigDecimal a = targetScoreRate.multiply(new BigDecimal(100)).divide(scoreBase, 3,
							RoundingMode.DOWN);
					BigDecimal b = targetClearRate.multiply(new BigDecimal(100)).divide(scoreBase, 3,
							RoundingMode.DOWN);
					rate = a.compareTo(b) > 0 ? b.setScale(2, RoundingMode.DOWN) : a.setScale(2, RoundingMode.DOWN);
				}
			}
			if (rate != null) {
				boolean addFlg = true;
				for (EstimateInfo already : result) {
					if (already.getScoreString().equals(scoreString)) {
						// すでに同じ目標で追加されているものがあれば新規追加しない。
						// 例えば今995とってて、(995+)UCだけで上がるのに990UCが候補になってた場合。無理やりだけど…
						addFlg = false;
						break;
					}
				}
				if (addFlg) {
					BigDecimal useValue = new BigDecimal(Math.pow(
							e.getValue().multiply(new BigDecimal(1000)).setScale(0, RoundingMode.DOWN).doubleValue(),
							0.6)).setScale(2, RoundingMode.DOWN);
					System.out.println(title + "(" + statusInfo.getEffectDiv() + ")" + scoreString + "=狙いやすさ:" + rate
							+ ",上昇:" + e.getValue().multiply(new BigDecimal(1000)).setScale(0, RoundingMode.DOWN)
							+ ",効能:" + useValue);
					rate = rate.multiply(useValue).divide(new BigDecimal(4)).setScale(2, RoundingMode.DOWN);
					// scoreString = "(VF+" + e.getValue() + ") " + scoreString;
					result.add(new EstimateInfo(title, statusInfo.getEffectDiv(), new BigDecimal(0), scoreString,
							effectInfo.getLevel(), rate, String.valueOf(rate) + " p"));
				}
			}
		}
		return result;
	}

	/**
	 * ボルフォースマップ取得 ボルフォース値 - レベル - クリアランプ - スコア という書式
	 */
	private static Map<Integer, Map<Integer, Map<ClearLamp, Integer>>> getVolforceTargetMap() {
		Map<Integer, Map<Integer, Map<ClearLamp, Integer>>> map = new HashMap<>();
		List<ScoreDiv> scoreDivList = Arrays.asList(new ScoreDiv[] { ScoreDiv.S_998, ScoreDiv.S_995, ScoreDiv.S,
				ScoreDiv.AAA_PLUS, ScoreDiv.AAA, ScoreDiv.AA_PLUS, ScoreDiv.AA, ScoreDiv.A_PLUS, ScoreDiv.A });
		for (int targetVolForce = 1; targetVolForce <= 500; targetVolForce++) {
			// レベル - クリアランプ - スコア のマップ
			Map<Integer, Map<ClearLamp, Integer>> mapA = map.get(targetVolForce);
			if (mapA == null) {
				mapA = new HashMap<>();
				map.put(targetVolForce, mapA);
			}
			for (int level = 1; level <= 20; level++) {
				// クリアランプ - スコア のマップ
				Map<ClearLamp, Integer> mapB = mapA.get(level);
				if (mapB == null) {
					mapB = new HashMap<>();
					mapA.put(level, mapB);
				}
				for (ClearLamp targetClearLamp : ClearLamp.values()) {
					if (targetClearLamp == ClearLamp.NOPLAY) {
						continue;
					}
					if (targetClearLamp == ClearLamp.PUC) {
						int candidateVolForce = new BigDecimal(level).multiply(new BigDecimal(20))
								.multiply(ClearLamp.PUC.getVolForceBase()).multiply(ScoreDiv.PUC.getVolForceBase())
								.intValue();
						if (targetVolForce == candidateVolForce) {
							mapB.put(targetClearLamp, 10000000);
						}
						continue;
					}
					for (ScoreDiv scoreDiv : scoreDivList) {
						int scoreCandidate = new BigDecimal(targetVolForce).multiply(new BigDecimal(500000))
								.divide(scoreDiv.getVolForceBase(), RoundingMode.DOWN)
								.divide(targetClearLamp.getVolForceBase(), RoundingMode.DOWN)
								.divide(BigDecimal.valueOf(level), RoundingMode.DOWN).intValue();
						int upCandidate = new BigDecimal(targetVolForce + 1).multiply(new BigDecimal(500000))
								.divide(scoreDiv.getVolForceBase(), RoundingMode.DOWN)
								.divide(targetClearLamp.getVolForceBase(), RoundingMode.DOWN)
								.divide(BigDecimal.valueOf(level), RoundingMode.DOWN).intValue();
						if (scoreDiv.getMin() <= upCandidate && scoreCandidate <= scoreDiv.getMax()) {
							int scoreTarget = (scoreDiv.getMin() > scoreCandidate ? scoreDiv.getMin() : scoreCandidate);
							mapB.put(targetClearLamp, scoreTarget);
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * 上位何%かという情報を検索条件でソート＆フィルタリング
	 */
	private List<EstimateInfo> getEstimateInfoList(int minLevel, int maxLevel, List<EstimateInfo> estimateInfoList,
			Mode mode) {
		/**
		 * 率でのソート
		 */
		Comparator<EstimateInfo> c = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				return o1.getEstimateRate().compareTo(o2.getEstimateRate());
			}
		};
		/**
		 * スコアでの降順ソート
		 */
		Comparator<EstimateInfo> sc = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				return o2.getScore().compareTo(o1.getScore());
			}
		};

		/**
		 * 曲名と譜面名区分でのソート 今は使っていないが将来を見越して残す
		 */
		@SuppressWarnings("unused")
		Comparator<EstimateInfo> tc = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				int titleCompareResult = o1.getTitle().compareTo(o2.getTitle());
				if (titleCompareResult != 0) {
					return titleCompareResult;
				} else {
					return Integer.valueOf(o1.getEffectDiv().ordinal()).compareTo(o2.getEffectDiv().ordinal());
				}
			}
		};

		List<EstimateInfo> result = estimateInfoList.stream().sorted(
				// 推定率のソート
				// 今回やりたいことをやるには先にComparatorを作っておかないとだめっぽい
				mode == Mode.FOR_CLEAR ? sc
						: mode == Mode.FOR_SCORE || mode == Mode.FOR_TARGET || mode == Mode.FOR_VOLFORCE ? c.reversed()
								: c)
				.filter(o -> {
					// レベルによる絞り込み
					boolean b = minLevel <= o.getLevel() && o.getLevel() <= maxLevel;
					if (mode == Mode.FOR_SCORE) {
						// スコア狙いの場合、すでにPUC済みの曲は表示しない
						return b && o.getScore().compareTo(BigDecimal.valueOf(10000000)) != 0;
					} else {
						return b;
					}
				}).collect(Collectors.toList());
		return result;
	}

	private int getBorderScore(BigDecimal border, StatusInfo statusInfo) {
		ScoreDiv scoreDiv;
		if (statusInfo.getPPer().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.PUC;
		} else if (statusInfo.getPGrade998().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S_998;
		} else if (statusInfo.getPGrade995().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S_995;
		} else if (statusInfo.getPGradeS().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S;
		} else if (statusInfo.getPGradeAAAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AAA_PLUS;
		} else if (statusInfo.getPGradeAAA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AAA;
		} else if (statusInfo.getPGradeAAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AA_PLUS;
		} else if (statusInfo.getPGradeAA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AA;
		} else if (statusInfo.getPGradeAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.A_PLUS;
		} else if (statusInfo.getPGradeA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.A;
		} else {
			scoreDiv = ScoreDiv.UNDER_A;
		}
		RatePair ratePair = statusInfo.getRatePair(scoreDiv);
		// 自身のScoreDivと1つ上のScoreDivの最低点と%を使って、
		// 指定されたボーダー%を比例計算で割り出す。
		BigDecimal a = BigDecimal.valueOf(scoreDiv.getRange());
		BigDecimal b = ratePair.getSelfRate().subtract(ratePair.getNextRate());
		BigDecimal c = (a).divide(b, 3, RoundingMode.DOWN);
		BigDecimal d = ratePair.getSelfRate().subtract(border);
		BigDecimal adjustValue = c.multiply(d);
		return scoreDiv.getMin() + adjustValue.intValue();
	}
}
