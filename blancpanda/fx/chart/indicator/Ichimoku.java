/**
 * 
 */
package blancpanda.fx.chart.indicator;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;

import blancpanda.fx.chart.FXChartUtils;

/**
 * @author Kaoru
 *
 */
public class Ichimoku {

	private static final int ICHIMOKU_KIJUN_PERIODS = 26;
	private static final int ICHIMOKU_TENKAN_PERIODS = 9;
	private static final int ICHIMOKU_KIJUN = 0;
	private static final int ICHIMOKU_TENKAN = 1;
	private static final int ICHIMOKU_SENKO1 = 0;
	private static final int ICHIMOKU_SENKO2 = 1;
	private static final int ICHIMOKU_CHIKO = 2;

	
	public static TimeSeriesCollection[] getIchimoku(OHLCSeries candle) {
		TimeSeriesCollection[] tsc = new TimeSeriesCollection[2];
		tsc[0] = new TimeSeriesCollection();
		tsc[1] = new TimeSeriesCollection();
		TimeSeries kijun = new TimeSeries("kijun");
		TimeSeries tenkan = new TimeSeries("tenkan");
		TimeSeries senko1 = new TimeSeries("senko1");
		TimeSeries senko2 = new TimeSeries("senko2");
		TimeSeries chiko = new TimeSeries("chiko");

		TimeSeriesDataItem[] item;
		OHLCItem tmp;
		RegularTimePeriod prd;

		// 基準線
		for (int i = ICHIMOKU_KIJUN_PERIODS - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS);
			kijun.add(item[FXChartUtils.MID]);
		}

		// 転換線
		for (int i = ICHIMOKU_TENKAN_PERIODS - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_TENKAN_PERIODS);
			tenkan.add(item[FXChartUtils.MID]);
		}

		// 先行スパン1
		// 基準線と転換線の中値を取る（基準線の方が短いはず）
		for (int i = 0; i < kijun.getItemCount(); i++) {
			double val = (kijun.getValue(i).doubleValue() + tenkan.getValue(i)
					.doubleValue()) / 2;
			// 26日先行させる
			prd = FXChartUtils.addTimePeriod(kijun.getTimePeriod(i), ICHIMOKU_KIJUN_PERIODS);
			senko1.add(prd, val);
		}

		// 先行スパン2
		for (int i = ICHIMOKU_KIJUN_PERIODS * 2 - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS * 2);
			// 26日先行させる
			prd = FXChartUtils.addTimePeriod(candle.getPeriod(i), ICHIMOKU_KIJUN_PERIODS);
			senko2.add(prd, item[FXChartUtils.MID].getValue().doubleValue());
		}

		// 遅行線
		// ロウソク足の終値を登録
		for (int i = 0; i < candle.getItemCount(); i++) {
			tmp = (OHLCItem) candle.getDataItem(i);
			// 26日遅行させる
			prd = FXChartUtils.addTimePeriod(candle.getPeriod(i), ICHIMOKU_KIJUN_PERIODS * -1);
			chiko.add(prd, tmp.getCloseValue());
		}

		// TimeSeriesCollectionに追加(Rendererが違うので、先行スパン1、2は別のシリーズに)
		tsc[0].addSeries(kijun);
		tsc[0].addSeries(tenkan);
		tsc[1].addSeries(senko1);
		tsc[1].addSeries(senko2);
		tsc[0].addSeries(chiko);

		return tsc;
	}
	
	public static void updatePartOfIchimoku(OHLCSeries candle, TimeSeriesCollection[] tsc){
		TimeSeries kijun = tsc[0].getSeries(ICHIMOKU_KIJUN);
		TimeSeries tenkan = tsc[0].getSeries(ICHIMOKU_TENKAN);
		TimeSeries senko1 = tsc[1].getSeries(ICHIMOKU_SENKO1);
		TimeSeries senko2 = tsc[1].getSeries(ICHIMOKU_SENKO2);
		TimeSeries chiko = tsc[0].getSeries(ICHIMOKU_CHIKO);
		
		int candle_index = candle.getItemCount() - 1;

		// 基準線
		TimeSeriesDataItem[] item = FXChartUtils.getMaxMinItem(candle, candle_index,
				ICHIMOKU_KIJUN_PERIODS);
		kijun.addOrUpdate(item[FXChartUtils.MID].getPeriod(), item[FXChartUtils.MID].getValue().doubleValue());
		
		int kijun_index = kijun.getItemCount() - 1;

		// 転換線
		item = FXChartUtils.getMaxMinItem(candle, candle_index,
				ICHIMOKU_TENKAN_PERIODS);
		tenkan.addOrUpdate(item[FXChartUtils.MID].getPeriod(), item[FXChartUtils.MID].getValue().doubleValue());

		// 先行スパン1
		// 基準線と転換線の中値を取る（基準線の方が短いはず）
		double val = (kijun.getValue(kijun_index).doubleValue() + kijun.getValue(kijun_index)
				.doubleValue()) / 2;
		// 26日先行させる
		RegularTimePeriod prd = FXChartUtils.addTimePeriod(kijun.getTimePeriod(kijun_index), ICHIMOKU_KIJUN_PERIODS);
		senko1.addOrUpdate(prd, val);

		// 先行スパン2
		item = FXChartUtils.getMaxMinItem(candle, candle_index,
				ICHIMOKU_KIJUN_PERIODS * 2);
		// 26日先行させる
		prd = FXChartUtils.addTimePeriod(item[FXChartUtils.MID].getPeriod(), ICHIMOKU_KIJUN_PERIODS);
		senko2.addOrUpdate(prd,
				item[FXChartUtils.MID].getValue().doubleValue());

		// 遅行線
		// ロウソク足の終値を登録
		OHLCItem tmp = (OHLCItem) candle.getDataItem(candle_index);
		// 26日遅行させる
		prd = FXChartUtils.addTimePeriod(tmp.getPeriod(), ICHIMOKU_KIJUN_PERIODS * -1);
		chiko.addOrUpdate(prd, tmp.getCloseValue());
	}
	
	public static void updateAllOfIchimoku(OHLCSeries candle, TimeSeriesCollection[] tsc){
		TimeSeries kijun = tsc[0].getSeries(ICHIMOKU_KIJUN);
		TimeSeries tenkan = tsc[0].getSeries(ICHIMOKU_TENKAN);
		TimeSeries senko1 = tsc[1].getSeries(ICHIMOKU_SENKO1);
		TimeSeries senko2 = tsc[1].getSeries(ICHIMOKU_SENKO2);
		TimeSeries chiko = tsc[0].getSeries(ICHIMOKU_CHIKO);
		
		kijun.clear();
		tenkan.clear();
		senko1.clear();
		senko2.clear();
		chiko.clear();

		TimeSeriesDataItem[] item;
		OHLCItem tmp;
		RegularTimePeriod prd;

		// 基準線
		for (int i = ICHIMOKU_KIJUN_PERIODS - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS);
			kijun.add(item[FXChartUtils.MID]);
		}

		// 転換線
		for (int i = ICHIMOKU_TENKAN_PERIODS - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_TENKAN_PERIODS);
			tenkan.add(item[FXChartUtils.MID]);
		}
		
		// 先行スパン1
		// 基準線と転換線の中値を取る（基準線の方が短いはず）
		for (int i = 0; i < kijun.getItemCount(); i++) {
			double val = (kijun.getValue(i).doubleValue() + tenkan.getValue(i)
					.doubleValue()) / 2;
			// 26日先行させる
			prd = FXChartUtils.addTimePeriod(kijun.getTimePeriod(i), ICHIMOKU_KIJUN_PERIODS);
			senko1.add(prd, val);
		}

		// 先行スパン2
		for (int i = ICHIMOKU_KIJUN_PERIODS * 2 - 1; i < candle.getItemCount(); i++) {
			item = FXChartUtils.getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS * 2);
			// 26日先行させる
			prd = FXChartUtils.addTimePeriod(candle.getPeriod(i), ICHIMOKU_KIJUN_PERIODS);
			senko2.add(prd, item[FXChartUtils.MID].getValue().doubleValue());
		}

		// 遅行線
		// ロウソク足の終値を登録
		for (int i = 0; i < candle.getItemCount(); i++) {
			tmp = (OHLCItem) candle.getDataItem(i);
			// 26日遅行させる
			prd = FXChartUtils.addTimePeriod(candle.getPeriod(i), ICHIMOKU_KIJUN_PERIODS * -1);
			chiko.add(prd, tmp.getCloseValue());
		}
	}
	
	public static XYLineAndShapeRenderer getIchimokuRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesShapesVisible(ICHIMOKU_KIJUN, false); // shapeを非表示にする
		xyr.setSeriesStroke(ICHIMOKU_KIJUN, new BasicStroke(2, 1, 1));
		xyr.setSeriesShapesVisible(ICHIMOKU_TENKAN, false); // shapeを非表示にする
		xyr.setSeriesStroke(ICHIMOKU_TENKAN, new BasicStroke(2, 1, 1));
		xyr.setSeriesShapesVisible(ICHIMOKU_CHIKO, false);
		xyr.setSeriesStroke(ICHIMOKU_CHIKO, new BasicStroke(1, 1, 1));
		xyr.setSeriesPaint(ICHIMOKU_KIJUN, new Color(0, 102, 255)); // 基準線は青
		xyr.setSeriesPaint(ICHIMOKU_TENKAN, new Color(255, 0, 102)); // 転換線は赤
		xyr.setSeriesPaint(ICHIMOKU_CHIKO, new Color(255, 153, 0)); // 遅行線はオレンジ
		return xyr;
	}

	public static XYDifferenceRenderer getIchimokuKumoRenderer() {
		XYDifferenceRenderer xdr = new XYDifferenceRenderer(new Color(102, 204, 255, 102),
				new Color(255, 102, 204, 102), false);
		xdr.setBaseStroke(new BasicStroke(1, 1, 1));
		xdr.setSeriesPaint(ICHIMOKU_SENKO1, new Color(153, 255, 153, 102));
		xdr.setSeriesPaint(ICHIMOKU_SENKO2, new Color(255, 153, 153, 102));
		return xdr;
	}
	
	public static void showICHI(XYLineAndShapeRenderer xyr){
		xyr.setSeriesLinesVisible(ICHIMOKU_KIJUN, true);
		xyr.setSeriesLinesVisible(ICHIMOKU_TENKAN, true);
		xyr.setSeriesLinesVisible(ICHIMOKU_CHIKO, true);
	}
	
	public static void showICHI_KUMO(XYDifferenceRenderer xdr){
		xdr.setSeriesPaint(ICHIMOKU_SENKO1, new Color(153, 255, 153, 102));
		xdr.setSeriesPaint(ICHIMOKU_SENKO2, new Color(255, 153, 153, 102));
		xdr.setPositivePaint(new Color(102, 204, 255, 102));
		xdr.setNegativePaint(new Color(255, 102, 204, 102));
	}
	
	public static void hideICHI(XYLineAndShapeRenderer xyr){
		xyr.setSeriesLinesVisible(ICHIMOKU_KIJUN, false);
		xyr.setSeriesLinesVisible(ICHIMOKU_TENKAN, false);
		xyr.setSeriesLinesVisible(ICHIMOKU_CHIKO, false);
	}
	
	public static void hideICHI_KUMO(XYDifferenceRenderer xdr){
		// 消し方がよくわからないので透明にする
		xdr.setSeriesPaint(ICHIMOKU_SENKO1, new Color(153, 255, 153, 0));
		xdr.setSeriesPaint(ICHIMOKU_SENKO2, new Color(255, 153, 153, 0));
		xdr.setPositivePaint(new Color(102, 204, 255, 0));
		xdr.setNegativePaint(new Color(255, 102, 204, 0));
	}
	
}
