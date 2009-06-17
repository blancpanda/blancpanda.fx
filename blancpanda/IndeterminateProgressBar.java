/**
 * 
 */
package blancpanda;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * @author Kaoru
 * 
 */
public class IndeterminateProgressBar {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndeterminateProgressBar.show("チャートを更新しています");
	}
	public static JFrame show(String str){
		UIManager.put("ProgressBar.repaintInterval", new Integer(150));
		UIManager.put("ProgressBar.cycleTime", new Integer(1050));
		final JProgressBar aJProgressBar = new JProgressBar(0, 100);
		aJProgressBar.setIndeterminate(true);
		Border border = new EmptyBorder( 10, 10, 10, 10);
		aJProgressBar.setBorder(border);
		JLabel msg = new JLabel(str);
		border = new EmptyBorder( 10, 10, 10, 10);
		msg.setBorder(border);
		JPanel header = new JPanel();
		header.add(msg, BorderLayout.CENTER);
		JFrame theFrame = new JFrame();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = theFrame.getContentPane();
		contentPane.add(aJProgressBar, BorderLayout.CENTER);
		contentPane.add(header, BorderLayout.NORTH);
		theFrame.setSize(300, 100);
		theFrame.setVisible(true);
		return theFrame;
	}
}
