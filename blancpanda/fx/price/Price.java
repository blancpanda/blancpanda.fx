package blancpanda.fx.price;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import blancpanda.fx.CandleStick;
import blancpanda.fx.FXUtils;

public class Price extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20090623L;
	
	private JLabel lbl_bid;
	private JLabel lbl_ask;
	private JLabel lbl_cp;
	private double pre_bid;
	private double pre_ask;
	private DecimalFormat df;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CandleStick cs = new CandleStick(CandleStick.USDJPY, CandleStick.M5);
		Price price = new Price(cs);
		JFrame frame = new JFrame("");
		frame.getContentPane().add(price, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(10, 10, 240, 120);
		frame.setVisible(true);
	}
	
	public Price(CandleStick cs){
		df = new DecimalFormat("##0.000");
		
		LineBorder lb = new LineBorder(Color.LIGHT_GRAY, 4);
		lbl_cp = new JLabel(FXUtils.STR_CURRENCY_PAIR[cs.getCurrency_pair()]);
		lbl_cp.setHorizontalAlignment(JLabel.CENTER);
		lbl_cp.setVerticalAlignment(JLabel.CENTER);
		
		lbl_bid = new JLabel(df.format(cs.getBid_close()));
		lbl_bid.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		lbl_bid.setHorizontalAlignment(JLabel.CENTER);
		lbl_bid.setVerticalAlignment(JLabel.CENTER);
		TitledBorder tb_bid = new TitledBorder(lb, "Bid", TitledBorder.LEFT, TitledBorder.TOP);
		lbl_bid.setBorder(tb_bid);

		lbl_ask = new JLabel(df.format(cs.getAsk_close()));
		lbl_ask.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		lbl_ask.setHorizontalAlignment(JLabel.CENTER);
		lbl_ask.setVerticalAlignment(JLabel.CENTER);
		TitledBorder tb_ask = new TitledBorder(lb, "Ask", TitledBorder.LEFT, TitledBorder.TOP);
		lbl_ask.setBorder(tb_ask);

		LineBorder price_border = new LineBorder(Color.LIGHT_GRAY, 4);
		this.setBorder(price_border);
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth = 2;
	    gbc.gridheight = 1;
	    gbc.weighty = 0.2d;
	    gbc.fill = GridBagConstraints.BOTH;
	    layout.setConstraints(lbl_cp, gbc);
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.gridwidth = 1;
	    gbc.gridheight = 1;
	    gbc.weightx = 1.0d;
	    gbc.weighty = 0.8d;
	    gbc.fill = GridBagConstraints.BOTH;
	    layout.setConstraints(lbl_bid, gbc);
	    gbc.gridx = 1;
	    gbc.gridy = 1;
	    gbc.gridwidth = 1;
	    gbc.gridheight = 1;
	    gbc.weightx = 1.0d;
	    gbc.weighty = 0.8d;
	    gbc.fill = GridBagConstraints.BOTH;
	    layout.setConstraints(lbl_ask, gbc);

		this.add(lbl_cp);
		this.add(lbl_bid);
		this.add(lbl_ask);

		pre_bid = cs.getBid_close();
		pre_ask = cs.getAsk_close();
	}
	
	public void updatePricePanel(CandleStick cs){
		if(pre_bid < cs.getBid_close()){
			// 上がった
			lbl_bid.setForeground(Color.RED);
		} else if(pre_bid == cs.getBid_close()){
			// そのまま
			lbl_bid.setForeground(Color.BLACK);
		} else {
			// 下がった
			lbl_bid.setForeground(Color.BLUE);
		}
		if(pre_ask < cs.getAsk_close()){
			// 上がった
			lbl_ask.setForeground(Color.RED);
		} else if(pre_ask == cs.getAsk_close()){
			// そのまま
			lbl_ask.setForeground(Color.BLACK);
		} else {
			// 下がった
			lbl_ask.setForeground(Color.BLUE);
		}
		lbl_cp.setText(FXUtils.STR_CURRENCY_PAIR[cs.getCurrency_pair()]);
		lbl_bid.setText(df.format(cs.getBid_close()));
		lbl_ask.setText(df.format(cs.getAsk_close()));
		pre_bid = cs.getBid_close();
		pre_ask = cs.getAsk_close();
	}

}
