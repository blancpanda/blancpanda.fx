package blancpanda.fx.chart.indicator;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;

public class SimpleMovingAverage {

	private static final int SMA_CLOSE = 0;
	private static final int SMA_SHORTER = 1;
	private static final int SMA_LONGER = 2;

	/**
	 * 単純移動平均線の短期線のピリオド数
	 */
	private static final int SMA_SHORTER_PERIODS = 21;

	/**
	 * 単純移動平均線の長期線のピリオド数
	 */
	private static final int SMA_LONGER_PERIODS = 90;

	public static TimeSeriesCollection getSimpleMovingAverage(OHLCSeries candle) {
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		// 終値のTimeSeriesを作成
		TimeSeries close = new TimeSeries("close");
		for (int i = 0; i < candle.getItemCount(); i++) {
			OHLCItem item = (OHLCItem) candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}
		tsc.addSeries(close);

		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		tsc.addSeries(sma_shorter);

		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		tsc.addSeries(sma_longer);

		return tsc;
	}

	public static void updatePartOfSMA(OHLCSeries candle,
			TimeSeriesCollection sma) {
		// 終値線の最後の方を更新または付け足し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		for (int i = close.getItemCount() - 1; i < candle.getItemCount(); i++) {
			if (i >= 0) {
				OHLCItem item = (OHLCItem) candle.getDataItem(i);
				close.addOrUpdate(item.getPeriod(), item.getCloseValue());
			}
		}
		// 短期線を更新
		TimeSeries sma_shorter = sma.getSeries(SMA_SHORTER);
		sma_shorter.addAndOrUpdate(MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, candle.getItemCount() - 1));
		// 長期線を更新
		TimeSeries sma_longer = sma.getSeries(SMA_LONGER);
		sma_longer.addAndOrUpdate(MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, candle.getItemCount() - 1));
	}

	public static void updateAllOfSMA(OHLCSeries candle,
			TimeSeriesCollection sma) {
		// 終値線を作り直し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		close.clear();
		for (int i = 0; i < candle.getItemCount(); i++) {
			OHLCItem item = (OHLCItem) candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}

		// 短期線、長期線を削除
		sma.removeSeries(SMA_LONGER);
		sma.removeSeries(SMA_SHORTER);

		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		sma.addSeries(sma_shorter);

		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		sma.addSeries(sma_longer);
	}

	public static XYLineAndShapeRenderer getSimpleMovingAverateRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesLinesVisible(SMA_CLOSE, false); // closeRateを非表示にする
		xyr.setSeriesShapesVisible(SMA_CLOSE, false);
		xyr.setSeriesShapesVisible(SMA_SHORTER, false); // shapeを非表示にする
		xyr.setSeriesStroke(SMA_SHORTER, new BasicStroke(1, 1, 1));
		xyr.setSeriesShapesVisible(SMA_LONGER, false);
		xyr.setSeriesStroke(SMA_LONGER, new BasicStroke(1, 1, 1));
		xyr.setSeriesPaint(SMA_SHORTER, Color.RED); // 短期線は赤
		xyr.setSeriesPaint(SMA_LONGER, Color.BLUE); // 長期線は青
		return xyr;
	}


}
