/**
 * 
 */
package blancpanda.fx.chart.indicator;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;

import blancpanda.fx.chart.FXChartUtils;

/**
 * @author Kaoru
 * 
 */
public class BollingerBandsFibonacciRatios {
	/**
	 * 移動平均のピリオド数
	 */
	private static int MA_PERIODS = 20;
	private static double FIBO1 = 1.618;
	private static double FIBO2 = 2.618;
	private static double FIBO3 = 4.236;
	private static int UP3 = 0;
	private static int UP2 = 1;
	private static int UP1 = 2;
	private static int MA = 3;
	private static int LOW1 = 4;
	private static int LOW2 = 5;
	private static int LOW3 = 6;

	public static TimeSeriesCollection getBollingerFibonacci(OHLCSeries candle) {
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		TimeSeries up3;
		TimeSeries up2;
		TimeSeries up1;
		TimeSeries ma;
		TimeSeries low1;
		TimeSeries low2;
		TimeSeries low3;

		ma = new TimeSeries("ma");
		up3 = new TimeSeries("up3");
		up2 = new TimeSeries("up2");
		up1 = new TimeSeries("up1");
		low1 = new TimeSeries("low1");
		low2 = new TimeSeries("low2");
		low3 = new TimeSeries("low3");

		for (int i = 0; i < candle.getItemCount(); i++) {
			ma.add(candle.getPeriod(i), FXChartUtils.getCloseAvg(candle, i,
					MA_PERIODS));
		}

		for (int i = 0; i < candle.getItemCount(); i++) {
			double sd = FXChartUtils.getCloseSD(candle, i, MA_PERIODS, ma
					.getValue(i).doubleValue());
			up3.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO3);
			up2.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO2);
			up1.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO1);
			low1.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO1);
			low2.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO2);
			low3.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO3);
		}

		tsc.addSeries(up3);
		tsc.addSeries(up2);
		tsc.addSeries(up1);
		tsc.addSeries(ma);
		tsc.addSeries(low1);
		tsc.addSeries(low2);
		tsc.addSeries(low3);

		return tsc;
	}

	public static void updatePartOfBollingerFibonacci(OHLCSeries candle,
			TimeSeriesCollection tsc) {
		int index = candle.getItemCount() - 1;
		TimeSeries ma = tsc.getSeries(MA);
		ma.addOrUpdate(candle.getPeriod(index), FXChartUtils.getCloseAvg(
				candle, index, MA_PERIODS));
		double sd = FXChartUtils.getCloseSD(candle, index, MA_PERIODS, ma
				.getValue(index).doubleValue());
		tsc.getSeries(UP3).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() + sd * FIBO3);
		tsc.getSeries(UP2).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() + sd * FIBO2);
		tsc.getSeries(UP1).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() + sd * FIBO1);
		tsc.getSeries(LOW1).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() - sd * FIBO1);
		tsc.getSeries(LOW2).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() - sd * FIBO2);
		tsc.getSeries(LOW3).addOrUpdate(candle.getPeriod(index),
				ma.getValue(index).doubleValue() - sd * FIBO3);
	}

	public static void updateAllOfBollingerFibonacci(OHLCSeries candle,
			TimeSeriesCollection tsc) {
		TimeSeries up3 = tsc.getSeries(UP3);
		TimeSeries up2 = tsc.getSeries(UP2);
		TimeSeries up1 = tsc.getSeries(UP1);
		TimeSeries ma = tsc.getSeries(MA);
		TimeSeries low1 = tsc.getSeries(LOW1);
		TimeSeries low2 = tsc.getSeries(LOW2);
		TimeSeries low3 = tsc.getSeries(LOW3);

		up3.clear();
		up2.clear();
		up1.clear();
		ma.clear();
		low1.clear();
		low2.clear();
		low3.clear();

		for (int i = 0; i < candle.getItemCount(); i++) {
			ma.add(candle.getPeriod(i), FXChartUtils.getCloseAvg(candle, i,
					MA_PERIODS));
		}

		for (int i = 0; i < candle.getItemCount(); i++) {
			double sd = FXChartUtils.getCloseSD(candle, i, MA_PERIODS, ma
					.getValue(i).doubleValue());
			up3.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO3);
			up2.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO2);
			up1.add(candle.getPeriod(i), ma.getValue(i).doubleValue() + sd
					* FIBO1);
			low1.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO1);
			low2.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO2);
			low3.add(candle.getPeriod(i), ma.getValue(i).doubleValue() - sd
					* FIBO3);
		}
	}

	public static XYLineAndShapeRenderer getBollingerFibonacciRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesShapesVisible(UP3, false); // shapeを非表示にする
		xyr.setSeriesShapesVisible(UP2, false);
		xyr.setSeriesShapesVisible(UP1, false);
		xyr.setSeriesShapesVisible(MA, false);
		xyr.setSeriesShapesVisible(LOW1, false);
		xyr.setSeriesShapesVisible(LOW2, false);
		xyr.setSeriesShapesVisible(LOW3, false);
		xyr.setSeriesStroke(UP3, new BasicStroke(2, 1, 1));	 // UP3
		xyr.setSeriesStroke(UP2, new BasicStroke(1, 1, 1));	 // UP2
		xyr.setSeriesStroke(UP1, new BasicStroke(1,
				BasicStroke.CAP_BUTT,				 // 端は残す
				BasicStroke.JOIN_MITER,				 // 継ぎ目は斜め
				10.0f,								 // 継ぎ目の限界
				new float[] { 4.0f, 3.0f },			 // 4書いて3書かないダッシュ
				0.0f)								 // ダッシュの開始フェーズ
				);											 // UP1
		xyr.setSeriesStroke(MA, new BasicStroke(1, 1, 1));	 // MA
		xyr.setSeriesStroke(LOW1, new BasicStroke(1,
				BasicStroke.CAP_BUTT,				 // 端は残す
				BasicStroke.JOIN_MITER,				 // 継ぎ目は斜め
				10.0f,								 // 継ぎ目の限界
				new float[] { 4.0f, 3.0f },			 // 4書いて3書かないダッシュ
				0.0f)								 // ダッシュの開始フェーズ
				);											 // LOW1
		xyr.setSeriesStroke(LOW2, new BasicStroke(1, 1, 1)); // LOW2
		xyr.setSeriesStroke(LOW3, new BasicStroke(2, 1, 1)); // LOW3
		xyr.setSeriesPaint(UP3, new Color(51, 204, 204));	// UP3
		xyr.setSeriesPaint(UP2, new Color(51, 204, 204));	// UP2
		xyr.setSeriesPaint(UP1, new Color(51, 204, 204));	// UP1
		xyr.setSeriesPaint(MA, new Color(51, 204, 204));	// MA
		xyr.setSeriesPaint(LOW1, new Color(51, 204, 204));	// LOW1
		xyr.setSeriesPaint(LOW2, new Color(51, 204, 204));	// LOW2
		xyr.setSeriesPaint(LOW3, new Color(51, 204, 204));	// LOW3
		return xyr;
	}
	
	public static void showBOFI(XYLineAndShapeRenderer xyr){
		xyr.setSeriesLinesVisible(UP3, true);
		xyr.setSeriesLinesVisible(UP2, true);
		xyr.setSeriesLinesVisible(UP1, true);
		xyr.setSeriesLinesVisible(MA, true);
		xyr.setSeriesLinesVisible(LOW1, true);
		xyr.setSeriesLinesVisible(LOW2, true);
		xyr.setSeriesLinesVisible(LOW3, true);
	}
	
	public static void hideBOFI(XYLineAndShapeRenderer xyr){
		xyr.setSeriesLinesVisible(UP3, false);
		xyr.setSeriesLinesVisible(UP2, false);
		xyr.setSeriesLinesVisible(UP1, false);
		xyr.setSeriesLinesVisible(MA, false);
		xyr.setSeriesLinesVisible(LOW1, false);
		xyr.setSeriesLinesVisible(LOW2, false);
		xyr.setSeriesLinesVisible(LOW3, false);
	}
	
}
