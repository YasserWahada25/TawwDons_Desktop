package interfaces;

import java.util.List;

public interface CrudArticle<T> {
    void create(T t);
    void update(T t);
    void delete(int id);
    List<T> getAll();
}
