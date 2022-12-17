import datetime
import pyodbc
import platform
import sys, os


from modules.helpers import config

class Connector:
    __connection = None
    __lastUsed = None
    
    def establishConnection():
        if Connector.__connection == None or datetime.datetime.now() - Connector.__lastUsed > datetime.timedelta(minutes = 15):
            Connector.__connection = pyodbc.connect(
                driver = '{ODBC Driver 17 for SQL Server}' if platform.system() == 'Linux' else '{SQL Server}',
                server=f'{config.Config.getValue("sql-server-host")},{config.Config.getValue("sql-server-port")}',
                database=f'{config.Config.getValue("dbname")}',
                uid=f'{config.Config.getValue("sql-server-username")}',
                pwd=f'{config.Config.getValue("sql-server-password")}',
            )
            Connector.__lastUsed = datetime.datetime.now()
        return Connector.__connection