package dao;

import utils.MyDataBase;

import java.sql.Connection;

public abstract class BaseDAO {
    protected Connection connection;

    public BaseDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }
}
