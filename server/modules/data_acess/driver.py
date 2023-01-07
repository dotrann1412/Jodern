import datetime
import pyodbc
import platform
import sys, os


from modules.helpers import config

class Connector:
    __connection = None
    __lastUsed = None
            
    def establishConnection():        
        if not Connector.__connection or datetime.datetime.now() - Connector.__lastUsed > datetime.timedelta(minutes = 15):
            Connector.__connection = pyodbc.connect(
                driver = '{ODBC Driver 17 for SQL Server}' if platform.system() == 'Linux' else '{SQL Server}',
                server=f'{config.Config.getValue("sql-server-host")},{config.Config.getValue("sql-server-port")}',
                database=f'{config.Config.getValue("dbname")}',
                uid=f'{config.Config.getValue("sql-server-username")}',
                pwd=f'{config.Config.getValue("sql-server-password")}',
                mars_connection='yes',
            )
            Connector.__lastUsed = datetime.datetime.now()
        return Connector.__connection
    
    __backgroudConnection = None
    __backgroundLastTimeEstablished = None
    def establishBackgroundConnection():
        if not Connector.__backgroudConnection or datetime.datetime.now() - Connector.__backgroundLastTimeEstablished > datetime.timedelta(minutes = 15):
            Connector.__backgroudConnection = pyodbc.connect(
                driver = '{ODBC Driver 17 for SQL Server}' if platform.system() == 'Linux' else '{SQL Server}',
                server=f'{config.Config.getValue("sql-server-host")},{config.Config.getValue("sql-server-port")}',
                database=f'{config.Config.getValue("dbname")}',
                uid=f'{config.Config.getValue("sql-server-username")}',
                pwd=f'{config.Config.getValue("sql-server-password")}',
                mars_connection='yes',
            )
            Connector.__backgroundLastTimeEstablished = datetime.datetime.now()
        return Connector.__backgroudConnection