package dao;

import java.util.List;

public interface ICRUD<T> {
    boolean add(T obj);
    boolean update(T obj);
    boolean delete(int id);
    T getById(int id);
    List<T> getAll();
}
