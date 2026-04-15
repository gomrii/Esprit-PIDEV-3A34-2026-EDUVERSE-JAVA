package Interfaces;

import Entities.Cours;

import java.sql.SQLException;
import java.util.List;

public interface Iservice <T> {


    void add(T t )throws SQLException;
    void delete(T t)throws  SQLException;
    void update (T t)throws  SQLException;
    List<T> display() throws SQLException;
}
