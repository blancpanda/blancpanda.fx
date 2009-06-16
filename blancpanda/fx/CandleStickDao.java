package blancpanda.fx;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;

import blancpanda.AbstractGenericDao;

public class CandleStickDao extends AbstractGenericDao<CandleStick, String> {

	public CandleStickDao() {
		super(CandleStick.class);
	}

	@SuppressWarnings("unchecked")
	public List<CandleStick> getRecentList(int period) {
        //HQL文
        String hql = "from CandleStick cs where cs.period_cd = :period_cd order by cs.time desc";
        Query query = getSession().createQuery(hql);

        //条件
        query.setParameter("period_cd", period, Hibernate.INTEGER);
        query.setMaxResults(150);

        //クエリ発行
        return query.list();
    }
}
