package blancpanda.fx.chart;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

import blancpanda.IndeterminateProgressBar;
import blancpanda.fx.CandleStick;
import blancpanda.fx.FXUtils;
import blancpanda.fx.chart.indicator.BollingerBandsFibonacciRatios;
import blancpanda.fx.chart.indicator.Ichimoku;
import blancpanda.fx.chart.indicator.SimpleMovingAverage;

public class FXChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20090614L;
	
	private static JComboBox cmb_currency_pair;
	private static JComboBox cmb_period;
	
	/**
	 * データベースへの接続の有無
	 */
	private boolean db;
	
	/**
	 * ロウソクの最大表示数
	 */
	private int max_visible;
	
	/**
	 * 時間間隔
	 */
	private int period;
	
	/**
	 * 通貨ペア
	 */
	private int currency_pair;

	/**
	 * X軸(時間)
	 */
	private DateAxis domain;

	/**
	 * 直前のピリオド
	 */
	private RegularTimePeriod pre_period;
	
	/**
	 * リアルタイム処理用CandleStick
	 */
	private CandleStick cs;
	
	/**
	 * ロウソク足
	 */
	private OHLCSeries candle;
	
	/**
	 * 現在のレートを示すマーカー
	 */
	private ValueMarker marker;
		
	/**
	 * 単純移動平均
	 */
	private TimeSeriesCollection tscSMA;
	
	/**
	 * 一目均衡表
	 */
	private TimeSeriesCollection[] tscICHI;
	private TimeSeriesCollection tscICHI0;
	private TimeSeriesCollection tscICHI1;
	
	/**
	 * ボリンジャー-フィボナッチ
	 */
	private TimeSeriesCollection tscBOFI;
	
	private IndeterminateProgressBar progress;
	private DecimalFormat df;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("リアルタイム為替チャート");
		FXChart panel = new FXChart(true, CandleStick.USDJPY, CandleStick.M5, 60); 
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		String[] str_currency_pair = {
				"USD/CAD",
				"EUR/JPY",
				"NZD/JPY",
				"GBP/CHF",
				"USD/CHF",
				"ZAR/JPY",
				"NZD/USD",
				"CAD/JPY",
				"EUR/GBP",
				"USD/JPY",	// 米ドル円
				"CHF/JPY",
				"GBP/JPY", 	// 英ポンド円
				"GBP/USD",
				"AUD/JPY",
				"EUR/USD",
				"AUD/USD"};
		cmb_currency_pair = new JComboBox(str_currency_pair);
		cmb_currency_pair.setSelectedIndex(CandleStick.USDJPY);
		cmb_currency_pair.addActionListener(panel.new ChartChanger());
		
		String[] str_period = {"M1", "M5", "M30", "H1", "H2"};
		cmb_period = new JComboBox(str_period);
		cmb_period.setSelectedIndex(CandleStick.M5);
		cmb_period.addActionListener(panel.new ChartChanger());
		
		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
		header.add(cmb_currency_pair);
		header.add(cmb_period);
		frame.getContentPane().add(header, BorderLayout.NORTH);
		
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setBounds( 10, 10, 600, 480);
	    frame.setVisible(true);
	    
	    
	    Timer timer = panel.new DataGenerator(1000);
		timer.start();
	}
	
	public FXChart(boolean db, int currency_pair, int period, int max){
		super(new BorderLayout());
		this.db = db;
		this.max_visible = max;
		this.currency_pair = currency_pair;
		this.period = period;
		progress = new IndeterminateProgressBar();
		cs = new CandleStick(currency_pair, period);
		candle = new OHLCSeries("CandleStick");
		ChartPanel chart = new ChartPanel(createChart());
		add(chart);
		df = new DecimalFormat("###.000");
	}
	
	private JFreeChart createChart() {
		progress.showProgress("チャートを作成しています");
		
		// 定義
		XYPlot plot;
		NumberAxis range;
		OHLCSeriesCollection osc;
		CandlestickRenderer cr;
		XYLineAndShapeRenderer xyr;
		XYDifferenceRenderer xdr;
		SegmentedTimeline fxtimeline;
		
		// プロットの作成
		plot = new XYPlot();
		
		// X軸
		domain = new DateAxis(""); // Time
		// 取引時間に対応
		fxtimeline = FXUtils.getFXTimeline();
		domain.setTimeline(fxtimeline);
		
		// Y軸
		range = new NumberAxis(""); // Price
		// 0を含まずに自動調整
		range.setAutoRangeIncludesZero(false);
		
		// マーカー
		marker = FXChartUtils.createMarker(0);
		
		// プロットに軸を追加
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		plot.addRangeMarker(marker);

		
		// 各データの作成
		// ロウソク足データ
		if(db){
			candle = FXChartUtils.loadCandleStick(candle, currency_pair, period);
		}
		osc = new OHLCSeriesCollection();
		osc.addSeries(candle);
		
		// テクニカル指標
		tscSMA = SimpleMovingAverage.getSimpleMovingAverage(candle);
		tscICHI = Ichimoku.getIchimoku(candle);
		tscICHI0 = tscICHI[0];
		tscICHI1 = tscICHI[1];
		tscBOFI = BollingerBandsFibonacciRatios.getBollingerFibonacci(candle);
		
		// プロットにデータを追加
		plot.setDataset(0, osc);
		plot.setDataset(1, tscSMA);
		plot.setDataset(2, tscICHI0);
		plot.setDataset(3, tscICHI1);
		plot.setDataset(4, tscBOFI);
		
		// データと軸の対応関係を設定
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToDomainAxis(1, 0);
		plot.mapDatasetToRangeAxis(1, 0);		
		plot.mapDatasetToDomainAxis(2, 0);
		plot.mapDatasetToRangeAxis(2, 0);		
		plot.mapDatasetToDomainAxis(3, 0);
		plot.mapDatasetToRangeAxis(3, 0);		
		plot.mapDatasetToDomainAxis(4, 0);
		plot.mapDatasetToRangeAxis(4, 0);		
		
		// 見た目の設定
		// ロウソク足
		cr = FXChartUtils.getCandleStickRenderer();
		plot.setRenderer(0, cr);		
		// 移動平均
		xyr = SimpleMovingAverage.getSimpleMovingAverateRenderer();
		plot.setRenderer(1, xyr);		
		// 一目均衡表
		xyr = Ichimoku.getIchimokuRenderer();
		plot.setRenderer(2, xyr);
		xdr = Ichimoku.getIchimokuKumoRenderer();
		plot.setRenderer(3, xdr);
		// ボリンジャー-フィボナッチ
		xyr = BollingerBandsFibonacciRatios.getBollingerFibonacciRenderer();
		plot.setRenderer(4, xyr);
		
		// 重ね順
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		
		JFreeChart jfreechart = new JFreeChart(null, null, plot, false);
		progress.hideProgress();
		
		return jfreechart;
	}
	
	@SuppressWarnings("unchecked")
	private void updateSeries(){
		// なぜか取得日時が逆戻りするので、戻った場合には何もしない。
		RegularTimePeriod prd;
		prd = cs.getCurrentRate(FXUtils.getRateData());
		if (prd.compareTo(pre_period) >= 0) {
			int index = candle.indexOf(prd);
			if (index >= 0) {
				candle.remove(prd);
			} else {
				cs.clearRate();
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
			// 移動平均
			SimpleMovingAverage.updatePartOfSMA(candle, tscSMA);
			// 一目均衡表
			Ichimoku.updatePartOfIchimoku(candle, tscICHI);
			// ボリンジャー-フィボナッチ
			BollingerBandsFibonacciRatios.updatePartOfBollingerFibonacci(candle, tscBOFI);
			
			// マーカー
			marker.setValue(cs.getBid_close());
			marker.setLabel(df.format(cs.getBid_close()));
			
			// 時間の表示範囲
			domain.setMinimumDate(FXUtils.calcDateAxisMin(prd.getEnd(), period, max_visible));
			// 最大は先行スパン？
			//domain.setMaximumDate(prd.getEnd());

			pre_period = prd;
		}	
	}
	
	private void updateChart() {
		// プログレスバーを表示
		//progress.showProgress("チャートを更新しています");
		
		boolean changed = false;
		if(period != cmb_period.getSelectedIndex()){
			// ピリオドが変わった
			period = cmb_period.getSelectedIndex();
			changed = true;
		}
		if(currency_pair != cmb_currency_pair.getSelectedIndex()){
			// 通貨ペアが変わった
			currency_pair = cmb_currency_pair.getSelectedIndex();
			changed = true;
		}
		if(changed){
			// ピリオドか通貨ペアが変わったら
			// CandleStickを書き換える
			cs = new CandleStick(currency_pair, period);
			// ロウソク足を全部消す
			candle.clear();
			if(db){
				// DBから読み込み直す
				candle = FXChartUtils.loadCandleStick(candle, currency_pair, period);
			}
			// 移動平均
			SimpleMovingAverage.updateAllOfSMA(candle, tscSMA);
			// 一目均衡表
			Ichimoku.updateAllOfIchimoku(candle, tscICHI);
			// ボリンジャー-フィボナッチ
			BollingerBandsFibonacciRatios.updateAllOfBollingerFibonacci(candle, tscBOFI);
			
/*			// 時間の表示範囲
			if(candle.getItemCount() >= 1){
				domain.setMinimumDate(FXUtils.calcDateAxisMin(candle.getPeriod(candle.getItemCount() - 1).getEnd(), period, max_visible));
				//domain.setMaximumDate(candle.getPeriod(candle.getItemCount() - 1).getEnd());
			}
*/			
			
			changed = false;
			
			// プログレスバーを隠す
			//progress.hideProgress();
		}
	}


	class DataGenerator extends Timer implements ActionListener {

		public DataGenerator(int delay) {
			super(delay, null);
			addActionListener(this);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 20090615L;

		public void actionPerformed(ActionEvent e) {
			// 各Seriesを更新
			updateSeries();	
		}
		
	}
	
	class ChartChanger implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// チャートを更新
			updateChart();			
		}
		
	}
}
