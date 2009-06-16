package blancpanda;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T, PK extends Serializable> {
	public T get(PK id);

	public List<T> getAll();

	public PK save(T o);

	public void update(T o);

	public void saveOrUpdate(T o);

	public void delete(T o);
}
