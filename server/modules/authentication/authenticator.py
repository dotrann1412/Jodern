from modules.data_acess.driver import Connector
from modules.helpers.config import Config
import platform

try:
    import pyjwt
except:
    import jwt as pyjwt

import datetime

class Verifier:
    def decode(token):
        return pyjwt.decode(token, Config.getValue('app-secret-key'), algorithms=['HS256'])

    def safeDecode(token):
        try: return Verifier.decode(token)
        except: pass
        return {}

    def verify(**kwargs):
        try: 
            decodedPayload = pyjwt.decode(kwargs['token'], 'secret', algorithms=['HS256'])
            if decodedPayload['exp'] < datetime.datetime.now().timestamp():
                return False
        except: return False
        return True

    def generateToken(**kwargs):
        return pyjwt.encode(kwargs['payload'], 'secret', algorithm='HS256')        

class FacebookAuthenticator:
    def Login(**kwargs):
        userid = kwargs['userid']
        token = kwargs['token']
        
        if not FacebookAuthenticator.verify(userid, token):
            return {
                "error": "invalid token"
            }
        
        cursor = Connector.establishConnection().cursor()
        query = "select * from users where userid = ?"
        cursor.execute(query, userid)
        row = cursor.fetchone()
        if not row:
            query = "insert into users (userid, token) values (?, ?)"
            cursor.execute(query, userid, token)
            Connector.establishConnection().commit()

        return {
            "message": "Chào mừng đến với Jodern!",
            "auth": Verifier.generateToken(
                payload = {
                    "userid": userid,
                    "exp": (datetime.datetime.now() + datetime.timedelta(minutes = Config.getValue("exp"))).timestamp()
                }
            )
        }

    #@todo: implement this
    def verify(userid, token):
        return True

class GoogleAuthenticator:
    def Login(**kwargs):
        pass
    
    def verify(**kwargs):
        pass