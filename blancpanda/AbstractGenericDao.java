package blancpanda;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;

public abstract class AbstractGenericDao<T, PK extends Serializable> implements
		IGenericDao<T, PK> {
	private Class<T> type;

	public AbstractGenericDao(Class<T> type) {
		this.type = type;
	}

    private Session session;
	public Session getSession() {
        session = HibernateUtils.currentSession();
        return session;
    }

	public void delete(T o) {
		getSession().delete(o);
	}

	@SuppressWarnings("unchecked")
	public T get(PK id) {
		return (T) getSession().get(type, id);
	}

	@SuppressWarnings("unchecked")
	public List<T> getAll() {
		return getSession().createCriteria(type).list();
	}

	@SuppressWarnings("unchecked")
	public PK save(T o) {
		return (PK) getSession().save(o);
	}

	public void saveOrUpdate(T o) {
		getSession().saveOrUpdate(o);

	}

	public void update(T o) {
		getSession().update(o);

	}
}
