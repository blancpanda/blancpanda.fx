/**
 * 
 */
package blancpanda.fx.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;

import blancpanda.fx.CandleStick;
import blancpanda.fx.CandleStickDao;
import blancpanda.fx.FXUtils;

/**
 * @author Kaoru
 * 
 */
public class FXChartUtils {
	
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
	
	public static OHLCSeries loadCandleStick(OHLCSeries candle,
			int currency_pair, int period) {
		CandleStick cs = new CandleStick();
		CandleStickDao csDao = new CandleStickDao();
		List<CandleStick> list = csDao.getRecentList(period);
		// List<CandleStick> list = csDao.getRecentList(period, currency_pair);
		int serice = list.size();
		RegularTimePeriod prd = null;
		for (int i = serice - 1; i >= 0; i--) { // 時間の降順で取得してくる
			cs = list.get(i);
			// DBに重複データができてしまっている可能性がある
			prd = FXUtils.getRegularTimePeriod(cs.getDate(), period);
			int index = candle.indexOf(prd);
			// System.out.println("index:" + index);
			if (index >= 0) {
				candle.remove(prd);
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
		}
		return candle;
	}
	
	public static TimeSeriesCollection getSimpleMovingAverage(OHLCSeries candle){
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		// 終値のTimeSeriesを作成
		TimeSeries close = new TimeSeries("close");
		for(int i = 0; i < candle.getItemCount(); i++){
			OHLCItem item = (OHLCItem)candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}
		tsc.addSeries(close);
		
		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close, "sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		tsc.addSeries(sma_shorter);
		
		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close, "sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		tsc.addSeries(sma_longer);

		return tsc;
	}
	
	public static TimeSeriesCollection updatePartOfSMA(OHLCSeries candle, TimeSeriesCollection sma){
		// 終値線の最後の方を更新または付け足し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		for(int i = close.getItemCount() - 1; i < candle.getItemCount(); i++){
			if(i >= 0){
				OHLCItem item = (OHLCItem)candle.getDataItem(i);
				close.addOrUpdate(item.getPeriod(), item.getCloseValue());
			}
		}
		// 短期線を更新
		TimeSeries sma_shorter = sma.getSeries(SMA_SHORTER);
		sma_shorter.addAndOrUpdate(MovingAverage.createMovingAverage(close, "sma_shorter", SMA_SHORTER_PERIODS, candle.getItemCount() - 1));
		// 長期線を更新
		TimeSeries sma_longer = sma.getSeries(SMA_LONGER);
		sma_longer.addAndOrUpdate(MovingAverage.createMovingAverage(close, "sma_longer", SMA_LONGER_PERIODS, candle.getItemCount() - 1));
		return sma;
	}

	public static TimeSeriesCollection updateAllOfSMA(OHLCSeries candle, TimeSeriesCollection sma){
		// 終値線を作り直し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		close.clear();
		for(int i = 0; i < candle.getItemCount(); i++){
			OHLCItem item = (OHLCItem)candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}
		
		// 短期線、長期線を削除
		sma.removeSeries(SMA_LONGER);
		sma.removeSeries(SMA_SHORTER);

		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close, "sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		sma.addSeries(sma_shorter);
		
		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close, "sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		sma.addSeries(sma_longer);
		return sma;
	}

	public static CandlestickRenderer getCandleStickRenderer() {
		CandlestickRenderer cr;
		cr = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
		// ローソク足の色を変える
		// 陽線を白に
		cr.setUpPaint(Color.WHITE);
		// 陰線を青に
		cr.setDownPaint(Color.BLACK);
		// ローソクの枠を黒、1ピクセルで描画
		cr.setUseOutlinePaint(true);
		cr.setBaseOutlinePaint(Color.BLACK);
		cr.setBaseOutlineStroke(new BasicStroke(1));
		return cr;
	}

	public static XYLineAndShapeRenderer getSimpleMovingAverateRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesLinesVisible(SMA_CLOSE, false); // closeRateを非表示にする
		xyr.setSeriesShapesVisible(SMA_CLOSE, false);
		xyr.setSeriesShapesVisible(SMA_SHORTER, false); // shapeを非表示にする
		xyr.setSeriesStroke(SMA_SHORTER, new BasicStroke(3, 1, 1));
		xyr.setSeriesShapesVisible(SMA_LONGER, false);
		xyr.setSeriesStroke(SMA_LONGER, new BasicStroke(3, 1, 1));
		xyr.setSeriesPaint(SMA_SHORTER, Color.RED);	// 短期線は赤
		xyr.setSeriesPaint(SMA_LONGER, Color.BLUE);	// 長期線は青
		return xyr;
	}
}
