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
            decodedPayload = pyjwt.decode(kwargs['token'], Config.getValue('app-secret-key'), algorithms=['HS256'])
            if decodedPayload['exp'] < datetime.datetime.now().timestamp():
                return False
        except: return False
        return True

    def generateToken(**kwargs):
        return pyjwt.encode(kwargs['payload'], Config.getValue('app-secret-key'), algorithm='HS256')        

class FacebookAuthenticator:
    def Login(**kwargs):
        userid = kwargs.get("userid", "Unknown")
        fullname = kwargs.get("fullname", "Unknown")
        email = kwargs.get("email", "Unknown")
        avt = kwargs.get("avt", "Unknown")
        phone = kwargs.get("phone", "Unknown")
        token = kwargs.get("token", "Unknown")


        if not FacebookAuthenticator.verify(userid, token):
            return {
                "error": "invalid token"
            }
        
        cursor = Connector.establishConnection().cursor()
        query = "select * from users where userid = ?"
        cursor.execute(query, userid)
        row = cursor.fetchone()
        if not row:
            query = "insert into users (userid, fullname, email, Phone, Address, avatar) values (?, ?, ?, ?, ?, ?)"
            cursor.execute(query, (userid, fullname, email, phone, "", avt))
            cursor.commit()

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