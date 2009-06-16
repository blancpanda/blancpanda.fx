package blancpanda.fx.timeperiod;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;

/*
 * 5分刻みのTimePeriod
 * 0-4,5-9,10-14,15-19,20-24,25-29,30-34,35-39,40-44,45-49,50-54,55-59
 */
public class Minute5 extends RegularTimePeriod implements Serializable {

	private static final long serialVersionUID = 20090611L;
	private Date date;
	private Calendar cal;
	private long firstMillisecond;
	private long lastMillisecond;
	
	private Day day;
    private byte hour;
    private byte minute;

	public Minute5(Date date) {
		this.date = date;
		cal = Calendar.getInstance();
		cal.setTime(date);
		peg(cal);
		cal.setTimeInMillis(getFirstMillisecond());
		minute = (byte)cal.get(Calendar.MINUTE);
		hour = (byte)cal.get(Calendar.HOUR_OF_DAY);
		day = new Day(date);
	}

	private long calcStart(Calendar cal) {
		long start;
		cal.setTime(date);
		if (0 <= cal.get(Calendar.MINUTE) && cal.get(Calendar.MINUTE) <= 4) {
			cal = getStartDate(cal, 0);
		} else if (5 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 9) {
			cal = getStartDate(cal, 5);
		} else if (10 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 14) {
			cal = getStartDate(cal, 10);
		} else if (15 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 19) {
			cal = getStartDate(cal, 15);
		} else if (20 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 24) {
			cal = getStartDate(cal, 20);
		} else if (25 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 29) {
			cal = getStartDate(cal, 25);
		} else if (30 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 34) {
			cal = getStartDate(cal, 30);
		} else if (35 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 39) {
			cal = getStartDate(cal, 35);
		} else if (40 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 44) {
			cal = getStartDate(cal, 40);
		} else if (45 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 49) {
			cal = getStartDate(cal, 45);
		} else if (50 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 54) {
			cal = getStartDate(cal, 50);
		} else {
			cal = getStartDate(cal, 55);
		}
		start = cal.getTimeInMillis();
		return start;
	}

	private long calcEnd(Calendar cal) {
		long end;
		cal.setTime(date);
		if (0 <= cal.get(Calendar.MINUTE) && cal.get(Calendar.MINUTE) <= 4) {
			cal = getEndDate(cal, 4);
		} else if (5 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 9) {
			cal = getEndDate(cal, 9);
		} else if (10 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 14) {
			cal = getEndDate(cal, 14);
		} else if (15 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 19) {
			cal = getEndDate(cal, 19);
		} else if (20 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 24) {
			cal = getEndDate(cal, 24);
		} else if (25 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 29) {
			cal = getEndDate(cal, 29);
		} else if (30 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 34) {
			cal = getEndDate(cal, 34);
		} else if (35 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 39) {
			cal = getEndDate(cal, 39);
		} else if (40 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 44) {
			cal = getEndDate(cal, 44);
		} else if (45 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 49) {
			cal = getEndDate(cal, 49);
		} else if (50 <= cal.get(Calendar.MINUTE)
				&& cal.get(Calendar.MINUTE) <= 54) {
			cal = getEndDate(cal, 54);
		} else {
			cal = getEndDate(cal, 59);
		}
		end = cal.getTimeInMillis();
		return end;
	}

	private Calendar getStartDate(Calendar cal, int minute) {
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	private Calendar getEndDate(Calendar cal, int minute) {
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal;
	}

	@Override
	public long getFirstMillisecond() {
		return firstMillisecond;
	}

	@Override
	public long getFirstMillisecond(Calendar cal) {
		long first = this.calcStart(cal);
		return first;
	}

	@Override
	public long getLastMillisecond() {
		return lastMillisecond;
	}

	@Override
	public long getLastMillisecond(Calendar cal) {
		long last = this.calcEnd(cal);
		return last;
	}

	@Override
	public long getSerialIndex() {
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        return hourIndex * (long)(60 / 5) + (long)(this.minute / 5);
	}

	@Override
	public RegularTimePeriod next() {
		cal.setTime(date);
		cal.add(Calendar.MINUTE, 5);
		return new Minute5(cal.getTime());
	}

	@Override
	public void peg(Calendar cal) {
		this.firstMillisecond = getFirstMillisecond(cal);
		this.lastMillisecond = getLastMillisecond(cal);
	}

	@Override
	public RegularTimePeriod previous() {
		cal.setTime(date);
		cal.add(Calendar.MINUTE, -5);
		return new Minute5(cal.getTime());
	}
	
	public int compareTo(Object obj) {
		int result;

		// CASE 1 : 他のMinute5との比較
		// -------------------------------------------
		if (obj instanceof Minute5) {
			Minute5 m5 = (Minute5) obj;
			if (this.getFirstMillisecond() == m5.getFirstMillisecond()
					&& this.getLastMillisecond() == m5.getLastMillisecond()) {
				result = 0;
			} else {
				result = (int) (this.getLastMillisecond() - m5.getLastMillisecond());
			}
		}

		// CASE 2 : 他のTimePeriodオブジェクトとの比較
		// -----------------------------------------------
		else if (obj instanceof RegularTimePeriod) {
			// 難しいので保留
			result = 0;
		}

		// CASE 3 : TimePeriodオブジェクトではないものと比較
		// ---------------------------------------------
		else {
			// time period は他の一般的なオブジェクトの後にくると考える
			result = 1;
		}

		return result;
	}
}
