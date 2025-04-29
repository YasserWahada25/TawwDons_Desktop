package org.example.event.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface Iparticipant <T>{

    void ajouter(T t) throws SQLException, IOException;
    void supprimer(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    List<T> getList() throws SQLException;
}
