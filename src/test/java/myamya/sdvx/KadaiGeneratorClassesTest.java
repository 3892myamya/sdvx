package myamya.sdvx;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfo;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfoAll;
import myamya.sdvx.KadaiGeneratorEnums.EffectDiv;

public class KadaiGeneratorClassesTest {

	private static final StatusInfo STATUSINFO_FOR_TAST = new StatusInfo("test",
			EffectDiv.NOVICE,
			new BigDecimal(45),
			new BigDecimal(35),
			new BigDecimal(25),
			new BigDecimal(15),
			new BigDecimal(5),
			new BigDecimal(95),
			new BigDecimal(90),
			new BigDecimal(81),
			new BigDecimal(70),
			new BigDecimal(63),
			new BigDecimal(52),
			new BigDecimal(40),
			new BigDecimal(28),
			new BigDecimal(15));
	private static final Map<String, Map<EffectDiv, StatusInfo>> STATUSMAP_FOR_TAST = new HashMap<>();
	static {
		Map<EffectDiv, StatusInfo> innerMap = new HashMap<>();
		innerMap.put(EffectDiv.NOVICE, STATUSINFO_FOR_TAST);
		STATUSMAP_FOR_TAST.put("test", innerMap);
	}

	/**
	 * StatusInfoAllの正常系のテスト
	 */
	@SuppressWarnings({ "unused" })
	@Test
	public void StatusInfoAllTest_001() throws IOException {
		new MockUp<StatusInfoAll>() {
			@Mock
			private Map<String, Map<EffectDiv, StatusInfo>> getStatusInfoMapFromScoreTool() {
				return STATUSMAP_FOR_TAST;
			}
		};
		StatusInfoAll statusInfoAll = new StatusInfoAll();
		Map<String, Map<EffectDiv, StatusInfo>> result = statusInfoAll.checkAndGetStatusInfoMap();
		Map<EffectDiv, StatusInfo> innerMap = result.get("test");
		assertNotEquals(innerMap, null);
		StatusInfo statusInfo = innerMap.get(EffectDiv.NOVICE);
		assertNotEquals(statusInfo, null);
		assertEquals(statusInfo.getTitle(), "test");
		assertEquals(statusInfo.getPGradeA(), new BigDecimal(95));
		assertEquals(statusInfo.getPGradeAp(), new BigDecimal(90));
		assertEquals(statusInfo.getPGradeAA(), new BigDecimal(81));
		assertEquals(statusInfo.getPGradeAAp(), new BigDecimal(70));
		assertEquals(statusInfo.getPGradeAAA(), new BigDecimal(63));
		assertEquals(statusInfo.getPGradeAAAp(), new BigDecimal(52));
		assertEquals(statusInfo.getPGradeS(), new BigDecimal(40));
		assertEquals(statusInfo.getPGrade995(), new BigDecimal(28));
		assertEquals(statusInfo.getPGrade998(), new BigDecimal(15));
		assertEquals(statusInfo.getPPer(), new BigDecimal(5));
	}

	/**
	 * StatusInfoAllの統計データ更新のテスト
	 */
	@SuppressWarnings({ "unused" })
	@Test
	public void StatusInfoAllTest_002() throws IOException {
		new MockUp<StatusInfoAll>() {
			@Mock
			private Map<String, Map<EffectDiv, StatusInfo>> getStatusInfoMapFromScoreTool() {
				return STATUSMAP_FOR_TAST;
			}
		};

		StatusInfoAll statusInfoAll = new StatusInfoAll();
		LocalDateTime before = Deencapsulation.getField(statusInfoAll, "lastUpdate");

		new MockUp<LocalDateTime>() {
			/**
			 * 必ず2019年1月6日(日曜日)を返す実装
			 */
			@Mock
			public LocalDateTime now() {
				LocalDateTime localDateTime = LocalDateTime.now(Clock.systemDefaultZone());
				localDateTime = localDateTime.withYear(2019);
				localDateTime = localDateTime.withMonth(1);
				localDateTime = localDateTime.withDayOfMonth(6);
				return localDateTime;
			}
		};

		statusInfoAll.checkAndGetStatusInfoMap();
		LocalDateTime first = Deencapsulation.getField(statusInfoAll, "lastUpdate");

		new MockUp<LocalDateTime>() {
			/**
			 * 必ず2019年1月7日(月曜日)を返す実装
			 */
			@Mock
			public LocalDateTime now() {
				LocalDateTime localDateTime = LocalDateTime.now(Clock.systemDefaultZone());
				localDateTime = localDateTime.withYear(2019);
				localDateTime = localDateTime.withMonth(1);
				localDateTime = localDateTime.withDayOfMonth(7);
				return localDateTime;
			}
		};
		statusInfoAll.checkAndGetStatusInfoMap();
		LocalDateTime second = Deencapsulation.getField(statusInfoAll, "lastUpdate");

		new MockUp<LocalDateTime>() {
			/**
			 * 必ず2019年1月8日(火曜日)を返す実装
			 */
			@Mock
			public LocalDateTime now() {
				LocalDateTime localDateTime = LocalDateTime.now(Clock.systemDefaultZone());
				localDateTime = localDateTime.withYear(2019);
				localDateTime = localDateTime.withMonth(1);
				localDateTime = localDateTime.withDayOfMonth(8);
				return localDateTime;
			}
		};
		statusInfoAll.checkAndGetStatusInfoMap();
		LocalDateTime third = Deencapsulation.getField(statusInfoAll, "lastUpdate");
		statusInfoAll.checkAndGetStatusInfoMap();
		LocalDateTime fourth = Deencapsulation.getField(statusInfoAll, "lastUpdate");

		// 実行前はnull
		assertEquals(before, null);
		// 日曜日→月曜日は変化なし
		assertEquals(first, second);
		// 火曜日に変化
		assertNotEquals(first, third);
		// 火曜日であってもその日2回目の実行は変化なし
		assertEquals(third, fourth);
	}

	/**
	 * ScoreEstimateRateCalculatorのテストです。
	 */
	@Test
	public void ScoreEstimateRateCalculatorTest() {
		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 0), new BigDecimal("100.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 8500000),
				new BigDecimal("95.11"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 8600000),
				new BigDecimal("95.06"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 8700000),
				new BigDecimal("95.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 8800000),
				new BigDecimal("93.33"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 8900000),
				new BigDecimal("91.67"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9000000),
				new BigDecimal("90.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9050000),
				new BigDecimal("88.50"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9100000),
				new BigDecimal("87.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9150000),
				new BigDecimal("85.50"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9200000),
				new BigDecimal("84.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9250000),
				new BigDecimal("82.50"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9300000),
				new BigDecimal("81.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9350000),
				new BigDecimal("78.25"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9400000),
				new BigDecimal("75.50"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9450000),
				new BigDecimal("72.75"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9500000),
				new BigDecimal("70.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9550000),
				new BigDecimal("68.25"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9660000),
				new BigDecimal("64.40"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9700000),
				new BigDecimal("63.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9760000),
				new BigDecimal("56.40"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9800000),
				new BigDecimal("52.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9840000),
				new BigDecimal("47.20"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9900000),
				new BigDecimal("40.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9920000),
				new BigDecimal("35.20"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9950000),
				new BigDecimal("28.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9960000),
				new BigDecimal("23.67"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9980000),
				new BigDecimal("15.00"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 9999000),
				new BigDecimal("5.50"));

		assertEquals(KadaiGeneratorUtil.getEstimateRate(STATUSINFO_FOR_TAST, 10000000),
				new BigDecimal("5.00"));
	}
}
